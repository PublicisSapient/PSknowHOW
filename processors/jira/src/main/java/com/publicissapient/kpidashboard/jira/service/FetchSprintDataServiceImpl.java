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

package com.publicissapient.kpidashboard.jira.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.ToolCredential;
import com.publicissapient.kpidashboard.common.model.application.ActiveItrFetchDetails;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintIssue;
import com.publicissapient.kpidashboard.common.repository.application.ActiveItrFetchRepository;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.service.ToolCredentialProvider;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import com.publicissapient.kpidashboard.jira.adapter.JiraAdapter;
import com.publicissapient.kpidashboard.jira.adapter.helper.JiraRestClientFactory;
import com.publicissapient.kpidashboard.jira.adapter.impl.OnlineAdapter;
import com.publicissapient.kpidashboard.jira.adapter.impl.async.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.client.jiraissue.ScrumJiraIssueClientImpl;
import com.publicissapient.kpidashboard.jira.client.sprint.SprintClientImpl;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.JiraInfo;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.oauth.JiraOAuthClient;
import com.publicissapient.kpidashboard.jira.oauth.JiraOAuthProperties;
import com.publicissapient.kpidashboard.jira.processor.mode.ModeBasedProcessor;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FetchSprintDataServiceImpl extends ModeBasedProcessor {
	@Autowired
	SprintClientImpl sprintClient;
	@Autowired
	ProjectBasicConfigRepository projectBasicConfigRepository;
	@Autowired
	FieldMappingRepository fieldMappingRepository;
	@Autowired
	private ConnectionRepository connectionRepository;
	@Autowired
	private ToolCredentialProvider toolCredentialProvider;
	@Autowired
	private AesEncryptionService aesEncryptionService;
	@Autowired
	private JiraProcessorConfig jiraProcessorConfig;

	@Autowired
	private ProjectToolConfigRepository toolRepository;
	@Autowired
	private JiraRestClientFactory jiraRestClientFactory;
	@Autowired
	private JiraOAuthProperties jiraOAuthProperties;
	@Autowired
	private JiraOAuthClient jiraOAuthClient;
	@Autowired
	SprintRepository sprintRepository;
	@Autowired
	ScrumJiraIssueClientImpl scrumJiraIssueClientImpl;
    @Autowired
    ActiveItrFetchRepository activeItrFetchRepository;


	public boolean fetchSprintData(String sprintID) {
		boolean executionStatus = true;
		SprintDetails sprintDetails = sprintRepository.findBySprintID(sprintID);
		List<String> originalBoardIds = sprintDetails.getOriginBoardId();

		Optional<ProjectBasicConfig> projectBasicConfig = projectBasicConfigRepository
				.findById(sprintDetails.getBasicProjectConfigId());
		FieldMapping fieldMapping = fieldMappingRepository
				.findByBasicProjectConfigId(sprintDetails.getBasicProjectConfigId());
		Map<String, ProjectConfFieldMapping> projectMapConfig = createProjectConfigMap(
				Collections.singletonList(projectBasicConfig.get()), Collections.singletonList(fieldMapping));
		ProjectConfFieldMapping projectConfig = projectMapConfig.get(projectBasicConfig.get().getProjectName());
		JiraAdapter jiraAdapter = null;
		ProcessorJiraRestClient client;
		List<ProjectToolConfig> jiraDetails = toolRepository.findByToolNameAndBasicProjectConfigId(
				ProcessorConstants.JIRA, projectConfig.getBasicProjectConfigId());
		if (CollectionUtils.isNotEmpty(jiraDetails) && jiraDetails.get(0).getConnectionId() != null) {
			Optional<Connection> jiraConn = connectionRepository.findById(jiraDetails.get(0).getConnectionId());
			if (jiraConn.isPresent() && projectConfig.getJira().getConnection().isPresent()) {
				projectConfig.setProjectToolConfig(jiraDetails.get(0));
				boolean isOauth = jiraConn.get().getIsOAuth();
				Optional<Connection> connectionOptional = projectConfig.getJira().getConnection();
				if (connectionOptional.isPresent()) {
					Connection conn = connectionOptional.get();
					KerberosClient krb5Client = new KerberosClient(conn.getJaasConfigFilePath(),
							conn.getKrb5ConfigFilePath(), conn.getJaasUser(), conn.getSamlEndPoint(),
							conn.getBaseUrl());
					client = getProcessorRestClient(Collections.singletonList(projectBasicConfig.get()), projectConfig,
							isOauth, conn, krb5Client);

					jiraAdapter = new OnlineAdapter(jiraProcessorConfig, client, aesEncryptionService,
							toolCredentialProvider, krb5Client);
				}
			}
		}
		try {
			// fetching the sprint details
			for (String boardId : originalBoardIds) {
				List<SprintDetails> sprintDetailsList = sprintClient.getSprints(projectConfig, boardId, jiraAdapter);
				if (CollectionUtils.isNotEmpty(sprintDetailsList)) {
					Set<SprintDetails> sprintDetailSet = sprintDetailsList.stream()
							.filter(s -> s.getSprintID().equalsIgnoreCase(sprintID)).collect(Collectors.toSet());
					try {
						sprintClient.processSprints(projectConfig, sprintDetailSet, jiraAdapter, true);
					} catch (InterruptedException e) {
						executionStatus = false;
                        log.error("Got error while fetching sprintDetails.", e);
					}
				}
			}
			log.info("Done sprint fetching");

			SprintDetails updatedSprintDetails = sprintRepository.findBySprintID(sprintID);

			//updating the jiraIssue & history of the sprint
			Set<String> issuesToUpdate = updatedSprintDetails.getTotalIssues().stream().map(SprintIssue::getNumber)
					.collect(Collectors.toSet());
			issuesToUpdate.addAll(updatedSprintDetails.getPuntedIssues().stream().map(SprintIssue::getNumber)
					.collect(Collectors.toSet()));
			issuesToUpdate.addAll(updatedSprintDetails.getCompletedIssuesAnotherSprint().stream()
					.map(SprintIssue::getNumber).collect(Collectors.toSet()));

			// checking if subtask as a bug in fieldMapping
			FieldMapping projFieldMapping = projectConfig.getFieldMapping();
			if (CollectionUtils.isNotEmpty(projFieldMapping.getJiradefecttype()) &&
					(fieldMapping.getJiradefecttype().contains("Studio Task") ||
							fieldMapping.getJiradefecttype().contains("Task"))) {

				List<String> allSubtaskKeys = ((OnlineAdapter) jiraAdapter).getSubtask(projectConfig,
						new ArrayList<>(issuesToUpdate));
				issuesToUpdate.addAll(allSubtaskKeys);
			}

			// fetching & updating the jira_issue & jira_issue_history
			 scrumJiraIssueClientImpl.processesJiraIssuesSprintFetch(projectConfig,jiraAdapter,false,
			 new ArrayList<>(issuesToUpdate));

		} catch (Exception e) {
			log.error("Got error while fetching sprint data.", e);
			executionStatus = false;
		}
		long endTime = System.currentTimeMillis();
		LocalDateTime time = DateUtil.convertMillisToLocalDateTime(endTime);
		ActiveItrFetchDetails fetchDetails = new ActiveItrFetchDetails();
		fetchDetails.setSprintId(sprintID);
		fetchDetails.setLastSyncDateTime(time);
		if (executionStatus) {
			fetchDetails.setErrorInFetch(false);
			fetchDetails.setFetchSuccessful(true);
		} else {
			fetchDetails.setErrorInFetch(true);
			fetchDetails.setFetchSuccessful(false);
		}
        activeItrFetchRepository.save(fetchDetails);
		return executionStatus;
	}

	@Override
	public Map<String, Integer> validateAndCollectIssues(List<ProjectBasicConfig> projectConfigList) {
		return null;
	}

	@Override
	public List<ProjectBasicConfig> getRelevantProjects(List<ProjectBasicConfig> projectConfigList) {
		return null;
	}

	private ProcessorJiraRestClient getProcessorRestClient(List<ProjectBasicConfig> projectConfigList,
			ProjectConfFieldMapping entry, boolean isOauth, Connection conn, KerberosClient krb5Client) {
		if (conn.isJaasKrbAuth()) {
			return jiraRestClientFactory.getSpnegoSamlClient(krb5Client);
		} else {
			return getProcessorJiraRestClient(projectConfigList, entry, isOauth, conn);
		}
	}

	private ProcessorJiraRestClient getProcessorJiraRestClient(List<ProjectBasicConfig> projectConfigList,
			ProjectConfFieldMapping entry, boolean isOauth, Connection conn) {
		ProcessorJiraRestClient client;

		String username = "";
		String password = "";
		if (conn.isVault()) {
			ToolCredential toolCredential = toolCredentialProvider.findCredential(conn.getUsername());
			if (toolCredential != null) {
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
			saveAccessToken(entry);
			jiraOAuthProperties.setAccessToken(conn.getAccessToken());

			client = jiraRestClientFactory.getJiraOAuthClient(JiraInfo.builder().jiraConfigBaseUrl(conn.getBaseUrl())
					.username(username).password(password).jiraConfigAccessToken(conn.getAccessToken())
					.jiraConfigProxyUrl(null).jiraConfigProxyPort(null).build());

		} else {

			client = jiraRestClientFactory.getJiraClient(JiraInfo.builder().jiraConfigBaseUrl(conn.getBaseUrl())
					.username(username).password(password).jiraConfigProxyUrl(null).jiraConfigProxyPort(null)
					.bearerToken(conn.isBearerToken()).build());

		}
		return client;
	}

	private String decryptJiraPassword(String encryptedPassword) {
		return aesEncryptionService.decrypt(encryptedPassword, jiraProcessorConfig.getAesEncryptionKey());
	}

	public void saveAccessToken(ProjectConfFieldMapping entry) {
		Optional<Connection> connectionOptional = entry.getJira().getConnection();
		if (connectionOptional.isPresent()) {
			Optional<String> checkNull = Optional.ofNullable(connectionOptional.get().getAccessToken());
			if (!checkNull.isPresent() || checkNull.get().isEmpty()) {
				JiraToolConfig jiraToolConfig = entry.getJira();
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

}
