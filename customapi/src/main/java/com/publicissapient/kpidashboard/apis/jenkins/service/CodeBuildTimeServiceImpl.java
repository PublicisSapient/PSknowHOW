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

package com.publicissapient.kpidashboard.apis.jenkins.service;

import static com.publicissapient.kpidashboard.common.constant.CommonConstant.HIERARCHY_LEVEL_ID_PROJECT;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.CodeBuildTimeInfo;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.ProjectFilter;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.AggregationUtils;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.common.constant.BuildStatus;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.Tool;
import com.publicissapient.kpidashboard.common.model.application.ValidationData;
import com.publicissapient.kpidashboard.common.repository.application.BuildRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * This service for managing code build time for scrum.
 *
 * @author prigupta8
 */
@Component
@Slf4j
public class CodeBuildTimeServiceImpl extends JenkinsKPIService<Long, List<Object>, Map<ObjectId, List<Build>>> {

	private static final long DAYS_IN_WEEKS = 7;
	private final List<String> processorsList = Arrays.asList(ProcessorConstants.BAMBOO, ProcessorConstants.JENKINS,
			ProcessorConstants.TEAMCITY, ProcessorConstants.AZUREPIPELINE);
	@Autowired
	private ConfigHelperService configHelperService;
	@Autowired
	private BuildRepository buildRepository;
	@Autowired
	private CustomApiConfig customApiConfig;

	@Override
	public String getQualifierType() {
		return KPICode.CODE_BUILD_TIME.name();
	}

	@SuppressWarnings("unchecked")
	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		Map<String, List<DataCount>> trendValueMap = new HashMap<>();
		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();

		List<Node> projectList = treeAggregatorDetail.getMapOfListOfProjectNodes().get(HIERARCHY_LEVEL_ID_PROJECT);
		projectWiseLeafNodeValue(kpiElement, mapTmp, projectList, trendValueMap);

		log.debug("[CODE-BUILD-TIME-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValueMap(root, nodeWiseKPIValue, KPICode.CODE_BUILD_TIME);

		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, nodeWiseKPIValue,
				KPICode.CODE_BUILD_TIME);
		Map<String, Map<String, List<DataCount>>> issueTypeProjectWiseDc = new LinkedHashMap<>();
		trendValuesMap.forEach((issueType, dataCounts) -> {
			Map<String, List<DataCount>> projectWiseDc = dataCounts.stream()
					.collect(Collectors.groupingBy(DataCount::getData));
			issueTypeProjectWiseDc.put(issueType, projectWiseDc);
		});

		List<DataCountGroup> dataCountGroups = new ArrayList<>();
		issueTypeProjectWiseDc.forEach((issueType, projectWiseDc) -> {
			DataCountGroup dataCountGroup = new DataCountGroup();
			List<DataCount> dataList = new ArrayList<>();
			projectWiseDc.entrySet().stream().forEach(trend -> dataList.addAll(trend.getValue()));
			dataCountGroup.setFilter(issueType);
			dataCountGroup.setValue(dataList);
			dataCountGroups.add(dataCountGroup);
		});
		kpiElement.setTrendValueList(dataCountGroups);

		return kpiElement;
	}

	/**
	 * Method to set value at sprint node.
	 *
	 * @param kpiElement
	 * @param mapTmp
	 * @param projectLeafNodeList
	 * @param trendValueMap
	 */
	private void projectWiseLeafNodeValue(KpiElement kpiElement, Map<String, Node> mapTmp,
			List<Node> projectLeafNodeList, Map<String, List<DataCount>> trendValueMap) {

		String requestTrackerId = getRequestTrackerId();
		LocalDateTime localStartDate = LocalDateTime.now()
				.minusDays(customApiConfig.getJenkinsWeekCount() * DAYS_IN_WEEKS);
		LocalDateTime localEndDate = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DateUtil.TIME_FORMAT);
		String startDate = localStartDate.format(formatter);
		String endDate = localEndDate.format(formatter);

		// gets the tool configuration
		Map<ObjectId, Map<String, List<Tool>>> toolMap = configHelperService.getToolItemMap();
		Map<ObjectId, List<Build>> buildGroup = fetchKPIDataFromDb(projectLeafNodeList, startDate, endDate, null);

		if (MapUtils.isEmpty(buildGroup)) {
			return;
		}

		List<KPIExcelData> excelData = new ArrayList<>();
		projectLeafNodeList.forEach(node -> {

			String trendLineName = node.getProjectFilter().getName();
			CodeBuildTimeInfo codeBuildTimeInfo = new CodeBuildTimeInfo();
			LocalDateTime end = localEndDate;

			List<Tool> jenkinsJob = getJenkinsJobTools(toolMap, node);

			if (CollectionUtils.isEmpty(jenkinsJob)) {
				mapTmp.get(node.getId()).setValue(null);
				return;
			}

			Map<String, List<DataCount>> aggDataMap = new HashMap<>();
			List<Build> aggBuildList = new ArrayList<>();
			jenkinsJob.forEach(job -> {

				if (isValidJob(job)) {
					List<Build> buildList = buildGroup.get(job.getProcessorItemList().get(0).getId());
					if (CollectionUtils.isEmpty(buildList)) {
						return;
					}
					aggBuildList.addAll(buildList);
					String jobName;
					if (StringUtils.isNotEmpty(buildList.get(0).getJobFolder())) {
						jobName = buildList.get(0).getJobFolder() + CommonConstant.ARROW + trendLineName;
					} else {
						jobName = job.getProcessorItemList().get(0).getDesc() + CommonConstant.ARROW + trendLineName;
					}
					prepareInfoForBuild(null, end, buildList, trendLineName, trendValueMap, jobName, aggDataMap);
				}
			});
			if (CollectionUtils.isEmpty(aggBuildList)) {
				mapTmp.get(node.getId()).setValue(null);
				return;
			}
			prepareInfoForBuild(codeBuildTimeInfo, end, aggBuildList, trendLineName, trendValueMap,
					Constant.AGGREGATED_VALUE, aggDataMap);
			mapTmp.get(node.getId()).setValue(aggDataMap);

			populateValidationDataObject(requestTrackerId, excelData, trendLineName, codeBuildTimeInfo);

		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.CODE_BUILD_TIME.getColumns());
	}

	private boolean isValidJob(Tool job) {
		return !CollectionUtils.isEmpty(job.getProcessorItemList())
				&& job.getProcessorItemList().get(0).getId() != null;
	}

	/**
	 * Sets build info to holder object and duration list
	 *
	 * @param codeBuildTimeInfo
	 * @param endTime
	 * @param buildList
	 * @param trendLineName
	 */
	private void prepareInfoForBuild(CodeBuildTimeInfo codeBuildTimeInfo, LocalDateTime endTime, List<Build> buildList,
			String trendLineName, Map<String, List<DataCount>> trendValueMap, String jobName,
			Map<String, List<DataCount>> aggDataMap) {
		LocalDate endDateTime = endTime.toLocalDate();
		List<Long> valueForCurrentLeafList = new ArrayList<>();
		Map<String, Long> weekRange = new LinkedHashMap<>();
		for (int i = 0; i < customApiConfig.getJenkinsWeekCount(); i++) {
			List<Long> durationList = new ArrayList<>();
			LocalDate monday = endDateTime;
			while (monday.getDayOfWeek() != DayOfWeek.MONDAY) {
				monday = monday.minusDays(1);
			}
			LocalDate sunday = endDateTime;
			while (sunday.getDayOfWeek() != DayOfWeek.SUNDAY) {
				sunday = sunday.plusDays(1);
			}
			String date = monday + " to " + sunday;
			for (Build build : buildList) {
				if (checkDateIsInWeeks(monday, sunday, build)) {
					durationList.add(build.getDuration());
					codeBuildTimeInfo(codeBuildTimeInfo, build, date);
				}
			}

			Long valueForCurrentLeaf = AggregationUtils.averageLong(durationList);
			if (null != valueForCurrentLeaf) {
				valueForCurrentLeafList.add(valueForCurrentLeaf);
				weekRange.put(date, valueForCurrentLeaf);
			}
			weekRange.putIfAbsent(date, null);
			endDateTime = endDateTime.minusWeeks(1);

		}
		trendValueMap.putIfAbsent(jobName, new ArrayList<>());
		aggDataMap.putIfAbsent(jobName, new ArrayList<>());
		weekRange.forEach((date, valueForCurrentLeaf) -> {
			DataCount dataCount = createDataCount(trendLineName, valueForCurrentLeaf, date);
			aggDataMap.get(jobName).add(dataCount);
			trendValueMap.get(jobName).add(dataCount);
		});

	}

	/**
	 * check build date is between given weeks or not
	 *
	 * @param monday
	 * @param sunday
	 * @param build
	 * @return double
	 */
	private boolean checkDateIsInWeeks(LocalDate monday, LocalDate sunday, Build build) {
		LocalDate buildTime = Instant.ofEpochMilli(build.getStartTime()).atZone(ZoneId.systemDefault()).toLocalDate();
		return (buildTime.isAfter(monday) || buildTime.isEqual(monday))
				&& (buildTime.isBefore(sunday) || buildTime.isEqual(sunday));
	}

	/**
	 * @param codeBuildTimeInfo
	 * @param build
	 */
	private void codeBuildTimeInfo(CodeBuildTimeInfo codeBuildTimeInfo, Build build, String date) {
		if (null != codeBuildTimeInfo) {
			long minutes = TimeUnit.MILLISECONDS.toMinutes(build.getDuration());
			long seconds = TimeUnit.MILLISECONDS.toSeconds(build.getDuration());
			seconds = seconds - minutes * 60;

			if (StringUtils.isNotEmpty(build.getJobFolder())) {
				codeBuildTimeInfo.addBuidJob(build.getJobFolder());
			} else {
				codeBuildTimeInfo.addBuidJob(build.getBuildJob());
			}
			codeBuildTimeInfo.addBuildUrl(build.getBuildUrl());
			codeBuildTimeInfo.addBuildStartTime(new Date(build.getStartTime()).toString());
			codeBuildTimeInfo.addWeeks(date);
			codeBuildTimeInfo.addBuildEndTime(new Date(build.getEndTime()).toString());
			codeBuildTimeInfo.addDuration(createDurationString(minutes, seconds));
			codeBuildTimeInfo.addBuildStatus(build.getBuildStatus().toString());
			codeBuildTimeInfo.addStartedBy(build.getStartedBy());
		}
	}

	/**
	 * Creates duration string
	 *
	 * @param minutes
	 * @param seconds
	 * @return
	 */
	private String createDurationString(long minutes, long seconds) {
		return minutes == 0L ? seconds + Constant.SEC : minutes + Constant.MIN + seconds + Constant.SEC;
	}

	/**
	 * Creates validation data for node.
	 *
	 * @param codeBuildTimeInfo
	 * @return ValidationData object
	 */
	private ValidationData createValidationDataForNode(CodeBuildTimeInfo codeBuildTimeInfo) {
		ValidationData validationData = new ValidationData();
		validationData.setJobName(codeBuildTimeInfo.getBuildJobList());
		validationData.setBuildUrl(codeBuildTimeInfo.getBuildUrlList());
		validationData.setStartTime(codeBuildTimeInfo.getBuildStartTimeList());
		validationData.setEndTime(codeBuildTimeInfo.getBuildEndTimeList());
		validationData.setStartedBy(codeBuildTimeInfo.getStartedByList());
		validationData.setWeeksList(codeBuildTimeInfo.getWeeksList());
		validationData.setBuildStatus(codeBuildTimeInfo.getBuildStatusList());
		validationData.setDuration(codeBuildTimeInfo.getDurationList());
		return validationData;
	}

	/**
	 * Set data to display on trend line.
	 *
	 * @param trendLineName
	 * @param valueForCurrentLeaf
	 * @param date
	 * @return
	 */
	private DataCount createDataCount(String trendLineName, Long valueForCurrentLeaf, String date) {
		DataCount dataCount = new DataCount();
		dataCount.setData(String
				.valueOf(valueForCurrentLeaf == null ? 0L : TimeUnit.MILLISECONDS.toMinutes(valueForCurrentLeaf)));
		dataCount.setSProjectName(trendLineName);
		dataCount.setDate(date);
		dataCount.setHoverValue(new HashMap<>());
		dataCount.setValue(valueForCurrentLeaf == null ? 0L : TimeUnit.MILLISECONDS.toMinutes(valueForCurrentLeaf));

		if (valueForCurrentLeaf == null) {
			dataCount.setPriority("0 sec");
		} else {
			long minutes = TimeUnit.MILLISECONDS.toMinutes(valueForCurrentLeaf);
			long seconds = TimeUnit.MILLISECONDS.toSeconds(valueForCurrentLeaf);
			dataCount.setPriority(minutes + Constant.MIN + seconds + Constant.SEC);
		}
		return dataCount;
	}

	/**
	 * Get tool config entry for Jenkins.
	 *
	 * @param toolMap
	 * @param node
	 * @return
	 */
	private List<Tool> getJenkinsJobTools(Map<ObjectId, Map<String, List<Tool>>> toolMap, Node node) {

		ProjectFilter projectFilter = node.getProjectFilter();
		ObjectId objectId = projectFilter == null ? null : projectFilter.getBasicProjectConfigId();

		List<Tool> jenkinsJob = new ArrayList<>();
		if (toolMap.containsKey(objectId)) {
			jenkinsJob = getProcessorItemList(toolMap, objectId);
		}

		if (CollectionUtils.isEmpty(jenkinsJob)) {
			log.error("[JENKINS-AGGREGATED-VALUE]. No Jobs found for this project {}", node.getProjectFilter());
		}

		return jenkinsJob;
	}

	@Override
	public Long calculateKPIMetrics(Map<ObjectId, List<Build>> builds) {
		return null;
	}

	@Override
	public Map<ObjectId, List<Build>> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		// gets the tool configuration
		Map<ObjectId, Map<String, List<Tool>>> toolMap = configHelperService.getToolItemMap();
		Set<ObjectId> processorItemIdList = new HashSet<>();
		List<String> statusList = new ArrayList<>();
		Map<String, List<String>> mapOfFilters = new HashMap<>();
		leafNodeList.forEach(node -> {
			ObjectId id = node.getProjectFilter().getBasicProjectConfigId();
			if (toolMap.get(id) == null) {
				return;
			}
			List<Tool> allProcessorItems = getProcessorItemList(toolMap, id);
			if (CollectionUtils.isEmpty(allProcessorItems)) {
				return;
			}

			allProcessorItems.forEach(job -> {
				if (isValidJob(job)) {
					processorItemIdList.addAll(prepareProcessorItemIdsList(job));
				}
			});

		});
		if (CollectionUtils.isEmpty(processorItemIdList)) {
			return new HashMap<>();
		}
		statusList.add(BuildStatus.SUCCESS.name());
		mapOfFilters.put("buildStatus", statusList);
		List<Build> buildList = buildRepository.findBuildList(mapOfFilters, processorItemIdList, startDate, endDate);
		return buildList.stream().collect(Collectors.groupingBy(Build::getProcessorItemId, Collectors.toList()));
	}

	/**
	 * Populates data for validation.
	 *
	 * @param requestTrackerId
	 * @param excelData
	 * @param projectName
	 * @param codeBuildTimeInfo
	 */
	private void populateValidationDataObject(String requestTrackerId, List<KPIExcelData> excelData, String projectName,
			CodeBuildTimeInfo codeBuildTimeInfo) {

		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			KPIExcelUtility.populateCodeBuildTime(excelData, projectName, codeBuildTimeInfo);

		}
	}

	/**
	 * returns list of all the tools
	 *
	 * @param toolMap
	 * @param id
	 * @return
	 */
	private List<Tool> getProcessorItemList(Map<ObjectId, Map<String, List<Tool>>> toolMap, ObjectId id) {
		List<Tool> allProcessorItems = new ArrayList<>();

		for (String processor : processorsList) {
			if (toolMap.get(id).containsKey(processor)) {
				List<Tool> processorItems = toolMap.get(id).get(processor);
				allProcessorItems.addAll(processorItems);
			}
		}
		return allProcessorItems;
	}

	/**
	 * prepare processorIds list
	 *
	 * @param job
	 * @return processorIds
	 */
	private List<ObjectId> prepareProcessorItemIdsList(Tool job) {
		List<ObjectId> processorIds = new ArrayList<>();
		job.getProcessorItemList().forEach(e -> processorIds.add(e.getId()));
		return processorIds;
	}

	@Override
	public Long calculateKpiValue(List<Long> valueList, String kpiId) {
		return calculateKpiValueForLong(valueList, kpiId);
	}
}
