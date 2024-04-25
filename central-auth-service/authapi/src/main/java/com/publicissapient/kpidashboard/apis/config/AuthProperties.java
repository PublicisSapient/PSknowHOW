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

package com.publicissapient.kpidashboard.apis.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.enums.AuthType;

import lombok.Data;

/**
 * This class maps authentication properties to object
 */

@Data
@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {

	private Long expirationTime;
	private String secret;
	private List<AuthType> authenticationProviders = new ArrayList<>();
	private String providers;
	private Integer accountLockedThreshold;
	private int accountLockedPeriod;
	@Value("${auth.samlMaxAuthenticationAgeMillis:300000}")
	private int samlMaxAuthenticationAgeMillis;
	private String baseUrl;
	private String baseUiUrl;
	private String nameId;
	private String logoutEmailQueryParam;
	private String loginCallback;
	private String logoutCallback;

	private String holdingEntityId;
	private String assertingEntityId;
	private String alias;
	private String samlLoginUrl;

	private String loginSuccessPageFormat;
	private String defaultRedirectToAfterLogout;
	private String defaultRedirectToAfterLogin;

	private List<String> corsFilterValidOrigin;
	private boolean ssoLogin;
	private int authCookieDuration;
	private boolean authCookieSecured;
	private boolean authCookieHttpOnly;
	private String authCookieSameSite;
	private boolean subDomainCookie;
	private String domain;

	private int exposeAPITokenExpiryDays;

	@Value("${forgotPassword.expiryInterval}")
	private String forgotPasswordExpiryInterval;
	// forgot password server host only for server where nginex is not
	// setup
	@Value("${forgotPassword.serverPort}")
	private String serverPort;
	// forgot password server host only for server where nginex is not
	// setup
	@Value("${forgotPassword.uiHost}")
	private String uiHost;
	// forgot password UI port only for server where nginex is not setup
	@Value("${forgotPassword.uiPort}")
	private String uiPort;

	@Value("$spring.kafka.producer.bootstrap-servers")
	private List<String> kafkaProducerBootStrapServers;
	@Value("${kafka.mailtopic}")
	private String kafkaMailTopic;
	private Map<String, String> notificationSubject;
	@Value("${notification.switch}")
	private boolean notificationSwitch;

	@Value("${notification.env}")
	private String notificationEnv;

	@Value("${flag.mailWithoutKafka}")
	private boolean mailWithoutKafka;

	private Map<String, String> mailTemplate;

	@Value("${ui.resetPath}")
	private String uiResetPath;

	// ------ verify user ------
	@Value("${verifyUser.tokenExpiryDays}")
	private String verifyUserTokenExpiryInterval;
	@Value("${ui.registerPath}")
	private String registerPath;
	@Value("${ui.validateUser}")
	private String validateUser;

}
