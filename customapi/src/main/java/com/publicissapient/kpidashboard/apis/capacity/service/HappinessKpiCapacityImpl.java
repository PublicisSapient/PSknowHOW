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

package com.publicissapient.kpidashboard.apis.capacity.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;

import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.jira.HappinessKpiDTO;
import com.publicissapient.kpidashboard.common.model.jira.HappinessKpiData;
import com.publicissapient.kpidashboard.common.repository.jira.HappinessKpiDataRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HappinessKpiCapacityImpl implements HappinessKpiCapacity {

	@Autowired
	HappinessKpiDataRepository happinessKpiDataRepository;

	@Override
	public ServiceResponse saveHappinessKpiData(HappinessKpiDTO happinessKpiDTO) {

		HappinessKpiData happinessKpiData = null;
		LocalDate localDate = LocalDate.now();
		DateTimeFormatter formatterLocalDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String result = formatterLocalDate.format(localDate);
		if (null != happinessKpiDTO) {
			ModelMapper mapper = new ModelMapper();
			happinessKpiDTO.setDateOfSubmission(result);
			happinessKpiData = mapper.map(happinessKpiDTO, HappinessKpiData.class);
			happinessKpiData.setBasicProjectConfigId(new ObjectId(happinessKpiDTO.getBasicProjectConfigId()));
		}
		if (null == happinessKpiData) {
			log.info("happinessKpiData object is empty");
			return new ServiceResponse(false, "happinessKpiData cannot be empty", null);
		}

		if (Objects.isNull(happinessKpiData.getUserRatingList())) {
			happinessKpiData.setUserRatingList(new ArrayList<>());
		}

		if (!valid(happinessKpiData)) {
			log.info("happinessKpiData is not valid");
			return new ServiceResponse(false,
					"BasicProjectConfigId, Sprint Id or userRatingList cannot be empty or null", null);
		}

		HappinessKpiData existingForSameDay = happinessKpiDataRepository
				.findExistingByBasicProjectConfigIdAndSprintIDAndDateOfSubmission(
						happinessKpiData.getBasicProjectConfigId(), happinessKpiData.getSprintID(),
						happinessKpiData.getDateOfSubmission());
		if (existingForSameDay != null) {
			happinessKpiData.setId(existingForSameDay.getId());
			happinessKpiDataRepository.save(happinessKpiData);
			log.info("Successfully updated happinessKpiData into db");
			return new ServiceResponse(true, "updated existing happinessKpiData", happinessKpiData);
		} else {
			happinessKpiDataRepository.save(happinessKpiData);
			log.info("Successfully created and saved happinessKpiData into db");
			return new ServiceResponse(true, "created and saved new happinessKpiData", happinessKpiData);
		}
	}

	public boolean valid(HappinessKpiData happinessKpiData) {
		if (happinessKpiData.getBasicProjectConfigId() == null) {
			log.info("projectBasicConfigId is null");
			return false;
		}
		if (happinessKpiData.getSprintID() == null || happinessKpiData.getSprintID().isEmpty()) {
			log.info("sprintID is null or empty");
			return false;
		}
		if (happinessKpiData.getDateOfSubmission() == null || happinessKpiData.getDateOfSubmission().isEmpty()) {
			log.info("dateOfSubmission is null or empty");
			return false;
		}

		return true;
	}
}
