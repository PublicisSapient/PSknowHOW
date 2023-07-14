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

package com.publicissapient.kpidashboard.apis.kpicolumnconfig.service;

import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.application.KpiColumnConfig;
import com.publicissapient.kpidashboard.common.model.application.KpiColumnConfigDTO;
import com.publicissapient.kpidashboard.common.repository.application.KpiColumnConfigRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KpiColumnConfigServiceImpl implements KpiColumnConfigService {

	@Autowired
	KpiColumnConfigRepository kpiColumnConfigRepository;

	@Override
	public ServiceResponse saveKpiColumnConfig(KpiColumnConfigDTO kpiColumnConfigDTO) {
		KpiColumnConfig kpiColumnConfig = null;
		if (null != kpiColumnConfigDTO) {
			ModelMapper mapper = new ModelMapper();
			kpiColumnConfig = mapper.map(kpiColumnConfigDTO, KpiColumnConfig.class);
		}
		if (null == kpiColumnConfig) {
			log.info("kpiColumnConfig object is empty");
			return new ServiceResponse(false, "kpiColumnConfig  cannot be empty", null);
		}
		if (!valid(kpiColumnConfig)) {
			log.info("kpiColumnConfig is not valid");
			return new ServiceResponse(false, "BasicProjectConfigId, KpiId, cannot be empty or null", null);
		}

		KpiColumnConfig existingKpiColumnConfig = kpiColumnConfigRepository.findByBasicProjectConfigIdAndKpiId(
				kpiColumnConfig.getBasicProjectConfigId(), kpiColumnConfig.getKpiId());

		if (existingKpiColumnConfig != null) {

			// Update the existing document
			existingKpiColumnConfig.setKpiColumnDetails(kpiColumnConfig.getKpiColumnDetails());
			kpiColumnConfigRepository.save(existingKpiColumnConfig);
			log.info("Successfully updated kpiColumnConfig in db");
			return new ServiceResponse(true, "updated kpiColumnConfig", existingKpiColumnConfig);
		} else {
			// Create a new document
			kpiColumnConfigRepository.save(kpiColumnConfig);
			log.info("Successfully created and saved kpiColumnConfig into db");
			return new ServiceResponse(true, "created and saved new kpiColumnConfig", kpiColumnConfig);
		}

	}

	@Override
	public KpiColumnConfigDTO getByKpiColumnConfig(String basicProjectConfigId, String kpiId) {
		ObjectId basicProjectConfigObjectId = new ObjectId(basicProjectConfigId);
		KpiColumnConfig existingKpiColumnConfig = kpiColumnConfigRepository
				.findByBasicProjectConfigIdAndKpiId(basicProjectConfigObjectId, kpiId);
		if (existingKpiColumnConfig != null) {
			return convertToKpiColumnConfigDTO(existingKpiColumnConfig);
		} else {
			// return the default configuration
			KpiColumnConfig defaultKpiColumnConfig = kpiColumnConfigRepository.findByBasicProjectConfigIdAndKpiId(null,
					kpiId);
			return convertToKpiColumnConfigDTO(defaultKpiColumnConfig);
		}
	}

	public boolean valid(KpiColumnConfig kpiColumnConfig) {
		if (kpiColumnConfig.getBasicProjectConfigId() == null) {
			log.info("projectBasicConfigId is null");
			return false;
		}

		if (kpiColumnConfig.getKpiId() == null) {
			log.info("kpiId is null");
			return false;
		}

		if (kpiColumnConfig.getKpiId().isEmpty()) {
			log.info("KpiId is empty");
			return false;
		}

		return true;
	}

	/**
	 * This method convert KpiColumnConfig to its dto
	 *
	 * @param kpiColumnConfig
	 *            kpiColumnConfig
	 * @return KpiColumnConfigDTO
	 */
	private KpiColumnConfigDTO convertToKpiColumnConfigDTO(KpiColumnConfig kpiColumnConfig) {
		KpiColumnConfigDTO kpiColumnConfigDTO = null;
		if (null != kpiColumnConfig) {
			ModelMapper mapper = new ModelMapper();
			kpiColumnConfigDTO = mapper.map(kpiColumnConfig, KpiColumnConfigDTO.class);
		}
		return kpiColumnConfigDTO;
	}

}
