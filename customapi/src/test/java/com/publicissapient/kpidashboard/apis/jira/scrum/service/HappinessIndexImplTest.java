package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.jira.HappinessKpiData;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.UserRatingData;
import com.publicissapient.kpidashboard.common.repository.jira.HappinessKpiDataRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Array;
import java.util.*;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HappinessIndexImplTest {

    public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
    public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private HappinessKpiDataRepository happinessKpiDataRepository;


    @Mock
    CacheService cacheService;

    @Mock
    ConfigHelperService configHelperService;

    private KpiRequest kpiRequest;

    @InjectMocks
    private HappinessIndexImpl happinessIndexImpl;
    private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();

    private static final String SPRINTSDETAILS = "sprints";
    private static final String HAPPINESS_INDEX_DETAILS = "heppinessIndexDetails";

    @Before
    public void setup(){
        KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
        kpiRequest = kpiRequestFactory.findKpiRequest(KPICode.STORY_COUNT.getKpiId());
        kpiRequest.setLabel("PROJECT");

        AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
                .newInstance();
        accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();


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
//        when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);

    }


    @Test
    public void testQualifierType() {
        String kpiName = KPICode.HAPPINESS_INDEX_RATE.name();
        String type = happinessIndexImpl.getQualifierType();
        assertEquals("KPI NAME : ", type, kpiName);
    }

    @Test
    public void testCalculateKPIMetrics() {
        Map<String, Object> subCategoryMap = new HashMap<>();
        Double storyCount = happinessIndexImpl.calculateKPIMetrics(subCategoryMap);
        assertThat("Story List : ", storyCount, equalTo(0.0));
    }

    @Test
    public void getKpiDataEmptyTest() throws ApplicationException {
        TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
                accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);

        Mockito.when(sprintRepository.findBySprintIDIn(Mockito.any())).thenReturn(new ArrayList<>());
        Mockito.when(happinessKpiDataRepository.findBySprintIDIn(Mockito.any())).thenReturn(new ArrayList<>());
        String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";

        when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
                .thenReturn(kpiRequestTrackerId);
        KpiElement kpiElement = happinessIndexImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
                treeAggregatorDetail);
        List<DataCount> dataCountList = (List<DataCount>) kpiElement.getTrendValueList();

        assertEquals("Story Count : ", dataCountList.size(), 1);



    }

    @Test
    public void getKpiDataSuccessTest() throws ApplicationException {
        TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
                accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);

        SprintDetails sprintDetails = new SprintDetails();
        sprintDetails.setSprintID("38294_Scrum Project_6335363749794a18e8a4479b");
        sprintDetails.setBasicProjectConfigId(new ObjectId("6335363749794a18e8a4479b"));

        HappinessKpiData happinessKpiData = new HappinessKpiData();
        happinessKpiData.setSprintID("38294_Scrum Project_6335363749794a18e8a4479b");
        happinessKpiData.setBasicProjectConfigId(new ObjectId("6335363749794a18e8a4479b"));
        happinessKpiData.setUserRatingList(Arrays.asList(new UserRatingData(2,"uid","uname")));


        Mockito.when(sprintRepository.findBySprintIDIn(Mockito.any())).thenReturn(Arrays.asList(sprintDetails));
        Mockito.when(happinessKpiDataRepository.findBySprintIDIn(Mockito.any())).thenReturn(Arrays.asList(happinessKpiData));
        String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";

        when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
                .thenReturn(kpiRequestTrackerId);
        KpiElement kpiElement = happinessIndexImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
                treeAggregatorDetail);
        List<DataCount> dataCountList = (List<DataCount>) kpiElement.getTrendValueList();

        assertEquals("Story Count : ", dataCountList.size(), 1);



    }

}