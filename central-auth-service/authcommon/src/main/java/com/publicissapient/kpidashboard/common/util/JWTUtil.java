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
package com.publicissapient.kpidashboard.common.util;

import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JWTUtil {

	/**
	 * This method generate JWT token
	 * 
	 * @param subject
	 * @param claims
	 * @param expirationDate
	 * @param secret
	 * @return jwt token
	 */
	public static String generateJwt(String subject, Map<String, Object> claims, Date expirationDate, String secret) {
		if (!jwtCreationChecks(subject, claims, expirationDate, secret)) {
			return null;
		}
		return Jwts.builder().setSubject(subject).addClaims(claims).setExpiration(expirationDate)
				.signWith(SignatureAlgorithm.HS512, secret).compact();
	}

	/**
	 * This method checks validity of data for jwt creation
	 * 
	 * @param subject
	 * @param claims
	 * @param expirationDate
	 * @param secret
	 * @return parameter values are valid or not
	 */
	private static boolean jwtCreationChecks(String subject, Map<String, Object> claims, Date expirationDate,
			String secret) {
		if (StringUtils.isEmpty(secret)) {
			log.info("Null secret found during jwt creation");
			return false;
		}

		if (StringUtils.isEmpty(subject)) {
			log.info("Null subject found during jwt creation");
			return false;
		}

		if (expirationDate == null) {
			log.info("No expiration date found during jwt creation");
			return false;
		}

		if (expirationDate.before(new Date())) {
			log.info("Old date found during jwt creation");
			return false;
		}

		return true;
	}

	/**
	 * This method parse claims from the token
	 * 
	 * @param token
	 * @param secret
	 * @return claims object
	 * @throws ExpiredJwtException
	 */
	private static Claims parseClaims(String token, String secret) throws ExpiredJwtException {
		return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
	}

	/**
	 * This method get claim based on claimKeyProvided
	 * 
	 * @param token
	 * @param secret
	 * @param claimKey
	 * @return claim object
	 */
	public static Object getClaim(String token, String secret, String claimKey) {
		Object claim = null;
		try {
			Claims claims = parseClaims(token, secret);
			claim = claims.get(claimKey);
		} catch (ExpiredJwtException e) {
			log.info("JWT expired while parsing claims : {}", e.getMessage());
		}
		return claim;
	}

	/**
	 * This method return the subject of jwt token
	 * 
	 * @param token
	 * @param secret
	 * @return subject value
	 */
	public static String getSubject(String token, String secret) {
		String username = null;
		try {
			Claims claims = parseClaims(token, secret);
			username = claims.getSubject();
		} catch (ExpiredJwtException e) {
			log.info("JWT expired while getting subject : {}", e.getMessage());
		}
		return username;
	}
}
