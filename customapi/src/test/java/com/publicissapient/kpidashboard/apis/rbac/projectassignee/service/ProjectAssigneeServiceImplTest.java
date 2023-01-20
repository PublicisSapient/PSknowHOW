package com.publicissapient.kpidashboard.apis.rbac.projectassignee.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

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

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.application.AssigneeRoles;
import com.publicissapient.kpidashboard.common.model.application.ProjectAssignee;
import com.publicissapient.kpidashboard.common.repository.rbac.ProjectAssigneeRepository;

@RunWith(MockitoJUnitRunner.class)
public class ProjectAssigneeServiceImplTest {

	@Mock
	private ProjectAssigneeRepository projectAssigneeRepository;

	@InjectMocks
	private ProjectAssigneeServiceImpl projectAssigneeService;

	private ProjectAssignee projectAssignee;

	private ProjectAssignee projectAssignee2;

	private String testId;

	List<ProjectAssignee> projectAssigneeList;

	@Before
	public void before() {

		testId = "5ca455aa70c53c4f50076e34";
		projectAssigneeList = new ArrayList<>();
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

		projectAssignee = new ProjectAssignee();
		projectAssignee.setBasicProjectConfigId(new ObjectId("5ca455aa70c53c4f50076e34"));
		projectAssignee.setProjectName("testProjectName");
		projectAssignee.setAssigneeRoles(assigneeRolesList);

		projectAssignee2 = new ProjectAssignee();
		projectAssignee2.setBasicProjectConfigId(new ObjectId("5da46000e645ca33dc927b4a"));
		projectAssignee2.setProjectName("testProjectName2");
		projectAssignee2.setAssigneeRoles(assigneeRolesList);

		projectAssigneeList.add(projectAssignee);
	}

	@After
	public void cleanup() {

		testId = "5ca455aa70c53c4f50076e34";
		projectAssigneeList = new ArrayList<>();
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

		projectAssigneeList.add(projectAssignee);
	}

	@Test
	public void testGetAllAssignee1() {
		when(projectAssigneeRepository.findAll()).thenReturn(null);
		ServiceResponse response = projectAssigneeService.getAllAssignees();
		assertEquals(false, response.getSuccess());
		assertEquals(null, response.getData());
	}

	/**
	 * 2. database call has no records and returns empty array
	 *
	 */
	@Test
	public void testGetAllAssignee2() {
		when(projectAssigneeRepository.findAll()).thenReturn(projectAssigneeList);
		ServiceResponse response = projectAssigneeService.getAllAssignees();
		assertEquals(true, response.getSuccess());
		assertEquals(projectAssigneeList, response.getData());
	}

	@Test
	public void testGetAssigneeByProjectConfigId1() {
		ServiceResponse response = projectAssigneeService.getAssigneeByProjectConfigId(testId);
		assertEquals(false, response.getSuccess());
		assertEquals(null, response.getData());
	}

	@Test
	public void testGetAssigneeByProjectConfigId2() {
		when(projectAssigneeRepository.findByBasicProjectConfigId(new ObjectId(testId))).thenReturn(projectAssignee);
		ServiceResponse response = projectAssigneeService.getAssigneeByProjectConfigId(testId);
		assertEquals(true, response.getSuccess());
		assertEquals(projectAssignee, response.getData());
	}

	@Test
	public void testUpdateOrSaveAssineeByProjectConfigId1() {
		when(projectAssigneeRepository.findByBasicProjectConfigId(new ObjectId(testId))).thenReturn(null);
		ServiceResponse response = projectAssigneeService.updateOrSaveAssineeByProjectConfigId(testId, projectAssignee);
		assertEquals(true, response.getSuccess());
		assertEquals(projectAssignee, response.getData());
	}

	@Test
	public void testUpdateOrSaveAssineeByProjectConfigId2() {
		when(projectAssigneeRepository.findByBasicProjectConfigId(new ObjectId(testId))).thenReturn(projectAssignee);
		ServiceResponse response = projectAssigneeService.updateOrSaveAssineeByProjectConfigId(testId, projectAssignee);
		assertEquals(true, response.getSuccess());
		assertEquals(projectAssignee, response.getData());
	}

}