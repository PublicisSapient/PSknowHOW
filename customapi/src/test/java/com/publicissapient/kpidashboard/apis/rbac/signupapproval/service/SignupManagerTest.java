package com.publicissapient.kpidashboard.apis.rbac.signupapproval.service;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import com.publicissapient.kpidashboard.common.service.NotificationService;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.auth.model.Authentication;
import com.publicissapient.kpidashboard.apis.auth.repository.AuthenticationRepository;
import com.publicissapient.kpidashboard.apis.auth.service.AuthenticationService;
import com.publicissapient.kpidashboard.apis.common.service.CommonService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.rbac.signupapproval.policy.GrantApprovalListener;
import com.publicissapient.kpidashboard.apis.rbac.signupapproval.policy.RejectApprovalListener;
import com.publicissapient.kpidashboard.common.constant.AuthType;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;
import com.publicissapient.kpidashboard.common.repository.rbac.UserInfoRepository;

@RunWith(MockitoJUnitRunner.class)
public class SignupManagerTest {
	String testId = "5dbfcc60e645ca2ee4075381";

	@InjectMocks
	SignupManager signupManager;
	@Mock
	RejectApprovalListener rejectApprovalListener;
	@Mock
	GrantApprovalListener grantApprovalListener;
	@Mock
	AuthenticationRepository authenticationRepository;
	@Mock
	AuthenticationService authenticationService;
	@Mock
	UserInfoRepository userInfoRepository;
	@Mock
	Authentication authentication;
	@Mock
	CommonService commonService;
	@Mock
	private CustomApiConfig customApiConfig;
	@Mock
	private NotificationService notificationService;

	@Test
	public void testRejectAccessRequestSuccess() throws Exception {
		when(authenticationRepository.save(ArgumentMatchers.any()))
				.thenReturn(authenticationObj(Constant.ACCESS_REQUEST_STATUS_REJECTED, false));
		when(authenticationService.getLoggedInUser()).thenReturn("");
		when(authenticationRepository.findByUsername(ArgumentMatchers.anyString()))
				.thenReturn(authenticationObj(Constant.ACCESS_REQUEST_STATUS_REJECTED, false));
		userInfoRepository.deleteById(new ObjectId(testId));
		when(userInfoRepository.findByUsername(ArgumentMatchers.anyString())).thenReturn(userInfoObj());
		when(authenticationService.getAuthentication(ArgumentMatchers.anyString()))
				.thenReturn(authenticationObj(Constant.ACCESS_REQUEST_STATUS_APPROVED, false));
		when(commonService.getApiHost()).thenReturn("http://www.test.com");
		when(customApiConfig.getNotificationSubject()).thenReturn(testMap("approvalRequest"));
		signupManager.rejectAccessRequest(testId, rejectApprovalListener);
		verify(rejectApprovalListener, atLeastOnce())
				.onSuccess(authenticationObj(Constant.ACCESS_REQUEST_STATUS_REJECTED, false));
	}

	private Map<String, String> testMap(String notificationKey) {
		Map<String, String> map = new HashMap<>();
		map.put("SUBJECT_KEY", notificationKey);
		return map;
	}

	@Test
	public void testGrantAccessRequestFailure() throws Exception {
		when(authenticationService.getAuthentication(ArgumentMatchers.anyString()))
				.thenReturn(authenticationObj(Constant.ACCESS_REQUEST_STATUS_APPROVED, true));
		when(authenticationService.getLoggedInUser()).thenReturn("");
		when(authenticationRepository.findByUsername(ArgumentMatchers.anyString()))
				.thenReturn(authenticationObj(Constant.ACCESS_REQUEST_STATUS_APPROVED, false));
		when(userInfoRepository.findByUsername(ArgumentMatchers.anyString())).thenReturn(userInfoObj());
		signupManager.grantAccess(testId, grantApprovalListener);
		verify(grantApprovalListener, atLeastOnce()).onFailure(
				authenticationObj(Constant.ACCESS_REQUEST_STATUS_APPROVED, true), "Failed to accept the request");
	}

	@Test
	public void testRejectAccessRequestFailure() throws Exception {
		when(authenticationService.getAuthentication(ArgumentMatchers.anyString()))
				.thenReturn(authenticationObj(Constant.ACCESS_REQUEST_STATUS_REJECTED, false));
		when(authenticationService.getLoggedInUser()).thenReturn("");
		when(authenticationRepository.findByUsername(ArgumentMatchers.anyString()))
				.thenReturn(authenticationObj(Constant.ACCESS_REQUEST_STATUS_REJECTED, false));
		when(authenticationRepository.save(ArgumentMatchers.any()))
				.thenReturn(authenticationObj(Constant.ACCESS_REQUEST_STATUS_REJECTED, true));
		when(userInfoRepository.findByUsername(ArgumentMatchers.anyString())).thenReturn(userInfoObj());
		signupManager.rejectAccessRequest(testId, rejectApprovalListener);
		verify(rejectApprovalListener, atLeastOnce()).onFailure(
				authenticationObj(Constant.ACCESS_REQUEST_STATUS_REJECTED, false), "Failed to reject the request");
	}

	@Test
	public void testDeleteAccessRequestById() {
		when(authenticationService.getAuthentication(ArgumentMatchers.anyString()))
				.thenReturn(authenticationObj(Constant.ACCESS_REQUEST_STATUS_REJECTED, false));
		when(userInfoRepository.findByUsername(ArgumentMatchers.anyString())).thenReturn(userInfoObj());
		assertTrue(signupManager.deleteUserById(testId));
	}

	private UserInfo userInfoObj() {
		UserInfo userInfo = new UserInfo();
		userInfo.setId(new ObjectId("61e4f7852747353d4405c762"));
		userInfo.setEmailAddress("testUser@gmail.com");
		userInfo.setUsername("testUser");
		userInfo.setAuthType(AuthType.STANDARD);
		return userInfo;
	}

	private Authentication authenticationObj(String accessRequestStatusRejected, boolean dbStatus) {
		authentication = new Authentication();
		authentication.setId(new ObjectId(testId));
		authentication.setUsername("testUser");
		authentication.setEmail("testUser@gmail.com");
		authentication.setApproved(dbStatus);
		return authentication;
	}

}
