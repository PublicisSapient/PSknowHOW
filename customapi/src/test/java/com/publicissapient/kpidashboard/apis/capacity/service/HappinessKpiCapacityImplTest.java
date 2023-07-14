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

import static org.testng.Assert.assertEquals;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.jira.HappinessKpiDTO;
import com.publicissapient.kpidashboard.common.model.jira.HappinessKpiData;
import com.publicissapient.kpidashboard.common.model.jira.UserRatingDTO;
import com.publicissapient.kpidashboard.common.model.jira.UserRatingData;
import com.publicissapient.kpidashboard.common.repository.jira.HappinessKpiDataRepository;

@RunWith(MockitoJUnitRunner.class)
public class HappinessKpiCapacityImplTest {
	@InjectMocks
	HappinessKpiCapacityImpl happinessKpiService;

	@Mock
	HappinessKpiDataRepository happinessKpiDataRepository;

	@Test
	public void saveHappinessKpiDataSuccessUpdateTest() {
		Mockito.when(happinessKpiDataRepository.findExistingByBasicProjectConfigIdAndSprintIDAndDateOfSubmission(
				Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(createHappinessData());
		ServiceResponse serviceResponse = happinessKpiService.saveHappinessKpiData(createHappinessDto());
		assertEquals(serviceResponse.getSuccess(), Boolean.TRUE);
	}

	@Test
	public void saveHappinessKpiDataSuccessSaveTest() {
		ServiceResponse serviceResponse = happinessKpiService.saveHappinessKpiData(createHappinessDto());
		assertEquals(serviceResponse.getSuccess(), Boolean.TRUE);
	}

	@Test
	public void saveHappinessKpiDataFailureTest() {
		ServiceResponse serviceResponse = happinessKpiService.saveHappinessKpiData(null);
		assertEquals(serviceResponse.getSuccess(), Boolean.FALSE);
	}

	@Test
	public void saveHappinessKpiDataValidationFailureTest() {
		HappinessKpiDTO happinessKpiDTO = createHappinessDto();
		happinessKpiDTO.setSprintID(null);
		ServiceResponse serviceResponse = happinessKpiService.saveHappinessKpiData(happinessKpiDTO);
		assertEquals(serviceResponse.getSuccess(), Boolean.FALSE);
	}

	private HappinessKpiData createHappinessData() {

		HappinessKpiData happinessKpiData = new HappinessKpiData();
		happinessKpiData.setBasicProjectConfigId(new ObjectId("64198a3c2a718307f32fb094"));
		happinessKpiData.setSprintID("41412_Elxi corp_64198a3c2a718307f32fb094");
		LocalDate localDate = LocalDate.now();
		DateTimeFormatter formatterLocalDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String result = formatterLocalDate.format(localDate);
		happinessKpiData.setDateOfSubmission(result);
		List<UserRatingData> userRatingData = new ArrayList<>();
		userRatingData.add(new UserRatingData(3, "Adina-Alexandra Ursache", "adiursac"));
		userRatingData.add(new UserRatingData(5, "Akshat Shrivastav", "aksshriv1"));
		userRatingData.add(new UserRatingData(2, "Anil Kumar Singh", "anisingh4"));
		happinessKpiData.setUserRatingList(userRatingData);
		return happinessKpiData;
	}

	private HappinessKpiDTO createHappinessDto() {

		HappinessKpiDTO happinessKpiDTO = new HappinessKpiDTO();
		happinessKpiDTO.setBasicProjectConfigId("64198a3c2a718307f32fb094");
		happinessKpiDTO.setSprintID("41412_Elxi corp_64198a3c2a718307f32fb094");
		LocalDate localDate = LocalDate.now();
		DateTimeFormatter formatterLocalDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String result = formatterLocalDate.format(localDate);
		happinessKpiDTO.setDateOfSubmission(result);
		List<UserRatingDTO> userRatingDTO = new ArrayList<>();
		userRatingDTO.add(new UserRatingDTO(3, "Adina-Alexandra Ursache", "adiursac"));
		userRatingDTO.add(new UserRatingDTO(5, "Akshat Shrivastav", "aksshriv1"));
		userRatingDTO.add(new UserRatingDTO(2, "Anil Kumar Singh", "anisingh4"));
		happinessKpiDTO.setUserRatingList(userRatingDTO);
		return happinessKpiDTO;
	}

}
