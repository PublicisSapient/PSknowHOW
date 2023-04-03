package com.publicissapient.kpidashboard.jira.fetchData;

import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class SaveData {

    @Autowired
    private AccountHierarchyRepository accountHierarchyRepository;

    @Autowired
    private SprintRepository sprintRepository;

    @Autowired
    private JiraIssueRepository jiraIssueRepository;

    @Autowired
    private JiraCommonService jiraCommonService;

    @Autowired
    private JiraProcessorConfig jiraProcessorConfig;

    @Autowired
    private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

    public void saveData(List<JiraIssue> jiraIssuesToSave, List<JiraIssueCustomHistory> jiraIssueHistoryToSave, List<SprintDetails> sprintDetailsToSave, Set<AccountHierarchy> accountHierarchiesToSave, Set<SprintDetails> setForCacheClean, ProjectConfFieldMapping projectConfig){

        boolean dataExist = (jiraIssueRepository
                .findTopByBasicProjectConfigId(projectConfig.getBasicProjectConfigId().toString()) != null);

        int sprintCount = jiraProcessorConfig.getSprintCountForCacheClean();
        boolean latestDataFetched=false;

        if(CollectionUtils.isNotEmpty(jiraIssuesToSave) && CollectionUtils.isNotEmpty(jiraIssueHistoryToSave)) {
            log.info("Saving jira issue and jira history");
            jiraIssueRepository.saveAll(jiraIssuesToSave);
            jiraIssueCustomHistoryRepository.saveAll(jiraIssueHistoryToSave);
        }

        if (CollectionUtils.isNotEmpty(accountHierarchiesToSave)) {
            log.info("Saving jira account hierarchies");
            accountHierarchyRepository.saveAll(accountHierarchiesToSave);

            if (!dataExist && !latestDataFetched && setForCacheClean.size() > sprintCount) {
                latestDataFetched = jiraCommonService.cleanCache();
                setForCacheClean.clear();
                log.info("latest sprint fetched cache cleaned.");
            }

        }

        if(CollectionUtils.isNotEmpty(sprintDetailsToSave)){
            sprintRepository.saveAll(sprintDetailsToSave);
        }

    }

}
