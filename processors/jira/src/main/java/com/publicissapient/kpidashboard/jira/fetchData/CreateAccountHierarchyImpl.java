package com.publicissapient.kpidashboard.jira.fetchData;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilter;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelService;
import com.publicissapient.kpidashboard.jira.client.jiraissue.JiraIssueClientUtil;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.util.AdditionalFilterHelper;
import com.publicissapient.kpidashboard.jira.util.JiraConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
public class CreateAccountHierarchyImpl implements CreateAccountHierarchy {

    @Autowired
    private HierarchyLevelService hierarchyLevelService;

    @Autowired
    private AdditionalFilterHelper additionalFilterHelper;

    @Autowired
    private AccountHierarchyRepository accountHierarchyRepository;

    @Override
    public Set<AccountHierarchy> creteAccountHierarchy(List<JiraIssue> jiraIssueList, ProjectConfFieldMapping projectConfig) {

        List<HierarchyLevel> hierarchyLevelList = hierarchyLevelService
                .getFullHierarchyLevels(projectConfig.isKanban());
        Map<String, HierarchyLevel> hierarchyLevelsMap = hierarchyLevelList.stream()
                .collect(Collectors.toMap(HierarchyLevel::getHierarchyLevelId, x -> x));

        HierarchyLevel sprintHierarchyLevel = hierarchyLevelsMap.get(CommonConstant.HIERARCHY_LEVEL_ID_SPRINT);

        Map<Pair<String, String>, AccountHierarchy> existingHierarchy = JiraIssueClientUtil
                .getAccountHierarchy(accountHierarchyRepository);

        Set<AccountHierarchy> setToSave = new HashSet<>();
        for (JiraIssue jiraIssue : jiraIssueList) {
            if (StringUtils.isNotBlank(jiraIssue.getProjectName()) && StringUtils.isNotBlank(jiraIssue.getSprintName())
                    && StringUtils.isNotBlank(jiraIssue.getSprintBeginDate())
                    && StringUtils.isNotBlank(jiraIssue.getSprintEndDate())) {

                AccountHierarchy projectData = accountHierarchyRepository
                        .findByLabelNameAndBasicProjectConfigId(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT,
                                new ObjectId(jiraIssue.getBasicProjectConfigId()))
                        .get(0);

                AccountHierarchy sprintHierarchy = createHierarchyForSprint(jiraIssue,
                        projectConfig.getProjectBasicConfig(), projectData, sprintHierarchyLevel);

                setToSaveAccountHierarchy(setToSave, sprintHierarchy, existingHierarchy);

                List<AccountHierarchy> additionalFiltersHierarchies = accountHierarchiesForAdditionalFilters(jiraIssue,
                        sprintHierarchy, sprintHierarchyLevel, hierarchyLevelList);
                additionalFiltersHierarchies.forEach(
                        accountHierarchy -> setToSaveAccountHierarchy(setToSave, accountHierarchy, existingHierarchy));

            }

        }
//        if (CollectionUtils.isNotEmpty(setToSave)) {
//            accountHierarchyRepository.saveAll(setToSave);
//        }
        return setToSave;
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

    private AccountHierarchy createHierarchyForSprint(JiraIssue jiraIssue, ProjectBasicConfig projectBasicConfig,
                                                      AccountHierarchy projectHierarchy, HierarchyLevel hierarchyLevel) {
        AccountHierarchy accountHierarchy = null;
        try {

            accountHierarchy = new AccountHierarchy();
            accountHierarchy.setBasicProjectConfigId(projectBasicConfig.getId());
            accountHierarchy.setIsDeleted(JiraConstants.FALSE);
            accountHierarchy.setLabelName(hierarchyLevel.getHierarchyLevelId());
            String sprintName = (String) PropertyUtils.getSimpleProperty(jiraIssue, "sprintName");
            String sprintId = (String) PropertyUtils.getSimpleProperty(jiraIssue, "sprintID");

            accountHierarchy.setNodeId(sprintId);
            accountHierarchy.setNodeName(sprintName + JiraConstants.COMBINE_IDS_SYMBOL + jiraIssue.getProjectName());

            accountHierarchy.setBeginDate((String) PropertyUtils.getSimpleProperty(jiraIssue, "sprintBeginDate"));
            accountHierarchy.setEndDate((String) PropertyUtils.getSimpleProperty(jiraIssue, "sprintEndDate"));
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
                                                                          List<HierarchyLevel> hierarchyLevelList) {

        List<AccountHierarchy> accountHierarchies = new ArrayList<>();
        List<AdditionalFilter> additionalFilters = ListUtils.emptyIfNull(jiraIssue.getAdditionalFilters());

        List<String> additionalFilterCategoryIds = hierarchyLevelList.stream()
                .filter(x -> x.getLevel() > sprintHierarchyLevel.getLevel()).map(HierarchyLevel::getHierarchyLevelId)
                .collect(Collectors.toList());

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
