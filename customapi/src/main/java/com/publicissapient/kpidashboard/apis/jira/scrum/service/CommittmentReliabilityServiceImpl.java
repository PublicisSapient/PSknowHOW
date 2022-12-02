package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CommittmentReliabilityServiceImpl extends JiraKPIService<Long, List<Object>, Map<String, Object>> {
	private static final String ISSUE_COUNT = "Issue Count";
	private static final String STORY_POINT = "Story Point";
	private static final String PROJECT_WISE_TOTAL_ISSUE = "projectWiseTotalIssues";
	private static final String DEV = "DeveloperKpi";
	private static final String TOTAL_ISSUE_SIZE = "totalIssueSize";
	private static final String COMPLETED_ISSUE_SIZE = "completedIssueSize";
	private static final String TOTAL_STORY_POINTS = "totalStoryPoints";
	private static final String COMPLETED_STORY_POINTS = "completedStoryPoints";
	private static final String DELIVERED = "Delivered";
	private static final String COMMITTED = "Commited";
	private static final String SPRINT_DETAILS = "sprintDetails";
	private static final String PROJECT_WISE_CLOSED_STORY_STATUS = "projectWiseClosedStoryStatus";
	@Autowired
	private SprintRepository sprintRepository;
	@Autowired
	private JiraIssueRepository jiraIssueRepository;
	@Autowired
	private ConfigHelperService configHelperService;
	@Autowired
	private CustomApiConfig customApiConfig;

	@Autowired
	private FilterHelperService flterHelperService;

	@Override
	public Long calculateKPIMetrics(Map<String, Object> stringObjectMap) {
		return 0L;
	}

	@Override
	public String getQualifierType() {
		return KPICode.COMMITMENT_RELIABILITY.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();

		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
			if (Filters.getFilter(k) == Filters.SPRINT) {
				sprintWiseLeafNodeValue(mapTmp, v, kpiElement, kpiRequest);

			}
		});

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValueMap(root, nodeWiseKPIValue, KPICode.COMMITMENT_RELIABILITY);

		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, nodeWiseKPIValue,
				KPICode.COMMITMENT_RELIABILITY);
		Map<String, Map<String, List<DataCount>>> statusTypeProjectWiseDc = new LinkedHashMap<>();

		trendValuesMap.forEach((statusType, dataCounts) -> {
			Map<String, List<DataCount>> projectWiseDc = dataCounts.stream()
					.collect(Collectors.groupingBy(DataCount::getData));
			statusTypeProjectWiseDc.put(statusType, projectWiseDc);
		});

		List<DataCountGroup> dataCountGroups = new ArrayList<>();
		statusTypeProjectWiseDc.forEach((issueType, projectWiseDc) -> {
			DataCountGroup dataCountGroup = new DataCountGroup();
			List<DataCount> dataList = new ArrayList<>();
			projectWiseDc.entrySet().stream().forEach(trend -> dataList.addAll(trend.getValue()));
			dataCountGroup.setFilter(issueType);
			dataCountGroup.setValue(dataList);
			dataCountGroups.add(dataCountGroup);
		});

		log.debug("[COMMITMENT-RELIABILITY-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		kpiElement.setTrendValueList(dataCountGroups);
		kpiElement.setNodeWiseKPIValue(nodeWiseKPIValue);

		return kpiElement;
	}

	private void sprintWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> sprintLeafNodeList, KpiElement kpiElement,
			KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();

		Collections.sort(sprintLeafNodeList, (Node o1, Node o2) -> o1.getSprintFilter().getStartDate()
				.compareTo(o2.getSprintFilter().getStartDate()));

		String startDate = sprintLeafNodeList.get(0).getSprintFilter().getStartDate();
		String endDate = sprintLeafNodeList.get(sprintLeafNodeList.size() - 1).getSprintFilter().getEndDate();

		Map<String, Object> resultMap = fetchKPIDataFromDb(sprintLeafNodeList, startDate, endDate, kpiRequest);

		List<JiraIssue> allJiraIssue = (List<JiraIssue>) resultMap.get(PROJECT_WISE_TOTAL_ISSUE);

		List<SprintDetails> sprintDetails = (List<SprintDetails>) resultMap.get(SPRINT_DETAILS);

		Map<Pair<String, String>, List<JiraIssue>> sprintWiseCreatedIssues = new HashMap<>();
		Map<Pair<String, String>, List<JiraIssue>> sprintWiseClosedIssues = new HashMap<>();

		if (CollectionUtils.isNotEmpty(allJiraIssue)) {
			if (CollectionUtils.isNotEmpty(sprintDetails)) {
				sprintDetails.forEach(sd -> {
					Set<JiraIssue> totalIssues = KpiDataHelper.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sd,
							sd.getTotalIssues(), allJiraIssue);
					Set<JiraIssue> completedIssues = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sd, sd.getCompletedIssues(),
									allJiraIssue);
					sprintWiseCreatedIssues.put(Pair.of(sd.getBasicProjectConfigId().toString(), sd.getSprintID()),
							new ArrayList<>(totalIssues));
					sprintWiseClosedIssues.put(Pair.of(sd.getBasicProjectConfigId().toString(), sd.getSprintID()),
							new ArrayList<>(completedIssues));
				});
			} else {
				// for azure board sprint details collections empty so that we have to prepare
				// data from jira issue.
				Map<String, List<String>> projectWiseClosedStatusMap = (Map<String, List<String>>) resultMap
						.get(PROJECT_WISE_CLOSED_STORY_STATUS);
				Map<String, List<JiraIssue>> projectWiseJiraIssues = allJiraIssue.stream()
						.collect(Collectors.groupingBy(JiraIssue::getBasicProjectConfigId));
				projectWiseJiraIssues.forEach((basicProjectConfigId, projectWiseIssuesList) -> {
					Map<String, List<JiraIssue>> sprintWiseJiraIssues = projectWiseIssuesList.stream()
							.filter(jiraIssue -> Objects.nonNull(jiraIssue.getSprintID()))
							.collect(Collectors.groupingBy(JiraIssue::getSprintID));
					sprintWiseJiraIssues.forEach((sprintId, totalIssues) -> sprintWiseCreatedIssues
							.put(Pair.of(basicProjectConfigId, sprintId), totalIssues));
					List<String> closedStatus = projectWiseClosedStatusMap.get(basicProjectConfigId);
					sprintWiseJiraIssues.forEach((sprintId, sprintWiseIssuesList) -> {
						List<JiraIssue> completedIssues = sprintWiseIssuesList.stream()
								.filter(jiraIssue -> closedStatus.contains(jiraIssue.getStatus()))
								.collect(Collectors.toList());
						sprintWiseClosedIssues.put(Pair.of(basicProjectConfigId, sprintId), completedIssues);
					});
				});
			}
		}

		List<KPIExcelData> excelData = new ArrayList<>();

		sprintLeafNodeList.forEach(node -> {
			String trendLineName = node.getProjectFilter().getName();

			String currentSprintComponentId = node.getSprintFilter().getId();
			Pair<String, String> currentNodeIdentifier = Pair
					.of(node.getProjectFilter().getBasicProjectConfigId().toString(), currentSprintComponentId);

			List<CommitmentReliabilityValidationData> validationDataList = new ArrayList<>();

			List<JiraIssue> totalPresentJiraIssue = new ArrayList<>();
			List<JiraIssue> totalPresentCompletedIssue = new ArrayList<>();

			if (CollectionUtils.isNotEmpty(sprintWiseCreatedIssues.get(currentNodeIdentifier))) {
				totalPresentJiraIssue = sprintWiseCreatedIssues.get(currentNodeIdentifier);
				totalPresentCompletedIssue = sprintWiseClosedIssues.get(currentNodeIdentifier);
			}

			Map<String, Double> commitmentHowerMap = new HashMap<>();
			Map<String, Long> commitmentMap = getCommitmentMap(totalPresentJiraIssue, totalPresentCompletedIssue,
					validationDataList, commitmentHowerMap);
			populateExcelData(requestTrackerId, excelData, validationDataList, node);

			Map<String, List<DataCount>> dataCountMap = new HashMap<>();

			for (Map.Entry<String, Long> map : commitmentMap.entrySet()) {
				DataCount dataCount = new DataCount();
				dataCount.setData(String.valueOf(map.getValue()));
				dataCount.setSProjectName(trendLineName);
				dataCount.setSSprintID(node.getSprintFilter().getId());
				dataCount.setSSprintName(node.getSprintFilter().getName());
				dataCount.setValue(map.getValue());
				dataCount.setKpiGroup(map.getKey());
				dataCount.setHoverValue(generateHowerMap(commitmentHowerMap, map.getKey()));
				dataCountMap.put(map.getKey(), new ArrayList<>(Arrays.asList(dataCount)));
			}
			mapTmp.get(node.getId()).setValue(dataCountMap);

		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.COMMITMENT_RELIABILITY.getColumns());
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		Map<String, Object> resultListMap = new HashMap<>();
		List<String> sprintList = new ArrayList<>();
		List<String> basicProjectConfigIds = new ArrayList<>();
		Map<String, Pair<String, String>> sprintWithDateMap = new HashMap<>();
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
		Map<String, List<String>> projectWiseClosedStatusMap = new HashMap<>();
		leafNodeList.forEach(leaf -> {

			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			sprintList.add(leaf.getSprintFilter().getId());
			basicProjectConfigIds.add(basicProjectConfigId.toString());

			sprintWithDateMap.put(leaf.getSprintFilter().getId(),
					Pair.of(leaf.getSprintFilter().getStartDate(), leaf.getSprintFilter().getEndDate()));

			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
			mapOfProjectFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
					CommonUtils.convertToPatternList(fieldMapping.getJiraSprintVelocityIssueType()));

			if (Optional.ofNullable(fieldMapping.getJiraIssueDeliverdStatus()).isPresent()) {
				projectWiseClosedStatusMap.put(basicProjectConfigId.toString(),
						fieldMapping.getJiraIssueDeliverdStatus().stream().distinct().collect(Collectors.toList()));
			}

			uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);

		});

		List<SprintDetails> sprintDetails = sprintRepository.findBySprintIDIn(sprintList);
		Set<String> totalIssue = new HashSet<>();
		sprintDetails.stream().forEach(sprintDetail -> {
			if (CollectionUtils.isNotEmpty(sprintDetail.getTotalIssues())) {
				totalIssue.addAll(KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetail,
						CommonConstant.TOTAL_ISSUES));
			}

		});

		/** additional filter **/
		KpiDataHelper.createAdditionalFilterMap(kpiRequest, mapOfFilters, Constant.SCRUM, DEV, flterHelperService);

		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));

		if (CollectionUtils.isNotEmpty(totalIssue)) {
			resultListMap.put(PROJECT_WISE_TOTAL_ISSUE,
					jiraIssueRepository.findIssueByNumber(mapOfFilters, totalIssue, new HashMap<>()));
			resultListMap.put(SPRINT_DETAILS, sprintDetails);
		} else {
			// start: for azure board sprint details collections put is empty due to we did
			// not have required data of issues.
			resultListMap.put(PROJECT_WISE_TOTAL_ISSUE,
					jiraIssueRepository.findIssuesBySprintAndType(mapOfFilters, uniqueProjectMap));
			resultListMap.put(SPRINT_DETAILS, null);
		}
		resultListMap.put(PROJECT_WISE_CLOSED_STORY_STATUS, projectWiseClosedStatusMap);
		// end: for azure board sprint details collections put is empty due to we did
		// not have required data of issues.
		return resultListMap;
	}

	/**
	 * generate Hower Map
	 *
	 * @param commitmentHowerMap
	 * @param key
	 * @return
	 */
	private Map<String, Integer> generateHowerMap(Map<String, Double> commitmentHowerMap, String key) {
		Map<String, Integer> howerMap = new LinkedHashMap<>();
		if (ISSUE_COUNT.equalsIgnoreCase(key)) {
			howerMap.put(DELIVERED, commitmentHowerMap.getOrDefault(COMPLETED_ISSUE_SIZE, 0.0d).intValue());
			howerMap.put(COMMITTED, commitmentHowerMap.getOrDefault(TOTAL_ISSUE_SIZE, 0.0d).intValue());
		} else {
			howerMap.put(DELIVERED, commitmentHowerMap.getOrDefault(COMPLETED_STORY_POINTS, 0.0d).intValue());
			howerMap.put(COMMITTED, commitmentHowerMap.getOrDefault(TOTAL_STORY_POINTS, 0.0d).intValue());

		}
		return howerMap;
	}

	/**
	 * generate Excel
	 *
	 * @param requestTrackerId
	 */
	private void populateExcelData(String requestTrackerId, List<KPIExcelData> excelData,
			List<CommitmentReliabilityValidationData> validationDataList, Node node) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			String sprintName = node.getSprintFilter().getName();
			if (CollectionUtils.isNotEmpty(validationDataList)) {
				validationDataList.stream().forEach(data -> {

					Map<String, JiraIssue> totalSprintStoryMap = new HashMap<>();
					data.getTotalIssueNumbers().stream()
							.forEach(issue -> totalSprintStoryMap.putIfAbsent(issue.getNumber(), issue));
					KPIExcelUtility.populateCommittmentReliability(sprintName, totalSprintStoryMap,
							data.getCompletedIssueNumber(), excelData);

				});

			}
		}

	}

	/**
	 * @param totalJiraIssue
	 * @param completed
	 * @param validationData
	 * @return
	 */
	private Map<String, Long> getCommitmentMap(List<JiraIssue> totalJiraIssue, List<JiraIssue> completed,
			List<CommitmentReliabilityValidationData> validationData, Map<String, Double> commitmentHowerMap) {

		Map<String, Long> commitmentResult = new LinkedHashMap<>();
		long issueCount = 0L;
		long storyCount = 0L;
		CommitmentReliabilityValidationData reliabilityValidationData = new CommitmentReliabilityValidationData();
		if (CollectionUtils.isNotEmpty(totalJiraIssue)) {
			reliabilityValidationData.setTotalIssueNumbers(totalJiraIssue);
			reliabilityValidationData.setCompletedIssueNumber(completed);
			double sprintSize = totalJiraIssue.size();
			double completedSize = completed.size();
			issueCount = (long) ((completedSize / sprintSize) * 100);

			double totalSum = totalJiraIssue.stream().filter(jiraIssue -> Objects.nonNull(jiraIssue.getStoryPoints()))
					.mapToDouble(JiraIssue::getStoryPoints).sum();
			double completedSum = completed.stream().filter(jiraIssue -> Objects.nonNull(jiraIssue.getStoryPoints()))
					.mapToDouble(JiraIssue::getStoryPoints).sum();
			storyCount = (long) ((completedSum / totalSum) * 100);
			commitmentHowerMap.put(TOTAL_ISSUE_SIZE, sprintSize);
			commitmentHowerMap.put(COMPLETED_ISSUE_SIZE, completedSize);
			commitmentHowerMap.put(TOTAL_STORY_POINTS, totalSum);
			commitmentHowerMap.put(COMPLETED_STORY_POINTS, completedSum);
			validationData.add(reliabilityValidationData);
		}

		commitmentResult.put(ISSUE_COUNT, ObjectUtils.defaultIfNull(issueCount, 0L));
		commitmentResult.put(STORY_POINT, ObjectUtils.defaultIfNull(storyCount, 0L));
		return commitmentResult;

	}

	@Override
	public Long calculateKpiValue(List<Long> valueList, String kpiId) {
		return calculateKpiValueForLong(valueList, kpiId);
	}

	public class CommitmentReliabilityValidationData {
		private List<JiraIssue> totalIssueNumbers;
		private String url;
		private String issueDescription;
		private List<JiraIssue> completedIssueNumber;

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getIssueDescription() {
			return issueDescription;
		}

		public void setIssueDescription(String issueDescription) {
			this.issueDescription = issueDescription;
		}

		public List<JiraIssue> getTotalIssueNumbers() {
			return totalIssueNumbers;
		}

		public void setTotalIssueNumbers(List<JiraIssue> totalIssueNumbers) {
			this.totalIssueNumbers = totalIssueNumbers;
		}

		public List<JiraIssue> getCompletedIssueNumber() {
			return completedIssueNumber;
		}

		public void setCompletedIssueNumber(List<JiraIssue> completedIssueNumber) {
			this.completedIssueNumber = completedIssueNumber;
		}
	}

}
