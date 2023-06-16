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

package com.publicissapient.kpidashboard.apis.auth.model;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapUserDetails;

/**
 * Model class to hold user details
 * 
 * @author prijain3
 *
 */
public class CustomUserDetails implements LdapUserDetails {
	private static final long serialVersionUID = 1L;
	private String displayName;
	private String dn;
	private String password;
	private String username;
	private Collection<GrantedAuthority> authorities;
	private boolean accountNonExpired;
	private boolean accountNonLocked;
	private boolean credentialsNonExpired;
	private boolean enabled;
	private int timeBeforeExpiration;
	private int graceLoginsRemaining;
	private String firstName;
	private String middleName;
	private String lastName;
	private String emailAddress;

	/**
	 * get displayName
	 * 
	 * @return the displayName
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * set displayName
	 * 
	 * @param displayName
	 *            the displayName to set
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * get dn
	 * 
	 * @return the dn
	 */
	@Override
	public String getDn() {
		return dn;
	}

	/**
	 * set dn
	 * 
	 * @param dn
	 *            the dn to set
	 */
	public void setDn(String dn) {
		this.dn = dn;
	}

	/**
	 * get password
	 * 
	 * @return the password
	 */
	@Override
	public String getPassword() {
		return password;
	}

	/**
	 * set password
	 * 
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * get username
	 * 
	 * @return the username
	 */
	@Override
	public String getUsername() {
		return username;
	}

	/**
	 * set username
	 * 
	 * @param username
	 *            the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * get authorities
	 * 
	 * @return authorities
	 */
	@Override
	public Collection<GrantedAuthority> getAuthorities() {
		return authorities;
	}

	/**
	 * set authorities
	 * 
	 * @param authorities
	 *            the authorities to set
	 */
	public void setAuthorities(Collection<GrantedAuthority> authorities) {
		this.authorities = authorities;
	}

	/**
	 * get accountNonExpired
	 * 
	 * @return accountNonExpired
	 */
	@Override
	public boolean isAccountNonExpired() {
		return accountNonExpired;
	}

	/**
	 * set accountNonExpired
	 * 
	 * @param accountNonExpired
	 *            the accountNonExpired to set
	 */
	public void setAccountNonExpired(boolean accountNonExpired) {
		this.accountNonExpired = accountNonExpired;
	}

	/**
	 * get accountNonLocked
	 * 
	 * @return the accountNonLocked
	 */
	@Override
	public boolean isAccountNonLocked() {
		return accountNonLocked;
	}

	/**
	 * set accountNonLocked
	 * 
	 * @param accountNonLocked
	 *            the accountNonLocked to set
	 */
	public void setAccountNonLocked(boolean accountNonLocked) {
		this.accountNonLocked = accountNonLocked;
	}

	/**
	 * get credentialsNonExpired
	 * 
	 * @return credentialsNonExpired
	 */
	@Override
	public boolean isCredentialsNonExpired() {
		return credentialsNonExpired;
	}

	/**
	 * set credentialsNonExpired
	 * 
	 * @param credentialsNonExpired
	 *            the credentialsNonExpired to set
	 */
	public void setCredentialsNonExpired(boolean credentialsNonExpired) {
		this.credentialsNonExpired = credentialsNonExpired;
	}

	/**
	 * get enabled
	 * 
	 * @return enabled
	 */
	@Override
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * set enabled
	 * 
	 * @param enabled
	 *            the enabled to set
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * get timeBeforeExpiration
	 * 
	 * @return timeBeforeExpiration
	 */
	public int getTimeBeforeExpiration() {
		return timeBeforeExpiration;
	}

	/**
	 * set timeBeforeExpiration
	 * 
	 * @param timeBeforeExpiration
	 *            the timeBeforeExpiration to set
	 */
	public void setTimeBeforeExpiration(int timeBeforeExpiration) {
		this.timeBeforeExpiration = timeBeforeExpiration;
	}

	/**
	 * get graceLoginsRemaining
	 * 
	 * @return graceLoginsRemaining
	 */
	public int getGraceLoginsRemaining() {
		return graceLoginsRemaining;
	}

	/**
	 * set graceLoginsRemaining
	 * 
	 * @param graceLoginsRemaining
	 *            the graceLoginsRemaining to set
	 */
	public void setGraceLoginsRemaining(int graceLoginsRemaining) {
		this.graceLoginsRemaining = graceLoginsRemaining;
	}

	/**
	 * get firstName
	 * 
	 * @return firstName
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * set firstName
	 * 
	 * @param firstName
	 *            the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * get middleName
	 * 
	 * @return middleName
	 */
	public String getMiddleName() {
		return middleName;
	}

	/**
	 * set middleName
	 * 
	 * @param middleName
	 *            the middleName to set
	 */
	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	/**
	 * get lastName
	 * 
	 * @return lastName
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * set lastName
	 * 
	 * @param lastName
	 *            the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * get emailAddress
	 * 
	 * @return emailAddress
	 */
	public String getEmailAddress() {
		return emailAddress;
	}

	/**
	 * set emailAddress
	 * 
	 * @param emailAddress
	 *            the emailAddress to set
	 */
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	@Override
	public void eraseCredentials() {
		// erase credentials
	}
}
