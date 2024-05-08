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

package com.publicissapient.kpidashboard.apis.filters.standard;

import com.publicissapient.kpidashboard.apis.entity.User;
import com.publicissapient.kpidashboard.apis.entity.UserToken;
import com.publicissapient.kpidashboard.apis.enums.AuthType;
import com.publicissapient.kpidashboard.apis.filters.AuthenticationResponseService;
import com.publicissapient.kpidashboard.apis.repository.UserRepository;
import com.publicissapient.kpidashboard.apis.service.TokenAuthenticationService;
import com.publicissapient.kpidashboard.apis.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.json.simple.JSONObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;

/**
 * Provides Standard Login Authentication Result Handler.
 *
 * @author Hiren Babariya
 */
@Component
@AllArgsConstructor
public class AuthenticationResultHandler implements AuthenticationSuccessHandler {
    private static final String USER_NAME = "user_name";
    private static final String USER_EMAIL = "user_email";
    private static final String USER_ID = "user_id";
    private static final String USER_TYPE = "user_type";
    private static final String AUTH_RESPONSE_HEADER = "X-Authentication-Token";

    private final AuthenticationResponseService authenticationResponseService;
    private final TokenAuthenticationService tokenAuthenticationService;

    private final UserRepository userRepository;

    private final UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        authenticationResponseService.handle(response, authentication, AuthType.STANDARD);
        // sgu106: Google Analytics data population starts
        String username = userService.getUsername(authentication);
        UserToken userToken = tokenAuthenticationService.getLatestTokenByUser(username);
        JSONObject json = loginJsonData(response, username, userToken);
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.print(json.toJSONString());
        // sgu106: Google Analytics data population ends

    }

    public JSONObject loginJsonData(HttpServletResponse httpServletResponse, String username, UserToken userToken) {
        JSONObject json = new JSONObject();
        httpServletResponse.setContentType("application/json");
        httpServletResponse.setCharacterEncoding("UTF-8");
        User userinfo = userRepository.findByUsername(username);
        json.put(USER_NAME, username);
        if (Objects.nonNull(userinfo) && Objects.nonNull(userToken)) {
            json.put(USER_EMAIL, userinfo.getEmail());
            json.put(USER_TYPE, userinfo.getAuthType());
            json.put(USER_ID, userinfo.getId().toString());
            json.put(AUTH_RESPONSE_HEADER, userToken.getToken());
        }
        return json;

    }

}
