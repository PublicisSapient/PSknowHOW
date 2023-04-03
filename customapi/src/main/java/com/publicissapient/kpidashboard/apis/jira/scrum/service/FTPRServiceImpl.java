package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.IterationKpiData;
import com.publicissapient.kpidashboard.apis.model.IterationKpiModalValue;
import com.publicissapient.kpidashboard.apis.model.IterationKpiValue;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

@Slf4j
@Component
public class FTPRServiceImpl extends JiraKPIService<Integer, List<Object>, Map<String, Object>> {

	public static final String UNCHECKED = "unchecked";
	public static final String DEFECT = "Defect";
	public static final String PERCENTAGE = "percentage";
	private static final Logger LOGGER = LoggerFactory.getLogger(FTPRServiceImpl.class);
	private static final String ISSUES = "issues";
	private static final String FIRST_TIME_PASS_STORIES = "First Time Pass Stories";
	private static final String TOTAL_STORIES = "Total Stories";
	private static final String FIRST_TIME_PASS_PERCENTAGE = "First Time Pass Rate";
	private static final String OVERALL = "Overall";
	private static final String SPRINT_DETAILS = "sprint details";
	private static final String SPRINT_HISTORY = "sprint history";
	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private KpiHelperService kpiHelperService;

	@Autowired
	private SprintRepository sprintRepository;

	@Autowired
	private CustomApiConfig customApiConfig;

	@Autowired
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private FilterHelperService flterHelperService;

	private static List<JiraIssue> getFTPSListBasedOnRCA(FieldMapping fieldMapping, List<JiraIssue> totalStoryList,
			List<JiraIssue> firstTimePassStoryList, List<JiraIssue> totalDeffects,
			Map<String, List<String>> projectWiseRCA) {
		if (CollectionUtils.isNotEmpty(fieldMapping.getExcludeRCAFromFTPR())) {
			List<String> uniqueRCA = new ArrayList<>();
			for (String rca : fieldMapping.getExcludeRCAFromFTPR()) {
				if (rca.equalsIgnoreCase(Constant.CODING) || rca.equalsIgnoreCase(Constant.CODE)) {
					rca = Constant.CODE_ISSUE;
				}
				uniqueRCA.add(rca.toLowerCase());
			}
			projectWiseRCA.put(fieldMapping.getBasicProjectConfigId().toString(), uniqueRCA);

			List<JiraIssue> rcaWiseDefect = totalDeffects.stream().filter(defects ->

			CollectionUtils.containsAny(projectWiseRCA.get(defects.getBasicProjectConfigId()),
					defects.getRootCauseList())

			).collect(Collectors.toList());

			if (CollectionUtils.isNotEmpty(rcaWiseDefect) && CollectionUtils.isNotEmpty(totalStoryList)) {
				Set<String> listOfStory = rcaWiseDefect.stream().map(JiraIssue::getDefectStoryID).flatMap(Set::stream)
						.collect(Collectors.toSet());
				List<JiraIssue> rcaWiseStories = totalStoryList.stream()
						.filter(issues -> listOfStory.contains(issues.getNumber())).collect(Collectors.toList());
				firstTimePassStoryList = Stream.concat(firstTimePassStoryList.stream(), rcaWiseStories.stream())
						.collect(Collectors.toList());
			}
		}
		return firstTimePassStoryList;
	}

	private static List<JiraIssue> getIssueList(FieldMapping fieldMapping, List<JiraIssue> totalStoryList,
			List<JiraIssue> firstTimePassStoryList, List<JiraIssue> totalDeffects,
			Map<String, List<String>> configPriority, Map<String, List<String>> projectWisePriority) {
		if (CollectionUtils.isNotEmpty(fieldMapping.getDefectPriority())) {
			List<String> priorValue = fieldMapping.getDefectPriority().stream().map(String::toUpperCase)
					.collect(Collectors.toList());
			if (CollectionUtils.isNotEmpty(priorValue)) {
				List<String> priorityValues = new ArrayList<>();
				priorValue.forEach(priority -> priorityValues.addAll(configPriority.get(priority)));
				projectWisePriority.put(fieldMapping.getBasicProjectConfigId().toString(), priorityValues);
			}

			List<JiraIssue> fieldMappingPriorityWiseDefect = totalDeffects.stream()
					.filter(defects -> projectWisePriority.get(defects.getBasicProjectConfigId())
							.contains(defects.getPriority()))
					.collect(Collectors.toList());
			if (CollectionUtils.isNotEmpty(fieldMappingPriorityWiseDefect)) {
				Set<String> listOfStoryId = fieldMappingPriorityWiseDefect.stream().map(JiraIssue::getDefectStoryID)
						.flatMap(Set::stream).collect(Collectors.toSet());
				List<JiraIssue> priorityWiseStories = totalStoryList.stream()
						.filter(issues -> listOfStoryId.contains(issues.getNumber())).collect(Collectors.toList());
				firstTimePassStoryList = Stream.concat(firstTimePassStoryList.stream(), priorityWiseStories.stream())
						.collect(Collectors.toList());
			}

			List<JiraIssue> priorityDefectWOMapping = totalDeffects.stream().filter(defects -> !(projectWisePriority
					.get(defects.getBasicProjectConfigId()).contains(defects.getPriority())))
					.collect(Collectors.toList());
			// excluding stories with linked defect from FTPR if any defect priority is not
			// maintained in field mapping
			if (CollectionUtils.isNotEmpty(priorityDefectWOMapping)) {
				Set<String> listOfStoryId = priorityDefectWOMapping.stream().map(JiraIssue::getDefectStoryID)
						.flatMap(Set::stream).collect(Collectors.toSet());
				firstTimePassStoryList = firstTimePassStoryList.stream()
						.filter(issues -> !(listOfStoryId.contains(issues.getNumber()))).collect(Collectors.toList());

			}

		}
		return firstTimePassStoryList;
	}

	private static List<JiraIssue> excludeLinkedDefectStories(List<JiraIssue> totalStoryList,
			List<JiraIssue> firstTimePassStoryList, Set<String> listOfStory) {
		if (CollectionUtils.isNotEmpty(listOfStory)) {

			firstTimePassStoryList = totalStoryList.stream()
					.filter(jiraIssue -> !listOfStory.contains(jiraIssue.getNumber())).collect(Collectors.toList());
		}
		return firstTimePassStoryList;
	}

	private static List<JiraIssue> getTotalStoryList(FieldMapping fieldMapping, List<JiraIssue> allIssues,
			List<JiraIssue> totalStoryList) {
		if (Optional.ofNullable(fieldMapping.getJiraFTPRStoryIdentification()).isPresent()) {
			totalStoryList = allIssues.stream().filter(
					jiraIssue -> fieldMapping.getJiraFTPRStoryIdentification().contains(jiraIssue.getTypeName()))
					.collect(Collectors.toList());

			// exclude the issue from total stories based on defect rejection status
			if (Optional.ofNullable(fieldMapping.getJiraDefectRejectionStatus()).isPresent()) {
				totalStoryList = totalStoryList.stream()
						.filter(jiraIssue -> !jiraIssue.getStatus().equals(fieldMapping.getJiraDefectRejectionStatus()))
						.collect(Collectors.toList());
			}
		}
		return totalStoryList;
	}

	private static List<JiraIssue> ftprBasedOnResolutionTypeForRejection(FieldMapping fieldMapping,
			List<JiraIssue> firstTimePassStoryList) {
		if (Optional.ofNullable(fieldMapping.getResolutionTypeForRejection()).isPresent()
				&& CollectionUtils.isNotEmpty(firstTimePassStoryList)) {

			firstTimePassStoryList = firstTimePassStoryList.stream()
					.filter(jiraIssue -> !fieldMapping.getResolutionTypeForRejection().contains(jiraIssue.getStatus()))
					.collect(Collectors.toList());
		}
		return firstTimePassStoryList;
	}

	private double calculateFTPR(double priorityWiseFTPS, double priorityWiseTotalStory) {
		return roundingOff((priorityWiseFTPS * 100) / priorityWiseTotalStory);
	}

	@Override
	public Integer calculateKPIMetrics(Map<String, Object> stringObjectMap) {
		return null;
	}

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
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {

		Map<String, Object> resultListMap = new HashMap<>();
		Node leafNode = leafNodeList.stream().findFirst().orElse(null);
		if (null != leafNode) {
			LOGGER.info("First Time Pass rate -> Requested sprint : {}", leafNode.getName());
			String basicProjectConfigId = leafNode.getProjectFilter().getBasicProjectConfigId().toString();
			String sprintId = leafNode.getSprintFilter().getId();
			SprintDetails sprintDetails = sprintRepository.findBySprintID(sprintId);
			if (null != sprintDetails) {
				List<String> completedIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.COMPLETED_ISSUES);
				if (CollectionUtils.isNotEmpty(completedIssues)) {
					List<JiraIssue> issueList = jiraIssueRepository
							.findByNumberInAndBasicProjectConfigId(completedIssues, basicProjectConfigId);
					Set<JiraIssue> filtersIssuesList = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails,
									sprintDetails.getCompletedIssues(), issueList);
					List<JiraIssueCustomHistory> totalJiraIssuesHistory = jiraIssueCustomHistoryRepository
							.findByStoryIDInAndBasicProjectConfigIdIn(
									filtersIssuesList.stream().map(JiraIssue::getNumber).collect(Collectors.toList()),
									Arrays.asList(basicProjectConfigId));

					resultListMap.put(ISSUES, new ArrayList<>(filtersIssuesList));
					resultListMap.put(SPRINT_DETAILS, sprintDetails);
					resultListMap.put(SPRINT_HISTORY, totalJiraIssuesHistory);
				}
			}
		}
		return resultListMap;
	}

	@Override
	public String getQualifierType() {
		return KPICode.FIRST_TIME_PASS_RATE_ITERATION.name();
	}

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
		List<JiraIssueCustomHistory> totalJiraIssuesHistory = (List<JiraIssueCustomHistory>) resultMap
				.get(SPRINT_HISTORY);

		if (CollectionUtils.isNotEmpty(allIssues)) {
			LOGGER.info("First Time Pass rate -> request id : {} total jira Issues : {}", requestTrackerId,
					allIssues.size());
			List<JiraIssue> totalStoryList = null;
			List<JiraIssue> firstTimePassStoryList = null;
			Set<String> listOfStory = new HashSet<>();

			// Total stories from issues completed collection in a sprint
			totalStoryList = getTotalStoryList(fieldMapping, allIssues, totalStoryList);

			List<JiraIssue> totalDeffects = allIssues.stream()
					.filter(jiraIssue -> jiraIssue.getTypeName().equalsIgnoreCase(DEFECT)).collect(Collectors.toList());

			listOfStory = getDefectIds(listOfStory, totalDeffects);
			// exclude stories from FTPR with linked defect
			firstTimePassStoryList = excludeLinkedDefectStories(totalStoryList, firstTimePassStoryList, listOfStory);

			// filter the issues from first time pass stories based on resolution type for
			// rejection
			firstTimePassStoryList = ftprBasedOnResolutionTypeForRejection(fieldMapping, firstTimePassStoryList);

			// exclude stories from FTPR with return transaction
			kpiHelperService.removeStoriesWithReturnTransaction(firstTimePassStoryList, totalJiraIssuesHistory);

			Map<String, List<String>> configPriority = customApiConfig.getPriority();
			Map<String, List<String>> projectWisePriority = new HashMap<>();
			Map<String, List<String>> projectWiseRCA = new HashMap<>();
			Map<String, List<JiraIssue>> priorityWiseTotalIssues = new HashMap<>();
			Map<String, List<JiraIssue>> priorityWiseTotalFTPSIssues = new HashMap<>();

			// consider stories based on defect priority to exclude from FTPR field mapping
			firstTimePassStoryList = getIssueList(fieldMapping, totalStoryList, firstTimePassStoryList, totalDeffects,
					configPriority, projectWisePriority);

			// consider stories based on RCA values to exclude from FTPR field mapping
			firstTimePassStoryList = getFTPSListBasedOnRCA(fieldMapping, totalStoryList, firstTimePassStoryList,
					totalDeffects, projectWiseRCA);

			if (CollectionUtils.isNotEmpty(totalStoryList)) {

				priorityWiseTotalIssues = totalStoryList.stream()
						.collect(Collectors.groupingBy(JiraIssue::getPriority));
			}

			if (CollectionUtils.isNotEmpty(firstTimePassStoryList)) {

				priorityWiseTotalFTPSIssues = firstTimePassStoryList.stream()
						.collect(Collectors.groupingBy(JiraIssue::getPriority));
			}

			Set<String> priorities = new HashSet<>();
			List<IterationKpiValue> iterationKpiValues = new ArrayList<>();
			List<Integer> overAllFTPS = Arrays.asList(0);
			List<Integer> overAllStory = Arrays.asList(0);
			List<IterationKpiModalValue> overAllmodalValues = new ArrayList<>();

			for (Map.Entry<String, List<JiraIssue>> entry : priorityWiseTotalIssues.entrySet()) {
				String priority = entry.getKey();
				List<JiraIssue> issues = entry.getValue();
				priorities.add(priority);
				if (priorityWiseTotalFTPSIssues.containsKey(priority)) {

					List<JiraIssue> finalFirstTimePassStoryList = priorityWiseTotalFTPSIssues.get(priority).stream()
							.collect(Collectors.toList());
					List<IterationKpiModalValue> modalValues = new ArrayList<>();
					List<String> expressions = new ArrayList<>();
					int priorityWiseFTPS = 0;
					int priorityWiseTotalStory = 0;
					for (JiraIssue jiraIssue : issues) {

						priorityWiseTotalStory = priorityWiseTotalStory + 1;
						overAllStory.set(0, overAllStory.get(0) + 1);

						priorityWiseFTPS = getPriorityWiseFTPS(overAllFTPS, finalFirstTimePassStoryList, priorityWiseFTPS, jiraIssue);

						KPIExcelUtility.populateIterationDataForFirstTimePassRate(overAllmodalValues, modalValues, jiraIssue,
								finalFirstTimePassStoryList, listOfStory, totalDeffects);

					}

					List<IterationKpiData> data = new ArrayList<>();
					expressions.add(FIRST_TIME_PASS_STORIES);
					expressions.add(TOTAL_STORIES);
					expressions.add(PERCENTAGE);

					IterationKpiData ftprStories = new IterationKpiData(FIRST_TIME_PASS_STORIES,
							(double) priorityWiseFTPS, null, null, null, null);

					IterationKpiData stories = new IterationKpiData(TOTAL_STORIES, (double) priorityWiseTotalStory,
							null, null, null, modalValues);

					IterationKpiData ftprPercentage = new IterationKpiData(FIRST_TIME_PASS_PERCENTAGE,
							calculateFTPR(priorityWiseFTPS, priorityWiseTotalStory), null, null, Constant.PERCENTAGE,
							null, expressions);

					data.add(ftprStories);
					data.add(stories);
					data.add(ftprPercentage);
					IterationKpiValue iterationKpiValue = new IterationKpiValue(priority, null, data);
					iterationKpiValues.add(iterationKpiValue);
				}
			}

			List<IterationKpiData> data = new ArrayList<>();

			IterationKpiData overAllFTPRStories = new IterationKpiData(FIRST_TIME_PASS_STORIES,
					Double.valueOf(overAllFTPS.get(0)), null, null, null, null);

			IterationKpiData overAllStories = new IterationKpiData(TOTAL_STORIES, Double.valueOf(overAllStory.get(0)),
					null, null, null, overAllmodalValues);

			IterationKpiData overAllFTPRPercentage = new IterationKpiData(FIRST_TIME_PASS_PERCENTAGE,
					calculateFTPR(firstTimePassStoryList.size(), totalStoryList.size()), null, null,
					Constant.PERCENTAGE, null);

			data.add(overAllFTPRStories);
			data.add(overAllStories);
			data.add(overAllFTPRPercentage);
			IterationKpiValue overAllIterationKpiValue = new IterationKpiValue(OVERALL, null, data);
			iterationKpiValues.add(overAllIterationKpiValue);

			trendValue.setValue(iterationKpiValues);
			kpiElement.setSprint(latestSprint.getName());
			kpiElement.setModalHeads(KPIExcelColumn.FIRST_TIME_PASS_RATE_ITERATION.getColumns());
			kpiElement.setTrendValueList(trendValue);
		}
	}

	private static int getPriorityWiseFTPS(List<Integer> overAllFTPS, List<JiraIssue> finalFirstTimePassStoryList, int priorityWiseFTPS, JiraIssue jiraIssue) {
		if (CollectionUtils.isNotEmpty(finalFirstTimePassStoryList)
				&& finalFirstTimePassStoryList.contains(jiraIssue)) {
			priorityWiseFTPS = priorityWiseFTPS + 1;
			overAllFTPS.set(0, overAllFTPS.get(0) + 1);
		}
		return priorityWiseFTPS;
	}

	private static Set<String> getDefectIds(Set<String> listOfStory, List<JiraIssue> totalDeffects) {
		if (CollectionUtils.isNotEmpty(totalDeffects)) {
			listOfStory = totalDeffects.stream().map(JiraIssue::getDefectStoryID).flatMap(Set::stream)
					.collect(Collectors.toSet());
		}
		return listOfStory;
	}

}
