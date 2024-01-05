/*
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.publicissapient.kpidashboard.apis.util;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.data.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.apis.model.CodeBuildTimeInfo;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.LeadTimeChangeData;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;

@RunWith(MockitoJUnitRunner.class)
public class KPIExcelUtilityTest {

	@InjectMocks
	KPIExcelUtility excelUtility;
	private List<JiraIssue> jiraIssues;

	@Before
	public void setup() {
		JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance();
		jiraIssues = jiraIssueDataFactory.getJiraIssues();
	}

	@Test
	public void populateFTPRExcelData_ValidData_PopulatesKPIExcelData() {
		// Arrange
		String sprint = "Sprint1";
		List<String> storyIds = Arrays.asList("STORY1", "STORY2");
		List<KPIExcelData> kpiExcelData = new ArrayList<>();

		Map<String, JiraIssue> issueData = jiraIssues.stream().collect(Collectors.toMap(JiraIssue::getNumber, x -> x));

		// Act
		excelUtility.populateFTPRExcelData(sprint, storyIds, jiraIssues, kpiExcelData, issueData);

		// Assert
		assertEquals(2, kpiExcelData.size());
		assertEquals("Sprint1", kpiExcelData.get(0).getSprintName());
		assertEquals("Sprint1", kpiExcelData.get(1).getSprintName());
	}

	@Test
	public void populateLeadTimeForChangeExcelData_ValidData_PopulatesKPIExcelData() {
		// Arrange
		String projectName = "Project1";
		Map<String, List<LeadTimeChangeData>> leadTimeMapTimeWise = new HashMap<>();
		List<LeadTimeChangeData> leadTimeList = Arrays.asList(createLeadTime(), createLeadTime());

		leadTimeMapTimeWise.put("Week1", leadTimeList);
		List<KPIExcelData> kpiExcelData = new ArrayList<>();
		String leadTimeConfigRepoTool = CommonConstant.REPO;

		// Act
		excelUtility.populateLeadTimeForChangeExcelData(projectName, leadTimeMapTimeWise, kpiExcelData,
				leadTimeConfigRepoTool);

		// Assert
		assertEquals(2, kpiExcelData.size());
	}

	public static void populatePickupTimeExcelData(String projectName, List<Map<String, Double>> repoWiseMRList,
			List<String> repoList, List<String> branchList, List<KPIExcelData> kpiExcelData) {

		if (CollectionUtils.isNotEmpty(repoWiseMRList)) {
			for (int i = 0; i < repoWiseMRList.size(); i++) {
				Map<String, Double> repoWiseMap = repoWiseMRList.get(i);
				for (Map.Entry<String, Double> m : repoWiseMap.entrySet()) {
					KPIExcelData excelData = new KPIExcelData();
					excelData.setProject(projectName);
					Map<String, String> repoUrl = new HashMap<>();
					repoUrl.put(repoList.get(i), repoList.get(i));
					excelData.setRepositoryURL(repoUrl);
					excelData.setBranch(branchList.get(i));
					excelData.setDaysWeeks(m.getKey());
					excelData.setPickupTime(m.getValue().toString());
					kpiExcelData.add(excelData);
				}

			}
		}

	}

	@Test
	public void populatePickupTimeExcelData_ValidData_PopulatesKPIExcelData() {
		// Arrange
		String projectName = "Project1";
		List<Map<String, Double>> repoWiseMRList = new ArrayList<>();
		Map<String, Double> repoWiseMap1 = new HashMap<>();
		repoWiseMap1.put("Week1", 5.0);
		repoWiseMap1.put("Week2", 8.0);
		repoWiseMRList.add(repoWiseMap1);

		List<String> repoList = Arrays.asList("Repo1");
		List<String> branchList = Arrays.asList("Branch1");
		List<KPIExcelData> kpiExcelData = new ArrayList<>();

		// Act
		excelUtility.populatePickupTimeExcelData(projectName, repoWiseMRList, repoList, branchList, kpiExcelData);

		// Assert
		assertEquals(2, kpiExcelData.size());
	}

	@Test
	public void populatePRSizeExcelData_ValidData_PopulatesKPIExcelData() {
		// Arrange
		String projectName = "Project1";
		List<Map<String, Long>> repoWiseMRList = new ArrayList<>();
		Map<String, Long> repoWiseMap1 = new HashMap<>();
		repoWiseMap1.put("Week1", 5L);
		repoWiseMap1.put("Week2", 8L);
		repoWiseMRList.add(repoWiseMap1);

		List<String> repoList = Arrays.asList("Repo1");
		List<String> branchList = Arrays.asList("Branch1");
		List<KPIExcelData> kpiExcelData = new ArrayList<>();

		// Act
		excelUtility.populatePRSizeExcelData(projectName, repoWiseMRList, repoList, branchList, kpiExcelData);

		// Assert
		assertEquals(2, kpiExcelData.size());
	}

	@Test
	public void populateCodeCommit_ValidData_PopulatesKPIExcelData() {
		// Arrange
		String projectName = "Project1";
		List<Map<String, Long>> repoWiseCommitList = new ArrayList<>();
		Map<String, Long> repoWiseCommitMap1 = new HashMap<>();
		repoWiseCommitMap1.put("Week1", 10L);
		repoWiseCommitMap1.put("Week2", 15L);
		repoWiseCommitList.add(repoWiseCommitMap1);

		List<Map<String, Long>> repoWiseMergeRequestList = new ArrayList<>();
		Map<String, Long> repoWiseMergeMap1 = new HashMap<>();
		repoWiseMergeMap1.put("Week1", 3L);
		repoWiseMergeMap1.put("Week2", 5L);
		repoWiseMergeRequestList.add(repoWiseMergeMap1);

		List<String> repoList = Arrays.asList("Repo1");
		List<String> branchList = Arrays.asList("Branch1");
		List<KPIExcelData> kpiExcelData = new ArrayList<>();

		// Act
		excelUtility.populateCodeCommit(projectName, repoWiseCommitList, repoList, branchList, kpiExcelData,
				repoWiseMergeRequestList);

		// Assert
		assertEquals(2, kpiExcelData.size());
	}

	@Test
	public void populateCodeBuildTimeExcelData_ValidData_PopulatesKPIExcelData() {
		// Arrange
		CodeBuildTimeInfo codeBuildTimeInfo = new CodeBuildTimeInfo();
		codeBuildTimeInfo.setBuildJobList(Arrays.asList("Job1", "Job2"));
		codeBuildTimeInfo.setBuildStartTimeList(Arrays.asList("2022-01-01T10:00:00", "2022-01-02T11:00:00"));
		codeBuildTimeInfo.setBuildEndTimeList(Arrays.asList("2022-01-01T11:00:00", "2022-01-02T12:00:00"));
		codeBuildTimeInfo.setDurationList(Arrays.asList("1 hour", "1 hour"));
		codeBuildTimeInfo.setBuildUrlList(Arrays.asList("url1", "url2"));
		codeBuildTimeInfo.setBuildStatusList(Arrays.asList("SUCCESS", "FAILURE"));

		String projectName = "Project1";
		List<KPIExcelData> kpiExcelData = new ArrayList<>();

		// Act
		excelUtility.populateCodeBuildTimeExcelData(codeBuildTimeInfo, projectName, kpiExcelData);

		// Assert
		assertEquals(2, kpiExcelData.size());
	}

	@Test
	public void populateRefinementRejectionExcelData_ValidData_PopulatesExcelDataList() {
		// Arrange
		List<KPIExcelData> excelDataList = new ArrayList<>();

		Map<String, Map<String, List<JiraIssue>>> weekAndTypeMap = new HashMap<>();
		Map<String, List<JiraIssue>> map = new HashMap<>();
		map.put("Type1", Arrays.asList(jiraIssues.get(0)));

		weekAndTypeMap.put("Week1", map);
		map.clear();
		map.put("Type2", Arrays.asList(jiraIssues.get(1)));
		weekAndTypeMap.put("Week2", map);

		Map<String, LocalDateTime> jiraDateMap = jiraIssues.stream()
				.collect(Collectors.toMap(JiraIssue::getNumber, x -> LocalDateTime.now()));

		// Act
		excelUtility.populateRefinementRejectionExcelData(excelDataList, jiraIssues, weekAndTypeMap, jiraDateMap);

		// Assert
		assertEquals(44, excelDataList.size());
	}

	private LeadTimeChangeData createLeadTime() {
		LeadTimeChangeData leadTimeChangeData = new LeadTimeChangeData();
		leadTimeChangeData.setLeadTime(2.0);
		leadTimeChangeData.setLeadTimeInDays("2");
		leadTimeChangeData.setDate(LocalDate.now().toString());
		leadTimeChangeData.setClosedDate(LocalDate.now().minusDays(1).toString());
		leadTimeChangeData.setReleaseDate(LocalDate.now().toString());
		leadTimeChangeData.setMergeID("123");
		leadTimeChangeData.setUrl("www.fhewjdh.com");
		return leadTimeChangeData;
	}

}