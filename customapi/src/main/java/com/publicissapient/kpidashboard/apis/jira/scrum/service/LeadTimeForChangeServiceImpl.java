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

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.apis.enums.JiraFeatureHistory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.LeadTimeChangeData;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
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

	@Autowired
	private CacheService cacheService;

	private static final String JIRA_DATA = "jiraIssueData";
	private static final String JIRA_HISTORY_DATA = "jiraIssueHistoryData";
	private static final String MERGE_REQUEST_DATA = "mergeRequestData";
	private static final String LEAD_TIME_CONFIG_REPO_TOOL = "leadTimeConfigRepoTool";
	private static final String DOD_STATUS = "dodStatus";

	private static final DecimalFormat df = new DecimalFormat(".##");

	@Override
	public String getQualifierType() {
		return KPICode.LEAD_TIME_FOR_CHANGE.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();

		List<Node> projectList = treeAggregatorDetail.getMapOfListOfProjectNodes().get(HIERARCHY_LEVEL_ID_PROJECT);
		projectWiseLeafNodeValue(mapTmp, projectList, kpiElement);

		log.debug("[LEAD-TIME-CHANGE-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValue(root, nodeWiseKPIValue, KPICode.LEAD_TIME_FOR_CHANGE);
		List<DataCount> trendValues = getTrendValues(kpiRequest, nodeWiseKPIValue, KPICode.LEAD_TIME_FOR_CHANGE);
		kpiElement.setTrendValueList(trendValues);

		return kpiElement;
	}

	/**
	 *
	 * @param leafNodeList
	 * @param startDate
	 * @param endDate
	 * @param kpiRequest
	 * @return
	 */
	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {

		List<String> projectBasicConfigIdList = new ArrayList<>();
		Map<String, Object> resultListMap = new HashMap<>();
		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		Map<String, List<String>> mapOfFiltersFH = new LinkedHashMap<>();
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
		Map<String, Map<String, Object>> uniqueProjectMapFH = new HashMap<>();
		Map<String, List<String>> mergeRequestStatusList = new HashMap<>();
		Map<String, String> toBranchForMRList = new HashMap<>();
		Map<String, Boolean> projectWiseLeadTimeConfigRepoTool = new HashMap<>();
		Map<String, List<String>> projectWiseDodStatus = new HashMap<>();

		Map<String, List<String>> sortedReleaseListProjectWise = getProjectWiseSortedReleases();

		leafNodeList.forEach(leafNode -> {
			ObjectId basicProjectConfigId = leafNode.getProjectFilter().getBasicProjectConfigId();
			Map<String, Object> mapOfProjectFiltersFH = new LinkedHashMap<>();
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();

			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
			projectWiseLeadTimeConfigRepoTool.put(basicProjectConfigId.toString(),
					fieldMapping.getLeadTimeConfigRepoTool());
			if (Boolean.TRUE.equals(fieldMapping.getLeadTimeConfigRepoTool())) {
				if (Optional.ofNullable(fieldMapping.getMergeRequestStatusKPI156()).isPresent()) {
					mergeRequestStatusList.put(basicProjectConfigId.toString(),
							fieldMapping.getMergeRequestStatusKPI156());
				}
				if (Optional.ofNullable(fieldMapping.getToBranchForMRKPI156()).isPresent()) {
					toBranchForMRList.put(basicProjectConfigId.toString(), fieldMapping.getToBranchForMRKPI156());
				}
			}
			List<String> sortedReleaseList = sortedReleaseListProjectWise.getOrDefault(basicProjectConfigId.toString(),
					new ArrayList<>());
			if (CollectionUtils.isNotEmpty(sortedReleaseList)) {
				mapOfProjectFilters.put(CommonConstant.RELEASE,
						CommonUtils.convertToPatternListForSubString(sortedReleaseList));
				uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);
			}
			if (Optional.ofNullable(fieldMapping.getJiraIssueTypeKPI156()).isPresent()) {
				mapOfProjectFiltersFH.put(JiraFeatureHistory.STORY_TYPE.getFieldValueInFeature(),
						CommonUtils.convertToPatternList(fieldMapping.getJiraIssueTypeKPI156()));
			}
			if (Optional.ofNullable(fieldMapping.getJiraDodKPI156()).isPresent()) {
				mapOfProjectFiltersFH.put("statusUpdationLog.story.changedTo",
						CommonUtils.convertToPatternList(fieldMapping.getJiraDodKPI156()));
				projectWiseDodStatus.put(basicProjectConfigId.toString(), fieldMapping.getJiraDodKPI156());
			}
			uniqueProjectMapFH.put(basicProjectConfigId.toString(), mapOfProjectFiltersFH);
			projectBasicConfigIdList.add(basicProjectConfigId.toString());
		});

		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				projectBasicConfigIdList.stream().distinct().collect(Collectors.toList()));

		List<JiraIssue> jiraIssueList = jiraIssueRepository.findByRelease(mapOfFilters, uniqueProjectMap);
		if (CollectionUtils.isNotEmpty(jiraIssueList)) {
			List<String> issueIdList = jiraIssueList.stream().map(JiraIssue::getNumber).collect(Collectors.toList());

			mapOfFiltersFH.put("storyID", issueIdList);
			List<JiraIssueCustomHistory> historyDataList = jiraIssueCustomHistoryRepository
					.findFeatureCustomHistoryStoryProjectWise(mapOfFiltersFH, uniqueProjectMapFH);

			Map<String, List<MergeRequests>> projectWiseMergeRequestList = new HashMap<>();
			findMergeRequestList(mergeRequestStatusList, toBranchForMRList, projectWiseLeadTimeConfigRepoTool,
					issueIdList, projectWiseMergeRequestList);

			resultListMap.put(JIRA_DATA, jiraIssueList);
			resultListMap.put(LEAD_TIME_CONFIG_REPO_TOOL, projectWiseLeadTimeConfigRepoTool);
			resultListMap.put(JIRA_HISTORY_DATA, historyDataList); // logic 1 data
			resultListMap.put(MERGE_REQUEST_DATA, projectWiseMergeRequestList);// logic 2 data
			resultListMap.put(DOD_STATUS, projectWiseDodStatus);
		}
		return resultListMap;
	}

	/**
	 * 
	 * @param mergeRequestStatusList
	 * @param toBranchForMRList
	 * @param projectWiseLeadTimeConfigRepoTool
	 * @param issueIdList
	 * @param projectWiseMergeRequestList
	 */
	private void findMergeRequestList(Map<String, List<String>> mergeRequestStatusList,
			Map<String, String> toBranchForMRList, Map<String, Boolean> projectWiseLeadTimeConfigRepoTool,
			List<String> issueIdList, Map<String, List<MergeRequests>> projectWiseMergeRequestList) {
		projectWiseLeadTimeConfigRepoTool.forEach((projectBasicConfigId, leadTimeConfigRepoTool) -> {
			if (Boolean.TRUE.equals(leadTimeConfigRepoTool)) {
				List<String> mergeRequestStatusKPI156 = mergeRequestStatusList.get(projectBasicConfigId);
				String toBranchForMRKPI156 = toBranchForMRList.get(projectBasicConfigId);
				List<MergeRequests> mergeRequestList = mergeRequestRepository
						.findMergeRequestListBasedOnBasicProjectConfigId(new ObjectId(projectBasicConfigId),
								CommonUtils.convertTestFolderToPatternList(new ArrayList<>(issueIdList)),
								mergeRequestStatusKPI156, toBranchForMRKPI156);
				projectWiseMergeRequestList.put(projectBasicConfigId, mergeRequestList);
			}
		});
	}

	/**
	 * get latest N Released releases project wise
	 * 
	 * @return
	 */
	private Map<String, List<String>> getProjectWiseSortedReleases() {
		Map<String, List<String>> sortedReleaseListProjectWise = new HashMap<>();
		List<AccountHierarchyData> accountHierarchyDataList = (List<AccountHierarchyData>) cacheService
				.cacheAccountHierarchyData();

		Map<ObjectId, List<Node>> releaseNodeProjectWise = accountHierarchyDataList.stream()
				.flatMap(accountHierarchyData -> accountHierarchyData.getNode().stream())
				.filter(accountHierarchyNode -> accountHierarchyNode.getAccountHierarchy().getLabelName()
						.equalsIgnoreCase(CommonConstant.HIERARCHY_LEVEL_ID_RELEASE)
						&& Objects.nonNull(accountHierarchyNode.getAccountHierarchy().getReleaseState())
						&& accountHierarchyNode.getAccountHierarchy().getReleaseState()
								.equalsIgnoreCase(CommonConstant.RELEASED))
				.collect(Collectors
						.groupingBy(releaseNode -> releaseNode.getAccountHierarchy().getBasicProjectConfigId()));

		releaseNodeProjectWise.forEach((basicProjectConfigId, projectNodes) -> {
			List<String> sortedReleaseList = new ArrayList<>();
			projectNodes.stream().filter(node -> Objects.nonNull(node.getAccountHierarchy().getEndDate()))

					.sorted(Comparator.comparing(node -> node.getAccountHierarchy().getEndDate()))
					.limit(customApiConfig.getJiraXaxisMonthCount())
					.forEach(node -> sortedReleaseList.add(node.getAccountHierarchy().getNodeName().split("_")[0]));

			sortedReleaseListProjectWise.put(basicProjectConfigId.toString(), sortedReleaseList);
		});

		return sortedReleaseListProjectWise;
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

			List<JiraIssueCustomHistory> historyDataList = (List<JiraIssueCustomHistory>) resultMap
					.get(JIRA_HISTORY_DATA);
			List<JiraIssue> jiraIssueList = (List<JiraIssue>) resultMap.get(JIRA_DATA);
			Map<String, Boolean> projectWiseLeadTimeConfigRepoTool = (Map<String, Boolean>) resultMap
					.get(LEAD_TIME_CONFIG_REPO_TOOL);
			Map<String, List<MergeRequests>> projectWiseMergeRequestList = (Map<String, List<MergeRequests>>) resultMap
					.get(MERGE_REQUEST_DATA);
			Map<String, List<String>> projectWiseDodStatus = (Map<String, List<String>>) resultMap.get(DOD_STATUS);

			Map<String, List<JiraIssue>> projectWiseJiraIssueList = jiraIssueList.stream()
					.collect(Collectors.groupingBy(JiraIssue::getBasicProjectConfigId));
			Map<String, List<JiraIssueCustomHistory>> projectWiseJiraIssueHistoryDataList = historyDataList.stream()
					.collect(Collectors.groupingBy(JiraIssueCustomHistory::getBasicProjectConfigId));

			projectLeafNodeList.forEach(node -> {
				String trendLineName = node.getProjectFilter().getName();
				String basicProjectConfigId = node.getProjectFilter().getBasicProjectConfigId().toString();

				Boolean leadTimeConfigRepoTool = projectWiseLeadTimeConfigRepoTool.get(basicProjectConfigId);
				List<JiraIssueCustomHistory> jiraIssueHistoryDataList = projectWiseJiraIssueHistoryDataList
						.get(basicProjectConfigId);
				List<JiraIssue> jiraIssueDataList = projectWiseJiraIssueList.get(basicProjectConfigId);
				List<String> dodStatus = projectWiseDodStatus.get(basicProjectConfigId);

				String weekOrMonth = (String) durationFilter.getOrDefault(Constant.DURATION, CommonConstant.WEEK);
				int defaultTimeCount = 8;
				Map<String, List<LeadTimeChangeData>> leadTimeMapTimeWise = weekOrMonth.equalsIgnoreCase(
						CommonConstant.WEEK) ? getLastNWeek(defaultTimeCount) : getLastNMonthCount(defaultTimeCount);

				if (CollectionUtils.isNotEmpty(jiraIssueHistoryDataList)
						&& CollectionUtils.isNotEmpty(jiraIssueDataList)) {
					Map<String, JiraIssue> jiraIssueMap = jiraIssueDataList.stream()
							.collect(Collectors.toMap(JiraIssue::getNumber, Function.identity()));
					List<DataCount> dataCountList = new ArrayList<>();

					if (Boolean.TRUE.equals(leadTimeConfigRepoTool)) {
						List<MergeRequests> mergeRequestList = projectWiseMergeRequestList.get(basicProjectConfigId);
						findLeadTimeChangeForRepoTool(mergeRequestList, weekOrMonth, leadTimeMapTimeWise, jiraIssueMap);
					} else {
						findLeadTimeChangeForJira(jiraIssueHistoryDataList, weekOrMonth, leadTimeMapTimeWise,
								jiraIssueMap, dodStatus);
					}

					leadTimeMapTimeWise.forEach((weekOrMonthName, leadTimeListCurrentTime) -> {
						DataCount dataCount = createDataCount(trendLineName, weekOrMonthName, leadTimeListCurrentTime);
						dataCountList.add(dataCount);
					});
					populateLeadTimeExcelData(excelData, requestTrackerId, trendLineName, leadTimeConfigRepoTool,
							leadTimeMapTimeWise);

					mapTmp.get(node.getId()).setValue(dataCountList);
				}
			});
			kpiElement.setExcelData(excelData);
			kpiElement.setExcelColumns(KPIExcelColumn.LEAD_TIME_FOR_CHANGE.getColumns());
		}
	}

	private void populateLeadTimeExcelData(List<KPIExcelData> excelData, String requestTrackerId, String trendLineName,
			Boolean leadTimeConfigRepoTool, Map<String, List<LeadTimeChangeData>> leadTimeMapTimeWise) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			KPIExcelUtility.populateLeadTimeForChangeExcelData(trendLineName, leadTimeMapTimeWise, excelData,
					leadTimeConfigRepoTool);
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
			Map<String, List<LeadTimeChangeData>> leadTimeMapTimeWise, Map<String, JiraIssue> jiraIssueMap,
			List<String> dodStatus) {
		jiraIssueHistoryDataList.forEach(jiraIssueHistoryData -> {
			AtomicReference<DateTime> closedTicketDate = new AtomicReference<>();
			AtomicReference<DateTime> releaseDate = new AtomicReference<>();

			jiraIssueHistoryData.getStatusUpdationLog().stream().forEach(jiraHistoryChangeLog -> {
				if (CollectionUtils.isNotEmpty(dodStatus) && dodStatus.contains(jiraHistoryChangeLog.getChangedTo())) {
					closedTicketDate.set(DateUtil.convertLocalDateTimeToDateTime(jiraHistoryChangeLog.getUpdatedOn()));
				}
			});
			if (Objects.nonNull(jiraIssueMap.get(jiraIssueHistoryData.getStoryID())) && CollectionUtils
					.isNotEmpty(jiraIssueMap.get(jiraIssueHistoryData.getStoryID()).getReleaseVersions())) {
				List<ReleaseVersion> releaseVersionList = jiraIssueMap.get(jiraIssueHistoryData.getStoryID())
						.getReleaseVersions();
				releaseVersionList.stream().forEach(releaseVersion -> releaseDate.set(releaseVersion.getReleaseDate()));
			}

			if (closedTicketDate.get() != null && releaseDate.get() != null) {
				double leadTimeChangeInDays = getLeadTimeChangeInDays(closedTicketDate, releaseDate);

				String weekOrMonthName = getDateFormatted(weekOrMonth, releaseDate.get());

				LeadTimeChangeData leadTimeChangeData = new LeadTimeChangeData();
				leadTimeChangeData.setStoryID(jiraIssueHistoryData.getStoryID());
				leadTimeChangeData.setUrl(jiraIssueHistoryData.getUrl());
				leadTimeChangeData.setClosedDate(
						DateUtil.dateTimeConverter(closedTicketDate.get(), DateUtil.TIME_FORMAT_WITH_SEC_ZONE));
				leadTimeChangeData.setReleaseDate(
						DateUtil.dateTimeConverter(releaseDate.get(), DateUtil.TIME_FORMAT_WITH_SEC_ZONE));
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
	 * 
	 * @param closedTicketDate
	 * @param releaseDate
	 * @return
	 */
	private double getLeadTimeChangeInDays(AtomicReference<DateTime> closedTicketDate,
			AtomicReference<DateTime> releaseDate) {
		Duration duration = new Duration(closedTicketDate.get(), releaseDate.get());
		long leadTimeChange = duration.getStandardMinutes();
		double leadTimeChangeDoubleValue = (double) leadTimeChange / 60 / 24;
		String formattedValue = df.format(leadTimeChangeDoubleValue);
		return Double.parseDouble(formattedValue);
	}

	/**
	 * find lead time changes differences between merge request ticket date and
	 * release date using repo tools
	 *
	 * @param mergeRequestList
	 * @param weekOrMonth
	 * @param leadTimeMapTimeWise
	 * @param jiraIssueMap
	 */
	private void findLeadTimeChangeForRepoTool(List<MergeRequests> mergeRequestList, String weekOrMonth,
			Map<String, List<LeadTimeChangeData>> leadTimeMapTimeWise, Map<String, JiraIssue> jiraIssueMap) {
		if (CollectionUtils.isNotEmpty(mergeRequestList) && MapUtils.isNotEmpty(jiraIssueMap)) {
			mergeRequestList.stream().forEach(mergeRequests -> {
				AtomicReference<DateTime> closedTicketDate = new AtomicReference<>();
				AtomicReference<DateTime> releaseDate = new AtomicReference<>();
				LocalDateTime closedDate = DateUtil.convertMillisToLocalDateTime(mergeRequests.getClosedDate());
				closedTicketDate.set(DateUtil.convertLocalDateTimeToDateTime(closedDate));
				String fromBranch = mergeRequests.getFromBranch();
				AtomicReference<JiraIssue> matchJiraIssue = new AtomicReference<>();
				jiraIssueMap.forEach((key, jiraIssue) -> {
					String matchIssueKey = ".*" + key + ".*";
					if (fromBranch.matches(matchIssueKey)) {
						matchJiraIssue.set(jiraIssue);
					}
				});

				if (matchJiraIssue.get() != null) {
					if (CollectionUtils.isNotEmpty(matchJiraIssue.get().getReleaseVersions())) {
						matchJiraIssue.get().getReleaseVersions().stream()
								.forEach(releaseVersion -> releaseDate.set(releaseVersion.getReleaseDate()));
					}

					setLeadTimeChangeDataForJira(weekOrMonth, leadTimeMapTimeWise, mergeRequests, closedTicketDate,
							releaseDate, matchJiraIssue);
				}
			});

		}
	}

	/**
	 * 
	 * @param weekOrMonth
	 * @param leadTimeMapTimeWise
	 * @param mergeRequests
	 * @param closedTicketDate
	 * @param releaseDate
	 * @param matchJiraIssue
	 */
	private void setLeadTimeChangeDataForJira(String weekOrMonth,
			Map<String, List<LeadTimeChangeData>> leadTimeMapTimeWise, MergeRequests mergeRequests,
			AtomicReference<DateTime> closedTicketDate, AtomicReference<DateTime> releaseDate,
			AtomicReference<JiraIssue> matchJiraIssue) {
		if (closedTicketDate.get() != null && releaseDate.get() != null) {
			double leadTimeChangeInDays = getLeadTimeChangeInDays(closedTicketDate, releaseDate);

			String weekOrMonthName = getDateFormatted(weekOrMonth, releaseDate.get());

			LeadTimeChangeData leadTimeChangeData = new LeadTimeChangeData();
			leadTimeChangeData.setStoryID(matchJiraIssue.get().getNumber());
			leadTimeChangeData.setUrl(matchJiraIssue.get().getUrl());
			leadTimeChangeData.setMergeID(mergeRequests.getRevisionNumber());
			leadTimeChangeData.setFromBranch(mergeRequests.getFromBranch());
			leadTimeChangeData.setClosedDate(
					DateUtil.dateTimeConverter(closedTicketDate.get(), DateUtil.TIME_FORMAT_WITH_SEC_ZONE));
			leadTimeChangeData
					.setReleaseDate(DateUtil.dateTimeConverter(releaseDate.get(), DateUtil.TIME_FORMAT_WITH_SEC_ZONE));
			leadTimeChangeData.setLeadTime(leadTimeChangeInDays);
			leadTimeChangeData.setDate(weekOrMonthName);
			leadTimeMapTimeWise.computeIfPresent(weekOrMonthName, (key, leadTimeChangeListCurrentTime) -> {
				leadTimeChangeListCurrentTime.add(leadTimeChangeData);
				return leadTimeChangeListCurrentTime;
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
		Map<String, Object> hoverValueMap = new HashMap<>();
		hoverValueMap.put("Change lead time", days);
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
	public Double calculateKpiValue(List<Double> valueList, String kpiId) {
		return calculateKpiValueForDouble(valueList, kpiId);
	}

	@Override
	public Double calculateKPIMetrics(Map<String, Object> t) {
		return null;
	}

	/**
	 *
	 * @param count
	 * @return
	 */
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

	/**
	 *
	 * @param count
	 * @return
	 */
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
