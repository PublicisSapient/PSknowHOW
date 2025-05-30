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

import static com.publicissapient.kpidashboard.apis.common.service.impl.UserInfoServiceImpl.ERROR_MESSAGE_CONSUMING_REST_API;
import static com.publicissapient.kpidashboard.apis.common.service.impl.UserInfoServiceImpl.ERROR_WHILE_CONSUMING_AUTH_SERVICE_IN_USER_INFO_SERVICE_IMPL;
import static com.publicissapient.kpidashboard.apis.common.service.impl.UserInfoServiceImpl.ERROR_WHILE_CONSUMING_REST_SERVICE_IN_USER_INFO_SERVICE_IMPL;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import com.publicissapient.kpidashboard.apis.auth.model.SystemUser;
import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.publicissapient.kpidashboard.apis.auth.AuthProperties;
import com.publicissapient.kpidashboard.apis.auth.exceptions.PendingApprovalException;
import com.publicissapient.kpidashboard.apis.auth.model.Authentication;
import com.publicissapient.kpidashboard.apis.auth.repository.AuthenticationRepository;
import com.publicissapient.kpidashboard.apis.auth.token.CookieUtil;
import com.publicissapient.kpidashboard.apis.errors.APIKeyInvalidException;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.common.constant.AuthType;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.rbac.UserAccessApprovalResponseDTO;
import com.publicissapient.kpidashboard.common.repository.rbac.UserInfoRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * This class provides method to perform CRUD and validation operations on user
 * authentication data.
 *
 * @author prijain3
 */
@Slf4j
@Service
public class DefaultAuthenticationServiceImpl implements AuthenticationService {

	private final AuthenticationRepository authenticationRepository;
	private final AuthProperties authProperties;
	private final UserInfoRepository userInfoRepository;
	private final CookieUtil cookieUtil;

	@Autowired
	public DefaultAuthenticationServiceImpl(AuthenticationRepository authenticationRepository,
			AuthProperties authProperties, UserInfoRepository userInfoRepository, CookieUtil cookieUtil) {
		this.authenticationRepository = authenticationRepository;
		this.authProperties = authProperties;
		this.userInfoRepository = userInfoRepository;
		this.cookieUtil = cookieUtil;
	}

	/** {@inheritDoc} */
	@Override
	public Iterable<Authentication> all() {
		return authenticationRepository.findAll();
	}

	/** {@inheritDoc} */
	@Override
	public Authentication get(ObjectId id) {
		Optional<Authentication> authOpt = authenticationRepository.findById(id);
		Authentication authentication = null;
		if (authOpt.isPresent()) {
			authentication = authOpt.get();
		}
		return authentication;
	}

	/** {@inheritDoc} */
	@Override
	public org.springframework.security.core.Authentication create(String username, String password, String email) {
		Authentication authentication = new Authentication(username, password, email);
		if (authenticationRepository.count() == 0) {
			authentication.setApproved(true);
		}
		authentication = authenticationRepository.save(authentication);
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(authentication.getUsername(),
				authentication.getPassword(), new ArrayList<>());
		token.setDetails(AuthType.STANDARD);
		return token;
	}

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
	@Override
	public void delete(ObjectId id) {
		Optional<Authentication> authentication = authenticationRepository.findById(id);
		if (authentication.isPresent()) {
			authenticationRepository.delete(authentication.get());
		}
	}

	/** {@inheritDoc} */
	@Override
	public void delete(String username) {
		Authentication authentication = authenticationRepository.findByUsername(username);
		if (authentication != null) {
			authenticationRepository.delete(authentication);
		}
	}

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
	@Override
	public Integer getUserAttempts(String userName) {
		Authentication authentication = authenticationRepository.findByUsername(userName);
		if (null == authentication) {
			return null;
		} else {
			return authentication.getLoginAttemptCount();
		}
	}

	/** {@inheritDoc} */
	@Override
	public org.springframework.security.core.Authentication authenticate(String username, String password) {
		Authentication authentication = authenticationRepository.findByUsername(username);
		DateTime now = DateTime.now(DateTimeZone.UTC);

		if (!Pattern.matches(CommonConstant.USERNAME_PATTERN, username) || authentication == null) {
			throw new BadCredentialsException("Login Failed: The username or password entered is incorrect");
		}

		if (checkForResetFailAttempts(authentication, now)) {
			resetFailAttempts(username);
		} else if (checkForLockedUser(authentication)) {
			throw new LockedException("Account Locked: Invalid Login Limit Reached " + username);
		}

		if (!authentication.isApproved()) {
			throw new PendingApprovalException("Login Failed: Your access request is pending for approval");
		}

		if (authentication.checkPassword(password)) {
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
	 *          the Authentication
	 * @return true if user is locked
	 */
	private boolean checkForLockedUser(Authentication authentication) {

		return authentication != null && authentication.getLoginAttemptCount() != null &&
				authentication.getLoginAttemptCount().equals(authProperties.getAccountLockedThreshold());
	}

	/**
	 * Checks if need to reset fail attempts.
	 *
	 * @param authentication
	 *          Authentication
	 * @param now
	 *          current date time
	 * @return true or false
	 */
	private boolean checkForResetFailAttempts(Authentication authentication, DateTime now) {
		return authentication != null && null != authentication.getLastUnsuccessfulLoginTime() &&
				now.isAfter(authentication.getLastUnsuccessfulLoginTime().plusMinutes(authProperties.getAccountLockedPeriod()));
	}

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
	@Override
	public boolean checkIfValidOldPassword(String email, String oldPassword) {
		List<Authentication> authenticateList = authenticationRepository.findByEmail(email);
		if (CollectionUtils.isNotEmpty(authenticateList)) {
			return authenticateList.get(0).checkPassword(oldPassword);
		}
		return false;
	}

	/** {@inheritDoc} */
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
		if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
			return authentication.getPrincipal().toString();
		}
		// If running via Cron, return a default system user
		return SystemUser.SYSTEM.getName();
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
	public List<UserAccessApprovalResponseDTO> getAuthenticationByApproved(boolean approved) {
		List<UserAccessApprovalResponseDTO> userAccessApprovalResponseDTOList = new ArrayList<>();
		List<Authentication> nonApprovedUserList = authenticationRepository.findByApproved(approved);
		nonApprovedUserList.stream().filter(Objects::nonNull).forEach(userInfoDTO -> {
			UserAccessApprovalResponseDTO userAccessApprovalResponseDTO = new UserAccessApprovalResponseDTO();
			userAccessApprovalResponseDTO.setUsername(userInfoDTO.getUsername());
			userAccessApprovalResponseDTO.setEmail(userInfoDTO.getEmail());
			userAccessApprovalResponseDTO.setApproved(userInfoDTO.isApproved());
			List<String> whitelistDomain = authProperties.getWhiteListDomainForEmail();
			if (CollectionUtils.isNotEmpty(whitelistDomain) &&
					whitelistDomain.stream().anyMatch(domain -> userInfoDTO.getEmail().contains(domain))) {
				userAccessApprovalResponseDTO.setWhitelistDomainEmail(true);
			} else {
				userAccessApprovalResponseDTO.setWhitelistDomainEmail(false);
			}
			userAccessApprovalResponseDTOList.add(userAccessApprovalResponseDTO);
		});
		return userAccessApprovalResponseDTOList;
	}

	@Override
	public ResponseEntity<ServiceResponse> changePasswordForCentralAuth(ChangePasswordRequest request) {
		String apiKey = authProperties.getResourceAPIKey();
		HttpHeaders headers = cookieUtil.getHeadersForApiKey(apiKey, true);
		String changePasswordUrl = CommonUtils.getAPIEndPointURL(authProperties.getCentralAuthBaseURL(),
				authProperties.getChangePasswordEndPoint(), "");
		HttpEntity<?> entity = new HttpEntity<>(request, headers);

		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response = null;
		try {
			response = restTemplate.exchange(changePasswordUrl, HttpMethod.POST, entity, String.class);
			if (response.getStatusCode().is2xxSuccessful() && Objects.nonNull(response.getBody())) {
				JSONObject jsonObject = new JSONObject(response.getBody());
				ServiceResponse serviceResponse = new ServiceResponse();
				serviceResponse.setMessage(jsonObject.getString("message"));
				serviceResponse.setSuccess(jsonObject.getBoolean("success"));
				serviceResponse.setData(jsonObject.getString("data"));
				return ResponseEntity.ok(serviceResponse);
			} else {
				log.error(ERROR_MESSAGE_CONSUMING_REST_API + response.getStatusCode().value());
				throw new APIKeyInvalidException(ERROR_WHILE_CONSUMING_AUTH_SERVICE_IN_USER_INFO_SERVICE_IMPL);
			}
		} catch (JSONException e) {
			throw new AuthenticationServiceException("Unable to parse response.", e);
		} catch (HttpClientErrorException e) {
			log.error(ERROR_WHILE_CONSUMING_AUTH_SERVICE_IN_USER_INFO_SERVICE_IMPL, e.getMessage());
			throw new APIKeyInvalidException(ERROR_WHILE_CONSUMING_AUTH_SERVICE_IN_USER_INFO_SERVICE_IMPL);
		} catch (RuntimeException e) {
			log.error(ERROR_WHILE_CONSUMING_REST_SERVICE_IN_USER_INFO_SERVICE_IMPL, e);
			throw new AuthenticationServiceException("Unable to parse response.", e);
		}
	}
}
