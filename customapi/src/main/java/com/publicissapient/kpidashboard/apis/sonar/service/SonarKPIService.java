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
import java.util.stream.Collectors;

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
import com.publicissapient.kpidashboard.apis.sonar.utiils.SonarQualityMetric;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.Tool;
import com.publicissapient.kpidashboard.common.model.sonar.SonarHistory;
import com.publicissapient.kpidashboard.common.model.sonar.SonarMetric;
import com.publicissapient.kpidashboard.common.repository.sonar.SonarDetailsRepository;
import com.publicissapient.kpidashboard.common.repository.sonar.SonarHistoryRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

/**
 * @param <R>
 *            KPIs calculated value type
 * @param <S>
 *            Trend object type
 * @param <T>
 *            Bind DB data with type
 * @author prigupta8
 */
public abstract class SonarKPIService<R, S, T> extends ToolsKPIService<R, S> implements ApplicationKPIService<R, S, T> {

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
		if (null != configHelperService.getToolItemMap()
				&& null != configHelperService.getToolItemMap().get(projectId)) {
			List<Tool> sonarConfigListBasedOnProject = configHelperService.getToolItemMap().get(projectId)
					.get(Constant.TOOL_SONAR);

			Long timestamp = currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

			if (CollectionUtils.isNotEmpty(sonarConfigListBasedOnProject)) {
				sonarConfigListBasedOnProject.forEach(job -> {
					if (CollectionUtils.isNotEmpty(job.getProcessorItemList())) {
						List<ObjectId> processorItemList = new ArrayList<>();
						job.getProcessorItemList()
								.forEach(processorItem -> processorItemList.add(processorItem.getId()));
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
	public Map<String, List<SonarHistory>> getSonarHistoryForAllProjects(List<Node> projectList,
			LocalDate currentDate) {
		Map<String, List<SonarHistory>> map = new HashMap<>();
		projectList.stream().filter(
				node -> null != node.getProjectFilter() && null != node.getProjectFilter().getBasicProjectConfigId())
				.forEach(node -> map.put(node.getId(),
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
	 *            day/week/month
	 * @param value
	 *            value of how many days/week/months
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
	 * @param projectNodeId
	 *            projectNodeId
	 * @param sonarKeyName
	 *            sonarKeyName
	 * @param branchName
	 *            sonar branch name
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
	 * Prepares a map pairing each job key from the given SonarHistory list with a
	 * Pair of SonarHistory, representing the earliest and latest records within the
	 * specified time range.
	 *
	 * @param sonarHistoryList
	 *            List of SonarHistory.
	 * @param start
	 *            Start timestamp of the time range (exclusive).
	 * @param end
	 *            End timestamp of the time range (exclusive).
	 * @return Map<String, Pair<SonarHistory, SonarHistory>>
	 */
	public Map<String, Pair<SonarHistory, SonarHistory>> prepareJobWiseHistoryMapPair(
			List<SonarHistory> sonarHistoryList, Long start, Long end) {

		Map<String, Pair<SonarHistory, SonarHistory>> map = new HashMap<>();

		for (SonarHistory sonarHistory : sonarHistoryList) {
			if (sonarHistory.getTimestamp().compareTo(start) > 0 && sonarHistory.getTimestamp().compareTo(end) < 0) {

				map.merge(sonarHistory.getKey(), Pair.of(sonarHistory, sonarHistory), (existingPair, newPair) -> {
					// Update the left if it is null or has a greater timestamp.
					if (existingPair.getLeft() == null
							|| existingPair.getLeft().getTimestamp().compareTo(newPair.getLeft().getTimestamp()) > 0) {
						existingPair = Pair.of(newPair.getLeft(), existingPair.getRight());
					}
					// Update the right if it is null or has a lesser timestamp.
					if (existingPair.getRight() == null || existingPair.getRight().getTimestamp()
							.compareTo(newPair.getRight().getTimestamp()) < 0) {
						existingPair = Pair.of(existingPair.getLeft(), newPair.getRight());
					}
					return existingPair;
				});
			}
		}

		return map;
	}

	/**
	 * prepare dummy data for empty responses
	 *
	 * @param sonarHistoryList
	 *            sonarHistoryList
	 * @param end
	 *            endDate
	 * @return map
	 */
	public Map<String, Pair<SonarHistory, SonarHistory>> prepareEmptyJobWiseHistoryMapPair(
			List<SonarHistory> sonarHistoryList, List<String> metricNames, Long end) {
		Map<String, Pair<SonarHistory, SonarHistory>> historyMap = new HashMap<>();
		if (CollectionUtils.isNotEmpty(sonarHistoryList)) {
			SonarHistory refHistory = sonarHistoryList.get(0);

			List<SonarMetric> metricsList = new ArrayList<>(
					metricNames.stream().map(metric -> new SonarMetric(metric, 0)).toList());

			List<String> uniqueKeys = sonarHistoryList.stream().map(SonarHistory::getKey).distinct().toList();
			uniqueKeys.forEach(keys -> {
				SonarHistory sonarHistory = SonarHistory.builder().processorItemId(refHistory.getProcessorItemId())
						.date(end).timestamp(end).key(keys).name(keys).branch(refHistory.getBranch())
						.metrics(metricsList).build();
				historyMap.put(keys, Pair.of(sonarHistory, sonarHistory));
			});
		}

		return historyMap;
	}

	/**
	 * Used to calculate the Quality Metric
	 * 
	 * @param sonarDetailPair
	 *            Pair of sonar history
	 * @param fieldMapping
	 *            fieldMapping for costPerLine
	 * @param projOverallMetric
	 *            projOverallMetric
	 * @param numKeyName
	 *            key name of num
	 * @param demKeyName
	 *            key name of den
	 * @return Quality Metrics
	 *
	 * @link <a href=
	 *       "https://publicissapient.atlassian.net/wiki/spaces/SPDS/pages/79822877/KnowHOW+TEngine+KPIs+on+KnowHOW">Refer
	 *       Docs.</a>
	 */
	public double calculateQualityMetric(Pair<SonarHistory, SonarHistory> sonarDetailPair, FieldMapping fieldMapping,
										 SonarQualityMetric projOverallMetric, String numKeyName, String demKeyName) {
		Map<String, Object> leftMetricMap = extractMetricMap(sonarDetailPair.getLeft());
		Map<String, Object> rightMetricMap = extractMetricMap(sonarDetailPair.getRight());

		int costPerLine = Optional.ofNullable(fieldMapping.getCostPerLineKPI174()).orElse(30);

		final long numLeft = getParsedValue(leftMetricMap.get(numKeyName));
		final long demLeft = getParsedValue(leftMetricMap.get(demKeyName));

		double ratioLeft = safeDivisionDouble(numLeft, (demLeft * costPerLine));

		final long numRight = getParsedValue(rightMetricMap.get(numKeyName));
		final long demRight = getParsedValue(rightMetricMap.get(demKeyName));

		double ratioRight = safeDivisionDouble(numRight, (demRight * costPerLine));

		final double qualityMetrics = roundingOff(safeDivisionDouble(ratioRight, ratioLeft) * 100);

		projOverallMetric.addQualityMetrics(numLeft, demLeft, numRight, demRight, costPerLine);

		return qualityMetrics;
	}

	/**
	 * Create the x-axis Date for sonar kpi's
	 * 
	 * @param monthStartDate
	 *            monthStartDate
	 * @param monthEndDate
	 *            monthEndDate
	 * @return Formatted Date string.
	 */
	public static String getFormattedDate(LocalDate monthStartDate, LocalDate monthEndDate) {
		return DateUtil.dateTimeConverter(monthStartDate.toString(), DateUtil.DATE_FORMAT, DateUtil.DISPLAY_DATE_FORMAT)
				+ " to " + DateUtil.dateTimeConverter(monthEndDate.toString(), DateUtil.DATE_FORMAT,
						DateUtil.DISPLAY_DATE_FORMAT);
	}

	/**
	 * Generate the metric map with Map<metricName,MetricValue>
	 * 
	 * @param sonarHistory
	 *            sonarHistory
	 * @return Map<metricName,MetricValue>
	 */
	private Map<String, Object> extractMetricMap(SonarHistory sonarHistory) {
		return sonarHistory.getMetrics().stream().filter(metric -> metric.getMetricValue() != null)
				.collect(Collectors.toMap(SonarMetric::getMetricName, SonarMetric::getMetricValue));
	}

	/**
	 * Used to parse the Map value w.r.t instance
	 * 
	 * @param valueToParse
	 *            valueToParse
	 * @return Value
	 */
	public Long getParsedValue(Object valueToParse) {
		long value = 0L;
		if (valueToParse != null) {
			if (valueToParse instanceof Double) {
				value = ((Double) valueToParse).longValue();
			} else if (valueToParse instanceof String) {
				value = Double.valueOf(valueToParse.toString()).longValue();
			} else if (valueToParse instanceof Integer) {
				value = ((Integer) valueToParse).longValue();
			} else {
				value = (Long) valueToParse;
			}
		}
		return value;
	}

	/**
	 * create sonar kpis data count obj
	 *
	 * @param value
	 *            value
	 * @param projectName
	 *            projectName
	 * @param date
	 *            date
	 * @return dataCount
	 */
	public DataCount getDataCount(Double value, String projectName, String date) {
		DataCount dataCount = new DataCount();
		dataCount.setData(String.valueOf(value));
		dataCount.setSProjectName(projectName);
		dataCount.setDate(date);
		dataCount.setValue(value);
		dataCount.setHoverValue(new HashMap<>());
		return dataCount;
	}

	/**
	 * Used for division
	 * 
	 * @param numerator
	 *            numerator
	 * @param denominator
	 *            denominator
	 * @return 0d if dem is zero, else num/den
	 */
	public double safeDivisionDouble(double numerator, double denominator) {
		return denominator != 0.0 ? numerator / denominator : 0.0;
	}

	/**
	 * to maintain values up to 2 places of decimal
	 * 
	 * @param value
	 *            value
	 * @return value up to 2 decimal
	 */
	public double roundingOff(double value) {
		return (double) Math.round(value * 100) / 100;
	}

}
