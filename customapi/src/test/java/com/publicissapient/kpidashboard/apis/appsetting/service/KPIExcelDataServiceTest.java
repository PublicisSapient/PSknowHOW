/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.apis.appsetting.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.bitbucket.service.BitBucketServiceKanbanR;
import com.publicissapient.kpidashboard.apis.bitbucket.service.BitBucketServiceR;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.jenkins.service.JenkinsServiceKanbanR;
import com.publicissapient.kpidashboard.apis.jenkins.service.JenkinsServiceR;
import com.publicissapient.kpidashboard.apis.jira.service.JiraServiceKanbanR;
import com.publicissapient.kpidashboard.apis.jira.service.JiraServiceR;
import com.publicissapient.kpidashboard.apis.kpicolumnconfig.service.KpiColumnConfigService;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KPIExcelValidationDataResponse;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.sonar.service.SonarServiceKanbanR;
import com.publicissapient.kpidashboard.apis.sonar.service.SonarServiceR;
import com.publicissapient.kpidashboard.apis.zephyr.service.ZephyrService;
import com.publicissapient.kpidashboard.apis.zephyr.service.ZephyrServiceKanban;
import com.publicissapient.kpidashboard.common.model.application.KpiColumnConfigDTO;
import com.publicissapient.kpidashboard.common.model.application.KpiColumnDetails;
import com.publicissapient.kpidashboard.common.model.application.KpiMaster;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ValidationData;
import com.publicissapient.kpidashboard.common.repository.application.KpiMasterRepository;

@RunWith(MockitoJUnitRunner.class)
public class KPIExcelDataServiceTest {

	@InjectMocks
	private KPIExcelDataService kpiExcelDataService;

	@Mock
	private CacheService cacheService;

	@Mock
	KpiColumnConfigService kpiColumnConfigService;

	@Mock
	private JiraServiceR jiraServiceR;

	@Mock
	private JenkinsServiceR jenkinsServiceR;

	@Mock
	private SonarServiceR sonarServiceR;

	@Mock
	private BitBucketServiceR bitbucketServiceR;

	@Mock
	private ZephyrService zephyrService;

	@Mock
	private JiraServiceKanbanR jiraServiceKanbanR;

	@Mock
	private SonarServiceKanbanR sonarServiceKanbanR;

	@Mock
	private ZephyrServiceKanban zephyrServiceKanban;

	@Mock
	private BitBucketServiceKanbanR bitBucketServiceKanbanR;

	@Mock
	private JenkinsServiceKanbanR jenkinsServiceKanbanR;

	@Mock
	private ConfigHelperService configHelperService;

	@Mock
	private KpiMasterRepository kpiMasterRepository;
	private Map<String, Double> projectData = new HashMap<>();
	private Map<String, Double> sprintData = new HashMap<>();
	private List<KpiElement> kpiJiraElementList = new ArrayList<>();
	private KpiElement validationJiraKpiElement = new KpiElement();
	private List<KpiElement> kpiJenkinsElementList = new ArrayList<>();
	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();

	private int level;
	private List<String> idList = new ArrayList<>();
	// Filter data to be sent in response e.g. PROJECT, SPRINT
	private List<String> acceptedFilterList = new ArrayList<>();

	@Before
	public void setUp() {

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();

		createKpiElementList();

		List<KpiMaster> kpiMasterList = new ArrayList<>();

		List<KPICode> kpiSourceList = Stream.of(KPICode.values()).filter(kpi -> kpi != KPICode.INVALID)
				.collect(Collectors.toList());

		kpiSourceList.forEach(kpi -> {
			KpiMaster kpiMaster = new KpiMaster();
			kpiMaster.setKpiId(kpi.getKpiId());
			kpiMaster.setKpiName(kpi.name());
			kpiMaster.setKpiCategory("Productivity");
			kpiMaster.setKpiSource(kpi.getSource());
			kpiMaster.setKanban(kpi.getSource().contains("Kanban"));
			kpiMasterList.add(kpiMaster);
		});
		when(configHelperService.loadKpiMaster()).thenReturn(kpiMasterList);
	}

	private void createKpiElementList() {

		Map<Pair<String, String>, Node> nodeWiseKPIValue1 = new HashMap<>();

		List<Node> nodeList = accountHierarchyDataList.get(0).getNode();
		Node node1 = setNode("id", 2d, "hierarchyLevelOne");
		Node node2 = setNode("id", 2d, "hierarchyLevelTwo");
		Node node5 = setNode("id", 12d, "hierarchyLevelThree");
		Node node7 = setNode("id", 4d, "project");
		Node node9 = setNode("id", 4d, "sprint");

		nodeWiseKPIValue1.put(Pair.of("Alpha_Test_CompId", node1.getGroupName()), node1);
		nodeWiseKPIValue1.put(Pair.of("Beta_Test_CompId", node2.getGroupName()), node2);
		nodeWiseKPIValue1.put(Pair.of("Alpha_Project1_CompId", node5.getGroupName()), node5);
		nodeWiseKPIValue1.put(Pair.of("Alpha_Project1_Sprint1_CompId", node7.getGroupName()), node7);
		nodeWiseKPIValue1.put(Pair.of("Alpha_Project1_Sprint1_Release1_CompId", node9.getGroupName()), node9);

		Map<String, ValidationData> kpiValidationDataMap = new HashMap<>();
		ValidationData validationData = new ValidationData();
		validationData.setDefectKeyList(Arrays.asList("Alpha_Project1_Sprint1_name_Defect1"));
		validationData.setStoryKeyList(Arrays.asList("Alpha_Project1_Sprint1_name_Story1"));
		kpiValidationDataMap.put("Alpha_Project1_Sprint1_name", validationData);

		KpiElement kpiElement1 = setKpiElement(KPICode.DEFECT_INJECTION_RATE.getKpiId(),
				KPICode.DEFECT_INJECTION_RATE.name(), 2d, nodeWiseKPIValue1);
		kpiElement1.setMapOfSprintAndData(kpiValidationDataMap);

		validationJiraKpiElement = kpiElement1;

		KpiElement kpiElement3 = setKpiElement(KPICode.CODE_BUILD_TIME.getKpiId(), KPICode.CODE_BUILD_TIME.name(), 27d,
				nodeWiseKPIValue1);

		KpiElement kpiElement4 = setKpiElement(KPICode.DEFECT_COUNT_BY_PRIORITY.getKpiId(),
				KPICode.DEFECT_COUNT_BY_PRIORITY.name(), 27d, null);

		Map<Pair<String, String>, Node> nodeWiseKPIValue2 = new HashMap<>();
		Node node3 = setNode("id", 12d, "hierarchyLevelOne");
		Node node4 = setNode("id", 13d, "hierarchyLevelTwo");
		Node node6 = setNode("id", 0d, "project");
		Node node8 = setNode("id", 3d, "sprint");

		nodeWiseKPIValue2.put(Pair.of("Alpha_Test_CompId", node3.getGroupName()), node3);
		nodeWiseKPIValue2.put(Pair.of("Beta_Test_CompId", node4.getGroupName()), node4);
		nodeWiseKPIValue2.put(Pair.of("Alpha_Project1_CompId", node6.getGroupName()), node6);
		nodeWiseKPIValue2.put(Pair.of("Alpha_Project1_Sprint1_CompId", node8.getGroupName()), node8);

		KpiElement kpiElement2 = setKpiElement(KPICode.DEFECT_REJECTION_RATE.getKpiId(),
				KPICode.DEFECT_REJECTION_RATE.name(), 5d, nodeWiseKPIValue2);

		kpiJiraElementList.add(kpiElement1);
		kpiJiraElementList.add(kpiElement2);
		kpiJiraElementList.add(kpiElement4);

		kpiJenkinsElementList.add(kpiElement3);
	}

	private Node setNode(String nodeId, Object value, String groupName) {

		Node node = new Node();
		node.setId(nodeId);
		node.setValue(value);
		node.setGroupName(groupName);

		return node;
	}

	private KpiElement setKpiElement(String kpiId, String kpiName, Object value,
			Map<Pair<String, String>, Node> nodeWiseKPIValue) {

		KpiElement kpiElement = new KpiElement();

		kpiElement.setKpiId(kpiId);
		kpiElement.setKpiName(kpiName);
		kpiElement.setValue(value);
		kpiElement.setNodeWiseKPIValue(nodeWiseKPIValue);

		return kpiElement;
	}

	@After
	public void after() {
	}

	@Test
	public void testProcessForKPIData_Kanban_KPIExcelValidation() throws Exception {

		level = 1;
		idList.add("TPRO_TPRO");
		idList.add("THPRO_THPRO");

		acceptedFilterList.add(Filters.PROJECT.name());

		List<KpiElement> totalKpiElementList = new ArrayList<>();
		totalKpiElementList.add(validationJiraKpiElement);

		KpiColumnConfigDTO kpiColumnConfigDTO = getKpiColumnConfigDTO("kpi48", new ObjectId("65118da7965fbb0d14bce23c"));
		KpiRequest kpiRequest = createKpiRequestForSprint("kpi48", "", "Abcd");
		when(kpiColumnConfigService.getByKpiColumnConfig(Mockito.any(), Mockito.any())).thenReturn(kpiColumnConfigDTO);
		when(jiraServiceKanbanR.process(Mockito.any())).thenReturn(totalKpiElementList);
		kpiExcelDataService.process("kpi48", level, idList, acceptedFilterList, kpiRequest, true, false);

		KpiColumnConfigDTO kpiColumnConfigDTO1 = getKpiColumnConfigDTO("kpi61", new ObjectId("65118da7965fbb0d14bce23c"));
		createKpiRequestForRelease("kpi61", "", "release");
		when(kpiColumnConfigService.getByKpiColumnConfig(Mockito.any(), Mockito.any())).thenReturn(kpiColumnConfigDTO1);
		when(sonarServiceKanbanR.process(Mockito.any())).thenReturn(totalKpiElementList);
		kpiExcelDataService.process("kpi61", level, idList, acceptedFilterList, kpiRequest, true, false);

		Assert.assertNotNull(
				kpiExcelDataService.process("kpi61", level, idList, acceptedFilterList, kpiRequest, true, false));

		KpiColumnConfigDTO kpiColumnConfigDTO2 = getKpiColumnConfigDTO("kpi63", new ObjectId("65118da7965fbb0d14bce23c"));
		createKpiRequest("kpi63", "", "project");
		when(kpiColumnConfigService.getByKpiColumnConfig(Mockito.any(), Mockito.any())).thenReturn(kpiColumnConfigDTO2);
		when(zephyrServiceKanban.process(Mockito.any())).thenReturn(totalKpiElementList);
		kpiExcelDataService.process("kpi63", level, idList, acceptedFilterList, kpiRequest, true, false);

		Assert.assertNotNull(
				kpiExcelDataService.process("kpi63", level, idList, acceptedFilterList, kpiRequest, true, false));

		KpiColumnConfigDTO kpiColumnConfigDTO3 = getKpiColumnConfigDTO("kpi65", new ObjectId("65118da7965fbb0d14bce23c"));
		createKpiRequest("kpi65", "", "project");
		when(kpiColumnConfigService.getByKpiColumnConfig(Mockito.any(), Mockito.any())).thenReturn(kpiColumnConfigDTO3);
		when(bitBucketServiceKanbanR.process(Mockito.any())).thenReturn(totalKpiElementList);
		kpiExcelDataService.process("kpi65", level, idList, acceptedFilterList, kpiRequest, true, false);
		Assert.assertNotNull(
				kpiExcelDataService.process("kpi65", level, idList, acceptedFilterList, kpiRequest, true, false));

		KpiColumnConfigDTO kpiColumnConfigDTO4 = getKpiColumnConfigDTO("kpi66", new ObjectId("65118da7965fbb0d14bce23c"));
		createKpiRequest("kpi66", "", "project");
		when(kpiColumnConfigService.getByKpiColumnConfig(Mockito.any(), Mockito.any())).thenReturn(kpiColumnConfigDTO4);
		when(jenkinsServiceKanbanR.process(Mockito.any())).thenReturn(totalKpiElementList);
		kpiExcelDataService.process("kpi66", level, idList, acceptedFilterList, kpiRequest, true, false);

		Assert.assertNotNull(
				kpiExcelDataService.process("kpi48", level, idList, acceptedFilterList, kpiRequest, true, false));
	}

	@Test
	public void testProcessForKPIData_Kanban_KPIExcelValidation2() throws Exception {

		level = 1;
		idList.add("TPRO_TPRO");
		idList.add("THPRO_THPRO");

		acceptedFilterList.add(Filters.PROJECT.name());

		List<KpiElement> totalKpiElementList = new ArrayList<>();
		totalKpiElementList.add(validationJiraKpiElement);

		KpiColumnConfigDTO kpiColumnConfigDTO = new KpiColumnConfigDTO();
		kpiColumnConfigDTO.setBasicProjectConfigId(null);
		kpiColumnConfigDTO.setSaveFlag(false);
		kpiColumnConfigDTO.setKpiId("kpi48");
		KpiRequest kpiRequest = createKpiRequestForSprint("kpi48", "", "sprint");

		List<ObjectId> objectId = new ArrayList<>();
		objectId.add(new ObjectId("65118da7965fbb0d14bce23c"));

		ProjectBasicConfig projectBasicConfig = new ProjectBasicConfig();
		projectBasicConfig.setId(new ObjectId("65118da7965fbb0d14bce23c"));
		projectBasicConfig.setProjectNodeId("projectId");
		when(configHelperService.getProjectHierarchyProjectConfigMap(anyList())).thenReturn(objectId);
		when(configHelperService.getProjectConfig(anyString())).thenReturn(projectBasicConfig);
		when(kpiColumnConfigService.getByKpiColumnConfig(Mockito.any(), Mockito.any())).thenReturn(kpiColumnConfigDTO);
		when(jiraServiceKanbanR.process(Mockito.any())).thenReturn(totalKpiElementList);
		kpiExcelDataService.process("kpi48", level, idList, acceptedFilterList, kpiRequest, true, false);

		Assert.assertNotNull(
				kpiExcelDataService.process("kpi48", level, idList, acceptedFilterList, kpiRequest, true, false));
	}

	@Test
	public void testProcessForValidationData() throws Exception {

		level = 1;
		idList.add("THPRO_THPRO");
		acceptedFilterList.add(Filters.PROJECT.name());

		List<ObjectId> objectId = new ArrayList<>();
		objectId.add(new ObjectId("65118da7965fbb0d14bce23c"));

		ProjectBasicConfig projectBasicConfig = new ProjectBasicConfig();
		projectBasicConfig.setId(new ObjectId("65118da7965fbb0d14bce23c"));
		projectBasicConfig.setProjectNodeId("projectId");
		when(configHelperService.getProjectHierarchyProjectConfigMap(anyList())).thenReturn(objectId);
		when(configHelperService.getProjectConfig(anyString())).thenReturn(projectBasicConfig);
		when(configHelperService.getProjectNodeIdWiseProjectConfig(anyString())).thenReturn(projectBasicConfig);

		List<KpiElement> validationKpiElementList = new ArrayList<>();
		validationKpiElementList.add(validationJiraKpiElement);

		when(jiraServiceR.process(Mockito.any())).thenReturn(validationKpiElementList);
		KpiRequest kpiRequest = createKpiRequestForRelease("kpi14", "", "release");
		KpiColumnConfigDTO kpiColumnConfigDTO = getKpiColumnConfigDTO("kpi14", new ObjectId("65118da7965fbb0d14bce23c"));
		when(kpiColumnConfigService.getByKpiColumnConfig(Mockito.any(), Mockito.any())).thenReturn(kpiColumnConfigDTO);
		KPIExcelValidationDataResponse kpiExcelValidationDataResponse = (KPIExcelValidationDataResponse) kpiExcelDataService
				.process("kpi14", level, idList, null, kpiRequest, null, false);

		kpiRequest = createKpiRequestForSprint("kpi8", "", "sprint");
		KpiColumnConfigDTO kpiColumnConfigDTO1 = getKpiColumnConfigDTO("kpi8", new ObjectId("65118da7965fbb0d14bce23c"));
		when(jenkinsServiceR.process(Mockito.any())).thenReturn(validationKpiElementList);
		when(kpiColumnConfigService.getByKpiColumnConfig(Mockito.any(), Mockito.any())).thenReturn(kpiColumnConfigDTO1);
		kpiExcelDataService.process("kpi8", level, idList, null, kpiRequest, null, false);

		kpiRequest = createKpiRequest("kpi11", "", "project");
		KpiColumnConfigDTO kpiColumnConfigDTO2 = getKpiColumnConfigDTO("kpi11", new ObjectId("65118da7965fbb0d14bce23c"));
		when(kpiColumnConfigService.getByKpiColumnConfig(Mockito.any(), Mockito.any())).thenReturn(kpiColumnConfigDTO2);
		when(bitbucketServiceR.process(Mockito.any())).thenReturn(validationKpiElementList);
		kpiExcelDataService.process("kpi11", level, idList, null, kpiRequest, null, false);

		kpiRequest = createKpiRequest("kpi168", "", "project");
		KpiColumnConfigDTO kpiColumnConfigDTO3 = getKpiColumnConfigDTO("kpi168", new ObjectId("65118da7965fbb0d14bce23c"));
		when(kpiColumnConfigService.getByKpiColumnConfig(Mockito.any(), Mockito.any())).thenReturn(kpiColumnConfigDTO3);
		when(sonarServiceR.process(Mockito.any())).thenReturn(validationKpiElementList);
		kpiExcelDataService.process("kpi168", level, idList, null, kpiRequest, null, false);

		kpiRequest = createKpiRequest("kpi16", "", "project");
		KpiColumnConfigDTO kpiColumnConfigDTO4 = getKpiColumnConfigDTO("kpi16", null);
		when(kpiColumnConfigService.getByKpiColumnConfig(Mockito.any(), Mockito.any())).thenReturn(kpiColumnConfigDTO4);
		when(zephyrService.process(Mockito.any())).thenReturn(validationKpiElementList);
		kpiExcelDataService.process("kpi16", level, idList, null, kpiRequest, null, false);

		assertThat("Excel Validation Process Data: ", kpiExcelValidationDataResponse.getMapOfSprintAndData()
				.get("Alpha_Project1_Sprint1_name").getDefectKeyList().size(), equalTo(1));
	}

	private static KpiColumnConfigDTO getKpiColumnConfigDTO(String kpi14, ObjectId objectId) {
		KpiColumnConfigDTO kpiColumnConfigDTO = new KpiColumnConfigDTO();
		kpiColumnConfigDTO.setBasicProjectConfigId(objectId);
		kpiColumnConfigDTO.setSaveFlag(true);
		kpiColumnConfigDTO.setKpiId(kpi14);
		KpiColumnDetails kpiColumnDetails = new KpiColumnDetails();
		kpiColumnDetails.setColumnName("ABCD");
		kpiColumnDetails.setIsDefault(true);
		kpiColumnDetails.setIsShown(true);
		kpiColumnDetails.setOrder(1);
		List<KpiColumnDetails> kpiColumnDetail = new ArrayList<>();
		kpiColumnDetail.add(kpiColumnDetails);
		KpiColumnDetails kpiColumnDetails1 = new KpiColumnDetails();
		kpiColumnDetails1.setColumnName("XYZ");
		kpiColumnDetails1.setIsDefault(true);
		kpiColumnDetails1.setIsShown(true);
		kpiColumnDetails1.setOrder(2);
		kpiColumnDetail.add(kpiColumnDetails1);
		kpiColumnConfigDTO.setKpiColumnDetails(kpiColumnDetail);
		return kpiColumnConfigDTO;
	}

	@Test
	public void testCreateKpiRequestForKPIExcelData() {

		level = 1;
		idList.add("Test1");
		idList.add("Test2");

		Map<String, KpiRequest> kpiRequestSourceWiseMap = kpiExcelDataService.createKPIRequest(null, level, idList, null,
				false);

		assertArrayEquals(idList.parallelStream().toArray(String[]::new),
				kpiRequestSourceWiseMap.get("EXCEL-SONAR").getIds());
		assertThat("kpi request: ", kpiRequestSourceWiseMap.size(), equalTo(5));
	}

	@Test
	public void testCreateKpiRequestForKPIExcelValidationData() {

		level = 1;
		idList.add("Level1");
		idList.add("Level2");
		KpiRequest kpiRequest = createKpiRequest("kpi14", "", "project");
		Map<String, KpiRequest> kpiRequestSourceWiseMap = kpiExcelDataService.createKPIRequest("kpi14", level, idList,
				kpiRequest, null);

		assertArrayEquals(idList.parallelStream().toArray(String[]::new),
				kpiRequestSourceWiseMap.get("EXCEL-JIRA").getIds());
		assertThat("kpi request: ", kpiRequestSourceWiseMap.get("EXCEL-JIRA").getKpiList().size(), equalTo(1));
		assertThat("kpi request: ", kpiRequestSourceWiseMap.get("EXCEL-JIRA").getKpiList().get(0).getKpiId(),
				equalTo("kpi14"));
	}

	private KpiRequest createKpiRequest(String kpiId, String kpiName, String label) {
		KpiRequest kpiRequest = new KpiRequest();
		List<KpiElement> kpiList = new ArrayList<>();
		KpiElement kpiElement = new KpiElement();
		kpiElement.setKpiId(kpiId);
		kpiElement.setKpiName(kpiName);
		kpiElement.setKpiCategory("Category One");
		kpiElement.setKpiSource("Jira");
		kpiElement.setMaxValue("500");
		kpiElement.setChartType("gaugeChart");
		kpiList.add(kpiElement);
		kpiRequest.setLevel(2);
		kpiRequest.setIds(new String[]{"7"});
		kpiRequest.setLabel(label);
		// Hierarchy List created above is sent for processing.
		kpiRequest.setKpiList(kpiList);
		kpiRequest.setRequestTrackerId();
		Map<String, List<String>> selectedMap = new HashMap<>();
		if (label.equalsIgnoreCase("project")) {
			selectedMap.put("project", Arrays.asList("THPRO_THPRO_65118da7965fbb0d14bce23c"));
		} else if (label.equalsIgnoreCase("sprint")) {
			selectedMap.put("sprint", Arrays.asList("345_THPRO_THPRO_65118da7965fbb0d14bce23c"));
		} else {
			selectedMap.put("project", new ArrayList<>());
			selectedMap.put("sprint", new ArrayList<>());
			selectedMap.put("release", Arrays.asList("34576_THPRO_THPRO_65118da7965fbb0d14bce23c"));
		}

		kpiRequest.setSelectedMap(selectedMap);
		return kpiRequest;
	}

	private KpiRequest createKpiRequestForSprint(String kpiId, String kpiName, String label) {
		KpiRequest kpiRequest = new KpiRequest();
		List<KpiElement> kpiList = new ArrayList<>();
		KpiElement kpiElement = new KpiElement();
		kpiElement.setKpiId(kpiId);
		kpiElement.setKpiName(kpiName);
		kpiElement.setKpiCategory("Category One");
		kpiElement.setKpiSource("Jira");
		kpiElement.setMaxValue("500");
		kpiElement.setChartType("gaugeChart");
		kpiList.add(kpiElement);
		kpiRequest.setLevel(2);
		kpiRequest.setIds(new String[]{"7"});
		kpiRequest.setLabel(label);
		// Hierarchy List created above is sent for processing.
		kpiRequest.setKpiList(kpiList);
		kpiRequest.setRequestTrackerId();
		Map<String, List<String>> selectedMap = new HashMap<>();
		selectedMap.put("project", new ArrayList<>());
		selectedMap.put("sprint", Arrays.asList("345_THPRO_THPRO_65118da7965fbb0d14bce23c"));
		kpiRequest.setSelectedMap(selectedMap);
		return kpiRequest;
	}

	private KpiRequest createKpiRequestForRelease(String kpiId, String kpiName, String label) {
		KpiRequest kpiRequest = new KpiRequest();
		List<KpiElement> kpiList = new ArrayList<>();
		KpiElement kpiElement = new KpiElement();
		kpiElement.setKpiId(kpiId);
		kpiElement.setKpiName(kpiName);
		kpiElement.setKpiCategory("Category One");
		kpiElement.setKpiSource("Jira");
		kpiElement.setMaxValue("500");
		kpiElement.setChartType("gaugeChart");
		kpiList.add(kpiElement);
		kpiRequest.setLevel(2);
		kpiRequest.setIds(new String[]{"7"});
		kpiRequest.setLabel(label);
		// Hierarchy List created above is sent for processing.
		kpiRequest.setKpiList(kpiList);
		kpiRequest.setRequestTrackerId();
		Map<String, List<String>> selectedMap = new HashMap<>();
		selectedMap.put("project", new ArrayList<>());
		selectedMap.put("sprint", new ArrayList<>());
		selectedMap.put("release", Arrays.asList("345_THPRO_THPRO_65118da7965fbb0d14bce23c"));
		kpiRequest.setSelectedMap(selectedMap);
		return kpiRequest;
	}
}
