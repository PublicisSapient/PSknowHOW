package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ResolutionTimeValidation;
import com.publicissapient.kpidashboard.common.model.application.ValidationData;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintWiseStory;

import lombok.extern.slf4j.Slf4j;

/**
 * template for DIR (i.e simple line chart kpi) you can reference DirServiceImpl
 * for line chart Impl AverageResolutionTimeServiceImpl for line chart with
 * filter Impl UnitCoverageServiceImpl for week wise data in line chart
 * SonarViolationServiceImpl for week wise data group column chart with filter
 * Impl.
 *
 */
@Component
@Slf4j
public class ScrumTemplateImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {
	private static final String STORY_DATA = "storyData";
	private static final String DEFECT_DATA = "defectData";
	private static final String STORY = "Stories";
	private static final String DEFECT = "Defects";
	private static final String SUBGROUPCATEGORY = "subGroupCategory";
	private static final String AGGREGATED = "Overall";
	private static final String STORY_HISTORY_DATA = "storyHistoryData";
	private static final String PROJECT_FIELDMAPPING = "projectFieldMapping";

	@Autowired
	private KpiHelperService kpiHelperService;

	@Autowired
	private CustomApiConfig customApiConfig;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getQualifierType() {
		// KPI Name from KPICODE
		return "ScrumTemplateKPI";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();

		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {

			if (Filters.getFilter(k) == Filters.SPRINT) {
				sprintWiseLeafNodeValueForSimpleLineChart(mapTmp, v, kpiElement, kpiRequest);

				sprintWiseLeafNodeValueForLineChartWithFilterOrGroupStackChartWithFilter(mapTmp, v, kpiElement,
						kpiRequest);
			}

		});

		log.debug("[DIR-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);
		// simple line chart aggregation
		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValue(root, nodeWiseKPIValue, KPICode.FIRST_TIME_PASS_RATE);
		List<DataCount> trendValues = getTrendValues(kpiRequest, kpiElement, nodeWiseKPIValue, KPICode.FIRST_TIME_PASS_RATE);
		kpiElement.setTrendValueList(trendValues);
		// end of simple line chart aggregation

		// Use these methods instead of above if kpi is line+filter, groupcolumn,
		// grouped column+filter,column,column + filter
		calculateAggregatedValueMap(root, nodeWiseKPIValue, KPICode.AVERAGE_RESOLUTION_TIME);

		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.AVERAGE_RESOLUTION_TIME);
		Map<String, Map<String, List<DataCount>>> issueTypeProjectWiseDc = new LinkedHashMap<>();
		trendValuesMap.forEach((issueType, dataCounts) -> {
			Map<String, List<DataCount>> projectWiseDc = dataCounts.stream()
					.collect(Collectors.groupingBy(DataCount::getData));
			issueTypeProjectWiseDc.put(issueType, projectWiseDc);
		});

		List<DataCountGroup> dataCountGroups = new ArrayList<>();
		issueTypeProjectWiseDc.forEach((issueType, projectWiseDc) -> {
			DataCountGroup dataCountGroup = new DataCountGroup();
			List<DataCount> dataList = new ArrayList<>();
			projectWiseDc.entrySet().stream().forEach(trend -> dataList.addAll(trend.getValue()));
			dataCountGroup.setFilter(issueType);
			dataCountGroup.setValue(dataList);
			dataCountGroups.add(dataCountGroup);
		});
		kpiElement.setTrendValueList(dataCountGroups);
		// end of map aggregation

		return kpiElement;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {

		long startTime = System.currentTimeMillis();

		Map<String, Object> resultListMap = kpiHelperService.fetchDIRDataFromDb(leafNodeList, kpiRequest);

		if (log.isDebugEnabled()) {
			List<SprintWiseStory> storyDataList = (List<SprintWiseStory>) resultListMap.get(STORY_DATA);
			List<JiraIssue> defectDataList = (List<JiraIssue>) resultListMap.get(DEFECT_DATA);
			long processTime = System.currentTimeMillis() - startTime;
			log.info("[DIR-DB-QUERY][]. storyData count: {} defectData count: {}  time: {}", storyDataList.size(),
					defectDataList.size(), processTime);
		}

		return resultListMap;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Double calculateKPIMetrics(Map<String, Object> filterComponentIdWiseFCHMap) {
		return null;
	}

	/**
	 * This method populates KPI value to sprint leaf nodes. It also gives the trend
	 * analysis at sprint wise.
	 * 
	 * @param mapTmp
	 *            node is map
	 * @param sprintLeafNodeList
	 *            sprint nodes list
	 * @param kpiElement
	 *            KpiElement
	 * @param kpiRequest
	 *            KpiRequest
	 */
	@SuppressWarnings("unchecked")
	private void sprintWiseLeafNodeValueForSimpleLineChart(Map<String, Node> mapTmp, List<Node> sprintLeafNodeList,
			KpiElement kpiElement, KpiRequest kpiRequest) {

		String requestTrackerId = getRequestTrackerId();
		sprintLeafNodeList.sort((node1, node2) -> node1.getSprintFilter().getStartDate()
				.compareTo(node2.getSprintFilter().getStartDate()));
		String startDate = sprintLeafNodeList.get(0).getSprintFilter().getStartDate();
		String endDate = sprintLeafNodeList.get(sprintLeafNodeList.size() - 1).getSprintFilter().getEndDate();

		// db call to make kpi specific queries
		Map<String, Object> storyDefectDataListMap = fetchKPIDataFromDb(sprintLeafNodeList, startDate, endDate,
				kpiRequest);
		String subGroupCategory = (String) storyDefectDataListMap.get(SUBGROUPCATEGORY);

		// grouping data to ease up operations ahead
		List<SprintWiseStory> sprintWiseStoryList = (List<SprintWiseStory>) storyDefectDataListMap.get(STORY_DATA);

		/** Additional Filter **/
		Map<Pair<String, String>, Map<String, List<String>>> sprintWiseMap = KpiDataHelper
				.createSubCategoryWiseMap(subGroupCategory, sprintWiseStoryList, kpiRequest.getFilterToShowOnTrend());
		Map<String, String> sprintIdSprintNameMap = sprintWiseStoryList.stream().collect(
				Collectors.toMap(SprintWiseStory::getSprint, SprintWiseStory::getSprintName, (name1, name2) -> name1));

		Map<Pair<String, String>, Double> sprintWiseDIRMap = new HashMap<>();
		Map<String, ValidationData> validationDataMap = new HashMap<>();
		Map<Pair<String, String>, Map<String, Object>> sprintWiseHowerMap = new HashMap<>();

		// transforming data coming from db and calculating Kpi information for each
		// sprint
		sprintWiseMap.forEach((sprint, subCategoryMap) -> {
			List<JiraIssue> sprintWiseDefectList = new ArrayList<>();
			List<Double> addFilterDirList = new ArrayList<>();
			List<String> totalStoryIdList = new ArrayList<>();
			subCategoryMap.forEach((subCategory, storyIdList) -> {
				List<JiraIssue> additionalFilterDefectList = ((List<JiraIssue>) storyDefectDataListMap.get(DEFECT_DATA))
						.stream()
						.filter(f -> sprint.getKey().equals(f.getProjectID())
								&& CollectionUtils.containsAny(f.getDefectStoryID(), storyIdList))
						.collect(Collectors.toList());

				double dirForCurrentLeaf = 0.0d;
				if (CollectionUtils.isNotEmpty(additionalFilterDefectList) && CollectionUtils.isNotEmpty(storyIdList)) {
					dirForCurrentLeaf = ((double) additionalFilterDefectList.size() / storyIdList.size()) * 100;
				}
				addFilterDirList.add(dirForCurrentLeaf);
				sprintWiseDefectList.addAll(additionalFilterDefectList);
				totalStoryIdList.addAll(storyIdList);
			});

			String validationDataKey = sprintIdSprintNameMap.get(sprint.getValue());
			// this will be written according to what u want to be present in excel
			populateValidationDataObject(kpiElement, requestTrackerId, validationDataKey, validationDataMap,
					totalStoryIdList, sprintWiseDefectList);
			double sprintWiseDir = calculateKpiValue(addFilterDirList, KPICode.FIRST_TIME_PASS_RATE.getKpiId());
			sprintWiseDIRMap.put(sprint, sprintWiseDir);
			setHowerMap(sprintWiseHowerMap, sprint, totalStoryIdList, sprintWiseDefectList);
		});

		sprintLeafNodeList.forEach(node -> {

			String trendLineName = node.getProjectFilter().getName();
			String currentSprintComponentId = node.getSprintFilter().getId();
			Pair<String, String> currentNodeIdentifier = Pair.of(node.getParentId(), currentSprintComponentId);

			// set the already calculated data into data count object and set into their
			// aggregation node
			double defectInjectionRateForCurrentLeaf;

			if (sprintWiseDIRMap.containsKey(currentNodeIdentifier)) {
				defectInjectionRateForCurrentLeaf = sprintWiseDIRMap.get(currentNodeIdentifier);
			} else {
				defectInjectionRateForCurrentLeaf = 0.0d;
			}

			log.debug("[DIR-SPRINT-WISE][{}]. DIR for sprint {}  is {}", requestTrackerId,
					node.getSprintFilter().getName(), defectInjectionRateForCurrentLeaf);

			DataCount dataCount = new DataCount();
			dataCount.setData(String.valueOf(Math.round(defectInjectionRateForCurrentLeaf)));
			dataCount.setSProjectName(trendLineName);
			dataCount.setSSprintID(node.getSprintFilter().getId());
			dataCount.setSSprintName(node.getSprintFilter().getName());
			dataCount.setSprintIds(new ArrayList<>(Arrays.asList(node.getSprintFilter().getId())));
			dataCount.setSprintNames(new ArrayList<>(Arrays.asList(node.getSprintFilter().getName())));
			dataCount.setValue(defectInjectionRateForCurrentLeaf);
			dataCount.setHoverValue(sprintWiseHowerMap.get(currentNodeIdentifier));
			mapTmp.get(node.getId()).setValue(new ArrayList<>(Arrays.asList(dataCount)));

		});
	}

	private void sprintWiseLeafNodeValueForLineChartWithFilterOrGroupStackChartWithFilter(Map<String, Node> mapTmp,
			List<Node> sprintLeafNodeList, KpiElement kpiElement, KpiRequest kpiRequest) {

		String requestTrackerId = getRequestTrackerId();
		sprintLeafNodeList.sort((node1, node2) -> node1.getSprintFilter().getStartDate()
				.compareTo(node2.getSprintFilter().getStartDate()));

		String startDate = sprintLeafNodeList.get(0).getSprintFilter().getStartDate();
		String endDate = sprintLeafNodeList.get(sprintLeafNodeList.size() - 1).getSprintFilter().getEndDate();

		// db call to fetch data
		Map<String, Object> resultMap = fetchKPIDataFromDb(sprintLeafNodeList, startDate, endDate, kpiRequest);

		// grouping and fetching all data to ease up operation ahead
		// Project wise Field Mapping Map
		Map<String, FieldMapping> fieldMappingMap = (Map<String, FieldMapping>) resultMap.get(PROJECT_FIELDMAPPING);
		// History Data
		List<JiraIssueCustomHistory> storiesHistory = (List<JiraIssueCustomHistory>) resultMap.get(STORY_HISTORY_DATA);

		log.info("[DIR-DB-QUERY][]. storyData count: {}", storiesHistory.size());
		Map<String, ValidationData> validationDataMap = new HashMap<>();
		Map<String, List<ResolutionTimeValidation>> sprintWiseResolution = new HashMap<>();

		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			kpiElement.setMapOfSprintAndData(validationDataMap);
		}

		Map<String, Map<String, Double>> sprintIssueTypeWiseTime = new HashMap<>();

		// transforming data
		sprintWiseResolution.forEach((sprint, issueWiseTimeList) -> {
			Map<String, Double> issueTypeAvgTime = new HashMap<>();
			// based on kpi
			Map<String, List<ResolutionTimeValidation>> issueWiseTime = issueWiseTimeList.stream()
					.collect(Collectors.groupingBy(ResolutionTimeValidation::getIssueType));
			List<Double> sprintTime = issueWiseTimeList.stream()
					.collect(Collectors.mapping(ResolutionTimeValidation::getResolutionTime, Collectors.toList()));
			issueWiseTime.forEach((issueType, timeList) -> {
				List<Double> sprintIssueTypeTime = timeList.stream()
						.collect(Collectors.mapping(ResolutionTimeValidation::getResolutionTime, Collectors.toList()));
				if (CollectionUtils.isNotEmpty(sprintTime)) {
					Double avgTime = sprintIssueTypeTime.stream().mapToDouble(a -> a).average().orElse(0.0);
					issueTypeAvgTime.put(issueType, avgTime);
				}
			});
			// this will be written according to what u want to be present in excel
			populateValidationDataObject(kpiElement, requestTrackerId, "sprint name", validationDataMap,
					new ArrayList<>(), new ArrayList<>());
			Double aggregateAvgTime = sprintTime.stream().mapToDouble(a -> a).average().orElse(0.0);
			issueTypeAvgTime.put(AGGREGATED, aggregateAvgTime);
			sprintIssueTypeWiseTime.put(sprint, issueTypeAvgTime);
		});

		// this method will set a map of list of data count in aggregation tree
		sprintLeafNodeList.forEach(node -> {
			String trendLineName = node.getProjectFilter().getName();
			String basicProjectConfigId = node.getProjectFilter().getBasicProjectConfigId().toString();
			Set<String> issueTypes = new HashSet<>();
			// based on kpi
			FieldMapping fieldMapping = fieldMappingMap.get(basicProjectConfigId);
			if (null != fieldMapping && null != fieldMapping.getJiraIssueTypeNames()) {
				issueTypes = Arrays.stream(fieldMapping.getJiraIssueTypeNames()).collect(Collectors.toSet());
				if (CollectionUtils.containsAny(issueTypes, fieldMapping.getJiradefecttype())) {
					issueTypes.removeIf(x -> fieldMapping.getJiradefecttype().contains(x));
					issueTypes.add(NormalizedJira.DEFECT_TYPE.getValue());
				}
				issueTypes.add(AGGREGATED);

			}
			Set<String> absentIssueTypesRoot = new HashSet<>();
			String currentSprintComponentId = node.getSprintFilter().getId();
			Map<String, Double> issueTypeAvgTime = new HashMap<>();
			if (sprintIssueTypeWiseTime.containsKey(currentSprintComponentId)) {
				issueTypeAvgTime = sprintIssueTypeWiseTime.get(currentSprintComponentId);
			}
			Set<String> issueTypesFound = issueTypeAvgTime.keySet();
			issueTypes.removeAll(issueTypesFound);
			absentIssueTypesRoot.addAll(issueTypes);
			Map<String, Double> absentIssueTypes = issueTypes.stream().distinct()
					.collect(Collectors.toMap(s -> s, s -> 0.0));
			// add issue types
			Map<String, Double> allIssueTypes = new HashMap<>();
			allIssueTypes.putAll(issueTypeAvgTime);
			allIssueTypes.putAll(absentIssueTypes);

			Map<String, List<DataCount>> dataCountMap = new HashMap<>();
			allIssueTypes.forEach((issueType, avgTime) -> {
				DataCount dataCount = new DataCount();
				dataCount.setData(String.valueOf(Math.round(avgTime)));
				dataCount.setSProjectName(trendLineName);
				dataCount.setSSprintID(node.getSprintFilter().getId());
				dataCount.setSSprintName(node.getSprintFilter().getName());
				dataCount.setValue(avgTime);
				dataCount.setKpiGroup(issueType);
				dataCount.setHoverValue(new HashMap<>());
				dataCountMap.put(issueType, new ArrayList<>(Arrays.asList(dataCount)));
			});
			mapTmp.get(node.getId()).setValue(dataCountMap);
		});
	}

	/**
	 * This method sets the defect and story count for each leaf node to show data
	 * on trend line on mouse hover.
	 * 
	 * @param sprintWiseHowerMap
	 *            map of sprint key and hover value
	 * @param sprint
	 *            key to identify sprint
	 * @param storyIdList
	 *            story id list
	 * @param sprintWiseDefectList
	 *            defects linked to story
	 */
	private void setHowerMap(Map<Pair<String, String>, Map<String, Object>> sprintWiseHowerMap,
			Pair<String, String> sprint, List<String> storyIdList, List<JiraIssue> sprintWiseDefectList) {
		Map<String, Object> howerMap = new LinkedHashMap<>();
		if (CollectionUtils.isNotEmpty(sprintWiseDefectList)) {
			howerMap.put(DEFECT, sprintWiseDefectList.size());
		} else {
			howerMap.put(DEFECT, 0);
		}
		if (CollectionUtils.isNotEmpty(storyIdList)) {
			howerMap.put(STORY, storyIdList.size());
		} else {
			howerMap.put(STORY, 0);
		}
		sprintWiseHowerMap.put(sprint, howerMap);
	}

	/**
	 * This method populates KPI Element with Validation data. It will be triggered
	 * only for request originated to get Excel data.
	 * 
	 * @param kpiElement
	 *            KpiElement
	 * @param requestTrackerId
	 *            request id
	 * @param validationDataKey
	 *            validation data key
	 * @param validationDataMap
	 *            validation data map
	 * @param storyIdList
	 *            story id list
	 * @param sprintWiseDefectList
	 *            sprints defect list
	 */
	private void populateValidationDataObject(KpiElement kpiElement, String requestTrackerId, String validationDataKey,
			Map<String, ValidationData> validationDataMap, List<String> storyIdList,
			List<JiraIssue> sprintWiseDefectList) {

		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			ValidationData validationData = new ValidationData();
			validationData.setStoryKeyList(storyIdList);
			validationData.setDefectKeyList(
					sprintWiseDefectList.stream().map(JiraIssue::getNumber).collect(Collectors.toList()));
			validationDataMap.put(validationDataKey, validationData);
			kpiElement.setMapOfSprintAndData(validationDataMap);
		}
	}
}
