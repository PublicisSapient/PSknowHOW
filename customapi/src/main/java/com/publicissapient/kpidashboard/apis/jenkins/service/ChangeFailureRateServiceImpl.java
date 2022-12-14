package com.publicissapient.kpidashboard.apis.jenkins.service;

import static com.publicissapient.kpidashboard.common.constant.CommonConstant.HIERARCHY_LEVEL_ID_PROJECT;

import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.ChangeFailureRateInfo;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.ProjectFilter;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.common.constant.BuildStatus;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.Tool;
import com.publicissapient.kpidashboard.common.repository.application.BuildRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * This service for managing change failure rate kpi for scrum.
 *
 * @author hiren babariya
 */
@Component
@Slf4j
public class ChangeFailureRateServiceImpl extends JenkinsKPIService<Double, List<Object>, Map<ObjectId, List<Build>>> {

	private static final DecimalFormat decimalFormat = new DecimalFormat("#0.00");
	private static final long DAYS_IN_WEEKS = 7;
	private static final String TOTAL_CHANGES = "Total number of Changes";
	private static final String FAILED_CHANGES = "Failed Changes";
	private final List<String> processorsList = Arrays.asList(ProcessorConstants.BAMBOO, ProcessorConstants.JENKINS,
			ProcessorConstants.TEAMCITY, ProcessorConstants.AZUREPIPELINE);
	@Autowired
	private ConfigHelperService configHelperService;
	@Autowired
	private BuildRepository buildRepository;
	@Autowired
	private CustomApiConfig customApiConfig;

	@Override
	public Double calculateKPIMetrics(Map<ObjectId, List<Build>> objectIdListMap) {
		return null;
	}

	@Override
	public String getQualifierType() {
		return KPICode.CHANGE_FAILURE_RATE.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();

		List<Node> projectList = treeAggregatorDetail.getMapOfListOfProjectNodes().get(HIERARCHY_LEVEL_ID_PROJECT);
		projectWiseLeafNodeValue(kpiElement, mapTmp, projectList);

		log.debug("[CHANGE-FAILURE-RATE-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValueMap(root, nodeWiseKPIValue, KPICode.CHANGE_FAILURE_RATE);
		kpiElement.setNodeWiseKPIValue(nodeWiseKPIValue);
		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, nodeWiseKPIValue,
				KPICode.CHANGE_FAILURE_RATE);
		Map<String, Map<String, List<DataCount>>> jobNameKeyProjectWiseDc = new LinkedHashMap<>();
		trendValuesMap.forEach((issueType, dataCounts) -> {
			Map<String, List<DataCount>> projectWiseDc = dataCounts.stream()
					.collect(Collectors.groupingBy(DataCount::getData));
			jobNameKeyProjectWiseDc.put(issueType, projectWiseDc);
		});

		List<DataCountGroup> dataCountGroups = new ArrayList<>();
		jobNameKeyProjectWiseDc.forEach((issueType, projectWiseDc) -> {
			DataCountGroup dataCountGroup = new DataCountGroup();
			List<DataCount> dataList = new ArrayList<>();
			projectWiseDc.entrySet().stream().forEach(trend -> dataList.addAll(trend.getValue()));
			dataCountGroup.setFilter(issueType);
			dataCountGroup.setValue(dataList);
			dataCountGroups.add(dataCountGroup);
		});
		kpiElement.setTrendValueList(dataCountGroups);

		log.debug("[CHANGE-FAILURE-RATE-AGGREGATED-VALUE][{}]. Aggregated Value at each level in the tree {}",
				kpiRequest.getRequestTrackerId(), root);
		return kpiElement;
	}

	@Override
	public Map<ObjectId, List<Build>> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		// gets the tool configuration
		Map<ObjectId, Map<String, List<Tool>>> toolMap = configHelperService.getToolItemMap();
		Set<ObjectId> processorItemIdList = new HashSet<>();
		List<String> statusListForTotalBuildCount = new ArrayList<>();
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
		statusListForTotalBuildCount.add(BuildStatus.SUCCESS.name());
		statusListForTotalBuildCount.add(BuildStatus.FAILURE.name());
		mapOfFilters.put("buildStatus", statusListForTotalBuildCount);
		List<Build> buildList = buildRepository.findBuildList(mapOfFilters, processorItemIdList, startDate, endDate);
		return buildList.stream().collect(Collectors.groupingBy(Build::getProcessorItemId, Collectors.toList()));
	}

	/**
	 * Method to set value at projects wise life node.
	 *
	 * @param kpiElement
	 * @param mapTmp
	 * @param projectLeafNodeList
	 *            // * @param trendValueMap
	 */
	private void projectWiseLeafNodeValue(KpiElement kpiElement, Map<String, Node> mapTmp,
			List<Node> projectLeafNodeList) {

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
			Map<String, List<DataCount>> trendValueMap = new HashMap<>();
			String trendLineName = node.getProjectFilter().getName();
			ChangeFailureRateInfo changeFailureRateInfo = new ChangeFailureRateInfo();
			LocalDateTime end = localEndDate;

			List<Tool> jenkinsJob = getJenkinsJobTools(toolMap, node);

			if (CollectionUtils.isEmpty(jenkinsJob)) {
				mapTmp.get(node.getId()).setValue(null);
				return;
			}
			List<DataCount> dataCountAggList = new ArrayList<>();
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
						jobName = buildList.get(0).getJobFolder();
					} else {
						jobName = job.getProcessorItemList().get(0).getDesc();
					}
					prepareInfoForBuild(changeFailureRateInfo, end, buildList, trendLineName, trendValueMap, jobName,
							dataCountAggList);
				}
			});
			if (CollectionUtils.isEmpty(aggBuildList)) {
				mapTmp.get(node.getId()).setValue(null);
				return;
			}
			List<DataCount> aggData = calculateAggregatedWeeksWise(KPICode.CHANGE_FAILURE_RATE.getKpiId(),
					dataCountAggList);
			if (CollectionUtils.isNotEmpty(aggData)) {
				trendValueMap.put(CommonConstant.OVERALL, aggData);
			}
			mapTmp.get(node.getId()).setValue(trendValueMap);
			populateExcelDataObject(requestTrackerId, excelData, trendLineName, changeFailureRateInfo);

		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.CHANGE_FAILURE_RATE.getColumns());
	}

	private void populateExcelDataObject(String requestTrackerId, List<KPIExcelData> excelData, String trendLineName,
			ChangeFailureRateInfo changeFailureRateInfo) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			KPIExcelUtility.populateChangeFailureRateExcelData(trendLineName, changeFailureRateInfo, excelData);
		}
	}

	/**
	 * Get tool config entry for Jenkins , bamboo , azure pipeline
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
			log.error("[JENKINS-AGGREGATED-VALUE]. No Jobs found for this project {}", node.getAccountHierarchy());
		}

		return jenkinsJob;
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

	private boolean isValidJob(Tool job) {
		return !CollectionUtils.isEmpty(job.getProcessorItemList())
				&& job.getProcessorItemList().get(0).getId() != null;
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

	/**
	 * Sets build info to holder object and duration list
	 *
	 * @param changeFailureRateInfo
	 * @param endTime
	 * @param buildList
	 * @param trendLineName
	 * @param trendValueMap
	 * @param jobName
	 * @param dataCountAggList
	 */
	private void prepareInfoForBuild(ChangeFailureRateInfo changeFailureRateInfo, LocalDateTime endTime,
			List<Build> buildList, String trendLineName, Map<String, List<DataCount>> trendValueMap, String jobName,
			List<DataCount> dataCountAggList) {
		LocalDate endDateTime = endTime.toLocalDate();
		List<DataCount> dataCountList = new ArrayList<>();
		for (int i = 0; i < customApiConfig.getJenkinsWeekCount(); i++) {
			Double failureBuildCount = 0.0d;
			Double buildFailurePercentage = 0.0d;
			Double totalBuildCount = 0.0d;
			LocalDate monday = endDateTime;
			while (monday.getDayOfWeek() != DayOfWeek.MONDAY) {
				monday = monday.minusDays(1);
			}
			LocalDate sunday = endDateTime;
			while (sunday.getDayOfWeek() != DayOfWeek.SUNDAY) {
				sunday = sunday.plusDays(1);
			}
			for (Build build : buildList) {
				if (checkDateIsInWeeks(monday, sunday, build)) {
					failureBuildCount = getFailureBuildCount(failureBuildCount, build);
					totalBuildCount = getTotalBuildCount(totalBuildCount, build);
				}
			}
			if (totalBuildCount > 0 && failureBuildCount > 0) {
				buildFailurePercentage = Double
						.parseDouble(decimalFormat.format(failureBuildCount / totalBuildCount * 100));
			}
			String date = monday + " to " + sunday;
			DataCount dataCount = createDataCount(trendLineName, buildFailurePercentage, date,
					totalBuildCount.intValue(), failureBuildCount.intValue(), jobName);
			setChangeFailureRateInfoForExcel(changeFailureRateInfo, jobName, totalBuildCount, failureBuildCount,
					buildFailurePercentage, date);
			dataCountList.add(dataCount);
			endDateTime = endDateTime.minusWeeks(1);
		}
		trendValueMap.putIfAbsent(jobName + CommonConstant.ARROW + trendLineName, new ArrayList<>());
		trendValueMap.get(jobName + CommonConstant.ARROW + trendLineName).addAll(dataCountList);
		dataCountAggList.addAll(dataCountList);
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
	 * count total build of given week
	 *
	 * @param totalBuildCount
	 * @param build
	 * @return double
	 */
	private Double getTotalBuildCount(Double totalBuildCount, Build build) {
		if (build.getBuildStatus().equals(BuildStatus.FAILURE) || build.getBuildStatus().equals(BuildStatus.SUCCESS)) {
			totalBuildCount++;
		}
		return totalBuildCount;
	}

	/**
	 * count failure build of given week
	 *
	 * @param failureBuildCount
	 * @param build
	 * @return double
	 */
	private Double getFailureBuildCount(Double failureBuildCount, Build build) {
		if (build.getBuildStatus().equals(BuildStatus.FAILURE)) {
			failureBuildCount++;
		}
		return failureBuildCount;
	}

	/**
	 * Set KPI data in excel list
	 *
	 * @param changeFailureRateInfo
	 * @param jobName
	 * @param totalBuildCount
	 * @param failureBuildCount
	 * @param buildFailurePercentage
	 * @param date
	 * @return
	 */

	private void setChangeFailureRateInfoForExcel(ChangeFailureRateInfo changeFailureRateInfo, String jobName,
			Double totalBuildCount, Double failureBuildCount, Double buildFailurePercentage, String date) {
		if (null != changeFailureRateInfo) {
			changeFailureRateInfo.addBuildJobNameList(jobName);
			changeFailureRateInfo.addTotalBuildCountList(totalBuildCount.intValue());
			changeFailureRateInfo.addTotalBuildFailureCountList(failureBuildCount.intValue());
			changeFailureRateInfo.addBuildFailurePercentageList(buildFailurePercentage);
			changeFailureRateInfo.addDateList(date);
		}
	}

	/**
	 * Set data to display on trend line.
	 *
	 * @param trendLineName
	 * @param valueForCurrentLeaf
	 * @param date
	 * @param totalCount
	 * @param failureCount
	 * @return dataCount
	 */
	private DataCount createDataCount(String trendLineName, Double valueForCurrentLeaf, String date, Integer totalCount,
			Integer failureCount, String jobName) {
		DataCount dataCount = new DataCount();
		dataCount.setData(valueForCurrentLeaf.toString());
		dataCount.setSProjectName(trendLineName);
		dataCount.setKpiGroup(jobName);
		dataCount.setDate(date);
		dataCount.setSSprintID(date);
		dataCount.setSSprintName(date);
		dataCount.setSprintIds(Arrays.asList(date));
		dataCount.setSprintNames(Arrays.asList(date));
		dataCount.setCount(totalCount);
		dataCount.setValue(valueForCurrentLeaf);
		Map<String, Integer> hoverMap = new HashMap<>();
		hoverMap.put(FAILED_CHANGES, failureCount);
		hoverMap.put(TOTAL_CHANGES, totalCount);
		dataCount.setHoverValue(hoverMap);
		return dataCount;
	}

	/**
	 * calculate aggregate values by weeks wise of all jobs dataCount list
	 *
	 * @param kpiId
	 * @param jobsAggregatedValueList
	 * @return list of DataCount
	 */

	public List<DataCount> calculateAggregatedWeeksWise(String kpiId, List<DataCount> jobsAggregatedValueList) {

		Map<String, List<DataCount>> weeksWiseDataCount = jobsAggregatedValueList.stream()
				.collect(Collectors.groupingBy(DataCount::getDate, LinkedHashMap::new, Collectors.toList()));

		List<DataCount> aggregatedDataCount = new ArrayList<>();
		weeksWiseDataCount.forEach((date, data) -> {
			Set<String> projectNames = new HashSet<>();
			DataCount dataCount = new DataCount();
			List<Double> values = new ArrayList<>();
			int totalBuilds = 0;
			int failedBuilds = 0;
			for (DataCount dc : data) {
				projectNames.add(dc.getSProjectName());
				Object obj = dc.getValue();
				Double value = obj instanceof Integer ? ((Integer) obj).doubleValue() : ((Double) obj).doubleValue();
				if (null != dc.getHoverValue().get(TOTAL_CHANGES)) {
					totalBuilds = totalBuilds + dc.getHoverValue().get(TOTAL_CHANGES);
				}
				if (null != dc.getHoverValue().get(FAILED_CHANGES)) {
					failedBuilds = failedBuilds + dc.getHoverValue().get(FAILED_CHANGES);
				}
				values.add(value);
			}
			Double aggregatedValue = calculateKpiValue(values, kpiId);
			Map<String, Integer> hoverMap = new HashMap<>();
			hoverMap.put(TOTAL_CHANGES, totalBuilds);
			hoverMap.put(FAILED_CHANGES, failedBuilds);
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
	public Double calculateKpiValue(List<Double> valueList, String kpiId) {
		return calculateKpiValueForDouble(valueList, kpiId);
	}

}
