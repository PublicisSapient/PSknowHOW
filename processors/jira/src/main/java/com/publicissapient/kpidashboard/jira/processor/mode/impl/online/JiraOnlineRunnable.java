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

package com.publicissapient.kpidashboard.jira.processor.mode.impl.online;

import java.util.concurrent.CountDownLatch;

import com.publicissapient.kpidashboard.jira.client.jiraprojectmetadata.JiraIssueMetadata;
import com.publicissapient.kpidashboard.jira.client.sprint.SprintClient;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.KanbanAccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectReleaseRepo;
import com.publicissapient.kpidashboard.common.repository.jira.BoardMetadataRepository;
import com.publicissapient.kpidashboard.common.repository.jira.MetadataIdentifierRepository;
import com.publicissapient.kpidashboard.jira.adapter.JiraAdapter;
import com.publicissapient.kpidashboard.jira.adapter.helper.JiraRestClientFactory;
import com.publicissapient.kpidashboard.jira.client.jiraissue.JiraIssueClient;
import com.publicissapient.kpidashboard.jira.client.jiraissue.JiraIssueClientFactory;
import com.publicissapient.kpidashboard.jira.client.metadata.MetaDataClientImpl;
import com.publicissapient.kpidashboard.jira.client.release.ReleaseDataClientImpl;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

import lombok.extern.slf4j.Slf4j;

/**
 * The Class JiraOnlineRunnable.
 */
@Service

/** The Constant log. */
@Slf4j
public class JiraOnlineRunnable implements Runnable {// NOPMD

	/** The latch. */
	private CountDownLatch latch;

	/** The online lineproject config map. */
	private ProjectConfFieldMapping onlineLineprojectConfigMap;

	/** The jira adapter. */
	private JiraAdapter jiraAdapter;

	/** The project release repo. */
	private ProjectReleaseRepo projectReleaseRepo;

	/** The account hierarchy repository. */
	private AccountHierarchyRepository accountHierarchyRepository;

	/** The kanban account hierarchy repo. */
	private KanbanAccountHierarchyRepository kanbanAccountHierarchyRepo;

	/** The factory. */
	private JiraIssueClientFactory factory;

	/** The jira processor config. */
	private JiraProcessorConfig jiraProcessorConfig;

	/** The metadata repository */
	private BoardMetadataRepository boardMetadataRepository;
	/** The Field mapping repository */
	private FieldMappingRepository fieldMappingRepository;

	private MetadataIdentifierRepository metadataIdentifierRepository;

	private JiraRestClientFactory jiraRestClientFactory;

	private SprintClient sprintClient;


	/**
	 * Run.
	 */
	@Override
	public void run() {

		try {
			long start = System.currentTimeMillis();
			MDC.put("ProjectDataStartTime", String.valueOf(start));
			log.info("START - Jira processing started for project {}",
					onlineLineprojectConfigMap.getProjectName());
			if (jiraProcessorConfig.isFetchMetadata()) {
				collectMetadata(jiraAdapter, onlineLineprojectConfigMap);
			}
			collectSprintReportData(jiraAdapter, onlineLineprojectConfigMap);
			collectJiraIssueData(jiraAdapter, onlineLineprojectConfigMap);
			collectReleaseData(jiraAdapter, onlineLineprojectConfigMap);
			log.info("END - Jira processing finished for project {}",
					onlineLineprojectConfigMap.getBasicProjectConfigId().toString());
			long end = System.currentTimeMillis();
			MDC.put("ProjectDataEndTime", String.valueOf(end));



		} catch (Exception ex){
			log.error(ex.getMessage(), ex);

		} finally {
			latch.countDown();
		}

	}

	/**
	 * Sets the configurations and variables .
	 *
	 * @param latch
	 *            latch
	 * @param jiraAdapter
	 *            JiraAdapter
	 * @param onlineLineprojectConfigMap
	 *            OnlineConfigurationMap
	 * @param projectReleaseRepo
	 *            ProjectReleaseRepo
	 * @param accountHierarchyRepository
	 *            AccountHierarchyRepository
	 * @param kanbanAccountHierarchyRepo
	 *            KanbanAccountHierarchyRepository
	 * @param factory
	 *            JiraIssueClientFactory
	 * @param jiraProcessorConfig
	 *            the jira processor config
	 * @param boardMetadataRepository
	 *            BoardMetadataRepository
	 * @param fieldMappingRepository
	 *            FieldMappingRepository
	 * @param metadataIdentifierRepository
	 *            MetadataIdentifierRepository
	 * @param jiraRestClientFactory
	 *            jiraRestClientFactory
	 */
	public JiraOnlineRunnable(CountDownLatch latch, JiraAdapter jiraAdapter,//NOSONAR
							  ProjectConfFieldMapping onlineLineprojectConfigMap,
							  ProjectReleaseRepo projectReleaseRepo, AccountHierarchyRepository accountHierarchyRepository,
							  KanbanAccountHierarchyRepository kanbanAccountHierarchyRepo, JiraIssueClientFactory factory,
							  JiraProcessorConfig jiraProcessorConfig, BoardMetadataRepository boardMetadataRepository,
							  FieldMappingRepository fieldMappingRepository, MetadataIdentifierRepository metadataIdentifierRepository,
							  JiraRestClientFactory jiraRestClientFactory, SprintClient sprintClient) { // NOPMD
		this.latch = latch;
		this.jiraAdapter = jiraAdapter;
		this.onlineLineprojectConfigMap = onlineLineprojectConfigMap;
		this.projectReleaseRepo = projectReleaseRepo;
		this.accountHierarchyRepository = accountHierarchyRepository;
		this.kanbanAccountHierarchyRepo = kanbanAccountHierarchyRepo;
		this.factory = factory;
		this.jiraProcessorConfig = jiraProcessorConfig;
		this.boardMetadataRepository = boardMetadataRepository;
		this.fieldMappingRepository = fieldMappingRepository;
		this.metadataIdentifierRepository = metadataIdentifierRepository;
		this.jiraRestClientFactory = jiraRestClientFactory;
		this.sprintClient = sprintClient;
	}

	/**
	 * Instantiates a new jira online runnable.
	 */
	public JiraOnlineRunnable() {
	}

	/**
	 * Collect release data.
	 *
	 * @param jiraAdapter
	 *            the jira adapter
	 * @param projectConfig
	 *            the project config
	 * 
	 */
	private void collectReleaseData(JiraAdapter jiraAdapter, ProjectConfFieldMapping projectConfig) {
		long releaseDataStart = System.currentTimeMillis();
		MDC.put("ReleaseDataStartTime", String.valueOf(releaseDataStart));
		ReleaseDataClientImpl releaseData = new ReleaseDataClientImpl(jiraAdapter, projectReleaseRepo,
				accountHierarchyRepository, kanbanAccountHierarchyRepo);
		releaseData.processReleaseInfo(projectConfig);
		long end = System.currentTimeMillis();
		MDC.put("ReleaseDataEndTime", String.valueOf(end));
	}
	private void collectSprintReportData(JiraAdapter jiraAdapter, ProjectConfFieldMapping projectConfig) {
		log.info("START - SprintReport fetching start");
		if(!projectConfig.isKanban()) {
			sprintClient.createSprintDetailBasedOnBoard(projectConfig, jiraAdapter);
		}
		log.info("END - SprintReport fetching End");
	}

		/**
         * Collects JiraIssue Data.
         *
         * @param jiraAdapter
         *            JiraAdapter to create Connection
         * @param projectConfig
         *            Project Configuration map
         */
	private void collectJiraIssueData(JiraAdapter jiraAdapter, ProjectConfFieldMapping projectConfig) {
		long storyDataStart = System.currentTimeMillis();
		MDC.put("storyDataStartTime", String.valueOf(storyDataStart));
		projectConfig.setIssueCount(0);
		JiraIssueClient jiraIssueClient = factory.getJiraIssueDataClient(projectConfig);
		int count = jiraIssueClient.processesJiraIssues(projectConfig, jiraAdapter, false);
		projectConfig.setIssueCount(count);
		MDC.put("JiraIssueCount", String.valueOf(count));
		long end = System.currentTimeMillis();
		MDC.put("storyDataEndTime", String.valueOf(end));
	}

	/**
	 * Collect jira metadata.
	 *
	 * @param jiraAdapter
	 *            the jira adapter
	 * @param projectConfig
	 *            the project config
	 */
	private void collectMetadata(JiraAdapter jiraAdapter, ProjectConfFieldMapping projectConfig) {
		if (null == boardMetadataRepository.findByProjectBasicConfigId(projectConfig.getBasicProjectConfigId())) {
			long metaDataStart = System.currentTimeMillis();
			MDC.put("MetaData Collection StartTime", String.valueOf(metaDataStart));
			MetaDataClientImpl metadata = new MetaDataClientImpl(jiraAdapter, boardMetadataRepository,
					fieldMappingRepository, metadataIdentifierRepository);
			boolean isSuccess = metadata.processMetadata(projectConfig);
			if(isSuccess){
				jiraRestClientFactory.cacheRestClient(CommonConstant.CACHE_CLEAR_ENDPOINT,
						CommonConstant.CACHE_FIELD_MAPPING_MAP);
				jiraRestClientFactory.cacheRestClient(CommonConstant.CACHE_CLEAR_ENDPOINT,
						CommonConstant.CACHE_PROJECT_CONFIG_MAP);
			}
			MDC.put("Fetched metadata", String.valueOf(isSuccess));
			long end = System.currentTimeMillis();
			MDC.put("metaDataEndTime", String.valueOf(end));
		}
	}

}