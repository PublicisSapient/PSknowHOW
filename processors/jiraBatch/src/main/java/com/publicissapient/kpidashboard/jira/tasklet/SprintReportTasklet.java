package com.publicissapient.kpidashboard.jira.tasklet;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.jira.config.FetchProjectConfiguration;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.service.FetchSprintReport;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@StepScope
public class SprintReportTasklet implements Tasklet {

    @Autowired
    FetchProjectConfiguration fetchProjectConfiguration;

    @Autowired
    private FetchSprintReport fetchSprintReport;

    @Autowired
    private SprintRepository sprintRepository;

    private String sprintId;

    @Autowired
    public SprintReportTasklet(@Value("#{jobParameters['sprintId']}") String sprintId) {
        this.sprintId = sprintId;
    }

    @Override
    public RepeatStatus execute(StepContribution sc, ChunkContext cc) throws Exception {
        log.info("**** Sprint report started * * *");
        try {
            ProjectConfFieldMapping projConfFieldMapping = fetchProjectConfiguration.fetchConfigurationBasedOnSprintId(sprintId);
            KerberosClient krb5Client = null;
            Set<SprintDetails> setForCacheClean = new HashSet<>();
            SprintDetails sprintDetails = sprintRepository.findBySprintID(sprintId);
            List<String> originalBoardIds = sprintDetails.getOriginBoardId();
            Set<SprintDetails> setOfSprintDetails=null;
            for (String boardId : originalBoardIds) {
                List<SprintDetails> sprintDetailsList = fetchSprintReport.getSprints(projConfFieldMapping, boardId, krb5Client);
                if (CollectionUtils.isNotEmpty(sprintDetailsList)) {
                    // filtering the sprint need to update
                    Set<SprintDetails> sprintDetailSet = sprintDetailsList.stream()
                            .filter(s -> s.getSprintID().equalsIgnoreCase(sprintId)).collect(Collectors.toSet());
                    setOfSprintDetails = fetchSprintReport
                            .fetchSprints(projConfFieldMapping, sprintDetailSet, setForCacheClean, krb5Client);
                }
            }
            sprintRepository.saveAll(setOfSprintDetails);
        } catch (Exception e) {
            log.error("Exception while fetching sprint data", e);
        }
        log.info("**** Sprint report ended * * *");
        return RepeatStatus.FINISHED;
    }

}
