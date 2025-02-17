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

package com.publicissapient.kpidashboard.jira.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.publicissapient.kpidashboard.common.repository.application.ProjectHierarchyRepository;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectVersion;
import com.publicissapient.kpidashboard.common.model.application.SubProjectConfig;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectReleaseRepo;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelService;
import com.publicissapient.kpidashboard.common.service.ProjectHierarchyService;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

@RunWith(MockitoJUnitRunner.class)
public class FetchScrumReleaseDataImplTest {

	@Mock
	ProjectHierarchyRepository accountHierarchyRepository;
	@Mock
	KerberosClient krb5Client;
	@Mock
	JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;
	@Mock
	ProjectReleaseRepo projectReleaseRepo;
	ProjectConfFieldMapping scrumProjectMapping = ProjectConfFieldMapping.builder().build();
	List<AccountHierarchy> accountHierarchylist = new ArrayList<>();
	List<HierarchyLevel> hierarchyLevels = new ArrayList<>();
	@Mock
	private HierarchyLevelService hierarchyLevelService;
	@Mock
	private JiraCommonService jiraCommonService;
	@InjectMocks
	private FetchScrumReleaseDataImpl fetchScrumReleaseData;
	@Mock
	private JiraProcessorConfig jiraProcessorConfig;
	@Mock
	private ProjectHierarchyService projectHierarchyService;
	@Mock
	private ProjectHierarchySyncService projectHierarchySyncService;

	@Before
	public void setUp() throws Exception {
		prepareAccountHierarchy();
		prepareProjectConfig();
		prepareHierarchyLevel();
		when(hierarchyLevelService.getFullHierarchyLevels(anyBoolean())).thenReturn(hierarchyLevels);
		ProjectVersion version = new ProjectVersion();
		List<ProjectVersion> versionList = new ArrayList<>();
		version.setId(Long.valueOf("123"));
		version.setName("V1.0.2");
		version.setArchived(false);
		version.setReleased(true);
		version.setReleaseDate(DateTime.now());
		versionList.add(version);
		when(jiraCommonService.getVersion(any(), any())).thenReturn(versionList);
		List<JiraIssueCustomHistory> jiraIssueCustomHistories = new ArrayList<>();
		JiraIssueCustomHistory jiraIssueCustomHistory = new JiraIssueCustomHistory();
		JiraHistoryChangeLog changeLog = new JiraHistoryChangeLog("", "V1.0.2", LocalDateTime.now());
		List<JiraHistoryChangeLog> logList = new ArrayList<>();
		logList.add(changeLog);
		jiraIssueCustomHistory.setFixVersionUpdationLog(logList);
		jiraIssueCustomHistories.add(jiraIssueCustomHistory);

		when(jiraIssueCustomHistoryRepository.findByBasicProjectConfigIdIn(anyString()))
				.thenReturn(jiraIssueCustomHistories);
	}

	private void prepareHierarchyLevel() {
		hierarchyLevels.add(new HierarchyLevel(5, "sprint", "Sprint", ""));
		hierarchyLevels.add(new HierarchyLevel(5, "release", "Release", ""));
		hierarchyLevels.add(new HierarchyLevel(4, "project", "Project", ""));
		hierarchyLevels.add(new HierarchyLevel(3, "hierarchyLevelThree", "Level Three", ""));
		hierarchyLevels.add(new HierarchyLevel(2, "hierarchyLevelTwo", "Level Two", ""));
		hierarchyLevels.add(new HierarchyLevel(1, "hierarchyLevelOne", "Level One", ""));
	}

	@Test
	public void processReleaseInfo() throws IOException, ParseException {
		try {
			fetchScrumReleaseData.processReleaseInfo(scrumProjectMapping, krb5Client);
		} catch (Exception ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void processReleaseInfoWhenHierachyExist() throws IOException, ParseException {
		prepareAccountHierarchy2();
		try {
			fetchScrumReleaseData.processReleaseInfo(scrumProjectMapping, krb5Client);
		} catch (Exception ex) {
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void processReleaseInfoNull() throws IOException, ParseException {
		List<JiraIssueCustomHistory> jiraIssueCustomHistories = new ArrayList<>();
		JiraIssueCustomHistory jiraIssueCustomHistory = new JiraIssueCustomHistory();
		jiraIssueCustomHistories.add(jiraIssueCustomHistory);
		when(jiraIssueCustomHistoryRepository.findByBasicProjectConfigIdIn(anyString()))
				.thenReturn(jiraIssueCustomHistories);
		fetchScrumReleaseData.processReleaseInfo(scrumProjectMapping, krb5Client);
	}

	private void prepareProjectConfig() {
		// Online Project Config data
		scrumProjectMapping.setBasicProjectConfigId(new ObjectId("5e15d8b195fe1300014538ce"));
		scrumProjectMapping.setProjectName("TEST Project Internal");
		SubProjectConfig subProjectConfig = new SubProjectConfig();
		subProjectConfig.setSubProjectIdentification("CustomField");
		subProjectConfig.setSubProjectIdentSingleValue("customfield_37903");
		scrumProjectMapping.setKanban(false);

		JiraToolConfig jiraToolConfig = new JiraToolConfig();
		jiraToolConfig.setBoardQuery("");
		jiraToolConfig.setQueryEnabled(false);
		jiraToolConfig.setProjectKey("TEST");
		ProjectBasicConfig projectBasicConfig = new ProjectBasicConfig();
		projectBasicConfig.setProjectName("TEST Project Internal");
		projectBasicConfig.setIsKanban(false);
		projectBasicConfig.setProjectNodeId("uniqueId");
		projectBasicConfig.setId(new ObjectId("5e15d8b195fe1300014538ce"));
		scrumProjectMapping.setProjectBasicConfig(projectBasicConfig);
		scrumProjectMapping.setJira(jiraToolConfig);
	}

	void prepareAccountHierarchy() {
		AccountHierarchy accountHierarchy = new AccountHierarchy();
		accountHierarchy.setId(new ObjectId("5e15d9d5e4b098db674614b8"));
		accountHierarchy.setNodeId("TEST_1234_TEST");
		accountHierarchy.setNodeName("TEST");
		accountHierarchy.setLabelName("Project");
		accountHierarchy.setFilterCategoryId(new ObjectId("5e15d7262b6a0532e258ce9c"));
		accountHierarchy.setParentId("25071_TestHow_61160fa56c1b4842c1741fe1");
		accountHierarchy.setBasicProjectConfigId(new ObjectId("5e15d8b195fe1300014538ce"));
		accountHierarchy.setIsDeleted("False");
		accountHierarchy.setPath(("25071_TestHow_61160fa56c1b4842c1741fe1###TestHow_61160fa56c1b4842c1741fe1"));
		accountHierarchylist.add(accountHierarchy);
	}

	void prepareAccountHierarchy2() {
		AccountHierarchy accountHierarchy = new AccountHierarchy();
		accountHierarchy.setId(new ObjectId("5e15d9d5e4b098db674614b7"));
		accountHierarchy.setNodeId("123_TEST_1234_TEST");
		accountHierarchy.setNodeName("V1.0.2_TEST_1234_TEST");
		accountHierarchy.setLabelName("release");
		accountHierarchy.setFilterCategoryId(new ObjectId("5e15d7262b6a0532e258ce9c"));
		accountHierarchy.setParentId("TEST_1234_TEST");
		accountHierarchy.setBasicProjectConfigId(new ObjectId("5e15d8b195fe1300014538ce"));
		accountHierarchy.setIsDeleted("False");
		accountHierarchy.setPath(
				("TEST_1234_TEST###25071_TestHow_61160fa56c1b4842c1741fe1###TestHow_61160fa56c1b4842c1741fe1"));
		accountHierarchy.setEndDate("2024-01-03T23:01:29.666+05:30");
		accountHierarchylist.add(accountHierarchy);
	}

}
