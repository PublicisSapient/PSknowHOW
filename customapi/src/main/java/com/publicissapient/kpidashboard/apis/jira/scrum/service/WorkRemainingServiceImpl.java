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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.IterationKpiData;
import com.publicissapient.kpidashboard.apis.model.IterationKpiFilters;
import com.publicissapient.kpidashboard.apis.model.IterationKpiFiltersOptions;
import com.publicissapient.kpidashboard.apis.model.IterationKpiModalValue;
import com.publicissapient.kpidashboard.apis.model.IterationKpiValue;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.IterationPotentialDelay;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

@Component
public class WorkRemainingServiceImpl extends JiraKPIService<Integer, List<Object>, Map<String, Object>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(WorkRemainingServiceImpl.class);

	private static final String SEARCH_BY_ISSUE_TYPE = "Filter by issue type";
	private static final String SEARCH_BY_PRIORITY = "Filter by status";
	public static final String UNCHECKED = "unchecked";
	private static final String ISSUES = "issues";
	private static final String ISSUE_COUNT = "Issue Count";
	private static final String REMAINING_WORK = "Remaining Work";
	private static final String POTENTIAL_DELAY = "Potential Delay";
	private static final String OVERALL = "Overall";
	private static final String SPRINT_DETAILS = "sprint details";
	private static final String CLOSED = "closed";

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private SprintRepository sprintRepository;

	@Autowired
	private ConfigHelperService configHelperService;

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		DataCount trendValue = new DataCount();
		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {

			Filters filters = Filters.getFilter(k);
			if (Filters.SPRINT == filters) {
				projectWiseLeafNodeValue(v, trendValue, kpiElement, kpiRequest);
			}
		});
		return kpiElement;
	}

	@Override
	public String getQualifierType() {
		return KPICode.WORK_REMAINING.name();
	}

	@Override
	public Integer calculateKPIMetrics(Map<String, Object> subCategoryMap) {
		return null;
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		Node leafNode = leafNodeList.stream().findFirst().orElse(null);
		if (null != leafNode) {
			LOGGER.info("Work Remaining -> Requested sprint : {}", leafNode.getName());
			String basicProjectConfigId = leafNode.getProjectFilter()
					.getBasicProjectConfigId().toString();
			String sprintId = leafNode.getSprintFilter().getId();
			SprintDetails sprintDetails = sprintRepository.findBySprintID(sprintId);
			if (null != sprintDetails) {
				List<String> notCompletedIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.NOT_COMPLETED_ISSUES);
				if (CollectionUtils.isNotEmpty(notCompletedIssues)) {
					List<JiraIssue> issueList = jiraIssueRepository
							.findByNumberInAndBasicProjectConfigId(notCompletedIssues, basicProjectConfigId);
					Set<JiraIssue> filtersIssuesList = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails,
									sprintDetails.getNotCompletedIssues(), issueList);
					resultListMap.put(ISSUES, new ArrayList<>(filtersIssuesList));
					resultListMap.put(SPRINT_DETAILS, sprintDetails);
				}
			}
		}
		return resultListMap;
	}

	/**
	 * Populates KPI value to sprint leaf nodes and gives the trend analysis at
	 * sprint level.
	 *
	 * @param sprintLeafNodeList
	 * @param trendValue
	 * @param kpiElement
	 * @param kpiRequest
	 */
	@SuppressWarnings("unchecked")
	private void projectWiseLeafNodeValue(List<Node> sprintLeafNodeList, DataCount trendValue, KpiElement kpiElement,
			KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();

		sprintLeafNodeList.sort((node1, node2) -> node1.getSprintFilter().getStartDate()
				.compareTo(node2.getSprintFilter().getStartDate()));
		List<Node> latestSprintNode = new ArrayList<>();
		Node latestSprint = sprintLeafNodeList.get(0);
		Optional.ofNullable(latestSprint).ifPresent(latestSprintNode::add);
		Object basicProjectConfigId = latestSprint.getProjectFilter().getBasicProjectConfigId();
		FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);

		Map<String, Object> resultMap = fetchKPIDataFromDb(latestSprintNode, null, null, kpiRequest);
		List<JiraIssue> allIssues = (List<JiraIssue>) resultMap.get(ISSUES);
		SprintDetails sprintDetails= (SprintDetails) resultMap.get(SPRINT_DETAILS);
		if (CollectionUtils.isNotEmpty(allIssues)) {
			LOGGER.info("Work Remaining -> request id : {} total jira Issues : {}", requestTrackerId, allIssues.size());

			Map<String, Map<String, List<JiraIssue>>> typeAndStatusWiseIssues = allIssues.stream().collect(
					Collectors.groupingBy(JiraIssue::getTypeName, Collectors.groupingBy(JiraIssue::getStatus)));
			List<IterationPotentialDelay> iterationPotentialDelayList=calculatePotentialDelay(sprintDetails,allIssues,fieldMapping);
			Map<String, List<IterationPotentialDelay>> issueWiseDelay = iterationPotentialDelayList.stream().collect(Collectors.groupingBy(IterationPotentialDelay::getIssueId));
			Set<String> issueTypes = new HashSet<>();
			Set<String> statuses = new HashSet<>();
			List<IterationKpiValue> iterationKpiValues = new ArrayList<>();
			List<Integer> overAllIssueCount = Arrays.asList(0);
			List<Double> overAllStoryPoints = Arrays.asList(0.0);
			List<Double> overAllOriginalEstimate = Arrays.asList(0.0);
			List<Integer> overAllRemHours = Arrays.asList(0);
			List<Integer> overallPotentialDelay = Arrays.asList(0);
			List<IterationKpiModalValue> overAllmodalValues = new ArrayList<>();
			typeAndStatusWiseIssues.forEach((issueType, statusWiseIssue) ->
				statusWiseIssue.forEach((status, issues) -> {
					issueTypes.add(issueType);
					statuses.add(status);
					List<IterationKpiModalValue> modalValues = new ArrayList<>();
					int issueCount = 0;
					Double storyPoint = 0.0;
					Double originalEstimate = 0.0;
					int remHours = 0;
					int delay=0;
					for (JiraIssue jiraIssue : issues) {
						KPIExcelUtility.populateWorkRemainingIterationData(overAllmodalValues, modalValues, jiraIssue, fieldMapping,issueWiseDelay);
						issueCount = issueCount + 1;
						overAllIssueCount.set(0, overAllIssueCount.get(0) + 1);
						if (null != jiraIssue.getRemainingEstimateMinutes()) {
							remHours = remHours + jiraIssue.getRemainingEstimateMinutes();
							overAllRemHours.set(0, overAllRemHours.get(0) + jiraIssue.getRemainingEstimateMinutes());
						}
						if (null != jiraIssue.getStoryPoints()) {
							storyPoint = storyPoint + jiraIssue.getStoryPoints();
							overAllStoryPoints.set(0, overAllStoryPoints.get(0) + jiraIssue.getStoryPoints());
						}
						if (null != jiraIssue.getOriginalEstimateMinutes()) {
							originalEstimate = originalEstimate + jiraIssue.getOriginalEstimateMinutes();
							overAllOriginalEstimate.set(0, overAllOriginalEstimate.get(0) + jiraIssue.getOriginalEstimateMinutes());
						}
						delay=checkDelay(jiraIssue,issueWiseDelay,delay,overallPotentialDelay);
					}
					List<IterationKpiData> data = new ArrayList<>();
					IterationKpiData issueCounts;
					if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria()) &&
							fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
						issueCounts = new IterationKpiData(ISSUE_COUNT+"/"+CommonConstant.STORY_POINT, Double.valueOf(issueCount), storyPoint,
								null, "",CommonConstant.SP, modalValues);
					} else {
						issueCounts = new IterationKpiData(ISSUE_COUNT+"/"+CommonConstant.ORIGINAL_ESTIMATE, Double.valueOf(issueCount), originalEstimate,
								null,"",CommonConstant.DAY, modalValues);
					}

					IterationKpiData hours = new IterationKpiData(REMAINING_WORK, Double.valueOf(remHours), null, null,
							CommonConstant.DAY, null);

					IterationKpiData potentialDelay = new IterationKpiData(POTENTIAL_DELAY, Double.valueOf(delay), null, null,
							CommonConstant.DAY, null);

					data.add(issueCounts);
					data.add(hours);
					data.add(potentialDelay);
					IterationKpiValue iterationKpiValue = new IterationKpiValue(issueType, status, data);
					iterationKpiValues.add(iterationKpiValue);
				}));
			List<IterationKpiData> data = new ArrayList<>();
			IterationKpiData overAllCount;
			if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria()) &&
					fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
				overAllCount = new IterationKpiData(ISSUE_COUNT+"/"+CommonConstant.STORY_POINT, Double.valueOf(overAllIssueCount.get(0)),
						overAllStoryPoints.get(0), null, "",CommonConstant.SP, overAllmodalValues);

			} else {
				overAllCount = new IterationKpiData(ISSUE_COUNT+"/"+CommonConstant.ORIGINAL_ESTIMATE, Double.valueOf(overAllIssueCount.get(0)),
						overAllOriginalEstimate.get(0), null,"", CommonConstant.DAY, overAllmodalValues);
			}
			IterationKpiData overAllHours = new IterationKpiData(REMAINING_WORK, Double.valueOf(overAllRemHours.get(0)),
					null, null, CommonConstant.DAY, null);

			IterationKpiData overAllPotentialDelay = new IterationKpiData(POTENTIAL_DELAY, Double.valueOf(overallPotentialDelay.get(0)),
					null, null, CommonConstant.DAY, null);

			data.add(overAllCount);
			data.add(overAllHours);
			data.add(overAllPotentialDelay);
			IterationKpiValue overAllIterationKpiValue = new IterationKpiValue(OVERALL, OVERALL, data);
			iterationKpiValues.add(overAllIterationKpiValue);

			// Create kpi level filters
			IterationKpiFiltersOptions filter1 = new IterationKpiFiltersOptions(SEARCH_BY_ISSUE_TYPE, issueTypes);
			IterationKpiFiltersOptions filter2 = new IterationKpiFiltersOptions(SEARCH_BY_PRIORITY, statuses);
			IterationKpiFilters iterationKpiFilters = new IterationKpiFilters(filter1, filter2);
			trendValue.setValue(iterationKpiValues);
			kpiElement.setFilters(iterationKpiFilters);
			kpiElement.setSprint(latestSprint.getName());
			kpiElement.setModalHeads(KPIExcelColumn.WORK_REMAINING.getColumns());
			kpiElement.setTrendValueList(trendValue);
		}
	}

	private int getDelayInMinutes(int delay) {
		return delay*60*8;
	}

	private int checkDelay(JiraIssue jiraIssue, Map<String, List<IterationPotentialDelay>> issueWiseDelay, int potentialDelay, List<Integer> overallPotentialDelay) {
		AtomicInteger finalDelay = new AtomicInteger();
		issueWiseDelay.computeIfPresent(jiraIssue.getNumber(),(issue,delay)->{
			finalDelay.set(potentialDelay + getDelayInMinutes(delay.get(0).getPotentialDelay()));
			overallPotentialDelay.set(0, overallPotentialDelay.get(0) + getDelayInMinutes(delay.get(0).getPotentialDelay()));
			return delay;
		});
		return finalDelay.get();
	}

	/**
	 * with assignees criteria calculating potential delay for inprogress and open issues and
	 * without assignees calculating potential delay for inprogress stories
	 * @param sprintDetails
	 * @param allIssues
	 * @param fieldMapping
	 * @return
	 */
	private List<IterationPotentialDelay> calculatePotentialDelay(SprintDetails sprintDetails,
			List<JiraIssue> allIssues, FieldMapping fieldMapping) {
		List<IterationPotentialDelay> iterationPotentialDelayList = new ArrayList<>();
		Map<String, List<JiraIssue>> assigneeWiseJiraIssue = allIssues.stream()
				.filter(jiraIssue -> jiraIssue.getAssigneeId() != null)
				.collect(Collectors.groupingBy(JiraIssue::getAssigneeId));

		if (MapUtils.isNotEmpty(assigneeWiseJiraIssue)) {
			assigneeWiseJiraIssue.forEach((assignee, jiraIssues) -> {
				List<JiraIssue> inProgressIssues = new ArrayList<>();
				List<JiraIssue> openIssues = new ArrayList<>();
				arrangeJiraIssueList(fieldMapping, jiraIssues, inProgressIssues, openIssues);
				iterationPotentialDelayList
						.addAll(sprintWiseDelayCalculation(inProgressIssues, openIssues, sprintDetails));
			});
		}

		if (CollectionUtils.isNotEmpty(fieldMapping.getJiraStatusForInProgress())) {
			List<JiraIssue> inProgressIssues = allIssues.stream()
					.filter(jiraIssue -> (jiraIssue.getAssigneeId() == null)
							&& StringUtils.isNotEmpty(jiraIssue.getDueDate())
							&& (fieldMapping.getJiraStatusForInProgress().contains(jiraIssue.getStatus())))
					.collect(Collectors.toList());

			List<JiraIssue> openIssues = new ArrayList<>();
			iterationPotentialDelayList.addAll(sprintWiseDelayCalculation(inProgressIssues, openIssues, sprintDetails));
		}
		return iterationPotentialDelayList;
	}

	private List<IterationPotentialDelay> sprintWiseDelayCalculation(List<JiraIssue> inProgressIssuesJiraIssueList,
			List<JiraIssue> openIssuesJiraIssueList, SprintDetails sprintDetails) {
		List<IterationPotentialDelay> iterationPotentialDelayList = new ArrayList<>();
		LocalDate pivotPCD = null;
		Map<LocalDate, List<JiraIssue>> dueDateWiseInProgressJiraIssue = createDueDateWiseMap(
				inProgressIssuesJiraIssueList);
		Map<LocalDate, List<JiraIssue>> dueDateWiseOpenJiraIssue = createDueDateWiseMap(openIssuesJiraIssueList);
		if (MapUtils.isNotEmpty(dueDateWiseInProgressJiraIssue)) {
			for (Map.Entry<LocalDate, List<JiraIssue>> entry : dueDateWiseInProgressJiraIssue.entrySet()) {
				LocalDate pivotPCDLocal = null;
				for (JiraIssue issue : entry.getValue()) {
					int remainingEstimateTime = getRemainingEstimateTime(issue);
					LocalDate potentialClosedDate = getPotentialClosedDate(sprintDetails, pivotPCD,
							remainingEstimateTime);
					int potentialDelay = getPotentialDelay(entry.getKey(), potentialClosedDate);
					iterationPotentialDelayList.add(
							createIterationPotentialDelay(potentialClosedDate, potentialDelay, remainingEstimateTime,
									issue, sprintDetails.getState().equalsIgnoreCase(CLOSED), entry.getKey()));
					pivotPCDLocal = checkPivotPCD(sprintDetails, potentialClosedDate, remainingEstimateTime,
							pivotPCDLocal);
				}
				pivotPCD = checkSubsequentPCD(pivotPCD, pivotPCDLocal);
			}
		}
		if (MapUtils.isNotEmpty(dueDateWiseOpenJiraIssue)) {
			for (Map.Entry<LocalDate, List<JiraIssue>> entry : dueDateWiseOpenJiraIssue.entrySet()) {
				LocalDate pivotPCDLocal = null;
				for (JiraIssue issue : entry.getValue()) {
					int remainingEstimateTime = getRemainingEstimateTime(issue);
					LocalDate potentialClosedDate = getPotentialClosedDate(sprintDetails, pivotPCD,
							remainingEstimateTime);
					int potentialDelay = getPotentialDelay(entry.getKey(), potentialClosedDate);
					iterationPotentialDelayList.add(
							createIterationPotentialDelay(potentialClosedDate, potentialDelay, remainingEstimateTime,
									issue, sprintDetails.getState().equalsIgnoreCase(CLOSED), entry.getKey()));
					pivotPCDLocal = checkPivotPCD(sprintDetails, potentialClosedDate, remainingEstimateTime,
							pivotPCDLocal);
				}
				pivotPCD = checkSubsequentPCD(pivotPCD, pivotPCDLocal);
			}
		}
		return iterationPotentialDelayList;
	}

	/**
	 * when a story is expected to get completed, the subsequent story will be
	 * picked up the next working day
	 * 
	 * @param pivotPCD
	 * @param pivotPCDLocal
	 * @return
	 */
	private LocalDate checkSubsequentPCD(LocalDate pivotPCD, LocalDate pivotPCDLocal) {
		LocalDate workingDayAfterAdditionofDays = CommonUtils.getWorkingDayAfterAdditionofDays(pivotPCDLocal, 1);
		pivotPCD = workingDayAfterAdditionofDays == null ? pivotPCD : workingDayAfterAdditionofDays;
		return pivotPCD;
	}

	private LocalDate getPotentialClosedDate(SprintDetails sprintDetails, LocalDate pivotPCD, int estimatedTime) {
		return (estimatedTime == 0 && sprintDetails.getState().equalsIgnoreCase(CLOSED))
				? DateUtil.stringToLocalDate(sprintDetails.getCompleteDate(), DateUtil.TIME_FORMAT_WITH_SEC)
				: createPotentialClosedDate(sprintDetails, estimatedTime, pivotPCD);
	}

	private IterationPotentialDelay createIterationPotentialDelay(LocalDate potentialClosedDate, int potentialDelay,
			int remainingEstimateTime, JiraIssue issue, boolean sprintClosed, LocalDate dueDate) {
		IterationPotentialDelay iterationPotentialDelay = new IterationPotentialDelay();
		iterationPotentialDelay.setIssueId(issue.getNumber());
		iterationPotentialDelay.setPotentialDelay((sprintClosed && remainingEstimateTime == 0) ? 0 : potentialDelay);
		iterationPotentialDelay.setDueDate(dueDate.toString());
		iterationPotentialDelay.setPredictedCompletedDate(potentialClosedDate.toString());
		return iterationPotentialDelay;

	}

	/**
	 * if due date is less than potential closed date, then potential delay will be negative
	 * @param dueDate
	 * @param potentialClosedDate
	 * @return
	 */
	private int getPotentialDelay(LocalDate dueDate, LocalDate potentialClosedDate) {
		int potentialDelays = CommonUtils.createPotentialDelays(dueDate, potentialClosedDate);
		return (dueDate.isAfter(potentialClosedDate)) ? potentialDelays * (-1) : potentialDelays;
	}

	/**
	 * In closed sprint if a Remaining Estimate is 0, then the potential closing
	 * date will be same as sprint' end date, whose potential closing date will not
	 * be taken into account for further storie's delay calculation
	 * 
	 * @param sprintDetails
	 * @param potentialClosedDate
	 * @param remainingEstimateTime
	 * @param pivotPCDLocal
	 * @return
	 */
	private LocalDate checkPivotPCD(SprintDetails sprintDetails, LocalDate potentialClosedDate,
			int remainingEstimateTime, LocalDate pivotPCDLocal) {
		if ((pivotPCDLocal == null || pivotPCDLocal.isBefore(potentialClosedDate))
				&& (!sprintDetails.getState().equalsIgnoreCase(CLOSED)
						|| (sprintDetails.getState().equalsIgnoreCase(CLOSED) && remainingEstimateTime != 0))) {
			pivotPCDLocal = potentialClosedDate;
		}
		return pivotPCDLocal;
	}

	/**
	 * create dueDateWise sorted Map only for the stories having dueDate 
	 * @param arrangeJiraIssueList
	 * @return
	 */
	private Map<LocalDate, List<JiraIssue>> createDueDateWiseMap(List<JiraIssue> arrangeJiraIssueList) {
		TreeMap<LocalDate, List<JiraIssue>> localDateListMap = new TreeMap<>();
		if(CollectionUtils.isNotEmpty(arrangeJiraIssueList)) {
			arrangeJiraIssueList.forEach(jiraIssue -> {
				LocalDate dueDate = DateUtil.stringToLocalDate(jiraIssue.getDueDate(), DateUtil.TIME_FORMAT_WITH_SEC);
				localDateListMap.computeIfPresent(dueDate, (date, issue) -> {
					issue.add(jiraIssue);
					return issue;
				});
				localDateListMap.computeIfAbsent(dueDate, value -> {
					List<JiraIssue> issues = new ArrayList<>();
					issues.add(jiraIssue);
					return issues;
				});
			});
		}
		return localDateListMap;
	}

	private LocalDate createPotentialClosedDate(SprintDetails sprintDetails, int remainingEstimateTime,
			LocalDate pivotPCD) {
		LocalDate pcd = null;
		if (pivotPCD == null) {
			// for the first calculation
			LocalDate startDate = sprintDetails.getState().equalsIgnoreCase("closed")
					? DateUtil.stringToLocalDate(sprintDetails.getCompleteDate(), DateUtil.TIME_FORMAT_WITH_SEC)
					: LocalDate.now();

			pcd = CommonUtils.getWorkingDayAfterAdditionofDays(startDate, remainingEstimateTime);
		} else {
			pcd = CommonUtils.getWorkingDayAfterAdditionofDays(pivotPCD, remainingEstimateTime);
		}
		return pcd;
	}

	private int getRemainingEstimateTime(JiraIssue issueObject) {
		int remainingEstimate = 0;
		if (issueObject.getRemainingEstimateMinutes() != null) {
			remainingEstimate = (issueObject.getRemainingEstimateMinutes() / 60) / 8;
		}
		return remainingEstimate;
	}

	/**
	 * setting in progress and open issues
	 * @param fieldMapping
	 * @param allIssues
	 * @param inProgressIssues
	 * @param openIssues
	 * @return
	 */
	private void arrangeJiraIssueList(FieldMapping fieldMapping, List<JiraIssue> allIssues, List<JiraIssue> inProgressIssues, List<JiraIssue> openIssues) {
		List<JiraIssue> jiraIssuesWithDueDate = allIssues.stream().filter(issue -> StringUtils.isNotEmpty(issue.getDueDate())).collect(Collectors.toList());
		if (CollectionUtils.isNotEmpty(fieldMapping.getJiraStatusForInProgress())) {
			inProgressIssues.addAll(jiraIssuesWithDueDate.stream()
					.filter(jiraIssue -> fieldMapping.getJiraStatusForInProgress().contains(jiraIssue.getStatus()))
					.collect(Collectors.toList()));
			openIssues.addAll(jiraIssuesWithDueDate.stream()
					.filter(jiraIssue -> !fieldMapping.getJiraStatusForInProgress().contains(jiraIssue.getStatus()))
					.collect(Collectors.toList()));
		} else {
			openIssues.addAll(jiraIssuesWithDueDate);
		}

	}
}
