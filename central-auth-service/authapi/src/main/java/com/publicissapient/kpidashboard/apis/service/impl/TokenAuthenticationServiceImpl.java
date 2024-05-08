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

import com.google.common.collect.Sets;
import com.publicissapient.kpidashboard.apis.config.AuthProperties;
import com.publicissapient.kpidashboard.apis.entity.*;
import com.publicissapient.kpidashboard.apis.enums.AuthType;
import com.publicissapient.kpidashboard.apis.errors.GenericException;
import com.publicissapient.kpidashboard.apis.errors.NoSSOImplementationFoundException;
import com.publicissapient.kpidashboard.apis.repository.ApiKeyRepository;
import com.publicissapient.kpidashboard.apis.repository.UserTokenRepository;
import com.publicissapient.kpidashboard.apis.service.*;
import com.publicissapient.kpidashboard.apis.util.CookieUtil;
import com.publicissapient.kpidashboard.common.model.GenerateAPIKeyResponseDTO;
import com.publicissapient.kpidashboard.common.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.util.CommonUtils;
import com.publicissapient.kpidashboard.common.util.Encryption;
import com.publicissapient.kpidashboard.common.util.EncryptionException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of {@link TokenAuthenticationService}
 */
@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class TokenAuthenticationServiceImpl implements TokenAuthenticationService {

	public static final String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
	private static final String ROLES_CLAIM = "roles";
	private static final String DETAILS_CLAIM = "details";

	// ? do we need this mapper?
	final ModelMapper modelMapper = new ModelMapper();

	private final UserService userService;

	private final AuthProperties authProperties;

	private final UserTokenRepository userTokenRepository;

	private final ApiKeyRepository apiKeyRepository;

	private final ResourceService resourceService;

	private final UserRoleService userRoleService;

	private final MessageService messageService;

	public String extractUsernameFromEmail(String email) {
		if (Objects.nonNull(email) &&
			email.contains("@")) {
			return email.substring(
					0,
					email.indexOf("@")
			);
		}

		return email;
	}

	@Override
	public String saveSamlData(
			Saml2AuthenticatedPrincipal principal,
			HttpServletResponse response
	) {
		String userEmail = principal.getName();
		// TOOD: remove log
		log.info("Logged in as: " +
				 userEmail);

		String username = extractUsernameFromEmail(userEmail);

		// * create the business logic in the UserServiceImpl:
		// - extract the username from the emailAddress
		// - generate the authToken with that username as the subject
		// - generate two cookies:
		// 1. authCookie- httpOnly=true, contains the authToken and will be used in the jwt filtering of the BE apps
		// 2. authCookie_EXPIRY - httpOnly=false, doesn't contain the authToken, but has the same expiry date as
		// 	the previous one and will be used by the FE apps guards in order to check if the user is logged in or not

		String jwt = createApplicationJWT(username, AuthType.SAML);

		CookieUtil.addCookie(response,
							 CookieUtil.COOKIE_NAME,
							 jwt,
							 authProperties.getAuthCookieDuration(),
							 authProperties.getDomain()
		);
		CookieUtil.addCookie(response,
							 CookieUtil.EXPIRY_COOKIE_NAME,
							 authProperties
									 .getAuthCookieDuration()
									 .toString(),
							 false,
							 authProperties.getAuthCookieDuration(),
							 authProperties.getDomain()
		);

		return userEmail;
	}

	@Override
	public String createApplicationJWT(@NotNull String subject, AuthType authType) {
		Date expirationDate = new Date(System.currentTimeMillis() +
									   authProperties.getExpirationTime());

		return Jwts
				.builder()
				.setSubject(subject)
				.claim(
						DETAILS_CLAIM,
						authType
				)
				.claim(
						ROLES_CLAIM,
						new HashSet<>()
				)
				.setExpiration(expirationDate)
				.signWith(
						SignatureAlgorithm.HS512,
						authProperties.getSecret()
				)
				.compact();
	}

	@Override
	public String addAuthentication(
			HttpServletResponse response,
			Authentication authentication
	) {
		Date expirationDate = new Date(System.currentTimeMillis() +
									   authProperties.getExpirationTime());
		String jwt = Jwts
				.builder()
				.setSubject(userService.getUsername(authentication))
				.claim(
						DETAILS_CLAIM,
						authentication.getDetails()
				)
				.claim(
						ROLES_CLAIM,
						getRoles(authentication.getAuthorities())
				)
				.setExpiration(expirationDate)
				.signWith(
						SignatureAlgorithm.HS512,
						authProperties.getSecret()
				)
				.compact();

		saveUserToken(
				authentication,
				jwt,
				expirationDate
		);

		CookieUtil.addCookie(response,
							 CookieUtil.COOKIE_NAME,
							 jwt,
							 authProperties.getAuthCookieDuration(),
							 authProperties.getDomain()
		);
		CookieUtil.addCookie(response,
							 CookieUtil.EXPIRY_COOKIE_NAME,
							 "",
							 false,
							 authProperties.getAuthCookieDuration(),
							 authProperties.getDomain()
		);
		// TODO: do we need this same site attribute?
		//		cookieUtil.addSameSiteCookieAttribute(response);
		return jwt;
	}

	@Transactional
	public void saveUserToken(
			Authentication authentication,
			String jwt,
			Date expirationDate
	) {
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
	public Authentication getAuthentication(
			HttpServletRequest request,
			HttpServletResponse response
	) {
		if (authProperties.isSsoLogin()) {
			// ? what is this
			throw new NoSSOImplementationFoundException("No implementation is found for SSO");
		} else {
			Optional<Cookie> authCookie = CookieUtil.getCookie(
					request,
					CookieUtil.COOKIE_NAME
			);
			if (authCookie.isEmpty()) {
				return null;
			}

			String token = authCookie
					.get()
					.getValue();

			if (null ==
				token) {
				return null;
			}
			return createAuthentication(
					token,
					response
			);
		}
	}

	private Authentication createAuthentication(
			String token,
			HttpServletResponse response
	) {
		PreAuthenticatedAuthenticationToken authentication = null;
		String username = getSubject(token);
		if (Objects.nonNull(username)) {
			Claims claims = parseClaims(token);
			Collection<? extends GrantedAuthority> authorities = getAuthorities(claims.get(
					ROLES_CLAIM,
					Collection.class
			));
			authentication = new PreAuthenticatedAuthenticationToken(userService.getAuthentication(username),
																	 null,
																	 authorities
			);
			authentication.setDetails(claims.get(DETAILS_CLAIM));
			List<UserToken> userTokenData = userTokenRepository.findAllByUsername(username);
			response.setHeader(
					CommonUtils.AUTH_DETAILS_UPDATED_FLAG,
					setUpdateAuthFlag(
							userTokenData,
							claims.getExpiration()
					)
			);
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

	public Object getClaim(
			String token,
			String claimKey
	) {
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
		return Jwts
				.parser()
				.setSigningKey(authProperties.getSecret())
				.parseClaimsJws(token)
				.getBody();
	}

	@Override
	public String setUpdateAuthFlag(
			List<UserToken> userTokenDataList,
			Date tokenExpiration
	) {
		UserToken userTokenData = getLatestUser(userTokenDataList);
		if (userTokenData !=
			null) {
			SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
			String expiryDate = dateFormat.format(tokenExpiration);
			DateTimeFormatter formatter = new DateTimeFormatterBuilder()
					.appendPattern(TIME_FORMAT)
					.optionalStart()
					.appendPattern(".")
					.appendFraction(ChronoField.MICRO_OF_SECOND,
									1,
									9,
									false
					)
					.optionalEnd()
					.toFormatter();
			if (LocalDateTime
					.parse(
							expiryDate,
							formatter
					)
					.isAfter(LocalDateTime.now())) {
				return Boolean.toString(true);
			}
		}
		return Boolean.toString(false);
	}

	private UserToken getLatestUser(List<UserToken> userTokenDataList) {
		if (CollectionUtils.isEmpty(userTokenDataList)) {
			return null;
		}
		List<UserToken> dataList = userTokenDataList
				.stream()
				.filter(data -> data.getExpiryDate() !=
								null)
				.collect(Collectors.toList());
		DateTimeFormatter formatter = new DateTimeFormatterBuilder()
				.appendPattern(TIME_FORMAT)
				.optionalStart()
				.appendPattern(".")
				.appendFraction(ChronoField.MICRO_OF_SECOND,
								1,
								9,
								false
				)
				.optionalEnd()
				.toFormatter();
		return dataList
				.stream()
				.max(Comparator.comparing(data -> LocalDateTime.parse(
						data.getExpiryDate(),
						formatter
				)))
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
	public void updateExpiryDate(
			String username,
			String expiryDate
	) {
		List<UserToken> dataList = userTokenRepository.findAllByUsername(username);
		dataList
				.stream()
				.forEach(data -> data.setExpiryDate(expiryDate));
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
	 * @param resourceName resourceName
	 * @return ServiceResponse
	 */
	@Override
	public ServiceResponse generateAndSaveToken(String resourceName) {
		GenerateAPIKeyResponseDTO generateAPIKeyResponseDTO;
		String loginUser = userService.getLoggedInUser();
		ApiKey apiKeyTokenExist = apiKeyRepository.findByResource(resourceName);
		User loginUserDetail = userService.validateUser(loginUser);
		validateLoggedUserRole(
				resourceName,
				loginUser
		);
		String apiAccessToken = "";
		try {
			apiAccessToken = Encryption.getStringKey();
			if (Objects.nonNull(apiKeyTokenExist)) {
				apiKeyTokenExist.setKey(apiAccessToken);
				apiKeyTokenExist.setExpiryDate(LocalDate
													   .now()
													   .plusDays(authProperties.getExposeAPITokenExpiryDays()));
				apiKeyTokenExist.setModifiedBy(loginUserDetail);
				apiKeyTokenExist.setModifiedDate(LocalDate.now());
				apiKeyRepository.save(apiKeyTokenExist);
				generateAPIKeyResponseDTO = modelMapper.map(
						apiKeyTokenExist,
						GenerateAPIKeyResponseDTO.class
				);
				return new ServiceResponse(true,
										   messageService.getMessage("error_apiKey_updated"),
										   generateAPIKeyResponseDTO
				);
			} else {
				ApiKey apiKeyToken = new ApiKey();
				Resource resource = resourceService.validateResource(resourceName);
				apiKeyToken.setResource(resource);
				apiKeyToken.setKey(apiAccessToken);
				apiKeyToken.setExpiryDate(LocalDate
												  .now()
												  .plusDays(authProperties.getExposeAPITokenExpiryDays()));
				apiKeyToken.setCreatedDate(LocalDate.now());
				apiKeyToken.setCreatedBy(loginUserDetail);
				apiKeyToken.setModifiedBy(loginUserDetail);
				apiKeyToken.setModifiedDate(LocalDate.now());
				apiKeyRepository.save(apiKeyToken);
				generateAPIKeyResponseDTO = modelMapper.map(
						apiKeyToken,
						GenerateAPIKeyResponseDTO.class
				);
				return new ServiceResponse(true,
										   messageService.getMessage("success_apiKey_generated"),
										   generateAPIKeyResponseDTO
				);
			}
		} catch (EncryptionException e) {
			return new ServiceResponse(
					false,
					messageService.getMessage("error_apiKey_generated"),
					null
			);
		}
	}

	/**
	 * @param resourceName resourceName
	 * @param loginUser    loginUser
	 */
	private void validateLoggedUserRole(
			String resourceName,
			String loginUser
	) {
		List<UserRole> userPermissionList = userRoleService.findUserRoleByUsernameAndResource(
				loginUser,
				resourceName
		);
		List<UserRole> rootUser = userPermissionList
				.stream()
				.filter(userRole -> userRole
						.getRole()
						.isRootUser())
				.collect(Collectors.toList());
		if (rootUser ==
			null) {
			log.error(messageService.getMessage("error_invalid_permission") +
					  loginUser);
			throw new GenericException(messageService.getMessage("error_invalid_permission"));
		}
	}

	private void validateApiKey(String apiKey) {
		boolean isValid = apiKeyRepository.validateApiKey(apiKey);
		if (!isValid) {
			log.error("Please provide a valid api key");
			throw new GenericException("Please provide a valid api key");
		}
	}

}
