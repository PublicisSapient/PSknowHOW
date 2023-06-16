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

package com.publicissapient.kpidashboard.apis.sonar.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.ApplicationKPIService;
import com.publicissapient.kpidashboard.apis.common.service.ToolsKPIService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.Tool;
import com.publicissapient.kpidashboard.common.model.sonar.SonarDetails;
import com.publicissapient.kpidashboard.common.model.sonar.SonarHistory;
import com.publicissapient.kpidashboard.common.repository.sonar.SonarDetailsRepository;
import com.publicissapient.kpidashboard.common.repository.sonar.SonarHistoryRepository;

/**
 * @param <R>
 *            KPIs calculated value type
 * @param <S>
 *            Trend object type
 * @param <T>
 *            Bind DB data with type
 * @author prigupta8
 */
public abstract class SonarKPIService<R, S, T> extends ToolsKPIService<R,S> implements ApplicationKPIService<R, S, T> {

	public static final String WEEK_SEPERATOR = " to ";
	DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	@Autowired
	private CacheService cacheService;

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private SonarDetailsRepository sonarDetailsRepository;

	@Autowired
	private SonarHistoryRepository sonarHistoryRepository;

	@Autowired
	private CustomApiConfig customApiConfig;

	public abstract String getQualifierType();

	/**
	 * Returns API Request tracker Id to be used for logging/debugging and using
	 * it for maintaining any sort of cache.
	 *
	 * @return
	 */
	protected String getRequestTrackerId() {
		return cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.SONAR.name());
	}

	protected String getRequestTrackerIdKanban() {
		return cacheService.getFromApplicationCache(Constant.KPI_REQUEST_TRACKER_ID_KEY + KPISource.SONARKANBAN.name());
	}

	/**
	 * Calculates sonar KPI Data
	 *
	 * @param kpiRequest
	 * @param kpiElement
	 * @param treeAggregatorDetail
	 * @return
	 * @throws ApplicationException
	 */
	public abstract KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
										  TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException;

	/**
	 * @param projectList
	 * @param tempMap
	 * @param kpiElement
	 * @return
	 */
	public abstract Map<String, Object> getSonarJobWiseKpiData(List<Node> projectList, Map<String, Node> tempMap,
															   KpiElement kpiElement);

	/**
	 * Returns the list of sonar project details related to a project ID in the
	 * DB
	 *
	 * @param projectId
	 * @return
	 */
	private List<SonarDetails> getSonarDetailsBasedOnProject(ObjectId projectId) {
		List<SonarDetails> projectSonarList = new ArrayList<>();
		if (null != configHelperService.getToolItemMap()
				&& null != configHelperService.getToolItemMap().get(projectId)) {
			List<Tool> sonarConfigListBasedOnProject = configHelperService.getToolItemMap().get(projectId)
					.get(Constant.TOOL_SONAR);
			if (CollectionUtils.isNotEmpty(sonarConfigListBasedOnProject)) {
				sonarConfigListBasedOnProject.forEach(job -> {
					if (CollectionUtils.isNotEmpty(job.getProcessorItemList())) {
						List<ObjectId> processorItemList = new ArrayList<>();
						job.getProcessorItemList()
								.forEach(processorItem -> processorItemList.add(processorItem.getId()));
						List<SonarDetails> sonarDetailsList = sonarDetailsRepository
								.findByProcessorItemIdIn(processorItemList);
						if (CollectionUtils.isNotEmpty(sonarDetailsList)) {
							sonarDetailsList.stream().forEach(projectSonarList::add);
						}

					}

				});
			}
		}
		return projectSonarList;
	}

	/**
	 * fetching data greater than start date from sonar history table
	 * @param projectId
	 * @param startDate
	 * @param kanban
	 * @return
	 */
	private List<SonarHistory> getSonarHistoryBasedOnProject(ObjectId projectId, String startDate, boolean kanban) {
		List<SonarHistory> projectSonarList = new ArrayList<>();
		if (null != configHelperService.getToolItemMap()
				&& null != configHelperService.getToolItemMap().get(projectId)) {
			List<Tool> sonarConfigListBasedOnProject = configHelperService.getToolItemMap().get(projectId)
					.get(Constant.TOOL_SONAR);

			LocalDate currentDate = LocalDate.now().minusWeeks(customApiConfig.getSonarWeekCount() + 1L);
			if (kanban) {
				currentDate = LocalDate.parse(startDate, dateTimeFormatter);
			}
			Long timestamp = currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

			if (CollectionUtils.isNotEmpty(sonarConfigListBasedOnProject)) {
				sonarConfigListBasedOnProject.forEach(job -> {
					if (CollectionUtils.isNotEmpty(job.getProcessorItemList())) {
						List<ObjectId> processorItemList = new ArrayList<>();
						job.getProcessorItemList()
								.forEach(processorItem -> processorItemList.add(processorItem.getId()));
						List<SonarHistory> sonarHistoryList = sonarHistoryRepository
								.findByProcessorItemIdInAndTimestampGreaterThan(processorItemList,timestamp);
						if (CollectionUtils.isNotEmpty(sonarHistoryList)) {
							projectSonarList.addAll(sonarHistoryList);
						}
					}
				});
			}
		}
		return projectSonarList;
	}

	/**
	 * Get SonarDetails list for all Projects from the projectList configuration
	 *
	 * @param projectList
	 * @return
	 */
	public Map<String, List<SonarDetails>> getSonarDetailsForAllProjects(List<Node> projectList) {
		Map<String, List<SonarDetails>> map = new HashMap<>();
		projectList.stream().filter(
				node -> null != node.getProjectFilter() && null != node.getProjectFilter().getBasicProjectConfigId())
				.forEach(node -> map.put(node.getId(),
						getSonarDetailsBasedOnProject(node.getProjectFilter().getBasicProjectConfigId())));
		return map;
	}

	/**
	 * fetchng data from history table based on kanban/scrum
	 * @param projectList
	 * @param startDate
	 * @param isKanban
	 * @return
	 */
	public Map<String, List<SonarHistory>> getSonarHistoryForAllProjects(List<Node> projectList, String startDate,
			boolean isKanban) {
		Map<String, List<SonarHistory>> map = new HashMap<>();
		if (isKanban) {
			projectList.stream()
					.filter(node -> null != node.getProjectFilter()
							&& null != node.getProjectFilter().getBasicProjectConfigId())
					.forEach(node -> map.put(node.getId(), getSonarHistoryBasedOnProject(
							node.getProjectFilter().getBasicProjectConfigId(), startDate, true)));
		} else {
			projectList.stream()
					.filter(node -> null != node.getProjectFilter()
							&& null != node.getProjectFilter().getBasicProjectConfigId())
					.forEach(node -> map.put(node.getId(), getSonarHistoryBasedOnProject(
							node.getProjectFilter().getBasicProjectConfigId(), startDate, false)));
		}
		return map;

	}

	/**
	 * Prepare sonar key name considering multiple project can have same sonar
	 * key
	 *
	 * @param projectNodeId
	 *            projectNodeId
	 * @param sonarKeyName
	 *            sonarKeyName
	 * @param branchName
	 * 			 sonar branch name
	 * @return modified keyname
	 */
	public String prepareSonarKeyName(String projectNodeId, String sonarKeyName, String branchName) {
		String projectName = projectNodeId.substring(0, projectNodeId.lastIndexOf(CommonConstant.UNDERSCORE));
		String sonarKey = sonarKeyName + CommonConstant.ARROW + projectName;
		if (StringUtils.isNotEmpty(branchName)) {
			sonarKey = sonarKeyName + CommonConstant.ARROW + branchName + CommonConstant.ARROW + projectName;
		}
		return sonarKey;
	}

	/**
	 * this method return jobwise latest data of week
	 *
	 * @param sonarHistoryList
	 *            sonarHistoryList
	 * @param start
	 *            start
	 * @param end
	 *            end
	 * @return map
	 */
	public Map<String, SonarHistory> prepareJobwiseHistoryMap(List<SonarHistory> sonarHistoryList, Long start,
															  Long end) {
		Map<String, SonarHistory> map = new HashMap<>();
		for (SonarHistory sonarHistory : sonarHistoryList) {
			if (sonarHistory.getTimestamp().compareTo(start) > 0 && sonarHistory.getTimestamp().compareTo(end) < 0) {
				map.putIfAbsent(sonarHistory.getKey(), sonarHistory);
				if (sonarHistory.getTimestamp().compareTo(map.get(sonarHistory.getKey()).getTimestamp()) > 0) {
					map.put(sonarHistory.getKey(), sonarHistory);
				}
			}
		}
		return map;
	}

	/**
	 * return start and end of week
	 *
	 * @param currentDate
	 *            currentDate
	 * @return array of localdate
	 */
	public LocalDate[] getWeeks(LocalDate currentDate){
		LocalDate[] week = new LocalDate[2];
		LocalDate monday = currentDate;
		while (monday.getDayOfWeek() != DayOfWeek.MONDAY) {
			monday = monday.minusDays(1);
		}
		week[0] = monday;
		LocalDate sunday = currentDate;
		while (sunday.getDayOfWeek() != DayOfWeek.SUNDAY) {
			sunday = sunday.plusDays(1);
		}
		week[1] = sunday;
		return week;
	}
}
