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

package com.publicissapient.kpidashboard.zephyr.processor.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.NormalizedJira;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.application.KanbanAccountHierarchy;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.model.zephyr.TestCaseDetails;
import com.publicissapient.kpidashboard.common.model.zephyr.ZephyrTestCaseDTO;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.KanbanAccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.zephyr.TestCaseDetailsRepository;
import com.publicissapient.kpidashboard.zephyr.processor.service.ZephyrDBService;
import com.publicissapient.kpidashboard.zephyr.repository.ZephyrProcessorRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ZephyrDBServiceImpl implements ZephyrDBService {

	private static final String TEST_TYPE = "Test";
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS");
	private final DateTimeFormatter parserForServer = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	private final DateTimeFormatter parserForCloud = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");

	@Autowired
	private ZephyrProcessorRepository zephyrProcessorRepository;

	@Autowired
	private KanbanAccountHierarchyRepository kanbanAccountHierarchyRepo;

	@Autowired
	private AccountHierarchyRepository accountHierarchyRepository;

	@Autowired
	private TestCaseDetailsRepository testCaseDetailsRepository;

	/**
	 * Persist Zephyr test case data into test_case_details collections
	 *
	 * @param testCases
	 * @param processorToolConnection
	 * @param isZephyrCloud
	 */
	@Override
	public void processTestCaseInfoToDB(final List<ZephyrTestCaseDTO> testCases,
			ProcessorToolConnection processorToolConnection, boolean isKanban, boolean isZephyrCloud) {
		if (CollectionUtils.isNotEmpty(testCases)) {
			ObjectId zephyrProcessorId = zephyrProcessorRepository.findByProcessorName(ProcessorConstants.ZEPHYR)
					.getId();
			if (null != zephyrProcessorId) {
				List<TestCaseDetails> testCaseDetailsList = new ArrayList<>();
				Map<String, AccountHierarchy> hierarchyDataMapForScrum = new HashMap<>();
				Map<String, KanbanAccountHierarchy> hierarchyDataMapForKanban = new HashMap<>();

				if (isKanban) {
					prepareAccountInfoForKanban(processorToolConnection, hierarchyDataMapForKanban);
				} else {
					prepareAccountInfoForScrum(processorToolConnection, hierarchyDataMapForScrum);
				}

				testCases.forEach(testCase -> {
					log.debug("Adding test cases in test case details Collections: {} ", testCase.getKey());
					String basicProjectId = processorToolConnection.getBasicProjectConfigId().toString();
					TestCaseDetails testCaseDetails = getTestCaseDetail(testCase.getKey(), basicProjectId);
					testCaseDetails.setBasicProjectConfigId(basicProjectId);
					testCaseDetails.setProcessorId(zephyrProcessorId);
					if (isKanban) {
						setAccountInfoForKanban(hierarchyDataMapForKanban, testCaseDetails);
					} else {
						setAccountInfoForScrum(hierarchyDataMapForScrum, testCaseDetails);
					}
					setTestCaseDetails(processorToolConnection, testCase, testCaseDetails, isZephyrCloud);
					testCaseDetailsList.add(testCaseDetails);
				});

				if (CollectionUtils.isNotEmpty(testCaseDetailsList)) {
					testCaseDetailsRepository.saveAll(testCaseDetailsList);
				}
			}
		}
	}

	/**
	 * prepare account related data for scrum
	 *
	 * @param processorToolConnection
	 * @param hierarchyDataMap
	 */
	private void prepareAccountInfoForScrum(ProcessorToolConnection processorToolConnection,
			Map<String, AccountHierarchy> hierarchyDataMap) {
		AccountHierarchy projectInfo = accountHierarchyRepository.findByLabelNameAndBasicProjectConfigId(
				CommonConstant.HIERARCHY_LEVEL_ID_PROJECT, processorToolConnection.getBasicProjectConfigId()).get(0);
		hierarchyDataMap.put(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT, projectInfo);
	}

	/**
	 * prepare account related data for kanban
	 *
	 * @param processorToolConnection
	 * @param hierarchyDataMap
	 */
	private void prepareAccountInfoForKanban(ProcessorToolConnection processorToolConnection,
			Map<String, KanbanAccountHierarchy> hierarchyDataMap) {
		KanbanAccountHierarchy projectInfo = kanbanAccountHierarchyRepo.findByLabelNameAndBasicProjectConfigId(
				CommonConstant.HIERARCHY_LEVEL_ID_PROJECT, processorToolConnection.getBasicProjectConfigId()).get(0);
		hierarchyDataMap.put(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT, projectInfo);
	}

	/**
	 * Setting scrum/kanban test case data save in test_case_details
	 *
	 * @param processorToolConnection
	 * @param testCase
	 * @param testCaseDetails
	 * @param isZephyrCloud
	 */
	private void setTestCaseDetails(ProcessorToolConnection processorToolConnection, ZephyrTestCaseDTO testCase,
			TestCaseDetails testCaseDetails, boolean isZephyrCloud) {
		if (CollectionUtils.isNotEmpty(testCase.getLabels())) {
			testCaseDetails.setLabels(testCase.getLabels());
		}
		setCustomFieldValues(testCaseDetails, testCase.getCustomFields(), testCase.getUpdatedOn(),
				processorToolConnection, isZephyrCloud);
		if (testCase.getFolder() != null) {
			testCaseDetails.setTestCaseFolderName(testCase.getFolder());
		}
		if (testCase.getIssueLinks() != null) {
			testCaseDetails.setDefectStoryID(testCase.getIssueLinks());
		}
		testCaseDetails.setTypeName(TEST_TYPE);
		testCaseDetails.setNumber(testCase.getKey());
		if (StringUtils.isNotBlank(testCase.getCreatedOn())) {
			testCaseDetails.setCreatedDate(getDateFormatter(testCase.getCreatedOn(), isZephyrCloud));
		}
		testCaseDetails.setDefectRaisedBy(testCase.getOwner());
		testCaseDetails.setName(testCase.getName());
	}

	/**
	 * set account info into test case details for scrum
	 *
	 * @param hierarchyDataMapForScrum
	 * @param testCaseDetails
	 *
	 */

	private void setAccountInfoForScrum(Map<String, AccountHierarchy> hierarchyDataMapForScrum,
			TestCaseDetails testCaseDetails) {
		testCaseDetails
				.setProjectID(hierarchyDataMapForScrum.get(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT).getNodeId());
		testCaseDetails
				.setProjectName(hierarchyDataMapForScrum.get(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT).getNodeName());
	}

	/**
	 * set account info into test case details for scrum
	 *
	 * @param hierarchyDataMapForKanban
	 * @param testCaseDetails
	 *
	 */

	private void setAccountInfoForKanban(Map<String, KanbanAccountHierarchy> hierarchyDataMapForKanban,
			TestCaseDetails testCaseDetails) {
		testCaseDetails
				.setProjectID(hierarchyDataMapForKanban.get(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT).getNodeId());
		testCaseDetails
				.setProjectName(hierarchyDataMapForKanban.get(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT).getNodeName());
	}

	/**
	 * mapping of custom fields values to testing fields
	 *
	 * @param testCaseDetails
	 * @param customFieldMap
	 * @param updatedOnDate
	 * @param processorToolConnection
	 * @param isZephyrCloud
	 *
	 */
	private void setCustomFieldValues(TestCaseDetails testCaseDetails, Map<String, String> customFieldMap,
			String updatedOnDate, ProcessorToolConnection processorToolConnection, boolean isZephyrCloud) {
		if (null != customFieldMap) {
			String testAutomatedCustomFieldValue = getTestAutomatedCustomFieldValue(
					processorToolConnection.getTestAutomated(), customFieldMap);
			String testCanBeAutomated = getTestCanBeAutomated(processorToolConnection.getCanNotAutomatedTestValue(),
					testAutomatedCustomFieldValue);
			String isTestAutomated = getIsTestAutomated(processorToolConnection.getAutomatedTestValue(),
					StringUtils.isNotBlank(processorToolConnection.getTestAutomationStatusLabel())
							? customFieldMap.get(processorToolConnection.getTestAutomationStatusLabel())
							: StringUtils.EMPTY);

			testCaseDetails.setIsTestAutomated(isTestAutomated);
			testCaseDetails.setTestAutomated(testAutomatedCustomFieldValue);
			testCaseDetails.setIsTestCanBeAutomated(testCanBeAutomated);
			if (isTestAutomated.equals(NormalizedJira.YES_VALUE.getValue())
					&& StringUtils.isBlank(testCaseDetails.getTestAutomatedDate())) {
				testCaseDetails.setTestAutomatedDate(getDateFormatter(updatedOnDate, isZephyrCloud));
			}

			setRegressionLabel(processorToolConnection, customFieldMap, testCaseDetails);
		}
	}

	/**
	 * @param testAutomated
	 * @param customFieldMap
	 * @return None in case isTestAutomated param is null otherwise isTestAutomated
	 */
	private String getTestAutomatedCustomFieldValue(String testAutomated, Map<String, String> customFieldMap) {
		if (null == testAutomated || customFieldMap.get(testAutomated) == null) {
			return "None";
		} else {
			return customFieldMap.get(testAutomated);
		}
	}

	/**
	 * Converts the dateFromServer date to a format yyyy-MM-dd'T'HH:mm:ss.SSSSSSS
	 * 
	 * @param dateFromServer
	 * @param isZephyrCloud
	 * @return formatted Date
	 */
	private String getDateFormatter(String dateFromServer, boolean isZephyrCloud) {
		String formattedDate = StringUtils.EMPTY;
		if (StringUtils.isNotBlank(dateFromServer)) {
			try {
				if (!isZephyrCloud) {
					formattedDate = DATE_FORMATTER.print(parserForServer.parseDateTime(dateFromServer));
				} else {
					formattedDate = DATE_FORMATTER.print(parserForCloud.parseDateTime(dateFromServer));
				}
			} catch (UnsupportedOperationException | IllegalArgumentException e) {
				log.warn("Could not set UpdatedOn date : {} with trace: {}", dateFromServer, e);
			}
		}
		return formattedDate;
	}

	/**
	 * Sets the regression labels..
	 * 
	 * @param processorToolConnection
	 *            processorToolConnection
	 * @param customFieldMap
	 *            map of custom fields
	 * @param testCaseDetails
	 *            scrum test case
	 */
	private void setRegressionLabel(ProcessorToolConnection processorToolConnection, Map<String, String> customFieldMap,
			TestCaseDetails testCaseDetails) {
		if (CollectionUtils.isNotEmpty(processorToolConnection.getTestRegressionValue())
				&& (customFieldMap.get(processorToolConnection.getTestRegressionLabel()) != null)) {
			Set<String> regressionCustomValueList = new HashSet<>(
					Arrays.asList(customFieldMap.get(processorToolConnection.getTestRegressionLabel()).split(", ")));
			if (CollectionUtils.containsAny(processorToolConnection.getTestRegressionValue(),
					regressionCustomValueList)) {
				if (CollectionUtils.isNotEmpty(testCaseDetails.getLabels())) {
					regressionCustomValueList.addAll(testCaseDetails.getLabels());
				}
				testCaseDetails.setLabels(new ArrayList<>(regressionCustomValueList));
			}
		}
	}

	/**
	 * Gets the value for automated test.
	 * 
	 * @param automatedTestValue
	 * @param testAutomationStatusFieldLabel
	 *            label used to check regression automation
	 * @return String
	 */
	private String getIsTestAutomated(List<String> automatedTestValue, String testAutomationStatusFieldLabel) {
		String testAutomated = NormalizedJira.NO_VALUE.getValue();
		if (CollectionUtils.isNotEmpty(automatedTestValue) && StringUtils.isNotBlank(testAutomationStatusFieldLabel)
				&& automatedTestValue.contains(testAutomationStatusFieldLabel)) {
			testAutomated = NormalizedJira.YES_VALUE.getValue();
		}
		return testAutomated;
	}

	/**
	 * @param canNotAutomatedTestValue
	 * @param testAutomatedValue
	 * @return testCanBeAutomated
	 */
	private String getTestCanBeAutomated(List<String> canNotAutomatedTestValue, String testAutomatedValue) {
		String testCanBeAutomated = NormalizedJira.YES_VALUE.getValue();
		if (CollectionUtils.isNotEmpty(canNotAutomatedTestValue)
				&& canNotAutomatedTestValue.contains(testAutomatedValue)) {
			testCanBeAutomated = NormalizedJira.NO_VALUE.getValue();
		}
		return testCanBeAutomated;
	}

	/**
	 * Check if there is any document already persisted in the test_case_details for
	 * the given key, if yes then return that or else create a new Document
	 *
	 * @param number
	 *            number
	 * @param basicProjectId
	 *            basicProjectId
	 * @return {@link TestCaseDetails}
	 */
	private TestCaseDetails getTestCaseDetail(final String number, String basicProjectId) {
		final List<TestCaseDetails> testCaseDetails = testCaseDetailsRepository
				.findByNumberAndBasicProjectConfigId(number, basicProjectId);
		if (testCaseDetails.size() > 1) {
			log.warn("More than 1 Test Case Detail Found for Key: {} ", number);
		}
		if (CollectionUtils.isNotEmpty(testCaseDetails)) {
			return testCaseDetails.get(0);
		}
		return new TestCaseDetails();
	}

}
