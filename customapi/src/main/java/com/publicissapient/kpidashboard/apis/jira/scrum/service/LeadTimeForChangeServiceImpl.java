package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import static com.publicissapient.kpidashboard.common.constant.CommonConstant.HIERARCHY_LEVEL_ID_PROJECT;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.repository.application.DeploymentRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * This service for managing DeploymentFrequency kpi for scrum.
 *
 * @author hiren babariya
 */
@Component
@Slf4j
public class LeadTimeForChangeServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {

	@Autowired
	private ConfigHelperService configHelperService;
	@Autowired
	private DeploymentRepository deploymentRepository;

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

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
		calculateAggregatedValue(root, nodeWiseKPIValue, KPICode.DEFECT_INJECTION_RATE);
		List<DataCount> trendValues = getTrendValues(kpiRequest, nodeWiseKPIValue, KPICode.DEFECT_INJECTION_RATE);
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
			projectLeafNodeList.forEach(node -> {
				String trendLineName = node.getProjectFilter().getName();

				List<DataCount> dataCountList = new ArrayList<>();
				String weekOrMonth = (String) durationFilter.getOrDefault(Constant.DURATION, CommonConstant.WEEK);
				int previousTimeCount = (int) durationFilter.getOrDefault(Constant.COUNT, 5);
				LocalDate endDateTime = LocalDate.now();

				// jiraIssueCustomHistoryList group by

				for (int i = 0; i < previousTimeCount; i++) {

					LocalDate currentDate = endDateTime;
					if (weekOrMonth.equalsIgnoreCase(CommonConstant.WEEK)) {
						currentDate = currentDate.minusWeeks(i);
					} else if (weekOrMonth.equalsIgnoreCase(CommonConstant.MONTH)) {
						currentDate = currentDate.minusMonths(i);
					}

					String date = getDateFormatted(weekOrMonth, currentDate);
					DataCount dataCount = new DataCount();
					double days = getRandomDays();

					dataCount.setData(String.valueOf(days));
					dataCount.setSProjectName(trendLineName);
					dataCount.setDate(date);
					dataCount.setValue(days);
					dataCount.setHoverValue(new HashMap<>());
					dataCountList.add(dataCount);
				}
				mapTmp.get(node.getId()).setValue(dataCountList);
			});
			kpiElement.setExcelData(excelData);
			kpiElement.setExcelColumns(KPIExcelColumn.LEAD_TIME_FOR_CHANGE.getColumns());
		}
	}

	private double getRandomDays() {
		double min = 1.00;
		double max = 100.00;
		double days = min + (max - min) * random.nextDouble();
		days = Math.round(days * 100.0) / 100.0;
		return days;
	}

	private String getDateFormatted(String weekOrMonth, LocalDate currentDate) {
		if (weekOrMonth.equalsIgnoreCase(CommonConstant.WEEK)) {
			return DateUtil.getWeekRange(currentDate);
		} else {
			return currentDate.getYear() + Constant.DASH + currentDate.getMonthValue();
		}
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {

		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
		Set<ObjectId> projectBasicConfigIds = new HashSet<>();
		Map<String, Object> resultListMap = new HashMap<>();
		leafNodeList.forEach(node -> {
			ObjectId basicProjectConfigId = node.getProjectFilter().getBasicProjectConfigId();

			// field maping logic 1 or logic 2
			projectBasicConfigIds.add(basicProjectConfigId);
			Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
			List<String> releaseList = new ArrayList<>();
			mapOfProjectFilters.put(CommonConstant.RELEASE, CommonUtils.convertToPatternListForSubString(releaseList));
			uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);

			// if logic 1 -> user fieldmapping status
			// if logic 2 -> nothing
		});

		List<JiraIssueCustomHistory> jiraIssueHistoryDataList = new ArrayList<>();

		// if logic 1 -> finds jiraHistory Data by release list and closed list
		// if logic 2 -> finds jiraHistory Data by release list
		// -> find merge request by jira ticket ids

		// add queries of merge Request

		resultListMap.put("jiraIssueHistoryData", jiraIssueHistoryDataList); // logic 1 data
		resultListMap.put("mergeRequestData", new ArrayList<>());// logic 2 data
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

}
