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

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import com.publicissapient.kpidashboard.apis.constant.Constant;
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
	public void testClearCache() {
		String cacheKey = "12345_kpi1";
		Cache cache = mock(Cache.class);
		ConcurrentHashMap<Object, Object> map = new ConcurrentHashMap();
		map.put(cacheKey, new Object());

		when(cache.getNativeCache()).thenReturn(map);
		when(cacheManager.getCache(Constant.CACHE_PROJECT_KPI_DATA)).thenReturn(cache);

		kpiDataCacheService.clearCache("kpi1");
		verify(cache, times(1)).evict(cacheKey);

		kpiDataCacheService.clearCache("kpi2");
		verify(cache, times(0)).evict("12345_kpi2");
	}

	@Test
	public void testClearCacheForProjectAndKpi() {
		kpiDataCacheService.clearCache("12345", "kpi1");
	}

	@Test
	public void testClearCacheForProject() {
		String cacheKey = "12345_kpi1";
		Cache cache = mock(Cache.class);
		ConcurrentHashMap<Object, Object> map = new ConcurrentHashMap();
		map.put(cacheKey, new Object());

		when(cache.getNativeCache()).thenReturn(map);
		when(cacheManager.getCache(Constant.CACHE_PROJECT_KPI_DATA)).thenReturn(cache);

		kpiDataCacheService.clearCacheForProject("12345");
		verify(cache, times(1)).evict(cacheKey);

		kpiDataCacheService.clearCacheForProject("12312");
		verify(cache, times(0)).evict("12312_kpi1");
	}

	@Test
	public void testClearCacheForSource() {
		String cacheKey = "12345_" + KPICode.BUILD_FREQUENCY.getKpiId();
		Cache cache = mock(Cache.class);
		ConcurrentHashMap<Object, Object> map = new ConcurrentHashMap();
		map.put(cacheKey, new Object());

		when(cache.getNativeCache()).thenReturn(map);
		when(cacheManager.getCache(Constant.CACHE_PROJECT_KPI_DATA)).thenReturn(cache);

		kpiDataCacheService.clearCacheForSource(KPISource.JENKINS.name());
		verify(cache, times(1)).evict(cacheKey);
	}

	@Test
    public void testFetchIssueCountData() {
        when(kpiDataProvider.fetchIssueCountDataFromDB(any(), any(), any())).thenReturn(new HashMap<>());
        assertNotNull(kpiDataCacheService.fetchIssueCountData(new KpiRequest(), new ObjectId(), new ArrayList<>(), "kpi1"));
    }

	@Test
	public void testfetchBuildFrequencydata() {
		when(kpiDataProvider.fetchBuildFrequencyData(any(), any(), any())).thenReturn(new ArrayList<>());
		List<Build> result = kpiDataCacheService.fetchBuildFrequencyData(new ObjectId(), "",
				"", "kpi1");
		assertNotNull(result);
	}

	@Test
	public void fetchSprintCapacityData_shouldReturnCorrectData_whenValidInput() {
		when(kpiDataProvider.fetchSprintCapacityDataFromDb(any(), any(), any())).thenReturn(new HashMap<>());
		Map<String, Object> result = kpiDataCacheService.fetchSprintCapacityData(new KpiRequest(), new ObjectId(),
				new ArrayList<>(), "kpi1");
		assertNotNull(result);
	}

	@Test
	public void fetchSprintPredictabilityData_shouldReturnCorrectData_whenValidInput() {
		when(kpiDataProvider.fetchSprintPredictabilityDataFromDb(any(), any(), any())).thenReturn(new HashMap<>());
		Map<String, Object> result = kpiDataCacheService.fetchSprintPredictabilityData(new KpiRequest(), new ObjectId(),
				new ArrayList<>(), "kpi5");
		assertNotNull(result);
	}

	@Test
	public void fetchSprintVelocityData_shouldReturnCorrectData_whenValidInput() {
		when(kpiDataProvider.fetchSprintVelocityDataFromDb(any(), any())).thenReturn(new HashMap<>());
		Map<String, Object> result = kpiDataCacheService.fetchSprintVelocityData(new KpiRequest(), new ObjectId(),
				"kpi39");
		assertNotNull(result);
	}

	@Test
	public void testFetchScopeChurnData() {
		when(kpiDataProvider.fetchScopeChurnData(any(), any(), any())).thenReturn(new HashMap<>());
		assertNotNull(kpiDataCacheService.fetchScopeChurnData(new KpiRequest(), new ObjectId(), new ArrayList<>(), "kpi1"));
	}

	@Test
	public void testFetchCommitmentReliabilityData() {
		when(kpiDataProvider.fetchCommitmentReliabilityData(any(), any(), any())).thenReturn(new HashMap<>());
		assertNotNull(kpiDataCacheService.fetchCommitmentReliabilityData(new KpiRequest(), new ObjectId(), new ArrayList<>(), "kpi1"));
	}

	@Test
	public void testFetchCostOfDelayData() {
		when(kpiDataProvider.fetchCostOfDelayData(any())).thenReturn(new HashMap<>());
		assertNotNull(kpiDataCacheService.fetchCostOfDelayData(new ObjectId(), KPICode.COST_OF_DELAY.getKpiId()));
	}

	@Test
	public void testFetchProjectReleaseData() {
		when(kpiDataProvider.fetchProjectReleaseData(any())).thenReturn(new ArrayList<>());
		assertNotNull(kpiDataCacheService.fetchProjectReleaseData(new ObjectId(), KPICode.PROJECT_RELEASES.getKpiId()));
	}
}