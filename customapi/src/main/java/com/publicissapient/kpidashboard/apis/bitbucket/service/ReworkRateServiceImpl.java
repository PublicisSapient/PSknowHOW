package com.publicissapient.kpidashboard.apis.bitbucket.service;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
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
import com.publicissapient.kpidashboard.apis.repotools.service.RepoToolsConfigServiceImpl;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.Tool;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author kunkambl
 */
@Component
@Slf4j
public class ReworkRateServiceImpl extends BitBucketKPIService<Double, List<Object>, Map<String, Object>> {

	private static final String REPO_TOOLS = "RepoTool";
	public static final String WEEK_FREQUENCY = "week";
	public static final String DAY_FREQUENCY = "day";

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private RepoToolsConfigServiceImpl repoToolsConfigService;

	@Autowired
	private CustomApiConfig customApiConfig;

	@Override
	public String getQualifierType() {
		return KPICode.REWORK_RATE.name();
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
		calculateAggregatedValueMap(projectNode, nodeWiseKPIValue, KPICode.REWORK_RATE);

		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.REWORK_RATE);
		Map<String, Map<String, List<DataCount>>> kpiFilterWiseProjectWiseDc = new LinkedHashMap<>();
		trendValuesMap.forEach((issueType, dataCounts) -> {
			Map<String, List<DataCount>> projectWiseDc = dataCounts.stream()
					.collect(Collectors.groupingBy(DataCount::getData));
			kpiFilterWiseProjectWiseDc.put(issueType, projectWiseDc);
		});

		List<DataCountGroup> dataCountGroups = new ArrayList<>();
		kpiFilterWiseProjectWiseDc.forEach((issueType, projectWiseDc) -> {
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

	private void projectWiseLeafNodeValue(KpiElement kpiElement, Map<String, Node> mapTmp, Node projectLeafNode,
			KpiRequest kpiRequest) {

		CustomDateRange dateRange = KpiDataHelper.getStartAndEndDate(kpiRequest);
		String requestTrackerId = getRequestTrackerId();
		LocalDate localEndDate = dateRange.getEndDate();

		Integer dataPoints = kpiRequest.getXAxisDataPoints();
		String duration = kpiRequest.getDuration();

		// gets the tool configuration
		Map<ObjectId, Map<String, List<Tool>>> toolMap = configHelperService.getToolItemMap();

		List<RepoToolKpiMetricResponse> repoToolKpiMetricResponseList = getRepoToolsKpiMetricResponse(localEndDate,
				toolMap, projectLeafNode, dataPoints, duration);

		if (CollectionUtils.isEmpty(repoToolKpiMetricResponseList)) {
			log.error("[BITBUCKET-AGGREGATED-VALUE]. No kpi data found for this project {}", projectLeafNode);
			return;
		}

		List<KPIExcelData> excelData = new ArrayList<>();
		ProjectFilter accountHierarchyData = projectLeafNode.getProjectFilter();
		ObjectId configId = accountHierarchyData == null ? null : accountHierarchyData.getBasicProjectConfigId();
		List<Tool> reposList = toolMap.get(configId).get(REPO_TOOLS) == null
				? Collections.emptyList()
				: toolMap.get(configId).get(REPO_TOOLS);
		if (CollectionUtils.isEmpty(reposList)) {
			log.error("[BITBUCKET-AGGREGATED-VALUE]. No Jobs found for this project {}",
					projectLeafNode.getProjectFilter());
			return;
		}

		List<Map<String, Double>> repoWiseReworkRateList = new ArrayList<>();
		List<String> repoList = new ArrayList<>();
		List<String> branchList = new ArrayList<>();
		String projectName = projectLeafNode.getProjectFilter().getName();
		Map<String, List<DataCount>> aggDataMap = new HashMap<>();
		Map<String, List<Double>> aggReworkRateForRepo = new HashMap<>();
		reposList.forEach(repo -> {
			if (!CollectionUtils.isEmpty(repo.getProcessorItemList()) && repo.getProcessorItemList().get(0)
					.getId() != null) {
				Map<String, Double> excelDataLoader = new HashMap<>();
				String branchName = getBranchSubFilter(repo, projectName);
				Map<String, Double> dateWiseReworkRate = new HashMap<>();
				createDateLabelWiseMap(repoToolKpiMetricResponseList, repo.getRepositoryName(), repo.getBranch(),
						dateWiseReworkRate);
				reworkRateForRepo(aggReworkRateForRepo, dateWiseReworkRate);
				setWeekWiseReworkRate(dateWiseReworkRate, excelDataLoader, branchName, projectName, aggDataMap,
						kpiRequest);
				repoWiseReworkRateList.add(excelDataLoader);
				repoList.add(repo.getUrl());
				branchList.add(repo.getBranch());

			}
		});
		setWeekWiseReworkRate(aggReworkRateForRepo.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
						e -> e.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0.0))), new HashMap<>(),
				Constant.AGGREGATED_VALUE, projectName, aggDataMap, kpiRequest);
		mapTmp.get(projectLeafNode.getId()).setValue(aggDataMap);

		populateExcelDataObject(requestTrackerId, repoWiseReworkRateList, repoList, branchList, excelData,
				projectLeafNode);
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.REWORK_RATE.getColumns());
	}

	private void reworkRateForRepo(Map<String, List<Double>> aggReworkRateForRepo,
			Map<String, Double> reworkRateForRepo) {
		if (MapUtils.isNotEmpty(reworkRateForRepo)) {
			reworkRateForRepo.forEach(
					(key, value) -> aggReworkRateForRepo.computeIfAbsent(key, k -> new ArrayList<>()).add(value));
		}
	}

	/**
	 * @param repoToolKpiMetricResponsesCommit
	 * @param repoName
	 * @param branchName
	 * @param dateWiseReworkRate
	 */
	private void createDateLabelWiseMap(List<RepoToolKpiMetricResponse> repoToolKpiMetricResponsesCommit,
			String repoName, String branchName, Map<String, Double> dateWiseReworkRate) {

		for (RepoToolKpiMetricResponse response : repoToolKpiMetricResponsesCommit) {
			if (response.getRepositories() != null) {
				Optional<Branches> matchingBranch = response.getRepositories().stream()
						.filter(repository -> repository.getName().equals(repoName))
						.flatMap(repository -> repository.getBranches().stream())
						.filter(branch -> branch.getName().equals(branchName)).findFirst();
				double reworkRate = matchingBranch.isPresent() ? matchingBranch.get().getBranchReworkRateScore() : 0d;
				dateWiseReworkRate.put(response.getDateLabel(), reworkRate);
			}
		}
	}

	/**
	 * @param weekWiseReworkRate
	 * @param excelDataLoader
	 * @param branchName
	 * @param projectName
	 * @param aggDataMap
	 * @param kpiRequest
	 */
	private void setWeekWiseReworkRate(Map<String, Double> weekWiseReworkRate, Map<String, Double> excelDataLoader,
			String branchName, String projectName, Map<String, List<DataCount>> aggDataMap, KpiRequest kpiRequest) {
		LocalDate currentDate = LocalDate.now();
		Integer dataPoints = kpiRequest.getXAxisDataPoints();
		String duration = kpiRequest.getDuration();
		for (int i = 0; i < dataPoints; i++) {
			CustomDateRange dateRange = KpiDataHelper.getStartAndEndDateForDataFiltering(currentDate, duration);
			Double reworkRate = weekWiseReworkRate.getOrDefault(dateRange.getStartDate().toString(), 0d);
			String date = getDateRange(dateRange, duration);
			aggDataMap.putIfAbsent(branchName, new ArrayList<>());
			DataCount dataCount = setDataCount(projectName, date, reworkRate);
			aggDataMap.get(branchName).add(dataCount);
			excelDataLoader.put(date, reworkRate);
			currentDate = getNextRangeDate(duration, currentDate);

		}

	}

	private String getDateRange(CustomDateRange dateRange, String duration) {
		String range = null;
		if (CommonConstant.WEEK.equalsIgnoreCase(duration)) {
			range = DateUtil.dateTimeConverter(dateRange.getStartDate().toString(), DateUtil.DATE_FORMAT,
					DateUtil.DISPLAY_DATE_FORMAT) + " to " + DateUtil.dateTimeConverter(
					dateRange.getEndDate().toString(), DateUtil.DATE_FORMAT, DateUtil.DISPLAY_DATE_FORMAT);
		} else {
			range = dateRange.getStartDate().toString();
		}
		return range;
	}

	private LocalDate getNextRangeDate(String duration, LocalDate currentDate) {
		if ((CommonConstant.WEEK).equalsIgnoreCase(duration)) {
			currentDate = currentDate.minusWeeks(1);
		} else {
			currentDate = currentDate.minusDays(1);
		}
		return currentDate;
	}

	private DataCount setDataCount(String projectName, String week, Double value) {
		Map<String, Object> hoverMap = new HashMap<>();
		DataCount dataCount = new DataCount();
		dataCount.setData(String.valueOf(value == null ? 0l : value));
		dataCount.setSProjectName(projectName);
		dataCount.setDate(week);
		dataCount.setHoverValue(hoverMap);
		dataCount.setValue(value == null ? 0l : value);
		return dataCount;
	}

	/**
	 * get kpi data from repo tools api
	 *
	 * @param endDate
	 * @param toolMap
	 * @param node
	 * @param dataPoint
	 * @param duration
	 * @return
	 */
	private List<RepoToolKpiMetricResponse> getRepoToolsKpiMetricResponse(LocalDate endDate,
			Map<ObjectId, Map<String, List<Tool>>> toolMap, Node node, Integer dataPoint, String duration) {

		List<String> projectCodeList = new ArrayList<>();

		ProjectFilter accountHierarchyData = node.getProjectFilter();
		ObjectId configId = accountHierarchyData == null ? null : accountHierarchyData.getBasicProjectConfigId();
		List<Tool> tools = toolMap.getOrDefault(configId, Collections.emptyMap())
				.getOrDefault(REPO_TOOLS, Collections.emptyList());
		if (!CollectionUtils.isEmpty(tools)) {
			projectCodeList.add(node.getId());
		}

		List<RepoToolKpiMetricResponse> repoToolKpiMetricResponseList = new ArrayList<>();

		if (CollectionUtils.isNotEmpty(projectCodeList)) {
			LocalDate startDate = LocalDate.now().minusDays(dataPoint);
			if (duration.equalsIgnoreCase(CommonConstant.WEEK)) {
				startDate = LocalDate.now().minusWeeks(dataPoint);
				while (startDate.getDayOfWeek() != DayOfWeek.MONDAY) {
					startDate = startDate.minusDays(1);
				}
			}

			String debbieDuration = duration.equalsIgnoreCase(CommonConstant.WEEK) ? WEEK_FREQUENCY : DAY_FREQUENCY;
			repoToolKpiMetricResponseList = repoToolsConfigService.getRepoToolKpiMetrics(projectCodeList,
					customApiConfig.getRepoToolReworkRateUrl(), startDate.toString(), endDate.toString(),
					debbieDuration);
		}

		return repoToolKpiMetricResponseList;
	}

	private void populateExcelDataObject(String requestTrackerId, List<Map<String, Double>> repoWiseMRList,
			List<String> repoList, List<String> branchList, List<KPIExcelData> validationDataMap, Node node) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {

			String projectName = node.getProjectFilter().getName();
			KPIExcelUtility.populateReworkRateExcelData(projectName, repoWiseMRList, repoList, branchList,
					validationDataMap);

		}
	}

	@Override
	public Double calculateKPIMetrics(Map<String, Object> stringObjectMap) {
		return null;
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiId) {
		return calculateKpiValueForDouble(valueList, kpiId);
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		return new HashMap<>();
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping) {
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI173(), KPICode.REWORK_RATE.getKpiId());
	}
}