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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.publicissapient.kpidashboard.jira.client.release.ReleaseDataClientFactory;
import lombok.extern.slf4j.Slf4j;

import com.publicissapient.kpidashboard.common.model.ToolCredential;
import com.publicissapient.kpidashboard.common.service.ToolCredentialProvider;
import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.jira.adapter.impl.OnlineAdapter;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.KanbanAccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectReleaseRepo;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.SubProjectRepository;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.common.repository.jira.BoardMetadataRepository;
import com.publicissapient.kpidashboard.common.repository.jira.MetadataIdentifierRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.jira.adapter.JiraAdapter;
import com.publicissapient.kpidashboard.jira.adapter.helper.JiraRestClientFactory;
import com.publicissapient.kpidashboard.jira.adapter.impl.async.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.client.jiraissue.JiraIssueClientFactory;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.JiraInfo;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.oauth.JiraOAuthClient;
import com.publicissapient.kpidashboard.jira.oauth.JiraOAuthProperties;
import com.publicissapient.kpidashboard.jira.processor.mode.ModeBasedProcessor;
import com.publicissapient.kpidashboard.jira.util.JiraConstants;

@Component
@Slf4j
public class OnlineDataProcessorImpl extends ModeBasedProcessor {

	@Autowired
	private JiraProcessorConfig jiraProcessorConfig;

	@Autowired
	private JiraRestClientFactory jiraRestClientFactory;

	@Autowired
	private FieldMappingRepository fieldMappingRepository;

	@Autowired
	private ProjectReleaseRepo projectReleaseRepo;

	@Autowired
	private AccountHierarchyRepository accountHierarchyRepository;

	@Autowired
	private KanbanAccountHierarchyRepository kanbanAccountHierarchyRepo;

	@Autowired
	private JiraIssueClientFactory jiraIssueClientFactory;

	@Autowired
	private JiraOAuthProperties jiraOAuthProperties;

	@Autowired
	private JiraOAuthClient jiraOAuthClient;

	@Autowired
	private AesEncryptionService aesEncryptionService;

	@Autowired
	private BoardMetadataRepository boardMetadataRepository;

	@Autowired
	private MetadataIdentifierRepository metadataIdentifierRepository;

	@Autowired
	private ConnectionRepository connectionRepository;

	@Autowired
	private ProjectToolConfigRepository toolRepository;

	@Autowired
	private SubProjectRepository subProjectRepository;

	@Autowired
	private ToolCredentialProvider toolCredentialProvider;

	@Autowired
	private ReleaseDataClientFactory releaseDataClientFactory;


	/**
	 * Validates and collects Jira issues using JIA API for projects with onlinemode
	 * 
	 * @param projectConfigList List of all configured projects
	 */
	@Override
	public Map<String, Integer> validateAndCollectIssues(List<ProjectBasicConfig> projectConfigList) {
		List<FieldMapping> fieldMappingList = fieldMappingRepository.findAll();

		ExecutorService executor = null;
		Map<String, Integer> issueCountMap = new HashMap<>();
		issueCountMap.put(JiraConstants.SCRUM_DATA, 0);
		issueCountMap.put(JiraConstants.KANBAN_DATA, 0);
		try {

			Map<String, ProjectConfFieldMapping> onlineLineprojectConfigMap = createProjectConfigMap(
					getRelevantProjects(projectConfigList), fieldMappingList);
			executor = Executors.newFixedThreadPool(jiraProcessorConfig.getThreadPoolSize());

			CountDownLatch latch = new CountDownLatch(onlineLineprojectConfigMap.size());
			for (Map.Entry<String, ProjectConfFieldMapping> entry : onlineLineprojectConfigMap.entrySet()) {

				ProcessorJiraRestClient client;
				List<ProjectToolConfig> jiraDetails = toolRepository.findByToolNameAndBasicProjectConfigId(
						ProcessorConstants.JIRA, entry.getValue().getBasicProjectConfigId());
				if (CollectionUtils.isNotEmpty(jiraDetails) && jiraDetails.get(0).getConnectionId() != null) {
					Optional<Connection> jiraConn = connectionRepository.findById(jiraDetails.get(0).getConnectionId());
					if (jiraConn.isPresent() && entry.getValue().getJira().getConnection().isPresent()) {
						entry.getValue().setProjectToolConfig(jiraDetails.get(0));
						boolean isOauth = jiraConn.get().getIsOAuth();
						Optional<Connection> connectionOptional = entry.getValue().getJira().getConnection();
						if (connectionOptional.isPresent()) {
							Connection conn = connectionOptional.get();
							KerberosClient krb5Client = new KerberosClient(conn.getJaasConfigFilePath(), conn.getKrb5ConfigFilePath(),
									conn.getJaasUser(), conn.getSamlEndPoint(), conn.getBaseUrl());
							client = getProcessorRestClient(projectConfigList, entry, isOauth, conn, krb5Client);

							JiraAdapter jiraAdapter = new OnlineAdapter(jiraProcessorConfig, client,
									aesEncryptionService, toolCredentialProvider, krb5Client);
							Runnable worker = new JiraOnlineRunnable(latch, jiraAdapter, entry.getValue(),
									projectReleaseRepo, accountHierarchyRepository, kanbanAccountHierarchyRepo,
									jiraIssueClientFactory, jiraProcessorConfig, boardMetadataRepository,
									fieldMappingRepository, metadataIdentifierRepository, jiraRestClientFactory,releaseDataClientFactory,
									getExecutionLogContext());// NOPMD
							executor.execute(worker);

						}
					}
				}
			}
			latch.await();

			Integer scrumIssueCount = onlineLineprojectConfigMap.values().stream().filter(x -> !x.isKanban())
					.mapToInt(ProjectConfFieldMapping::getIssueCount).sum();
			Integer kanbanIssueCount = onlineLineprojectConfigMap.values().stream()
					.filter(ProjectConfFieldMapping::isKanban).mapToInt(ProjectConfFieldMapping::getIssueCount).sum();
			issueCountMap.put(JiraConstants.SCRUM_DATA, scrumIssueCount);
			issueCountMap.put(JiraConstants.KANBAN_DATA, kanbanIssueCount);
		} catch (InterruptedException ex) {
			log.error("Error while executing an online jira project", ex);
			Thread.currentThread().interrupt();
		} finally {
			if (executor != null) {
				executor.shutdown();
			}
			destroyLogContext();
			MDC.clear();
		}
		return issueCountMap;
	}

	private ProcessorJiraRestClient getProcessorRestClient(List<ProjectBasicConfig> projectConfigList,
														   Map.Entry<String, ProjectConfFieldMapping> entry,
														   boolean isOauth, Connection conn, KerberosClient krb5Client){
		if(conn.isJaasKrbAuth()){
			return jiraRestClientFactory.getSpnegoSamlClient(krb5Client);
		}else{
			return getProcessorJiraRestClient(projectConfigList, entry, isOauth, conn);
		}
	}

	private ProcessorJiraRestClient getProcessorJiraRestClient(List<ProjectBasicConfig> projectConfigList,
															   Map.Entry<String, ProjectConfFieldMapping> entry,
															   boolean isOauth, Connection conn) {
		ProcessorJiraRestClient client;

		String username = "";
		String password = "";
		if (conn.isVault()) {
			ToolCredential toolCredential = toolCredentialProvider.findCredential(conn.getUsername());
			if(toolCredential != null){
				username = toolCredential.getUsername();
				password = toolCredential.getPassword();
			}

		} else if (conn.isBearerToken()) {
			password = decryptJiraPassword(conn.getPatOAuthToken());
		} else {
			username = conn.getUsername();
			password = decryptJiraPassword(conn.getPassword());
		}


		if (isOauth) {
			// Sets Jira OAuth properties
			jiraOAuthProperties.setJiraBaseURL(conn.getBaseUrl());
			jiraOAuthProperties.setConsumerKey(conn.getConsumerKey());
			jiraOAuthProperties.setPrivateKey(decryptJiraPassword(conn.getPrivateKey()));

			// Generate and save accessToken
			saveAccessToken(entry, projectConfigList);
			jiraOAuthProperties.setAccessToken(conn.getAccessToken());

			client = jiraRestClientFactory.getJiraOAuthClient(JiraInfo.builder()
					.jiraConfigBaseUrl(conn.getBaseUrl()).username(username)
					.password(password)
					.jiraConfigAccessToken(conn.getAccessToken()).jiraConfigProxyUrl(null)
					.jiraConfigProxyPort(null).build());

		} else {

			client = jiraRestClientFactory.getJiraClient(JiraInfo.builder()
					.jiraConfigBaseUrl(conn.getBaseUrl()).username(username)
					.password(password).jiraConfigProxyUrl(null)
					.jiraConfigProxyPort(null).bearerToken(conn.isBearerToken()).build());

		}
		return client;
	}

	/**
	 * Generate and save accessToken
	 * 
	 * @param entry             Map of Jira project Configuration field mapping
	 * @param projectConfigList List of project configuration mapping
	 */
	public void saveAccessToken(Map.Entry<String, ProjectConfFieldMapping> entry,
			List<ProjectBasicConfig> projectConfigList) {
		Optional<Connection> connectionOptional = entry.getValue().getJira().getConnection();
		if (connectionOptional.isPresent()) {
			Optional<String> checkNull = Optional
					.ofNullable(connectionOptional.get().getAccessToken());
			if (!checkNull.isPresent() || checkNull.get().isEmpty()) {

				JiraToolConfig jiraToolConfig = entry.getValue().getJira();
				generateAndSaveAccessToken(jiraToolConfig);
			}
		}
	}

	/**
	 * Generate and save accessToken
	 * 
	 * @param jiraToolConfig
	 */
	private void generateAndSaveAccessToken(JiraToolConfig jiraToolConfig) {

		Optional<Connection> connectionOptional = jiraToolConfig.getConnection();
		if (connectionOptional.isPresent()) {
			String username = connectionOptional.get().getUsername();
			String plainTextPassword = decryptJiraPassword(connectionOptional.get().getPassword());

			String accessToken;
			try {
				accessToken = jiraOAuthClient.getAccessToken(username, plainTextPassword);
				connectionOptional.get().setAccessToken(accessToken);
				connectionRepository.save(connectionOptional.get());
			} catch (FailingHttpStatusCodeException e) {
				log.error("HTTP Status code error while generating accessToken", e);
			} catch (MalformedURLException e) {
				log.error("Malformed URL error while generating accessToken", e);
			} catch (IOException e) {
				log.error("Error while generating accessToken", e);
			}
		}
	}

	private String decryptJiraPassword(String encryptedPassword) {
		return aesEncryptionService.decrypt(encryptedPassword, jiraProcessorConfig.getAesEncryptionKey());
	}

	@Override
	public List<ProjectBasicConfig> getRelevantProjects(List<ProjectBasicConfig> projectConfigList) {
		List<ProjectBasicConfig> onlineJiraProjects = new ArrayList<>();
		for (ProjectBasicConfig config : projectConfigList) {
			List<ProjectToolConfig> jiraDetails = toolRepository
					.findByToolNameAndBasicProjectConfigId(ProcessorConstants.JIRA, config.getId());
			if (CollectionUtils.isNotEmpty(jiraDetails) && jiraDetails.get(0).getConnectionId() != null) {
				Optional<Connection> jiraConn = connectionRepository.findById(jiraDetails.get(0).getConnectionId());
				if (jiraConn.isPresent() && !jiraConn.get().isOffline()) {
					onlineJiraProjects.add(config);
				}
			}
		}

		return onlineJiraProjects;
	}

}
