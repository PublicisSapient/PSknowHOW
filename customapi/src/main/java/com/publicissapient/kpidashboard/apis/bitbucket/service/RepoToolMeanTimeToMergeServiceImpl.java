package com.publicissapient.kpidashboard.apis.bitbucket.service;

import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.apis.repotools.model.Branches;
import com.publicissapient.kpidashboard.apis.repotools.model.RepoToolKpiMetricResponse;
import com.publicissapient.kpidashboard.apis.repotools.service.RepoToolsConfigServiceImpl;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.ProjectFilter;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.model.application.Tool;
import com.publicissapient.kpidashboard.common.repository.scm.MergeRequestRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RepoToolMeanTimeToMergeServiceImpl extends BitBucketKPIService<Double, List<Object>, Map<String, Object>> {

    public static final String MEAN_TIME_TO_MERGE = "meanTimeToMerge";
    public static final String REPO_TOOLS_KPI = "mr-life-cycle-bulk";
    public static final String FREQUENCY = "week";
    public static final DecimalFormat decformat = new DecimalFormat("#0.00");
    public static final String WEEK_SEPERATOR = " to ";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String AZURE_REPO = "AzureRepository";
    private static final String BITBUCKET = "Bitbucket";
    private static final String GITLAB = "GitLab";
    private static final String GITHUB = "GitHub";
    private static final String REPO_TOOLS = "Repo_Tools";
    private static final String AGGREGATED = "Overall";
    @Autowired
    private ConfigHelperService configHelperService;

    @Autowired
    private CustomApiConfig customApiConfig;

    @Autowired
    private RepoToolsConfigServiceImpl repoToolsConfigService;

    @Override
    public String getQualifierType() {
        return KPICode.REPO_TOOL_MEAN_TIME_TO_MERGE.name();
    }

    @Override
    public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
                                 TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
        Node root = treeAggregatorDetail.getRoot();
        Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
        treeAggregatorDetail.getMapOfListOfProjectNodes().forEach((k, v) -> {

            Filters filters = Filters.getFilter(k);
            if (Filters.PROJECT == filters) {
                projectWiseLeafNodeValue(kpiElement, mapTmp, v);
            }

        });
        Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
        calculateAggregatedValueMap(root, nodeWiseKPIValue, KPICode.REPO_TOOL_MEAN_TIME_TO_MERGE);

        Map<String, List<DataCount>> trendValuesMap = getTrendValuesMap(kpiRequest, nodeWiseKPIValue,
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

    private void aggMeanTimeToMerge(Map<String, Double> aggPickupTimeForRepo, Map<String, Double> pickupTimeForRepo) {
        if (MapUtils.isNotEmpty(pickupTimeForRepo)) {
            aggPickupTimeForRepo.putAll(pickupTimeForRepo);
        }
    }

    private void projectWiseLeafNodeValue(KpiElement kpiElement, Map<String, Node> mapTmp,
                                          List<Node> projectLeafNodeList) {
        String requestTrackerId = getRequestTrackerId();
        LocalDateTime localStartDate = LocalDateTime.now().minusDays(40);
        LocalDateTime localEndDate = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        String startDate = localStartDate.format(formatter);
        String endDate = localEndDate.format(formatter);

        // gets the tool configuration
        Map<ObjectId, Map<String, List<Tool>>> toolMap = configHelperService.getToolItemMap();
        List<RepoToolKpiMetricResponse> repoToolKpiMetricRespons = getRepoToolsKpiMetricResponse(localStartDate,
                localEndDate, toolMap, projectLeafNodeList);

        List<KPIExcelData> excelData = new ArrayList<>();
        projectLeafNodeList.stream().forEach(node -> {
            String projectName = node.getProjectFilter().getName();
            LocalDateTime end = localEndDate;

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
            Map<String, Double> excelDataLoader = new HashMap<>();

            Map<String, List<DataCount>> aggDataMap = new HashMap<>();
            Map<String, Double> aggMeanTimeToMerge = new HashMap<>();
            reposList.forEach(repo -> {
                if (!CollectionUtils.isEmpty(repo.getProcessorItemList())
                        && repo.getProcessorItemList().get(0).getId() != null) {
                    String branchName = getBranchSubFilter(repo, projectName);
                    if(CollectionUtils.isNotEmpty(repoToolKpiMetricRespons)) {
                        Map<String, Double> dateWiseMeanTimeToMerge = new HashMap<>();
                        createDateLabelWiseMap(repoToolKpiMetricRespons, repo.getRepositoryName(), repo.getBranch(), dateWiseMeanTimeToMerge);
                        aggMeanTimeToMerge(aggMeanTimeToMerge, dateWiseMeanTimeToMerge);
                        setWeekWiseMeanTimeToMergeForRepoTools(dateWiseMeanTimeToMerge, end, excelDataLoader, branchName, projectName,
                                aggDataMap);
                    }
                    repoWiseMRList.add(excelDataLoader);
                    repoList.add(repo.getUrl());
                    branchList.add(repo.getBranch());

                }
            });
           if(CollectionUtils.isNotEmpty(repoToolKpiMetricRespons)) {
                setWeekWiseMeanTimeToMergeForRepoTools(aggMeanTimeToMerge, end, excelDataLoader, Constant.AGGREGATED_VALUE, projectName,
                        aggDataMap);
            }
            mapTmp.get(node.getId()).setValue(aggDataMap);
            populateExcelDataObject(requestTrackerId, repoWiseMRList, repoList, branchList, excelData, node);
        });
        kpiElement.setExcelData(excelData);
        kpiElement.setExcelColumns(KPIExcelColumn.MEAN_TIME_TO_MERGE.getColumns());
    }

    /**
     *
     * @param mergeReqList
     * @param end
     * @param excelDataLoader
     * @param branchName
     * @param projectName
     * @param aggDataMap
     */
    private void setWeekWiseMeanTimeToMergeForRepoTools(Map<String, Double> mergeReqList, LocalDateTime end,
                                                        Map<String, Double> excelDataLoader, String branchName, String projectName,
                                                        Map<String, List<DataCount>> aggDataMap) {
        LocalDate endDate = end.toLocalDate();
        for (int i = 0; i < customApiConfig.getRepoXAxisCount(); i++) {
            LocalDate monday = endDate;
            while (monday.getDayOfWeek() != DayOfWeek.MONDAY) {
                monday = monday.minusDays(1);
            }
            LocalDate sunday = endDate;
            while (sunday.getDayOfWeek() != DayOfWeek.SUNDAY) {
                sunday = sunday.plusDays(1);
            }
            double meanTimeToMerge = mergeReqList.getOrDefault(monday.toString(), 0.0d)*1000;
//			double meanTimeToMerge = branches != null?branches.getAverage()*1000: 0.0d;
            String date = DateUtil.dateTimeConverter(monday.toString(), DateUtil.DATE_FORMAT,
                    DateUtil.DISPLAY_DATE_FORMAT) + WEEK_SEPERATOR
                    + DateUtil.dateTimeConverter(sunday.toString(), DateUtil.DATE_FORMAT, DateUtil.DISPLAY_DATE_FORMAT);
            aggDataMap.putIfAbsent(branchName, new ArrayList<>());
            DataCount dataCount = setDataCount(projectName, date, meanTimeToMerge);
            aggDataMap.get(branchName).add(dataCount);
            excelDataLoader.put(date, (double) TimeUnit.MILLISECONDS.toHours((long) meanTimeToMerge));
            endDate = endDate.minusWeeks(1);

        }

    }



    private void createDateLabelWiseMap(List<RepoToolKpiMetricResponse> repoToolKpiMetricResponsesCommit,
                                        String repoName, String branchName, Map<String, Double> dateWisePickupTime) {

        for (RepoToolKpiMetricResponse response : repoToolKpiMetricResponsesCommit) {
            if (response.getRepositories() != null) {
                Optional<Branches> matchingBranch = response.getRepositories().stream()
                        .filter(repository -> repository.getName().equals(repoName))
                        .flatMap(repository -> repository.getBranches().stream())
                        .filter(branch -> branch.getName().equals(branchName)).findFirst();

                double pickupTime = matchingBranch.map(Branches::getAverage).orElse(0.0d);
                matchingBranch.ifPresent(branch -> dateWisePickupTime.put(response.getDateLabel(), pickupTime));
            }
        }
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
    public Double calculateKPIMetrics(Map<String, Object> stringObjectMap) {
        return null;
    }

    @Override
    public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
                                                  KpiRequest kpiRequest) {
        return null;
    }

    /**
     *
     * @param startDate
     * @param endDate
     * @param toolMap
     * @param leafNodeList
     * @return
     */
    private List<RepoToolKpiMetricResponse> getRepoToolsKpiMetricResponse(LocalDateTime startDate, LocalDateTime endDate,
                                                                          Map<ObjectId, Map<String, List<Tool>>> toolMap, List<Node> leafNodeList) {
        List<String> projectCode = new ArrayList<>();
        leafNodeList.forEach(node -> {
            List<Tool> repoToolsJobs = getRepoToolsJobs(toolMap, node);
            if (CollectionUtils.isNotEmpty(repoToolsJobs)) {
                projectCode.add(node.getId());
            }
        });
        if(CollectionUtils.isEmpty(projectCode)) {
            return new ArrayList<>();
        }
        LocalDate monday = startDate.toLocalDate();
        while (monday.getDayOfWeek() != DayOfWeek.MONDAY) {
            monday = monday.minusDays(1);
        }
        LocalDate sunday = endDate.toLocalDate();
        while (sunday.getDayOfWeek() != DayOfWeek.SUNDAY) {
            sunday = sunday.plusDays(1);
        }
        return repoToolsConfigService.getRepoToolKpiMetrics(projectCode, REPO_TOOLS_KPI, monday.toString(), sunday.toString(),
                FREQUENCY);
    }

    /**
     *
     * @param toolMap
     * @param node
     * @return
     */
    private List<Tool> getRepoToolsJobs(Map<ObjectId, Map<String, List<Tool>>> toolMap, Node node) {
        ProjectFilter accountHierarchyData = node.getProjectFilter();
        ObjectId configId = accountHierarchyData == null ? null : accountHierarchyData.getBasicProjectConfigId();
        Map<String, List<Tool>> toolListMap = toolMap == null ? null : toolMap.get(configId);
        List<Tool> bitbucketJob = new ArrayList<>();
        if (null != toolListMap) {
            bitbucketJob.addAll(toolListMap.get(REPO_TOOLS) == null ? Collections.emptyList() : toolListMap.get(REPO_TOOLS));
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
            reposList.addAll(
                    mapOfListOfTools.get(REPO_TOOLS) == null ? Collections.emptyList() : mapOfListOfTools.get(REPO_TOOLS));
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
    public Double calculateKpiValue(List<Double> valueList, String kpiName) {
        return calculateKpiValueForDouble(valueList, kpiName);
    }

}
