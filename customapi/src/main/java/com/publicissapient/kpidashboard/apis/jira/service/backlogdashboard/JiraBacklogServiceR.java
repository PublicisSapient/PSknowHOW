/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package com.publicissapient.kpidashboard.apis.jira.service.backlogdashboard;

import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.errors.EntityNotFoundException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.jira.factory.JiraNonTrendKPIServiceFactory;
import com.publicissapient.kpidashboard.apis.jira.service.JiraNonTrendKPIServiceR;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.ProjectFilter;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueReleaseStatus;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueReleaseStatusRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * This class handle all Backlog JIRA based KPI request and call each KPIs service
 * in thread. It is responsible for cache of KPI data at different level.
 *
 * @author purgupta2
 */
@Service
@Slf4j
public class JiraBacklogServiceR implements JiraNonTrendKPIServiceR {

    private final ThreadLocal<List<JiraIssue>> threadLocalJiraIssues = ThreadLocal.withInitial(ArrayList::new);
    private final ThreadLocal<List<JiraIssueCustomHistory>> threadLocalHistory = ThreadLocal.withInitial(ArrayList::new);
    JiraIssueReleaseStatus jiraIssueReleaseStatus = new JiraIssueReleaseStatus();
    @Autowired
    private KpiHelperService kpiHelperService;
    @Autowired
    private FilterHelperService filterHelperService;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private SprintRepository sprintRepository;
    @Autowired
    private JiraIssueRepository jiraIssueRepository;
    @Autowired
    private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;
    @Autowired
    private JiraIssueReleaseStatusRepository jiraIssueReleaseStatusRepository;
    @Autowired
    private CustomApiConfig customApiConfig;
    private List<SprintDetails> futureSprintDetails;
    private List<JiraIssue> jiraIssueList;
    private List<JiraIssueCustomHistory> jiraIssueCustomHistoryList;

    /**
     * This method process scrum jira based Backlog kpis request, cache data and call service
     * in multiple thread.
     *
     * @param kpiRequest JIRA KPI request true if flow for precalculated, false for direct
     *                   flow.
     * @return List of KPI data
     * @throws EntityNotFoundException EntityNotFoundException
     */
    @SuppressWarnings({"PMD.AvoidCatchingGenericException", "unchecked"})
    @Override
    public List<KpiElement> process(KpiRequest kpiRequest) throws EntityNotFoundException {

        log.info("Processing KPI calculation for data {}", kpiRequest.getKpiList());
        List<KpiElement> origRequestedKpis = kpiRequest.getKpiList().stream().map(KpiElement::new).collect(Collectors.toList());
        List<KpiElement> responseList = new ArrayList<>();
        String[] projectKeyCache = null;
        try {
            Integer groupId = kpiRequest.getKpiList().get(0).getGroupId();
            String groupName = filterHelperService.getHierarachyLevelId(kpiRequest.getLevel(), kpiRequest.getLabel(), false);
            if (null != groupName) {
                kpiRequest.setLabel(groupName.toUpperCase());
            } else {
                log.error("label name for selected hierarchy not found");
            }
            List<AccountHierarchyData> filteredAccountDataList = getFilteredAccountHierarchyData(kpiRequest);
            if (!CollectionUtils.isEmpty(filteredAccountDataList)) {
                projectKeyCache = kpiHelperService.getProjectKeyCache(kpiRequest, filteredAccountDataList);

                filteredAccountDataList = kpiHelperService.getAuthorizedFilteredList(kpiRequest, filteredAccountDataList);
                if (filteredAccountDataList.isEmpty()) {
                    return responseList;
                }
                Object cachedData = cacheService.getFromApplicationCache(projectKeyCache, KPISource.JIRA.name(), groupId, kpiRequest.getSprintIncluded());
                if (!kpiRequest.getRequestTrackerId().toLowerCase().contains(KPISource.EXCEL.name().toLowerCase()) && null != cachedData && isLeadTimeDuration(kpiRequest.getKpiList())) {
                    log.info("Fetching value from cache for {}", Arrays.toString(kpiRequest.getIds()));
                    return (List<KpiElement>) cachedData;
                }

                Node filteredNode = getFilteredNodes(kpiRequest, filteredAccountDataList);

                if (!CollectionUtils.isEmpty(origRequestedKpis) && StringUtils.isNotEmpty(origRequestedKpis.get(0).getKpiCategory())) {
                    updateJiraIssueList(filteredAccountDataList);
                }
                // set filter value to show on trend line. If subprojects are
                // in
                // selection then show subprojects on trend line else show
                // projects
                kpiRequest.setFilterToShowOnTrend(groupName);

                ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

                List<CompletableFuture<Void>> futures = new ArrayList<>();

                for (KpiElement kpiEle : kpiRequest.getKpiList()) {
                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                        threadLocalJiraIssues.set(jiraIssueList);
                        threadLocalHistory.set(jiraIssueCustomHistoryList);

                        try {
                            calculateAllKPIAggregatedMetrics(kpiRequest, responseList, kpiEle, filteredNode);
                        } catch (Exception e) {
                            log.error("Error while KPI calculation for data {}", kpiRequest.getKpiList(), e);
                        }
                    }, executorService);
                    futures.add(future);
                }

                CompletableFuture<Void>[] futureArray = futures.toArray(new CompletableFuture[0]);

                CompletableFuture<Void> allOf = CompletableFuture.allOf(futureArray);
                allOf.join(); // Wait for all tasks to complete

                executorService.shutdown();
                List<KpiElement> missingKpis = origRequestedKpis.stream().filter(reqKpi -> responseList.stream().noneMatch(responseKpi -> reqKpi.getKpiId().equals(responseKpi.getKpiId()))).collect(Collectors.toList());
                responseList.addAll(missingKpis);

                kpiHelperService.setIntoApplicationCache(kpiRequest, responseList, groupId, projectKeyCache);
            } else {
                responseList.addAll(origRequestedKpis);
            }

        } catch (Exception e) {
            log.error("Error while KPI calculation for data {}", kpiRequest.getKpiList(), e);
            throw new HttpMessageNotWritableException(e.getMessage(), e);
        } finally {
            threadLocalJiraIssues.remove();
            threadLocalHistory.remove();
        }

        return responseList;
    }

    private Node getFilteredNodes(KpiRequest kpiRequest, List<AccountHierarchyData> filteredAccountDataList) {
        Node filteredNode = filteredAccountDataList.get(0).getNode().get(kpiRequest.getLevel() - 1);

        filteredNode.setProjectFilter(new ProjectFilter(filteredNode.getId(), filteredNode.getName(),
                filteredNode.getAccountHierarchy().getBasicProjectConfigId()));
        return filteredNode;
    }

    private List<AccountHierarchyData> getFilteredAccountHierarchyData(KpiRequest kpiRequest) {
        List<AccountHierarchyData> accountDataListAll = (List<AccountHierarchyData>) cacheService
                .cacheAccountHierarchyData();

        List<AccountHierarchyData> projectAccountHierarchyData = accountDataListAll.stream()
                .filter(accountHierarchyData ->
                        accountHierarchyData.getLeafNodeId().equalsIgnoreCase(kpiRequest.getSelectedMap().get(CommonConstant.PROJECT.toLowerCase()).get(0))
                )
                .collect(Collectors.toList());
        if (projectAccountHierarchyData.isEmpty()){
            return accountDataListAll.stream()
                    .filter(accountHierarchyData ->
                            accountHierarchyData.getLeafNodeId().equalsIgnoreCase(kpiRequest.getSelectedMap().get(CommonConstant.SPRINT).get(0))
                    )
                    .collect(Collectors.toList());
        }
        return projectAccountHierarchyData;
    }

    private void updateJiraIssueList(List<AccountHierarchyData> filteredAccountDataList) {
        futureProjectWiseSprintDetails(filteredAccountDataList.get(0).getBasicProjectConfigId(), SprintDetails.SPRINT_STATE_FUTURE);
        fetchJiraIssues(filteredAccountDataList.get(0).getBasicProjectConfigId().toString(), CommonConstant.BACKLOG);
        fetchJiraIssuesCustomHistory(filteredAccountDataList.get(0).getBasicProjectConfigId().toString(), CommonConstant.BACKLOG);
        fetchJiraIssueReleaseForProject(filteredAccountDataList.get(0).getBasicProjectConfigId().toString(), CommonConstant.BACKLOG);
    }

    private boolean isLeadTimeDuration(List<KpiElement> kpiList) {
        return kpiList.size() != 1 || !kpiList.get(0).getKpiId().equalsIgnoreCase("kpi3");
    }

    public void futureProjectWiseSprintDetails(ObjectId basicProjectConfigId, String sprintState) {
        futureSprintDetails = sprintRepository.findByBasicProjectConfigIdAndStateIgnoreCaseOrderByStartDateASC(basicProjectConfigId, sprintState);
    }

    public void fetchJiraIssues(String basicProjectConfigId, String board) {
        if (board.equalsIgnoreCase(CommonConstant.BACKLOG)) {
            jiraIssueList = jiraIssueRepository.findByBasicProjectConfigIdIn(basicProjectConfigId);
        }
    }

    public void fetchJiraIssuesCustomHistory(String basicProjectConfigId, String board) {
        if (board.equalsIgnoreCase(CommonConstant.BACKLOG)) {
            jiraIssueCustomHistoryList = jiraIssueCustomHistoryRepository.findByBasicProjectConfigIdIn(basicProjectConfigId);
        }
    }

    public void fetchJiraIssueReleaseForProject(String basicProjectConfigId, String board) {

        if (board.equalsIgnoreCase(CommonConstant.BACKLOG)) {
            jiraIssueReleaseStatus = jiraIssueReleaseStatusRepository.findByBasicProjectConfigId(basicProjectConfigId);
        }
    }

    public JiraIssueReleaseStatus getJiraIssueReleaseForProject() {
        return jiraIssueReleaseStatus;
    }

    public List<JiraIssue> getJiraIssuesForCurrentSprint() {
        return threadLocalJiraIssues.get();
    }

    public List<JiraIssueCustomHistory> getJiraIssuesCustomHistoryForCurrentSprint() {
        return threadLocalHistory.get();
    }

    private void calculateAllKPIAggregatedMetrics(KpiRequest kpiRequest, List<KpiElement> responseList, KpiElement kpiElement, Node filteredAccountNode) throws ApplicationException {

        JiraBacklogKPIService jiraKPIService = null;
        KPICode kpi = KPICode.getKPI(kpiElement.getKpiId());
        jiraKPIService = (JiraBacklogKPIService) JiraNonTrendKPIServiceFactory.getJiraKPIService(kpi.name());
        long startTime = System.currentTimeMillis();
        if (KPICode.THROUGHPUT.equals(kpi)) {
            log.info("No need to fetch Throughput KPI data");
        } else {
            Node nodeDataClone = (Node) SerializationUtils
                    .clone(filteredAccountNode);
            responseList.add(jiraKPIService.getKpiData(kpiRequest, kpiElement, nodeDataClone));
            long processTime = System.currentTimeMillis() - startTime;
            log.info("[JIRA-{}-TIME][{}]. KPI took {} ms", kpi.name(), kpiRequest.getRequestTrackerId(), processTime);
        }
    }

    /**
     * This method return list of 5 distinct future sprint names
     *
     * @return return list of sprintNames
     */
    public List<String> getFutureSprintsList() {
        List<String> sprintNames = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(futureSprintDetails)) {
            sprintNames = futureSprintDetails.stream()
                    .sorted(Comparator.comparing(SprintDetails::getStartDate,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .map(SprintDetails::getSprintName).distinct()
                    .limit(customApiConfig.getSprintCountForBackLogStrength()).collect(Collectors.toList());

        }
        return sprintNames;
    }
}
