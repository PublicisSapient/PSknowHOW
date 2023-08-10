package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.enums.Filters;
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

	private static ThreadLocal<Integer> threadLocalVariable = ThreadLocal.withInitial(() -> 0);

	@Override
	public String getQualifierType() {
		return KPICode.PI_PREDICTABILITY.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();

		treeAggregatorDetail.getMapOfListOfProjectNodes().forEach((k, v) -> {
			if (Filters.getFilter(k) == Filters.PROJECT) {
				projectWiseLeafNodeValue(kpiElement, mapTmp, v);
			}

		});

		log.debug("[PROJECT-WISE][{}]. Values of leaf node after KPI calculation {}", kpiRequest.getRequestTrackerId(),
				root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedMultipleValueGroup(root, nodeWiseKPIValue, KPICode.PI_PREDICTABILITY);
		List<DataCount> trendValues = getTrendValues(kpiRequest, nodeWiseKPIValue, KPICode.PI_PREDICTABILITY);
		kpiElement.setTrendValueList(trendValues);
		return kpiElement;
	}

	private void projectWiseLeafNodeValue(KpiElement kpiElement, Map<String, Node> mapTmp,
			List<Node> projectLeafNodeList) {

		Map<String, Object> resultMap = fetchKPIDataFromDb(projectLeafNodeList, null, null, null);

		List<JiraIssue> epicData = (List<JiraIssue>) resultMap.get("EpicData");

		Map<String, List<JiraIssue>> projectWiseEpicData = epicData.stream()
				.collect(Collectors.groupingBy(JiraIssue::getBasicProjectConfigId));

		List<KPIExcelData> excelData = new ArrayList<>();

		projectLeafNodeList.forEach(node -> {
			 String currentProjectId = node.getProjectFilter().getBasicProjectConfigId().toString();
			List<JiraIssue> epicList = projectWiseEpicData.get(currentProjectId);
			List<DataCount> dataCountList = new ArrayList<>();
			//jiraIssueList.stream().forEach(jiraIssue -> jiraIssue.
			Map<DateTime , ReleaseWiseLatestEpicData> piNameWiseEpicData = new HashMap<>();
			// project wise PI EPIC List<JiraIssue>
			if (CollectionUtils.isNotEmpty(epicList)) {
				epicList.stream().forEach(jiraIssue -> {
					if (CollectionUtils.isNotEmpty(jiraIssue.getReleaseVersions())
							&& jiraIssue.getReleaseVersions().get(0).getReleaseDate() != null) {
						piNameWiseEpicData.putIfAbsent(jiraIssue.getReleaseVersions().get(0).getReleaseDate(),
								new ReleaseWiseLatestEpicData());
						piNameWiseEpicData.computeIfPresent(jiraIssue.getReleaseVersions().get(0).getReleaseDate(),
								(k, v) -> {
									v.setPiName(jiraIssue.getReleaseVersions().get(0).getReleaseName());
									v.setPiEndDate(jiraIssue.getReleaseVersions().get(0).getReleaseDate());
									List<JiraIssue> piWiseEpicList = v.getEpicList();
									piWiseEpicList.add(jiraIssue);
									v.setEpicList(piWiseEpicList);
									return v;
								});
					}
				});

				Map<DateTime, ReleaseWiseLatestEpicData> sortedPINameWiseEpicData = piNameWiseEpicData.entrySet()
						.stream().sorted(Map.Entry.comparingByKey()).limit(5)
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
								(oldValue, newValue) -> oldValue, LinkedHashMap::new));

				String trendLineName = node.getProjectFilter().getName();
				String requestTrackerId = getRequestTrackerId();

				sortedPINameWiseEpicData.forEach((releaseDate, releaseWiseLatestEpicData) -> {
					String piName = releaseWiseLatestEpicData.getPiName();
					Double plannedValueSum = releaseWiseLatestEpicData.getEpicList().stream()
							.mapToDouble(JiraIssue::getBusinessValue).sum();
					Double achievedValueSum = releaseWiseLatestEpicData.getEpicList().stream()
							.mapToDouble(JiraIssue::getTimeCriticality).sum();

					List<DataValue> dataValueList = new ArrayList<>();
					DataCount dataCount = new DataCount();
					dataCount.setSProjectName(trendLineName);
					dataCount.setSSprintID(piName);
					dataCount.setSSprintName(piName);
					DataValue dataValue1 = new DataValue();
					dataValue1.setData(plannedValueSum.toString());
					Map<String, Object> hoverValueMap1 = new HashMap<>();
					dataValue1.setHoverValue(hoverValueMap1);
					dataValue1.setLineType("solid");
					dataValue1.setName("Achieved Value");
					dataValue1.setValue(achievedValueSum);

					DataValue dataValue2 = new DataValue();
					Map<String, Object> hoverValueMap2 = new HashMap<>();
					dataValue2.setData(plannedValueSum.toString());
					dataValue2.setHoverValue(hoverValueMap2);
					dataValue2.setLineType("dotted");
					dataValue2.setName("Planned Value");
					dataValue2.setValue(plannedValueSum);
					dataValueList.add(dataValue1);
					dataValueList.add(dataValue2);
					dataCount.setDataValue(dataValueList);
					dataCountList.add(dataCount);

				});
				mapTmp.get(node.getId()).setValue(dataCountList);
				if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
					KPIExcelUtility.populatePIPredictabilityExcelData(trendLineName, epicList, excelData);
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

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {

		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		Map<String, Object> resultListMap = new HashMap<>();
		List<String> basicProjectConfigIds = new ArrayList<>();
		List<ObjectId> basicProjectConfigObjectsIds = new ArrayList<>();
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();

		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
			basicProjectConfigIds.add(basicProjectConfigId.toString());
			basicProjectConfigObjectsIds.add(basicProjectConfigId);
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
			List<String> issueTypeList = new ArrayList<>();
			String epicName = fieldMapping.getJiraIssueEpicTypeKPI153();
			/*if (Optional.ofNullable(fieldMapping.getJiradefecttype()).isPresent()) {
				issueTypeList.add(fieldMapping.getJiraIssueEpicTypeKPI153());
				mapOfProjectFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
						CommonUtils.convertToPatternList(issueTypeList));
				uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);
			}*/
		});
		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));

		List<ReleaseWisePI> releaseWisePIList = jiraIssueRepository
				.findUniqueReleaseVersionByUniqueTypeName(mapOfFilters, uniqueProjectMap, "Epic");

		List<String> piList = new ArrayList<>();
		releaseWisePIList.stream().forEach(releaseWisePI -> {
			if (releaseWisePI.isConsiderVersionAsPI()
					&& CollectionUtils.isNotEmpty(releaseWisePI.getReleaseVersion())) {
				piList.add(releaseWisePI.getReleaseVersion().get(0));
			}
		});

		Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
		mapOfProjectFilters.put(CommonConstant.RELEASE, CommonUtils.convertToPatternListForSubString(piList));
		uniqueProjectMap.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(), mapOfProjectFilters);
		List<JiraIssue> piWiseEpicList = jiraIssueRepository.findByRelease(mapOfFilters, uniqueProjectMap);
		resultListMap.put("EpicData", piWiseEpicList);
		return resultListMap;
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiId) {
		return calculateKpiValueForDouble(valueList, kpiId);
	}

	@Getter
	@Setter
	public class ReleaseWiseLatestEpicData {
		private String basicProjectConfigId;
		private String piName;
		private DateTime piEndDate;
		private List<JiraIssue> epicList = new ArrayList<>();
	}

}
