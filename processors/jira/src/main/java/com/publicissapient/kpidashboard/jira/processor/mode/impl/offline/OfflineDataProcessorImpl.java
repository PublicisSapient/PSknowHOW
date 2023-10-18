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

package com.publicissapient.kpidashboard.jira.processor.mode.impl.offline;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.atlassian.jira.rest.client.internal.json.VersionJsonParser;
import com.google.common.collect.Lists;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueOfflineFileTraceLogs;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.KanbanAccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectReleaseRepo;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.SubProjectRepository;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.common.repository.jira.IssueOfflineTraceLogsRepository;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.jira.adapter.JiraAdapter;
import com.publicissapient.kpidashboard.jira.adapter.atlassianbespoke.parser.CustomSearchResultJsonParser;
import com.publicissapient.kpidashboard.jira.client.jiraissue.JiraIssueClient;
import com.publicissapient.kpidashboard.jira.client.jiraissue.JiraIssueClientFactory;
import com.publicissapient.kpidashboard.jira.client.release.ReleaseDataClient;
import com.publicissapient.kpidashboard.jira.client.release.ReleaseDataClientFactory;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.processor.mode.ModeBasedProcessor;
import com.publicissapient.kpidashboard.jira.util.AlphanumComparator;
import com.publicissapient.kpidashboard.jira.util.JiraConstants;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OfflineDataProcessorImpl extends ModeBasedProcessor {

	private final CustomSearchResultJsonParser searchResultJsonParser = new CustomSearchResultJsonParser();
	private final VersionJsonParser versionJsonParser = new VersionJsonParser();
	@Autowired
	private JiraProcessorConfig jiraProcessorConfig;
	@Autowired
	private IssueOfflineTraceLogsRepository issueOfflineTraceLogsRepository;
	@Autowired
	private FieldMappingRepository fieldMappingRepository;
	@Autowired
	private AlphanumComparator alphanumComparator;
	@Autowired
	private JiraIssueClientFactory jiraIssueClientFactory;
	@Autowired
	private ProjectReleaseRepo projectReleaseRepo;
	@Autowired
	private AccountHierarchyRepository accountHierarchyRepository;
	@Autowired
	private KanbanAccountHierarchyRepository kanbanAccountHierarchyRepo;
	@Autowired
	private ConnectionRepository connectionRepository;
	@Autowired
	private ProjectToolConfigRepository toolRepository;
	@Autowired
	private SubProjectRepository subProjectRepository;
	@Autowired
	private ProcessorExecutionTraceLogService processorExecutionTraceLogService;
	@Autowired
	private ReleaseDataClientFactory releaseDataClientFactory;

	/**
	 * Filters Offline Project and collectsIssues from files at shared URL
	 * 
	 * @param projectConfigList
	 *            list of configured projects
	 */
	@Override
	public Map<String, Integer> validateAndCollectIssues(List<ProjectBasicConfig> projectConfigList) {
		List<FieldMapping> fieldMappingList = fieldMappingRepository.findAll();
		Map<String, Integer> issueCountMap = new HashMap<>();
		issueCountMap.put(JiraConstants.SCRUM_DATA, 0);
		issueCountMap.put(JiraConstants.KANBAN_DATA, 0);

		// try-with-resources. way of closing the IO stream.
		try (Stream<Path> rootDir = Files.walk(Paths.get(jiraProcessorConfig.getJsonFileName()))) {
			Map<String, ProjectConfFieldMapping> projectMapConfig = createProjectConfigMap(
					getRelevantProjects(projectConfigList), fieldMappingList);
			MDC.put("OfflineProjectCount", String.valueOf(projectConfigList.size()));
			final Map<String, ProjectConfFieldMapping> offlineProjectConfigMap = projectMapConfig;
			List<File> projectFolders = rootDir.filter(Files::isDirectory).map(Path::toFile) // NOSONAR
					.filter(d -> offlineProjectConfigMap.keySet().contains(d.getName())).collect(Collectors.toList());
			for (File directory : projectFolders) {
				try (Stream<Path> stream = Files.walk(Paths.get(directory.getAbsolutePath()))) {
					List<File> filesInFolder = stream.filter(Files::isRegularFile).map(Path::toFile) // NOSONAR
							.collect(Collectors.toList());
					Collections.sort(filesInFolder, alphanumComparator);
					List<JiraIssueOfflineFileTraceLogs> listTraceLogs = issueOfflineTraceLogsRepository.findAll();
					Map<String, JiraIssueOfflineFileTraceLogs> listFiles = listTraceLogs.stream()
							.collect(Collectors.toMap(JiraIssueOfflineFileTraceLogs::getFileName, data -> data));

					CollectionUtils.emptyIfNull(filesInFolder).forEach(file -> {
						try {
							collectOfflineJiraData(offlineProjectConfigMap.get(directory.getName()), listFiles, file);
						} catch (IOException | JSONException | ParseException e) {
							log.error(
									"JIRA PROCESSOR | collectOfflineJiraData method exception, unable to collect offline data...{} {}",
									file.getName(), e);
						}
					});
				}
			}
			Integer scrumIssueCount = offlineProjectConfigMap.values().stream().filter(x -> !x.isKanban())
					.mapToInt(ProjectConfFieldMapping::getIssueCount).sum();
			Integer kanbanIssueCount = offlineProjectConfigMap.values().stream()
					.filter(ProjectConfFieldMapping::isKanban).mapToInt(ProjectConfFieldMapping::getIssueCount).sum();
			issueCountMap.put(JiraConstants.SCRUM_DATA, scrumIssueCount);
			issueCountMap.put(JiraConstants.KANBAN_DATA, kanbanIssueCount);
		} catch (IOException ioe) {
			log.error("JIRA PROCESSOR | Offline | IO Exception occured while reading offline data files from dir", ioe);
		}
		return issueCountMap;
	}

	@Override
	public List<ProjectBasicConfig> getRelevantProjects(List<ProjectBasicConfig> projectConfigList) {
		List<ProjectBasicConfig> offlineJiraProjects = new ArrayList<>();
		for (ProjectBasicConfig config : projectConfigList) {
			List<ProjectToolConfig> jiraDetails = toolRepository
					.findByToolNameAndBasicProjectConfigId(ProcessorConstants.JIRA, config.getId());
			if (CollectionUtils.isNotEmpty(jiraDetails) && jiraDetails.get(0).getConnectionId() != null) {
				Optional<Connection> jiraConn = connectionRepository.findById(jiraDetails.get(0).getConnectionId());
				if (jiraConn.isPresent() && jiraConn.get().isOffline()) {
					offlineJiraProjects.add(config);
				}
			}
		}

		return offlineJiraProjects;
	}

	private List<Version> parseJIRAOfflineDataForVersion(String fileContents) throws JSONException {
		List<Version> listVersion = Lists.newArrayList();
		try {
			org.codehaus.jettison.json.JSONArray obj = new org.codehaus.jettison.json.JSONArray(fileContents);
			for (int i = 0; i <= obj.length() - 1; i++) {
				listVersion.add(versionJsonParser.parse((JSONObject) obj.get(i)));
			}
		} catch (JSONException jse) {
			log.error("JIRA PROCESSOR | Incorrect JSON file format ");
		}
		return listVersion;
	}

	/**
	 * Collects JIRA data in offline mode from provided file
	 *
	 * @param offLineprojectConfig
	 *            offlineProjectConfig
	 * @param listFiles
	 *            List of files
	 * @param file
	 *            File Object
	 * @throws IOException
	 *             IOException
	 * @throws JSONException
	 *             JSONException
	 * @throws ParseException
	 *             ParseException
	 */
	private void collectOfflineJiraData(ProjectConfFieldMapping offLineprojectConfig,
			Map<String, JiraIssueOfflineFileTraceLogs> listFiles, File file)
			throws IOException, JSONException, ParseException {

		if (isAlreadyProcessed(listFiles, file) && !isProjectConfigChanged(listFiles, file)) {
			return;
		}
		/*
		 * ProcessorExecutionTraceLog processorExecutionTraceLog =
		 * createProcessorExecutionTraceLog(
		 * offLineprojectConfig.getBasicProjectConfigId().toHexString());
		 * processorExecutionTraceLog.setExecutionStartedAt(System.currentTimeMillis());
		 * if (file.getName().toLowerCase().contains(CommonConstant.RELEASE)) { String
		 * fileContents = new String(Files.readAllBytes(Paths.get(file.getPath())),
		 * StandardCharsets.UTF_8); List<Version> listVersion =
		 * parseJIRAOfflineDataForVersion(fileContents); if
		 * (CollectionUtils.isEmpty(listVersion)) {
		 * log.error("[Jira-Feature Collector]. File {} is not parsed.",
		 * file.getName()); } JiraAdapter jiraClient = new
		 * OfflineAdapter(jiraProcessorConfig, null, listVersion);
		 * collectReleaseData(jiraClient, offLineprojectConfig);
		 * updateOfflineTraceLogs(file,
		 * offLineprojectConfig.getBasicProjectConfigId().toString());
		 * processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
		 * processorExecutionTraceLog.setExecutionSuccess(true);
		 * processorExecutionTraceLogService.save(processorExecutionTraceLog); return; }
		 * MDC.put("OfflineProcessor", file.getName()); String fileContents = new
		 * String(Files.readAllBytes(Paths.get(file.getPath())),
		 * StandardCharsets.UTF_8); SearchResult searchResult =
		 * parseJIRAOfflineData(fileContents); if (null == searchResult) {
		 * MDC.put("UnparsedFile", file.getName()); } JiraAdapter jiraAdapter = new
		 * OfflineAdapter(jiraProcessorConfig, searchResult, null);
		 * collectStoryData(jiraAdapter, offLineprojectConfig);
		 * updateOfflineTraceLogs(file,
		 * offLineprojectConfig.getBasicProjectConfigId().toString());
		 * processorExecutionTraceLog.setExecutionEndedAt(System.currentTimeMillis());
		 * processorExecutionTraceLog.setExecutionSuccess(offLineprojectConfig.
		 * getIssueCount() > 0);
		 * 
		 * processorExecutionTraceLogService.save(processorExecutionTraceLog);
		 */
	}

	private ProcessorExecutionTraceLog createProcessorExecutionTraceLog(String basicProjectConfigId) {
		ProcessorExecutionTraceLog processorExecutionTraceLog = new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setProcessorName(ProcessorConstants.JIRA);
		processorExecutionTraceLog.setBasicProjectConfigId(basicProjectConfigId);
		return processorExecutionTraceLog;
	}

	private boolean isProjectConfigChanged(Map<String, JiraIssueOfflineFileTraceLogs> listFiles, File file) {
		return listFiles.get(file.getName()).getStatus().equalsIgnoreCase(CommonConstant.REPROCESS);
	}

	private boolean isAlreadyProcessed(Map<String, JiraIssueOfflineFileTraceLogs> listFiles, File file) {
		return listFiles.containsKey(file.getName());
	}

	private void collectReleaseData(JiraAdapter jiraClient, ProjectConfFieldMapping projectConfig) {
		long releaseDataStart = System.currentTimeMillis();
		ReleaseDataClient jiraIssueDataClient = releaseDataClientFactory.getReleaseDataClient(projectConfig,
				jiraClient);
		jiraIssueDataClient.processReleaseInfo(projectConfig);
		long end = System.currentTimeMillis();
		log.info("Jira-Feature Collector Release ends at: %s", end);
		long timeTaken = end - releaseDataStart;
		log.info("Jira-Feature Collector Release took time : %s", timeTaken);
	}

	/**
	 * Parses the offline json and return the parsed json in object
	 *
	 * @param fileContents
	 *            Offline Jira data file content
	 * @return Search results after parsing file content
	 * @throws JSONException
	 *             JSONException
	 */
	private SearchResult parseJIRAOfflineData(String fileContents) throws JSONException {

		SearchResult searchResult = null;
		JSONObject obj = new JSONObject(fileContents);
		searchResult = searchResultJsonParser.parse(obj);

		return searchResult;
	}

	/**
	 * Update offline Trace logs
	 *
	 * @param file
	 *            File Object
	 */
	private void updateOfflineTraceLogs(File file, String projectConfigId) {
		JiraIssueOfflineFileTraceLogs jiraIssueOfflineFileTraceLogs = issueOfflineTraceLogsRepository
				.findByProjectConfigIdAndFileName(projectConfigId, file.getName());
		if (jiraIssueOfflineFileTraceLogs == null) {
			jiraIssueOfflineFileTraceLogs = new JiraIssueOfflineFileTraceLogs();
			jiraIssueOfflineFileTraceLogs.setFileName(file.getName());
			jiraIssueOfflineFileTraceLogs.setProjectConfigId(projectConfigId);
		}
		jiraIssueOfflineFileTraceLogs.setStatus(CommonConstant.FILE_STATUS_UPLOADED);
		jiraIssueOfflineFileTraceLogs.setDate(DateTime.now(DateTimeZone.UTC));
		issueOfflineTraceLogsRepository.save(jiraIssueOfflineFileTraceLogs);
	}

	/**
	 * Collects Jira Data
	 *
	 * @param jiraAdapter
	 *            JiraAdpater to establish connection
	 * @param projectConfig
	 *            User provided ProjectConfiguration
	 * @throws ParseException
	 *             ParseException
	 * @throws JSONException
	 *             JSONException
	 */
	private void collectStoryData(JiraAdapter jiraAdapter, ProjectConfFieldMapping projectConfig) {
		long storyDataStart = System.currentTimeMillis();
		MDC.put("storyDataStartTime", String.valueOf(storyDataStart));
		projectConfig.setIssueCount(0);
		JiraIssueClient jiraIssueClient = jiraIssueClientFactory.getJiraIssueDataClient(projectConfig);
		int count = jiraIssueClient == null ? 0 : jiraIssueClient.processesJiraIssues(projectConfig, jiraAdapter, true);
		projectConfig.setIssueCount(count);
		MDC.put("JiraIssueCount", String.valueOf(count));
		long end = System.currentTimeMillis();
		MDC.put("storyDataEndTime", String.valueOf(end));
	}

}