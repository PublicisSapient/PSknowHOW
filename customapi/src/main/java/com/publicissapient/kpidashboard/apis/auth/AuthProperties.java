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
	// fields are required for standard, LDAP and Crowd sso

	private static final String STANDARD = "STANDARD";
	private static final String LDAP = "LDAP";
	private static final String CROWDSSO = "CROWDSSO";

	private Long expirationTime;
	private String secret;
	private String ldapUserDnPattern;
	private String ldapServerUrl;
	private List<AuthType> authenticationProviders = Lists.newArrayList();

	private String adDomain;
	private String adLionsDomain;
	private String adRootDn;
	private String adLionsRootDn;
	private String adUrl;
	private String adLionsUrl;

	private String ldapBindUser;
	private String ldapBindPass;
	private String providers;

	private Integer accountLockedThreshold;
	private int accountLockedPeriod;

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
	 * Gets ldap user dn pattern.
	 *
	 * @return the ldap user dn pattern
	 */
	public String getLdapUserDnPattern() {
		return ldapUserDnPattern;
	}

	/**
	 * Sets ldap user dn pattern.
	 *
	 * @param ldapUserDnPattern
	 *            the ldap user dn pattern
	 */
	public void setLdapUserDnPattern(String ldapUserDnPattern) {
		this.ldapUserDnPattern = ldapUserDnPattern;
	}

	/**
	 * Gets ldap server url.
	 *
	 * @return the ldap server url
	 */
	public String getLdapServerUrl() {
		return ldapServerUrl;
	}

	/**
	 * Sets ldap server url.
	 *
	 * @param ldapServerUrl
	 *            the ldap server url
	 */
	public void setLdapServerUrl(String ldapServerUrl) {
		this.ldapServerUrl = ldapServerUrl;
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
	 * Gets ad domain.
	 *
	 * @return the ad domain
	 */
	public String getAdDomain() {
		return adDomain;
	}

	/**
	 * Sets ad domain.
	 *
	 * @param adDomain
	 *            the ad domain
	 */
	public void setAdDomain(String adDomain) {
		this.adDomain = adDomain;
	}

	/**
	 * Gets ad root dn.
	 *
	 * @return the ad root dn
	 */
	public String getAdRootDn() {
		return adRootDn;
	}

	/**
	 * Sets ad root dn.
	 *
	 * @param adRootDn
	 *            the ad root dn
	 */
	public void setAdRootDn(String adRootDn) {
		this.adRootDn = adRootDn;
	}

	/**
	 * Gets ad url.
	 *
	 * @return the ad url
	 */
	public String getAdUrl() {
		return adUrl;
	}

	/**
	 * Sets ad url.
	 *
	 * @param adUrl
	 *            the ad url
	 */
	public void setAdUrl(String adUrl) {
		this.adUrl = adUrl;
	}

	/**
	 * Gets ldap bind user.
	 *
	 * @return the ldap bind user
	 */
	public String getLdapBindUser() {
		return ldapBindUser;
	}

	/**
	 * Sets ldap bind user.
	 *
	 * @param ldapBindUser
	 *            the ldap bind user
	 */
	public void setLdapBindUser(String ldapBindUser) {
		this.ldapBindUser = ldapBindUser;
	}

	/**
	 * Gets ldap bind pass.
	 *
	 * @return the ldap bind pass
	 */
	public String getLdapBindPass() {
		return ldapBindPass;
	}

	/**
	 * Sets ldap bind pass.
	 *
	 * @param ldapBindPass
	 *            the ldap bind pass
	 */
	public void setLdapBindPass(String ldapBindPass) {
		this.ldapBindPass = ldapBindPass;
	}

	/**
	 * Gets ad lions domain.
	 *
	 * @return the ad lions domain
	 */
	public String getAdLionsDomain() {
		return adLionsDomain;
	}

	/**
	 * Sets ad lions domain.
	 *
	 * @param adLionsDomain
	 *            the ad lions domain
	 */
	public void setAdLionsDomain(String adLionsDomain) {
		this.adLionsDomain = adLionsDomain;
	}

	/**
	 * Gets ad lions url.
	 *
	 * @return the ad lions url
	 */
	public String getAdLionsUrl() {
		return adLionsUrl;
	}

	/**
	 * Sets ad lions url.
	 *
	 * @param adLionsUrl
	 *            the ad lions url
	 */
	public void setAdLionsUrl(String adLionsUrl) {
		this.adLionsUrl = adLionsUrl;
	}

	/**
	 * Gets ad lions root dn.
	 *
	 * @return the ad lions root dn
	 */
	public String getAdLionsRootDn() {
		return adLionsRootDn;
	}

	/**
	 * Sets ad lions root dn.
	 *
	 * @param adLionsRootDn
	 *            the ad lions root dn
	 */
	public void setAdLionsRootDn(String adLionsRootDn) {
		this.adLionsRootDn = adLionsRootDn;
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
				} else if (LDAP.equalsIgnoreCase(providers)) {
					authenticationProviders.add(AuthType.LDAP);
				} else if (CROWDSSO.equalsIgnoreCase(providers)) {
					authenticationProviders.add(AuthType.CROWDSSO);
				}
			}

		}
	}

}
