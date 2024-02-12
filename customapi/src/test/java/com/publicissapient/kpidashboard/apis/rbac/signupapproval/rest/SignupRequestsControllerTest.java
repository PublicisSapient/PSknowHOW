package com.publicissapient.kpidashboard.apis.rbac.signupapproval.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.publicissapient.kpidashboard.apis.auth.token.CookieUtil;
import com.publicissapient.kpidashboard.apis.common.service.impl.UserInfoServiceImpl;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
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
import com.publicissapient.kpidashboard.apis.auth.model.Authentication;
import com.publicissapient.kpidashboard.apis.auth.service.AuthenticationService;
import com.publicissapient.kpidashboard.apis.rbac.signupapproval.service.SignupManager;

import javax.servlet.http.Cookie;

@RunWith(MockitoJUnitRunner.class)
public class SignupRequestsControllerTest {

	Authentication authentication;
	ObjectMapper mapper = new ObjectMapper();
	@Mock
	AuthenticationService authenticationService;
	@Mock
	SignupManager signupManager;
	@Mock
	private CustomApiConfig customApiConfig;
	@Mock
	UserInfoServiceImpl userInfoService;
	private MockMvc mockMvc;
	private String testId;
	@Mock
	private CookieUtil cookieUtil;
	@InjectMocks
	private SignupRequestsController signupRequestsController;

	@Before
	public void before() {
		testId = "5dbfcc60e645ca2ee4075381";
		authentication = new Authentication();
		authentication.setId(new ObjectId(testId));
		authentication.setUsername("testUser");
		authentication.setEmail("testUser@gmail.com");
		authentication.setApproved(false);

		mockMvc = MockMvcBuilders.standaloneSetup(signupRequestsController).build();
	}

	@After
	public void after() {
		mockMvc = null;
	}

	/**
	 * method to get all unapproved requests when CA switch is Off
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetUnApprovedRequests() throws Exception {
		when(customApiConfig.isCentralAuthSwitch()).thenReturn(false);
		mockMvc.perform(MockMvcRequestBuilders.get("/userapprovals").contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk());
	}

	/**
	 * method to get all unapproved requests when CA switch is On
	 * @throws Exception
	 */
	//@Test
	public void testGetAllUnApprovedRequestsCASwitchOn() throws Exception {
		when(cookieUtil.getAuthCookie(any())).thenReturn(new Cookie("authCookie", "token"));
		when(customApiConfig.isCentralAuthSwitch()).thenReturn(true);
		mockMvc.perform(MockMvcRequestBuilders.get("/userapprovals").contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk());
	}

	/**
	 * method to test GET /grantrequest/all restPoint ;
	 *
	 * Get all signup requests
	 *
	 * @throws Exception
	 */
	@Test
	public void testGetAllRequests() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/userapprovals/all").contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk());
	}

	/**
	 * method to test PUT /grantrequest/{id} restPoint ; Modify access request with
	 * id
	 *
	 * @throws Exception
	 */
	//@Test
	public void testModifyAccessRequest_Approved() throws Exception {
		String request = "{\n" + "    \"status\": \"Approved\",\n" + "    \"role\": \"ROLE_PROJECT_ADMIN\",\n"
				+ "    \"message\": \"\"\n" + "}";
		when(cookieUtil.getAuthCookie(any())).thenReturn(new Cookie("authCookie", "token"));
		when(customApiConfig.isCentralAuthSwitch()).thenReturn(true);
		mockMvc.perform(MockMvcRequestBuilders.put("/userapprovals/testUser").content(request)
				.contentType(MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isOk());

	}

	//@Test
	public void testModifyAccessRequest_Reject() throws Exception {
		String request = "{\n" + "    \"status\": \"Rejected\",\n" + "    \"role\": \"ROLE_PROJECT_ADMIN\",\n"
				+ "    \"message\": \"\"\n" + "}";
		when(cookieUtil.getAuthCookie(any())).thenReturn(new Cookie("authCookie", "token"));
		when(customApiConfig.isCentralAuthSwitch()).thenReturn(true);
		when(userInfoService.deleteFromCentralAuthUser(any(), any())).thenReturn(true);
		mockMvc.perform(MockMvcRequestBuilders.put("/userapprovals/testUser").content(request)
				.contentType(MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isOk());

	}

}
