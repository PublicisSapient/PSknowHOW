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

package com.publicissapient.kpidashboard.bamboo.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * This class is used to hold settings specific to the Bamboo processor.
 */
@Component
@ConfigurationProperties(prefix = "bamboo")
public class BambooConfig {

	@Getter
	@Setter
	private String cron;

	@Getter
	@Setter
	private boolean saveLog;

	@Getter
	@Setter
	private String username;

	@Getter
	@Setter
	private String apiKey;

	@Getter
	@Setter
	private String customApiBaseUrl;

	@Getter
	@Setter
	@Value("${aesEncryptionKey}")
	private String aesEncryptionKey;

	/**
	 * null if not running in docker on http://localhost
	 */
	@Setter
	private String dockerLocalHostIP;

	/**
	 * Docker NATs the real host localhost to 10.0.2.2 when running in docker as
	 * localhost is stored in the JSON payload from jenkins we need this hack to fix
	 * the addresses
	 * 
	 * @return dockerLocalHostIP
	 */
	public String getDockerLocalHostIP() {

		// we have to do this as spring will return NULL if the value is not set vs and
		// empty string
		String localHostOverride = StringUtils.EMPTY;
		if (null != dockerLocalHostIP) {
			localHostOverride = dockerLocalHostIP;
		}
		return localHostOverride;
	}
}
