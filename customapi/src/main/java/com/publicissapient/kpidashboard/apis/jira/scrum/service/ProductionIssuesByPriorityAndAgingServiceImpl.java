package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import static java.time.temporal.ChronoUnit.DAYS;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.CustomDateRange;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ValidationData;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

import lombok.extern.slf4j.Slf4j;

/**
 *
 *
 * @author Daya Shankar
 */
@Slf4j
@Component
public class ProductionIssuesByPriorityAndAgingServiceImpl
		extends JiraKPIService<Long, List<Object>, Map<String, Object>> {

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final String PROJECT = "project";
	private static final String RANGE = "range";
	private static final String RANGE_TICKET_LIST = "rangeTickets";
	private static final String NIN = "nin";
	@Autowired
	private ConfigHelperService configHelperService;
	@Autowired
	private CustomApiConfig customApiConfig;
	@Autowired
	private KpiHelperService kpiHelperService;
	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Override
	public Long calculateKPIMetrics(Map<String, Object> stringMapMap) {
		return 0L;
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		Map<String, Object> resultListMap = new HashMap<>();
		List<String> basicProjectConfigIds = new ArrayList<>();
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();

		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
			basicProjectConfigIds.add(basicProjectConfigId.toString());

			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
			List<String> defectList = new ArrayList<>();
			defectList.add(CommonConstant.BUG);
			mapOfProjectFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
					CommonUtils.convertToPatternList(defectList));

			if (Optional.ofNullable(fieldMapping.getJiraDodKPI127()).isPresent()) {
				List<String> closedStatusList = new ArrayList<>();
				closedStatusList.addAll(fieldMapping.getJiraDodKPI127());
				if (Optional.ofNullable(fieldMapping.getJiraLiveStatusKPI127()).isPresent()) {
					closedStatusList.add(fieldMapping.getJiraLiveStatusKPI127());
				}
				if (Optional.ofNullable(fieldMapping.getJiraDefectDroppedStatusKPI127()).isPresent()) {
					closedStatusList.addAll(fieldMapping.getJiraDefectDroppedStatusKPI127());
				}
				mapOfProjectFilters.put(JiraFeature.JIRA_ISSUE_STATUS.getFieldValueInFeature(),
						CommonUtils.convertToPatternList(closedStatusList));
			}
			uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);

		});
		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));
		resultListMap.put(RANGE_TICKET_LIST, jiraIssueRepository.findIssuesByDateAndTypeAndStatus(mapOfFilters,
				uniqueProjectMap, startDate, endDate, RANGE, NIN, true));

		return resultListMap;
	}

	/**
	 * Gets Qualifier Type
	 *
	 * @return KPICode's <tt>PRODUCTION_ISSUES_BY_PRIORITY_AND_AGING</tt> enum
	 */
	@Override
	public String getQualifierType() {
		return KPICode.PRODUCTION_ISSUES_BY_PRIORITY_AND_AGING.name();
	}

	@Override
	public Long calculateKpiValue(List<Long> valueList, String kpiName) {
		return calculateKpiValueForLong(valueList, kpiName);
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		log.info("PRODUCTION-ISSUES-BY-PRIORITY-AND-AGING {}", kpiRequest.getRequestTrackerId());
		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
		List<Node> projectList = treeAggregatorDetail.getMapOfListOfProjectNodes().get(PROJECT);

		dateWiseLeafNodeValue(mapTmp, projectList, kpiElement, kpiRequest);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		// for chart with filter,group stack chart
		calculateAggregatedValueMap(root, nodeWiseKPIValue, KPICode.PRODUCTION_ISSUES_BY_PRIORITY_AND_AGING);
		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.PRODUCTION_ISSUES_BY_PRIORITY_AND_AGING);

		trendValuesMap = sortTrendValueMap(trendValuesMap, priorityTypes(true));
		Map<String, Map<String, List<DataCount>>> priorityTypeProjectWiseDc = new LinkedHashMap<>();
		trendValuesMap.forEach((priority, dataCounts) -> {
			Map<String, List<DataCount>> projectWiseDc = dataCounts.stream()
					.collect(Collectors.groupingBy(DataCount::getData));
			priorityTypeProjectWiseDc.put(priority, projectWiseDc);
		});

		List<DataCountGroup> dataCountGroups = new ArrayList<>();
		priorityTypeProjectWiseDc.forEach((priority, projectWiseDc) -> {
			DataCountGroup dataCountGroup = new DataCountGroup();
			List<DataCount> dataList = new ArrayList<>();
			projectWiseDc.entrySet().stream().forEach(trend -> dataList.addAll(trend.getValue()));
			dataCountGroup.setFilter(priority);
			dataCountGroup.setValue(dataList);
			dataCountGroups.add(dataCountGroup);
		});

		kpiElement.setTrendValueList(dataCountGroups);
		kpiElement.setNodeWiseKPIValue(nodeWiseKPIValue);

		log.debug(
				"[PRODUCTION-ISSUES-BY-PRIORITY-AND-AGING -AGGREGATED-VALUE][{}]. Aggregated Value at each level in the tree {}",
				kpiRequest.getRequestTrackerId(), root);
		return kpiElement;
	}

	private void dateWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> leafNodeList, KpiElement kpiElement,
			KpiRequest kpiRequest) {

		// this method fetch start and end date to fetch data.
		CustomDateRange dateRange = KpiDataHelper.getMonthsForPastDataHistory(15);

		// get start and end date in yyyy-mm-dd format
		String startDate = dateRange.getStartDate().format(DATE_FORMATTER);
		String endDate = dateRange.getEndDate().format(DATE_FORMATTER);

		// past all tickets and given range ticket data fetch from db
		Map<String, Object> resultMap = fetchKPIDataFromDb(leafNodeList, startDate, endDate, kpiRequest);

		List<JiraIssue> jiraIssueList = (List<JiraIssue>) resultMap.get(RANGE_TICKET_LIST);
		Map<String, List<JiraIssue>> projectWiseJiraIssue = jiraIssueList.stream()
				.collect(Collectors.groupingBy(JiraIssue::getBasicProjectConfigId));

		kpiWithFilter(projectWiseJiraIssue, mapTmp, leafNodeList, kpiElement);
	}

	private void kpiWithFilter(Map<String, List<JiraIssue>> projectWiseJiraIssueMap, Map<String, Node> mapTmp,
			List<Node> leafNodeList, KpiElement kpiElement) {
		String requestTrackerId = getRequestTrackerId();
		List<KPIExcelData> excelData = new ArrayList<>();
		List<String> xAxisRange = customApiConfig.getTotalDefectCountAgingXAxisRange();
		kpiElement.setxAxisValues(xAxisRange);

		leafNodeList.forEach(node -> {
			Map<String, List<DataCount>> trendValueMap = new HashMap<>();
			String projectNodeId = node.getProjectFilter().getBasicProjectConfigId().toString();
			String projectName = node.getProjectFilter().getName();
			List<JiraIssue> projectWiseJiraIssueList = projectWiseJiraIssueMap.getOrDefault(projectNodeId,
					new ArrayList<>());

			if (CollectionUtils.isNotEmpty(projectWiseJiraIssueList)) {

				Set<String> priorityList = projectWiseJiraIssueList.stream()
						.map(issue -> KPIHelperUtil.mappingPriority(issue.getPriority(), customApiConfig))
						.collect(Collectors.toSet());

				Map<String, List<JiraIssue>> rangeWiseJiraIssuesMap = new LinkedHashMap<>();
				filterDataBasedOnXAxisRangeWise(xAxisRange, projectWiseJiraIssueList, rangeWiseJiraIssuesMap);

				Map<String, Map<String, Long>> rangeWisePriorityCountMap = new LinkedHashMap<>();
				rangeWiseJiraIssuesMap.forEach((range, issueList) -> {
					Map<String, Long> priorityCountMap = KPIHelperUtil.setpriorityScrumForBacklog(issueList,
							customApiConfig);
					rangeWisePriorityCountMap.put(range, priorityCountMap);
				});

				rangeWisePriorityCountMap
						.forEach((rangeMonth, priorityCountMap) -> populateProjectFilterWiseDataMap(priorityCountMap,
								priorityList, trendValueMap, projectName, rangeMonth));

				// Populates data in Excel for validation for tickets created
				// before
				if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
					KPIExcelUtility.populateProductionDefectAgingExcelData(projectName, projectWiseJiraIssueList,
							excelData);
				}
			}
			mapTmp.get(node.getId()).setValue(trendValueMap);

		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.PRODUCTION_DEFECTS_AGEING.getColumns());
	}

	/**
	 * as per x Axis values initialize month wise range map and put issues as per
	 * months bucket follows
	 *
	 * @param xAxisRange
	 * @param projectWiseJiraIssueList
	 * @param rangeWiseJiraIssuesMap
	 */
	private void filterDataBasedOnXAxisRangeWise(List<String> xAxisRange, List<JiraIssue> projectWiseJiraIssueList,
			Map<String, List<JiraIssue>> rangeWiseJiraIssuesMap) {
		String highestRange = xAxisRange.get(xAxisRange.size() - 1);
		Map<Integer, String> monthRangeMap = new HashMap<>();

		initializeRangeMapForProjects(rangeWiseJiraIssuesMap, xAxisRange, monthRangeMap);

		projectWiseJiraIssueList.forEach(issue -> {
			long daysBetween = DAYS.between(KpiDataHelper.convertStringToDate(issue.getCreatedDate()), LocalDate.now());
			Integer monthsBetween = (int) Math.ceil((double) daysBetween / Constant.DAYS_IN_MONTHS);
			String range;
			if (null == monthRangeMap.get(monthsBetween)) {
				range = highestRange;
			} else {
				range = monthRangeMap.get(monthsBetween);
			}
			if (CollectionUtils.isEmpty(rangeWiseJiraIssuesMap.get(range))) {
				List<JiraIssue> jiraIssueList = new ArrayList<>();
				jiraIssueList.add(issue);
				rangeWiseJiraIssuesMap.put(range, jiraIssueList);
			} else {
				rangeWiseJiraIssuesMap.get(range).add(issue);
			}
		});
	}

	/**
	 * As per x axis ranges put data point values as per priority
	 *
	 * @param projectWisePriorityCountMap
	 * @param projectWisePriorityList
	 * @param trendValueMap
	 * @param rangeMonth
	 */
	private void populateProjectFilterWiseDataMap(Map<String, Long> projectWisePriorityCountMap,
			Set<String> projectWisePriorityList, Map<String, List<DataCount>> trendValueMap, String projectName,
			String rangeMonth) {
		Map<String, Long> projectFilterWiseDataMap = new HashMap<>();
		Map<String, Object> hoverValueMap = new HashMap<>();
		if (CollectionUtils.isNotEmpty(projectWisePriorityList)) {
			projectWisePriorityList.forEach(priority -> {
				Long priorityCount = projectWisePriorityCountMap.getOrDefault(priority, 0L);
				projectFilterWiseDataMap.put(StringUtils.capitalize(priority), priorityCount);
				hoverValueMap.put(StringUtils.capitalize(priority), priorityCount.intValue());
			});
			Long overAllCount = projectFilterWiseDataMap.values().stream().mapToLong(val -> val).sum();
			projectFilterWiseDataMap.put(CommonConstant.OVERALL, overAllCount);
		}

		projectFilterWiseDataMap.forEach((priority, value) -> {
			DataCount dcObj = getDataCountObject(value, projectName, rangeMonth, priority, hoverValueMap);
			trendValueMap.computeIfAbsent(priority, k -> new ArrayList<>()).add(dcObj);
		});

	}

	/**
	 * particulate date format given as per date type
	 *
	 * @param value
	 * @param projectName
	 * @param date
	 * @param priority
	 * @param
	 */
	private DataCount getDataCountObject(Long value, String projectName, String date, String priority,
			Map<String, Object> overAllHoverValueMap) {
		DataCount dataCount = new DataCount();
		dataCount.setData(String.valueOf(value));
		dataCount.setSProjectName(projectName);
		dataCount.setDate(date);
		dataCount.setKpiGroup(priority);
		Map<String, Object> hoverValueMap = new HashMap<>();
		if (priority.equalsIgnoreCase(CommonConstant.OVERALL)) {
			dataCount.setHoverValue(overAllHoverValueMap);
		} else {
			hoverValueMap.put(priority, value.intValue());
			dataCount.setHoverValue(hoverValueMap);
		}
		dataCount.setSSprintID(date);
		dataCount.setSSprintName(date);
		dataCount.setValue(value);
		return dataCount;
	}

	/**
	 * populate data for excel
	 *
	 * @param kpiElement
	 * @param requestTrackerId
	 * @param validationDataMap
	 * @param projectWiseJiraIssueList
	 */
	public void populateValidationDataObject(KpiElement kpiElement, String requestTrackerId, String projectName,
			Map<String, ValidationData> validationDataMap, List<JiraIssue> projectWiseJiraIssueList) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			ValidationData validationData = new ValidationData();
			validationData.setStoryKeyList(
					projectWiseJiraIssueList.stream().map(JiraIssue::getNumber).collect(Collectors.toList()));
			validationData.setDefectPriorityList(
					projectWiseJiraIssueList.stream().map(JiraIssue::getPriority).collect(Collectors.toList()));
			validationData.setStatus(
					projectWiseJiraIssueList.stream().map(JiraIssue::getJiraStatus).collect(Collectors.toList()));
			validationData.setDateList(projectWiseJiraIssueList.stream()
					.map(issue -> KpiDataHelper.convertStringToDate(issue.getCreatedDate()).toString())
					.collect(Collectors.toList()));
			validationDataMap.put(projectName, validationData);
			kpiElement.setMapOfSprintAndData(validationDataMap);
		}
	}

	/**
	 * As per x Axis range list puts months wise range map
	 *
	 * @param rangeWiseJiraIssuesMap
	 * @param xAxisRange
	 * @param monthRangeMap
	 */
	private void initializeRangeMapForProjects(Map<String, List<JiraIssue>> rangeWiseJiraIssuesMap,
			List<String> xAxisRange, Map<Integer, String> monthRangeMap) {
		xAxisRange.forEach(range -> {
			String[] rangeSplitted = range.trim().split("-");
			if (rangeSplitted.length == 2) {
				for (int i = Integer.parseInt(rangeSplitted[0]); i <= Integer.parseInt(rangeSplitted[1]); i++) {
					if (null == monthRangeMap.get(i)) {
						monthRangeMap.put(i, range);
					}
				}
			}
			rangeWiseJiraIssuesMap.put(range, new ArrayList<>());
		});
	}

	private Map<String, List<DataCount>> sortTrendValueMap(Map<String, List<DataCount>> trendMap,
			List<String> keyOrder) {
		Map<String, List<DataCount>> sortedMap = new LinkedHashMap<>();
		keyOrder.forEach(order -> {
			if (null != trendMap.get(order)) {
				sortedMap.put(order, trendMap.get(order));
			}
		});
		return sortedMap;
	}

	private List<String> priorityTypes(boolean addOverall) {
		if (addOverall) {
			return Arrays.asList(CommonConstant.OVERALL, Constant.P1, Constant.P2, Constant.P3, Constant.P4,
					Constant.MISC);
		} else {
			return Arrays.asList(Constant.P1, Constant.P2, Constant.P3, Constant.P4, Constant.MISC);
		}
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping) {
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI127(),
				KPICode.PRODUCTION_ISSUES_BY_PRIORITY_AND_AGING.getKpiId());
	}

}
