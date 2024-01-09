package com.publicissapient.kpidashboard.apis.jenkins.service;

import static com.publicissapient.kpidashboard.common.constant.CommonConstant.HIERARCHY_LEVEL_ID_PROJECT;

import java.time.Instant;
import java.time.LocalDate;
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

import javax.validation.constraints.NotNull;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
import com.publicissapient.kpidashboard.apis.model.CodeBuildTimeInfo;
import com.publicissapient.kpidashboard.apis.model.CustomDateRange;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.AggregationUtils;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.BuildStatus;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.repository.application.BuildRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Jenkins KPI - This service for managing code build time for kanban.
 *
 * @author Hiren Babariya
 */
@Component
@Slf4j
public class CodeBuildTimeKanbanServiceImpl extends JenkinsKPIService<Long, List<Object>, Map<ObjectId, List<Build>>> {

	@Autowired
	private ConfigHelperService configHelperService;
	@Autowired
	private KpiHelperService kpiHelperService;
	@Autowired
	private BuildRepository buildRepository;
	@Autowired
	private CustomApiConfig customApiConfig;

	@Override
	public String getQualifierType() {
		return KPICode.CODE_BUILD_TIME_KANBAN.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		log.info("CODE-BUILD-TIME-LEAF-NODE-VALUE", kpiRequest.getRequestTrackerId());
		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
		List<Node> projectList = treeAggregatorDetail.getMapOfListOfProjectNodes().get(HIERARCHY_LEVEL_ID_PROJECT);

		// This method will contain the main logic to fetch data from db and set it in
		// aggregation tree
		dateWiseLeafNodeValue(mapTmp, projectList, kpiElement, kpiRequest);

		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();

		calculateAggregatedValueMap(root, nodeWiseKPIValue, KPICode.CODE_BUILD_TIME_KANBAN);
		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.CODE_BUILD_TIME_KANBAN);

		List<DataCountGroup> dataCountGroups = new ArrayList<>();
		trendValuesMap.forEach((key, dateWiseDataCount) -> {
			DataCountGroup dataCountGroup = new DataCountGroup();
			dataCountGroup.setFilter(key);
			dataCountGroup.setValue(dateWiseDataCount);
			dataCountGroups.add(dataCountGroup);
		});

		kpiElement.setTrendValueList(dataCountGroups);

		kpiElement.setNodeWiseKPIValue(nodeWiseKPIValue);
		log.debug("[CODE-BUILD-TIME-LEAF-NODE-VALUE][{}]. Aggregated Value at each level in the tree {}",
				kpiRequest.getRequestTrackerId(), root);
		return kpiElement;
	}

	@Override
	public Long calculateKPIMetrics(Map<ObjectId, List<Build>> objectIdListMap) {
		return null;
	}

	@Override
	public Map<ObjectId, List<Build>> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Set<ObjectId> projectBasicConfigIds = new HashSet<>();
		List<String> statusList = new ArrayList<>();
		Map<String, List<String>> mapOfFilters = new HashMap<>();
		leafNodeList.forEach(node -> {
			ObjectId basicProjectConfigId = node.getProjectFilter().getBasicProjectConfigId();
			projectBasicConfigIds.add(basicProjectConfigId);
		});

		statusList.add(BuildStatus.SUCCESS.name());
		mapOfFilters.put("buildStatus", statusList);
		List<Build> buildList = buildRepository.findBuildList(mapOfFilters, projectBasicConfigIds, startDate, endDate);
		if (CollectionUtils.isEmpty(buildList)) {
			return new HashMap<>();
		}
		return buildList.stream().collect(Collectors.groupingBy(Build::getBasicProjectConfigId, Collectors.toList()));
	}

	private void dateWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> leafNodeList, KpiElement kpiElement,
			KpiRequest kpiRequest) {

		// this method fetch start and end date to fetch data.
		CustomDateRange dateRange = KpiDataHelper.getStartAndEndDate(kpiRequest);

		// get start and end date in yyyy-mm-dd format
		String startDate = dateRange.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		String endDate = dateRange.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

		Map<ObjectId, List<Build>> resultMap = fetchKPIDataFromDb(leafNodeList, startDate, endDate, kpiRequest);

		if (MapUtils.isEmpty(resultMap)) {
			return;
		}

		kpiWithFilter(resultMap, mapTmp, leafNodeList, kpiElement, kpiRequest);

	}

	private void kpiWithFilter(Map<ObjectId, List<Build>> resultMap, Map<String, Node> mapTmp, List<Node> leafNodeList,
			KpiElement kpiElement, KpiRequest kpiRequest) {
		String requestTrackerId = getKanbanRequestTrackerId();
		List<KPIExcelData> excelData = new ArrayList<>();
		leafNodeList.forEach(node -> {
			Map<String, List<DataCount>> trendValueMap = new HashMap<>();
			CodeBuildTimeInfo codeBuildTimeInfo = new CodeBuildTimeInfo();
			List<DataCount> dataCountAggList = new ArrayList<>();
			String projectNodeId = node.getId();
			ObjectId basicProjectConfigId = node.getProjectFilter().getBasicProjectConfigId();
			List<Build> buildListProjectWise = resultMap.get(basicProjectConfigId);

			if (CollectionUtils.isNotEmpty(buildListProjectWise)) {

				Map<String, List<Build>> buildMapJobWise = buildListProjectWise.stream()
						.collect(Collectors.groupingBy(Build::getBuildJob, Collectors.toList()));

				filterDataBasedOnJobAndRangeWise(kpiRequest, trendValueMap, codeBuildTimeInfo, dataCountAggList,
						projectNodeId, buildMapJobWise);

				List<DataCount> aggData = calculateAggregatedRangeWise(KPICode.CODE_BUILD_TIME_KANBAN.getKpiId(),
						dataCountAggList);

				if (CollectionUtils.isNotEmpty(aggData)) {
					trendValueMap.put(CommonConstant.OVERALL, aggData);
				}
				if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {

					KPIExcelUtility.populateCodeBuildTimeExcelData(codeBuildTimeInfo, node.getProjectFilter().getName(),
							excelData);
				}
				mapTmp.get(node.getId()).setValue(trendValueMap);
			} else {
				mapTmp.get(node.getId()).setValue(null);
				return;
			}
		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.CODE_BUILD_TIME_KANBAN.getColumns());
	}

	private void filterDataBasedOnJobAndRangeWise(KpiRequest kpiRequest, Map<String, List<DataCount>> trendValueMap,
			CodeBuildTimeInfo codeBuildTimeInfo, List<DataCount> dataCountAggList, String projectNodeId,
			Map<String, List<Build>> buildMapJobWise) {

		String projectName = projectNodeId.substring(0, projectNodeId.lastIndexOf(CommonConstant.UNDERSCORE));

		for (Map.Entry<String, List<Build>> entry : buildMapJobWise.entrySet()) {
			String jobName;
			List<Build> buildList = entry.getValue();
			if (StringUtils.isNotEmpty(buildList.get(0).getJobFolder())) {
				jobName = buildList.get(0).getJobFolder();
			} else {
				jobName = entry.getKey();
			}
			LocalDate currentDate = LocalDate.now();
			for (int i = 0; i < kpiRequest.getKanbanXaxisDataPoints(); i++) {

				CustomDateRange dateRange = KpiDataHelper.getStartAndEndDateForDataFiltering(currentDate,
						kpiRequest.getDuration());

				String date = getRange(dateRange, kpiRequest);

				Map<String, Long> projectWiseBuildTimeCountMap = filterKanbanDataBasedOnDateAndBuildTimeWise(buildList,
						dateRange, date, codeBuildTimeInfo, jobName, projectName);

				populateProjectFilterWiseDataMap(projectWiseBuildTimeCountMap, trendValueMap, projectName,
						projectNodeId, date, dataCountAggList);

				currentDate = getNextRangeDate(kpiRequest, currentDate);

			}
		}
	}

	@Override
	public Long calculateKpiValue(List<Long> valueList, String kpiId) {
		return calculateKpiValueForLong(valueList, kpiId);
	}

	public Map<String, Long> filterKanbanDataBasedOnDateAndBuildTimeWise(List<Build> buildList,
			CustomDateRange dateRange, String date, CodeBuildTimeInfo codeBuildTimeInfo, String jobName,
			String projectName) {
		Map<String, Long> projectBuildTimeMap = new HashMap<>();
		Long valueForCurrentRange = 0l;
		List<Long> durationList = new ArrayList<>();
		buildList.forEach(build -> {
			LocalDate buildTime = Instant.ofEpochMilli(build.getStartTime()).atZone(ZoneId.systemDefault())
					.toLocalDate();
			if ((buildTime.isAfter(dateRange.getStartDate()) || buildTime.isEqual(dateRange.getStartDate()))
					&& (buildTime.isBefore(dateRange.getEndDate()) || buildTime.isEqual(dateRange.getEndDate()))) {
				durationList.add(build.getDuration());
				prepareCodeBuildTimeInfo(codeBuildTimeInfo, build, date);
			}
		});
		if (CollectionUtils.isNotEmpty(durationList)) {
			valueForCurrentRange = AggregationUtils.averageLong(durationList);
		}
		projectBuildTimeMap.put(jobName + CommonConstant.ARROW + projectName, valueForCurrentRange);
		return projectBuildTimeMap;
	}

	/**
	 * build time wise prepare data count list and treadValueMap
	 *
	 * @param projectWiseBuildTimeCountMap
	 * @param projectFilterWiseDataMap
	 * @param projectNodeId
	 * @param date
	 */
	private void populateProjectFilterWiseDataMap(Map<String, Long> projectWiseBuildTimeCountMap,
			Map<String, List<DataCount>> projectFilterWiseDataMap, String projectName, String projectNodeId,
			String date, List<DataCount> dataCountAggList) {

		projectWiseBuildTimeCountMap.forEach((key, value) -> {
			DataCount dcObj = getDataCountObject(value, projectName, date, projectNodeId, key);
			dataCountAggList.add(dcObj);
			projectFilterWiseDataMap.computeIfAbsent(key, k -> new ArrayList<>()).add(dcObj);
		});
	}

	/**
	 * as per date type given next range date
	 *
	 * @param kpiRequest
	 * @param currentDate
	 */
	@NotNull
	private LocalDate getNextRangeDate(KpiRequest kpiRequest, LocalDate currentDate) {
		if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.WEEK)) {
			currentDate = currentDate.minusWeeks(1);
		} else if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.MONTH)) {
			currentDate = currentDate.minusMonths(1);
		} else {
			currentDate = currentDate.minusDays(1);
		}
		return currentDate;
	}

	/**
	 * particulate date format given as per date type
	 *
	 * @param dateRange
	 * @param kpiRequest
	 */
	private String getRange(CustomDateRange dateRange, KpiRequest kpiRequest) {
		String range = null;
		if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.WEEK)) {
			range = dateRange.getStartDate().toString() + " to " + dateRange.getEndDate().toString();
		} else if (kpiRequest.getDuration().equalsIgnoreCase(CommonConstant.MONTH)) {
			range = dateRange.getStartDate().getMonth().toString() + " " + dateRange.getStartDate().getYear();
		} else {
			range = dateRange.getStartDate().toString();
		}
		return range;
	}

	/**
	 * particulate date format given as per date type
	 *
	 * @param value
	 * @param projectName
	 * @param date
	 * @param projectNodeId
	 * @param jobName
	 * @param
	 */
	private DataCount getDataCountObject(Long value, String projectName, String date, String projectNodeId,
			String jobName) {
		DataCount dataCount = new DataCount();
		dataCount.setData(String.valueOf(value == null ? 0L : TimeUnit.MILLISECONDS.toMinutes(value)));
		dataCount.setSProjectName(projectName);
		dataCount.setDate(date);
		dataCount.setHoverValue(new HashMap<>());
		dataCount.setKpiGroup(jobName);
		dataCount.setValue(value == null ? 0L : TimeUnit.MILLISECONDS.toMinutes(value));
		if (value == null) {
			dataCount.setPriority("0 sec");
		} else {
			long minutes = TimeUnit.MILLISECONDS.toMinutes(value);
			long seconds = TimeUnit.MILLISECONDS.toSeconds(value);
			dataCount.setPriority(minutes + Constant.MIN + seconds + Constant.SEC);
		}
		dataCount.setSprintIds(new ArrayList<>(Arrays.asList(projectNodeId)));
		dataCount.setSprintNames(new ArrayList<>(Arrays.asList(projectName)));
		return dataCount;
	}

	private void prepareCodeBuildTimeInfo(CodeBuildTimeInfo codeBuildTimeInfo, Build build, String date) {
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

	public List<DataCount> calculateAggregatedRangeWise(String kpiId, List<DataCount> jobsAggregatedValueList) {

		Map<String, List<DataCount>> weeksWiseDataCount = jobsAggregatedValueList.stream()
				.collect(Collectors.groupingBy(DataCount::getDate, LinkedHashMap::new, Collectors.toList()));

		List<DataCount> aggregatedDataCount = new ArrayList<>();
		weeksWiseDataCount.forEach((date, data) -> {
			Set<String> projectNames = new HashSet<>();
			DataCount dataCount = new DataCount();
			List<Long> values = new ArrayList<>();
			for (DataCount dc : data) {
				projectNames.add(dc.getSProjectName());
				Object obj = dc.getValue();
				Long value = obj instanceof Long ? (Long) obj : 0L;
				values.add(value);
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
			dataCount.setHoverValue(new HashMap<>());
			aggregatedDataCount.add(dataCount);
		});
		return aggregatedDataCount;
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

}