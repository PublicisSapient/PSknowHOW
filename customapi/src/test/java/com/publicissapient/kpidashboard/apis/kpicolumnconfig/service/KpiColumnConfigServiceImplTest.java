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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.modelmapper.ModelMapper;

import com.publicissapient.kpidashboard.apis.data.KpiColumnConfigDataFactory;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.application.KpiColumnConfig;
import com.publicissapient.kpidashboard.common.model.application.KpiColumnConfigDTO;
import com.publicissapient.kpidashboard.common.model.application.KpiColumnDetails;
import com.publicissapient.kpidashboard.common.repository.application.KpiColumnConfigRepository;

@RunWith(MockitoJUnitRunner.class)
public class KpiColumnConfigServiceImplTest {

	@InjectMocks
	private KpiColumnConfigServiceImpl kpiColumnConfigService;

	@Mock
	private KpiColumnConfigRepository kpiColumnConfigRepository;

	private List<KpiColumnConfig> kpiColumnConfigs;

	@Before
	public void setUp() {
		kpiColumnConfigs = KpiColumnConfigDataFactory.newInstance().getKpiColumnConfigs();
	}

	@Test
	public void testSaveKpiColumnConfig() {
		when(kpiColumnConfigRepository.findByBasicProjectConfigIdAndKpiId(any(), any())).thenReturn(null);
		ServiceResponse response = kpiColumnConfigService
				.saveKpiColumnConfig(convertToKpiColumnConfigDTO(kpiColumnConfigs.get(0)));
		assertEquals(response.getSuccess(), Boolean.TRUE);
	}

	@Test
	public void testGetByKpiColumnConfig() {
		KpiColumnConfig kpiColumnConfig1 = kpiColumnConfigs.get(0);
		when(kpiColumnConfigRepository.findByBasicProjectConfigIdAndKpiId(any(), any())).thenReturn(kpiColumnConfig1);
		KpiColumnConfigDTO kpiColumnConfigDTO = kpiColumnConfigService.getByKpiColumnConfig(
				kpiColumnConfig1.getBasicProjectConfigId().toString(), kpiColumnConfig1.getKpiId());
		KpiColumnConfigDTO kpiColumnConfig1DTO = convertToKpiColumnConfigDTO(kpiColumnConfig1);
		assertEquals(kpiColumnConfigDTO, kpiColumnConfig1DTO);
	}

	@Test
	public void testGetByKpiColumnConfigDefault() {
		KpiColumnConfig kpiColumnConfig1 = kpiColumnConfigs.get(1);
		String basicProjectConfigId = "6417fe6a74821060a7133de7";
		when(kpiColumnConfigRepository.findByBasicProjectConfigIdAndKpiId(any(), any())).thenReturn(null);
		KpiColumnConfigDTO kpiColumnConfigDTO = kpiColumnConfigService.getByKpiColumnConfig(basicProjectConfigId,
				kpiColumnConfig1.getKpiId());
		assertNull(kpiColumnConfigDTO);
	}

	@Test
	public void testSaveKpiColumnConfigNull() {
		ServiceResponse response = kpiColumnConfigService.saveKpiColumnConfig(null);
		assertEquals(response.getSuccess(), Boolean.FALSE);
	}

	@Test
	public void testSaveKpiColumnConfigValidBasicProjectConfigId() {
		KpiColumnConfigDTO kpiColumnConfigDTO = convertToKpiColumnConfigDTO(kpiColumnConfigs.get(0));
		kpiColumnConfigDTO.setBasicProjectConfigId(null);
		ServiceResponse response = kpiColumnConfigService.saveKpiColumnConfig(kpiColumnConfigDTO);
		assertEquals(response.getSuccess(), Boolean.FALSE);
	}

	@Test
	public void testSaveKpiColumnConfigValidKpiId() {
		KpiColumnConfigDTO kpiColumnConfigDTO = convertToKpiColumnConfigDTO(kpiColumnConfigs.get(0));
		kpiColumnConfigDTO.setKpiId(null);
		ServiceResponse response = kpiColumnConfigService.saveKpiColumnConfig(kpiColumnConfigDTO);
		assertEquals(response.getSuccess(), Boolean.FALSE);
	}

	@Test
	public void testSaveKpiColumnConfigKpiIdNotEmpty() {
		KpiColumnConfigDTO kpiColumnConfigDTO = convertToKpiColumnConfigDTO(kpiColumnConfigs.get(0));
		kpiColumnConfigDTO.setKpiId("");
		ServiceResponse response = kpiColumnConfigService.saveKpiColumnConfig(kpiColumnConfigDTO);
		assertEquals(response.getSuccess(), Boolean.FALSE);
	}

	@Test
	public void testSaveKpiColumnConfigUpdateExistingDoc() {
		KpiColumnConfig kpiColumnConfig1 = kpiColumnConfigs.get(0);
		when(kpiColumnConfigRepository.findByBasicProjectConfigIdAndKpiId(any(), any())).thenReturn(kpiColumnConfig1);
		List<KpiColumnDetails> updatedConfig = kpiColumnConfig1.getKpiColumnDetails();
		KpiColumnConfig kpiColumnConfig2 = new KpiColumnConfig();
		kpiColumnConfig2.setBasicProjectConfigId(kpiColumnConfig1.getBasicProjectConfigId());
		kpiColumnConfig2.setKpiId(kpiColumnConfig1.getKpiId());
		KpiColumnConfigDTO kpiColumnConfig2DTO = convertToKpiColumnConfigDTO(kpiColumnConfig2);
		ServiceResponse response = kpiColumnConfigService.saveKpiColumnConfig(kpiColumnConfig2DTO);
		assertEquals(response.getSuccess(), Boolean.TRUE);
	}

	private KpiColumnConfigDTO convertToKpiColumnConfigDTO(KpiColumnConfig kpiColumnConfig) {
		KpiColumnConfigDTO kpiColumnConfigDTO = null;
		if (null != kpiColumnConfig) {
			ModelMapper mapper = new ModelMapper();
			kpiColumnConfigDTO = mapper.map(kpiColumnConfig, KpiColumnConfigDTO.class);
		}
		return kpiColumnConfigDTO;
	}

	@After
	public void cleanup() {
		kpiColumnConfigRepository.deleteAll();
	}
}