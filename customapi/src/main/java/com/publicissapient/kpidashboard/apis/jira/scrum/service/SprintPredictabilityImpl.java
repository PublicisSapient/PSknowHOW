package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.AtomicDouble;
import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ValidationData;
import com.publicissapient.kpidashboard.common.model.jira.IssueDetails;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintWiseStory;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SprintPredictabilityImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {

	private static final Integer SP_CONSTANT = 3;
	private static final String DEV = "DeveloperKpi";
	private static final String SPRINT_WISE_PREDICTABILITY = "predictability";
	private static final String SPRINT_WISE_SPRINT_DETAILS = "sprintWiseSprintDetailMap";

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private CustomApiConfig customApiConfig;

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private KpiHelperService kpiHelperService;

	@Autowired
	private FilterHelperService flterHelperService;

	@Autowired
	private SprintRepository sprintRepository;

	/**
	 * Gets Qualifier Type
	 *
	 * @return KPICode's <tt>SPRINT_PREDICTABILITY</tt> enum
	 */
	@Override
	public String getQualifierType() {
		return KPICode.SPRINT_PREDICTABILITY.name();
	}

	/**
	 * Gets KPI Data
	 *
	 * @param kpiRequest
	 * @param kpiElement
	 * @param treeAggregatorDetail
	 * @return KpiElement
	 * @throws ApplicationException
	 */
	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		List<DataCount> trendValueList = new ArrayList<>();
		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();

		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
			if (Filters.getFilter(k) == Filters.SPRINT) {
				sprintWiseLeafNodeValue(mapTmp, v, trendValueList, kpiElement, kpiRequest);
			}
		});

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValue(root, nodeWiseKPIValue, KPICode.SPRINT_PREDICTABILITY);
		List<DataCount> trendValues = getTrendValues(kpiRequest, nodeWiseKPIValue, KPICode.SPRINT_PREDICTABILITY);
		kpiElement.setTrendValueList(trendValues);
		log.debug("[SPRINTPREDICTABILITY-LEAF-NODE-VALUE][{}]. Aggregated Value at each level in the tree {}",
				kpiRequest.getRequestTrackerId(), root);
		return kpiElement;
	}

	/**
	 * Fetches KPI Data from DB
	 *
	 * @param leafNodeList
	 * @param startDate
	 * @param endDate
	 * @param kpiRequest
	 * @return {@code Map<String, Object>}
	 */
	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {

		Map<String, Object> resultListMap = new HashMap<>();
		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		List<String> sprintList = new ArrayList<>();
		List<String> basicProjectConfigIds = new ArrayList<>();
		Set<ObjectId> basicProjectConfigObjectIds = new HashSet<>();
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
		Map<String, List<String>> closedStatusMap = new HashMap<>();
		Map<String, List<String>> typeNameMap = new HashMap<>();

		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();

			sprintList.add(leaf.getSprintFilter().getId());
			basicProjectConfigIds.add(basicProjectConfigId.toString());
			basicProjectConfigObjectIds.add(basicProjectConfigId);
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);

			mapOfProjectFilters.put(JiraFeature.JIRA_ISSUE_STATUS.getFieldValueInFeature(),
					CommonUtils.convertToPatternList(fieldMapping.getJiraIssueDeliverdStatus()));
			mapOfProjectFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
					CommonUtils.convertToPatternList(fieldMapping.getJiraSprintVelocityIssueType()));
			closedStatusMap.put(basicProjectConfigId.toString(), fieldMapping.getJiraIssueDeliverdStatus());
			typeNameMap.put(basicProjectConfigId.toString(), fieldMapping.getJiraSprintVelocityIssueType());
			uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);

		});

		List<SprintDetails> totalSprintDetails = sprintRepository
				.findByBasicProjectConfigIdInAndStateOrderByStartDateDesc(basicProjectConfigObjectIds,
						SprintDetails.SPRINT_STATE_CLOSED);
		List<String> totalIssueIds = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(totalSprintDetails)) {

			Map<ObjectId, List<SprintDetails>> projectWiseTotalSprintDetails = totalSprintDetails.stream()
					.collect(Collectors.groupingBy(SprintDetails::getBasicProjectConfigId));

			List<SprintDetails> projectWiseSprintDetails = new ArrayList<>();
			projectWiseTotalSprintDetails.forEach((basicProjectConfigId, sprintDetailsList) -> {
				List<SprintDetails> sprintDetails = sprintDetailsList.stream()
						.limit(Long.valueOf(customApiConfig.getSprintCountForFilters()) + SP_CONSTANT)
						.collect(Collectors.toList());
				sprintDetails.stream().forEach(sprintDetail -> {
					if (CollectionUtils.isNotEmpty(sprintDetail.getCompletedIssues())) {
						List<String> sprintWiseIssueIds = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(
								sprintDetail, CommonConstant.COMPLETED_ISSUES);
						totalIssueIds.addAll(sprintWiseIssueIds);
					}
					projectWiseSprintDetails.addAll(sprintDetails);
				});
				resultListMap.put(SPRINT_WISE_SPRINT_DETAILS, projectWiseSprintDetails);
				mapOfFilters.put(JiraFeature.ISSUE_NUMBER.getFieldValueInFeature(),
						totalIssueIds.stream().distinct().collect(Collectors.toList()));

			});
		} else {
			mapOfFilters.put(JiraFeature.SPRINT_ID.getFieldValueInFeature(),
					sprintList.stream().distinct().collect(Collectors.toList()));
		}

		/** additional filter **/
		KpiDataHelper.createAdditionalFilterMap(kpiRequest, mapOfFilters, Constant.SCRUM, DEV, flterHelperService);

		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));

		if (CollectionUtils.isNotEmpty(totalIssueIds)) {
			List<JiraIssue> sprintWiseJiraList = jiraIssueRepository.findIssuesBySprintAndType(mapOfFilters,
					new HashMap<>());
			resultListMap.put(SPRINT_WISE_PREDICTABILITY, sprintWiseJiraList);
		} else {
			// start: for azure board sprint details collections put is empty due to we did
			// not have required data of issues.
			List<JiraIssue> sprintWiseJiraList = jiraIssueRepository.findIssuesBySprintAndType(mapOfFilters,
					uniqueProjectMap);
			resultListMap.put(SPRINT_WISE_PREDICTABILITY, sprintWiseJiraList);
			resultListMap.put(SPRINT_WISE_SPRINT_DETAILS, new ArrayList<>());
		}
		// end: for azure board sprint details collections put is empty due to we did
		// not have required data of issues.

		return resultListMap;

	}

	/**
	 * Calculates KPI Metrics
	 *
	 * @param filterComponentIdWiseDefectMap
	 * @return Double
	 */
	@Override
	public Double calculateKPIMetrics(Map<String, Object> filterComponentIdWiseDefectMap) {
		return (double) Math.round(100.0);
	}

	/**
	 * Populates KPI value to sprint leaf nodes and gives the trend analysis at
	 * sprint wise.
	 *
	 * @param mapTmp
	 * @param kpiElement
	 * @param sprintLeafNodeList
	 * @param trendValueList
	 */
	@SuppressWarnings("unchecked")
	private void sprintWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> sprintLeafNodeList,
			List<DataCount> trendValueList, KpiElement kpiElement, KpiRequest kpiRequest) {

		String requestTrackerId = getRequestTrackerId();

		String startDate;
		String endDate;

		sprintLeafNodeList.sort((node1, node2) -> node1.getSprintFilter().getStartDate()
				.compareTo(node2.getSprintFilter().getStartDate()));

		startDate = sprintLeafNodeList.get(0).getSprintFilter().getStartDate();
		endDate = sprintLeafNodeList.get(sprintLeafNodeList.size() - 1).getSprintFilter().getEndDate();

		Map<String, Object> sprintWisePredictabilityMap = fetchKPIDataFromDb(sprintLeafNodeList, startDate, endDate,
				kpiRequest);

		List<SprintWiseStory> sprintWisePredictabilityList = new ArrayList<>();

		List<JiraIssue> sprintWiseJiraStoryList = (List<JiraIssue>) sprintWisePredictabilityMap
				.get(SPRINT_WISE_PREDICTABILITY);

		List<SprintDetails> sprintDetails = (List<SprintDetails>) sprintWisePredictabilityMap
				.get(SPRINT_WISE_SPRINT_DETAILS);

		Map<Pair<String, String>, Set<IssueDetails>> currentSprintLeafPredictabilityMap = new HashMap<>();
		Map<Pair<String, String>, List<JiraIssue>> sprintWiseIssues = new HashMap<>();

		if (CollectionUtils.isNotEmpty(sprintDetails)) {
			sprintDetails.forEach(sd -> {
				Set<IssueDetails> filterIssueDetailsSet = new HashSet<>();
				List<String> storyList = new ArrayList<>();
				AtomicDouble effectSumDouble = new AtomicDouble();
				if (CollectionUtils.isNotEmpty(sd.getCompletedIssues())) {
					sd.getCompletedIssues().stream()
							.forEach(sprintIssue -> sprintWiseJiraStoryList.stream().forEach(jiraIssue -> {
								if (sprintIssue.getNumber().equals(jiraIssue.getNumber())) {
									IssueDetails issueDetails = new IssueDetails();
									issueDetails.setSprintIssue(sprintIssue);
									issueDetails.setUrl(jiraIssue.getUrl());
									issueDetails.setDesc(jiraIssue.getName());
									storyList.add(sprintIssue.getNumber());
									effectSumDouble.addAndGet(Optional.ofNullable(sprintIssue.getStoryPoints())
											.orElse(0.0d).doubleValue());
									filterIssueDetailsSet.add(issueDetails);
								}
							}));
				}
				SprintWiseStory sprintWiseStory = new SprintWiseStory();
				sprintWiseStory.setSprint(sd.getSprintID());
				sprintWiseStory.setSprintName(sd.getSprintName());
				sprintWiseStory.setBasicProjectConfigId(sd.getBasicProjectConfigId().toString());
				sprintWiseStory.setStoryList(storyList);
				sprintWiseStory.setEffortSum(effectSumDouble.get());
				sprintWisePredictabilityList.add(sprintWiseStory);
				Pair<String, String> currentNodeIdentifier = Pair.of(sd.getBasicProjectConfigId().toString(),
						sd.getSprintID());
				currentSprintLeafPredictabilityMap.put(currentNodeIdentifier, filterIssueDetailsSet);
			});
		} else {
			// start : for azure board sprint details collections empty so that we have to
			// prepare data from jira issue
			Map<String, List<JiraIssue>> projectWiseJiraIssues = sprintWiseJiraStoryList.stream()
					.collect(Collectors.groupingBy(JiraIssue::getBasicProjectConfigId));
			projectWiseJiraIssues.forEach((basicProjectConfigId, projectWiseIssuesList) -> {
				Map<String, List<JiraIssue>> sprintWiseJiraIssues = projectWiseIssuesList.stream()
						.filter(jiraIssue -> Objects.nonNull(jiraIssue.getSprintID()))
						.collect(Collectors.groupingBy(JiraIssue::getSprintID));
				sprintWiseJiraIssues.forEach((sprintId, sprintWiseIssuesList) -> sprintWiseIssues
						.put(Pair.of(basicProjectConfigId, sprintId), sprintWiseIssuesList));
			});
			for (Map.Entry<Pair<String, String>, List<JiraIssue>> currentNodeIdentifier : sprintWiseIssues.entrySet()) {
				Set<IssueDetails> filterIssueDetailsSet = new HashSet<>();
				List<String> storyList = new ArrayList<>();
				AtomicDouble effectSumDouble = new AtomicDouble();
				currentNodeIdentifier.getValue().stream().forEach(jiraIssue -> {
					IssueDetails issueDetails = new IssueDetails();
					SprintIssue sprintIssue = new SprintIssue();
					sprintIssue.setNumber(jiraIssue.getNumber());
					sprintIssue.setStoryPoints(jiraIssue.getStoryPoints());
					issueDetails.setSprintIssue(sprintIssue);
					issueDetails.setUrl(jiraIssue.getUrl());
					issueDetails.setDesc(jiraIssue.getName());
					filterIssueDetailsSet.add(issueDetails);
					storyList.add(jiraIssue.getNumber());
					effectSumDouble
							.addAndGet(Optional.ofNullable(jiraIssue.getStoryPoints()).orElse(0.0d).doubleValue());
				});
				SprintWiseStory sprintWiseStory = new SprintWiseStory();
				sprintWiseStory.setSprint(currentNodeIdentifier.getKey().getValue());
				// sprintWiseStory.setSprintName(jiraIssue.getSprintName());
				sprintWiseStory.setBasicProjectConfigId(currentNodeIdentifier.getKey().getKey());
				sprintWiseStory.setStoryList(storyList);
				sprintWiseStory.setEffortSum(effectSumDouble.get());
				currentSprintLeafPredictabilityMap.put(currentNodeIdentifier.getKey(), filterIssueDetailsSet);
				sprintWisePredictabilityList.add(sprintWiseStory);
			}
		}

		Map<Pair<String, String>, Double> predictability = prepareSprintPredictMap(sprintWisePredictabilityList);
		List<KPIExcelData> excelData = new ArrayList<>();
		sprintLeafNodeList.forEach(node -> {
			String trendLineName = node.getProjectFilter().getName();
			String currentSprintComponentId = node.getSprintFilter().getId();

			Pair<String, String> currentNodeIdentifier = Pair
					.of(node.getProjectFilter().getBasicProjectConfigId().toString(), currentSprintComponentId);
			populateExcelDataObject(requestTrackerId , excelData, currentSprintLeafPredictabilityMap, node);
			log.debug("[SPRINTPREDICTABILITY-SPRINT-WISE][{}]. SPRINTPREDICTABILITY for sprint {}  is {}",
					requestTrackerId, node.getSprintFilter().getName(), currentNodeIdentifier);
			if (predictability.get(currentNodeIdentifier) != null) {
				DataCount dataCount = new DataCount();
				dataCount.setData(String.valueOf(Math.round(predictability.get(currentNodeIdentifier))));
				dataCount.setSProjectName(trendLineName);
				dataCount.setSSprintID(node.getSprintFilter().getId());
				dataCount.setSSprintName(node.getSprintFilter().getName());
				dataCount.setSprintIds(new ArrayList<>(Arrays.asList(node.getSprintFilter().getId())));
				dataCount.setSprintNames(new ArrayList<>(Arrays.asList(node.getSprintFilter().getName())));
				dataCount.setValue(predictability.get(currentNodeIdentifier));
				dataCount.setValue(Math.round(predictability.get(currentNodeIdentifier)));
				dataCount.setHoverValue(new HashMap<>());
				mapTmp.get(node.getId()).setValue(new ArrayList<>(Arrays.asList(dataCount)));
				trendValueList.add(dataCount);
			}
		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.SPRINT_PREDICTABILITY.getColumns());
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiName) {
		return calculateKpiValueForDouble(valueList, kpiName);
	}

	/**
	 * Prepares Sprint Predict Map
	 *
	 * @param stories
	 * @return resultMap
	 */
	public Map<Pair<String, String>, Double> prepareSprintPredictMap(List<SprintWiseStory> stories) {
		Map<Pair<String, String>, Double> resultMap = new LinkedHashMap<>();
		Map<String, List<SprintWiseStory>> projectWiseStories = stories.stream()
				.collect(Collectors.groupingBy(SprintWiseStory::getBasicProjectConfigId, Collectors.toList()));
		projectWiseStories.forEach((projectId, storyList) -> {
			int varCount = SP_CONSTANT;
			for (int count = 0; count < storyList.size(); count++) {
				if (count + SP_CONSTANT >= storyList.size()) {
					varCount = 1;
				}
				String projectKey = storyList.get(count).getBasicProjectConfigId();
				Pair<String, String> sprintKey = Pair.of(projectKey, storyList.get(count).getSprint());

				if ((count + varCount) < storyList.size()) {
					Double total = 0d;
					Double avg = calculateAverage(storyList, varCount, count, total);
					if (avg == 0) {
						calculateFirstSprintPredictability(storyList, resultMap, count, projectKey);
					} else {
						Double finalResult = (double) Math.round((storyList.get(count).getEffortSum() / avg) * 100);
						resultMap.put(sprintKey, finalResult);
					}
				} else {
					calculateFirstSprintPredictability(storyList, resultMap, count, projectKey);

				}

			}
		});
		return resultMap;
	}

	/**
	 * @param storyList
	 * @param varCount
	 * @param count
	 * @param total
	 * @return
	 */
	private Double calculateAverage(List<SprintWiseStory> storyList, int varCount, int count, Double total) {
		Double avgCount = 0d;
		for (int innerCount = count + 1; innerCount <= count + varCount; innerCount++) {
			total += storyList.get(innerCount).getEffortSum();
			avgCount++;
		}
		return getAvarage(total, avgCount);
	}

	/**
	 * Gets average
	 *
	 * @param total
	 * @param avgCount
	 * @return avgCount
	 */
	private double getAvarage(Double total, Double avgCount) {
		return avgCount == 0 ? 0 : total / avgCount;
	}

	/**
	 * @param projectKey
	 * @param resultMap
	 * @param storyList
	 * @param count
	 */
	private void calculateFirstSprintPredictability(List<SprintWiseStory> storyList,
			Map<Pair<String, String>, Double> resultMap, int count, String projectKey) {
		if (storyList.get(count).getEffortSum() == 0) {
			resultMap.put(Pair.of(projectKey, storyList.get(count).getSprint()), 0d);
		} else {
			Double finalResult = 100d;
			resultMap.put(Pair.of(projectKey, storyList.get(count).getSprint()), finalResult);
		}
	}

	/**
	 *
	 * @param requestTrackerId
	 * @param excelData
	 * @param currentSprintLeafVelocityMap
	 * @param node
	 */
	private void populateExcelDataObject(String requestTrackerId, List<KPIExcelData> excelData,
			Map<Pair<String, String>, Set<IssueDetails>> currentSprintLeafVelocityMap, Node node) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			Pair<String, String> currentNodeIdentifier = Pair
					.of(node.getProjectFilter().getBasicProjectConfigId().toString(), node.getSprintFilter().getId());

			if (MapUtils.isNotEmpty(currentSprintLeafVelocityMap)
					&& CollectionUtils.isNotEmpty(currentSprintLeafVelocityMap.get(currentNodeIdentifier))) {
				Set<IssueDetails> issueDetailsSet = currentSprintLeafVelocityMap.get(currentNodeIdentifier);
				KPIExcelUtility.populateSprintPredictability(node.getSprintFilter().getName(), issueDetailsSet,
						excelData);
			}
		}
	}
}
