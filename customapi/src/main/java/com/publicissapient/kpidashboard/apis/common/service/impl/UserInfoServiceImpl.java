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

package com.publicissapient.kpidashboard.apis.common.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.publicissapient.kpidashboard.apis.abac.ProjectAccessManager;
import com.publicissapient.kpidashboard.apis.auth.AuthProperties;
import com.publicissapient.kpidashboard.apis.auth.exceptions.DeleteLastAdminException;
import com.publicissapient.kpidashboard.apis.auth.exceptions.UserNotFoundException;
import com.publicissapient.kpidashboard.apis.auth.model.Authentication;
import com.publicissapient.kpidashboard.apis.auth.repository.AuthenticationRepository;
import com.publicissapient.kpidashboard.apis.auth.service.AuthenticationService;
import com.publicissapient.kpidashboard.apis.auth.service.UserTokenDeletionService;
import com.publicissapient.kpidashboard.apis.auth.token.CookieUtil;
import com.publicissapient.kpidashboard.apis.auth.token.TokenAuthenticationService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.UserInfoService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.projectconfig.basic.service.ProjectBasicConfigService;
import com.publicissapient.kpidashboard.apis.userboardconfig.service.UserBoardConfigService;
import com.publicissapient.kpidashboard.common.constant.AuthType;
import com.publicissapient.kpidashboard.common.model.rbac.ProjectsAccess;
import com.publicissapient.kpidashboard.common.model.rbac.RoleWiseProjects;
import com.publicissapient.kpidashboard.common.model.rbac.UserDetailsResponseDTO;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfoDTO;
import com.publicissapient.kpidashboard.common.model.rbac.UserTokenData;
import com.publicissapient.kpidashboard.common.repository.rbac.UserInfoCustomRepository;
import com.publicissapient.kpidashboard.common.repository.rbac.UserInfoRepository;
import com.publicissapient.kpidashboard.common.repository.rbac.UserTokenReopository;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of {@link UserInfoService}.
 */

@Component
@Slf4j
public class UserInfoServiceImpl implements UserInfoService {

	HttpServletRequest contextreq;
	@Autowired
	TokenAuthenticationService tokenAuthenticationService;
	@Autowired
	private UserInfoRepository userInfoRepository;
	@Autowired
	private UserInfoCustomRepository userInfoCustomRepository;
	@Autowired
	private AuthenticationRepository authenticationRepository;
	@Autowired
	private AuthProperties authProperties;
	@Autowired
	private ProjectBasicConfigService projectBasicConfigService;

	@Autowired
	private ProjectAccessManager projectAccessManager;

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private UserTokenDeletionService userTokenDeletionService;

	@Autowired
	private UserBoardConfigService userBoardConfigService;

	@Autowired
	private CacheService cacheService;

	@Autowired
	private CookieUtil cookieUtil;
	@Autowired
	private UserTokenReopository userTokenReopository;

	@Override
	public Collection<GrantedAuthority> getAuthorities(String username) {
		UserInfo userInfo = userInfoRepository.findByUsername(username);
		List<String> roles = userInfo.getAuthorities();
		return createAuthorities(roles);
	}

	@Override
	public UserInfo getUserInfo(String username, AuthType authType) {

		return userInfoRepository.findByUsernameAndAuthType(username, authType);
	}

	@Override
	public UserInfo getUserInfo(String username) {
		return userInfoRepository.findByUsername(username);
	}

	@Override
	public Collection<UserInfo> getUsers() {
		List<UserInfo> userInfoList = userInfoRepository.findAll();
		List<String> userNames = userInfoList.stream().map(UserInfo::getUsername).collect(Collectors.toList());

		List<Authentication> authentications = authenticationRepository.findByUsernameIn(userNames);

		Map<String, Authentication> authMap = authentications.stream()
				.collect(Collectors.toMap(Authentication::getUsername, Function.identity()));

		List<UserInfo> nonApprovedUserList = new ArrayList<>();

		userInfoList.forEach(userInfo -> {

			Authentication auth = authMap.get(userInfo.getUsername());
			if (auth != null) {
				userInfo.setEmailAddress(auth.getEmail());
				if (!auth.isApproved()) {
					nonApprovedUserList.add(userInfo);
				}

			}
			createProjectAccess(userInfo);
		});
		List<UserInfo> approvedUserList = Lists.newArrayList(userInfoList);
		approvedUserList.removeAll(nonApprovedUserList);
		return approvedUserList;
	}

	/**
	 * when autority is Superadmin, then project access has to be send with a role
	 * SUPERADMIN as requIred in projectAccess page
	 * 
	 * @param userInfo
	 */
	private void createProjectAccess(UserInfo userInfo) {
		if (userInfo.getAuthorities().contains(Constant.ROLE_SUPERADMIN)) {
			ProjectsAccess access = new ProjectsAccess();
			access.setRole(Constant.ROLE_SUPERADMIN);
			userInfo.setProjectsAccess(Arrays.asList(access));
		}
	}

	@Override
	public ServiceResponse getAllUserInfo() {
		List<UserInfo> userInfoList = (List<UserInfo>) getUsers();

		if (CollectionUtils.isEmpty(userInfoList)) {
			log.info("Db has no userinfo");
			return new ServiceResponse(true, "No userinfo in user_info collection", userInfoList);
		}
		userInfoList.sort(Comparator.comparing(UserInfo::getUsername));
		log.info("Successfully fetched all userinfo");
		return new ServiceResponse(true, "Found all users info", userInfoList);
	}

	@Override
	public UserInfo demoteFromAdmin(String username, AuthType authType) {
		int numberOfAdmins = this.userInfoRepository.findByAuthoritiesIn(Arrays.asList(Constant.ROLE_SUPERADMIN))
				.size();
		if (numberOfAdmins <= 1) {
			throw new DeleteLastAdminException();
		}
		UserInfo user = this.userInfoRepository.findByUsernameAndAuthType(username, authType);
		if (user == null) {
			throw new UserNotFoundException(username, authType);
		}

		user.getAuthorities().remove(Constant.ROLE_SUPERADMIN);
		return this.userInfoRepository.save(user);
	}

	/**
	 * Creates authority.
	 *
	 * @param roles
	 * @return
	 */
	private Collection<GrantedAuthority> createAuthorities(List<String> roles) {
		Collection<GrantedAuthority> grantedAuthorities = new HashSet<>();
		if (CollectionUtils.isNotEmpty(roles)) {
			roles.forEach(authority -> grantedAuthorities.add(new SimpleGrantedAuthority(authority)));
		}

		return grantedAuthorities;
	}

	/**
	 * Checks validity of userId when creating a dashboard remotely via api
	 *
	 * @param userId
	 * @param authType
	 * @return true if valid user
	 */
	@Override
	public boolean isUserValid(String userId, AuthType authType) {
		if (this.userInfoRepository.findByUsernameAndAuthType(userId, authType) == null) {
			if (authType == AuthType.LDAP) {
				try {
					return searchLdapUser(userId);
				} catch (NamingException ne) {
					log.error("Failed to query ldap for: {}", userId, ne);
					return false;
				}
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	/**
	 * Search Ldap user.
	 *
	 * @param searchId
	 * @return <code>true</code> if user exists.
	 * @throws NamingException
	 */
	@SuppressWarnings("PMD.AvoidCatchingGenericException")
	private boolean searchLdapUser(String searchId) throws NamingException {
		boolean searchResult = false;

		Properties props = new Properties();
		props.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		props.put("java.naming.security.protocol", "ssl");
		props.put(Context.SECURITY_AUTHENTICATION, "simple");

		try {
			if (StringUtils.isBlank(this.authProperties.getAdUrl())) {
				props.put(Context.PROVIDER_URL, this.authProperties.getLdapServerUrl());
				props.put(Context.SECURITY_PRINCIPAL, StringUtils.replace(this.authProperties.getLdapUserDnPattern(),
						"{0}", this.authProperties.getLdapBindUser()));
			} else {
				props.put(Context.PROVIDER_URL, this.authProperties.getAdUrl());
				props.put(Context.SECURITY_PRINCIPAL,
						this.authProperties.getLdapBindUser() + "@" + this.authProperties.getAdDomain());
			}
			props.put(Context.SECURITY_CREDENTIALS, this.authProperties.getLdapBindPass());
		} catch (Exception e) {
			log.error("Failed to retrieve properties for InitialDirContext", e);
			return false;
		}

		InitialDirContext context = new InitialDirContext(props);

		try {
			SearchControls ctrls = new SearchControls();
			ctrls.setSearchScope(SearchControls.SUBTREE_SCOPE);

			String searchBase = "";
			String searchFilter = "";
			if (StringUtils.isBlank(this.authProperties.getAdUrl())) {
				searchBase = this.authProperties.getLdapUserDnPattern().substring(
						this.authProperties.getLdapUserDnPattern().indexOf(',') + 1,
						this.authProperties.getLdapUserDnPattern().length());
				searchFilter = "(&(objectClass=user)(sAMAccountName=" + searchId + "))";
			} else {
				searchBase = this.authProperties.getAdRootDn();
				searchFilter = "(&(objectClass=user)(userPrincipalName=" + searchId + "@"
						+ this.authProperties.getAdDomain() + "))";
			}

			NamingEnumeration<SearchResult> results = context.search(searchBase, searchFilter, ctrls);

			if (!results.hasMore()) {
				return searchResult;
			}

			SearchResult result = results.next();

			Attribute memberOf = result.getAttributes().get("memberOf");
			if (memberOf != null) {
				searchResult = true;
			}
		} finally {
			context.close();
		}

		return searchResult;
	}

	/**
	 * update userInfo collection
	 *
	 * @param username
	 * @return true if valid user
	 */
	@Override
	public ServiceResponse updateUserRole(String username, UserInfo userInfo) {
		UserInfo existingUserInfo = userInfoRepository.findByUsername(username);

		existingUserInfo = createUserInCaseSSOUserNotFound(existingUserInfo, userInfo);

		if (existingUserInfo == null) {
			return new ServiceResponse(false, "No user in user_info collection", userInfo);
		}
		UserInfo resultUserInfo = projectAccessManager.updateAccessOfUserInfo(existingUserInfo, userInfo);
		if (resultUserInfo == null) {
			return new ServiceResponse(false, "Unable to update Role.", null);
		}
		tokenAuthenticationService.updateExpiryDate(resultUserInfo.getUsername(), LocalDateTime.now().toString());
		return new ServiceResponse(true, "Updated the role Successfully", resultUserInfo);
	}

	private UserInfo createUserInCaseSSOUserNotFound(UserInfo existingUserInfo, UserInfo userInfo) {
		if (existingUserInfo == null && StringUtils.isNotEmpty(userInfo.getUsername()) && null != userInfo.getAuthType()
				&& userInfo.getAuthType().equals(AuthType.SSO)) {
			UserInfo defaultUserInfo = createDefaultUserInfo(userInfo.getUsername(), AuthType.SSO,
					userInfo.getEmailAddress());
			existingUserInfo = save(defaultUserInfo);
		}
		return existingUserInfo;
	}

	/**
	 * hasRoleSuperadmin()
	 *
	 * @param userInfoDto
	 * @return true if valid
	 */
	public boolean hasRoleSuperadmin(UserInfoDTO userInfoDto) {
		List<ProjectsAccess> projectsAccess = userInfoDto.getProjectsAccess();
		return projectsAccess.stream().anyMatch(pa -> pa.getRole().equalsIgnoreCase(Constant.ROLE_SUPERADMIN));
	}

	/**
	 * update userInfo collection
	 *
	 * @param userInfo
	 * @return true if valid user
	 */
	@Override
	public UserInfo updateUserInfo(final UserInfo userInfo) {
		return userInfoRepository.save(userInfo);
	}

	/**
	 * Return userinfo along with email in case of ldap or standardlogin
	 *
	 * @param username
	 *            username
	 * @param authType
	 *            authtype enum
	 * @return userinfo
	 */
	public UserInfo getUserInfoWithEmail(String username, AuthType authType) {
		UserInfo userInfo = userInfoRepository.findByUsernameAndAuthType(username, authType);
		if (null != userInfo) {
			addEmailForStandardAuthType(userInfo);
		}
		return userInfo;
	}

	@Override
	public UserInfo save(UserInfo userInfo) {
		if (userInfoRepository.count() == 0) {
			UserInfo superAdminUserInfo = createSuperAdminUserInfo(userInfo.getUsername(), userInfo.getEmailAddress());
			return userInfoRepository.save(superAdminUserInfo);
		}
		return userInfoRepository.save(userInfo);

	}

	@Override
	public UserInfo createDefaultUserInfo(String username, AuthType authType, String email) {
		UserInfo userInfo = new UserInfo();
		userInfo.setUsername(username);
		userInfo.setAuthType(authType);
		userInfo.setAuthorities(Collections.singletonList(Constant.ROLE_VIEWER));
		userInfo.setProjectsAccess(Collections.emptyList());
		userInfo.setEmailAddress(email);

		return userInfo;
	}

	/*
	to create Super admin User info for first time user
	 */
	public UserInfo createSuperAdminUserInfo(String username, String email) {
		UserInfo userInfo = new UserInfo();
		userInfo.setUsername(username);
		userInfo.setAuthType(AuthType.STANDARD);
		userInfo.setAuthorities(Collections.singletonList(Constant.ROLE_SUPERADMIN));
		userInfo.setProjectsAccess(Collections.emptyList());
		userInfo.setEmailAddress(email);
		return userInfo;
	}

	private void addEmailForStandardAuthType(UserInfo userInfo) {
		if (AuthType.STANDARD == userInfo.getAuthType()) {
			Authentication auth = authenticationRepository.findByUsername(userInfo.getUsername());
			if (auth != null) {
				userInfo.setEmailAddress(auth.getEmail());
			}
		}
	}

	/**
	 * This method is for deleting the users
	 *
	 * @param username
	 *            username
	 */
	@Override
	public ServiceResponse deleteUser(String username) {
		try {
			userInfoRepository.deleteByUsername(username);
			authenticationService.delete(username);
			userTokenDeletionService.invalidateSession(username);
			userBoardConfigService.deleteUser(username);
			cleanAllCache();
		} catch (Exception exception) {
			log.error("Error in Repository :  {} " + exception);
			return new ServiceResponse(false, "There is some issue in Repository", "Failed");
		}

		return new ServiceResponse(true, username + " deleted Successfully", "Ok");
	}

	@Override
	public List<UserInfo> getUserInfoByAuthType(String authType) {
		return userInfoRepository.findByAuthType(authType);
	}

	@Override
	public UserInfoDTO getOrSaveDefaultUserInfo(String username, AuthType authType, String email) {
		UserInfo userInfo = getUserInfo(username);
		if (null == userInfo) {
			userInfo = createDefaultUserInfo(username, authType, email);
			userInfo = save(userInfo);
		}
		return convertToDTOObject(userInfo);
	}

	private UserInfoDTO convertToDTOObject(UserInfo userInfo) {
		UserInfoDTO userInfoDTO = null;
		if (null != userInfo) {
			userInfoDTO = UserInfoDTO.builder().username(userInfo.getUsername()).authType(userInfo.getAuthType())
					.authorities(userInfo.getAuthorities()).emailAddress(userInfo.getEmailAddress())
					.projectsAccess(userInfo.getProjectsAccess()).build();
		}
		return userInfoDTO;
	}

	private void cleanAllCache() {
		cacheService.clearAllCache();
		log.info("cache cleared");
	}

	@Override
	public UserDetailsResponseDTO getUserInfoByToken(HttpServletRequest request) {
		Cookie authCookie = cookieUtil.getAuthCookie(request);
		if (StringUtils.isBlank(authCookie.getValue())) {
			return null;
		}
		String token = authCookie.getValue();
		UserTokenData userTokenData = userTokenReopository.findByUserToken(token);
		UserDetailsResponseDTO userDetailsResponseDTO = new UserDetailsResponseDTO();
		if (Objects.nonNull(userTokenData)) {
			String username = userTokenData.getUserName();
			UserInfo userinfo = userInfoRepository.findByUsername(username);
			Authentication authentication = authenticationRepository.findByUsername(username);
			String email = authentication == null ? userinfo.getEmailAddress() : authentication.getEmail();

			userDetailsResponseDTO.setUserName(username);
			userDetailsResponseDTO.setUserEmail(email);
			userDetailsResponseDTO.setAuthorities(userinfo.getAuthorities());

			List<RoleWiseProjects> projectAccessesWithRole = projectAccessManager.getProjectAccessesWithRole(username);

			userDetailsResponseDTO.setProjectsAccess(projectAccessesWithRole);
		}
		return userDetailsResponseDTO;

	}

	public UserInfo getOrSaveUserInfo(String userName, AuthType authType, List<String> authorities) {
		UserInfo userInfo = userInfoRepository.findByUsername(userName);
		if (userInfo == null) {
			userInfo = new UserInfo();
			userInfo.setUsername(userName);
			userInfo.setAuthorities(authorities);
			userInfo.setAuthType(authType);
			userInfoRepository.save(userInfo);
		}
		return userInfo;
	}
}