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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
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
import com.publicissapient.kpidashboard.apis.util.KPIExcelUtility;
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
	public static final String LINKED_DEFECTS = "Story Linked Defects";
	public static final String UNLINKED_DEFECTS = "Unlinked Defects";
	public static final String DIR = "DIR";
	public static final String DEFECT_DENSITY = "Defect Density";
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

			log.info("Quality Status  -> Requested sprint : {}", leafNode.getName());
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

		sprintLeafNodeList.sort(Comparator.comparing(node -> node.getSprintFilter().getStartDate()));
		String startDate = sprintLeafNodeList.get(0).getSprintFilter().getStartDate();

		String endDate = sprintLeafNodeList.get(sprintLeafNodeList.size() - 1).getSprintFilter().getEndDate();

		List<Node> latestSprintNode = new ArrayList<>();
		Node latestSprint = sprintLeafNodeList.get(0);

		Optional.ofNullable(latestSprint).ifPresent(latestSprintNode::add);

		Map<String, Object> resultMap = fetchKPIDataFromDb(latestSprintNode, startDate, endDate, kpiRequest);

		if (CollectionUtils.isNotEmpty((List<JiraIssue>) resultMap.get(STORY_LIST))) {
			List<JiraIssue> totalJiraIssues = (List<JiraIssue>) resultMap.get(STORY_LIST);
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(latestSprint.getProjectFilter().getBasicProjectConfigId());

			List<String> defectTypes = Optional.ofNullable(fieldMapping).map(FieldMapping::getJiradefecttype)
					.orElse(Collections.emptyList());
			Map<String, JiraIssue> totalStoriesMap =  totalJiraIssues.stream().collect(Collectors.toMap(
					JiraIssue::getNumber, Function.identity()));
			List<JiraIssue> allDefects = totalJiraIssues.stream()
					.filter(issue -> defectTypes.contains(issue.getTypeName())).collect(Collectors.toList());
			List<JiraIssue> allStory = totalJiraIssues.stream()
					.filter(issue -> !defectTypes.contains(issue.getTypeName())).collect(Collectors.toList());

			if (CollectionUtils.isNotEmpty(allDefects) && CollectionUtils.isNotEmpty(allStory)) {

				List<IterationKpiValue> iterationKpiValues = new ArrayList<>();
				List<IterationKpiModalValue> overAllUnlinkedmodalValues = new ArrayList<>();
				List<IterationKpiModalValue> overAlllinkedmodalValues = new ArrayList<>();

				List<JiraIssue> linkedDefectList = new ArrayList<>();

				List<JiraIssue> unlinkedDefectList = new ArrayList<>();

				for (JiraIssue jiraIssue : allDefects) {
					createLinkAndUnlinkDefectList(overAllUnlinkedmodalValues, overAlllinkedmodalValues,
							linkedDefectList, unlinkedDefectList, jiraIssue, endDate, startDate, totalStoriesMap,
							fieldMapping );
				}
				
				double overAllDefectDensity = calculateDefectDensity(allStory, linkedDefectList, fieldMapping);

				double overAllDir = calculateDIR(allStory, linkedDefectList);

				List<IterationKpiData> data = new ArrayList<>();
				IterationKpiData overAllLD = new IterationKpiData(LINKED_DEFECTS,
						Double.valueOf(linkedDefectList.size()), null, null, null, overAlllinkedmodalValues);
				IterationKpiData overAllDD = new IterationKpiData(DIR + "/" + DEFECT_DENSITY, overAllDir,
						overAllDefectDensity, null, Constant.PERCENTAGE, "", null);
				IterationKpiData overAllUD = new IterationKpiData(UNLINKED_DEFECTS,
						Double.valueOf(unlinkedDefectList.size()), null, null, null, overAllUnlinkedmodalValues);
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
	 * calculate based on total linked defects created this sprint divide by
	 * Sum of the story point of stories that have linked defects
	 *
	 * @param allStory
	 * @param linkedDefect
	 * @param fieldMapping
	 * @return
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

	/**
	 * calculate based on total linked defects divide by total linked stories
	 * @param allStory
	 * @param linkedDefect
	 * @return
	 */
	private double calculateDIR(List<JiraIssue> allStory, List<JiraIssue> linkedDefect) {

		if (CollectionUtils.isEmpty(linkedDefect)) {
			return 0;
		}
		Set<String> listOfStory = linkedDefect.stream().map(JiraIssue::getDefectStoryID).flatMap(Set::stream)
				.collect(Collectors.toSet());

		Set<JiraIssue> allLinkedStories = allStory.stream()
				.filter(jiraIssue -> listOfStory.contains(jiraIssue.getNumber())).collect(Collectors.toSet());

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
			List<IterationKpiModalValue> overAlllinkedmodalValues, List<JiraIssue> linkedDefect,
			List<JiraIssue> unlinkedDefect, JiraIssue jiraIssue, String endDate, String startDate,
			Map<String, JiraIssue> totalStoriesMap, FieldMapping fieldMapping ) {
		try {
			if (checkIssueCreatedInSprintDuration(jiraIssue, startDate, endDate)) {
				if (CollectionUtils.isNotEmpty(jiraIssue.getDefectStoryID())) {
					List<JiraIssue> linkedJiraIssueStoryList = new ArrayList<>();
					filtersLinkedStories(overAllUnlinkedmodalValues, unlinkedDefect, jiraIssue, totalStoriesMap, fieldMapping,
							linkedJiraIssueStoryList);
					if (CollectionUtils.isNotEmpty(linkedJiraIssueStoryList)) {
						linkedDefect.add(jiraIssue);
						KPIExcelUtility.populateIterationDataForQualityStatus(overAlllinkedmodalValues, jiraIssue, true, fieldMapping,
								linkedJiraIssueStoryList);
					}

				} else {
					unlinkedDefect.add(jiraIssue);
					KPIExcelUtility.populateIterationDataForQualityStatus(overAllUnlinkedmodalValues, jiraIssue, false, null,
							new ArrayList<>());
				}
			}
		} catch (ParseException e) {
			log.error("There is some error occured in parsing  ", e);
		}
	}

	/**
	 * if any defects is linked to stories then only consider to linked story and
	 * if any defects is linked to defect then consider unlinked
	 * fix for DTS-23222
	 * @param overAllUnlinkedmodalValues
	 * @param unlinkedDefect
	 * @param jiraIssue
	 * @param totalStoriesMap
	 * @param fieldMapping
	 * @param linkedJiraIssueStoryList
	 */
	private void filtersLinkedStories(List<IterationKpiModalValue> overAllUnlinkedmodalValues, List<JiraIssue> unlinkedDefect,
			JiraIssue jiraIssue, Map<String, JiraIssue> totalStoriesMap, FieldMapping fieldMapping,
			List<JiraIssue> linkedJiraIssueStoryList) {
		jiraIssue.getDefectStoryID()
				.forEach(storyNumber -> totalStoriesMap.computeIfPresent(storyNumber, (k, linkedJiraIssueStory) -> {
					if (fieldMapping.getJiradefecttype().contains(linkedJiraIssueStory.getTypeName())) {
						unlinkedDefect.add(jiraIssue);
						KPIExcelUtility.populateIterationDataForQualityStatus(overAllUnlinkedmodalValues, jiraIssue,
								false, null, new ArrayList<>());
					} else {
						linkedJiraIssueStoryList.add(linkedJiraIssueStory);
					}
					return linkedJiraIssueStory;
				}));
	}

	/**
	 *  filters issues on issue created date based on sprint duration
	 * @param jiraIssue
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws ParseException
	 */

	private boolean checkIssueCreatedInSprintDuration(JiraIssue jiraIssue, String startDate, String endDate)
			throws ParseException {
		return (dateFormat.parse(jiraIssue.getCreatedDate()).equals(dateFormat.parse(endDate))
				|| dateFormat.parse(jiraIssue.getCreatedDate()).equals(dateFormat.parse(startDate)))
				|| (dateFormat.parse(jiraIssue.getCreatedDate()).before(dateFormat.parse(endDate))
						&& dateFormat.parse(jiraIssue.getCreatedDate()).after(dateFormat.parse(startDate)));
	}
}
