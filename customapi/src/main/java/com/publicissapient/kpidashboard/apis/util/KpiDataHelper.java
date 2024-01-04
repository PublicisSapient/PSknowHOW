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

package com.publicissapient.kpidashboard.apis.util;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Duration;
import org.joda.time.Hours;

import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.model.CustomDateRange;
import com.publicissapient.kpidashboard.apis.model.IterationKpiModalValue;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterCategory;
import com.publicissapient.kpidashboard.common.model.application.CycleTimeValidationData;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.excel.KanbanCapacity;
import com.publicissapient.kpidashboard.common.model.jira.IterationPotentialDelay;
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintWiseStory;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * The class contains methods for helping kpi to prepare data
 *
 * @author anisingh4
 */
@Slf4j
public final class KpiDataHelper {
	private static final String CLOSED = "closed";
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
	private static final DecimalFormat df = new DecimalFormat(".##");

	private KpiDataHelper() {
	}

	/**
	 * Creates condition map for additional filters
	 *
	 * @param kpiRequest
	 * @param mapOfFilters
	 * @param methodology
	 * @param individualDevOrQa
	 * @return sub group category as String
	 */
	public static String createAdditionalFilterMap(KpiRequest kpiRequest, Map<String, List<String>> mapOfFilters,
			String methodology, String individualDevOrQa, FilterHelperService flterHelperService) {
		String subGroupCategory = Constant.SPRINT;
		if (methodology.equals(Constant.KANBAN)) {
			subGroupCategory = Constant.DATE;
		}
		Map<String, AdditionalFilterCategory> addFilterCat = flterHelperService.getAdditionalFilterHierarchyLevel();
		Map<String, AdditionalFilterCategory> addFilterCategory = addFilterCat.entrySet().stream()
				.collect(Collectors.toMap(entry -> entry.getKey().toUpperCase(), Map.Entry::getValue));

		if (MapUtils.isNotEmpty(kpiRequest.getSelectedMap())) {
			for (Map.Entry<String, List<String>> entry : kpiRequest.getSelectedMap().entrySet()) {
				if (CollectionUtils.isNotEmpty(entry.getValue())
						&& null != addFilterCategory.get(entry.getKey().toUpperCase())) {
					mapOfFilters.put(JiraFeature.ADDITIONAL_FILTERS_FILTERID.getFieldValueInFeature(),
							Arrays.asList(entry.getKey()));
					mapOfFilters.put(JiraFeature.ADDITIONAL_FILTERS_FILTERVALUES_VALUEID.getFieldValueInFeature(),
							entry.getValue());
					subGroupCategory = entry.getKey();
				}
			}
		}
		return subGroupCategory;
	}

	/**
	 * Creates subcategory wise map.
	 *
	 * @param subGroupCategory
	 * @param sprintWiseStoryList
	 * @return {@code Map<String , Map <String , List <String>>>} Map of sprint and
	 *         subcategory wise list of featureId
	 */
	public static Map<Pair<String, String>, Map<String, List<String>>> createSubCategoryWiseMap(String subGroupCategory,
			List<SprintWiseStory> sprintWiseStoryList, String filterToShowOnTrend) {

		Map<Pair<String, String>, Map<String, List<String>>> sprintWiseStoryMap = new HashMap<>();

		Map<Pair<String, String>, List<SprintWiseStory>> sprintAndFilterDataMap = sprintWiseStoryList.stream().collect(
				Collectors.groupingBy(sws -> Pair.of(sws.getProjectID(), sws.getSprint()), Collectors.toList()));

		sprintAndFilterDataMap.entrySet().forEach(data -> {
			Map<String, List<String>> subCategoryDataMap = new HashMap<>();
			if (Constant.SPRINT.equals(subGroupCategory)) {
				subCategoryDataMap = data.getValue().stream()
						.collect(Collectors.toMap(SprintWiseStory::getSprint, SprintWiseStory::getStoryList));
			}
			sprintWiseStoryMap.put(data.getKey(), subCategoryDataMap);
		});

		return sprintWiseStoryMap;
	}

	/**
	 * Creates date wise map for kanban
	 *
	 * @param ticketList
	 * @param subGroupCategory
	 * @param flterHelperService
	 * @return
	 */
	public static Map<String, List<KanbanJiraIssue>> createProjectWiseMapKanban(List<KanbanJiraIssue> ticketList,
			String subGroupCategory, FilterHelperService flterHelperService) {
		Map<String, List<KanbanJiraIssue>> projectAndDateWiseTicketMap = new HashMap<>();
		Map<String, AdditionalFilterCategory> addFilterCat = flterHelperService.getAdditionalFilterHierarchyLevel();
		List<String> addFilterCategoryList = new ArrayList<>(addFilterCat.keySet());
		if (Constant.DATE.equals(subGroupCategory) || addFilterCategoryList.contains(subGroupCategory)) {
			projectAndDateWiseTicketMap = ticketList.stream()
					.collect(Collectors.groupingBy(KanbanJiraIssue::getBasicProjectConfigId));
		}

		return projectAndDateWiseTicketMap;
	}

	/**
	 * Creates date wise map for kanban history
	 *
	 * @param ticketList
	 * @param subGroupCategory
	 * @param flterHelperService
	 * @return
	 */
	public static Map<String, List<KanbanIssueCustomHistory>> createProjectWiseMapKanbanHistory(
			List<KanbanIssueCustomHistory> ticketList, String subGroupCategory,
			FilterHelperService flterHelperService) {
		Map<String, List<KanbanIssueCustomHistory>> projectAndDateWiseTicketMap = new HashMap<>();
		Map<String, AdditionalFilterCategory> addFilterCat = flterHelperService.getAdditionalFilterHierarchyLevel();
		List<String> addFilterCategoryList = new ArrayList<>(addFilterCat.keySet());
		if (Constant.DATE.equals(subGroupCategory) || addFilterCategoryList.contains(subGroupCategory)) {
			projectAndDateWiseTicketMap = ticketList.stream()
					.collect(Collectors.groupingBy(KanbanIssueCustomHistory::getBasicProjectConfigId));
		}

		return projectAndDateWiseTicketMap;
	}

	/**
	 * Creates date wise category map
	 *
	 * @param ticketList
	 * @param subGroupCategory
	 * @param flterHelperService
	 */
	public static Map<String, Map<String, List<KanbanCapacity>>> createDateWiseCapacityMap(
			List<KanbanCapacity> ticketList, String subGroupCategory, FilterHelperService flterHelperService) {
		Map<String, AdditionalFilterCategory> addFilterCat = flterHelperService.getAdditionalFilterHierarchyLevel();
		List<String> addFilterCategoryList = new ArrayList<>(addFilterCat.keySet());
		Map<String, Map<String, List<KanbanCapacity>>> projectAndDateWiseCapacityMap = new HashMap<>();
		if (Constant.DATE.equals(subGroupCategory) || addFilterCategoryList.contains(subGroupCategory)) {
			Map<String, List<KanbanCapacity>> projectWiseCapacityMap = ticketList.stream().collect(
					Collectors.groupingBy(ticket -> ticket.getBasicProjectConfigId().toString(), Collectors.toList()));
			projectWiseCapacityMap.forEach((project, capacityList) -> {
				Map<String, List<KanbanCapacity>> dateWiseCapacityMap = new HashMap<>();
				capacityList.forEach(kanbanCapacity -> {
					for (LocalDate date = kanbanCapacity.getStartDate(); (date.isBefore(kanbanCapacity.getEndDate())
							|| date.isEqual(kanbanCapacity.getEndDate()))
							&& !(date.getDayOfWeek().equals(DayOfWeek.SUNDAY)
									|| date.getDayOfWeek().equals(DayOfWeek.SATURDAY)); date = date.plusDays(1)) {
						String formattedDate = DateUtil.localDateTimeConverter(date);
						dateWiseCapacityMap.putIfAbsent(formattedDate, new ArrayList<>());
						dateWiseCapacityMap.get(formattedDate).add(kanbanCapacity);
					}
				});
				projectAndDateWiseCapacityMap.put(project, dateWiseCapacityMap);
			});
		}
		return projectAndDateWiseCapacityMap;
	}

	public static LocalDate convertStringToDate(String dateString) {
		return LocalDate.parse(dateString.split("T")[0]);
	}

	public static CustomDateRange getStartAndEndDate(KpiRequest kpiRequest) {
		int dataPoint = (int) ObjectUtils.defaultIfNull(kpiRequest.getKanbanXaxisDataPoints(), 7) + 1;
		CustomDateRange cdr = new CustomDateRange();
		cdr.setEndDate(LocalDate.now());
		LocalDate startDate = null;
		if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.WEEK)) {
			startDate = LocalDate.now().minusWeeks(dataPoint);
		} else if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.MONTH)) {
			startDate = LocalDate.now().minusMonths(dataPoint);
		} else {
			startDate = LocalDate.now().minusDays(dataPoint);
		}
		cdr.setStartDate(startDate);
		return cdr;
	}

	public static CustomDateRange getStartAndEndDateForDataFiltering(LocalDate date, String period) {
		CustomDateRange dateRange = new CustomDateRange();
		LocalDate startDate = null;
		LocalDate endDate = null;
		if (period.equalsIgnoreCase(CommonConstant.WEEK)) {
			LocalDate monday = date;
			while (monday.getDayOfWeek() != DayOfWeek.MONDAY) {
				monday = monday.minusDays(1);
			}
			startDate = monday;
			LocalDate sunday = date;
			while (sunday.getDayOfWeek() != DayOfWeek.SUNDAY) {
				sunday = sunday.plusDays(1);
			}
			endDate = sunday;
		} else if (period.equalsIgnoreCase(CommonConstant.MONTH)) {
			YearMonth month = YearMonth.from(date);
			startDate = month.atDay(1);
			endDate = month.atEndOfMonth();
		} else {
			startDate = date;
			endDate = date;
		}
		dateRange.setStartDate(startDate);
		dateRange.setEndDate(endDate);
		return dateRange;
	}

	/**
	 * CustomDateRange calculation for Cumulative data and start date is always
	 * monday for week and or 1st day of month for months calculation.
	 *
	 * @param kpiRequest
	 * @return CustomDateRange
	 */
	public static CustomDateRange getStartAndEndDatesForCumulative(KpiRequest kpiRequest) {
		int dataPoint = (int) ObjectUtils.defaultIfNull(kpiRequest.getKanbanXaxisDataPoints(), 7) - 1;
		CustomDateRange cdr = new CustomDateRange();
		LocalDate startDate = null;
		if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.WEEK)) {
			startDate = LocalDate.now().minusWeeks(dataPoint);
			LocalDate monday = startDate;
			while (monday.getDayOfWeek() != DayOfWeek.MONDAY) {
				monday = monday.minusDays(1);
			}
			startDate = monday;
		} else if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.MONTH)) {
			startDate = LocalDate.now().minusMonths(dataPoint);
			YearMonth month = YearMonth.from(startDate);
			startDate = month.atDay(1);
		} else {
			startDate = LocalDate.now().minusDays(dataPoint);
		}
		cdr.setStartDate(startDate);
		cdr.setEndDate(LocalDate.now());
		return cdr;
	}

	/**
	 * months calculation for how many months past data is measure Data for
	 * Cumulative
	 *
	 * @param pastMonthsCount
	 * @return startDate
	 */
	public static CustomDateRange getMonthsForPastDataHistory(int pastMonthsCount) {
		CustomDateRange cdr = new CustomDateRange();
		int dataPoint = (int) ObjectUtils.defaultIfNull(pastMonthsCount, 15) - 1;
		LocalDate endDate = LocalDate.now();
		YearMonth month = YearMonth.from(endDate.minusMonths(dataPoint));
		LocalDate startDate = month.atDay(1);
		cdr.setStartDate(startDate);
		cdr.setEndDate(endDate);
		return cdr;
	}

	public static CustomDateRange getDayForPastDataHistory(int pastDayCount) {
		CustomDateRange cdr = new CustomDateRange();
		LocalDate endDate = LocalDate.now().plusDays(1);
		LocalDate startDate = endDate.minusDays(pastDayCount);
		cdr.setStartDate(startDate);
		cdr.setEndDate(endDate);
		return cdr;
	}

	/**
	 * Based on sprint details type converted sprint issue objects to jira issue
	 * number ids list
	 * 
	 * @param sprintDetails
	 * @param issueType
	 * @return
	 */
	public static List<String> getIssuesIdListBasedOnTypeFromSprintDetails(SprintDetails sprintDetails,
			String issueType) {
		if (issueType.equalsIgnoreCase(CommonConstant.COMPLETED_ISSUES)) {
			return CollectionUtils.emptyIfNull(sprintDetails.getCompletedIssues()).stream().filter(Objects::nonNull)
					.map(SprintIssue::getNumber).distinct().collect(Collectors.toList());
		} else if (issueType.equalsIgnoreCase(CommonConstant.NOT_COMPLETED_ISSUES)) {
			return CollectionUtils.emptyIfNull(sprintDetails.getNotCompletedIssues()).stream().filter(Objects::nonNull)
					.map(SprintIssue::getNumber).distinct().collect(Collectors.toList());
		} else if (issueType.equalsIgnoreCase(CommonConstant.PUNTED_ISSUES)) {
			return CollectionUtils.emptyIfNull(sprintDetails.getPuntedIssues()).stream().filter(Objects::nonNull)
					.map(SprintIssue::getNumber).distinct().collect(Collectors.toList());
		} else if (issueType.equalsIgnoreCase(CommonConstant.COMPLETED_ISSUES_ANOTHER_SPRINT)) {
			return CollectionUtils.emptyIfNull(sprintDetails.getCompletedIssuesAnotherSprint()).stream()
					.filter(Objects::nonNull).map(SprintIssue::getNumber).distinct().collect(Collectors.toList());
		} else if (issueType.equalsIgnoreCase(CommonConstant.TOTAL_ISSUES)) {
			return CollectionUtils.emptyIfNull(sprintDetails.getTotalIssues()).stream().filter(Objects::nonNull)
					.map(SprintIssue::getNumber).distinct().collect(Collectors.toList());
		} else if (issueType.equalsIgnoreCase(CommonConstant.ADDED_ISSUES)) {
			return CollectionUtils.emptyIfNull(sprintDetails.getAddedIssues()).stream().filter(Objects::nonNull)
					.collect(Collectors.toList());
		} else {
			return new ArrayList<>();
		}
	}

	public static void prepareFieldMappingDefectTypeTransformation(Map<String, Object> mapOfProjectFilters,
			List<String> defectType, List<String> kpiWiseDefectsFieldMapping, String key) {
		if (Optional.ofNullable(defectType).isPresent()
				&& CollectionUtils.containsAny(kpiWiseDefectsFieldMapping, defectType)) {
			kpiWiseDefectsFieldMapping.removeIf(x -> defectType.contains(x));
			kpiWiseDefectsFieldMapping.add(NormalizedJira.DEFECT_TYPE.getValue());
		}
		mapOfProjectFilters.put(key, CommonUtils.convertToPatternList(kpiWiseDefectsFieldMapping));
	}

	/**
	 * replace some details of jira issue as per sprint report
	 *
	 * @param sprintDetails
	 * @param sprintIssues
	 * @param allJiraIssue
	 * @return
	 */
	public static Set<JiraIssue> getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(SprintDetails sprintDetails,
			Set<SprintIssue> sprintIssues, List<JiraIssue> allJiraIssue) {
		Set<JiraIssue> filteredIssues = new HashSet<>();
		Map<String, SprintIssue> issueKeyWiseSprintIssue = new HashMap<>();
		if (CollectionUtils.isNotEmpty(sprintIssues)) {
			sprintIssues.stream()
					.forEach(sprintIssue -> issueKeyWiseSprintIssue.put(sprintIssue.getNumber(), sprintIssue));
		} else {
			CollectionUtils.emptyIfNull(sprintDetails.getTotalIssues()).stream()
					.forEach(sprintIssue -> issueKeyWiseSprintIssue.put(sprintIssue.getNumber(), sprintIssue));
			CollectionUtils.emptyIfNull(sprintDetails.getPuntedIssues()).stream()
					.forEach(sprintIssue -> issueKeyWiseSprintIssue.put(sprintIssue.getNumber(), sprintIssue));
		}
		allJiraIssue.stream().forEach(jiraIssue -> {
			if (Objects.nonNull(issueKeyWiseSprintIssue.get(jiraIssue.getNumber()))) {
				JiraIssue filterJiraIssue = null;
				try {
					filterJiraIssue = (JiraIssue) jiraIssue.clone();
				} catch (CloneNotSupportedException e) {
					filterJiraIssue = jiraIssue;
					log.error("[KPIDataHelper]. exception while clone ing object jira issue{}", e);
				}
				SprintIssue sprintIssue = issueKeyWiseSprintIssue.get(jiraIssue.getNumber());
				filterJiraIssue.setStoryPoints(sprintIssue.getStoryPoints());
				filterJiraIssue.setPriority(sprintIssue.getPriority());
				filterJiraIssue.setStatus(sprintIssue.getStatus());
				filterJiraIssue.setTypeName(sprintIssue.getTypeName());
				if (null != filterJiraIssue.getAggregateTimeRemainingEstimateMinutes()) {
					filterJiraIssue
							.setRemainingEstimateMinutes((filterJiraIssue.getAggregateTimeRemainingEstimateMinutes()));
				} else if (Objects.nonNull(sprintIssue.getRemainingEstimate())) {
					Double remainingEst = (sprintIssue.getRemainingEstimate()) / 60;
					filterJiraIssue.setRemainingEstimateMinutes(remainingEst.intValue());
				}
				if (null != filterJiraIssue.getAggregateTimeOriginalEstimateMinutes()) {
					filterJiraIssue
							.setOriginalEstimateMinutes((filterJiraIssue.getAggregateTimeOriginalEstimateMinutes()));
				}
				filteredIssues.add(filterJiraIssue);
			}
		});
		return filteredIssues;
	}

	public static List<IterationPotentialDelay> sprintWiseDelayCalculation(
			List<JiraIssue> inProgressIssuesJiraIssueList, List<JiraIssue> openIssuesJiraIssueList,
			SprintDetails sprintDetails) {
		List<IterationPotentialDelay> iterationPotentialDelayList = new ArrayList<>();
		LocalDate pivotPCD = null;
		Map<LocalDate, List<JiraIssue>> dueDateWiseInProgressJiraIssue = createDueDateWiseMap(
				inProgressIssuesJiraIssueList);
		Map<LocalDate, List<JiraIssue>> dueDateWiseOpenJiraIssue = createDueDateWiseMap(openIssuesJiraIssueList);
		if (MapUtils.isNotEmpty(dueDateWiseInProgressJiraIssue)) {
			for (Map.Entry<LocalDate, List<JiraIssue>> entry : dueDateWiseInProgressJiraIssue.entrySet()) {
				pivotPCD = getNextPotentialClosedDate(sprintDetails, iterationPotentialDelayList, pivotPCD, entry);
			}
		}
		if (MapUtils.isNotEmpty(dueDateWiseOpenJiraIssue)) {
			for (Map.Entry<LocalDate, List<JiraIssue>> entry : dueDateWiseOpenJiraIssue.entrySet()) {
				pivotPCD = getNextPotentialClosedDate(sprintDetails, iterationPotentialDelayList, pivotPCD, entry);
			}
		}
		return iterationPotentialDelayList;
	}

	private static LocalDate getNextPotentialClosedDate(SprintDetails sprintDetails,
			List<IterationPotentialDelay> iterationPotentialDelayList, LocalDate pivotPCD,
			Map.Entry<LocalDate, List<JiraIssue>> entry) {
		LocalDate pivotPCDLocal = null;
		for (JiraIssue issue : entry.getValue()) {
			int remainingEstimateTime = getRemainingEstimateTime(issue);
			LocalDate potentialClosedDate = getPotentialClosedDate(sprintDetails, pivotPCD, remainingEstimateTime);
			int potentialDelay = getPotentialDelay(entry.getKey(), potentialClosedDate);
			iterationPotentialDelayList.add(createIterationPotentialDelay(potentialClosedDate, potentialDelay,
					remainingEstimateTime, issue, sprintDetails.getState().equalsIgnoreCase(CLOSED), entry.getKey()));
			pivotPCDLocal = checkPivotPCD(sprintDetails, potentialClosedDate, remainingEstimateTime, pivotPCDLocal);
		}
		pivotPCD = pivotPCDLocal == null ? pivotPCD : pivotPCDLocal;
		return pivotPCD;
	}

	/**
	 * if remaining time is 0 and sprint is closed, then PCD is sprint end time
	 * otherwise will create PCD
	 *
	 * @param sprintDetails
	 * @param pivotPCD
	 * @param estimatedTime
	 * @return
	 */
	private static LocalDate getPotentialClosedDate(SprintDetails sprintDetails, LocalDate pivotPCD,
			int estimatedTime) {
		return (estimatedTime == 0 && sprintDetails.getState().equalsIgnoreCase(CLOSED))
				? DateUtil.stringToLocalDate(sprintDetails.getEndDate(), DateUtil.TIME_FORMAT_WITH_SEC)
				: createPotentialClosedDate(sprintDetails, estimatedTime, pivotPCD);
	}

	private static IterationPotentialDelay createIterationPotentialDelay(LocalDate potentialClosedDate,
			int potentialDelay, int remainingEstimateTime, JiraIssue issue, boolean sprintClosed, LocalDate dueDate) {
		IterationPotentialDelay iterationPotentialDelay = new IterationPotentialDelay();
		iterationPotentialDelay.setIssueId(issue.getNumber());
		iterationPotentialDelay.setPotentialDelay((sprintClosed && remainingEstimateTime == 0) ? 0 : potentialDelay);
		iterationPotentialDelay.setDueDate(dueDate.toString());
		iterationPotentialDelay.setPredictedCompletedDate(potentialClosedDate.toString());
		iterationPotentialDelay.setAssigneeId(issue.getAssigneeId());
		iterationPotentialDelay.setStatus(issue.getStatus());
		return iterationPotentialDelay;

	}

	/**
	 * if due date is less than potential closed date, then potential delay will be
	 * negative
	 *
	 * @param dueDate
	 * @param potentialClosedDate
	 * @return
	 */
	private static int getPotentialDelay(LocalDate dueDate, LocalDate potentialClosedDate) {
		int potentialDelays = CommonUtils.getWorkingDays(dueDate, potentialClosedDate);
		return (dueDate.isAfter(potentialClosedDate)) ? potentialDelays * (-1) : potentialDelays;
	}

	/**
	 * In closed sprint if a Remaining Estimate is 0, then the potential closing
	 * date will be same as sprint' end date, whose potential closing date will not
	 * be taken into account for further storie's delay calculation
	 *
	 * @param sprintDetails
	 * @param potentialClosedDate
	 * @param remainingEstimateTime
	 * @param pivotPCDLocal
	 * @return
	 */
	private static LocalDate checkPivotPCD(SprintDetails sprintDetails, LocalDate potentialClosedDate,
			int remainingEstimateTime, LocalDate pivotPCDLocal) {
		if ((pivotPCDLocal == null || pivotPCDLocal.isBefore(potentialClosedDate))
				&& (!sprintDetails.getState().equalsIgnoreCase(CLOSED)
						|| (sprintDetails.getState().equalsIgnoreCase(CLOSED) && remainingEstimateTime != 0))) {
			pivotPCDLocal = potentialClosedDate;
		}
		return pivotPCDLocal;
	}

	/**
	 * create dueDateWise sorted Map only for the stories having dueDate
	 *
	 * @param arrangeJiraIssueList
	 * @return
	 */
	private static Map<LocalDate, List<JiraIssue>> createDueDateWiseMap(List<JiraIssue> arrangeJiraIssueList) {
		TreeMap<LocalDate, List<JiraIssue>> localDateListMap = new TreeMap<>();
		if (CollectionUtils.isNotEmpty(arrangeJiraIssueList)) {
			arrangeJiraIssueList.forEach(jiraIssue -> {
				LocalDate dueDate = DateUtil.stringToLocalDate(jiraIssue.getDueDate(), DateUtil.TIME_FORMAT_WITH_SEC);
				localDateListMap.computeIfPresent(dueDate, (date, issue) -> {
					issue.add(jiraIssue);
					return issue;
				});
				localDateListMap.computeIfAbsent(dueDate, value -> {
					List<JiraIssue> issues = new ArrayList<>();
					issues.add(jiraIssue);
					return issues;
				});
			});
		}
		return localDateListMap;
	}

	/*
	 * add remaining estimates to the PCD calculated from the previous stories
	 */
	private static LocalDate createPotentialClosedDate(SprintDetails sprintDetails, int remainingEstimateTime,
			LocalDate pivotPCD) {
		LocalDate pcd = null;
		if (pivotPCD == null) {
			// for the first calculation
			LocalDate startDate = sprintDetails.getState().equalsIgnoreCase(CLOSED)
					? DateUtil.stringToLocalDate(sprintDetails.getEndDate(), DateUtil.TIME_FORMAT_WITH_SEC)
					: LocalDate.now();

			pcd = CommonUtils.getWorkingDayAfterAdditionofDays(startDate, remainingEstimateTime);
		} else {
			pcd = CommonUtils.getWorkingDayAfterAdditionofDays(pivotPCD, remainingEstimateTime);
		}
		return pcd;
	}

	private static int getRemainingEstimateTime(JiraIssue issueObject) {
		int remainingEstimate = 0;
		if (issueObject.getRemainingEstimateMinutes() != null) {
			remainingEstimate = (issueObject.getRemainingEstimateMinutes() / 60) / 8;
		}
		return remainingEstimate;
	}

	/**
	 * To collect originalEstimate
	 * 
	 * @param overAllOriginalEstimate
	 * @param originalEstimate
	 * @param jiraIssue
	 * @return
	 */
	public static Double getOriginalEstimate(List<Double> overAllOriginalEstimate, Double originalEstimate,
			JiraIssue jiraIssue) {
		if (null != jiraIssue.getOriginalEstimateMinutes()) {
			originalEstimate = originalEstimate + jiraIssue.getOriginalEstimateMinutes();
			overAllOriginalEstimate.set(0, overAllOriginalEstimate.get(0) + jiraIssue.getOriginalEstimateMinutes());
		}
		return originalEstimate;
	}

	/**
	 * To collect StoryPoint
	 * 
	 * @param overAllStoryPoints
	 * @param storyPoint
	 * @param jiraIssue
	 * @return
	 */
	public static Double getStoryPoint(List<Double> overAllStoryPoints, Double storyPoint, JiraIssue jiraIssue) {
		if (null != jiraIssue.getStoryPoints()) {
			storyPoint = storyPoint + jiraIssue.getStoryPoints();
			overAllStoryPoints.set(0, overAllStoryPoints.get(0) + jiraIssue.getStoryPoints());
		}
		return storyPoint;
	}

	/**
	 * Calculating max delay of each assignee based on max marker
	 * 
	 * @param jiraIssue
	 * @param issueWiseDelay
	 * @param potentialDelay
	 * @param overallPotentialDelay
	 * @return
	 */
	public static int checkDelay(JiraIssue jiraIssue, Map<String, IterationPotentialDelay> issueWiseDelay,
			int potentialDelay, List<Integer> overallPotentialDelay) {
		int finalDelay = 0;
		if (issueWiseDelay.containsKey(jiraIssue.getNumber())
				&& issueWiseDelay.get(jiraIssue.getNumber()).isMaxMarker()) {
			IterationPotentialDelay iterationPotentialDelay = issueWiseDelay.get(jiraIssue.getNumber());
			finalDelay = potentialDelay + getDelayInMinutes(iterationPotentialDelay.getPotentialDelay());
			overallPotentialDelay.set(0,
					overallPotentialDelay.get(0) + getDelayInMinutes(iterationPotentialDelay.getPotentialDelay()));
		} else {
			finalDelay = potentialDelay + finalDelay;
		}
		return finalDelay;
	}

	public static int getDelayInMinutes(int delay) {
		return delay * 60 * 8;
	}

	/**
	 * To create Map of Modal Object
	 * 
	 * @param jiraIssueList
	 * @return
	 */
	public static Map<String, IterationKpiModalValue> createMapOfModalObject(List<JiraIssue> jiraIssueList) {
		return jiraIssueList.stream()
				.collect(Collectors.toMap(JiraIssue::getNumber, issue -> new IterationKpiModalValue()));
	}

	public static SprintDetails processSprintBasedOnFieldMappings(SprintDetails dbSprintDetail,
			List<String> fieldMappingCompletionType, List<String> fieldMappingCompletionStatus,
			Map<ObjectId, Map<String, List<LocalDateTime>>> projectWiseDuplicateIssuesWithMinCloseDate) {
		if ((CollectionUtils.isNotEmpty(fieldMappingCompletionType)
				|| CollectionUtils.isNotEmpty(fieldMappingCompletionStatus))) {
			dbSprintDetail
					.setCompletedIssues(CollectionUtils.isEmpty(dbSprintDetail.getCompletedIssues()) ? new HashSet<>()
							: dbSprintDetail.getCompletedIssues());
			dbSprintDetail.setNotCompletedIssues(
					CollectionUtils.isEmpty(dbSprintDetail.getNotCompletedIssues()) ? new HashSet<>()
							: dbSprintDetail.getNotCompletedIssues());
			Set<SprintIssue> newCompletedSet = filteringByFieldMapping(dbSprintDetail, fieldMappingCompletionType,
					fieldMappingCompletionStatus);
			dbSprintDetail.getNotCompletedIssues().removeAll(newCompletedSet);
			// filtering by minimum closed date, if an issue is originally in RFT in jira
			// report, and spilled in the same status through fieldmapping we changed RFT as
			// closed, then the first sprint in which it appeared in RFT should be
			// considered as the only sprint when it was closed, and not in further sprint,
			// as it changes the velocity of each sprint
			newCompletedSet = changeSprintDetails(dbSprintDetail, newCompletedSet, fieldMappingCompletionStatus,
					projectWiseDuplicateIssuesWithMinCloseDate);
			dbSprintDetail.setCompletedIssues(newCompletedSet);
			dbSprintDetail.getNotCompletedIssues().removeAll(newCompletedSet);
			Set<SprintIssue> totalIssue = new HashSet<>();
			totalIssue.addAll(dbSprintDetail.getCompletedIssues());
			totalIssue.addAll(dbSprintDetail.getNotCompletedIssues());
			dbSprintDetail.setTotalIssues(totalIssue);
		}
		return dbSprintDetail;
	}

	public static Set<SprintIssue> changeSprintDetails(SprintDetails sprintDetail, Set<SprintIssue> completedIssues,
			List<String> customCompleteStatus, Map<ObjectId, Map<String, List<LocalDateTime>>> issueWiseMinimumDates) {
		if (CollectionUtils.isNotEmpty(customCompleteStatus) && CollectionUtils.isNotEmpty(completedIssues)
				&& MapUtils.isNotEmpty(issueWiseMinimumDates)) {
			ObjectId projectId = sprintDetail.getBasicProjectConfigId();
			Map<String, List<LocalDateTime>> stringListMap = issueWiseMinimumDates.get(projectId);
			if (MapUtils.isNotEmpty(stringListMap)) {
				LocalDateTime endLocalDate = sprintDetail.getState().equalsIgnoreCase(SprintDetails.SPRINT_STATE_ACTIVE)
						? LocalDateTime.now()
						: LocalDateTime.ofInstant(Instant.parse(sprintDetail.getCompleteDate()),
								ZoneId.systemDefault());
				return completedIssues.stream().filter(completedIssue -> {
					List<LocalDateTime> issueDateMap = stringListMap.get(completedIssue.getNumber());
					if (CollectionUtils.isNotEmpty(issueDateMap)) {
						return issueDateMap.stream()
								.anyMatch(dateTime -> DateUtil.isWithinDateTimeRange(dateTime, LocalDateTime
										.ofInstant(Instant.parse(sprintDetail.getStartDate()), ZoneId.systemDefault()),
										endLocalDate));
					}
					return true;
				}).collect(Collectors.toSet());
			}
		}
		return completedIssues;
	}

	private static Set<SprintIssue> getCombinationalCompletedSet(Set<SprintIssue> typeWiseIssues,
			Set<SprintIssue> statusWiseIssues) {
		Set<SprintIssue> newCompletedSet;
		if (CollectionUtils.isNotEmpty(typeWiseIssues) && CollectionUtils.isNotEmpty(statusWiseIssues)) {
			newCompletedSet = new HashSet<>(CollectionUtils.intersection(typeWiseIssues, statusWiseIssues));
		} else if (CollectionUtils.isNotEmpty(typeWiseIssues)) {
			newCompletedSet = typeWiseIssues;
		} else {
			newCompletedSet = statusWiseIssues;
		}
		return newCompletedSet;
	}

	private static Set<SprintIssue> filteringByFieldMapping(SprintDetails dbSprintDetail,
			List<String> fieldMapingCompletionType, List<String> fieldMappingCompletionStatus) {
		Set<SprintIssue> typeWiseIssues = new HashSet<>();
		Set<SprintIssue> statusWiseIssues = new HashSet<>();
		if (CollectionUtils.isNotEmpty(fieldMappingCompletionStatus)
				&& CollectionUtils.isNotEmpty(fieldMapingCompletionType)) {
			statusWiseIssues.addAll(dbSprintDetail.getCompletedIssues().stream()
					.filter(issue -> fieldMappingCompletionStatus.contains(issue.getStatus()))
					.collect(Collectors.toSet()));
			statusWiseIssues.addAll(dbSprintDetail.getNotCompletedIssues().stream()
					.filter(issue -> fieldMappingCompletionStatus.contains(issue.getStatus()))
					.collect(Collectors.toSet()));
			typeWiseIssues.addAll(dbSprintDetail.getCompletedIssues().stream()
					.filter(issue -> fieldMapingCompletionType.contains(issue.getTypeName()))
					.collect(Collectors.toSet()));
			typeWiseIssues.addAll(dbSprintDetail.getNotCompletedIssues().stream()
					.filter(issue -> fieldMapingCompletionType.contains(issue.getTypeName()))
					.collect(Collectors.toSet()));
		} else if (CollectionUtils.isNotEmpty(fieldMappingCompletionStatus)) {
			statusWiseIssues.addAll(dbSprintDetail.getCompletedIssues().stream()
					.filter(issue -> fieldMappingCompletionStatus.contains(issue.getStatus()))
					.collect(Collectors.toSet()));
			statusWiseIssues.addAll(dbSprintDetail.getNotCompletedIssues().stream()
					.filter(issue -> fieldMappingCompletionStatus.contains(issue.getStatus()))
					.collect(Collectors.toSet()));
		} else if (CollectionUtils.isNotEmpty(fieldMapingCompletionType)) {
			typeWiseIssues.addAll(dbSprintDetail.getCompletedIssues().stream()
					.filter(issue -> fieldMapingCompletionType.contains(issue.getTypeName()))
					.collect(Collectors.toSet()));
		}
		return getCombinationalCompletedSet(typeWiseIssues, statusWiseIssues);
	}

	/**
	 * To create Map of Modal Object
	 *
	 * @param jiraIssueCustomHistories
	 * @param cycleTimeList
	 * @return
	 */
	public static Map<String, IterationKpiModalValue> createMapOfModalObjectFromJiraHistory(
			List<JiraIssueCustomHistory> jiraIssueCustomHistories, List<CycleTimeValidationData> cycleTimeList) {
		Map<String, IterationKpiModalValue> dataMap = new HashMap<>();
		for (JiraIssueCustomHistory customHistory : jiraIssueCustomHistories) {
			Optional<CycleTimeValidationData> cycleTimeValidationDataOptional = cycleTimeList.stream()
					.filter(cyc -> cyc.getIssueNumber().equalsIgnoreCase(customHistory.getStoryID())).findFirst();
			if (cycleTimeValidationDataOptional.isPresent()) {
				CycleTimeValidationData cycleTimeValidationData = cycleTimeValidationDataOptional.get();
				IterationKpiModalValue iterationKpiModalValue = new IterationKpiModalValue();
				iterationKpiModalValue.setIssueId(customHistory.getStoryID());
				iterationKpiModalValue.setIssueType(customHistory.getStoryType());
				iterationKpiModalValue.setIssueURL(customHistory.getUrl());
				iterationKpiModalValue.setDescription(customHistory.getDescription());
				if (isNotEmpty(cycleTimeValidationData.getIntakeTime())) {
					iterationKpiModalValue.setIntakeToDOR(
							CommonUtils.convertIntoDays(Math.toIntExact(cycleTimeValidationData.getIntakeTime())));
					iterationKpiModalValue.setDorDate(
							DateUtil.dateTimeConverter(cycleTimeValidationData.getDorDate().toString().split("T")[0],
									DateUtil.DATE_FORMAT, DateUtil.DISPLAY_DATE_FORMAT));
				} else {
					iterationKpiModalValue.setIntakeToDOR(Constant.NOT_AVAILABLE);
					iterationKpiModalValue.setDorDate(Constant.NOT_AVAILABLE);
				}
				if (isNotEmpty(cycleTimeValidationData.getDorTime())) {
					iterationKpiModalValue.setDodDate(
							DateUtil.dateTimeConverter(cycleTimeValidationData.getDodDate().toString().split("T")[0],
									DateUtil.DATE_FORMAT, DateUtil.DISPLAY_DATE_FORMAT));
					iterationKpiModalValue.setDorToDod(
							CommonUtils.convertIntoDays(Math.toIntExact(cycleTimeValidationData.getDorTime())));
				} else {
					iterationKpiModalValue.setDodDate(Constant.NOT_AVAILABLE);
					iterationKpiModalValue.setDorToDod(Constant.NOT_AVAILABLE);
				}
				if (isNotEmpty(cycleTimeValidationData.getDodTime())) {
					iterationKpiModalValue.setLiveDate(
							DateUtil.dateTimeConverter(cycleTimeValidationData.getLiveDate().toString().split("T")[0],
									DateUtil.DATE_FORMAT, DateUtil.DISPLAY_DATE_FORMAT));
					iterationKpiModalValue.setDodToLive(
							CommonUtils.convertIntoDays(Math.toIntExact(cycleTimeValidationData.getDodTime())));
				} else {
					iterationKpiModalValue.setLiveDate(Constant.NOT_AVAILABLE);
					iterationKpiModalValue.setDodToLive(Constant.NOT_AVAILABLE);
				}
				dataMap.put(customHistory.getStoryID(), iterationKpiModalValue);
			}
		}
		return dataMap;
	}

	private static String getTimeValue(String time) {
		if (time != null && !time.equalsIgnoreCase(Constant.NOT_AVAILABLE)) {
			return CommonUtils.convertIntoDays((int) calculateTimeInDays(Long.parseLong(time)));
		} else {
			return Constant.NOT_AVAILABLE;
		}
	}

	public static String calWeekHours(DateTime startDateTime, DateTime endDateTime) {
		if (startDateTime != null && endDateTime != null) {
			int hours = Hours.hoursBetween(startDateTime, endDateTime).getHours();
			int weekendsCount = countSaturdaysAndSundays(startDateTime, endDateTime);
			int res = hours - weekendsCount * 24;
			return String.valueOf(res);
		}
		return DateUtil.NOT_APPLICABLE;
	}

	/**
	 *
	 * @param startDateTime
	 * @param endDateTime
	 * @return
	 */

	public static double calWeekDaysExcludingWeekends(DateTime startDateTime, DateTime endDateTime) {
		if (startDateTime != null && endDateTime != null) {
			Duration duration = new Duration(startDateTime, endDateTime);
			long leadTimeChangeInMin = duration.getStandardMinutes();
			double leadTimeChangeInFullDay = (double) leadTimeChangeInMin / 60 / 24;
			if (leadTimeChangeInFullDay > 0) {
				String formattedValue = df.format(leadTimeChangeInFullDay);
				double leadTimeChangeIncluded = Double.parseDouble(formattedValue);
				int weekendsCount = countSaturdaysAndSundays(startDateTime, endDateTime);
				double leadTimeChangeExcluded = leadTimeChangeIncluded - weekendsCount;
				return leadTimeChangeExcluded;
			}
		}
		return 0.0d;
	}

	/**
	 * Cal time with 8hr in a day
	 *
	 * @param timeInHours
	 * @return
	 */
	public static long calculateTimeInDays(long timeInHours) {
		long timeInMin = (timeInHours / 24) * 8 * 60;
		long remainingTimeInMin = (timeInHours % 24) * 60;
		if (remainingTimeInMin >= 480) {
			timeInMin = timeInMin + 480;
		} else {
			timeInMin = timeInMin + remainingTimeInMin;
		}
		return timeInMin;
	}

	public static int countSaturdaysAndSundays(DateTime startDateTime, DateTime endDateTime) {
		int count = 0;
		DateTime current = startDateTime;
		while (current.isBefore(endDateTime)) {
			if (current.getDayOfWeek() == DateTimeConstants.SATURDAY
					|| current.getDayOfWeek() == DateTimeConstants.SUNDAY) {
				count++;
			}
			current = current.plusDays(1);
		}
		return count;
	}

	/**
	 * Get completed subtask of sprint
	 *
	 * @param totalSubTask
	 * @param subTaskHistory
	 * @param sprintDetail
	 * @param fieldMappingDoneStatus
	 * @return
	 */
	public static List<JiraIssue> getCompletedSubTasksByHistory(List<JiraIssue> totalSubTask,
			List<JiraIssueCustomHistory> subTaskHistory, SprintDetails sprintDetail,
			List<String> fieldMappingDoneStatus) {
		List<JiraIssue> resolvedSubtaskForSprint = new ArrayList<>();
		LocalDateTime sprintEndDateTime = sprintDetail.getCompleteDate() != null
				? LocalDateTime.parse(sprintDetail.getCompleteDate().split("\\.")[0], DATE_TIME_FORMATTER)
				: LocalDateTime.parse(sprintDetail.getEndDate().split("\\.")[0], DATE_TIME_FORMATTER);
		LocalDateTime sprintStartDateTime = sprintDetail.getActivatedDate() != null
				? LocalDateTime.parse(sprintDetail.getActivatedDate().split("\\.")[0], DATE_TIME_FORMATTER)
				: LocalDateTime.parse(sprintDetail.getStartDate().split("\\.")[0], DATE_TIME_FORMATTER);

		totalSubTask.forEach(jiraIssue -> {
			JiraIssueCustomHistory jiraIssueCustomHistory = subTaskHistory.stream().filter(
					issueCustomHistory -> issueCustomHistory.getStoryID().equalsIgnoreCase(jiraIssue.getNumber()))
					.findFirst().orElse(new JiraIssueCustomHistory());
			Optional<JiraHistoryChangeLog> issueSprint = jiraIssueCustomHistory.getStatusUpdationLog().stream()
					.filter(jiraIssueSprint -> DateUtil.isWithinDateTimeRange(jiraIssueSprint.getUpdatedOn(),
							sprintStartDateTime, sprintEndDateTime))
					.reduce((a, b) -> b);
			if (issueSprint.isPresent()
					&& fieldMappingDoneStatus.contains(issueSprint.get().getChangedTo().toLowerCase()))
				resolvedSubtaskForSprint.add(jiraIssue);
		});
		return resolvedSubtaskForSprint;
	}

	/**
	 * Get total subtask of sprint
	 *
	 * @param allSubTasks
	 * @param sprintDetails
	 * @param subTaskHistory
	 * @param fieldMappingDoneStatus
	 * @return
	 */
	public static List<JiraIssue> getTotalSprintSubTasks(List<JiraIssue> allSubTasks, SprintDetails sprintDetails,
			List<JiraIssueCustomHistory> subTaskHistory, List<String> fieldMappingDoneStatus) {
		LocalDateTime sprintEndDate = sprintDetails.getCompleteDate() != null
				? LocalDateTime.parse(sprintDetails.getCompleteDate().split("\\.")[0], DATE_TIME_FORMATTER)
				: LocalDateTime.parse(sprintDetails.getEndDate().split("\\.")[0], DATE_TIME_FORMATTER);
		LocalDateTime sprintStartDate = sprintDetails.getActivatedDate() != null
				? LocalDateTime.parse(sprintDetails.getActivatedDate().split("\\.")[0], DATE_TIME_FORMATTER)
				: LocalDateTime.parse(sprintDetails.getStartDate().split("\\.")[0], DATE_TIME_FORMATTER);
		List<JiraIssue> subTaskTaggedWithSprint = new ArrayList<>();

		allSubTasks.forEach(jiraIssue -> {
			JiraIssueCustomHistory jiraIssueCustomHistory = subTaskHistory.stream().filter(
					issueCustomHistory -> issueCustomHistory.getStoryID().equalsIgnoreCase(jiraIssue.getNumber()))
					.findFirst().orElse(new JiraIssueCustomHistory());
			Optional<JiraHistoryChangeLog> jiraHistoryChangeLog = jiraIssueCustomHistory.getStatusUpdationLog().stream()
					.filter(changeLog -> fieldMappingDoneStatus.contains(changeLog.getChangedTo().toLowerCase())
							&& changeLog.getUpdatedOn().isAfter(sprintStartDate))
					.findFirst();
			if (jiraHistoryChangeLog.isPresent() && sprintEndDate
					.isAfter(LocalDateTime.parse(jiraIssue.getCreatedDate().split("\\.")[0], DATE_TIME_FORMATTER)))
				subTaskTaggedWithSprint.add(jiraIssue);

		});
		return subTaskTaggedWithSprint;
	}

	/**
	 * Return the duration filter details for dora dashboard
	 * 
	 * @param kpiElement
	 * @return
	 */
	public static Map<String, Object> getDurationFilter(KpiElement kpiElement) {
		LinkedHashMap<String, Object> filterDuration = (LinkedHashMap<String, Object>) kpiElement.getFilterDuration();
		int value = 8; // Default value for 'value'
		String duration = CommonConstant.WEEK; // Default value for 'duration'
		LocalDateTime startDateTime = null;

		if (filterDuration != null) {
			value = (int) filterDuration.getOrDefault("value", 8);
			duration = (String) filterDuration.getOrDefault(Constant.DURATION, CommonConstant.WEEK);
		}

		if (duration.equalsIgnoreCase(CommonConstant.WEEK)) {
			startDateTime = LocalDateTime.now().minusWeeks(value);
		} else if (duration.equalsIgnoreCase(CommonConstant.MONTH)) {
			startDateTime = LocalDateTime.now().minusMonths(value);
		}

		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put(Constant.DATE, startDateTime);
		resultMap.put(Constant.DURATION, duration);
		resultMap.put(Constant.COUNT, value);
		return resultMap;
	}

	public static void getMiniDateOfCompleteCycle(List<String> completionStatus,
			List<JiraHistoryChangeLog> statusUpdationLog, Map<String, LocalDateTime> minimumCompletedStatusWiseMap,
			List<LocalDateTime> minimumDate) {
		for (JiraHistoryChangeLog log : statusUpdationLog) {
			String changedTo = log.getChangedTo();
			if (completionStatus.contains(changedTo)) {
				LocalDateTime updatedOn = log.getUpdatedOn();
				minimumCompletedStatusWiseMap.putIfAbsent(changedTo, updatedOn);
			} else if (!minimumCompletedStatusWiseMap.isEmpty()) {
				// if found a status which is not among closed statuses, then save the minimum
				// date and clear the map
				LocalDateTime minDate = minimumCompletedStatusWiseMap.values().stream().min(LocalDateTime::compareTo)
						.orElse(null);
				if (minDate != null) {
					minimumDate.add(minDate);
					minimumCompletedStatusWiseMap.clear();
				}
			}
		}
	}

	/**
	 * Calculate sum of storyPoint/OriginalEstimate for list of JiraIssue
	 *
	 * @param jiraIssueList
	 *            list of Jira Issue
	 * @param fieldMapping
	 *            fieldMapping
	 * @return sum of storyPoint/OriginalEstimate
	 */
	public static double calculateStoryPoints(List<JiraIssue> jiraIssueList, FieldMapping fieldMapping) {
		boolean isStoryPoint = StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
				&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT);
		return jiraIssueList.stream().mapToDouble(jiraIssue -> {
			if (isStoryPoint) {
				return Optional.ofNullable(jiraIssue.getStoryPoints()).orElse(0.0d);
			} else {
				Integer timeInMin = Optional.ofNullable(jiraIssue.getAggregateTimeOriginalEstimateMinutes()).orElse(0);
				int inHours = timeInMin / 60;
				return inHours / fieldMapping.getStoryPointToHourMapping();
			}
		}).sum();
	}


	/**
	 * To calculate the added/removed date of sprint change for JiraIssues
	 *
	 * @param issues
	 *            list of jiraIssue
	 * @param sprint
	 *            name of sprint
	 * @param issueWiseHistoryMap
	 *            Map of issueKey vs sprintUpdateLog
	 * @param changeType
	 *            add/remove
	 * @return Map<issueKey, Date>
	 */
	public static Map<String, String> processSprintIssues(List<JiraIssue> issues, String sprint,
			Map<String, List<JiraHistoryChangeLog>> issueWiseHistoryMap, String changeType) {
		Map<String, String> issueDateMap = new HashMap<>();

		issues.stream().map(JiraIssue::getNumber).forEach(issueKey -> {
			List<JiraHistoryChangeLog> sprintUpdateLog = issueWiseHistoryMap.get(issueKey);

			sprintUpdateLog.stream()
					.filter(sprintUpdate -> changeType.equals(CommonConstant.ADDED)
							? sprintUpdate.getChangedTo().equalsIgnoreCase(sprint)
							: changeType.equals(CommonConstant.REMOVED)
									&& sprintUpdate.getChangedFrom().equalsIgnoreCase(sprint))
					.forEach(sprintUpdate -> issueDateMap.put(issueKey,
							DateUtil.dateTimeConverter(sprintUpdate.getUpdatedOn().toString(), DateUtil.DATE_FORMAT,
									DateUtil.DISPLAY_DATE_FORMAT)));
		});

		return issueDateMap;
	}

}