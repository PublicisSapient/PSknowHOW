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

package com.publicissapient.kpidashboard.sonar.factory;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.sonar.processor.adapter.SonarClient;
import com.publicissapient.kpidashboard.sonar.processor.adapter.impl.Sonar6And7Client;
import com.publicissapient.kpidashboard.sonar.processor.adapter.impl.Sonar8Client;

import lombok.extern.slf4j.Slf4j;

/**
 * Provides factory to create Sonar Clients.
 *
 */
@Component
@Slf4j
public class SonarClientFactory {

	public static final String VERSION_6 = "6";
	public static final String VERSION_7 = "7";
	public static final String VERSION_8 = "8";
	public static final String VERSION_9 = "9";
	private final Sonar8Client sonar8Client;
	private final Sonar6And7Client sonar6And7Client;

	/**
	 * Instantiate SonarClientFactory.
	 * 
	 * @param sonar8Client
	 *            the Sonar version 8 Client
	 * @param sonar6And7Client
	 *            the Sonar version 6 Client
	 */
	@Autowired
	public SonarClientFactory(Sonar8Client sonar8Client, Sonar6And7Client sonar6And7Client) {
		this.sonar8Client = sonar8Client;
		this.sonar6And7Client = sonar6And7Client;
	}

	/**
	 * Provides the respective Sonar client based on client specified in properties
	 * file.
	 * 
	 * @param version
	 *            the required version
	 * @return the Sonar Client
	 */
	public SonarClient getSonarClient(String version) {
		if (!StringUtils.isNotBlank(version)) {
			log.error("API Version should be Empty For Sonar");
			throw new NullPointerException("API Version should be Empty For Sonar");
		}
		SonarClient temp = null;
		version = version.split("\\.")[0];

		if (version.contains(VERSION_6) || version.contains(VERSION_7)) {
			temp = sonar6And7Client;
		} else if (version.contains(VERSION_8) || version.contains(VERSION_9)) {
			temp = sonar8Client;
		} else {
			temp = sonar8Client;
		}
		return temp;
	}

}
