package com.publicissapient.kpidashboard.jira.fetchData;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilter;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelService;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.util.JiraConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Slf4j
@Service
public class CreateAccountHierarchyImpl implements CreateAccountHierarchy {

    @Autowired
    private HierarchyLevelService hierarchyLevelService;

    @Autowired
    private AccountHierarchyRepository accountHierarchyRepository;

    @Override
    public Set<AccountHierarchy> createAccountHierarchy(List<JiraIssue> jiraIssueList, ProjectConfFieldMapping projectConfig, Set<SprintDetails> sprintDetailsSet) {

        List<HierarchyLevel> hierarchyLevelList = hierarchyLevelService
                .getFullHierarchyLevels(projectConfig.isKanban());
        Map<String, HierarchyLevel> hierarchyLevelsMap = hierarchyLevelList.stream()
                .collect(Collectors.toMap(HierarchyLevel::getHierarchyLevelId, Function.identity()));

        HierarchyLevel sprintHierarchyLevel = hierarchyLevelsMap.get(CommonConstant.HIERARCHY_LEVEL_ID_SPRINT);

        Map<Pair<String, String>, AccountHierarchy> existingHierarchy = getAccountHierarchy(accountHierarchyRepository);

        List<String> additionalFilterCategoryIds = hierarchyLevelList.stream()
                .filter(x -> x.getLevel() > sprintHierarchyLevel.getLevel()).map(HierarchyLevel::getHierarchyLevelId)
                .collect(Collectors.toList());
        Set<AccountHierarchy> setToSave = new HashSet<>();
        Map<ObjectId, AccountHierarchy> projectDataMap = new HashMap<>();
        for (JiraIssue jiraIssue : jiraIssueList) {
            ObjectId basicProjectConfigId = new ObjectId(jiraIssue.getBasicProjectConfigId());
            Map<String, SprintDetails> sprintDetailsMap = sprintDetailsSet.stream()
                    .filter(sprintDetails -> sprintDetails.getBasicProjectConfigId().equals(basicProjectConfigId))
                    .collect(Collectors.toMap(sprintDetails -> sprintDetails.getSprintID().split("_")[0],
                            sprintDetails -> sprintDetails));
            AccountHierarchy projectData = projectDataMap.computeIfAbsent(basicProjectConfigId, id -> {
                List<AccountHierarchy> projectDataList = accountHierarchyRepository
                        .findByLabelNameAndBasicProjectConfigId(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT, id);
                return projectDataList.isEmpty() ? null : projectDataList.get(0);
            });

            for (String sprintId : jiraIssue.getSprintIdList()) {
                SprintDetails sprintDetails = sprintDetailsMap.get(sprintId);
                if (sprintDetails != null) {
                    AccountHierarchy sprintHierarchy = createHierarchyForSprint(sprintDetails,
                            projectConfig.getProjectBasicConfig(), projectData, sprintHierarchyLevel);

                    setToSaveAccountHierarchy(setToSave, sprintHierarchy, existingHierarchy);

                    List<AccountHierarchy> additionalFiltersHierarchies = accountHierarchiesForAdditionalFilters(
                            jiraIssue, sprintHierarchy, sprintHierarchyLevel, hierarchyLevelList, additionalFilterCategoryIds);
                    additionalFiltersHierarchies.forEach(accountHierarchy -> setToSaveAccountHierarchy(setToSave,
                            accountHierarchy, existingHierarchy));
                }

            }

        }
        log.info("Created Account Hierarchy {}",setToSave);
//        if (!setToSave.isEmpty()) {
//            accountHierarchyRepository.saveAll(setToSave);
//        }
        return setToSave;
    }

    private static Map<Pair<String, String>, AccountHierarchy> getAccountHierarchy(AccountHierarchyRepository accountHierarchyRepository) {
        List<AccountHierarchy> accountHierarchyList = accountHierarchyRepository.findAll();
        return accountHierarchyList.stream()
                .collect(Collectors.toMap(p -> Pair.of(p.getNodeId(), p.getPath()), p -> p));

    }

    private void setToSaveAccountHierarchy(Set<AccountHierarchy> setToSave, AccountHierarchy accountHierarchy,
                                           Map<Pair<String, String>, AccountHierarchy> existingHierarchy) {
        if (StringUtils.isNotBlank(accountHierarchy.getParentId())) {
            AccountHierarchy exHiery = existingHierarchy
                    .get(Pair.of(accountHierarchy.getNodeId(), accountHierarchy.getPath()));

            if (null == exHiery) {
                accountHierarchy.setCreatedDate(LocalDateTime.now());
                setToSave.add(accountHierarchy);
            }
        }
    }

    private AccountHierarchy createHierarchyForSprint(SprintDetails sprintDetails,
                                                      ProjectBasicConfig projectBasicConfig, AccountHierarchy projectHierarchy, HierarchyLevel hierarchyLevel) {
        AccountHierarchy accountHierarchy = null;
        try {

            accountHierarchy = new AccountHierarchy();
            accountHierarchy.setBasicProjectConfigId(projectBasicConfig.getId());
            accountHierarchy.setIsDeleted(JiraConstants.FALSE);
            accountHierarchy.setLabelName(hierarchyLevel.getHierarchyLevelId());
            String sprintName = (String) PropertyUtils.getSimpleProperty(sprintDetails, "sprintName");
            String sprintId = (String) PropertyUtils.getSimpleProperty(sprintDetails, "sprintID");

            accountHierarchy.setNodeId(sprintId);
            accountHierarchy
                    .setNodeName(sprintName + JiraConstants.COMBINE_IDS_SYMBOL + projectBasicConfig.getProjectName());

            accountHierarchy.setPath(new StringBuffer(56).append(projectHierarchy.getNodeId())
                    .append(CommonConstant.ACC_HIERARCHY_PATH_SPLITTER).append(projectHierarchy.getPath()).toString());
            accountHierarchy.setParentId(projectHierarchy.getNodeId());

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error("Jira Processor Failed to get Account Hierarchy data {}", e);
        }
        return accountHierarchy;
    }

    private List<AccountHierarchy> accountHierarchiesForAdditionalFilters(JiraIssue jiraIssue,
                                                                          AccountHierarchy sprintHierarchy, HierarchyLevel sprintHierarchyLevel,
                                                                          List<HierarchyLevel> hierarchyLevelList, List<String> additionalFilterCategoryIds) {

        List<AccountHierarchy> accountHierarchies = new ArrayList<>();
        List<AdditionalFilter> additionalFilters = ListUtils.emptyIfNull(jiraIssue.getAdditionalFilters());

        additionalFilters.forEach(additionalFilter -> {
            if (additionalFilterCategoryIds.contains(additionalFilter.getFilterId())) {
                String labelName = additionalFilter.getFilterId();
                additionalFilter.getFilterValues().forEach(additionalFilterValue -> {
                    AccountHierarchy adFilterAccountHierarchy = new AccountHierarchy();
                    adFilterAccountHierarchy.setLabelName(labelName);
                    adFilterAccountHierarchy.setNodeId(additionalFilterValue.getValueId());
                    adFilterAccountHierarchy.setNodeName(additionalFilterValue.getValue());
                    adFilterAccountHierarchy.setParentId(sprintHierarchy.getNodeId());
                    adFilterAccountHierarchy.setPath(sprintHierarchy.getNodeId()
                            + CommonConstant.ACC_HIERARCHY_PATH_SPLITTER + sprintHierarchy.getPath());
                    adFilterAccountHierarchy.setBasicProjectConfigId(new ObjectId(jiraIssue.getBasicProjectConfigId()));
                    accountHierarchies.add(adFilterAccountHierarchy);
                });
            }

        });

        return accountHierarchies;
    }

}
