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
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
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

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CommittmentReliabilityServiceImpl extends JiraKPIService<Long, List<Object>, Map<String, Object>> {
	private static final String FINAL_SCOPE_COUNT = "Final Scope (Count)";
	private static final String INITIAL_ISSUE_COUNT = "Initial Commitment (Count)";
	private static final String FINAL_SCOPE_STORY_POINTS = "Final Scope (Story Points)";
	private static final String INITIAL_STORY_POINT = "Initial Commitment (Story Points)";
	private static final String INITIAL_ORIGINAL_ESTIMATE = "Initial Commitment (Hours)";
	private static final String FINAL_SCOPE_ORIGINAL_ESTIMATE = "Final Scope (Hours)";
	private static final String PROJECT_WISE_TOTAL_ISSUE = "projectWiseTotalIssues";
	private static final String DEV = "DeveloperKpi";
	private static final String TOTAL_ISSUE_SIZE = "totalIssueSize";
	private static final String COMPLETED_ISSUE_SIZE = "completedIssueSize";
	private static final String INITIAL_ISSUE_SIZE = "initialIssueSize";
	private static final String INITIALCMPLTD_ISSUE_SIZE = "initialCompletedIssueSize";
	private static final String TOTAL_STORY_POINTS = "totalStoryPoints";
	private static final String COMPLETED_STORY_POINTS = "completedStoryPoints";
	private static final String INITIALISSUE_STORY_POINTS = "initialIssueStoryPoint";
	private static final String INITIALCMPLTDISSUE_STORY_POINTS = "initialCompletedIssueStoryPoint";
	private static final String DELIVERED = "Delivered";
	private static final String COMMITTED = "Final Scope";
	private static final String INITIALLYCOMMITED = "Initially Commited";
	private static final String SPRINT_DETAILS = "sprintDetails";
	private static final String TOTAL_ORIGINAL_ESTIMATE = "totalOriginalEstimate";
	private static final String COMPLETED_ORIGINAL_ESTIMATE = "completedOriginalEstimate";
	private static final String INITIALISSUE_ORIGINAL_ESTIMATE = "initialIssueOriginalEstimate";
	private static final String INITIALCMPLTD_ORIGINAL_ESTIMATE = "initialCompletedIssueOriginalEstimate";
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
		Map<String, List<DataCount>> unsortedMap = trendValuesMap.entrySet().stream()
				.sorted(Collections.reverseOrder(Map.Entry.comparingByKey()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
		Map<String, Map<String, List<DataCount>>> statusTypeProjectWiseDc = new LinkedHashMap<>();
		unsortedMap.forEach((statusType, dataCounts) -> {
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
		FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
				.get(sprintLeafNodeList.get(0).getProjectFilter().getBasicProjectConfigId());

		Map<String, Object> resultMap = fetchKPIDataFromDb(sprintLeafNodeList, startDate, endDate, kpiRequest);

		List<JiraIssue> allJiraIssue = (List<JiraIssue>) resultMap.get(PROJECT_WISE_TOTAL_ISSUE);
		List<SprintDetails> sprintDetails = (List<SprintDetails>) resultMap.get(SPRINT_DETAILS);

		Map<Pair<String, String>, List<JiraIssue>> sprintWiseCreatedIssues = new HashMap<>();
		Map<Pair<String, String>, List<JiraIssue>> sprintWiseClosedIssues = new HashMap<>();
		Map<Pair<String, String>, List<JiraIssue>> sprintWiseInitialScopeIssues = new HashMap<>();
		Map<Pair<String, String>, List<JiraIssue>> sprintWisePuntedIssues = new HashMap<>();
		Map<Pair<String, String>, List<JiraIssue>> sprintWiseInitialScopeCompletedIssues = new HashMap<>();

		if (CollectionUtils.isNotEmpty(allJiraIssue) && CollectionUtils.isNotEmpty(sprintDetails)) {
			sprintDetails.forEach(sd -> {
				Set<JiraIssue> totalIssues = KpiDataHelper.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sd,
						sd.getTotalIssues(), allJiraIssue);
				Set<JiraIssue> completedIssues = KpiDataHelper.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sd,
						sd.getCompletedIssues(), allJiraIssue);
				Set<JiraIssue> totalInitialIssues = new HashSet<>(totalIssues);
				// Add the punted issues
				totalInitialIssues.addAll(KpiDataHelper.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sd,
						sd.getPuntedIssues(), allJiraIssue));
				// removed added issues
				if (CollectionUtils.isNotEmpty(sd.getAddedIssues())) {
					totalInitialIssues.removeIf(issue -> sd.getAddedIssues().contains(issue.getNumber()));
				}
				Set<JiraIssue> totalCompltdInitialIssues = new HashSet<>(totalInitialIssues);
				totalCompltdInitialIssues = new HashSet<>(
						CollectionUtils.intersection(totalCompltdInitialIssues, completedIssues));

				sprintWiseCreatedIssues.put(Pair.of(sd.getBasicProjectConfigId().toString(), sd.getSprintID()),
						new ArrayList<>(totalIssues));
				sprintWiseClosedIssues.put(Pair.of(sd.getBasicProjectConfigId().toString(), sd.getSprintID()),
						new ArrayList<>(completedIssues));
				sprintWiseInitialScopeIssues.put(Pair.of(sd.getBasicProjectConfigId().toString(), sd.getSprintID()),
						new ArrayList<>(totalInitialIssues));
				sprintWiseInitialScopeCompletedIssues.put(
						Pair.of(sd.getBasicProjectConfigId().toString(), sd.getSprintID()),
						new ArrayList<>(totalCompltdInitialIssues));
			});
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
			List<JiraIssue> totalPresentInitialIssue = new ArrayList<>();
			List<JiraIssue> totalPresentCompltdInitialIssue = new ArrayList<>();

			if (CollectionUtils.isNotEmpty(sprintWiseCreatedIssues.get(currentNodeIdentifier))) {
				totalPresentJiraIssue = sprintWiseCreatedIssues.get(currentNodeIdentifier);
				totalPresentCompletedIssue = sprintWiseClosedIssues.get(currentNodeIdentifier);
				totalPresentInitialIssue = sprintWiseInitialScopeIssues.get(currentNodeIdentifier);
				if (CollectionUtils.isNotEmpty(sprintWisePuntedIssues.get(currentNodeIdentifier))) {
					totalPresentInitialIssue.addAll(sprintWisePuntedIssues.get(currentNodeIdentifier));
				}
				totalPresentCompltdInitialIssue = sprintWiseInitialScopeCompletedIssues.get(currentNodeIdentifier);

			}

			Map<String, Double> commitmentHowerMap = new HashMap<>();
			Map<String, Long> commitmentMap = getCommitmentMap(totalPresentJiraIssue, totalPresentCompletedIssue,
					validationDataList, commitmentHowerMap, fieldMapping, totalPresentInitialIssue,
					totalPresentCompltdInitialIssue);
			populateExcelData(requestTrackerId, excelData, validationDataList, node, fieldMapping);

			Map<String, List<DataCount>> dataCountMap = new HashMap<>();

			for (Map.Entry<String, Long> map : commitmentMap.entrySet()) {
				DataCount dataCount = new DataCount();
				dataCount.setData(String.valueOf(map.getValue()));
				dataCount.setSProjectName(trendLineName);
				dataCount.setSSprintID(node.getSprintFilter().getId());
				dataCount.setSSprintName(node.getSprintFilter().getName());
				dataCount.setValue(map.getValue());
				dataCount.setKpiGroup(map.getKey());
				dataCount.setHoverValue(generateHowerMap(commitmentHowerMap, map.getKey(), fieldMapping));
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
		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			sprintList.add(leaf.getSprintFilter().getId());
			basicProjectConfigIds.add(basicProjectConfigId.toString());
		});

		List<SprintDetails> sprintDetails = new ArrayList<>(sprintRepository.findBySprintIDIn(sprintList));
		Set<String> totalIssue = new HashSet<>();
		sprintDetails.stream().forEach(dbSprintDetail -> {
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(dbSprintDetail.getBasicProjectConfigId());
			// to modify sprintdetails on the basis of configuration for the project
			SprintDetails sprintDetail=KpiDataHelper.processSprintBasedOnFieldMappings(Collections.singletonList(dbSprintDetail),
					fieldMapping.getJiraIterationIssuetypeKpi72(),
					fieldMapping.getJiraIterationCompletionStatusKpi72()).get(0);
			if (CollectionUtils.isNotEmpty(sprintDetail.getTotalIssues())) {
				totalIssue.addAll(KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetail,
						CommonConstant.TOTAL_ISSUES));
			}
			if (CollectionUtils.isNotEmpty(sprintDetail.getPuntedIssues())) {
				totalIssue.addAll(KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetail,
						CommonConstant.PUNTED_ISSUES));

			}
			if (CollectionUtils.isNotEmpty(sprintDetail.getAddedIssues())) {
				totalIssue.addAll(sprintDetail.getAddedIssues());
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
		}
		return resultListMap;
	}

	/**
	 * generate Hower Map
	 *
	 * @param commitmentHowerMap
	 * @param key
	 * @return
	 */
	private Map<String, Object> generateHowerMap(Map<String, Double> commitmentHowerMap, String key,
			FieldMapping fieldMapping) {
		Map<String, Object> howerMap = new LinkedHashMap<>();
		if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
				&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
			if (FINAL_SCOPE_COUNT.equalsIgnoreCase(key)) {
				howerMap.put(DELIVERED, commitmentHowerMap.getOrDefault(COMPLETED_ISSUE_SIZE, 0.0d));
				howerMap.put(COMMITTED, commitmentHowerMap.getOrDefault(TOTAL_ISSUE_SIZE, 0.0d));
			} else if (INITIAL_ISSUE_COUNT.equalsIgnoreCase(key)) {
				howerMap.put(DELIVERED, commitmentHowerMap.getOrDefault(INITIALCMPLTD_ISSUE_SIZE, 0.0d));
				howerMap.put(INITIALLYCOMMITED, commitmentHowerMap.getOrDefault(INITIAL_ISSUE_SIZE, 0.0d));
			} else if (INITIAL_STORY_POINT.equalsIgnoreCase(key)) {
				howerMap.put(DELIVERED, commitmentHowerMap.getOrDefault(INITIALCMPLTDISSUE_STORY_POINTS, 0.0d));
				howerMap.put(INITIALLYCOMMITED, commitmentHowerMap.getOrDefault(INITIALISSUE_STORY_POINTS, 0.0d));
			} else {
				howerMap.put(DELIVERED, commitmentHowerMap.getOrDefault(COMPLETED_STORY_POINTS, 0.0d));
				howerMap.put(COMMITTED, commitmentHowerMap.getOrDefault(TOTAL_STORY_POINTS, 0.0d));
			}
		} else {
			if (FINAL_SCOPE_COUNT.equalsIgnoreCase(key)) {
				howerMap.put(DELIVERED, commitmentHowerMap.getOrDefault(COMPLETED_ISSUE_SIZE, 0.0d));
				howerMap.put(COMMITTED, commitmentHowerMap.getOrDefault(TOTAL_ISSUE_SIZE, 0.0d));
			} else if (INITIAL_ISSUE_COUNT.equalsIgnoreCase(key)) {
				howerMap.put(DELIVERED, commitmentHowerMap.getOrDefault(INITIALCMPLTD_ISSUE_SIZE, 0.0d));
				howerMap.put(INITIALLYCOMMITED, commitmentHowerMap.getOrDefault(INITIAL_ISSUE_SIZE, 0.0d));
			} else if (INITIAL_ORIGINAL_ESTIMATE.equalsIgnoreCase(key)) {
				howerMap.put(DELIVERED, commitmentHowerMap.get(INITIALCMPLTD_ORIGINAL_ESTIMATE) + " hrs");
				howerMap.put(INITIALLYCOMMITED, commitmentHowerMap.get(INITIALISSUE_ORIGINAL_ESTIMATE) + " hrs");
			} else {
				howerMap.put(DELIVERED, commitmentHowerMap.get(COMPLETED_ORIGINAL_ESTIMATE) + " hrs");
				howerMap.put(COMMITTED, commitmentHowerMap.get(TOTAL_ORIGINAL_ESTIMATE) + " hrs");
			}
		}
		return howerMap;
	}

	/**
	 * generate Excel
	 *
	 * @param requestTrackerId
	 */
	private void populateExcelData(String requestTrackerId, List<KPIExcelData> excelData,
			List<CommitmentReliabilityValidationData> validationDataList, Node node, FieldMapping fieldMapping) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			String sprintName = node.getSprintFilter().getName();
			if (CollectionUtils.isNotEmpty(validationDataList)) {
				validationDataList.stream().forEach(data -> {

					Map<String, JiraIssue> totalSprintStoryMap = new HashMap<>();
					data.getTotalIssueNumbers().stream()
							.forEach(issue -> totalSprintStoryMap.putIfAbsent(issue.getNumber(), issue));
					data.getInitialIssueNumber().stream()
							.forEach(issue -> totalSprintStoryMap.putIfAbsent(issue.getNumber(), issue));
					KPIExcelUtility.populateCommittmentReliability(sprintName, totalSprintStoryMap,
							data.getInitialIssueNumber(), excelData, fieldMapping);

				});

			}
		}

	}

	/**
	 * @param totalJiraIssue
	 * @param completed
	 * @param validationData
	 * @param totalPresentInitialIssue
	 * @param totalPresentCompltdInitialIssue
	 * @return
	 */
	private Map<String, Long> getCommitmentMap(List<JiraIssue> totalJiraIssue, List<JiraIssue> completed,
			List<CommitmentReliabilityValidationData> validationData, Map<String, Double> commitmentHowerMap,
			FieldMapping fieldMapping, List<JiraIssue> totalPresentInitialIssue,
			List<JiraIssue> totalPresentCompltdInitialIssue) {

		Map<String, Long> commitmentResult = new LinkedHashMap<>();
		long issueCount = 0L;
		long storyCount = 0L;
		long totalHours = 0L;
		long initialIssueCount = 0l;
		long initialStoryCount = 0l;
		long initialTotalHours = 0L;
		CommitmentReliabilityValidationData reliabilityValidationData = new CommitmentReliabilityValidationData();
		if (CollectionUtils.isNotEmpty(totalJiraIssue)) {
			reliabilityValidationData.setTotalIssueNumbers(totalJiraIssue);
			reliabilityValidationData.setCompletedIssueNumber(completed);
			reliabilityValidationData.setInitialIssueNumber(totalPresentInitialIssue);
			reliabilityValidationData.setInitialCompletedIssueNumber(totalPresentCompltdInitialIssue);
			double sprintSize = totalJiraIssue.size();
			double completedSize = completed.size();
			double initialIssueSize = totalPresentInitialIssue.size();
			double initialCompltdIssueSize = totalPresentCompltdInitialIssue.size();
			issueCount = (long) ((completedSize / sprintSize) * 100);
			initialIssueCount = (long) ((initialCompltdIssueSize / initialIssueSize) * 100);

			double totalSum = totalJiraIssue.stream().filter(jiraIssue -> Objects.nonNull(jiraIssue.getStoryPoints()))
					.mapToDouble(JiraIssue::getStoryPoints).sum();
			double completedSum = completed.stream().filter(jiraIssue -> Objects.nonNull(jiraIssue.getStoryPoints()))
					.mapToDouble(JiraIssue::getStoryPoints).sum();
			double initialIssueSum = totalPresentInitialIssue.stream()
					.filter(jiraIssue -> Objects.nonNull(jiraIssue.getStoryPoints()))
					.mapToDouble(JiraIssue::getStoryPoints).sum();
			double initialCompltdIssueSum = totalPresentCompltdInitialIssue.stream()
					.filter(jiraIssue -> Objects.nonNull(jiraIssue.getStoryPoints()))
					.mapToDouble(JiraIssue::getStoryPoints).sum();
			double totalOriginalEstimate = totalJiraIssue.stream()
					.filter(jiraIssue -> Objects.nonNull(jiraIssue.getOriginalEstimateMinutes()))
					.mapToDouble(JiraIssue::getOriginalEstimateMinutes).sum();
			double completedOriginalEstimate = completed.stream()
					.filter(jiraIssue -> Objects.nonNull(jiraIssue.getOriginalEstimateMinutes()))
					.mapToDouble(JiraIssue::getOriginalEstimateMinutes).sum();
			double initialIssueOriginalEstimate = totalPresentInitialIssue.stream()
					.filter(jiraIssue -> Objects.nonNull(jiraIssue.getOriginalEstimateMinutes()))
					.mapToDouble(JiraIssue::getOriginalEstimateMinutes).sum();
			double initialCompltdIssueOriginalEstimate = totalPresentCompltdInitialIssue.stream()
					.filter(jiraIssue -> Objects.nonNull(jiraIssue.getOriginalEstimateMinutes()))
					.mapToDouble(JiraIssue::getOriginalEstimateMinutes).sum();

			storyCount = (long) ((completedSum / totalSum) * 100);
			initialStoryCount = (long) ((initialCompltdIssueSum / initialIssueSum) * 100);
			Double totalOriginalEstimateInHours = totalOriginalEstimate / 60;
			Double completedOriginalEstimateInHours = completedOriginalEstimate / 60;
			Double initialOriginalEstimateInHours = initialIssueOriginalEstimate / 60;
			Double initialCompltdOriginalEstimateInHours = initialCompltdIssueOriginalEstimate / 60;
			totalHours = (long) ((completedOriginalEstimateInHours / totalOriginalEstimateInHours) * 100);
			initialTotalHours = (long) ((initialCompltdOriginalEstimateInHours / initialOriginalEstimateInHours) * 100);
			commitmentHowerMap.put(TOTAL_ORIGINAL_ESTIMATE, totalOriginalEstimateInHours);
			commitmentHowerMap.put(COMPLETED_ORIGINAL_ESTIMATE, completedOriginalEstimateInHours);
			commitmentHowerMap.put(INITIALISSUE_ORIGINAL_ESTIMATE, initialOriginalEstimateInHours);
			commitmentHowerMap.put(INITIALCMPLTD_ORIGINAL_ESTIMATE, initialCompltdOriginalEstimateInHours);
			commitmentHowerMap.put(TOTAL_ISSUE_SIZE, sprintSize);
			commitmentHowerMap.put(COMPLETED_ISSUE_SIZE, completedSize);
			commitmentHowerMap.put(INITIAL_ISSUE_SIZE, initialIssueSize);
			commitmentHowerMap.put(INITIALCMPLTD_ISSUE_SIZE, initialCompltdIssueSize);
			commitmentHowerMap.put(TOTAL_STORY_POINTS, totalSum);
			commitmentHowerMap.put(COMPLETED_STORY_POINTS, completedSum);
			commitmentHowerMap.put(INITIALISSUE_STORY_POINTS, initialIssueSum);
			commitmentHowerMap.put(INITIALCMPLTDISSUE_STORY_POINTS, initialCompltdIssueSum);
			validationData.add(reliabilityValidationData);
		}

		if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
				&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
			commitmentResult.put(FINAL_SCOPE_STORY_POINTS, ObjectUtils.defaultIfNull(storyCount, 0L));
			commitmentResult.put(INITIAL_STORY_POINT, ObjectUtils.defaultIfNull(initialStoryCount, 0L));
		} else {
			commitmentResult.put(FINAL_SCOPE_ORIGINAL_ESTIMATE, ObjectUtils.defaultIfNull(totalHours, 0L));
			commitmentResult.put(INITIAL_ORIGINAL_ESTIMATE, ObjectUtils.defaultIfNull(initialTotalHours, 0L));
		}
		commitmentResult.put(FINAL_SCOPE_COUNT, ObjectUtils.defaultIfNull(issueCount, 0L));
		commitmentResult.put(INITIAL_ISSUE_COUNT, ObjectUtils.defaultIfNull(initialIssueCount, 0L));
		return commitmentResult;

	}

	@Override
	public Long calculateKpiValue(List<Long> valueList, String kpiId) {
		return calculateKpiValueForLong(valueList, kpiId);
	}

	@Getter
	@Setter
	public class CommitmentReliabilityValidationData {
		private List<JiraIssue> totalIssueNumbers;
		private String url;
		private String issueDescription;
		private List<JiraIssue> completedIssueNumber;
		private List<JiraIssue> initialIssueNumber;
		private List<JiraIssue> initialCompletedIssueNumber;
	}

}
