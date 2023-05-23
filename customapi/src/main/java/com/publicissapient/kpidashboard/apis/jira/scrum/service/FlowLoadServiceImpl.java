package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.JiraFeatureHistory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.CustomDateRange;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FlowLoadServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private CustomApiConfig customApiConfig;

    @Autowired
    private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

    @Override
    public String getQualifierType() {
        return KPICode.FLOW_LOAD.name();
    }

    @Override
    public Double calculateKPIMetrics(Map<String, Object> stringObjectMap) {
       return null;
    }

    @Override
    public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate, KpiRequest kpiRequest) {
        Map<String, Object> resultListMap = new HashMap<>();
        List<String> projectList = new ArrayList<>();

        leafNodeList.forEach(leaf -> {
            ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
            projectList.add(basicProjectConfigId.toString());
        });

        Map<String, Map<String, Integer>> a = jiraIssueCustomHistoryRepository.getStoryStatusCountByDateRange(projectList, startDate, endDate);
        resultListMap.put("typeCountMap",a);
        return  resultListMap;
    }


    @Override
    public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement, TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
        Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
        List<DataCount> trendValueList = new ArrayList<>();
        treeAggregatorDetail.getMapOfListOfProjectNodes().forEach((k, v) -> {
            Filters filters = Filters.getFilter(k);
            if (Filters.PROJECT == filters) {
                projectWiseLeafNodeValue(v, trendValueList, kpiElement, kpiRequest, mapTmp);
            }
        });
        return kpiElement;
    }

    private void projectWiseLeafNodeValue(List<Node> leafNode, List<DataCount> trendValueList, KpiElement kpiElement, KpiRequest kpiRequest, Map<String, Node> mapTmp) {
        // this method fetch dates for past history data
        CustomDateRange dateRange = KpiDataHelper.getMonthsForPastDataHistory(customApiConfig.getFlowKpiMonthCount());

        // get start and end date in yyyy-mm-dd format
        String startDate = dateRange.getStartDate().format(DATE_FORMATTER);
        String endDate = dateRange.getEndDate().format(DATE_FORMATTER);

        Map<String, Object> resultMap = fetchKPIDataFromDb(leafNode, startDate, endDate, kpiRequest);
    }
}
