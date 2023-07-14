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

package com.publicissapient.kpidashboard.apis.kpivideo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.kpivideolink.KPIVideoLink;
import com.publicissapient.kpidashboard.common.repository.kpivideolink.KPIVideoLinkRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KPIVideoLinkServiceImpl implements KPIVideoLinkService {

	@Autowired
	private KPIVideoLinkRepository kpiLinkRepository;

	/**
	 * Update kpi_video_link data.
	 *
	 * @return ServiceResponse with data object,message and status flag true if data
	 *         is found,false if not data found
	 */

	@Override
	public ServiceResponse update(String kpiId, KPIVideoLink kvl) {

		final KPIVideoLink existingKpiDetails = kpiLinkRepository.findByKpiId(kpiId);
		if (null == existingKpiDetails) {
			return new ServiceResponse(false, "No kpi id found to update", null);
		}

		existingKpiDetails.setVideoUrl(kvl.getVideoUrl());
		kpiLinkRepository.save(existingKpiDetails);
		log.info("Successfully Updated Kpi Video Links  Into Db @{}", kpiId);
		return new ServiceResponse(true, "Updated The Video Link Successfully", existingKpiDetails);
	}

	@Override
	public List<KPIVideoLink> getAllVideoLinks() {
		return kpiLinkRepository.findAll();
	}
}
