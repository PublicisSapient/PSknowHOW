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

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import com.publicissapient.kpidashboard.jira.client.release.ReleaseDataClient;
import com.publicissapient.kpidashboard.jira.client.release.ReleaseDataClientFactory;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.context.ExecutionLogContext;
import com.publicissapient.kpidashboard.common.model.tracelog.PSLogData;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.KanbanAccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectReleaseRepo;
import com.publicissapient.kpidashboard.common.repository.jira.BoardMetadataRepository;
import com.publicissapient.kpidashboard.common.repository.jira.MetadataIdentifierRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import com.publicissapient.kpidashboard.jira.adapter.JiraAdapter;
import com.publicissapient.kpidashboard.jira.adapter.helper.JiraRestClientFactory;
import com.publicissapient.kpidashboard.jira.client.jiraissue.JiraIssueClient;
import com.publicissapient.kpidashboard.jira.client.jiraissue.JiraIssueClientFactory;
import com.publicissapient.kpidashboard.jira.client.metadata.MetaDataClientImpl;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

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

	private ReleaseDataClientFactory releaseDataClientFactory;

	private ExecutionLogContext executionLogContext;

	private PSLogData psLogData = new PSLogData();

	/**
	 * Run.
	 */
	@Override
	public void run() {

		try {
			setMDCContext();
			// Change-4-- as thread changed context is to be get
			ExecutionLogContext context = ExecutionLogContext.getContext();
			context.setProjectName(onlineLineprojectConfigMap.getProjectName());
			context.setProjectBasicConfgId(onlineLineprojectConfigMap.getBasicProjectConfigId().toHexString());
			ExecutionLogContext.set(context);
			psLogData.setProjectName(onlineLineprojectConfigMap.getProjectName());
			psLogData.setAction(CommonConstant.PROJECT_RUN);
			long start = System.currentTimeMillis();
			psLogData.setProjectStartTime(DateUtil.convertMillisToDateTime(start));
			// Change-5 when inserting logs as per the requirement add field in the
			// PSLogData class
			// and use in below manner
			log.info("START - Jira processing started for project {}", onlineLineprojectConfigMap.getProjectName(),
					kv(CommonConstant.PSLOGDATA, psLogData));
			if (jiraProcessorConfig.isFetchMetadata()) {
				collectMetadata(jiraAdapter, onlineLineprojectConfigMap);
			}
			collectJiraIssueData(jiraAdapter, onlineLineprojectConfigMap);
			collectReleaseData(jiraAdapter, onlineLineprojectConfigMap);
			long end = System.currentTimeMillis();
			psLogData.setProjectEndTime(DateUtil.convertMillisToDateTime(end));
			psLogData.setTimeTaken(String.valueOf(end - start));
			log.info("END - Jira processing finished for project {}", onlineLineprojectConfigMap.getProjectName(),
					kv(CommonConstant.PSLOGDATA, psLogData));
		} catch (Exception ex) {
			log.error("Exception in processing Jira Project", ex);
		} finally {
			latch.countDown();
			ExecutionLogContext.getContext().destroy();
			MDC.clear();
		}

	}

	private void setMDCContext() {
		ExecutionLogContext context = executionLogContext;
		if (Objects.nonNull(context) && Objects.nonNull(context.getRequestId())) {
			ExecutionLogContext.updateContext(context);
		}
		executionLogContext=null;
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
							  JiraRestClientFactory jiraRestClientFactory,ReleaseDataClientFactory releaseDataClientFactory, ExecutionLogContext executionLogContext) //NOPMD
	{
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
		this.releaseDataClientFactory=releaseDataClientFactory;
		this.executionLogContext=executionLogContext;
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
		Instant start = Instant.now();
		ReleaseDataClient jiraIssueDataClient = releaseDataClientFactory.getReleaseDataClient(projectConfig,jiraAdapter);
		jiraIssueDataClient.processReleaseInfo(projectConfig);
		psLogData.setTimeTaken(String.valueOf(Duration.between(start,Instant.now()).toMillis()));
		psLogData.setAction(CommonConstant.RELEASE_DATA);
		log.info("Time Taken to process release data", kv(CommonConstant.PSLOGDATA, psLogData));

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
		long startJiraIssueTime = System.currentTimeMillis();
		projectConfig.setIssueCount(0);
		JiraIssueClient jiraIssueClient = factory.getJiraIssueDataClient(projectConfig);
		int count = jiraIssueClient.processesJiraIssues(projectConfig, jiraAdapter, false);
		projectConfig.setIssueCount(count);
		psLogData.setTimeTaken(String.valueOf(System.currentTimeMillis() - startJiraIssueTime));
		psLogData.setAction(CommonConstant.JIRAISSUE_DATA);
		log.info("Time Taken to process Jira Issue", kv(CommonConstant.PSLOGDATA, psLogData));

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
			psLogData.setAction(CommonConstant.METADATA);
			MetaDataClientImpl metadata = new MetaDataClientImpl(jiraAdapter, boardMetadataRepository,
					fieldMappingRepository, metadataIdentifierRepository);
			boolean isSuccess = metadata.processMetadata(projectConfig);
			if (isSuccess) {
				jiraRestClientFactory.cacheRestClient(CommonConstant.CACHE_CLEAR_ENDPOINT,
						CommonConstant.CACHE_FIELD_MAPPING_MAP);
				jiraRestClientFactory.cacheRestClient(CommonConstant.CACHE_CLEAR_ENDPOINT,
						CommonConstant.CACHE_PROJECT_CONFIG_MAP);
			}
			log.info("Fetched metadata", String.valueOf(isSuccess));
		} else {
			log.info("metadata already present in db");
		}
	}

}