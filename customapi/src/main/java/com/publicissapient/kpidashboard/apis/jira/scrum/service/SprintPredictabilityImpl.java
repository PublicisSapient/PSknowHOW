package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.time.LocalDateTime;
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

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
	@Autowired
	private SprintRepositoryCustom sprintRepositoryCustom;

	private static void setEstimation(FieldMapping fieldMapping, AtomicDouble effectSumDouble, SprintIssue sprintIssue,
			JiraIssue jiraIssue) {
		if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
				&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
			effectSumDouble.addAndGet(Optional.ofNullable(sprintIssue.getStoryPoints()).orElse(0.0d));
		} else if (null != jiraIssue.getAggregateTimeOriginalEstimateMinutes()) {
			Double totalOriginalEstimateInHours = (double) (jiraIssue.getAggregateTimeOriginalEstimateMinutes()) / 60;
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

		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
			if (Filters.getFilter(k) == Filters.SPRINT) {
				sprintWiseLeafNodeValue(mapTmp, v, trendValueList, kpiElement, kpiRequest);
			}
		});

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValue(root, nodeWiseKPIValue, KPICode.SPRINT_PREDICTABILITY);
		List<DataCount> trendValues = getTrendValues(kpiRequest, kpiElement, nodeWiseKPIValue, KPICode.SPRINT_PREDICTABILITY);
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
		List<String> sprintStatusList = new ArrayList<>();

		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();

			sprintList.add(leaf.getSprintFilter().getId());
			basicProjectConfigIds.add(basicProjectConfigId.toString());
			basicProjectConfigObjectIds.add(basicProjectConfigId);

		});
		sprintStatusList.add(SprintDetails.SPRINT_STATE_CLOSED);
		sprintStatusList.add(SprintDetails.SPRINT_STATE_CLOSED.toLowerCase());

		List<SprintDetails> totalSprintDetails = sprintRepositoryCustom
				.findByBasicProjectConfigIdInAndStateInOrderByStartDateDesc(basicProjectConfigObjectIds,
						sprintStatusList, Long.valueOf(customApiConfig.getSprintCountForFilters()) + SP_CONSTANT);

		List<String> totalIssueIds = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(totalSprintDetails)) {

			Map<ObjectId, List<SprintDetails>> projectWiseTotalSprintDetails = totalSprintDetails.stream()
					.collect(Collectors.groupingBy(SprintDetails::getBasicProjectConfigId));

			Map<ObjectId, Set<String>> duplicateIssues = kpiHelperService
					.getProjectWiseTotalSprintDetail(projectWiseTotalSprintDetails);
			Map<ObjectId, Map<String, List<LocalDateTime>>> projectWiseDuplicateIssuesWithMinCloseDate = null;
			Map<ObjectId, FieldMapping> fieldMappingMap = configHelperService.getFieldMappingMap();

			if (MapUtils.isNotEmpty(fieldMappingMap) && !duplicateIssues.isEmpty()) {
				Map<ObjectId, List<String>> customFieldMapping = duplicateIssues.keySet().stream()
						.filter(fieldMappingMap::containsKey).collect(Collectors.toMap(Function.identity(), key -> {
							FieldMapping fieldMapping = fieldMappingMap.get(key);
							return Optional.ofNullable(fieldMapping)
									.map(FieldMapping::getJiraIterationCompletionStatusKpi5)
									.orElse(Collections.emptyList());
						}));
				projectWiseDuplicateIssuesWithMinCloseDate = kpiHelperService
						.getMinimumClosedDateFromConfiguration(duplicateIssues, customFieldMapping);
			}

			Map<ObjectId, Map<String, List<LocalDateTime>>> finalProjectWiseDuplicateIssuesWithMinCloseDate = projectWiseDuplicateIssuesWithMinCloseDate;

			List<SprintDetails> projectWiseSprintDetails = new ArrayList<>();
			projectWiseTotalSprintDetails.forEach((basicProjectConfigId, sprintDetailsList) -> {
				List<SprintDetails> sprintDetails = sprintDetailsList.stream()
						.limit(Long.valueOf(customApiConfig.getSprintCountForFilters()) + SP_CONSTANT)
						.collect(Collectors.toList());
				sprintDetails.stream().forEach(dbSprintDetail -> {
					FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
							.get(dbSprintDetail.getBasicProjectConfigId());
					// to modify sprintdetails on the basis of configuration for the project
					SprintDetails sprintDetail = KpiDataHelper.processSprintBasedOnFieldMappings(dbSprintDetail,
							fieldMapping.getJiraIterationIssuetypeKpi5(),
							fieldMapping.getJiraIterationCompletionStatusKpi5(),
							finalProjectWiseDuplicateIssuesWithMinCloseDate);
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
		}
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

		FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
				.get(sprintLeafNodeList.get(0).getProjectFilter().getBasicProjectConfigId());

		if (CollectionUtils.isNotEmpty(sprintDetails)) {

			Map<String, JiraIssue> jiraIssueMap = sprintWiseJiraStoryList.stream().collect(
					Collectors.toMap(JiraIssue::getNumber, Function.identity(), (existing, replacement) -> existing));

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
		}

		Map<Pair<String, String>, Double> predictability = prepareSprintPredictMap(sprintWisePredictabilityList);
		List<KPIExcelData> excelData = new ArrayList<>();
		sprintLeafNodeList.forEach(node -> {
			String trendLineName = node.getProjectFilter().getName();
			String currentSprintComponentId = node.getSprintFilter().getId();

			Pair<String, String> currentNodeIdentifier = Pair
					.of(node.getProjectFilter().getBasicProjectConfigId().toString(), currentSprintComponentId);
			populateExcelDataObject(requestTrackerId, excelData, currentSprintLeafPredictabilityMap, node,
					fieldMapping);
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
			Map<Pair<String, String>, Set<IssueDetails>> currentSprintLeafVelocityMap, Node node,
			FieldMapping fieldMapping) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			Pair<String, String> currentNodeIdentifier = Pair
					.of(node.getProjectFilter().getBasicProjectConfigId().toString(), node.getSprintFilter().getId());

			if (MapUtils.isNotEmpty(currentSprintLeafVelocityMap)
					&& CollectionUtils.isNotEmpty(currentSprintLeafVelocityMap.get(currentNodeIdentifier))) {
				Set<IssueDetails> issueDetailsSet = currentSprintLeafVelocityMap.get(currentNodeIdentifier);
				KPIExcelUtility.populateSprintPredictability(node.getSprintFilter().getName(), issueDetailsSet,
						excelData, fieldMapping);
			}
		}
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping) {
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI5(), KPICode.SPRINT_PREDICTABILITY.getKpiId());
	}

}
