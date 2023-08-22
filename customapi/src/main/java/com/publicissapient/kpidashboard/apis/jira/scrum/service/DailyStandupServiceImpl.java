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

package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.CalculatePCDHelper;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.Filter;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.AssigneeCapacity;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.excel.CapacityKpiData;
import com.publicissapient.kpidashboard.common.model.jira.IterationPotentialDelay;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.excel.CapacityKpiDataRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * First Screen of Daily StandUpView
 */

@Component
@Slf4j
public class DailyStandupServiceImpl extends JiraKPIService<Map<String, Long>, List<Object>, Map<String, Object>> {
    public static final String UNCHECKED = "unchecked";
    public static final String UNASSIGNED = "Unassigned";
    private static final String SPRINT = "sprint";
    private static final String ISSUES = "issues";
    public static final String NOT_COMPLETED_JIRAISSUE = "notCompletedJiraIssue";
    public static final String ASSIGNEE_DETAILS = "AssigneeDetails";
    public static final String REMAINING_CAPACITY = "Remaining Capacity";
    public static final String REMAINING_ESTIMATE = "Remaining Estimate";
    public static final String REMAINING_WORK = "Remaining Work";
    public static final String DELAY = "Delay";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private ConfigHelperService configHelperService;

    @Autowired
    private CapacityKpiDataRepository capacityKpiDataRepository;

    @Autowired
    private KpiHelperService kpiHelperService;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getQualifierType() {
        return KPICode.DAILY_STANDUP_VIEW.name();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
                                 TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
        treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
            if (Filters.getFilter(k) == Filters.SPRINT) {
                sprintWiseLeafNodeValue(v, kpiElement, kpiRequest);
            }
        });
        return kpiElement;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Long> calculateKPIMetrics(Map<String, Object> objectMap) {
        return new HashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> fetchKPIDataFromDb(final List<Node> leafNodeList, final String startDate,
                                                  final String endDate, final KpiRequest kpiRequest) {
        Map<String, Object> resultListMap = new HashMap<>();
        Node leafNode = leafNodeList.stream().findFirst().orElse(null);
        if (null != leafNode) {
            log.info("Daily Standup View -> Requested sprint : {}", leafNode.getName());
            SprintDetails sprintDetails;
            SprintDetails dbSprintDetail = getSprintDetailsFromBaseClass();
            if (null != dbSprintDetail && dbSprintDetail.getState().equals(SprintDetails.SPRINT_STATE_ACTIVE)) {
                FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
                        .get(leafNode.getProjectFilter().getBasicProjectConfigId());
                // to modify sprintdetails on the basis of configuration for the project
                sprintDetails = KpiDataHelper.processSprintBasedOnFieldMappings(
                        Collections.singletonList(dbSprintDetail), fieldMapping.getJiraIterationIssuetypeKPI119(),
                        fieldMapping.getJiraIterationCompletionStatusKPI119(), null).get(0);

                List<String> notCompletedIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(
                        sprintDetails, CommonConstant.NOT_COMPLETED_ISSUES);
                List<String> allIssues = new ArrayList<>();
                allIssues.addAll(notCompletedIssues);
                allIssues.addAll(KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
                        CommonConstant.COMPLETED_ISSUES));
                allIssues.addAll(KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
                        CommonConstant.ADDED_ISSUES));
                allIssues.addAll(KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
                        CommonConstant.PUNTED_ISSUES));

                if (CollectionUtils.isNotEmpty(allIssues)) {
                    Set<JiraIssue> totalIssueList = KpiDataHelper.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(
                            sprintDetails, null, getJiraIssuesFromBaseClass(allIssues));
                    Set<JiraIssue> notCompletedJiraIssues = KpiDataHelper
                            .getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails,
                                    sprintDetails.getNotCompletedIssues(),
                                    getJiraIssuesFromBaseClass(notCompletedIssues));

                    resultListMap.put(SPRINT, sprintDetails);
                    resultListMap.put(ISSUES, totalIssueList);
                    resultListMap.put(NOT_COMPLETED_JIRAISSUE, new ArrayList<>(notCompletedJiraIssues));
                    resultListMap.put(ASSIGNEE_DETAILS, capacityKpiDataRepository.findBySprintIDAndBasicProjectConfigId(
                            sprintDetails.getSprintID(), sprintDetails.getBasicProjectConfigId()));
                }
            }
        }
        return resultListMap;
    }

    /**
     * This method populates KPI value to sprint leaf nodes. It also gives the trend
     * analysis at sprint wise.
     *
     * @param sprintLeafNodeList
     * @param kpiElement
     * @param kpiRequest
     */
    @SuppressWarnings(UNCHECKED)
    private void sprintWiseLeafNodeValue(List<Node> sprintLeafNodeList, KpiElement kpiElement, KpiRequest kpiRequest) {
        sprintLeafNodeList.sort((node1, node2) -> node1.getSprintFilter().getStartDate()
                .compareTo(node2.getSprintFilter().getStartDate()));
        List<Node> latestSprintNode = new ArrayList<>();
        Node latestSprint = sprintLeafNodeList.get(0);
        Optional.ofNullable(latestSprint).ifPresent(latestSprintNode::add);
        Object basicProjectConfigId = latestSprint.getProjectFilter().getBasicProjectConfigId();
        FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
        List<UserWiseCardDetail> userWiseCardDetails = new ArrayList<>();

        // fetch from db
        Map<String, Object> resultMap = fetchKPIDataFromDb(latestSprintNode, null, null, kpiRequest);
        SprintDetails sprintDetails = (SprintDetails) resultMap.get(SPRINT);
        if (ObjectUtils.isNotEmpty(sprintDetails)
                && sprintDetails.getState().equalsIgnoreCase(SprintDetails.SPRINT_STATE_ACTIVE)) {
            List<JiraIssue> notCompletedJiraIssue = (List<JiraIssue>) resultMap.get(NOT_COMPLETED_JIRAISSUE);
            CapacityKpiData capacityKpiData = (CapacityKpiData) resultMap.get(ASSIGNEE_DETAILS);
            List<JiraIssue> totalIssueList = new ArrayList<>((Set<JiraIssue>) resultMap.get(ISSUES));
            List<Filter> filtersList = new ArrayList<>();
            Map<String, String> userWiseRole = new HashMap<>();
            Map<String, StandUpViewKpiData> userWiseRemainingCapacity = new HashMap<>();
            Map<String, StandUpViewKpiData> remianingWork = new HashMap<>();
            Map<String, StandUpViewKpiData> assigneeWiseRemaingEstimate = new HashMap<>();
            Map<String, StandUpViewKpiData> assigneeWiseMaxDelay = new HashMap<>();
            Function<JiraIssue, String> function = issue -> {
                if (issue.getAssigneeId() == null) {
                    issue.setAssigneeName(UNASSIGNED);
                    return UNASSIGNED;
                }
                return issue.getAssigneeId();
            };

            Map<String, List<JiraIssue>> assigneeWiseList = totalIssueList.stream()
                    .collect(Collectors.groupingBy(function));
            Map<String, List<JiraIssue>> assigneeWiseNotCompleted = notCompletedJiraIssue.stream()
                    .collect(Collectors.groupingBy(function));
            String estimationCriteria = fieldMapping.getEstimationCriteria();

            // calculate remianing estimate
            calculateAssigneeWiseRemainingEstimate(assigneeWiseNotCompleted, remianingWork, assigneeWiseRemaingEstimate,
                    estimationCriteria);

            // Calculate Delay
            List<IterationPotentialDelay> iterationPotentialDelayList = CalculatePCDHelper
                    .calculatePotentialDelay(sprintDetails, notCompletedJiraIssue, fieldMapping);
            List<IterationPotentialDelay> maxPotentualDelay = new ArrayList<>(
                    CalculatePCDHelper.checkMaxDelayAssigneeWise(iterationPotentialDelayList, fieldMapping).values());
            calculateAssigneeWiseMaxDelay(maxPotentualDelay, assigneeWiseMaxDelay);

            // Calculate Remaining Capacity
            calculateRemainingCapacity(sprintDetails, capacityKpiData, userWiseRole, userWiseRemainingCapacity);

            StandUpViewKpiData defaultRemainingWork = estimationCriteria.equalsIgnoreCase(CommonConstant.STORY_POINT)
                    ? StandUpViewKpiData.builder().value(Constant.DASH).unit1(CommonConstant.SP).build()
                    : StandUpViewKpiData.builder().value(Constant.DASH).unit1(CommonConstant.HOURS).build();

            Set<String> allRoles = new HashSet<>();
            for (Map.Entry<String, List<JiraIssue>> listEntry : assigneeWiseList.entrySet()) {
                UserWiseCardDetail userWiseCardDetail = new UserWiseCardDetail();
                LinkedHashMap<String, StandUpViewKpiData> cardDetails = new LinkedHashMap<>();
                String role = UNASSIGNED;
                if (MapUtils.isNotEmpty(userWiseRole)) {
                    role = userWiseRole.getOrDefault(listEntry.getKey(), UNASSIGNED);
                }
                List<JiraIssue> jiraIssueList = listEntry.getValue();
                String assigneeId = listEntry.getKey();
                String assigneeName = jiraIssueList.stream().findFirst().orElse(new JiraIssue()).getAssigneeName();

                cardDetails.put(REMAINING_CAPACITY, userWiseRemainingCapacity.getOrDefault(assigneeId,
                        StandUpViewKpiData.builder().value(Constant.DASH).unit(CommonConstant.HOURS).build()));
                cardDetails.put(REMAINING_ESTIMATE, assigneeWiseRemaingEstimate.getOrDefault(assigneeId,
                        StandUpViewKpiData.builder().value(Constant.DASH).unit(CommonConstant.DAY).build()));
                cardDetails.put(REMAINING_WORK, remianingWork.getOrDefault(assigneeId, defaultRemainingWork));
                cardDetails.put(DELAY, assigneeWiseMaxDelay.getOrDefault(assigneeId,
                        StandUpViewKpiData.builder().value(Constant.DASH).unit(CommonConstant.DAY).build()));

                userWiseCardDetail.setCardDetails(cardDetails);
                userWiseCardDetail.setAssigneeId(assigneeId);
                userWiseCardDetail.setRole(role);
                allRoles.add(role);
                userWiseCardDetail.setAssigneeName(assigneeName);
                userWiseCardDetails.add(userWiseCardDetail);
            }
            createRoleFilter(filtersList, allRoles);
            userWiseCardDetails.sort(Comparator.comparing(UserWiseCardDetail::getAssigneeName));
            kpiElement.setModalHeads(KPIExcelColumn.DAILY_STANDUP_VIEW.getColumns());
            kpiElement.setFilterData(filtersList);
        }
        kpiElement.setTrendValueList(userWiseCardDetails);

    }

    private void calculateRemainingCapacity(SprintDetails sprintDetails, CapacityKpiData capacityKpiData, Map<String, String> userWiseRole, Map<String, StandUpViewKpiData> userWiseRemainingCapacity) {
        LocalDate sprintStartDate = LocalDate.parse(sprintDetails.getStartDate().split("T")[0],
                DATE_TIME_FORMATTER);
        LocalDate sprintEndDate = LocalDate.parse(sprintDetails.getEndDate().split("T")[0], DATE_TIME_FORMATTER);
        int daysBetween = checkWorkingDays(sprintStartDate, sprintEndDate);
        int daysLeft = checkWorkingDays(LocalDate.now(), sprintEndDate);

        if (capacityKpiData != null && CollectionUtils.isNotEmpty(capacityKpiData.getAssigneeCapacity())) {
            capacityKpiData.getAssigneeCapacity().forEach(assignee -> {
                calculateRemainigCapacity(daysBetween, daysLeft, assignee, userWiseRemainingCapacity);
                if (assignee.getRole() != null)
                    userWiseRole.put(assignee.getUserId(), assignee.getRole().getRoleValue());
            });
        }
    }

    /*
    excluding weekends in calculating of capacity
     */
    private int checkWorkingDays(LocalDate startDate, LocalDate endDate) {
        int incrementCounter = 1;
        if (startDate.getDayOfWeek() == DayOfWeek.SATURDAY || startDate.getDayOfWeek() == DayOfWeek.SUNDAY)
            incrementCounter--;
        return CommonUtils.getWorkingDays(startDate, endDate) + incrementCounter;
    }

    private void createRoleFilter(List<Filter> filtersList, Set<String> allRoles) {
        List<String> values = allRoles.stream().sorted().collect(Collectors.toList());
        Filter filter = new Filter();
        filter.setFilterKey("role");
        filter.setFilterType("singleSelect");
        filter.setOptions(values);
        filtersList.add(filter);
    }

    private void calculateAssigneeWiseMaxDelay(List<IterationPotentialDelay> maxPotentialDelayList,
                                               Map<String, StandUpViewKpiData> assigneeWiseMaxDelay) {
        Map<String, List<IterationPotentialDelay>> collect = maxPotentialDelayList.stream()
                .filter(IterationPotentialDelay::isMaxMarker)
                .collect(Collectors.groupingBy(IterationPotentialDelay::getAssigneeId));

        collect.forEach((assigneeId, delayList) -> {
            int totalDelayInMinutes = (int) delayList.stream()
                    .mapToDouble(delay -> Optional.of(delay.getPotentialDelay()).orElse(0) * 60 * 8).sum();

            assigneeWiseMaxDelay.put(assigneeId,
                    new StandUpViewKpiData(String.valueOf(totalDelayInMinutes), null, CommonConstant.DAY, null));
        });
    }

    /**
     * calculate remainung estimate on the basis of fieldmapping
     */
    private void calculateAssigneeWiseRemainingEstimate(Map<String, List<JiraIssue>> assigneeWiseNotCompleted,
                                                        Map<String, StandUpViewKpiData> remainingWork,
                                                        Map<String, StandUpViewKpiData> assigneeWiseRemainingEstimate, String estimationCriteria) {
        String estimationUnit = estimationCriteria.equalsIgnoreCase(CommonConstant.STORY_POINT) ? CommonConstant.SP
                : CommonConstant.HOURS;

        assigneeWiseNotCompleted.forEach((assigneeId, jiraIssueList) -> {
            int totalEstimate = (int) jiraIssueList.stream()
                    .mapToDouble(issue -> estimationCriteria.equalsIgnoreCase(CommonConstant.STORY_POINT)
                            ? Optional.ofNullable(issue.getStoryPoints()).orElse(0d)
                            : Optional.ofNullable(issue.getOriginalEstimateMinutes()).orElse(0))
                    .sum();
            remainingWork.put(assigneeId, new StandUpViewKpiData(String.valueOf(jiraIssueList.size()),
                    String.valueOf(totalEstimate), null, estimationUnit));

            int totalRemainingEstimate = jiraIssueList.stream()
                    .mapToInt(issue -> Optional.ofNullable(issue.getRemainingEstimateMinutes()).orElse(0)).sum();
            assigneeWiseRemainingEstimate.put(assigneeId,
                    new StandUpViewKpiData(String.valueOf(totalRemainingEstimate), null, CommonConstant.DAY, null));
        });
    }

    /**
     * calculate Remaining Capacity
     * (available capacity/days in sprint)* remaining days
     */
    private void calculateRemainigCapacity(int daysBetween, int daysLeft, AssigneeCapacity assignee,
                                           Map<String, StandUpViewKpiData> userWiseRemainingCapacity) {
        if (assignee.getAvailableCapacity() != null) {
            double remainingCapacity = roundingOff((assignee.getAvailableCapacity() / daysBetween) * daysLeft);
            userWiseRemainingCapacity.putIfAbsent(assignee.getUserId(),
                    new StandUpViewKpiData(String.valueOf(remainingCapacity), null, CommonConstant.HOURS, null));
        }
    }

    @Data
    private class UserWiseCardDetail {
        String assigneeId;
        String assigneeName;
        String role;
        LinkedHashMap<String, StandUpViewKpiData> cardDetails;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class StandUpViewKpiData {
        private String value;
        private String value1;
        private String unit;
        private String unit1;
    }

}
