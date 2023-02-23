/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.apis.zephyr.service;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.model.KPIExcelData;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintWiseStory;
import com.publicissapient.kpidashboard.common.model.zephyr.TestCaseDetails;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.zephyr.TestCaseDetailsRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This the specific KPI service class which calculates the KPI metric value.
 *
 * @author tauakram
 */
@Service
@Slf4j
public final class AutomationPercentageServiceImpl extends ZephyrKPIService<Double, List<Object>, Map<String, Object>> {

    private static final String TESTCASEKEY = "testCaseData";
    private static final String AUTOMATEDTESTCASEKEY = "automatedTestCaseData";
    private static final String SPRINTSTORIES = "storyData";
    private static final String AUTOMATED = "In-Sprint test cases automated";
    private static final String TOTAL = "Total In-Sprint test cases";
    private static final String DEV = "DeveloperKpi";
    private static final String ISSUE_DATA = "issueData";
    private static final String TOOL_ZEPHYR = ProcessorConstants.ZEPHYR;
    private static final String TOOL_JIRA_TEST = ProcessorConstants.JIRA_TEST;

    private static final String NIN = "nin";
    @Autowired
    private JiraIssueRepository jiraIssueRepository;
    @Autowired
    private TestCaseDetailsRepository testCaseDetailsRepository;
    @Autowired
    private ConfigHelperService configHelperService;
    @Autowired
    private KpiHelperService kpiHelperService;
    @Autowired
    private CustomApiConfig customApiConfig;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private FilterHelperService flterHelperService;

    @Override
    public String getQualifierType() {
        return KPICode.INSPRINT_AUTOMATION_COVERAGE.name();
    }

    @Override
    public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
                                 TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

        List<DataCount> trendValueList = new ArrayList<>();
        Node root = treeAggregatorDetail.getRoot();
        Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();
        treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
            if (Filters.getFilter(k) == Filters.SPRINT) {
                sprintWiseLeafNodeValue(mapTmp, v, trendValueList, kpiElement, kpiRequest);

            }

        });

        log.debug("[TEST-AUTOMATION-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
                kpiRequest.getRequestTrackerId(), root);

        Map<Pair<String, String>, Node> nodeWiseKPIValue = new HashMap<>();
        calculateAggregatedValue(root, nodeWiseKPIValue, KPICode.INSPRINT_AUTOMATION_COVERAGE);
        // 3rd change : remove code to set trendValuelist and call getTrendValues method
        List<DataCount> trendValues = getTrendValues(kpiRequest, nodeWiseKPIValue, KPICode.INSPRINT_AUTOMATION_COVERAGE);
        kpiElement.setTrendValueList(trendValues);

        return kpiElement;
    }

    @Override
    public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
                                                  KpiRequest kpiRequest) {

        Map<String, Object> resultListMap = new HashMap<>();
        Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
        List<String> sprintList = new ArrayList<>();
        List<String> basicProjectConfigIds = new ArrayList<>();
        Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
        Map<String, Map<String, Object>> uniqueProjectMapForTestCase = new HashMap<>();
        Map<String, String> sprintProjectIdMap = new HashMap<>();
        Map<ObjectId, Map<String, List<ProjectToolConfig>>> toolMap = (Map<ObjectId, Map<String, List<ProjectToolConfig>>>) cacheService
                .cacheProjectToolConfigMapData();
        Map<ObjectId, FieldMapping> basicProjetWiseConfig = configHelperService.getFieldMappingMap();
        leafNodeList.forEach(leaf -> {
            ObjectId basicProjectConfigId = leaf.getProjectFilter().getBasicProjectConfigId();
            List<ProjectToolConfig> zephyrTools = getToolConfigBasedOnProcessors(toolMap, basicProjectConfigId, TOOL_ZEPHYR);

            List<ProjectToolConfig> jiraTestTools = getToolConfigBasedOnProcessors(toolMap, basicProjectConfigId, TOOL_JIRA_TEST);


            Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
            Map<String, Object> mapOfFolderPathFilters = new LinkedHashMap<>();
            Map<String, Object> mapOfProjectFiltersNotIn = new LinkedHashMap<>();
            FieldMapping fieldMapping = basicProjetWiseConfig.get(basicProjectConfigId);

            sprintList.add(leaf.getSprintFilter().getId());
            basicProjectConfigIds.add(basicProjectConfigId.toString());
            sprintProjectIdMap.put(leaf.getSprintFilter().getId(), basicProjectConfigId.toString());

            mapOfProjectFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
                    CommonUtils.convertToPatternList(fieldMapping.getJiraTestAutomationIssueType()));
            // if Zephyr scale as a tool is setup with project
            if (CollectionUtils.isNotEmpty(zephyrTools)) {
                List<String> sprintAutomationFolderPath = new ArrayList<>();
                zephyrTools.forEach(tool -> {
                    if (CollectionUtils.isNotEmpty(tool.getInSprintAutomationFolderPath())) {
                        sprintAutomationFolderPath.addAll(tool.getInSprintAutomationFolderPath());
                    }
                });
                if (CollectionUtils.isNotEmpty(sprintAutomationFolderPath)) {
                    mapOfFolderPathFilters.put(JiraFeature.ATM_TEST_FOLDER.getFieldValueInFeature(),
                            CommonUtils.convertTestFolderToPatternList(sprintAutomationFolderPath));
                }

                uniqueProjectMapForTestCase.put(basicProjectConfigId.toString(), mapOfFolderPathFilters);
            }
            // if Zephyr squad as a jira plguin is setup with project
			if (CollectionUtils.isNotEmpty(jiraTestTools)) {
				jiraTestTools.forEach(tool -> {
					if (CollectionUtils.isNotEmpty(tool.getTestCaseStatus())) {
						mapOfProjectFiltersNotIn.put(JiraFeature.TEST_CASE_STATUS.getFieldValueInFeature(),
								CommonUtils.convertTestFolderToPatternList(tool.getTestCaseStatus()));
					}
				});
                uniqueProjectMapForTestCase.put(basicProjectConfigId.toString(), mapOfProjectFiltersNotIn);
			}

            uniqueProjectMap.put(basicProjectConfigId.toString(), mapOfProjectFilters);

        });
        // additional filter
        createAdditionalFilterMap(kpiRequest, mapOfFilters, DEV, flterHelperService);

        mapOfFilters.put(JiraFeature.SPRINT_ID.getFieldValueInFeature(),
                sprintList.stream().distinct().collect(Collectors.toList()));
        mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
                basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));


        List<SprintWiseStory> sprintWiseStoryList = jiraIssueRepository.findIssuesGroupBySprint(mapOfFilters,
                uniqueProjectMap, kpiRequest.getFilterToShowOnTrend(), DEV);

        Map<String, List<String>> projectStoryNumberMap = new HashMap<>();
        List<String> storyIdList = new ArrayList<>();
        sprintWiseStoryList.forEach(s -> {
            String basicProjConfId = sprintProjectIdMap.get(s.getSprint());
            projectStoryNumberMap.putIfAbsent(basicProjConfId, new ArrayList<>());
            projectStoryNumberMap.get(basicProjConfId).addAll(s.getStoryList());
            storyIdList.addAll(s.getStoryList());
        });

        projectStoryNumberMap.forEach((k, v) -> {
            Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
            uniqueProjectMapForTestCase.putIfAbsent(k, mapOfProjectFilters);
            uniqueProjectMapForTestCase.get(k).put(JiraFeature.DEFECT_STORY_ID.getFieldValueInFeature(),
                    v.stream().distinct().collect(Collectors.toList()));
        });
        Map<String, List<String>> mapOfFiltersStoryQuery = new LinkedHashMap<>();
        mapOfFiltersStoryQuery.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
                basicProjectConfigIds.stream().distinct().collect(Collectors.toList()));
        mapOfFiltersStoryQuery.put(JiraFeature.CAN_TEST_AUTOMATED.getFieldValueInFeature(),
                Arrays.asList(NormalizedJira.YES_VALUE.getValue()));
        mapOfFiltersStoryQuery.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
                Arrays.asList(NormalizedJira.TEST_TYPE.getValue()));


        List<TestCaseDetails> testCasesList = testCaseDetailsRepository.findTestDetails(mapOfFiltersStoryQuery,
                uniqueProjectMapForTestCase, NIN);

        resultListMap.put(SPRINTSTORIES, sprintWiseStoryList);
        resultListMap.put(TESTCASEKEY, testCasesList);
        resultListMap.put(ISSUE_DATA, jiraIssueRepository.findIssueAndDescByNumber(storyIdList));
        return resultListMap;

    }

    @SuppressWarnings("unchecked")
    @Override
    public Double calculateKPIMetrics(Map<String, Object> filterComponentIdWiseDefectMap) {
        Double automatedPercentage = 0d;
        List<JiraIssue> automatedTest = (List<JiraIssue>) filterComponentIdWiseDefectMap.get(AUTOMATEDTESTCASEKEY);
        List<JiraIssue> totalTest = (List<JiraIssue>) filterComponentIdWiseDefectMap.get(TESTCASEKEY);
        if (CollectionUtils.isNotEmpty(automatedTest) && CollectionUtils.isNotEmpty(totalTest)) {
            int automatedCount = automatedTest.size();
            int totalCount = totalTest.size();
            automatedPercentage = (double) Math.round((100.0 * automatedCount) / (totalCount));
        }
        return automatedPercentage;
    }

    /**
     * Populates KPI value to sprint leaf nodes. It also gives the trend analysis at
     * sprint wise.
     *
     * @param mapTmp
     * @param sprintLeafNodeList
     */
    @SuppressWarnings("unchecked")
    private void sprintWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> sprintLeafNodeList,
                                         List<DataCount> trendValueList, KpiElement kpiElement, KpiRequest kpiRequest) {

        String requestTrackerId = getRequestTrackerId();
        Collections.sort(sprintLeafNodeList, (Node o1, Node o2) -> o1.getSprintFilter().getStartDate()
                .compareTo(o2.getSprintFilter().getStartDate()));

        Map<String, Object> defectDataListMap = fetchKPIDataFromDb(sprintLeafNodeList, null, null, kpiRequest);

        List<SprintWiseStory> sprintWiseStoryList = (List<SprintWiseStory>) defectDataListMap.get(SPRINTSTORIES);

        Map<Pair<String, String>, List<SprintWiseStory>> sprintWiseMap = sprintWiseStoryList.stream().collect(Collectors
                .groupingBy(sws -> Pair.of(sws.getBasicProjectConfigId(), sws.getSprint()), Collectors.toList()));

        List<TestCaseDetails> testCaseList = (List<TestCaseDetails>) defectDataListMap.get(TESTCASEKEY);
        Map<String, Set<JiraIssue>> projectWiseStories = ((List<JiraIssue>) defectDataListMap.get(ISSUE_DATA)).stream().collect(Collectors.groupingBy(JiraIssue::getBasicProjectConfigId, Collectors.toSet()));

        Map<Pair<String, String>, List<TestCaseDetails>> sprintWiseAutoTestMap = new HashMap<>();
        Map<Pair<String, String>, List<TestCaseDetails>> sprintWiseTotalTestMap = new HashMap<>();
        Map<Pair<String, String>, Double> sprintWisePercentage = new HashMap<>();

        sprintWiseMap.forEach((sprintFilter, sprintWiseStories) -> {
            List<TestCaseDetails> sprintWiseAutomatedTestList = new ArrayList<>();
            List<TestCaseDetails> sprintWiseTotalTestList = new ArrayList<>();

            sprintWiseAutomatedTestList.addAll(automatedTestCases(testCaseList, sprintWiseStories));
            sprintWiseTotalTestList.addAll(totalTestCases(testCaseList, sprintWiseStories));
            Map<String, Object> currentSprintLeafNodeDefectDataMap = new HashMap<>();
            currentSprintLeafNodeDefectDataMap.put(AUTOMATEDTESTCASEKEY,
                    automatedTestCases(testCaseList, sprintWiseStories));
            currentSprintLeafNodeDefectDataMap.put(TESTCASEKEY, totalTestCases(testCaseList, sprintWiseStories));

            Double autoPercentage = calculateKPIMetrics(currentSprintLeafNodeDefectDataMap);

            sprintWisePercentage.put(sprintFilter, autoPercentage);
            sprintWiseAutoTestMap.put(sprintFilter, sprintWiseAutomatedTestList);
            sprintWiseTotalTestMap.put(sprintFilter, sprintWiseTotalTestList);
        });
        List<KPIExcelData> excelData = new ArrayList<>();
        sprintLeafNodeList.forEach(node -> {
            // Leaf node wise data
            String trendLineName = node.getProjectFilter().getName();
            Pair<String, String> currentNodeIdentifier = Pair
                    .of(node.getProjectFilter().getBasicProjectConfigId().toString(), node.getSprintFilter().getId());
            String validationKey = node.getSprintFilter().getName();

            Map<String, Object> howerMap = new LinkedHashMap<>();
            Map<String, Object> currentSprintLeafNodeDefectDataMap = new HashMap<>();
            currentSprintLeafNodeDefectDataMap.put(AUTOMATEDTESTCASEKEY,
                    sprintWiseAutoTestMap.get(currentNodeIdentifier));
            currentSprintLeafNodeDefectDataMap.put(TESTCASEKEY, sprintWiseTotalTestMap.get(currentNodeIdentifier));
            double automationForCurrentLeaf = 0.0;
            if (null != sprintWisePercentage.get(currentNodeIdentifier)) {
                automationForCurrentLeaf = sprintWisePercentage.get(currentNodeIdentifier);
            }
            mapTmp.get(node.getId()).setValue(automationForCurrentLeaf);
            populateExcelDataObject(requestTrackerId, currentSprintLeafNodeDefectDataMap,
                    excelData, validationKey, projectWiseStories.get(node.getProjectFilter().getBasicProjectConfigId().toString()));
            log.debug("[TEST-AUTOMATION-SPRINT-WISE][{}]. TEST-AUTOMATION for sprint {}  is {}", requestTrackerId,
                    node.getSprintFilter().getName(), automationForCurrentLeaf);
            setHowerMap(sprintWiseAutoTestMap, sprintWiseTotalTestMap, currentNodeIdentifier, howerMap);
            DataCount dataCount = new DataCount();
            dataCount.setData(String.valueOf(Math.round(automationForCurrentLeaf)));
            dataCount.setSProjectName(trendLineName);
            dataCount.setSprintIds(new ArrayList<>(Arrays.asList(node.getSprintFilter().getId())));
            dataCount.setSprintNames(new ArrayList<>(Arrays.asList(node.getSprintFilter().getName())));
            dataCount.setSSprintID(node.getSprintFilter().getId());
            dataCount.setSSprintName(node.getSprintFilter().getName());
            dataCount.setHoverValue(howerMap);
            dataCount.setValue(automationForCurrentLeaf);
            mapTmp.get(node.getId()).setValue(new ArrayList<>(Arrays.asList(dataCount)));
            trendValueList.add(dataCount);

        });
        kpiElement.setExcelData(excelData);
        kpiElement.setExcelColumns(KPIExcelColumn.INSPRINT_AUTOMATION_COVERAGE.getColumns());
    }

    /**
     * @param testCaseList
     * @return
     */
    private List<TestCaseDetails> automatedTestCases(List<TestCaseDetails> testCaseList,
                                                     List<SprintWiseStory> sprintWiseStory) {
        return testCaseList.stream()
                .filter(tc -> sprintWiseStory.stream()
                        .anyMatch(st -> st.getBasicProjectConfigId().equals(tc.getBasicProjectConfigId())
                                && CollectionUtils.isNotEmpty(tc.getDefectStoryID())
                                && CollectionUtils.containsAny(tc.getDefectStoryID(), st.getStoryList())
                                && NormalizedJira.YES_VALUE.getValue().equals(tc.getIsTestAutomated())))
                .collect(Collectors.toList());
    }

    /**
     * @param testCaseList
     * @return
     */
    private List<TestCaseDetails> totalTestCases(List<TestCaseDetails> testCaseList,
                                                 List<SprintWiseStory> sprintWiseStory) {
        return testCaseList.stream()
                .filter(tc -> sprintWiseStory.stream()
                        .anyMatch(st -> st.getBasicProjectConfigId().equals(tc.getBasicProjectConfigId())
                                && CollectionUtils.isNotEmpty(tc.getDefectStoryID())
                                && CollectionUtils.containsAny(tc.getDefectStoryID(), st.getStoryList())))
                .collect(Collectors.toList());
    }


    private void populateExcelDataObject(String requestTrackerId,
                                         Map<String, Object> currentSprintLeafNodeDefectDataMap, List<KPIExcelData> excelData,
                                         String sprint, Set<JiraIssue> jiraIssues) {

        if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
            List<TestCaseDetails> automatedTest = (List<TestCaseDetails>) currentSprintLeafNodeDefectDataMap
                    .get(AUTOMATEDTESTCASEKEY);
            List<TestCaseDetails> totalTest = (List<TestCaseDetails>) currentSprintLeafNodeDefectDataMap
                    .get(TESTCASEKEY);
            KPIExcelUtility.populateInSprintAutomationExcelData(sprint, totalTest, automatedTest, jiraIssues, excelData);
        }
    }

    /**
     * Sets Hower map
     *  @param sprintId
     * @param howerMap
     */
    private void setHowerMap(Map<Pair<String, String>, List<TestCaseDetails>> sprintWiseAutomatedMap,
                             Map<Pair<String, String>, List<TestCaseDetails>> sprintWiseTotalMap, Pair<String, String> sprintId,
                             Map<String, Object> howerMap) {
        if (CollectionUtils.isNotEmpty(sprintWiseAutomatedMap.get(sprintId))) {
            howerMap.put(AUTOMATED, sprintWiseAutomatedMap.get(sprintId).size());
        } else {
            howerMap.put(AUTOMATED, 0);
        }
        if (CollectionUtils.isNotEmpty(sprintWiseTotalMap.get(sprintId))) {
            howerMap.put(TOTAL, sprintWiseTotalMap.get(sprintId).size());
        } else {
            howerMap.put(TOTAL, 0);
        }
    }

    @Override
    public Double calculateKpiValue(List<Double> valueList, String kpiName) {
        return calculateKpiValueForDouble(valueList, kpiName);
    }

}
