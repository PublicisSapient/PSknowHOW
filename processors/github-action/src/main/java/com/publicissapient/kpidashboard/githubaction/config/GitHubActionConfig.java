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

package com.publicissapient.kpidashboard.githubaction.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * GutHubActionConfig represents a class which holds GutHubActionConfig details.
 *
 */
@Component
@ConfigurationProperties(prefix = "githubaction")
@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GitHubActionConfig {
	private String host;
	private String cron;
	@Value("${aesEncryptionKey}")
	private String aesEncryptionKey;
	private String api;
	private String customApiBaseUrl;
	private int pageSize;
	private int initialPageSize;
}
