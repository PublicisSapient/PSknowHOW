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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPIExcelColumn;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.IterationKpiData;
import com.publicissapient.kpidashboard.apis.model.IterationKpiModalValue;
import com.publicissapient.kpidashboard.apis.model.IterationKpiValue;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class QualityStatusServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {
	private static final String OVERALL = "Overall";
	public static final String LINKED_DEFECTS = "Story Linked defects";
	public static final String UNLINKED_DEFECTS = "Unlinked defects";
	public static final String DIR = "DIR";
	public static final String DEFECT_DENSITY = "Defect density";
	private static final String STORY_LIST = "Storylist";

	final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS");

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private SprintRepository sprintRepository;

	@Autowired
	private ConfigHelperService configHelperService;

	@Autowired
	private KpiHelperService kpiHelperService;

	@Autowired
	private CustomApiConfig customApiConfig;

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		DataCount trendValue = new DataCount();

		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {

			Filters filters = Filters.getFilter(k);
			if (Filters.SPRINT == filters) {
				projectWiseLeafNodeValue(v, trendValue, kpiElement, kpiRequest);
			}
		});
		return kpiElement;
	}

	@Override
	public String getQualifierType() {
		return KPICode.QUALITY_STATUS.name();
	}

	@Override
	public Double calculateKPIMetrics(Map<String, Object> subCategoryMap) {
		return null;
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();

		List<String> sprintList = new ArrayList<>();
		Node leafNode = leafNodeList.stream().findFirst().orElse(null);
		if (null != leafNode) {

			log.info("Defect raised -> Requested sprint : {}", leafNode.getName());
			String basicProjectConfigId = leafNode.getProjectFilter().getBasicProjectConfigId().toString();
			String sprintId = leafNode.getSprintFilter().getId();
			SprintDetails sprintDetails = sprintRepository.findBySprintID(sprintId);
			sprintList.add(sprintId);

			if (null != sprintDetails) {
				List<String> totalIssue = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.TOTAL_ISSUES);
				if (CollectionUtils.isNotEmpty(totalIssue)) {
					List<JiraIssue> issueList = jiraIssueRepository.findByNumberInAndBasicProjectConfigId(totalIssue,
							basicProjectConfigId);
					Set<JiraIssue> filtersIssuesList = KpiDataHelper
							.getFilteredJiraIssuesListBasedOnTypeFromSprintDetails(sprintDetails,
									sprintDetails.getTotalIssues(), issueList);

					resultListMap.put(STORY_LIST, new ArrayList<>(filtersIssuesList));
				}
			}

		}
		return resultListMap;

	}

	/**
	 * Populates KPI value to sprint leaf nodes and gives the trend analysis at
	 * sprint level.
	 *
	 * @param trendValue
	 * @param sprintLeafNodeList
	 * @param kpiElement
	 * @param kpiRequest
	 */
	@SuppressWarnings("unchecked")
	private void projectWiseLeafNodeValue(List<Node> sprintLeafNodeList, DataCount trendValue, KpiElement kpiElement,
			KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();

		sprintLeafNodeList.sort(Comparator.comparing(node -> node.getSprintFilter().getStartDate()));
		String startDate = sprintLeafNodeList.get(0).getSprintFilter().getStartDate();

		String endDate = sprintLeafNodeList.get(sprintLeafNodeList.size() - 1).getSprintFilter().getEndDate();

		List<Node> latestSprintNode = new ArrayList<>();
		Node latestSprint = sprintLeafNodeList.get(0);

		Optional.ofNullable(latestSprint).ifPresent(latestSprintNode::add);

		Map<String, Object> resultMap = fetchKPIDataFromDb(latestSprintNode, startDate, endDate, kpiRequest);

		assert latestSprint != null;
		FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
				.get(latestSprint.getProjectFilter().getBasicProjectConfigId());

		List<String> defectTypes = Optional.ofNullable(fieldMapping).map(FieldMapping::getJiradefecttype)
				.orElse(Collections.emptyList());

		if (CollectionUtils.isNotEmpty((List<JiraIssue>) resultMap.get(STORY_LIST))) {
			List<JiraIssue> alldefects = ((List<JiraIssue>) resultMap.get(STORY_LIST)).stream()
					.filter(issue -> defectTypes.contains(issue.getTypeName())).collect(Collectors.toList());
			List<JiraIssue> allStory = ((List<JiraIssue>) resultMap.get(STORY_LIST)).stream()
					.filter(issue -> !defectTypes.contains(issue.getTypeName())).collect(Collectors.toList());

			if (CollectionUtils.isNotEmpty(alldefects) && CollectionUtils.isNotEmpty(allStory)) {

				List<JiraIssue> allLinkedDefects = alldefects.stream()
						.filter(jiraIssue -> CollectionUtils.isNotEmpty(jiraIssue.getDefectStoryID()))
						.collect(Collectors.toList());

				log.info("Defect raised -> request id : {} total linked defects : {}", requestTrackerId,
						allLinkedDefects.size());

				Map<String, Map<String, List<JiraIssue>>> priorityAndStatusWiseIssues = alldefects.stream().collect(
						Collectors.groupingBy(JiraIssue::getPriority, Collectors.groupingBy(JiraIssue::getStatus)));

				Set<String> priorities = new HashSet<>();
				Set<String> statuses = new HashSet<>();

				Map<String, JiraIssue> allStoryMap = new HashMap<>();
				allStory.stream().forEach(story -> allStoryMap.putIfAbsent(story.getNumber(), story));

				List<JiraIssue> linkedDefectsCreatedInSprint = allLinkedDefects.stream().filter(jiraIssue -> {
					try {
						return (checkIssueCreatedInSprintDuration(jiraIssue, startDate, endDate));
					} catch (ParseException e) {
						log.error("There is some error occured in parsing  ", e);
					}
					return false;
				}).collect(Collectors.toList());

				double overAllDefectDensity = calculateDefectDensity(allStory, linkedDefectsCreatedInSprint,
						fieldMapping);

				double overAllDir = calculateDIR(allStory, linkedDefectsCreatedInSprint);

				List<Double> overAllLinkedDefects = Arrays.asList(0.0);
				List<Double> overAllUnlinkedDefects = Arrays.asList(0.0);
				List<IterationKpiValue> iterationKpiValues = new ArrayList<>();
				List<IterationKpiModalValue> overAllUnlinkedmodalValues = new ArrayList<>();
				List<IterationKpiModalValue> overAlllinkedmodalValues = new ArrayList<>();

				priorityAndStatusWiseIssues
						.forEach((priority, statusWiseIssue) -> statusWiseIssue.forEach((status, issues) -> {
							priorities.add(priority);
							statuses.add(status);

							List<IterationKpiModalValue> linkedModalValues = new ArrayList<>();
							List<IterationKpiModalValue> unLinkedModalValues = new ArrayList<>();
							List<IterationKpiModalValue> modalValues = new ArrayList<>();

							List<JiraIssue> linkedDefect = new ArrayList<>();

							List<JiraIssue> unlinkedDefect = new ArrayList<>();

							for (JiraIssue jiraIssue : issues) {

								createLinkAndUnlinkDefectList(overAllUnlinkedmodalValues, overAlllinkedmodalValues,
										linkedModalValues, unLinkedModalValues, linkedDefect, unlinkedDefect, jiraIssue,
										endDate, startDate, allStoryMap, fieldMapping);
							}
							double defectDensity = calculateDefectDensity(allStory, linkedDefect, fieldMapping);

							double dir = calculateDIR(allStory, linkedDefectsCreatedInSprint);

							overAllLinkedDefects.set(0, overAllLinkedDefects.get(0) + linkedDefect.size());
							overAllUnlinkedDefects.set(0, overAllUnlinkedDefects.get(0) + unlinkedDefect.size());
							List<IterationKpiData> data = new ArrayList<>();
							IterationKpiData ld = new IterationKpiData(LINKED_DEFECTS, (double) linkedDefect.size(),
									null, null, null, linkedModalValues);

							IterationKpiData dd = new IterationKpiData(DIR + "/" + DEFECT_DENSITY, dir, defectDensity,
									null, "%" , "", modalValues);
							IterationKpiData ud = new IterationKpiData(UNLINKED_DEFECTS, (double) unlinkedDefect.size(),
									null, null, null, unLinkedModalValues);

							data.add(ld);
							data.add(dd);
							data.add(ud);

							IterationKpiValue iterationKpiValue = new IterationKpiValue(priority, status, data);
							iterationKpiValues.add(iterationKpiValue);
						}));

				List<IterationKpiData> data = new ArrayList<>();
				IterationKpiData overAllLD = new IterationKpiData(LINKED_DEFECTS, overAllLinkedDefects.get(0), null,
						null, null, overAlllinkedmodalValues);
				IterationKpiData overAllDD = new IterationKpiData(DIR + "/" + DEFECT_DENSITY, overAllDir,
						overAllDefectDensity, null, Constant.PERCENTAGE, "", null);
				IterationKpiData overAllUD = new IterationKpiData(UNLINKED_DEFECTS, overAllUnlinkedDefects.get(0), null,
						null, null, overAllUnlinkedmodalValues);
				data.add(overAllLD);
				data.add(overAllDD);
				data.add(overAllUD);
				IterationKpiValue overAllIterationKpiValue = new IterationKpiValue(OVERALL, OVERALL, data);
				iterationKpiValues.add(overAllIterationKpiValue);

				trendValue.setValue(iterationKpiValues);
				kpiElement.setSprint(latestSprint.getName());
				kpiElement.setModalHeads(KPIExcelColumn.QUALITY_STATUS.getColumns());
				kpiElement.setTrendValueList(trendValue);
			}
		}
	}

	/**
	 * This is a Java method that calculates the defect density of a set of linked
	 * defects relative to a set of all user stories.
	 */

	private double calculateDefectDensity(List<JiraIssue> allStory, List<JiraIssue> linkedDefect,
			FieldMapping fieldMapping) {

		if (CollectionUtils.isEmpty(linkedDefect)) {
			return 0;
		}
		Set<String> linkedStoriesSet = linkedDefect.stream().map(JiraIssue::getDefectStoryID).flatMap(Set::stream)
				.collect(Collectors.toSet());

		List<JiraIssue> linkedStoriesJiraIssueList = allStory.stream()
				.filter(jiraIssue -> linkedStoriesSet.contains(jiraIssue.getNumber())).collect(Collectors.toList());

		if (CollectionUtils.isEmpty(linkedStoriesJiraIssueList)) {
			return 0;
		}
		if (StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
				&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
			double storyPointSum = linkedStoriesJiraIssueList.stream()
					.filter(jiraIssue -> Objects.nonNull(jiraIssue.getStoryPoints()))
					.mapToDouble(JiraIssue::getStoryPoints).sum();
			return (Math.round(100.0 * (storyPointSum == 0 ? 0 : linkedDefect.size() / storyPointSum)) / 100.0);
		} else {
			int originalEstimateMinutesSum = linkedStoriesJiraIssueList.stream()
					.filter(jiraIssue -> Objects.nonNull(jiraIssue.getOriginalEstimateMinutes()))
					.mapToInt(JiraIssue::getOriginalEstimateMinutes).sum();
			double originalEstimateInDays = (double) originalEstimateMinutesSum / 480;
			return Math.round(originalEstimateInDays == 0 ? 0 : linkedDefect.size() / originalEstimateInDays);
		}
	}

	private double calculateDIR(List<JiraIssue> allStory, List<JiraIssue> linkedDefect) {

		if (CollectionUtils.isEmpty(allStory) || CollectionUtils.isEmpty(linkedDefect)) {
			return 0;
		}
		Set<String> listOfStory = linkedDefect.stream().map(JiraIssue::getDefectStoryID).flatMap(Set::stream)
				.collect(Collectors.toSet());

		List<JiraIssue> allLinkedStories = allStory.stream()
				.filter(jiraIssue -> listOfStory.contains(jiraIssue.getNumber())).collect(Collectors.toList());

		if (CollectionUtils.isEmpty(allLinkedStories)) {
			return 0;
		}
		return Math.round(((double) linkedDefect.size() / allLinkedStories.size()) * 100);
	}

	/**
	 * This is a Java method that takes in a set of parameters and checks Jira issue
	 * based on its creation date, and adds it to either a list of linked or
	 * unlinked defects accordingly.
	 */

	private void createLinkAndUnlinkDefectList(List<IterationKpiModalValue> overAllUnlinkedmodalValues, // NOSONAR
			List<IterationKpiModalValue> overAlllinkedmodalValues, List<IterationKpiModalValue> linkedModalValues,
			List<IterationKpiModalValue> unLinkedModalValues, List<JiraIssue> linkedDefect,
			List<JiraIssue> unlinkedDefect, JiraIssue jiraIssue, String endDate, String startDate,
			Map<String, JiraIssue> allStoriesMap, FieldMapping fieldMapping) {

		try {
			if (checkIssueCreatedInSprintDuration(jiraIssue, startDate, endDate)) {
				if (CollectionUtils.isNotEmpty(jiraIssue.getDefectStoryID())) {
					List<JiraIssue> linkedJiraIssueStoryList = new ArrayList<>();
					jiraIssue.getDefectStoryID().forEach(
							storyNumber -> allStoriesMap.computeIfPresent(storyNumber, (k, linkedJiraIssueStory) -> {
								linkedJiraIssueStoryList.add(linkedJiraIssueStory);
								return linkedJiraIssueStory;
							}));
					if (CollectionUtils.isNotEmpty(linkedJiraIssueStoryList)) {
						linkedDefect.add(jiraIssue);
						populateIterationDataForDefectsLinkKPI(overAlllinkedmodalValues, linkedModalValues, jiraIssue,
								true, fieldMapping, linkedJiraIssueStoryList);
					}

				} else {
					unlinkedDefect.add(jiraIssue);
					populateIterationDataForDefectsLinkKPI(overAllUnlinkedmodalValues, unLinkedModalValues, jiraIssue,
							false, null, new ArrayList<>());
				}
			}
		} catch (ParseException e) {
			log.error("There is some error occured in parsing  ", e);
		}
	}

	private boolean checkIssueCreatedInSprintDuration(JiraIssue jiraIssue, String startDate, String endDate)
			throws ParseException {
		return (dateFormat.parse(jiraIssue.getCreatedDate()).equals(dateFormat.parse(endDate))
				|| dateFormat.parse(jiraIssue.getCreatedDate()).equals(dateFormat.parse(startDate)))
				|| (dateFormat.parse(jiraIssue.getCreatedDate()).before(dateFormat.parse(endDate))
						&& dateFormat.parse(jiraIssue.getCreatedDate()).after(dateFormat.parse(startDate)));
	}

	public void populateIterationDataForDefectsLinkKPI(List<IterationKpiModalValue> overAllmodalValues,
			List<IterationKpiModalValue> modalValues, JiraIssue jiraIssue, boolean estimationFlag,
			FieldMapping fieldMapping, List<JiraIssue> linkedJiraIssueStoryList) {

		IterationKpiModalValue iterationKpiModalValue = new IterationKpiModalValue();
		iterationKpiModalValue.setIssueId(jiraIssue.getNumber());
		iterationKpiModalValue.setIssueURL(jiraIssue.getUrl());
		iterationKpiModalValue.setDescription(jiraIssue.getName());
		iterationKpiModalValue.setIssueStatus(jiraIssue.getStatus());
		iterationKpiModalValue.setIssueType(jiraIssue.getTypeName());
		iterationKpiModalValue.setPriority(jiraIssue.getPriority());
		AtomicReference<Double> storyPoint = new AtomicReference<>(0.0d);
		Map<String, String> linkedStoriesMap = new HashMap<>();
		linkedJiraIssueStoryList.forEach(linkedStory -> {
			linkedStoriesMap.put(linkedStory.getNumber(), linkedStory.getUrl());
			iterationKpiModalValue.setLinkedStoriesTest(linkedStory.getNumber());
			if (estimationFlag) {
				if (null != linkedStory.getStoryPoints() && StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
						&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.STORY_POINT)) {
					storyPoint.updateAndGet(v -> v + linkedStory.getStoryPoints());
				}
				if (null != linkedStory.getOriginalEstimateMinutes()
						&& StringUtils.isNotEmpty(fieldMapping.getEstimationCriteria())
						&& fieldMapping.getEstimationCriteria().equalsIgnoreCase(CommonConstant.ACTUAL_ESTIMATION)) {
					storyPoint.updateAndGet(v -> v + (double) linkedStory.getOriginalEstimateMinutes() / 480);
				}
			}
			iterationKpiModalValue.setLinkedStoriesTest(linkedStory.getNumber());
		});
		iterationKpiModalValue.setLinkedStories(linkedStoriesMap);
		iterationKpiModalValue.setLinkedStoriesSize(storyPoint.get().toString());
		modalValues.add(iterationKpiModalValue);
		overAllmodalValues.add(iterationKpiModalValue);
	}
}
