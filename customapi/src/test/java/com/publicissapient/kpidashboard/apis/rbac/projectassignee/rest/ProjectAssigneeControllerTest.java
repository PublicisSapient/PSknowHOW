package com.publicissapient.kpidashboard.apis.rbac.projectassignee.rest;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.publicissapient.kpidashboard.apis.rbac.projectassignee.service.ProjectAssigneeService;
import com.publicissapient.kpidashboard.apis.util.TestUtil;
import com.publicissapient.kpidashboard.common.model.application.AssigneeRoles;
import com.publicissapient.kpidashboard.common.model.application.ProjectAssignee;

@RunWith(MockitoJUnitRunner.class)
public class ProjectAssigneeControllerTest {

	private MockMvc mockMvc;

	@InjectMocks
	ProjectAssigneeController projectAssigneeController;

	@Mock
	private ProjectAssigneeService assigneeService;

	private ProjectAssignee projectAssignee;

	private String testId;

	@Before
	public void before() {
		mockMvc = MockMvcBuilders.standaloneSetup(projectAssigneeController).build();
		testId = "5ca455aa70c53c4f50076e34";

		projectAssignee = new ProjectAssignee();
		List<AssigneeRoles> assigneeRolesList = new ArrayList<>();
		AssigneeRoles assigneeRoles1 = new AssigneeRoles();
		assigneeRoles1.setName("testName1");
		assigneeRoles1.setDisplayName("testDisplayName1");
		assigneeRoles1.setRole("testRole1");
		assigneeRolesList.add(assigneeRoles1);

		AssigneeRoles assigneeRoles2 = new AssigneeRoles();
		assigneeRoles1.setName("testName2");
		assigneeRoles1.setDisplayName("testDisplayName2");
		assigneeRoles1.setRole("testRole2");
		assigneeRolesList.add(assigneeRoles2);

		projectAssignee.setBasicProjectConfigId(new ObjectId("5ca455aa70c53c4f50076e34"));
		projectAssignee.setProjectName("testProjectName");
		projectAssignee.setAssigneeRoles(assigneeRolesList);

	}

	@After
	public void after() {
		mockMvc = null;
		testId = null;
	}

	@Test
	public void testGetAllAssignees() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/assignee").contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk());
	}

	@Test
	public void testGetRoleByProjectId() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/assignee/" + testId).contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk());
	}

	@Test
	public void testSaveOrUpdateAssignee() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/assignee/" + testId)
				.content(TestUtil.convertObjectToJsonBytes(projectAssignee))
				.contentType(MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isOk());
	}

	@Test
	public void testGetAllRoles() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/assignee/roles").contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk());
	}

}