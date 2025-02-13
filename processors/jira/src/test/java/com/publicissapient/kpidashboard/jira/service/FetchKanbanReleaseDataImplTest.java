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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.publicissapient.kpidashboard.common.repository.application.ProjectHierarchyRepository;
import com.publicissapient.kpidashboard.common.service.ProjectHierarchyService;
import com.publicissapient.kpidashboard.jira.dataFactories.HierachyLevelFactory;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.KanbanAccountHierarchy;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectVersion;
import com.publicissapient.kpidashboard.common.model.application.SubProjectConfig;
import com.publicissapient.kpidashboard.common.repository.application.KanbanAccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectReleaseRepo;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelService;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

@RunWith(MockitoJUnitRunner.class)
public class FetchKanbanReleaseDataImplTest {

	@Mock
	ProjectHierarchyRepository kanbanAccountHierarchyRepo;
	@Mock
	KerberosClient krb5Client;
	@Mock
	ProjectReleaseRepo projectReleaseRepo;
	ProjectConfFieldMapping kanbanProjectMapping = ProjectConfFieldMapping.builder().build();
	List<KanbanAccountHierarchy> kanbanAccountHierarchylist = new ArrayList<>();
	List<HierarchyLevel> hierarchyLevels = new ArrayList<>();
	@Mock
	private HierarchyLevelService hierarchyLevelService;
	@Mock
	private JiraCommonService jiraCommonService;
	@Mock
	private JiraProcessorConfig jiraProcessorConfig;
	@Mock
	private ProjectHierarchySyncService projectHierarchySyncService;
	@InjectMocks
	private FetchKanbanReleaseDataImpl fetchKanbanReleaseData;
	@Mock
	private ProjectHierarchyService projectHierarchyService;

	@Before
	public void setUp() throws Exception {
		prepareKanbanAccountHierarchy();
		prepareProjectConfig();
		prepareHierarchyLevel();
		ProjectVersion version = new ProjectVersion();
		List<ProjectVersion> versionList = new ArrayList<>();
		version.setId(Long.valueOf("123"));
		version.setName("V1.0.2");
		version.setArchived(false);
		version.setReleased(true);
		version.setReleaseDate(DateTime.now());
		versionList.add(version);
		when(jiraCommonService.getVersion(any(), any())).thenReturn(versionList);
		when(hierarchyLevelService.getFullHierarchyLevels(true)).thenReturn(hierarchyLevels);
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
	public void processReleaseInfoNull() throws IOException, ParseException {
		try {
			fetchKanbanReleaseData.processReleaseInfo(kanbanProjectMapping, krb5Client);
		}
		catch (Exception ex){
			Assert.fail(ex.getMessage());
		}
	}

	@Test
	public void processReleaseInfoWhenHierachyExist() throws IOException, ParseException {
		prepareKanbanAccountHierarchy2();
		try {
			fetchKanbanReleaseData.processReleaseInfo(kanbanProjectMapping, krb5Client);
		}
		catch (Exception ex){
			Assert.fail(ex.getMessage());
		}
	}


	private void prepareProjectConfig() {
		// Online Project Config data
		SubProjectConfig subProjectConfig = new SubProjectConfig();
		JiraToolConfig jiraToolConfig = new JiraToolConfig();
		jiraToolConfig.setBoardQuery("");
		jiraToolConfig.setQueryEnabled(false);
		jiraToolConfig.setProjectKey("TEST");
		// Online Project Config data Kanban
		kanbanProjectMapping = ProjectConfFieldMapping.builder().build();
		kanbanProjectMapping.setBasicProjectConfigId(new ObjectId("5e1811cc0d248f0001ba6271"));
		kanbanProjectMapping.setProjectName("Tools-Atlassian Tools Support");
		subProjectConfig.setSubProjectIdentification("CustomField");
		subProjectConfig.setSubProjectIdentSingleValue("customfield_20810");
		ProjectBasicConfig kanbanBasicConfig = new ProjectBasicConfig();
		kanbanBasicConfig.setId(new ObjectId("5e15d9d5e4b098db674614b5"));
		kanbanBasicConfig.setProjectName("Tools-Atlassian Tools Support");
		kanbanBasicConfig.setIsKanban(true);
		kanbanBasicConfig.setProjectNodeId("uniqueId");
		kanbanProjectMapping.setProjectBasicConfig(kanbanBasicConfig);
		kanbanProjectMapping.setKanban(true);
		kanbanProjectMapping.setJira(jiraToolConfig);

	}

	private void prepareKanbanAccountHierarchy() {

		KanbanAccountHierarchy kanbanAccountHierarchy = new KanbanAccountHierarchy();
		kanbanAccountHierarchy.setId(new ObjectId("5e15d9d5e4b098db674614b8"));
		kanbanAccountHierarchy.setNodeId("TEST_1234_TEST");
		kanbanAccountHierarchy.setNodeName("TEST");
		kanbanAccountHierarchy.setLabelName("Project");
		kanbanAccountHierarchy.setFilterCategoryId(new ObjectId("5e15d7262b6a0532e258ce9c"));
		kanbanAccountHierarchy.setParentId("25071_TestHow_61160fa56c1b4842c1741fe1");
		kanbanAccountHierarchy.setBasicProjectConfigId(new ObjectId("5e15d8b195fe1300014538ce"));
		kanbanAccountHierarchy.setIsDeleted("False");
		kanbanAccountHierarchy.setPath(("25071_TestHow_61160fa56c1b4842c1741fe1###TestHow_61160fa56c1b4842c1741fe1"));
		kanbanAccountHierarchylist.add(kanbanAccountHierarchy);

	}

	private void prepareKanbanAccountHierarchy2() {

		KanbanAccountHierarchy kanbanAccountHierarchy = new KanbanAccountHierarchy();
		kanbanAccountHierarchy.setId(new ObjectId("5e15d9d5e4b098db674614b7"));
		kanbanAccountHierarchy.setNodeId("123_TEST_1234_TEST");
		kanbanAccountHierarchy.setNodeName("V1.0.2_TEST_1234_TEST");
		kanbanAccountHierarchy.setLabelName("release");
		kanbanAccountHierarchy.setFilterCategoryId(new ObjectId("5e15d7262b6a0532e258ce9c"));
		kanbanAccountHierarchy.setParentId("TEST_1234_TEST");
		kanbanAccountHierarchy.setBasicProjectConfigId(new ObjectId("5e15d8b195fe1300014538ce"));
		kanbanAccountHierarchy.setIsDeleted("False");
		kanbanAccountHierarchy.setPath(("TEST_1234_TEST###25071_TestHow_61160fa56c1b4842c1741fe1###TestHow_61160fa56c1b4842c1741fe1"));
		kanbanAccountHierarchy.setEndDate("2024-01-03T23:01:29.666+05:30");
		kanbanAccountHierarchylist.add(kanbanAccountHierarchy);

	}

}
