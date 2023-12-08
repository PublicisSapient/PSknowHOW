package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.enums.*;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.*;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataValue;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.ReleaseWisePI;
import com.publicissapient.kpidashboard.common.repository.application.ProjectReleaseRepo;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PIPredictabilityServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private ProjectReleaseRepo projectReleaseRepo;

	@Autowired
	private CustomApiConfig customApiConfig;

	private static final String EPIC_DATA = "EpicData";

	private static final String ARCHIVED_VALUE = "Achieved Value";

	private static final String PLANNED_VALUE = "Planned Value";

	@Override
	public String getQualifierType() {
		return KPICode.PI_PREDICTABILITY.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
		Map<String, List<DataCount>> mapForCache = new HashMap<>();
		List<Node> projectsFromCache = kpiElement.getProjectsFromCache();
		List<DataCount> trendValueList = (List<DataCount>) kpiElement.getTrendValueListFormCache();

		treeAggregatorDetail.getMapOfListOfProjectNodes().forEach((k, v) -> {
			if (Filters.getFilter(k) == Filters.PROJECT) {

				/* for adding a check for data from cache */
				addingACheckForDataFromCache(v, projectsFromCache);

				projectWiseLeafNodeValue(kpiElement, mapTmp, v, trendValueList, mapForCache);
			}

		});

		log.debug("[PROJECT-WISE][{}]. Values of leaf node after KPI calculation {}", kpiRequest.getRequestTrackerId(),
				root);

		kpiElement.setMapForCache(mapForCache);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedMultipleValueGroup(root, nodeWiseKPIValue, KPICode.PI_PREDICTABILITY);
		List<DataCount> trendValues = getTrendValues(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.PI_PREDICTABILITY);
		kpiElement.setTrendValueList(trendValues);
		return kpiElement;
	}

	private void projectWiseLeafNodeValue(KpiElement kpiElement, Map<String, Node> mapTmp,
			List<Node> projectLeafNodeList, List<DataCount> trendValueList, Map<String, List<DataCount>> mapForCache) {
		/* #deepak start changes */
		List<Node> projectLeafNodeListUpdated = projectLeafNodeList.stream().filter(node -> !node.isFromCache())
				.collect(Collectors.toList());
		Map<String, List<JiraIssue>> projectWiseEpicData = new HashMap<>();
		if (isNotEmpty(projectLeafNodeListUpdated)) {
			Map<String, Object> resultMap = fetchKPIDataFromDb(projectLeafNodeListUpdated, null, null, null);

			List<JiraIssue> epicData = (List<JiraIssue>) resultMap.get(EPIC_DATA);

			if (CollectionUtils.isNotEmpty(epicData))
				projectWiseEpicData = epicData.stream()
						.collect(Collectors.groupingBy(JiraIssue::getBasicProjectConfigId));
		}
		List<KPIExcelData> excelData = new ArrayList<>();

		Map<String, List<JiraIssue>> finalProjectWiseEpicData = projectWiseEpicData;
		projectLeafNodeList.forEach(node -> {
			String currentProjectId = node.getProjectFilter().getBasicProjectConfigId().toString();
			List<JiraIssue> epicList = finalProjectWiseEpicData.get(currentProjectId);
			List<DataCount> dataCountList = new ArrayList<>();

			if (CollectionUtils.isNotEmpty(epicList)) {
				Map<DateTime, PiWiseLatestEpicData> piNameWiseEpicData = epicList.stream()
						.filter(jiraIssue -> CollectionUtils.isNotEmpty(jiraIssue.getReleaseVersions())
								&& jiraIssue.getReleaseVersions().get(0).getReleaseDate() != null)
						.collect(Collectors.toMap(jiraIssue -> jiraIssue.getReleaseVersions().get(0).getReleaseDate(),
								jiraIssue -> {
									PiWiseLatestEpicData releaseWiseLatestEpicData = new PiWiseLatestEpicData();
									releaseWiseLatestEpicData
											.setBasicProjectConfigId(jiraIssue.getBasicProjectConfigId());
									releaseWiseLatestEpicData
											.setPiName(jiraIssue.getReleaseVersions().get(0).getReleaseName());
									releaseWiseLatestEpicData
											.setPiEndDate(jiraIssue.getReleaseVersions().get(0).getReleaseDate());
									List<JiraIssue> piWiseEpicList = new ArrayList<>();
									piWiseEpicList.add(jiraIssue);
									releaseWiseLatestEpicData.setEpicList(piWiseEpicList);
									return releaseWiseLatestEpicData;
								}, (existing, replacement) -> {
									existing.getEpicList().addAll(replacement.getEpicList());
									return existing;
								}));

				Map<DateTime, PiWiseLatestEpicData> sortedPINameWiseEpicData = piNameWiseEpicData.entrySet().stream()
						.filter(epicDataEntry -> epicDataEntry.getValue().getPiEndDate().isBefore(DateTime.now()))
						.sorted(Map.Entry.comparingByKey()).limit(customApiConfig.getJiraXaxisMonthCount())
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
								(existing, replacement) -> existing, LinkedHashMap::new));

				String trendLineName = node.getProjectFilter().getName();
				String requestTrackerId = getRequestTrackerId();

				/* #deepak starts changes check indicator value for cache */
				if (!node.isFromCache()) {
					/* #deepak ends changes */
					sortedPINameWiseEpicData.forEach((releaseDate, releaseWiseLatestEpicData) -> {
						String piName = releaseWiseLatestEpicData.getPiName();
						Double plannedValueSum = releaseWiseLatestEpicData.getEpicList().stream()
								.mapToDouble(JiraIssue::getEpicPlannedValue).sum();
						Double achievedValueSum = releaseWiseLatestEpicData.getEpicList().stream()
								.mapToDouble(JiraIssue::getEpicAchievedValue).sum();

						List<DataValue> dataValueList = new ArrayList<>();
						DataCount dataCount = new DataCount();
						dataCount.setSProjectName(trendLineName);
						dataCount.setSSprintID(piName);
						dataCount.setSSprintName(piName);

						// for line 1
						DataValue dataValue1 = new DataValue();
						dataValue1.setData(plannedValueSum.toString());
						Map<String, Object> hoverValueMap1 = new HashMap<>();
						dataValue1.setHoverValue(hoverValueMap1);
						dataValue1.setLineType(CommonConstant.SOLID_LINE_TYPE);
						dataValue1.setName(ARCHIVED_VALUE);
						dataValue1.setValue(achievedValueSum);
						dataValueList.add(dataValue1);

						// for line 2
						DataValue dataValue2 = new DataValue();
						Map<String, Object> hoverValueMap2 = new HashMap<>();
						dataValue2.setData(plannedValueSum.toString());
						dataValue2.setHoverValue(hoverValueMap2);
						dataValue2.setLineType(CommonConstant.DOTTED_LINE_TYPE);
						dataValue2.setName(PLANNED_VALUE);
						dataValue2.setValue(plannedValueSum);
						dataValueList.add(dataValue2);
						dataCount.setDataValue(dataValueList);
						// #deepak add projectid to datacount
						dataCount.setBasicProjectConfigId(node.getProjectFilter().getBasicProjectConfigId().toString());
						dataCountList.add(dataCount);

					});
					mapTmp.get(node.getId()).setValue(dataCountList);
					mapForCache.put(node.getId(), dataCountList);
				} else {
					List<DataCount> list = trendValueList.stream()
							.filter(dataCount -> node.getName().equals(dataCount.getSProjectName())).distinct()
							.collect(Collectors.toList());
					if (isNotEmpty(list))
						mapTmp.get(node.getId()).setValue(new ArrayList<DataCount>(list));
				}
				if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
					FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
							.get(node.getProjectFilter().getBasicProjectConfigId());
					List<JiraIssue> sortedEpicList = sortedPINameWiseEpicData.values().stream()
							.flatMap(piWiseLatestEpicData -> piWiseLatestEpicData.getEpicList().stream()
									.filter(jiraIssue -> CollectionUtils.isNotEmpty(jiraIssue.getReleaseVersions())
											&& jiraIssue.getReleaseVersions().get(0).getReleaseDate() != null)
									.filter(jiraIssue -> jiraIssue.getReleaseVersions().get(0).getReleaseDate()
											.isBefore(DateTime.now()))
									.filter(jiraIssue -> fieldMapping.getJiraIssueEpicTypeKPI153()
											.contains(jiraIssue.getTypeName()))
									.sorted(Comparator
											.comparing(epic -> epic.getReleaseVersions().get(0).getReleaseName())))
							.collect(Collectors.toList());
					KPIExcelUtility.populatePIPredictabilityExcelData(trendLineName, sortedEpicList, excelData);
				}
			}
		});

		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.PI_PREDICTABILITY.getColumns());

	}

	@Override
	public Double calculateKPIMetrics(Map<String, Object> stringObjectMap) {
		return null;
	}

	/**
	 * Fetches KPI data from the database
	 *
	 * @param leafNodeList
	 *            The list of leaf nodes
	 * @param startDate
	 *            The start date
	 * @param endDate
	 *            The end date
	 * @param kpiRequest
	 *            The KPI request
	 * @return The fetched KPI data
	 */
	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {

		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		Map<String, Object> resultListMap = new HashMap<>();
		List<String> basicProjectConfigIds = new ArrayList<>();
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
		Map<String, List<String>> projectWiseIssueTypeMap = new HashMap<>();

		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			basicProjectConfigIds.add(basicProjectConfigId.toString());
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
			if (Optional.ofNullable(fieldMapping.getJiraIssueEpicTypeKPI153()).isPresent()) {
				projectWiseIssueTypeMap.put(basicProjectConfigId.toString(), fieldMapping.getJiraIssueEpicTypeKPI153());
			}
		});
		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));
		mapOfFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(), projectWiseIssueTypeMap
				.get(leafNodeList.get(0).getProjectFilter().getBasicProjectConfigId().toString()));

		List<ReleaseWisePI> releaseWisePIList = jiraIssueRepository
				.findUniqueReleaseVersionByUniqueTypeName(mapOfFilters);

		Map<String, List<String>> projectWisePIList = new HashMap<>();

		Map<String, List<ReleaseWisePI>> projectWIseData = releaseWisePIList.stream()
				.collect(Collectors.groupingBy(ReleaseWisePI::getBasicProjectConfigId));

		projectWIseData.forEach((basicProjectConfigId, releaseWIseData) -> {
			Map<String, List<ReleaseWisePI>> versionWiseData = releaseWIseData.stream()
					.filter(releaseWisePI -> CollectionUtils.isNotEmpty(releaseWisePI.getReleaseName()))
					.collect(Collectors.groupingBy(releaseWisePI -> releaseWisePI.getReleaseName().get(0)));
			versionWiseData.forEach((version, piData) -> {
				if (CollectionUtils.isNotEmpty(piData)
						&& CollectionUtils.isNotEmpty(projectWiseIssueTypeMap.get(basicProjectConfigId))
						&& piData.stream().anyMatch(releaseWisePI -> projectWiseIssueTypeMap.get(basicProjectConfigId)
								.contains(releaseWisePI.getUniqueTypeName()))) {
					projectWisePIList.putIfAbsent(basicProjectConfigId, new ArrayList<>());
					projectWisePIList.computeIfPresent(basicProjectConfigId, (k, v) -> {
						Optional<ReleaseWisePI> epicPIData = piData.stream()
								.filter(releaseWisePI -> projectWiseIssueTypeMap.get(basicProjectConfigId)
										.contains(releaseWisePI.getUniqueTypeName()))
								.findFirst();
						epicPIData.ifPresent(releaseWisePI -> v.add(releaseWisePI.getReleaseName().get(0)));
						return v;
					});
				}
			});
		});

		projectWisePIList.forEach((basicProjectConfigId, piDataList) -> {
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
			mapOfProjectFilters.put(CommonConstant.RELEASE, CommonUtils.convertToPatternListForSubString(piDataList));
			uniqueProjectMap.put(basicProjectConfigId, mapOfProjectFilters);
		});

		if (MapUtils.isNotEmpty(uniqueProjectMap)) {
			List<JiraIssue> piWiseEpicList = jiraIssueRepository.findByRelease(mapOfFilters, uniqueProjectMap);
			resultListMap.put(EPIC_DATA, piWiseEpicList);
		}
		return resultListMap;
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiId) {
		return calculateKpiValueForDouble(valueList, kpiId);
	}

	@Getter
	@Setter
	public class PiWiseLatestEpicData {
		private String basicProjectConfigId;
		private String piName;
		private DateTime piEndDate;
		private List<JiraIssue> epicList = new ArrayList<>();
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping) {
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI153(), KPICode.PI_PREDICTABILITY.getKpiId());
	}

}
