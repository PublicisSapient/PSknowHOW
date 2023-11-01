package com.publicissapient.kpidashboard.apis.jira.service.releasedashboard;

import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.errors.EntityNotFoundException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.jira.factory.JiraIterationKPIServiceFactory;
import com.publicissapient.kpidashboard.apis.jira.factory.JiraKPIServiceFactory;
import com.publicissapient.kpidashboard.apis.jira.factory.JiraReleaseKPIServiceFactory;
import com.publicissapient.kpidashboard.apis.jira.service.iterationdashboard.JiraIterationKPIService;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueReleaseStatus;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueReleaseStatusRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JiraReleaseServiceR {

    private final ThreadLocal<List<SprintDetails>> threadLocalSprintDetails = ThreadLocal.withInitial(ArrayList::new);
    private final ThreadLocal<List<JiraIssue>> threadLocalJiraIssues = ThreadLocal.withInitial(ArrayList::new);
    private final ThreadLocal<List<JiraIssueCustomHistory>> threadLocalHistory = ThreadLocal
            .withInitial(ArrayList::new);
    private final ThreadLocal<List<JiraIssue>> threadReleaseIssues = ThreadLocal.withInitial(ArrayList::new);
    private final ThreadLocal<Set<JiraIssue>> threadSubtaskDefects = ThreadLocal.withInitial(HashSet::new);
    JiraIssueReleaseStatus jiraIssueReleaseStatus = new JiraIssueReleaseStatus();
    ExecutorService executorService;
    @Autowired
    private KpiHelperService kpiHelperService;
    @Autowired
    private FilterHelperService filterHelperService;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private UserAuthorizedProjectsService authorizedProjectsService;
    @Autowired
    private SprintRepository sprintRepository;
    @Autowired
    private JiraIssueRepository jiraIssueRepository;
    @Autowired
    private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;
    @Autowired
    private ConfigHelperService configHelperService;
    @Autowired
    private JiraIssueReleaseStatusRepository jiraIssueReleaseStatusRepository;
    private List<SprintDetails> sprintDetails;
    private List<JiraIssue> jiraIssueList;
    private List<JiraIssue> jiraIssueReleaseList;
    private Set<JiraIssue> subtaskDefectReleaseList;
    private List<JiraIssueCustomHistory> jiraIssueCustomHistoryList;
    private List<String> releaseList;

    /**
     * This method process scrum JIRA based kpi request, cache data and call service
     * in multiple thread.
     *
     * @param kpiRequest JIRA KPI request true if flow for precalculated, false for direct
     *                   flow.
     * @return List of KPI data
     * @throws EntityNotFoundException EntityNotFoundException
     */
    @SuppressWarnings({"PMD.AvoidCatchingGenericException", "unchecked"})
    public List<KpiElement> process(KpiRequest kpiRequest) throws EntityNotFoundException {

        log.info("Processing KPI calculation for data {}", kpiRequest.getKpiList());
        List<KpiElement> origRequestedKpis = kpiRequest.getKpiList().stream().map(KpiElement::new)
                .collect(Collectors.toList());
        List<KpiElement> responseList = new ArrayList<>();
        String[] projectKeyCache = null;
        try {
            Integer groupId = kpiRequest.getKpiList().get(0).getGroupId();
            String groupName = filterHelperService.getHierarachyLevelId(kpiRequest.getLevel(), kpiRequest.getLabel(),
                    false);
            if (null != groupName) {
                kpiRequest.setLabel(groupName.toUpperCase());
            } else {
                log.error("label name for selected hierarchy not found");
            }
            List<AccountHierarchyData> filteredAccountDataList = filterHelperService.getFilteredBuilds(kpiRequest,
                    groupName);

            if (!CollectionUtils.isEmpty(filteredAccountDataList)) {
                projectKeyCache = getProjectKeyCache(kpiRequest, filteredAccountDataList);

                filteredAccountDataList = getAuthorizedFilteredList(kpiRequest, filteredAccountDataList);
                if (filteredAccountDataList.isEmpty()) {
                    return responseList;
                }
                Object cachedData = cacheService.getFromApplicationCache(projectKeyCache, KPISource.JIRA.name(),
                        groupId, kpiRequest.getSprintIncluded());
                if (!kpiRequest.getRequestTrackerId().toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
                        && null != cachedData && isLeadTimeDuration(kpiRequest.getKpiList())) {
                    log.info("Fetching value from cache for {}", Arrays.toString(kpiRequest.getIds()));
                    return (List<KpiElement>) cachedData;
                }

                List<Node> filteredNodes = filteredAccountDataList.stream()
                        .flatMap(accountHierarchyData ->
                                accountHierarchyData.getNode().stream()
                                        .filter(node -> accountHierarchyData.getLeafNodeId().equalsIgnoreCase(node.getId()))
                        )
                        .collect(Collectors.toList());

                if (!CollectionUtils.isEmpty(origRequestedKpis)
                        && StringUtils.isNotEmpty(origRequestedKpis.get(0).getKpiCategory())) {
                    updateJiraIssueList(kpiRequest, origRequestedKpis, filteredAccountDataList, filteredNodes);
                }
                // set filter value to show on trend line. If subprojects are
                // in
                // selection then show subprojects on trend line else show
                // projects
                kpiRequest.setFilterToShowOnTrend(groupName);

                executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()); // 10 is just an example, adjust according to your needs

                List<Callable<Void>> listOfTask = new ArrayList<>();
                for (KpiElement kpiEle : kpiRequest.getKpiList()) {
                    listOfTask.add(new ParallelJiraServices(kpiRequest, responseList, kpiEle, filteredNodes.get(0)));
                }

                executorService.invokeAll(listOfTask);
                List<KpiElement> missingKpis = origRequestedKpis.stream()
                        .filter(reqKpi -> responseList.stream()
                                .noneMatch(responseKpi -> reqKpi.getKpiId().equals(responseKpi.getKpiId())))
                        .collect(Collectors.toList());
                responseList.addAll(missingKpis);

                setIntoApplicationCache(kpiRequest, responseList, groupId, projectKeyCache);
            } else {
                responseList.addAll(origRequestedKpis);
            }

        } catch (Exception e) {
            log.error("Error while KPI calculation for data {}", kpiRequest.getKpiList(), e);
            throw new HttpMessageNotWritableException(e.getMessage(), e);
        } finally {
            threadLocalJiraIssues.remove();
            threadLocalHistory.remove();
            threadReleaseIssues.remove();
            threadSubtaskDefects.remove();
            executorService.shutdown();
        }

        return responseList;
    }

    private void updateJiraIssueList(KpiRequest kpiRequest, List<KpiElement> origRequestedKpis,
                                     List<AccountHierarchyData> filteredAccountDataList, List<Node> filteredNodes) {
        if (origRequestedKpis.get(0).getKpiCategory().equalsIgnoreCase(CommonConstant.RELEASE)) {
            releaseList = getReleaseList(filteredNodes);
            fetchJiraIssues(filteredAccountDataList.get(0).getBasicProjectConfigId().toString(), releaseList,
                    CommonConstant.RELEASE);
            fetchJiraIssuesCustomHistory(filteredAccountDataList.get(0).getBasicProjectConfigId().toString(),
                    releaseList, CommonConstant.RELEASE);
            fetchJiraIssueReleaseForProject(filteredAccountDataList.get(0).getBasicProjectConfigId().toString(),
                    CommonConstant.RELEASE);
        }
    }

    /**
     * creating release List on the basis of releaseId
     *
     * @param filteredNodes
     * @return release names
     */
    private List<String> getReleaseList(List<Node> filteredNodes) {
        List<Node> nodes = filteredNodes;
        List<String> processedList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(nodes)) {
            nodes.forEach(releaseNode -> {
                String projectName = CommonConstant.UNDERSCORE + releaseNode.getProjectFilter().getName();
                processedList.add(releaseNode.getReleaseFilter().getName().split(projectName)[0]);
            });
        }
        return processedList;
    }

    /**
     * @param kpiRequest              kpiRequest
     * @param filteredAccountDataList filteredAccountDataList
     * @return list of AccountHierarchyData
     */
    private List<AccountHierarchyData> getAuthorizedFilteredList(KpiRequest kpiRequest,
                                                                 List<AccountHierarchyData> filteredAccountDataList) {
        kpiHelperService.kpiResolution(kpiRequest.getKpiList());
        if (!authorizedProjectsService.ifSuperAdminUser()) {
            filteredAccountDataList = authorizedProjectsService.filterProjects(filteredAccountDataList);
        }

        return filteredAccountDataList;
    }

    /**
     * @param kpiRequest              kpiRequest
     * @param filteredAccountDataList filteredAccountDataList
     */
    private String[] getProjectKeyCache(KpiRequest kpiRequest, List<AccountHierarchyData> filteredAccountDataList) {
        String[] projectKeyCache;
        if (!authorizedProjectsService.ifSuperAdminUser()) {
            projectKeyCache = authorizedProjectsService.getProjectKey(filteredAccountDataList, kpiRequest);
        } else {
            projectKeyCache = kpiRequest.getIds();
        }

        return projectKeyCache;
    }

    /**
     * @param kpiRequest   kpiRequest
     * @param responseList responseList
     * @param groupId      groupId
     */
    private void setIntoApplicationCache(KpiRequest kpiRequest, List<KpiElement> responseList, Integer groupId,
                                         String[] projects) {
        Integer sprintLevel = filterHelperService.getHierarchyIdLevelMap(false)
                .get(CommonConstant.HIERARCHY_LEVEL_ID_SPRINT);

        if (!kpiRequest.getRequestTrackerId().toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())
                && sprintLevel >= kpiRequest.getLevel() && isLeadTimeDuration(kpiRequest.getKpiList())) {
            cacheService.setIntoApplicationCache(projects, responseList, KPISource.JIRA.name(), groupId,
                    kpiRequest.getSprintIncluded());
        }

    }

    private boolean isLeadTimeDuration(List<KpiElement> kpiList) {
        return kpiList.size() != 1 || !kpiList.get(0).getKpiId().equalsIgnoreCase("kpi3");
    }

    public void fetchJiraIssues(String basicProjectConfigId, List<String> sprintIssuesList, String board) {
        if (board.equalsIgnoreCase(CommonConstant.RELEASE)) {
            jiraIssueReleaseList = jiraIssueRepository
                    .findByBasicProjectConfigIdAndReleaseVersionsReleaseNameIn(basicProjectConfigId, sprintIssuesList);
            Set<String> storyIDs = jiraIssueReleaseList.stream().filter(
                            jiraIssue -> !jiraIssue.getTypeName().equalsIgnoreCase(NormalizedJira.DEFECT_TYPE.getValue()))
                    .map(JiraIssue::getNumber).collect(Collectors.toSet());
            subtaskDefectReleaseList = fetchSubTaskDefectsRelease(basicProjectConfigId, storyIDs);
        }
    }

    public List<JiraIssue> getJiraIssuesForSelectedRelease() {
        return threadReleaseIssues.get();
    }


    public void fetchJiraIssuesCustomHistory(String basicProjectConfigId, List<String> sprintIssuesList, String board) {
        if (board.equalsIgnoreCase(CommonConstant.RELEASE)) {
            jiraIssueCustomHistoryList = jiraIssueCustomHistoryRepository.findByFilterAndFromReleaseMap(
                    Collections.singletonList(basicProjectConfigId),
                    CommonUtils.convertToPatternListForSubString(releaseList));
        }
    }

    public void fetchJiraIssueReleaseForProject(String basicProjectConfigId, String board) {

        if (board.equalsIgnoreCase(CommonConstant.BACKLOG) || board.equalsIgnoreCase(CommonConstant.RELEASE)) {
            jiraIssueReleaseStatus = jiraIssueReleaseStatusRepository.findByBasicProjectConfigId(basicProjectConfigId);
        }
    }

    /**
     * This method is used to fetch subtask defects which are not tagged to release
     *
     * @param projectConfigId projectConfigId
     * @param storyIDs        storyIDs
     * @return return
     */
    private Set<JiraIssue> fetchSubTaskDefectsRelease(String projectConfigId, Set<String> storyIDs) {
        ObjectId basicProjectConfigId = new ObjectId(projectConfigId);
        FieldMapping fieldMapping = configHelperService.getFieldMappingMap().get(basicProjectConfigId);
        if (CollectionUtils.isNotEmpty(storyIDs) && fieldMapping != null
                && CollectionUtils.isNotEmpty(fieldMapping.getJiraSubTaskDefectType())) {
            return jiraIssueRepository.findByBasicProjectConfigIdAndDefectStoryIDInAndOriginalTypeIn(projectConfigId,
                    storyIDs, fieldMapping.getJiraSubTaskDefectType());
        }
        return new HashSet<>();
    }

    public JiraIssueReleaseStatus getJiraIssueReleaseForProject() {
        return jiraIssueReleaseStatus;
    }

    public List<String> getReleaseList() {
        return releaseList;
    }

    public Set<JiraIssue> getSubTaskDefects() {
        return threadSubtaskDefects.get();
    }

    public class ParallelJiraServices implements Callable<Void>//extends RecursiveAction {
    {
        private static final long serialVersionUID = 1L;
        private final KpiRequest kpiRequest;
        private final transient List<KpiElement> responseList;
        private final transient KpiElement kpiEle;
        Node filteredAccountData;

        /*
         * @param kpiRequest
         *
         * @param responseList
         *
         * @param kpiEle
         *
         * @param treeAggregatorDetail
         */
        public ParallelJiraServices(KpiRequest kpiRequest, List<KpiElement> responseList, KpiElement kpiEle,
                                    Node filteredAccountData) {
            super();
            this.kpiRequest = kpiRequest;
            this.responseList = responseList;
            this.kpiEle = kpiEle;
            this.filteredAccountData = filteredAccountData;
        }

        /**
         * {@inheritDoc}
         *
         * @return
         */
        @SuppressWarnings("PMD.AvoidCatchingGenericException")
        @Override
        public Void call() {
            try {
                threadLocalJiraIssues.set(jiraIssueList);
                threadLocalHistory.set(jiraIssueCustomHistoryList);
                threadReleaseIssues.set(jiraIssueReleaseList);
                threadSubtaskDefects.set(subtaskDefectReleaseList);
                calculateAllKPIAggregatedMetrics(kpiRequest, responseList, kpiEle, filteredAccountData);
            } catch (Exception e) {
                log.error("[PARALLEL_JIRA_SERVICE].Exception occurred", e);
            }
            return null;
        }

        /**
         * This method call by multiple thread, take object of specific KPI and call
         * method of these KPIs
         *
         * @param kpiRequest              JIRA KPI request
         * @param responseList            List of KpiElements having data of each KPI
         * @param kpiElement              kpiElement object
         * @param filteredAccountNodeData filter tree object
         * @throws ApplicationException ApplicationException
         */
        private void calculateAllKPIAggregatedMetrics(KpiRequest kpiRequest, List<KpiElement> responseList,
                                                      KpiElement kpiElement, Node filteredAccountNodeData) throws ApplicationException {

            JiraReleaseKPIService<?, ?, ?> jiraKPIService = null;
            KPICode kpi = KPICode.getKPI(kpiElement.getKpiId());
            jiraKPIService = JiraReleaseKPIServiceFactory.getJiraKPIService(kpi.name());
            if (jiraKPIService == null) {
                throw new ApplicationException(JiraKPIServiceFactory.class, "Jira KPI Service Factory not initalized");
            }
            long startTime = System.currentTimeMillis();
            if (KPICode.THROUGHPUT.equals(kpi)) {
                log.info("No need to fetch Throughput KPI data");
            } else {
                Node nodeDataClone = (Node) SerializationUtils
                        .clone(filteredAccountNodeData);
                responseList.add(jiraKPIService.getKpiData(kpiRequest, kpiElement, nodeDataClone));

                long processTime = System.currentTimeMillis() - startTime;
                log.info("[JIRA-{}-TIME][{}]. KPI took {} ms", kpi.name(), kpiRequest.getRequestTrackerId(),
                        processTime);
            }
        }

    }


}
