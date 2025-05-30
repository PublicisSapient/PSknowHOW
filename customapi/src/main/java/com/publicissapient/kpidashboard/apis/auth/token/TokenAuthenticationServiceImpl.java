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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;
import com.publicissapient.kpidashboard.apis.abac.ProjectAccessManager;
import com.publicissapient.kpidashboard.apis.auth.AuthProperties;
import com.publicissapient.kpidashboard.apis.auth.service.AuthenticationService;
import com.publicissapient.kpidashboard.apis.common.service.UserInfoService;
import com.publicissapient.kpidashboard.apis.common.service.UsersSessionService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.errors.NoSSOImplementationFoundException;
import com.publicissapient.kpidashboard.common.constant.AuthType;
import com.publicissapient.kpidashboard.common.model.rbac.ProjectsForAccessRequest;
import com.publicissapient.kpidashboard.common.model.rbac.RoleWiseProjects;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;
import com.publicissapient.kpidashboard.common.model.rbac.UserTokenData;
import com.publicissapient.kpidashboard.common.repository.rbac.UserTokenReopository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/** Implementation of {@link TokenAuthenticationService} */
@Component
@Slf4j
public class TokenAuthenticationServiceImpl implements TokenAuthenticationService {
	private static final String AUTH_RESPONSE_HEADER = "X-Authentication-Token";
	private static final String ROLES_CLAIM = "roles";
	private static final String DETAILS_CLAIM = "details";
	private static final String USER_NAME = "username";
	private static final String USER_EMAIL = "emailAddress";
	private static final String PROJECTS_ACCESS = "projectsAccess";
	private static final Object USER_AUTHORITIES = "authorities";
	public static final String EXCEPTION_MSG = "No implementation is found for SSO";
	@Autowired
	AuthenticationService authenticationService;
	@Autowired
	UserInfoService userInfoService;
	@Autowired
	CustomApiConfig customApiConfig;
	@Autowired
	private AuthProperties tokenAuthProperties;
	@Autowired
	private UserTokenReopository userTokenReopository;
	@Autowired
	private ProjectAccessManager projectAccessManager;
	@Autowired
	private CookieUtil cookieUtil;
	@Autowired
	private UsersSessionService usersSessionService;

	@Override
	public void addAuthentication(HttpServletResponse response, Authentication authentication) {
		String jwt = createJwtToken(authentication);
		UserTokenData data = new UserTokenData();
		data.setUserName(authentication.getName());
		data.setUserToken(jwt);
		userTokenReopository.deleteAllByUserName(authentication.getName());
		userTokenReopository.save(data);
		response.addHeader(AUTH_RESPONSE_HEADER, jwt);
		Cookie cookie = cookieUtil.createAccessTokenCookie(jwt);
		cookie.setSecure(true);
		response.addCookie(cookie);
		cookieUtil.addSameSiteCookieAttribute(response);
	}

	public String createJwtToken(Authentication authentication) {
		return Jwts.builder().setSubject(authentication.getName()).claim(DETAILS_CLAIM, authentication.getDetails())
				.claim(ROLES_CLAIM, getRoles(authentication.getAuthorities()))
				.setExpiration(new Date(System.currentTimeMillis() + tokenAuthProperties.getExpirationTime()))
				.signWith(SignatureAlgorithm.HS512, tokenAuthProperties.getSecret()).compact();
	}

	@Override
	public boolean isJWTTokenExpired(String jwtToken) {
		Claims decodedJWT = Jwts.parser().setSigningKey(tokenAuthProperties.getSecret()).parseClaimsJws(jwtToken).getBody();
		Date expiresAt = decodedJWT.getExpiration();
		return new Date().after(expiresAt);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Authentication getAuthentication(HttpServletRequest httpServletRequest, HttpServletResponse response) {

		if (customApiConfig.isSsoLogin()) {
			throw new NoSSOImplementationFoundException(EXCEPTION_MSG);
		} else {
			Cookie authCookieToken = cookieUtil.getAuthCookie(httpServletRequest);
			if (Objects.nonNull(authCookieToken)) {
				return createAuthentication(authCookieToken.getValue(), response);
			} else {
				return null;
			}
		}
	}

	@Override
	public String getAuthToken(HttpServletRequest httpServletRequest) {

		if (customApiConfig.isSsoLogin()) {
			throw new NoSSOImplementationFoundException(EXCEPTION_MSG);
		} else {
			Cookie authCookieToken = cookieUtil.getAuthCookie(httpServletRequest);
			if (Objects.nonNull(authCookieToken)) {
				return authCookieToken.getValue();
			} else {
				return null;
			}
		}
	}

	@Override
	public Authentication validateAuthentication(HttpServletRequest request, HttpServletResponse response) {

		if (customApiConfig.isSsoLogin()) {
			throw new NoSSOImplementationFoundException(EXCEPTION_MSG);
		} else {
			Cookie authCookie = cookieUtil.getAuthCookie(request);
			if (StringUtils.isBlank(authCookie.getValue())) {
				return null;
			}

			String token = authCookie.getValue();

			if (null == token) {
				return null;
			}
			return createAuthentication(token, response);
		}
	}

	private Authentication createAuthentication(String token, HttpServletResponse response) {
		try {
			Claims claims = Jwts.parser().setSigningKey(tokenAuthProperties.getSecret()).parseClaimsJws(token).getBody();
			String username = claims.getSubject();
			Collection<? extends GrantedAuthority> authorities = getAuthorities(claims.get(ROLES_CLAIM, Collection.class));
			PreAuthenticatedAuthenticationToken authentication = new PreAuthenticatedAuthenticationToken(username, null,
					authorities);
			authentication.setDetails(claims.get(DETAILS_CLAIM));
			Date tokenCreationDate = new Date(claims.getExpiration().getTime() - tokenAuthProperties.getExpirationTime());
			Date tokenExpiration = claims.getExpiration();
			boolean isJWTTokenExpired = new Date().after(tokenExpiration);
			LocalDateTime lastLogout = usersSessionService.getLastLogoutTimeOfUser(username);
			ZonedDateTime tokenCreationZonedDateTime = tokenCreationDate.toInstant().atZone(ZoneId.systemDefault());
			ZonedDateTime lastLogoutZonedDateTime = lastLogout != null ? lastLogout.atZone(ZoneId.systemDefault()) : null;
			boolean isJWTTokenValid = lastLogoutZonedDateTime == null ||
					tokenCreationZonedDateTime.isAfter(lastLogoutZonedDateTime);
			if (isJWTTokenExpired || !isJWTTokenValid) {
				response.setStatus(HttpStatus.UNAUTHORIZED.value());
				return null;
			}

			return authentication;

		} catch (ExpiredJwtException e) {
			log.error("JWT filtering failed with message: {}", e.getMessage());
			return null;
		}
	}

	public UserTokenData getLatestUser(List<UserTokenData> userTokenDataList) {
		if (CollectionUtils.isEmpty(userTokenDataList)) {
			return null;
		}
		List<UserTokenData> dataList = userTokenDataList.stream().filter(data -> data.getExpiryDate() != null)
				.collect(Collectors.toList());
		DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern(DateUtil.TIME_FORMAT).optionalStart()
				.appendPattern(".").appendFraction(ChronoField.MICRO_OF_SECOND, 1, 9, false).optionalEnd().toFormatter();
		return dataList.stream().max(Comparator.comparing(data -> LocalDateTime.parse(data.getExpiryDate(), formatter)))
				.orElse(null);
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

	@Override
	public void updateExpiryDate(String username, String expiryDate) {
		List<UserTokenData> dataList = userTokenReopository.findAllByUserName(username);
		dataList.stream().forEach(data -> data.setExpiryDate(expiryDate));
		userTokenReopository.saveAll(dataList);
	}

	@Override
	public JSONObject getOrSaveUserByToken(HttpServletRequest request, Authentication authentication) {
		UserInfo userInfo = new UserInfo();
		if (cookieUtil.getAuthCookie(request) != null) {
			String userName = request.getHeader(USER_NAME) != null ? request.getHeader(USER_NAME) : authentication.getName();
			List<UserTokenData> userTokenDataList = userTokenReopository.findAllByUserName(userName);
			UserTokenData userTokenData = getLatestUser(userTokenDataList);
			if (userTokenData != null) {
				updateExpiryDate(userTokenData.getUserName(), LocalDateTime.now().toString());
			} else {
				userTokenReopository.deleteAllByUserName(userName);
				userTokenData = new UserTokenData(userName, cookieUtil.getAuthCookie(request).getValue(),
						LocalDateTime.now().toString());
				userTokenReopository.save(userTokenData);
			}
			List<String> authorities = new ArrayList<>(getRoles(authentication.getAuthorities()));
			AuthType authType = AuthType.valueOf(authentication.getDetails().toString());
			userInfo = userInfoService.getOrSaveUserInfo(userTokenData.getUserName(), authType, authorities);
		}
		return createAuthDetailsJson(userInfo);
	}

	@Override
	public JSONObject createAuthDetailsJson(UserInfo userInfo) {
		if (userInfo != null) {
			JSONObject json = new JSONObject();
			json.put(USER_NAME, userInfo.getUsername());
			json.put(USER_EMAIL, userInfo.getEmailAddress());
			json.put(USER_AUTHORITIES, userInfo.getAuthorities());
			List<RoleWiseProjects> projectAccessesWithRole = projectAccessManager
					.getProjectAccessesWithRole(userInfo.getUsername());
			json.put(PROJECTS_ACCESS, projectAccessesWithRole);
			return json;
		}
		return new JSONObject();
	}

	@Override
	public String getUserNameFromToken(String jwtToken) {
		Claims claims = Jwts.parser().setSigningKey(tokenAuthProperties.getSecret()).parseClaimsJws(jwtToken).getBody();
		return claims.getSubject();
	}
}
