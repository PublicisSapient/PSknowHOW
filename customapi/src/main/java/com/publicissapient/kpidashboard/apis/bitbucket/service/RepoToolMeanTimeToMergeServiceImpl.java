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

package com.publicissapient.kpidashboard.apis.bitbucket.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.CustomDateRange;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.ProjectFilter;
import com.publicissapient.kpidashboard.apis.repotools.model.Branches;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolKpiMetricResponse;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolUserDetails;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolValidationData;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.Tool;
import com.publicissapient.kpidashboard.common.model.jira.Assignee;
import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.repository.jira.AssigneeDetailsRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RepoToolMeanTimeToMergeServiceImpl extends BitBucketKPIService<Double, List<Object>, Map<String, Object>> {

	private static final String ASSIGNEE = "assignee";

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private CustomApiConfig customApiConfig;

	@Autowired
	private AssigneeDetailsRepository assigneeDetailsRepository;

	@Autowired
	private KpiHelperService kpiHelperService;

	@Override
	public String getQualifierType() {
		return KPICode.REPO_TOOL_MEAN_TIME_TO_MERGE.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement, Node projectNode)
			throws ApplicationException {
		Map<String, Node> mapTmp = new HashMap<>();
		mapTmp.put(projectNode.getId(), projectNode);
		projectWiseLeafNodeValue(kpiElement, mapTmp, projectNode, kpiRequest);
		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValueMap(projectNode, nodeWiseKPIValue, KPICode.REPO_TOOL_MEAN_TIME_TO_MERGE);

		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.REPO_TOOL_MEAN_TIME_TO_MERGE);
		Map<String, Map<String, List<DataCount>>> statusTypeProjectWiseDc = new LinkedHashMap<>();
		trendValuesMap.forEach((statusType, dataCounts) -> {
			Map<String, List<DataCount>> projectWiseDc = dataCounts.stream()
					.collect(Collectors.groupingBy(DataCount::getData));
			statusTypeProjectWiseDc.put(statusType, projectWiseDc);
		});

		List<DataCountGroup> dataCountGroups = new ArrayList<>();
		statusTypeProjectWiseDc.forEach((issueType, projectWiseDc) -> {
			DataCountGroup dataCountGroup = new DataCountGroup();
			List<DataCount> dataList = new ArrayList<>();
			projectWiseDc.entrySet().forEach(trend -> dataList.addAll(trend.getValue()));
			// split for filters
			String[] issueFilter = issueType.split("#");
			dataCountGroup.setFilter1(issueFilter[0]);
			dataCountGroup.setFilter2(issueFilter[1]);
			dataCountGroup.setValue(dataList);
			dataCountGroups.add(dataCountGroup);
		});
		kpiElement.setTrendValueList(dataCountGroups);
		return kpiElement;
	}

	private void projectWiseLeafNodeValue(KpiElement kpiElement, Map<String, Node> mapTmp, Node projectLeafNode,
			KpiRequest kpiRequest) {

		String requestTrackerId = getRequestTrackerId();
		CustomDateRange dateRange = KpiDataHelper.getStartAndEndDate(kpiRequest);
		LocalDate localEndDate = dateRange.getEndDate();

		int dataPoints = kpiRequest.getXAxisDataPoints();
		String duration = kpiRequest.getDuration();

		// gets the tool configuration
		Map<ObjectId, Map<String, List<Tool>>> toolMap = configHelperService.getToolItemMap();
		ProjectFilter projectFilter = projectLeafNode.getProjectFilter();
		ObjectId projectBasicConfigId = projectFilter == null ? null : projectFilter.getBasicProjectConfigId();
		Map<String, List<Tool>> toolListMap = toolMap == null ? null : toolMap.get(projectBasicConfigId);
		List<RepoToolKpiMetricResponse> repoToolKpiMetricRespons = kpiHelperService.getRepoToolsKpiMetricResponse(
				localEndDate, kpiHelperService.getScmToolJobs(toolListMap, projectLeafNode), projectLeafNode, duration,
				dataPoints, customApiConfig.getRepoToolMeanTimeToMergeUrl());
		if (CollectionUtils.isEmpty(repoToolKpiMetricRespons)) {
			log.error("[BITBUCKET-AGGREGATED-VALUE]. No kpi data found for this project {}", projectLeafNode);
			return;
		}

		List<KPIExcelData> excelData = new ArrayList<>();
		List<Tool> reposList = kpiHelperService.populateSCMToolsRepoList(toolListMap);
		String projectName = projectLeafNode.getProjectFilter().getName();
		if (CollectionUtils.isEmpty(reposList)) {
			log.error("[BITBUCKET-AGGREGATED-VALUE]. No Jobs found for this project {}", projectLeafNode.getProjectFilter());
			return;
		}

		Map<String, List<DataCount>> aggDataMap = new LinkedHashMap<>();
		Map<String, Object> resultmap = fetchKPIDataFromDb(List.of(projectLeafNode), null, null, kpiRequest);
		Set<Assignee> assignees = (Set<Assignee>) resultmap.get(ASSIGNEE);

		LocalDate currentDate = LocalDate.now();
		Set<String> overAllUsers = repoToolKpiMetricRespons.stream().flatMap(value -> value.getUsers().stream())
				.map(RepoToolUserDetails::getEmail).collect(Collectors.toSet());
		List<RepoToolValidationData> repoToolValidationDataList = new ArrayList<>();

		for (int i = 0; i < dataPoints; i++) {

			LocalDate finalCurrentDate = currentDate;
			CustomDateRange weekRange = KpiDataHelper.getStartAndEndDateForDataFiltering(finalCurrentDate, duration);
			String date = KpiHelperService.getDateRange(weekRange, duration);
			Optional<RepoToolKpiMetricResponse> repoToolKpiMetricResponse = repoToolKpiMetricRespons.stream()
					.filter(value -> value.getDateLabel().equals(weekRange.getStartDate().toString())).findFirst();

			reposList.forEach(repo -> {
				if (!CollectionUtils.isEmpty(repo.getProcessorItemList()) &&
						repo.getProcessorItemList().get(0).getId() != null) {

					String branchName = getBranchSubFilter(repo, projectName);
					double meanTimeToMerge = 0.0d;
					String overallKpiGroup = branchName + "#" + Constant.AGGREGATED_VALUE;
					List<RepoToolUserDetails> repoToolUserDetailsList = new ArrayList<>();
					if (repoToolKpiMetricResponse.isPresent()) {
						Optional<Branches> matchingBranch = repoToolKpiMetricResponse.get().getRepositories().stream()
								.filter(repository -> repository.getName().equals(repo.getRepositoryName()))
								.flatMap(repository -> repository.getBranches().stream())
								.filter(branch -> branch.getName().equals(repo.getBranch())).findFirst();
						meanTimeToMerge = matchingBranch.map(Branches::getAverage).orElse(0.0d);
						repoToolUserDetailsList = matchingBranch.map(Branches::getUsers).orElse(new ArrayList<>());
					}
					repoToolValidationDataList.addAll(
							setUserDataCounts(overAllUsers, repoToolUserDetailsList, assignees, repo, projectName, date, aggDataMap));
					setDataCount(projectName, date, overallKpiGroup,
							KpiHelperService.convertMilliSecondsToHours(meanTimeToMerge * 1000), aggDataMap);
				}
			});

			currentDate = KpiHelperService.getNextRangeDate(duration, finalCurrentDate);
		}

		mapTmp.get(projectLeafNode.getId()).setValue(aggDataMap);
		populateExcelDataObject(requestTrackerId, repoToolValidationDataList, excelData);
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.REPO_TOOL_MEAN_TIME_TO_MERGE.getColumns());
	}

	/**
	 * set data count for user filter
	 *
	 * @param overAllUsers
	 *          list of user emails from repotool
	 * @param repoToolUserDetailsList
	 *          list of repo tool user data
	 * @param assignees
	 *          assignee data
	 * @param repo
	 *          repo tool
	 * @param projectName
	 *          project name
	 * @param date
	 *          date
	 * @param aggDataMap
	 *          total data map
	 * @return repo tool validation data
	 */
	private List<RepoToolValidationData> setUserDataCounts(Set<String> overAllUsers,
			List<RepoToolUserDetails> repoToolUserDetailsList, Set<Assignee> assignees, Tool repo, String projectName,
			String date, Map<String, List<DataCount>> aggDataMap) {
		List<RepoToolValidationData> repoToolValidationDataList = new ArrayList<>();
		overAllUsers.forEach(userEmail -> {
			Optional<RepoToolUserDetails> repoToolUserDetails = repoToolUserDetailsList.stream()
					.filter(user -> userEmail.equalsIgnoreCase(user.getEmail())).findFirst();
			Optional<Assignee> assignee = assignees.stream()
					.filter(assign -> CollectionUtils.isNotEmpty(assign.getEmail()) && assign.getEmail().contains(userEmail))
					.findFirst();
			String developerName = assignee.isPresent() ? assignee.get().getAssigneeName() : userEmail;
			Double userAverageSeconds = repoToolUserDetails.map(RepoToolUserDetails::getAverage).orElse(0.0d);
			Long userAverageHrs = KpiHelperService.convertMilliSecondsToHours(userAverageSeconds * 1000);
			String branchName = getBranchSubFilter(repo, projectName);
			String userKpiGroup = branchName + "#" + developerName;
			DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern(DateUtil.TIME_FORMAT).optionalStart()
					.appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true).optionalEnd().appendPattern("'Z'").toFormatter();
			if (repoToolUserDetails.isPresent()) {
				repoToolUserDetails.get().getMergeRequestList().forEach(mr -> {
					RepoToolValidationData repoToolValidationData = new RepoToolValidationData();
					repoToolValidationData.setProjectName(projectName);
					repoToolValidationData.setBranchName(repo.getBranch());
					repoToolValidationData.setDeveloperName(developerName);
					repoToolValidationData.setDate(date);
					repoToolValidationData.setMergeRequestComment(mr.getComments());
					repoToolValidationData.setMeanTimeToMerge(
							KpiHelperService.convertMilliSecondsToHours(mr.getTimeToMerge()*1000.0));
					repoToolValidationData.setMergeRequestUrl(mr.getLink());
					repoToolValidationData.setRepoUrl(repo.getRepositoryName());
					LocalDateTime dateTime = LocalDateTime.parse(mr.getCreatedAt(), formatter);
					repoToolValidationData
							.setPrRaisedTime(dateTime.format(DateTimeFormatter.ofPattern(DateUtil.DISPLAY_DATE_TIME_FORMAT)));
					dateTime = LocalDateTime.parse(mr.getUpdatedAt(), formatter);
					repoToolValidationData
							.setPrActivityTime(dateTime.format(DateTimeFormatter.ofPattern(DateUtil.DISPLAY_DATE_TIME_FORMAT)));
					repoToolValidationDataList.add(repoToolValidationData);
				});
			}
			setDataCount(projectName, date, userKpiGroup, userAverageHrs, aggDataMap);
		});
		return repoToolValidationDataList;
	}

	/**
	 * set individual data count
	 *
	 * @param projectName
	 *          project name
	 * @param week
	 *          date
	 * @param kpiGroup
	 *          combined filter
	 * @param value
	 *          value
	 * @param dataCountMap
	 *          data count map by filter
	 */
	private void setDataCount(String projectName, String week, String kpiGroup, Long value,
			Map<String, List<DataCount>> dataCountMap) {
		List<DataCount> dataCounts = dataCountMap.get(kpiGroup);
		Optional<DataCount> optionalDataCount = dataCounts != null
				? dataCounts.stream().filter(dataCount1 -> dataCount1.getDate().equals(week)).findFirst()
				: Optional.empty();
		if (optionalDataCount.isPresent()) {
			DataCount updatedDataCount = optionalDataCount.get();
			updatedDataCount.setValue(((Number) updatedDataCount.getValue()).longValue() + value);
			dataCounts.set(dataCounts.indexOf(optionalDataCount.get()), updatedDataCount);
		} else {
			DataCount dataCount = new DataCount();
			dataCount.setData(String.valueOf(value));
			dataCount.setSProjectName(projectName);
			dataCount.setDate(week);
			dataCount.setValue(value);
			dataCount.setKpiGroup(kpiGroup);
			dataCount.setHoverValue(new HashMap<>());
			dataCountMap.computeIfAbsent(kpiGroup, k -> new ArrayList<>()).add(dataCount);
		}
	}

	@Override
	public Double calculateKPIMetrics(Map<String, Object> stringObjectMap) {
		return null;
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		AssigneeDetails assigneeDetails = assigneeDetailsRepository
				.findByBasicProjectConfigId(leafNodeList.get(0).getProjectFilter().getBasicProjectConfigId().toString());
		Set<Assignee> assignees = assigneeDetails != null ? assigneeDetails.getAssignee() : new HashSet<>();
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put(ASSIGNEE, assignees);
		return resultMap;
	}

	private void populateExcelDataObject(String requestTrackerId, List<RepoToolValidationData> repoToolValidationDataList,
			List<KPIExcelData> validationDataMap) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			KPIExcelUtility.populateMeanTimeMergeExcelData(repoToolValidationDataList, validationDataMap);
		}
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiName) {
		return calculateKpiValueForDouble(valueList, kpiName);
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping) {
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI158(),
				KPICode.REPO_TOOL_MEAN_TIME_TO_MERGE.getKpiId());
	}
}
