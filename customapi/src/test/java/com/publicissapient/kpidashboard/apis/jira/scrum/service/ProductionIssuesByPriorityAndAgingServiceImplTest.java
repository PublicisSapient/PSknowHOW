package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ProductionIssuesByPriorityAndAgingServiceImplTest {
	private static final String P1 = "p1,P1 - Blocker, blocker, 1, 0, p0, Urgent";
	private static final String P2 = "p2, critical, P2 - Critical, 2, High";
	private static final String P3 = "p3, P3 - Major, major, 3, Medium";
	private static final String P4 = "p4, P4 - Minor, minor, 4, Low";

	private static final String RANGE_TICKET_LIST = "rangeTickets";
	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	List<JiraIssue> totalIssueBacklogList = new ArrayList<>();
	@Mock
	JiraIssueRepository jiraIssueRepository;
	@Mock
	CacheService cacheService;
	@Mock
	ConfigHelperService configHelperService;
	@Mock
	KpiHelperService kpiHelperService;
	@InjectMocks
	ProductionIssuesByPriorityAndAgingServiceImpl productionIssuesByPriorityAndAgingService;
	@Mock
	ProjectBasicConfigRepository projectConfigRepository;
	@Mock
	FieldMappingRepository fieldMappingRepository;
	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
	private List<FieldMapping> fieldMappingList = new ArrayList<>();
	private Map<String, List<DataCount>> trendValueMap = new HashMap<>();
	private List<DataCount> trendValues = new ArrayList<>();
	@Mock
	private CustomApiConfig customApiConfig;

	@Mock
	private CommonService commonService;

	private KpiRequest kpiRequest;
	private KpiElement kpiElement;
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();

	@Before
	public void setup() {
		ProjectBasicConfig projectConfig = new ProjectBasicConfig();
		projectConfig.setId(new ObjectId("6335363749794a18e8a4479b"));
		projectConfig.setProjectName("Scrum Project");
		projectConfigMap.put(projectConfig.getProjectName(), projectConfig);

		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		FieldMapping fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		fieldMappingMap.put(fieldMapping.getBasicProjectConfigId(), fieldMapping);
		configHelperService.setProjectConfigMap(projectConfigMap);
		configHelperService.setFieldMappingMap(fieldMappingMap);
		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		setTreadValuesDataCount();
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi127");
		kpiRequest.setLabel("PROJECT");
		kpiElement = kpiRequest.getKpiList().get(0);
		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();
		totalIssueBacklogList = JiraIssueDataFactory.newInstance().getJiraIssues();
		when(jiraIssueRepository.findIssuesByDateAndTypeAndStatus(anyMap(),anyMap(),anyString(),anyString(),anyString(),anyString(),anyBoolean())).thenReturn(totalIssueBacklogList);

	}

	@Test
	public void testGetProductionDefectsAgingByPriority() throws ApplicationException {

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		List<String> xAxisRange = new ArrayList<>(Arrays.asList("0-1", "1-3", "3-6", "6-12", ">12"));
		when(customApiConfig.getTotalDefectCountAgingXAxisRange()).thenReturn(xAxisRange);

		when(customApiConfig.getpriorityP1()).thenReturn(P1);
		when(customApiConfig.getpriorityP2()).thenReturn(P2);
		when(customApiConfig.getpriorityP3()).thenReturn(P3);
		when(customApiConfig.getpriorityP4()).thenReturn(P4);
		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(productionIssuesByPriorityAndAgingService.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);
		when(commonService.sortTrendValueMap(anyMap())).thenReturn(trendValueMap);

		try {
			KpiElement kpiElement = productionIssuesByPriorityAndAgingService.getKpiData(kpiRequest,
					kpiRequest.getKpiList().get(0), treeAggregatorDetail);

			((List<DataCountGroup>) kpiElement.getTrendValueList()).forEach(dc -> {

				String priority = dc.getFilter();
				switch (priority) {
				case "P1":
					assertThat("Production Defect Priority Count Value :", dc.getValue().size(), equalTo(1));
					break;
				case "P2":
					assertThat("Production Defect  Priority Count Value :", dc.getValue().size(), equalTo(1));
					break;
				case "P3":
					assertThat("Production Defect  Priority Count Value :", dc.getValue().size(), equalTo(1));
					break;
				case "P4":
					assertThat("Production Defect  Priority Count Value :", dc.getValue().size(), equalTo(1));
					break;
				case "MISC":
					assertThat("Production Defect  Priority Count Value :", dc.getValue().size(), equalTo(1));
					break;

				default:
					break;
				}

			});

		} catch (ApplicationException applicationException) {

		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFetchKPIDataFromDb() throws ApplicationException {
		List<Node> leafNodeList = new ArrayList<>();
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
			if (Filters.getFilter(k) == Filters.SPRINT) {
				leafNodeList.addAll(v);
			}
		});

		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		Map<String, Object> defectDataListMap = productionIssuesByPriorityAndAgingService
				.fetchKPIDataFromDb(leafNodeList, null, null, kpiRequest);

		assertThat("Total Defects issue list :", ((List<JiraIssue>) defectDataListMap.get(RANGE_TICKET_LIST)).size(),
				equalTo(0));
	}

	@Test
	public void testCalculateKPIMetrics() {
		assertThat("Total Aging value :", productionIssuesByPriorityAndAgingService.calculateKPIMetrics(null),
				equalTo(0L));
	}

	@Test
	public void testGetQualifierType() {
		assertThat("Kpi Name :", productionIssuesByPriorityAndAgingService.getQualifierType(),
				equalTo("PRODUCTION_ISSUES_BY_PRIORITY_AND_AGING"));
	}

	private void setTreadValuesDataCount() {
		List<DataCount> dataCountList = new ArrayList<>();
		DataCount dataCountValue = new DataCount();
		dataCountValue.setData(String.valueOf(5L));
		dataCountValue.setValue(5L);
		dataCountList.add(dataCountValue);
		DataCount dataCount = setDataCountValues("Scrum Project", "3", "4", dataCountList);
		trendValues.add(dataCount);
		trendValueMap.put(P1, trendValues);
		trendValueMap.put(P2, trendValues);
		trendValueMap.put(P3, trendValues);
		trendValueMap.put(P4, trendValues);
	}

	private DataCount setDataCountValues(String data, String maturity, Object maturityValue, Object value) {
		DataCount dataCount = new DataCount();
		dataCount.setData(data);
		dataCount.setMaturity(maturity);
		dataCount.setMaturityValue(maturityValue);
		dataCount.setValue(value);
		return dataCount;
	}

}
