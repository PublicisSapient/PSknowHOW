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

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cache.CacheManager;

import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.common.model.application.Build;

@RunWith(MockitoJUnitRunner.class)
public class KpiDataCacheServiceImplTest {
	@Mock
	private CacheManager cacheManager;

	@Mock
	private KpiDataProvider kpiDataProvider;

	@InjectMocks
	private KpiDataCacheServiceImpl kpiDataCacheService;

	@Test
	public void testClearCacheForProjectAndKpi() {
		kpiDataCacheService.clearCache("12345", "kpi1");
	}

	@Test
	public void testGetKpiBasedOnSource() {
		List<String> list = kpiDataCacheService.getKpiBasedOnSource(KPISource.JIRA.name());
		assertNotEquals(0, list.size());
	}

	@Test
	public void testFetchIssueCountData() {
		when(kpiDataProvider.fetchIssueCountDataFromDB(any(), any(), any()))
				.thenReturn(new HashMap<>());
		assertNotNull(
				kpiDataCacheService.fetchIssueCountData(
						new KpiRequest(), new ObjectId(), new ArrayList<>(), "kpi1"));
	}

	@Test
	public void testfetchBuildFrequencydata() {
		when(kpiDataProvider.fetchBuildFrequencyData(any(), any(), any()))
				.thenReturn(new ArrayList<>());
		List<Build> result =
				kpiDataCacheService.fetchBuildFrequencyData(new ObjectId(), "", "", "kpi1");
		assertNotNull(result);
	}

	@Test
	public void fetchSprintCapacityData_shouldReturnCorrectData_whenValidInput() {
		when(kpiDataProvider.fetchSprintCapacityDataFromDb(any(), any(), any()))
				.thenReturn(new HashMap<>());
		Map<String, Object> result =
				kpiDataCacheService.fetchSprintCapacityData(
						new KpiRequest(), new ObjectId(), new ArrayList<>(), "kpi1");
		assertNotNull(result);
	}

	@Test
	public void fetchSprintPredictabilityData_shouldReturnCorrectData_whenValidInput() {
		when(kpiDataProvider.fetchSprintPredictabilityDataFromDb(any(), any(), any()))
				.thenReturn(new HashMap<>());
		Map<String, Object> result =
				kpiDataCacheService.fetchSprintPredictabilityData(
						new KpiRequest(), new ObjectId(), new ArrayList<>(), "kpi5");
		assertNotNull(result);
	}

	@Test
	public void fetchHappinessIndexData_shouldReturnCorrectData_whenValidInput() {
		when(kpiDataProvider.fetchHappinessIndexDataFromDb(any())).thenReturn(new HashMap<>());
		Map<String, Object> result =
				kpiDataCacheService.fetchHappinessIndexData(new ObjectId(), new ArrayList<>(), "kpi149");
		assertNotNull(result);
	}

	@Test
	public void fetchDIRData_shouldReturnCorrectData_whenValidInput() {
		when(kpiDataProvider.fetchDefectInjectionRateDataFromDb(any(), any(), any()))
				.thenReturn(new HashMap<>());
		Map<String, Object> result =
				kpiDataCacheService.fetchDefectInjectionRateData(
						new KpiRequest(), new ObjectId(), new ArrayList<>(), "kpi14");
		assertNotNull(result);
	}

	@Test
	public void fetchDFTPRData_shouldReturnCorrectData_whenValidInput() {
		when(kpiDataProvider.fetchFirstTimePassRateDataFromDb(any(), any(), any()))
				.thenReturn(new HashMap<>());
		Map<String, Object> result =
				kpiDataCacheService.fetchFirstTimePassRateData(
						new KpiRequest(), new ObjectId(), new ArrayList<>(), "kpi14");
		assertNotNull(result);
	}

	@Test
	public void fetchDDData_shouldReturnCorrectData_whenValidInput() {
		when(kpiDataProvider.fetchDefectDensityDataFromDb(any(), any(), any()))
				.thenReturn(new HashMap<>());
		Map<String, Object> result =
				kpiDataCacheService.fetchDefectDensityData(
						new KpiRequest(), new ObjectId(), new ArrayList<>(), "kpi111");
		assertNotNull(result);
	}

	@Test
	public void fetchSprintVelocityData_shouldReturnCorrectData_whenValidInput() {
		when(kpiDataProvider.fetchSprintVelocityDataFromDb(any(), any())).thenReturn(new HashMap<>());
		Map<String, Object> result =
				kpiDataCacheService.fetchSprintVelocityData(new KpiRequest(), new ObjectId(), "kpi39");
		assertNotNull(result);
	}

	@Test
	public void testFetchScopeChurnData() {
		when(kpiDataProvider.fetchScopeChurnData(any(), any(), any())).thenReturn(new HashMap<>());
		assertNotNull(
				kpiDataCacheService.fetchScopeChurnData(
						new KpiRequest(), new ObjectId(), new ArrayList<>(), "kpi1"));
	}

	@Test
	public void testFetchCommitmentReliabilityData() {
		when(kpiDataProvider.fetchCommitmentReliabilityData(any(), any(), any()))
				.thenReturn(new HashMap<>());
		assertNotNull(
				kpiDataCacheService.fetchCommitmentReliabilityData(
						new KpiRequest(), new ObjectId(), new ArrayList<>(), "kpi1"));
	}

	@Test
	public void testFetchCostOfDelayData() {
		when(kpiDataProvider.fetchCostOfDelayData(any())).thenReturn(new HashMap<>());
		assertNotNull(
				kpiDataCacheService.fetchCostOfDelayData(new ObjectId(), KPICode.COST_OF_DELAY.getKpiId()));
	}

	@Test
	public void testFetchProjectReleaseData() {
		when(kpiDataProvider.fetchProjectReleaseData(any())).thenReturn(new ArrayList<>());
		assertNotNull(
				kpiDataCacheService.fetchProjectReleaseData(
						new ObjectId(), KPICode.PROJECT_RELEASES.getKpiId()));
	}

	@Test
	public void testFetchPiPredictabilityData() {
		when(kpiDataProvider.fetchPiPredictabilityData(any())).thenReturn(new ArrayList<>());
		assertNotNull(
				kpiDataCacheService.fetchPiPredictabilityData(
						new ObjectId(), KPICode.PI_PREDICTABILITY.getKpiId()));
	}

	@Test
	public void testFetchCreatedVsResolvedData() {
		when(kpiDataProvider.fetchCreatedVsResolvedData(any(), any(), any()))
				.thenReturn(new HashMap<>());
		assertNotNull(
				kpiDataCacheService.fetchCreatedVsResolvedData(
						new KpiRequest(),
						new ObjectId(),
						new ArrayList<>(),
						KPICode.CREATED_VS_RESOLVED_DEFECTS.getKpiId()));
	}

	@Test
	public void testFetchDRRData() {
		when(kpiDataProvider.fetchDRRData(any(), any(), any())).thenReturn(new HashMap<>());
		assertNotNull(
				kpiDataCacheService.fetchDRRData(
						new KpiRequest(),
						new ObjectId(),
						new ArrayList<>(),
						KPICode.DEFECT_REJECTION_RATE.getKpiId()));
	}

	@Test
	public void testFetchDSRData() {
		when(kpiDataProvider.fetchDSRData(any(), any(), any())).thenReturn(new HashMap<>());
		assertNotNull(
				kpiDataCacheService.fetchDSRData(
						new KpiRequest(),
						new ObjectId(),
						new ArrayList<>(),
						KPICode.DEFECT_SEEPAGE_RATE.getKpiId()));
	}
}
