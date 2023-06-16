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

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import com.google.common.collect.Sets;
import com.publicissapient.kpidashboard.common.constant.AuthType;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class AuthenticationFixture {

	private static final String ROLES_CLAIM = "roles";
	private static final String DETAILS_CLAIM = "details";

	public static void createAuthentication(String username) {
		Collection<GrantedAuthority> authorities = Sets.newHashSet(new SimpleGrantedAuthority("ROLE_ADMIN"));
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, "password",
				authorities);
		auth.setDetails(AuthType.STANDARD.name());
		SecurityContext context = new SecurityContextImpl();
		context.setAuthentication(auth);
		SecurityContextHolder.setContext(context);
	}

	public static Authentication getAuthentication(String username) {
		createAuthentication(username);
		return SecurityContextHolder.getContext().getAuthentication();
	}

	public static String getJwtToken(String username, String secret, long expirationTime) {
		Authentication authentication = getAuthentication(username);
		List<String> authorities = Arrays.asList("ROLE_VIEWER", "ROLE_SUPERADMIN");
		return Jwts.builder().setSubject(authentication.getName()).claim(DETAILS_CLAIM, authentication.getDetails())
				.claim(ROLES_CLAIM, authorities).setExpiration(new Date(System.currentTimeMillis() + expirationTime))
				.signWith(SignatureAlgorithm.HS512, secret).compact();
	}
}
