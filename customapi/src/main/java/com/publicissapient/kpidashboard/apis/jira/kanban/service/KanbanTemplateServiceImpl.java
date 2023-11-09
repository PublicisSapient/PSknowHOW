package com.publicissapient.kpidashboard.apis.jira.kanban.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.apis.enums.JiraFeatureHistory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.CustomDateRange;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ValidationData;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KanbanTemplateServiceImpl extends JiraKPIService<Long, List<Object>, Map<String, Object>> {

	private static final String TICKET_LIST = "tickets";
	private static final String SUBGROUPCATEGORY = "subGroupCategory";
	private static final String RANGE = "range";
	private static final String PROJECT_WISE_ISSUETYPES = "projectWiseIssueTypes";
	private static final String DEV = "DeveloperKpi";
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
	private static final String PROJECT = "PROJECT";
	@Autowired
	private KanbanJiraIssueRepository kanbanJiraIssueRepository;
	@Autowired
	private ConfigHelperService configHelperService;
	@Autowired
	private FilterHelperService flterHelperService;

	public static List<KanbanJiraIssue> filterKanbanDataBasedOnStartAndEndDate(List<KanbanJiraIssue> issueList,
			LocalDate startDate, LocalDate endDate) {
		Predicate<KanbanJiraIssue> predicate = issue -> LocalDateTime
				.parse(issue.getCreatedDate().split("\\.")[0], DATE_TIME_FORMATTER).isAfter(startDate.atTime(0, 0, 0))
				&& LocalDateTime.parse(issue.getCreatedDate().split("\\.")[0], DATE_TIME_FORMATTER)
						.isBefore(endDate.atTime(23, 59, 59));
		return issueList.stream().filter(predicate).collect(Collectors.toList());
	}

	public static Map<String, Long> filterKanbanDataBasedOnStartAndEndDateAndIssueType(List<KanbanJiraIssue> issueList,
			List<String> issueTypeList, LocalDate startDate, LocalDate endDate) {
		Predicate<KanbanJiraIssue> predicate = issue -> LocalDateTime
				.parse(issue.getCreatedDate().split("\\.")[0], DATE_TIME_FORMATTER).isAfter(startDate.atTime(0, 0, 0))
				&& LocalDateTime.parse(issue.getCreatedDate().split("\\.")[0], DATE_TIME_FORMATTER)
						.isBefore(endDate.atTime(23, 59, 59));
		List<KanbanJiraIssue> filteredIssue = issueList.stream().filter(predicate).collect(Collectors.toList());
		Map<String, Long> projectIssueTypeMap = filteredIssue.stream().map(KanbanJiraIssue::getTypeName)
				.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

		// adding missing issue type for this date
		issueTypeList.forEach(issueType -> projectIssueTypeMap.computeIfAbsent(issueType, val -> 0L));
		return projectIssueTypeMap;
	}

	/**
	 * Gets Qualifier Type
	 *
	 * @return KPICode's <tt>STORY_OPEN_RATE_BY_ISSUE_TYPE</tt> enum
	 */
	@Override
	public String getQualifierType() {
		// KPI Name from KPICODE
		return "KanbanTemplateKPI";
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

		log.info("kpiname", kpiRequest.getRequestTrackerId());
		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
		List<Node> projectList = treeAggregatorDetail.getMapOfListOfProjectNodes().get(PROJECT);

		// This method will contain the main logic to fetch data from db and set it in
		// aggregation tree
		dateWiseLeafNodeValue(mapTmp, projectList, kpiElement, kpiRequest);

		// choose one aggregation method from below
		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		// for simple chart
		calculateAggregatedValue(root, nodeWiseKPIValue, KPICode.TICKET_OPEN_VS_CLOSED_RATE_BY_TYPE);
		List<DataCount> trendValues = getTrendValues(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.TICKET_OPEN_VS_CLOSED_RATE_BY_TYPE);
		kpiElement.setTrendValueList(trendValues);
		// simple aggregation implementation over

		// for chart with filter,group stack and column chart
		calculateAggregatedValueMap(root, nodeWiseKPIValue, KPICode.TICKET_OPEN_VS_CLOSED_RATE_BY_TYPE);
		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.TICKET_OPEN_VS_CLOSED_RATE_BY_TYPE);

		List<DataCountGroup> dataCountGroups = new ArrayList<>();
		trendValuesMap.forEach((key, datewiseDataCount) -> {
			DataCountGroup dataCountGroup = new DataCountGroup();
			dataCountGroup.setFilter(key);
			dataCountGroup.setValue(datewiseDataCount);
			dataCountGroups.add(dataCountGroup);
		});

		kpiElement.setTrendValueList(dataCountGroups);
		// map aggregation implementation over

		kpiElement.setNodeWiseKPIValue(nodeWiseKPIValue);

		log.debug(
				"[STORY OPEN RATE BY ISSUE-KANBAN-AGGREGATED-VALUE][{}]. Aggregated Value at each level in the tree {}",
				kpiRequest.getRequestTrackerId(), root);
		return kpiElement;
	}

	/**
	 * Calculates KPI Metrics
	 *
	 * @param subCategoryMap
	 * @return Integer
	 */
	@Override
	public Long calculateKPIMetrics(Map<String, Object> subCategoryMap) {
		return 0L;
	}

	/**
	 * Fetches KPI Data From Database
	 *
	 * @param leafNodeList
	 * @param startDate
	 * @param endDate
	 * @return resultListMap
	 */
	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		Map<String, Object> resultListMap = new HashMap<>();
		List<String> projectList = new ArrayList<>();
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
		Map<String, List<String>> projectWiseIssueTypeMap = new HashMap<>();
		leafNodeList.forEach(leaf -> {
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			projectList.add(basicProjectConfigId.toString());

			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(leaf.getProjectFilter().getBasicProjectConfigId());
			if (Optional.ofNullable(fieldMapping.getTicketCountIssueType()).isPresent()) {
				mapOfProjectFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
						CommonUtils.convertToPatternList(fieldMapping.getTicketCountIssueType()));

				projectWiseIssueTypeMap.put(leaf.getProjectFilter().getId(),
						fieldMapping.getTicketCountIssueType().stream().distinct().collect(Collectors.toList()));
			}
			uniqueProjectMap.put(leaf.getProjectFilter().getId(), mapOfProjectFilters);

		});

		/** additional filter **/
		String subGroupCategory = KpiDataHelper.createAdditionalFilterMap(kpiRequest, mapOfFilters, Constant.KANBAN,
				DEV, flterHelperService);
		mapOfFilters.put(JiraFeatureHistory.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				projectList.stream().distinct().collect(Collectors.toList()));
		resultListMap.put(TICKET_LIST, kanbanJiraIssueRepository.findIssuesByDateAndType(mapOfFilters, uniqueProjectMap,
				startDate, endDate, RANGE));
		resultListMap.put(SUBGROUPCATEGORY, subGroupCategory);
		resultListMap.put(PROJECT_WISE_ISSUETYPES, projectWiseIssueTypeMap);

		return resultListMap;
	}

	/**
	 * Populates KPI value to sprint leaf nodes and gives the trend analysis at
	 * sprint wise.
	 *
	 * @param mapTmp
	 * @param leafNodeList
	 * @param kpiElement
	 * @param kpiRequest
	 *
	 */
	@SuppressWarnings("unchecked")
	private void dateWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> leafNodeList, KpiElement kpiElement,
			KpiRequest kpiRequest) {

		// this method fetch start and end date to fetch data.
		CustomDateRange dateRange = KpiDataHelper.getStartAndEndDate(kpiRequest);

		// get start and end date in yyyy-mm-dd format
		String startDate = dateRange.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		String endDate = dateRange.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

		// data fetch method (no change)
		Map<String, Object> resultMap = fetchKPIDataFromDb(leafNodeList, startDate, endDate, kpiRequest);

		// now that you have all the data
		// you need to know the chart type of your kpi.
		// if it contains filter on it then we will create a map of filters per project
		// and store it on the aggregation tree and if it is a simple kpi then list will
		// be
		// in aggregation tree.
		String subGroupCategory = (String) resultMap.get(SUBGROUPCATEGORY);

		Map<String, List<KanbanJiraIssue>> projectWiseJiraIssue = KpiDataHelper.createProjectWiseMapKanban(
				(List<KanbanJiraIssue>) resultMap.get(TICKET_LIST), subGroupCategory, flterHelperService);

		// choose one method from below
		kpiWithoutFilter(projectWiseJiraIssue, mapTmp, leafNodeList, kpiElement, kpiRequest);

		kpiWithFilter(projectWiseJiraIssue, mapTmp, leafNodeList, kpiElement, kpiRequest);

	}

	private void kpiWithoutFilter(Map<String, List<KanbanJiraIssue>> projectWiseJiraIssue, Map<String, Node> mapTmp,
			List<Node> leafNodeList, KpiElement kpiElement, KpiRequest kpiRequest) {
		Map<String, ValidationData> validationDataMap = new HashMap<>();
		String requestTrackerId = getKanbanRequestTrackerId();
		leafNodeList.forEach(node -> {
			List<KanbanJiraIssue> kanbanIssueList = projectWiseJiraIssue.get(node.getId());
			if (CollectionUtils.isNotEmpty(kanbanIssueList)) {
				String projectNodeId = node.getId();
				String projectName = projectNodeId.substring(0, projectNodeId.lastIndexOf(CommonConstant.UNDERSCORE));
				LocalDate currentDate = LocalDate.now();
				List<DataCount> dc = new ArrayList<>();
				for (int i = 0; i < kpiRequest.getKanbanXaxisDataPoints(); i++) {
					// fetch date range based on period for which request came
					CustomDateRange dateRange = KpiDataHelper.getStartAndEndDateForDataFiltering(currentDate,
							kpiRequest.getDuration());

					// filter based on date your scenario this time its start and end date
					List<KanbanJiraIssue> filteredList = filterKanbanDataBasedOnStartAndEndDate(kanbanIssueList,
							dateRange.getStartDate(), dateRange.getEndDate());
					// make it based on month, week and day
					// This kpi is week wise
					String date = getRange(dateRange, kpiRequest);
					// calculation based on your kpi
					Long techDebt = 0L;
					for (KanbanJiraIssue jiraIssue : filteredList) {
						techDebt = techDebt + Math.round(Double.valueOf(jiraIssue.getEstimate()));
					}
					DataCount dcObj = getDataCountObject(techDebt, projectName, date, projectNodeId);
					dc.add(dcObj);
					// below method is to get excel export functionality
					// input and implementation and position of this fuction may vary depending on
					// kpi
					populateValidationDataObject(kpiElement, requestTrackerId, validationDataMap, filteredList,
							date + Constant.UNDERSCORE + projectName);

					if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.WEEK)) {
						currentDate = currentDate.minusWeeks(1);
					} else if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.MONTH)) {
						currentDate = currentDate.minusMonths(1);
					} else {
						currentDate = currentDate.minusDays(1);
					}
				}
				mapTmp.get(node.getId()).setValue(dc);
			}
		});
	}

	private void kpiWithFilter(Map<String, List<KanbanJiraIssue>> projectWiseJiraIssue, Map<String, Node> mapTmp,
			List<Node> leafNodeList, KpiElement kpiElement, KpiRequest kpiRequest) {
		// implementing ticket type dropdown filter json
		Map<String, ValidationData> validationDataMap = new HashMap<>();
		String requestTrackerId = getKanbanRequestTrackerId();
		leafNodeList.forEach(node -> {
			List<KanbanJiraIssue> kanbanIssueList = projectWiseJiraIssue.get(node.getId());
			if (CollectionUtils.isNotEmpty(kanbanIssueList)) {
				List<String> issueTypeList = kanbanIssueList.stream().map(KanbanJiraIssue::getTypeName).distinct()
						.collect(Collectors.toList());
				Map<String, List<DataCount>> projectFilterWiseDataMap = new HashMap<>();
				String projectNodeId = node.getId();

				LocalDate currentDate = LocalDate.now();
				for (int i = 0; i < kpiRequest.getKanbanXaxisDataPoints(); i++) {
					// fetch date range based on period for which request came
					CustomDateRange dateRange = KpiDataHelper.getStartAndEndDateForDataFiltering(currentDate,
							kpiRequest.getDuration());

					// create a map based on your kpi in this case story open rate it will be map of
					// story type and count
					Map<String, Long> issueTypeCountMap = filterKanbanDataBasedOnStartAndEndDateAndIssueType(
							kanbanIssueList, issueTypeList, dateRange.getStartDate(), dateRange.getEndDate());
					// make it based on month, week and day
					// This kpi is week wise

					String date = getRange(dateRange, kpiRequest);

					// create it according to your kpi
					populateProjectFilterWiseDataMap(issueTypeCountMap, projectFilterWiseDataMap, projectNodeId, date);

					if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.WEEK)) {
						currentDate = currentDate.minusWeeks(1);
					} else if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.MONTH)) {
						currentDate = currentDate.minusMonths(1);
					} else {
						currentDate = currentDate.minusDays(1);
					}
					String projectName = projectNodeId.substring(0,
							projectNodeId.lastIndexOf(CommonConstant.UNDERSCORE));
					populateValidationDataObject(kpiElement, requestTrackerId, validationDataMap, kanbanIssueList,
							date + Constant.UNDERSCORE + projectName);
				}
				// move or create this method based on your kpi

				mapTmp.get(node.getId()).setValue(projectFilterWiseDataMap);
			}
		});
	}

	private String getRange(CustomDateRange dateRange, KpiRequest kpiRequest) {
		String range = null;
		if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.WEEK)) {
			range = dateRange.getStartDate().toString() + " to " + dateRange.getEndDate().toString();
		} else if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.MONTH)) {
			range = dateRange.getStartDate().getMonth().toString();
		} else {
			range = dateRange.getStartDate().toString();
		}
		return range;
	}

	private void populateProjectFilterWiseDataMap(Map<String, Long> issueTypeCountMap,
			Map<String, List<DataCount>> projectFilterWiseDataMap, String projectNodeId, String date) {
		String projectName = projectNodeId.substring(0, projectNodeId.lastIndexOf(CommonConstant.UNDERSCORE));

		issueTypeCountMap.forEach((key, value) -> {
			Map<String, Long> issueWiseMap = new HashMap<>();
			issueWiseMap.put(key, value);
			DataCount dcObj = getDataCountObject(issueWiseMap, projectName, date, projectNodeId);
			projectFilterWiseDataMap.computeIfAbsent(key, k -> new ArrayList<>()).add(dcObj);
		});

		projectFilterWiseDataMap.computeIfAbsent(CommonConstant.OVERALL, k -> new ArrayList<>())
				.add(getDataCountObject(issueTypeCountMap, projectName, date, projectNodeId));
	}

	private DataCount getDataCountObject(Long value, String projectName, String date, String projectNodeId) {
		DataCount dataCount = new DataCount();
		dataCount.setData(String.valueOf(value));
		dataCount.setSProjectName(projectName);
		dataCount.setKanbanDate(date);
		dataCount.setHoverValue(new HashMap<>());
		dataCount.setSprintIds(new ArrayList<>(Arrays.asList(projectNodeId)));
		dataCount.setSprintNames(new ArrayList<>(Arrays.asList(projectName)));
		dataCount.setValue(value);
		return dataCount;
	}

	private DataCount getDataCountObject(Map<String, Long> value, String projectName, String date,
			String projectNodeId) {
		DataCount dataCount = new DataCount();
		dataCount.setData(String.valueOf(value));
		dataCount.setSProjectName(projectName);
		dataCount.setDate(date);
		dataCount.setHoverValue(new HashMap<>());
		dataCount.setSprintIds(new ArrayList<>(Arrays.asList(projectNodeId)));
		dataCount.setSprintNames(new ArrayList<>(Arrays.asList(projectName)));
		dataCount.setValue(value);
		return dataCount;
	}

	private void populateValidationDataObject(KpiElement kpiElement, String requestTrackerId,
			Map<String, ValidationData> validationDataMap, List<KanbanJiraIssue> projectWiseFeatureMap,
			String dateProjectKey) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {

			if (CollectionUtils.isNotEmpty(projectWiseFeatureMap)) {
				ValidationData validationData = new ValidationData();
				validationData.setTicketKeyList(
						projectWiseFeatureMap.stream().map(KanbanJiraIssue::getNumber).collect(Collectors.toList()));
				validationData.setIssueTypeList(
						projectWiseFeatureMap.stream().map(KanbanJiraIssue::getTypeName).collect(Collectors.toList()));

				validationDataMap.put(dateProjectKey, validationData);
			}
			kpiElement.setMapOfSprintAndData(validationDataMap);
		}
	}
}
