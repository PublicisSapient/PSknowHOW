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

package com.publicissapient.kpidashboard.apis.auth.ldap;

import java.util.Collection;

import javax.naming.NamingException;

import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;

import com.publicissapient.kpidashboard.apis.auth.model.CustomUserDetails;

import lombok.extern.slf4j.Slf4j;

/**
 * Map user details
 * 
 * @author prijain3
 *
 */
@Configuration
@Slf4j
public class CustomUserDetailsContextMapper extends LdapUserDetailsMapper {

	public static final String INITIALS = "initials";
	public static final String GIVEN_NAME = "givenName";
	public static final String DISPLAY_NAME = "displayName";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CustomUserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection authorities) {

		LdapUserDetailsImpl ldapUserDetailsImpl = (LdapUserDetailsImpl) super.mapUserFromContext(ctx, username,
				authorities);
		CustomUserDetails customUserDetails = new CustomUserDetails();
		customUserDetails.setAccountNonExpired(ldapUserDetailsImpl.isAccountNonExpired());
		customUserDetails.setAccountNonLocked(ldapUserDetailsImpl.isAccountNonLocked());
		customUserDetails.setCredentialsNonExpired(ldapUserDetailsImpl.isCredentialsNonExpired());
		customUserDetails.setEnabled(ldapUserDetailsImpl.isEnabled());
		customUserDetails.setUsername(ldapUserDetailsImpl.getUsername());
		customUserDetails.setAuthorities(ldapUserDetailsImpl.getAuthorities());

		log.info("DN from ctx: {}", ctx.getDn());
		try {
			if (ctx.getAttributes().get(GIVEN_NAME) != null) {
				log.info(String.format("givenName from attr: %s", ctx.getAttributes().get(GIVEN_NAME).get()));
				customUserDetails.setFirstName(String.format("%s", ctx.getAttributes().get(GIVEN_NAME).get()));
			}

			if (ctx.getAttributes().get(INITIALS) != null) {
				log.info("initials from attr: {}", ctx.getAttributes().get(INITIALS).get());
				customUserDetails.setMiddleName(String.format("%s", ctx.getAttributes().get(INITIALS).get()));
			}

			if (ctx.getAttributes().get("sn") != null) {
				log.info("sn from attr: {}", ctx.getAttributes().get("sn").get());
				customUserDetails.setLastName(String.format("%s", ctx.getAttributes().get("sn").get()));
			}

			if (ctx.getAttributes().get(DISPLAY_NAME) != null) {
				log.info("displayName from attr: {}", ctx.getAttributes().get(DISPLAY_NAME).get());
				customUserDetails.setDisplayName(String.format("%s", ctx.getAttributes().get(DISPLAY_NAME).get()));
			}

			if (ctx.getAttributes().get("mail") != null) {
				log.info("mail from attr: {}", ctx.getAttributes().get("mail").get());
				customUserDetails.setEmailAddress(String.format("%s", ctx.getAttributes().get("mail").get()));
			}
		} catch (NamingException e) {
			log.warn("NamingException: {}", e.getMessage(), e);
		}
		log.info("Attributes size: {}", ctx.getAttributes().size());

		return customUserDetails;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
		// default
	}
}
