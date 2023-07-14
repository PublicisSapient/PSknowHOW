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

package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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
import com.publicissapient.kpidashboard.apis.data.SprintDetailsDataFactory;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.jira.service.JiraServiceR;
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
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

/**
 * Test class for testing CommitmentReliability implementation
 * 
 * @author chimudga
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class CommitmentReliabilityServiceImplTest {

	private static final String PROJECT_WISE_TOTAL_ISSUE = "projectWiseTotalIssues";
	private static String COMMITMENTRELIABILITY = "commitmentReliability";
	public Map<String, ProjectBasicConfig> projectConfigMap = new HashMap<>();
	public Map<ObjectId, FieldMapping> fieldMappingMap = new HashMap<>();
	List<JiraIssue> totalIssueList = new ArrayList<>();
	List<SprintDetails> sprintDetailsList = new ArrayList<>();
	@Mock
	JiraIssueRepository jiraIssueRepository;
	@Mock
	SprintRepository sprintRepository;
	@Mock
	CacheService cacheService;
	@Mock
	ConfigHelperService configHelperService;
	@InjectMocks
	CommittmentReliabilityServiceImpl commitmentReliabilityImpl;
	@Mock
	ProjectBasicConfigRepository projectConfigRepository;
	@Mock
	FieldMappingRepository fieldMappingRepository;
	@Mock
	CustomApiConfig customApiConfig;
	@Mock
	private FilterHelperService filterHelperService;
	@Mock
	private JiraServiceR jiraService;

	private List<AccountHierarchyData> accountHierarchyDataList = new ArrayList<>();
	private KpiRequest kpiRequest;
	private Map<String, Object> filterLevelMap;
	private List<ProjectBasicConfig> projectConfigList = new ArrayList<>();
	private List<FieldMapping> fieldMappingList = new ArrayList<>();
	private Map<String, String> kpiWiseAggregation = new HashMap<>();
	private Map<String, List<DataCount>> trendValueMap = new HashMap<>();
	@Mock
	private KpiHelperService kpiHelperService;
	@Mock
	private CommonService commonService;

	/**
	 * Set up the data
	 */
	@Before
	public void setup() {

		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance();
		kpiRequest = kpiRequestFactory.findKpiRequest(KPICode.COMMITMENT_RELIABILITY.getKpiId());
		kpiRequest.setLabel("PROJECT");

		AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
				.newInstance();
		accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();

		filterLevelMap = new LinkedHashMap<>();
		filterLevelMap.put("PROJECT", Filters.PROJECT);
		filterLevelMap.put("SPRINT", Filters.SPRINT);

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

		SprintDetailsDataFactory sprintDetailsDataFactory = SprintDetailsDataFactory.newInstance();
		sprintDetailsList = sprintDetailsDataFactory.getSprintDetails();

		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();

		totalIssueList = jiraIssueDataFactory.getBugs();

		kpiWiseAggregation.put(COMMITMENTRELIABILITY, "average");
		setTreadValuesDataCount();

	}

	private void setTreadValuesDataCount() {

		List<DataCount> dataCountList = new ArrayList<>();
		List<DataCount> dataCountList1 = new ArrayList<>();
		List<DataCount> dataCountList2 = new ArrayList<>();
		dataCountList1.add(createDataCount("100", "12140_PR", "OPRO Sprint 73_PR", 28, 28));
		dataCountList1.add(createDataCount("54", "12139_PR", "OPRO Sprint 72_PR", 18, 33));
		dataCountList1.add(createDataCount("53", "12138_PR", "OPRO Sprint 71_PR", 15, 28));
		DataCount dataCountValue = new DataCount();
		dataCountValue.setData("PR");
		dataCountValue.setValue(dataCountList1);
		dataCountList.add(dataCountValue);
		trendValueMap.put("Story Point", dataCountList);

		List<DataCount> dataCountList_2 = new ArrayList<>();
		dataCountList_2.add(createDataCount("100", "12140_PR", "OPRO Sprint 73_PR", 3, 3));
		dataCountList_2.add(createDataCount("50", "12139_PR", "OPRO Sprint 72_PR", 3, 4));
		dataCountList_2.add(createDataCount("66", "12138_PR", "OPRO Sprint 71_PR", 2, 3));
		DataCount dataCountValue2 = new DataCount();
		dataCountValue2.setData("PR");
		dataCountValue2.setValue(dataCountList_2);
		dataCountList2.add(dataCountValue2);

		trendValueMap.put("Issue Count", dataCountList2);

	}

	private DataCount createDataCount(String data, String sprint, String sprintName, int delivered, int commited) {
		DataCount dataCount = new DataCount();
		dataCount.setData(data);
		dataCount.setSProjectName("PR");
		dataCount.setSSprintID(sprint);
		dataCount.setSSprintName(sprintName);
		Map<String, Object> howermap = new HashMap<>();
		howermap.put("Delivered", delivered);
		howermap.put("Committed", commited);
		dataCount.setHoverValue(howermap);
		dataCount.setValue(Long.valueOf(data));
		dataCount.setKpiGroup("Story Point");
		return dataCount;

	}

	/**
	 * clean up method
	 */
	@After
	public void cleanup() {
	}

	/**
	 * Test the data when fetched from db
	 */
	@Test
	public void testFetchKPIDataFromDbData() throws ApplicationException {
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);
		List<Node> leafNodeList = new ArrayList<>();
		leafNodeList = KPIHelperUtil.getLeafNodes(treeAggregatorDetail.getRoot(), leafNodeList);
		String startDate = leafNodeList.get(0).getSprintFilter().getStartDate();
		String endDate = leafNodeList.get(leafNodeList.size() - 1).getSprintFilter().getEndDate();

		when(sprintRepository.findBySprintIDIn(Mockito.any())).thenReturn(sprintDetailsList);
		when(jiraIssueRepository.findIssueByNumber(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(totalIssueList);

		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);
		Map<String, Object> predictList = commitmentReliabilityImpl.fetchKPIDataFromDb(leafNodeList, startDate, endDate,
				kpiRequest);
		assertThat("Sprint story size :", ((List<JiraIssue>) predictList.get(PROJECT_WISE_TOTAL_ISSUE)).size(),
				equalTo(19));
	}

	/**
	 * Test the method to calculate commitment reliability
	 */
	@Test
	public void testGetSprintCommitmentReliability() throws ApplicationException {

		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				accountHierarchyDataList, new ArrayList<>(), "hierarchyLevelOne", 5);

		Map<String, List<String>> maturityRangeMap = new HashMap<>();
		maturityRangeMap.put(COMMITMENTRELIABILITY, Arrays.asList("-20", "20-40", "40-60", "60-80", "80-"));

		when(sprintRepository.findBySprintIDIn(Mockito.any())).thenReturn(sprintDetailsList);
		when(jiraIssueRepository.findIssueByNumber(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(totalIssueList);

		when(configHelperService.getFieldMappingMap()).thenReturn(fieldMappingMap);

		String kpiRequestTrackerId = "Excel-Jira-5be544de025de212549176a9";
		when(cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.JIRA.name()))
				.thenReturn(kpiRequestTrackerId);
		when(commitmentReliabilityImpl.getRequestTrackerId()).thenReturn(kpiRequestTrackerId);

		try {
			KpiElement kpiElement = commitmentReliabilityImpl.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
					treeAggregatorDetail);
			((List<DataCountGroup>) kpiElement.getTrendValueList()).forEach(dc -> {

				String status = dc.getFilter();
				switch (status) {
				case "Story Point":
					assertThat("Story Point :", dc.getValue().size(), equalTo(1));
					break;
				case "Issue Count":
					assertThat("Issue Count :", dc.getValue().size(), equalTo(1));
					break;
				default:
					break;
				}

			});

		} catch (ApplicationException e) {
			e.printStackTrace();

		}
	}

	@Test
	public void testQualifierType() {
		String kpiName = KPICode.COMMITMENT_RELIABILITY.name();
		String type = commitmentReliabilityImpl.getQualifierType();
		assertThat("KPI Name : ", type, equalTo(kpiName));
	}
}
