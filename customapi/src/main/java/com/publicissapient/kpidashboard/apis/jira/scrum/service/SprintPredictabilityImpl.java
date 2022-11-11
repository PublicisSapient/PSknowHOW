package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
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
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ValidationData;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
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
	private static final String TOTAL_ISSUE_WITH_STORY_POINTS = "totalIssueWithStoryPoints";

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private SprintRepository sprintRepository;

	@Autowired
	private CustomApiConfig customApiConfig;

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private KpiHelperService kpiHelperService;

	@Autowired
	private FilterHelperService flterHelperService;

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
		Map<Pair<String, String>, Map<String, Double>> totalIssue = new HashMap<>();
		if (CollectionUtils.isNotEmpty(totalSprintDetails)) {
			List<SprintDetails> sprintDetails = totalSprintDetails.stream()
					.limit(Long.valueOf(customApiConfig.getSprintCountForFilters()) + SP_CONSTANT).collect(Collectors.toList());
			resultListMap.put(SPRINT_WISE_SPRINT_DETAILS, sprintDetails);
			sprintDetails.stream().forEach(sprintDetail -> {

				if (CollectionUtils.isNotEmpty(sprintDetail.getTotalIssues())) {

					List<String> closedStatus = closedStatusMap
							.getOrDefault(sprintDetail.getBasicProjectConfigId().toString(), new ArrayList<>());
					List<String> typeName = typeNameMap.getOrDefault(sprintDetail.getBasicProjectConfigId().toString(),
							new ArrayList<>());
					Map<String, Double> storyWiseStoryPoint = new HashMap<>();
					sprintDetail.getTotalIssues().stream()
							.filter(sprintIssue -> (closedStatus.contains(sprintIssue.getStatus())
									&& typeName.contains(sprintIssue.getTypeName())))
							.forEach(sprintIssue -> storyWiseStoryPoint.putIfAbsent(sprintIssue.getNumber(),
									sprintIssue.getStoryPoints()));

					totalIssue.put(Pair.of(sprintDetail.getBasicProjectConfigId().toString(), sprintDetail.getSprintID()),
							storyWiseStoryPoint);
				}

			});
		} else {
			mapOfFilters.put(JiraFeature.SPRINT_ID.getFieldValueInFeature(),
					sprintList.stream().distinct().collect(Collectors.toList()));
		}

		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));

		if (MapUtils.isNotEmpty(totalIssue)) {
			resultListMap.put(TOTAL_ISSUE_WITH_STORY_POINTS, totalIssue);
			resultListMap.put(SPRINT_WISE_PREDICTABILITY, new ArrayList<>());
		} else {
			// start: for azure board sprint details collections put is empty due to we did
			// not have required data of issues.
			List<SprintWiseStory> sprintWisePredictabilityList = jiraIssueRepository.findStoriesByType(mapOfFilters,
					uniqueProjectMap, kpiRequest.getFilterToShowOnTrend(), DEV);
			resultListMap.put(SPRINT_WISE_PREDICTABILITY, sprintWisePredictabilityList);
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

		List<SprintWiseStory> sprintWisePredictabilityList = (List<SprintWiseStory>) sprintWisePredictabilityMap
				.get(SPRINT_WISE_PREDICTABILITY);

		List<SprintDetails> sprintDetails = (List<SprintDetails>) sprintWisePredictabilityMap
				.get(SPRINT_WISE_SPRINT_DETAILS);

		Map<Pair<String, String>, Map<String, Double>> sprintWiseNumberStoryMap = (Map<Pair<String, String>, Map<String, Double>>) sprintWisePredictabilityMap
				.get(TOTAL_ISSUE_WITH_STORY_POINTS);

		if (CollectionUtils.isEmpty(sprintWisePredictabilityList) && CollectionUtils.isNotEmpty(sprintDetails)) {
			sprintDetails.forEach(sd -> {
				Map<String, Double> storyWiseStoryPoint = sprintWiseNumberStoryMap
						.get((Pair.of(sd.getBasicProjectConfigId().toString(), sd.getSprintID())));
				SprintWiseStory sprintWiseStory = new SprintWiseStory();
				sprintWiseStory.setSprint(sd.getSprintID());
				sprintWiseStory.setSprintName(sd.getSprintName());
				sprintWiseStory.setBasicProjectConfigId(sd.getBasicProjectConfigId().toString());
				if (MapUtils.isNotEmpty(storyWiseStoryPoint)) {
					double effectSum = 0.0d;
					for (Map.Entry<String, Double> map : storyWiseStoryPoint.entrySet()) {
						effectSum += Optional.ofNullable(map.getValue()).orElse(0.0d).doubleValue();
					}
					sprintWiseStory.setStoryList(new ArrayList<>(storyWiseStoryPoint.keySet()));
					sprintWiseStory.setEffortSum(effectSum);
				}
				sprintWisePredictabilityList.add(sprintWiseStory);
			});
		}

		Map<Pair<String, String>, List<String>> sprintWiseMap = sprintWisePredictabilityList.stream().collect(Collectors
				.toMap(x -> Pair.of(x.getBasicProjectConfigId(), x.getSprint()), SprintWiseStory::getStoryList));

		Map<Pair<String, String>, Double> predictability = prepareSprintPredictMap(sprintWisePredictabilityList);
		Map<String, ValidationData> validationDataMap = new HashMap<>();
		sprintLeafNodeList.forEach(node -> {
			String trendLineName = node.getProjectFilter().getName();
			String currentSprintComponentId = node.getSprintFilter().getId();
			String sprintName = node.getSprintFilter().getName();

			Pair<String, String> currentNodeIdentifier = Pair
					.of(node.getProjectFilter().getBasicProjectConfigId().toString(), currentSprintComponentId);
			Map<String, Double> storyWiseStoryPoint = sprintWiseNumberStoryMap.get(currentNodeIdentifier);
			populateValidationDataObject(kpiElement, requestTrackerId, validationDataMap,
					sprintWiseMap.get(currentNodeIdentifier), sprintName, storyWiseStoryPoint);
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
	 * Populates Validation Data Object
	 *
	 * @param kpiElement
	 * @param requestTrackerId
	 * @param sprintName
	 * @param validationDataMap
	 * @param storyList
	 * @param storyWiseStoryPoint
	 */
	private void populateValidationDataObject(KpiElement kpiElement, String requestTrackerId,
			Map<String, ValidationData> validationDataMap, List<String> storyList, String sprintName,
			Map<String, Double> storyWiseStoryPoint) {

		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {

			ValidationData validationData = new ValidationData();
			List<String> storyKeyList = new ArrayList<>();
			List<String> storyPointList = new ArrayList<>();
			if (MapUtils.isNotEmpty(storyWiseStoryPoint)) {
				for (Map.Entry<String, Double> storyWiseStory : storyWiseStoryPoint.entrySet()) {
					storyKeyList.add(storyWiseStory.getKey());
					storyPointList.add(Optional.ofNullable(storyWiseStory.getValue()).orElse(0.0).toString());
				}
				validationData.setStoryKeyList(storyKeyList);
				validationData.setStoryPointList(storyPointList);
			} else {
				validationData.setStoryKeyList(storyList);
			}

			validationDataMap.put(sprintName, validationData);

			kpiElement.setMapOfSprintAndData(validationDataMap);

		}
	}
}
