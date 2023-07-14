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

package com.publicissapient.kpidashboard.apis.appsetting.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.application.GlobalConfig;
import com.publicissapient.kpidashboard.common.repository.application.GlobalConfigRepository;

/**
 * This class provides various methods related to operations on Global
 * Configurations Data
 *
 * @author pansharm5
 */
@Service
public class GlobalConfigServiceImpl implements GlobalConfigService {

	private final GlobalConfigRepository globalConfigRepository;

	/**
	 * @param globalConfigRepository
	 */
	@Autowired
	public GlobalConfigServiceImpl(GlobalConfigRepository globalConfigRepository) {
		this.globalConfigRepository = globalConfigRepository;
	}

	@Override
	public ServiceResponse getZephyrCloudUrlDetails() {
		boolean success = false;
		List<GlobalConfig> globalConfigs = globalConfigRepository.findAll();
		GlobalConfig globalConfig = CollectionUtils.isEmpty(globalConfigs) ? null : globalConfigs.get(0);
		String zephyrCloudUrl = globalConfig == null ? null : globalConfig.getZephyrCloudBaseUrl();
		if (zephyrCloudUrl != null) {
			success = true;
		}

		return new ServiceResponse(success, "Fetched Zephyr Cloud Base Url successfully", zephyrCloudUrl);
	}
}