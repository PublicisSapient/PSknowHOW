package com.publicissapient.kpidashboard.apis.rbac.signupapproval.service;

import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.publicissapient.kpidashboard.common.service.NotificationService;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.auth.model.Authentication;
import com.publicissapient.kpidashboard.apis.auth.repository.AuthenticationRepository;
import com.publicissapient.kpidashboard.apis.auth.service.AuthenticationService;
import com.publicissapient.kpidashboard.apis.auth.token.TokenAuthenticationService;
import com.publicissapient.kpidashboard.apis.common.service.CommonService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.NotificationCustomDataEnum;
import com.publicissapient.kpidashboard.apis.rbac.signupapproval.policy.GrantApprovalListener;
import com.publicissapient.kpidashboard.apis.rbac.signupapproval.policy.RejectApprovalListener;
import com.publicissapient.kpidashboard.common.constant.AuthType;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;
import com.publicissapient.kpidashboard.common.repository.rbac.UserInfoRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SignupManager {
	private static final String NOTIFICATION_KEY_SUCCESS = "Approve_User_Success";
	private static final String NOTIFICATION_KEY_REJECT = "Approve_User_Reject";
	private static final String APPROVAL_SUBJECT_KEY = "approvalRequest";
	private static final String PRE_APPROVAL_NOTIFICATION_SUBJECT_KEY = "preApproval";
	private static final String PRE_APPROVAL_NOTIFICATION_KEY = "Pre_Approval";

	@Autowired
	private AuthenticationService authenticationService;
	@Autowired
	private AuthenticationRepository authenticationRepository;
	@Autowired
	private UserInfoRepository userInfoRepository;
	@Autowired
	private CommonService commonService;
	@Autowired
	private CustomApiConfig customApiConfig;
	@Autowired
	private NotificationService notificationService;
	@Autowired
	private KafkaTemplate<String, Object> kafkaTemplate;
	@Autowired
	private TokenAuthenticationService tokenAuthenticationService;

	/**
	 * when grant is provided to user
	 *
	 * @param username
	 * @param grantApprovalListener
	 */
	public void grantAccess(String username, GrantApprovalListener grantApprovalListener) {
		String superAdminEmail;
		String loggedInUser = authenticationService.getLoggedInUser();
		if (checkForLdapUser(loggedInUser)) {
			superAdminEmail = userInfoRepository.findByUsername(loggedInUser).getEmailAddress();
		} else {
			superAdminEmail = authenticationRepository.findByUsername(loggedInUser).getEmail();
		}
		Authentication authentication = getAuthenticationByUserName(username);
		if (authentication.isApproved()) {
			if (grantApprovalListener != null) {
				grantApprovalListener.onFailure(authentication, "Failed to accept the request");
			}
		} else {
			if (grantApprovalListener != null) {
				authentication.setApproved(true);
				Authentication updateAuthenticationApprovalStatus = updateAuthenticationApprovalStatus(authentication);
				grantApprovalListener.onSuccess(updateAuthenticationApprovalStatus);
				tokenAuthenticationService.updateExpiryDate(username, LocalDateTime.now().toString());
				List<String> emailAddresses = new ArrayList<>();
				emailAddresses.add(updateAuthenticationApprovalStatus.getEmail());
				String serverPath = getServerPath();
				Map<String, String> customData = createCustomData("", "", serverPath, superAdminEmail);
				sendEmailNotification(emailAddresses, customData, APPROVAL_SUBJECT_KEY, NOTIFICATION_KEY_SUCCESS);
			}
		}

	}

	private boolean checkForLdapUser(String userName) {
		UserInfo loggedInUser = userInfoRepository.findByUsername(userName);
		return loggedInUser.getAuthType().equals(AuthType.LDAP);

	}

	/**
	 * common method to get server path
	 *
	 * @return
	 */
	private String getServerPath() {
		String serverPath = "";
		try {
			serverPath = commonService.getApiHost();
		} catch (UnknownHostException e) {
			log.error("ApproveRequestController: Server Host name is not bind with Approval Request mail ");
		}
		return serverPath;
	}

	/**
	 * send notification to super admin for approval and notification to user for
	 * the status of the request
	 *
	 * @param emailAddresses
	 * @param customData
	 * @param subjectKey
	 * @param notKey
	 */
	public void sendEmailNotification(List<String> emailAddresses, Map<String, String> customData, String subjectKey,
									  String notKey) {
		Map<String, String> notificationSubjects = customApiConfig.getNotificationSubject();
		if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(emailAddresses)
				&& MapUtils.isNotEmpty(notificationSubjects)) {
			String subject = notificationSubjects.get(subjectKey);
			log.info("Notification message sent to kafka with key : {}", notKey);
			String templateKey = customApiConfig.getMailTemplate().getOrDefault(notKey,"");
			notificationService.sendNotificationEvent(emailAddresses, customData, subject, notKey,
					customApiConfig.getKafkaMailTopic(),customApiConfig.isNotificationSwitch(),kafkaTemplate,templateKey,customApiConfig.isMailWithoutKafka());
		} else {
			log.error("Notification Event not sent : No email address found "
					+ "or Property - notificationSubject.accessRequest not set in property file ");
		}
	}

	/**
	 * * create custom data for email
	 *
	 * @param username
	 * @param email
	 * @param serverPath
	 * @param adminEmail
	 * @return
	 */
	private Map<String, String> createCustomData(String username, String email, String serverPath, String adminEmail) {
		Map<String, String> customData = new HashMap<>();
		customData.put(NotificationCustomDataEnum.USER_NAME.getValue(), username);
		customData.put(NotificationCustomDataEnum.USER_EMAIL.getValue(), email);
		customData.put(NotificationCustomDataEnum.SERVER_HOST.getValue(), serverPath);
		customData.put(NotificationCustomDataEnum.ADMIN_EMAIL.getValue(), adminEmail);
		return customData;
	}

	/**
	 * @param username
	 * @param listener
	 */
	public void rejectAccessRequest(String username, RejectApprovalListener listener) {
		String superAdminEmail;
		String loggedInUser = authenticationService.getLoggedInUser();
		if (checkForLdapUser(loggedInUser)) {
			superAdminEmail = userInfoRepository.findByUsername(loggedInUser).getEmailAddress();
		} else {
			superAdminEmail = authenticationRepository.findByUsername(loggedInUser).getEmail();
		}
		Authentication authentication = getAuthenticationByUserName(username);
		Authentication updatedAuthenticationRequest = updateAuthenticationApprovalStatus(authentication);
		if (updatedAuthenticationRequest.isApproved()) {
			if (listener != null) {
				listener.onFailure(authentication, "Failed to reject the request");
			}
		} else {
			if (listener != null) {
				List<String> emailAddresses = new ArrayList<>();
				emailAddresses.add(authentication.getEmail());
				String serverPath = getServerPath();
				Map<String, String> customData = createCustomData("", "", serverPath, superAdminEmail);
				sendEmailNotification(emailAddresses, customData, APPROVAL_SUBJECT_KEY, NOTIFICATION_KEY_REJECT);
				deleteUserById(username);
				listener.onSuccess(updatedAuthenticationRequest);
			}
		}

	}

	/**
	 * @param username
	 * @return
	 */
	private Authentication getAuthenticationByUserName(String username) {
		return authenticationService.getAuthentication(username);
	}

	/**
	 * delete user info and aunthentication from collections
	 *
	 * @param username
	 * @return
	 */
	public boolean deleteUserById(String username) {
		boolean isDeleted = false;
		Authentication authenticationById = getAuthenticationByUserName(username);
		if (authenticationById == null) {
			log.info("Sign up request is not deleted for the user: ", username);
		} else {
			userInfoRepository.deleteById(userInfoRepository.findByUsername(authenticationById.getUsername()).getId());
			authenticationRepository.delete(authenticationById);
			isDeleted = true;
			log.info("Sign up request is deleted for the user: ", username);
		}
		return isDeleted;
	}

	/**
	 * @param authentication
	 * @return
	 */
	private Authentication updateAuthenticationApprovalStatus(Authentication authentication) {
		return authenticationRepository.save(authentication);
	}

	/**
	 * when new register user then send approval notification email to super admin
	 *
	 * @param username
	 * @param email
	 */
	public void sendUserPreApprovalRequestEmailToAdmin(String username, String email) {
		List<String> emailAddresses = commonService
				.getEmailAddressBasedOnRoles(Arrays.asList(Constant.ROLE_SUPERADMIN));
		String serverPath = getServerPath();
		Map<String, String> customData = createCustomData(username, email, serverPath, "");
		sendEmailNotification(emailAddresses, customData, PRE_APPROVAL_NOTIFICATION_SUBJECT_KEY,
				PRE_APPROVAL_NOTIFICATION_KEY);
	}

}
