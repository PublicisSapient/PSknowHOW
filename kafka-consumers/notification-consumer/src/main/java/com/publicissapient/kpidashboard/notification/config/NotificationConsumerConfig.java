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
package com.publicissapient.kpidashboard.notification.config;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Component
@ConfigurationProperties
@AllArgsConstructor
@NoArgsConstructor
@Data
public class NotificationConsumerConfig {

	@Value("${kafka.mailtopic}")
	private String kafkaMailTopic;

	private String fromEmail;

	private Map<String, String> mailTemplate;

	private boolean sendGridEnabled;

	@Value("${mail.sendgrid.api-key}")
	private String sendGridApiKey;

	@Value("${mail.sendgrid.api-end-point}")
	private String sendGridApiEndPoint;
}
