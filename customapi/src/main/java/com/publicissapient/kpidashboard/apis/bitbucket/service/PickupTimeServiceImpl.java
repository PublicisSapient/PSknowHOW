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
import com.publicissapient.kpidashboard.apis.repotools.model.MergeRequests;
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
public class PickupTimeServiceImpl extends BitBucketKPIService<Double, List<Object>, Map<String, Object>> {

	public static final String MR_COUNT = "No of PRs";
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
		return KPICode.PICKUP_TIME.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement, Node projectNode)
			throws ApplicationException {

		Map<String, Node> mapTmp = new HashMap<>();
		mapTmp.put(projectNode.getId(), projectNode);
		projectWiseLeafNodeValue(kpiElement, mapTmp, projectNode, kpiRequest);
		log.debug("[PROJECT-WISE][{}]. Values of leaf node after KPI calculation {}", kpiRequest.getRequestTrackerId(),
				projectNode);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValueMap(projectNode, nodeWiseKPIValue, KPICode.PICKUP_TIME);

		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.PICKUP_TIME);
		Map<String, List<DataCount>> unsortedMap = trendValuesMap.entrySet().stream().sorted(Map.Entry.comparingByKey())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
		Map<String, Map<String, List<DataCount>>> statusTypeProjectWiseDc = new LinkedHashMap<>();
		unsortedMap.forEach((statusType, dataCounts) -> {
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

	/**
	 * Populates KPI value to project leaf nodes. It also gives the trend analysis
	 * project wise.
	 *
	 * @param kpiElement
	 *          kpi element
	 * @param mapTmp
	 *          node map
	 * @param projectLeafNode
	 *          leaf node of project
	 * @param kpiRequest
	 *          kpi request
	 */
	@SuppressWarnings("unchecked")
	private void projectWiseLeafNodeValue(KpiElement kpiElement, Map<String, Node> mapTmp, Node projectLeafNode,
			KpiRequest kpiRequest) {

		CustomDateRange dateRange = KpiDataHelper.getStartAndEndDate(kpiRequest);
		String requestTrackerId = getRequestTrackerId();
		LocalDate localEndDate = dateRange.getEndDate();

		int dataPoints = kpiRequest.getXAxisDataPoints();
		String duration = kpiRequest.getDuration();

		// gets the tool configuration
		Map<ObjectId, Map<String, List<Tool>>> toolMap = configHelperService.getToolItemMap();
		ProjectFilter projectFilter = projectLeafNode.getProjectFilter();
		ObjectId projectBasicConfigId = projectFilter == null ? null : projectFilter.getBasicProjectConfigId();
		Map<String, List<Tool>> toolListMap = toolMap == null ? null : toolMap.get(projectBasicConfigId);
		List<RepoToolKpiMetricResponse> repoToolKpiMetricResponseList = kpiHelperService.getRepoToolsKpiMetricResponse(
				localEndDate, kpiHelperService.getScmToolJobs(toolListMap, projectLeafNode), projectLeafNode, duration,
				dataPoints, customApiConfig.getRepoToolPickupTimeUrl());
		if (CollectionUtils.isEmpty(repoToolKpiMetricResponseList)) {
			log.error("[BITBUCKET-AGGREGATED-VALUE]. No kpi data found for this project {}", projectLeafNode);
			return;
		}

		List<KPIExcelData> excelData = new ArrayList<>();
		List<Tool> reposList = kpiHelperService.populateSCMToolsRepoList(toolListMap);
		if (CollectionUtils.isEmpty(reposList)) {
			log.error("[BITBUCKET-AGGREGATED-VALUE]. No Jobs found for this project {}", projectLeafNode.getProjectFilter());
			return;
		}

		String projectName = projectLeafNode.getProjectFilter().getName();
		Map<String, List<DataCount>> aggDataMap = new HashMap<>();
		Map<String, Object> resultmap = fetchKPIDataFromDb(List.of(projectLeafNode), null, null, kpiRequest);
		Set<Assignee> assignees = (Set<Assignee>) resultmap.get(ASSIGNEE);
		LocalDate currentDate = LocalDate.now();
		Set<String> overAllUsers = repoToolKpiMetricResponseList.stream().flatMap(value -> value.getUsers().stream())
				.map(RepoToolUserDetails::getEmail).collect(Collectors.toSet());
		List<RepoToolValidationData> repoToolValidationDataList = new ArrayList<>();

		for (int i = 0; i < dataPoints; i++) {

			LocalDate finalCurrentDate = currentDate;
			CustomDateRange weekRange = KpiDataHelper.getStartAndEndDateForDataFiltering(finalCurrentDate, duration);
			String date = KpiHelperService.getDateRange(weekRange, duration);

			Optional<RepoToolKpiMetricResponse> repoToolKpiMetricResponse = repoToolKpiMetricResponseList.stream()
					.filter(value -> value.getDateLabel().equals(weekRange.getStartDate().toString())).findFirst();

			reposList.forEach(repo -> {
				if (!CollectionUtils.isEmpty(repo.getProcessorItemList()) &&
						repo.getProcessorItemList().get(0).getId() != null) {
					List<RepoToolUserDetails> repoToolUserDetailsList = new ArrayList<>();
					String branchName = getBranchSubFilter(repo, projectName);
					Double pickupTime = 0.0d;
					long mrCount = 0L;
					String overallKpiGroup = branchName + "#" + Constant.AGGREGATED_VALUE;
					if (repoToolKpiMetricResponse.isPresent()) {
						Optional<Branches> matchingBranch = repoToolKpiMetricResponse.get().getRepositories().stream()
								.filter(repository -> repository.getName().equals(repo.getRepositoryName()))
								.flatMap(repository -> repository.getBranches().stream())
								.filter(branch -> branch.getName().equals(repo.getBranch())).findFirst();

						pickupTime = matchingBranch.map(Branches::getHours).orElse(0.0d);
						mrCount = matchingBranch.map(Branches::getMergeRequestsPT).map(Map::size).orElse(0);
						repoToolUserDetailsList = matchingBranch.map(Branches::getUsers).orElse(new ArrayList<>());
					}
					repoToolValidationDataList.addAll(
							setUserDataCounts(overAllUsers, repoToolUserDetailsList, assignees, repo, projectName, date, aggDataMap));
					setDataCount(projectName, date, overallKpiGroup, pickupTime, mrCount, aggDataMap);
				}
			});

			currentDate = KpiHelperService.getNextRangeDate(duration, currentDate);
		}
		mapTmp.get(projectLeafNode.getId()).setValue(aggDataMap);
		populateExcelDataObject(requestTrackerId, repoToolValidationDataList, excelData);
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.PICKUP_TIME.getColumns());
	}

	/**
	 * fetch data from db
	 *
	 * @param leafNodeList
	 *          leaf node list
	 * @param startDate
	 *          start date
	 * @param endDate
	 *          end date
	 * @param kpiRequest
	 *          kpi request
	 * @return map of data
	 */
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
	 *          repo tool item
	 * @param projectName
	 *          project name
	 * @param date
	 *          date
	 * @param dateUserWiseAverage
	 *          total data map
	 * @return repotool validation data
	 */
	private List<RepoToolValidationData> setUserDataCounts(Set<String> overAllUsers,
			List<RepoToolUserDetails> repoToolUserDetailsList, Set<Assignee> assignees, Tool repo, String projectName,
			String date, Map<String, List<DataCount>> dateUserWiseAverage) {
		List<RepoToolValidationData> repoToolValidationDataList = new ArrayList<>();
		overAllUsers.forEach(userEmail -> {
			Optional<RepoToolUserDetails> repoToolUserDetails = repoToolUserDetailsList.stream()
					.filter(user -> userEmail.equalsIgnoreCase(user.getEmail())).findFirst();
			Optional<Assignee> assignee = assignees.stream()
					.filter(assign -> CollectionUtils.isNotEmpty(assign.getEmail()) && assign.getEmail().contains(userEmail))
					.findFirst();
			String developerName = assignee.isPresent() ? assignee.get().getAssigneeName() : userEmail;
			Double userHours = repoToolUserDetails.map(RepoToolUserDetails::getHours).orElse(0.0d);
			long userMrCount = repoToolUserDetails.map(repoToolUserDetails1 -> {
				List<MergeRequests> mergeRequestsPT = Optional.ofNullable(repoToolUserDetails1.getMergeRequestList())
						.orElse(new ArrayList<>()).stream().filter(mergeRequests -> mergeRequests.getUpdatedAt() != null).toList();
				return mergeRequestsPT.size();
			}).orElse(0);

			String branchName = getBranchSubFilter(repo, projectName);
			String userKpiGroup = branchName + "#" + developerName;
			DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern(DateUtil.TIME_FORMAT).optionalStart()
					.appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true).optionalEnd().appendPattern("'Z'").toFormatter();
			if (repoToolUserDetails.isPresent()) {
				repoToolUserDetails.get().getMergeRequestList().forEach(mr -> {
					RepoToolValidationData repoToolValidationData = new RepoToolValidationData();
					repoToolValidationData.setProjectName(projectName);
					repoToolValidationData.setBranchName(repo.getBranch());
					repoToolValidationData.setRepoUrl(repo.getRepositoryName());
					repoToolValidationData.setMergeRequestUrl(mr.getLink());
					repoToolValidationData.setDeveloperName(developerName);
					repoToolValidationData.setDate(date);
					repoToolValidationData.setPrStatus(mr.getMrState());
					LocalDateTime dateTime = LocalDateTime.parse(mr.getCreatedAt(), formatter);
					repoToolValidationData
							.setPrRaisedTime(dateTime.format(DateTimeFormatter.ofPattern(DateUtil.DISPLAY_DATE_TIME_FORMAT)));
					if (mr.getUpdatedAt() != null) {
						dateTime = LocalDateTime.parse(mr.getUpdatedAt(), formatter);
						repoToolValidationData
								.setPrActivityTime(dateTime.format(DateTimeFormatter.ofPattern(DateUtil.DISPLAY_DATE_TIME_FORMAT)));
						repoToolValidationData.setPickupTime(mr.getFirstReviewedAt());
					}

					repoToolValidationDataList.add(repoToolValidationData);
				});
			}
			setDataCount(projectName, date, userKpiGroup, userHours, userMrCount, dateUserWiseAverage);
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
	private void setDataCount(String projectName, String week, String kpiGroup, Double value, Long mrCount,
			Map<String, List<DataCount>> dataCountMap) {
		List<DataCount> dataCounts = dataCountMap.get(kpiGroup);
		Optional<DataCount> optionalDataCount = dataCounts != null
				? dataCounts.stream().filter(dataCount1 -> dataCount1.getDate().equals(week)).findFirst()
				: Optional.empty();
		if (optionalDataCount.isPresent()) {
			DataCount updatedDataCount = optionalDataCount.get();
			updatedDataCount.getHoverValue().put(MR_COUNT,
					((Number) updatedDataCount.getHoverValue().get(MR_COUNT)).longValue() + mrCount);
			updatedDataCount.setValue(((Number) updatedDataCount.getValue()).longValue() + value);
			dataCounts.set(dataCounts.indexOf(optionalDataCount.get()), updatedDataCount);
		} else {
			DataCount dataCount = new DataCount();
			dataCount.setData(String.valueOf(value));
			dataCount.setSProjectName(projectName);
			dataCount.setDate(week);
			dataCount.setValue(value);
			dataCount.setKpiGroup(kpiGroup);
			Map<String, Object> hoverValues = new HashMap<>();
			hoverValues.put(MR_COUNT, mrCount);
			dataCount.setHoverValue(hoverValues);
			dataCountMap.computeIfAbsent(kpiGroup, k -> new ArrayList<>()).add(dataCount);
		}
	}

	/**
	 * populate excel data
	 *
	 * @param requestTrackerId
	 *          request tracker id
	 * @param repoToolValidationDataList
	 *          repo tool validation data
	 * @param validationDataMap
	 *          excel data map
	 */
	private void populateExcelDataObject(String requestTrackerId, List<RepoToolValidationData> repoToolValidationDataList,
			List<KPIExcelData> validationDataMap) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {

			KPIExcelUtility.populatePickupTimeExcelData(repoToolValidationDataList, validationDataMap);
		}
	}

	@Override
	public Double calculateKPIMetrics(Map<String, Object> stringObjectMap) {
		return null;
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiName) {
		return calculateKpiValueForDouble(valueList, kpiName);
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping) {
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI160(), KPICode.PICKUP_TIME.getKpiId());
	}
}
