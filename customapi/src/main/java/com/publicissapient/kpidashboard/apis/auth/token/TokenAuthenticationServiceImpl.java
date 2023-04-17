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

package com.publicissapient.kpidashboard.apis.auth.token;


import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;
import com.publicissapient.kpidashboard.apis.abac.ProjectAccessManager;
import com.publicissapient.kpidashboard.apis.auth.AuthProperties;
import com.publicissapient.kpidashboard.apis.auth.service.AuthenticationService;
import com.publicissapient.kpidashboard.common.model.rbac.ProjectsForAccessRequest;
import com.publicissapient.kpidashboard.common.model.rbac.RoleWiseProjects;
import com.publicissapient.kpidashboard.common.model.rbac.UserTokenData;
import com.publicissapient.kpidashboard.common.repository.rbac.UserTokenReopository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * Implementation of {@link TokenAuthenticationService}
 */
@Component
public class TokenAuthenticationServiceImpl implements TokenAuthenticationService {

	private static final String AUTH_RESPONSE_HEADER = "X-Authentication-Token";
	private static final String ROLES_CLAIM = "roles";
	private static final String DETAILS_CLAIM = "details";

	@Autowired
	private AuthProperties tokenAuthProperties;
	@Autowired
	private UserTokenReopository userTokenReopository;

	@Autowired
	private ProjectAccessManager projectAccessManager;

	@Autowired
	AuthenticationService authenticationService;

	@Autowired
	private CookieUtil cookieUtil;

	@Override
	public void addAuthentication(HttpServletResponse response, Authentication authentication) {
		String jwt = Jwts.builder().setSubject(authentication.getName())
				.claim(DETAILS_CLAIM, authentication.getDetails())
				.claim(ROLES_CLAIM, getRoles(authentication.getAuthorities()))
				.setExpiration(new Date(System.currentTimeMillis() + tokenAuthProperties.getExpirationTime()))
				.signWith(SignatureAlgorithm.HS512, tokenAuthProperties.getSecret()).compact();
		UserTokenData data = new UserTokenData();
		data.setUserName(authentication.getName());
		data.setUserToken(jwt);
		userTokenReopository.save(data);
		response.addHeader(AUTH_RESPONSE_HEADER, jwt);
		Cookie cookie = cookieUtil.createAccessTokenCookie(jwt);
		response.addCookie(cookie);
		cookieUtil.addSameSiteCookieAttribute(response);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Authentication getAuthentication(HttpServletRequest request) {

		Cookie authCookie = cookieUtil.getAuthCookie(request);
		if (StringUtils.isBlank(authCookie.getValue())) {
			return null;
		}

		String token = authCookie.getValue();

		UserTokenData data = null;

		data = userTokenReopository.findByUserToken(token);

		if (null == data) {
			return null;
		}

		try {
			Claims claims = Jwts.parser().setSigningKey(tokenAuthProperties.getSecret()).parseClaimsJws(token)
					.getBody();
			String username = claims.getSubject();
			Collection<? extends GrantedAuthority> authorities = getAuthorities(
					claims.get(ROLES_CLAIM, Collection.class));
			PreAuthenticatedAuthenticationToken authentication = new PreAuthenticatedAuthenticationToken(username, null,
					authorities);
			authentication.setDetails(claims.get(DETAILS_CLAIM));

			return authentication;

		} catch (ExpiredJwtException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<String> getUserProjects() {
		List<RoleWiseProjects> projectAccessesWithRole = projectAccessManager
				.getProjectAccessesWithRole(authenticationService.getLoggedInUser());
		if (CollectionUtils.isNotEmpty(projectAccessesWithRole)) {
			return projectAccessesWithRole.stream().flatMap(roleWiseProjects -> roleWiseProjects.getProjects().stream())
					.map(ProjectsForAccessRequest::getProjectId).collect(Collectors.toSet());
		}
		return new HashSet<>();
	}

	/**
	 * Gets roles.
	 * 
	 * @param authorities
	 * @return
	 */
	private Collection<String> getRoles(Collection<? extends GrantedAuthority> authorities) {
		Collection<String> roles = Sets.newHashSet();
		authorities.forEach(authority -> roles.add(authority.getAuthority()));

		return roles;
	}

	/**
	 * Gets authories.
	 * 
	 * @param roles
	 * @return
	 */
	private Collection<? extends GrantedAuthority> getAuthorities(Collection<String> roles) {
		Collection<GrantedAuthority> authorities = Sets.newHashSet();
		roles.forEach(role -> authorities.add(new SimpleGrantedAuthority(role)));

		return authorities;
	}

	@Override
	public List<RoleWiseProjects> refreshToken(HttpServletRequest req, HttpServletResponse resp) {
		return projectAccessManager.getProjectAccessesWithRole(authenticationService.getLoggedInUser());

	}

	@Override
	public void invalidateAuthToken(List<String> users) {
		userTokenReopository.deleteByUserNameIn(users);
	}

}
