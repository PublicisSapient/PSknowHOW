package com.publicissapient.kpidashboard.apis.autoapprove.rest;

import static org.mockito.Mockito.when;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.publicissapient.kpidashboard.apis.autoapprove.service.AutoApproveAccessService;
import com.publicissapient.kpidashboard.common.model.rbac.AutoApproveAccessConfig;
import com.publicissapient.kpidashboard.common.model.rbac.AutoApproveAccessConfigDTO;
import com.publicissapient.kpidashboard.common.model.rbac.RoleData;

/**
 * @author sanbhand1
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class AutoApproveAccessControllerTest {

	AutoApproveAccessConfig autoApproveAccessConfig;
	List<RoleData> roles = new ArrayList<>();
	ObjectMapper mapper = new ObjectMapper();
	private MockMvc mockMvc;
	private AutoApproveAccessConfigDTO autoApproveAccessConfigDTO;
	@InjectMocks
	private AutoApproveAccessController autoApproveController;
	@Mock
	private AutoApproveAccessService autoApproveAccessService;
	private String testId;

	@Before
	public void before() {
		mockMvc = MockMvcBuilders.standaloneSetup(autoApproveController).build();
		autoApproveAccessConfigDTO = new AutoApproveAccessConfigDTO();
		testId = "5da46000e645ca33dc927b4a";
		RoleData roleData = new RoleData();
		roleData.setId(new ObjectId("5da46000e645ca33dc927b4a"));
		roleData.setRoleName("ROLE_PROJECT_ADMIN");
		roleData.setIsDeleted("false");
		roles.add(roleData);
		autoApproveAccessConfigDTO.setEnableAutoApprove("true");
		autoApproveAccessConfigDTO.setRoles(roles);
		autoApproveAccessConfig = new AutoApproveAccessConfig();
		autoApproveAccessConfig.setEnableAutoApprove("true");
		autoApproveAccessConfig.setRoles(roles);

	}

	/**
	 * method includes post processes for test cases
	 */
	@After
	public void after() {
		mockMvc = null;
		autoApproveAccessConfigDTO = null;

	}

	@Test
	public void testGetAutoApproveConfig() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get("/autoapprove").contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk());
	}

	@Test
	public void testGetAutoApproveConfigWithDAte() throws Exception {
		when(autoApproveAccessService.getAutoApproveConfig()).thenReturn(autoApproveAccessConfig);
		mockMvc.perform(MockMvcRequestBuilders.get("/autoapprove").contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk());
	}

	@Test
	public void testSaveAutoApproveRoles() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/autoapprove")
				.content(mapper.writeValueAsString(autoApproveAccessConfigDTO))
				.contentType(MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isOk());

	}

	@Test
	public void modifyAutoApprovConfigById() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.put("/autoapprove/" + testId)
				.content(mapper.writeValueAsString(autoApproveAccessConfigDTO))
				.contentType(MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isOk());

	}

	@Test
	public void modifyAutoApprovConfigByInvalidId() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.put("/autoapprove/" + "6000e645ca33dc927b4a")
				.content(mapper.writeValueAsString(autoApproveAccessConfigDTO))
				.contentType(MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isOk());

	}
}
