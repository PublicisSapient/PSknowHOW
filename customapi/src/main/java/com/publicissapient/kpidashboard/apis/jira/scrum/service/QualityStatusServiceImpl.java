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

package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import static com.publicissapient.kpidashboard.apis.util.IterationKpiHelper.transformIterSprintdetail;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.JiraFeature;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.iterationdashboard.JiraIterationKPIService;
import com.publicissapient.kpidashboard.apis.model.IssueKpiModalValue;
import com.publicissapient.kpidashboard.apis.model.KpiData;
import com.publicissapient.kpidashboard.apis.model.KpiDataGroup;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.IterationKpiHelper;
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintIssue;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class QualityStatusServiceImpl extends JiraIterationKPIService {
	public static final String LINKED_DEFECTS = "Story Linked Defects";
	public static final String UNLINKED_DEFECTS = "Unlinked Defects";
	public static final String DIR = "DIR";
	public static final String DEFECT_DENSITY = "Defect Density";
	public static final String LINKED_ISSUES = "linkedIssues";
	private static final String TOTAL_ISSUES = "totalIssues";
	private static final String COMPLETED_ISSUES = "completedIssue";
	private static final String NOT_APPLICABLE = "N/A";
	public static final DecimalFormat decformat = new DecimalFormat("#0.00");
	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private CustomApiConfig customApiConfig;

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement, Node sprintNode)
			throws ApplicationException {
		projectWiseLeafNodeValue(sprintNode, kpiElement, kpiRequest);
		return kpiElement;
	}

	@Override
	public String getQualifierType() {
		return KPICode.QUALITY_STATUS.name();
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(Node leafNode, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();

		Map<String, List<String>> mapOfFilters = new LinkedHashMap<>();
		Map<String, Map<String, Object>> uniqueProjectMap = new HashMap<>();
		if (null != leafNode) {

			log.info("Quality Status  -> Requested sprint : {}", leafNode.getName());
			String basicProjectConfigId = leafNode.getProjectFilter().getBasicProjectConfigId().toString();

			List<String> defectType = new ArrayList<>();
			SprintDetails dbSprintDetail = getSprintDetailsFromBaseClass();
			SprintDetails sprintDetails;
			if (null != dbSprintDetail) {
				FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
						.get(leafNode.getProjectFilter().getBasicProjectConfigId());
				// to modify sprint details on the basis of configuration for the project
				List<JiraIssueCustomHistory> totalHistoryList = getJiraIssuesCustomHistoryFromBaseClass();
				List<JiraIssue> totalJiraIssueList = getJiraIssuesFromBaseClass();
				Set<String> issueList = totalJiraIssueList.stream().map(JiraIssue::getNumber).collect(Collectors.toSet());

				sprintDetails = transformIterSprintdetail(totalHistoryList, issueList, dbSprintDetail, new ArrayList<>(),
						fieldMapping.getJiraIterationCompletionStatusKPI133(),
						leafNode.getProjectFilter().getBasicProjectConfigId());

				List<String> totalIssue = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.TOTAL_ISSUES);
				List<String> completedIssue = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.COMPLETED_ISSUES);
				List<String> defectTypes = new ArrayList<>(
						Optional.ofNullable(fieldMapping.getJiradefecttype()).orElse(Collections.emptyList()));
				Set<String> totalSprintReportDefects = new HashSet<>();
				Set<String> totalSprintReportStories = new HashSet<>();
				sprintDetails.getTotalIssues().forEach(sprintIssue -> separateDefectAndStories(sprintIssue, defectTypes,
						totalSprintReportDefects, totalSprintReportStories));

				Map<String, Object> mapOfProjectFilters = new LinkedHashMap<>();
				defectType.add(NormalizedJira.DEFECT_TYPE.getValue());
				mapOfProjectFilters.put(JiraFeature.ISSUE_TYPE.getFieldValueInFeature(),
						CommonUtils.convertToPatternList(defectType));
				uniqueProjectMap.put(basicProjectConfigId, mapOfProjectFilters);
				mapOfFilters.put(JiraFeature.BASIC_PROJECT_CONFIG_ID.getFieldValueInFeature(),
						Collections.singletonList(basicProjectConfigId));
				List<String> typeNameList = new ArrayList<>(
						Optional.ofNullable(fieldMapping.getJiraItrQSIssueTypeKPI133()).orElse(Collections.emptyList()));
				if (CollectionUtils.isNotEmpty(totalIssue)) {
					List<JiraIssue> filteredJiraIssue = IterationKpiHelper.getFilteredJiraIssue(totalIssue, totalJiraIssueList);
					Set<JiraIssue> sprintReportIssueList = KpiDataHelper.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(
							sprintDetails, sprintDetails.getTotalIssues(), filteredJiraIssue);

					sprintReportIssueList = getTypeNameFilterJiraIssueList(defectTypes, typeNameList, sprintReportIssueList); // has
					// present
					// sprint
					// issue
					// based
					// on
					// getJiraItrQSIssueTypeKPI133
					// and
					// defectType

					sprintReportIssueList = getLabelFilteredJiraIssues(fieldMapping, defectTypes, sprintReportIssueList);

					// fetched all defects which is linked to current sprint report stories
					List<JiraIssue> linkedDefects = jiraIssueRepository.findLinkedDefects(mapOfFilters, totalSprintReportStories,
							uniqueProjectMap);

					// filter defects whose issue type not coming in sprint report
					List<JiraIssue> subTaskDefects = linkedDefects.stream()
							.filter(jiraIssue -> !totalSprintReportDefects.contains(jiraIssue.getNumber())).toList();

					List<JiraIssue> jiraIssueDefects = sprintReportIssueList.stream()
							.filter(jiraIssue -> totalSprintReportDefects.contains(jiraIssue.getNumber())).toList();

					// forming linked story ids, which may lies outside selected sprint ,fix for
					// DTS-24813
					Set<String> linkedStories = jiraIssueDefects.stream().map(JiraIssue::getDefectStoryID).flatMap(Set::stream)
							.collect(Collectors.toSet());

					List<JiraIssue> jiraIssueLinkedStories = jiraIssueRepository
							.findByNumberInAndBasicProjectConfigId(new ArrayList<>(linkedStories), basicProjectConfigId);

					jiraIssueLinkedStories = getJiraIssueLinkedStories(typeNameList, jiraIssueLinkedStories);

					Set<JiraIssue> jiraIssueLinkedStoriesSet = new HashSet<>(jiraIssueLinkedStories);

					List<JiraIssue> totalIssues = new ArrayList<>();
					totalIssues.addAll(sprintReportIssueList);
					totalIssues.addAll(subTaskDefects);
					jiraIssueLinkedStoriesSet = getLabelFilteredJiraIssues(fieldMapping, defectTypes, jiraIssueLinkedStoriesSet);
					resultListMap.put(TOTAL_ISSUES, totalIssues);
					resultListMap.put(LINKED_ISSUES, new ArrayList<>(jiraIssueLinkedStoriesSet));
				}
				if (CollectionUtils.isNotEmpty(completedIssue)) {
					List<JiraIssue> completedIssueList = IterationKpiHelper.getFilteredJiraIssue(completedIssue,
							totalJiraIssueList);
					Set<JiraIssue> completedJiraIssue = KpiDataHelper.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(
							sprintDetails, sprintDetails.getCompletedIssues(), completedIssueList);
					completedJiraIssue = getTypeNameFilterJiraIssueList(defectTypes, typeNameList, completedJiraIssue);
					completedJiraIssue = getLabelFilteredJiraIssues(fieldMapping, defectTypes, completedJiraIssue);
					resultListMap.put(COMPLETED_ISSUES, new ArrayList<>(completedJiraIssue));
				} else
					resultListMap.put(COMPLETED_ISSUES, new ArrayList<>());
			}
		}
		return resultListMap;
	}

	private static Set<JiraIssue> getLabelFilteredJiraIssues(FieldMapping fieldMapping, List<String> defectTypes,
			Set<JiraIssue> jiraIssueSet) {
		if (CollectionUtils.isNotEmpty(fieldMapping.getJiraLabelsKPI133()) && CollectionUtils.isNotEmpty(defectTypes)) {
			jiraIssueSet = jiraIssueSet.stream()
					.filter(jiraIssue -> defectTypes.contains(jiraIssue.getTypeName()) ||
							fieldMapping.getJiraLabelsKPI133().stream().anyMatch(label -> jiraIssue.getLabels().contains(label)))
					.collect(Collectors.toSet());
		}
		return jiraIssueSet;
	}

	private static void separateDefectAndStories(SprintIssue sprintIssue, List<String> defectTypes,
			Set<String> totalSprintReportDefects, Set<String> totalSprintReportStories) {
		if (defectTypes.contains(sprintIssue.getTypeName())) {
			totalSprintReportDefects.add(sprintIssue.getNumber());
		} else {
			totalSprintReportStories.add(sprintIssue.getNumber());
		}
	}

	private static List<JiraIssue> getJiraIssueLinkedStories(List<String> typeNameList,
			List<JiraIssue> jiraIssueLinkedStories) {
		if (CollectionUtils.isNotEmpty(typeNameList)) {
			jiraIssueLinkedStories = jiraIssueLinkedStories.stream()
					.filter(jiraIssue -> typeNameList.contains(jiraIssue.getTypeName())).collect(Collectors.toList());
		}
		return jiraIssueLinkedStories;
	}

	private static Set<JiraIssue> getTypeNameFilterJiraIssueList(List<String> defectTypes, List<String> typeNameList,
			Set<JiraIssue> sprintReportIssueList) {
		if (CollectionUtils.isNotEmpty(typeNameList)) {
			typeNameList.addAll(defectTypes);
			sprintReportIssueList = sprintReportIssueList.stream()
					.filter(jiraIssue -> typeNameList.contains(jiraIssue.getTypeName())).collect(Collectors.toSet());
		}
		return sprintReportIssueList;
	}

	/**
	 * Populates KPI value to sprint leaf nodes and gives the trend analysis at
	 * sprint level.
	 *
	 * @param latestSprint
	 * @param kpiElement
	 * @param kpiRequest
	 */
	@SuppressWarnings("unchecked")
	private void projectWiseLeafNodeValue(Node latestSprint, KpiElement kpiElement, KpiRequest kpiRequest) {

		String startDate = latestSprint.getSprintFilter().getStartDate();

		String endDate = latestSprint.getSprintFilter().getEndDate();

		Map<String, Object> resultMap = fetchKPIDataFromDb(latestSprint, startDate, endDate, kpiRequest);

		if (CollectionUtils.isNotEmpty((List<JiraIssue>) resultMap.get(TOTAL_ISSUES))) {
			List<JiraIssue> jiraIssueList = (List<JiraIssue>) resultMap.get(TOTAL_ISSUES);
			List<JiraIssue> completedIssueList = (List<JiraIssue>) resultMap.get(COMPLETED_ISSUES);
			List<JiraIssue> jiraIssueLinkedIssues = (List<JiraIssue>) resultMap.get(LINKED_ISSUES);
			List<JiraIssue> totalJiraIssues = new ArrayList<>();
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(Objects.requireNonNull(latestSprint).getProjectFilter().getBasicProjectConfigId());
			Map<String, List<String>> projectWisePriority = new HashMap<>();
			Map<String, List<String>> configPriority = customApiConfig.getPriority();
			Map<String, Set<String>> projectWiseRCA = new HashMap<>();
			Map<String, Map<String, List<String>>> droppedDefects = new HashMap<>();

			KpiHelperService.addPriorityProjectWise(projectWisePriority, configPriority, latestSprint,
					fieldMapping.getDefectPriorityKPI133());
			KpiHelperService.addRCAProjectWise(projectWiseRCA, latestSprint, fieldMapping.getIncludeRCAForKPI133());
			KpiHelperService.getDroppedDefectsFilters(droppedDefects,
					latestSprint.getProjectFilter().getBasicProjectConfigId(), fieldMapping.getResolutionTypeForRejectionKPI133(),
					fieldMapping.getJiraDefectRejectionStatusKPI133());
			KpiHelperService.getDefectsWithoutDrop(droppedDefects, jiraIssueList, totalJiraIssues);

			List<String> defectTypes = new ArrayList<>(
					Optional.ofNullable(fieldMapping.getJiradefecttype()).orElse(Collections.emptyList()));
			defectTypes.add(NormalizedJira.DEFECT_TYPE.getValue());
			List<JiraIssue> allDefects = totalJiraIssues.stream().filter(issue -> defectTypes.contains(issue.getTypeName()))
					.collect(Collectors.toList());
			allDefects = KpiHelperService.excludePriorityAndIncludeRCA(allDefects, projectWisePriority, projectWiseRCA);
			List<JiraIssue> allStory = totalJiraIssues.stream().filter(issue -> !defectTypes.contains(issue.getTypeName()))
					.toList();
			List<JiraIssue> allClosedStory = completedIssueList.stream()
					.filter(issue -> !defectTypes.contains(issue.getTypeName())).toList();
			// adding all closed stories
			List<JiraIssue> closedPlusOpenLinkedStories = new ArrayList<>(allClosedStory);

			if (CollectionUtils.isNotEmpty(allDefects)) {
				List<JiraIssue> linkedDefectList = new ArrayList<>();
				List<JiraIssue> unlinkedDefectList = new ArrayList<>();
				Map<String, JiraIssue> linkedIssueMap = jiraIssueLinkedIssues.stream()
						.collect(Collectors.toMap(JiraIssue::getNumber, Function.identity()));

				// Creating map of modal Objects
				Map<String, IssueKpiModalValue> issueKpiModalObject = KpiDataHelper.createMapOfIssueModal(totalJiraIssues);
				Set<IssueKpiModalValue> issueData = new HashSet<>();

				for (JiraIssue jiraIssue : allDefects) {
					createLinkDefectListAndUnlinkDefectModal(issueKpiModalObject, linkedDefectList, unlinkedDefectList, jiraIssue,
							totalJiraIssues, fieldMapping, completedIssueList, linkedIssueMap, issueData);
				}
				Set<String> linkedStoriesSet = linkedDefectList.stream().map(JiraIssue::getDefectStoryID).flatMap(Set::stream)
						.collect(Collectors.toSet());
				List<JiraIssue> linkedStoriesJiraIssueList = allStory.stream()
						.filter(jiraIssue -> linkedStoriesSet.contains(jiraIssue.getNumber())).toList();

				// adding all LinkedStories
				closedPlusOpenLinkedStories.addAll(linkedStoriesJiraIssueList);
				// Removing duplicates if any
				closedPlusOpenLinkedStories = closedPlusOpenLinkedStories.stream().distinct().collect(Collectors.toList());

				double overAllDefectDensity = calculateDefectDensity(closedPlusOpenLinkedStories, linkedDefectList,
						fieldMapping);
				double overAllDir = calculateDIR(closedPlusOpenLinkedStories, linkedDefectList);
				createOverallLinkedModal(issueKpiModalObject, linkedDefectList, totalJiraIssues, completedIssueList,
						fieldMapping, issueData);

				KpiDataGroup dataGroup = new KpiDataGroup();
				List<KpiData> dataGroup1 = new ArrayList<>();
				dataGroup1.add(createKpiData(DIR, 1, Constant.PERCENTAGE, overAllDir));
				dataGroup1.add(createKpiData(DEFECT_DENSITY, 2, "", overAllDefectDensity));
				dataGroup1.add(createKpiData(LINKED_DEFECTS, 3, "", (double) linkedDefectList.size()));
				dataGroup1.add(createKpiData(UNLINKED_DEFECTS, 4, "", (double) unlinkedDefectList.size()));
				dataGroup.setDataGroup1(dataGroup1);

				Map<String, String> markerInfo = new HashMap<>();
				markerInfo.put(Constant.GREEN, "Represent the open linked stories");
				dataGroup.setMarkerInfo(markerInfo);
				dataGroup.setMetaDataColumns(List.of("marker"));

				kpiElement.setSprint(latestSprint.getName());
				kpiElement.setModalHeads(KPIExcelColumn.QUALITY_STATUS.getColumns());
				kpiElement.setIssueData(issueData);
				kpiElement.setDataGroup(dataGroup);
			}
		}
	}

	/**
	 * Creates kpi data object.
	 *
	 * @param name
	 * @param order
	 * @param unit
	 * @return
	 */
	private KpiData createKpiData(String name, Integer order, String unit, Double kpiValue) {
		KpiData data = new KpiData();
		data.setName(name);
		data.setOrder(order);
		data.setUnit(unit);
		data.setShowAsLegend(false);
		data.setKpiValue(kpiValue);
		return data;
	}

	private void createOverallLinkedModal(Map<String, IssueKpiModalValue> issueKpiModalObject,
			List<JiraIssue> linkedDefectList, List<JiraIssue> totalJiraIssues, List<JiraIssue> completedIssueList,
			FieldMapping fieldMapping, Set<IssueKpiModalValue> issueData) {
		Map<String, List<JiraIssue>> storyWithLinkedDefects = new HashMap<>();
		Map<String, JiraIssue> totalStoriesMap = totalJiraIssues.stream()
				.collect(Collectors.toMap(JiraIssue::getNumber, Function.identity()));
		for (JiraIssue jiraIssue : linkedDefectList) {
			Set<String> storyIds = jiraIssue.getDefectStoryID();
			for (String storyId : storyIds) {
				if (totalStoriesMap.get(storyId) != null && storyWithLinkedDefects.containsKey(storyId))
					storyWithLinkedDefects.get(storyId).add(jiraIssue);
				else if (totalStoriesMap.get(storyId) != null) {
					List<JiraIssue> defects = new ArrayList<>();
					defects.add(jiraIssue);
					storyWithLinkedDefects.put(storyId, defects);
				}
			}
		}

		storyWithLinkedDefects.forEach((storyId, defects) -> {
			JiraIssue jiraIssue = totalStoriesMap.get(storyId);
			List<JiraIssue> jiraIssueList = new ArrayList<>();
			jiraIssueList.add(jiraIssue);
			KPIExcelUtility.populateIssueModal(jiraIssue, fieldMapping, issueKpiModalObject);
			IssueKpiModalValue jiraIssueModalObject = issueKpiModalObject.get(jiraIssue.getNumber());
			issueData.add(jiraIssueModalObject);
			Map<String, String> linkedDefects = defects.stream().collect(Collectors
					.toMap(jiraIssue1 -> jiraIssue1.getNumber() + " ( " + jiraIssue1.getPriority() + " ) ", JiraIssue::getUrl));
			jiraIssueModalObject.setLinkedDefefect(linkedDefects);
			jiraIssueModalObject.setDefectInjectRate(String.valueOf(defects.size()));
			jiraIssueModalObject
					.setDefectDensity(String.valueOf(calculateDefectDensity(jiraIssueList, defects, fieldMapping)));
			if (jiraIssueModalObject.getIssueSize() == null)
				jiraIssueModalObject.setIssueSize("0.0");
			if (!completedIssueList.contains(jiraIssue)) {
				jiraIssueModalObject.setMarker(Constant.GREEN);
			}
		});
	}

	/**
	 * calculate based on total linked defects created this sprint divide by Sum of
	 * the story point of closed + open linked stories
	 *
	 * @param closedPlusOpenLinkedStories
	 * @param linkedDefect
	 * @param fieldMapping
	 * @return
	 */
	private double calculateDefectDensity(List<JiraIssue> closedPlusOpenLinkedStories, List<JiraIssue> linkedDefect,
			FieldMapping fieldMapping) {

		if (CollectionUtils.isEmpty(linkedDefect)) {
			return 0;
		}

		if (CollectionUtils.isEmpty(closedPlusOpenLinkedStories)) {
			return 0;
		}
		if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria()) &&
				fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
			double storyPointSum = closedPlusOpenLinkedStories.stream()
					.filter(jiraIssue -> Objects.nonNull(jiraIssue.getStoryPoints())).mapToDouble(JiraIssue::getStoryPoints)
					.sum();
			return (Math.round(100.0 * (storyPointSum == 0 ? 0 : linkedDefect.size() / storyPointSum)) / 100.0);
		} else {
			int originalEstimateMinutesSum = closedPlusOpenLinkedStories.stream()
					.filter(jiraIssue -> Objects.nonNull(jiraIssue.getOriginalEstimateMinutes()))
					.mapToInt(JiraIssue::getOriginalEstimateMinutes).sum();
			double originalEstimateInDays = (double) originalEstimateMinutesSum / 480;
			return originalEstimateInDays == 0
					? 0
					: Double.parseDouble(decformat.format(linkedDefect.size() / originalEstimateInDays));
		}
	}

	/**
	 * calculate based on total linked defects divide by total closed + open linked
	 * stories
	 *
	 * @param closedPlusOpenLinkedStories
	 * @param linkedDefect
	 * @return
	 */
	private double calculateDIR(List<JiraIssue> closedPlusOpenLinkedStories, List<JiraIssue> linkedDefect) {

		if (CollectionUtils.isEmpty(linkedDefect)) {
			return 0;
		}

		if (CollectionUtils.isEmpty(closedPlusOpenLinkedStories)) {
			return 0;
		}
		return Math.round(((double) linkedDefect.size() / closedPlusOpenLinkedStories.size()) * 100);
	}

	/**
	 * This is a Java method that takes in a set of parameters and checks Jira issue
	 * and adds it to either a list of linked or unlinked defects accordingly.
	 */
	private void createLinkDefectListAndUnlinkDefectModal(Map<String, IssueKpiModalValue> issueKpiModalObject, // NOSONAR
			List<JiraIssue> linkedDefect, List<JiraIssue> unlinkedDefect, JiraIssue jiraIssue,
			List<JiraIssue> totalJiraIssues, FieldMapping fieldMapping, List<JiraIssue> completedIssueList,
			Map<String, JiraIssue> linkedIssueMap, Set<IssueKpiModalValue> issueData) {

		Map<String, JiraIssue> totalStoriesMap = totalJiraIssues.stream()
				.collect(Collectors.toMap(JiraIssue::getNumber, Function.identity()));

		if (CollectionUtils.isNotEmpty(jiraIssue.getDefectStoryID())) {
			List<JiraIssue> linkedJiraIssueStoryList = new ArrayList<>();
			filtersLinkedStories(issueKpiModalObject, unlinkedDefect, jiraIssue, totalStoriesMap, fieldMapping,
					linkedJiraIssueStoryList, linkedIssueMap, issueData);
			if (CollectionUtils.isNotEmpty(linkedJiraIssueStoryList)) {
				linkedDefect.add(jiraIssue);
				IssueKpiModalValue data = issueKpiModalObject.get(jiraIssue.getNumber());
				setKpiSpecificData(fieldMapping, data, linkedJiraIssueStoryList, true, completedIssueList);
			}

		} else if (!unlinkedDefect.contains(jiraIssue)) {
			unlinkedDefect.add(jiraIssue);
			KPIExcelUtility.populateIssueModal(jiraIssue, fieldMapping, issueKpiModalObject);
			IssueKpiModalValue data = issueKpiModalObject.get(jiraIssue.getNumber());
			setKpiSpecificData(fieldMapping, data, new ArrayList<>(), false, null);
			issueData.add(data);
		}
	}

	/**
	 * if any defects is linked to stories then only consider to linked story and if
	 * any defects is linked to defect then consider unlinked fix for DTS-23222
	 *
	 * @param unlinkedDefect
	 * @param jiraIssue
	 * @param totalStoriesMap
	 * @param fieldMapping
	 * @param linkedJiraIssueStoryList
	 * @param issueData
	 */
	private void filtersLinkedStories(Map<String, IssueKpiModalValue> issueKpiModalObject, // NOSONAR
			List<JiraIssue> unlinkedDefect, JiraIssue jiraIssue, Map<String, JiraIssue> totalStoriesMap,
			FieldMapping fieldMapping, List<JiraIssue> linkedJiraIssueStoryList, Map<String, JiraIssue> linkedIssueMap,
			Set<IssueKpiModalValue> issueData) {
		jiraIssue.getDefectStoryID().forEach(storyNumber -> {
			totalStoriesMap.computeIfPresent(storyNumber, (k, linkedJiraIssueStory) -> {
				if (fieldMapping.getJiradefecttype().contains(linkedJiraIssueStory.getTypeName())) {
					if (!unlinkedDefect.contains(jiraIssue)) {
						unlinkedDefect.add(jiraIssue);
						KPIExcelUtility.populateIssueModal(jiraIssue, fieldMapping, issueKpiModalObject);
						IssueKpiModalValue data = issueKpiModalObject.get(jiraIssue.getNumber());
						setKpiSpecificData(fieldMapping, data, new ArrayList<>(), false, null);
						issueData.add(data);
					}
				} else {
					linkedJiraIssueStoryList.add(linkedJiraIssueStory);
				}
				return linkedJiraIssueStory;
			});
			// fix for DTS-24813
			totalStoriesMap.computeIfAbsent(storyNumber, k -> {
				JiraIssue linkedIssue = linkedIssueMap.get(storyNumber);
				if (linkedIssue != null && fieldMapping.getJiradefecttype().contains(linkedIssue.getTypeName()) &&
						(!unlinkedDefect.contains(jiraIssue))) {
					unlinkedDefect.add(jiraIssue);
					KPIExcelUtility.populateIssueModal(jiraIssue, fieldMapping, issueKpiModalObject);
					IssueKpiModalValue data = issueKpiModalObject.get(jiraIssue.getNumber());
					setKpiSpecificData(fieldMapping, data, new ArrayList<>(), false, null);
					issueData.add(data);
				}
				return null;
			});
		});
	}

	private void setKpiSpecificData(FieldMapping fieldMapping, // NOSONAR
			IssueKpiModalValue jiraIssueModalObject, List<JiraIssue> linkedJiraIssueStoryList, boolean estimationFlag,
			List<JiraIssue> completedIssueList) {
		if (CollectionUtils.isNotEmpty(linkedJiraIssueStoryList)) {
			AtomicReference<Double> storyPoint = new AtomicReference<>(0.0d);
			Map<String, String> linkedStoriesMap = new HashMap<>();
			linkedJiraIssueStoryList.forEach(linkedStory -> {
				linkedStoriesMap.put(linkedStory.getNumber(), linkedStory.getUrl());
				if (estimationFlag) {
					if (null != linkedStory.getStoryPoints() && StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria()) &&
							fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
						storyPoint.updateAndGet(v -> v + linkedStory.getStoryPoints());
					}
					if (null != linkedStory.getOriginalEstimateMinutes() &&
							StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria()) &&
							fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.ACTUAL_ESTIMATION)) {
						storyPoint.updateAndGet(v -> v + (double) linkedStory.getOriginalEstimateMinutes() / 480);
					}
					if (!completedIssueList.contains(linkedStory)) {
						jiraIssueModalObject.setMarker(Constant.GREEN);
					}
				}
			});
			jiraIssueModalObject.setLinkedStories(linkedStoriesMap);
			jiraIssueModalObject.setLinkedStoriesSize(storyPoint.get().toString());
		} else {
			Map<String, String> dummyMap = new HashMap<>();
			dummyMap.put(NOT_APPLICABLE, NOT_APPLICABLE);
			jiraIssueModalObject.setLinkedDefefect(dummyMap);
			jiraIssueModalObject.setDefectInjectRate(NOT_APPLICABLE);
			jiraIssueModalObject.setDefectDensity(NOT_APPLICABLE);
		}
	}
}
