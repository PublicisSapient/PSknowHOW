package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.enums.Filters;
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
import com.publicissapient.kpidashboard.common.model.jira.JiraHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import java.time.LocalDate;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class FlowLoadServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String JIRA_ISSUE_HISTORY = "Jira Issue History";

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

    public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate, KpiRequest kpiRequest) {
        Map<String, Object> resultListMap = new HashMap<>();

        String basicProjectConfigId = leafNodeList.get(0).getProjectFilter().getBasicProjectConfigId().toString();

        List<JiraIssueCustomHistory> jiraIssueCustomHistoryList = jiraIssueCustomHistoryRepository.findByBasicProjectConfigId(basicProjectConfigId);
        resultListMap.put(JIRA_ISSUE_HISTORY,jiraIssueCustomHistoryList);
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

        LocalDate startDate = LocalDateTime.parse(dateRange.getStartDate().toString()).toLocalDate();
        LocalDate endDate = LocalDateTime.parse(dateRange.getEndDate().toString()).toLocalDate();

        Map<String, Object> resultMap = fetchKPIDataFromDb(leafNode, startDate.toString(), endDate.toString(), kpiRequest);

        List<JiraIssueCustomHistory> jiraIssueCustomHistories = (List<JiraIssueCustomHistory>) resultMap
                .get(JIRA_ISSUE_HISTORY);
        Map<String, List<Pair<String,String>>> statusesWithStartAndEndDate = new HashMap<>();

        //segregation all different status present in issue with time range
        jiraIssueCustomHistories.forEach(issueCustomHistory->{
            List<JiraHistoryChangeLog> statusChangeLog = issueCustomHistory.getStatusUpdationLog();
           for(int index=0;index+1<statusChangeLog.size();index++)
           {
               JiraHistoryChangeLog changeLog = statusChangeLog.get(index);
               JiraHistoryChangeLog nextChangeLog = statusChangeLog.get(index+1);
               String status = changeLog.getChangedTo();
               LocalDate intervalStartDate = changeLog.getUpdatedOn().toLocalDate();
               LocalDate intervalEndDate = nextChangeLog.getUpdatedOn().toLocalDate();
               intervalStartDate = intervalStartDate.compareTo(startDate)<0?startDate:intervalStartDate;
               intervalEndDate = intervalEndDate.compareTo(endDate)>0?endDate:intervalEndDate;
               Pair<String, String> intervalRange = Pair.of(intervalStartDate.toString(),intervalEndDate.toString());
               if(!statusesWithStartAndEndDate.containsKey(status))
                   statusesWithStartAndEndDate.put(status, new ArrayList<>());
               statusesWithStartAndEndDate.get(status).add(intervalRange);
           }
           JiraHistoryChangeLog lastChangeLog = statusChangeLog.get(statusChangeLog.size()-1);
           String status = lastChangeLog.getChangedTo();
           LocalDate intervalStartDate = lastChangeLog.getUpdatedOn().toLocalDate();
           LocalDate intervalEndDate = endDate;
           if(intervalStartDate.compareTo(startDate)<0)
           {
               intervalStartDate =startDate;
           }
            Pair<String, String> intervalRange =  Pair.of(intervalStartDate.toString(),intervalEndDate.toString());
            if(!statusesWithStartAndEndDate.containsKey(status))
                statusesWithStartAndEndDate.put(status, new ArrayList<>());
            statusesWithStartAndEndDate.get(status).add(intervalRange);
        });

        Map<String, Object> dateWithStatusCount = new HashMap<>();
        LocalDate tempStartDate = startDate;
        while(tempStartDate.compareTo(endDate)>0)
        {
            dateWithStatusCount.put(tempStartDate.toString(),new Object());
        }
        long totalDays = ChronoUnit.DAYS.between(endDate,startDate);
        statusesWithStartAndEndDate.forEach((status, listOfStartAndEndDate)->{
           int[] statusCountPresentInEachDay = new int[(int) (totalDays+ 2)];
            Arrays.fill(statusCountPresentInEachDay,0);
            listOfStartAndEndDate.forEach(intervalRange->{

            });

        });


    }
}
