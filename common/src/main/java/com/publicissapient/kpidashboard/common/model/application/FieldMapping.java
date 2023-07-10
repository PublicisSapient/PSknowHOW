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
	private List<String> jiradefecttypeKPI132;
	private List<String> jiradefecttypeKPI136;
	private List<String> jiradefecttypeKPI140;
	private List<String> jiradefecttypeRDCA;
	private List<String> jiradefecttypeRDCP;
	private List<String> jiradefecttypeRDCR;
	private List<String> jiradefecttypeRDCS;
	private List<String> jiradefecttypeKPI133;
	private List<String> jiradefecttypeIWS;
	private List<String> jiradefecttypeLT;
	private List<String> jiradefecttypeMW;
	private List<String> jiradefecttypeFTPR;
	private List<String> jiradefecttypeKPI135;
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
	private List<String> defectPriorityKPI133;

	private String[] jiraIssueTypeNames;//TODO: Extra field
	private String[] jiraIssueTypeNamesAVR;
	private List<String> jiraIssueEpicType;
	private String storyFirstStatus;//TODO: Extra field
	private String storyFirstStatusLT;
	private String rootCause;
	private List<String> jiraStatusForDevelopment;// TODO: Extra field
	private List<String> jiraStatusForDevelopmentAVR;
	private List<String> jiraStatusForDevelopmentFTPR;
	private List<String> jiraStatusForDevelopmentKPI135;
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
	private String jiraDefectRejectionStatus;// TODO: Extra field //test done
	private String jiraDefectRejectionStatusAVR;
	private String jiraDefectRejectionStatusDC;
	private String jiraDefectRejectionStatusDRE;
	private String jiraDefectRejectionStatusDRR;
	private String jiraDefectRejectionStatusDSR;
	private String jiraDefectRejectionStatusFTPR;
	private String jiraDefectRejectionStatusIFTPR;
	private String jiraDefectRejectionStatusKPI133;
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

	private String jiraStoryPointsCustomField;
	// parent issue type for the test
	private List<String> jiraTestAutomationIssueType;
	// value of the automated test case Eg. Yes, Cannot Automate, No

	private List<String> jiraSprintVelocityIssueType;// TODO: Extra field  //test done
	private List<String> jiraSprintVelocityIssueTypeSV;
	private List<String> jiraSprintVelocityIssueTypeBR;

	private List<String> jiraSprintCapacityIssueType;

	private List<String> jiraDefectRejectionlIssueType;
	private List<String> jiraDefectCountlIssueType;// TODO: Extra field // test done
	private List<String> jiraDefectCountlIssueTypeDC;
	private List<String> jiraDefectCountlIssueTypeRCA;

	private List<String> jiraIssueDeliverdStatus;// TODO: Extra field //test done
	private List<String> jiraIssueDeliverdStatusBR;
	private List<String> jiraIssueDeliverdStatusSV;
	private List<String> jiraIssueDeliverdStatusAVR;
	private List<String> jiraIssueDeliverdStatusCVR;
	private List<String> jiraIssueDeliverdStatusFTPR;
	private String readyForDevelopmentStatus;


	private String jiraDorLT;

	private List<String> jiraIntakeToDorIssueType;// TODO: Extra field
	private List<String> jiraIntakeToDorIssueTypeLT;

	private List<String> jiraStoryIdentification;// TODO: Duplicate
	private List<String> jiraStoryIdentificationIC;

	private String jiraLiveStatus;//TODO: Extra field
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
	private List<String> excludeRCAFromFTPR;//TODO: Extra field // test done
	private List<String> excludeRCAFromIFTPR;
	private List<String> excludeRCAFromDIR;
	private List<String> excludeRCAFromQADD;
	private List<String> excludeRCAFromKPI133;

	// For Lloyds KPIs
	private List<String> jiraDorToLiveIssueType;
	private List<String> jiraProductiveStatus;

	private List<String> jiraCommitmentReliabilityIssueType;

	private List<String> resolutionTypeForRejection;// TODO: Extra field  //test done
	private List<String> resolutionTypeForRejectionAVR;
	private List<String> resolutionTypeForRejectionDC;
	private List<String> resolutionTypeForRejectionDRE;
	private List<String> resolutionTypeForRejectionDRR;
	private List<String> resolutionTypeForRejectionDSR;
	private List<String> resolutionTypeForRejectionFTPR;
	private List<String> resolutionTypeForRejectionIFTPR;
	private List<String> resolutionTypeForRejectionKPI133;
	private List<String> resolutionTypeForRejectionRCA;
	private List<String> resolutionTypeForRejectionDIR;
	private List<String> resolutionTypeForRejectionQADD;
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
	private List<String> jiraStatusForInProgress;// TODO: Extra field
	private List<String> jiraStatusForInProgressKPI122;
	private List<String> jiraStatusForInProgressKPI145;
	private List<String> jiraStatusForInProgressKPI125;
	private List<String> jiraStatusForInProgressKPI128;
	private List<String> jiraStatusForInProgressKPI123;
	private List<String> jiraStatusForInProgressKPI119;

	@Builder.Default
	private String estimationCriteria = "Story Point";

	@Builder.Default
	private Double storyPointToHourMapping = 8D;

	@Builder.Default
	private Double workingHoursDayCPT = 6D;

	// additional filter config fields
	private List<AdditionalFilterConfig> additionalFilterConfig;

	// issue status to exclude missing worklogs
	private List<String> issueStatusExcluMissingWork;// TODO: Extra field
	private List<String> issueStatusExcluMissingWorkKPI124;

	// issue On Hold status to exclude Closure possible
	private List<String> jiraOnHoldStatus;

	// field for FTPR
	private List<String> jiraFTPRStoryIdentification;
	private List<String> jiraKPI135StoryIdentification;

	// field for Wasting - wait status
	private List<String> jiraWaitStatus;// TODO: Extra field
	private List<String> jiraWaitStatusKPI131;

	// field for Wasting - block status
	private List<String> jiraBlockedStatus;// TODO: Extra field
	private List<String> jiraBlockedStatusIW;

	// field for Wasting - Include Blocked Status
	private String jiraIncludeBlockedStatus; // TODO: Extra field
	private String jiraIncludeBlockedStatusKPI131;

	// for for JiraDueDate
	@Builder.Default
	private String jiraDueDateField = CommonConstant.DUE_DATE;
	private String jiraDueDateCustomField;
	private String jiraDevDueDateCustomField;
	private List<String> jiraDevDoneStatus;// TODO: Extra field
	private List<String> jiraDevDoneStatusKPI119;
	private List<String> jiraDevDoneStatusKPI145;
	private List<String> jiraDevDoneStatusKPI128;

	// For DTS_21154 - field for Team refinement status
	private List<String> jiraRejectedInRefinement;

	// For DTS_21154 - field for Stakeholder refinement status
	private List<String> jiraAcceptedInRefinement;

	// For DTS_21154 - field for Stakeholder refinement status
	private List<String> jiraReadyForRefinement;
	private List<String> jiraFtprRejectStatus;

	private List<String> jiraIterationCompletionStatusCustomField;// TODO: Duplicate //test done
	private List<String> jiraIterationCompletionStatusKPI135;
	private List<String> jiraIterationCompletionStatusKPI122;
	private List<String> jiraIterationCompletionStatusKPI75;
	private List<String> jiraIterationCompletionStatusKPI145;
	private List<String> jiraIterationCompletionStatusKPI140;
	private List<String> jiraIterationCompletionStatusKPI132;
	private List<String> jiraIterationCompletionStatusKPI136;
	private List<String> jiraIterationCompletionStatusIC;
	private List<String> jiraIterationCompletionStatusCR;
	private List<String> jiraIterationCompletionStatusSV;
	private List<String> jiraIterationCompletionStatusSP;
	private List<String> jiraIterationCompletionStatusKPI124;
	private List<String> jiraIterationCompletionStatusKPI123;
	private List<String> jiraIterationCompletionStatusKPI125;
	private List<String> jiraIterationCompletionStatusKPI120;
	private List<String> jiraIterationCompletionStatusKPI128;
	private List<String> jiraIterationCompletionStatusKPI134;
	private List<String> jiraIterationCompletionStatusKPI133;
	private List<String> jiraIterationCompletionStatusKPI119;
	private List<String> jiraIterationCompletionStatusKPI131;
	private List<String> jiraIterationCompletionStatusBRE;

	private List<String> jiraIterationCompletionTypeCustomField;// TODO: Extra field  //test done
	private List<String> jiraIterationCompletionTypeKPI122;
	private List<String> jiraIterationCompletionTypeBRE;
//	private List<String> jiraIterationCompletionTypeQS;
	private List<String> jiraIterationCompletionTypeKPI131;
	private List<String> jiraIterationCompletionTypeKPI128;
	private List<String> jiraIterationCompletionTypeKPI134;
	private List<String> jiraIterationCompletionTypeKPI145;
//	private List<String> jiraIterationCompletionTypeIFTPR;
//	private List<String> jiraIterationCompletionTypeIDCP;
//	private List<String> jiraIterationCompletionTypeIDCR;
//	private List<String> jiraIterationCompletionTypeIDCS;
//	private List<String> jiraIterationCompletionTypeIC;
	private List<String> jiraIterationCompletionTypeCR;
//	private List<String> jiraIterationCompletionTypeSV;
	private List<String> jiraIterationCompletionTypeKPI119;
	private List<String> jiraIterationCompletionTypeSP;
	private List<String> jiraIterationCompletionTypeKPI75;
	private List<String> jiraIterationCompletionTypeKPI123;
	private List<String> jiraIterationCompletionTypeKPI125;
	private List<String> jiraIterationCompletionTypeKPI120;
	private List<String> jiraIterationCompletionTypeKPI124;
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