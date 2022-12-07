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
package com.publicissapient.kpidashboard.jira.client.sprint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.atlassian.jira.rest.client.api.RestClientException;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.BoardDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintIssue;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.util.JiraConstants;
import com.publicissapient.kpidashboard.jira.util.JiraProcessorUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.jira.adapter.JiraAdapter;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.repository.JiraProcessorRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * @author yasbano
 *
 */

@Service
@Slf4j
public class SprintClientImpl implements SprintClient {
	private static final String ID = "id";

	private static final String STATE = "state";
	private static final String RAPIDVIEWID = "rapidViewId";
	private static final String NAME = "name";
	private static final String STARTDATE = "startDate";
	private static final String ENDDATE = "endDate";
	private static final String COMPLETEDATE = "completeDate";
	private static final String ACTIVATEDDATE = "activatedDate";
	private static final String GOAL = "goal";
	@Autowired
	private SprintRepository sprintRepository;
	@Autowired
	private JiraProcessorRepository jiraProcessorRepository;
	@Autowired
	private JiraProcessorConfig jiraProcessorConfig;
	@Autowired
	private AesEncryptionService aesEncryptionService;

	/**
	 * This method handles sprint detailsList
	 * 
	 * @param projectConfig
	 *            projectConfig
	 * @param sprintDetailsSet
	 *            sprintDetailsSet
	 */
	@Override
	public void processSprints(ProjectConfFieldMapping projectConfig, Set<SprintDetails> sprintDetailsSet,
			JiraAdapter jiraAdapter) throws InterruptedException{
		ObjectId jiraProcessorId = jiraProcessorRepository.findByProcessorName(ProcessorConstants.JIRA).getId();
		if (CollectionUtils.isNotEmpty(sprintDetailsSet)) {
			List<String> sprintIds = sprintDetailsSet.stream().map(SprintDetails::getSprintID)
					.collect(Collectors.toList());
			List<SprintDetails> dbSprints = sprintRepository.findBySprintIDIn(sprintIds);
			Map<String, SprintDetails> dbSprintDetailMap = dbSprints.stream()
					.collect(Collectors.toMap(SprintDetails::getSprintID, Function.identity()));
			List<SprintDetails> sprintToSave = new ArrayList<>();
			for(SprintDetails sprint : sprintDetailsSet ) {
				boolean fetchReport = false;
				String boardId = sprint.getOriginBoardId().get(0);
				sprint.setProcessorId(jiraProcessorId);
				sprint.setBasicProjectConfigId(projectConfig.getBasicProjectConfigId());
				if (null != dbSprintDetailMap.get(sprint.getSprintID())) {
					SprintDetails dbSprintDetails =  dbSprintDetailMap.get(sprint.getSprintID());
					sprint.setId(dbSprintDetails.getId());
					//case 1 : same sprint different board id
					if (!dbSprintDetails.getOriginBoardId().containsAll(sprint.getOriginBoardId())) {
						sprint.getOriginBoardId().addAll(dbSprintDetails.getOriginBoardId());
						fetchReport = true;
					}//case 2 : sprint state is active or changed which is present in db
					else if (sprint.getState().equalsIgnoreCase(SprintDetails.SPRINT_STATE_ACTIVE) ||
							!sprint.getState().equalsIgnoreCase(dbSprintDetails.getState())) {
						sprint.setOriginBoardId(dbSprintDetails.getOriginBoardId());
						fetchReport = true;
					}else {
						log.info("Sprint not to be saved again : {}, status: {} ", sprint.getOriginalSprintId(),
								sprint.getState());
						fetchReport = false;
					}
				} else {
					fetchReport = true;
				}

				if(fetchReport){
					log.info("Sprint report Api call delay started");
					TimeUnit.MILLISECONDS.sleep(jiraProcessorConfig.getSubsequentApiCallDelayInMilli());
					log.info("Sprint report Api call delay ended");
					getSprintReport(sprint, jiraAdapter, projectConfig, boardId,
							dbSprintDetailMap.get(sprint.getSprintID()));
					sprintToSave.add(sprint);
				}
			}
			sprintRepository.saveAll(sprintToSave);
			log.info("{} sprints found", sprintDetailsSet.size());
		}
	}

	private void getSprintReport(SprintDetails sprint, JiraAdapter jiraAdapter, ProjectConfFieldMapping projectConfig,
								 String boardId,SprintDetails dbSprintDetails) {
		if(sprint.getOriginalSprintId() != null && sprint.getOriginBoardId() != null){
			jiraAdapter.getSprintReport(projectConfig, sprint.getOriginalSprintId(),
					boardId, sprint, dbSprintDetails);
		}
	}


	public void createSprintDetailBasedOnBoard(ProjectConfFieldMapping projectConfig, JiraAdapter jiraAdapter)
			throws InterruptedException {
		List<BoardDetails> boardDetailsList = projectConfig.getProjectToolConfig().getBoards();
		for(BoardDetails boardDetails : boardDetailsList){
			List<SprintDetails> sprintDetailsList = getSprints(projectConfig,boardDetails.getBoardId());
			if (CollectionUtils.isNotEmpty(sprintDetailsList)) {
				Set<SprintDetails> sprintDetailSet = limitSprint(sprintDetailsList);
				processSprints(projectConfig, sprintDetailSet, jiraAdapter);
			}
		}
	}

	private Set<SprintDetails> limitSprint(List<SprintDetails> sprintDetailsList) {
		Set<SprintDetails> sd = sprintDetailsList.stream()
				.filter(sprintDetails -> sprintDetails.getState().equalsIgnoreCase(SprintDetails.SPRINT_STATE_CLOSED))
				.sorted((sprint1,sprint2)->sprint2.getStartDate().compareTo(sprint1.getStartDate()))
				.limit(jiraProcessorConfig.getSprintReportCountToBeFetched()).collect(Collectors.toSet());
		sd.addAll(sprintDetailsList.stream()
				.filter(sprintDetails -> !sprintDetails.getState().equalsIgnoreCase(SprintDetails.SPRINT_STATE_CLOSED))
				.collect(Collectors.toSet()));
		return sd;
	}

	private List<SprintDetails> getSprints(ProjectConfFieldMapping projectConfig, String boardId) {
		List<SprintDetails> sprintDetailsList = new ArrayList<>();
		try {
			JiraToolConfig jiraToolConfig = projectConfig.getJira();
			if (null != jiraToolConfig) {
				boolean isLast = false;
				int startIndex = 0;
				do {
					URL url = getSprintUrl(projectConfig, boardId, startIndex);
					URLConnection connection;
					connection = url.openConnection();
					String jsonResponse = getDataFromServer(projectConfig, (HttpURLConnection) connection);
					isLast = populateSprintDetailsList(jsonResponse, sprintDetailsList, projectConfig, boardId);
					startIndex = sprintDetailsList.size() + 1;
				}while(!isLast);
			}
		} catch (RestClientException rce) {
			log.error("Client exception when loading sprint report", rce);
			throw rce;
		} catch (MalformedURLException mfe) {
			log.error("Malformed url for loading sprint report", mfe);
		} catch (IOException ioe) {
			log.error("IOException", ioe);
		}
		return sprintDetailsList;
	}

	private boolean populateSprintDetailsList(String sprintReportObj,List<SprintDetails> sprintDetailsSet,
								ProjectConfFieldMapping projectConfig,String boardId) {
		boolean isLast = true;
		if (StringUtils.isNotBlank(sprintReportObj)) {
			JSONArray valuesJson = new JSONArray();
			try {
				JSONObject obj = (JSONObject)new JSONParser().parse(sprintReportObj);
				if(null!=obj) {
					valuesJson = (JSONArray)obj.get("values");
				}
				setSprintDetails(valuesJson, sprintDetailsSet, projectConfig, boardId);
				isLast = Boolean.valueOf(obj.get("isLast").toString());
			} catch (ParseException pe) {
				log.error("Parser exception when parsing statuses", pe);
			}
		}
		return isLast;
	}

	private void setSprintDetails(JSONArray valuesJson,List<SprintDetails> sprintDetailsSet,
								  ProjectConfFieldMapping projectConfig,String boardId) {
		valuesJson.forEach(values->{
			JSONObject sprintJson = (JSONObject) values;
			if(null != sprintJson) {
				SprintDetails sprintDetails = new SprintDetails();
				sprintDetails.setSprintName(sprintJson.get(NAME).toString());
				List<String> boardList = new ArrayList<>();
				boardList.add(boardId);
				sprintDetails.setOriginBoardId(boardList);
				sprintDetails.setOriginalSprintId(sprintJson.get(ID).toString());
				sprintDetails.setState(sprintJson.get(STATE).toString().toUpperCase());
				String sprintId = sprintDetails.getOriginalSprintId() + JiraConstants.COMBINE_IDS_SYMBOL
						+ projectConfig.getProjectName() + JiraConstants.COMBINE_IDS_SYMBOL
						+ projectConfig.getBasicProjectConfigId();
				sprintDetails.setSprintID(sprintId);
				sprintDetails.setStartDate(sprintJson.get(STARTDATE) == null ? null
						: JiraProcessorUtil.getFormattedDateForSprintDetails(sprintJson.get(STARTDATE).toString()));
				sprintDetails.setEndDate(
						sprintJson.get(ENDDATE) == null ? null : JiraProcessorUtil.getFormattedDateForSprintDetails(sprintJson.get(ENDDATE).toString()));
				sprintDetails.setCompleteDate(sprintJson.get(COMPLETEDATE) == null ? null
						: JiraProcessorUtil.getFormattedDateForSprintDetails(sprintJson.get(COMPLETEDATE).toString()));
				sprintDetails.setActivatedDate(sprintJson.get(ACTIVATEDDATE) == null ? null
						: JiraProcessorUtil.getFormattedDateForSprintDetails(sprintJson.get(ACTIVATEDDATE).toString()));
				sprintDetails.setGoal(sprintJson.get(GOAL) == null ? null : sprintJson.get(GOAL).toString());
				sprintDetailsSet.add(sprintDetails);
			}
		});
	}
	private String getDataFromServer(ProjectConfFieldMapping projectConfig, HttpURLConnection connection)
			throws IOException {
		HttpURLConnection request = connection;
		Optional<Connection> connectionOptional = projectConfig.getJira().getConnection();

		String username = connectionOptional.map(Connection::getUsername).orElse(null);
		String password = decryptJiraPassword(connectionOptional.map(Connection::getPassword).orElse(null));
		request.setRequestProperty("Authorization", "Basic " + encodeCredentialsToBase64(username, password)); // NOSONAR
		request.connect();
		StringBuilder sb = new StringBuilder();
		try (InputStream in = (InputStream) request.getContent();
			 BufferedReader inReader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));) {
			int cp;
			while ((cp = inReader.read()) != -1) {
				sb.append((char) cp);
			}
		} catch (IOException ie) {
			log.error("Read exception when connecting to server {}", ie);
		}
		return sb.toString();
	}
	private String encodeCredentialsToBase64(String username, String password) {
		String cred = username + ":" + password;
		return Base64.getEncoder().encodeToString(cred.getBytes());
	}


	private String decryptJiraPassword(String encryptedPassword) {
		return aesEncryptionService.decrypt(encryptedPassword, jiraProcessorConfig.getAesEncryptionKey());
	}


	private URL getSprintUrl(ProjectConfFieldMapping projectConfig, String boardId, int startIndex)
			throws MalformedURLException {

		Optional<Connection> connectionOptional = projectConfig.getJira().getConnection();
		String serverURL = jiraProcessorConfig.getJiraSprintByBoardUrlApi();
		serverURL = serverURL.replace("{startAtIndex}",String.valueOf(startIndex)).replace("{boardId}",boardId);
		String baseUrl = connectionOptional.map(Connection::getBaseUrl).orElse("");
		return new URL(baseUrl + (baseUrl.endsWith("/") ? "" : "/")  + serverURL);
	}

}
