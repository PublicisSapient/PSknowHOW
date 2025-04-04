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

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.publicissapient.kpidashboard.apis.common.service.VersionMetadataService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.model.VersionDetails;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of {@link VersionMetadataService}. It provides method to get
 * the version metadata.
 */
@Service
@Slf4j
public class VersionMetadataServiceImpl implements VersionMetadataService {

	private static final String CURRENT_VERSION_DEFAULT_KEY = "currentVersion";

	@Autowired
	private CustomApiConfig apiConfig;

	@Value("${versionnumber:}")
	private String version;

	@Override
	public VersionDetails getVersionMetadata() {

		log.debug("VersionMetadataServiceImpl:: getVersionMetadata:: start");

		VersionDetails details = new VersionDetails();
		details.setVersionDetailsMap(new HashMap<>());

		String currentVersionValue = version;

		details.getVersionDetailsMap().put(CURRENT_VERSION_DEFAULT_KEY, currentVersionValue);

		log.debug("VersionMetadataServiceImpl:: getVersionMetadata:: return");

		return details;
	}

	public RestTemplate getRestContext() {
		return new RestTemplate();
	}
}
