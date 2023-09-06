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

package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import static com.publicissapient.kpidashboard.common.constant.CommonConstant.HIERARCHY_LEVEL_ID_PROJECT;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.LeadTimeChangeData;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.ReleaseVersion;
import com.publicissapient.kpidashboard.common.model.scm.MergeRequests;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.scm.MergeRequestRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * This service for managing Lead time for change kpi for dora tab.
 *
 * @author hiren babariya
 */
@Component
@Slf4j
public class LeadTimeForChangeServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

	@Autowired
	private MergeRequestRepository mergeRequestRepository;

	@Autowired
	private CustomApiConfig customApiConfig;

	private final Random random = new Random();

	@Override
	public String getQualifierType() {
		return KPICode.LEAD_TIME_CHANGE.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();

		List<Node> projectList = treeAggregatorDetail.getMapOfListOfProjectNodes().get(HIERARCHY_LEVEL_ID_PROJECT);
		projectWiseLeafNodeValue(mapTmp, projectList, kpiElement);

		log.debug("[DIR-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValue(root, nodeWiseKPIValue, KPICode.LEAD_TIME_CHANGE);
		List<DataCount> trendValues = getTrendValues(kpiRequest, nodeWiseKPIValue, KPICode.LEAD_TIME_CHANGE);
		kpiElement.setTrendValueList(trendValues);

		return kpiElement;
	}

	/**
	 * calculate and set project wise leaf node value
	 *
	 * @param mapTmp
	 * @param projectLeafNodeList
	 * @param kpiElement
	 */
	private void projectWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> projectLeafNodeList,
			KpiElement kpiElement) {

		Map<String, Object> durationFilter = KpiDataHelper.getDurationFilter(kpiElement);
		LocalDateTime localStartDate = (LocalDateTime) durationFilter.get(Constant.DATE);
		LocalDateTime localEndDate = LocalDateTime.now();
		DateTimeFormatter formatterMonth = DateTimeFormatter.ofPattern(DateUtil.TIME_FORMAT);
		String startDate = localStartDate.format(formatterMonth);
		String endDate = localEndDate.format(formatterMonth);
		List<KPIExcelData> excelData = new ArrayList<>();
		Map<String, Object> resultMap = fetchKPIDataFromDb(projectLeafNodeList, startDate, endDate, null);

		if (MapUtils.isNotEmpty(resultMap)) {
			String requestTrackerId = getRequestTrackerId();
			projectLeafNodeList.forEach(node -> {
				String trendLineName = node.getProjectFilter().getName();

				List<JiraIssueCustomHistory> jiraIssueHistoryDataList = (List<JiraIssueCustomHistory>) resultMap
						.get("jiraIssueHistoryData");
				List<JiraIssue> jiraIssueDataList = (List<JiraIssue>) resultMap.get("jiraIssueData");
				boolean leadTimeConfigRepoTool = (boolean) resultMap.get("leadTimeConfigRepoTool");

				log.info("Lead-time-change 4 -> {}" , trendLineName);
				String weekOrMonth = (String) durationFilter.getOrDefault(Constant.DURATION, CommonConstant.WEEK);
				int defaultTimeCount = 8;
				Map<String, List<LeadTimeChangeData>> leadTimeMapTimeWise = weekOrMonth.equalsIgnoreCase(
						CommonConstant.WEEK) ? getLastNWeek(defaultTimeCount) : getLastNMonthCount(defaultTimeCount);

				Map<String, JiraIssue> jiraIssueMap = jiraIssueDataList.stream()
						.collect(Collectors.toMap(JiraIssue::getNumber, Function.identity()));
				List<DataCount> dataCountList = new ArrayList<>();
				if (CollectionUtils.isNotEmpty(jiraIssueHistoryDataList)
						&& CollectionUtils.isNotEmpty(jiraIssueDataList)) {
					if (leadTimeConfigRepoTool) {
						findLeadTimeChangeForRepoTool(resultMap, weekOrMonth, leadTimeMapTimeWise, jiraIssueMap);
					} else {
						findLeadTimeChangeForJira(jiraIssueHistoryDataList, weekOrMonth, leadTimeMapTimeWise,
								jiraIssueMap);
					}

					leadTimeMapTimeWise.forEach((weekOrMonthName, leadTimeListCurrentTime) -> {
						DataCount dataCount = createDataCount(trendLineName, weekOrMonthName, leadTimeListCurrentTime);
						dataCountList.add(dataCount);
					});
					if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
						KPIExcelUtility.populateLeadTimeForChangeExcelData(trendLineName, leadTimeMapTimeWise,
								excelData);
						log.info("Lead-time-change 5 -> {}" , requestTrackerId);
					}
				}
				mapTmp.get(node.getId()).setValue(dataCountList);
			});
			kpiElement.setExcelData(excelData);
			kpiElement.setExcelColumns(KPIExcelColumn.LEAD_TIME_FOR_CHANGE.getColumns());
		}
	}

	/**
	 * find lead time changes differences between closed ticket date and release
	 * date
	 * 
	 * @param jiraIssueHistoryDataList
	 * @param weekOrMonth
	 * @param leadTimeMapTimeWise
	 * @param jiraIssueMap
	 */
	private void findLeadTimeChangeForJira(List<JiraIssueCustomHistory> jiraIssueHistoryDataList, String weekOrMonth,
			Map<String, List<LeadTimeChangeData>> leadTimeMapTimeWise, Map<String, JiraIssue> jiraIssueMap) {
		jiraIssueHistoryDataList.forEach(jiraIssueHistoryData -> {
			AtomicReference<DateTime> closedTicketDate = new AtomicReference<>();
			AtomicReference<DateTime> releaseDate = new AtomicReference<>();

			jiraIssueHistoryData.getStatusUpdationLog().stream().forEach(jiraHistoryChangeLog -> {
				if (jiraHistoryChangeLog.getChangedTo().equals(CommonConstant.CLOSED)) {
					closedTicketDate.set(DateUtil.convertLocalDateTimeToDateTime(jiraHistoryChangeLog.getUpdatedOn()));
				}
			});
			if (Objects.nonNull(jiraIssueMap.get(jiraIssueHistoryData.getStoryID())) && CollectionUtils
					.isNotEmpty(jiraIssueMap.get(jiraIssueHistoryData.getStoryID()).getReleaseVersions())) {
				List<ReleaseVersion> releaseVersionList = jiraIssueMap.get(jiraIssueHistoryData.getStoryID())
						.getReleaseVersions();
				releaseVersionList.stream().forEach(releaseVersion -> {
					releaseDate.set(releaseVersion.getReleaseDate());
				});
			}

			if (closedTicketDate.get() != null && releaseDate.get() != null) {
				Duration duration = new Duration(closedTicketDate.get(), releaseDate.get());
				long leadTimeChange = duration.getStandardMinutes();
				double leadTimeChangeInDays = leadTimeChange / 60 / 24;

				String weekOrMonthName = getDateFormatted(weekOrMonth, releaseDate.get());

				LeadTimeChangeData leadTimeChangeData = new LeadTimeChangeData();
				leadTimeChangeData.setStoryID(jiraIssueHistoryData.getStoryID());
				leadTimeChangeData.setStoryType(jiraIssueHistoryData.getStoryType());
				leadTimeChangeData.setCreatedDate(jiraIssueHistoryData.getCreatedDate());
				leadTimeChangeData.setClosedDate(closedTicketDate.get());
				leadTimeChangeData.setReleaseDate(releaseDate.get());
				leadTimeChangeData.setLeadTime(leadTimeChangeInDays);
				leadTimeChangeData.setDate(weekOrMonthName);
				leadTimeMapTimeWise.computeIfPresent(weekOrMonthName, (key, leadTimeChangeListCurrentTime) -> {
					leadTimeChangeListCurrentTime.add(leadTimeChangeData);
					return leadTimeChangeListCurrentTime;
				});
			}
		});
	}

	/**
	 * find lead time changes differences between merge request ticket date and
	 * release date using repo tools
	 *
	 * @param resultMap
	 * @param weekOrMonth
	 * @param leadTimeMapTimeWise
	 * @param jiraIssueMap
	 */
	private void findLeadTimeChangeForRepoTool(Map<String, Object> resultMap, String weekOrMonth,
			Map<String, List<LeadTimeChangeData>> leadTimeMapTimeWise, Map<String, JiraIssue> jiraIssueMap) {
		List<MergeRequests> mergeRequestList = (List<MergeRequests>) resultMap.get("mergeRequestData");
		if (CollectionUtils.isNotEmpty(mergeRequestList) && MapUtils.isNotEmpty(jiraIssueMap)) {
			mergeRequestList.stream().filter(resultMap::containsKey).forEach(mergeRequests -> {
				AtomicReference<DateTime> closedTicketDate = new AtomicReference<>();
				AtomicReference<DateTime> releaseDate = new AtomicReference<>();
				LocalDateTime closedDate = DateUtil.convertMillisToLocalDateTime(mergeRequests.getClosedDate());
				closedTicketDate.set(DateUtil.convertLocalDateTimeToDateTime(closedDate));
				JiraIssue jiraIssue = (JiraIssue) resultMap.get(mergeRequests.getFromBranch());
				if (CollectionUtils.isNotEmpty(jiraIssue.getReleaseVersions())) {
					jiraIssue.getReleaseVersions().stream().forEach(releaseVersion -> {
						releaseDate.set(releaseVersion.getReleaseDate());
					});
				}
				if (closedTicketDate.get() != null && releaseDate.get() != null) {
					Duration duration = new Duration(closedTicketDate.get(), releaseDate.get());
					long leadTimeChange = duration.getStandardMinutes();
					double leadTimeChangeInDays = leadTimeChange / 60 / 24;

					String weekOrMonthName = getDateFormatted(weekOrMonth, releaseDate.get());

					LeadTimeChangeData leadTimeChangeData = new LeadTimeChangeData();
					leadTimeChangeData.setStoryID(jiraIssue.getNumber());
					leadTimeChangeData.setStoryType(jiraIssue.getTypeName());
					// leadTimeChangeData.setCreatedDate(jiraIssue.getCreatedDate());
					leadTimeChangeData.setClosedDate(closedTicketDate.get());
					leadTimeChangeData.setReleaseDate(releaseDate.get());
					leadTimeChangeData.setLeadTime(leadTimeChangeInDays);
					leadTimeChangeData.setDate(weekOrMonthName);
					leadTimeMapTimeWise.computeIfPresent(weekOrMonthName, (key, leadTimeChangeListCurrentTime) -> {
						leadTimeChangeListCurrentTime.add(leadTimeChangeData);
						return leadTimeChangeListCurrentTime;
					});
				}
			});

		}
	}

	private DataCount createDataCount(String trendLineName, String weekOrMonthName,
			List<LeadTimeChangeData> leadTimeListCurrentTime) {
		double days = leadTimeListCurrentTime.stream().mapToDouble(LeadTimeChangeData::getLeadTime).sum();
		DataCount dataCount = new DataCount();
		dataCount.setData(String.valueOf(days));
		dataCount.setSProjectName(trendLineName);
		dataCount.setDate(weekOrMonthName);
		dataCount.setValue(days);
		dataCount.setHoverValue(new HashMap<>());
		return dataCount;
	}

	private String getDateFormatted(String weekOrMonth, DateTime currentDate) {
		if (weekOrMonth.equalsIgnoreCase(CommonConstant.WEEK)) {
			return DateUtil.getWeekRangeUsingDateTime(currentDate);
		} else {
			return currentDate.getYear() + Constant.DASH + currentDate.getMonthOfYear();
		}
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {

		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
		Set<ObjectId> projectBasicConfigIds = new HashSet<>();
		List<String> projectBasicConfigIdList = new ArrayList<>();
		Map<String, Object> resultListMap = new HashMap<>();
		AtomicBoolean leadTimeConfigRepoTool = new AtomicBoolean(false);
		List<String> mergeRequestStatusKPI156 = new ArrayList<>();
		leafNodeList.forEach(node -> {
			ObjectId basicProjectConfigId = node.getProjectFilter().getBasicProjectConfigId();
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();

			if (Optional.ofNullable(fieldMapping.isLeadTimeConfigRepoTool()).isPresent()) {
				leadTimeConfigRepoTool.set(fieldMapping.isLeadTimeConfigRepoTool());
				if (fieldMapping.isLeadTimeConfigRepoTool()) {
					mergeRequestStatusKPI156.addAll(fieldMapping.getMergeRequestStatusKPI156());
				}
				log.info("Lead-time-change 1 -> {}" , mergeRequestStatusKPI156);
			}

			// if (Optional.ofNullable(fieldMapping.getJiraIssueTypeKPI156()).isPresent()) {
			// mapOfProjectFilters.put(basicProjectConfigId.toString(),
			// fieldMapping.getJiraIssueTypeKPI156());
			// }

			projectBasicConfigIds.add(basicProjectConfigId);
			projectBasicConfigIdList.add(basicProjectConfigId.toString());
			uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);
		});

		// if logic 1 -> finds jiraHistory Data by release list and closed list
		// if logic 2 -> finds jiraHistory Data by release list
		// -> find merge request by jira ticket ids
		List<String> releaseList = getReleaseList();
		List<JiraIssue> jiraIssueList;
		if (CollectionUtils.isNotEmpty(releaseList)) {
			jiraIssueList = jiraIssueRepository.findByBasicProjectConfigIdAndReleaseVersionsReleaseNameIn(
					projectBasicConfigIdList.get(0), releaseList);
		} else {
			jiraIssueList = jiraIssueRepository.findByBasicProjectConfigIdIn(projectBasicConfigIdList.get(0));
			log.info("Lead-time-change 2 -> {}" , jiraIssueList.size());
		}
		Set<String> issueIdList = jiraIssueList.stream().map(JiraIssue::getNumber).collect(Collectors.toSet());

		List<JiraIssueCustomHistory> jiraIssueHistoryDataList = jiraIssueCustomHistoryRepository
				.findByStoryIDInAndBasicProjectConfigId(issueIdList, projectBasicConfigIdList.get(0));

		List<MergeRequests> mergeRequestList = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(jiraIssueHistoryDataList) && leadTimeConfigRepoTool.get()) {
			List<String> issueListSameAsBranchName = jiraIssueHistoryDataList.stream()
					.map(JiraIssueCustomHistory::getStoryID).collect(Collectors.toList());
			mergeRequestList = mergeRequestRepository.findMergeRequestListBasedOnBasicProjectConfigId(
					projectBasicConfigIds, issueListSameAsBranchName, mergeRequestStatusKPI156);
			log.info("Lead-time-change 3 -> {}" , mergeRequestList.size());
		}
		resultListMap.put("jiraIssueData", jiraIssueList);
		resultListMap.put("leadTimeConfigRepoTool", leadTimeConfigRepoTool.get());
		resultListMap.put("jiraIssueHistoryData", jiraIssueHistoryDataList); // logic 1 data
		resultListMap.put("mergeRequestData", mergeRequestList);// logic 2 data
		return resultListMap;
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiId) {
		return calculateKpiValueForDouble(valueList, kpiId);
	}

	@Override
	public Double calculateKPIMetrics(Map<String, Object> t) {
		return null;
	}

	private Map<String, List<LeadTimeChangeData>> getLastNWeek(int count) {
		Map<String, List<LeadTimeChangeData>> lastNWeek = new LinkedHashMap<>();
		LocalDate endDateTime = LocalDate.now();

		for (int i = 0; i < count; i++) {

			String currentWeekStr = DateUtil.getWeekRange(endDateTime);
			lastNWeek.put(currentWeekStr, new ArrayList<>());

			endDateTime = endDateTime.minusWeeks(1);
		}
		return lastNWeek;
	}

	private Map<String, List<LeadTimeChangeData>> getLastNMonthCount(int count) {
		Map<String, List<LeadTimeChangeData>> lastNMonth = new LinkedHashMap<>();
		LocalDateTime currentDate = LocalDateTime.now();
		String currentDateStr = currentDate.getYear() + Constant.DASH + currentDate.getMonthValue();
		lastNMonth.put(currentDateStr, new ArrayList<>());
		LocalDateTime lastMonth = LocalDateTime.now();
		for (int i = 1; i < count; i++) {
			lastMonth = lastMonth.minusMonths(1);
			String lastMonthStr = lastMonth.getYear() + Constant.DASH + lastMonth.getMonthValue();
			lastNMonth.put(lastMonthStr, new ArrayList<>());

		}
		return lastNMonth;
	}

}
