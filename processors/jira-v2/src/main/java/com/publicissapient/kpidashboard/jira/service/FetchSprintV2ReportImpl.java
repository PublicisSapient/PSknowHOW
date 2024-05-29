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
import java.net.URL;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atlassian.jira.rest.client.api.RestClientException;
import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.BoardDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetailsV2;
import com.publicissapient.kpidashboard.common.model.jira.SprintIssueV2;
import com.publicissapient.kpidashboard.common.repository.jira.SprintV2Repository;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.constant.JiraConstants;
import com.publicissapient.kpidashboard.jira.model.JiraIssueMetadata;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.repository.JiraProcessorRepository;
import com.publicissapient.kpidashboard.jira.util.JiraProcessorUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author pankumar8
 */
@Slf4j
@Service
public class FetchSprintV2ReportImpl implements FetchSprintV2Report {

	private static final String CONTENTS = "contents";
	private static final String COMPLETED_ISSUES = "completedIssues";
	private static final String PUNTED_ISSUES = "puntedIssues";
	private static final String COMPLETED_ISSUES_ANOTHER_SPRINT = "issuesCompletedInAnotherSprint";
	private static final String ADDED_ISSUES = "issueKeysAddedDuringSprint";
	private static final String NOT_COMPLETED_ISSUES = "issuesNotCompletedInCurrentSprint";
	private static final String KEY = "key";
	private static final String ENTITY_DATA = "entityData";
	private static final String PRIORITYID = "priorityId";
	private static final String STATUSID = "statusId";
	private static final String TYPEID = "typeId";
	private static final String ID = "id";
	private static final String STATE = "state";
	private static final String NAME = "name";
	private static final String STARTDATE = "startDate";
	private static final String ENDDATE = "endDate";
	private static final String COMPLETEDATE = "completeDate";
	private static final String ACTIVATEDDATE = "activatedDate";
	private static final String GOAL = "goal";
	@Autowired
	private JiraProcessorConfig jiraProcessorConfig;
	@Autowired
	private SprintV2Repository sprintV2Repository;
	@Autowired
	private JiraCommonService jiraCommonService;
	@Autowired
	private JiraProcessorRepository jiraProcessorRepository;

	@Override
	public Set<SprintDetailsV2> fetchSprintV2s(ProjectConfFieldMapping projectConfig, Set<SprintDetailsV2> sprintDetailsSet,
												   KerberosClient krb5Client, boolean isSprintFetch) throws IOException {
		Set<SprintDetailsV2> sprintToSave = new HashSet<>();
		ObjectId jiraProcessorId = jiraProcessorRepository.findByProcessorName(ProcessorConstants.JIRA_V2).getId();
		if (CollectionUtils.isNotEmpty(sprintDetailsSet)) {
			List<String> sprintIds = sprintDetailsSet.stream().map(SprintDetailsV2::getSprintID)
					.collect(Collectors.toList());
			List<SprintDetailsV2> dbSprints = sprintV2Repository.findBySprintIDIn(sprintIds);
			Map<String, SprintDetailsV2> dbSprintDetailMap = dbSprints.stream()
					.collect(Collectors.toMap(SprintDetailsV2::getSprintID, Function.identity()));
			for (SprintDetailsV2 sprint : sprintDetailsSet) {
				boolean fetchReport = false;
				String boardId = sprint.getOriginBoardId().get(0);
				log.info("processing sprint with sprintId: {}, state: {} and boardId: {} ", sprint.getSprintID(),
						sprint.getState(), boardId);
				sprint.setProcessorId(jiraProcessorId);
				sprint.setBasicProjectConfigId(projectConfig.getBasicProjectConfigId());
				if (null != dbSprintDetailMap.get(sprint.getSprintID())) {
					SprintDetailsV2 dbSprintDetails = dbSprintDetailMap.get(sprint.getSprintID());
					sprint.setId(dbSprintDetails.getId());
					// case 1 : same sprint different board id
					if (!dbSprintDetails.getOriginBoardId().containsAll(sprint.getOriginBoardId())) {
						sprint.getOriginBoardId().addAll(dbSprintDetails.getOriginBoardId());
						fetchReport = true;
					} // case 2 : sprint state is active or changed which is present in db
					else if (sprint.getState().equalsIgnoreCase(SprintDetailsV2.SPRINT_STATE_ACTIVE)
							|| !sprint.getState().equalsIgnoreCase(dbSprintDetails.getState())) {
						sprint.setOriginBoardId(dbSprintDetails.getOriginBoardId());
						fetchReport = true;
					} // fetching for only Iteration data don't change the state of sprint
					else if (!sprint.getState().equalsIgnoreCase(dbSprintDetails.getState()) && isSprintFetch) {
						sprint.setState(dbSprintDetails.getState());
						sprint.setOriginBoardId(dbSprintDetails.getOriginBoardId());
						fetchReport = true;
					} else {
						log.debug("Sprint not to be saved again : {}, status: {} ", sprint.getOriginalSprintId(),
								sprint.getState());
						fetchReport = false;
					}
				} else {
					log.info("sprint id {} not found in db.", sprint.getSprintID());
					fetchReport = true;
				}

				if (fetchReport) {
					try {
						TimeUnit.MILLISECONDS.sleep(jiraProcessorConfig.getSubsequentApiCallDelayInMilli());
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						throw new RuntimeException(e);
					}
					getSprintReport(sprint, projectConfig, boardId, dbSprintDetailMap.get(sprint.getSprintID()),
							krb5Client);
					sprintToSave.add(sprint);
				}
			}
		}

		return sprintToSave;
	}

	private void getSprintReport(SprintDetailsV2 sprint, ProjectConfFieldMapping projectConfig, String boardId,
								 SprintDetailsV2 dbSprintDetails, KerberosClient krb5Client) throws IOException {
		if (sprint.getOriginalSprintId() != null && sprint.getOriginBoardId() != null
				&& sprint.getOriginBoardId().stream().anyMatch(id -> id != null && !id.isEmpty())) {
			// If there's at least one non-null and non-empty string in the list, the
			// condition is true.
			getSprintReport(projectConfig, sprint.getOriginalSprintId(), boardId, sprint, dbSprintDetails, krb5Client);
		}
	}

	private void getSprintReport(ProjectConfFieldMapping projectConfig, String sprintId, String boardId,
								 SprintDetailsV2 sprint, SprintDetailsV2 dbSprintDetails, KerberosClient krb5Client) throws IOException {
		try {
			JiraToolConfig jiraToolConfig = projectConfig.getJira();
			if (null != jiraToolConfig) {
				URL url = getSprintReportUrl(projectConfig, sprintId, boardId);
				getReport(jiraCommonService.getDataFromClient(projectConfig, url, krb5Client), sprint, projectConfig,
						dbSprintDetails, boardId);
			}
			log.info(String.format("Fetched Sprint Report for Sprint Id : %s , Board Id : %s", sprintId, boardId));
		} catch (RestClientException rce) {
			log.error("Client exception when loading sprint report for sprint :{} ", sprintId, rce);
			throw rce;
		} catch (MalformedURLException mfe) {
			log.error("Malformed url for loading sprint report for sprint :{} ", sprintId, mfe);
			throw mfe;
		}
	}

	private void getReport(String sprintReportObj, SprintDetailsV2 sprint, ProjectConfFieldMapping projectConfig,
						   SprintDetailsV2 dbSprintDetails, String boardId) {
		if (StringUtils.isNotBlank(sprintReportObj)) {
			JSONArray completedIssuesJson = new JSONArray();
			JSONArray notCompletedIssuesJson = new JSONArray();
			JSONArray puntedIssuesJson = new JSONArray();
			JSONArray completedIssuesAnotherSprintJson = new JSONArray();
			JSONObject addedIssuesJson = new JSONObject();
			JSONObject entityDataJson = new JSONObject();

			boolean otherBoardExist = findIfOtherBoardExist(sprint);
			Set<SprintIssueV2> completedIssues = initializeIssues(
					null == dbSprintDetails ? new HashSet<>() : dbSprintDetails.getCompletedIssues(), boardId,
					otherBoardExist);
			Set<SprintIssueV2> notCompletedIssues = initializeIssues(
					null == dbSprintDetails ? new HashSet<>() : dbSprintDetails.getNotCompletedIssues(), boardId,
					otherBoardExist);
			Set<SprintIssueV2> puntedIssues = initializeIssues(
					null == dbSprintDetails ? new HashSet<>() : dbSprintDetails.getPuntedIssues(), boardId,
					otherBoardExist);
			Set<SprintIssueV2> completedIssuesAnotherSprint = initializeIssues(
					null == dbSprintDetails ? new HashSet<>() : dbSprintDetails.getCompletedIssuesAnotherSprint(),
					boardId, otherBoardExist);
			Set<SprintIssueV2> totalIssues = initializeIssues(
					null == dbSprintDetails ? new HashSet<>() : dbSprintDetails.getTotalIssues(), boardId,
					otherBoardExist);
			Set<String> addedIssues = initializeAddedIssues(
					null == dbSprintDetails ? new HashSet<>() : dbSprintDetails.getAddedIssues(), totalIssues,
					puntedIssues, otherBoardExist);
			try {
				JSONObject obj = (JSONObject) new JSONParser().parse(sprintReportObj);
				if (null != obj) {
					JSONObject contentObj = (JSONObject) obj.get(CONTENTS);
					completedIssuesJson = (JSONArray) contentObj.get(COMPLETED_ISSUES);
					notCompletedIssuesJson = (JSONArray) contentObj.get(NOT_COMPLETED_ISSUES);
					puntedIssuesJson = (JSONArray) contentObj.get(PUNTED_ISSUES);
					completedIssuesAnotherSprintJson = (JSONArray) contentObj.get(COMPLETED_ISSUES_ANOTHER_SPRINT);
					addedIssuesJson = (JSONObject) contentObj.get(ADDED_ISSUES);
					entityDataJson = (JSONObject) contentObj.get(ENTITY_DATA);
				}

				populateMetaData(entityDataJson, projectConfig);

				setIssues(completedIssuesJson, completedIssues, totalIssues, projectConfig, boardId);

				setIssues(notCompletedIssuesJson, notCompletedIssues, totalIssues, projectConfig, boardId);

				setPuntedCompletedAnotherSprint(puntedIssuesJson, puntedIssues, projectConfig, boardId);

				setPuntedCompletedAnotherSprint(completedIssuesAnotherSprintJson, completedIssuesAnotherSprint,
						projectConfig, boardId);

				addedIssues = setAddedIssues(addedIssuesJson, addedIssues);

				if (null != sprint) {
					sprint.setCompletedIssues(completedIssues);
					sprint.setNotCompletedIssues(notCompletedIssues);
					sprint.setCompletedIssuesAnotherSprint(completedIssuesAnotherSprint);
					sprint.setPuntedIssues(puntedIssues);
					sprint.setAddedIssues(addedIssues);
					sprint.setTotalIssues(totalIssues);
				}

			} catch (ParseException pe) {
				log.error("Parser exception when parsing statuses", pe);
			}
		}
	}

	private Set<String> setAddedIssues(JSONObject addedIssuesJson, Set<String> addedIssues) {
		Set<String> keys = addedIssuesJson.keySet();
		if (CollectionUtils.isNotEmpty(keys)) {
			addedIssues.addAll(keys.stream().collect(Collectors.toSet()));
		}
		return addedIssues;
	}

	private void setPuntedCompletedAnotherSprint(JSONArray puntedIssuesJson, Set<SprintIssueV2> puntedIssues,
			ProjectConfFieldMapping projectConfig, String boardId) {
		puntedIssuesJson.forEach(puntedObj -> {
			JSONObject punObj = (JSONObject) puntedObj;
			if (null != punObj) {
				SprintIssueV2 issue = getSprintIssue(punObj, projectConfig, boardId);
				puntedIssues.remove(issue);
				puntedIssues.add(issue);
			}
		});
	}

	private boolean findIfOtherBoardExist(SprintDetailsV2 sprint) {
		boolean exist = false;
		if (null != sprint && sprint.getOriginBoardId().size() > 1) {
			exist = true;
		}
		return exist;
	}

	private Set<SprintIssueV2> initializeIssues(Set<SprintIssueV2> sprintIssues, String boardId, boolean otherBoardExist) {
		if (otherBoardExist) {
			return CollectionUtils.emptyIfNull(sprintIssues).stream().filter(
					issue -> null != issue.getOriginBoardId() && !issue.getOriginBoardId().equalsIgnoreCase(boardId))
					.collect(Collectors.toSet());
		} else {
			return new HashSet<>();
		}
	}

	private Set<String> initializeAddedIssues(Set<String> addedIssue, Set<SprintIssueV2> totalIssues,
											  Set<SprintIssueV2> puntedIssues, boolean otherBoardExist) {
		if (otherBoardExist) {
			if (null == addedIssue) {
				addedIssue = new HashSet<>();
			}
			Set<String> keySet = CollectionUtils.emptyIfNull(totalIssues).stream().map(issue -> issue.getKey())
					.collect(Collectors.toSet());
			keySet.addAll(CollectionUtils.emptyIfNull(puntedIssues).stream().map(issue -> issue.getKey())
					.collect(Collectors.toSet()));
			addedIssue.retainAll(keySet);
			return addedIssue;
		} else {
			return new HashSet<>();
		}
	}

	private void populateMetaData(JSONObject entityDataJson, ProjectConfFieldMapping projectConfig) {
		JiraIssueMetadata jiraIssueMetadata = new JiraIssueMetadata();
		if (Objects.nonNull(entityDataJson)) {
			jiraIssueMetadata.setIssueTypeMap(
					getMetaDataMap((JSONObject) entityDataJson.get("types"), "typeName"));
			jiraIssueMetadata.setStatusMap(
					getMetaDataMap((JSONObject) entityDataJson.get("statuses"), "statusName"));
			jiraIssueMetadata.setPriorityMap(
					getMetaDataMap((JSONObject) entityDataJson.get("priorities"), "priorityName"));
			projectConfig.setJiraIssueMetadata(jiraIssueMetadata);
		}
	}

	private Map<String, String> getMetaDataMap(JSONObject object, String fieldName) {
		Map<String, String> map = new HashMap<>();
		if (null != object) {
			object.keySet().forEach(key -> {
				JSONObject innerObj = (JSONObject) object.get(key);
				Object fieldObject = innerObj.get(fieldName);
				if (null != fieldObject) {
					map.put(key.toString(), fieldObject.toString());
				}
			});
		}
		return map;
	}

	private void setIssues(JSONArray issuesJson, Set<SprintIssueV2> issues, Set<SprintIssueV2> totalIssues,
						   ProjectConfFieldMapping projectConfig, String boardId) {
		issuesJson.forEach(jsonObj -> {
			JSONObject obj = (JSONObject) jsonObj;
			if (null != obj) {
				SprintIssueV2 issue = getSprintIssue(obj, projectConfig, boardId);
				issues.remove(issue);
				issues.add(issue);
				totalIssues.remove(issue);
				totalIssues.add(issue);
			}
		});
	}

	private SprintIssueV2 getSprintIssue(JSONObject obj, ProjectConfFieldMapping projectConfig,
										 String boardId) {
		SprintIssueV2 issue = new SprintIssueV2();
		issue.setKey(obj.get(KEY).toString());
		issue.setOriginBoardId(boardId);
		Optional<Connection> connectionOptional = projectConfig.getJira().getConnection();
		boolean isCloudEnv = connectionOptional.map(Connection::isCloudEnv).orElse(false);
		if (isCloudEnv) {
			issue.setPriority(getOptionalString(obj, "priorityName"));
			issue.setStatus(getOptionalString(obj, "statusName"));
			issue.setTypeName(getOptionalString(obj, "typeName"));

        } else {
			issue.setPriority(getName(projectConfig, PRIORITYID, obj));
			issue.setStatus(getName(projectConfig, STATUSID, obj));
			issue.setTypeName(getName(projectConfig, TYPEID, obj));

        }
        issue.setAssignee(getOptionalString(obj, "assigneeName"));
        issue.setUpdateDate(formatUpdateDate(getOptionalString(obj, "updatedAt")));
        issue.setSummary(getOptionalString(obj, "summary"));
        setEstimateStatistics(issue, obj, projectConfig);
		setTimeTrackingStatistics(issue, obj);
		return issue;
	}

	private String formatUpdateDate(String timestampStr) {
		if (timestampStr != null) {
			long timestamp = Long.parseLong(timestampStr);
			Instant instant = Instant.ofEpochMilli(timestamp);
			return DateTimeFormatter.ISO_INSTANT.format(instant);
		}
		return null;
	}

	private void setTimeTrackingStatistics(SprintIssueV2 issue, JSONObject obj) {
		Object timeEstimateFieldId = getStatisticsFieldId((JSONObject) obj.get("trackingStatistic"),
				"statFieldId");
		if (null != timeEstimateFieldId) {
			Object timeTrackingObject = getStatistics((JSONObject) obj.get("trackingStatistic"),
					"statFieldValue", "value");
			issue.setRemainingEstimate(
					timeTrackingObject == null ? null : Double.valueOf(timeTrackingObject.toString()));
		}
	}

	private void setEstimateStatistics(SprintIssueV2 issue, JSONObject obj,
									   ProjectConfFieldMapping projectConfig) {
		Object currentEstimateFieldId = getStatisticsFieldId(
				(JSONObject) obj.get("currentEstimateStatistic"), "statFieldId");
		if (null != currentEstimateFieldId) {
			Object estimateObject = getStatistics((JSONObject) obj.get("currentEstimateStatistic"),
					"statFieldValue", "value");
			String storyPointCustomField = StringUtils
					.defaultIfBlank(projectConfig.getFieldMapping().getJiraStoryPointsCustomField(), "");
			if (storyPointCustomField.equalsIgnoreCase(currentEstimateFieldId.toString())) {
				issue.setStoryPoints(estimateObject == null ? null : Double.valueOf(estimateObject.toString()));
			} else {
				issue.setOriginalEstimate(estimateObject == null ? null : Double.valueOf(estimateObject.toString()));
			}
		}
	}

	private Object getStatistics(JSONObject object, String objectName, String fieldName) {
		Object resultObj = null;
		if (null != object) {
			JSONObject innerObj = (JSONObject) object.get(objectName);
			if (null != innerObj) {
				resultObj = innerObj.get(fieldName);
			}
		}
		return resultObj;
	}

	private Object getStatisticsFieldId(JSONObject object, String fieldName) {
		Object resultObj = null;
		if (null != object) {
			resultObj = object.get(fieldName);
		}
		return resultObj;
	}

	private String getName(ProjectConfFieldMapping projectConfig, String entityDataKey,
			JSONObject jsonObject) {
		String name = null;
		Object obj = jsonObject.get(entityDataKey);
		if (null != obj) {
			JiraIssueMetadata metadata = projectConfig.getJiraIssueMetadata();
			switch (entityDataKey) {
			case PRIORITYID:
				name = metadata.getPriorityMap().getOrDefault(obj.toString(), null);
				break;
			case STATUSID:
				name = metadata.getStatusMap().getOrDefault(obj.toString(), null);
				break;
			case TYPEID:
				name = metadata.getIssueTypeMap().getOrDefault(obj.toString(), null);
				break;
			default:
				break;
			}
		}
		return name;
	}

	private String getOptionalString(final JSONObject jsonObject, final String attributeName) {
		final Object res = jsonObject.get(attributeName);
		if (res == null) {
			return null;
		}
		return res.toString();
	}

	private URL getSprintReportUrl(ProjectConfFieldMapping projectConfig, String sprintId, String boardId)
			throws MalformedURLException {

		Optional<Connection> connectionOptional = projectConfig.getJira().getConnection();
		boolean isCloudEnv = connectionOptional.map(Connection::isCloudEnv).orElse(false);
		String serverURL = jiraProcessorConfig.getJiraServerSprintReportApi();
		if (isCloudEnv) {
			serverURL = jiraProcessorConfig.getJiraCloudSprintReportApi();
		}
		serverURL = serverURL.replace("{rapidViewId}", boardId).replace("{sprintId}", sprintId);
		String baseUrl = connectionOptional.map(Connection::getBaseUrl).orElse("");
		return new URL(baseUrl + (baseUrl.endsWith("/") ? "" : "/") + serverURL);

	}

	@Override
	public List<SprintDetailsV2> createSprintV2DetailBasedOnBoard(ProjectConfFieldMapping projectConfig,
																	  KerberosClient krb5Client, BoardDetails boardDetails) throws IOException {
		List<SprintDetailsV2> sprintDetailsBasedOnBoard = new ArrayList<>();
		List<SprintDetailsV2> sprintDetailsList = getSprintV2s(projectConfig, boardDetails.getBoardId(), krb5Client);
		if (CollectionUtils.isNotEmpty(sprintDetailsList)) {
			Set<SprintDetailsV2> sprintDetailSet = limitSprint(sprintDetailsList);
			sprintDetailsBasedOnBoard.addAll(fetchSprintV2s(projectConfig, sprintDetailSet, krb5Client, false));
		}
		return sprintDetailsBasedOnBoard;
	}

	private Set<SprintDetailsV2> limitSprint(List<SprintDetailsV2> sprintDetailsList) {
		Set<SprintDetailsV2> sd = sprintDetailsList.stream()
				.filter(sprintDetails -> sprintDetails.getState().equalsIgnoreCase(SprintDetailsV2.SPRINT_STATE_CLOSED))
				.sorted((sprint1, sprint2) -> sprint2.getStartDate().compareTo(sprint1.getStartDate()))
				.limit(jiraProcessorConfig.getSprintReportCountToBeFetched()).collect(Collectors.toSet());
		sd.addAll(sprintDetailsList.stream()
				.filter(sprintDetails -> !sprintDetails.getState().equalsIgnoreCase(SprintDetailsV2.SPRINT_STATE_CLOSED))
				.collect(Collectors.toSet()));
		return sd;
	}

	@Override
	public List<SprintDetailsV2> getSprintV2s(ProjectConfFieldMapping projectConfig, String boardId,
												  KerberosClient krb5Client) throws IOException {
		List<SprintDetailsV2> sprintDetailsList = new ArrayList<>();
		try {
			JiraToolConfig jiraToolConfig = projectConfig.getJira();
			if (null != jiraToolConfig) {
				boolean isLast = false;
				int startIndex = 0;
				do {
					URL url = getSprintUrl(projectConfig, boardId, startIndex);
					String jsonResponse = jiraCommonService.getDataFromClient(projectConfig, url, krb5Client);
					isLast = populateSprintDetailsList(jsonResponse, sprintDetailsList, projectConfig, boardId);
					startIndex = sprintDetailsList.size();
					TimeUnit.MILLISECONDS.sleep(jiraProcessorConfig.getSubsequentApiCallDelayInMilli());
				} while (!isLast);

			}
		} catch (RestClientException rce) {
			log.error("Client exception when fetching sprints for board", rce);
			throw rce;
		} catch (MalformedURLException mfe) {
			log.error("Malformed url for loading sprint sprints for board", mfe);
			throw mfe;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}
		return sprintDetailsList;
	}

	private boolean populateSprintDetailsList(String sprintReportObj, List<SprintDetailsV2> sprintDetailsSet,
			ProjectConfFieldMapping projectConfig, String boardId) {
		boolean isLast = true;
		if (StringUtils.isNotBlank(sprintReportObj)) {
			JSONArray valuesJson = new JSONArray();
			try {
				JSONObject obj = (JSONObject) new JSONParser().parse(sprintReportObj);
				if (null != obj) {
					valuesJson = (JSONArray) obj.get("values");
				}
				setSprintDetails(valuesJson, sprintDetailsSet, projectConfig, boardId);
				isLast = Boolean.parseBoolean(Objects.requireNonNull(obj).get("isLast").toString());
			} catch (ParseException pe) {
				log.error("Parser exception when parsing statuses", pe);
			}
		}
		return isLast;
	}

	private void setSprintDetails(JSONArray valuesJson, List<SprintDetailsV2> sprintDetailsSet,
			ProjectConfFieldMapping projectConfig, String boardId) {
		valuesJson.forEach(values -> {
			JSONObject sprintJson = (JSONObject) values;
			if (null != sprintJson) {
				SprintDetailsV2 sprintDetails = new SprintDetailsV2();
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
				sprintDetails.setEndDate(sprintJson.get(ENDDATE) == null ? null
						: JiraProcessorUtil.getFormattedDateForSprintDetails(sprintJson.get(ENDDATE).toString()));
				sprintDetails.setCompleteDate(sprintJson.get(COMPLETEDATE) == null ? null
						: JiraProcessorUtil.getFormattedDateForSprintDetails(sprintJson.get(COMPLETEDATE).toString()));
				sprintDetails.setActivatedDate(sprintJson.get(ACTIVATEDDATE) == null ? null
						: JiraProcessorUtil.getFormattedDateForSprintDetails(sprintJson.get(ACTIVATEDDATE).toString()));
				sprintDetails.setGoal(sprintJson.get(GOAL) == null ? null : sprintJson.get(GOAL).toString());
				sprintDetailsSet.add(sprintDetails);
			}
		});
	}

	private URL getSprintUrl(ProjectConfFieldMapping projectConfig, String boardId, int startIndex)
			throws MalformedURLException {

		Optional<Connection> connectionOptional = projectConfig.getJira().getConnection();
		String serverURL = jiraProcessorConfig.getJiraSprintByBoardUrlApi();
		serverURL = serverURL.replace("{startAtIndex}", String.valueOf(startIndex)).replace("{boardId}", boardId);
		String baseUrl = connectionOptional.map(Connection::getBaseUrl).orElse("");
		return new URL(baseUrl + (baseUrl.endsWith("/") ? "" : "/") + serverURL);
	}

}
