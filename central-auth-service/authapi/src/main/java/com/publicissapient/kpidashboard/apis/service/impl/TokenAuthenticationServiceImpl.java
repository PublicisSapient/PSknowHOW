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

package com.publicissapient.kpidashboard.apis.service.impl;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;
import com.publicissapient.kpidashboard.apis.config.AuthProperties;
import com.publicissapient.kpidashboard.apis.entity.ApiKey;
import com.publicissapient.kpidashboard.apis.entity.Resource;
import com.publicissapient.kpidashboard.apis.entity.User;
import com.publicissapient.kpidashboard.apis.entity.UserRole;
import com.publicissapient.kpidashboard.apis.entity.UserToken;
import com.publicissapient.kpidashboard.apis.errors.GenericException;
import com.publicissapient.kpidashboard.apis.errors.NoSSOImplementationFoundException;
import com.publicissapient.kpidashboard.apis.repository.ApiKeyRepository;
import com.publicissapient.kpidashboard.apis.repository.RoleRepository;
import com.publicissapient.kpidashboard.apis.repository.UserTokenRepository;
import com.publicissapient.kpidashboard.apis.service.ApiTokenService;
import com.publicissapient.kpidashboard.apis.service.MessageService;
import com.publicissapient.kpidashboard.apis.service.ResourceService;
import com.publicissapient.kpidashboard.apis.service.TokenAuthenticationService;
import com.publicissapient.kpidashboard.apis.service.UserRoleService;
import com.publicissapient.kpidashboard.apis.service.UserService;
import com.publicissapient.kpidashboard.apis.util.CookieUtil;
import com.publicissapient.kpidashboard.common.model.GenerateAPIKeyResponseDTO;
import com.publicissapient.kpidashboard.common.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.util.CommonUtils;
import com.publicissapient.kpidashboard.common.util.EncryptionException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of {@link TokenAuthenticationService}
 */
@Component
@Transactional
@Slf4j
public class TokenAuthenticationServiceImpl implements TokenAuthenticationService {

	public static final String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
	private static final String ROLES_CLAIM = "roles";
	private static final String DETAILS_CLAIM = "details";
	@Autowired
	UserService userService;
	@Autowired
	AuthProperties tokenAuthProperties;
	@Autowired
	private CookieUtil cookieUtil;

	@Autowired
	private UserTokenRepository userTokenRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private ApiKeyRepository apiKeyRepository;

	@Autowired
	private ApiTokenService apiTokenService;

	@Autowired
	private ResourceService resourceService;

	@Autowired
	private UserRoleService userRoleService;

	private MessageService messageService;

	@Autowired
	public TokenAuthenticationServiceImpl(@Lazy MessageService messageService) {
		this.messageService = messageService;
	}

	@Override
	public String addAuthentication(HttpServletResponse response, Authentication authentication) {
		Date expirationDate = new Date(System.currentTimeMillis() + tokenAuthProperties.getExpirationTime());
		String jwt = Jwts.builder().setSubject(userService.getUsername(authentication))
				.claim(DETAILS_CLAIM, authentication.getDetails())
				.claim(ROLES_CLAIM, getRoles(authentication.getAuthorities())).setExpiration(expirationDate)
				.signWith(SignatureAlgorithm.HS512, tokenAuthProperties.getSecret()).compact();

		saveUserToken(authentication, jwt, expirationDate);
		Cookie cookie = cookieUtil.createAccessTokenCookie(jwt);
		response.addCookie(cookie);
		cookieUtil.addSameSiteCookieAttribute(response);
		return jwt;
	}

	@Transactional
	public void saveUserToken(Authentication authentication, String jwt, Date expirationDate) {
		UserToken data = new UserToken();
		String username = userService.getUsername(authentication);
		data.setUsername(username);
		data.setToken(jwt);
		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
		String expiryDate = dateFormat.format(expirationDate);
		data.setExpiryDate(expiryDate);
		userTokenRepository.deleteByUsername(username);
		userTokenRepository.save(data);
	}

	@Override
	public Authentication getAuthentication(HttpServletRequest request, HttpServletResponse response) {
		if (tokenAuthProperties.isSsoLogin()) {
			throw new NoSSOImplementationFoundException("No implementation is found for SSO");
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
		PreAuthenticatedAuthenticationToken authentication = null;
		String username = getSubject(token);
		if (null != username) {
			Claims claims = Jwts.parser().setSigningKey(tokenAuthProperties.getSecret()).parseClaimsJws(token)
					.getBody();
			Collection<? extends GrantedAuthority> authorities = getAuthorities(
					claims.get(ROLES_CLAIM, Collection.class));
			authentication = new PreAuthenticatedAuthenticationToken(userService.getAuthentication(username), null,
					authorities);
			authentication.setDetails(claims.get(DETAILS_CLAIM));
			List<UserToken> userTokenData = userTokenRepository.findAllByUsername(username);
			response.setHeader(CommonUtils.AUTH_DETAILS_UPDATED_FLAG,
					setUpdateAuthFlag(userTokenData, claims.getExpiration()));
		}
		return authentication;
	}

	public String getSubject(String token) {
		String username = null;
		try {
			Claims claims = parseClaims(token);
			username = claims.getSubject();
		} catch (ExpiredJwtException e) {
			throw new GenericException("token will be expired");
		}
		return username;
	}

	public Object getClaim(String token, String claimKey) {
		Object claim = null;
		try {
			Claims claims = parseClaims(token);
			claim = claims.get(claimKey);
		} catch (ExpiredJwtException e) {
			throw new GenericException("token will be expired");
		}
		return claim;
	}

	private Claims parseClaims(String token) throws ExpiredJwtException {
		return Jwts.parser().setSigningKey(tokenAuthProperties.getSecret()).parseClaimsJws(token).getBody();
	}

	@Override
	public String setUpdateAuthFlag(List<UserToken> userTokenDataList, Date tokenExpiration) {
		UserToken userTokenData = getLatestUser(userTokenDataList);
		if (userTokenData != null) {
			SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
			String expiryDate = dateFormat.format(tokenExpiration);
			DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern(TIME_FORMAT).optionalStart()
					.appendPattern(".").appendFraction(ChronoField.MICRO_OF_SECOND, 1, 9, false).optionalEnd()
					.toFormatter();
			if (LocalDateTime.parse(expiryDate, formatter).isAfter(LocalDateTime.now())) {
				return Boolean.toString(true);
			}
		}
		return Boolean.toString(false);
	}

	private UserToken getLatestUser(List<UserToken> userTokenDataList) {
		if (CollectionUtils.isEmpty(userTokenDataList)) {
			return null;
		}
		List<UserToken> dataList = userTokenDataList.stream().filter(data -> data.getExpiryDate() != null)
				.collect(Collectors.toList());
		DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern(TIME_FORMAT).optionalStart()
				.appendPattern(".").appendFraction(ChronoField.MICRO_OF_SECOND, 1, 9, false).optionalEnd()
				.toFormatter();
		return dataList.stream().max(Comparator.comparing(data -> LocalDateTime.parse(data.getExpiryDate(), formatter)))
				.orElse(null);
	}

	/**
	 * Gets roles.
	 *
	 * @param authorities
	 * @return
	 */
	private Collection<String> getRoles(Collection<? extends GrantedAuthority> authorities) {
		Collection<String> roles = new HashSet<>();
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
	public void updateExpiryDate(String username, String expiryDate) {
		List<UserToken> dataList = userTokenRepository.findAllByUsername(username);
		dataList.stream().forEach(data -> data.setExpiryDate(expiryDate));
		userTokenRepository.saveAll(dataList);
	}

	@Override
	public UserToken getLatestTokenByUser(String userName) {
		List<UserToken> userTokenDataList = userTokenRepository.findAllByUsername(userName);
		return getLatestUser(userTokenDataList);
	}

	/**
	 * root user can only one generate token. if same resourceName request again
	 * generate token then previously generated token will be updated and token will
	 * be expired based on configure days.
	 *
	 * @param resourceName
	 *            resourceName
	 * @return ServiceResponse
	 */
	@Override
	public ServiceResponse generateAndSaveToken(String resourceName) {
		GenerateAPIKeyResponseDTO generateAPIKeyResponseDTO = new GenerateAPIKeyResponseDTO();
		String loginUser = userService.getLoggedInUser();
		User loginUserDetail = userService.validateUser(loginUser);
		validateLoggedUserRole(resourceName, loginUser);
		Resource resource = resourceService.validateResource(resourceName);
		try {
			ApiKey apiKeyTokenExist = apiTokenService.getApiToken(resourceName);
			if (Objects.nonNull(apiKeyTokenExist)) {
				apiTokenService.updateAPIKeyForResource(apiKeyTokenExist, resourceName, loginUserDetail);
				generateAPIKeyResponseDTO.setResource(resource.getName());
				generateAPIKeyResponseDTO.setKey(apiKeyTokenExist.getKey());
				generateAPIKeyResponseDTO.setExpiryDate(apiKeyTokenExist.getExpiryDate().toString());
				generateAPIKeyResponseDTO.setCreatedDate(apiKeyTokenExist.getCreatedDate().toString());
				generateAPIKeyResponseDTO.setCreatedBy(apiKeyTokenExist.getCreatedBy().getUsername());
				generateAPIKeyResponseDTO.setModifiedDate(apiKeyTokenExist.getModifiedDate().toString());
				generateAPIKeyResponseDTO.setModifiedBy(apiKeyTokenExist.getCreatedBy().getUsername());
				return new ServiceResponse(true, messageService.getMessage("success_apiKey_updated"),
						generateAPIKeyResponseDTO);
			} else {
				ApiKey apiKeyToken = apiTokenService.generateNewAPIKeyForResource(loginUserDetail, resource);
				generateAPIKeyResponseDTO.setResource(resource.getName());
				generateAPIKeyResponseDTO.setKey(apiKeyToken.getKey());
				generateAPIKeyResponseDTO.setExpiryDate(apiKeyToken.getExpiryDate().toString());
				generateAPIKeyResponseDTO.setCreatedDate(apiKeyToken.getCreatedDate().toString());
				generateAPIKeyResponseDTO.setCreatedBy(apiKeyToken.getCreatedBy().getUsername());
				return new ServiceResponse(true, messageService.getMessage("success_apiKey_generated"),
						generateAPIKeyResponseDTO);
			}
		} catch (EncryptionException e) {
			return new ServiceResponse(false, messageService.getMessage("error_apiKey_generated"), null);
		}
	}

	/**
	 *
	 * @param resourceName
	 *            resourceName
	 * @param loginUser
	 *            loginUser
	 */
	private void validateLoggedUserRole(String resourceName, String loginUser) {
		List<UserRole> userPermissionList = userRoleService.findUserRoleByUsernameAndResource(loginUser, resourceName);
		List<UserRole> rootUser = userPermissionList.stream().filter(userRole -> userRole.getRole().isRootUser())
				.collect(Collectors.toList());
		if (rootUser == null) {
			log.error(messageService.getMessage("error_invalid_permission") + loginUser);
			throw new GenericException(messageService.getMessage("error_invalid_permission"));
		}
	}

}
