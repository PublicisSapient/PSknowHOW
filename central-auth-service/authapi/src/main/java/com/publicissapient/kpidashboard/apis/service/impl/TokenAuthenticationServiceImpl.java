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

import com.publicissapient.kpidashboard.apis.config.AuthConfig;
import com.publicissapient.kpidashboard.apis.config.CookieConfig;
import com.publicissapient.kpidashboard.apis.enums.AuthType;
import com.publicissapient.kpidashboard.apis.errors.GenericException;
import com.publicissapient.kpidashboard.apis.service.TokenAuthenticationService;
import com.publicissapient.kpidashboard.apis.service.dto.UserDTO;
import com.publicissapient.kpidashboard.apis.util.CookieUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class TokenAuthenticationServiceImpl implements TokenAuthenticationService {

	private static final String ROLES_CLAIM = "roles";

	private static final String DETAILS_CLAIM = "details";

	private final AuthConfig authProperties;

	private final CookieConfig cookieConfig;

	@Override
	public String createJWT(@NotNull String subject, AuthType authType,
			Collection<? extends GrantedAuthority> authorities) {
		Instant expirationInstant = Instant.now().plusSeconds(cookieConfig.getDuration());

		return Jwts.builder()
				.setSubject(subject)
				.claim(DETAILS_CLAIM, authType)
				.claim(ROLES_CLAIM,
						Objects.nonNull(authorities)
								? authorities.stream().map(GrantedAuthority::getAuthority).toList()
								: new ArrayList<>())
				.setExpiration(Date.from(expirationInstant))
				.signWith(SignatureAlgorithm.HS512, authProperties.getSecret())
				.compact();
	}

	@Override
	public void addStandardCookies(String jwt, HttpServletResponse response) {
		CookieUtil.addCookie(response, CookieUtil.COOKIE_NAME, jwt, cookieConfig.getDuration(), cookieConfig.getDomain(),
				cookieConfig.getIsSameSite(), cookieConfig.getIsSecure());
		CookieUtil.addCookie(response, CookieUtil.EXPIRY_COOKIE_NAME, cookieConfig.getDuration().toString(), false,
				cookieConfig.getDuration(), cookieConfig.getDomain(), cookieConfig.getIsSameSite(), cookieConfig.getIsSecure());
	}

	@Override
	public void addSamlCookies(String username, String jwt, HttpServletResponse response) {
		CookieUtil.addCookie(response, CookieUtil.USERNAME_COOKIE_NAME, username, false, cookieConfig.getDuration(),
				cookieConfig.getDomain(), cookieConfig.getIsSameSite(), cookieConfig.getIsSecure());

		addStandardCookies(jwt, response);
	}

	@Override
	public String getSubject(String token) throws GenericException {
		try {
			Claims claims = parseClaims(token);

			return claims.getSubject();
		} catch (ExpiredJwtException e) {
			throw new GenericException("token has expired");
		}
	}

	@Override
	public Collection<GrantedAuthority> createAuthorities(List<String> roles) {
		Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();

		roles.forEach(authority -> grantedAuthorities.add(new SimpleGrantedAuthority(authority)));

		return grantedAuthorities;
	}

	private Claims parseClaims(String token) throws ExpiredJwtException {
		return Jwts.parser().setSigningKey(authProperties.getSecret()).parseClaimsJws(token).getBody();
	}

	@Override
	public String extractUsernameFromAuthentication(Authentication authentication) {
		if (authentication != null) {
			if (authentication.getPrincipal() instanceof UserDTO) {
				return ((UserDTO) authentication.getPrincipal()).getUsername();
			} else if (authentication.getPrincipal() instanceof String) {
				return authentication.getPrincipal().toString();
			}

			return null;
		}
		return null;
	}

	@Override
	public void addGuestCookies(String guestDisplayName, String jwt, HttpServletResponse response) {
		addStandardCookies(jwt, response);
		CookieUtil.addCookie(response, CookieUtil.GUEST_DISPLAY_NAME_COOKIE_NAME, guestDisplayName,
				cookieConfig.getDuration(), cookieConfig.getDomain(), cookieConfig.getIsSameSite(), cookieConfig.getIsSecure());
	}
}
