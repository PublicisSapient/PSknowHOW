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

package com.publicissapient.kpidashboard.common.model.application; // NOPMD

import java.time.LocalDateTime;
import java.util.List;

import com.publicissapient.kpidashboard.common.model.generic.BasicModel;

import lombok.Getter;
import lombok.Setter;

/**
 * The History of each FieldMapping
 *
 * @author shi6
 */
@SuppressWarnings("PMD.TooManyFields")
@Getter
@Setter
public class FieldMappingHistory extends BasicModel {

	private List<ConfigurationHistoryChangeLog> historysprintName;
	private List<ConfigurationHistoryChangeLog> historyepicName;
	private List<ConfigurationHistoryChangeLog> historyjiradefecttype;
	private List<ConfigurationHistoryChangeLog> historyepicLink;
	private List<ConfigurationHistoryChangeLog> historyjiraSubTaskDefectType;
	private List<ConfigurationHistoryChangeLog> historydefectPriority;
	private List<ConfigurationHistoryChangeLog> historydefectPriorityKPI135;
	private List<ConfigurationHistoryChangeLog> historydefectPriorityKPI35;
	private List<ConfigurationHistoryChangeLog> historydefectPriorityKPI14;
	private List<ConfigurationHistoryChangeLog> historydefectPriorityQAKPI111;
	private List<ConfigurationHistoryChangeLog> historydefectPriorityKPI82;
	private List<ConfigurationHistoryChangeLog> historydefectPriorityKPI133;
	private List<ConfigurationHistoryChangeLog> historydefectPriorityKPI34;
	private List<ConfigurationHistoryChangeLog> historyjiraIssueTypeNames;
	private List<ConfigurationHistoryChangeLog> historyjiraIssueTypeNamesAVR;
	private List<ConfigurationHistoryChangeLog> historyjiraIssueEpicType;
	private List<ConfigurationHistoryChangeLog> historyjiraIssueRiskTypeKPI176;
	private List<ConfigurationHistoryChangeLog> historyjiraIssueDependencyTypeKPI176;
	private List<ConfigurationHistoryChangeLog> historystoryFirstStatus;
	private List<ConfigurationHistoryChangeLog> historystoryFirstStatusKPI148;
	private List<ConfigurationHistoryChangeLog> historyrootCause;
	private List<ConfigurationHistoryChangeLog> historyrootCauseValues;
	private List<ConfigurationHistoryChangeLog> historyrootCauseIdentifier;
	private List<ConfigurationHistoryChangeLog> historyjiraStatusForDevelopment;
	private List<ConfigurationHistoryChangeLog> historyjiraStatusForDevelopmentAVR;
	private List<ConfigurationHistoryChangeLog> historyjiraStatusForDevelopmentKPI82;
	private List<ConfigurationHistoryChangeLog> historyjiraStatusForDevelopmentKPI135;
	private List<ConfigurationHistoryChangeLog> historyjiraStatusForQa;
	private List<ConfigurationHistoryChangeLog> historyjiraStatusForQaKPI148;
	private List<ConfigurationHistoryChangeLog> historyjiraStatusForQaKPI135;
	private List<ConfigurationHistoryChangeLog> historyjiraStatusForQaKPI82;
	private List<ConfigurationHistoryChangeLog> historyjiraDefectInjectionIssueType;
	private List<ConfigurationHistoryChangeLog> historyjiraDefectInjectionIssueTypeKPI14;

	private List<ConfigurationHistoryChangeLog> historyjiraDod;
	private List<ConfigurationHistoryChangeLog> historyjiraDodKPI152;
	private List<ConfigurationHistoryChangeLog> historyjiraDodKPI151;
	private List<ConfigurationHistoryChangeLog> historyjiraDodKPI14;
	private List<ConfigurationHistoryChangeLog> historyjiraDodQAKPI111;
	private List<ConfigurationHistoryChangeLog> historyjiraDodKPI37;
	private List<ConfigurationHistoryChangeLog> historyjiraDodKPI142;
	private List<ConfigurationHistoryChangeLog> historyjiraDodKPI144;
	private List<ConfigurationHistoryChangeLog> historyjiraDodKPI143;

	private List<ConfigurationHistoryChangeLog> historyjiraDefectCreatedStatus;
	private List<ConfigurationHistoryChangeLog> historyjiraDefectCreatedStatusKPI14;
	private List<ConfigurationHistoryChangeLog> historyjiraTechDebtIssueType;
	private List<ConfigurationHistoryChangeLog> historyjiraTechDebtIdentification;
	private List<ConfigurationHistoryChangeLog> historyjiraTechDebtCustomField;
	private List<ConfigurationHistoryChangeLog> historyjiraTechDebtValue;

	private List<ConfigurationHistoryChangeLog> historyjiraDefectRejectionStatus;
	private List<ConfigurationHistoryChangeLog> historyjiraDefectRejectionStatusKPI152;
	private List<ConfigurationHistoryChangeLog> historyjiraDefectRejectionStatusKPI151;
	private List<ConfigurationHistoryChangeLog> historyjiraDefectRejectionStatusAVR;
	private List<ConfigurationHistoryChangeLog> historyjiraDefectRejectionStatusKPI28;
	private List<ConfigurationHistoryChangeLog> historyjiraDefectRejectionStatusKPI37;
	private List<ConfigurationHistoryChangeLog> historyjiraDefectRejectionStatusKPI35;
	private List<ConfigurationHistoryChangeLog> historyjiraDefectRejectionStatusKPI82;
	private List<ConfigurationHistoryChangeLog> historyjiraDefectRejectionStatusKPI135;
	private List<ConfigurationHistoryChangeLog> historyjiraDefectRejectionStatusKPI133;
	private List<ConfigurationHistoryChangeLog> historyjiraDefectRejectionStatusRCAKPI36;
	private List<ConfigurationHistoryChangeLog> historyjiraDefectRejectionStatusKPI14;
	private List<ConfigurationHistoryChangeLog> historyjiraDefectRejectionStatusQAKPI111;
	private List<ConfigurationHistoryChangeLog> historyjiraDefectRejectionStatusKPI34;

	private List<ConfigurationHistoryChangeLog> historyjiraBugRaisedByIdentification;
	private List<ConfigurationHistoryChangeLog> historyjiraBugRaisedByValue;
	private List<ConfigurationHistoryChangeLog> historyjiraBugRaisedByCustomField;

	private List<ConfigurationHistoryChangeLog> historyjiraDefectSeepageIssueType;
	private List<ConfigurationHistoryChangeLog> historyjiraIssueTypeKPI35;

	private List<ConfigurationHistoryChangeLog> historyjiraDefectRemovalStatus;
	private List<ConfigurationHistoryChangeLog> historyjiraDefectRemovalStatusKPI34;
	private List<ConfigurationHistoryChangeLog> historyjiraDefectRemovalIssueType;
	// Added for Defect Reopen Rate KPI.
	private List<ConfigurationHistoryChangeLog> historyjiraDefectClosedStatus;
	private List<ConfigurationHistoryChangeLog> historyjiraDefectClosedStatusKPI137;

	private List<ConfigurationHistoryChangeLog> historyjiraStoryPointsCustomField;
	// parent issue type for the test
	private List<ConfigurationHistoryChangeLog> historyjiraTestAutomationIssueType;
	// value of the automated test case Eg. Yes, Cannot Automate, No

	private List<ConfigurationHistoryChangeLog> historyjiraSprintVelocityIssueType;
	private List<ConfigurationHistoryChangeLog> historyjiraSprintVelocityIssueTypeKPI138;

	private List<ConfigurationHistoryChangeLog> historyjiraSprintCapacityIssueType;
	private List<ConfigurationHistoryChangeLog> historyjiraSprintCapacityIssueTypeKpi46;

	private List<ConfigurationHistoryChangeLog> historyjiraDefectRejectionlIssueType;

	private List<ConfigurationHistoryChangeLog> historyjiraDefectCountlIssueType;
	private List<ConfigurationHistoryChangeLog> historyjiraDefectCountlIssueTypeKPI28;
	private List<ConfigurationHistoryChangeLog> historyjiraDefectCountlIssueTypeKPI36;

	private List<ConfigurationHistoryChangeLog> historyjiraIssueDeliverdStatus;
	private List<ConfigurationHistoryChangeLog> historyjiraIssueDeliverdStatusKPI138;
	private List<ConfigurationHistoryChangeLog> historyjiraIssueDeliverdStatusAVR;
	private List<ConfigurationHistoryChangeLog> historyjiraIssueDeliverdStatusKPI126;
	private List<ConfigurationHistoryChangeLog> historyjiraIssueDeliverdStatusKPI82;

	private List<ConfigurationHistoryChangeLog> historyreadyForDevelopmentStatus;
	private List<ConfigurationHistoryChangeLog> historyreadyForDevelopmentStatusKPI138;

	private List<ConfigurationHistoryChangeLog> historyjiraDor;

	private List<ConfigurationHistoryChangeLog> historyjiraIntakeToDorIssueType;
	private List<ConfigurationHistoryChangeLog> historyjiraIssueTypeKPI3;

	private List<ConfigurationHistoryChangeLog> historyjiraStoryIdentification;
	private List<ConfigurationHistoryChangeLog> historyjiraStoryIdentificationKPI129;
	private List<ConfigurationHistoryChangeLog> historyjiraStoryIdentificationKpi40;
	private List<ConfigurationHistoryChangeLog> historyjiraStoryCategoryKpi40;
	private List<ConfigurationHistoryChangeLog> historyjiraStoryIdentificationKPI164;

	private List<ConfigurationHistoryChangeLog> historyjiraLiveStatus;
	private List<ConfigurationHistoryChangeLog> historyjiraLiveStatusKPI152;
	private List<ConfigurationHistoryChangeLog> historyjiraLiveStatusKPI151;
	private List<ConfigurationHistoryChangeLog> historyjiraLiveStatusKPI3;
	private List<ConfigurationHistoryChangeLog> historyjiraLiveStatusKPI53;
	private List<ConfigurationHistoryChangeLog> historyjiraLiveStatusKPI50;
	private List<ConfigurationHistoryChangeLog> historyjiraLiveStatusKPI48;
	private List<ConfigurationHistoryChangeLog> historyjiraLiveStatusKPI51;
	private List<ConfigurationHistoryChangeLog> historyjiraLiveStatusKPI997;

	private List<ConfigurationHistoryChangeLog> historyticketCountIssueType;
	private List<ConfigurationHistoryChangeLog> historyticketCountIssueTypeKPI50;
	private List<ConfigurationHistoryChangeLog> historyticketCountIssueTypeKPI48;
	private List<ConfigurationHistoryChangeLog> historyticketCountIssueTypeKPI997;
	private List<ConfigurationHistoryChangeLog> historyticketCountIssueTypeKPI54;
	private List<ConfigurationHistoryChangeLog> historyticketCountIssueTypeKPI55;

	private List<ConfigurationHistoryChangeLog> historykanbanRCACountIssueType;
	private List<ConfigurationHistoryChangeLog> historykanbanRCACountIssueTypeKPI51;

	private List<ConfigurationHistoryChangeLog> historyjiraTicketVelocityIssueTypeKPI49;

	private List<ConfigurationHistoryChangeLog> historyticketDeliveredStatusKPI49;

	private List<ConfigurationHistoryChangeLog> historyticketReopenStatus;

	private List<ConfigurationHistoryChangeLog> historykanbanJiraTechDebtIssueType;

	private List<ConfigurationHistoryChangeLog> historyjiraTicketResolvedStatus;
	private List<ConfigurationHistoryChangeLog> historyjiraTicketClosedStatus;
	private List<ConfigurationHistoryChangeLog> historyjiraTicketClosedStatusKPI48;
	private List<ConfigurationHistoryChangeLog> historyjiraTicketClosedStatusKPI50;
	private List<ConfigurationHistoryChangeLog> historyjiraTicketClosedStatusKPI51;
	private List<ConfigurationHistoryChangeLog> historyjiraTicketClosedStatusKPI53;
	private List<ConfigurationHistoryChangeLog> historyjiraTicketClosedStatusKPI54;
	private List<ConfigurationHistoryChangeLog> historyjiraTicketClosedStatusKPI55;
	private List<ConfigurationHistoryChangeLog> historyjiraTicketClosedStatusKPI997;
	private List<ConfigurationHistoryChangeLog> historykanbanCycleTimeIssueType;
	private List<ConfigurationHistoryChangeLog> historykanbanCycleTimeIssueTypeKPI53;
	private List<ConfigurationHistoryChangeLog> historyjiraTicketTriagedStatus;
	private List<ConfigurationHistoryChangeLog> historyjiraTicketTriagedStatusKPI53;
	private List<ConfigurationHistoryChangeLog> historyjiraTicketWipStatus;
	private List<ConfigurationHistoryChangeLog> historyjiraTicketRejectedStatus;
	private List<ConfigurationHistoryChangeLog> historyjiraTicketRejectedStatusKPI50;
	private List<ConfigurationHistoryChangeLog> historyjiraTicketRejectedStatusKPI151;
	private List<ConfigurationHistoryChangeLog> historyjiraTicketRejectedStatusKPI48;
	private List<ConfigurationHistoryChangeLog> historyjiraTicketRejectedStatusKPI997;
	private List<ConfigurationHistoryChangeLog> historyexcludeStatusKpi129;

	private List<ConfigurationHistoryChangeLog> historyjiraStatusMappingCustomField;

	private List<ConfigurationHistoryChangeLog> historyrootCauseValue;
	private List<ConfigurationHistoryChangeLog> historyexcludeRCAFromFTPR;
	private List<ConfigurationHistoryChangeLog> historyexcludeRCAFromKPI163;
	private List<ConfigurationHistoryChangeLog> historyincludeRCAForKPI82;
	private List<ConfigurationHistoryChangeLog> historyincludeRCAForKPI135;
	private List<ConfigurationHistoryChangeLog> historyincludeRCAForKPI14;
	private List<ConfigurationHistoryChangeLog> historyincludeRCAForQAKPI111;
	private List<ConfigurationHistoryChangeLog> historyincludeRCAForKPI133;
	private List<ConfigurationHistoryChangeLog> historyincludeRCAForKPI35;
	private List<ConfigurationHistoryChangeLog> historyincludeRCAForKPI34;

	private List<ConfigurationHistoryChangeLog> historyjiraDorToLiveIssueType;
	private List<ConfigurationHistoryChangeLog> historyjiraProductiveStatus;

	private List<ConfigurationHistoryChangeLog> historyjiraCommitmentReliabilityIssueType;

	private List<ConfigurationHistoryChangeLog> historyresolutionTypeForRejection;
	private List<ConfigurationHistoryChangeLog> historyresolutionTypeForRejectionAVR;
	private List<ConfigurationHistoryChangeLog> historyresolutionTypeForRejectionKPI28;
	private List<ConfigurationHistoryChangeLog> historyresolutionTypeForRejectionKPI37;
	private List<ConfigurationHistoryChangeLog> historyresolutionTypeForRejectionKPI35;
	private List<ConfigurationHistoryChangeLog> historyresolutionTypeForRejectionKPI82;
	private List<ConfigurationHistoryChangeLog> historyresolutionTypeForRejectionKPI135;
	private List<ConfigurationHistoryChangeLog> historyresolutionTypeForRejectionKPI133;
	private List<ConfigurationHistoryChangeLog> historyresolutionTypeForRejectionRCAKPI36;
	private List<ConfigurationHistoryChangeLog> historyresolutionTypeForRejectionKPI14;
	private List<ConfigurationHistoryChangeLog> historyresolutionTypeForRejectionQAKPI111;
	private List<ConfigurationHistoryChangeLog> historyresolutionTypeForRejectionKPI34;

	private List<ConfigurationHistoryChangeLog> historyjiraQADefectDensityIssueType;
	private List<ConfigurationHistoryChangeLog> historyjiraQAKPI111IssueType;
	private List<ConfigurationHistoryChangeLog> historyjiraItrQSIssueTypeKPI133;

	private List<ConfigurationHistoryChangeLog> historyjiraDefectDroppedStatus;

	private List<ConfigurationHistoryChangeLog> historyepicCostOfDelay;
	private List<ConfigurationHistoryChangeLog> historyepicRiskReduction;
	private List<ConfigurationHistoryChangeLog> historyepicUserBusinessValue;
	private List<ConfigurationHistoryChangeLog> historyepicWsjf;
	private List<ConfigurationHistoryChangeLog> historyepicTimeCriticality;
	private List<ConfigurationHistoryChangeLog> historyepicJobSize;
	private List<ConfigurationHistoryChangeLog> historyepicPlannedValue;
	private List<ConfigurationHistoryChangeLog> historyepicAchievedValue;

	// Squad Mapping
	private List<ConfigurationHistoryChangeLog> historysquadIdentifier;
	private List<ConfigurationHistoryChangeLog> historysquadIdentMultiValue;
	private List<ConfigurationHistoryChangeLog> historysquadIdentSingleValue;

	// Production Defect Mapping
	private List<ConfigurationHistoryChangeLog> historyproductionDefectCustomField;
	private List<ConfigurationHistoryChangeLog> historyproductionDefectIdentifier;
	private List<ConfigurationHistoryChangeLog> historyproductionDefectValue;
	private List<ConfigurationHistoryChangeLog> historyproductionDefectComponentValue;

	// field for In Progress status
	private List<ConfigurationHistoryChangeLog> historyjiraStatusForInProgress;
	private List<ConfigurationHistoryChangeLog> historyjiraStatusForInProgressKPI148;
	private List<ConfigurationHistoryChangeLog> historyjiraStatusForInProgressKPI122;
	private List<ConfigurationHistoryChangeLog> historyjiraStatusForInProgressKPI145;
	private List<ConfigurationHistoryChangeLog> historyjiraStatusForInProgressKPI125;
	private List<ConfigurationHistoryChangeLog> historyjiraStatusForInProgressKPI128;
	private List<ConfigurationHistoryChangeLog> historyjiraStatusForInProgressKPI123;
	private List<ConfigurationHistoryChangeLog> historyjiraStatusForInProgressKPI119;

	private List<ConfigurationHistoryChangeLog> historyestimationCriteria;

	private List<ConfigurationHistoryChangeLog> historystoryPointToHourMapping;

	private List<ConfigurationHistoryChangeLog> historyworkingHoursDayCPT;

	// additional filter config fields
	private List<ConfigurationHistoryChangeLog> historyadditionalFilterConfig;

	// issue status to exclude missing worklogs
	private List<ConfigurationHistoryChangeLog> historyissueStatusExcluMissingWork;
	private List<ConfigurationHistoryChangeLog> historyissueStatusExcluMissingWorkKPI124;

	// issue On Hold status to exclude Closure possible
	private List<ConfigurationHistoryChangeLog> historyjiraOnHoldStatus;

	// field for FTPR
	private List<ConfigurationHistoryChangeLog> historyjiraFTPRStoryIdentification;
	private List<ConfigurationHistoryChangeLog> historyjiraKPI82StoryIdentification;
	private List<ConfigurationHistoryChangeLog> historyjiraKPI135StoryIdentification;

	// field for Wasting - wait status
	private List<ConfigurationHistoryChangeLog> historyjiraWaitStatus;
	private List<ConfigurationHistoryChangeLog> historyjiraWaitStatusKPI131;

	// field for Wasting - block status
	private List<ConfigurationHistoryChangeLog> historyjiraBlockedStatus;
	private List<ConfigurationHistoryChangeLog> historyjiraBlockedStatusKPI131;

	// field for Wasting - Include Blocked Status
	private List<ConfigurationHistoryChangeLog> historyjiraIncludeBlockedStatus;
	private List<ConfigurationHistoryChangeLog> historyjiraIncludeBlockedStatusKPI131;

	private List<ConfigurationHistoryChangeLog> historyjiraDueDateField;
	private List<ConfigurationHistoryChangeLog> historyjiraDueDateCustomField;
	private List<ConfigurationHistoryChangeLog> historyjiraDevDueDateField;
	private List<ConfigurationHistoryChangeLog> historyjiraDevDueDateCustomField;
	private List<ConfigurationHistoryChangeLog> historyjiraDevDoneStatus;
	private List<ConfigurationHistoryChangeLog> historyjiraDevDoneStatusKPI119;
	private List<ConfigurationHistoryChangeLog> historyjiraDevDoneStatusKPI145;
	private List<ConfigurationHistoryChangeLog> historyjiraDevDoneStatusKPI128;

	// For DTS_21154 - field for Team refinement status
	private List<ConfigurationHistoryChangeLog> historyjiraRejectedInRefinement;
	private List<ConfigurationHistoryChangeLog> historyjiraRejectedInRefinementKPI139;

	// For DTS_21154 - field for Stakeholder refinement status
	private List<ConfigurationHistoryChangeLog> historyjiraAcceptedInRefinement;
	private List<ConfigurationHistoryChangeLog> historyjiraAcceptedInRefinementKPI139;

	// For DTS_21154 - field for Stakeholder refinement status
	private List<ConfigurationHistoryChangeLog> historyjiraReadyForRefinement;
	private List<ConfigurationHistoryChangeLog> historyjiraReadyForRefinementKPI139;
	private List<ConfigurationHistoryChangeLog> historyjiraFtprRejectStatus;
	private List<ConfigurationHistoryChangeLog> historyjiraFtprRejectStatusKPI135;
	private List<ConfigurationHistoryChangeLog> historyjiraFtprRejectStatusKPI82;

	private List<ConfigurationHistoryChangeLog> historyjiraIterationCompletionStatusCustomField;
	private List<ConfigurationHistoryChangeLog> historyjiraIterationCompletionStatusKPI135;
	private List<ConfigurationHistoryChangeLog> historyjiraIterationCompletionStatusKPI122;
	private List<ConfigurationHistoryChangeLog> historyjiraIterationCompletionStatusKPI75;
	private List<ConfigurationHistoryChangeLog> historyjiraIterationCompletionStatusKPI145;
	private List<ConfigurationHistoryChangeLog> historyjiraIterationCompletionStatusKPI140;
	private List<ConfigurationHistoryChangeLog> historyjiraIterationCompletionStatusKPI132;
	private List<ConfigurationHistoryChangeLog> historyjiraIterationCompletionStatusKPI136;
	private List<ConfigurationHistoryChangeLog> historyjiraIterationCompletionStatusKpi72;
	private List<ConfigurationHistoryChangeLog> historyjiraIterationCompletionStatusKpi39;
	private List<ConfigurationHistoryChangeLog> historyjiraIterationCompletionStatusKpi5;
	private List<ConfigurationHistoryChangeLog> historyjiraIterationCompletionStatusKPI124;
	private List<ConfigurationHistoryChangeLog> historyjiraIterationCompletionStatusKPI123;
	private List<ConfigurationHistoryChangeLog> historyjiraIterationCompletionStatusKPI125;
	private List<ConfigurationHistoryChangeLog> historyjiraIterationCompletionStatusKPI120;
	private List<ConfigurationHistoryChangeLog> historyjiraIterationCompletionStatusKPI128;
	private List<ConfigurationHistoryChangeLog> historyjiraIterationCompletionStatusKPI134;
	private List<ConfigurationHistoryChangeLog> historyjiraIterationCompletionStatusKPI133;
	private List<ConfigurationHistoryChangeLog> historyjiraIterationCompletionStatusKPI119;
	private List<ConfigurationHistoryChangeLog> historyjiraIterationCompletionStatusKPI131;
	private List<ConfigurationHistoryChangeLog> historyjiraIterationCompletionStatusKPI138;
	private List<ConfigurationHistoryChangeLog> historyjiraIterationCompletionStatusKPI176;

	private List<ConfigurationHistoryChangeLog> historyjiraIterationCompletionTypeCustomField;
	private List<ConfigurationHistoryChangeLog> historyjiraIterationIssuetypeKPI122;
	private List<ConfigurationHistoryChangeLog> historyjiraIterationIssuetypeKPI138;
	private List<ConfigurationHistoryChangeLog> historyjiraIterationIssuetypeKPI131;
	private List<ConfigurationHistoryChangeLog> historyjiraIterationIssuetypeKPI128;
	private List<ConfigurationHistoryChangeLog> historyjiraIterationIssuetypeKPI134;
	private List<ConfigurationHistoryChangeLog> historyjiraIterationIssuetypeKPI145;
	private List<ConfigurationHistoryChangeLog> historyjiraIterationIssuetypeKpi72;
	private List<ConfigurationHistoryChangeLog> historyjiraIterationIssuetypeKPI119;
	private List<ConfigurationHistoryChangeLog> historyjiraIterationIssuetypeKpi5;
	private List<ConfigurationHistoryChangeLog> historyjiraIterationIssuetypeKPI75;
	private List<ConfigurationHistoryChangeLog> historyjiraIterationIssuetypeKPI123;
	private List<ConfigurationHistoryChangeLog> historyjiraIterationIssuetypeKPI125;
	private List<ConfigurationHistoryChangeLog> historyjiraIterationIssuetypeKPI120;
	private List<ConfigurationHistoryChangeLog> historyjiraIterationIssuetypeKPI124;
	private List<ConfigurationHistoryChangeLog> historyjiraIterationIssuetypeKPI39;

	private List<ConfigurationHistoryChangeLog> historyuploadData;
	private List<ConfigurationHistoryChangeLog> historyuploadDataKPI42;
	private List<ConfigurationHistoryChangeLog> historyuploadDataKPI16;
	private LocalDateTime historycreatedDate;
	private List<ConfigurationHistoryChangeLog> historyjiraDefectRejectionStatusKPI155;
	private List<ConfigurationHistoryChangeLog> historyjiraDodKPI155;
	private List<ConfigurationHistoryChangeLog> historyjiraLiveStatusKPI155;

	private List<ConfigurationHistoryChangeLog> historynotificationEnabler;
	private List<ConfigurationHistoryChangeLog> historyexcludeUnlinkedDefects;

	private List<ConfigurationHistoryChangeLog> historyjiraIssueEpicTypeKPI153;

	// DTS-26150 start
	// Testing Phase Defect Mapping
	private List<ConfigurationHistoryChangeLog> historytestingPhaseDefectCustomField;
	private List<ConfigurationHistoryChangeLog> historytestingPhaseDefectsIdentifier;
	private List<ConfigurationHistoryChangeLog> historytestingPhaseDefectValue;
	private List<ConfigurationHistoryChangeLog> historytestingPhaseDefectComponentValue;
	private List<ConfigurationHistoryChangeLog> historyjiraDodKPI163;
	// DTS-26150 end

	private List<ConfigurationHistoryChangeLog> historyjiraIssueTypeNamesKPI161;
	private List<ConfigurationHistoryChangeLog> historyjiraIssueTypeNamesKPI146;
	private List<ConfigurationHistoryChangeLog> historyjiraIssueTypeNamesKPI148;
	private List<ConfigurationHistoryChangeLog> historyjiraIssueTypeNamesKPI151;
	private List<ConfigurationHistoryChangeLog> historyjiraIssueTypeNamesKPI152;

	private List<ConfigurationHistoryChangeLog> historyjiraDodKPI156;
	private List<ConfigurationHistoryChangeLog> historyjiraIssueTypeKPI156;
	private List<ConfigurationHistoryChangeLog> historyjiraLabelsKPI14;
	private List<ConfigurationHistoryChangeLog> historyjiraLabelsKPI82;
	private List<ConfigurationHistoryChangeLog> historyjiraLabelsQAKPI111;
	private List<ConfigurationHistoryChangeLog> historyjiraLabelsKPI133;
	private List<ConfigurationHistoryChangeLog> historyjiraIssueWaitStateKPI170;
	private List<ConfigurationHistoryChangeLog> historyjiraIssueClosedStateKPI170;

	private List<ConfigurationHistoryChangeLog> historyleadTimeConfigRepoTool;

	private List<ConfigurationHistoryChangeLog> historytoBranchForMRKPI156;
	private List<ConfigurationHistoryChangeLog> historystartDateCountKPI150;
	private List<ConfigurationHistoryChangeLog> historyjiraDevDoneStatusKPI150;
	private List<ConfigurationHistoryChangeLog> historypopulateByDevDoneKPI150;
	private List<ConfigurationHistoryChangeLog> historyreleaseListKPI150;

	// threshold field
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI14;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI82;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI111;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI35;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI34;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI37;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI28;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI36;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI16;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI17;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI38;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI27;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI72;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI84;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI11;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI157;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI158;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI160;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI164;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI3;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI126;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI42;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI168;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI70;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI40;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI5;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI39;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI46;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI8;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI172;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI73;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI113;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI149;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI153;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI162;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI116;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI156;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI118;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI127;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI170;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI139;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI166;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI173;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI180;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI181;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI182;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI185;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI186;

	/** kanban kpis threshold fields history starts * */
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI51;

	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI55;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI54;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI50;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI48;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI997;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI63;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI62;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI64;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI67;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI71;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI49;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI58;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI66;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI65;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI53;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI74;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI114;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI159;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI184;
	private List<ConfigurationHistoryChangeLog> historythresholdValueKPI183;

	/** kanban kpi threshold fields history ends * */

	// Production Incident Mapping
	private List<ConfigurationHistoryChangeLog> historyjiraProductionIncidentIdentification;

	private List<ConfigurationHistoryChangeLog> historyjiraProdIncidentRaisedByCustomField;
	private List<ConfigurationHistoryChangeLog> historyjiraProdIncidentRaisedByValue;

	private List<ConfigurationHistoryChangeLog> historyjiraStoryIdentificationKPI166;
	private List<ConfigurationHistoryChangeLog> historyjiraDodKPI166;
	private List<ConfigurationHistoryChangeLog> historystoryFirstStatusKPI154;
	private List<ConfigurationHistoryChangeLog> historyjiraStatusForInProgressKPI154;
	private List<ConfigurationHistoryChangeLog> historyjiraDevDoneStatusKPI154;
	private List<ConfigurationHistoryChangeLog> historyjiraQADoneStatusKPI154;
	private List<ConfigurationHistoryChangeLog> historyjiraOnHoldStatusKPI154;
	private List<ConfigurationHistoryChangeLog> historyjiraIterationCompletionStatusKPI154;
	private List<ConfigurationHistoryChangeLog> historyjiraSubTaskIdentification;
	private List<ConfigurationHistoryChangeLog> historyjiraStatusStartDevelopmentKPI154;

	private List<ConfigurationHistoryChangeLog> historyjiraLabelsKPI135;

	private List<ConfigurationHistoryChangeLog> historyjiraStatusForInProgressKPI161;
	private List<ConfigurationHistoryChangeLog> historyjiraStatusForRefinedKPI161;
	private List<ConfigurationHistoryChangeLog> historyjiraStatusForNotRefinedKPI161;

	private List<ConfigurationHistoryChangeLog> historyjiraIssueTypeKPI171;
	private List<ConfigurationHistoryChangeLog> historyjiraDodKPI171;
	private List<ConfigurationHistoryChangeLog> historyjiraDorKPI171;
	private List<ConfigurationHistoryChangeLog> historyjiraLiveStatusKPI171;
	private List<ConfigurationHistoryChangeLog> historystoryFirstStatusKPI171;

	private List<ConfigurationHistoryChangeLog> historyjiraIssueTypeExcludeKPI124;
	private List<ConfigurationHistoryChangeLog> historyjiraIssueTypeExcludeKPI75;
	private List<ConfigurationHistoryChangeLog> historyjiraStatusToConsiderKPI127;
	private List<ConfigurationHistoryChangeLog> historyissueTypesToConsiderKpi113;
	private List<ConfigurationHistoryChangeLog> historyclosedIssueStatusToConsiderKpi113;

	private List<ConfigurationHistoryChangeLog> historyincludeActiveSprintInBacklogKPI;
}
