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

package com.publicissapient.kpidashboard.apis.auth.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.publicissapient.kpidashboard.apis.auth.AuthProperties;
import com.publicissapient.kpidashboard.apis.auth.exceptions.PendingApprovalException;
import com.publicissapient.kpidashboard.apis.auth.model.Authentication;
import com.publicissapient.kpidashboard.apis.auth.repository.AuthenticationRepository;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.constant.AuthType;
import com.publicissapient.kpidashboard.common.repository.rbac.UserInfoRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * This class provides method to perform CRUD and validation operations on user
 * authentication data.
 *
 * @author prijain3
 *
 */
@Slf4j
@Service
public class DefaultAuthenticationServiceImpl implements AuthenticationService {

	private final AuthenticationRepository authenticationRepository;
	private final AuthProperties authProperties;
	private final UserInfoRepository userInfoRepository;

	// ------- auth-N-auth required code starts here -------
	private static final String RESPONSE = "response {}";
	private static final String DATA_FOUND = "data found";
	private static final String FETCHED_RESPONSE = "fetched response {}";
	private static final String ERROR_CODE = "Error while fetching from {}. with status {}";
	private static final String ERROR_MESSAGE = "Error while fetching from {}:  {}";

	// ----- auth-N-auth required code end here ----------------

	@Autowired
	public DefaultAuthenticationServiceImpl(AuthenticationRepository authenticationRepository,
			AuthProperties authProperties, UserInfoRepository userInfoRepository) {
		this.authenticationRepository = authenticationRepository;
		this.authProperties = authProperties;
		this.userInfoRepository = userInfoRepository;
	}

	@Autowired
	private RestTemplate restTemplate;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterable<Authentication> all() {
		return authenticationRepository.findAll();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Authentication get(ObjectId id) {
		Optional<Authentication> authOpt = authenticationRepository.findById(id);
		Authentication authentication = null;
		if (authOpt.isPresent()) {
			authentication = authOpt.get();
		}
		return authentication;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.springframework.security.core.Authentication create(String username, String password, String email) {
		Authentication authentication = new Authentication(username, password, email);
		if (authenticationRepository.count() == 0) {
			authentication.setApproved(true);
		}
		authentication = authenticationRepository.save(authentication);
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
				authentication.getUsername(), authentication.getPassword(), new ArrayList<>());
		token.setDetails(AuthType.STANDARD);
		return token;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String update(String username, String password) {
		Authentication authentication = authenticationRepository.findByUsername(username);
		if (null == authentication) {
			return "User Does not Exist";
		} else {
			authentication.setPassword(password);
			authenticationRepository.save(authentication);
			return "User is updated";
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(ObjectId id) {
		Optional<Authentication> authentication = authenticationRepository.findById(id);
		if (authentication.isPresent()) {
			authenticationRepository.delete(authentication.get());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(String username) {
		Authentication authentication = authenticationRepository.findByUsername(username);
		if (authentication != null) {
			authenticationRepository.delete(authentication);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Boolean updateFailAttempts(String userName, DateTime unsuccessAttemptTime) {
		Authentication authentication = authenticationRepository.findByUsername(userName);
		if (null == authentication) {
			return Boolean.FALSE;
		} else {
			Integer attemptCount = authentication.getLoginAttemptCount();
			if (null == attemptCount) {
				attemptCount = 1;
			} else {
				attemptCount++;
			}
			authentication.setLoginAttemptCount(attemptCount);
			authentication.setLastUnsuccessfulLoginTime(unsuccessAttemptTime);
			authenticationRepository.save(authentication);
			return Boolean.TRUE;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void resetFailAttempts(String userName) {
		Authentication authentication = authenticationRepository.findByUsername(userName);
		if (null != authentication) {
			Integer attemptCount = null;
			authentication.setLoginAttemptCount(attemptCount);
			authentication.setLastUnsuccessfulLoginTime(null);
			authenticationRepository.save(authentication);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer getUserAttempts(String userName) {
		Authentication authentication = authenticationRepository.findByUsername(userName);
		if (null == authentication) {
			return null;
		} else {
			return authentication.getLoginAttemptCount();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.springframework.security.core.Authentication authenticate(String username, String password) {
		Authentication authentication = authenticationRepository.findByUsername(username);
		DateTime now = DateTime.now(DateTimeZone.UTC);

		if (checkForResetFailAttempts(authentication, now)) {
			resetFailAttempts(username);
		} else if (checkForLockedUser(authentication)) {
			throw new LockedException("Account Locked: Invalid Login Limit Reached " + username);
		}

		if (authentication != null && !authentication.isApproved()) {
			throw new PendingApprovalException("Login Failed: Your access request is pending for approval");
		}

		if (authentication != null && authentication.checkPassword(password)) {
			return new UsernamePasswordAuthenticationToken(authentication.getUsername(), authentication.getPassword(),
					new ArrayList<>());
		}
		// commented code to fix the security issues
		// throw new BadCredentialsException("Login Failed: Invalid credentials
		// for user
		throw new BadCredentialsException("Login Failed: The username or password entered is incorrect");
	}

	/**
	 * Checks if user is locked
	 *
	 * @param authentication
	 *            the Authentication
	 * @return true if user is locked
	 */
	private boolean checkForLockedUser(Authentication authentication) {

		return authentication != null && authentication.getLoginAttemptCount() != null
				&& authentication.getLoginAttemptCount().equals(authProperties.getAccountLockedThreshold());
	}

	/**
	 * Checks if need to reset fail attempts.
	 *
	 * @param authentication
	 *            Authentication
	 * @param now
	 *            current date time
	 * @return true or false
	 */
	private boolean checkForResetFailAttempts(Authentication authentication, DateTime now) {
		return authentication != null && null != authentication.getLastUnsuccessfulLoginTime() && now.isAfter(
				authentication.getLastUnsuccessfulLoginTime().plusMinutes(authProperties.getAccountLockedPeriod()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEmailExist(String email) {

		List<Authentication> authenticate = authenticationRepository.findByEmail(email);

		return CollectionUtils.isNotEmpty(authenticate);
	}

	@Override
	public boolean isUsernameExists(String username) {
		return authenticationRepository.findByUsername(username) != null;
	}

	@Override
	public boolean isUsernameExistsInUserInfo(String username) {
		return userInfoRepository.findByUsername(username) != null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean checkIfValidOldPassword(String email, String oldPassword) {
		List<Authentication> authenticateList = authenticationRepository.findByEmail(email);
		if (CollectionUtils.isNotEmpty(authenticateList)) {
			return authenticateList.get(0).checkPassword(oldPassword);
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.springframework.security.core.Authentication changePassword(String email, String password) {
		UsernamePasswordAuthenticationToken token = null;
		List<Authentication> authenticateList = authenticationRepository.findByEmail(email);
		if (CollectionUtils.isNotEmpty(authenticateList)) {
			Authentication auth = authenticateList.get(0);
			auth.setPassword(password);
			Authentication authentication = authenticationRepository.save(auth);
			token = new UsernamePasswordAuthenticationToken(authentication.getUsername(), authentication.getPassword(),
					new ArrayList<>());
			token.setDetails(AuthType.STANDARD);
		}
		return token;
	}

	@Override
	public Authentication getAuthentication(String username) {
		return authenticationRepository.findByUsername(username);
	}

	@Override
	public boolean updateEmail(String username, String email) {
		Authentication authentication = authenticationRepository.findByUsername(username);
		if (null == authentication) {
			return false;
		} else {
			authentication.setEmail(email);
			authenticationRepository.save(authentication);
			return true;
		}
	}

	@Override
	public boolean isPasswordIdentical(String oldPassword, String newPassword) {
		return oldPassword.equals(newPassword);
	}

	@Override
	public String getLoggedInUser() {
		org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext()
				.getAuthentication();
		return authentication.getPrincipal().toString();
	}

	@Override
	public String getUsername(org.springframework.security.core.Authentication authentication) {

		if (authentication == null) {
			return null;
		}

		return authentication.getPrincipal().toString();
	}

	/**
	 * get authentication on the basis of approval
	 * 
	 * @param approved
	 * @return
	 */
	@Override
	public Iterable<Authentication> getAuthenticationByApproved(boolean approved) {
		return authenticationRepository.findByApproved(approved);
	}

	// ---- auth-N-auth required code starts here ----

	/**
	 *
	 * @param responseEntity
	 * @param url
	 * @return
	 */
	public ServiceResponse getAuthNAuthResponse(ResponseEntity<ServiceResponse> responseEntity, String url) {
		ServiceResponse fetchDataResponse = new ServiceResponse();
		try {
			log.info(RESPONSE, responseEntity);
			if (responseEntity.getStatusCode() == HttpStatus.OK) {
				log.info(DATA_FOUND);
				fetchDataResponse = responseEntity.getBody();
				log.info(FETCHED_RESPONSE, fetchDataResponse);
			} else {
				String statusCode = responseEntity.getStatusCode().toString();
				log.error(ERROR_CODE, url, statusCode);
			}
		} catch (Exception exception) {
			log.error(ERROR_MESSAGE, url, exception.getMessage());
		}
		return fetchDataResponse;
	}
	// --- auth-N-auth required code end here --------------

}
