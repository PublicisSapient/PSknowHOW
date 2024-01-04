/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package com.publicissapient.kpidashboard.jira.tasklet;

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
import com.publicissapient.kpidashboard.jira.aspect.TrackExecutionTime;
import com.publicissapient.kpidashboard.jira.config.FetchProjectConfiguration;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.service.FetchSprintReport;

import lombok.extern.slf4j.Slf4j;

/**
 * @author purgupta2
 */
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

	/**
	 * @param sc
	 *            StepContribution
	 * @param cc
	 *            ChunkContext
	 * @return RepeatStatus
	 * @throws Exception
	 *             Exception
	 */
	@TrackExecutionTime
	@Override
	public RepeatStatus execute(StepContribution sc, ChunkContext cc) throws Exception {
		log.info("Sprint report job started for the sprint : {}", sprintId);
		ProjectConfFieldMapping projConfFieldMapping = fetchProjectConfiguration
				.fetchConfigurationBasedOnSprintId(sprintId);
		KerberosClient krb5Client = null;
		SprintDetails sprintDetails = sprintRepository.findBySprintID(sprintId);
		List<String> originalBoardIds = sprintDetails.getOriginBoardId();
		for (String boardId : originalBoardIds) {
			List<SprintDetails> sprintDetailsList = fetchSprintReport.getSprints(projConfFieldMapping, boardId,
					krb5Client);
			if (CollectionUtils.isNotEmpty(sprintDetailsList)) {
				// filtering the sprint need to update
				Set<SprintDetails> sprintDetailSet = sprintDetailsList.stream()
						.filter(s -> s.getSprintID().equalsIgnoreCase(sprintId)).collect(Collectors.toSet());
				Set<SprintDetails> setOfSprintDetails = fetchSprintReport.fetchSprints(projConfFieldMapping,
						sprintDetailSet, krb5Client, true);
				sprintRepository.saveAll(setOfSprintDetails);
			}
		}

		return RepeatStatus.FINISHED;
	}

}
