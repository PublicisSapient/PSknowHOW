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

package com.publicissapient.kpidashboard.apis.maturity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.errors.EntityNotFoundException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraServiceR;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.sonar.service.SonarServiceR;
import com.publicissapient.kpidashboard.apis.zephyr.service.ZephyrService;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.KpiMaster;
import com.publicissapient.kpidashboard.common.repository.application.KpiMasterRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * @author kunkambl
 */
@Slf4j
@Service
public class MaturityServiceImpl {

	private static final List<String> FILTER_LIST = Arrays.asList("Final Scope (Story Points)", "Average Coverage",
			"Story Points", "Overall");
	private static final String KPI_SOURCE_JIRA = "Jira";
	private static final String KPI_SOURCE_SONAR = "Sonar";
	private static final String KPI_SOURCE_ZEPHYR = "Zypher";
	private static final String HIERARCHY_LABEL_PORT = "port";
	private static final int HIERARCHY_LEVEL_PORT = 4;
	private static final String SPRINT_CLOSED = "CLOSED";


	@Autowired
	KpiMasterRepository kpiMasterRepository;

	@Autowired
	private JiraServiceR jiraService;

	@Autowired
	private SonarServiceR sonarService;

	@Autowired
	private ZephyrService zephyrService;


	/**
	 * get kpi element list with maturity
	 * assuming req for hierarchy level 4
	 * @param kpiRequest
 * 				kpiRequest to fetch kpi data
	 * @return list of KpiElement
	 */
	public List<KpiElement> getMaturityValues(KpiRequest kpiRequest) {
		List<KpiMaster> kpiMasterList = kpiMasterRepository.findByKpiIdIn(kpiRequest.getKpiIdList());
		Map<String, List<KpiMaster>> sourceWiseKpiList = kpiMasterList.stream()
				.collect(Collectors.groupingBy(KpiMaster::getKpiSource));

		List<KpiElement> kpiElements = new ArrayList<>();
		// it is assumed that request is for heirarchy level 4 i.e. port
		String[] hierarchyIdList = {
				kpiRequest.getHierarchyName().concat(Constant.UNDERSCORE).concat(HIERARCHY_LABEL_PORT) };
		Map<String, List<String>> selectedMap = new HashMap<>();
		selectedMap.put(HIERARCHY_LABEL_PORT, Arrays.stream(hierarchyIdList).collect(Collectors.toList()));
		kpiRequest.setIds(hierarchyIdList);
		kpiRequest.setLevel(HIERARCHY_LEVEL_PORT);
		kpiRequest.setLabel(HIERARCHY_LABEL_PORT);
		kpiRequest.setSelectedMap(selectedMap);
		kpiRequest.setSprintIncluded(Arrays.asList(SPRINT_CLOSED));
		sourceWiseKpiList.forEach((source, kpiList) -> {
			try {
				kpiRequest.setKpiList(sourceWiseKpiList.get(source).stream().map(this::mapKpiMasterToKpiElement)
						.collect(Collectors.toList()));
				switch (source) {
				case KPI_SOURCE_JIRA:
					kpiElements.addAll(getJiraKpiMaturity(kpiRequest));
					break;
				case KPI_SOURCE_SONAR:
					kpiElements.addAll(getSonarKpiMaturity(kpiRequest));
					break;
				case KPI_SOURCE_ZEPHYR:
					kpiElements.addAll(getZephyrKpiMaturity(kpiRequest));
					break;
				default:
					log.error("Invalid Kpi");
				}
			} catch (Exception ex) {
				log.error("Error while fetching kpi maturity data", ex);
			}
		});
		kpiElements.forEach(kpiElement -> {
			List<?> trendValueList = (List<?>) kpiElement.getTrendValueList();
			if (CollectionUtils.isNotEmpty(trendValueList)) {
				if (trendValueList.get(0) instanceof DataCountGroup) {
					List<DataCountGroup> dataCountGroups = (List<DataCountGroup>) trendValueList;

					Optional<DataCount> firstMatchingDataCount = dataCountGroups.stream().filter(trend -> FILTER_LIST
							.contains(trend.getFilter())
							|| (FILTER_LIST.contains(trend.getFilter1()) && FILTER_LIST.contains(trend.getFilter2())))
							.map(DataCountGroup::getValue).flatMap(List::stream).findFirst();

					firstMatchingDataCount.ifPresent(dataCount -> {
						kpiElement.setMaturityValue((String) dataCount.getMaturityValue());
						kpiElement.setMaturity(dataCount.getMaturity());
					});
				} else {
					List<DataCount> dataCounts = (List<DataCount>) trendValueList;
					DataCount firstDataCount = dataCounts.get(0);

					kpiElement.setMaturityValue((String) firstDataCount.getMaturityValue());
					kpiElement.setMaturity(firstDataCount.getMaturity());
				}
			}
		});

		return kpiElements;
	}

	/**
	 * get kpi data for source jira
	 * @param kpiRequest
	 * 			kpiRequest to fetch kpi data
	 * @return list of jira KpiElement
	 * @throws EntityNotFoundException
	 * 			entity not found exception for jira service method
	 */
	private List<KpiElement> getJiraKpiMaturity(KpiRequest kpiRequest)
			throws EntityNotFoundException {
		MDC.put("JiraScrumKpiRequest", kpiRequest.getRequestTrackerId());
		log.info("Received Jira KPI request {}", kpiRequest);
		long jiraRequestStartTime = System.currentTimeMillis();
		MDC.put("JiraRequestStartTime", String.valueOf(jiraRequestStartTime));
		List<KpiElement> responseList = jiraService.process(kpiRequest);
		MDC.put("TotalJiraRequestTime", String.valueOf(System.currentTimeMillis() - jiraRequestStartTime));
		MDC.clear();
		return responseList;
	}

	/**
	 * get kpi data for source sonar
	 * @param kpiRequest
	 * 			kpiRequest to fetch kpi data
	 * @return list of sonar KpiElement
	 */
	private List<KpiElement> getSonarKpiMaturity(KpiRequest kpiRequest) {
		MDC.put("SonarKpiRequest", kpiRequest.getRequestTrackerId());
		log.info("Received Sonar KPI request {}", kpiRequest);
		long sonarRequestStartTime = System.currentTimeMillis();
		MDC.put("SonarRequestStartTime", String.valueOf(sonarRequestStartTime));
		List<KpiElement> responseList = sonarService.process(kpiRequest);
		MDC.put("TotalSonarRequestTime", String.valueOf(System.currentTimeMillis() - sonarRequestStartTime));
		MDC.clear();
		return responseList;
	}

	/**
	 * get kpi data for source sonar
	 * @param kpiRequest
	 * 			kpiRequest to fetch kpi data
	 * @return list of sonar KpiElement
	 * @throws EntityNotFoundException
	 * 			entity not found exception for zephyr service method
	 */
	private List<KpiElement> getZephyrKpiMaturity(KpiRequest kpiRequest)
			throws EntityNotFoundException {
		MDC.put("ZephyrKpiRequest", kpiRequest.getRequestTrackerId());
		log.info("Received Zephyr KPI request {}", kpiRequest);
		long zypherRequestStartTime = System.currentTimeMillis();
		MDC.put("ZephyrRequestStartTime", String.valueOf(zypherRequestStartTime));
		List<KpiElement> responseList = zephyrService.process(kpiRequest);
		MDC.put("TotalZephyrRequestTime", String.valueOf(System.currentTimeMillis() - zypherRequestStartTime));
		MDC.clear();
		return responseList;
	}

	/**
	 * Map KpiMaster object to KpiElement
	 * @param kpiMaster
	 * 			KpiMaster object fetched from db
	 * @return KpiElement
	 */
	public KpiElement mapKpiMasterToKpiElement(KpiMaster kpiMaster) {
		KpiElement kpiElement = new KpiElement();
		kpiElement.setKpiId(kpiMaster.getKpiId());
		kpiElement.setKpiName(kpiMaster.getKpiName());
		kpiElement.setIsDeleted(kpiMaster.getIsDeleted());
		kpiElement.setKpiCategory(kpiMaster.getKpiCategory());
		kpiElement.setKpiInAggregatedFeed(kpiMaster.getKpiInAggregatedFeed());
		kpiElement.setKpiOnDashboard(kpiMaster.getKpiOnDashboard());
		kpiElement.setKpiBaseLine(kpiMaster.getKpiBaseLine());
		kpiElement.setKpiUnit(kpiMaster.getKpiUnit());
		kpiElement.setIsTrendUpOnValIncrease(kpiMaster.getIsTrendUpOnValIncrease());
		kpiElement.setKanban(kpiMaster.getKanban());
		kpiElement.setKpiSource(kpiMaster.getKpiSource());
		kpiElement.setThresholdValue(kpiMaster.getThresholdValue());
		kpiElement.setAggregationType(kpiMaster.getAggregationCriteria());
		kpiElement.setMaturityRange(kpiMaster.getMaturityRange());
		kpiElement.setGroupId(kpiMaster.getGroupId());
		return kpiElement;
	}
}
