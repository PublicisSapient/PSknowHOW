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

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

	@CacheEvict(value = Constant.CACHE_PROJECT_KPI_DATA, key = "#basicProjectConfigId.concat('_').concat(#kpiId)")
	@Override
	public void clearCache(String basicProjectConfigId, String kpiId) {
		log.info("Evict KPI cache for project id - {} and kpi - {}", basicProjectConfigId, kpiId);
	}

	@Override
	public List<String> getKpiBasedOnSource(String source) {
		Map<String, List<String>> kpiMap = new HashMap<>();
		kpiMap.put(KPISource.JIRA.name(),
				List.of(KPICode.ISSUE_COUNT.getKpiId(), KPICode.COMMITMENT_RELIABILITY.getKpiId(),
						KPICode.SPRINT_CAPACITY_UTILIZATION.getKpiId(), KPICode.SCOPE_CHURN.getKpiId(),
						KPICode.COST_OF_DELAY.getKpiId(), KPICode.SPRINT_PREDICTABILITY.getKpiId(),
						KPICode.SPRINT_VELOCITY.getKpiId(), KPICode.PROJECT_RELEASES.getKpiId(),
						KPICode.PI_PREDICTABILITY.getKpiId(), KPICode.CREATED_VS_RESOLVED_DEFECTS.getKpiId(),
						KPICode.HAPPINESS_INDEX_RATE.getKpiId(), KPICode.DEFECT_INJECTION_RATE.getKpiId(),
						KPICode.DEFECT_DENSITY.getKpiId(), KPICode.DEFECT_REJECTION_RATE.getKpiId(),
						KPICode.DEFECT_REMOVAL_EFFICIENCY.getKpiId(), KPICode.DEFECT_COUNT_BY_RCA.getKpiId(),
						KPICode.FIRST_TIME_PASS_RATE.getKpiId(), KPICode.DEFECT_SEEPAGE_RATE.getKpiId(),
						KPICode.DEFECT_COUNT_BY_PRIORITY.getKpiId()));
		kpiMap.put(KPISource.JIRAKANBAN.name(),
				List.of(KPICode.TEAM_CAPACITY.getKpiId(), KPICode.TICKET_VELOCITY.getKpiId(),
						KPICode.LEAD_TIME_KANBAN.getKpiId(), KPICode.COST_OF_DELAY_KANBAN.getKpiId(),
						KPICode.PROJECT_RELEASES_KANBAN.getKpiId(), KPICode.NET_OPEN_TICKET_COUNT_BY_STATUS.getKpiId(),
						KPICode.TICKET_COUNT_BY_PRIORITY.getKpiId(), KPICode.NET_OPEN_TICKET_COUNT_BY_RCA.getKpiId(),
						KPICode.TICKET_OPEN_VS_CLOSE_BY_PRIORITY.getKpiId(),
						KPICode.TICKET_OPEN_VS_CLOSED_RATE_BY_TYPE.getKpiId(),
						KPICode.OPEN_TICKET_AGING_BY_PRIORITY.getKpiId()));
		kpiMap.put(KPISource.SONAR.name(),
				List.of(KPICode.SONAR_CODE_QUALITY.getKpiId(), KPICode.SONAR_TECH_DEBT.getKpiId(),
						KPICode.CODE_VIOLATIONS.getKpiId(), KPICode.UNIT_TEST_COVERAGE.getKpiId(),
						KPICode.SONAR_TECH_DEBT_KANBAN.getKpiId(), KPICode.CODE_VIOLATIONS_KANBAN.getKpiId(),
						KPICode.UNIT_TEST_COVERAGE_KANBAN.getKpiId()));
		kpiMap.put(KPISource.BITBUCKET.name(), new ArrayList<>());
		kpiMap.put(KPISource.JENKINS.name(),
				List.of(KPICode.BUILD_FREQUENCY.getKpiId(), KPICode.CODE_BUILD_TIME_KANBAN.getKpiId()));
		kpiMap.put(KPISource.ZEPHYR.name(), List.of(KPICode.INSPRINT_AUTOMATION_COVERAGE.getKpiId(),
				KPICode.REGRESSION_AUTOMATION_COVERAGE.getKpiId(),
				KPICode.TEST_EXECUTION_AND_PASS_PERCENTAGE.getKpiId(),
				KPICode.KANBAN_REGRESSION_PASS_PERCENTAGE.getKpiId(), KPICode.TEST_EXECUTION_KANBAN.getKpiId()));

		List<String> allKpis = kpiMap.values().stream().flatMap(List::stream).toList();
		kpiMap.put(CommonConstant.ALL_KPI, allKpis);

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
	public Map<String, Object> fetchDefectInjectionRateData(KpiRequest kpiRequest, ObjectId basicProjectConfigId,
			List<String> sprintList, String kpiId) {
		log.info("Fetching DIR KPI Data for Project {} and KPI {}", basicProjectConfigId.toString(), kpiId);
		return kpiDataProvider.fetchDefectInjectionRateDataFromDb(kpiRequest, basicProjectConfigId, sprintList);
	}

	@Cacheable(value = Constant.CACHE_PROJECT_KPI_DATA, key = "#basicProjectConfigId.toString().concat('_').concat(#kpiId)")
	@Override
	public Map<String, Object> fetchFirstTimePassRateData(KpiRequest kpiRequest, ObjectId basicProjectConfigId,
			List<String> sprintList, String kpiId) {
		log.info("Fetching FTPR KPI Data for Project {} and KPI {}", basicProjectConfigId.toString(), kpiId);
		return kpiDataProvider.fetchFirstTimePassRateDataFromDb(kpiRequest, basicProjectConfigId, sprintList);
	}

	@Cacheable(value = Constant.CACHE_PROJECT_KPI_DATA, key = "#basicProjectConfigId.toString().concat('_').concat(#kpiId)")
	@Override
	public Map<String, Object> fetchDefectDensityData(KpiRequest kpiRequest, ObjectId basicProjectConfigId,
			List<String> sprintList, String kpiId) {
		log.info("Fetching Defect Density KPI Data for Project {} and KPI {}", basicProjectConfigId.toString(), kpiId);
		return kpiDataProvider.fetchDefectDensityDataFromDb(kpiRequest, basicProjectConfigId, sprintList);
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

	@Cacheable(value = Constant.CACHE_PROJECT_KPI_DATA, key = "#basicProjectConfigId.toString().concat('_').concat(#kpiId)")
	@Override
	public Map<String, Object> fetchCreatedVsResolvedData(KpiRequest kpiRequest, ObjectId basicProjectConfigId,
			List<String> sprintList, String kpiId) {
		log.info("Fetching Created vs Resolved KPI Data for Project {} and KPI {}", basicProjectConfigId.toString(),
				kpiId);
		return kpiDataProvider.fetchCreatedVsResolvedData(kpiRequest, basicProjectConfigId, sprintList);
	}

	@Cacheable(value = Constant.CACHE_PROJECT_KPI_DATA, key = "#basicProjectConfigId.toString().concat('_').concat(#kpiId)")
	@Override
	public Map<String, Object> fetchHappinessIndexData(ObjectId basicProjectConfigId, List<String> sprintList,
			String kpiId) {
		log.info("Fetching Happiness Index KPI Data for Project {} and KPI {}", basicProjectConfigId.toString(),
				kpiId);
		return kpiDataProvider.fetchHappinessIndexDataFromDb(sprintList);
	}

	@Cacheable(value = Constant.CACHE_PROJECT_KPI_DATA, key = "#basicProjectConfigId.toString().concat('_').concat(#kpiId)")
	@Override
	public Map<String, Object> fetchDRRData(KpiRequest kpiRequest, ObjectId basicProjectConfigId,
											List<String> sprintList, String kpiId) {
		log.info("Fetching DRR KPI Data for Project {} and KPI {}", basicProjectConfigId.toString(), kpiId);
		return kpiDataProvider.fetchDRRData(kpiRequest, basicProjectConfigId, sprintList);
	}

	@Cacheable(value = Constant.CACHE_PROJECT_KPI_DATA, key = "#basicProjectConfigId.toString().concat('_').concat(#kpiId)")
	@Override
	public Map<String, Object> fetchDSRData(KpiRequest kpiRequest, ObjectId basicProjectConfigId,
											List<String> sprintList, String kpiId) {
		log.info("Fetching DSR KPI Data for Project {} and KPI {}", basicProjectConfigId.toString(), kpiId);
		return kpiDataProvider.fetchDSRData(kpiRequest, basicProjectConfigId, sprintList);
	}
}
