package com.publicissapient.kpidashboard.apis.capacity.rest;

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

import com.publicissapient.kpidashboard.apis.capacity.service.CapacityMasterService;
import com.publicissapient.kpidashboard.apis.util.TestUtil;
import com.publicissapient.kpidashboard.common.constant.Role;
import com.publicissapient.kpidashboard.common.model.application.AssigneeCapacity;
import com.publicissapient.kpidashboard.common.model.application.CapacityMaster;

@RunWith(MockitoJUnitRunner.class)
public class ProjectAssigneeControllerTest {

	private MockMvc mockMvc;

	@InjectMocks
	ProjectAssigneeController projectAssigneeController;

	@Mock
	private CapacityMasterService assigneeService;

	private CapacityMaster capacityMaster;

	@Before
	public void before() {
		mockMvc = MockMvcBuilders.standaloneSetup(projectAssigneeController).build();

		capacityMaster = new CapacityMaster();
		List<AssigneeCapacity> assigneeRolesList = new ArrayList<>();
		AssigneeCapacity assigneeRoles1 = new AssigneeCapacity();
		assigneeRoles1.setUserId("testName1");
		assigneeRoles1.setUserName("testDisplayName1");
		assigneeRoles1.setRole(Role.BACKEND_DEVELOPER);
		assigneeRolesList.add(assigneeRoles1);

		AssigneeCapacity assigneeRoles2 = new AssigneeCapacity();
		assigneeRoles1.setUserId("testName2");
		assigneeRoles1.setUserName("testDisplayName2");
		assigneeRoles1.setRole(Role.BACKEND_DEVELOPER);
		assigneeRolesList.add(assigneeRoles2);

		capacityMaster.setBasicProjectConfigId(new ObjectId("5ca455aa70c53c4f50076e34"));
		capacityMaster.setAssigneeCapacity(assigneeRolesList);

	}

	@After
	public void after() {
		mockMvc = null;
	}

	@Test
	public void testSaveOrUpdateAssignee() throws Exception {
		mockMvc.perform(
				MockMvcRequestBuilders.post("/assignee").content(TestUtil.convertObjectToJsonBytes(capacityMaster))
						.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk());

	}

	@Test
	public void testGetAllRoles() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/assignee/roles").contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk());
	}

}