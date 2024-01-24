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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MapUtils;
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
import com.publicissapient.kpidashboard.apis.model.DeploymentFrequencyInfo;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.DeploymentStatus;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.Deployment;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.repository.application.DeploymentRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * This service for managing DeploymentFrequency kpi for scrum.
 *
 * @author hiren babariya
 */
@Component
@Slf4j
public class DeploymentFrequencyServiceImpl extends JenkinsKPIService<Long, Long, Map<ObjectId, List<Deployment>>> {

	@Autowired
	private ConfigHelperService configHelperService;
	@Autowired
	private DeploymentRepository deploymentRepository;
	@Autowired
	private CustomApiConfig customApiConfig;

	@Override
	public Long calculateKPIMetrics(Map<ObjectId, List<Deployment>> objectIdListMap) {
		return 0L;
	}

	@Override
	public String getQualifierType() {
		return KPICode.DEPLOYMENT_FREQUENCY.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();

		List<Node> projectList = treeAggregatorDetail.getMapOfListOfProjectNodes().get(HIERARCHY_LEVEL_ID_PROJECT);
		projectWiseLeafNodeValue(mapTmp, projectList, kpiElement);

		log.debug("[DEPLOYMENT-FREQUENCY-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValueMap(root, nodeWiseKPIValue, KPICode.DEPLOYMENT_FREQUENCY);
		kpiElement.setNodeWiseKPIValue(nodeWiseKPIValue);
		Map<String, List<DataCount>> trendValuesMap = getAggregateTrendValuesMap(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.DEPLOYMENT_FREQUENCY);
		Map<String, Map<String, List<DataCount>>> envNameProjectWiseDc = new LinkedHashMap<>();
		trendValuesMap.forEach((envName, dataCounts) -> {
			Map<String, List<DataCount>> projectWiseDc = dataCounts.stream()
					.collect(Collectors.groupingBy(DataCount::getData));
			envNameProjectWiseDc.put(envName, projectWiseDc);
		});

		List<DataCountGroup> dataCountGroups = new ArrayList<>();
		envNameProjectWiseDc.forEach((envName, projectWiseDc) -> {
			DataCountGroup dataCountGroup = new DataCountGroup();
			List<DataCount> dataList = new ArrayList<>();
			projectWiseDc.entrySet().stream().forEach(trend -> dataList.addAll(trend.getValue()));
			dataCountGroup.setFilter(envName);
			dataCountGroup.setValue(dataList);
			dataCountGroups.add(dataCountGroup);
		});
		kpiElement.setTrendValueList(dataCountGroups);
		log.debug("[DEPLOYMENT-FREQUENCY-LEAF-AGGREGATED-VALUE][{}]. Aggregated Value at each level in the tree {}",
				kpiRequest.getRequestTrackerId(), root);
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

		String requestTrackerId = getRequestTrackerId();
		Map<String, Object> durationFilter = KpiDataHelper.getDurationFilter(kpiElement);
		LocalDateTime localStartDate = (LocalDateTime) durationFilter.get(Constant.DATE);
		LocalDateTime localEndDate = LocalDateTime.now();
		DateTimeFormatter formatterMonth = DateTimeFormatter.ofPattern(DateUtil.TIME_FORMAT);
		String startDate = localStartDate.format(formatterMonth);
		String endDate = localEndDate.format(formatterMonth);
		List<KPIExcelData> excelData = new ArrayList<>();
		Map<ObjectId, List<Deployment>> deploymentGroup = fetchKPIDataFromDb(projectLeafNodeList, startDate, endDate,
				null);

		if (MapUtils.isEmpty(deploymentGroup)) {
			return;
		}

		DeploymentFrequencyInfo deploymentFrequencyInfo = new DeploymentFrequencyInfo();
		projectLeafNodeList.forEach(node -> {
			Map<String, List<DataCount>> trendValueMap = new HashMap<>();
			List<DataCount> dataCountAggList = new ArrayList<>();
			String trendLineName = node.getProjectFilter().getName();
			ObjectId basicProjectConfigId = node.getProjectFilter().getBasicProjectConfigId();
			String projectNodeId = node.getProjectFilter().getId();
			String projectName = projectNodeId.substring(0, projectNodeId.lastIndexOf(CommonConstant.UNDERSCORE));
			List<Deployment> deploymentListProjectWise = deploymentGroup.get(basicProjectConfigId);

			if (CollectionUtils.isNotEmpty(deploymentListProjectWise)) {
				prepareProjectNodeValue(deploymentListProjectWise, dataCountAggList, trendValueMap, trendLineName,
						deploymentFrequencyInfo, durationFilter);
			}
			if (CollectionUtils.isEmpty(dataCountAggList)) {
				mapTmp.get(node.getId()).setValue(null);
				return;
			}
			List<DataCount> aggData = calculateAggregatedWeeksWise(KPICode.DEPLOYMENT_FREQUENCY.getKpiId(),
					dataCountAggList);
			if (CollectionUtils.isNotEmpty(aggData)) {
				trendValueMap.put(CommonConstant.OVERALL, aggData);
			}
			mapTmp.get(node.getId()).setValue(trendValueMap);

			if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
				KPIExcelUtility.populateDeploymentFrequencyExcelData(projectName, deploymentFrequencyInfo, excelData);
			}
		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.DEPLOYMENT_FREQUENCY.getColumns());

	}

	@Override
	public Map<ObjectId, List<Deployment>> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {

		Map<String, List<String>> mapOfFilters = new HashMap<>();
		List<String> statusList = new ArrayList<>();
		Set<ObjectId> projectBasicConfigIds = new HashSet<>();
		leafNodeList.forEach(node -> {
			ObjectId basicProjectConfigId = node.getProjectFilter().getBasicProjectConfigId();
			projectBasicConfigIds.add(basicProjectConfigId);
		});
		statusList.add(DeploymentStatus.SUCCESS.name());
		mapOfFilters.put("deploymentStatus", statusList);
		List<Deployment> deploymentList = deploymentRepository.findDeploymentList(mapOfFilters, projectBasicConfigIds,
				startDate, endDate);
		return deploymentList.stream()
				.collect(Collectors.groupingBy(Deployment::getBasicProjectConfigId, Collectors.toList()));
	}

	/**
	 * Method to set value at project node.
	 * 
	 * @param deploymentListProjectWise
	 * @param aggDataCountList
	 * @param trendValueMap
	 * @param trendLineName
	 * @param deploymentFrequencyInfo
	 * @param durationFilter
	 */
	private void prepareProjectNodeValue(List<Deployment> deploymentListProjectWise, List<DataCount> aggDataCountList,
			Map<String, List<DataCount>> trendValueMap, String trendLineName,
			DeploymentFrequencyInfo deploymentFrequencyInfo, Map<String, Object> durationFilter) {
		String duration = (String) durationFilter.getOrDefault(Constant.DURATION, CommonConstant.WEEK);
		int previousTimeCount = (int) durationFilter.getOrDefault(Constant.COUNT, 5);
		Map<String, List<Deployment>> deploymentMapEnvWise = deploymentListProjectWise.stream()
				.collect(Collectors.groupingBy(Deployment::getEnvName, Collectors.toList()));

		deploymentMapEnvWise.forEach((envName, deploymentListEnvWise) -> {
			if (StringUtils.isNotEmpty(envName) && CollectionUtils.isNotEmpty(deploymentListEnvWise)) {

				Map<String, List<Deployment>> deploymentMapTimeWise = duration.equalsIgnoreCase(CommonConstant.WEEK)
						? getLastNWeek(previousTimeCount)
						: getLastNMonth(previousTimeCount);
				List<DataCount> dataCountList = new ArrayList<>();

				for (Deployment deployment : deploymentListEnvWise) {
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DateUtil.TIME_FORMAT);
					LocalDateTime dateValue = LocalDateTime.parse(deployment.getStartTime(), formatter);
					String timeValue = duration.equalsIgnoreCase(CommonConstant.WEEK)
							? DateUtil.getWeekRange(dateValue.toLocalDate())
							: dateValue.getYear() + Constant.DASH + dateValue.getMonthValue();

					deploymentMapTimeWise.computeIfPresent(timeValue, (key, deploymentListCurrentTime) -> {
						deploymentListCurrentTime.add(deployment);
						return deploymentListCurrentTime;
					});
				}

				deploymentMapTimeWise.forEach((time, deploymentListCurrentTime) -> {
					DataCount dataCount = createDataCount(trendLineName, envName, time, deploymentListCurrentTime);
					dataCountList.add(dataCount);
					setDeploymentFrequencyInfoForExcel(deploymentFrequencyInfo, deploymentListCurrentTime);
				});

				aggDataCountList.addAll(dataCountList);
				trendValueMap.putIfAbsent(envName + CommonConstant.ARROW + trendLineName, new ArrayList<>());
				trendValueMap.get(envName + CommonConstant.ARROW + trendLineName).addAll(dataCountList);
			}
		});
	}

	/**
	 * Creates data count for node.
	 *
	 * @param trendLineName
	 * @param envName
	 * @param month
	 * @param deploymentListCurrentMonth
	 * @return ValidationData object
	 */
	private DataCount createDataCount(String trendLineName, String envName, String month,
			List<Deployment> deploymentListCurrentMonth) {
		Long envCount = Long.valueOf(deploymentListCurrentMonth.size());
		DataCount dataCount = new DataCount();
		dataCount.setData(String.valueOf(envCount));
		dataCount.setSProjectName(trendLineName);
		dataCount.setSSprintID(month);
		dataCount.setSSprintName(month);
		dataCount.setSprintIds(Arrays.asList(month));
		dataCount.setSprintNames(Arrays.asList(month));
		dataCount.setDate(month);
		dataCount.setKpiGroup(envName);
		dataCount.setValue(envCount);
		Map<String, Object> hoverMap = new HashMap<>();
		hoverMap.put(envName, envCount.intValue());
		dataCount.setHoverValue(hoverMap);
		return dataCount;
	}

	/**
	 * Set KPI data list for excel
	 *
	 * @param deploymentFrequencyInfo
	 * @param deploymentListCurrentMonth
	 * @return
	 */
	private void setDeploymentFrequencyInfoForExcel(DeploymentFrequencyInfo deploymentFrequencyInfo,
			List<Deployment> deploymentListCurrentMonth) {
		if (null != deploymentFrequencyInfo && CollectionUtils.isNotEmpty(deploymentListCurrentMonth)) {
			deploymentListCurrentMonth.stream().forEach(deployment -> {
				deploymentFrequencyInfo.addEnvironmentList(deployment.getEnvName());
				if (StringUtils.isNotEmpty(deployment.getJobFolderName())) {
					deploymentFrequencyInfo.addJobNameList(deployment.getJobFolderName());
				} else {
					deploymentFrequencyInfo.addJobNameList(deployment.getJobName());
				}
				deploymentFrequencyInfo.addDeploymentDateList(DateUtil.dateTimeConverter(deployment.getStartTime(),
						DateUtil.TIME_FORMAT, DateUtil.DISPLAY_DATE_FORMAT));
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DateUtil.TIME_FORMAT);
				LocalDateTime dateTime = LocalDateTime.parse(deployment.getStartTime(), formatter);
				deploymentFrequencyInfo.addMonthList(DateUtil.getWeekRange(dateTime.toLocalDate()));
			});
		}
	}

	/**
	 * prepare month list of last N(count) month
	 *
	 * @param count
	 */
	private Map<String, List<Deployment>> getLastNMonth(int count) {
		Map<String, List<Deployment>> lastNMonth = new LinkedHashMap<>();
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

	private Map<String, List<Deployment>> getLastNWeek(int count) {
		Map<String, List<Deployment>> lastNWeek = new LinkedHashMap<>();
		LocalDate endDateTime = LocalDate.now();

		for (int i = 0; i < count; i++) {

			String currentWeekStr = DateUtil.getWeekRange(endDateTime);
			lastNWeek.put(currentWeekStr, new ArrayList<>());

			endDateTime = endDateTime.minusWeeks(1);
		}
		return lastNWeek;
	}

	public List<DataCount> calculateAggregatedWeeksWise(String kpiId, List<DataCount> jobsAggregatedValueList) {

		Map<String, List<DataCount>> weeksWiseDataCount = jobsAggregatedValueList.stream()
				.collect(Collectors.groupingBy(DataCount::getDate, LinkedHashMap::new, Collectors.toList()));

		List<DataCount> aggregatedDataCount = new ArrayList<>();
		weeksWiseDataCount.forEach((date, data) -> {
			Set<String> projectNames = new HashSet<>();
			DataCount dataCount = new DataCount();
			List<Long> values = new ArrayList<>();
			Map<String, Object> hoverMap = new HashMap<>();
			for (DataCount dc : data) {
				projectNames.add(dc.getSProjectName());
				Object obj = dc.getValue();
				String keyName = dc.getKpiGroup();
				Long value = obj instanceof Long ? ((Long) obj) : 0L;
				values.add(value);
				hoverMap.put(keyName, value.intValue());
			}
			Long aggregatedValue = calculateKpiValue(values, kpiId);
			dataCount.setProjectNames(new ArrayList<>(projectNames));
			dataCount.setSSprintID(date);
			dataCount.setSSprintName(date);
			dataCount.setSprintIds(Arrays.asList(date));
			dataCount.setSprintNames(Arrays.asList(date));
			dataCount.setSProjectName(projectNames.stream().collect(Collectors.joining(" ")));
			dataCount.setValue(aggregatedValue);
			dataCount.setData(aggregatedValue.toString());
			dataCount.setDate(date);
			dataCount.setHoverValue(hoverMap);
			aggregatedDataCount.add(dataCount);
		});
		return aggregatedDataCount;
	}

	@Override
	public Long calculateKpiValue(List<Long> valueList, String kpiId) {
		return calculateKpiValueForLong(valueList, kpiId);
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping) {
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI118(), KPICode.DEPLOYMENT_FREQUENCY.getKpiId());
	}

}
