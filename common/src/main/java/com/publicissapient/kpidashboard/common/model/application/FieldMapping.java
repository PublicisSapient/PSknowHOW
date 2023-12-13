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


import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.generic.BasicModel;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * The type Field mapping. Represents Jira field mapping values
 */
@SuppressWarnings("PMD.TooManyFields")
@Getter
@Setter
@Document(collection = "field_mapping")
public class FieldMapping extends BasicModel {

	public static final String READY_FOR_TESTING = "Ready For Testing";
	public static final String IN_TESTING = "In Testing";
	private ObjectId projectToolConfigId;
	private ObjectId basicProjectConfigId;
	private String projectId;
	private String sprintName;
	private String epicName;
	private List<String> jiradefecttype;
	private String epicLink;
	private List<String> jiraSubTaskDefectType;

	// defectPriority
	private List<String> defectPriority;
	private List<String> defectPriorityKPI135;
	private List<String> defectPriorityKPI14;
	private List<String> defectPriorityQAKPI111;
	private List<String> defectPriorityKPI82;
	private List<String> defectPriorityKPI133;
	private String[] jiraIssueTypeNames;
	private String[] jiraIssueTypeNamesAVR;
	private List<String> jiraIssueEpicType;
	private String storyFirstStatus;
	private String storyFirstStatusKPI148;
	private String rootCause;
	private List<String> jiraStatusForDevelopment;
	private List<String> jiraStatusForDevelopmentAVR;
	private List<String> jiraStatusForDevelopmentKPI82;
	private List<String> jiraStatusForDevelopmentKPI135;
	@Builder.Default
	private List<String> jiraStatusForQa = Arrays.asList(READY_FOR_TESTING, IN_TESTING);
	@Builder.Default
	private List<String> jiraStatusForQaKPI148 = Arrays.asList(READY_FOR_TESTING, IN_TESTING);
	@Builder.Default
	private List<String> jiraStatusForQaKPI135 = Arrays.asList(READY_FOR_TESTING, IN_TESTING);
	@Builder.Default
	private List<String> jiraStatusForQaKPI82 = Arrays.asList(READY_FOR_TESTING, IN_TESTING);
	// type of test cases
	private List<String> jiraDefectInjectionIssueType;
	private List<String> jiraDefectInjectionIssueTypeKPI14;

	private List<String> jiraDod;
	private List<String> jiraDodKPI152;
	private List<String> jiraDodKPI151;
	private List<String> jiraDodKPI14;
	private List<String> jiraDodQAKPI111;
	private List<String> jiraDodKPI127;
	private List<String> jiraDodKPI37;

	private String jiraDefectCreatedStatus;
	private String jiraDefectCreatedStatusKPI14;
	private List<String> jiraTechDebtIssueType;
	private String jiraTechDebtIdentification;
	private String jiraTechDebtCustomField;
	private List<String> jiraTechDebtValue;

	private String jiraDefectRejectionStatus;
	private String jiraDefectRejectionStatusKPI152;
	private String jiraDefectRejectionStatusKPI151;
	private String jiraDefectRejectionStatusAVR;
	private String jiraDefectRejectionStatusKPI28;
	private String jiraDefectRejectionStatusKPI37;
	private String jiraDefectRejectionStatusKPI35;
	private String jiraDefectRejectionStatusKPI82;
	private String jiraDefectRejectionStatusKPI135;
	private String jiraDefectRejectionStatusKPI133;
	private String jiraDefectRejectionStatusRCAKPI36;
	private String jiraDefectRejectionStatusKPI14;
	private String jiraDefectRejectionStatusQAKPI111;

	private String jiraBugRaisedByIdentification;
	private List<String> jiraBugRaisedByValue;
	private String jiraBugRaisedByCustomField;

	private List<String> jiraDefectSeepageIssueType;
	private List<String> jiraIssueTypeKPI35;

	private List<String> jiraDefectRemovalStatus;
	private List<String> jiraDefectRemovalStatusKPI34;
	private List<String> jiraDefectRemovalIssueType;
	// Added for Defect Reopen Rate KPI.
	private List<String> jiraDefectClosedStatus;
	private List<String> jiraDefectClosedStatusKPI137;

	private String jiraStoryPointsCustomField;
	// parent issue type for the test
	private List<String> jiraTestAutomationIssueType;
	// value of the automated test case Eg. Yes, Cannot Automate, No

	private List<String> jiraSprintVelocityIssueType;
	private List<String> jiraSprintVelocityIssueTypeKPI138;

	private List<String> jiraSprintCapacityIssueType;
	private List<String> jiraSprintCapacityIssueTypeKpi46;

	private List<String> jiraDefectRejectionlIssueType;

	private List<String> jiraDefectCountlIssueType;
	private List<String> jiraDefectCountlIssueTypeKPI28;
	private List<String> jiraDefectCountlIssueTypeKPI36;

	private List<String> jiraIssueDeliverdStatus;
	private List<String> jiraIssueDeliverdStatusKPI138;
	private List<String> jiraIssueDeliverdStatusAVR;
	private List<String> jiraIssueDeliverdStatusKPI126;
	private List<String> jiraIssueDeliverdStatusKPI82;

	private String readyForDevelopmentStatus;
	private List<String> readyForDevelopmentStatusKPI138;

	private String jiraDor;

	private List<String> jiraIntakeToDorIssueType;
	private List<String> jiraIssueTypeKPI3;

	private List<String> jiraStoryIdentification;
	private List<String> jiraStoryIdentificationKPI129;
	private List<String> jiraStoryIdentificationKpi40;
	private List<String> jiraStoryIdentificationKPI164;

	private String jiraLiveStatus;
	private String jiraLiveStatusKPI152;
	private String jiraLiveStatusKPI151;
	private List<String> jiraLiveStatusKPI3;
	private String jiraLiveStatusLTK;
	private String jiraLiveStatusNOPK;
	private String jiraLiveStatusNOSK;
	private String jiraLiveStatusNORK;
	private String jiraLiveStatusOTA;
	private String jiraLiveStatusKPI127;

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
	private List<String> excludeStatusKpi129;

	private String jiraStatusMappingCustomField;

	private List<String> rootCauseValue;
	private List<String> excludeRCAFromFTPR;
	private List<String> includeRCAForKPI82;
	private List<String> includeRCAForKPI135;
	private List<String> includeRCAForKPI14;
	private List<String> includeRCAForQAKPI111;
	private List<String> includeRCAForKPI133;

	// For Lloyds KPIs
	private List<String> jiraDorToLiveIssueType;
	private List<String> jiraProductiveStatus;

	private List<String> jiraCommitmentReliabilityIssueType;

	private List<String> resolutionTypeForRejection;
	private List<String> resolutionTypeForRejectionAVR;
	private List<String> resolutionTypeForRejectionKPI28;
	private List<String> resolutionTypeForRejectionKPI37;
	private List<String> resolutionTypeForRejectionKPI35;
	private List<String> resolutionTypeForRejectionKPI82;
	private List<String> resolutionTypeForRejectionKPI135;
	private List<String> resolutionTypeForRejectionKPI133;
	private List<String> resolutionTypeForRejectionRCAKPI36;
	private List<String> resolutionTypeForRejectionKPI14;
	private List<String> resolutionTypeForRejectionQAKPI111;

	private List<String> jiraQADefectDensityIssueType;
	private List<String> jiraQAKPI111IssueType;
	private List<String> jiraItrQSIssueTypeKPI133;

	private String jiraBugRaisedByQACustomField;
	private String jiraBugRaisedByQAIdentification;
	private List<String> jiraBugRaisedByQAValue;
	private List<String> jiraDefectDroppedStatus;
	private List<String> jiraDefectDroppedStatusKPI127;

	// Epic custom Field mapping
	private String epicCostOfDelay;
	private String epicRiskReduction;
	private String epicUserBusinessValue;
	private String epicWsjf;
	private String epicTimeCriticality;
	private String epicJobSize;
	private String epicPlannedValue;
	private String epicAchievedValue;

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
	private List<String> jiraStatusForInProgressKPI148;
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
	private List<String> issueStatusExcluMissingWork;
	private List<String> issueStatusExcluMissingWorkKPI124;

	// issue On Hold status to exclude Closure possible
	private List<String> jiraOnHoldStatus;

	// field for FTPR
	private List<String> jiraFTPRStoryIdentification;
	private List<String> jiraKPI82StoryIdentification;
	private List<String> jiraKPI135StoryIdentification;

	// field for Wasting - wait status
	private List<String> jiraWaitStatus;
	private List<String> jiraWaitStatusKPI131;

	// field for Wasting - block status
	private List<String> jiraBlockedStatus;
	private List<String> jiraBlockedStatusKPI131;

	// field for Wasting - Include Blocked Status
	private String jiraIncludeBlockedStatus;
	private String jiraIncludeBlockedStatusKPI131;

	// for for JiraDueDate
	@Builder.Default
	private String jiraDueDateField = CommonConstant.DUE_DATE;
	private String jiraDueDateCustomField;
	private String jiraDevDueDateField = CommonConstant.DEV_DUE_DATE;
	private String jiraDevDueDateCustomField;
	private List<String> jiraDevDoneStatus;
	private List<String> jiraDevDoneStatusKPI119;
	private List<String> jiraDevDoneStatusKPI145;
	private List<String> jiraDevDoneStatusKPI128;

	// For DTS_21154 - field for Team refinement status
	private List<String> jiraRejectedInRefinement;
	private List<String> jiraRejectedInRefinementKPI139;

	// For DTS_21154 - field for Stakeholder refinement status
	private List<String> jiraAcceptedInRefinement;
	private List<String> jiraAcceptedInRefinementKPI139;

	// For DTS_21154 - field for Stakeholder refinement status
	private List<String> jiraReadyForRefinement;
	private List<String> jiraReadyForRefinementKPI139;
	private List<String> jiraFtprRejectStatus;
	private List<String> jiraFtprRejectStatusKPI135;
	private List<String> jiraFtprRejectStatusKPI82;

	private List<String> jiraIterationCompletionStatusCustomField;
	private List<String> jiraIterationCompletionStatusKPI135;
	private List<String> jiraIterationCompletionStatusKPI122;
	private List<String> jiraIterationCompletionStatusKPI75;
	private List<String> jiraIterationCompletionStatusKPI145;
	private List<String> jiraIterationCompletionStatusKPI140;
	private List<String> jiraIterationCompletionStatusKPI132;
	private List<String> jiraIterationCompletionStatusKPI136;
	private List<String> jiraIterationCompletionStatusKpi72;
	private List<String> jiraIterationCompletionStatusKpi39;
	private List<String> jiraIterationCompletionStatusKpi5;
	private List<String> jiraIterationCompletionStatusKPI124;
	private List<String> jiraIterationCompletionStatusKPI123;
	private List<String> jiraIterationCompletionStatusKPI125;
	private List<String> jiraIterationCompletionStatusKPI120;
	private List<String> jiraIterationCompletionStatusKPI128;
	private List<String> jiraIterationCompletionStatusKPI134;
	private List<String> jiraIterationCompletionStatusKPI133;
	private List<String> jiraIterationCompletionStatusKPI119;
	private List<String> jiraIterationCompletionStatusKPI131;
	private List<String> jiraIterationCompletionStatusKPI138;

	private List<String> jiraIterationCompletionTypeCustomField;
	private List<String> jiraIterationIssuetypeKPI122;
	private List<String> jiraIterationIssuetypeKPI138;
	private List<String> jiraIterationIssuetypeKPI131;
	private List<String> jiraIterationIssuetypeKPI128;
	private List<String> jiraIterationIssuetypeKPI134;
	private List<String> jiraIterationIssuetypeKPI145;
	private List<String> jiraIterationIssuetypeKpi72;
	private List<String> jiraIterationIssuetypeKPI119;
	private List<String> jiraIterationIssuetypeKpi5;
	private List<String> jiraIterationIssuetypeKPI75;
	private List<String> jiraIterationIssuetypeKPI123;
	private List<String> jiraIterationIssuetypeKPI125;
	private List<String> jiraIterationIssuetypeKPI120;
	private List<String> jiraIterationIssuetypeKPI124;
	private List<String> jiraIterationIssuetypeKPI39;

	private boolean uploadData;
	private boolean uploadDataKPI42;
	private boolean uploadDataKPI16;
	private LocalDateTime createdDate;
	private String jiraDefectRejectionStatusKPI155;
	private List<String> jiraDodKPI155;
	private String jiraLiveStatusKPI155;

	@Builder.Default
	private boolean notificationEnabler=true;

	private List<String> jiraIssueEpicTypeKPI153;

	// DTS-26150 start
	// Testing Phase Defect Mapping
	private String testingPhaseDefectCustomField;
	private String testingPhaseDefectsIdentifier;
	private List<String> testingPhaseDefectValue;
	private String testingPhaseDefectComponentValue;
	private List<String> jiraDodKPI163;
	// DTS-26150 end

	private List<String> jiraIssueTypeNamesKPI161;
	private List<String> jiraIssueTypeNamesKPI146;
	private List<String> jiraIssueTypeNamesKPI148;
	private List<String> jiraIssueTypeNamesKPI151;
	private List<String> jiraIssueTypeNamesKPI152;

	private List<String> jiraDodKPI156;
	private List<String> jiraIssueTypeKPI156;
	private List<String> jiraLabelsKPI14;
	private List<String> jiraLabelsKPI82;
	private List<String> jiraIssueWaitStateKPI170;
	private List<String> jiraIssueClosedStateKPI170;

	@Builder.Default
	private String leadTimeConfigRepoTool = CommonConstant.JIRA;

	@Builder.Default
	private String toBranchForMRKPI156 = "master";
	private Integer startDateCountKPI150;
	private List<String> jiraDevDoneStatusKPI150;
	private boolean populateByDevDoneKPI150;

	//threshold field
	private String thresholdValueKPI14;
	private String thresholdValueKPI82;
	private String thresholdValueKPI111;
	private String thresholdValueKPI35;
	private String thresholdValueKPI34;
	private String thresholdValueKPI37;
	private String thresholdValueKPI28;
	private String thresholdValueKPI36;
	private String thresholdValueKPI16;
	private String thresholdValueKPI17;
	private String thresholdValueKPI38;
	private String thresholdValueKPI27;
	private String thresholdValueKPI72;
	private String thresholdValueKPI84;
	private String thresholdValueKPI11;
	private String thresholdValueKPI62;
	private String thresholdValueKPI64;
	private String thresholdValueKPI67;
	private String thresholdValueKPI65;
	private String thresholdValueKPI157;
	private String thresholdValueKPI158;
	private String thresholdValueKPI159;
	private String thresholdValueKPI160;
	private String thresholdValueKPI164;
	private String thresholdValueKPI3;
	private String thresholdValueKPI126;
	private String thresholdValueKPI42;
	private String thresholdValueKPI168;
	private String thresholdValueKPI70;
	private String thresholdValueKPI40;
	private String thresholdValueKPI5;
	private String thresholdValueKPI39;
	private String thresholdValueKPI46;
	private String thresholdValueKPI8;
	private String thresholdValueKPI73;
	private String thresholdValueKPI113;
	private String thresholdValueKPI149;
	private String thresholdValueKPI153;
	private String thresholdValueKPI162;
	private String thresholdValueKPI116;
	private String thresholdValueKPI156;
	private String thresholdValueKPI118;
	private String thresholdValueKPI127;
	private String thresholdValueKPI170;
	private String thresholdValueKPI139;
	private String thresholdValueKPI166;

	// Production Incident Mapping
	private String jiraProductionIncidentIdentification;
	private String jiraProdIncidentRaisedByCustomField;
	private List<String> jiraProdIncidentRaisedByValue;

	private List<String> jiraStoryIdentificationKPI166;
	private List<String> jiraDodKPI166;
	private List<String> storyFirstStatusKPI154;
	private List<String> jiraStatusForInProgressKPI154;
	private List<String> jiraDevDoneStatusKPI154;
	private List<String> jiraQADoneStatusKPI154;
	private List<String> jiraOnHoldStatusKPI154;
	private List<String> jiraIterationCompletionStatusKPI154;
	private List<String> jiraSubTaskIdentification;
	private List<String> jiraStatusStartDevelopmentKPI154;

	private List<String> jiraLabelsKPI135;

	private List<String> jiraStatusForInProgressKPI161;
	private List<String> jiraStatusForRefinedKPI161;
	private List<String> jiraStatusForNotRefinedKPI161;

	private List<String> jiraIssueTypeKPI171;
	private List<String> jiraDodKPI171;
	private List<String> jiraDorKPI171;
	private List<String> jiraLiveStatusKPI171;
	private String storyFirstStatusKPI171;

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

	public boolean getNotificationEnabler() {
		return notificationEnabler;
	}

	public List<String> getJiraStatusForInProgressKPI154() {
		return jiraStatusForInProgressKPI119;
	}

	public void setJiraStatusForInProgressKPI119(List<String> status) {
		this.jiraStatusForInProgressKPI154 = status;
		this.jiraStatusForInProgressKPI119 = status;
	}
}