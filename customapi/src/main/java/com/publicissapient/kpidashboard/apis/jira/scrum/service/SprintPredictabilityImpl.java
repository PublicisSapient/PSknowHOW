package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.AtomicDouble;
import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.KpiDataCacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiDataProvider;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
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
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.IssueDetails;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintWiseStory;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepositoryCustom;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SprintPredictabilityImpl extends JiraKPIService<Long, List<Object>, Map<String, Object>> {

	private static final Integer SP_CONSTANT = 3;
	private static final String SPRINT_WISE_PREDICTABILITY = "predictability";
	private static final String SPRINT_WISE_SPRINT_DETAILS = "sprintWiseSprintDetailMap";

	private static final String HOVER_KEY_VELOCITY = "Velocity";
	private static final String HOVER_KEY_AVERAGE_VELOCITY = "Average Velocity";

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
	private CacheService cacheService;

	@Autowired
	private SprintRepository sprintRepository;
	@Autowired
	private SprintRepositoryCustom sprintRepositoryCustom;
	@Autowired
	private KpiDataCacheService kpiDataCacheService;
	@Autowired
	private KpiDataProvider kpiDataProvider;
	private List<String> sprintIdList = Collections.synchronizedList(new ArrayList<>());

	private static void setEstimation(FieldMapping fieldMapping, AtomicDouble effectSumDouble, SprintIssue sprintIssue,
			JiraIssue jiraIssue) {
		if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
				&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
			effectSumDouble.addAndGet(Optional.ofNullable(sprintIssue.getStoryPoints()).orElse(0.0d));
		} else if (null != jiraIssue.getAggregateTimeOriginalEstimateMinutes()) {
			Double totalOriginalEstimateInHours = (double) (jiraIssue.getAggregateTimeOriginalEstimateMinutes()) / 60;
			sprintIssue.setOriginalEstimate(Double.valueOf(jiraIssue.getAggregateTimeOriginalEstimateMinutes()));
			effectSumDouble.addAndGet(totalOriginalEstimateInHours / fieldMapping.getStoryPointToHourMapping());
		}
	}

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
		sprintIdList = treeAggregatorDetail.getMapOfListOfLeafNodes().get(CommonConstant.SPRINT_MASTER).stream()
				.map(node -> node.getSprintFilter().getId()).collect(Collectors.toList());
		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
			if (Filters.getFilter(k) == Filters.SPRINT) {
				sprintWiseLeafNodeValue(mapTmp, v, trendValueList, kpiElement, kpiRequest);
			}
		});

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValue(root, nodeWiseKPIValue, KPICode.SPRINT_PREDICTABILITY);
		List<DataCount> trendValues = getTrendValues(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.SPRINT_PREDICTABILITY);
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
		Map<ObjectId, List<String>> projectWiseSprints = new HashMap<>();

		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			String sprint = leaf.getSprintFilter().getId();
			projectWiseSprints.putIfAbsent(basicProjectConfigId, new ArrayList<>());
			projectWiseSprints.get(basicProjectConfigId).add(sprint);
		});

		List<SprintDetails> projectWiseSprintDetails = new ArrayList<>();
		List<JiraIssue> sprintWiseJiraList = new ArrayList<>();
		boolean fetchCachedData = flterHelperService.isFilterSelectedTillSprintLevel(kpiRequest.getLevel(), false);
		projectWiseSprints.forEach((basicProjectConfigId, sprintList) -> {
			Map<String, Object> result;
			if (fetchCachedData) { // fetch data from cache only if Filter is selected till Sprint
				// level.
				result = kpiDataCacheService.fetchSprintPredictabilityData(kpiRequest, basicProjectConfigId,
						sprintIdList, KPICode.SPRINT_PREDICTABILITY.getKpiId());
			} else { // fetch data from DB if filters below Sprint level (i.e. additional filters)
				result = kpiDataProvider.fetchSprintPredictabilityDataFromDb(kpiRequest, basicProjectConfigId,
						sprintList);
			}

			sprintWiseJiraList.addAll((List<JiraIssue>) result.get(SPRINT_WISE_PREDICTABILITY));
			projectWiseSprintDetails.addAll((List<SprintDetails>) result.get(SPRINT_WISE_SPRINT_DETAILS));
		});

		resultListMap.put(SPRINT_WISE_PREDICTABILITY, sprintWiseJiraList);
		resultListMap.put(SPRINT_WISE_SPRINT_DETAILS, projectWiseSprintDetails);

		return resultListMap;
	}

	/**
	 * Calculates KPI Metrics
	 *
	 * @param filterComponentIdWiseDefectMap
	 * @return Double
	 */
	@Override
	public Long calculateKPIMetrics(Map<String, Object> filterComponentIdWiseDefectMap) {
		return Math.round(100.0);
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

		FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
				.get(sprintLeafNodeList.get(0).getProjectFilter().getBasicProjectConfigId());
		Map<String, JiraIssue> jiraIssueMap = sprintWiseJiraStoryList.stream().collect(
				Collectors.toMap(JiraIssue::getNumber, Function.identity(), (existing, replacement) -> existing));

		if (CollectionUtils.isNotEmpty(sprintDetails)) {

			sprintDetails.forEach(sd -> {
				Set<IssueDetails> filterIssueDetailsSet = new HashSet<>();
				List<String> storyList = new ArrayList<>();
				AtomicDouble effectSumDouble = new AtomicDouble();
				if (CollectionUtils.isNotEmpty(sd.getCompletedIssues())) {
					sd.getCompletedIssues().stream().forEach(sprintIssue -> {
						JiraIssue jiraIssue = jiraIssueMap.get(sprintIssue.getNumber());
						if (jiraIssue != null) {
							IssueDetails issueDetails = new IssueDetails();
							issueDetails.setSprintIssue(sprintIssue);
							issueDetails.setUrl(jiraIssue.getUrl());
							issueDetails.setDesc(jiraIssue.getName());
							storyList.add(sprintIssue.getNumber());
							setEstimation(fieldMapping, effectSumDouble, sprintIssue, jiraIssue);
							filterIssueDetailsSet.add(issueDetails);
						}
					});
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
				}

			});
		}

		Map<Pair<String, String>, Map<String, Object>> sprintWiseHowerMap = new HashMap<>();
		Map<Pair<String, String>, Double> predictability = prepareSprintPredictMap(sprintWisePredictabilityList,
				sprintWiseHowerMap);
		List<KPIExcelData> excelData = new ArrayList<>();
		sprintLeafNodeList.forEach(node -> {
			String trendLineName = node.getProjectFilter().getName();
			String currentSprintComponentId = node.getSprintFilter().getId();

			Pair<String, String> currentNodeIdentifier = Pair
					.of(node.getProjectFilter().getBasicProjectConfigId().toString(), currentSprintComponentId);
			populateExcelDataObject(requestTrackerId, excelData, currentSprintLeafPredictabilityMap, node, fieldMapping,
					jiraIssueMap);
			log.debug("[SPRINTPREDICTABILITY-SPRINT-WISE][{}]. SPRINTPREDICTABILITY for sprint {}  is {}",
					requestTrackerId, node.getSprintFilter().getName(), currentNodeIdentifier);

			DataCount dataCount = new DataCount();
			createDataCountData(predictability, currentNodeIdentifier, dataCount, sprintWiseHowerMap);
			dataCount.setSProjectName(trendLineName);
			dataCount.setSSprintID(node.getSprintFilter().getId());
			dataCount.setSSprintName(node.getSprintFilter().getName());
			dataCount.setSprintIds(new ArrayList<>(Arrays.asList(node.getSprintFilter().getId())));
			dataCount.setSprintNames(new ArrayList<>(Arrays.asList(node.getSprintFilter().getName())));
			mapTmp.get(node.getId()).setValue(new ArrayList<>(Arrays.asList(dataCount)));
			trendValueList.add(dataCount);

		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(
				KPIExcelColumn.SPRINT_PREDICTABILITY.getColumns(sprintLeafNodeList, cacheService, flterHelperService));
	}

	private static void createDataCountData(Map<Pair<String, String>, Double> predictability,
			Pair<String, String> currentNodeIdentifier, DataCount dataCount,
			Map<Pair<String, String>, Map<String, Object>> sprintWiseHowerMap) {
		if (predictability.get(currentNodeIdentifier) != null) {
			dataCount.setData(String.valueOf(Math.round(predictability.get(currentNodeIdentifier))));
			dataCount.setValue(Math.round(predictability.get(currentNodeIdentifier)));
			dataCount.setHoverValue(sprintWiseHowerMap.get(currentNodeIdentifier));
		}
	}

	@Override
	public Long calculateKpiValue(List<Long> valueList, String kpiName) {
		return calculateKpiValueForLong(valueList, kpiName);
	}

	/**
	 * Prepares Sprint Predict Map
	 *
	 * @param stories
	 * @return resultMap
	 */
	public Map<Pair<String, String>, Double> prepareSprintPredictMap(List<SprintWiseStory> stories,
			Map<Pair<String, String>, Map<String, Object>> sprintWiseHowerMap) {
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

				if ((count + varCount) < storyList.size()) { // 3
					Double total = 0d;
					Double avg = calculateAverage(storyList, varCount, count, total);
					if (avg == 0) {
						calculateFirstSprintPredictability(storyList, resultMap, count, projectKey, sprintWiseHowerMap);
					} else {
						Double finalResult = (double) Math.round((storyList.get(count).getEffortSum() / avg) * 100);
						resultMap.put(sprintKey, finalResult);
						setHoverValue(sprintWiseHowerMap, sprintKey, storyList.get(count).getEffortSum(), avg);
					}
				} else {
					calculateFirstSprintPredictability(storyList, resultMap, count, projectKey, sprintWiseHowerMap);
				}
			}
		});
		return resultMap;
	}

	/**
	 * set hover value
	 *
	 * @param sprintWiseHowerMap
	 * @param sprintKey
	 * @param velocity
	 * @param avgVelocity
	 */
	private void setHoverValue(Map<Pair<String, String>, Map<String, Object>> sprintWiseHowerMap,
			Pair<String, String> sprintKey, Double velocity, Double avgVelocity) {
		Map<String, Object> hoverValue = new HashMap<>();
		hoverValue.put(HOVER_KEY_VELOCITY, velocity);
		hoverValue.put(HOVER_KEY_AVERAGE_VELOCITY, roundingOff(avgVelocity));
		sprintWiseHowerMap.put(sprintKey, hoverValue);
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
			Map<Pair<String, String>, Double> resultMap, int count, String projectKey,
			Map<Pair<String, String>, Map<String, Object>> sprintWiseHowerMap) {
		if (storyList.get(count).getEffortSum() == 0) {
			resultMap.put(Pair.of(projectKey, storyList.get(count).getSprint()), 0d);
			setHoverValue(sprintWiseHowerMap, Pair.of(projectKey, storyList.get(count).getSprint()), 0d, 0d);
		} else {
			Double finalResult = 100d;
			resultMap.put(Pair.of(projectKey, storyList.get(count).getSprint()), finalResult);
			setHoverValue(sprintWiseHowerMap, Pair.of(projectKey, storyList.get(count).getSprint()), 100d, 100d);
		}
	}

	/**
	 * @param requestTrackerId
	 * @param excelData
	 * @param currentSprintLeafVelocityMap
	 * @param node
	 * @param jiraIssueMap
	 */
	private void populateExcelDataObject(String requestTrackerId, List<KPIExcelData> excelData,
			Map<Pair<String, String>, Set<IssueDetails>> currentSprintLeafVelocityMap, Node node,
			FieldMapping fieldMapping, Map<String, JiraIssue> jiraIssueMap) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			Pair<String, String> currentNodeIdentifier = Pair
					.of(node.getProjectFilter().getBasicProjectConfigId().toString(), node.getSprintFilter().getId());

			if (MapUtils.isNotEmpty(currentSprintLeafVelocityMap)
					&& CollectionUtils.isNotEmpty(currentSprintLeafVelocityMap.get(currentNodeIdentifier))) {
				Set<IssueDetails> issueDetailsSet = currentSprintLeafVelocityMap.get(currentNodeIdentifier);
				KPIExcelUtility.populateSprintPredictability(node.getSprintFilter().getName(), issueDetailsSet,
						excelData, fieldMapping, jiraIssueMap, customApiConfig);
			}
		}
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping) {
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI5(), KPICode.SPRINT_PREDICTABILITY.getKpiId());
	}
}
