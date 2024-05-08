package com.publicissapient.kpidashboard.apis.service;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

/**
 * The interface Common service to get maturity level.
 *
 * @author Hiren Babariya
 *
 */
public interface CommonService {

	/**
	 *
	 * @param roles
	 *            roles
	 * @return list of email address based on roles
	 */
	List<String> getEmailAddressBasedOnRoles(List<String> roles);

	/**
	 *
	 * @param emailAddresses
	 * @param customData
	 * @param subjectKey
	 * @param notKey
	 */
	void sendEmailNotification(List<String> emailAddresses, Map<String, String> customData, String subjectKey,
			String notKey);
	/**
	 *
	 * @return String
	 * @throws UnknownHostException
	 */
	String getApiHost() throws UnknownHostException;

	String getUIHost() throws UnknownHostException;

}
