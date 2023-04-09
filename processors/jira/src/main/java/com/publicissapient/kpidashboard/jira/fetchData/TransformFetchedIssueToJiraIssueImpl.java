package com.publicissapient.kpidashboard.jira.fetchData;

import com.atlassian.jira.rest.client.api.domain.*;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.*;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.*;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import com.publicissapient.kpidashboard.jira.client.jiraissue.JiraIssueClientUtil;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.repository.JiraProcessorRepository;
import com.publicissapient.kpidashboard.jira.util.AdditionalFilterHelper;
import com.publicissapient.kpidashboard.jira.util.JiraConstants;
import com.publicissapient.kpidashboard.jira.util.JiraProcessorUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static com.publicissapient.kpidashboard.jira.fetchData.JiraHelper.*;

//List<Issue> to List<JiraIssue>
@Slf4j
@Service
public class TransformFetchedIssueToJiraIssueImpl implements TransformFetchedIssueToJiraIssue{

    @Autowired
    private JiraIssueRepository jiraIssueRepository;

    @Autowired
    private JiraProcessorRepository jiraProcessorRepository;

    @Autowired
    private JiraProcessorConfig jiraProcessorConfig;

    @Autowired
    private AdditionalFilterHelper additionalFilterHelper;

    @Autowired
    private CreateAccountHierarchy createAccountHierarchy;

    @Autowired
    private FetchSprintReportImpl fetchSprintReport;

    @Autowired
    private CreateJiraIssueHistoryImpl createJiraIssueHistory;

    @Autowired
    private SaveData saveData;

    @Autowired
    private CreateAssigneeDetails createAssigneeDetails;

    @Override
    public List<JiraIssue> convertToJiraIssue(List<Issue> currentPagedJiraRs, ProjectConfFieldMapping projectConfig,
                                              boolean dataFromBoard) throws JSONException,InterruptedException {

        List<JiraIssue> jiraIssuesToSave=new ArrayList<>();
        List<JiraIssueCustomHistory> jiraIssueHistoryToSave = new ArrayList<>();

        if (null == currentPagedJiraRs) {
            log.error("JIRA Processor | No list of current paged JIRA's issues found");
            return jiraIssuesToSave;
        }

        Map<String, String> issueEpics = new HashMap<>();
        Set<SprintDetails> sprintDetailsSet=new HashSet<>();
        Set<Assignee> assigneeSetToSave = new HashSet<>();
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

            Map<String, IssueField> fields = buildFieldMap(issue.getFields());

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
                jiraIssue.setLabels(getLabelsList(issue));
                processJiraIssueData(jiraIssue, issue, fields, fieldMapping);

                // Set project specific details
                setProjectSpecificDetails(projectConfig, jiraIssue, issue);

                // Set additional filters
                setAdditionalFilters(jiraIssue, issue, projectConfig);

                setStoryLinkWithDefect(issue, jiraIssue);

                // ADD QA identification field to feature
                setQADefectIdentificationField(fieldMapping, issue, jiraIssue, fields);
                setProductionDefectIdentificationField(fieldMapping, issue, jiraIssue, fields);

                setIssueTechStoryType(fieldMapping, issue, jiraIssue, fields);
                jiraIssue.setAffectedVersions(getAffectedVersions(issue));
                setIssueEpics(issueEpics, epic, jiraIssue);

                setJiraIssueValues(jiraIssue, issue, fieldMapping, fields);

                processSprintData(jiraIssue, sprint, projectConfig, sprintDetailsSet);

                updateAssigneeDetails(projectConfig, jiraIssue, assignee , assigneeSetToSave);

                setEstimates(jiraIssue, issue);

                setDueDates(jiraIssue, issue,fields,fieldMapping);

                JiraIssueCustomHistory jiraIssueCustomHistory=createJiraIssueHistory.createIssueCustomHistory(projectConfig,issueId,jiraIssue,issue,fieldMapping,fields);

                if (StringUtils.isNotBlank(jiraIssue.getProjectID())) {
                    jiraIssuesToSave.add(jiraIssue);
                    jiraIssueHistoryToSave.add(jiraIssueCustomHistory);
                }
            }
        }

        Set<SprintDetails> setForCacheClean = new HashSet<>();
        Set<AccountHierarchy> createAccountHierarchySet=createAccountHierarchy.createAccountHierarchy(jiraIssuesToSave,projectConfig);
        List<SprintDetails> sprintDetailsList =new ArrayList<>();
        //now we will be putting setCacheClean in fetchSprints fn
        if (!dataFromBoard) {
            sprintDetailsList=fetchSprintReport.fetchSprints(projectConfig,sprintDetailsSet,setForCacheClean);
        }
        AssigneeDetails assigneeDetails=createAssigneeDetails.createAssigneeDetails(projectConfig,assigneeSetToSave);
        saveData.saveData(jiraIssuesToSave,jiraIssueHistoryToSave,sprintDetailsList,createAccountHierarchySet,assigneeDetails,setForCacheClean,projectConfig);

        return jiraIssuesToSave;
    }

    private void updateAssigneeDetails(ProjectConfFieldMapping projectConfig, JiraIssue jiraIssue, User assignee,
                                       Set<Assignee> assigneeSetToSave) {
        if (projectConfig.getProjectBasicConfig().isSaveAssigneeDetails()) {
            setJiraAssigneeDetails(jiraIssue, assignee, assigneeSetToSave);
        }
    }

    private void setJiraAssigneeDetails(JiraIssue jiraIssue, User user , Set<Assignee> assigneeSetToSave) {
        if (user == null) {
            jiraIssue.setOwnersUsername(Collections.<String>emptyList());
            jiraIssue.setOwnersShortName(Collections.<String>emptyList());
            jiraIssue.setOwnersID(Collections.<String>emptyList());
            jiraIssue.setOwnersFullName(Collections.<String>emptyList());
        } else {
            List<String> assigneeKey = new ArrayList<>();
            List<String> assigneeName = new ArrayList<>();
            String assigneeUniqueId = getAssignee(user);
            if ((assigneeUniqueId == null) || assigneeUniqueId.isEmpty()) {
                assigneeKey = new ArrayList<>();
                assigneeName = new ArrayList<>();
            } else {
                assigneeKey.add(JiraProcessorUtil.deodeUTF8String(assigneeUniqueId));
                assigneeName.add(JiraProcessorUtil.deodeUTF8String(assigneeUniqueId));
                jiraIssue.setAssigneeId(assigneeUniqueId);
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
            if (StringUtils.isNotEmpty(jiraIssue.getAssigneeId())
                    && StringUtils.isNotEmpty(jiraIssue.getAssigneeName())) {
                assigneeSetToSave.add(new Assignee(jiraIssue.getAssigneeId(), jiraIssue.getAssigneeName()));
            }
        }
    }

    private String getAssignee(User user) {
        String userId = "";
        String query = user.getSelf().getQuery();
        if (StringUtils.isNotEmpty(query) && (query.contains("accountId") || query.contains("username"))) {
            userId = query.split("=")[1];
        }
        return userId;
    }

    private void setIssueTechStoryType(FieldMapping fieldMapping, Issue issue, JiraIssue jiraIssue,
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

    private void processJiraIssueData(JiraIssue jiraIssue, Issue issue, Map<String, IssueField> fields,
                                     FieldMapping fieldMapping) throws JSONException {

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

    private void setEstimate(JiraIssue jiraIssue, Map<String, IssueField> fields, FieldMapping fieldMapping) {

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

    private void setDevicePlatform(FieldMapping fieldMapping, JiraIssue jiraIssue, Map<String, IssueField> fields) {

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
    private boolean isBugRaisedByValueMatchesRaisedByCustomField(List<String> bugRaisedValue, Object issueFieldValue) {
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

    private void setDueDates(JiraIssue jiraIssue, Issue issue, Map<String, IssueField> fields,
                             FieldMapping fieldMapping) {
        if (StringUtils.isNotEmpty(fieldMapping.getJiraDueDateField())) {
            if (fieldMapping.getJiraDueDateField().equalsIgnoreCase(CommonConstant.DUE_DATE)
                    && ObjectUtils.isNotEmpty(issue.getDueDate())) {
                jiraIssue.setDueDate(JiraProcessorUtil.deodeUTF8String(issue.getDueDate()).split("T")[0]
                        .concat(DateUtil.ZERO_TIME_ZONE_FORMAT));
            } else if (StringUtils.isNotEmpty(fieldMapping.getJiraDueDateCustomField())
                    && ObjectUtils.isNotEmpty(fields.get(fieldMapping.getJiraDueDateCustomField()))) {
                IssueField issueField = fields.get(fieldMapping.getJiraDueDateCustomField());
                if (ObjectUtils.isNotEmpty(issueField.getValue())) {
                    jiraIssue.setDueDate(JiraProcessorUtil.deodeUTF8String(issueField.getValue()).split("T")[0]
                            .concat(DateUtil.ZERO_TIME_ZONE_FORMAT));
                }
            }
        }
        if (StringUtils.isNotEmpty(fieldMapping.getJiraDevDueDateCustomField())
                && ObjectUtils.isNotEmpty(fields.get(fieldMapping.getJiraDevDueDateCustomField()))) {
            IssueField issueField = fields.get(fieldMapping.getJiraDevDueDateCustomField());
            if (ObjectUtils.isNotEmpty(issueField.getValue())) {
                jiraIssue.setDevDueDate((JiraProcessorUtil.deodeUTF8String(issueField.getValue()).split("T")[0]
                        .concat(DateUtil.ZERO_TIME_ZONE_FORMAT)));
            }
        }
    }


}
