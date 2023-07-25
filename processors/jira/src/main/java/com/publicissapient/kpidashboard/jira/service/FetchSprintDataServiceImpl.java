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
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.ToolCredential;
import com.publicissapient.kpidashboard.common.model.application.SprintTraceLog;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintIssue;
import com.publicissapient.kpidashboard.common.repository.application.SprintTraceLogRepository;
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

import lombok.extern.slf4j.Slf4j;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Component
@Slf4j
public class FetchSprintDataServiceImpl {
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
	SprintTraceLogRepository sprintTraceLogRepository;
	@Autowired
	JiraIssueRepository jiraIssueRepository;
	public static final String TILDA_SYMBOL = "^";
	public static final String DOLLAR_SYMBOL = "$";

	public boolean fetchSprintData(String sprintID) {
		boolean executionStatus = true;
		SprintDetails sprintDetails = sprintRepository.findBySprintID(sprintID);
		List<String> originalBoardIds = sprintDetails.getOriginBoardId();

		ProjectBasicConfig projectBasicConfig = projectBasicConfigRepository
				.findById(sprintDetails.getBasicProjectConfigId()).orElse(new ProjectBasicConfig());

		FieldMapping fieldMapping = fieldMappingRepository
				.findByBasicProjectConfigId(sprintDetails.getBasicProjectConfigId());

		Map<String, ProjectConfFieldMapping> projectMapConfig = createProjectConfigMap(
				Collections.singletonList(projectBasicConfig), Collections.singletonList(fieldMapping));

		ProjectConfFieldMapping projectConfig = projectMapConfig.get(projectBasicConfig.getProjectName());
		MDC.put(CommonConstant.PROJECTNAME,projectBasicConfig.getProjectName());

		JiraAdapter jiraAdapter = getJiraAdapter(projectConfig);

		try {
			// fetching the sprint details
			for (String boardId : originalBoardIds) {
				List<SprintDetails> sprintDetailsList = sprintClient.getSprints(projectConfig, boardId, jiraAdapter);
				if (CollectionUtils.isNotEmpty(sprintDetailsList)) {
					// filtering the sprint need to update
					Set<SprintDetails> sprintDetailSet = sprintDetailsList.stream()
							.filter(s -> s.getSprintID().equalsIgnoreCase(sprintID)).collect(Collectors.toSet());
					sprintClient.processSprints(projectConfig, sprintDetailSet, jiraAdapter, true);
				}
			}
			log.info("Fetched & updated the active sprint {} of proj {}", sprintID,
					projectBasicConfig.getProjectName());

			SprintDetails updatedSprintDetails = sprintRepository.findBySprintID(sprintID);

			// collecting the jiraIssue & history of to be updated
			Set<String> issuesToUpdate = Optional.ofNullable(updatedSprintDetails.getTotalIssues())
					.map(Collection::stream).orElse(Stream.empty()).map(SprintIssue::getNumber)
					.collect(Collectors.toSet());

			issuesToUpdate.addAll(Optional.ofNullable(updatedSprintDetails.getPuntedIssues()).map(Collection::stream)
					.orElse(Stream.empty()).map(SprintIssue::getNumber).collect(Collectors.toSet()));

			issuesToUpdate.addAll(
					Optional.ofNullable(updatedSprintDetails.getCompletedIssuesAnotherSprint()).map(Collection::stream)
							.orElse(Stream.empty()).map(SprintIssue::getNumber).collect(Collectors.toSet()));

			// checking if subtask is configured as bug
			getSubTaskAsBug(fieldMapping, updatedSprintDetails, issuesToUpdate);

			// fetching & updating the jira_issue & jira_issue_history
			int count = scrumJiraIssueClientImpl.processesJiraIssuesSprintFetch(projectConfig, jiraAdapter, false,
					new ArrayList<>(issuesToUpdate));
			log.info("Fetched & updated {} jira issues of active sprint {}", count, sprintID);

		} catch (InterruptedException e) {// NOSONAR
			executionStatus = false;
			log.error("Interruption thrown while sprint fetch for sprint {}", sprintID);
		} catch (Exception e) {
			log.error("Got error while fetching sprint data.", e);
			executionStatus = false;
		}
		long endTime = System.currentTimeMillis();
		// saving the execution details
		SprintTraceLog fetchDetails = sprintTraceLogRepository.findBySprintId(sprintID);
		fetchDetails.setLastSyncDateTime(endTime);
		if (executionStatus) {
			fetchDetails.setErrorInFetch(false);
			fetchDetails.setFetchSuccessful(true);
			// clearing cache
			jiraRestClientFactory.cacheRestClient(CommonConstant.CACHE_CLEAR_ENDPOINT, CommonConstant.JIRA_KPI_CACHE);

		} else {
			fetchDetails.setErrorInFetch(true);
			fetchDetails.setFetchSuccessful(false);
		}
		sprintTraceLogRepository.save(fetchDetails);
		return executionStatus;
	}

	private void getSubTaskAsBug(FieldMapping fieldMapping, SprintDetails updatedSprintDetails,
			Set<String> issuesToUpdate) {
		if (CollectionUtils.isNotEmpty(updatedSprintDetails.getTotalIssues())) {
			List<String> defectTypes = Optional.ofNullable(fieldMapping).map(FieldMapping::getJiradefecttype)
					.orElse(Collections.emptyList());
			Set<String> totalSprintReportDefects = new HashSet<>();
			Set<String> totalSprintReportStories = new HashSet<>();

			updatedSprintDetails.getTotalIssues().stream().forEach(sprintIssue -> {
				if (defectTypes.contains(sprintIssue.getTypeName())) {
					totalSprintReportDefects.add(sprintIssue.getNumber());
				} else {
					totalSprintReportStories.add(sprintIssue.getNumber());
				}
			});
			List<String> defectType = new ArrayList<>();
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
			Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
			Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
			String basicProjConfigId = updatedSprintDetails.getBasicProjectConfigId().toString();

			defectType.add(NormalizedJira.DEFECT_TYPE.getValue());
			mapOfProjectFilters.put("typeName", convertToPatternList(defectType));
			uniqueProjectMap.put(basicProjConfigId, mapOfProjectFilters);
			mapOfFilters.put("basicProjectConfigId", Collections.singletonList(basicProjConfigId));

			// fetched all defects which is linked to current sprint report stories
			List<JiraIssue> linkedDefects = jiraIssueRepository.findLinkedDefects(mapOfFilters,
					totalSprintReportStories, uniqueProjectMap);

			// filter defects which is issue type not coming in sprint report
			List<JiraIssue> subTaskDefects = linkedDefects.stream()
					.filter(jiraIssue -> !totalSprintReportDefects.contains(jiraIssue.getNumber()))
					.collect(Collectors.toList());
			Set<String> subTaskDefectsKey = subTaskDefects.stream().map(JiraIssue::getNumber)
					.collect(Collectors.toSet());
			issuesToUpdate.addAll(subTaskDefectsKey);
		}
	}

	private JiraAdapter getJiraAdapter(ProjectConfFieldMapping projectConfig) {
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
					client = getProcessorRestClient(projectConfig, isOauth, conn, krb5Client);

					jiraAdapter = new OnlineAdapter(jiraProcessorConfig, client, aesEncryptionService,
							toolCredentialProvider, krb5Client);
				}
			}
		}
		return jiraAdapter;
	}
	private ProcessorJiraRestClient getProcessorRestClient(ProjectConfFieldMapping entry, boolean isOauth,
			Connection conn, KerberosClient krb5Client) {
		if (conn.isJaasKrbAuth()) {
			return jiraRestClientFactory.getSpnegoSamlClient(krb5Client);
		} else {
			return getProcessorJiraRestClient(entry, isOauth, conn);
		}
	}

	private ProcessorJiraRestClient getProcessorJiraRestClient(ProjectConfFieldMapping entry, boolean isOauth,
			Connection conn) {
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

	/**
	 * This method used to convert string list to pattern list to support ignore
	 * case
	 *
	 * @param stringList
	 * @return return list of patttern
	 */
	public List<Pattern> convertToPatternList(List<String> stringList) {
		List<Pattern> regexList = new ArrayList<>();
		if (org.apache.commons.collections.CollectionUtils.isNotEmpty(stringList)) {
			for (String value : stringList) {
				regexList.add(
						Pattern.compile(TILDA_SYMBOL + Pattern.quote(value) + DOLLAR_SYMBOL, Pattern.CASE_INSENSITIVE));

			}
		}
		return regexList;
	}

	public Map<String, ProjectConfFieldMapping> createProjectConfigMap(List<ProjectBasicConfig> projectConfigList,
			List<FieldMapping> fieldMappingList) {
		Map<String, ProjectConfFieldMapping> projectConfigMap = new HashMap<>();
		CollectionUtils.emptyIfNull(projectConfigList).forEach(projectConfig -> {
			ProjectConfFieldMapping projectConfFieldMapping = ProjectConfFieldMapping.builder().build();
			try {
				BeanUtils.copyProperties(projectConfFieldMapping, projectConfig);
				projectConfFieldMapping.setProjectBasicConfig(projectConfig);
				projectConfFieldMapping.setKanban(projectConfig.getIsKanban());
				projectConfFieldMapping.setBasicProjectConfigId(projectConfig.getId());
				projectConfFieldMapping.setJira(getJiraToolConfig(projectConfig.getId()));
				projectConfFieldMapping.setJiraToolConfigId(getToolConfigId(projectConfig.getId()));

			} catch (IllegalAccessException e) {
				log.error("Error while copying Project Config to ProjectConfFieldMapping", e);
			} catch (InvocationTargetException e) {
				log.error("Error while copying Project Config to ProjectConfFieldMapping invocation error", e);
			}
			CollectionUtils.emptyIfNull(fieldMappingList).stream()
					.filter(fieldMapping -> projectConfig.getId().equals(fieldMapping.getBasicProjectConfigId()))
					.forEach(projectConfFieldMapping::setFieldMapping);
			projectConfigMap.putIfAbsent(projectConfig.getProjectName(), projectConfFieldMapping);
		});
		return projectConfigMap;
	}

	private JiraToolConfig getJiraToolConfig(ObjectId basicProjectConfigId) {
		JiraToolConfig toolObj = new JiraToolConfig();
		List<ProjectToolConfig> jiraDetails = toolRepository
				.findByToolNameAndBasicProjectConfigId(ProcessorConstants.JIRA, basicProjectConfigId);
		if (CollectionUtils.isNotEmpty(jiraDetails)) {

			try {
				BeanUtils.copyProperties(toolObj, jiraDetails.get(0));
			} catch (IllegalAccessException | InvocationTargetException e) {
				log.error("Could not set JiraToolConfig", e);
			}
			if (jiraDetails.get(0).getConnectionId() != null) {
				Optional<Connection> conn = connectionRepository.findById(jiraDetails.get(0).getConnectionId());
				if (conn.isPresent()) {
					toolObj.setConnection(conn);
				}
			}
		}
		return toolObj;
	}

	private ObjectId getToolConfigId(ObjectId basicProjectConfigId) {
		List<ProjectToolConfig> boardsDetails = toolRepository
				.findByToolNameAndBasicProjectConfigId(ProcessorConstants.JIRA, basicProjectConfigId);
		return CollectionUtils.isNotEmpty(boardsDetails) ? boardsDetails.get(0).getId() : null;
	}

}
