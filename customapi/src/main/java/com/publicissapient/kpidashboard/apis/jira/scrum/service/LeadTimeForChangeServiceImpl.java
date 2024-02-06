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
import java.util.Arrays;
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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
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

	private static final String STATUS_FIELD = "statusUpdationLog.story.changedTo";

	private static final String STORY_ID = "storyID";

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
		List<DataCount> trendValues = getAggregateTrendValues(kpiRequest, kpiElement,nodeWiseKPIValue,
				KPICode.LEAD_TIME_FOR_CHANGE);
		kpiElement.setTrendValueList(trendValues);

		return kpiElement;
	}

	/**
	 * fetch data based on field mapping and project wise
	 * 
	 * @param leafNodeList
	 *            project node
	 * @param startDate
	 *            start date
	 * @param endDate
	 *            end date
	 * @param kpiRequest
	 *            kpi request
	 * @return Map<String, Object> map of object
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
		Map<String, String> toBranchForMRList = new HashMap<>();
		Map<String, String> projectWiseLeadTimeConfigRepoTool = new HashMap<>();
		Map<String, List<String>> projectWiseDodStatus = new HashMap<>();

		Map<String, List<String>> sortedReleaseListProjectWise = getProjectWiseSortedReleases();

		leafNodeList.forEach(leafNode -> {
			ObjectId basicProjectConfigId = leafNode.getProjectFilter().getBasicProjectConfigId();
			Map<String, Object> mapOfProjectFiltersFH = new LinkedHashMap<>();
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();

			FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
			projectWiseLeadTimeConfigRepoTool.put(basicProjectConfigId.toString(),
					fieldMapping.getLeadTimeConfigRepoTool());
			setFieldMappingForRepoTools(toBranchForMRList, basicProjectConfigId, fieldMapping);
			setFieldMappingOfRelease(uniqueProjectMap, sortedReleaseListProjectWise, basicProjectConfigId,
					mapOfProjectFilters);
			setFieldMappingForJira(projectWiseDodStatus, basicProjectConfigId, mapOfProjectFiltersFH, fieldMapping);
			uniqueProjectMapFH.put(basicProjectConfigId.toString(), mapOfProjectFiltersFH);
			projectBasicConfigIdList.add(basicProjectConfigId.toString());
		});

		mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
				projectBasicConfigIdList.stream().distinct().collect(Collectors.toList()));

		List<JiraIssue> jiraIssueList = jiraIssueRepository.findByRelease(mapOfFilters, uniqueProjectMap);
		if (CollectionUtils.isNotEmpty(jiraIssueList)) {
			List<String> issueIdList = jiraIssueList.stream().map(JiraIssue::getNumber).collect(Collectors.toList());

			mapOfFiltersFH.put(STORY_ID, issueIdList);
			List<JiraIssueCustomHistory> historyDataList = jiraIssueCustomHistoryRepository
					.findFeatureCustomHistoryStoryProjectWise(mapOfFiltersFH, uniqueProjectMapFH, Sort.Direction.ASC);

			Map<String, List<MergeRequests>> projectWiseMergeRequestList = new HashMap<>();
			findMergeRequestList(toBranchForMRList, projectWiseLeadTimeConfigRepoTool, issueIdList,
					projectWiseMergeRequestList);

			resultListMap.put(JIRA_DATA, jiraIssueList);
			resultListMap.put(LEAD_TIME_CONFIG_REPO_TOOL, projectWiseLeadTimeConfigRepoTool);
			resultListMap.put(JIRA_HISTORY_DATA, historyDataList); // logic 1 data
			resultListMap.put(MERGE_REQUEST_DATA, projectWiseMergeRequestList);// logic 2 data
			resultListMap.put(DOD_STATUS, projectWiseDodStatus);
		}
		return resultListMap;
	}

	/**
	 * set field maaping for history fetch data of jira
	 * 
	 * @param projectWiseDodStatus
	 *            done status
	 * @param basicProjectConfigId
	 *            basic config id
	 * @param mapOfProjectFiltersFH
	 *            db fetching object
	 * @param fieldMapping
	 *            field mapping
	 */
	private void setFieldMappingForJira(Map<String, List<String>> projectWiseDodStatus, ObjectId basicProjectConfigId,
			Map<String, Object> mapOfProjectFiltersFH, FieldMapping fieldMapping) {
		if (CollectionUtils.isNotEmpty(fieldMapping.getJiraIssueTypeKPI156())) {
			mapOfProjectFiltersFH.put(JiraFeatureHistory.STORY_TYPE.getFieldValueInFeature(),
					CommonUtils.convertToPatternList(fieldMapping.getJiraIssueTypeKPI156()));
		} else {
			List<String> defaultIssueTypes = Arrays.stream(fieldMapping.getJiraIssueTypeNames())
					.collect(Collectors.toList());
			mapOfProjectFiltersFH.put(JiraFeatureHistory.STORY_TYPE.getFieldValueInFeature(),
					CommonUtils.convertToPatternList(defaultIssueTypes));
		}
		if (CollectionUtils.isNotEmpty(fieldMapping.getJiraDodKPI156())) {
			mapOfProjectFiltersFH.put(STATUS_FIELD, CommonUtils.convertToPatternList(fieldMapping.getJiraDodKPI156()));
			projectWiseDodStatus.put(basicProjectConfigId.toString(), fieldMapping.getJiraDodKPI156());
		} else {
			List<String> defaultDODStatus = new ArrayList<>();
			defaultDODStatus.add(CommonConstant.CLOSED);
			mapOfProjectFiltersFH.put(STATUS_FIELD, CommonUtils.convertToPatternList(defaultDODStatus));
			projectWiseDodStatus.put(basicProjectConfigId.toString(), defaultDODStatus);
		}
	}

	/**
	 * set field mapping for fetch data release wise
	 * 
	 * @param uniqueProjectMap
	 *            db fetching object
	 * @param sortedReleaseListProjectWise
	 *            last N release
	 * @param basicProjectConfigId
	 *            basic config id
	 * @param mapOfProjectFilters
	 *            db fetching object
	 */
	private void setFieldMappingOfRelease(Map<String, Map<String, Object>> uniqueProjectMap,
			Map<String, List<String>> sortedReleaseListProjectWise, ObjectId basicProjectConfigId,
			Map<String, Object> mapOfProjectFilters) {
		List<String> sortedReleaseList = sortedReleaseListProjectWise.getOrDefault(basicProjectConfigId.toString(),
				new ArrayList<>());
		if (CollectionUtils.isNotEmpty(sortedReleaseList)) {
			mapOfProjectFilters.put(CommonConstant.RELEASE,
					CommonUtils.convertToPatternListForSubString(sortedReleaseList));
			uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);
		}
	}

	/**
	 * set field mapping for repo tools
	 *
	 * @param toBranchForMRList
	 *            production branch
	 * @param basicProjectConfigId
	 *            basic config id
	 * @param fieldMapping
	 *            field mapping
	 */
	private void setFieldMappingForRepoTools(Map<String, String> toBranchForMRList, ObjectId basicProjectConfigId,
			FieldMapping fieldMapping) {
		if (CommonConstant.REPO.equals(fieldMapping.getLeadTimeConfigRepoTool())
				&& Optional.ofNullable(fieldMapping.getToBranchForMRKPI156()).isPresent()) {
			toBranchForMRList.put(basicProjectConfigId.toString(), fieldMapping.getToBranchForMRKPI156());
		}
	}

	/**
	 * find merge request list if field mapping true based on basic config id and
	 * from branch , to branch
	 *
	 * @param toBranchForMRList
	 *            to branch name
	 * @param projectWiseLeadTimeConfigRepoTool
	 *            config logic of kpi
	 * @param issueIdList
	 *            jira issue ids
	 * @param projectWiseMergeRequestList
	 *            merge Request list
	 */
	private void findMergeRequestList(Map<String, String> toBranchForMRList,
			Map<String, String> projectWiseLeadTimeConfigRepoTool, List<String> issueIdList,
			Map<String, List<MergeRequests>> projectWiseMergeRequestList) {
		projectWiseLeadTimeConfigRepoTool.forEach((projectBasicConfigId, leadTimeConfigRepoTool) -> {
			if (CommonConstant.REPO.equals(leadTimeConfigRepoTool)) {
				String toBranchForMRKPI156 = toBranchForMRList.get(projectBasicConfigId);
				List<MergeRequests> mergeRequestList = mergeRequestRepository
						.findMergeRequestListBasedOnBasicProjectConfigId(new ObjectId(projectBasicConfigId),
								CommonUtils.convertTestFolderToPatternList(new ArrayList<>(issueIdList)),
								toBranchForMRKPI156);
				projectWiseMergeRequestList.put(projectBasicConfigId, mergeRequestList);
			}
		});
	}

	/**
	 * get latest N Released releases project wise
	 * 
	 * @return sorted release
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
	 *            map tmp data
	 * @param projectLeafNodeList
	 *            projectLeafNodeList
	 * @param kpiElement
	 *            kpiElement
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
			Map<String, String> projectWiseLeadTimeConfigRepoTool = (Map<String, String>) resultMap
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

				String leadTimeConfigRepoTool = projectWiseLeadTimeConfigRepoTool.get(basicProjectConfigId);
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

					if (CommonConstant.REPO.equals(leadTimeConfigRepoTool)) {
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

	/**
	 * populate excel data
	 * 
	 * @param excelData
	 *            excel data
	 * @param requestTrackerId
	 *            tracker id for cache
	 * @param trendLineName
	 *            project name
	 * @param leadTimeConfigRepoTool
	 *            config for logic of kpi
	 * @param leadTimeMapTimeWise
	 *            lead time in days
	 */
	private void populateLeadTimeExcelData(List<KPIExcelData> excelData, String requestTrackerId, String trendLineName,
			String leadTimeConfigRepoTool, Map<String, List<LeadTimeChangeData>> leadTimeMapTimeWise) {
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
	 *            history data list
	 * @param weekOrMonth
	 *            date of x axis
	 * @param leadTimeMapTimeWise
	 *            lead time in days
	 * @param jiraIssueMap
	 *            jira issues
	 */
	private void findLeadTimeChangeForJira(List<JiraIssueCustomHistory> jiraIssueHistoryDataList, String weekOrMonth,
			Map<String, List<LeadTimeChangeData>> leadTimeMapTimeWise, Map<String, JiraIssue> jiraIssueMap,
			List<String> dodStatus) {
		jiraIssueHistoryDataList.forEach(jiraIssueHistoryData -> {
			AtomicReference<DateTime> closedTicketDate = new AtomicReference<>();
			AtomicReference<DateTime> releaseDate = new AtomicReference<>();

			jiraIssueHistoryData.getStatusUpdationLog().forEach(jiraHistoryChangeLog -> {
				if (CollectionUtils.isNotEmpty(dodStatus) && dodStatus.contains(jiraHistoryChangeLog.getChangedTo())) {
					closedTicketDate.set(DateUtil.convertLocalDateTimeToDateTime(jiraHistoryChangeLog.getUpdatedOn()));
				}
			});
			if (Objects.nonNull(jiraIssueMap.get(jiraIssueHistoryData.getStoryID())) && CollectionUtils
					.isNotEmpty(jiraIssueMap.get(jiraIssueHistoryData.getStoryID()).getReleaseVersions())) {
				List<ReleaseVersion> releaseVersionList = jiraIssueMap.get(jiraIssueHistoryData.getStoryID())
						.getReleaseVersions();
				releaseVersionList.forEach(releaseVersion -> releaseDate.set(releaseVersion.getReleaseDate()));
			}

			if (closedTicketDate.get() != null && releaseDate.get() != null) {

				double leadTimeChangeInDays = KpiDataHelper.calWeekDaysExcludingWeekends(closedTicketDate.get(),
						releaseDate.get());

				String weekOrMonthName = getDateFormatted(weekOrMonth, releaseDate.get());

				setLeadTimeChangeDataListForJira(leadTimeMapTimeWise, jiraIssueHistoryData, closedTicketDate,
						releaseDate, leadTimeChangeInDays, weekOrMonthName);
			}
		});
	}

	/**
	 * find lead time changes differences between closed ticket date and release
	 * date using jira
	 * 
	 * @param leadTimeMapTimeWise
	 *            lead time list
	 * @param jiraIssueHistoryData
	 *            history data
	 * @param closedTicketDate
	 *            closed ticket date
	 * @param releaseDate
	 *            release date
	 * @param leadTimeChange
	 *            lead time
	 * @param weekOrMonthName
	 *            date
	 */

	private void setLeadTimeChangeDataListForJira(Map<String, List<LeadTimeChangeData>> leadTimeMapTimeWise,
			JiraIssueCustomHistory jiraIssueHistoryData, AtomicReference<DateTime> closedTicketDate,
			AtomicReference<DateTime> releaseDate, double leadTimeChange, String weekOrMonthName) {
		LeadTimeChangeData leadTimeChangeData = new LeadTimeChangeData();
		leadTimeChangeData.setStoryID(jiraIssueHistoryData.getStoryID());
		leadTimeChangeData.setUrl(jiraIssueHistoryData.getUrl());
		leadTimeChangeData.setClosedDate(DateUtil.dateTimeConverterUsingFromAndTo(closedTicketDate.get(),
				DateUtil.TIME_FORMAT_WITH_SEC_ZONE, DateUtil.DISPLAY_DATE_TIME_FORMAT));
		leadTimeChangeData.setReleaseDate(DateUtil.dateTimeConverterUsingFromAndTo(releaseDate.get(),
				DateUtil.TIME_FORMAT_WITH_SEC_ZONE, DateUtil.DISPLAY_DATE_TIME_FORMAT));
		String leadTimeChangeInDays = DateUtil.convertDoubleToDaysAndHoursString(leadTimeChange);
		leadTimeChangeData.setLeadTimeInDays(leadTimeChangeInDays);
		leadTimeChangeData.setLeadTime(leadTimeChange);
		leadTimeChangeData.setDate(weekOrMonthName);
		leadTimeMapTimeWise.computeIfPresent(weekOrMonthName, (key, leadTimeChangeListCurrentTime) -> {
			leadTimeChangeListCurrentTime.add(leadTimeChangeData);
			return leadTimeChangeListCurrentTime;
		});
	}

	/**
	 * find lead time changes differences between merge request ticket date and
	 * release date using repo tools
	 *
	 * @param mergeRequestList
	 *            Merge request list
	 * @param weekOrMonth
	 *            date
	 * @param leadTimeMapTimeWise
	 *            lead time in days
	 * @param jiraIssueMap
	 *            jira issues
	 */
	private void findLeadTimeChangeForRepoTool(List<MergeRequests> mergeRequestList, String weekOrMonth,
			Map<String, List<LeadTimeChangeData>> leadTimeMapTimeWise, Map<String, JiraIssue> jiraIssueMap) {
		if (CollectionUtils.isNotEmpty(mergeRequestList) && MapUtils.isNotEmpty(jiraIssueMap)) {
			mergeRequestList.forEach(mergeRequests -> {
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
						matchJiraIssue.get().getReleaseVersions()
								.forEach(releaseVersion -> releaseDate.set(releaseVersion.getReleaseDate()));
					}

					setLeadTimeChangeDataForRepo(weekOrMonth, leadTimeMapTimeWise, mergeRequests, closedTicketDate,
							releaseDate, matchJiraIssue);
				}
			});

		}
	}

	/**
	 * set lead time changes data as per repo as per merge request from branch
	 * linked with jira issue id
	 * 
	 * @param weekOrMonth
	 *            date
	 * @param leadTimeMapTimeWise
	 *            lead time
	 * @param mergeRequests
	 *            MR
	 * @param closedTicketDate
	 *            merge date
	 * @param releaseDate
	 *            release date
	 * @param matchJiraIssue
	 *            linked with branch
	 */
	private void setLeadTimeChangeDataForRepo(String weekOrMonth,
			Map<String, List<LeadTimeChangeData>> leadTimeMapTimeWise, MergeRequests mergeRequests,
			AtomicReference<DateTime> closedTicketDate, AtomicReference<DateTime> releaseDate,
			AtomicReference<JiraIssue> matchJiraIssue) {
		if (closedTicketDate.get() != null && releaseDate.get() != null) {
			double leadTimeChange = KpiDataHelper.calWeekDaysExcludingWeekends(closedTicketDate.get(),
					releaseDate.get());

			String weekOrMonthName = getDateFormatted(weekOrMonth, releaseDate.get());

			LeadTimeChangeData leadTimeChangeData = new LeadTimeChangeData();
			leadTimeChangeData.setStoryID(matchJiraIssue.get().getNumber());
			leadTimeChangeData.setUrl(matchJiraIssue.get().getUrl());
			leadTimeChangeData.setMergeID(mergeRequests.getRevisionNumber());
			leadTimeChangeData.setFromBranch(mergeRequests.getFromBranch());
			leadTimeChangeData.setClosedDate(DateUtil.dateTimeConverterUsingFromAndTo(closedTicketDate.get(),
					DateUtil.TIME_FORMAT_WITH_SEC_ZONE, DateUtil.DISPLAY_DATE_TIME_FORMAT));
			leadTimeChangeData.setReleaseDate(DateUtil.dateTimeConverterUsingFromAndTo(releaseDate.get(),
					DateUtil.TIME_FORMAT_WITH_SEC_ZONE, DateUtil.DISPLAY_DATE_TIME_FORMAT));
			String leadTimeChangeInDays = DateUtil.convertDoubleToDaysAndHoursString(leadTimeChange);
			leadTimeChangeData.setLeadTimeInDays(leadTimeChangeInDays);
			leadTimeChangeData.setLeadTime(leadTimeChange);
			leadTimeChangeData.setDate(weekOrMonthName);
			leadTimeMapTimeWise.computeIfPresent(weekOrMonthName, (key, leadTimeChangeListCurrentTime) -> {
				leadTimeChangeListCurrentTime.add(leadTimeChangeData);
				return leadTimeChangeListCurrentTime;
			});
		}
	}

	/**
	 * set data count
	 * 
	 * @param trendLineName
	 *            project name
	 * @param weekOrMonthName
	 *            date
	 * @param leadTimeListCurrentTime
	 *            lead time list
	 * @return data count
	 */
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
	public Double calculateKpiValue(List<Double> valueList, String kpiId) {
		return calculateKpiValueForDouble(valueList, kpiId);
	}

	@Override
	public Double calculateKPIMetrics(Map<String, Object> t) {
		return null;
	}

	/**
	 * get last N weeks
	 * 
	 * @param count
	 *            count
	 * @return map of list of LeadTimeChangeData
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
	 * get last N months
	 * 
	 * @param count
	 *            count
	 * @return map of list of LeadTimeChangeData
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

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping) {
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI156(), KPICode.LEAD_TIME_FOR_CHANGE.getKpiId());
	}

}
