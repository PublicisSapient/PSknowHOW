package com.publicissapient.kpidashboard.apis.service;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

public interface NotificationService {

	/**
	 * @param roles
	 *          roles
	 * @return list of email address based on roles
	 */
	List<String> getEmailAddressBasedOnRoles(List<String> roles);

	/**
	 * @param emailAddresses
	 * @param customData
	 * @param subjectKey
	 * @param notKey
	 */
	void sendEmailNotification(List<String> emailAddresses, Map<String, String> customData, String subjectKey,
			String notKey);

	void sendUserApprovalEmail(String username, String email);

	void sendRecoverPasswordEmail(String email, String username, String forgotPasswordToken);

	void sendVerificationMailToRegisterUser(String username, String email, String token);

	void sendVerificationFailedMailUser(String username, String email);

	void sendUserPreApprovalRequestEmailToAdmin(String username, String email);

	/**
	 * @return String
	 * @throws UnknownHostException
	 */
	String getApiHost() throws UnknownHostException;
}
