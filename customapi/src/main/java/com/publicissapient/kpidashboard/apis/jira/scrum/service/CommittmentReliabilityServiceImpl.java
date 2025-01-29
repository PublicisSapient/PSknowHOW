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

import com.publicissapient.kpidashboard.apis.common.service.KpiDataCacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiDataProvider;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.enums.Filters;
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
import com.publicissapient.kpidashboard.common.model.jira.SprintIssue;

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
	private static final String DELIVERED_INITIAL_COMMITMENT = "Delivered out of Initial Commitment";
	private static final String DELIVERED_FINAL_COMMITMENT = "Delivered out of Final Scope";
	private static final String SPECIAL_SYMBOL = "#";

	@Autowired
	private ConfigHelperService configHelperService;
	@Autowired
	private CustomApiConfig customApiConfig;
		@Autowired
	private FilterHelperService filterHelperService;
	@Autowired
	private CacheService cacheService;
	@Autowired
	private KpiDataCacheService kpiDataCacheService;
	@Autowired
	private KpiDataProvider kpiDataProvider;

	private List<String> sprintIdList = Collections.synchronizedList(new ArrayList<>());

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
		sprintIdList = treeAggregatorDetail.getMapOfListOfLeafNodes().get(CommonConstant.SPRINT_MASTER).stream()
				.map(node -> node.getSprintFilter().getId()).collect(Collectors.toList());
		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
			if (Filters.getFilter(k) == Filters.SPRINT) {
				sprintWiseLeafNodeValue(mapTmp, v, kpiElement, kpiRequest);

			}
		});

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValueMap(root, nodeWiseKPIValue, KPICode.COMMITMENT_RELIABILITY);

		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.COMMITMENT_RELIABILITY);
		Map<String, List<DataCount>> unsortedMap = trendValuesMap.entrySet().stream().sorted(Map.Entry.comparingByKey())
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
			// split for filters
			String[] issueFilter = issueType.split(SPECIAL_SYMBOL);
			dataCountGroup.setFilter1(issueFilter[0]);
			dataCountGroup.setFilter2(issueFilter[1]);
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
		long time = System.currentTimeMillis();
		Map<String, Object> resultMap = fetchKPIDataFromDb(sprintLeafNodeList, startDate, endDate, kpiRequest);
		log.info("CommitmentReliability taking fetchKPIDataFromDb {}",
				String.valueOf(System.currentTimeMillis() - time));

		List<JiraIssue> allJiraIssue = (List<JiraIssue>) resultMap.get(PROJECT_WISE_TOTAL_ISSUE);
		List<SprintDetails> sprintDetails = (List<SprintDetails>) resultMap.get(SPRINT_DETAILS);

		Map<Pair<String, String>, Set<JiraIssue>> sprintWiseCreatedIssues = new HashMap<>();
		Map<Pair<String, String>, Set<JiraIssue>> sprintWiseClosedIssues = new HashMap<>();
		Map<Pair<String, String>, Set<JiraIssue>> sprintWiseInitialScopeIssues = new HashMap<>();
		Map<Pair<String, String>, Set<JiraIssue>> sprintWiseInitialScopeCompletedIssues = new HashMap<>();
		Map<Pair<String, String>, Set<String>> sprintWiseAddedIssues = new HashMap<>();
		Map<Pair<String, String>, Set<String>> sprintWiseRemovedIssues = new HashMap<>();

		if (CollectionUtils.isNotEmpty(allJiraIssue) && CollectionUtils.isNotEmpty(sprintDetails)) {
			sprintDetails.forEach(sd -> {
				Set<JiraIssue> totalIssues = KpiDataHelper.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sd,
						sd.getTotalIssues(), allJiraIssue);
				Set<JiraIssue> completedIssues = new HashSet<>();
				completedIssues = getCompletedIssues(allJiraIssue, sd, completedIssues);

				Set<JiraIssue> totalInitialIssues = new HashSet<>(totalIssues);
				// Add the punted issues
				totalInitialIssues.addAll(KpiDataHelper.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sd,
						sd.getPuntedIssues(), allJiraIssue));
				// removed added issues
				Set<String> addedIssues = new HashSet<>();
				if (CollectionUtils.isNotEmpty(sd.getAddedIssues())) {
					totalInitialIssues.removeIf(issue -> sd.getAddedIssues().contains(issue.getNumber()));
					addedIssues.addAll(sd.getAddedIssues());
				}
				Set<String> puntedIssues = CollectionUtils.emptyIfNull(sd.getPuntedIssues()).stream()
						.map(SprintIssue::getNumber).collect(Collectors.toSet());
				Set<JiraIssue> totalCompltdInitialIssues = new HashSet<>(totalInitialIssues);
				totalCompltdInitialIssues = new HashSet<>(
						CollectionUtils.intersection(totalCompltdInitialIssues, completedIssues));
				sprintWiseCreatedIssues.put(Pair.of(sd.getBasicProjectConfigId().toString(), sd.getSprintID()),
						new HashSet<>(totalIssues));
				sprintWiseClosedIssues.put(Pair.of(sd.getBasicProjectConfigId().toString(), sd.getSprintID()),
						new HashSet<>(completedIssues));
				sprintWiseInitialScopeIssues.put(Pair.of(sd.getBasicProjectConfigId().toString(), sd.getSprintID()),
						new HashSet<>(totalInitialIssues));
				sprintWiseInitialScopeCompletedIssues.put(
						Pair.of(sd.getBasicProjectConfigId().toString(), sd.getSprintID()),
						new HashSet<>(totalCompltdInitialIssues));
				sprintWiseAddedIssues.put(Pair.of(sd.getBasicProjectConfigId().toString(), sd.getSprintID()),
						addedIssues);
				sprintWiseRemovedIssues.put(Pair.of(sd.getBasicProjectConfigId().toString(), sd.getSprintID()),
						puntedIssues);
			});
		}

		List<KPIExcelData> excelData = new ArrayList<>();

		sprintLeafNodeList.forEach(node -> {
			String trendLineName = node.getProjectFilter().getName();

			String currentSprintComponentId = node.getSprintFilter().getId();
			Pair<String, String> currentNodeIdentifier = Pair
					.of(node.getProjectFilter().getBasicProjectConfigId().toString(), currentSprintComponentId);

			List<CommitmentReliabilityValidationData> validationDataList = new ArrayList<>();

			Set<JiraIssue> totalPresentJiraIssue = new HashSet<>();
			Set<JiraIssue> totalPresentCompletedIssue = new HashSet<>();
			Set<JiraIssue> totalPresentInitialIssue = new HashSet<>();
			Set<JiraIssue> totalPresentCompltdInitialIssue = new HashSet<>();
			Set<String> totalAddedIssue = new HashSet<>();
			Set<String> totalPuntedIssue = new HashSet<>();

			if (CollectionUtils.isNotEmpty(sprintWiseCreatedIssues.get(currentNodeIdentifier))) {
				totalPresentJiraIssue = sprintWiseCreatedIssues.get(currentNodeIdentifier);
				totalPresentCompletedIssue = sprintWiseClosedIssues.get(currentNodeIdentifier);
				totalPresentInitialIssue = sprintWiseInitialScopeIssues.get(currentNodeIdentifier);
				if (CollectionUtils.isNotEmpty(sprintWiseRemovedIssues.get(currentNodeIdentifier))) {
					totalPuntedIssue.addAll(sprintWiseRemovedIssues.get(currentNodeIdentifier));
				}

				if (CollectionUtils.isNotEmpty(sprintWiseAddedIssues.get(currentNodeIdentifier))) {
					totalAddedIssue.addAll(sprintWiseAddedIssues.get(currentNodeIdentifier));
				}
				totalPresentCompltdInitialIssue = sprintWiseInitialScopeCompletedIssues.get(currentNodeIdentifier);

			}

			Map<String, Double> commitmentHowerMap = new HashMap<>();
			Set<JiraIssue> totalSumIssues = new HashSet<>();
			totalSumIssues.addAll(totalPresentJiraIssue);
			totalSumIssues.addAll(totalPresentCompletedIssue);
			totalSumIssues.addAll(totalPresentInitialIssue);
			totalSumIssues.addAll(totalPresentCompltdInitialIssue);

			List<String> uniqueIssues = totalSumIssues.stream().map(JiraIssue::getTypeName).distinct().toList();
			Map<String, List<JiraIssue>> totalPresentJiraIssueGroup = getGroupByAllIssues(totalPresentJiraIssue);
			Map<String, List<JiraIssue>> totalPresentCompletedIssueGroup = getGroupByAllIssues(
					totalPresentCompletedIssue);
			Map<String, List<JiraIssue>> totalPresentInitialIssueGroup = getGroupByAllIssues(totalPresentInitialIssue);
			Map<String, List<JiraIssue>> totalPresentCompltdInitialIssueGroup = getGroupByAllIssues(
					totalPresentCompltdInitialIssue);
			Map<String, Long> commitmentMap = null;
			Map<String, List<DataCount>> dataCountMap = new HashMap<>();
			for (String issueType : uniqueIssues) {
				Map<String, Double> issueTypeWiseHowerMap = new HashMap<>();
				commitmentMap = getCommitmentMap(totalPresentJiraIssueGroup.getOrDefault(issueType, new ArrayList<>()),
						totalPresentCompletedIssueGroup.getOrDefault(issueType, new ArrayList<>()),
						issueTypeWiseHowerMap, fieldMapping,
						totalPresentInitialIssueGroup.getOrDefault(issueType, new ArrayList<>()),
						totalPresentCompltdInitialIssueGroup.getOrDefault(issueType, new ArrayList<>()), issueType);
				prepareDataCount(issueTypeWiseHowerMap, commitmentMap, trendLineName, node, fieldMapping, dataCountMap);
				commitmentHowerMap.putAll(issueTypeWiseHowerMap);
			}
			commitmentMap = getCommitmentMap(new ArrayList<>(totalPresentJiraIssue),
					new ArrayList<>(totalPresentCompletedIssue), commitmentHowerMap, fieldMapping,
					new ArrayList<>(totalPresentInitialIssue), new ArrayList<>(totalPresentCompltdInitialIssue),
					CommonConstant.OVERALL);
			CommitmentReliabilityValidationData reliabilityValidationData = new CommitmentReliabilityValidationData();
			reliabilityValidationData.setTotalIssueNumbers(totalPresentJiraIssue);
			reliabilityValidationData.setCompletedIssueNumber(totalPresentCompletedIssue);
			reliabilityValidationData.setInitialIssueNumber(totalPresentInitialIssue);
			reliabilityValidationData.setInitialCompletedIssueNumber(totalPresentCompltdInitialIssue);
			reliabilityValidationData.setAddedIssues(totalAddedIssue);
			reliabilityValidationData.setPuntedIssues(totalPuntedIssue);
			validationDataList.add(reliabilityValidationData);
			populateExcelData(requestTrackerId, excelData, validationDataList, node, fieldMapping);
			prepareDataCount(commitmentHowerMap, commitmentMap, trendLineName, node, fieldMapping, dataCountMap);

			mapTmp.get(node.getId()).setValue(dataCountMap);

		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.COMMITMENT_RELIABILITY.getColumns(sprintLeafNodeList, cacheService,
				filterHelperService));
	}

	private static Set<JiraIssue> getCompletedIssues(List<JiraIssue> allJiraIssue, SprintDetails sd,
			Set<JiraIssue> completedIssues) {
		if (sd.getCompletedIssues() != null && CollectionUtils.isNotEmpty(sd.getCompletedIssues())) {
			completedIssues = KpiDataHelper.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sd,
					sd.getCompletedIssues(), allJiraIssue);
		}
		return completedIssues;
	}

	public Map<String, List<JiraIssue>> getGroupByAllIssues(Set<JiraIssue> jiraIssuesList) {
		return jiraIssuesList.stream().collect(Collectors.groupingBy(JiraIssue::getTypeName));
	}

	public void prepareDataCount(Map<String, Double> commitmentHowerMap, Map<String, Long> commitmentMap,
			String trendLineName, Node node, FieldMapping fieldMapping, Map<String, List<DataCount>> dataCountMap) {
		for (Map.Entry<String, Long> map : commitmentMap.entrySet()) {
			DataCount dataCount = new DataCount();
			dataCount.setData(String.valueOf(map.getValue()));
			dataCount.setSProjectName(trendLineName);
			dataCount.setSSprintID(node.getSprintFilter().getId());
			dataCount.setSSprintName(node.getSprintFilter().getName());
			dataCount.setValue(map.getValue());
			dataCount.setKpiGroup(map.getKey());
			// split for filter
			String[] keyIssues = map.getKey().split(SPECIAL_SYMBOL);
			dataCount.setHoverValue(generateHowerMap(commitmentHowerMap, keyIssues[0], fieldMapping));
			dataCountMap.put(map.getKey(), new ArrayList<>(Arrays.asList(dataCount)));
		}

	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		Map<ObjectId, List<String>> projectWiseSprints = new HashMap<>();
		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			projectWiseSprints.putIfAbsent(basicProjectConfigId, new ArrayList<>());
			projectWiseSprints.get(basicProjectConfigId).add(leaf.getSprintFilter().getId());
		});

		List<JiraIssue> totalIssue = new ArrayList<>();
		List<SprintDetails> sprintDetails = new ArrayList<>();
		boolean fetchCachedData = filterHelperService.isFilterSelectedTillSprintLevel(kpiRequest.getLevel(), false);
		projectWiseSprints.forEach((basicProjectConfigId, sprintList) -> {
			Map<String, Object> result;
			if (fetchCachedData) {// fetch data from cache only if Filter is selected till Sprint level.
				result = kpiDataCacheService.fetchCommitmentReliabilityData(kpiRequest, basicProjectConfigId,
						sprintIdList, KPICode.COMMITMENT_RELIABILITY.getKpiId());
			} else {// fetch data from DB if filters below Sprint level (i.e. additional filters)
				result = kpiDataProvider.fetchCommitmentReliabilityData(kpiRequest, basicProjectConfigId, sprintList);
			}
			List<JiraIssue> allJiraIssue = (List<JiraIssue>) result.get(PROJECT_WISE_TOTAL_ISSUE);
			List<SprintDetails> sprintDetailsList = (List<SprintDetails>) result.get(SPRINT_DETAILS);

			sprintDetails.addAll(sprintDetailsList.stream().filter(sprint -> sprintList.contains(sprint.getSprintID()))
					.collect(Collectors.toSet()));
			totalIssue.addAll(allJiraIssue);
		});

		if (CollectionUtils.isNotEmpty(totalIssue)) {
			resultListMap.put(PROJECT_WISE_TOTAL_ISSUE, totalIssue);
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
				howerMap.put(DELIVERED_FINAL_COMMITMENT, commitmentHowerMap.getOrDefault(COMPLETED_ISSUE_SIZE, 0.0d));
				howerMap.put(DELIVERED_INITIAL_COMMITMENT,
						commitmentHowerMap.getOrDefault(INITIALCMPLTD_ISSUE_SIZE, 0.0d));
				howerMap.put(INITIALLYCOMMITED, commitmentHowerMap.getOrDefault(INITIAL_ISSUE_SIZE, 0.0d));
			} else if (INITIAL_STORY_POINT.equalsIgnoreCase(key)) {
				howerMap.put(DELIVERED_FINAL_COMMITMENT, commitmentHowerMap.getOrDefault(COMPLETED_STORY_POINTS, 0.0d));
				howerMap.put(DELIVERED_INITIAL_COMMITMENT,
						commitmentHowerMap.getOrDefault(INITIALCMPLTDISSUE_STORY_POINTS, 0.0d));
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
				howerMap.put(DELIVERED_FINAL_COMMITMENT, commitmentHowerMap.getOrDefault(COMPLETED_ISSUE_SIZE, 0.0d));
				howerMap.put(DELIVERED_INITIAL_COMMITMENT,
						commitmentHowerMap.getOrDefault(INITIALCMPLTD_ISSUE_SIZE, 0.0d));
				howerMap.put(INITIALLYCOMMITED, commitmentHowerMap.getOrDefault(INITIAL_ISSUE_SIZE, 0.0d));
			} else if (INITIAL_ORIGINAL_ESTIMATE.equalsIgnoreCase(key)) {
				howerMap.put(DELIVERED_FINAL_COMMITMENT, commitmentHowerMap.get(COMPLETED_ORIGINAL_ESTIMATE) + " hrs");
				howerMap.put(DELIVERED_INITIAL_COMMITMENT,
						commitmentHowerMap.get(INITIALCMPLTD_ORIGINAL_ESTIMATE) + " hrs");
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
					KPIExcelUtility.populateCommittmentReliability(sprintName, totalSprintStoryMap, data, excelData,
							fieldMapping, customApiConfig);

				});

			}
		}

	}

	/**
	 * @param totalJiraIssue
	 * @param completed
	 * @param totalPresentInitialIssue
	 * @param totalPresentCompltdInitialIssue
	 * @return
	 */
	private Map<String, Long> getCommitmentMap(List<JiraIssue> totalJiraIssue, List<JiraIssue> completed,
			Map<String, Double> commitmentHowerMap, FieldMapping fieldMapping, List<JiraIssue> totalPresentInitialIssue,
			List<JiraIssue> totalPresentCompltdInitialIssue, String issues) {

		Map<String, Long> commitmentResult = new LinkedHashMap<>();

		double sprintSize = totalJiraIssue.size();
		double completedSize = completed.size();
		long issueCount = (long) ((completedSize / sprintSize) * 100);
		double totalSum = getTotalSum(totalJiraIssue);
		double completedSum = getTotalSum(completed);
		double totalOriginalEstimate = totalJiraIssue.stream()
				.filter(jiraIssue -> Objects.nonNull(jiraIssue.getAggregateTimeOriginalEstimateMinutes()))
				.mapToDouble(JiraIssue::getAggregateTimeOriginalEstimateMinutes).sum();
		double completedOriginalEstimate = completed.stream()
				.filter(jiraIssue -> Objects.nonNull(jiraIssue.getAggregateTimeOriginalEstimateMinutes()))
				.mapToDouble(JiraIssue::getAggregateTimeOriginalEstimateMinutes).sum();

		double initialIssueSize = totalPresentInitialIssue.size();
		double initialCompltdIssueSize = totalPresentCompltdInitialIssue.size();
		long initialIssueCount = (long) ((initialCompltdIssueSize / initialIssueSize) * 100);

		double initialIssueSum = getTotalSum(totalPresentInitialIssue);
		double initialCompltdIssueSum = getTotalSum(totalPresentCompltdInitialIssue);
		double initialIssueOriginalEstimate = totalPresentInitialIssue.stream()
				.filter(jiraIssue -> Objects.nonNull(jiraIssue.getAggregateTimeOriginalEstimateMinutes()))
				.mapToDouble(JiraIssue::getAggregateTimeOriginalEstimateMinutes).sum();
		double initialCompltdIssueOriginalEstimate = totalPresentCompltdInitialIssue.stream()
				.filter(jiraIssue -> Objects.nonNull(jiraIssue.getAggregateTimeOriginalEstimateMinutes()))
				.mapToDouble(JiraIssue::getAggregateTimeOriginalEstimateMinutes).sum();

		long storyCount = (long) ((completedSum / totalSum) * 100);
		long initialStoryCount = (long) ((initialCompltdIssueSum / initialIssueSum) * 100);
		Double totalOriginalEstimateInHours = totalOriginalEstimate / 60;
		Double completedOriginalEstimateInHours = completedOriginalEstimate / 60;
		Double initialOriginalEstimateInHours = initialIssueOriginalEstimate / 60;
		Double initialCompltdOriginalEstimateInHours = initialCompltdIssueOriginalEstimate / 60;
		long totalHours = (long) ((completedOriginalEstimateInHours / totalOriginalEstimateInHours) * 100);
		long initialTotalHours = (long) ((initialCompltdOriginalEstimateInHours / initialOriginalEstimateInHours)
				* 100);
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

		if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
				&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
			commitmentResult.put(FINAL_SCOPE_STORY_POINTS + SPECIAL_SYMBOL + issues,
					ObjectUtils.defaultIfNull(storyCount, 0L));
			commitmentResult.put(INITIAL_STORY_POINT + SPECIAL_SYMBOL + issues,
					ObjectUtils.defaultIfNull(initialStoryCount, 0L));
		} else {
			commitmentResult.put(FINAL_SCOPE_ORIGINAL_ESTIMATE + SPECIAL_SYMBOL + issues,
					ObjectUtils.defaultIfNull(totalHours, 0L));
			commitmentResult.put(INITIAL_ORIGINAL_ESTIMATE + SPECIAL_SYMBOL + issues,
					ObjectUtils.defaultIfNull(initialTotalHours, 0L));
		}
		commitmentResult.put(FINAL_SCOPE_COUNT + SPECIAL_SYMBOL + issues, ObjectUtils.defaultIfNull(issueCount, 0L));
		commitmentResult.put(INITIAL_ISSUE_COUNT + SPECIAL_SYMBOL + issues,
				ObjectUtils.defaultIfNull(initialIssueCount, 0L));
		return commitmentResult;

	}

	private double getTotalSum(List<JiraIssue> totalJiraIssue) {
		return totalJiraIssue.stream().filter(jiraIssue -> Objects.nonNull(jiraIssue.getStoryPoints()))
				.mapToDouble(JiraIssue::getStoryPoints).sum();

	}

	@Override
	public Long calculateKpiValue(List<Long> valueList, String kpiId) {
		return calculateKpiValueForLong(valueList, kpiId);
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping) {
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI72(),
				KPICode.COMMITMENT_RELIABILITY.getKpiId());
	}

	@Getter
	@Setter
	public class CommitmentReliabilityValidationData {
		private Set<JiraIssue> totalIssueNumbers;
		private String url;
		private String issueDescription;
		private Set<JiraIssue> completedIssueNumber;
		private Set<JiraIssue> initialIssueNumber;
		private Set<JiraIssue> initialCompletedIssueNumber;
		private Set<String> addedIssues;
		private Set<String> puntedIssues;
	}

}