package com.publicissapient.kpidashboard.apis.bitbucket.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.Filters;
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
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.AggregationUtils;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.Tool;
import com.publicissapient.kpidashboard.common.model.scm.BranchMergeReqCount;
import com.publicissapient.kpidashboard.common.model.scm.MergeReqCount;
import com.publicissapient.kpidashboard.common.model.scm.MergeRequests;
import com.publicissapient.kpidashboard.common.repository.scm.MergeRequestRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author yasbano
 */
@Component
@Slf4j
public class MeanTimeToMergeServiceImpl extends BitBucketKPIService<Double, List<Object>, List<MergeRequests>> {

	public static final String DATE_FORMAT = "yyyy-MM-dd";
	private static final String AZURE_REPO = "AzureRepository";
	private static final String BITBUCKET = "Bitbucket";
	private static final String GITLAB = "GitLab";
	private static final String GITHUB = "GitHub";
	private static final String AGGREGATED = "Overall";
	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private MergeRequestRepository mergeRequestRepository;

	@Autowired
	private CustomApiConfig customApiConfig;

	@Override
	public String getQualifierType() {
		return KPICode.MEAN_TIME_TO_MERGE.name();
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
		treeAggregatorDetail.getMapOfListOfProjectNodes().forEach((k, v) -> {

			Filters filters = Filters.getFilter(k);
			if (Filters.PROJECT == filters) {
				projectWiseLeafNodeValue(kpiElement, mapTmp, v, kpiRequest);
			}

		});
		Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
		calculateAggregatedValueMap(root, nodeWiseKPIValue, KPICode.MEAN_TIME_TO_MERGE);

		Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, kpiElement, nodeWiseKPIValue,
				KPICode.MEAN_TIME_TO_MERGE);
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

	private void projectWiseLeafNodeValue(KpiElement kpiElement, Map<String, Node> mapTmp,
			List<Node> projectLeafNodeList, KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();
		CustomDateRange dateRange = KpiDataHelper.getStartAndEndDate(kpiRequest);
		LocalDate localStartDate = dateRange.getStartDate();
		LocalDate localEndDate = dateRange.getEndDate();

		Integer dataPoints = kpiRequest.getXAxisDataPoints();
		String duration = kpiRequest.getDuration();

		// gets the tool configuration
		Map<ObjectId, Map<String, List<Tool>>> toolMap = configHelperService.getToolItemMap();
		List<MergeRequests> mergeRequestsList = fetchKPIDataFromDb(projectLeafNodeList, localStartDate.toString(),
				localEndDate.toString(), null);

		// converting to map with keys collectorItemId
		Map<ObjectId, List<MergeRequests>> mergeRequestsListItemId = mergeRequestsList.stream()
				.collect(Collectors.groupingBy(MergeRequests::getProcessorItemId));

		List<KPIExcelData> excelData = new ArrayList<>();
		projectLeafNodeList.stream().forEach(node -> {
			String projectName = node.getProjectFilter().getName();

			ProjectFilter accountHierarchyData = node.getProjectFilter();
			ObjectId configId = accountHierarchyData == null ? null : accountHierarchyData.getBasicProjectConfigId();
			Map<String, List<Tool>> mapOfListOfTools = toolMap.get(configId);
			List<Tool> reposList = new ArrayList<>();
			populateRepoList(reposList, mapOfListOfTools);
			if (CollectionUtils.isEmpty(reposList)) {
				log.error("[BITBUCKET-AGGREGATED-VALUE]. No Jobs found for this project {}", node.getProjectFilter());
				return;
			}

			List<Map<String, Double>> repoWiseMRList = new ArrayList<>();
			List<String> repoList = new ArrayList<>();
			List<String> branchList = new ArrayList<>();

			Map<String, List<DataCount>> aggDataMap = new HashMap<>();
			List<MergeRequests> aggMergeRequests = new ArrayList<>();
			reposList.forEach(repo -> {
				if (!CollectionUtils.isEmpty(repo.getProcessorItemList())
						&& repo.getProcessorItemList().get(0).getId() != null) {
					List<MergeRequests> mergeReqList = mergeRequestsListItemId
							.get(repo.getProcessorItemList().get(0).getId());
					if (CollectionUtils.isNotEmpty(mergeReqList)) {
						Map<String, Double> excelDataLoader = new HashMap<>();
						aggMergeRequests.addAll(mergeReqList);
						List<DataCount> dataCountList = setWeekWiseMeanTimeToMerge(mergeReqList, excelDataLoader,
								projectName, duration, dataPoints);
						aggDataMap.put(getBranchSubFilter(repo, projectName), dataCountList);
						repoWiseMRList.add(excelDataLoader);
						repoList.add(repo.getUrl());
						branchList.add(repo.getBranch());
					}
				}
			});
			List<DataCount> dataCountList = setWeekWiseMeanTimeToMerge(aggMergeRequests, new HashMap<>(), projectName,
					duration, dataPoints);
			aggDataMap.put(Constant.AGGREGATED_VALUE, dataCountList);
			mapTmp.get(node.getId()).setValue(aggDataMap);
			populateExcelDataObject(requestTrackerId, repoWiseMRList, repoList, branchList, excelData, node);
		});
		kpiElement.setExcelData(excelData);
		kpiElement.setExcelColumns(KPIExcelColumn.MEAN_TIME_TO_MERGE.getColumns());
	}

	private List<DataCount> setWeekWiseMeanTimeToMerge(List<MergeRequests> mergeReqList,
			Map<String, Double> excelDataLoader, String projectName, String duration, Integer dataPoints) {

		List<DataCount> dataCountList = new ArrayList<>();
		LocalDate currentDate = LocalDate.now();
		for (int i = 0; i < dataPoints; i++) {
			CustomDateRange dateRange = KpiDataHelper.getStartAndEndDateForDataFiltering(currentDate, duration);
			List<Double> durationList = new ArrayList<>();
			for (MergeRequests mergeReq : mergeReqList) {
				LocalDate closedDate = Instant.ofEpochMilli(mergeReq.getClosedDate()).atZone(ZoneId.systemDefault())
						.toLocalDate();
				if (closedDate.compareTo(dateRange.getStartDate()) >= 0
						&& closedDate.compareTo(dateRange.getEndDate()) <= 0) {
					double mergeDuration = (double) (mergeReq.getClosedDate()) - mergeReq.getCreatedDate();
					durationList.add(mergeDuration);
				}
			}
			String date = getDateRange(dateRange, duration);
			Double valueForCurrentLeaf = ObjectUtils.defaultIfNull(AggregationUtils.average(durationList), 0.0d);
			if (null != valueForCurrentLeaf) {
				DataCount dataCount = setDataCount(projectName, date, valueForCurrentLeaf);
				dataCountList.add(dataCount);
				excelDataLoader.put(date, (double) TimeUnit.MILLISECONDS.toHours(valueForCurrentLeaf.longValue()));
			}
			currentDate = getNextRangeDate(duration, currentDate);
		}
		Collections.reverse(dataCountList);
		return dataCountList;
	}

	private String getDateRange(CustomDateRange dateRange, String duration) {
		String range = null;
		if (CommonConstant.WEEK.equalsIgnoreCase(duration)) {
			range = DateUtil.dateTimeConverter(dateRange.getStartDate().toString(), DateUtil.DATE_FORMAT,
					DateUtil.DISPLAY_DATE_FORMAT) + " to "
					+ DateUtil.dateTimeConverter(dateRange.getEndDate().toString(), DateUtil.DATE_FORMAT,
							DateUtil.DISPLAY_DATE_FORMAT);
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

	/**
	 * @param projectName
	 * @param week
	 * @param value
	 * @return
	 */
	private DataCount setDataCount(String projectName, String week, Double value) {
		DataCount dataCount = new DataCount();
		dataCount.setData(String.valueOf(value == null ? 0L : TimeUnit.MILLISECONDS.toHours(value.longValue())));
		dataCount.setSProjectName(projectName);
		dataCount.setDate(week);
		dataCount.setHoverValue(new HashMap<>());
		dataCount.setValue(value == null ? 0.0 : TimeUnit.MILLISECONDS.toHours(value.longValue()));
		return dataCount;
	}

	@Override
	public List<MergeRequests> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		List<ObjectId> tools = new ArrayList<>();
		Map<ObjectId, Map<String, List<Tool>>> toolMap;
		// gets the tool configuration
		toolMap = configHelperService.getToolItemMap();
		BasicDBList filter = new BasicDBList();
		leafNodeList.forEach(node -> {
			List<Tool> bitbucketJob = getBitBucketJobs(toolMap, node);
			if (CollectionUtils.isEmpty(bitbucketJob)) {
				return;
			}
			bitbucketJob.forEach(job -> {
				if (org.springframework.util.CollectionUtils.isEmpty(job.getProcessorItemList())) {
					return;
				}
				tools.add(job.getProcessorItemList().get(0).getId());
				filter.add(new BasicDBObject("processorItemId", job.getProcessorItemList().get(0).getId())
						.append("toBranch", job.getBranch())
						.append("repoSlug", StringUtils.defaultIfEmpty(job.getRepoSlug(), "NA"))
						.append("state", "MERGED"));
			});
		});

		if (filter.isEmpty()) {
			return new ArrayList<>();
		}
		return mergeRequestRepository.findMergeRequestList(tools,
				new DateTime(startDate, DateTimeZone.UTC).toDate().getTime(),
				new DateTime(endDate, DateTimeZone.UTC).toDate().getTime(), filter);
	}

	private List<Tool> getBitBucketJobs(Map<ObjectId, Map<String, List<Tool>>> toolMap, Node node) {
		ProjectFilter accountHierarchyData = node.getProjectFilter();
		ObjectId configId = accountHierarchyData == null ? null : accountHierarchyData.getBasicProjectConfigId();
		Map<String, List<Tool>> toolListMap = toolMap == null ? null : toolMap.get(configId);
		List<Tool> bitbucketJob = new ArrayList<>();
		if (null != toolListMap) {
			bitbucketJob
					.addAll(toolListMap.get(BITBUCKET) == null ? Collections.emptyList() : toolListMap.get(BITBUCKET));
			bitbucketJob.addAll(
					toolListMap.get(AZURE_REPO) == null ? Collections.emptyList() : toolListMap.get(AZURE_REPO));
			bitbucketJob.addAll(toolListMap.get(GITLAB) == null ? Collections.emptyList() : toolListMap.get(GITLAB));
			bitbucketJob.addAll(toolListMap.get(GITHUB) == null ? Collections.emptyList() : toolListMap.get(GITHUB));
		}
		if (CollectionUtils.isEmpty(bitbucketJob)) {
			log.error("[BITBUCKET]. No repository found for this project {}", node.getProjectFilter());
		}
		return bitbucketJob;
	}

	private void populateRepoList(List<Tool> reposList, Map<String, List<Tool>> mapOfListOfTools) {
		if (null != mapOfListOfTools) {
			reposList.addAll(mapOfListOfTools.get(BITBUCKET) == null ? Collections.emptyList()
					: mapOfListOfTools.get(BITBUCKET));
			reposList.addAll(mapOfListOfTools.get(AZURE_REPO) == null ? Collections.emptyList()
					: mapOfListOfTools.get(AZURE_REPO));
			reposList.addAll(
					mapOfListOfTools.get(GITLAB) == null ? Collections.emptyList() : mapOfListOfTools.get(GITLAB));
			reposList.addAll(
					mapOfListOfTools.get(GITHUB) == null ? Collections.emptyList() : mapOfListOfTools.get(GITHUB));
		}
	}

	private void populateExcelDataObject(String requestTrackerId, List<Map<String, Double>> repoWiseMRList,
			List<String> repoList, List<String> branchList, List<KPIExcelData> validationDataMap, Node node) {
		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {

			String projectName = node.getProjectFilter().getName();

			KPIExcelUtility.populateMeanTimeMergeExcelData(projectName, repoWiseMRList, repoList, branchList,
					validationDataMap);

		}
	}

	@Override
	public Object calculateAggregatedValue(Node node, Map<Pair<String, String>, Node> nodeWiseKPIValue,
			KPICode kpiCode) {
		String kpiId = kpiCode.getKpiId();

		if (node == null) {
			return 0;
		}
		if (CollectionUtils.isEmpty(node.getChildren())) {
			if (node.getValue() instanceof Integer || CollectionUtils.isEmpty((Collection) node.getValue())) {
				return new ArrayList<>();
			}
			return node.getValue();
		}
		List<Node> children = node.getChildren();
		List<BranchMergeReqCount> aggregatedValueList = new ArrayList<>();
		for (Node child : children) {
			if (child.getChildren() != null) {
				Object value = calculateAggregatedValue(child, nodeWiseKPIValue, kpiCode);
				if (value instanceof Collection<?>) {
					aggregatedValueList.addAll((List<BranchMergeReqCount>) value);
				}
			}
		}
		if (CollectionUtils.isNotEmpty(aggregatedValueList)) {
			aggregatedCodeCommitValue(kpiId, aggregatedValueList, node);
		}
		nodeWiseKPIValue.put(Pair.of(node.getGroupName(), node.getId()), node);
		return node.getValue();
	}

	private void aggregatedCodeCommitValue(String kpiId, List<BranchMergeReqCount> valueList, Node node) {

		Map<String, Double> resultMap = new HashMap<>();
		List<MergeReqCount> aggregatedValueList = new ArrayList<>();
		valueList.forEach(mergReq -> aggregatedValueList.addAll(mergReq.getWeekWiseData()));

		Map<String, List<MergeReqCount>> aggMap = aggregatedValueList.stream()
				.collect(Collectors.groupingBy(MergeReqCount::getWeek));
		List<MergeReqCount> aggregatedValue = new ArrayList<>();
		aggMap.forEach((key, merReq) -> {
			List<Double> value = merReq.stream().map(MergeReqCount::getTime).collect(Collectors.toList());
			String unit = "Hours";
			MergeReqCount aggCommit = null;
			if (Constant.PERCENTILE.equalsIgnoreCase(configHelperService.calculateCriteria().get(kpiId))) {

				if (null == customApiConfig.getPercentileValue()) {
					aggCommit = new MergeReqCount(key, AggregationUtils.percentiles(value, 90.0D), unit);
				} else {
					aggCommit = new MergeReqCount(key,
							AggregationUtils.percentiles(value, customApiConfig.getPercentileValue()), unit);
					resultMap.put(key, AggregationUtils.percentiles(value, customApiConfig.getPercentileValue()));
				}
			} else if (Constant.MEDIAN.equalsIgnoreCase(configHelperService.calculateCriteria().get(kpiId))) {
				aggCommit = new MergeReqCount(key, AggregationUtils.median(value), unit);
			} else if (Constant.AVERAGE.equalsIgnoreCase(configHelperService.calculateCriteria().get(kpiId))) {
				aggCommit = new MergeReqCount(key, AggregationUtils.average(value), unit);
			} else if (Constant.SUM.equalsIgnoreCase(configHelperService.calculateCriteria().get(kpiId))) {
				aggCommit = new MergeReqCount(key, AggregationUtils.sum(value), unit);
			}
			if (null != aggCommit) {
				aggregatedValue.add(aggCommit);
			}
		});
		BranchMergeReqCount branchCommitCount = new BranchMergeReqCount(AGGREGATED, aggregatedValue);
		List<BranchMergeReqCount> nodeValue = new ArrayList<>();
		nodeValue.add(branchCommitCount);
		node.setValue(nodeValue);

	}

	@Override
	public Double calculateKPIMetrics(List<MergeRequests> mergeRequests) {
		return null;
	}

	@Override
	public Double calculateKpiValue(List<Double> valueList, String kpiName) {
		return calculateKpiValueForDouble(valueList, kpiName);
	}

	@Override
	public Double calculateThresholdValue(FieldMapping fieldMapping){
		return calculateThresholdValue(fieldMapping.getThresholdValueKPI84(),KPICode.MEAN_TIME_TO_MERGE.getKpiId());
	}

}
