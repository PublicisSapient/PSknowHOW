package com.publicissapient.kpidashboard.jira.fetchData;

import com.atlassian.jira.rest.client.api.RestClientException;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintIssue;
import com.publicissapient.kpidashboard.common.model.tracelog.PSLogData;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.jira.client.jiraprojectmetadata.JiraIssueMetadata;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.repository.JiraProcessorRepository;
import com.publicissapient.kpidashboard.jira.util.AdditionalFilterHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Service
public class FetchSprintReportImpl {

    @Autowired
    private JiraProcessorRepository jiraProcessorRepository;

    @Autowired
    private JiraProcessorConfig jiraProcessorConfig;

    @Autowired
    private SprintRepository sprintRepository;

    @Autowired
    private JiraCommonService jiraCommonService;

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


    public void processSprints(ProjectConfFieldMapping projectConfig, Set<SprintDetails> sprintDetailsSet) throws InterruptedException {
        ObjectId jiraProcessorId = jiraProcessorRepository.findByProcessorName(ProcessorConstants.JIRA).getId();
        if (CollectionUtils.isNotEmpty(sprintDetailsSet)) {
            List<String> sprintIds = sprintDetailsSet.stream().map(SprintDetails::getSprintID)
                    .collect(Collectors.toList());
            List<SprintDetails> dbSprints = sprintRepository.findBySprintIDIn(sprintIds);
            Map<String, SprintDetails> dbSprintDetailMap = dbSprints.stream()
                    .collect(Collectors.toMap(SprintDetails::getSprintID, Function.identity()));
            List<SprintDetails> sprintToSave = new ArrayList<>();
            PSLogData sprintLogData = new PSLogData();
            for (SprintDetails sprint : sprintDetailsSet) {
                boolean fetchReport = false;
                String boardId = sprint.getOriginBoardId().get(0);
                sprint.setProcessorId(jiraProcessorId);
                sprint.setBasicProjectConfigId(projectConfig.getBasicProjectConfigId());
                if (null != dbSprintDetailMap.get(sprint.getSprintID())) {
                    SprintDetails dbSprintDetails = dbSprintDetailMap.get(sprint.getSprintID());
                    sprint.setId(dbSprintDetails.getId());
                    // case 1 : same sprint different board id
                    if (!dbSprintDetails.getOriginBoardId().containsAll(sprint.getOriginBoardId())) {
                        sprint.getOriginBoardId().addAll(dbSprintDetails.getOriginBoardId());
                        fetchReport = true;
                    } // case 2 : sprint state is active or changed which is present in db
                    else if (sprint.getState().equalsIgnoreCase(SprintDetails.SPRINT_STATE_ACTIVE)
                            || !sprint.getState().equalsIgnoreCase(dbSprintDetails.getState())) {
                        sprint.setOriginBoardId(dbSprintDetails.getOriginBoardId());
                        fetchReport = true;
                    } else {
                        log.info("Sprint not to be saved again : {}, status: {} ", sprint.getOriginalSprintId(),
                                sprint.getState());
                        fetchReport = false;
                    }
                } else {
                    fetchReport = true;
                }

                if (fetchReport) {
                    TimeUnit.MILLISECONDS.sleep(jiraProcessorConfig.getSubsequentApiCallDelayInMilli());
                    getSprintReport(sprint, projectConfig, boardId,
                            dbSprintDetailMap.get(sprint.getSprintID()));
                    sprintToSave.add(sprint);
                }
            }
            sprintRepository.saveAll(sprintToSave);
            sprintLogData.setAction(CommonConstant.SPRINT_DATA);
            sprintLogData
                    .setSprintListSaved(
                            sprintToSave.stream()
                                    .map(sprintDetails -> sprintDetails.getSprintID() + CommonConstant.ARROW
                                            + sprintDetails.getState() + CommonConstant.NEWLINE)
                                    .collect(Collectors.toList()));
            sprintLogData.setTotalSavedSprints(String.valueOf(sprintToSave.size()));
            sprintLogData
                    .setSprintListFetched(
                            sprintDetailsSet.stream()
                                    .map(sprintDetails -> sprintDetails.getSprintID() + CommonConstant.ARROW
                                            + sprintDetails.getState() + CommonConstant.NEWLINE)
                                    .collect(Collectors.toList()));
            sprintLogData.setTotalFetchedSprints(String.valueOf(sprintDetailsSet.size()));
            log.info("Sprints Fetched and saved", kv(CommonConstant.PSLOGDATA, sprintLogData));
        }
    }

    private void getSprintReport(SprintDetails sprint, ProjectConfFieldMapping projectConfig,
                                 String boardId, SprintDetails dbSprintDetails) {
        if (sprint.getOriginalSprintId() != null && sprint.getOriginBoardId() != null) {
            getSprintReport(projectConfig, sprint.getOriginalSprintId(), boardId, sprint, dbSprintDetails);
        }
    }

    public void getSprintReport(ProjectConfFieldMapping projectConfig, String sprintId, String boardId,
                                SprintDetails sprint, SprintDetails dbSprintDetails) {
        PSLogData sprintReportLog= new PSLogData();
        sprintReportLog.setAction(CommonConstant.SPRINT_REPORTDATA);
        sprintReportLog.setBoardId(boardId);
        sprintReportLog.setSprintId(sprintId);
        try {
            JiraToolConfig jiraToolConfig = projectConfig.getJira();
            if (null != jiraToolConfig) {
                Instant start = Instant.now();
                URL url = getSprintReportUrl(projectConfig, sprintId, boardId);
                sprintReportLog.setUrl(url.toString());
                URLConnection connection;
                connection = url.openConnection();
                getReport(jiraCommonService.getDataFromServer(projectConfig, (HttpURLConnection) connection), sprint, projectConfig,
                        dbSprintDetails, boardId);
                sprintReportLog.setTimeTaken(String.valueOf(Duration.between(start,Instant.now()).toMillis()));
            }
            log.info(String.format("Fetched Sprint Report for Sprint Id : %s , Board Id : %s", sprintId, boardId),
                    kv(CommonConstant.PSLOGDATA, sprintReportLog));
        }
        catch (RestClientException rce) {
            log.error("Client exception when loading sprint report " + rce, kv(CommonConstant.PSLOGDATA, sprintReportLog));
            throw rce;
        } catch (MalformedURLException mfe) {
            log.error("Malformed url for loading sprint report", mfe, kv(CommonConstant.PSLOGDATA, sprintReportLog));
        }
        catch (IOException ioe) {
            log.error("IOException", ioe, kv(CommonConstant.PSLOGDATA, sprintReportLog));
        }
    }

    private void getReport(String sprintReportObj, SprintDetails sprint, ProjectConfFieldMapping projectConfig,
                           SprintDetails dbSprintDetails, String boardId) {
        if (StringUtils.isNotBlank(sprintReportObj)) {
            JSONArray completedIssuesJson = new JSONArray();
            JSONArray notCompletedIssuesJson = new JSONArray();
            JSONArray puntedIssuesJson = new JSONArray();
            JSONArray completedIssuesAnotherSprintJson = new JSONArray();
            org.json.simple.JSONObject addedIssuesJson = new org.json.simple.JSONObject();
            org.json.simple.JSONObject entityDataJson = new org.json.simple.JSONObject();

            boolean otherBoardExist = findIfOtherBoardExist(sprint);
            Set<SprintIssue> completedIssues = initializeIssues(null == dbSprintDetails ? new HashSet<>()
                    : dbSprintDetails.getCompletedIssues(), boardId, otherBoardExist);
            Set<SprintIssue> notCompletedIssues = initializeIssues(null == dbSprintDetails ? new HashSet<>()
                    : dbSprintDetails.getNotCompletedIssues(), boardId, otherBoardExist);
            Set<SprintIssue> puntedIssues = initializeIssues(null == dbSprintDetails ? new HashSet<>()
                    : dbSprintDetails.getPuntedIssues(), boardId, otherBoardExist);
            Set<SprintIssue> completedIssuesAnotherSprint = initializeIssues(null == dbSprintDetails ? new HashSet<>()
                    : dbSprintDetails.getCompletedIssuesAnotherSprint(), boardId, otherBoardExist);
            Set<SprintIssue> totalIssues = initializeIssues(null == dbSprintDetails ? new HashSet<>()
                    : dbSprintDetails.getTotalIssues(), boardId, otherBoardExist);
            Set<String> addedIssues = initializeAddedIssues(null == dbSprintDetails ? new HashSet<>()
                    : dbSprintDetails.getAddedIssues(), totalIssues, puntedIssues, otherBoardExist);
            try {
                org.json.simple.JSONObject obj = (org.json.simple.JSONObject) new JSONParser().parse(sprintReportObj);
                if (null != obj) {
                    org.json.simple.JSONObject contentObj = (org.json.simple.JSONObject) obj.get(CONTENTS);
                    completedIssuesJson = (JSONArray) contentObj.get(COMPLETED_ISSUES);
                    notCompletedIssuesJson = (JSONArray) contentObj.get(NOT_COMPLETED_ISSUES);
                    puntedIssuesJson = (JSONArray) contentObj.get(PUNTED_ISSUES);
                    completedIssuesAnotherSprintJson = (JSONArray) contentObj.get(COMPLETED_ISSUES_ANOTHER_SPRINT);
                    addedIssuesJson = (org.json.simple.JSONObject) contentObj.get(ADDED_ISSUES);
                    entityDataJson = (org.json.simple.JSONObject) contentObj.get(ENTITY_DATA);
                }

                populateMetaData(entityDataJson, projectConfig);

                setIssues(completedIssuesJson, completedIssues, totalIssues, projectConfig, boardId);

                setIssues(notCompletedIssuesJson, notCompletedIssues, totalIssues, projectConfig, boardId);

                setPuntedCompletedAnotherSprint(puntedIssuesJson, puntedIssues, projectConfig, boardId);

                setPuntedCompletedAnotherSprint(completedIssuesAnotherSprintJson, completedIssuesAnotherSprint,
                        projectConfig, boardId);

                addedIssues = setAddedIssues(addedIssuesJson, addedIssues);

                sprint.setCompletedIssues(completedIssues);
                sprint.setNotCompletedIssues(notCompletedIssues);
                sprint.setCompletedIssuesAnotherSprint(completedIssuesAnotherSprint);
                sprint.setPuntedIssues(puntedIssues);
                sprint.setAddedIssues(addedIssues);
                sprint.setTotalIssues(totalIssues);

            } catch (org.json.simple.parser.ParseException pe) {
                log.error("Parser exception when parsing statuses", pe);
            }
        }
    }

    private Set<String> setAddedIssues(org.json.simple.JSONObject addedIssuesJson, Set<String> addedIssues) {
        Set<String> keys = addedIssuesJson.keySet();
        if (CollectionUtils.isNotEmpty(keys)) {
            addedIssues.addAll(keys.stream().collect(Collectors.toSet()));
        }
        return addedIssues;
    }

    private void setPuntedCompletedAnotherSprint(JSONArray puntedIssuesJson, Set<SprintIssue> puntedIssues
            , ProjectConfFieldMapping projectConfig, String boardId) {
        puntedIssuesJson.forEach(puntedObj -> {
            org.json.simple.JSONObject punObj = (org.json.simple.JSONObject) puntedObj;
            if (null != punObj) {
                SprintIssue issue = getSprintIssue(punObj, projectConfig, boardId);
                puntedIssues.remove(issue);
                puntedIssues.add(issue);
            }
        });
    }

    private boolean findIfOtherBoardExist(SprintDetails sprint) {
        boolean exist = false;
        if (null != sprint && sprint.getOriginBoardId().size() > 1) {
            exist = true;
        }
        return exist;
    }

    private Set<SprintIssue> initializeIssues(Set<SprintIssue> sprintIssues, String boardId, boolean otherBoardExist) {
        if (otherBoardExist) {
            return CollectionUtils.emptyIfNull(sprintIssues).stream().filter(issue -> null != issue.getOriginBoardId() &&
                            !issue.getOriginBoardId().equalsIgnoreCase(boardId))
                    .collect(Collectors.toSet());
        } else {
            return new HashSet<>();
        }
    }

    private Set<String> initializeAddedIssues(Set<String> addedIssue, Set<SprintIssue> totalIssues,
                                              Set<SprintIssue> puntedIssues, boolean otherBoardExist) {
        if (otherBoardExist) {
            if (null == addedIssue) {
                addedIssue = new HashSet<>();
            }
            Set<String> keySet = CollectionUtils.emptyIfNull(totalIssues).stream().map(issue -> issue.getNumber())
                    .collect(Collectors.toSet());
            keySet.addAll(CollectionUtils.emptyIfNull(puntedIssues).stream().map(issue -> issue.getNumber())
                    .collect(Collectors.toSet()));
            addedIssue.retainAll(keySet);
            return addedIssue;
        } else {
            return new HashSet<>();
        }
    }

    private void populateMetaData(org.json.simple.JSONObject entityDataJson, ProjectConfFieldMapping projectConfig) {
        JiraIssueMetadata jiraIssueMetadata = new JiraIssueMetadata();
        if (Objects.nonNull(entityDataJson)) {
            jiraIssueMetadata.setIssueTypeMap(getMetaDataMap((org.json.simple.JSONObject) entityDataJson.get("types"), "typeName"));
            jiraIssueMetadata.setStatusMap(getMetaDataMap((org.json.simple.JSONObject) entityDataJson.get("statuses"), "statusName"));
            jiraIssueMetadata
                    .setPriorityMap(getMetaDataMap((org.json.simple.JSONObject) entityDataJson.get("priorities"), "priorityName"));
            projectConfig.setJiraIssueMetadata(jiraIssueMetadata);
        }
    }

    private Map<String, String> getMetaDataMap(org.json.simple.JSONObject object, String fieldName) {
        Map<String, String> map = new HashMap<>();
        if (null != object) {
            object.keySet().forEach(key -> {
                org.json.simple.JSONObject innerObj = (org.json.simple.JSONObject) object.get(key);
                Object fieldObject = innerObj.get(fieldName);
                if (null != fieldObject) {
                    map.put(key.toString(), fieldObject.toString());
                }
            });
        }
        return map;
    }

    private void setIssues(JSONArray issuesJson, Set<SprintIssue> issues,
                           Set<SprintIssue> totalIssues, ProjectConfFieldMapping projectConfig,
                           String boardId) {
        issuesJson.forEach(jsonObj -> {
            org.json.simple.JSONObject obj = (org.json.simple.JSONObject) jsonObj;
            if (null != obj) {
                SprintIssue issue = getSprintIssue(obj, projectConfig, boardId);
                issues.remove(issue);
                issues.add(issue);
                totalIssues.remove(issue);
                totalIssues.add(issue);
            }
        });
    }

    private SprintIssue getSprintIssue(org.json.simple.JSONObject obj, ProjectConfFieldMapping projectConfig,
                                       String boardId) {
        SprintIssue issue = new SprintIssue();
        issue.setNumber(obj.get(KEY).toString());
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
        setEstimateStatistics(issue, obj, projectConfig);
        setTimeTrackingStatistics(issue, obj);
        return issue;
    }

    private void setTimeTrackingStatistics(SprintIssue issue, org.json.simple.JSONObject obj) {
        Object timeEstimateFieldId = getStatisticsFieldId((org.json.simple.JSONObject) obj.get("trackingStatistic"),
                "statFieldId");
        if (null != timeEstimateFieldId) {
            Object timeTrackingObject = getStatistics((org.json.simple.JSONObject) obj.get("trackingStatistic"),
                    "statFieldValue", "value");
            issue.setRemainingEstimate(timeTrackingObject == null ? null : Double.valueOf(timeTrackingObject.toString()));
        }
    }

    private void setEstimateStatistics(SprintIssue issue, org.json.simple.JSONObject obj, ProjectConfFieldMapping projectConfig) {
        Object currentEstimateFieldId = getStatisticsFieldId((org.json.simple.JSONObject) obj.get("currentEstimateStatistic"),
                "statFieldId");
        if (null != currentEstimateFieldId) {
            Object estimateObject = getStatistics((org.json.simple.JSONObject) obj.get("currentEstimateStatistic"),
                    "statFieldValue", "value");
            String storyPointCustomField = StringUtils.defaultIfBlank(projectConfig.getFieldMapping().getJiraStoryPointsCustomField(), "");
            if (storyPointCustomField.equalsIgnoreCase(currentEstimateFieldId.toString())) {
                issue.setStoryPoints(estimateObject == null ? null : Double.valueOf(estimateObject.toString()));
            } else {
                issue.setOriginalEstimate(estimateObject == null ? null : Double.valueOf(estimateObject.toString()));
            }
        }
    }

    private Object getStatistics(org.json.simple.JSONObject object, String objectName, String fieldName) {
        Object resultObj = null;
        if (null != object) {
            org.json.simple.JSONObject innerObj = (org.json.simple.JSONObject) object.get(objectName);
            if (null != innerObj) {
                resultObj = innerObj.get(fieldName);
            }
        }
        return resultObj;
    }

    private Object getStatisticsFieldId(org.json.simple.JSONObject object, String fieldName) {
        Object resultObj = null;
        if (null != object) {
            resultObj = object.get(fieldName);
        }
        return resultObj;
    }

    private String getName(ProjectConfFieldMapping projectConfig, String entityDataKey, org.json.simple.JSONObject jsonObject) {
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

    private String getOptionalString(final org.json.simple.JSONObject jsonObject, final String attributeName) {
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



}
