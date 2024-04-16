/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.apis.auth;

import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.publicissapient.kpidashboard.common.constant.AuthType;

import lombok.extern.slf4j.Slf4j;

/**
 * This class maps authentication properties to object
 */
@Slf4j
@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {// NOPMD
	// do not remove NOPMD comment. This is for ignoring TooManyFields.
	// fields are required for standard and Crowd sso

	private static final String STANDARD = "STANDARD";
	private static final String CROWDSSO = "CROWDSSO";

	private Long expirationTime;
	private String secret;
	private List<AuthType> authenticationProviders = Lists.newArrayList();
	private String providers;

	private Integer accountLockedThreshold;
	private int accountLockedPeriod;

	// --- auth-N-auth changes starts here ----
	private String centralAuthBaseURL;
	private String resourceAPIKey;
	private String userLogoutEndPoint;
	private String deleteUserEndpoint;
	private String fetchUserDetailsEndPoint;
	private String fetchPendingUsersApprovalEndPoint;
	private String updateUserApprovalStatus;
	private String changePasswordEndPoint;
	private String resourceName;

	private boolean subDomainCookie;
	private String domain;

	/**
	 * Gets expiration time.
	 *
	 * @return the expiration time
	 */
	public Long getExpirationTime() {
		return expirationTime;
	}

	/**
	 * Sets expiration time.
	 *
	 * @param expirationTime
	 *            the expiration time
	 */
	public void setExpirationTime(Long expirationTime) {
		this.expirationTime = expirationTime;
	}

	/**
	 * Gets secret.
	 *
	 * @return the secret
	 */
	public String getSecret() {
		return secret;
	}

	/**
	 * Sets secret.
	 *
	 * @param secret
	 *            the secret
	 */
	public void setSecret(String secret) {
		this.secret = secret;
	}

	/**
	 * Gets authentication providers.
	 *
	 * @return the authentication providers
	 */
	public List<AuthType> getAuthenticationProviders() {
		return authenticationProviders;
	}

	/**
	 * Sets authentication providers.
	 *
	 * @param authenticationProviders
	 *            the authentication providers
	 */
	public void setAuthenticationProviders(List<AuthType> authenticationProviders) {
		this.authenticationProviders = authenticationProviders;
	}

	/**
	 * Gets providers.
	 *
	 * @return the providers
	 */
	public String getProviders() {
		return providers;
	}

	/**
	 * Sets providers.
	 *
	 * @param providers
	 *            the providers
	 */
	public void setProviders(String providers) {
		this.providers = providers;
	}

	/**
	 * Gets account locked threshold.
	 *
	 * @return the account locked threshold
	 */
	public Integer getAccountLockedThreshold() {
		return accountLockedThreshold;
	}

	/**
	 * Sets account locked threshold.
	 *
	 * @param accountLockedThreshold
	 *            the account locked threshold
	 */
	public void setAccountLockedThreshold(Integer accountLockedThreshold) {
		this.accountLockedThreshold = accountLockedThreshold;
	}

	/**
	 * Gets account locked period.
	 *
	 * @return the account locked period
	 */
	public int getAccountLockedPeriod() {
		return accountLockedPeriod;
	}

	/**
	 * Sets account locked period.
	 *
	 * @param accountLockedPeriod
	 *            the account locked period
	 */
	public void setAccountLockedPeriod(int accountLockedPeriod) {
		this.accountLockedPeriod = accountLockedPeriod;
	}

	/**
	 * Get central auth base url
	 *
	 * @return
	 */
	public String getCentralAuthBaseURL() {
		return centralAuthBaseURL;
	}

	/**
	 * Set central auth base url
	 *
	 * @param centralAuthBaseURL
	 */
	public void setCentralAuthBaseURL(String centralAuthBaseURL) {
		this.centralAuthBaseURL = centralAuthBaseURL;
	}

	/**
	 * Set resourceName
	 *
	 * @param resourceName
	 */
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	/**
	 * get resource Name
	 * 
	 * @return resourceName
	 */
	public String getResourceName() {
		return resourceName;
	}

	/**
	 * Set central auth userinfo url
	 *
	 * @param fetchUserDetailsEndPoint
	 */
	public void setFetchUserDetailsEndPoint(String fetchUserDetailsEndPoint) {
		this.fetchUserDetailsEndPoint = fetchUserDetailsEndPoint;
	}

	/**
	 * get central auth userinfo url
	 * 
	 * @return
	 */
	public String getFetchUserDetailsEndPoint() {
		return fetchUserDetailsEndPoint;
	}

	/**
	 * Set central auth user approval url
	 *
	 * @param updateUserApprovalStatus
	 */
	public void setUpdateUserApprovalStatus(String updateUserApprovalStatus) {
		this.updateUserApprovalStatus = updateUserApprovalStatus;
	}

	/**
	 * get central auth update user approval url
	 * 
	 * @return
	 */
	public String getUpdateUserApprovalStatus() {
		return updateUserApprovalStatus;
	}

	/**
	 * get central auth userinfo url
	 *
	 * @return
	 */
	public String getChangePasswordEndPoint() {
		return changePasswordEndPoint;
	}

	public void setChangePasswordEndPoint(String changePasswordEndPoint) {
		this.changePasswordEndPoint = changePasswordEndPoint;
	}

	/**
	 * Set central auth userinfo url
	 *
	 * @param fetchPendingUsersApprovalEndPoint
	 */
	public void setFetchPendingUsersApprovalEndPoint(String fetchPendingUsersApprovalEndPoint) {
		this.fetchPendingUsersApprovalEndPoint = fetchPendingUsersApprovalEndPoint;
	}

	/**
	 * get central auth userinfo url
	 *
	 * @return
	 */
	public String getFetchPendingUsersApprovalEndPoint() {
		return fetchPendingUsersApprovalEndPoint;
	}

	/**
	 * get resourceAPIKey
	 * 
	 * @return
	 */
	public String getResourceAPIKey() {
		return resourceAPIKey;
	}

	/**
	 * get user logout end point for central auth
	 * 
	 * @return
	 */
	public String getUserLogoutEndPoint() {
		return userLogoutEndPoint;
	}

	public void setUserLogoutEndPoint(String userLogoutEndPoint) {
		this.userLogoutEndPoint = userLogoutEndPoint;
	}

	/**
	 * delete user for central auth
	 * @return
	 */
	public String getDeleteUserEndpoint() {
		return deleteUserEndpoint;
	}

	public void setDeleteUserEndpoint(String deleteUserEndpoint) {
		this.deleteUserEndpoint = deleteUserEndpoint;
	}

	/**
	 * Set resourceAPIKey
	 *
	 * @param resourceAPIKey
	 */
	public void setResourceAPIKey(String resourceAPIKey) {
		this.resourceAPIKey = resourceAPIKey;
	}

	public boolean isSubDomainCookie() {
		return subDomainCookie;
	}

	public void setSubDomainCookie(boolean subDomainCookie) {
		this.subDomainCookie = subDomainCookie;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	/**
	 * Apply defaults if needed.
	 */
	@PostConstruct
	public void applyDefaultsIfNeeded() {
		if (getSecret() == null) {
			log.info("No JWT secret found in configuration, generating random secret by default.");
			setSecret(UUID.randomUUID().toString().replace("-", ""));
		}

		if (getExpirationTime() == null) {
			log.info("No JWT expiration time found in configuration, setting to one day.");
			setExpirationTime((long) 1000 * 60 * 60 * 24);
		}

		if (CollectionUtils.isEmpty(authenticationProviders)) {
			if (providers == null || providers.equalsIgnoreCase("")) {
				authenticationProviders.add(AuthType.CROWDSSO);
			} else {
				if (STANDARD.equalsIgnoreCase(providers)) {
					authenticationProviders.add(AuthType.STANDARD);
				}  else if (CROWDSSO.equalsIgnoreCase(providers)) {
					authenticationProviders.add(AuthType.CROWDSSO);
				}
			}

		}
	}

}
