package com.publicissapient.kpidashboard.apis.autoapprove.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.publicissapient.kpidashboard.common.model.rbac.AutoApproveAccessConfig;
import com.publicissapient.kpidashboard.common.model.rbac.AutoApproveAccessConfigDTO;
import com.publicissapient.kpidashboard.common.model.rbac.RoleData;
import com.publicissapient.kpidashboard.common.repository.rbac.AutoApproveAccessConfigRepository;
import com.publicissapient.kpidashboard.common.repository.rbac.RolesRepository;

/**
 * @author sanbhand1
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class AutoApproveAccessServiceImplTest {

	AutoApproveAccessConfig autoApproveAccessConfig;
	List<RoleData> roles = new ArrayList<>();
	ObjectMapper mapper = new ObjectMapper();
	List<AutoApproveAccessConfig> listAutoApproveAccessConfig = new ArrayList<>();
	Optional<AutoApproveAccessConfig> oAutoApproveAccessConfig;
	private AutoApproveAccessConfigDTO autoApproveAccessConfigDTO;
	@InjectMocks
	private AutoApproveAccessServiceImpl autoApproveAccessServiceImpl;
	@Mock
	private AutoApproveAccessService autoApproveAccessService;
	@Mock
	private AutoApproveAccessConfigRepository autoAccessRepository;
	@Mock
	private RolesRepository rolesRepository;
	private String testId;

	@Before
	public void before() {
		// mockMvc = MockMvcBuilders.standaloneSetup(autoApproveController).build();
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
		listAutoApproveAccessConfig.add(autoApproveAccessConfig);

	}

	/**
	 * method includes post processes for test cases
	 */
	@After
	public void after() {
		autoApproveAccessConfigDTO = null;

	}

	@SuppressWarnings("deprecation")
	@Test
	public void testGetAutoApproveConfig() throws Exception {
		when(autoAccessRepository.findAll()).thenReturn(listAutoApproveAccessConfig);
		AutoApproveAccessConfig response = autoApproveAccessServiceImpl.getAutoApproveConfig();
		assertThat("status: ", response.getEnableAutoApprove(), equalTo("true"));
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testSaveAutoApproveRoles() throws Exception {

		when(autoAccessRepository.save(Mockito.any())).thenReturn(autoApproveAccessConfig);
		AutoApproveAccessConfig response = autoApproveAccessServiceImpl.saveAutoApproveConfig(autoApproveAccessConfig);
		assertThat("status: ", response.getEnableAutoApprove(), equalTo("true"));

	}

	@Test
	public void modifyAutoApprovConfigById() throws Exception {
		RoleData roledata = new RoleData();
		when(rolesRepository.findByRoleName(ArgumentMatchers.anyString())).thenReturn(roledata);
		AutoApproveAccessConfig response = autoApproveAccessServiceImpl.modifyAutoApprovConfigById(testId,
				autoApproveAccessConfig);
		assertThat("status: ", response.getEnableAutoApprove(), equalTo("true"));

	}

	@Test
	public void isAutoApproveEnabled_True() {

		when(autoAccessRepository.findAll()).thenReturn(Arrays.asList(autoApproveAccessConfig));

		boolean isEnabled = autoApproveAccessServiceImpl.isAutoApproveEnabled("ROLE_PROJECT_ADMIN");

		assertTrue(isEnabled);

	}

	@Test
	public void isAutoApproveEnabled_False() {

		when(autoAccessRepository.findAll()).thenReturn(Arrays.asList(autoApproveAccessConfig));

		boolean isEnabled = autoApproveAccessServiceImpl.isAutoApproveEnabled("ROLE_PROJECT_VIEWER");

		assertFalse(isEnabled);

	}

	@Test
	public void isAutoApproveEnabled_Null() {

		when(autoAccessRepository.findAll()).thenReturn(null);

		boolean isEnabled = autoApproveAccessServiceImpl.isAutoApproveEnabled("ROLE_PROJECT_VIEWER");

		assertFalse(isEnabled);

	}

}
