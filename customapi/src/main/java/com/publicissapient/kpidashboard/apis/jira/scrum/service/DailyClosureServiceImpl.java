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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KpiDataHelper;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.ValidationData;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * This class fetches the daily closure on Iteration dashboard. Trend analysis
 * for Daily Closure KPI has total closed defect count at y-axis and day at
 * x-axis. {@link JiraKPIService}
 * 
 * @author tauakram
 *
 */
@Component
@Slf4j
public class DailyClosureServiceImpl extends JiraKPIService<Map<String, Long>, List<Object>, Map<String, Object>> {
	private static final String ISSUES = "issues";
	private static final String SPRINT = "sprint";
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
	private static final DateTimeFormatter YYYY_MM_DD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	public static final String UNCHECKED = "unchecked";

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private SprintRepository sprintRepository;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getQualifierType() {
		return KPICode.DAILY_CLOSURES.name();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {

		List<DataCount> trendValueList = new ArrayList<>();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();

		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {

			if (Filters.getFilter(k) == Filters.SPRINT) {
				sprintWiseLeafNodeValue(mapTmp, v, trendValueList, kpiElement, kpiRequest);
			}

		});

		kpiElement.setTrendValueList(trendValueList);

		return kpiElement;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Long> calculateKPIMetrics(Map<String, Object> objectMap) {
		return new HashMap<>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> fetchKPIDataFromDb(final List<Node> leafNodeList, final String startDate,
			final String endDate, final KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		Node leafNode = leafNodeList.stream().findFirst().orElse(null);
		if (null != leafNode) {
			log.info("Daily Closure -> Requested sprint : {}", leafNode.getName());
			String basicProjectConfigId = leafNode.getProjectFilter()
					.getBasicProjectConfigId().toString();
			String sprintId = leafNode.getSprintFilter().getId();
			SprintDetails sprintDetails = sprintRepository.findBySprintID(sprintId);
			if (null != sprintDetails) {
				List<String> totalIssues = KpiDataHelper.getIssuesIdListBasedOnTypeFromSprintDetails(sprintDetails,
						CommonConstant.COMPLETED_ISSUES);
				if (CollectionUtils.isNotEmpty(totalIssues)) {
					List<JiraIssue> issueList = jiraIssueRepository.findByNumberInAndBasicProjectConfigId(totalIssues,
							basicProjectConfigId);
					resultListMap.put(ISSUES, issueList);
					resultListMap.put(SPRINT, sprintDetails);
				}
			}
		}
		return resultListMap;
	}

	/**
	 * This method populates KPI value to sprint leaf nodes. It also gives the
	 * trend analysis at sprint wise.
	 * 
	 * @param mapTmp
	 *            node id map
	 * @param sprintLeafNodeList
	 *            sprint nodes list
	 * @param trendValueList
	 *            list to hold trend nodes data
	 * @param kpiElement
	 *            the KpiElement
	 * @param kpiRequest
	 *            the KpiRequest
	 */
	@SuppressWarnings(UNCHECKED)
	private void sprintWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> sprintLeafNodeList,
			List<DataCount> trendValueList, KpiElement kpiElement, KpiRequest kpiRequest) {

		String requestTrackerId = getRequestTrackerId();

		sprintLeafNodeList.sort((node1, node2) -> node1.getSprintFilter().getStartDate()
				.compareTo(node2.getSprintFilter().getStartDate()));
		List<Node> latestSprintNode = new ArrayList<>();
		Node latestSprint = sprintLeafNodeList.get(0);
		Optional.ofNullable(latestSprint).ifPresent(latestSprintNode::add);

		Map<String, Object> resultMap = fetchKPIDataFromDb(latestSprintNode, null, null, kpiRequest);
		List<JiraIssue> allIssues = (List<JiraIssue>) resultMap.get(ISSUES);
		SprintDetails sprintDetails=(SprintDetails) resultMap.get(SPRINT);
		if (CollectionUtils.isNotEmpty(allIssues)) {
			log.info("Daily Closures -> request id : {} total jira Issues : {}", requestTrackerId, allIssues.size());

			Map<String, Map<String, List<JiraIssue>>> dateAndtypeWiseIssues = allIssues.stream()
					.filter(f-> StringUtils.isNotBlank( f.getUpdateDate()))
					.collect(Collectors.groupingBy(
							f -> LocalDate.parse(f.getUpdateDate().split("\\.")[0], DATE_TIME_FORMATTER).toString(),
							Collectors.groupingBy(JiraIssue::getTypeName)));

			LocalDate end = LocalDate.parse(sprintDetails.getEndDate().split("\\.")[0], DATE_TIME_FORMATTER);
			LocalDate start=LocalDate.parse(sprintDetails.getStartDate().split("\\.")[0], DATE_TIME_FORMATTER).minusDays(1);
			Map<String, DataCount> dateWiseDataCount = new LinkedHashMap<>();
			for (LocalDate date = end; date.isAfter(start); date = date.minusDays(1)) {
				dateWiseDataCount.put(date.format(YYYY_MM_DD_FORMATTER), new DataCount());
			}
			List<JiraIssue> issuesExcel=new ArrayList<>();
			List<DataCount> data = new ArrayList<>();
			dateWiseDataCount.forEach((date, dataCount) -> {
				Map<String, Integer> value = new HashMap<>();
				if (null != dateAndtypeWiseIssues.get(date)) {
					Map<String, List<JiraIssue>> typeWiseMap = dateAndtypeWiseIssues.get(date);
					typeWiseMap.forEach((type, issues) -> {
						value.put(type, issues.size());
						issuesExcel.addAll(issues);

					});	
				}
				dataCount.setValue(value);
				dataCount.setSProjectName(latestSprint.getProjectFilter().getName());
				dataCount.setSSprintID(latestSprint.getSprintFilter().getId());
				dataCount.setSSprintName(date);
				dataCount.setHoverValue(new HashMap<>());
				data.add(dataCount);
			});
			trendValueList.add(new DataCount(latestSprint.getProjectFilter().getName(), Lists.reverse(data)));
			populateValidationDataObject(kpiElement,requestTrackerId,issuesExcel,latestSprint );
		}
	}

	/**
	 * This method check for API request source. If it is Excel it populates the
	 * validation data node of the KPI element.
	 * 
	 * @param kpiElement
	 *            KpiElement
	 * @param requestTrackerId
	 *            request id
	 * @param issuesExcel
	 *            list of jiraIssues
	
	 * @param sprint
	 *            unique key
	 */
	private void populateValidationDataObject(KpiElement kpiElement, String requestTrackerId,
			List<JiraIssue> issuesExcel,Node sprint) {

		if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
			Map<String, ValidationData> validationDataMap = new HashMap<>();
			List<String> types = new ArrayList<>();
			List<String> jiraIssues = new ArrayList<>();

			
			for (JiraIssue jiraIssue : issuesExcel) {

				jiraIssues.add(jiraIssue.getNumber());
				types.add(jiraIssue.getTypeName());
			}
			
			ValidationData validationData = new ValidationData();
			validationData.setIssues(jiraIssues);
			validationData.setIssueTypeList(types);
			if (requestTrackerId.toLowerCase().contains(KPISource.EXCEL.name().toLowerCase())) {
				String key =sprint.getSprintFilter().getId();
				validationDataMap.put(key, validationData);
				kpiElement.setMapOfSprintAndData(validationDataMap);
			}
		
		}
	}

	@Override
	public Map<String, Long> calculateKpiValue(List<Map<String, Long>> valueList, String kpiName) {
		return calculateKpiValueForMap(valueList, kpiName);
	}
}
