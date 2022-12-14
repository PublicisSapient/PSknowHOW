package com.publicissapient.kpidashboard.apis.jenkins.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.CommonService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.BuildDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.Tool;
import com.publicissapient.kpidashboard.common.model.generic.ProcessorItem;
import com.publicissapient.kpidashboard.common.repository.application.BuildRepository;

@RunWith(MockitoJUnitRunner.class)
public class ChangeFailureRateServiceImplTest {

	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	private Map<String, Object> filterLevelMap;
	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
	private List<FieldMapping> fieldMappingList = new ArrayList<>();
	private Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	private Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	private Map<ObjectId, Map<String, List<Tool>>> toolMap = new HashMap<>();
	private Map<String, List<Tool>> toolGroup = new HashMap<>();
	private List<Build> buildList = new ArrayList<>();
	private Map<String, List<String>> maturityRangeMap = new HashMap<>();
	private List<DataCount> trendValues = new ArrayList<>();
	private Map<String, List<DataCount>> trendValueMap = new LinkedHashMap<>();

	private static Tool tool1;
	private static Tool tool2;

	@Mock
	BuildRepository buildRepository;

	@Mock
	CacheService cacheService;

	@Mock
	ConfigHelperService configHelperService;

	@Mock
	FilterHelperService filterHelperService;

	@Mock
	CustomApiConfig customApiConfig;

	@InjectMocks
	ChangeFailureRateServiceImpl changeFailureRateService;

	@Mock
	private CommonService commonService;

	private KpiRequest kpiRequest;
	private KpiElement kpiElement;

	List<Tool> toolList;

	@Before
	public void setup() {

		setToolMap();

		setTreadValuesDataCount();

		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi116");
		kpiRequest.setLabel("PROJECT");
		kpiElement = kpiRequest.getKpiList().get(0);

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();

		BuildDataFactory buildDataFactory = BuildDataFactory.newInstance();
		buildList = buildDataFactory.getbuildDataList();

		projectConfigList.forEach(projectConfig -> {
			projectConfigMap.put(projectConfig.getProjectName(), projectConfig);
		});
		fieldMappingList.forEach(fieldMapping -> {
			fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		});

		configHelperService.setProjectConfigMap(projectConfigMap);
		configHelperService.setFieldMappingMap(fieldMappingMap);

	}

	private void setTreadValuesDataCount() {
		DataCount dataCount = setDataCountValues("KnowHow", "3", "4", new DataCount());
		trendValues.add(dataCount);
		trendValueMap.put("OverAll", trendValues);
		trendValueMap.put("UI_Build -> KnowHow", trendValues);
		trendValueMap.put("API_Build -> KnowHow", trendValues);
	}

	private DataCount setDataCountValues(String data, String maturity, Object maturityValue, Object value) {
		DataCount dataCount = new DataCount();
		dataCount.setData(data);
		dataCount.setMaturity(maturity);
		dataCount.setMaturityValue(maturityValue);
		dataCount.setValue(value);
		return dataCount;
	}

	private void setToolMap() {
		toolList = new ArrayList<>();

		ProcessorItem collectorItemFirst = new ProcessorItem();
		collectorItemFirst.setId(new ObjectId("633552f909c7635933ad192c"));
		collectorItemFirst.setDesc("UI_BUILD");

		ProcessorItem collectorItemSecond = new ProcessorItem();
		collectorItemSecond.setId(new ObjectId("633552f809c7635933ad1924"));
		collectorItemSecond.setDesc("API_BUILD");

		List<ProcessorItem> collectorItemFirstList = new ArrayList<>();
		collectorItemFirstList.add(collectorItemFirst);

		List<ProcessorItem> collectorItemSecondList = new ArrayList<>();
		collectorItemSecondList.add(collectorItemSecond);

		tool1 = createTool("UI_BUILD", "url1", collectorItemFirstList);
		tool2 = createTool("API_BUILD", "url2", collectorItemSecondList);

		toolList.add(tool1);
		toolList.add(tool2);

		toolGroup.put(Constant.TOOL_JENKINS, toolList);

		toolMap.put(new ObjectId("6335363749794a18e8a4479b"), toolGroup);
	}

	private Tool createTool(String toolType, String url, List<ProcessorItem> collectorItemList) {
		Tool tool = new Tool();
		tool.setTool(toolType);
		tool.setUrl(url);
		tool.setProcessorItemList(collectorItemList);
		return tool;
	}

	@Test
	public void testGetChangeFailureRate() throws Exception {
		setToolMap();

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);

		when(buildRepository.findBuildList(any(), any(), any(), any())).thenReturn(buildList);
		when(configHelperService.getToolItemMap()).thenReturn(toolMap);

		when(customApiConfig.getJenkinsWeekCount()).thenReturn(5);

		when(commonService.sortTrendValueMap(anyMap())).thenReturn(trendValueMap);

		String kpiRequestTrackerId = "Excel-JENKINS-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JENKINS.name()))
				.thenReturn(kpiRequestTrackerId);
		when(buildRepository.findBuildList(any(), any(), any(), any())).thenReturn(buildList);
		when(configHelperService.getToolItemMap()).thenReturn(toolMap);

		try {

			KpiElement kpiElement = changeFailureRateService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			assertThat("CHANGE-FAILURE-RATE :", ((List<DataCount>) kpiElement.getTrendValueList()).size(), equalTo(3));
		} catch (Exception e) {
		}

	}

	@Test
	public void getQualifierType() {
		String result = changeFailureRateService.getQualifierType();
		assertEquals(result, KPICode.CHANGE_FAILURE_RATE.name());
	}

}
