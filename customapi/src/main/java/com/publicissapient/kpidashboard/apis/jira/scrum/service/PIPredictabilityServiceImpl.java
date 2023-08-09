package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataValue;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.ReleaseWisePI;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PIPredictabilityServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private ConfigHelperService configHelperService;

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
		calculateAggregatedValueForMultipleLine(root, nodeWiseKPIValue, KPICode.PI_PREDICTABILITY);
		List<DataCount> trendValues = getTrendValues(kpiRequest, nodeWiseKPIValue, KPICode.PI_PREDICTABILITY);
		kpiElement.setTrendValueList(trendValues);
		return kpiElement;
	}

	private void projectWiseLeafNodeValue(KpiElement kpiElement, Map<String, Node> mapTmp,
			List<Node> projectLeafNodeList) {

		Map<String, Object> resultMap = fetchKPIDataFromDb(projectLeafNodeList, null, null, null);
		// find resultMap.PIData all projects

		List<JiraIssue> epicData = (List<JiraIssue>) resultMap.get("EpicData");

		Map<String, List<JiraIssue>> projectWiseEpicData = epicData.stream()
				.collect(Collectors.groupingBy(JiraIssue::getBasicProjectConfigId));


		List<KPIExcelData> excelData = new ArrayList<>();

		List<DataCount> dataCountList = new ArrayList<>();
		projectLeafNodeList.forEach(node -> {
			 String currentProjectId = node.getProjectFilter().getBasicProjectConfigId().toString();
			List<JiraIssue> jiraIssueList = projectWiseEpicData.get(currentProjectId);

			//jiraIssueList.stream().forEach(jiraIssue -> jiraIssue.

			// project wise PI EPIC List<JiraIssue>
					jiraIssueList.stream().collect(Collectors.groupingBy(JiraIssue::getReleaseVersions));
			// group by release Version list
			String trendLineName = node.getProjectFilter().getName();
			for (int i = 0; i < 3; i++) {
				List<DataValue> dataValueList = new ArrayList<>();
				DataCount dataCount = new DataCount();
				dataCount.setSProjectName(trendLineName);
				dataCount.setSSprintID("sprintID " + i);
				dataCount.setSSprintName("sprintName " + i);
				DataValue dataValue1 = new DataValue();
				dataValue1.setData("10");
				Map<String, Object> hoverValueMap1 = new HashMap<>();
				dataValue1.setHoverValue(hoverValueMap1);
				dataValue1.setLineType("simple");
				// aggregatedDataValue.setName();
				Double aValue = 20.0d;
				Double pValue = 30.0d;
				dataValue1.setValue(aValue);

				DataValue dataValue2 = new DataValue();
				Map<String, Object> hoverValueMap2 = new HashMap<>();
				dataValue2.setData("20");
				dataValue2.setHoverValue(hoverValueMap2);
				dataValue2.setLineType("dotted");
				// aggregatedDataValue.setName();
				dataValue2.setValue(pValue);
				dataValueList.add(dataValue1);
				dataValueList.add(dataValue2);
				dataCount.setDataValue(dataValueList);
				dataCountList.add(dataCount);
			}
			mapTmp.get(node.getId()).setValue(dataCountList);
			/*
			 * if
			 * (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase()
			 * )) { KPIExcelUtility.populateCODExcelData(projectName, epicList, excelData);
			 * }
			 */

		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.COST_OF_DELAY.getColumns());

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
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();

		leafNodeList.forEach(leaf -> {
			ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
			basicProjectConfigIds.add(basicProjectConfigId.toString());
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
			if (Optional.ofNullable(fieldMapping.getJiradefecttype()).isPresent()) {
				mapOfProjectFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
						CommonUtils.convertToPatternList(fieldMapping.getTicketCountIssueType()));
				uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);
			}
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
		// release Version List List<String> releaseList/PI Name ( for X axis)
		// find epic is linked to above releaseList
		// List<JiraIssue> // all epic data which is tagged to PI

		return null;
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiId) {
		return calculateKpiValueForDouble(valueList, kpiId);
	}

}
