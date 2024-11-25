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
import org.springframework.context.annotation.Configuration;

import com.publicissapient.kpidashboard.apis.enums.AuthType;

import lombok.Data;

@Data
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "auth")
public class AuthConfig {

	private String secret;

	private List<AuthType> authenticationProviders = new ArrayList<>();

	private Integer accountLockedThreshold;
	private int accountLockedPeriod;
	private Long maxAgeSeconds;
	private Boolean includeSubdomains;
	private String contentSecurityPolicy;

	private String baseUiUrl;

	private String loginView;
	private String logoutView;

	@Value("${kafka.mailtopic}")
	private String kafkaMailTopic;

	private Map<String, String> notificationSubject;

	@Value("${notification.switch}")
	private boolean notificationSwitch;

	@Value("${notification.env}")
	private String notificationEnv;

	private boolean mailWithoutKafka;

	private Map<String, String> mailTemplate;

	private String verifyUserTokenExpiryDays;

	private String serverApiKey;
}
