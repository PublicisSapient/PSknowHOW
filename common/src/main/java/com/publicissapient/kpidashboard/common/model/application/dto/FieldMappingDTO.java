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

package com.publicissapient.kpidashboard.common.model.application.dto;//NOPMD

import java.util.List;

import org.bson.types.ObjectId;

import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterConfig;
import com.publicissapient.kpidashboard.common.model.generic.BasicModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The type Field mapping. Represents Jira field mapping values
 */
@SuppressWarnings("PMD.TooManyFields")
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FieldMappingDTO extends BasicModel {

	private ObjectId projectToolConfigId;
	private ObjectId basicProjectConfigId;
	private String projectId;
	private String atmQueryEndpoint;
	private String sprintName;
	private String epicName;
	private List<String> jiradefecttype;
	private List<String> defectPriority;
	private String[] jiraIssueTypeNames;
	private String storyFirstStatus;
	private String[] linkDefectToStoryField;
	private String envImpacted;
	private String rootCause;
	private List<String> jiraStatusForDevelopment;
	private String jiraAtmProjectId;
	private String jiraAtmProjectKey;
	private List<String> jiraIssueEpicType;

	private List<String> jiraStatusForQa;
	private String testAutomationStatusLabel;
	private String testRegressionLabel;
	private List<String> testRegressionValue;
	// type of test cases
	private List<String> jiraDefectInjectionIssueType;
	private List<String> jiraDod;
	private String jiraDefectCreatedStatus;
	private List<String> jiraTechDebtIssueType;
	private String jiraTechDebtIdentification;
	private String jiraTechDebtCustomField;
	private List<String> jiraTechDebtValue;
	private String jiraDefectRejectionStatus;
	private String jiraBugRaisedByIdentification;
	private List<String> jiraBugRaisedByValue;
	private List<String> jiraDefectSeepageIssueType;
	private String jiraBugRaisedByCustomField;
	private List<String> jiraDefectRemovalStatus;
	private List<String> jiraDefectRemovalIssueType;
	private List<String> regressionAutomationFolderPath;
	private List<String> inSprintAutomationFolderPath;
	/**
	 * Device Platform (iOS/Android/Desktop)
	 */
	private String devicePlatform;
	private String jiraStoryPointsCustomField;
	// parent issue type for the test
	private List<String> jiraTestAutomationIssueType;
	// value of the automated test case Eg. Yes, Cannot Automate, No

	private List<String> jiraCanNotAutomatedTestValue;

	private List<String> jiraSprintVelocityIssueType;

	private List<String> jiraSprintCapacityIssueType;

	private List<String> jiraDefectRejectionlIssueType;
	private List<String> jiraDefectCountlIssueType;

	private List<String> jiraIssueDeliverdStatus;

	private String jiraDor;

	private List<String> jiraIntakeToDorIssueType;

	private List<String> jiraStoryIdentification;

	private String jiraLiveStatus;

	private List<String> regressionAutomationLabels;// TODO delete this field

	private List<String> ticketCountIssueType;

	private List<String> kanbanRCACountIssueType;

	private List<String> jiraTicketVelocityIssueType;

	private List<String> ticketDeliverdStatus;

	private List<String> ticketReopenStatus;

	private List<String> kanbanJiraTechDebtIssueType;

	private List<String> jiraTicketResolvedStatus;
	private List<String> jiraTicketClosedStatus;
	private List<String> kanbanCycleTimeIssueType;
	private List<String> jiraTicketTriagedStatus;
	private List<String> jiraTicketWipStatus;
	private List<String> jiraTicketRejectedStatus;

	private String jiraStatusMappingCustomField;

	private List<String> rootCauseValue;
	private List<String> excludeRCAFromFTPR;

	private Boolean pickNewATMJIRADetails;

	private List<String> jiraDorToLiveIssueType;
	private List<String> jiraProductiveStatus;

	private List<String> jiraCommitmentReliabilityIssueType;

	private List<String> resolutionTypeForRejection;

	private List<String> jiraWasteStatus;
	private List<String> qaRootCauseValue;
	private List<String> jiraQADefectDensityIssueType;
	private String jiraBugRaisedByQACustomField;
	private String jiraBugRaisedByQAIdentification;
	private List<String> jiraBugRaisedByQAValue;
	private List<String> jiraDefectDroppedStatus;

	// Epic custom Field mapping
	private String epicCostOfDelay;
	private String epicRiskReduction;
	private String epicUserBusinessValue;
	private String epicWsjf;
	private String epicTimeCriticality;
	private String epicJobSize;

	private String atmSubprojectField;

	// Squad Mapping
	private String squadIdentifier;
	private List<String> squadIdentMultiValue;
	private String squadIdentSingleValue;

	// Production Defect Mapping
	private String productionDefectCustomField;
	private String productionDefectIdentifier;
	private List<String> productionDefectValue;
	private String productionDefectComponentValue;

	// testCaseMapping
	private String[] jiraTestCaseType;
	private List<String> testCaseStatus;
	private String testAutomatedIdentification;
	private String testAutomationCompletedIdentification;
	private String testRegressionIdentification;
	private String testAutomated;
	private String testAutomationCompletedByCustomField;
	private String testRegressionByCustomField;
	private List<String> jiraAutomatedTestValue;
	private List<String> jiraRegressionTestValue;
	private List<String> jiraCanBeAutomatedTestValue;

	// issue status to exclude missing worklogs
	private List<String> issueStatusToBeExcludedFromMissingWorklogs;
	//field for FTPR
	private List<String> jiraFTPRStoryIdentification;

	@Builder.Default
	private String estimationCriteria = "Story Point";

	@Builder.Default
	private Double storyPointToHourMapping = 8D;

	@Builder.Default
	private Double workingHoursDayCPT = 6D;

	// additional filter config fields
	private List<AdditionalFilterConfig> additionalFilterConfig;

	// issue status to exclude missing worklogs
	private List<String> issueStatusExcluMissingWork;

	/**
	 * Get jira issue type names string [ ].
	 *
	 * @return the string [ ]
	 */
	public String[] getJiraIssueTypeNames() {
		return jiraIssueTypeNames == null ? null : jiraIssueTypeNames.clone();
	}

	/**
	 * Sets jira issue type names.
	 *
	 * @param jiraIssueTypeNames
	 *            the jira issue type names
	 */
	public void setJiraIssueTypeNames(String[] jiraIssueTypeNames) {
		this.jiraIssueTypeNames = jiraIssueTypeNames == null ? null : jiraIssueTypeNames.clone();
	}

	/**
	 * Get link defect to story field string [ ].
	 *
	 * @return the string [ ]
	 */
	public String[] getLinkDefectToStoryField() {
		return linkDefectToStoryField == null ? linkDefectToStoryField : linkDefectToStoryField.clone();
	}

	/**
	 * Sets link defect to story field.
	 *
	 * @param linkDefectToStoryField
	 *            the link defect to story field
	 */
	public void setLinkDefectToStoryField(String[] linkDefectToStoryField) {
		this.linkDefectToStoryField = linkDefectToStoryField == null ? null : linkDefectToStoryField.clone();
	}

	/**
	 * Get jira test case type string [ ].
	 *
	 * @return the string [ ]
	 */
	public String[] getJiraTestCaseType() {
		return jiraTestCaseType == null ? null : jiraTestCaseType.clone();
	}

	/**
	 * Sets jira test case type.
	 *
	 * @param jiraTestCaseType
	 *            the jira test case type
	 */
	public void setJiraTestCaseType(String[] jiraTestCaseType) {
		this.jiraTestCaseType = jiraTestCaseType == null ? null : jiraTestCaseType.clone();
	}

}