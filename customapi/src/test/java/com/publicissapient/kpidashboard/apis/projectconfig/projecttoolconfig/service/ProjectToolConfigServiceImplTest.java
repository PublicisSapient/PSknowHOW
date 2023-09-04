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

package com.publicissapient.kpidashboard.apis.projectconfig.projecttoolconfig.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.common.model.connection.Connection;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.cleanup.SonarDataCleanUpService;
import com.publicissapient.kpidashboard.apis.cleanup.ToolDataCleanUpServiceFactory;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.errors.ToolNotFoundException;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfigDTO;
import com.publicissapient.kpidashboard.common.model.application.Subproject;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.SubProjectRepository;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;

/***
 * @author yasbano
 * @author dilipK
 *
 */

@RunWith(MockitoJUnitRunner.class)
public class ProjectToolConfigServiceImplTest {
	ProjectToolConfig listProjectTool = new ProjectToolConfig();
	ProjectToolConfig listProjectTool1 = new ProjectToolConfig();
	ProjectToolConfig listProjectTool2 = new ProjectToolConfig();
	Connection connection = new Connection();
	String testId;
	String toolName;
	@InjectMocks
	private ProjectToolConfigServiceImpl projectToolServiceImpl;
	@Mock
	private ProjectToolConfigRepository toolRepositroy;
	@Mock
	private SubProjectRepository subProjectRepository;
	@Mock
	private ToolDataCleanUpServiceFactory dataCleanUpServiceFactory;
	@Mock
	private SonarDataCleanUpService sonarDataCleanUpService;
	@Mock
	private CacheService cacheService;
	@Mock
	private ConnectionRepository connectionRepository;
	@Mock
	private ProjectBasicConfigRepository projectBasicConfigRepository;

	/**
	 * method includes preprocesses for test cases
	 */
	@Before
	public void setUp() {

		toolName = "Test1";
		listProjectTool.setId(new ObjectId("5fa0023dbb5fa781ccd5ac2c"));
		listProjectTool.setToolName(toolName);
		listProjectTool.setConnectionId(new ObjectId("5fb3a6412064a35b8069930a"));
		listProjectTool.setBasicProjectConfigId(new ObjectId("5fb364612064a31c9ccd517a"));
		listProjectTool.setBranch("test1");
		listProjectTool.setJobName("testing1");

		toolName = "Test2";
		listProjectTool1.setId(new ObjectId("4fa0023dbb5fa781ccd5ac2c"));
		listProjectTool1.setToolName(toolName);
		listProjectTool1.setBranch("test2");
		listProjectTool1.setJobName("testing2");
		listProjectTool.setBasicProjectConfigId(new ObjectId("5fb364612064a31c9ccd517a"));
		listProjectTool1.setProjectId("12345");

		toolName = "Test1";
		listProjectTool2.setToolName(toolName);
		listProjectTool2.setId(null);
		listProjectTool2.setBranch(null);
		listProjectTool2.setJobName(null);
		listProjectTool.setBasicProjectConfigId(new ObjectId("5fb364612064a31c9ccd517a"));
		listProjectTool2.setProjectId(null);

		connection.setConnectionName("TestConn");
		connection.setId(new ObjectId("5fb3a6412064a35b8069930a"));

	}

	/**
	 * method includes post processes for test cases
	 */
	@After
	public void cleanUp() {
		listProjectTool = new ProjectToolConfig();
		listProjectTool.setToolName(toolName);
		listProjectTool.setId(new ObjectId("5fa0023dbb5fa781ccd5ac2c"));
		listProjectTool.setBranch("test1");
		listProjectTool.setJobName("testing1");
		listProjectTool1 = new ProjectToolConfig();
		listProjectTool1.setToolName(toolName);
		listProjectTool1.setId(new ObjectId("4fa0023dbb5fa781ccd5ac2c"));
		listProjectTool1.setBranch("test2");
		listProjectTool1.setJobName("testing2");
		listProjectTool2 = new ProjectToolConfig();
		listProjectTool2.setToolName(null);
		listProjectTool2.setId(null);
		listProjectTool2.setBranch(null);
		listProjectTool2.setJobName(null);
	}

	/**
	 * 1. Input projectToolConfig1 no data.
	 *
	 */
	@Test
	public void testSaveToolDetailsNoData() {
		ProjectToolConfig projectToolConfig1 = null;
		ServiceResponse response = projectToolServiceImpl.saveProjectToolDetails(projectToolConfig1);
		assertThat("status: ", response.getSuccess(), equalTo(false));
		assertEquals(null, response.getData());
	}

	/**
	 * 2. Input projectToolConfig1 valid data save.
	 *
	 */
	@Test
	public void testSaveToolDetails() {
		List<ProjectToolConfig> projectTool = new ArrayList<ProjectToolConfig>();
		projectTool.add(listProjectTool);
		ServiceResponse response = projectToolServiceImpl.saveProjectToolDetails(listProjectTool);
		assertThat("status: ", response.getSuccess(), equalTo(true));
		assertEquals(response.getData(), listProjectTool);

	}

	/**
	 * 3. check toolName Null
	 */

	@Test
	public void testSaveToolDetails1() {
		listProjectTool2.setId(new ObjectId("5fa0023dbb5fa781ccd5ac3c"));
		listProjectTool2.setToolName(null);
		ServiceResponse response = projectToolServiceImpl.saveProjectToolDetails(listProjectTool2);
		assertThat("status: ", response.getSuccess(), equalTo(false));
		assertEquals(null, response.getData());
	}

	/**
	 * 4. check connectionId Null
	 */

	@Test
	public void testSaveToolDetails2() {
		listProjectTool2.setId(new ObjectId("5fa0023dbb5fa781ccd5ac3c"));
		listProjectTool2.setBasicProjectConfigId(new ObjectId("5fb364612064a31c9ccd517a"));
		listProjectTool2.setToolName("gitlab");
		listProjectTool2.setConnectionId(null);
		ServiceResponse response = projectToolServiceImpl.saveProjectToolDetails(listProjectTool2);
		assertThat("status: ", response.getSuccess(), equalTo(false));
		assertEquals(null, response.getData());
	}

	/**
	 * 5. check BasicProjectConfigId Null
	 */

	@Test
	public void testSaveToolDetails3() {
		listProjectTool2.setId(new ObjectId("5fa0023dbb5fa781ccd5ac3c"));
		listProjectTool2.setToolName("gitlab");
		listProjectTool2.setBasicProjectConfigId(null);
		ServiceResponse response = projectToolServiceImpl.saveProjectToolDetails(listProjectTool2);
		assertThat("status: ", response.getSuccess(), equalTo(false));
		assertEquals(null, response.getData());
	}

	/**
	 * 1. Input String testId is no data
	 *
	 */
	@Test
	public void testModifyProjectToolByIdNoData() {
		testId = null;
		ServiceResponse response = projectToolServiceImpl.modifyProjectToolById(listProjectTool, testId);
		assertThat("status: ", response.getSuccess(), equalTo(false));
		assertEquals(null, response.getData());
	}

	/**
	 * 2. check ProjectToolConfig object Null when modify by id
	 */
	@Test
	public void testModifyProjectToolById1() {
		testId = "5fa0023dbb5fa781ccd5ac3c";
		listProjectTool2 = null;
		List<ProjectToolConfig> toolData = new ArrayList<ProjectToolConfig>();
		toolData.add(listProjectTool2);
		when(toolRepositroy.findById(testId)).thenReturn(listProjectTool2);
		ServiceResponse response = projectToolServiceImpl.modifyProjectToolById(listProjectTool2, testId);
		assertThat("status: ", response.getSuccess(), equalTo(false));
		assertEquals(null, response.getData());
	}

	/**
	 * 3. check ToolName Null when modify by id
	 */
	@Test
	public void testModifyProjectToolById2() {
		testId = "5fa0023dbb5fa781ccd5ac3c";
		listProjectTool2.setToolName(null);
		List<ProjectToolConfig> toolData = new ArrayList<ProjectToolConfig>();
		toolData.add(listProjectTool2);
		when(toolRepositroy.findById(testId)).thenReturn(listProjectTool2);
		ServiceResponse response = projectToolServiceImpl.modifyProjectToolById(listProjectTool2, testId);
		assertThat("status: ", response.getSuccess(), equalTo(false));
		assertEquals(null, response.getData());
	}

	/**
	 * 4. check BasicProjectConfigId Null when modify by id
	 */
	@Test
	public void testModifyProjectToolById3() {
		testId = "5fa0023dbb5fa781ccd5ac3c";
		listProjectTool2.setToolName("gitlab");
		listProjectTool2.setBasicProjectConfigId(null);
		List<ProjectToolConfig> toolData = new ArrayList<ProjectToolConfig>();
		toolData.add(listProjectTool2);
		when(toolRepositroy.findById(testId)).thenReturn(listProjectTool2);
		ServiceResponse response = projectToolServiceImpl.modifyProjectToolById(listProjectTool2, testId);
		assertThat("status: ", response.getSuccess(), equalTo(false));
		assertEquals(null, response.getData());
	}

	/**
	 * 5. check ConnectionId Null when modify by id
	 */
	@Test
	public void testModifyProjectToolById4() {
		testId = "5fa0023dbb5fa781ccd5ac3c";
		listProjectTool2.setBasicProjectConfigId(new ObjectId("5fb364612064a31c9ccd517a"));
		listProjectTool2.setToolName("gitlab");
		listProjectTool2.setConnectionId(null);
		List<ProjectToolConfig> toolData = new ArrayList<ProjectToolConfig>();
		toolData.add(listProjectTool2);
		when(toolRepositroy.findById(testId)).thenReturn(listProjectTool2);
		ServiceResponse response = projectToolServiceImpl.modifyProjectToolById(listProjectTool2, testId);
		assertThat("status: ", response.getSuccess(), equalTo(false));
		assertEquals(null, response.getData());
	}

	/**
	 * 6. Modify By testId
	 *
	 */
	@Test
	public void testModifyProjectToolById() {
		testId = "5fa0023dbb5fa781ccd5ac3c";
		when(toolRepositroy.findById(testId)).thenReturn(listProjectTool);
		ServiceResponse response = projectToolServiceImpl.modifyProjectToolById(listProjectTool, testId);
		assertThat("status: ", response.getSuccess(), equalTo(true));
		assertEquals(listProjectTool, response.getData());
	}

	/**
	 * 1. database call has records and returns them as an array
	 *
	 */
	@Test
	public void testgetAllProjectTool() {
		List<ProjectToolConfig> projectToolConfigDataList = new ArrayList<>();
		projectToolConfigDataList.add(listProjectTool);
		List<Subproject> subProjectList = new ArrayList<>();
		List<Subproject> subProjectList1 = new ArrayList<>();
		List<ProjectToolConfigDTO> projectToolConfigDTOList = new ArrayList<>();
		Subproject subproject = new Subproject();
		Subproject subproject1 = new Subproject();
		subproject.setBasicProjectConfigId(new ObjectId("5fb364612064a31c9ccd517a"));
		subproject.setToolConfigId(new ObjectId("5fa0023dbb5fa781ccd5ac2c"));
		subProjectList.add(subproject);
		subProjectList1.add(subproject1);
		ProjectToolConfigDTO projectToolConfigDTO = new ProjectToolConfigDTO();
		projectToolConfigDTO.setId(listProjectTool.getId().toString());
		projectToolConfigDTO.setBasicProjectConfigId(listProjectTool.getBasicProjectConfigId());
		projectToolConfigDTO.setToolName(toolName);
		projectToolConfigDTO.setConnectionId(new ObjectId("5fb3a6412064a35b8069930a"));
		projectToolConfigDTO.setBasicProjectConfigId(new ObjectId("5fb364612064a31c9ccd517a"));
		projectToolConfigDTO.setBranch("test1");
		projectToolConfigDTO.setJobName("testing1");
		projectToolConfigDTO.setProjectId("1234");
		projectToolConfigDTO.setSubprojects(subProjectList1);
		projectToolConfigDTOList.add(projectToolConfigDTO);
		List<ObjectId> toolConfiragrationIds = projectToolConfigDataList.stream().map(ProjectToolConfig::getId)
				.collect(Collectors.toList());
		when(subProjectRepository.findBytoolConfigIdIn(toolConfiragrationIds)).thenReturn(subProjectList);
		when(toolRepositroy.findAll()).thenReturn(projectToolConfigDataList);
		when(connectionRepository.findById(new ObjectId("5fb3a6412064a35b8069930a")))
				.thenReturn(Optional.ofNullable(connection));
		ServiceResponse response = projectToolServiceImpl.getAllProjectTool();
		assertThat("status : ", response.getSuccess(), equalTo(true));
		assertThat("Data should exist: ", response.getData(), equalTo(projectToolConfigDataList));
	}

	/**
	 * 2. database call has an error and returns null
	 *
	 */
	@Test
	public void testgetAllProjectToolNoData() {
		when(toolRepositroy.findAll()).thenReturn(null);
		ServiceResponse response = projectToolServiceImpl.getAllProjectTool();
		assertThat("status: ", response.getSuccess(), equalTo(false));
		assertThat("Data shouldn't exist:  ", response.getData(), equalTo(null));
	}

	/**
	 * 3. database call has record and returns true
	 *
	 */
	@Test
	public void testgetProjectToolByType() {
		List<ProjectToolConfig> projectToolConfigDataList = new ArrayList<>();
		List<Subproject> subProjectList = new ArrayList<>();
		List<Subproject> subProjectList1 = new ArrayList<>();
		List<ProjectToolConfigDTO> projectToolConfigDTOList = new ArrayList<>();
		projectToolConfigDataList.add(listProjectTool);
		Subproject subproject = new Subproject();
		Subproject subproject1 = new Subproject();
		subproject.setBasicProjectConfigId(new ObjectId("5fb364612064a31c9ccd517a"));
		subproject.setToolConfigId(new ObjectId("5fa0023dbb5fa781ccd5ac2c"));
		subProjectList.add(subproject);
		subProjectList1.add(subproject1);
		ProjectToolConfigDTO projectToolConfigDTO = new ProjectToolConfigDTO();
		projectToolConfigDTO.setId(listProjectTool.getId().toString());
		projectToolConfigDTO.setBasicProjectConfigId(listProjectTool.getBasicProjectConfigId());
		projectToolConfigDTO.setToolName(toolName);
		projectToolConfigDTO.setConnectionId(new ObjectId("5fb3a6412064a35b8069930a"));
		projectToolConfigDTO.setBasicProjectConfigId(new ObjectId("5fb364612064a31c9ccd517a"));
		projectToolConfigDTO.setBranch("test1");
		projectToolConfigDTO.setJobName("testing1");
		projectToolConfigDTO.setProjectId("1234");
		projectToolConfigDTO.setSubprojects(subProjectList1);
		projectToolConfigDTOList.add(projectToolConfigDTO);
		List<ObjectId> toolConfiragrationIds = projectToolConfigDataList.stream().map(ProjectToolConfig::getId)
				.collect(Collectors.toList());
		String toolType = "GitLab";
		when(toolRepositroy.findByToolName(toolType)).thenReturn(projectToolConfigDataList);
		when(subProjectRepository.findBytoolConfigIdIn(toolConfiragrationIds)).thenReturn(subProjectList);
		when(connectionRepository.findById(new ObjectId("5fb3a6412064a35b8069930a")))
				.thenReturn(Optional.ofNullable(connection));
		ServiceResponse response = projectToolServiceImpl.getProjectToolByType(toolType);
		assertThat("status: ", response.getSuccess(), equalTo(true));

	}

	/**
	 * 3. check toolName Null
	 */

	@Test
	public void testgetAllProjectToolNoData1() {
		String toolType = "GitLab";
		List<ProjectToolConfig> projectToolConfigDataList = null;
		when(toolRepositroy.findByToolName(toolType)).thenReturn(projectToolConfigDataList);
		ServiceResponse response = projectToolServiceImpl.getProjectToolByType(toolType);
		assertThat("status: ", response.getSuccess(), equalTo(false));
		assertEquals(null, response.getData());
	}

	/**
	 * 4. database call has an error and returns null
	 *
	 */
	@Test
	public void testgetProjectToolByTypeNoData() {

		ServiceResponse response = projectToolServiceImpl.getProjectToolByType(null);
		assertThat("status", response.getSuccess(), equalTo(false));
		assertThat("Data shouldn't exist: ", response.getData(), equalTo(null));
	}

	/**
	 * 5. To test Both Method of Project Tool Configs, fetch by projectId and Fetch
	 * by toolType and ProjectId
	 *
	 */
	@Test
	public void testgetProjectToolConfigs() {
		String basicProjectConfigId = "5fb364612064a31c9ccd517a";
		String toolType1 = "Jira";
		List<ProjectToolConfig> projectToolConfigDataList = new ArrayList<>();
		List<Subproject> subProjectList = new ArrayList<>();
		List<Subproject> subProjectList1 = new ArrayList<>();
		List<ProjectToolConfigDTO> projectToolConfigDTOList = new ArrayList<>();
		projectToolConfigDataList.add(listProjectTool);
		Subproject subproject = new Subproject();
		Subproject subproject1 = new Subproject();
		subproject.setBasicProjectConfigId(new ObjectId("5fb364612064a31c9ccd517a"));
		subproject.setToolConfigId(new ObjectId("5fa0023dbb5fa781ccd5ac2c"));
		subProjectList.add(subproject);
		subProjectList1.add(subproject1);
		ProjectToolConfigDTO projectToolConfigDTO = new ProjectToolConfigDTO();
		projectToolConfigDTO.setId(listProjectTool.getId().toString());
		projectToolConfigDTO.setBasicProjectConfigId(listProjectTool.getBasicProjectConfigId());
		projectToolConfigDTO.setToolName(toolName);
		projectToolConfigDTO.setConnectionId(new ObjectId("5fb3a6412064a35b8069930a"));
		projectToolConfigDTO.setBasicProjectConfigId(new ObjectId("5fb364612064a31c9ccd517a"));
		projectToolConfigDTO.setBranch("test1");
		projectToolConfigDTO.setJobName("testing1");
		projectToolConfigDTO.setProjectId("1234");
		projectToolConfigDTO.setSubprojects(subProjectList1);
		projectToolConfigDTOList.add(projectToolConfigDTO);
		List<ObjectId> toolConfiragrationIds = projectToolConfigDataList.stream().map(ProjectToolConfig::getId)
				.collect(Collectors.toList());
		when(toolRepositroy.findByBasicProjectConfigId(new ObjectId(basicProjectConfigId)))
				.thenReturn(projectToolConfigDataList);
		when(connectionRepository.findById(new ObjectId("5fb3a6412064a35b8069930a")))
				.thenReturn(Optional.ofNullable(connection));

		when(subProjectRepository.findBytoolConfigIdIn(toolConfiragrationIds)).thenReturn(subProjectList);
		List<ProjectToolConfigDTO> response = projectToolServiceImpl.getProjectToolConfigs(basicProjectConfigId);
		assertEquals(response.get(0).getToolName(), "Test1");
		assertEquals(response.size(), 1);
		assertNotNull(response.get(0).getSubprojects());
		List<ProjectToolConfigDTO> responseByType = projectToolServiceImpl.getProjectToolConfigs(basicProjectConfigId);
		assertEquals(responseByType.get(0).getToolName(), "Test1");
		assertEquals(responseByType.size(), 1);
		assertNotNull(responseByType.get(0).getSubprojects());
	}

	@Test
	public void deleteTool_Success() {
		String toolId = "5fc4d61f80b6350f048a93e3";
		String basicProjectId = "5fc4d61e80b6350f048a9381";

		when(toolRepositroy.findByBasicProjectConfigId(new ObjectId("5fc4d61e80b6350f048a9381")))
				.thenReturn(createMockTools());
		when(dataCleanUpServiceFactory.getService(anyString())).thenReturn(sonarDataCleanUpService);
		doNothing().when(toolRepositroy).deleteById(new ObjectId(toolId));
		doNothing().when(sonarDataCleanUpService).clean(toolId);
		assertTrue(projectToolServiceImpl.deleteTool(basicProjectId, toolId));
	}

	@Test
	public void deleteTool_Exception() {
		String toolId = "5fc4d61f80b6350f048a93e5";
		String basicProjectId = "5fc4d61e80b6350f048a9381";
		assertThrows(ToolNotFoundException.class, () -> {
			when(toolRepositroy.findByBasicProjectConfigId(new ObjectId(basicProjectId))).thenReturn(createMockTools());

			assertTrue(projectToolServiceImpl.deleteTool(basicProjectId, toolId));
		});
	}

	private ProjectToolConfig findToolById(String id) {
		Optional<ProjectToolConfig> first = createMockTools().stream()
				.filter(tool -> tool.getId().toHexString().equals(id)).findFirst();
		return first.orElse(null);
	}

	private List<ProjectToolConfig> createMockTools() {
		ProjectToolConfig sonar1 = new ProjectToolConfig();
		sonar1.setId(new ObjectId("5fc4d61f80b6350f048a93e3"));
		sonar1.setToolName(ProcessorConstants.SONAR);
		sonar1.setBasicProjectConfigId(new ObjectId("5fc4d61e80b6350f048a9381"));
		sonar1.setConnectionId(new ObjectId("5fc4d61f80b6350f048a93da"));
		sonar1.setProjectKey("test-project-one");

		ProjectToolConfig sonar2 = new ProjectToolConfig();
		sonar2.setId(new ObjectId("5fc4d61f80b6350f048a93e2"));
		sonar2.setToolName(ProcessorConstants.SONAR);
		sonar2.setBasicProjectConfigId(new ObjectId("5fc4d61e80b6350f048a9381"));
		sonar2.setConnectionId(new ObjectId("5fc4d61f80b6350f048a93da"));
		sonar2.setProjectKey("test-project-two");

		ProjectToolConfig jira = new ProjectToolConfig();
		jira.setId(new ObjectId("5fc4d61f80b6350f048a93d9"));
		jira.setToolName(ProcessorConstants.JIRA);
		jira.setBasicProjectConfigId(new ObjectId("5fc4d61e80b6350f048a9381"));
		jira.setConnectionId(new ObjectId("5fc4d61e80b6350f048a93ad"));
		jira.setProjectId("1234");
		jira.setProjectKey("JIRA_PROJECT");

		ProjectToolConfig azure = new ProjectToolConfig();
		azure.setId(new ObjectId("5fc4d61f80b6350f048a93d7"));
		azure.setToolName(ProcessorConstants.AZURE);
		azure.setBasicProjectConfigId(new ObjectId("5fc4d61e80b6350f048a9381"));
		azure.setConnectionId(new ObjectId("5fc4d61e80b6350f048a93ae"));
		azure.setProjectId("5678");
		azure.setProjectKey("AZURE_PROJECT");

		return Arrays.asList(jira, azure, sonar1, sonar2);
	}

}
