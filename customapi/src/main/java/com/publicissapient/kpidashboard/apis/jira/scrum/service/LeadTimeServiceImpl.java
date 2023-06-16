package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.apis.enums.JiraFeatureHistory;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.AggregationUtils;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.common.model.application.CycleTime;
import com.publicissapient.kpidashboard.common.model.application.CycleTimeValidationData;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.LeadTimeData;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LeadTimeServiceImpl extends JiraKPIService<Long, List<Object>, Map<String, Object>> {
	private static final String LEAD_TIME = "Lead Time";
	private static final String STORY_HISTORY_DATA = "storyHistoryData";
	private static final String INTAKE_TO_DOR = "Intake - DoR";
	private static final String DOR_TO_DOD = "DoR - DoD";
	private static final String DOD_TO_LIVE = "DoD - Live";
	private static final String PROJECT = "project";

	private static final long DAYS_IN_SPRINT = 14;

	@Autowired
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private CustomApiConfig customApiConfig;

	@Override
	public Long calculateKPIMetrics(Map<String, Object> stringObjectMap) {
		return 0L;
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();

		List<String> basicProjectConfigIds = new ArrayList<>();
		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();

			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);

			basicProjectConfigIds.add(basicProjectConfigId.toString());

			if (Optional.ofNullable(fieldMapping.getJiraIntakeToDorIssueType()).isPresent()) {

				KpiDataHelper.prepareFieldMappingDefectTypeTransformation(mapOfProjectFilters, fieldMapping,
						fieldMapping.getJiraIntakeToDorIssueType(),
						JiraFeatureHistory.STORY_TYPE.getFieldValueInFeature());
				uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);

			}
		});
		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));

		resultListMap.put(STORY_HISTORY_DATA, jiraIssueCustomHistoryRepository
				.findIssuesByCreatedDateAndType(mapOfFilters, uniqueProjectMap, startDate, endDate));

		return resultListMap;
	}

	@Override
	public String getQualifierType() {
		return KPICode.LEAD_TIME.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		log.info("LEAD-TIME", kpiRequest.getRequestTrackerId());
		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
		List<Node> projectList = treeAggregatorDetail.getMapOfListOfProjectNodes().get(PROJECT);

		projectWiseLeafNodeValue(mapTmp, projectList, kpiElement, kpiRequest);

		log.debug("[LEAD-TIME-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValueMap(root, nodeWiseKPIValue, KPICode.LEAD_TIME);

		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, nodeWiseKPIValue,
				KPICode.LEAD_TIME);

		List<DataCountGroup> dataCountGroups = getDataCountGroups(trendValuesMap);

		kpiElement.setTrendValueList(dataCountGroups);
		List<String> maturityRanges = new ArrayList<>(
				configHelperService.calculateMaturity().get(LEAD_TIME.replace(" ", "")));
		maturityRanges.addAll(configHelperService.calculateMaturity().get(INTAKE_TO_DOR.replace(" ", "")));
		maturityRanges.addAll(configHelperService.calculateMaturity().get(DOR_TO_DOD.replace(" ", "")));
		maturityRanges.addAll(configHelperService.calculateMaturity().get(DOD_TO_LIVE.replace(" ", "")));
		kpiElement.setMaturityRange(maturityRanges);

		log.debug("[LEAD-TIME-AGGREGATED-VALUE][{}]. Aggregated Value at each level in the tree {}",
				kpiRequest.getRequestTrackerId(), root);
		return kpiElement;
	}

	private List<DataCountGroup> getDataCountGroups(Map<String, List<DataCount>> trendValuesMap) {
		trendValuesMap = sortTrendValueMap(trendValuesMap, filterOrder());
		Map<String, Map<String, List<DataCount>>> filterProjectWiseDc = new LinkedHashMap<>();
		trendValuesMap.forEach((filter, dataCounts) -> {
			Map<String, List<DataCount>> projectWiseDc = dataCounts.stream()
					.collect(Collectors.groupingBy(DataCount::getData));
			filterProjectWiseDc.put(filter, projectWiseDc);
		});

		List<DataCountGroup> dataCountGroups = new ArrayList<>();
		filterProjectWiseDc.forEach((filter, projectWiseDc) -> {
			DataCountGroup dataCountGroup = new DataCountGroup();
			List<DataCount> dataList = new ArrayList<>();
			projectWiseDc.entrySet().stream().forEach(trend -> {
				List<DataCount> dcList = trend.getValue();
				dcList.forEach(dc -> {
					List<Long> dcValues = new ArrayList<>();
					List<DataCount> latestDcList = (List<DataCount>) dc.getValue();
					latestDcList.forEach(dataCount -> dcValues.add((Long) dataCount.getValue()));
					Long aggValue = calculateKpiValue(dcValues, KPICode.LEAD_TIME.getKpiId());
					String maturityValue = calculateMaturity(
							configHelperService.calculateMaturity().get(filter.replace(" ", "")),
							KPICode.LEAD_TIME.getKpiId(), String.valueOf(aggValue));
					dc.setMaturity(maturityValue);
					dc.setMaturityValue(aggValue);
				});
				dataList.addAll(dcList);
			});
			dataCountGroup.setFilter(filter);
			dataCountGroup.setValue(dataList);
			dataCountGroups.add(dataCountGroup);
		});
		return dataCountGroups;
	}

	private void projectWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> leafNodeList, KpiElement kpiElement,
			KpiRequest kpiRequest) {
		// calculate days in based on data points in sprints
		String startDate = LocalDate.now().minusDays(customApiConfig.getSprintCountForFilters() * DAYS_IN_SPRINT)
				.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		String endDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

		Map<String, Object> resultMap = fetchKPIDataFromDb(leafNodeList, startDate, endDate, kpiRequest);

		List<JiraIssueCustomHistory> ticketList = (List<JiraIssueCustomHistory>) resultMap.get(STORY_HISTORY_DATA);
		Map<String, List<JiraIssueCustomHistory>> projectWiseJiraIssue = ticketList.stream()
				.collect(Collectors.groupingBy(JiraIssueCustomHistory::getBasicProjectConfigId));

		kpiWithFilter(projectWiseJiraIssue, mapTmp, leafNodeList, kpiElement);

	}

	private void kpiWithFilter(Map<String, List<JiraIssueCustomHistory>> projectWiseJiraIssue, Map<String, Node> mapTmp,
			List<Node> leafNodeList, KpiElement kpiElement) {

		List<KPIExcelData> excelData = new ArrayList<>();
		String requestTrackerId = getRequestTrackerId();

		leafNodeList.forEach(node -> {
			String trendLineName = node.getProjectFilter().getName();
			List<JiraIssueCustomHistory> issueCustomHistoryList = projectWiseJiraIssue
					.get(node.getProjectFilter().getBasicProjectConfigId().toString());
			if (CollectionUtils.isNotEmpty(issueCustomHistoryList)) {
				FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
						.get(node.getProjectFilter().getBasicProjectConfigId());
				List<CycleTimeValidationData> cycleTimeList = new ArrayList<>();
				Map<String, Long> cycleMap = getCycleTime(issueCustomHistoryList, fieldMapping, cycleTimeList);

				Map<String, List<DataCount>> dataCountMap = getDataCountMap(trendLineName, cycleMap);
				mapTmp.get(node.getId()).setValue(dataCountMap);

				log.debug(
						"[LEAD-TIME-FILTER-WISE][{}].  : Intake to DOR: {} . DoR to DoD: {} . DoD to Live: {} . Intake to Live: {}",
						requestTrackerId, cycleMap.get(INTAKE_TO_DOR), cycleMap.get(DOR_TO_DOD),
						cycleMap.get(DOD_TO_LIVE), cycleMap.get(LEAD_TIME));
				LeadTimeData leadTimeData = getLeadTime(cycleTimeList);

				if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {

					KPIExcelUtility.populateLeadTime(excelData, trendLineName, leadTimeData);

				}

			}
		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.LEAD_TIME.getColumns());
	}

	private Map<String, List<DataCount>> getDataCountMap(String trendLineName, Map<String, Long> cycleMap) {
		Map<String, List<DataCount>> dataCountMap = new HashMap<>();

		cycleMap.forEach((key, value) -> {
			DataCount dataCount = new DataCount();
			dataCount.setData(String.valueOf(value));
			dataCount.setSProjectName(trendLineName);
			dataCount.setSSprintID(trendLineName);
			dataCount.setSSprintName(trendLineName);
			dataCount.setValue(value);
			dataCount.setKpiGroup(key);
			dataCount.setHoverValue(new HashMap<>());
			dataCountMap.put(key, new ArrayList<>(Arrays.asList(dataCount)));
		});
		return dataCountMap;
	}

	private LeadTimeData getLeadTime(List<CycleTimeValidationData> cycletimeList) {

		List<String> intakeDorDay = new ArrayList<>();
		List<String> dorDodDay = new ArrayList<>();
		List<String> dodLiveDay = new ArrayList<>();
		List<String> intakeLiveDay = new ArrayList<>();
		List<String> issueNumber = new ArrayList<>();
		List<String> issueURL = new ArrayList<>();
		List<String> issueDisc = new ArrayList<>();
		LeadTimeData leadTimeData = new LeadTimeData();

		if (CollectionUtils.isNotEmpty(cycletimeList)) {

			for (CycleTimeValidationData cycleTimeValidationData : cycletimeList) {

				issueNumber.add(cycleTimeValidationData.getIssueNumber());
				issueURL.add(cycleTimeValidationData.getUrl());
				issueDisc.add(cycleTimeValidationData.getIssueDesc());

				if (cycleTimeValidationData.getIntakeDate() != null && cycleTimeValidationData.getDorDate() != null) {
					Long diff = cycleTimeValidationData.getDorDate().getMillis()
							- cycleTimeValidationData.getIntakeDate().getMillis();
					intakeDorDay.add(String.valueOf(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)));
				} else {
					intakeDorDay.add("NA");
				}
				if (cycleTimeValidationData.getDorDate() != null && cycleTimeValidationData.getDodDate() != null) {
					Long diff = cycleTimeValidationData.getDodDate().getMillis()
							- cycleTimeValidationData.getDorDate().getMillis();
					dorDodDay.add(String.valueOf(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)));
				} else {
					dorDodDay.add("NA");
				}
				if (cycleTimeValidationData.getDodDate() != null && cycleTimeValidationData.getLiveDate() != null) {
					Long diff = cycleTimeValidationData.getLiveDate().getMillis()
							- cycleTimeValidationData.getDodDate().getMillis();
					dodLiveDay.add(String.valueOf(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)));
				} else {
					dodLiveDay.add("NA");
				}
				if (cycleTimeValidationData.getIntakeDate() != null && cycleTimeValidationData.getLiveDate() != null) {
					Long diff = cycleTimeValidationData.getLiveDate().getMillis()
							- cycleTimeValidationData.getIntakeDate().getMillis();
					intakeLiveDay.add(String.valueOf(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)));
				} else {
					intakeLiveDay.add("NA");
				}

			}
			leadTimeData.setIssueNumber(issueNumber);
			leadTimeData.setUrlList(issueURL);
			leadTimeData.setIssueDiscList(issueDisc);
			leadTimeData.setIntakeToDor(intakeDorDay);
			leadTimeData.setDorToDOD(dorDodDay);
			leadTimeData.setDodToLive(dodLiveDay);
			leadTimeData.setIntakeToLive(intakeLiveDay);
		}

		return leadTimeData;
	}

	private Map<String, Long> getCycleTime(List<JiraIssueCustomHistory> jiraIssueCustomHistories,
			FieldMapping fieldMapping, List<CycleTimeValidationData> cycleTimeList) {
		List<Long> intakeDorTime = new ArrayList<>();
		List<Long> dorDodTime = new ArrayList<>();
		List<Long> dodLiveTime = new ArrayList<>();
		List<Long> intakeLiveTime = new ArrayList<>();
		Long intakeDor = 0L;
		Long dorDod = 0L;
		Long dodLive = 0L;
		Long intakeLive = 0L;
		Map<String, Long> cycleResult = new LinkedHashMap<>();
		if (CollectionUtils.isNotEmpty(jiraIssueCustomHistories)) {
			// in below loop create list of day difference between Intake and
			// DOR. Here Intake is created date of issue.
			for (JiraIssueCustomHistory jiraIssueCustomHistory : jiraIssueCustomHistories) {
				String dor = fieldMapping.getJiraDor();
				List<String> dod = fieldMapping.getJiraDod();
				String live = fieldMapping.getJiraLiveStatus();
				CycleTimeValidationData cycleTimeValidationData = new CycleTimeValidationData();
				cycleTimeValidationData.setIssueNumber(jiraIssueCustomHistory.getStoryID());
				cycleTimeValidationData.setUrl(jiraIssueCustomHistory.getUrl());
				cycleTimeValidationData.setIssueDesc(jiraIssueCustomHistory.getDescription());
				CycleTime cycleTime = new CycleTime();
				cycleTime.setIntakeTime(jiraIssueCustomHistory.getCreatedDate());
				cycleTimeValidationData.setIntakeDate(jiraIssueCustomHistory.getCreatedDate());
				jiraIssueCustomHistory.getStatusUpdationLog().forEach(sprintDetail -> updateCycleTimeValidationData(dor,
						dod, live, cycleTimeValidationData, cycleTime, sprintDetail));
				setCycleTimeAsPerFilter(intakeDorTime, dorDodTime, dodLiveTime, intakeLiveTime, cycleTime);
				cycleTimeList.add(cycleTimeValidationData);
			}

			intakeDor = AggregationUtils.averageLong(intakeDorTime);
			dorDod = AggregationUtils.averageLong(dorDodTime);
			dodLive = AggregationUtils.averageLong(dodLiveTime);
			intakeLive = AggregationUtils.averageLong(intakeLiveTime);
		}

		cycleResult.put(LEAD_TIME, ObjectUtils.defaultIfNull(intakeLive, 0L));
		cycleResult.put(INTAKE_TO_DOR, ObjectUtils.defaultIfNull(intakeDor, 0L));
		cycleResult.put(DOR_TO_DOD, ObjectUtils.defaultIfNull(dorDod, 0L));
		cycleResult.put(DOD_TO_LIVE, ObjectUtils.defaultIfNull(dodLive, 0L));
		return cycleResult;
	}

	/**
	 * calculate time as per filter based on cycle status time
	 * 
	 * @param intakeDorTime
	 * @param dorDodTime
	 * @param dodLiveTime
	 * @param intakeLiveTime
	 * @param cycleTime
	 */
	private void setCycleTimeAsPerFilter(List<Long> intakeDorTime, List<Long> dorDodTime, List<Long> dodLiveTime,
			List<Long> intakeLiveTime, CycleTime cycleTime) {
		if (cycleTime.getReadyTime() != null && cycleTime.getIntakeTime() != null) {
			Long diff = cycleTime.getReadyTime().getMillis() - cycleTime.getIntakeTime().getMillis();
			intakeDorTime.add(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));
		}
		if (cycleTime.getDeliveryTime() != null && cycleTime.getReadyTime() != null) {
			Long diff = cycleTime.getDeliveryTime().getMillis() - cycleTime.getReadyTime().getMillis();
			dorDodTime.add(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));
		}
		if (cycleTime.getLiveTime() != null && cycleTime.getDeliveryTime() != null) {
			Long diff = cycleTime.getLiveTime().getMillis() - cycleTime.getDeliveryTime().getMillis();
			dodLiveTime.add(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));
		}
		if (cycleTime.getIntakeTime() != null && cycleTime.getLiveTime() != null) {
			Long diff = cycleTime.getLiveTime().getMillis() - cycleTime.getIntakeTime().getMillis();
			intakeLiveTime.add(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));
		}
	}

	/**
	 * Updates cycle time data for validation.
	 *
	 * @param dor
	 *            DOR
	 * @param dod
	 *            DOD
	 * @param live
	 *            Live
	 * @param cycleTimeValidationData
	 *            CycleTimeValidationData
	 * @param cycleTime
	 *            CycleTime
	 * @param jiraIssueSprint
	 *            FeatureSprint
	 */
	private void updateCycleTimeValidationData(String dor, List<String> dod, String live,
			CycleTimeValidationData cycleTimeValidationData, CycleTime cycleTime,
			JiraHistoryChangeLog jiraIssueSprint) {
		if (cycleTime.getReadyTime() == null && null != dor && dor.equalsIgnoreCase(jiraIssueSprint.getChangedTo())) {
			cycleTime.setReadyTime(DateTime.parse(jiraIssueSprint.getUpdatedOn().toString()));
			cycleTimeValidationData.setDorDate(DateTime.parse(jiraIssueSprint.getUpdatedOn().toString()));
		}
		if (org.apache.commons.collections.CollectionUtils.isNotEmpty(dod)
				&& dod.contains(jiraIssueSprint.getChangedTo())) {
			cycleTime.setDeliveryTime(DateTime.parse(jiraIssueSprint.getUpdatedOn().toString()));
			cycleTimeValidationData.setDodDate(DateTime.parse(jiraIssueSprint.getUpdatedOn().toString()));
		}
		if (Optional.ofNullable(live).isPresent() && live.equalsIgnoreCase(jiraIssueSprint.getChangedTo())) {
			cycleTime.setLiveTime(DateTime.parse(jiraIssueSprint.getUpdatedOn().toString()));
			cycleTimeValidationData.setLiveDate(DateTime.parse(jiraIssueSprint.getUpdatedOn().toString()));
		}
	}

	/**
	 * @param requestTrackerId
	 * @param excelData
	 * @param trendLineName
	 * @param cycleTimeList
	 */
	private void populateExcelDataObject(String requestTrackerId, List<KPIExcelData> excelData,
			Map<String, Long> cycleMap, String trendLineName, List<CycleTimeValidationData> cycleTimeList) {

	}

	/**
	 *
	 * @param trendMap
	 * @param keyOrder
	 * @return
	 */
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

	private List<String> filterOrder() {
		return Arrays.asList(LEAD_TIME, INTAKE_TO_DOR, DOR_TO_DOD, DOD_TO_LIVE);
	}

	@Override
	public Long calculateKpiValue(List<Long> valueList, String kpiName) {
		return calculateKpiValueForLong(valueList, kpiName);
	}
}
