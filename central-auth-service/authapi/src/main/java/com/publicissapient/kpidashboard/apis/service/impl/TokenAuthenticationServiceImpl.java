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
import com.publicissapient.kpidashboard.apis.config.AuthConfig;
import com.publicissapient.kpidashboard.apis.config.CookieConfig;
import com.publicissapient.kpidashboard.apis.enums.AuthType;
import com.publicissapient.kpidashboard.apis.errors.GenericException;
import com.publicissapient.kpidashboard.apis.service.*;
import com.publicissapient.kpidashboard.apis.util.CookieUtil;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Implementation of {@link TokenAuthenticationService}
 */
@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class TokenAuthenticationServiceImpl implements TokenAuthenticationService {

	private static final String ROLES_CLAIM = "roles";

	private static final String DETAILS_CLAIM = "details";

	private final UserService userService;

	private final AuthConfig authProperties;

	private final CookieConfig cookieConfig;

	public String extractUsernameFromEmail(String email) {
		if (Objects.nonNull(email) && email.contains("@")) {
			return email.substring(
					0,
					email.indexOf("@")
			);
		}

		return email;
	}

	@Override
	public void saveSamlData(
			Saml2AuthenticatedPrincipal principal,
			HttpServletResponse response
	) {
		String userEmail = principal.getName();

		String username = extractUsernameFromEmail(userEmail);

		String jwt = createApplicationJWT(
				username,
				AuthType.SAML
		);

		addCookies(
				username,
				jwt,
				response
		);

		this.userService.saveSamlUserData(principal);
	}

	@Override
	public String createApplicationJWT(
			@NotNull
			String subject,
			AuthType authType
	) {
		Date expirationDate = new Date(System.currentTimeMillis() + cookieConfig.getDuration());

		return Jwts.builder()
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
		Date expirationDate = new Date(System.currentTimeMillis() + cookieConfig.getDuration());
		String jwt = Jwts.builder()
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

		addCookies(
				jwt,
				response
		);

		return jwt;
	}

	private void addCookies(
			String jwt,
			HttpServletResponse response
	) {
		CookieUtil.addCookie(
				response,
				CookieUtil.COOKIE_NAME,
				jwt,
				cookieConfig.getDuration(),
				cookieConfig.getDomain(),
				cookieConfig.getIsSameSite(),
				cookieConfig.getIsSecure()
		);
		CookieUtil.addCookie(
				response,
				CookieUtil.EXPIRY_COOKIE_NAME,
				cookieConfig.getDuration()
							.toString(),
				false,
				cookieConfig.getDuration(),
				cookieConfig.getDomain(),
				cookieConfig.getIsSameSite(),
				cookieConfig.getIsSecure()
		);
	}

	private void addCookies(
			String username,
			String jwt,
			HttpServletResponse response
	) {
		CookieUtil.addCookie(
				response,
				CookieUtil.USERNAME_COOKIE_NAME,
				username,
				false,
				cookieConfig.getDuration(),
				cookieConfig.getDomain(),
				cookieConfig.getIsSameSite(),
				cookieConfig.getIsSecure()
		);

		addCookies(
				jwt,
				response
		);
	}

	@Override
	public Authentication getAuthentication(
			HttpServletRequest request,
			HttpServletResponse response
	) {
		Optional<Cookie> authCookie = CookieUtil.getCookie(
				request,
				CookieUtil.COOKIE_NAME
		);
		if (authCookie.isEmpty()) {
			return null;
		}

		String token = authCookie.get()
								 .getValue();

		if (null == token) {
			return null;
		}
		return createAuthentication(
				token,
				response
		);

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
			authentication = new PreAuthenticatedAuthenticationToken(
					userService.findByUsername(username),
					null,
					authorities
			);
			authentication.setDetails(claims.get(DETAILS_CLAIM));

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
		return Jwts.parser()
				   .setSigningKey(authProperties.getSecret())
				   .parseClaimsJws(token)
				   .getBody();
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
}
