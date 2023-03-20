package com.publicissapient.kpidashboard.jira.fetchData;

import com.atlassian.jira.rest.client.api.domain.*;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilter;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueRepository;
import com.publicissapient.kpidashboard.jira.client.jiraissue.JiraIssueClientUtil;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.repository.JiraProcessorRepository;
import com.publicissapient.kpidashboard.jira.util.AdditionalFilterHelper;
import com.publicissapient.kpidashboard.jira.util.JiraConstants;
import com.publicissapient.kpidashboard.jira.util.JiraProcessorUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.publicissapient.kpidashboard.jira.fetchData.JiraHelper.*;

@Service
@Slf4j
public class TransformFetchedIssueToKanbanJiraIssueImpl implements TransformFetchedIssueToKanbanJiraIssue{

    @Autowired
    private JiraProcessorRepository jiraProcessorRepository;

    @Autowired
    private KanbanJiraIssueRepository kanbanJiraRepo;

    @Autowired
    private JiraProcessorConfig jiraProcessorConfig;

    @Autowired
    private AdditionalFilterHelper additionalFilterHelper;

    @Autowired
    private CreateKanbanAccountHierarchy accountHierarchy;

    @Override
    public List<KanbanJiraIssue> convertToJiraIssue(List<Issue> currentPagedJiraRs,
                                                       ProjectConfFieldMapping projectConfig)// NOPMD
        // //NOSONAR
            throws JSONException {

        List<KanbanJiraIssue> kanbanIssuesToSave = new ArrayList<>();
//        List<KanbanIssueCustomHistory> kanbanIssueHistoryToSave = new ArrayList<>();

        if (null == currentPagedJiraRs) {
            log.error("JIRA Processor |. No list of current paged JIRA's issues found");
            return kanbanIssuesToSave;
        }
        log.debug("Jira response:", currentPagedJiraRs.size());

        Map<String, String> issueEpics = new HashMap<>();
        ObjectId jiraIssueId = jiraProcessorRepository.findByProcessorName(ProcessorConstants.JIRA).getId();

        for (Issue issue : currentPagedJiraRs) {
            FieldMapping fieldMapping = projectConfig.getFieldMapping();
            if (null == fieldMapping) {
                return kanbanIssuesToSave;
            }
            Set<String> issueTypeNames = getIssueTypeNames(fieldMapping);
            String issueId = JiraProcessorUtil.deodeUTF8String(issue.getId());
            KanbanJiraIssue jiraIssue = getKanbanJiraIssue(projectConfig, issueId);
//            KanbanIssueCustomHistory jiraIssueHistory = getKanbanIssueCustomHistory(projectConfig, issue);

            Map<String, IssueField> fields = buildFieldMap(issue.getFields());

            IssueType issueType = issue.getIssueType();
            User assignee = issue.getAssignee();

            IssueField epic = fields.get(fieldMapping.getEpicName());
            // Add url to Issue
            setURL(issue.getKey(), jiraIssue, projectConfig);
            // Add RCA to Issue
            setRCA(fieldMapping, issue, jiraIssue, fields);

            // Add device platform filed to issue
            setDevicePlatform(fieldMapping, jiraIssue, fields);
            if (issueTypeNames.contains(
                    JiraProcessorUtil.deodeUTF8String(issueType.getName()).toLowerCase(Locale.getDefault()))) {
                // collectorId
                jiraIssue.setProcessorId(jiraIssueId);
                // ID
                jiraIssue.setIssueId(JiraProcessorUtil.deodeUTF8String(issue.getId()));
                // Type
                jiraIssue.setTypeId(JiraProcessorUtil.deodeUTF8String(issueType.getId()));
                jiraIssue.setTypeName(JiraProcessorUtil.deodeUTF8String(issueType.getName()));

                // Label
                jiraIssue.setLabels(getLabelsList(issue));
                processJiraIssueData(jiraIssue, issue, fields, fieldMapping, jiraProcessorConfig);

                // Set project specific details
                setProjectSpecificDetails(projectConfig, jiraIssue, issue);

                // Set additional filters
                setAdditionalFilters(jiraIssue, issue, projectConfig);

                setStoryLinkWithDefect(issue, jiraIssue);

                // Add Tech Debt Story identificatin to jira issue
                setIssueTechStoryType(fieldMapping, issue, jiraIssue, fields);

                // Affected Version
                jiraIssue.setAffectedVersions(getAffectedVersions(issue));

                setJiraIssuuefields(issue, jiraIssue, fieldMapping, fields, epic, issueEpics);

                setJiraAssigneeDetails(jiraIssue, assignee);
                // setting filter data from Jira issue to
                // jira_issue_custom_history
//                setJiraIssueHistory(jiraIssueHistory, jiraIssue, issue, fieldMapping);
                // Add Test Automated data to Jira_issue and TestDetails Repo
                if (StringUtils.isNotBlank(jiraIssue.getProjectID())) {
                    kanbanIssuesToSave.add(jiraIssue);
//                    kanbanIssueHistoryToSave.add(jiraIssueHistory);
                }

            }
        }

        return kanbanIssuesToSave;
    }

    public static Set<String> getIssueTypeNames(FieldMapping fieldMapping){
        Set<String> issueTypeNames = new HashSet<>();
        for (String issueTypeName : fieldMapping.getJiraIssueTypeNames()) {
            issueTypeNames.add(issueTypeName.toLowerCase(Locale.getDefault()));
        }
        return issueTypeNames;
    }

    private void setAdditionalFilters(KanbanJiraIssue jiraIssue, Issue issue, ProjectConfFieldMapping projectConfig) {
        List<AdditionalFilter> additionalFilter = additionalFilterHelper.getAdditionalFilter(issue, projectConfig);
        jiraIssue.setAdditionalFilters(additionalFilter);
    }

    private void setProjectSpecificDetails(ProjectConfFieldMapping projectConfig, KanbanJiraIssue jiraIssue,
                                           Issue issue) {
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

    private KanbanJiraIssue getKanbanJiraIssue(ProjectConfFieldMapping projectConfig, String issueId) {
        KanbanJiraIssue jiraIssue = findOneKanbanIssueRepo(issueId, projectConfig.getBasicProjectConfigId().toString());
        if (jiraIssue == null) {
            jiraIssue = new KanbanJiraIssue();
        }
        return jiraIssue;
    }

    /**
     * @param issue
     * @param jiraIssue
     * @param fieldMapping
     * @param fields
     * @param epic
     * @param issueEpics
     */
    private void setJiraIssuuefields(Issue issue, KanbanJiraIssue jiraIssue, FieldMapping fieldMapping,
                                     Map<String, IssueField> fields, IssueField epic, Map<String, String> issueEpics) {
        // Priority
        if (issue.getPriority() != null) {
            jiraIssue.setPriority(JiraProcessorUtil.deodeUTF8String(issue.getPriority().getName()));
        }
        // Set EPIC issue data for issue type epic
        if (CollectionUtils.isNotEmpty(fieldMapping.getJiraIssueEpicType())
                && fieldMapping.getJiraIssueEpicType().contains(issue.getIssueType().getName())) {
            setEpicIssueData(fieldMapping, jiraIssue, fields);
        }
        // delay processing epic data for performance
        if (epic != null && epic.getValue() != null && !JiraProcessorUtil.deodeUTF8String(epic.getValue()).isEmpty()) {
            issueEpics.put(jiraIssue.getIssueId(), JiraProcessorUtil.deodeUTF8String(epic.getValue()));
        }
    }

    private KanbanJiraIssue findOneKanbanIssueRepo(String issueId, String basicProjectConfigId) {
        List<KanbanJiraIssue> jiraIssues = kanbanJiraRepo
                .findByIssueIdAndBasicProjectConfigId(StringEscapeUtils.escapeHtml4(issueId), basicProjectConfigId);

        // Not sure of the state of the data
        if (jiraIssues.size() > 1) {
            log.warn("JIRA Processor | More than one collector item found for scopeId {}", issueId);
        }

        if (!jiraIssues.isEmpty()) {
            return jiraIssues.get(0);
        }

        return null;
    }

    /**
     * set RCA root cause values
     *
     * @param fieldMapping
     *            fieldMapping provided by the User
     * @param issue
     *            issue
     * @param jiraIssue
     *            JiraIssue instance
     * @param fields
     *            Map of Issue Fields
     */
    private void setRCA(FieldMapping fieldMapping, Issue issue, KanbanJiraIssue jiraIssue,
                        Map<String, IssueField> fields) {
        List<String> rcaList = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(fieldMapping.getKanbanRCACountIssueType()) && fieldMapping
                .getKanbanRCACountIssueType().stream().anyMatch(issue.getIssueType().getName()::equalsIgnoreCase)) {
            if (fields.get(fieldMapping.getRootCause()) != null
                    && fields.get(fieldMapping.getRootCause()).getValue() != null) {
                rcaList.addAll(getRootCauses(fieldMapping, fields));
            } else {
                // when issue type defects but did not set root cause value in
                // Jira
                rcaList.add(JiraConstants.RCA_NOT_AVAILABLE);
            }
        }
        jiraIssue.setRootCauseList(rcaList);
    }

    /**
     * if root cause getting json then story as list of string
     *
     * @param fieldMapping
     * @param fields
     * @return List<String>
     */
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

    /**
     * @param rcaCause
     * @return String
     */
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
     * Sets Device Platform
     *
     * @param fieldMapping
     *            fieldMapping provided by the User
     * @param jiraIssue
     *            JiraIssue instance
     * @param fields
     *            Map of Issue Fields
     */
    public void setDevicePlatform(FieldMapping fieldMapping, KanbanJiraIssue jiraIssue,
                                  Map<String, IssueField> fields) {

        try {
            String devicePlatform = null;
            if (fields.get(fieldMapping.getDevicePlatform()) != null
                    && fields.get(fieldMapping.getDevicePlatform()).getValue() != null) {
                devicePlatform = ((JSONObject) fields.get(fieldMapping.getDevicePlatform()).getValue())
                        .getString(JiraConstants.VALUE);
            }
            jiraIssue.setDevicePlatform(devicePlatform);
        } catch (JSONException e) {
            log.error("JIRA Processor | Error while parsing Device Platform ");
        }
    }

    /**
     * Process Jira issue Data
     *
     * @param jiraIssue
     *            JiraIssue instance
     * @param issue
     *            Atlassian Issue
     * @param fields
     *            Map of Issue Fields
     * @param fieldMapping
     *            fieldMapping provided by the User
     * @param jiraProcessorConfig
     *            Jira processor Configuration
     * @throws JSONException
     *             Error while parsing JSON
     */
    public void processJiraIssueData(KanbanJiraIssue jiraIssue, Issue issue, Map<String, IssueField> fields,
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
        setEstimate(jiraIssue, fields, fieldMapping, jiraProcessorConfig);
        Integer timeSpent = 0;
        if (issue.getTimeTracking() != null && issue.getTimeTracking().getTimeSpentMinutes() != null) {
            timeSpent = issue.getTimeTracking().getTimeSpentMinutes();
        } else if (fields.get(JiraConstants.AGGREGATED_TIME_SPENT) != null
                && fields.get(JiraConstants.AGGREGATED_TIME_SPENT).getValue() != null) {
            timeSpent = ((Integer) fields.get(JiraConstants.AGGREGATED_TIME_SPENT).getValue()) / 60;
        }
        jiraIssue.setTimeSpentInMinutes(timeSpent);

        setEnvironmentImpacted(jiraIssue, fields, fieldMapping);

        jiraIssue.setChangeDate(JiraProcessorUtil.getFormattedDate(JiraProcessorUtil.deodeUTF8String(changeDate)));
        jiraIssue.setIsDeleted(JiraConstants.FALSE);

        jiraIssue.setOwnersState(Arrays.asList("Active"));

        jiraIssue.setOwnersChangeDate(Collections.<String>emptyList());

        jiraIssue.setOwnersIsDeleted(Collections.<String>emptyList());

        // Created Date
        jiraIssue.setCreatedDate(JiraProcessorUtil.getFormattedDate(JiraProcessorUtil.deodeUTF8String(createdDate)));

    }

    /**
     * Sets Issue Tech Story Type after identifying s whether a story is tech
     * story or simple Jira issue. There can be possible 3 ways to identify a
     * tech story 1. Specific 'label' is maintained 2. 'Issue type' itself is a
     * 'Tech Story' 3. A separate 'custom field' is maintained
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
    public void setIssueTechStoryType(FieldMapping fieldMapping, Issue issue, KanbanJiraIssue jiraIssue,
                                      Map<String, IssueField> fields) {
        if (Optional.ofNullable(fieldMapping.getJiraTechDebtIdentification()).isPresent()) {
            if (fieldMapping.getJiraTechDebtIdentification().trim().equalsIgnoreCase(JiraConstants.LABELS)) {
                if (CollectionUtils.containsAny(issue.getLabels(), fieldMapping.getJiraTechDebtValue())) {
                    jiraIssue.setSpeedyIssueType(NormalizedJira.TECHSTORY.getValue());
                }
            } else if (fieldMapping.getJiraTechDebtIdentification().trim().equalsIgnoreCase(JiraConstants.ISSUE_TYPE)
                    && fieldMapping.getJiraTechDebtValue().contains(jiraIssue.getTypeName())) {
                jiraIssue.setSpeedyIssueType(NormalizedJira.TECHSTORY.getValue());
            } else if (fieldMapping.getJiraTechDebtIdentification().trim().equalsIgnoreCase(CommonConstant.CUSTOM_FIELD)
                    && null != fields.get(fieldMapping.getJiraTechDebtCustomField())
                    && fields.get(fieldMapping.getJiraTechDebtCustomField().trim()) != null
                    && fields.get(fieldMapping.getJiraTechDebtCustomField().trim()).getValue() != null
                    && CollectionUtils.containsAny(fieldMapping.getJiraTechDebtValue(), JiraIssueClientUtil
                    .getListFromJson(fields.get(fieldMapping.getJiraTechDebtCustomField().trim())))) {
                jiraIssue.setSpeedyIssueType(NormalizedJira.TECHSTORY.getValue());
            }
        }

    }

    /**
     * This method process owner and user details
     *
     * @param jiraIssue
     *            JiraIssue Object to set Owner details
     * @param user
     *            Jira issue User Object
     */
    public void setJiraAssigneeDetails(KanbanJiraIssue jiraIssue, User user) {
        if (user == null) {
            jiraIssue.setOwnersUsername(Collections.<String>emptyList());
            jiraIssue.setOwnersShortName(Collections.<String>emptyList());
            jiraIssue.setOwnersID(Collections.<String>emptyList());
            jiraIssue.setOwnersFullName(Collections.<String>emptyList());
        } else {
            List<String> assigneeKey = new ArrayList<>();
            List<String> assigneeName = new ArrayList<>();
            if (user.getName().isEmpty() || (user.getName() == null)) {
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

    /**
     * Sets Estimate
     *
     * @param jiraIssue
     *            JiraIssue instance
     * @param fields
     *            Map of Issue Fields
     * @param fieldMapping
     *            fieldMapping provided by the User
     * @param jiraProcessorConfig
     *            Jira Processor Configuration
     */
    public void setEstimate(KanbanJiraIssue jiraIssue, Map<String, IssueField> fields, FieldMapping fieldMapping, // NOSONAR
                            JiraProcessorConfig jiraProcessorConfig) {
        Double value = 0d;
        String valueString = "0";
        String estimationCriteria = fieldMapping.getEstimationCriteria();
        if (StringUtils.isNotBlank(estimationCriteria)) {
            String estimationField = fieldMapping.getJiraStoryPointsCustomField();
            if (StringUtils.isNotBlank(estimationField) && fields.get(estimationField) != null
                    && fields.get(estimationField).getValue() != null
                    && !JiraProcessorUtil.deodeUTF8String(fields.get(estimationField).getValue()).isEmpty()) {
                if (JiraConstants.ACTUAL_ESTIMATION.equalsIgnoreCase(estimationCriteria)) {
                    value = ((Double) fields.get(estimationField).getValue()) / 3600D;
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

    /**
     * Sets the environment impacted custom field.
     *
     * @param jiraIssue
     *            JiraIssue instance
     * @param fields
     *            Map of Issue Fields
     * @param fieldMapping
     *            fieldMapping provided by the User
     */
    private void setEnvironmentImpacted(KanbanJiraIssue jiraIssue, Map<String, IssueField> fields,
                                        FieldMapping fieldMapping) {
        if (fields.get(fieldMapping.getEnvImpacted()) != null
                && fields.get(fieldMapping.getEnvImpacted()).getValue() != null) {
            JSONObject customField;
            try {
                customField = new JSONObject(fields.get(fieldMapping.getEnvImpacted()).getValue().toString());
                jiraIssue.setEnvImpacted(JiraProcessorUtil.deodeUTF8String(customField.get(JiraConstants.VALUE)));
            } catch (JSONException e) {
                log.error("JIRA Processor | Error while parsing the environment custom field Environment", e);
            }

        }
    }

    /**
     * Set Details related to issues with Epic Issue type
     *
     * @param fieldMapping
     * @param jiraIssue
     * @param fields
     */
    private void setEpicIssueData(FieldMapping fieldMapping, KanbanJiraIssue jiraIssue,
                                  Map<String, IssueField> fields) {
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

    private void setStoryLinkWithDefect(Issue issue, KanbanJiraIssue jiraIssue) {
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
     * setting Url to KanbanJiraIssue
     *
     * @param ticketNumber
     * @param kanbanJiraIssue
     * @param projectConfig
     */
    private void setURL(String ticketNumber, KanbanJiraIssue kanbanJiraIssue, ProjectConfFieldMapping projectConfig) {
        Optional<Connection> connectionOptional = projectConfig.getJira().getConnection();
        Boolean cloudEnv = connectionOptional.map(Connection::isCloudEnv).get();
        String baseUrl = connectionOptional.map(Connection::getBaseUrl).orElse("");
        baseUrl = baseUrl + (baseUrl.endsWith("/") ? "" : "/");
        if (cloudEnv) {
            baseUrl = baseUrl.equals("") ? "" : baseUrl + jiraProcessorConfig.getJiraCloudDirectTicketLinkKey() + ticketNumber;
        } else {
            baseUrl = baseUrl.equals("") ? "" : baseUrl + jiraProcessorConfig.getJiraDirectTicketLinkKey() + ticketNumber;
        }
        kanbanJiraIssue.setUrl(baseUrl);
    }

}
