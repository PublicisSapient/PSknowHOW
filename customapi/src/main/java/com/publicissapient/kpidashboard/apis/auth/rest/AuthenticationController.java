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

package com.publicissapient.kpidashboard.apis.auth.rest;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.auth.AuthProperties;
import com.publicissapient.kpidashboard.apis.auth.AuthenticationResponseService;
import com.publicissapient.kpidashboard.apis.auth.service.AuthTypesConfigService;
import com.publicissapient.kpidashboard.apis.auth.service.AuthenticationRequest;
import com.publicissapient.kpidashboard.apis.auth.service.AuthenticationService;
import com.publicissapient.kpidashboard.apis.auth.service.ChangePasswordRequest;
import com.publicissapient.kpidashboard.apis.auth.token.TokenAuthenticationService;
import com.publicissapient.kpidashboard.apis.common.service.UserInfoService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.rbac.signupapproval.service.SignupManager;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.common.constant.AuthType;
import com.publicissapient.kpidashboard.common.model.application.AuthTypeStatus;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfoDTO;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Rest Controller to handle authentication requests
 */
@RestController
@Slf4j
public class AuthenticationController {

    private static final String AUTH_RESPONSE_HEADER = "X-Authentication-Token";
    private static final String STATUS = "Success";
    private static final String PASSWORD_PATTERN = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[$@$!%*?&]).{8,20})"; // NOSONAR
    private final AuthenticationService authenticationService;
    private final AuthenticationResponseService authenticationResponseService;
    private final AuthProperties authProperties;
    private final UserInfoService userInfoService;
    private final SignupManager signupManager;
    private AuthTypesConfigService authTypesConfigService;
    private TokenAuthenticationService tokenAuthenticationService;

    /**
     * Instantiates a new Authentication controller.
     *
     * @param authenticationService         the authentication service
     * @param authenticationResponseService the authentication response service
     * @param authProperties                the auth properties
     */
    @Autowired
    public AuthenticationController(AuthenticationService authenticationService,
                                    AuthenticationResponseService authenticationResponseService, UserInfoService userInfoService,
                                    AuthProperties authProperties, SignupManager signupManager, AuthTypesConfigService authTypesConfigService,
                                    TokenAuthenticationService tokenAuthenticationService) {
        this.authenticationService = authenticationService;
        this.authenticationResponseService = authenticationResponseService;
        this.authProperties = authProperties;
        this.userInfoService = userInfoService;
        this.signupManager = signupManager;
        this.authTypesConfigService = authTypesConfigService;
        this.tokenAuthenticationService = tokenAuthenticationService;
    }

    /**
     * Register user.
     *
     * @param httpServletRequest  the http servlet request
     * @param httpServletResponse the http servlet response
     * @param request             the request
     * @return the response entity
     * @throws IOException      the io exception
     * @throws ServletException the servlet exception
     */
    @PostMapping(value = "/registerUser")
    public ResponseEntity<ServiceResponse> registerUser(HttpServletRequest httpServletRequest,
                                                        HttpServletResponse httpServletResponse, @Valid @RequestBody AuthenticationRequest request) {

        try {
            AuthTypeStatus authTypesStatus = authTypesConfigService.getAuthTypesStatus();

            if (authTypesStatus != null && !authTypesStatus.isStandardLogin()) {
                return ResponseEntity.status(HttpStatus.OK).body(new ServiceResponse(false,
                        "Cannot complete the registration process. Standard authentication is disabled", null));
            }
            Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
            Matcher matcher = pattern.matcher(request.getPassword());
            boolean flag = matcher.matches();
            boolean isEmailExist = authenticationService.isEmailExist(request.getEmail());
            boolean isUsernameExists = authenticationService.isUsernameExists(request.getUsername());
            boolean isUsernameExistsInUserInfo = authenticationService
                    .isUsernameExistsInUserInfo(request.getUsername());

            if (isUsernameExists || isUsernameExistsInUserInfo) {
                return ResponseEntity.status(HttpStatus.OK).body(new ServiceResponse(false,
                        "Cannot complete the registration process, Try with different username", null));
            }
            if (isEmailExist) {
                return ResponseEntity.status(HttpStatus.OK).body(new ServiceResponse(false,
                        "Cannot complete the registration process, Try with different email", null));
            }
            if (flag) {
                Authentication authentication = authenticationService.create(request.getUsername(),
                        request.getPassword(), request.getEmail());

                UserInfo useInfo = userInfoService.save(userInfoService.createDefaultUserInfo(request.getUsername(),
                        AuthType.STANDARD, request.getEmail()));

                authenticationResponseService.handle(httpServletResponse, authentication);

                if (useInfo.getAuthorities().contains(Constant.ROLE_SUPERADMIN)) {
                    return ResponseEntity.status(HttpStatus.ACCEPTED).body(new ServiceResponse(true,
                            "User successfully created and assigned the server administration rights.", null));
                }
                signupManager.sendUserPreApprovalRequestEmailToAdmin(request.getUsername(), request.getEmail());
                return ResponseEntity.status(HttpStatus.ACCEPTED)
                        .body(new ServiceResponse(true, "Your access request has been sent for approval", null));

            } else {
                return ResponseEntity.status(HttpStatus.OK).body(new ServiceResponse(false,
                        "Cannot complete the registration process, Try with different password", null));
            }

        } catch (DuplicateKeyException dke) {
            log.error("Error in registration ", dke);
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(new ServiceResponse(true,
                    "Cannot complete the registration process, Try with different username", null));
        }
    }

    private String getResponse(HttpServletResponse response) {
        JSONObject json = new JSONObject();
        json.put(AUTH_RESPONSE_HEADER, response.getHeader(AUTH_RESPONSE_HEADER));
        json.put(STATUS, STATUS);
        return json.toJSONString();
    }

    /**
     * Update user.
     *
     * @param request the request
     * @return the response entity
     */
    @RequestMapping(value = "/updateUser", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    // NOSONAR
    public ResponseEntity<String> updateUser(@Valid @RequestBody AuthenticationRequest request) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(authenticationService.update(request.getUsername(), request.getPassword()));
    }

    /**
     * Gets authentication providers.
     *
     * @return the authentication providers
     */
    @RequestMapping(value = "/authenticationProviders", method = GET, produces = APPLICATION_JSON_VALUE) // NOSONAR
    public List<AuthType> getAuthenticationProviders() {
        return authProperties.getAuthenticationProviders();
    }

    /**
     * Change password.
     *
     * @param httpServletRequest  the http servlet request
     * @param httpServletResponse the http servlet response
     * @param request             the request
     * @return the response entity
     * @throws IOException      the io exception
     * @throws ServletException the servlet exception
     */
    @RequestMapping(value = "/changePassword", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    // NOSONAR
    public ResponseEntity<ServiceResponse> changePassword(HttpServletRequest httpServletRequest,
                                                          HttpServletResponse httpServletResponse, @Valid @RequestBody ChangePasswordRequest request)
            throws IOException, ServletException { // NOSONAR
        try {
            Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
            Matcher matcher = pattern.matcher(request.getPassword());
            boolean flag = matcher.matches();
            boolean isEmailExist = authenticationService.isEmailExist(request.getEmail());
            boolean isPasswordIdentical = authenticationService.isPasswordIdentical(request.getOldPassword(),
                    request.getPassword());
            if (isEmailExist) {
                if (flag) {
                    if (isPassContainUser(request.getPassword(), request.getUser())) {
                        if (isPasswordIdentical) {
                            return ResponseEntity.ok().body(
                                    new ServiceResponse(false, "New Password Can Not Be Same As Old Password", null));
                        } else {
                            boolean isValidUser = authenticationService.checkIfValidOldPassword(request.getEmail(),
                                    request.getOldPassword());
                            return isValidUser(isValidUser, request, httpServletResponse);
                        }
                    } else {
                        return ResponseEntity.ok()
                                .body(new ServiceResponse(false, "Password should not contain userName", null));
                    }
                } else {
                    return ResponseEntity.ok().body(new ServiceResponse(false, "Password Pattern Fails", null));
                }
            } else {
                return ResponseEntity.ok().body(new ServiceResponse(false, "Email Not Exist", null));
            }
        } catch (DuplicateKeyException dke) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(new ServiceResponse(false, "Unprocessable Entity", null));
        }
    }

    /**
     * @param isValidUser
     * @param request
     * @param httpServletResponse
     * @return
     */
    private ResponseEntity<ServiceResponse> isValidUser(boolean isValidUser, @Valid ChangePasswordRequest request,
                                                        HttpServletResponse httpServletResponse) {
        if (isValidUser) {
            Authentication authentication = authenticationService.changePassword(request.getEmail(),
                    request.getPassword());
            authenticationResponseService.handle(httpServletResponse, authentication);
            return ResponseEntity.ok().body(new ServiceResponse(true, getResponse(httpServletResponse), null));
        } else {
            return ResponseEntity.ok().body(new ServiceResponse(false, "Wrong Old Password", null));
        }
    }

    @RequestMapping(value = "/users/{username}", method = GET) // NOSONAR
    public ResponseEntity<ServiceResponse> getUser(@PathVariable String username, Principal principal) {

        username = CommonUtils.handleCrossScriptingTaintedValue(username);
        com.publicissapient.kpidashboard.apis.auth.model.Authentication authentication = authenticationService
                .getAuthentication(username);

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ServiceResponse(false, "user not found with username " + username, null));
        }

        UserInfo userInfo = userInfoService.getUserInfo(username);

        List<String> authorities = userInfo.getAuthorities() == null ? new ArrayList<>() : userInfo.getAuthorities();

        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setUsername(username);
        userInfoDTO.setAuthType(userInfo.getAuthType());
        userInfoDTO.setEmailAddress(authentication.getEmail());
        userInfoDTO.setDisplayName(userInfo.getDisplayName());
        userInfoDTO.setAuthorities(authorities);

        if (isAuthorizeForUserDetail(username, principal)) {
            return ResponseEntity.status(HttpStatus.OK).body(new ServiceResponse(true, "User details", userInfoDTO));
        } else {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ServiceResponse(false, "You are not authorised to get this user's details", null));
        }
    }

    private boolean isAuthorizeForUserDetail(String username, Principal principal) {
        String loggedInUser = principal.getName();
        UserInfo loggedInUserInfo = userInfoService.getUserInfo(loggedInUser);
        return loggedInUser.equals(username) || loggedInUserInfo.getAuthorities().contains("ROLE_SUPERADMIN");
    }

    @RequestMapping(value = "/users/{username}/updateEmail", method = PUT) // NOSONAR
    public ResponseEntity<ServiceResponse> updateUserInfo(@PathVariable String username,
                                                          @RequestBody Map<String, String> emailObject, Principal principal) {

        username = CommonUtils.handleCrossScriptingTaintedValue(username);
        com.publicissapient.kpidashboard.apis.auth.model.Authentication authentication = authenticationService
                .getAuthentication(username);
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ServiceResponse(false, "user not found with username " + username, null));
        }

        String email = emailObject.get("email");

        if (StringUtils.isEmpty(email)) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ServiceResponse(false, "Provide a valid email id", null));

        }
        String loggedInUser = principal.getName();

        if (loggedInUser.equals(username)) {

            if (authenticationService.isEmailExist(email)) {
                return ResponseEntity.status(HttpStatus.OK).body(
                        new ServiceResponse(false, "Email already registered. Try with a different email id", null));

            }
            authentication.setEmail(email);
            authenticationService.updateEmail(username, email);
            UserInfo userInfo = userInfoService.getUserInfo(username);

            UserInfoDTO userInfoDTO = new UserInfoDTO();
            userInfoDTO.setUsername(userInfo.getUsername());
            userInfoDTO.setEmailAddress(email);
            userInfoDTO.setAuthorities(userInfo.getAuthorities());
            userInfoDTO.setAuthType(userInfo.getAuthType());

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ServiceResponse(true, "Email updated successfully", userInfoDTO));

        } else {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ServiceResponse(false, "You are not authorised to update the email", null));
        }

    }

    private boolean isPassContainUser(String reqPassword, String username) {
        return !(StringUtils.containsIgnoreCase(reqPassword, username));
    }

    @RequestMapping(value = "/authdetails", method = GET)
    public ResponseEntity<ServiceResponse> getAuthDetails(HttpServletRequest request, Authentication authentication) {
        JSONObject jsonObject = tokenAuthenticationService.getOrSaveUserByToken(request, authentication);
        if (jsonObject != null) {
            return ResponseEntity.status(HttpStatus.OK).body(new ServiceResponse(true, "User Data Found", jsonObject));
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ServiceResponse(false, "Invalid token", null));

    }

}
