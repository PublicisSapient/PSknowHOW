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

package com.publicissapient.kpidashboard.teamcity.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Bean to hold settings specific to the Teamcity processor.
 */
@Component
@ConfigurationProperties(prefix = "teamcity")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamcityConfig {

	private String cron;
	private boolean includeLogs;
	private String dockerHostIp;
	private int pageSize;
	@Value("${folderDepth:10}")
	private int folderDepth;
	private String customApiBaseUrl;

	@Value("${aesEncryptionKey}")
	private String aesEncryptionKey;

	/**
	 * Provides the IP of Docker Localhost.
	 * 
	 * @return the Localhost Docker IP
	 */
	public String getDockerHostIp() {

		return dockerHostIp == null ? "" : dockerHostIp;
	}

}
