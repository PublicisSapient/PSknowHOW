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
package com.publicissapient.kpidashboard.rally.reader;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.publicissapient.kpidashboard.rally.aspect.TrackExecutionTime;
import com.publicissapient.kpidashboard.rally.config.FetchProjectConfiguration;
import com.publicissapient.kpidashboard.rally.config.RallyProcessorConfig;
import com.publicissapient.kpidashboard.rally.constant.RallyConstants;
import com.publicissapient.kpidashboard.rally.helper.ReaderRetryHelper;
import com.publicissapient.kpidashboard.rally.model.HierarchicalRequirement;
import com.publicissapient.kpidashboard.rally.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.rally.model.ReadData;
import com.publicissapient.kpidashboard.rally.service.RallyCommonService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.bson.types.ObjectId;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.util.StringUtils;

/**
 * @author pankumar8
 */
@Slf4j
@Component
@StepScope
public class IssueRqlReader implements ItemReader<ReadData> {

	@Autowired
	FetchProjectConfiguration fetchProjectConfiguration;

	@Autowired
	RallyCommonService rallyCommonService;

	@Autowired
	RallyProcessorConfig rallyProcessorConfig;

	int pageSize = 50;
	int pageNumber = 0;
	List<HierarchicalRequirement> hierarchicalRequirements = new ArrayList<>();
	Map<String, String> projectWiseDeltaDate;
	int issueSize = 0;
	boolean fetchLastIssue;
	@Autowired
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepo;
	private Iterator<HierarchicalRequirement> hierarchicalRequirementIterator;
	ProjectConfFieldMapping projectConfFieldMapping;
	private ReaderRetryHelper retryHelper;

	@Value("#{jobParameters['projectId']}")
	private String projectId;

	@Value("#{jobParameters['processorId']}")
	private String processorId;

	public void initializeReader(String projectId) {
		log.info("**** Rally Issue fetch started * * *");
		pageSize = rallyProcessorConfig.getPageSize();
		projectConfFieldMapping = fetchProjectConfiguration.fetchConfiguration(projectId);
		retryHelper = new ReaderRetryHelper();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.batch.item.ItemReader#read()
	 */
	@Override
	public ReadData read() throws Exception {

		if (null == projectConfFieldMapping) {
			log.info("Gathering data for batch - Scrum projects with JQL configuration for the project : {} ", projectId);
			initializeReader(projectId);
		}
		ReadData readData = null;
		//if (null != projectConfFieldMapping && !fetchLastIssue) {
			if (hierarchicalRequirementIterator == null || !hierarchicalRequirementIterator.hasNext()) {
				fetchIssues();
				if (CollectionUtils.isNotEmpty(hierarchicalRequirements)) {
					hierarchicalRequirementIterator = hierarchicalRequirements.iterator();
				}
			}

			if (checkIssueIterator()) {
				HierarchicalRequirement hierarchicalRequirement = hierarchicalRequirementIterator.next();
				readData = new ReadData();
				readData.setHierarchicalRequirement(hierarchicalRequirement);
				readData.setProjectConfFieldMapping(projectConfFieldMapping);
				readData.setSprintFetch(false);
				readData.setProcessorId(new ObjectId(processorId));
			}

			if (null == hierarchicalRequirementIterator || (!hierarchicalRequirementIterator.hasNext() && issueSize < pageSize)) {
				log.info("Data has been fetched for the project : {}", projectConfFieldMapping.getProjectName());
				fetchLastIssue = true;
				return readData;
			}
		//}

		return readData;
	}

	private boolean checkIssueIterator() {
		return null != hierarchicalRequirementIterator && hierarchicalRequirementIterator.hasNext();
	}

	@TrackExecutionTime
	private void fetchIssues() throws Exception {

		ReaderRetryHelper.RetryableOperation<Void> retryableOperation = () -> {
			log.info("Reading issues for project : {}, page No : {}", projectConfFieldMapping.getProjectName(),
					pageNumber / pageSize);
			String deltaDate = getDeltaDateFromTraceLog();
			hierarchicalRequirements = rallyCommonService.fetchIssuesBasedOnJql(projectConfFieldMapping, pageNumber, deltaDate);
			issueSize = hierarchicalRequirements.size();
			pageNumber += pageSize;
			return null;
		};

		try {
			retryHelper.executeWithRetry(retryableOperation);
		} catch (Exception e) {
			log.error("Exception while fetching issues for project: {}, page No: {}",
					projectConfFieldMapping.getProjectName(), pageNumber / pageSize);
			log.error("All retries attempts are failed");
			throw e;
		}
	}

	private String getDeltaDateFromTraceLog() {
		String deltaDate = DateUtil.dateTimeFormatter(
				LocalDateTime.now().minusMonths(rallyProcessorConfig.getPrevMonthCountToFetchData()),
				RallyConstants.QUERYDATEFORMAT);
		if (MapUtils.isEmpty(projectWiseDeltaDate) ||
				StringUtils.isBlank(projectWiseDeltaDate.get(projectConfFieldMapping.getBasicProjectConfigId().toString()))) {
			log.info("fetching project status from trace log for project: {}", projectConfFieldMapping.getProjectName());
			List<ProcessorExecutionTraceLog> procExecTraceLogs = processorExecutionTraceLogRepo
					.findByProcessorNameAndBasicProjectConfigIdAndProgressStatsFalse(RallyConstants.RALLY,
							projectConfFieldMapping.getBasicProjectConfigId().toString());
			if (CollectionUtils.isNotEmpty(procExecTraceLogs)) {
				String lastSuccessfulRun = deltaDate;
				for (ProcessorExecutionTraceLog processorExecutionTraceLog : procExecTraceLogs) {
					lastSuccessfulRun = processorExecutionTraceLog.getLastSuccessfulRun();
				}
				log.info("project: {}  found in trace log. Data will be fetched from one day before {}",
						projectConfFieldMapping.getProjectName(), lastSuccessfulRun);
				projectWiseDeltaDate = new HashMap<>();
				projectWiseDeltaDate.put(projectConfFieldMapping.getBasicProjectConfigId().toString(), lastSuccessfulRun);
			} else {
				log.info("project: {} not found in trace log so data will be fetched from beginning",
						projectConfFieldMapping.getProjectName());
				projectWiseDeltaDate = new HashMap<>();
				projectWiseDeltaDate.put(projectConfFieldMapping.getBasicProjectConfigId().toString(), deltaDate);
			}
		}
		if (MapUtils.isNotEmpty(projectWiseDeltaDate) &&
				!StringUtils.isBlank(projectWiseDeltaDate.get(projectConfFieldMapping.getBasicProjectConfigId().toString()))) {
			deltaDate = projectWiseDeltaDate.get(projectConfFieldMapping.getBasicProjectConfigId().toString());
		}

		return deltaDate;
	}
}
