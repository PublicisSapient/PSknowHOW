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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.publicissapient.kpidashboard.rally.aspect.TrackExecutionTime;
import com.publicissapient.kpidashboard.rally.config.FetchProjectConfiguration;
import com.publicissapient.kpidashboard.rally.config.RallyProcessorConfig;
import com.publicissapient.kpidashboard.rally.helper.ReaderRetryHelper;
import com.publicissapient.kpidashboard.rally.model.HierarchicalRequirement;
import com.publicissapient.kpidashboard.rally.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.rally.model.ReadData;
import com.publicissapient.kpidashboard.rally.service.FetchIssueSprint;
import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * @author purgupta2
 */
@Slf4j
@Component
@StepScope
public class IssueSprintReader implements ItemReader<ReadData> {

	@Autowired
	FetchProjectConfiguration fetchProjectConfiguration;

	@Autowired
	RallyProcessorConfig rallyProcessorConfig;

	@Autowired
	FetchIssueSprint fetchIssueSprint;
	int pageSize = 50;
	int pageNumber = 0;
	List<HierarchicalRequirement> hierarchicalRequirements = new ArrayList<>();
	int issueSize = 0;
	private Iterator<HierarchicalRequirement> issueIterator;
	ProjectConfFieldMapping projectConfFieldMapping;

	@Value("#{jobParameters['sprintId']}")
	private String sprintId;

	private ReaderRetryHelper retryHelper;

	@Value("#{jobParameters['processorId']}")
	private String processorId;

	public void initializeReader(String sprintId) {
		log.info("**** Jira Issue fetch started * * *");
		pageSize = rallyProcessorConfig.getPageSize();
		projectConfFieldMapping = fetchProjectConfiguration.fetchConfigurationBasedOnSprintId(sprintId);
		retryHelper = new ReaderRetryHelper();
	}

	@Override
	public ReadData read() throws Exception {

		if (null == projectConfFieldMapping) {
			log.info("Gathering data for batch - Scrum projects with JQL configuration");
			initializeReader(sprintId);
		}
		ReadData readData = null;
		if (null != projectConfFieldMapping) {
			if (null == issueIterator) {
				pageNumber = 0;
				fetchIssues();
			}

			if (null != issueIterator && !issueIterator.hasNext()) {
				fetchIssues();
			}

			if (null != issueIterator && issueIterator.hasNext()) {
				HierarchicalRequirement issue = issueIterator.next();
				readData = new ReadData();
				readData.setHierarchicalRequirement(issue);
				readData.setProjectConfFieldMapping(projectConfFieldMapping);
				readData.setSprintFetch(true);
				readData.setProcessorId(new ObjectId(processorId));
			}

			if (null == issueIterator || (!issueIterator.hasNext() && issueSize < pageSize)) {
				log.info("Data has been fetched for the project : {}", projectConfFieldMapping.getProjectName());
				readData = null;
			}
		}

		return readData;
	}

	@TrackExecutionTime
	private void fetchIssues() throws Exception {
		ReaderRetryHelper.RetryableOperation<Void> retryableOperation = () -> {
			log.info("Reading issues for project : {}, page No : {}", projectConfFieldMapping.getProjectName(),
					pageNumber / pageSize);
			hierarchicalRequirements = fetchIssueSprint.fetchIssuesSprintBasedOnJql(projectConfFieldMapping, pageNumber, sprintId);
			issueSize = hierarchicalRequirements.size();
			pageNumber += pageSize;
			if (CollectionUtils.isNotEmpty(hierarchicalRequirements)) {
				issueIterator = hierarchicalRequirements.iterator();
			}
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
}
