package com.publicissapient.kpidashboard.jira.fetchData;

import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.*;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.*;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.*;
import com.publicissapient.kpidashboard.common.model.tracelog.PSLogData;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelService;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.common.service.ToolCredentialProvider;
import com.publicissapient.kpidashboard.jira.adapter.helper.JiraRestClientFactory;
import com.publicissapient.kpidashboard.jira.client.jiraissue.JiraIssueClientUtil;
import com.publicissapient.kpidashboard.jira.client.jiraprojectmetadata.JiraIssueMetadata;
import com.publicissapient.kpidashboard.jira.client.sprint.SprintClient;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.repository.JiraProcessorRepository;
import com.publicissapient.kpidashboard.jira.util.AdditionalFilterHelper;
import com.publicissapient.kpidashboard.jira.util.JiraConstants;
import com.publicissapient.kpidashboard.jira.util.JiraProcessorUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.StringEscapeUtils;
import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.logstash.logback.argument.StructuredArguments.kv;

//List<Issue> to List<JiraIssue>
@Slf4j
@Service
public class TransformFetchedIssueToJiraIssueImpl extends JiraIssueClient2 implements TransformFetchedIssueToJiraIssue{

    @Autowired
    private JiraIssueRepository jiraIssueRepository;

    @Autowired
    private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

    @Autowired
    private JiraProcessorRepository jiraProcessorRepository;

    @Autowired
    private AccountHierarchyRepository accountHierarchyRepository;

    @Autowired
    private JiraProcessorConfig jiraProcessorConfig;

    @Autowired
    private SprintClient sprintClient;

    @Autowired
    private JiraRestClientFactory jiraRestClientFactory;

    @Autowired
    private ProcessorExecutionTraceLogService processorExecutionTraceLogService;

    @Autowired
    private HierarchyLevelService hierarchyLevelService;

    @Autowired
    private AdditionalFilterHelper additionalFilterHelper;

    @Autowired
    private SprintRepository sprintRepository;

    @Autowired
    private KanbanJiraIssueRepository kanbanJiraRepo;

    @Autowired
    private ToolCredentialProvider toolCredentialProvider;

    @Autowired
    private AesEncryptionService aesEncryptionService;

    @Autowired
    private JiraCommon jiraCommon;

    @Autowired
    private CreateAccountHierarchy accountHierarchy;

    protected static final String QUERYDATEFORMAT = "yyyy-MM-dd HH:mm";

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

    @Override
    public List<JiraIssue> convertToJiraIssue(List<Issue> currentPagedJiraRs, ProjectConfFieldMapping projectConfig,
                                                Set<SprintDetails> setForCacheClean, boolean dataFromBoard) throws JSONException,InterruptedException {

        List<JiraIssue> jiraIssuesToSave=new ArrayList<>();

        if (null == currentPagedJiraRs) {
            log.error("JIRA Processor | No list of current paged JIRA's issues found");
            return jiraIssuesToSave;
        }

        Map<String, String> issueEpics = new HashMap<>();
        Set<SprintDetails> sprintDetailsSet = new LinkedHashSet<>();
        ObjectId jiraProcessorId = jiraProcessorRepository.findByProcessorName(ProcessorConstants.JIRA).getId();

        for (Issue issue : currentPagedJiraRs) {
            FieldMapping fieldMapping = projectConfig.getFieldMapping();

            if (null == fieldMapping) {
                return jiraIssuesToSave;
            }
            Set<String> issueTypeNames = new HashSet<>();
            for (String issueTypeName : fieldMapping.getJiraIssueTypeNames()) {
                issueTypeNames.add(issueTypeName.toLowerCase(Locale.getDefault()));
            }
            String issueId = JiraProcessorUtil.deodeUTF8String(issue.getId());
            String issueNumber = JiraProcessorUtil.deodeUTF8String(issue.getKey());

            JiraIssue jiraIssue= getJiraIssue(projectConfig, issueId);

            Map<String, IssueField> fields = JiraIssueClientUtil.buildFieldMap(issue.getFields());

            IssueType issueType = issue.getIssueType();
            User assignee = issue.getAssignee();

            IssueField epic = fields.get(fieldMapping.getEpicName());
            IssueField sprint = fields.get(fieldMapping.getSprintName());

            //set URL to jiraIssue
            setURL(issue.getKey(),jiraIssue,projectConfig);

            // Add RCA to JiraIssue
            setRCA(fieldMapping, issue, jiraIssue, fields);

            // Add device platform filed to issue
            setDevicePlatform(fieldMapping, jiraIssue, fields);

            // Add UAT/Third Party identification field to JiraIssue
            setThirdPartyDefectIdentificationField(fieldMapping, issue, jiraIssue, fields);

            if (issueTypeNames.contains(
                    JiraProcessorUtil.deodeUTF8String(issueType.getName()).toLowerCase(Locale.getDefault())) || dataFromBoard) {
                // collectorId
                jiraIssue.setProcessorId(jiraProcessorId);

                // ID
                jiraIssue.setIssueId(JiraProcessorUtil.deodeUTF8String(issue.getId()));

                // Type
                jiraIssue.setTypeId(JiraProcessorUtil.deodeUTF8String(issueType.getId()));
                jiraIssue.setTypeName(JiraProcessorUtil.deodeUTF8String(issueType.getName()));

                setDefectIssueType(jiraIssue, issueType, fieldMapping);

                // Label
                jiraIssue.setLabels(JiraIssueClientUtil.getLabelsList(issue));
                processJiraIssueData(jiraIssue, issue, fields, fieldMapping, jiraProcessorConfig);

                // Set project specific details
                setProjectSpecificDetails(projectConfig, jiraIssue, issue);

                // Set additional filters
                setAdditionalFilters(jiraIssue, issue, projectConfig);

                setStoryLinkWithDefect(issue, jiraIssue);

                // ADD QA identification field to feature
                setQADefectIdentificationField(fieldMapping, issue, jiraIssue, fields);
                setProductionDefectIdentificationField(fieldMapping, issue, jiraIssue, fields);

                setIssueTechStoryType(fieldMapping, issue, jiraIssue, fields);
                jiraIssue.setAffectedVersions(JiraIssueClientUtil.getAffectedVersions(issue));
                setIssueEpics(issueEpics, epic, jiraIssue);

                setJiraIssueValues(jiraIssue, issue, fieldMapping, fields);

                processSprintData(jiraIssue, sprint, projectConfig, sprintDetailsSet);

                setJiraAssigneeDetails(jiraIssue, assignee);

                setEstimates(jiraIssue, issue);

                if (StringUtils.isNotBlank(jiraIssue.getProjectID())) {
                    jiraIssuesToSave.add(jiraIssue);
                }
            }
        }


        if (!dataFromBoard) {
            processSprints(projectConfig, sprintDetailsSet);
        }

        setForCacheClean.addAll(sprintDetailsSet.stream()
                .filter(sprint -> !sprint.getState().equalsIgnoreCase(SprintDetails.SPRINT_STATE_FUTURE))
                .collect(Collectors.toSet()));
        return jiraIssuesToSave;
    }

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
            getSprintReportImpl(projectConfig, sprint.getOriginalSprintId(), boardId, sprint, dbSprintDetails);
        }
    }

    public void getSprintReportImpl(ProjectConfFieldMapping projectConfig, String sprintId, String boardId,
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
                getReport(jiraCommon.getDataFromServer(projectConfig, (HttpURLConnection) connection), sprint, projectConfig,
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

    public void setJiraAssigneeDetails(JiraIssue jiraIssue, User user) {
        if (user == null) {
            jiraIssue.setOwnersUsername(Collections.<String>emptyList());
            jiraIssue.setOwnersShortName(Collections.<String>emptyList());
            jiraIssue.setOwnersID(Collections.<String>emptyList());
            jiraIssue.setOwnersFullName(Collections.<String>emptyList());
        } else {
            List<String> assigneeKey = new ArrayList<>();
            List<String> assigneeName = new ArrayList<>();
            if ((user.getName() == null) || user.getName().isEmpty()) {
                assigneeKey = new ArrayList<>();
                assigneeName = new ArrayList<>();
            } else {
                assigneeKey.add(JiraProcessorUtil.deodeUTF8String(user.getName()));
                assigneeName.add(JiraProcessorUtil.deodeUTF8String(user.getName()));
                jiraIssue.setAssigneeId(user.getName());
            }
            jiraIssue.setOwnersShortName(assigneeName);
            jiraIssue.setOwnersUsername(assigneeName);
            jiraIssue.setOwnersID(assigneeKey);

            List<String> assigneeDisplayName = new ArrayList<>();
            if (user.getDisplayName().isEmpty() || (user.getDisplayName() == null)) {
                assigneeDisplayName.add("");
            } else {
                assigneeDisplayName.add(JiraProcessorUtil.deodeUTF8String(user.getDisplayName()));
                jiraIssue.setAssigneeName(user.getDisplayName());
            }
            jiraIssue.setOwnersFullName(assigneeDisplayName);
        }
    }


    public void setIssueTechStoryType(FieldMapping fieldMapping, Issue issue, JiraIssue jiraIssue,
                                      Map<String, IssueField> fields) {

        if (StringUtils.isNotBlank(fieldMapping.getJiraTechDebtIdentification())) {
            if (fieldMapping.getJiraTechDebtIdentification().trim().equalsIgnoreCase(JiraConstants.LABELS)) {
                if (org.apache.commons.collections.CollectionUtils.containsAny(issue.getLabels(), fieldMapping.getJiraTechDebtValue())) {
                    jiraIssue.setSpeedyIssueType(NormalizedJira.TECHSTORY.getValue());
                }
            } else if (fieldMapping.getJiraTechDebtIdentification().trim().equalsIgnoreCase(JiraConstants.ISSUE_TYPE)
                    && fieldMapping.getJiraTechDebtValue().contains(jiraIssue.getTypeName())) {
                jiraIssue.setSpeedyIssueType(NormalizedJira.TECHSTORY.getValue());
            } else if (fieldMapping.getJiraTechDebtIdentification().trim().equalsIgnoreCase(CommonConstant.CUSTOM_FIELD)
                    && null != fields.get(fieldMapping.getJiraTechDebtCustomField())
                    && fields.get(fieldMapping.getJiraTechDebtCustomField().trim()) != null
                    && fields.get(fieldMapping.getJiraTechDebtCustomField().trim()).getValue() != null
                    && org.apache.commons.collections.CollectionUtils.containsAny(fieldMapping.getJiraTechDebtValue(), JiraIssueClientUtil
                    .getListFromJson(fields.get(fieldMapping.getJiraTechDebtCustomField().trim())))) {
                jiraIssue.setSpeedyIssueType(NormalizedJira.TECHSTORY.getValue());
            }
        }

    }

    public void processJiraIssueData(JiraIssue jiraIssue, Issue issue, Map<String, IssueField> fields,
                                     FieldMapping fieldMapping, JiraProcessorConfig jiraProcessorConfig) throws JSONException {

        String status = issue.getStatus().getName();
        String changeDate = issue.getUpdateDate().toString();
        String createdDate = issue.getCreationDate().toString();
        jiraIssue.setNumber(JiraProcessorUtil.deodeUTF8String(issue.getKey()));
        jiraIssue.setName(JiraProcessorUtil.deodeUTF8String(issue.getSummary()));
        jiraIssue.setStatus(JiraProcessorUtil.deodeUTF8String(status));
        jiraIssue.setState(JiraProcessorUtil.deodeUTF8String(status));

        if (StringUtils.isNotEmpty(fieldMapping.getJiraStatusMappingCustomField())) {
            JSONObject josnObject = (JSONObject) fields.get(fieldMapping.getJiraStatusMappingCustomField()).getValue();
            if (null != josnObject) {
                jiraIssue.setJiraStatus((String) josnObject.get(JiraConstants.VALUE));
            }
        } else {
            jiraIssue.setJiraStatus(issue.getStatus().getName());
        }
        if (issue.getResolution() != null) {
            jiraIssue.setResolution(JiraProcessorUtil.deodeUTF8String(issue.getResolution().getName()));
        }
        setEstimate(jiraIssue, fields, fieldMapping);
        Integer timeSpent = 0;
        if (fields.get(JiraConstants.AGGREGATED_TIME_SPENT) != null
                && fields.get(JiraConstants.AGGREGATED_TIME_SPENT).getValue() != null) {
            timeSpent = ((Integer) fields.get(JiraConstants.AGGREGATED_TIME_SPENT).getValue()) / 60;
        }
        jiraIssue.setTimeSpentInMinutes(timeSpent);

        jiraIssue.setChangeDate(JiraProcessorUtil.getFormattedDate(JiraProcessorUtil.deodeUTF8String(changeDate)));
        jiraIssue.setUpdateDate(JiraProcessorUtil.getFormattedDate(JiraProcessorUtil.deodeUTF8String(changeDate)));
        jiraIssue.setIsDeleted(JiraConstants.FALSE);

        jiraIssue.setOwnersState(Arrays.asList("Active"));

        jiraIssue.setOwnersChangeDate(Collections.<String>emptyList());

        jiraIssue.setOwnersIsDeleted(Collections.<String>emptyList());

        // Created Date
        jiraIssue.setCreatedDate(JiraProcessorUtil.getFormattedDate(JiraProcessorUtil.deodeUTF8String(createdDate)));

    }

    public void setEstimate(JiraIssue jiraIssue, Map<String, IssueField> fields, FieldMapping fieldMapping) {

        Double value = 0d;
        String valueString = "0";
        String estimationCriteria = fieldMapping.getEstimationCriteria();
        if (StringUtils.isNotBlank(estimationCriteria)) {
            String estimationField = fieldMapping.getJiraStoryPointsCustomField();
            if (StringUtils.isNotBlank(estimationField) && fields.get(estimationField) != null
                    && fields.get(estimationField).getValue() != null
                    && !JiraProcessorUtil.deodeUTF8String(fields.get(estimationField).getValue()).isEmpty()) {
                if (JiraConstants.ACTUAL_ESTIMATION.equalsIgnoreCase(estimationCriteria)) {
                    if (fields.get(estimationField).getValue() instanceof Integer) {
                        value = ((Integer) fields.get(estimationField).getValue()) / 3600D;
                    } else {
                        value = ((Double) (fields.get(estimationField).getValue()));
                    }
                    valueString = String.valueOf(value.doubleValue());
                } else if (JiraConstants.BUFFERED_ESTIMATION.equalsIgnoreCase(estimationCriteria)) {
                    if (fields.get(estimationField).getValue() instanceof Integer) {
                        value = ((Double) fields.get(estimationField).getValue()) / 3600D;
                    } else {
                        value = ((Double) (fields.get(estimationField).getValue()));
                    }
                    valueString = String.valueOf(value.doubleValue());

                } else if (JiraConstants.STORY_POINT.equalsIgnoreCase(estimationCriteria)) {
                    value = Double
                            .parseDouble(JiraProcessorUtil.deodeUTF8String(fields.get(estimationField).getValue()));
                    valueString = String.valueOf(value.doubleValue());
                }
            }
        } else {
            // by default storypoints
            IssueField estimationField = fields.get(fieldMapping.getJiraStoryPointsCustomField());
            if (estimationField != null && estimationField.getValue() != null
                    && !JiraProcessorUtil.deodeUTF8String(estimationField.getValue()).isEmpty()) {
                value = Double.parseDouble(JiraProcessorUtil.deodeUTF8String(estimationField.getValue()));
                valueString = String.valueOf(value.doubleValue());
            }
        }
        jiraIssue.setEstimate(valueString);
        jiraIssue.setStoryPoints(value);
    }

    public void setDevicePlatform(FieldMapping fieldMapping, JiraIssue jiraIssue, Map<String, IssueField> fields) {

        try {
            String devicePlatform = null;
            if (fields.get(fieldMapping.getDevicePlatform()) != null
                    && fields.get(fieldMapping.getDevicePlatform()).getValue() != null) {
                devicePlatform = ((JSONObject) fields.get(fieldMapping.getDevicePlatform()).getValue())
                        .getString(JiraConstants.VALUE);
            }
            jiraIssue.setDevicePlatform(devicePlatform);
        } catch (JSONException e) {
            log.error("JIRA Processor | Error while parsing Device Platform data", e);
        }
    }

    private JiraIssue getJiraIssue(ProjectConfFieldMapping projectConfig, String issueId) {
        JiraIssue jiraIssue;
        jiraIssue=findOneJiraIssue(issueId, projectConfig.getBasicProjectConfigId().toString());
        if(jiraIssue==null){
            jiraIssue=new JiraIssue();
        }
        return jiraIssue;
    }

    private void setAdditionalFilters(JiraIssue jiraIssue, Issue issue, ProjectConfFieldMapping projectConfig) {
        List<AdditionalFilter> additionalFilter = additionalFilterHelper.getAdditionalFilter(issue, projectConfig);
        jiraIssue.setAdditionalFilters(additionalFilter);
    }

    private void setProjectSpecificDetails(ProjectConfFieldMapping projectConfig, JiraIssue jiraIssue, Issue issue) {
        String name = projectConfig.getProjectName();
        String id = new StringBuffer(name).append(CommonConstant.UNDERSCORE)
                .append(projectConfig.getBasicProjectConfigId().toString()).toString();

        jiraIssue.setProjectID(id);
        jiraIssue.setProjectName(name);
        jiraIssue.setProjectKey(issue.getProject().getKey());
        jiraIssue.setBasicProjectConfigId(projectConfig.getBasicProjectConfigId().toString());
        jiraIssue.setProjectBeginDate("");
        jiraIssue.setProjectEndDate("");
        jiraIssue.setProjectChangeDate("");
        jiraIssue.setProjectState("");
        jiraIssue.setProjectIsDeleted("False");
        jiraIssue.setProjectPath("");
    }


    /**
     * @param issueEpics
     * @param epic
     * @param jiraIssue
     */
    private void setIssueEpics(Map<String, String> issueEpics, IssueField epic, JiraIssue jiraIssue) {
        if (epic != null && epic.getValue() != null && !JiraProcessorUtil.deodeUTF8String(epic.getValue()).isEmpty()) {
            issueEpics.put(jiraIssue.getIssueId(), JiraProcessorUtil.deodeUTF8String(epic.getValue()));
        }
    }

    private void setDefectIssueType(JiraIssue jiraIssue, IssueType issueType, FieldMapping fieldMapping) {
        // set defecttype to BUG
        if (CollectionUtils.isNotEmpty(fieldMapping.getJiradefecttype())
                && fieldMapping.getJiradefecttype().stream().anyMatch(issueType.getName()::equalsIgnoreCase)) {
            jiraIssue.setTypeName(NormalizedJira.DEFECT_TYPE.getValue());
        }
    }

    private void setJiraIssueValues(JiraIssue jiraIssue, Issue issue, FieldMapping fieldMapping,
                                    Map<String, IssueField> fields) {

        // Priority
        if (issue.getPriority() != null) {
            jiraIssue.setPriority(JiraProcessorUtil.deodeUTF8String(issue.getPriority().getName()));
        }
        // Set EPIC issue data for issue type epic
        if (CollectionUtils.isNotEmpty(fieldMapping.getJiraIssueEpicType())
                && fieldMapping.getJiraIssueEpicType().contains(issue.getIssueType().getName())) {
            setEpicIssueData(fieldMapping, jiraIssue, fields);
        }
        // Release Version
        if (issue.getFixVersions() != null) {
            List<ReleaseVersion> releaseVersions = new ArrayList<>();
            for (Version fixVersionName : issue.getFixVersions()) {
                ReleaseVersion release = new ReleaseVersion();
                release.setReleaseDate(fixVersionName.getReleaseDate());
                release.setReleaseName(fixVersionName.getName());
                releaseVersions.add(release);
            }
            jiraIssue.setReleaseVersions(releaseVersions);
        }
    }

    /**
     * Sets RCA
     *
     * @param fieldMapping
     *            fieldMapping provided by the User
     * @param issue
     *            Atlassian Issue
     * @param jiraIssue
     *            JiraIssue instance
     * @param fields
     *            Map of Issue Fields
     */
    private void setRCA(FieldMapping fieldMapping, Issue issue, JiraIssue jiraIssue, Map<String, IssueField> fields) {

        List<String> rcaList = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(fieldMapping.getJiradefecttype())
                && fieldMapping.getJiradefecttype().stream().anyMatch(issue.getIssueType().getName()::equalsIgnoreCase)
                && fields.get(fieldMapping.getRootCause()) != null
                && fields.get(fieldMapping.getRootCause()).getValue() != null) {
            rcaList.addAll(getRootCauses(fieldMapping, fields));
        }
        if (rcaList.isEmpty()) {
            rcaList.add(JiraConstants.RCA_CAUSE_NONE);
        }

        jiraIssue.setRootCauseList(rcaList);

    }

    private List<String> getRootCauses(FieldMapping fieldMapping, Map<String, IssueField> fields) {
        List<String> rootCauses = new ArrayList<>();

        if (fields.get(fieldMapping.getRootCause()).getValue() instanceof org.codehaus.jettison.json.JSONArray) {
            // Introduce enum to standarize the values of RCA
            org.codehaus.jettison.json.JSONArray jsonArray = (org.codehaus.jettison.json.JSONArray) fields
                    .get(fieldMapping.getRootCause()).getValue();
            for (int i = 0; i < jsonArray.length(); i++) {
                String rcaCause = null;
                try {
                    rcaCause = jsonArray.getJSONObject(i).getString(JiraConstants.VALUE);
                    if (rcaCause != null) {
                        rootCauses.add(rcaCauseStringToSave(rcaCause));
                    }
                } catch (JSONException ex) {
                    log.error("JIRA Processor | Error while parsing RCA Custom_Field", ex);
                }

            }
        } else if (fields.get(fieldMapping.getRootCause())
                .getValue() instanceof org.codehaus.jettison.json.JSONObject) {
            String rcaCause = null;
            try {
                rcaCause = ((org.codehaus.jettison.json.JSONObject) fields.get(fieldMapping.getRootCause()).getValue())
                        .getString(JiraConstants.VALUE);
            } catch (JSONException ex) {
                log.error("JIRA Processor | Error while parsing RCA Custom_Field", ex);
            }

            if (rcaCause != null) {
                rootCauses.add(rcaCauseStringToSave(rcaCause));
            }

        }

        return rootCauses;
    }

    private String rcaCauseStringToSave(String rcaCause) {

        if (rcaCause == null) {
            return null;
        }
        String rcaCauseResult = "";

        if (jiraProcessorConfig.getRcaValuesForCodeIssue().stream().anyMatch(rcaCause::equalsIgnoreCase)) {
            rcaCauseResult = JiraConstants.CODE_ISSUE;
        } else {
            rcaCauseResult = rcaCause;
        }

        return rcaCauseResult.toLowerCase();
    }

    /**
     * @param featureConfig
     * @param issue
     * @param feature
     * @param fields
     */
    private void setQADefectIdentificationField(FieldMapping featureConfig, Issue issue, JiraIssue feature,
                                                Map<String, IssueField> fields) {
        try {
            if (CollectionUtils.isNotEmpty(featureConfig.getJiradefecttype()) && featureConfig.getJiradefecttype()
                    .stream().anyMatch(issue.getIssueType().getName()::equalsIgnoreCase)) {
                if (null != featureConfig.getJiraBugRaisedByQAIdentification() && featureConfig
                        .getJiraBugRaisedByQAIdentification().trim().equalsIgnoreCase(JiraConstants.LABELS)) {
                    List<String> commonLabel = issue.getLabels().stream()
                            .filter(x -> featureConfig.getJiraBugRaisedByQAValue().contains(x))
                            .collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(commonLabel)) {
                        feature.setDefectRaisedByQA(true);
                    }
                } else if (null != featureConfig.getJiraBugRaisedByQAIdentification()
                        && featureConfig.getJiraBugRaisedByQAIdentification().trim()
                        .equalsIgnoreCase(CommonConstant.CUSTOM_FIELD)
                        && fields.get(featureConfig.getJiraBugRaisedByQACustomField().trim()) != null
                        && fields.get(featureConfig.getJiraBugRaisedByQACustomField().trim()).getValue() != null
                        && isBugRaisedByValueMatchesRaisedByCustomField(featureConfig.getJiraBugRaisedByQAValue(),
                        fields.get(featureConfig.getJiraBugRaisedByQACustomField().trim()).getValue())) {
                    feature.setDefectRaisedByQA(true);
                } else {
                    feature.setDefectRaisedByQA(false);
                }
            }

        } catch (Exception e) {
            log.error("Error while parsing QA field {}", e);
        }

    }

    private void setProductionDefectIdentificationField(FieldMapping featureConfig, Issue issue, JiraIssue feature,
                                                        Map<String, IssueField> fields) {
        try {
            if (CollectionUtils.isNotEmpty(featureConfig.getJiradefecttype()) && featureConfig.getJiradefecttype()
                    .stream().anyMatch(issue.getIssueType().getName()::equalsIgnoreCase)) {
                if (null != featureConfig.getProductionDefectIdentifier() && featureConfig
                        .getProductionDefectIdentifier().trim().equalsIgnoreCase(JiraConstants.LABELS)) {
                    List<String> commonLabel = issue.getLabels().stream()
                            .filter(x -> featureConfig.getProductionDefectValue().contains(x))
                            .collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(commonLabel)) {
                        feature.setProductionDefect(true);
                    }
                } else if (null != featureConfig.getProductionDefectIdentifier()
                        && featureConfig.getProductionDefectIdentifier().trim()
                        .equalsIgnoreCase(JiraConstants.CUSTOM_FIELD)
                        && fields.get(featureConfig.getProductionDefectCustomField().trim()) != null
                        && fields.get(featureConfig.getProductionDefectCustomField().trim()).getValue() != null
                        && isBugRaisedByValueMatchesRaisedByCustomField(featureConfig.getProductionDefectValue(),
                        fields.get(featureConfig.getProductionDefectCustomField().trim()).getValue())) {
                    feature.setProductionDefect(true);
                } else if (null != featureConfig.getProductionDefectIdentifier()
                        && featureConfig.getProductionDefectIdentifier().trim()
                        .equalsIgnoreCase(JiraConstants.COMPONENT)
                        && null != featureConfig.getProductionDefectComponentValue()
                        && isComponentMatchWithJiraComponent(issue, featureConfig)) {
                    feature.setProductionDefect(true);

                } else {
                    feature.setProductionDefect(false);
                }
            }

        } catch (Exception e) {
            log.error("Error while parsing Production Defect Identification field {}", e);
        }

    }

    private boolean isComponentMatchWithJiraComponent(Issue issue, FieldMapping featureConfig) {
        boolean isRaisedByThirdParty = false;
        Iterable<BasicComponent> components = issue.getComponents();
        List<BasicComponent> componentList = new ArrayList<>();
        components.forEach(componentList::add);

        if (CollectionUtils.isNotEmpty(componentList)) {
            List<String> componentNameList = componentList.stream().map(BasicComponent::getName)
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(componentNameList) && componentNameList.stream()
                    .anyMatch(featureConfig.getProductionDefectComponentValue()::equalsIgnoreCase)) {
                isRaisedByThirdParty = true;
            }
        }
        return isRaisedByThirdParty;
    }

    /**
     * Sets Story Link with Defect
     *
     * @param issue
     * @param jiraIssue
     */
    private void setStoryLinkWithDefect(Issue issue, JiraIssue jiraIssue) {
        if (NormalizedJira.DEFECT_TYPE.getValue().equalsIgnoreCase(jiraIssue.getTypeName())
                || NormalizedJira.TEST_TYPE.getValue().equalsIgnoreCase(jiraIssue.getTypeName())) {
            Set<String> defectStorySet = new HashSet<>();
            for (IssueLink issueLink : issue.getIssueLinks()) {
                if (CollectionUtils.isNotEmpty(jiraProcessorConfig.getExcludeLinks())
                        && jiraProcessorConfig.getExcludeLinks().stream()
                        .anyMatch(issueLink.getIssueLinkType().getDescription()::equalsIgnoreCase)) {
                    break;
                }
                defectStorySet.add(issueLink.getTargetIssueKey());
            }
            jiraIssue.setDefectStoryID(defectStorySet);
        }
    }

    /**
     * Finds one JiraIssue by issueId
     *
     * @param issueId
     *            jira issueId
     * @param basicProjectConfigId
     *            basicProjectConfigId
     * @return JiraIssue corresponding to provided IssueId in DB
     */
    private JiraIssue findOneJiraIssue(String issueId, String basicProjectConfigId) {
        List<JiraIssue> jiraIssues = jiraIssueRepository.findByIssueIdAndBasicProjectConfigId(StringEscapeUtils.escapeHtml4(issueId),
                basicProjectConfigId);

        if (jiraIssues.size() > 1) {
            log.error("JIRA Processor | More than one Jira Issue item found for id {}", issueId);
        }

        if (!jiraIssues.isEmpty()) {
            return jiraIssues.get(0);
        }
        return null;

    }

    /**
     * Populate field to identify if defect is from Third party or UAT. Get
     * customfield value from jiraBugRaisedByField. This value can be any custom
     * field or "labels"
     *
     * @param fieldMapping
     *            Porject Field mapping
     * @param issue
     *            Atlassian issue
     * @param jiraIssue
     *            jiraIssue
     * @param fields
     *            Map of IssueField Id and IssueField
     */
    private void setThirdPartyDefectIdentificationField(FieldMapping fieldMapping, Issue issue, JiraIssue jiraIssue,
                                                        Map<String, IssueField> fields) {
        if (CollectionUtils.isNotEmpty(fieldMapping.getJiradefecttype()) && fieldMapping.getJiradefecttype().stream()
                .anyMatch(issue.getIssueType().getName()::equalsIgnoreCase)) {
            if (StringUtils.isNotBlank(fieldMapping.getJiraBugRaisedByIdentification())
                    && fieldMapping.getJiraBugRaisedByIdentification().trim()
                    .equalsIgnoreCase(JiraConstants.CUSTOM_FIELD)
                    && fields.get(fieldMapping.getJiraBugRaisedByCustomField().trim()) != null
                    && fields.get(fieldMapping.getJiraBugRaisedByCustomField().trim()).getValue() != null
                    && isBugRaisedByValueMatchesRaisedByCustomField(fieldMapping.getJiraBugRaisedByValue(),
                    fields.get(fieldMapping.getJiraBugRaisedByCustomField().trim()).getValue())) {
                jiraIssue.setDefectRaisedBy(NormalizedJira.THIRD_PARTY_DEFECT_VALUE.getValue());
            } else {
                jiraIssue.setDefectRaisedBy("");
            }

        }
    }

    /**
     * Checks if the bug is raised by third party
     *
     * @param bugRaisedValue
     *            Value of raised defect
     * @param issueFieldValue
     *            Issue Field Value Object
     * @return boolean
     */
    public boolean isBugRaisedByValueMatchesRaisedByCustomField(List<String> bugRaisedValue, Object issueFieldValue) {
        List<String> lowerCaseBugRaisedValue = bugRaisedValue.stream().map(String::toLowerCase)
                .collect(Collectors.toList());
        JSONParser parser = new JSONParser();
        JSONArray array = new JSONArray();
        boolean isRaisedByThirdParty = false;
        org.json.simple.JSONObject jsonObject = new org.json.simple.JSONObject();
        try {
            if (issueFieldValue instanceof org.codehaus.jettison.json.JSONArray) {
                array = (JSONArray) parser.parse(issueFieldValue.toString());
                for (int i = 0; i < array.size(); i++) {

                    jsonObject = (org.json.simple.JSONObject) parser.parse(array.get(i).toString());
                    if (lowerCaseBugRaisedValue
                            .contains(jsonObject.get(JiraConstants.VALUE).toString().toLowerCase())) {
                        isRaisedByThirdParty = true;
                        break;
                    }

                }
            } else if (issueFieldValue instanceof org.codehaus.jettison.json.JSONObject
                    && lowerCaseBugRaisedValue.contains(((org.codehaus.jettison.json.JSONObject) issueFieldValue)
                    .get(JiraConstants.VALUE).toString().toLowerCase())) {
                isRaisedByThirdParty = true;
            }

        } catch (org.json.simple.parser.ParseException | JSONException e) {
            log.error("JIRA Processor | Error while parsing third party field {}", e);
        }
        return isRaisedByThirdParty;
    }

    /**
     * Process sprint details
     *
     * @param jiraIssue
     *            JiraIssue
     * @param sprintField
     *            Issuefield containing sprint Data
     */
    private void processSprintData(JiraIssue jiraIssue, IssueField sprintField, ProjectConfFieldMapping projectConfig,
                                   Set<SprintDetails> sprintDetailsSet) {
        if (sprintField == null || sprintField.getValue() == null
                || JiraConstants.EMPTY_STR.equals(sprintField.getValue())) {
            // Issue #678 - leave sprint blank. Not having a sprint does not
            // imply kanban
            // as a story on a scrum board without a sprint is really on the
            // backlog
            jiraIssue.setSprintID("");
            jiraIssue.setSprintName("");
            jiraIssue.setSprintBeginDate("");
            jiraIssue.setSprintEndDate("");
            jiraIssue.setSprintAssetState("");
        } else {
            Object sValue = sprintField.getValue();
            try {
                List<SprintDetails> sprints = JiraProcessorUtil.processSprintDetail(sValue);
                // Now sort so we can use the most recent one
                // yyyy-MM-dd'T'HH:mm:ss format so string compare will be fine
                Collections.sort(sprints, JiraIssueClientUtil.SPRINT_COMPARATOR);
                setSprintData(sprints, jiraIssue, sValue, projectConfig, sprintDetailsSet);

            } catch (ParseException | JSONException e) {
                log.error("JIRA Processor | Failed to obtain sprint data from {} {}", sValue, e);
            }
        }
        jiraIssue.setSprintChangeDate("");
        jiraIssue.setSprintIsDeleted(JiraConstants.FALSE);
    }

    private void setSprintData(List<SprintDetails> sprints, JiraIssue jiraIssue, Object sValue,
                               ProjectConfFieldMapping projectConfig, Set<SprintDetails> sprintDetailsSet) {
        List<String> sprintsList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(sprints)) {
            for (SprintDetails sprint : sprints) {
                sprintsList.add(sprint.getOriginalSprintId());
                jiraIssue.setSprintIdList(sprintsList);
            }
            // Use the latest sprint
            // if any sprint date is blank set that sprint to JiraIssue
            // because this sprint is
            // future sprint and Jira issue should be tagged with latest
            // sprint
            SprintDetails sprint = sprints.stream().filter(s -> StringUtils.isBlank(s.getStartDate())).findFirst()
                    .orElse(sprints.get(sprints.size() - 1));
            String sprintId = sprint.getOriginalSprintId() + JiraConstants.COMBINE_IDS_SYMBOL
                    + jiraIssue.getProjectName() + JiraConstants.COMBINE_IDS_SYMBOL
                    + projectConfig.getBasicProjectConfigId();

            jiraIssue.setSprintName(sprint.getSprintName() == null ? StringUtils.EMPTY : sprint.getSprintName());
            jiraIssue.setSprintID(sprint.getOriginalSprintId() == null ? StringUtils.EMPTY : sprintId);
            jiraIssue.setSprintBeginDate(sprint.getStartDate() == null ? StringUtils.EMPTY
                    : JiraProcessorUtil.getFormattedDate(sprint.getStartDate()));
            jiraIssue.setSprintEndDate(sprint.getEndDate() == null ? StringUtils.EMPTY
                    : JiraProcessorUtil.getFormattedDate(sprint.getEndDate()));
            jiraIssue.setSprintAssetState(sprint.getState() == null ? StringUtils.EMPTY : sprint.getState());

            sprint.setSprintID(sprintId);
            sprintDetailsSet.add(sprint);
        } else {
            log.error("JIRA Processor | Failed to obtain sprint data for {}", sValue);
        }

    }

    private void setEpicIssueData(FieldMapping fieldMapping, JiraIssue jiraIssue, Map<String, IssueField> fields) {
        if (fields.get(fieldMapping.getEpicJobSize()) != null
                && fields.get(fieldMapping.getEpicJobSize()).getValue() != null) {
            String fieldValue = getFieldValue(fieldMapping.getEpicJobSize(), fields);
            jiraIssue.setJobSize(Double.parseDouble(fieldValue));

        }
        if (fields.get(fieldMapping.getEpicRiskReduction()) != null
                && fields.get(fieldMapping.getEpicRiskReduction()).getValue() != null) {
            String fieldValue = getFieldValue(fieldMapping.getEpicRiskReduction(), fields);
            jiraIssue.setRiskReduction(Double.parseDouble(fieldValue));

        }
        if (fields.get(fieldMapping.getEpicTimeCriticality()) != null
                && fields.get(fieldMapping.getEpicTimeCriticality()).getValue() != null) {
            String fieldValue = getFieldValue(fieldMapping.getEpicTimeCriticality(), fields);
            jiraIssue.setTimeCriticality(Double.parseDouble(fieldValue));

        }
        if (fields.get(fieldMapping.getEpicUserBusinessValue()) != null
                && fields.get(fieldMapping.getEpicUserBusinessValue()).getValue() != null) {
            String fieldValue = getFieldValue(fieldMapping.getEpicUserBusinessValue(), fields);
            jiraIssue.setBusinessValue(Double.parseDouble(fieldValue));

        }
        if (fields.get(fieldMapping.getEpicWsjf()) != null
                && fields.get(fieldMapping.getEpicWsjf()).getValue() != null) {
            String fieldValue = getFieldValue(fieldMapping.getEpicWsjf(), fields);
            jiraIssue.setWsjf(Double.parseDouble(fieldValue));

        }
        double costOfDelay = jiraIssue.getBusinessValue() + jiraIssue.getRiskReduction()
                + jiraIssue.getTimeCriticality();
        jiraIssue.setCostOfDelay(costOfDelay);

    }

    private void setEstimates(JiraIssue jiraIssue, Issue issue) {
        if (null != issue.getTimeTracking()) {
            jiraIssue.setOriginalEstimateMinutes(issue.getTimeTracking().getOriginalEstimateMinutes());
            jiraIssue.setRemainingEstimateMinutes(issue.getTimeTracking().getRemainingEstimateMinutes());
        }
    }


    private void setURL(String ticketNumber, JiraIssue jiraIssue, ProjectConfFieldMapping projectConfig) {
        Optional<Connection> connectionOptional = projectConfig.getJira().getConnection();
        Boolean cloudEnv = connectionOptional.map(Connection::isCloudEnv).get();
        String baseUrl = connectionOptional.map(Connection::getBaseUrl).orElse("");
        baseUrl= baseUrl + (baseUrl.endsWith("/") ? "" : "/");
        if(cloudEnv){
            baseUrl=baseUrl.equals("")?"": baseUrl+jiraProcessorConfig.getJiraCloudDirectTicketLinkKey() + ticketNumber;
        }else{
            baseUrl=baseUrl.equals("")?"": baseUrl+jiraProcessorConfig.getJiraDirectTicketLinkKey() + ticketNumber;
        }
        jiraIssue.setUrl(baseUrl);
    }

}
