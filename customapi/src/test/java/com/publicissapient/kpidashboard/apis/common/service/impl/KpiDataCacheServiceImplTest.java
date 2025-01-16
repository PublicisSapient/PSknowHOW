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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.SimpleValueWrapper;

import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterCategory;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;

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
	public void testClearCacheForProject() {
		kpiDataCacheService.clearCache("12345", "kpi1");
	}

	@Test
    public void testFetchIssueCountData() {
        when(kpiDataProvider.fetchIssueCountDataFromDB(any(), any(), any())).thenReturn(new HashMap<>());
        assertNotNull(kpiDataCacheService.fetchIssueCountData(new KpiRequest(), new ObjectId(), new ArrayList<>(), "kpi1"));
    }

}