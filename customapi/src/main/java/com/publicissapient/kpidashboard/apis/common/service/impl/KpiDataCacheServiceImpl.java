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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.common.service.KpiDataCacheService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.ProjectRelease;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;

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

	@Override
	public void clearCacheForProject(String basicProjectConfigId) {
		log.info("Evict KPI data cache for project - {} ", basicProjectConfigId);
		Cache cache = cacheManager.getCache(Constant.CACHE_PROJECT_KPI_DATA);
		if (null != cache) {
			ConcurrentHashMap<Object, Object> map = (ConcurrentHashMap<Object, Object>) cache.getNativeCache();
			Set<Object> keys = map.keySet();
			keys.forEach(key -> {
				if (key.toString().startsWith(basicProjectConfigId)) {
					cache.evict(key);
				}
			});
		}
	}

	@Override
	public void clearCacheForSource(String source) {
		List<String> kpiList = getKpiBasedOnSource(source);
		kpiList.forEach(this::clearCache);
	}

	@Override
	public List<String> getKpiBasedOnSource(String source) {
		Map<String, List<String>> kpiMap = new HashMap<>();
		kpiMap.put(KPISource.JIRA.name(),
				List.of(KPICode.ISSUE_COUNT.getKpiId(), KPICode.COMMITMENT_RELIABILITY.getKpiId(),
						KPICode.SPRINT_CAPACITY_UTILIZATION.getKpiId(), KPICode.SCOPE_CHURN.getKpiId(),
						KPICode.COST_OF_DELAY.getKpiId(), KPICode.SPRINT_PREDICTABILITY.getKpiId(),
						KPICode.SPRINT_VELOCITY.getKpiId(), KPICode.PROJECT_RELEASES.getKpiId(),
						KPICode.PI_PREDICTABILITY.getKpiId()));
		kpiMap.put(KPISource.JIRAKANBAN.name(), new ArrayList<>());
		kpiMap.put(KPISource.SONAR.name(), new ArrayList<>());
		kpiMap.put(KPISource.SONARKANBAN.name(), new ArrayList<>());
		kpiMap.put(KPISource.BITBUCKET.name(), new ArrayList<>());
		kpiMap.put(KPISource.BITBUCKETKANBAN.name(), new ArrayList<>());
		kpiMap.put(KPISource.JENKINS.name(), List.of(KPICode.BUILD_FREQUENCY.getKpiId()));
		kpiMap.put(KPISource.JENKINSKANBAN.name(), new ArrayList<>());
		kpiMap.put(KPISource.ZEPHYR.name(), new ArrayList<>());
		kpiMap.put(KPISource.ZEPHYRKANBAN.name(), new ArrayList<>());

		return kpiMap.getOrDefault(source, new ArrayList<>());
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
	public Map<String, Object> fetchSprintPredictabilityData(KpiRequest kpiRequest, ObjectId basicProjectConfigId,
			List<String> sprintList, String kpiId) {
		log.info("Fetching Sprint Predictability KPI Data for Project {} and KPI {}", basicProjectConfigId.toString(),
				kpiId);
		return kpiDataProvider.fetchSprintPredictabilityDataFromDb(kpiRequest, basicProjectConfigId, sprintList);
	}

	@Cacheable(value = Constant.CACHE_PROJECT_KPI_DATA, key = "#basicProjectConfigId.toString().concat('_').concat(#kpiId)")
	@Override
	public Map<String, Object> fetchSprintVelocityData(KpiRequest kpiRequest, ObjectId basicProjectConfigId,
			String kpiId) {
		log.info("Fetching Sprint Velocity KPI Data for Project {} and KPI {}", basicProjectConfigId.toString(), kpiId);
		return kpiDataProvider.fetchSprintVelocityDataFromDb(kpiRequest, basicProjectConfigId);
	}

	@Cacheable(value = Constant.CACHE_PROJECT_KPI_DATA, key = "#basicProjectConfigId.toString().concat('_').concat(#kpiId)")
	@Override
	public List<Build> fetchBuildFrequencyData(ObjectId basicProjectConfigId, String startDate, String endDate,
			String kpiId) {
		log.info("Fetching Build Frequency KPI Data for Project {} and KPI {}", basicProjectConfigId.toString(), kpiId);
		return kpiDataProvider.fetchBuildFrequencyData(basicProjectConfigId, startDate, endDate);
	}

	@Cacheable(value = Constant.CACHE_PROJECT_KPI_DATA, key = "#basicProjectConfigId.toString().concat('_').concat(#kpiId)")
	@Override
	public Map<String, Object> fetchSprintCapacityData(KpiRequest kpiRequest, ObjectId basicProjectConfigId,
			List<String> sprintList, String kpiId) {
		log.info("Fetching Sprint Capacity Utilization KPI Data for Project {} and KPI {}",
				basicProjectConfigId.toString(), kpiId);
		return kpiDataProvider.fetchSprintCapacityDataFromDb(kpiRequest, basicProjectConfigId, sprintList);
	}

	@Cacheable(value = Constant.CACHE_PROJECT_KPI_DATA, key = "#basicProjectConfigId.toString().concat('_').concat(#kpiId)")
	@Override
	public Map<String, Object> fetchScopeChurnData(KpiRequest kpiRequest, ObjectId basicProjectConfigId,
			List<String> sprintList, String kpiId) {
		log.info("Fetching Scope Churn KPI Data for Project {} and KPI {}", basicProjectConfigId.toString(), kpiId);
		return kpiDataProvider.fetchScopeChurnData(kpiRequest, basicProjectConfigId, sprintList);
	}

	@Cacheable(value = Constant.CACHE_PROJECT_KPI_DATA, key = "#basicProjectConfigId.toString().concat('_').concat(#kpiId)")
	@Override
	public Map<String, Object> fetchCommitmentReliabilityData(KpiRequest kpiRequest, ObjectId basicProjectConfigId,
			List<String> sprintList, String kpiId) {
		log.info("Fetching Commitment Reliability KPI Data for Project {} and KPI {}", basicProjectConfigId.toString(),
				kpiId);
		return kpiDataProvider.fetchCommitmentReliabilityData(kpiRequest, basicProjectConfigId, sprintList);
	}

	@Cacheable(value = Constant.CACHE_PROJECT_KPI_DATA, key = "#basicProjectConfigId.toString().concat('_').concat(#kpiId)")
	@Override
	public Map<String, Object> fetchCostOfDelayData(ObjectId basicProjectConfigId, String kpiId) {
		log.info("Fetching Cost of Delay KPI Data for Project {} and KPI {}", basicProjectConfigId.toString(), kpiId);
		return kpiDataProvider.fetchCostOfDelayData(basicProjectConfigId);
	}

	@Cacheable(value = Constant.CACHE_PROJECT_KPI_DATA, key = "#basicProjectConfigId.toString().concat('_').concat(#kpiId)")
	@Override
	public List<ProjectRelease> fetchProjectReleaseData(ObjectId basicProjectConfigId, String kpiId) {
		log.info("Fetching Release Frequency KPI Data for Project {} and KPI {}", basicProjectConfigId.toString(),
				kpiId);
		return kpiDataProvider.fetchProjectReleaseData(basicProjectConfigId);
	}

	@Cacheable(value = Constant.CACHE_PROJECT_KPI_DATA, key = "#basicProjectConfigId.toString().concat('_').concat(#kpiId)")
	@Override
	public List<JiraIssue> fetchPiPredictabilityData(ObjectId basicProjectConfigId, String kpiId) {
		log.info("Fetching PI Predictability KPI Data for Project {} and KPI {}", basicProjectConfigId.toString(),
				kpiId);
		return kpiDataProvider.fetchPiPredictabilityData(basicProjectConfigId);
	}

}