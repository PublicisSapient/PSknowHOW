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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.publicissapient.kpidashboard.apis.common.service.KpiDataCacheService;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.common.model.application.Build;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.constant.Constant;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of {@link KpiDataCacheService}.
 * 
 * @author prijain3
 */
@Service
@Slf4j
public class KpiDataCacheServiceImpl implements KpiDataCacheService {

	@Autowired
	@Qualifier("cacheManager")
	private CacheManager cacheManager;

	@Autowired
	KpiDataProvider kpiDataProvider;

	@Override
	public void clearCache(String kpiId) {
		log.info("Evict KPI cache for KPI - {} ", kpiId);
		Cache cache = cacheManager.getCache(Constant.CACHE_PROJECT_KPI_DATA);
		if (null != cache) {
			ConcurrentHashMap<Object, Object> map = (ConcurrentHashMap<Object, Object>) cache.getNativeCache();
			Set<Object> keys = map.keySet();
			keys.forEach(key -> {
				if (key.toString().endsWith(kpiId)) {
					cache.evict(key);
				}
			});
		}
	}

	@CacheEvict(value = Constant.CACHE_PROJECT_KPI_DATA, key = "#basicProjectConfigId.concat('_').concat(#kpiId)")
	@Override
	public void clearCache(String basicProjectConfigId, String kpiId) {
		log.info("Evict KPI cache for project id - {} and kpi - {}", basicProjectConfigId, kpiId);
	}

	@Cacheable(value = Constant.CACHE_PROJECT_KPI_DATA, key = "#basicProjectConfigId.toString().concat('_').concat(#kpiId)")
	@Override
	public Map<String, Object> fetchIssueCountData(KpiRequest kpiRequest, ObjectId basicProjectConfigId,
			List<String> sprintList, String kpiId) {
		log.info("Fetching Data for Project {} and KPI {}", basicProjectConfigId.toString(), kpiId);
		return kpiDataProvider.fetchIssueCountDataFromDB(kpiRequest, basicProjectConfigId, sprintList);
	}

	@Cacheable(value = Constant.CACHE_PROJECT_KPI_DATA, key = "#basicProjectConfigId.toString().concat('_').concat(#kpiId)")
	@Override
	public List<Build> fetchBuildFrequencydata(ObjectId basicProjectConfigId, String startDate, String endDate,
											   String kpiId) {
		log.info("Fetching Build Frequency KPI Data for Project {} and KPI {}", basicProjectConfigId.toString(), kpiId);
		return kpiDataProvider.fetchBuildFrequencydata(basicProjectConfigId, startDate, endDate);
	}

}