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

package com.publicissapient.kpidashboard.common.model.application;//NOPMD

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
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
@Document(collection = "field_mapping")
public class FieldMapping extends BasicModel {

	private ObjectId projectToolConfigId;
	private ObjectId basicProjectConfigId;
	private String projectId;
	private String sprintName;
	private String epicName;
	private List<String> jiradefecttype;//TODO: duplicate
	private List<String> jiradefecttypeSWE;
	private List<String> jiradefecttypeIDCR;
	private List<String> jiradefecttypeIDCS;
	private List<String> jiradefecttypeIDCP;
	private List<String> jiradefecttypeRDCA;
	private List<String> jiradefecttypeRDCP;
	private List<String> jiradefecttypeRDCR;
	private List<String> jiradefecttypeRDCS;
	private List<String> jiradefecttypeQS;
	private List<String> jiradefecttypeIWS;
	private List<String> jiradefecttypeLT;
	private List<String> jiradefecttypeMW;
	private List<String> jiradefecttypeFTPR;
	private List<String> jiradefecttypeIFTPR;
	private List<String> jiradefecttypeIC;
	private List<String> jiradefecttypeAVR;
	private List<String> jiradefecttypeCVR;
	private List<String> jiradefecttypeBDRR;

	// defectPriority
	private List<String> defectPriority;//TODO: Extra field
	private List<String> defectPriorityIFTPR;
	private List<String> defectPriorityDIR;
	private List<String> defectPriorityQADD;
	private List<String> defectPriorityFTPR;
	private List<String> defectPriorityQS;
	private String[] jiraIssueTypeNames;//TODO: duplicate
	private String[] jiraIssueTypeNamesAVR;
	private List<String> jiraIssueEpicType;
	private String storyFirstStatus;
	private String envImpacted;
	private String rootCause;
	private List<String> jiraStatusForDevelopment;// TODO: Extra field
	private List<String> jiraStatusForDevelopmentAVR;
	private List<String> jiraStatusForDevelopmentFTPR;
	private List<String> jiraStatusForDevelopmentIFTPR;
	@Builder.Default
	private List<String> jiraStatusForQa = Arrays.asList("Ready For Testing", "In Testing");
	// type of test cases
	private List<String> jiraDefectInjectionIssueType;
	private List<String> jiraDod;//TODO: Extra field
	private List<String> jiraDodDIR;
	private List<String> jiraDodQADD;
	private List<String> jiraDodLT;
	private List<String> jiraDodPDA;
	private String jiraDefectCreatedStatus;
	private List<String> jiraTechDebtIssueType;
	private String jiraTechDebtIdentification;
	private String jiraTechDebtCustomField;
	private List<String> jiraTechDebtValue;
	private String jiraDefectRejectionStatus;// TODO: Extra field
	private String jiraDefectRejectionStatusAVR;
	private String jiraDefectRejectionStatusDC;
	private String jiraDefectRejectionStatusDRE;
	private String jiraDefectRejectionStatusDRR;
	private String jiraDefectRejectionStatusDSR;
	private String jiraDefectRejectionStatusFTPR;
	private String jiraDefectRejectionStatusIFTPR;
	private String jiraDefectRejectionStatusQS;
	private String jiraDefectRejectionStatusRCA;
	private String jiraDefectRejectionStatusDIR;
	private String jiraDefectRejectionStatusQADD;
	private String jiraBugRaisedByIdentification;
	private List<String> jiraBugRaisedByValue;

	private List<String> jiraDefectSeepageIssueType;
	private String jiraBugRaisedByCustomField;
	private List<String> jiraDefectRemovalStatus;
	private List<String> jiraDefectRemovalIssueType;
	// Added for Defect Reopen Rate KPI.
	private List<String> jiraDefectClosedStatus;
	/**
	 * Device Platform (iOS/Android/Desktop)
	 */
	private String devicePlatform;
	private String jiraStoryPointsCustomField;
	// parent issue type for the test
	private List<String> jiraTestAutomationIssueType;
	// value of the automated test case Eg. Yes, Cannot Automate, No

	private List<String> jiraSprintVelocityIssueType;// TODO: Extra field
	private List<String> jiraSprintVelocityIssueTypeSV;
	private List<String> jiraSprintVelocityIssueTypeBR;

	private List<String> jiraSprintCapacityIssueType;

	private List<String> jiraDefectRejectionlIssueType;
	private List<String> jiraDefectCountlIssueType;// TODO: Extra field
	private List<String> jiraDefectCountlIssueTypeDC;
	private List<String> jiraDefectCountlIssueTypeRCA;

	private List<String> jiraIssueDeliverdStatus;// TODO: Extra field
	private List<String> jiraIssueDeliverdStatusBR;
	private List<String> jiraIssueDeliverdStatusSV;
	private List<String> jiraIssueDeliverdStatusAVR;
	private List<String> jiraIssueDeliverdStatusCVR;
	private List<String> jiraIssueDeliverdStatusFTPR;
	private String readyForDevelopmentStatus;


	private String jiraDor;

	private List<String> jiraIntakeToDorIssueType;

	private List<String> jiraStoryIdentification;
	private List<String> jiraStoryIdentificationIC;

	private String jiraLiveStatus;//TODO: duplicate
	private String jiraLiveStatusLT;
	private String jiraLiveStatusLTK;
	private String jiraLiveStatusNOPK;
	private String jiraLiveStatusNOSK;
	private String jiraLiveStatusNORK;
	private String jiraLiveStatusOTA;//openticketaging
	private String jiraLiveStatusPDA;//productionissueaging

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
	//TODO: Extra field
	private List<String> excludeRCAFromFTPR;//TODO: Extra field
	private List<String> excludeRCAFromIFTPR;
	private List<String> excludeRCAFromDIR;
	private List<String> excludeRCAFromQADD;
	private List<String> excludeRCAFromQS;

	// For Lloyds KPIs
	private List<String> jiraDorToLiveIssueType;
	private List<String> jiraProductiveStatus;

	private List<String> jiraCommitmentReliabilityIssueType;

	private List<String> resolutionTypeForRejection;// TODO: Extra field
	private List<String> resolutionTypeForRejectionAVR;
	private List<String> resolutionTypeForRejectionDC;
	private List<String> resolutionTypeForRejectionDRE;
	private List<String> resolutionTypeForRejectionDRR;
	private List<String> resolutionTypeForRejectionDSR;
	private List<String> resolutionTypeForRejectionFTPR;
	private List<String> resolutionTypeForRejectionIFTPR;
	private List<String> resolutionTypeForRejectionQS;
	private List<String> resolutionTypeForRejectionRCA;
	private List<String> resolutionTypeForRejectionDIR;
	private List<String> resolutionTypeForRejectionQADD;
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

	// Squad Mapping
	private String squadIdentifier;
	private List<String> squadIdentMultiValue;
	private String squadIdentSingleValue;

	// Production Defect Mapping
	private String productionDefectCustomField;
	private String productionDefectIdentifier;
	private List<String> productionDefectValue;
	private String productionDefectComponentValue;

	// field for In Progress status
	private List<String> jiraStatusForInProgress;

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

	// issue On Hold status to exclude Closure possible
	private List<String> jiraOnHoldStatus;

	// field for FTPR
	private List<String> jiraFTPRStoryIdentification;
	private List<String> jiraIFTPRStoryIdentification;

	// field for Wasting - wait status
	private List<String> jiraWaitStatus;

	// field for Wasting - block status
	private List<String> jiraBlockedStatus;

	// field for Wasting - Include Blocked Status
	private String jiraIncludeBlockedStatus;

	// for for JiraDueDate
	@Builder.Default
	private String jiraDueDateField = CommonConstant.DUE_DATE;
	private String jiraDueDateCustomField;
	private String jiraDevDueDateCustomField;
	private List<String> jiraDevDoneStatus;

	// For DTS_21154 - field for Team refinement status
	private List<String> jiraRejectedInRefinement;

	// For DTS_21154 - field for Stakeholder refinement status
	private List<String> jiraAcceptedInRefinement;

	// For DTS_21154 - field for Stakeholder refinement status
	private List<String> jiraReadyForRefinement;
	private List<String> jiraFtprRejectStatus;

	private List<String> jiraIterationCompletionStatusCustomField;// TODO: Extra field
	private List<String> jiraIterationCompletionStatusIFTPR;
	private List<String> jiraIterationCompletionStatusCPT;
	private List<String> jiraIterationCompletionStatusEVA;
	private List<String> jiraIterationCompletionStatusDCS;
	private List<String> jiraIterationCompletionStatusIDCP;
	private List<String> jiraIterationCompletionStatusIDCR;
	private List<String> jiraIterationCompletionStatusIDCS;
	private List<String> jiraIterationCompletionStatusIC;
	private List<String> jiraIterationCompletionStatusCR;
	private List<String> jiraIterationCompletionStatusSV;
	private List<String> jiraIterationCompletionStatusSP;
	private List<String> jiraIterationCompletionStatusEH;
	private List<String> jiraIterationCompletionStatusILS;
	private List<String> jiraIterationCompletionStatusIBU;
	private List<String> jiraIterationCompletionStatusICO;
	private List<String> jiraIterationCompletionStatusPWS;
	private List<String> jiraIterationCompletionStatusUWS;
	private List<String> jiraIterationCompletionStatusQS;
	private List<String> jiraIterationCompletionStatusWR;
	private List<String> jiraIterationCompletionStatusIW;
	private List<String> jiraIterationCompletionStatusBRE;

	private List<String> jiraIterationCompletionTypeCustomField;// TODO: Extra field
	private List<String> jiraIterationCompletionTypeCPT;
	private List<String> jiraIterationCompletionTypeBRE;
	private List<String> jiraIterationCompletionTypeQS;
	private List<String> jiraIterationCompletionTypeIW;
	private List<String> jiraIterationCompletionTypePWS;
	private List<String> jiraIterationCompletionTypeUPWS;
	private List<String> jiraIterationCompletionTypeDCS;
	private List<String> jiraIterationCompletionTypeIFTPR;
	private List<String> jiraIterationCompletionTypeIDCP;
	private List<String> jiraIterationCompletionTypeIDCR;
	private List<String> jiraIterationCompletionTypeIDCS;
	private List<String> jiraIterationCompletionTypeIC;
	private List<String> jiraIterationCompletionTypeCR;
	private List<String> jiraIterationCompletionTypeSV;
	private List<String> jiraIterationCompletionTypeWR;
	private List<String> jiraIterationCompletionTypeSP;
	private List<String> jiraIterationCompletionTypeEVA;
	private List<String> jiraIterationCompletionTypeILS;
	private List<String> jiraIterationCompletionTypeIBU;
	private List<String> jiraIterationCompletionTypeICO;
	private List<String> jiraIterationCompletionTypeEH;
	private LocalDate createdDate;

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

}