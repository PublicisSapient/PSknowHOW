package com.publicissapient.kpidashboard.jira.fetchData;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilter;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.KanbanAccountHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.common.repository.application.KanbanAccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueRepository;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelService;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CreateKanbanAccountHierarchyImpl implements CreateKanbanAccountHierarchy{

    @Autowired
    private KanbanAccountHierarchyRepository kanbanAccountHierarchyRepo;

    @Autowired
    private HierarchyLevelService hierarchyLevelService;

    @Override
    public Set<KanbanAccountHierarchy> createKanbanAccountHierarchy(List<KanbanJiraIssue> jiraIssueList,
                                           ProjectConfFieldMapping projectConfig) {

        List<HierarchyLevel> hierarchyLevelList = hierarchyLevelService
                .getFullHierarchyLevels(projectConfig.isKanban());
        Map<String, HierarchyLevel> hierarchyLevelsMap = hierarchyLevelList.stream()
                .collect(Collectors.toMap(HierarchyLevel::getHierarchyLevelId, x -> x));
        HierarchyLevel projectHierarchyLevel = hierarchyLevelsMap.get(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT);

        Map<Pair<String, String>, KanbanAccountHierarchy> existingKanbanHierarchy = getKanbanAccountHierarchy();
        Set<KanbanAccountHierarchy> accHierarchyToSave = new HashSet<>();

        for (KanbanJiraIssue kanbanJiraIssue : jiraIssueList) {
            if (StringUtils.isNotBlank(kanbanJiraIssue.getProjectName())) {
                KanbanAccountHierarchy projectHierarchy = kanbanAccountHierarchyRepo
                        .findByLabelNameAndBasicProjectConfigId(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT,
                                new ObjectId(kanbanJiraIssue.getBasicProjectConfigId()))
                        .get(0);

                List<KanbanAccountHierarchy> additionalFiltersHierarchies = accountHierarchiesForAdditionalFilters(
                        kanbanJiraIssue, projectHierarchy, projectHierarchyLevel, hierarchyLevelList);

                additionalFiltersHierarchies.forEach(accountHierarchy -> accHierarchyToSave(accountHierarchy,
                        existingKanbanHierarchy, accHierarchyToSave));

            }
        }
//        if (CollectionUtils.isNotEmpty(accHierarchyToSave)) {
//            kanbanAccountHierarchyRepo.saveAll(accHierarchyToSave);
//        }

        return accHierarchyToSave;
    }

    private List<KanbanAccountHierarchy> accountHierarchiesForAdditionalFilters(KanbanJiraIssue jiraIssue,
                                                                                KanbanAccountHierarchy projectHierarchy, HierarchyLevel projectHierarchyLevel,
                                                                                List<HierarchyLevel> hierarchyLevelList) {

        List<KanbanAccountHierarchy> accountHierarchies = new ArrayList<>();
        List<AdditionalFilter> additionalFilters = ListUtils.emptyIfNull(jiraIssue.getAdditionalFilters());

        List<String> additionalFilterCategoryIds = hierarchyLevelList.stream()
                .filter(x -> x.getLevel() > projectHierarchyLevel.getLevel()).map(HierarchyLevel::getHierarchyLevelId)
                .collect(Collectors.toList());

        additionalFilters.forEach(additionalFilter -> {
            if (additionalFilterCategoryIds.contains(additionalFilter.getFilterId())) {
                String labelName = additionalFilter.getFilterId();
                additionalFilter.getFilterValues().forEach(additionalFilterValue -> {
                    KanbanAccountHierarchy adFilterAccountHierarchy = new KanbanAccountHierarchy();
                    adFilterAccountHierarchy.setLabelName(labelName);
                    adFilterAccountHierarchy.setNodeId(additionalFilterValue.getValueId());
                    adFilterAccountHierarchy.setNodeName(additionalFilterValue.getValue());
                    adFilterAccountHierarchy.setParentId(projectHierarchy.getNodeId());
                    adFilterAccountHierarchy.setPath(projectHierarchy.getNodeId()
                            + CommonConstant.ACC_HIERARCHY_PATH_SPLITTER + projectHierarchy.getPath());
                    adFilterAccountHierarchy.setBasicProjectConfigId(new ObjectId(jiraIssue.getBasicProjectConfigId()));
                    accountHierarchies.add(adFilterAccountHierarchy);
                });
            }

        });

        return accountHierarchies;
    }

    private void accHierarchyToSave(KanbanAccountHierarchy accountHierarchy,
                                    Map<Pair<String, String>, KanbanAccountHierarchy> existingKanbanHierarchy,
                                    Set<KanbanAccountHierarchy> accHierarchyToSave) {
        if (StringUtils.isNotBlank(accountHierarchy.getParentId())
                || (StringUtils.isBlank(accountHierarchy.getParentId()))) {
            KanbanAccountHierarchy exHiery = existingKanbanHierarchy
                    .get(Pair.of(accountHierarchy.getNodeId(), accountHierarchy.getPath()));

            if (null == exHiery) {
                accountHierarchy.setCreatedDate(LocalDateTime.now());
                accHierarchyToSave.add(accountHierarchy);
            }
        }
    }

    /**
     * Fetches all saved kanban account hierarchy.
     *
     * @return Map<Pair < String, String>, KanbanAccountHierarchy>
     */
    private Map<Pair<String, String>, KanbanAccountHierarchy> getKanbanAccountHierarchy() {
        List<KanbanAccountHierarchy> accountHierarchyList = kanbanAccountHierarchyRepo.findAll();
        return accountHierarchyList.stream()
                .collect(Collectors.toMap(p -> Pair.of(p.getNodeId(), p.getPath()), p -> p));
    }



}
