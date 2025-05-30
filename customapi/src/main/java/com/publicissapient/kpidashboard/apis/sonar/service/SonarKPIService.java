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
import java.util.Optional;

import com.publicissapient.kpidashboard.apis.jira.service.SprintDetailsServiceImpl;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.ApplicationKPIService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
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
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.Tool;
import com.publicissapient.kpidashboard.common.model.sonar.SonarHistory;
import com.publicissapient.kpidashboard.common.repository.sonar.SonarHistoryRepository;

/**
 * @param <R>
 *          KPIs calculated value type
 * @param <S>
 *          Trend object type
 * @param <T>
 *          Bind DB data with type
 * @author prigupta8
 */
public abstract class SonarKPIService<R, S, T> extends ToolsKPIService<R, S> implements ApplicationKPIService<R, S, T> {

	public static final String WEEK_SEPERATOR = " to ";
	DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final String DATE_TIME_FORMAT_REGEX = "Z|\\.\\d+";

	@Autowired
	private CacheService cacheService;

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private SprintDetailsServiceImpl sprintDetailsService;

	@Autowired
	private SonarHistoryRepository sonarHistoryRepository;

	@Autowired
	private CustomApiConfig customApiConfig;

	public abstract String getQualifierType();

	/**
	 * Returns API Request tracker Id to be used for logging/debugging and using it
	 * for maintaining any sort of cache.
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
	 * fetching data greater than start date from sonar history table
	 *
	 * @param projectId
	 * @param currentDate
	 * @return
	 */
	private List<SonarHistory> getSonarHistoryBasedOnProject(ObjectId projectId, LocalDate currentDate) {
		List<SonarHistory> projectSonarList = new ArrayList<>();
		if (null != configHelperService.getToolItemMap() && null != configHelperService.getToolItemMap().get(projectId)) {
			List<Tool> sonarConfigListBasedOnProject = configHelperService.getToolItemMap().get(projectId)
					.get(Constant.TOOL_SONAR);

			Long timestamp = currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

			if (CollectionUtils.isNotEmpty(sonarConfigListBasedOnProject)) {
				sonarConfigListBasedOnProject.forEach(job -> {
					if (CollectionUtils.isNotEmpty(job.getProcessorItemList())) {
						List<ObjectId> processorItemList = new ArrayList<>();
						job.getProcessorItemList().forEach(processorItem -> processorItemList.add(processorItem.getId()));
						List<SonarHistory> sonarHistoryList = sonarHistoryRepository
								.findByProcessorItemIdInAndTimestampGreaterThan(processorItemList, timestamp);
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
	 * fetchng data from history table based on kanban/scrum
	 *
	 * @param projectList
	 * @param currentDate
	 * @return
	 */
	public Map<Pair<String, String>, List<SonarHistory>> getSonarHistoryForAllProjects(List<Node> projectList,
			LocalDate currentDate) {
		Map<Pair<String, String>, List<SonarHistory>> map = new HashMap<>();
		projectList.stream()
				.filter(node -> null != node.getProjectFilter() && null != node.getProjectFilter().getBasicProjectConfigId())
				.forEach(node -> map.put(Pair.of(node.getId(), node.getProjectFilter().getName()),
						getSonarHistoryBasedOnProject(node.getProjectFilter().getBasicProjectConfigId(), currentDate)));
		return map;
	}

	/**
	 * get start date to fetch from db for Kanban
	 *
	 * @param startDate
	 * @return
	 */
	public LocalDate getKanbanCurrentDateToFetchFromDb(String startDate) {
		return LocalDate.parse(startDate, dateTimeFormatter);
	}

	/**
	 * get start date to fetch from db for scrum
	 *
	 * @param duration
	 *          day/week/month
	 * @param value
	 *          value of how many days/week/months
	 * @return
	 */
	public LocalDate getScrumCurrentDateToFetchFromDb(String duration, Long value) {
		if (duration.equalsIgnoreCase(CommonConstant.WEEK))
			return LocalDate.now().minusWeeks(value + 1L);
		else if (duration.equalsIgnoreCase(CommonConstant.MONTH))
			return LocalDate.now().minusMonths(value + 1L);
		else if (duration.equalsIgnoreCase(CommonConstant.DAYS))
			return LocalDate.now().minusDays(value + 1L);
		return LocalDate.now();
	}

	/**
	 * Prepare sonar key name considering multiple project can have same sonar key
	 *
	 * @param projectName
	 *          projectName
	 * @param sonarKeyName
	 *          sonarKeyName
	 * @param branchName
	 *          sonar branch name
	 * @return modified keyname
	 */
	public String prepareSonarKeyName(String projectName, String sonarKeyName, String branchName) {
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
	 *          sonarHistoryList
	 * @param start
	 *          start
	 * @param end
	 *          end
	 * @return map
	 */
	public Map<String, SonarHistory> prepareJobwiseHistoryMap(List<SonarHistory> sonarHistoryList, Long start, Long end) {
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
	 *          currentDate
	 * @return array of localdate
	 */
	public LocalDate[] getWeeks(LocalDate currentDate) {
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

	/**
	 * create sonar kpis data count obj
	 *
	 * @param value
	 * @param hoverValues
	 * @param projectName
	 * @param date
	 * @return
	 */
	public DataCount getDataCountObject(Long value, Map<String, Object> hoverValues, String projectName, String date) {
		DataCount dataCount = new DataCount();
		dataCount.setData(String.valueOf(value));
		dataCount.setSProjectName(projectName);
		dataCount.setDate(date);
		dataCount.setValue(value);
		dataCount.setHoverValue(hoverValues);
		return dataCount;
	}

	/**
	 * Refines the code quality based on value
	 *
	 * @param value
	 * @return codeQuality in String or null if value is null
	 */
	public String refineQuality(Object value) {
		if (value != null) {
			if (1 == (Long) value) {
				return "A";
			} else if (2 == (Long) value) {
				return "B";
			} else if (3 == (Long) value) {
				return "C";
			} else if (4 == (Long) value) {
				return "D";
			} else if (5 == (Long) value) {
				return "E";
			} else {
				return "E";
			}
		}
		return null;
	}

	/**
	 * Fetches KPI data from the database for the given list of leaf nodes.
	 *
	 * @param leafNodeList
	 *            A list of leaf nodes for which KPI data needs to be fetched.
	 * @return A map where the key is the project ID and the value is a list of
	 *         objects (SprintDetails) associated with that project.
	 */
	protected Map<String, SprintDetails> getSprintDetailsByIds(List<Node> leafNodeList) {
		Map<String, SprintDetails> resultMap = new HashMap<>();
		if (CollectionUtils.isNotEmpty(leafNodeList)) {
			Map<String, String> processedProjectIds = new HashMap<>();
			leafNodeList.forEach(sprintNode -> {
				String projectId = sprintNode.getProjectFilter().getId();
				if (!processedProjectIds.containsKey(projectId)) {
					processedProjectIds.put(projectId, sprintNode.getId());
				}
			});
			List<SprintDetails> sprintDetailsList = sprintDetailsService
					.getSprintDetailsByIds(processedProjectIds.values().stream().toList());
			processedProjectIds.forEach((key, value) -> {
				Optional<SprintDetails> filtered = sprintDetailsList
						.stream()
						.filter(sprintDetails -> sprintDetails.getSprintID().equals(value))
						.findFirst();
				filtered.ifPresent(sprintDetails -> resultMap.put(key, sprintDetails));
			});
		}
		return resultMap;
	}

	/**
	 * Determines the end date for a sprint based on the provided SprintDetails
	 * object.
	 *
	 * @param sprintDetails
	 *            The SprintDetails object containing information about the sprint.
	 *            Can be null.
	 * @return The calculated end date as a LocalDate. If sprintDetails is null, it
	 *         returns the current date minus one week.
	 */
	protected LocalDate getEndDate(SprintDetails sprintDetails) {
		if (sprintDetails != null) {
			return sprintDetails.getCompleteDate() != null
					? DateUtil.stringToLocalDate(sprintDetails.getCompleteDate().replaceAll(DATE_TIME_FORMAT_REGEX, ""),
					DateUtil.TIME_FORMAT)
					: DateUtil.stringToLocalDate(sprintDetails.getEndDate().replaceAll(DATE_TIME_FORMAT_REGEX, ""),
					DateUtil.TIME_FORMAT);
		}
		return LocalDate.now().minusWeeks(1);
	}

}
