/*
 *
 *   Copyright 2014 CapitalOne, LLC.
 *   Further development Copyright 2022 Sapient Corporation.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.publicissapient.kpidashboard.common.model.application.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterConfig;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.Test;

public class FieldMappingDTOTest {
	/**
	 * Methods under test:
	 *
	 * <ul>
	 *   <li>default or parameterless constructor of {@link FieldMappingDTO}
	 *   <li>{@link FieldMappingDTO#setAdditionalFilterConfig(List)}
	 *   <li>{@link FieldMappingDTO#setAtmQueryEndpoint(String)}
	 *   <li>{@link FieldMappingDTO#setAtmSubprojectField(String)}
	 *   <li>{@link FieldMappingDTO#setBasicProjectConfigId(ObjectId)}
	 *   <li>{@link FieldMappingDTO#setDefectPriority(List)}
	 *   <li>{@link FieldMappingDTO#setDefectPriorityKPI133(List)}
	 *   <li>{@link FieldMappingDTO#setDefectPriorityKPI135(List)}
	 *   <li>{@link FieldMappingDTO#setDefectPriorityKPI14(List)}
	 *   <li>{@link FieldMappingDTO#setDefectPriorityKPI82(List)}
	 *   <li>{@link FieldMappingDTO#setDefectPriorityQAKPI111(List)}
	 *   <li>{@link FieldMappingDTO#setEpicAchievedValue(String)}
	 *   <li>{@link FieldMappingDTO#setEpicCostOfDelay(String)}
	 *   <li>{@link FieldMappingDTO#setEpicJobSize(String)}
	 *   <li>{@link FieldMappingDTO#setEpicLink(String)}
	 *   <li>{@link FieldMappingDTO#setEpicName(String)}
	 *   <li>{@link FieldMappingDTO#setEpicPlannedValue(String)}
	 *   <li>{@link FieldMappingDTO#setEpicRiskReduction(String)}
	 *   <li>{@link FieldMappingDTO#setEpicTimeCriticality(String)}
	 *   <li>{@link FieldMappingDTO#setEpicUserBusinessValue(String)}
	 *   <li>{@link FieldMappingDTO#setEpicWsjf(String)}
	 *   <li>{@link FieldMappingDTO#setEstimationCriteria(String)}
	 *   <li>{@link FieldMappingDTO#setExcludeRCAFromFTPR(List)}
	 *   <li>{@link FieldMappingDTO#setExcludeStatusKpi129(List)}
	 *   <li>{@link FieldMappingDTO#setIncludeRCAForKPI133(List)}
	 *   <li>{@link FieldMappingDTO#setIncludeRCAForKPI135(List)}
	 *   <li>{@link FieldMappingDTO#setIncludeRCAForKPI14(List)}
	 *   <li>{@link FieldMappingDTO#setIncludeRCAForKPI82(List)}
	 *   <li>{@link FieldMappingDTO#setIncludeRCAForQAKPI111(List)}
	 *   <li>{@link FieldMappingDTO#setIssueStatusExcluMissingWork(List)}
	 *   <li>{@link FieldMappingDTO#setIssueStatusExcluMissingWorkKPI124(List)}
	 *   <li>{@link FieldMappingDTO#setIssueStatusToBeExcludedFromMissingWorklogs(List)}
	 *   <li>{@link FieldMappingDTO#setJiraAcceptedInRefinement(List)}
	 *   <li>{@link FieldMappingDTO#setJiraAcceptedInRefinementKPI139(List)}
	 *   <li>{@link FieldMappingDTO#setJiraAtmProjectId(String)}
	 *   <li>{@link FieldMappingDTO#setJiraAtmProjectKey(String)}
	 *   <li>{@link FieldMappingDTO#setJiraBlockedStatus(List)}
	 *   <li>{@link FieldMappingDTO#setJiraBlockedStatusKPI131(List)}
	 *   <li>{@link FieldMappingDTO#setJiraBugRaisedByCustomField(String)}
	 *   <li>{@link FieldMappingDTO#setJiraBugRaisedByIdentification(String)}
	 *   <li>{@link FieldMappingDTO#setJiraBugRaisedByQACustomField(String)}
	 *   <li>{@link FieldMappingDTO#setJiraBugRaisedByQAIdentification(String)}
	 *   <li>{@link FieldMappingDTO#setJiraBugRaisedByQAValue(List)}
	 *   <li>{@link FieldMappingDTO#setJiraBugRaisedByValue(List)}
	 *   <li>{@link FieldMappingDTO#setJiraCommitmentReliabilityIssueType(List)}
	 *   <li>{@link FieldMappingDTO#setJiraDefectClosedStatus(List)}
	 *   <li>{@link FieldMappingDTO#setJiraDefectClosedStatusKPI137(List)}
	 *   <li>{@link FieldMappingDTO#setJiraDefectCountlIssueType(List)}
	 *   <li>{@link FieldMappingDTO#setJiraDefectCountlIssueTypeKPI28(List)}
	 *   <li>{@link FieldMappingDTO#setJiraDefectCountlIssueTypeKPI36(List)}
	 *   <li>{@link FieldMappingDTO#setJiraDefectCreatedStatus(String)}
	 *   <li>{@link FieldMappingDTO#setJiraDefectCreatedStatusKPI14(String)}
	 *   <li>{@link FieldMappingDTO#setJiraDefectDroppedStatus(List)}
	 *   <li>{@link FieldMappingDTO#setJiraDefectDroppedStatusKPI127(List)}
	 *   <li>{@link FieldMappingDTO#setJiraDefectInjectionIssueType(List)}
	 *   <li>{@link FieldMappingDTO#setJiraDefectInjectionIssueTypeKPI14(List)}
	 *   <li>{@link FieldMappingDTO#setJiraDefectRejectionStatus(String)}
	 *   <li>{@link FieldMappingDTO#setJiraDefectRejectionStatusAVR(String)}
	 *   <li>{@link FieldMappingDTO#setJiraDefectRejectionStatusKPI133(String)}
	 *   <li>{@link FieldMappingDTO#setJiraDefectRejectionStatusKPI135(String)}
	 *   <li>{@link FieldMappingDTO#setJiraDefectRejectionStatusKPI14(String)}
	 *   <li>{@link FieldMappingDTO#setJiraDefectRejectionStatusKPI151(String)}
	 *   <li>{@link FieldMappingDTO#setJiraDefectRejectionStatusKPI152(String)}
	 *   <li>{@link FieldMappingDTO#setJiraDefectRejectionStatusKPI155(String)}
	 *   <li>{@link FieldMappingDTO#setJiraDefectRejectionStatusKPI28(String)}
	 *   <li>{@link FieldMappingDTO#setJiraDefectRejectionStatusKPI35(String)}
	 *   <li>{@link FieldMappingDTO#setJiraDefectRejectionStatusKPI37(String)}
	 *   <li>{@link FieldMappingDTO#setJiraDefectRejectionStatusKPI82(String)}
	 *   <li>{@link FieldMappingDTO#setJiraDefectRejectionStatusQAKPI111(String)}
	 *   <li>{@link FieldMappingDTO#setJiraDefectRejectionStatusRCAKPI36(String)}
	 *   <li>{@link FieldMappingDTO#setJiraDefectRejectionlIssueType(List)}
	 *   <li>{@link FieldMappingDTO#setJiraDefectRemovalIssueType(List)}
	 *   <li>{@link FieldMappingDTO#setJiraDefectRemovalStatus(List)}
	 *   <li>{@link FieldMappingDTO#setJiraDefectRemovalStatusKPI34(List)}
	 *   <li>{@link FieldMappingDTO#setJiraDefectSeepageIssueType(List)}
	 *   <li>{@link FieldMappingDTO#setJiraDevDoneStatus(List)}
	 *   <li>{@link FieldMappingDTO#setJiraDevDoneStatusKPI119(List)}
	 *   <li>{@link FieldMappingDTO#setJiraDevDoneStatusKPI128(List)}
	 *   <li>{@link FieldMappingDTO#setJiraDevDoneStatusKPI145(List)}
	 *   <li>{@link FieldMappingDTO#setJiraDevDoneStatusKPI150(List)}
	 *   <li>{@link FieldMappingDTO#setJiraDevDoneStatusKPI154(List)}
	 *   <li>{@link FieldMappingDTO#setJiraDevDueDateCustomField(String)}
	 *   <li>{@link FieldMappingDTO#setJiraDevDueDateField(String)}
	 *   <li>{@link FieldMappingDTO#setJiraDod(List)}
	 *   <li>{@link FieldMappingDTO#setJiraDodKPI127(List)}
	 *   <li>{@link FieldMappingDTO#setJiraDodKPI14(List)}
	 *   <li>{@link FieldMappingDTO#setJiraDodKPI151(List)}
	 *   <li>{@link FieldMappingDTO#setJiraDodKPI152(List)}
	 *   <li>{@link FieldMappingDTO#setJiraDodKPI155(List)}
	 *   <li>{@link FieldMappingDTO#setJiraDodKPI156(List)}
	 *   <li>{@link FieldMappingDTO#setJiraDodKPI163(List)}
	 *   <li>{@link FieldMappingDTO#setJiraDodKPI166(List)}
	 *   <li>{@link FieldMappingDTO#setJiraDodKPI171(List)}
	 *   <li>{@link FieldMappingDTO#setJiraDodKPI37(List)}
	 *   <li>{@link FieldMappingDTO#setJiraDodQAKPI111(List)}
	 *   <li>{@link FieldMappingDTO#setJiraDor(String)}
	 *   <li>{@link FieldMappingDTO#setJiraDorKPI171(List)}
	 *   <li>{@link FieldMappingDTO#setJiraDorToLiveIssueType(List)}
	 *   <li>{@link FieldMappingDTO#setJiraDueDateCustomField(String)}
	 *   <li>{@link FieldMappingDTO#setJiraDueDateField(String)}
	 *   <li>{@link FieldMappingDTO#setJiraFTPRStoryIdentification(List)}
	 *   <li>{@link FieldMappingDTO#setJiraFtprRejectStatus(List)}
	 *   <li>{@link FieldMappingDTO#setJiraFtprRejectStatusKPI135(List)}
	 *   <li>{@link FieldMappingDTO#setJiraFtprRejectStatusKPI82(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIncludeBlockedStatus(String)}
	 *   <li>{@link FieldMappingDTO#setJiraIncludeBlockedStatusKPI131(String)}
	 *   <li>{@link FieldMappingDTO#setJiraIntakeToDorIssueType(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIssueClosedStateKPI170(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIssueDeliverdStatus(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIssueDeliverdStatusAVR(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIssueDeliverdStatusKPI126(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIssueDeliverdStatusKPI138(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIssueDeliverdStatusKPI82(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIssueEpicType(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIssueEpicTypeKPI153(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIssueTypeKPI156(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIssueTypeKPI171(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIssueTypeKPI35(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIssueTypeKPI3(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIssueTypeNames(String[])}
	 *   <li>{@link FieldMappingDTO#setJiraIssueTypeNamesAVR(String[])}
	 *   <li>{@link FieldMappingDTO#setJiraIssueTypeNamesKPI146(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIssueTypeNamesKPI148(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIssueTypeNamesKPI151(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIssueTypeNamesKPI152(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIssueTypeNamesKPI161(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIssueWaitStateKPI170(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIterationCompletionStatusCustomField(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIterationCompletionStatusKPI119(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIterationCompletionStatusKPI120(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIterationCompletionStatusKPI122(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIterationCompletionStatusKPI123(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIterationCompletionStatusKPI124(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIterationCompletionStatusKPI125(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIterationCompletionStatusKPI128(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIterationCompletionStatusKPI131(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIterationCompletionStatusKPI132(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIterationCompletionStatusKPI133(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIterationCompletionStatusKPI134(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIterationCompletionStatusKPI135(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIterationCompletionStatusKPI136(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIterationCompletionStatusKPI138(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIterationCompletionStatusKPI140(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIterationCompletionStatusKPI145(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIterationCompletionStatusKPI154(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIterationCompletionStatusKPI75(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIterationCompletionStatusKpi39(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIterationCompletionStatusKpi5(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIterationCompletionStatusKpi72(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIterationCompletionTypeCustomField(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIterationIssuetypeKPI119(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIterationIssuetypeKPI120(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIterationIssuetypeKPI122(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIterationIssuetypeKPI123(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIterationIssuetypeKPI124(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIterationIssuetypeKPI125(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIterationIssuetypeKPI128(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIterationIssuetypeKPI131(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIterationIssuetypeKPI134(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIterationIssuetypeKPI138(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIterationIssuetypeKPI145(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIterationIssuetypeKPI39(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIterationIssuetypeKPI75(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIterationIssuetypeKpi5(List)}
	 *   <li>{@link FieldMappingDTO#setJiraIterationIssuetypeKpi72(List)}
	 *   <li>{@link FieldMappingDTO#setJiraItrQSIssueTypeKPI133(List)}
	 *   <li>{@link FieldMappingDTO#setJiraKPI135StoryIdentification(List)}
	 *   <li>{@link FieldMappingDTO#setJiraKPI82StoryIdentification(List)}
	 *   <li>{@link FieldMappingDTO#setJiraLabelsKPI135(List)}
	 *   <li>{@link FieldMappingDTO#setJiraLabelsKPI14(List)}
	 *   <li>{@link FieldMappingDTO#setJiraLabelsKPI82(List)}
	 *   <li>{@link FieldMappingDTO#setJiraLiveStatus(String)}
	 *   <li>{@link FieldMappingDTO#setJiraLiveStatusKPI127(String)}
	 *   <li>{@link FieldMappingDTO#setJiraLiveStatusKPI151(String)}
	 *   <li>{@link FieldMappingDTO#setJiraLiveStatusKPI152(String)}
	 *   <li>{@link FieldMappingDTO#setJiraLiveStatusKPI155(String)}
	 *   <li>{@link FieldMappingDTO#setJiraLiveStatusKPI171(List)}
	 *   <li>{@link FieldMappingDTO#setJiraLiveStatusKPI3(List)}
	 *   <li>{@link FieldMappingDTO#setJiraLiveStatusLTK(String)}
	 *   <li>{@link FieldMappingDTO#setJiraLiveStatusNOPK(String)}
	 *   <li>{@link FieldMappingDTO#setJiraLiveStatusNORK(String)}
	 *   <li>{@link FieldMappingDTO#setJiraLiveStatusNOSK(String)}
	 *   <li>{@link FieldMappingDTO#setJiraLiveStatusOTA(String)}
	 *   <li>{@link FieldMappingDTO#setJiraOnHoldStatus(List)}
	 *   <li>{@link FieldMappingDTO#setJiraOnHoldStatusKPI154(List)}
	 *   <li>{@link FieldMappingDTO#setJiraProdIncidentRaisedByCustomField(String)}
	 *   <li>{@link FieldMappingDTO#setJiraProdIncidentRaisedByValue(List)}
	 *   <li>{@link FieldMappingDTO#setJiraProductionIncidentIdentification(String)}
	 *   <li>{@link FieldMappingDTO#setJiraProductiveStatus(List)}
	 *   <li>{@link FieldMappingDTO#setJiraQADefectDensityIssueType(List)}
	 *   <li>{@link FieldMappingDTO#setJiraQADoneStatusKPI154(List)}
	 *   <li>{@link FieldMappingDTO#setJiraQAKPI111IssueType(List)}
	 *   <li>{@link FieldMappingDTO#setJiraReadyForRefinement(List)}
	 *   <li>{@link FieldMappingDTO#setJiraReadyForRefinementKPI139(List)}
	 *   <li>{@link FieldMappingDTO#setJiraRejectedInRefinement(List)}
	 *   <li>{@link FieldMappingDTO#setJiraRejectedInRefinementKPI139(List)}
	 *   <li>{@link FieldMappingDTO#setJiraSprintCapacityIssueType(List)}
	 *   <li>{@link FieldMappingDTO#setJiraSprintCapacityIssueTypeKpi46(List)}
	 *   <li>{@link FieldMappingDTO#setJiraSprintVelocityIssueType(List)}
	 *   <li>{@link FieldMappingDTO#setJiraSprintVelocityIssueTypeKPI138(List)}
	 *   <li>{@link FieldMappingDTO#setJiraStatusForDevelopment(List)}
	 *   <li>{@link FieldMappingDTO#setJiraStatusForDevelopmentAVR(List)}
	 *   <li>{@link FieldMappingDTO#setJiraStatusForDevelopmentKPI135(List)}
	 *   <li>{@link FieldMappingDTO#setJiraStatusForDevelopmentKPI82(List)}
	 *   <li>{@link FieldMappingDTO#setJiraStatusForInProgress(List)}
	 *   <li>{@link FieldMappingDTO#setJiraStatusForInProgressKPI119(List)}
	 *   <li>{@link FieldMappingDTO#setJiraStatusForInProgressKPI122(List)}
	 *   <li>{@link FieldMappingDTO#setJiraStatusForInProgressKPI123(List)}
	 *   <li>{@link FieldMappingDTO#setJiraStatusForInProgressKPI125(List)}
	 *   <li>{@link FieldMappingDTO#setJiraStatusForInProgressKPI128(List)}
	 *   <li>{@link FieldMappingDTO#setJiraStatusForInProgressKPI145(List)}
	 *   <li>{@link FieldMappingDTO#setJiraStatusForInProgressKPI148(List)}
	 *   <li>{@link FieldMappingDTO#setJiraStatusForInProgressKPI154(List)}
	 *   <li>{@link FieldMappingDTO#setJiraStatusForInProgressKPI161(List)}
	 *   <li>{@link FieldMappingDTO#setJiraStatusForNotRefinedKPI161(List)}
	 *   <li>{@link FieldMappingDTO#setJiraStatusForQa(List)}
	 *   <li>{@link FieldMappingDTO#setJiraStatusForQaKPI135(List)}
	 *   <li>{@link FieldMappingDTO#setJiraStatusForQaKPI148(List)}
	 *   <li>{@link FieldMappingDTO#setJiraStatusForQaKPI82(List)}
	 *   <li>{@link FieldMappingDTO#setJiraStatusForRefinedKPI161(List)}
	 *   <li>{@link FieldMappingDTO#setJiraStatusMappingCustomField(String)}
	 *   <li>{@link FieldMappingDTO#setJiraStatusStartDevelopmentKPI154(List)}
	 *   <li>{@link FieldMappingDTO#setJiraStoryIdentification(List)}
	 *   <li>{@link FieldMappingDTO#setJiraStoryIdentificationKPI129(List)}
	 *   <li>{@link FieldMappingDTO#setJiraStoryIdentificationKPI164(List)}
	 *   <li>{@link FieldMappingDTO#setJiraStoryIdentificationKPI166(List)}
	 *   <li>{@link FieldMappingDTO#setJiraStoryIdentificationKpi40(List)}
	 *   <li>{@link FieldMappingDTO#setJiraStoryPointsCustomField(String)}
	 *   <li>{@link FieldMappingDTO#setJiraSubTaskDefectType(List)}
	 *   <li>{@link FieldMappingDTO#setJiraSubTaskIdentification(List)}
	 *   <li>{@link FieldMappingDTO#setJiraTechDebtCustomField(String)}
	 *   <li>{@link FieldMappingDTO#setJiraTechDebtIdentification(String)}
	 *   <li>{@link FieldMappingDTO#setJiraTechDebtIssueType(List)}
	 *   <li>{@link FieldMappingDTO#setJiraTechDebtValue(List)}
	 *   <li>{@link FieldMappingDTO#setJiraTestAutomationIssueType(List)}
	 *   <li>{@link FieldMappingDTO#setJiraTicketClosedStatus(List)}
	 *   <li>{@link FieldMappingDTO#setJiraTicketRejectedStatus(List)}
	 *   <li>{@link FieldMappingDTO#setJiraTicketResolvedStatus(List)}
	 *   <li>{@link FieldMappingDTO#setJiraTicketTriagedStatus(List)}
	 *   <li>{@link FieldMappingDTO#setJiraTicketVelocityIssueType(List)}
	 *   <li>{@link FieldMappingDTO#setJiraTicketWipStatus(List)}
	 *   <li>{@link FieldMappingDTO#setJiraWaitStatus(List)}
	 *   <li>{@link FieldMappingDTO#setJiraWaitStatusKPI131(List)}
	 *   <li>{@link FieldMappingDTO#setJiradefecttype(List)}
	 *   <li>{@link FieldMappingDTO#setKanbanCycleTimeIssueType(List)}
	 *   <li>{@link FieldMappingDTO#setKanbanJiraTechDebtIssueType(List)}
	 *   <li>{@link FieldMappingDTO#setKanbanRCACountIssueType(List)}
	 *   <li>{@link FieldMappingDTO#setLeadTimeConfigRepoTool(String)}
	 *   <li>{@link FieldMappingDTO#setLinkDefectToStoryField(String[])}
	 *   <li>{@link FieldMappingDTO#setNotificationEnabler(boolean)}
	 *   <li>{@link FieldMappingDTO#setPickNewATMJIRADetails(Boolean)}
	 *   <li>{@link FieldMappingDTO#setPopulateByDevDoneKPI150(boolean)}
	 *   <li>{@link FieldMappingDTO#setProductionDefectComponentValue(String)}
	 *   <li>{@link FieldMappingDTO#setProductionDefectCustomField(String)}
	 *   <li>{@link FieldMappingDTO#setProductionDefectIdentifier(String)}
	 *   <li>{@link FieldMappingDTO#setProductionDefectValue(List)}
	 *   <li>{@link FieldMappingDTO#setProjectId(String)}
	 *   <li>{@link FieldMappingDTO#setProjectToolConfigId(ObjectId)}
	 *   <li>{@link FieldMappingDTO#setReadyForDevelopmentStatus(String)}
	 *   <li>{@link FieldMappingDTO#setReadyForDevelopmentStatusKPI138(List)}
	 *   <li>{@link FieldMappingDTO#setResolutionTypeForRejection(List)}
	 *   <li>{@link FieldMappingDTO#setResolutionTypeForRejectionAVR(List)}
	 *   <li>{@link FieldMappingDTO#setResolutionTypeForRejectionKPI133(List)}
	 *   <li>{@link FieldMappingDTO#setResolutionTypeForRejectionKPI135(List)}
	 *   <li>{@link FieldMappingDTO#setResolutionTypeForRejectionKPI14(List)}
	 *   <li>{@link FieldMappingDTO#setResolutionTypeForRejectionKPI28(List)}
	 *   <li>{@link FieldMappingDTO#setResolutionTypeForRejectionKPI35(List)}
	 *   <li>{@link FieldMappingDTO#setResolutionTypeForRejectionKPI37(List)}
	 *   <li>{@link FieldMappingDTO#setResolutionTypeForRejectionKPI82(List)}
	 *   <li>{@link FieldMappingDTO#setResolutionTypeForRejectionQAKPI111(List)}
	 *   <li>{@link FieldMappingDTO#setResolutionTypeForRejectionRCAKPI36(List)}
	 *   <li>{@link FieldMappingDTO#setRootCause(String)}
	 *   <li>{@link FieldMappingDTO#setRootCauseValue(List)}
	 *   <li>{@link FieldMappingDTO#setSprintName(String)}
	 *   <li>{@link FieldMappingDTO#setSquadIdentMultiValue(List)}
	 *   <li>{@link FieldMappingDTO#setSquadIdentSingleValue(String)}
	 *   <li>{@link FieldMappingDTO#setSquadIdentifier(String)}
	 *   <li>{@link FieldMappingDTO#setStartDateCountKPI150(Integer)}
	 *   <li>{@link FieldMappingDTO#setStoryFirstStatus(String)}
	 *   <li>{@link FieldMappingDTO#setStoryFirstStatusKPI148(String)}
	 *   <li>{@link FieldMappingDTO#setStoryFirstStatusKPI154(List)}
	 *   <li>{@link FieldMappingDTO#setStoryFirstStatusKPI171(String)}
	 *   <li>{@link FieldMappingDTO#setStoryPointToHourMapping(Double)}
	 *   <li>{@link FieldMappingDTO#setTestingPhaseDefectComponentValue(String)}
	 *   <li>{@link FieldMappingDTO#setTestingPhaseDefectCustomField(String)}
	 *   <li>{@link FieldMappingDTO#setTestingPhaseDefectValue(List)}
	 *   <li>{@link FieldMappingDTO#setTestingPhaseDefectsIdentifier(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI111(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI113(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI116(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI118(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI11(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI126(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI127(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI139(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI149(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI14(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI153(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI156(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI157(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI158(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI159(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI160(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI162(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI164(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI166(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI168(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI16(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI170(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI17(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI27(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI28(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI34(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI35(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI36(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI37(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI38(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI39(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI3(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI40(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI42(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI46(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI5(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI62(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI64(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI65(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI67(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI70(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI72(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI73(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI82(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI84(String)}
	 *   <li>{@link FieldMappingDTO#setThresholdValueKPI8(String)}
	 *   <li>{@link FieldMappingDTO#setTicketCountIssueType(List)}
	 *   <li>{@link FieldMappingDTO#setTicketDeliverdStatus(List)}
	 *   <li>{@link FieldMappingDTO#setTicketReopenStatus(List)}
	 *   <li>{@link FieldMappingDTO#setToBranchForMRKPI156(String)}
	 *   <li>{@link FieldMappingDTO#setUploadData(boolean)}
	 *   <li>{@link FieldMappingDTO#setUploadDataKPI16(boolean)}
	 *   <li>{@link FieldMappingDTO#setUploadDataKPI42(boolean)}
	 *   <li>{@link FieldMappingDTO#setWorkingHoursDayCPT(Double)}
	 *   <li>{@link FieldMappingDTO#getAdditionalFilterConfig()}
	 *   <li>{@link FieldMappingDTO#getAtmQueryEndpoint()}
	 *   <li>{@link FieldMappingDTO#getAtmSubprojectField()}
	 *   <li>{@link FieldMappingDTO#getBasicProjectConfigId()}
	 *   <li>{@link FieldMappingDTO#getDefectPriority()}
	 *   <li>{@link FieldMappingDTO#getDefectPriorityKPI133()}
	 *   <li>{@link FieldMappingDTO#getDefectPriorityKPI135()}
	 *   <li>{@link FieldMappingDTO#getDefectPriorityKPI14()}
	 *   <li>{@link FieldMappingDTO#getDefectPriorityKPI82()}
	 *   <li>{@link FieldMappingDTO#getDefectPriorityQAKPI111()}
	 *   <li>{@link FieldMappingDTO#getEpicAchievedValue()}
	 *   <li>{@link FieldMappingDTO#getEpicCostOfDelay()}
	 *   <li>{@link FieldMappingDTO#getEpicJobSize()}
	 *   <li>{@link FieldMappingDTO#getEpicLink()}
	 *   <li>{@link FieldMappingDTO#getEpicName()}
	 *   <li>{@link FieldMappingDTO#getEpicPlannedValue()}
	 *   <li>{@link FieldMappingDTO#getEpicRiskReduction()}
	 *   <li>{@link FieldMappingDTO#getEpicTimeCriticality()}
	 *   <li>{@link FieldMappingDTO#getEpicUserBusinessValue()}
	 *   <li>{@link FieldMappingDTO#getEpicWsjf()}
	 *   <li>{@link FieldMappingDTO#getEstimationCriteria()}
	 *   <li>{@link FieldMappingDTO#getExcludeRCAFromFTPR()}
	 *   <li>{@link FieldMappingDTO#getExcludeStatusKpi129()}
	 *   <li>{@link FieldMappingDTO#getIncludeRCAForKPI133()}
	 *   <li>{@link FieldMappingDTO#getIncludeRCAForKPI135()}
	 *   <li>{@link FieldMappingDTO#getIncludeRCAForKPI14()}
	 *   <li>{@link FieldMappingDTO#getIncludeRCAForKPI82()}
	 *   <li>{@link FieldMappingDTO#getIncludeRCAForQAKPI111()}
	 *   <li>{@link FieldMappingDTO#getIssueStatusExcluMissingWork()}
	 *   <li>{@link FieldMappingDTO#getIssueStatusExcluMissingWorkKPI124()}
	 *   <li>{@link FieldMappingDTO#getIssueStatusToBeExcludedFromMissingWorklogs()}
	 *   <li>{@link FieldMappingDTO#getJiraAcceptedInRefinement()}
	 *   <li>{@link FieldMappingDTO#getJiraAcceptedInRefinementKPI139()}
	 *   <li>{@link FieldMappingDTO#getJiraAtmProjectId()}
	 *   <li>{@link FieldMappingDTO#getJiraAtmProjectKey()}
	 *   <li>{@link FieldMappingDTO#getJiraBlockedStatus()}
	 *   <li>{@link FieldMappingDTO#getJiraBlockedStatusKPI131()}
	 *   <li>{@link FieldMappingDTO#getJiraBugRaisedByCustomField()}
	 *   <li>{@link FieldMappingDTO#getJiraBugRaisedByIdentification()}
	 *   <li>{@link FieldMappingDTO#getJiraBugRaisedByQACustomField()}
	 *   <li>{@link FieldMappingDTO#getJiraBugRaisedByQAIdentification()}
	 *   <li>{@link FieldMappingDTO#getJiraBugRaisedByQAValue()}
	 *   <li>{@link FieldMappingDTO#getJiraBugRaisedByValue()}
	 *   <li>{@link FieldMappingDTO#getJiraCommitmentReliabilityIssueType()}
	 *   <li>{@link FieldMappingDTO#getJiraDefectClosedStatus()}
	 *   <li>{@link FieldMappingDTO#getJiraDefectClosedStatusKPI137()}
	 *   <li>{@link FieldMappingDTO#getJiraDefectCountlIssueType()}
	 *   <li>{@link FieldMappingDTO#getJiraDefectCountlIssueTypeKPI28()}
	 * </ul>
	 */
	@Test
	public void testConstructor2() {
		FieldMappingDTO actualFieldMappingDTO = new FieldMappingDTO();
		ArrayList<AdditionalFilterConfig> additionalFilterConfigList = new ArrayList<>();
		actualFieldMappingDTO.setAdditionalFilterConfig(additionalFilterConfigList);
		actualFieldMappingDTO.setAtmQueryEndpoint("https://config.us-east-2.amazonaws.com");
		actualFieldMappingDTO.setAtmSubprojectField("Atm Subproject Field");
		ObjectId getResult = ObjectId.get();
		actualFieldMappingDTO.setBasicProjectConfigId(getResult);
		ArrayList<String> stringList = new ArrayList<>();
		actualFieldMappingDTO.setDefectPriority(stringList);
		ArrayList<String> stringList1 = new ArrayList<>();
		actualFieldMappingDTO.setDefectPriorityKPI133(stringList1);
		ArrayList<String> stringList2 = new ArrayList<>();
		actualFieldMappingDTO.setDefectPriorityKPI135(stringList2);
		ArrayList<String> stringList3 = new ArrayList<>();
		actualFieldMappingDTO.setDefectPriorityKPI14(stringList3);
		ArrayList<String> stringList4 = new ArrayList<>();
		actualFieldMappingDTO.setDefectPriorityKPI82(stringList4);
		ArrayList<String> stringList5 = new ArrayList<>();
		actualFieldMappingDTO.setDefectPriorityQAKPI111(stringList5);
		actualFieldMappingDTO.setEpicAchievedValue("42");
		actualFieldMappingDTO.setEpicCostOfDelay("Epic Cost Of Delay");
		actualFieldMappingDTO.setEpicJobSize("Epic Job Size");
		actualFieldMappingDTO.setEpicLink("Epic Link");
		actualFieldMappingDTO.setEpicName("Epic Name");
		actualFieldMappingDTO.setEpicPlannedValue("42");
		actualFieldMappingDTO.setEpicRiskReduction("Epic Risk Reduction");
		actualFieldMappingDTO.setEpicTimeCriticality("Epic Time Criticality");
		actualFieldMappingDTO.setEpicUserBusinessValue("42");
		actualFieldMappingDTO.setEpicWsjf("Epic Wsjf");
		actualFieldMappingDTO.setEstimationCriteria("Estimation Criteria");
		ArrayList<String> stringList6 = new ArrayList<>();
		actualFieldMappingDTO.setExcludeRCAFromFTPR(stringList6);
		ArrayList<String> stringList7 = new ArrayList<>();
		actualFieldMappingDTO.setExcludeStatusKpi129(stringList7);
		ArrayList<String> stringList8 = new ArrayList<>();
		actualFieldMappingDTO.setIncludeRCAForKPI133(stringList8);
		ArrayList<String> stringList9 = new ArrayList<>();
		actualFieldMappingDTO.setIncludeRCAForKPI135(stringList9);
		ArrayList<String> stringList10 = new ArrayList<>();
		actualFieldMappingDTO.setIncludeRCAForKPI14(stringList10);
		ArrayList<String> stringList11 = new ArrayList<>();
		actualFieldMappingDTO.setIncludeRCAForKPI82(stringList11);
		ArrayList<String> stringList12 = new ArrayList<>();
		actualFieldMappingDTO.setIncludeRCAForQAKPI111(stringList12);
		ArrayList<String> stringList13 = new ArrayList<>();
		actualFieldMappingDTO.setIssueStatusExcluMissingWork(stringList13);
		ArrayList<String> stringList14 = new ArrayList<>();
		actualFieldMappingDTO.setIssueStatusExcluMissingWorkKPI124(stringList14);
		ArrayList<String> stringList15 = new ArrayList<>();
		actualFieldMappingDTO.setIssueStatusToBeExcludedFromMissingWorklogs(stringList15);
		ArrayList<String> stringList16 = new ArrayList<>();
		actualFieldMappingDTO.setJiraAcceptedInRefinement(stringList16);
		ArrayList<String> stringList17 = new ArrayList<>();
		actualFieldMappingDTO.setJiraAcceptedInRefinementKPI139(stringList17);
		actualFieldMappingDTO.setJiraAtmProjectId("myproject");
		actualFieldMappingDTO.setJiraAtmProjectKey("Jira Atm Project Key");
		ArrayList<String> stringList18 = new ArrayList<>();
		actualFieldMappingDTO.setJiraBlockedStatus(stringList18);
		ArrayList<String> stringList19 = new ArrayList<>();
		actualFieldMappingDTO.setJiraBlockedStatusKPI131(stringList19);
		actualFieldMappingDTO.setJiraBugRaisedByCustomField("Jira Bug Raised By Custom Field");
		actualFieldMappingDTO.setJiraBugRaisedByIdentification("Jira Bug Raised By Identification");
		actualFieldMappingDTO.setJiraBugRaisedByQACustomField("Jira Bug Raised By QACustom Field");
		actualFieldMappingDTO.setJiraBugRaisedByQAIdentification("Jira Bug Raised By QAIdentification");
		ArrayList<String> stringList20 = new ArrayList<>();
		actualFieldMappingDTO.setJiraBugRaisedByQAValue(stringList20);
		ArrayList<String> stringList21 = new ArrayList<>();
		actualFieldMappingDTO.setJiraBugRaisedByValue(stringList21);
		ArrayList<String> stringList22 = new ArrayList<>();
		actualFieldMappingDTO.setJiraCommitmentReliabilityIssueType(stringList22);
		ArrayList<String> stringList23 = new ArrayList<>();
		actualFieldMappingDTO.setJiraDefectClosedStatus(stringList23);
		ArrayList<String> stringList24 = new ArrayList<>();
		actualFieldMappingDTO.setJiraDefectClosedStatusKPI137(stringList24);
		ArrayList<String> stringList25 = new ArrayList<>();
		actualFieldMappingDTO.setJiraDefectCountlIssueType(stringList25);
		ArrayList<String> stringList26 = new ArrayList<>();
		actualFieldMappingDTO.setJiraDefectCountlIssueTypeKPI28(stringList26);
		actualFieldMappingDTO.setJiraDefectCountlIssueTypeKPI36(new ArrayList<>());
		actualFieldMappingDTO.setJiraDefectCreatedStatus("Jan 1, 2020 8:00am GMT+0100");
		actualFieldMappingDTO.setJiraDefectCreatedStatusKPI14("Jan 1, 2020 8:00am GMT+0100");
		actualFieldMappingDTO.setJiraDefectDroppedStatus(new ArrayList<>());
		actualFieldMappingDTO.setJiraDefectDroppedStatusKPI127(new ArrayList<>());
		actualFieldMappingDTO.setJiraDefectInjectionIssueType(new ArrayList<>());
		actualFieldMappingDTO.setJiraDefectInjectionIssueTypeKPI14(new ArrayList<>());
		actualFieldMappingDTO.setJiraDefectRejectionStatus("Jira Defect Rejection Status");
		actualFieldMappingDTO.setJiraDefectRejectionStatusAVR("Jira Defect Rejection Status AVR");
		actualFieldMappingDTO.setJiraDefectRejectionStatusKPI133("Jira Defect Rejection Status KPI133");
		actualFieldMappingDTO.setJiraDefectRejectionStatusKPI135("Jira Defect Rejection Status KPI135");
		actualFieldMappingDTO.setJiraDefectRejectionStatusKPI14("Jira Defect Rejection Status KPI14");
		actualFieldMappingDTO.setJiraDefectRejectionStatusKPI151("Jira Defect Rejection Status KPI151");
		actualFieldMappingDTO.setJiraDefectRejectionStatusKPI152("Jira Defect Rejection Status KPI152");
		actualFieldMappingDTO.setJiraDefectRejectionStatusKPI155("Jira Defect Rejection Status KPI155");
		actualFieldMappingDTO.setJiraDefectRejectionStatusKPI28("Jira Defect Rejection Status KPI28");
		actualFieldMappingDTO.setJiraDefectRejectionStatusKPI35("Jira Defect Rejection Status KPI35");
		actualFieldMappingDTO.setJiraDefectRejectionStatusKPI37("Jira Defect Rejection Status KPI37");
		actualFieldMappingDTO.setJiraDefectRejectionStatusKPI82("Jira Defect Rejection Status KPI82");
		actualFieldMappingDTO.setJiraDefectRejectionStatusQAKPI111("Jira Defect Rejection Status QAKPI111");
		actualFieldMappingDTO.setJiraDefectRejectionStatusRCAKPI36("Jira Defect Rejection Status RCAKPI36");
		actualFieldMappingDTO.setJiraDefectRejectionlIssueType(new ArrayList<>());
		actualFieldMappingDTO.setJiraDefectRemovalIssueType(new ArrayList<>());
		actualFieldMappingDTO.setJiraDefectRemovalStatus(new ArrayList<>());
		actualFieldMappingDTO.setJiraDefectRemovalStatusKPI34(new ArrayList<>());
		actualFieldMappingDTO.setJiraDefectSeepageIssueType(new ArrayList<>());
		actualFieldMappingDTO.setJiraDevDoneStatus(new ArrayList<>());
		actualFieldMappingDTO.setJiraDevDoneStatusKPI119(new ArrayList<>());
		actualFieldMappingDTO.setJiraDevDoneStatusKPI128(new ArrayList<>());
		actualFieldMappingDTO.setJiraDevDoneStatusKPI145(new ArrayList<>());
		actualFieldMappingDTO.setJiraDevDoneStatusKPI150(new ArrayList<>());
		actualFieldMappingDTO.setJiraDevDoneStatusKPI154(new ArrayList<>());
		actualFieldMappingDTO.setJiraDevDueDateCustomField("2020-03-01");
		actualFieldMappingDTO.setJiraDevDueDateField("2020-03-01");
		actualFieldMappingDTO.setJiraDod(new ArrayList<>());
		actualFieldMappingDTO.setJiraDodKPI127(new ArrayList<>());
		actualFieldMappingDTO.setJiraDodKPI14(new ArrayList<>());
		actualFieldMappingDTO.setJiraDodKPI151(new ArrayList<>());
		actualFieldMappingDTO.setJiraDodKPI152(new ArrayList<>());
		actualFieldMappingDTO.setJiraDodKPI155(new ArrayList<>());
		actualFieldMappingDTO.setJiraDodKPI156(new ArrayList<>());
		actualFieldMappingDTO.setJiraDodKPI163(new ArrayList<>());
		actualFieldMappingDTO.setJiraDodKPI166(new ArrayList<>());
		actualFieldMappingDTO.setJiraDodKPI171(new ArrayList<>());
		actualFieldMappingDTO.setJiraDodKPI37(new ArrayList<>());
		actualFieldMappingDTO.setJiraDodQAKPI111(new ArrayList<>());
		actualFieldMappingDTO.setJiraDor("Jira Dor");
		actualFieldMappingDTO.setJiraDorKPI171(new ArrayList<>());
		actualFieldMappingDTO.setJiraDorToLiveIssueType(new ArrayList<>());
		actualFieldMappingDTO.setJiraDueDateCustomField("2020-03-01");
		actualFieldMappingDTO.setJiraDueDateField("2020-03-01");
		actualFieldMappingDTO.setJiraFTPRStoryIdentification(new ArrayList<>());
		actualFieldMappingDTO.setJiraFtprRejectStatus(new ArrayList<>());
		actualFieldMappingDTO.setJiraFtprRejectStatusKPI135(new ArrayList<>());
		actualFieldMappingDTO.setJiraFtprRejectStatusKPI82(new ArrayList<>());
		actualFieldMappingDTO.setJiraIncludeBlockedStatus("Jira Include Blocked Status");
		actualFieldMappingDTO.setJiraIncludeBlockedStatusKPI131("Jira Include Blocked Status KPI131");
		actualFieldMappingDTO.setJiraIntakeToDorIssueType(new ArrayList<>());
		actualFieldMappingDTO.setJiraIssueClosedStateKPI170(new ArrayList<>());
		actualFieldMappingDTO.setJiraIssueDeliverdStatus(new ArrayList<>());
		actualFieldMappingDTO.setJiraIssueDeliverdStatusAVR(new ArrayList<>());
		actualFieldMappingDTO.setJiraIssueDeliverdStatusKPI126(new ArrayList<>());
		actualFieldMappingDTO.setJiraIssueDeliverdStatusKPI138(new ArrayList<>());
		actualFieldMappingDTO.setJiraIssueDeliverdStatusKPI82(new ArrayList<>());
		actualFieldMappingDTO.setJiraIssueEpicType(new ArrayList<>());
		actualFieldMappingDTO.setJiraIssueEpicTypeKPI153(new ArrayList<>());
		actualFieldMappingDTO.setJiraIssueTypeKPI156(new ArrayList<>());
		actualFieldMappingDTO.setJiraIssueTypeKPI171(new ArrayList<>());
		actualFieldMappingDTO.setJiraIssueTypeKPI35(new ArrayList<>());
		actualFieldMappingDTO.setJiraIssueTypeKPI3(new ArrayList<>());
		actualFieldMappingDTO.setJiraIssueTypeNames(new String[]{"Jira Issue Type Names"});
		actualFieldMappingDTO.setJiraIssueTypeNamesAVR(new String[]{"Jira Issue Type Names AVR"});
		actualFieldMappingDTO.setJiraIssueTypeNamesKPI146(new ArrayList<>());
		actualFieldMappingDTO.setJiraIssueTypeNamesKPI148(new ArrayList<>());
		actualFieldMappingDTO.setJiraIssueTypeNamesKPI151(new ArrayList<>());
		actualFieldMappingDTO.setJiraIssueTypeNamesKPI152(new ArrayList<>());
		actualFieldMappingDTO.setJiraIssueTypeNamesKPI161(new ArrayList<>());
		actualFieldMappingDTO.setJiraIssueWaitStateKPI170(new ArrayList<>());
		actualFieldMappingDTO.setJiraIterationCompletionStatusCustomField(new ArrayList<>());
		actualFieldMappingDTO.setJiraIterationCompletionStatusKPI119(new ArrayList<>());
		actualFieldMappingDTO.setJiraIterationCompletionStatusKPI120(new ArrayList<>());
		actualFieldMappingDTO.setJiraIterationCompletionStatusKPI122(new ArrayList<>());
		actualFieldMappingDTO.setJiraIterationCompletionStatusKPI123(new ArrayList<>());
		actualFieldMappingDTO.setJiraIterationCompletionStatusKPI124(new ArrayList<>());
		actualFieldMappingDTO.setJiraIterationCompletionStatusKPI125(new ArrayList<>());
		actualFieldMappingDTO.setJiraIterationCompletionStatusKPI128(new ArrayList<>());
		actualFieldMappingDTO.setJiraIterationCompletionStatusKPI131(new ArrayList<>());
		actualFieldMappingDTO.setJiraIterationCompletionStatusKPI132(new ArrayList<>());
		actualFieldMappingDTO.setJiraIterationCompletionStatusKPI133(new ArrayList<>());
		actualFieldMappingDTO.setJiraIterationCompletionStatusKPI134(new ArrayList<>());
		actualFieldMappingDTO.setJiraIterationCompletionStatusKPI135(new ArrayList<>());
		actualFieldMappingDTO.setJiraIterationCompletionStatusKPI136(new ArrayList<>());
		actualFieldMappingDTO.setJiraIterationCompletionStatusKPI138(new ArrayList<>());
		actualFieldMappingDTO.setJiraIterationCompletionStatusKPI140(new ArrayList<>());
		actualFieldMappingDTO.setJiraIterationCompletionStatusKPI145(new ArrayList<>());
		actualFieldMappingDTO.setJiraIterationCompletionStatusKPI154(new ArrayList<>());
		actualFieldMappingDTO.setJiraIterationCompletionStatusKPI75(new ArrayList<>());
		actualFieldMappingDTO.setJiraIterationCompletionStatusKpi39(new ArrayList<>());
		actualFieldMappingDTO.setJiraIterationCompletionStatusKpi5(new ArrayList<>());
		actualFieldMappingDTO.setJiraIterationCompletionStatusKpi72(new ArrayList<>());
		actualFieldMappingDTO.setJiraIterationCompletionTypeCustomField(new ArrayList<>());
		actualFieldMappingDTO.setJiraIterationIssuetypeKPI119(new ArrayList<>());
		actualFieldMappingDTO.setJiraIterationIssuetypeKPI120(new ArrayList<>());
		actualFieldMappingDTO.setJiraIterationIssuetypeKPI122(new ArrayList<>());
		actualFieldMappingDTO.setJiraIterationIssuetypeKPI123(new ArrayList<>());
		actualFieldMappingDTO.setJiraIterationIssuetypeKPI124(new ArrayList<>());
		actualFieldMappingDTO.setJiraIterationIssuetypeKPI125(new ArrayList<>());
		actualFieldMappingDTO.setJiraIterationIssuetypeKPI128(new ArrayList<>());
		actualFieldMappingDTO.setJiraIterationIssuetypeKPI131(new ArrayList<>());
		actualFieldMappingDTO.setJiraIterationIssuetypeKPI134(new ArrayList<>());
		actualFieldMappingDTO.setJiraIterationIssuetypeKPI138(new ArrayList<>());
		actualFieldMappingDTO.setJiraIterationIssuetypeKPI145(new ArrayList<>());
		actualFieldMappingDTO.setJiraIterationIssuetypeKPI39(new ArrayList<>());
		actualFieldMappingDTO.setJiraIterationIssuetypeKPI75(new ArrayList<>());
		actualFieldMappingDTO.setJiraIterationIssuetypeKpi5(new ArrayList<>());
		actualFieldMappingDTO.setJiraIterationIssuetypeKpi72(new ArrayList<>());
		actualFieldMappingDTO.setJiraItrQSIssueTypeKPI133(new ArrayList<>());
		actualFieldMappingDTO.setJiraKPI135StoryIdentification(new ArrayList<>());
		actualFieldMappingDTO.setJiraKPI82StoryIdentification(new ArrayList<>());
		actualFieldMappingDTO.setJiraLabelsKPI135(new ArrayList<>());
		actualFieldMappingDTO.setJiraLabelsKPI14(new ArrayList<>());
		actualFieldMappingDTO.setJiraLabelsKPI82(new ArrayList<>());
		actualFieldMappingDTO.setJiraLiveStatus("Jira Live Status");
		actualFieldMappingDTO.setJiraLiveStatusKPI127("Jira Live Status KPI127");
		actualFieldMappingDTO.setJiraLiveStatusKPI151("Jira Live Status KPI151");
		actualFieldMappingDTO.setJiraLiveStatusKPI152("Jira Live Status KPI152");
		actualFieldMappingDTO.setJiraLiveStatusKPI155("Jira Live Status KPI155");
		actualFieldMappingDTO.setJiraLiveStatusKPI171(new ArrayList<>());
		actualFieldMappingDTO.setJiraLiveStatusKPI3(new ArrayList<>());
		actualFieldMappingDTO.setJiraLiveStatusLTK("Jira Live Status LTK");
		actualFieldMappingDTO.setJiraLiveStatusNOPK("Jira Live Status NOPK");
		actualFieldMappingDTO.setJiraLiveStatusNORK("Jira Live Status NORK");
		actualFieldMappingDTO.setJiraLiveStatusNOSK("Jira Live Status NOSK");
		actualFieldMappingDTO.setJiraLiveStatusOTA("Jira Live Status OTA");
		actualFieldMappingDTO.setJiraOnHoldStatus(new ArrayList<>());
		actualFieldMappingDTO.setJiraOnHoldStatusKPI154(new ArrayList<>());
		actualFieldMappingDTO.setJiraProdIncidentRaisedByCustomField("Jira Prod Incident Raised By Custom Field");
		actualFieldMappingDTO.setJiraProdIncidentRaisedByValue(new ArrayList<>());
		actualFieldMappingDTO.setJiraProductionIncidentIdentification("Jira Production Incident Identification");
		actualFieldMappingDTO.setJiraProductiveStatus(new ArrayList<>());
		actualFieldMappingDTO.setJiraQADefectDensityIssueType(new ArrayList<>());
		actualFieldMappingDTO.setJiraQADoneStatusKPI154(new ArrayList<>());
		actualFieldMappingDTO.setJiraQAKPI111IssueType(new ArrayList<>());
		actualFieldMappingDTO.setJiraReadyForRefinement(new ArrayList<>());
		actualFieldMappingDTO.setJiraReadyForRefinementKPI139(new ArrayList<>());
		actualFieldMappingDTO.setJiraRejectedInRefinement(new ArrayList<>());
		actualFieldMappingDTO.setJiraRejectedInRefinementKPI139(new ArrayList<>());
		actualFieldMappingDTO.setJiraSprintCapacityIssueType(new ArrayList<>());
		actualFieldMappingDTO.setJiraSprintCapacityIssueTypeKpi46(new ArrayList<>());
		actualFieldMappingDTO.setJiraSprintVelocityIssueType(new ArrayList<>());
		actualFieldMappingDTO.setJiraSprintVelocityIssueTypeKPI138(new ArrayList<>());
		actualFieldMappingDTO.setJiraStatusForDevelopment(new ArrayList<>());
		actualFieldMappingDTO.setJiraStatusForDevelopmentAVR(new ArrayList<>());
		actualFieldMappingDTO.setJiraStatusForDevelopmentKPI135(new ArrayList<>());
		actualFieldMappingDTO.setJiraStatusForDevelopmentKPI82(new ArrayList<>());
		actualFieldMappingDTO.setJiraStatusForInProgress(new ArrayList<>());
		actualFieldMappingDTO.setJiraStatusForInProgressKPI119(new ArrayList<>());
		actualFieldMappingDTO.setJiraStatusForInProgressKPI122(new ArrayList<>());
		actualFieldMappingDTO.setJiraStatusForInProgressKPI123(new ArrayList<>());
		actualFieldMappingDTO.setJiraStatusForInProgressKPI125(new ArrayList<>());
		actualFieldMappingDTO.setJiraStatusForInProgressKPI128(new ArrayList<>());
		actualFieldMappingDTO.setJiraStatusForInProgressKPI145(new ArrayList<>());
		actualFieldMappingDTO.setJiraStatusForInProgressKPI148(new ArrayList<>());
		actualFieldMappingDTO.setJiraStatusForInProgressKPI154(new ArrayList<>());
		actualFieldMappingDTO.setJiraStatusForInProgressKPI161(new ArrayList<>());
		actualFieldMappingDTO.setJiraStatusForNotRefinedKPI161(new ArrayList<>());
		actualFieldMappingDTO.setJiraStatusForQa(new ArrayList<>());
		actualFieldMappingDTO.setJiraStatusForQaKPI135(new ArrayList<>());
		actualFieldMappingDTO.setJiraStatusForQaKPI148(new ArrayList<>());
		actualFieldMappingDTO.setJiraStatusForQaKPI82(new ArrayList<>());
		actualFieldMappingDTO.setJiraStatusForRefinedKPI161(new ArrayList<>());
		actualFieldMappingDTO.setJiraStatusMappingCustomField("Jira Status Mapping Custom Field");
		actualFieldMappingDTO.setJiraStatusStartDevelopmentKPI154(new ArrayList<>());
		actualFieldMappingDTO.setJiraStoryIdentification(new ArrayList<>());
		actualFieldMappingDTO.setJiraStoryIdentificationKPI129(new ArrayList<>());
		actualFieldMappingDTO.setJiraStoryIdentificationKPI164(new ArrayList<>());
		actualFieldMappingDTO.setJiraStoryIdentificationKPI166(new ArrayList<>());
		actualFieldMappingDTO.setJiraStoryIdentificationKpi40(new ArrayList<>());
		actualFieldMappingDTO.setJiraStoryPointsCustomField("Jira Story Points Custom Field");
		actualFieldMappingDTO.setJiraSubTaskDefectType(new ArrayList<>());
		actualFieldMappingDTO.setJiraSubTaskIdentification(new ArrayList<>());
		actualFieldMappingDTO.setJiraTechDebtCustomField("Jira Tech Debt Custom Field");
		actualFieldMappingDTO.setJiraTechDebtIdentification("Jira Tech Debt Identification");
		actualFieldMappingDTO.setJiraTechDebtIssueType(new ArrayList<>());
		actualFieldMappingDTO.setJiraTechDebtValue(new ArrayList<>());
		actualFieldMappingDTO.setJiraTestAutomationIssueType(new ArrayList<>());
		actualFieldMappingDTO.setJiraTicketClosedStatus(new ArrayList<>());
		actualFieldMappingDTO.setJiraTicketRejectedStatus(new ArrayList<>());
		actualFieldMappingDTO.setJiraTicketResolvedStatus(new ArrayList<>());
		actualFieldMappingDTO.setJiraTicketTriagedStatus(new ArrayList<>());
		actualFieldMappingDTO.setJiraTicketVelocityIssueType(new ArrayList<>());
		actualFieldMappingDTO.setJiraTicketWipStatus(new ArrayList<>());
		actualFieldMappingDTO.setJiraWaitStatus(new ArrayList<>());
		actualFieldMappingDTO.setJiraWaitStatusKPI131(new ArrayList<>());
		actualFieldMappingDTO.setJiradefecttype(new ArrayList<>());
		actualFieldMappingDTO.setKanbanCycleTimeIssueType(new ArrayList<>());
		actualFieldMappingDTO.setKanbanJiraTechDebtIssueType(new ArrayList<>());
		actualFieldMappingDTO.setKanbanRCACountIssueType(new ArrayList<>());
		actualFieldMappingDTO.setLeadTimeConfigRepoTool("Lead Time Config Repo Tool");
		actualFieldMappingDTO.setLinkDefectToStoryField(new String[]{"Link Defect To Story Field"});
		actualFieldMappingDTO.setNotificationEnabler(true);
		actualFieldMappingDTO.setPickNewATMJIRADetails(true);
		actualFieldMappingDTO.setPopulateByDevDoneKPI150(true);
		actualFieldMappingDTO.setProductionDefectComponentValue("42");
		actualFieldMappingDTO.setProductionDefectCustomField("Production Defect Custom Field");
		actualFieldMappingDTO.setProductionDefectIdentifier("42");
		actualFieldMappingDTO.setProductionDefectValue(new ArrayList<>());
		actualFieldMappingDTO.setProjectId("myproject");
		actualFieldMappingDTO.setProjectToolConfigId(ObjectId.get());
		actualFieldMappingDTO.setReadyForDevelopmentStatus("Ready For Development Status");
		actualFieldMappingDTO.setReadyForDevelopmentStatusKPI138(new ArrayList<>());
		actualFieldMappingDTO.setResolutionTypeForRejection(new ArrayList<>());
		actualFieldMappingDTO.setResolutionTypeForRejectionAVR(new ArrayList<>());
		actualFieldMappingDTO.setResolutionTypeForRejectionKPI133(new ArrayList<>());
		actualFieldMappingDTO.setResolutionTypeForRejectionKPI135(new ArrayList<>());
		actualFieldMappingDTO.setResolutionTypeForRejectionKPI14(new ArrayList<>());
		actualFieldMappingDTO.setResolutionTypeForRejectionKPI28(new ArrayList<>());
		actualFieldMappingDTO.setResolutionTypeForRejectionKPI35(new ArrayList<>());
		actualFieldMappingDTO.setResolutionTypeForRejectionKPI37(new ArrayList<>());
		actualFieldMappingDTO.setResolutionTypeForRejectionKPI82(new ArrayList<>());
		actualFieldMappingDTO.setResolutionTypeForRejectionQAKPI111(new ArrayList<>());
		actualFieldMappingDTO.setResolutionTypeForRejectionRCAKPI36(new ArrayList<>());
		actualFieldMappingDTO.setRootCause("Root Cause");
		actualFieldMappingDTO.setRootCauseValue(new ArrayList<>());
		actualFieldMappingDTO.setSprintName("Sprint Name");
		actualFieldMappingDTO.setSquadIdentMultiValue(new ArrayList<>());
		actualFieldMappingDTO.setSquadIdentSingleValue("42");
		actualFieldMappingDTO.setSquadIdentifier("42");
		actualFieldMappingDTO.setStartDateCountKPI150(3);
		actualFieldMappingDTO.setStoryFirstStatus("Story First Status");
		actualFieldMappingDTO.setStoryFirstStatusKPI148("Story First Status KPI148");
		actualFieldMappingDTO.setStoryFirstStatusKPI154(new ArrayList<>());
		actualFieldMappingDTO.setStoryFirstStatusKPI171("Story First Status KPI171");
		actualFieldMappingDTO.setStoryPointToHourMapping(10.0d);
		actualFieldMappingDTO.setTestingPhaseDefectComponentValue("42");
		actualFieldMappingDTO.setTestingPhaseDefectCustomField("Testing Phase Defect Custom Field");
		actualFieldMappingDTO.setTestingPhaseDefectValue(new ArrayList<>());
		actualFieldMappingDTO.setTestingPhaseDefectsIdentifier("42");
		actualFieldMappingDTO.setThresholdValueKPI111("42");
		actualFieldMappingDTO.setThresholdValueKPI113("42");
		actualFieldMappingDTO.setThresholdValueKPI116("42");
		actualFieldMappingDTO.setThresholdValueKPI118("42");
		actualFieldMappingDTO.setThresholdValueKPI11("42");
		actualFieldMappingDTO.setThresholdValueKPI126("42");
		actualFieldMappingDTO.setThresholdValueKPI127("42");
		actualFieldMappingDTO.setThresholdValueKPI139("42");
		actualFieldMappingDTO.setThresholdValueKPI149("42");
		actualFieldMappingDTO.setThresholdValueKPI14("42");
		actualFieldMappingDTO.setThresholdValueKPI153("42");
		actualFieldMappingDTO.setThresholdValueKPI156("42");
		actualFieldMappingDTO.setThresholdValueKPI157("42");
		actualFieldMappingDTO.setThresholdValueKPI158("42");
		actualFieldMappingDTO.setThresholdValueKPI159("42");
		actualFieldMappingDTO.setThresholdValueKPI160("42");
		actualFieldMappingDTO.setThresholdValueKPI162("42");
		actualFieldMappingDTO.setThresholdValueKPI164("42");
		actualFieldMappingDTO.setThresholdValueKPI166("42");
		actualFieldMappingDTO.setThresholdValueKPI168("42");
		actualFieldMappingDTO.setThresholdValueKPI16("42");
		actualFieldMappingDTO.setThresholdValueKPI170("42");
		actualFieldMappingDTO.setThresholdValueKPI17("42");
		actualFieldMappingDTO.setThresholdValueKPI27("42");
		actualFieldMappingDTO.setThresholdValueKPI28("42");
		actualFieldMappingDTO.setThresholdValueKPI34("42");
		actualFieldMappingDTO.setThresholdValueKPI35("42");
		actualFieldMappingDTO.setThresholdValueKPI36("42");
		actualFieldMappingDTO.setThresholdValueKPI37("42");
		actualFieldMappingDTO.setThresholdValueKPI38("42");
		actualFieldMappingDTO.setThresholdValueKPI39("42");
		actualFieldMappingDTO.setThresholdValueKPI3("42");
		actualFieldMappingDTO.setThresholdValueKPI40("42");
		actualFieldMappingDTO.setThresholdValueKPI42("42");
		actualFieldMappingDTO.setThresholdValueKPI46("42");
		actualFieldMappingDTO.setThresholdValueKPI5("42");
		actualFieldMappingDTO.setThresholdValueKPI62("42");
		actualFieldMappingDTO.setThresholdValueKPI64("42");
		actualFieldMappingDTO.setThresholdValueKPI65("42");
		actualFieldMappingDTO.setThresholdValueKPI67("42");
		actualFieldMappingDTO.setThresholdValueKPI70("42");
		actualFieldMappingDTO.setThresholdValueKPI72("42");
		actualFieldMappingDTO.setThresholdValueKPI73("42");
		actualFieldMappingDTO.setThresholdValueKPI82("42");
		actualFieldMappingDTO.setThresholdValueKPI84("42");
		actualFieldMappingDTO.setThresholdValueKPI8("42");
		actualFieldMappingDTO.setTicketCountIssueType(new ArrayList<>());
		actualFieldMappingDTO.setTicketDeliverdStatus(new ArrayList<>());
		actualFieldMappingDTO.setTicketReopenStatus(new ArrayList<>());
		actualFieldMappingDTO.setToBranchForMRKPI156("janedoe/featurebranch");
		actualFieldMappingDTO.setUploadData(true);
		actualFieldMappingDTO.setUploadDataKPI16(true);
		actualFieldMappingDTO.setUploadDataKPI42(true);
		actualFieldMappingDTO.setWorkingHoursDayCPT(10.0d);
		assertSame(additionalFilterConfigList, actualFieldMappingDTO.getAdditionalFilterConfig());
		assertEquals("https://config.us-east-2.amazonaws.com", actualFieldMappingDTO.getAtmQueryEndpoint());
		assertEquals("Atm Subproject Field", actualFieldMappingDTO.getAtmSubprojectField());
		assertSame(getResult, actualFieldMappingDTO.getBasicProjectConfigId());
		assertSame(stringList, actualFieldMappingDTO.getDefectPriority());
		assertSame(stringList1, actualFieldMappingDTO.getDefectPriorityKPI133());
		assertSame(stringList2, actualFieldMappingDTO.getDefectPriorityKPI135());
		assertSame(stringList3, actualFieldMappingDTO.getDefectPriorityKPI14());
		assertSame(stringList4, actualFieldMappingDTO.getDefectPriorityKPI82());
		assertSame(stringList5, actualFieldMappingDTO.getDefectPriorityQAKPI111());
		assertEquals("42", actualFieldMappingDTO.getEpicAchievedValue());
		assertEquals("Epic Cost Of Delay", actualFieldMappingDTO.getEpicCostOfDelay());
		assertEquals("Epic Job Size", actualFieldMappingDTO.getEpicJobSize());
		assertEquals("Epic Link", actualFieldMappingDTO.getEpicLink());
		assertEquals("Epic Name", actualFieldMappingDTO.getEpicName());
		assertEquals("42", actualFieldMappingDTO.getEpicPlannedValue());
		assertEquals("Epic Risk Reduction", actualFieldMappingDTO.getEpicRiskReduction());
		assertEquals("Epic Time Criticality", actualFieldMappingDTO.getEpicTimeCriticality());
		assertEquals("42", actualFieldMappingDTO.getEpicUserBusinessValue());
		assertEquals("Epic Wsjf", actualFieldMappingDTO.getEpicWsjf());
		assertEquals("Estimation Criteria", actualFieldMappingDTO.getEstimationCriteria());
		assertSame(stringList6, actualFieldMappingDTO.getExcludeRCAFromFTPR());
		assertSame(stringList7, actualFieldMappingDTO.getExcludeStatusKpi129());
		assertSame(stringList8, actualFieldMappingDTO.getIncludeRCAForKPI133());
		assertSame(stringList9, actualFieldMappingDTO.getIncludeRCAForKPI135());
		assertSame(stringList10, actualFieldMappingDTO.getIncludeRCAForKPI14());
		assertSame(stringList11, actualFieldMappingDTO.getIncludeRCAForKPI82());
		assertSame(stringList12, actualFieldMappingDTO.getIncludeRCAForQAKPI111());
		assertSame(stringList13, actualFieldMappingDTO.getIssueStatusExcluMissingWork());
		assertSame(stringList14, actualFieldMappingDTO.getIssueStatusExcluMissingWorkKPI124());
		assertSame(stringList15, actualFieldMappingDTO.getIssueStatusToBeExcludedFromMissingWorklogs());
		assertSame(stringList16, actualFieldMappingDTO.getJiraAcceptedInRefinement());
		assertSame(stringList17, actualFieldMappingDTO.getJiraAcceptedInRefinementKPI139());
		assertEquals("myproject", actualFieldMappingDTO.getJiraAtmProjectId());
		assertEquals("Jira Atm Project Key", actualFieldMappingDTO.getJiraAtmProjectKey());
		assertSame(stringList18, actualFieldMappingDTO.getJiraBlockedStatus());
		assertSame(stringList19, actualFieldMappingDTO.getJiraBlockedStatusKPI131());
		assertEquals("Jira Bug Raised By Custom Field", actualFieldMappingDTO.getJiraBugRaisedByCustomField());
		assertEquals("Jira Bug Raised By Identification", actualFieldMappingDTO.getJiraBugRaisedByIdentification());
		assertEquals("Jira Bug Raised By QACustom Field", actualFieldMappingDTO.getJiraBugRaisedByQACustomField());
		assertEquals("Jira Bug Raised By QAIdentification", actualFieldMappingDTO.getJiraBugRaisedByQAIdentification());
		assertSame(stringList20, actualFieldMappingDTO.getJiraBugRaisedByQAValue());
		assertSame(stringList21, actualFieldMappingDTO.getJiraBugRaisedByValue());
		assertSame(stringList22, actualFieldMappingDTO.getJiraCommitmentReliabilityIssueType());
		assertSame(stringList23, actualFieldMappingDTO.getJiraDefectClosedStatus());
		assertSame(stringList24, actualFieldMappingDTO.getJiraDefectClosedStatusKPI137());
		assertSame(stringList25, actualFieldMappingDTO.getJiraDefectCountlIssueType());
		assertSame(stringList26, actualFieldMappingDTO.getJiraDefectCountlIssueTypeKPI28());
	}

	/**
	 * Method under test: {@link FieldMappingDTO#getJiraIssueTypeNames()}
	 */
	@Test
	public void testGetJiraIssueTypeNames() {
		assertNull((new FieldMappingDTO()).getJiraIssueTypeNames());
	}

	/**
	 * Method under test: {@link FieldMappingDTO#getJiraIssueTypeNames()}
	 */
	@Test
	public void testGetJiraIssueTypeNames2() {
		FieldMappingDTO fieldMappingDTO = new FieldMappingDTO();
		fieldMappingDTO.setAdditionalFilterConfig(new ArrayList<>());
		fieldMappingDTO.setAtmQueryEndpoint("https://config.us-east-2.amazonaws.com");
		fieldMappingDTO.setAtmSubprojectField("Atm Subproject Field");
		fieldMappingDTO.setBasicProjectConfigId(ObjectId.get());
		fieldMappingDTO.setDefectPriority(new ArrayList<>());
		fieldMappingDTO.setDefectPriorityKPI133(new ArrayList<>());
		fieldMappingDTO.setDefectPriorityKPI135(new ArrayList<>());
		fieldMappingDTO.setDefectPriorityKPI14(new ArrayList<>());
		fieldMappingDTO.setDefectPriorityKPI82(new ArrayList<>());
		fieldMappingDTO.setDefectPriorityQAKPI111(new ArrayList<>());
		fieldMappingDTO.setEpicAchievedValue("42");
		fieldMappingDTO.setEpicCostOfDelay("Epic Cost Of Delay");
		fieldMappingDTO.setEpicJobSize("Epic Job Size");
		fieldMappingDTO.setEpicLink("Epic Link");
		fieldMappingDTO.setEpicName("Epic Name");
		fieldMappingDTO.setEpicPlannedValue("42");
		fieldMappingDTO.setEpicRiskReduction("Epic Risk Reduction");
		fieldMappingDTO.setEpicTimeCriticality("Epic Time Criticality");
		fieldMappingDTO.setEpicUserBusinessValue("42");
		fieldMappingDTO.setEpicWsjf("Epic Wsjf");
		fieldMappingDTO.setEstimationCriteria("Estimation Criteria");
		fieldMappingDTO.setExcludeRCAFromFTPR(new ArrayList<>());
		fieldMappingDTO.setExcludeStatusKpi129(new ArrayList<>());
		fieldMappingDTO.setId(ObjectId.get());
		fieldMappingDTO.setIncludeRCAForKPI133(new ArrayList<>());
		fieldMappingDTO.setIncludeRCAForKPI135(new ArrayList<>());
		fieldMappingDTO.setIncludeRCAForKPI14(new ArrayList<>());
		fieldMappingDTO.setIncludeRCAForKPI82(new ArrayList<>());
		fieldMappingDTO.setIncludeRCAForQAKPI111(new ArrayList<>());
		fieldMappingDTO.setIssueStatusExcluMissingWork(new ArrayList<>());
		fieldMappingDTO.setIssueStatusExcluMissingWorkKPI124(new ArrayList<>());
		fieldMappingDTO.setIssueStatusToBeExcludedFromMissingWorklogs(new ArrayList<>());
		fieldMappingDTO.setJiraAcceptedInRefinement(new ArrayList<>());
		fieldMappingDTO.setJiraAcceptedInRefinementKPI139(new ArrayList<>());
		fieldMappingDTO.setJiraAtmProjectId("myproject");
		fieldMappingDTO.setJiraAtmProjectKey("Jira Atm Project Key");
		fieldMappingDTO.setJiraBlockedStatus(new ArrayList<>());
		fieldMappingDTO.setJiraBlockedStatusKPI131(new ArrayList<>());
		fieldMappingDTO.setJiraBugRaisedByCustomField("Jira Bug Raised By Custom Field");
		fieldMappingDTO.setJiraBugRaisedByIdentification("Jira Bug Raised By Identification");
		fieldMappingDTO.setJiraBugRaisedByQACustomField("Jira Bug Raised By QACustom Field");
		fieldMappingDTO.setJiraBugRaisedByQAIdentification("Jira Bug Raised By QAIdentification");
		fieldMappingDTO.setJiraBugRaisedByQAValue(new ArrayList<>());
		fieldMappingDTO.setJiraBugRaisedByValue(new ArrayList<>());
		fieldMappingDTO.setJiraCommitmentReliabilityIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraDefectClosedStatus(new ArrayList<>());
		fieldMappingDTO.setJiraDefectClosedStatusKPI137(new ArrayList<>());
		fieldMappingDTO.setJiraDefectCountlIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraDefectCountlIssueTypeKPI28(new ArrayList<>());
		fieldMappingDTO.setJiraDefectCountlIssueTypeKPI36(new ArrayList<>());
		fieldMappingDTO.setJiraDefectCreatedStatus("Jan 1, 2020 8:00am GMT+0100");
		fieldMappingDTO.setJiraDefectCreatedStatusKPI14("Jan 1, 2020 8:00am GMT+0100");
		fieldMappingDTO.setJiraDefectDroppedStatus(new ArrayList<>());
		fieldMappingDTO.setJiraDefectDroppedStatusKPI127(new ArrayList<>());
		fieldMappingDTO.setJiraDefectInjectionIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraDefectInjectionIssueTypeKPI14(new ArrayList<>());
		fieldMappingDTO.setJiraDefectRejectionStatus("Jira Defect Rejection Status");
		fieldMappingDTO.setJiraDefectRejectionStatusAVR("Jira Defect Rejection Status AVR");
		fieldMappingDTO.setJiraDefectRejectionStatusKPI133("Jira Defect Rejection Status KPI133");
		fieldMappingDTO.setJiraDefectRejectionStatusKPI135("Jira Defect Rejection Status KPI135");
		fieldMappingDTO.setJiraDefectRejectionStatusKPI14("Jira Defect Rejection Status KPI14");
		fieldMappingDTO.setJiraDefectRejectionStatusKPI151("Jira Defect Rejection Status KPI151");
		fieldMappingDTO.setJiraDefectRejectionStatusKPI152("Jira Defect Rejection Status KPI152");
		fieldMappingDTO.setJiraDefectRejectionStatusKPI155("Jira Defect Rejection Status KPI155");
		fieldMappingDTO.setJiraDefectRejectionStatusKPI28("Jira Defect Rejection Status KPI28");
		fieldMappingDTO.setJiraDefectRejectionStatusKPI35("Jira Defect Rejection Status KPI35");
		fieldMappingDTO.setJiraDefectRejectionStatusKPI37("Jira Defect Rejection Status KPI37");
		fieldMappingDTO.setJiraDefectRejectionStatusKPI82("Jira Defect Rejection Status KPI82");
		fieldMappingDTO.setJiraDefectRejectionStatusQAKPI111("Jira Defect Rejection Status QAKPI111");
		fieldMappingDTO.setJiraDefectRejectionStatusRCAKPI36("Jira Defect Rejection Status RCAKPI36");
		fieldMappingDTO.setJiraDefectRejectionlIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraDefectRemovalIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraDefectRemovalStatus(new ArrayList<>());
		fieldMappingDTO.setJiraDefectRemovalStatusKPI34(new ArrayList<>());
		fieldMappingDTO.setJiraDefectSeepageIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraDevDoneStatus(new ArrayList<>());
		fieldMappingDTO.setJiraDevDoneStatusKPI119(new ArrayList<>());
		fieldMappingDTO.setJiraDevDoneStatusKPI128(new ArrayList<>());
		fieldMappingDTO.setJiraDevDoneStatusKPI145(new ArrayList<>());
		fieldMappingDTO.setJiraDevDoneStatusKPI150(new ArrayList<>());
		fieldMappingDTO.setJiraDevDoneStatusKPI154(new ArrayList<>());
		fieldMappingDTO.setJiraDevDueDateCustomField("2020-03-01");
		fieldMappingDTO.setJiraDevDueDateField("2020-03-01");
		fieldMappingDTO.setJiraDod(new ArrayList<>());
		fieldMappingDTO.setJiraDodKPI127(new ArrayList<>());
		fieldMappingDTO.setJiraDodKPI14(new ArrayList<>());
		fieldMappingDTO.setJiraDodKPI151(new ArrayList<>());
		fieldMappingDTO.setJiraDodKPI152(new ArrayList<>());
		fieldMappingDTO.setJiraDodKPI155(new ArrayList<>());
		fieldMappingDTO.setJiraDodKPI156(new ArrayList<>());
		fieldMappingDTO.setJiraDodKPI163(new ArrayList<>());
		fieldMappingDTO.setJiraDodKPI166(new ArrayList<>());
		fieldMappingDTO.setJiraDodKPI171(new ArrayList<>());
		fieldMappingDTO.setJiraDodKPI37(new ArrayList<>());
		fieldMappingDTO.setJiraDodQAKPI111(new ArrayList<>());
		fieldMappingDTO.setJiraDor("Jira Dor");
		fieldMappingDTO.setJiraDorKPI171(new ArrayList<>());
		fieldMappingDTO.setJiraDorToLiveIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraDueDateCustomField("2020-03-01");
		fieldMappingDTO.setJiraDueDateField("2020-03-01");
		fieldMappingDTO.setJiraFTPRStoryIdentification(new ArrayList<>());
		fieldMappingDTO.setJiraFtprRejectStatus(new ArrayList<>());
		fieldMappingDTO.setJiraFtprRejectStatusKPI135(new ArrayList<>());
		fieldMappingDTO.setJiraFtprRejectStatusKPI82(new ArrayList<>());
		fieldMappingDTO.setJiraIncludeBlockedStatus("Jira Include Blocked Status");
		fieldMappingDTO.setJiraIncludeBlockedStatusKPI131("Jira Include Blocked Status KPI131");
		fieldMappingDTO.setJiraIntakeToDorIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraIssueClosedStateKPI170(new ArrayList<>());
		fieldMappingDTO.setJiraIssueDeliverdStatus(new ArrayList<>());
		fieldMappingDTO.setJiraIssueDeliverdStatusAVR(new ArrayList<>());
		fieldMappingDTO.setJiraIssueDeliverdStatusKPI126(new ArrayList<>());
		fieldMappingDTO.setJiraIssueDeliverdStatusKPI138(new ArrayList<>());
		fieldMappingDTO.setJiraIssueDeliverdStatusKPI82(new ArrayList<>());
		fieldMappingDTO.setJiraIssueEpicType(new ArrayList<>());
		fieldMappingDTO.setJiraIssueEpicTypeKPI153(new ArrayList<>());
		fieldMappingDTO.setJiraIssueTypeKPI156(new ArrayList<>());
		fieldMappingDTO.setJiraIssueTypeKPI171(new ArrayList<>());
		fieldMappingDTO.setJiraIssueTypeKPI3(new ArrayList<>());
		fieldMappingDTO.setJiraIssueTypeKPI35(new ArrayList<>());
		fieldMappingDTO.setJiraIssueTypeNames(new String[]{"Jira Issue Type Names"});
		fieldMappingDTO.setJiraIssueTypeNamesAVR(new String[]{"Jira Issue Type Names AVR"});
		fieldMappingDTO.setJiraIssueTypeNamesKPI146(new ArrayList<>());
		fieldMappingDTO.setJiraIssueTypeNamesKPI148(new ArrayList<>());
		fieldMappingDTO.setJiraIssueTypeNamesKPI151(new ArrayList<>());
		fieldMappingDTO.setJiraIssueTypeNamesKPI152(new ArrayList<>());
		fieldMappingDTO.setJiraIssueTypeNamesKPI161(new ArrayList<>());
		fieldMappingDTO.setJiraIssueWaitStateKPI170(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusCustomField(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI119(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI120(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI122(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI123(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI124(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI125(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI128(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI131(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI132(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI133(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI134(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI135(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI136(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI138(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI140(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI145(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI154(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI75(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKpi39(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKpi5(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKpi72(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionTypeCustomField(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI119(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI120(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI122(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI123(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI124(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI125(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI128(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI131(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI134(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI138(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI145(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI39(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI75(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKpi5(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKpi72(new ArrayList<>());
		fieldMappingDTO.setJiraItrQSIssueTypeKPI133(new ArrayList<>());
		fieldMappingDTO.setJiraKPI135StoryIdentification(new ArrayList<>());
		fieldMappingDTO.setJiraKPI82StoryIdentification(new ArrayList<>());
		fieldMappingDTO.setJiraLabelsKPI135(new ArrayList<>());
		fieldMappingDTO.setJiraLabelsKPI14(new ArrayList<>());
		fieldMappingDTO.setJiraLabelsKPI82(new ArrayList<>());
		fieldMappingDTO.setJiraLiveStatus("Jira Live Status");
		fieldMappingDTO.setJiraLiveStatusKPI127("Jira Live Status KPI127");
		fieldMappingDTO.setJiraLiveStatusKPI151("Jira Live Status KPI151");
		fieldMappingDTO.setJiraLiveStatusKPI152("Jira Live Status KPI152");
		fieldMappingDTO.setJiraLiveStatusKPI155("Jira Live Status KPI155");
		fieldMappingDTO.setJiraLiveStatusKPI171(new ArrayList<>());
		fieldMappingDTO.setJiraLiveStatusKPI3(new ArrayList<>());
		fieldMappingDTO.setJiraLiveStatusLTK("Jira Live Status LTK");
		fieldMappingDTO.setJiraLiveStatusNOPK("Jira Live Status NOPK");
		fieldMappingDTO.setJiraLiveStatusNORK("Jira Live Status NORK");
		fieldMappingDTO.setJiraLiveStatusNOSK("Jira Live Status NOSK");
		fieldMappingDTO.setJiraLiveStatusOTA("Jira Live Status OTA");
		fieldMappingDTO.setJiraOnHoldStatus(new ArrayList<>());
		fieldMappingDTO.setJiraOnHoldStatusKPI154(new ArrayList<>());
		fieldMappingDTO.setJiraProdIncidentRaisedByCustomField("Jira Prod Incident Raised By Custom Field");
		fieldMappingDTO.setJiraProdIncidentRaisedByValue(new ArrayList<>());
		fieldMappingDTO.setJiraProductionIncidentIdentification("Jira Production Incident Identification");
		fieldMappingDTO.setJiraProductiveStatus(new ArrayList<>());
		fieldMappingDTO.setJiraQADefectDensityIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraQADoneStatusKPI154(new ArrayList<>());
		fieldMappingDTO.setJiraQAKPI111IssueType(new ArrayList<>());
		fieldMappingDTO.setJiraReadyForRefinement(new ArrayList<>());
		fieldMappingDTO.setJiraReadyForRefinementKPI139(new ArrayList<>());
		fieldMappingDTO.setJiraRejectedInRefinement(new ArrayList<>());
		fieldMappingDTO.setJiraRejectedInRefinementKPI139(new ArrayList<>());
		fieldMappingDTO.setJiraSprintCapacityIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraSprintCapacityIssueTypeKpi46(new ArrayList<>());
		fieldMappingDTO.setJiraSprintVelocityIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraSprintVelocityIssueTypeKPI138(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForDevelopment(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForDevelopmentAVR(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForDevelopmentKPI135(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForDevelopmentKPI82(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForInProgress(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForInProgressKPI119(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForInProgressKPI122(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForInProgressKPI123(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForInProgressKPI125(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForInProgressKPI128(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForInProgressKPI145(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForInProgressKPI148(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForInProgressKPI154(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForInProgressKPI161(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForNotRefinedKPI161(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForQa(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForQaKPI135(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForQaKPI148(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForQaKPI82(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForRefinedKPI161(new ArrayList<>());
		fieldMappingDTO.setJiraStatusMappingCustomField("Jira Status Mapping Custom Field");
		fieldMappingDTO.setJiraStatusStartDevelopmentKPI154(new ArrayList<>());
		fieldMappingDTO.setJiraStoryIdentification(new ArrayList<>());
		fieldMappingDTO.setJiraStoryIdentificationKPI129(new ArrayList<>());
		fieldMappingDTO.setJiraStoryIdentificationKPI164(new ArrayList<>());
		fieldMappingDTO.setJiraStoryIdentificationKPI166(new ArrayList<>());
		fieldMappingDTO.setJiraStoryIdentificationKpi40(new ArrayList<>());
		fieldMappingDTO.setJiraStoryPointsCustomField("Jira Story Points Custom Field");
		fieldMappingDTO.setJiraSubTaskDefectType(new ArrayList<>());
		fieldMappingDTO.setJiraSubTaskIdentification(new ArrayList<>());
		fieldMappingDTO.setJiraTechDebtCustomField("Jira Tech Debt Custom Field");
		fieldMappingDTO.setJiraTechDebtIdentification("Jira Tech Debt Identification");
		fieldMappingDTO.setJiraTechDebtIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraTechDebtValue(new ArrayList<>());
		fieldMappingDTO.setJiraTestAutomationIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraTicketClosedStatus(new ArrayList<>());
		fieldMappingDTO.setJiraTicketRejectedStatus(new ArrayList<>());
		fieldMappingDTO.setJiraTicketResolvedStatus(new ArrayList<>());
		fieldMappingDTO.setJiraTicketTriagedStatus(new ArrayList<>());
		fieldMappingDTO.setJiraTicketVelocityIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraTicketWipStatus(new ArrayList<>());
		fieldMappingDTO.setJiraWaitStatus(new ArrayList<>());
		fieldMappingDTO.setJiraWaitStatusKPI131(new ArrayList<>());
		fieldMappingDTO.setJiradefecttype(new ArrayList<>());
		fieldMappingDTO.setKanbanCycleTimeIssueType(new ArrayList<>());
		fieldMappingDTO.setKanbanJiraTechDebtIssueType(new ArrayList<>());
		fieldMappingDTO.setKanbanRCACountIssueType(new ArrayList<>());
		fieldMappingDTO.setLeadTimeConfigRepoTool("Lead Time Config Repo Tool");
		fieldMappingDTO.setLinkDefectToStoryField(new String[]{"Link Defect To Story Field"});
		fieldMappingDTO.setNotificationEnabler(true);
		fieldMappingDTO.setPickNewATMJIRADetails(true);
		fieldMappingDTO.setPopulateByDevDoneKPI150(true);
		fieldMappingDTO.setProductionDefectComponentValue("42");
		fieldMappingDTO.setProductionDefectCustomField("Production Defect Custom Field");
		fieldMappingDTO.setProductionDefectIdentifier("42");
		fieldMappingDTO.setProductionDefectValue(new ArrayList<>());
		fieldMappingDTO.setProjectId("myproject");
		fieldMappingDTO.setProjectToolConfigId(ObjectId.get());
		fieldMappingDTO.setReadyForDevelopmentStatus("Ready For Development Status");
		fieldMappingDTO.setReadyForDevelopmentStatusKPI138(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejection(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejectionAVR(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejectionKPI133(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejectionKPI135(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejectionKPI14(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejectionKPI28(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejectionKPI35(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejectionKPI37(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejectionKPI82(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejectionQAKPI111(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejectionRCAKPI36(new ArrayList<>());
		fieldMappingDTO.setRootCause("Root Cause");
		fieldMappingDTO.setRootCauseValue(new ArrayList<>());
		fieldMappingDTO.setSprintName("Sprint Name");
		fieldMappingDTO.setSquadIdentMultiValue(new ArrayList<>());
		fieldMappingDTO.setSquadIdentSingleValue("42");
		fieldMappingDTO.setSquadIdentifier("42");
		fieldMappingDTO.setStartDateCountKPI150(3);
		fieldMappingDTO.setStoryFirstStatus("Story First Status");
		fieldMappingDTO.setStoryFirstStatusKPI148("Story First Status KPI148");
		fieldMappingDTO.setStoryFirstStatusKPI154(new ArrayList<>());
		fieldMappingDTO.setStoryFirstStatusKPI171("Story First Status KPI171");
		fieldMappingDTO.setStoryPointToHourMapping(10.0d);
		fieldMappingDTO.setTestingPhaseDefectComponentValue("42");
		fieldMappingDTO.setTestingPhaseDefectCustomField("Testing Phase Defect Custom Field");
		fieldMappingDTO.setTestingPhaseDefectValue(new ArrayList<>());
		fieldMappingDTO.setTestingPhaseDefectsIdentifier("42");
		fieldMappingDTO.setThresholdValueKPI11("42");
		fieldMappingDTO.setThresholdValueKPI111("42");
		fieldMappingDTO.setThresholdValueKPI113("42");
		fieldMappingDTO.setThresholdValueKPI116("42");
		fieldMappingDTO.setThresholdValueKPI118("42");
		fieldMappingDTO.setThresholdValueKPI126("42");
		fieldMappingDTO.setThresholdValueKPI127("42");
		fieldMappingDTO.setThresholdValueKPI139("42");
		fieldMappingDTO.setThresholdValueKPI14("42");
		fieldMappingDTO.setThresholdValueKPI149("42");
		fieldMappingDTO.setThresholdValueKPI153("42");
		fieldMappingDTO.setThresholdValueKPI156("42");
		fieldMappingDTO.setThresholdValueKPI157("42");
		fieldMappingDTO.setThresholdValueKPI158("42");
		fieldMappingDTO.setThresholdValueKPI159("42");
		fieldMappingDTO.setThresholdValueKPI16("42");
		fieldMappingDTO.setThresholdValueKPI160("42");
		fieldMappingDTO.setThresholdValueKPI162("42");
		fieldMappingDTO.setThresholdValueKPI164("42");
		fieldMappingDTO.setThresholdValueKPI166("42");
		fieldMappingDTO.setThresholdValueKPI168("42");
		fieldMappingDTO.setThresholdValueKPI17("42");
		fieldMappingDTO.setThresholdValueKPI170("42");
		fieldMappingDTO.setThresholdValueKPI27("42");
		fieldMappingDTO.setThresholdValueKPI28("42");
		fieldMappingDTO.setThresholdValueKPI3("42");
		fieldMappingDTO.setThresholdValueKPI34("42");
		fieldMappingDTO.setThresholdValueKPI35("42");
		fieldMappingDTO.setThresholdValueKPI36("42");
		fieldMappingDTO.setThresholdValueKPI37("42");
		fieldMappingDTO.setThresholdValueKPI38("42");
		fieldMappingDTO.setThresholdValueKPI39("42");
		fieldMappingDTO.setThresholdValueKPI40("42");
		fieldMappingDTO.setThresholdValueKPI42("42");
		fieldMappingDTO.setThresholdValueKPI46("42");
		fieldMappingDTO.setThresholdValueKPI5("42");
		fieldMappingDTO.setThresholdValueKPI62("42");
		fieldMappingDTO.setThresholdValueKPI64("42");
		fieldMappingDTO.setThresholdValueKPI65("42");
		fieldMappingDTO.setThresholdValueKPI67("42");
		fieldMappingDTO.setThresholdValueKPI70("42");
		fieldMappingDTO.setThresholdValueKPI72("42");
		fieldMappingDTO.setThresholdValueKPI73("42");
		fieldMappingDTO.setThresholdValueKPI8("42");
		fieldMappingDTO.setThresholdValueKPI82("42");
		fieldMappingDTO.setThresholdValueKPI84("42");
		fieldMappingDTO.setTicketCountIssueType(new ArrayList<>());
		fieldMappingDTO.setTicketDeliverdStatus(new ArrayList<>());
		fieldMappingDTO.setTicketReopenStatus(new ArrayList<>());
		fieldMappingDTO.setToBranchForMRKPI156("janedoe/featurebranch");
		fieldMappingDTO.setUploadData(true);
		fieldMappingDTO.setUploadDataKPI16(true);
		fieldMappingDTO.setUploadDataKPI42(true);
		fieldMappingDTO.setWorkingHoursDayCPT(10.0d);
		String[] actualJiraIssueTypeNames = fieldMappingDTO.getJiraIssueTypeNames();
		assertEquals(1, actualJiraIssueTypeNames.length);
		assertEquals("Jira Issue Type Names", actualJiraIssueTypeNames[0]);
	}

	/**
	 * Method under test: {@link FieldMappingDTO#setJiraIssueTypeNames(String[])}
	 */
	@Test
	public void testSetJiraIssueTypeNames() {
		// TODO: Complete this test.
		// Reason: R004 No meaningful assertions found.
		// Diffblue Cover was unable to create an assertion.
		// Make sure that fields modified by setJiraIssueTypeNames(String[])
		// have package-private, protected, or public getters.
		// See https://diff.blue/R004 to resolve this issue.

		(new FieldMappingDTO()).setJiraIssueTypeNames(new String[]{"Jira Issue Type Names"});
	}

	/**
	 * Method under test: {@link FieldMappingDTO#setJiraIssueTypeNames(String[])}
	 */
	@Test
	public void testSetJiraIssueTypeNames2() {
		// TODO: Complete this test.
		// Reason: R004 No meaningful assertions found.
		// Diffblue Cover was unable to create an assertion.
		// Make sure that fields modified by setJiraIssueTypeNames(String[])
		// have package-private, protected, or public getters.
		// See https://diff.blue/R004 to resolve this issue.

		FieldMappingDTO fieldMappingDTO = new FieldMappingDTO();
		fieldMappingDTO.setAdditionalFilterConfig(new ArrayList<>());
		fieldMappingDTO.setAtmQueryEndpoint("https://config.us-east-2.amazonaws.com");
		fieldMappingDTO.setAtmSubprojectField("Atm Subproject Field");
		fieldMappingDTO.setBasicProjectConfigId(ObjectId.get());
		fieldMappingDTO.setDefectPriority(new ArrayList<>());
		fieldMappingDTO.setDefectPriorityKPI133(new ArrayList<>());
		fieldMappingDTO.setDefectPriorityKPI135(new ArrayList<>());
		fieldMappingDTO.setDefectPriorityKPI14(new ArrayList<>());
		fieldMappingDTO.setDefectPriorityKPI82(new ArrayList<>());
		fieldMappingDTO.setDefectPriorityQAKPI111(new ArrayList<>());
		fieldMappingDTO.setEpicAchievedValue("42");
		fieldMappingDTO.setEpicCostOfDelay("Epic Cost Of Delay");
		fieldMappingDTO.setEpicJobSize("Epic Job Size");
		fieldMappingDTO.setEpicLink("Epic Link");
		fieldMappingDTO.setEpicName("Epic Name");
		fieldMappingDTO.setEpicPlannedValue("42");
		fieldMappingDTO.setEpicRiskReduction("Epic Risk Reduction");
		fieldMappingDTO.setEpicTimeCriticality("Epic Time Criticality");
		fieldMappingDTO.setEpicUserBusinessValue("42");
		fieldMappingDTO.setEpicWsjf("Epic Wsjf");
		fieldMappingDTO.setEstimationCriteria("Estimation Criteria");
		fieldMappingDTO.setExcludeRCAFromFTPR(new ArrayList<>());
		fieldMappingDTO.setExcludeStatusKpi129(new ArrayList<>());
		fieldMappingDTO.setId(ObjectId.get());
		fieldMappingDTO.setIncludeRCAForKPI133(new ArrayList<>());
		fieldMappingDTO.setIncludeRCAForKPI135(new ArrayList<>());
		fieldMappingDTO.setIncludeRCAForKPI14(new ArrayList<>());
		fieldMappingDTO.setIncludeRCAForKPI82(new ArrayList<>());
		fieldMappingDTO.setIncludeRCAForQAKPI111(new ArrayList<>());
		fieldMappingDTO.setIssueStatusExcluMissingWork(new ArrayList<>());
		fieldMappingDTO.setIssueStatusExcluMissingWorkKPI124(new ArrayList<>());
		fieldMappingDTO.setIssueStatusToBeExcludedFromMissingWorklogs(new ArrayList<>());
		fieldMappingDTO.setJiraAcceptedInRefinement(new ArrayList<>());
		fieldMappingDTO.setJiraAcceptedInRefinementKPI139(new ArrayList<>());
		fieldMappingDTO.setJiraAtmProjectId("myproject");
		fieldMappingDTO.setJiraAtmProjectKey("Jira Atm Project Key");
		fieldMappingDTO.setJiraBlockedStatus(new ArrayList<>());
		fieldMappingDTO.setJiraBlockedStatusKPI131(new ArrayList<>());
		fieldMappingDTO.setJiraBugRaisedByCustomField("Jira Bug Raised By Custom Field");
		fieldMappingDTO.setJiraBugRaisedByIdentification("Jira Bug Raised By Identification");
		fieldMappingDTO.setJiraBugRaisedByQACustomField("Jira Bug Raised By QACustom Field");
		fieldMappingDTO.setJiraBugRaisedByQAIdentification("Jira Bug Raised By QAIdentification");
		fieldMappingDTO.setJiraBugRaisedByQAValue(new ArrayList<>());
		fieldMappingDTO.setJiraBugRaisedByValue(new ArrayList<>());
		fieldMappingDTO.setJiraCommitmentReliabilityIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraDefectClosedStatus(new ArrayList<>());
		fieldMappingDTO.setJiraDefectClosedStatusKPI137(new ArrayList<>());
		fieldMappingDTO.setJiraDefectCountlIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraDefectCountlIssueTypeKPI28(new ArrayList<>());
		fieldMappingDTO.setJiraDefectCountlIssueTypeKPI36(new ArrayList<>());
		fieldMappingDTO.setJiraDefectCreatedStatus("Jan 1, 2020 8:00am GMT+0100");
		fieldMappingDTO.setJiraDefectCreatedStatusKPI14("Jan 1, 2020 8:00am GMT+0100");
		fieldMappingDTO.setJiraDefectDroppedStatus(new ArrayList<>());
		fieldMappingDTO.setJiraDefectDroppedStatusKPI127(new ArrayList<>());
		fieldMappingDTO.setJiraDefectInjectionIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraDefectInjectionIssueTypeKPI14(new ArrayList<>());
		fieldMappingDTO.setJiraDefectRejectionStatus("Jira Defect Rejection Status");
		fieldMappingDTO.setJiraDefectRejectionStatusAVR("Jira Defect Rejection Status AVR");
		fieldMappingDTO.setJiraDefectRejectionStatusKPI133("Jira Defect Rejection Status KPI133");
		fieldMappingDTO.setJiraDefectRejectionStatusKPI135("Jira Defect Rejection Status KPI135");
		fieldMappingDTO.setJiraDefectRejectionStatusKPI14("Jira Defect Rejection Status KPI14");
		fieldMappingDTO.setJiraDefectRejectionStatusKPI151("Jira Defect Rejection Status KPI151");
		fieldMappingDTO.setJiraDefectRejectionStatusKPI152("Jira Defect Rejection Status KPI152");
		fieldMappingDTO.setJiraDefectRejectionStatusKPI155("Jira Defect Rejection Status KPI155");
		fieldMappingDTO.setJiraDefectRejectionStatusKPI28("Jira Defect Rejection Status KPI28");
		fieldMappingDTO.setJiraDefectRejectionStatusKPI35("Jira Defect Rejection Status KPI35");
		fieldMappingDTO.setJiraDefectRejectionStatusKPI37("Jira Defect Rejection Status KPI37");
		fieldMappingDTO.setJiraDefectRejectionStatusKPI82("Jira Defect Rejection Status KPI82");
		fieldMappingDTO.setJiraDefectRejectionStatusQAKPI111("Jira Defect Rejection Status QAKPI111");
		fieldMappingDTO.setJiraDefectRejectionStatusRCAKPI36("Jira Defect Rejection Status RCAKPI36");
		fieldMappingDTO.setJiraDefectRejectionlIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraDefectRemovalIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraDefectRemovalStatus(new ArrayList<>());
		fieldMappingDTO.setJiraDefectRemovalStatusKPI34(new ArrayList<>());
		fieldMappingDTO.setJiraDefectSeepageIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraDevDoneStatus(new ArrayList<>());
		fieldMappingDTO.setJiraDevDoneStatusKPI119(new ArrayList<>());
		fieldMappingDTO.setJiraDevDoneStatusKPI128(new ArrayList<>());
		fieldMappingDTO.setJiraDevDoneStatusKPI145(new ArrayList<>());
		fieldMappingDTO.setJiraDevDoneStatusKPI150(new ArrayList<>());
		fieldMappingDTO.setJiraDevDoneStatusKPI154(new ArrayList<>());
		fieldMappingDTO.setJiraDevDueDateCustomField("2020-03-01");
		fieldMappingDTO.setJiraDevDueDateField("2020-03-01");
		fieldMappingDTO.setJiraDod(new ArrayList<>());
		fieldMappingDTO.setJiraDodKPI127(new ArrayList<>());
		fieldMappingDTO.setJiraDodKPI14(new ArrayList<>());
		fieldMappingDTO.setJiraDodKPI151(new ArrayList<>());
		fieldMappingDTO.setJiraDodKPI152(new ArrayList<>());
		fieldMappingDTO.setJiraDodKPI155(new ArrayList<>());
		fieldMappingDTO.setJiraDodKPI156(new ArrayList<>());
		fieldMappingDTO.setJiraDodKPI163(new ArrayList<>());
		fieldMappingDTO.setJiraDodKPI166(new ArrayList<>());
		fieldMappingDTO.setJiraDodKPI171(new ArrayList<>());
		fieldMappingDTO.setJiraDodKPI37(new ArrayList<>());
		fieldMappingDTO.setJiraDodQAKPI111(new ArrayList<>());
		fieldMappingDTO.setJiraDor("Jira Dor");
		fieldMappingDTO.setJiraDorKPI171(new ArrayList<>());
		fieldMappingDTO.setJiraDorToLiveIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraDueDateCustomField("2020-03-01");
		fieldMappingDTO.setJiraDueDateField("2020-03-01");
		fieldMappingDTO.setJiraFTPRStoryIdentification(new ArrayList<>());
		fieldMappingDTO.setJiraFtprRejectStatus(new ArrayList<>());
		fieldMappingDTO.setJiraFtprRejectStatusKPI135(new ArrayList<>());
		fieldMappingDTO.setJiraFtprRejectStatusKPI82(new ArrayList<>());
		fieldMappingDTO.setJiraIncludeBlockedStatus("Jira Include Blocked Status");
		fieldMappingDTO.setJiraIncludeBlockedStatusKPI131("Jira Include Blocked Status KPI131");
		fieldMappingDTO.setJiraIntakeToDorIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraIssueClosedStateKPI170(new ArrayList<>());
		fieldMappingDTO.setJiraIssueDeliverdStatus(new ArrayList<>());
		fieldMappingDTO.setJiraIssueDeliverdStatusAVR(new ArrayList<>());
		fieldMappingDTO.setJiraIssueDeliverdStatusKPI126(new ArrayList<>());
		fieldMappingDTO.setJiraIssueDeliverdStatusKPI138(new ArrayList<>());
		fieldMappingDTO.setJiraIssueDeliverdStatusKPI82(new ArrayList<>());
		fieldMappingDTO.setJiraIssueEpicType(new ArrayList<>());
		fieldMappingDTO.setJiraIssueEpicTypeKPI153(new ArrayList<>());
		fieldMappingDTO.setJiraIssueTypeKPI156(new ArrayList<>());
		fieldMappingDTO.setJiraIssueTypeKPI171(new ArrayList<>());
		fieldMappingDTO.setJiraIssueTypeKPI3(new ArrayList<>());
		fieldMappingDTO.setJiraIssueTypeKPI35(new ArrayList<>());
		fieldMappingDTO.setJiraIssueTypeNames(new String[]{"Jira Issue Type Names"});
		fieldMappingDTO.setJiraIssueTypeNamesAVR(new String[]{"Jira Issue Type Names AVR"});
		fieldMappingDTO.setJiraIssueTypeNamesKPI146(new ArrayList<>());
		fieldMappingDTO.setJiraIssueTypeNamesKPI148(new ArrayList<>());
		fieldMappingDTO.setJiraIssueTypeNamesKPI151(new ArrayList<>());
		fieldMappingDTO.setJiraIssueTypeNamesKPI152(new ArrayList<>());
		fieldMappingDTO.setJiraIssueTypeNamesKPI161(new ArrayList<>());
		fieldMappingDTO.setJiraIssueWaitStateKPI170(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusCustomField(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI119(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI120(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI122(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI123(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI124(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI125(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI128(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI131(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI132(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI133(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI134(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI135(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI136(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI138(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI140(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI145(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI154(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI75(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKpi39(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKpi5(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKpi72(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionTypeCustomField(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI119(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI120(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI122(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI123(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI124(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI125(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI128(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI131(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI134(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI138(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI145(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI39(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI75(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKpi5(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKpi72(new ArrayList<>());
		fieldMappingDTO.setJiraItrQSIssueTypeKPI133(new ArrayList<>());
		fieldMappingDTO.setJiraKPI135StoryIdentification(new ArrayList<>());
		fieldMappingDTO.setJiraKPI82StoryIdentification(new ArrayList<>());
		fieldMappingDTO.setJiraLabelsKPI135(new ArrayList<>());
		fieldMappingDTO.setJiraLabelsKPI14(new ArrayList<>());
		fieldMappingDTO.setJiraLabelsKPI82(new ArrayList<>());
		fieldMappingDTO.setJiraLiveStatus("Jira Live Status");
		fieldMappingDTO.setJiraLiveStatusKPI127("Jira Live Status KPI127");
		fieldMappingDTO.setJiraLiveStatusKPI151("Jira Live Status KPI151");
		fieldMappingDTO.setJiraLiveStatusKPI152("Jira Live Status KPI152");
		fieldMappingDTO.setJiraLiveStatusKPI155("Jira Live Status KPI155");
		fieldMappingDTO.setJiraLiveStatusKPI171(new ArrayList<>());
		fieldMappingDTO.setJiraLiveStatusKPI3(new ArrayList<>());
		fieldMappingDTO.setJiraLiveStatusLTK("Jira Live Status LTK");
		fieldMappingDTO.setJiraLiveStatusNOPK("Jira Live Status NOPK");
		fieldMappingDTO.setJiraLiveStatusNORK("Jira Live Status NORK");
		fieldMappingDTO.setJiraLiveStatusNOSK("Jira Live Status NOSK");
		fieldMappingDTO.setJiraLiveStatusOTA("Jira Live Status OTA");
		fieldMappingDTO.setJiraOnHoldStatus(new ArrayList<>());
		fieldMappingDTO.setJiraOnHoldStatusKPI154(new ArrayList<>());
		fieldMappingDTO.setJiraProdIncidentRaisedByCustomField("Jira Prod Incident Raised By Custom Field");
		fieldMappingDTO.setJiraProdIncidentRaisedByValue(new ArrayList<>());
		fieldMappingDTO.setJiraProductionIncidentIdentification("Jira Production Incident Identification");
		fieldMappingDTO.setJiraProductiveStatus(new ArrayList<>());
		fieldMappingDTO.setJiraQADefectDensityIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraQADoneStatusKPI154(new ArrayList<>());
		fieldMappingDTO.setJiraQAKPI111IssueType(new ArrayList<>());
		fieldMappingDTO.setJiraReadyForRefinement(new ArrayList<>());
		fieldMappingDTO.setJiraReadyForRefinementKPI139(new ArrayList<>());
		fieldMappingDTO.setJiraRejectedInRefinement(new ArrayList<>());
		fieldMappingDTO.setJiraRejectedInRefinementKPI139(new ArrayList<>());
		fieldMappingDTO.setJiraSprintCapacityIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraSprintCapacityIssueTypeKpi46(new ArrayList<>());
		fieldMappingDTO.setJiraSprintVelocityIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraSprintVelocityIssueTypeKPI138(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForDevelopment(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForDevelopmentAVR(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForDevelopmentKPI135(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForDevelopmentKPI82(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForInProgress(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForInProgressKPI119(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForInProgressKPI122(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForInProgressKPI123(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForInProgressKPI125(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForInProgressKPI128(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForInProgressKPI145(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForInProgressKPI148(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForInProgressKPI154(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForInProgressKPI161(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForNotRefinedKPI161(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForQa(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForQaKPI135(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForQaKPI148(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForQaKPI82(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForRefinedKPI161(new ArrayList<>());
		fieldMappingDTO.setJiraStatusMappingCustomField("Jira Status Mapping Custom Field");
		fieldMappingDTO.setJiraStatusStartDevelopmentKPI154(new ArrayList<>());
		fieldMappingDTO.setJiraStoryIdentification(new ArrayList<>());
		fieldMappingDTO.setJiraStoryIdentificationKPI129(new ArrayList<>());
		fieldMappingDTO.setJiraStoryIdentificationKPI164(new ArrayList<>());
		fieldMappingDTO.setJiraStoryIdentificationKPI166(new ArrayList<>());
		fieldMappingDTO.setJiraStoryIdentificationKpi40(new ArrayList<>());
		fieldMappingDTO.setJiraStoryPointsCustomField("Jira Story Points Custom Field");
		fieldMappingDTO.setJiraSubTaskDefectType(new ArrayList<>());
		fieldMappingDTO.setJiraSubTaskIdentification(new ArrayList<>());
		fieldMappingDTO.setJiraTechDebtCustomField("Jira Tech Debt Custom Field");
		fieldMappingDTO.setJiraTechDebtIdentification("Jira Tech Debt Identification");
		fieldMappingDTO.setJiraTechDebtIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraTechDebtValue(new ArrayList<>());
		fieldMappingDTO.setJiraTestAutomationIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraTicketClosedStatus(new ArrayList<>());
		fieldMappingDTO.setJiraTicketRejectedStatus(new ArrayList<>());
		fieldMappingDTO.setJiraTicketResolvedStatus(new ArrayList<>());
		fieldMappingDTO.setJiraTicketTriagedStatus(new ArrayList<>());
		fieldMappingDTO.setJiraTicketVelocityIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraTicketWipStatus(new ArrayList<>());
		fieldMappingDTO.setJiraWaitStatus(new ArrayList<>());
		fieldMappingDTO.setJiraWaitStatusKPI131(new ArrayList<>());
		fieldMappingDTO.setJiradefecttype(new ArrayList<>());
		fieldMappingDTO.setKanbanCycleTimeIssueType(new ArrayList<>());
		fieldMappingDTO.setKanbanJiraTechDebtIssueType(new ArrayList<>());
		fieldMappingDTO.setKanbanRCACountIssueType(new ArrayList<>());
		fieldMappingDTO.setLeadTimeConfigRepoTool("Lead Time Config Repo Tool");
		fieldMappingDTO.setLinkDefectToStoryField(new String[]{"Link Defect To Story Field"});
		fieldMappingDTO.setNotificationEnabler(true);
		fieldMappingDTO.setPickNewATMJIRADetails(true);
		fieldMappingDTO.setPopulateByDevDoneKPI150(true);
		fieldMappingDTO.setProductionDefectComponentValue("42");
		fieldMappingDTO.setProductionDefectCustomField("Production Defect Custom Field");
		fieldMappingDTO.setProductionDefectIdentifier("42");
		fieldMappingDTO.setProductionDefectValue(new ArrayList<>());
		fieldMappingDTO.setProjectId("myproject");
		fieldMappingDTO.setProjectToolConfigId(ObjectId.get());
		fieldMappingDTO.setReadyForDevelopmentStatus("Ready For Development Status");
		fieldMappingDTO.setReadyForDevelopmentStatusKPI138(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejection(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejectionAVR(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejectionKPI133(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejectionKPI135(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejectionKPI14(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejectionKPI28(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejectionKPI35(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejectionKPI37(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejectionKPI82(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejectionQAKPI111(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejectionRCAKPI36(new ArrayList<>());
		fieldMappingDTO.setRootCause("Root Cause");
		fieldMappingDTO.setRootCauseValue(new ArrayList<>());
		fieldMappingDTO.setSprintName("Sprint Name");
		fieldMappingDTO.setSquadIdentMultiValue(new ArrayList<>());
		fieldMappingDTO.setSquadIdentSingleValue("42");
		fieldMappingDTO.setSquadIdentifier("42");
		fieldMappingDTO.setStartDateCountKPI150(3);
		fieldMappingDTO.setStoryFirstStatus("Story First Status");
		fieldMappingDTO.setStoryFirstStatusKPI148("Story First Status KPI148");
		fieldMappingDTO.setStoryFirstStatusKPI154(new ArrayList<>());
		fieldMappingDTO.setStoryFirstStatusKPI171("Story First Status KPI171");
		fieldMappingDTO.setStoryPointToHourMapping(10.0d);
		fieldMappingDTO.setTestingPhaseDefectComponentValue("42");
		fieldMappingDTO.setTestingPhaseDefectCustomField("Testing Phase Defect Custom Field");
		fieldMappingDTO.setTestingPhaseDefectValue(new ArrayList<>());
		fieldMappingDTO.setTestingPhaseDefectsIdentifier("42");
		fieldMappingDTO.setThresholdValueKPI11("42");
		fieldMappingDTO.setThresholdValueKPI111("42");
		fieldMappingDTO.setThresholdValueKPI113("42");
		fieldMappingDTO.setThresholdValueKPI116("42");
		fieldMappingDTO.setThresholdValueKPI118("42");
		fieldMappingDTO.setThresholdValueKPI126("42");
		fieldMappingDTO.setThresholdValueKPI127("42");
		fieldMappingDTO.setThresholdValueKPI139("42");
		fieldMappingDTO.setThresholdValueKPI14("42");
		fieldMappingDTO.setThresholdValueKPI149("42");
		fieldMappingDTO.setThresholdValueKPI153("42");
		fieldMappingDTO.setThresholdValueKPI156("42");
		fieldMappingDTO.setThresholdValueKPI157("42");
		fieldMappingDTO.setThresholdValueKPI158("42");
		fieldMappingDTO.setThresholdValueKPI159("42");
		fieldMappingDTO.setThresholdValueKPI16("42");
		fieldMappingDTO.setThresholdValueKPI160("42");
		fieldMappingDTO.setThresholdValueKPI162("42");
		fieldMappingDTO.setThresholdValueKPI164("42");
		fieldMappingDTO.setThresholdValueKPI166("42");
		fieldMappingDTO.setThresholdValueKPI168("42");
		fieldMappingDTO.setThresholdValueKPI17("42");
		fieldMappingDTO.setThresholdValueKPI170("42");
		fieldMappingDTO.setThresholdValueKPI27("42");
		fieldMappingDTO.setThresholdValueKPI28("42");
		fieldMappingDTO.setThresholdValueKPI3("42");
		fieldMappingDTO.setThresholdValueKPI34("42");
		fieldMappingDTO.setThresholdValueKPI35("42");
		fieldMappingDTO.setThresholdValueKPI36("42");
		fieldMappingDTO.setThresholdValueKPI37("42");
		fieldMappingDTO.setThresholdValueKPI38("42");
		fieldMappingDTO.setThresholdValueKPI39("42");
		fieldMappingDTO.setThresholdValueKPI40("42");
		fieldMappingDTO.setThresholdValueKPI42("42");
		fieldMappingDTO.setThresholdValueKPI46("42");
		fieldMappingDTO.setThresholdValueKPI5("42");
		fieldMappingDTO.setThresholdValueKPI62("42");
		fieldMappingDTO.setThresholdValueKPI64("42");
		fieldMappingDTO.setThresholdValueKPI65("42");
		fieldMappingDTO.setThresholdValueKPI67("42");
		fieldMappingDTO.setThresholdValueKPI70("42");
		fieldMappingDTO.setThresholdValueKPI72("42");
		fieldMappingDTO.setThresholdValueKPI73("42");
		fieldMappingDTO.setThresholdValueKPI8("42");
		fieldMappingDTO.setThresholdValueKPI82("42");
		fieldMappingDTO.setThresholdValueKPI84("42");
		fieldMappingDTO.setTicketCountIssueType(new ArrayList<>());
		fieldMappingDTO.setTicketDeliverdStatus(new ArrayList<>());
		fieldMappingDTO.setTicketReopenStatus(new ArrayList<>());
		fieldMappingDTO.setToBranchForMRKPI156("janedoe/featurebranch");
		fieldMappingDTO.setUploadData(true);
		fieldMappingDTO.setUploadDataKPI16(true);
		fieldMappingDTO.setUploadDataKPI42(true);
		fieldMappingDTO.setWorkingHoursDayCPT(10.0d);
		fieldMappingDTO.setJiraIssueTypeNames(null);
	}

	/**
	 * Method under test: {@link FieldMappingDTO#getLinkDefectToStoryField()}
	 */
	@Test
	public void testGetLinkDefectToStoryField() {
		assertNull((new FieldMappingDTO()).getLinkDefectToStoryField());
	}

	/**
	 * Method under test: {@link FieldMappingDTO#getLinkDefectToStoryField()}
	 */
	@Test
	public void testGetLinkDefectToStoryField2() {
		FieldMappingDTO fieldMappingDTO = new FieldMappingDTO();
		fieldMappingDTO.setAdditionalFilterConfig(new ArrayList<>());
		fieldMappingDTO.setAtmQueryEndpoint("https://config.us-east-2.amazonaws.com");
		fieldMappingDTO.setAtmSubprojectField("Atm Subproject Field");
		fieldMappingDTO.setBasicProjectConfigId(ObjectId.get());
		fieldMappingDTO.setDefectPriority(new ArrayList<>());
		fieldMappingDTO.setDefectPriorityKPI133(new ArrayList<>());
		fieldMappingDTO.setDefectPriorityKPI135(new ArrayList<>());
		fieldMappingDTO.setDefectPriorityKPI14(new ArrayList<>());
		fieldMappingDTO.setDefectPriorityKPI82(new ArrayList<>());
		fieldMappingDTO.setDefectPriorityQAKPI111(new ArrayList<>());
		fieldMappingDTO.setEpicAchievedValue("42");
		fieldMappingDTO.setEpicCostOfDelay("Epic Cost Of Delay");
		fieldMappingDTO.setEpicJobSize("Epic Job Size");
		fieldMappingDTO.setEpicLink("Epic Link");
		fieldMappingDTO.setEpicName("Epic Name");
		fieldMappingDTO.setEpicPlannedValue("42");
		fieldMappingDTO.setEpicRiskReduction("Epic Risk Reduction");
		fieldMappingDTO.setEpicTimeCriticality("Epic Time Criticality");
		fieldMappingDTO.setEpicUserBusinessValue("42");
		fieldMappingDTO.setEpicWsjf("Epic Wsjf");
		fieldMappingDTO.setEstimationCriteria("Estimation Criteria");
		fieldMappingDTO.setExcludeRCAFromFTPR(new ArrayList<>());
		fieldMappingDTO.setExcludeStatusKpi129(new ArrayList<>());
		fieldMappingDTO.setId(ObjectId.get());
		fieldMappingDTO.setIncludeRCAForKPI133(new ArrayList<>());
		fieldMappingDTO.setIncludeRCAForKPI135(new ArrayList<>());
		fieldMappingDTO.setIncludeRCAForKPI14(new ArrayList<>());
		fieldMappingDTO.setIncludeRCAForKPI82(new ArrayList<>());
		fieldMappingDTO.setIncludeRCAForQAKPI111(new ArrayList<>());
		fieldMappingDTO.setIssueStatusExcluMissingWork(new ArrayList<>());
		fieldMappingDTO.setIssueStatusExcluMissingWorkKPI124(new ArrayList<>());
		fieldMappingDTO.setIssueStatusToBeExcludedFromMissingWorklogs(new ArrayList<>());
		fieldMappingDTO.setJiraAcceptedInRefinement(new ArrayList<>());
		fieldMappingDTO.setJiraAcceptedInRefinementKPI139(new ArrayList<>());
		fieldMappingDTO.setJiraAtmProjectId("myproject");
		fieldMappingDTO.setJiraAtmProjectKey("Jira Atm Project Key");
		fieldMappingDTO.setJiraBlockedStatus(new ArrayList<>());
		fieldMappingDTO.setJiraBlockedStatusKPI131(new ArrayList<>());
		fieldMappingDTO.setJiraBugRaisedByCustomField("Jira Bug Raised By Custom Field");
		fieldMappingDTO.setJiraBugRaisedByIdentification("Jira Bug Raised By Identification");
		fieldMappingDTO.setJiraBugRaisedByQACustomField("Jira Bug Raised By QACustom Field");
		fieldMappingDTO.setJiraBugRaisedByQAIdentification("Jira Bug Raised By QAIdentification");
		fieldMappingDTO.setJiraBugRaisedByQAValue(new ArrayList<>());
		fieldMappingDTO.setJiraBugRaisedByValue(new ArrayList<>());
		fieldMappingDTO.setJiraCommitmentReliabilityIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraDefectClosedStatus(new ArrayList<>());
		fieldMappingDTO.setJiraDefectClosedStatusKPI137(new ArrayList<>());
		fieldMappingDTO.setJiraDefectCountlIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraDefectCountlIssueTypeKPI28(new ArrayList<>());
		fieldMappingDTO.setJiraDefectCountlIssueTypeKPI36(new ArrayList<>());
		fieldMappingDTO.setJiraDefectCreatedStatus("Jan 1, 2020 8:00am GMT+0100");
		fieldMappingDTO.setJiraDefectCreatedStatusKPI14("Jan 1, 2020 8:00am GMT+0100");
		fieldMappingDTO.setJiraDefectDroppedStatus(new ArrayList<>());
		fieldMappingDTO.setJiraDefectDroppedStatusKPI127(new ArrayList<>());
		fieldMappingDTO.setJiraDefectInjectionIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraDefectInjectionIssueTypeKPI14(new ArrayList<>());
		fieldMappingDTO.setJiraDefectRejectionStatus("Jira Defect Rejection Status");
		fieldMappingDTO.setJiraDefectRejectionStatusAVR("Jira Defect Rejection Status AVR");
		fieldMappingDTO.setJiraDefectRejectionStatusKPI133("Jira Defect Rejection Status KPI133");
		fieldMappingDTO.setJiraDefectRejectionStatusKPI135("Jira Defect Rejection Status KPI135");
		fieldMappingDTO.setJiraDefectRejectionStatusKPI14("Jira Defect Rejection Status KPI14");
		fieldMappingDTO.setJiraDefectRejectionStatusKPI151("Jira Defect Rejection Status KPI151");
		fieldMappingDTO.setJiraDefectRejectionStatusKPI152("Jira Defect Rejection Status KPI152");
		fieldMappingDTO.setJiraDefectRejectionStatusKPI155("Jira Defect Rejection Status KPI155");
		fieldMappingDTO.setJiraDefectRejectionStatusKPI28("Jira Defect Rejection Status KPI28");
		fieldMappingDTO.setJiraDefectRejectionStatusKPI35("Jira Defect Rejection Status KPI35");
		fieldMappingDTO.setJiraDefectRejectionStatusKPI37("Jira Defect Rejection Status KPI37");
		fieldMappingDTO.setJiraDefectRejectionStatusKPI82("Jira Defect Rejection Status KPI82");
		fieldMappingDTO.setJiraDefectRejectionStatusQAKPI111("Jira Defect Rejection Status QAKPI111");
		fieldMappingDTO.setJiraDefectRejectionStatusRCAKPI36("Jira Defect Rejection Status RCAKPI36");
		fieldMappingDTO.setJiraDefectRejectionlIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraDefectRemovalIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraDefectRemovalStatus(new ArrayList<>());
		fieldMappingDTO.setJiraDefectRemovalStatusKPI34(new ArrayList<>());
		fieldMappingDTO.setJiraDefectSeepageIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraDevDoneStatus(new ArrayList<>());
		fieldMappingDTO.setJiraDevDoneStatusKPI119(new ArrayList<>());
		fieldMappingDTO.setJiraDevDoneStatusKPI128(new ArrayList<>());
		fieldMappingDTO.setJiraDevDoneStatusKPI145(new ArrayList<>());
		fieldMappingDTO.setJiraDevDoneStatusKPI150(new ArrayList<>());
		fieldMappingDTO.setJiraDevDoneStatusKPI154(new ArrayList<>());
		fieldMappingDTO.setJiraDevDueDateCustomField("2020-03-01");
		fieldMappingDTO.setJiraDevDueDateField("2020-03-01");
		fieldMappingDTO.setJiraDod(new ArrayList<>());
		fieldMappingDTO.setJiraDodKPI127(new ArrayList<>());
		fieldMappingDTO.setJiraDodKPI14(new ArrayList<>());
		fieldMappingDTO.setJiraDodKPI151(new ArrayList<>());
		fieldMappingDTO.setJiraDodKPI152(new ArrayList<>());
		fieldMappingDTO.setJiraDodKPI155(new ArrayList<>());
		fieldMappingDTO.setJiraDodKPI156(new ArrayList<>());
		fieldMappingDTO.setJiraDodKPI163(new ArrayList<>());
		fieldMappingDTO.setJiraDodKPI166(new ArrayList<>());
		fieldMappingDTO.setJiraDodKPI171(new ArrayList<>());
		fieldMappingDTO.setJiraDodKPI37(new ArrayList<>());
		fieldMappingDTO.setJiraDodQAKPI111(new ArrayList<>());
		fieldMappingDTO.setJiraDor("Jira Dor");
		fieldMappingDTO.setJiraDorKPI171(new ArrayList<>());
		fieldMappingDTO.setJiraDorToLiveIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraDueDateCustomField("2020-03-01");
		fieldMappingDTO.setJiraDueDateField("2020-03-01");
		fieldMappingDTO.setJiraFTPRStoryIdentification(new ArrayList<>());
		fieldMappingDTO.setJiraFtprRejectStatus(new ArrayList<>());
		fieldMappingDTO.setJiraFtprRejectStatusKPI135(new ArrayList<>());
		fieldMappingDTO.setJiraFtprRejectStatusKPI82(new ArrayList<>());
		fieldMappingDTO.setJiraIncludeBlockedStatus("Jira Include Blocked Status");
		fieldMappingDTO.setJiraIncludeBlockedStatusKPI131("Jira Include Blocked Status KPI131");
		fieldMappingDTO.setJiraIntakeToDorIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraIssueClosedStateKPI170(new ArrayList<>());
		fieldMappingDTO.setJiraIssueDeliverdStatus(new ArrayList<>());
		fieldMappingDTO.setJiraIssueDeliverdStatusAVR(new ArrayList<>());
		fieldMappingDTO.setJiraIssueDeliverdStatusKPI126(new ArrayList<>());
		fieldMappingDTO.setJiraIssueDeliverdStatusKPI138(new ArrayList<>());
		fieldMappingDTO.setJiraIssueDeliverdStatusKPI82(new ArrayList<>());
		fieldMappingDTO.setJiraIssueEpicType(new ArrayList<>());
		fieldMappingDTO.setJiraIssueEpicTypeKPI153(new ArrayList<>());
		fieldMappingDTO.setJiraIssueTypeKPI156(new ArrayList<>());
		fieldMappingDTO.setJiraIssueTypeKPI171(new ArrayList<>());
		fieldMappingDTO.setJiraIssueTypeKPI3(new ArrayList<>());
		fieldMappingDTO.setJiraIssueTypeKPI35(new ArrayList<>());
		fieldMappingDTO.setJiraIssueTypeNames(new String[]{"Jira Issue Type Names"});
		fieldMappingDTO.setJiraIssueTypeNamesAVR(new String[]{"Jira Issue Type Names AVR"});
		fieldMappingDTO.setJiraIssueTypeNamesKPI146(new ArrayList<>());
		fieldMappingDTO.setJiraIssueTypeNamesKPI148(new ArrayList<>());
		fieldMappingDTO.setJiraIssueTypeNamesKPI151(new ArrayList<>());
		fieldMappingDTO.setJiraIssueTypeNamesKPI152(new ArrayList<>());
		fieldMappingDTO.setJiraIssueTypeNamesKPI161(new ArrayList<>());
		fieldMappingDTO.setJiraIssueWaitStateKPI170(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusCustomField(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI119(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI120(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI122(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI123(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI124(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI125(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI128(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI131(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI132(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI133(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI134(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI135(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI136(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI138(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI140(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI145(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI154(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI75(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKpi39(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKpi5(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKpi72(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionTypeCustomField(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI119(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI120(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI122(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI123(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI124(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI125(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI128(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI131(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI134(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI138(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI145(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI39(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI75(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKpi5(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKpi72(new ArrayList<>());
		fieldMappingDTO.setJiraItrQSIssueTypeKPI133(new ArrayList<>());
		fieldMappingDTO.setJiraKPI135StoryIdentification(new ArrayList<>());
		fieldMappingDTO.setJiraKPI82StoryIdentification(new ArrayList<>());
		fieldMappingDTO.setJiraLabelsKPI135(new ArrayList<>());
		fieldMappingDTO.setJiraLabelsKPI14(new ArrayList<>());
		fieldMappingDTO.setJiraLabelsKPI82(new ArrayList<>());
		fieldMappingDTO.setJiraLiveStatus("Jira Live Status");
		fieldMappingDTO.setJiraLiveStatusKPI127("Jira Live Status KPI127");
		fieldMappingDTO.setJiraLiveStatusKPI151("Jira Live Status KPI151");
		fieldMappingDTO.setJiraLiveStatusKPI152("Jira Live Status KPI152");
		fieldMappingDTO.setJiraLiveStatusKPI155("Jira Live Status KPI155");
		fieldMappingDTO.setJiraLiveStatusKPI171(new ArrayList<>());
		fieldMappingDTO.setJiraLiveStatusKPI3(new ArrayList<>());
		fieldMappingDTO.setJiraLiveStatusLTK("Jira Live Status LTK");
		fieldMappingDTO.setJiraLiveStatusNOPK("Jira Live Status NOPK");
		fieldMappingDTO.setJiraLiveStatusNORK("Jira Live Status NORK");
		fieldMappingDTO.setJiraLiveStatusNOSK("Jira Live Status NOSK");
		fieldMappingDTO.setJiraLiveStatusOTA("Jira Live Status OTA");
		fieldMappingDTO.setJiraOnHoldStatus(new ArrayList<>());
		fieldMappingDTO.setJiraOnHoldStatusKPI154(new ArrayList<>());
		fieldMappingDTO.setJiraProdIncidentRaisedByCustomField("Jira Prod Incident Raised By Custom Field");
		fieldMappingDTO.setJiraProdIncidentRaisedByValue(new ArrayList<>());
		fieldMappingDTO.setJiraProductionIncidentIdentification("Jira Production Incident Identification");
		fieldMappingDTO.setJiraProductiveStatus(new ArrayList<>());
		fieldMappingDTO.setJiraQADefectDensityIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraQADoneStatusKPI154(new ArrayList<>());
		fieldMappingDTO.setJiraQAKPI111IssueType(new ArrayList<>());
		fieldMappingDTO.setJiraReadyForRefinement(new ArrayList<>());
		fieldMappingDTO.setJiraReadyForRefinementKPI139(new ArrayList<>());
		fieldMappingDTO.setJiraRejectedInRefinement(new ArrayList<>());
		fieldMappingDTO.setJiraRejectedInRefinementKPI139(new ArrayList<>());
		fieldMappingDTO.setJiraSprintCapacityIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraSprintCapacityIssueTypeKpi46(new ArrayList<>());
		fieldMappingDTO.setJiraSprintVelocityIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraSprintVelocityIssueTypeKPI138(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForDevelopment(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForDevelopmentAVR(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForDevelopmentKPI135(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForDevelopmentKPI82(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForInProgress(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForInProgressKPI119(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForInProgressKPI122(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForInProgressKPI123(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForInProgressKPI125(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForInProgressKPI128(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForInProgressKPI145(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForInProgressKPI148(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForInProgressKPI154(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForInProgressKPI161(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForNotRefinedKPI161(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForQa(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForQaKPI135(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForQaKPI148(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForQaKPI82(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForRefinedKPI161(new ArrayList<>());
		fieldMappingDTO.setJiraStatusMappingCustomField("Jira Status Mapping Custom Field");
		fieldMappingDTO.setJiraStatusStartDevelopmentKPI154(new ArrayList<>());
		fieldMappingDTO.setJiraStoryIdentification(new ArrayList<>());
		fieldMappingDTO.setJiraStoryIdentificationKPI129(new ArrayList<>());
		fieldMappingDTO.setJiraStoryIdentificationKPI164(new ArrayList<>());
		fieldMappingDTO.setJiraStoryIdentificationKPI166(new ArrayList<>());
		fieldMappingDTO.setJiraStoryIdentificationKpi40(new ArrayList<>());
		fieldMappingDTO.setJiraStoryPointsCustomField("Jira Story Points Custom Field");
		fieldMappingDTO.setJiraSubTaskDefectType(new ArrayList<>());
		fieldMappingDTO.setJiraSubTaskIdentification(new ArrayList<>());
		fieldMappingDTO.setJiraTechDebtCustomField("Jira Tech Debt Custom Field");
		fieldMappingDTO.setJiraTechDebtIdentification("Jira Tech Debt Identification");
		fieldMappingDTO.setJiraTechDebtIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraTechDebtValue(new ArrayList<>());
		fieldMappingDTO.setJiraTestAutomationIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraTicketClosedStatus(new ArrayList<>());
		fieldMappingDTO.setJiraTicketRejectedStatus(new ArrayList<>());
		fieldMappingDTO.setJiraTicketResolvedStatus(new ArrayList<>());
		fieldMappingDTO.setJiraTicketTriagedStatus(new ArrayList<>());
		fieldMappingDTO.setJiraTicketVelocityIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraTicketWipStatus(new ArrayList<>());
		fieldMappingDTO.setJiraWaitStatus(new ArrayList<>());
		fieldMappingDTO.setJiraWaitStatusKPI131(new ArrayList<>());
		fieldMappingDTO.setJiradefecttype(new ArrayList<>());
		fieldMappingDTO.setKanbanCycleTimeIssueType(new ArrayList<>());
		fieldMappingDTO.setKanbanJiraTechDebtIssueType(new ArrayList<>());
		fieldMappingDTO.setKanbanRCACountIssueType(new ArrayList<>());
		fieldMappingDTO.setLeadTimeConfigRepoTool("Lead Time Config Repo Tool");
		fieldMappingDTO.setLinkDefectToStoryField(new String[]{"Link Defect To Story Field"});
		fieldMappingDTO.setNotificationEnabler(true);
		fieldMappingDTO.setPickNewATMJIRADetails(true);
		fieldMappingDTO.setPopulateByDevDoneKPI150(true);
		fieldMappingDTO.setProductionDefectComponentValue("42");
		fieldMappingDTO.setProductionDefectCustomField("Production Defect Custom Field");
		fieldMappingDTO.setProductionDefectIdentifier("42");
		fieldMappingDTO.setProductionDefectValue(new ArrayList<>());
		fieldMappingDTO.setProjectId("myproject");
		fieldMappingDTO.setProjectToolConfigId(ObjectId.get());
		fieldMappingDTO.setReadyForDevelopmentStatus("Ready For Development Status");
		fieldMappingDTO.setReadyForDevelopmentStatusKPI138(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejection(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejectionAVR(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejectionKPI133(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejectionKPI135(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejectionKPI14(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejectionKPI28(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejectionKPI35(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejectionKPI37(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejectionKPI82(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejectionQAKPI111(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejectionRCAKPI36(new ArrayList<>());
		fieldMappingDTO.setRootCause("Root Cause");
		fieldMappingDTO.setRootCauseValue(new ArrayList<>());
		fieldMappingDTO.setSprintName("Sprint Name");
		fieldMappingDTO.setSquadIdentMultiValue(new ArrayList<>());
		fieldMappingDTO.setSquadIdentSingleValue("42");
		fieldMappingDTO.setSquadIdentifier("42");
		fieldMappingDTO.setStartDateCountKPI150(3);
		fieldMappingDTO.setStoryFirstStatus("Story First Status");
		fieldMappingDTO.setStoryFirstStatusKPI148("Story First Status KPI148");
		fieldMappingDTO.setStoryFirstStatusKPI154(new ArrayList<>());
		fieldMappingDTO.setStoryFirstStatusKPI171("Story First Status KPI171");
		fieldMappingDTO.setStoryPointToHourMapping(10.0d);
		fieldMappingDTO.setTestingPhaseDefectComponentValue("42");
		fieldMappingDTO.setTestingPhaseDefectCustomField("Testing Phase Defect Custom Field");
		fieldMappingDTO.setTestingPhaseDefectValue(new ArrayList<>());
		fieldMappingDTO.setTestingPhaseDefectsIdentifier("42");
		fieldMappingDTO.setThresholdValueKPI11("42");
		fieldMappingDTO.setThresholdValueKPI111("42");
		fieldMappingDTO.setThresholdValueKPI113("42");
		fieldMappingDTO.setThresholdValueKPI116("42");
		fieldMappingDTO.setThresholdValueKPI118("42");
		fieldMappingDTO.setThresholdValueKPI126("42");
		fieldMappingDTO.setThresholdValueKPI127("42");
		fieldMappingDTO.setThresholdValueKPI139("42");
		fieldMappingDTO.setThresholdValueKPI14("42");
		fieldMappingDTO.setThresholdValueKPI149("42");
		fieldMappingDTO.setThresholdValueKPI153("42");
		fieldMappingDTO.setThresholdValueKPI156("42");
		fieldMappingDTO.setThresholdValueKPI157("42");
		fieldMappingDTO.setThresholdValueKPI158("42");
		fieldMappingDTO.setThresholdValueKPI159("42");
		fieldMappingDTO.setThresholdValueKPI16("42");
		fieldMappingDTO.setThresholdValueKPI160("42");
		fieldMappingDTO.setThresholdValueKPI162("42");
		fieldMappingDTO.setThresholdValueKPI164("42");
		fieldMappingDTO.setThresholdValueKPI166("42");
		fieldMappingDTO.setThresholdValueKPI168("42");
		fieldMappingDTO.setThresholdValueKPI17("42");
		fieldMappingDTO.setThresholdValueKPI170("42");
		fieldMappingDTO.setThresholdValueKPI27("42");
		fieldMappingDTO.setThresholdValueKPI28("42");
		fieldMappingDTO.setThresholdValueKPI3("42");
		fieldMappingDTO.setThresholdValueKPI34("42");
		fieldMappingDTO.setThresholdValueKPI35("42");
		fieldMappingDTO.setThresholdValueKPI36("42");
		fieldMappingDTO.setThresholdValueKPI37("42");
		fieldMappingDTO.setThresholdValueKPI38("42");
		fieldMappingDTO.setThresholdValueKPI39("42");
		fieldMappingDTO.setThresholdValueKPI40("42");
		fieldMappingDTO.setThresholdValueKPI42("42");
		fieldMappingDTO.setThresholdValueKPI46("42");
		fieldMappingDTO.setThresholdValueKPI5("42");
		fieldMappingDTO.setThresholdValueKPI62("42");
		fieldMappingDTO.setThresholdValueKPI64("42");
		fieldMappingDTO.setThresholdValueKPI65("42");
		fieldMappingDTO.setThresholdValueKPI67("42");
		fieldMappingDTO.setThresholdValueKPI70("42");
		fieldMappingDTO.setThresholdValueKPI72("42");
		fieldMappingDTO.setThresholdValueKPI73("42");
		fieldMappingDTO.setThresholdValueKPI8("42");
		fieldMappingDTO.setThresholdValueKPI82("42");
		fieldMappingDTO.setThresholdValueKPI84("42");
		fieldMappingDTO.setTicketCountIssueType(new ArrayList<>());
		fieldMappingDTO.setTicketDeliverdStatus(new ArrayList<>());
		fieldMappingDTO.setTicketReopenStatus(new ArrayList<>());
		fieldMappingDTO.setToBranchForMRKPI156("janedoe/featurebranch");
		fieldMappingDTO.setUploadData(true);
		fieldMappingDTO.setUploadDataKPI16(true);
		fieldMappingDTO.setUploadDataKPI42(true);
		fieldMappingDTO.setWorkingHoursDayCPT(10.0d);
		String[] actualLinkDefectToStoryField = fieldMappingDTO.getLinkDefectToStoryField();
		assertEquals(1, actualLinkDefectToStoryField.length);
		assertEquals("Link Defect To Story Field", actualLinkDefectToStoryField[0]);
	}

	/**
	 * Method under test:
	 * {@link FieldMappingDTO#setLinkDefectToStoryField(String[])}
	 */
	@Test
	public void testSetLinkDefectToStoryField() {
		// TODO: Complete this test.
		// Reason: R004 No meaningful assertions found.
		// Diffblue Cover was unable to create an assertion.
		// Make sure that fields modified by setLinkDefectToStoryField(String[])
		// have package-private, protected, or public getters.
		// See https://diff.blue/R004 to resolve this issue.

		(new FieldMappingDTO()).setLinkDefectToStoryField(new String[]{"Link Defect To Story Field"});
	}

	/**
	 * Method under test:
	 * {@link FieldMappingDTO#setLinkDefectToStoryField(String[])}
	 */
	@Test
	public void testSetLinkDefectToStoryField2() {
		// TODO: Complete this test.
		// Reason: R004 No meaningful assertions found.
		// Diffblue Cover was unable to create an assertion.
		// Make sure that fields modified by setLinkDefectToStoryField(String[])
		// have package-private, protected, or public getters.
		// See https://diff.blue/R004 to resolve this issue.

		FieldMappingDTO fieldMappingDTO = new FieldMappingDTO();
		fieldMappingDTO.setAdditionalFilterConfig(new ArrayList<>());
		fieldMappingDTO.setAtmQueryEndpoint("https://config.us-east-2.amazonaws.com");
		fieldMappingDTO.setAtmSubprojectField("Atm Subproject Field");
		fieldMappingDTO.setBasicProjectConfigId(ObjectId.get());
		fieldMappingDTO.setDefectPriority(new ArrayList<>());
		fieldMappingDTO.setDefectPriorityKPI133(new ArrayList<>());
		fieldMappingDTO.setDefectPriorityKPI135(new ArrayList<>());
		fieldMappingDTO.setDefectPriorityKPI14(new ArrayList<>());
		fieldMappingDTO.setDefectPriorityKPI82(new ArrayList<>());
		fieldMappingDTO.setDefectPriorityQAKPI111(new ArrayList<>());
		fieldMappingDTO.setEpicAchievedValue("42");
		fieldMappingDTO.setEpicCostOfDelay("Epic Cost Of Delay");
		fieldMappingDTO.setEpicJobSize("Epic Job Size");
		fieldMappingDTO.setEpicLink("Epic Link");
		fieldMappingDTO.setEpicName("Epic Name");
		fieldMappingDTO.setEpicPlannedValue("42");
		fieldMappingDTO.setEpicRiskReduction("Epic Risk Reduction");
		fieldMappingDTO.setEpicTimeCriticality("Epic Time Criticality");
		fieldMappingDTO.setEpicUserBusinessValue("42");
		fieldMappingDTO.setEpicWsjf("Epic Wsjf");
		fieldMappingDTO.setEstimationCriteria("Estimation Criteria");
		fieldMappingDTO.setExcludeRCAFromFTPR(new ArrayList<>());
		fieldMappingDTO.setExcludeStatusKpi129(new ArrayList<>());
		fieldMappingDTO.setId(ObjectId.get());
		fieldMappingDTO.setIncludeRCAForKPI133(new ArrayList<>());
		fieldMappingDTO.setIncludeRCAForKPI135(new ArrayList<>());
		fieldMappingDTO.setIncludeRCAForKPI14(new ArrayList<>());
		fieldMappingDTO.setIncludeRCAForKPI82(new ArrayList<>());
		fieldMappingDTO.setIncludeRCAForQAKPI111(new ArrayList<>());
		fieldMappingDTO.setIssueStatusExcluMissingWork(new ArrayList<>());
		fieldMappingDTO.setIssueStatusExcluMissingWorkKPI124(new ArrayList<>());
		fieldMappingDTO.setIssueStatusToBeExcludedFromMissingWorklogs(new ArrayList<>());
		fieldMappingDTO.setJiraAcceptedInRefinement(new ArrayList<>());
		fieldMappingDTO.setJiraAcceptedInRefinementKPI139(new ArrayList<>());
		fieldMappingDTO.setJiraAtmProjectId("myproject");
		fieldMappingDTO.setJiraAtmProjectKey("Jira Atm Project Key");
		fieldMappingDTO.setJiraBlockedStatus(new ArrayList<>());
		fieldMappingDTO.setJiraBlockedStatusKPI131(new ArrayList<>());
		fieldMappingDTO.setJiraBugRaisedByCustomField("Jira Bug Raised By Custom Field");
		fieldMappingDTO.setJiraBugRaisedByIdentification("Jira Bug Raised By Identification");
		fieldMappingDTO.setJiraBugRaisedByQACustomField("Jira Bug Raised By QACustom Field");
		fieldMappingDTO.setJiraBugRaisedByQAIdentification("Jira Bug Raised By QAIdentification");
		fieldMappingDTO.setJiraBugRaisedByQAValue(new ArrayList<>());
		fieldMappingDTO.setJiraBugRaisedByValue(new ArrayList<>());
		fieldMappingDTO.setJiraCommitmentReliabilityIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraDefectClosedStatus(new ArrayList<>());
		fieldMappingDTO.setJiraDefectClosedStatusKPI137(new ArrayList<>());
		fieldMappingDTO.setJiraDefectCountlIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraDefectCountlIssueTypeKPI28(new ArrayList<>());
		fieldMappingDTO.setJiraDefectCountlIssueTypeKPI36(new ArrayList<>());
		fieldMappingDTO.setJiraDefectCreatedStatus("Jan 1, 2020 8:00am GMT+0100");
		fieldMappingDTO.setJiraDefectCreatedStatusKPI14("Jan 1, 2020 8:00am GMT+0100");
		fieldMappingDTO.setJiraDefectDroppedStatus(new ArrayList<>());
		fieldMappingDTO.setJiraDefectDroppedStatusKPI127(new ArrayList<>());
		fieldMappingDTO.setJiraDefectInjectionIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraDefectInjectionIssueTypeKPI14(new ArrayList<>());
		fieldMappingDTO.setJiraDefectRejectionStatus("Jira Defect Rejection Status");
		fieldMappingDTO.setJiraDefectRejectionStatusAVR("Jira Defect Rejection Status AVR");
		fieldMappingDTO.setJiraDefectRejectionStatusKPI133("Jira Defect Rejection Status KPI133");
		fieldMappingDTO.setJiraDefectRejectionStatusKPI135("Jira Defect Rejection Status KPI135");
		fieldMappingDTO.setJiraDefectRejectionStatusKPI14("Jira Defect Rejection Status KPI14");
		fieldMappingDTO.setJiraDefectRejectionStatusKPI151("Jira Defect Rejection Status KPI151");
		fieldMappingDTO.setJiraDefectRejectionStatusKPI152("Jira Defect Rejection Status KPI152");
		fieldMappingDTO.setJiraDefectRejectionStatusKPI155("Jira Defect Rejection Status KPI155");
		fieldMappingDTO.setJiraDefectRejectionStatusKPI28("Jira Defect Rejection Status KPI28");
		fieldMappingDTO.setJiraDefectRejectionStatusKPI35("Jira Defect Rejection Status KPI35");
		fieldMappingDTO.setJiraDefectRejectionStatusKPI37("Jira Defect Rejection Status KPI37");
		fieldMappingDTO.setJiraDefectRejectionStatusKPI82("Jira Defect Rejection Status KPI82");
		fieldMappingDTO.setJiraDefectRejectionStatusQAKPI111("Jira Defect Rejection Status QAKPI111");
		fieldMappingDTO.setJiraDefectRejectionStatusRCAKPI36("Jira Defect Rejection Status RCAKPI36");
		fieldMappingDTO.setJiraDefectRejectionlIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraDefectRemovalIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraDefectRemovalStatus(new ArrayList<>());
		fieldMappingDTO.setJiraDefectRemovalStatusKPI34(new ArrayList<>());
		fieldMappingDTO.setJiraDefectSeepageIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraDevDoneStatus(new ArrayList<>());
		fieldMappingDTO.setJiraDevDoneStatusKPI119(new ArrayList<>());
		fieldMappingDTO.setJiraDevDoneStatusKPI128(new ArrayList<>());
		fieldMappingDTO.setJiraDevDoneStatusKPI145(new ArrayList<>());
		fieldMappingDTO.setJiraDevDoneStatusKPI150(new ArrayList<>());
		fieldMappingDTO.setJiraDevDoneStatusKPI154(new ArrayList<>());
		fieldMappingDTO.setJiraDevDueDateCustomField("2020-03-01");
		fieldMappingDTO.setJiraDevDueDateField("2020-03-01");
		fieldMappingDTO.setJiraDod(new ArrayList<>());
		fieldMappingDTO.setJiraDodKPI127(new ArrayList<>());
		fieldMappingDTO.setJiraDodKPI14(new ArrayList<>());
		fieldMappingDTO.setJiraDodKPI151(new ArrayList<>());
		fieldMappingDTO.setJiraDodKPI152(new ArrayList<>());
		fieldMappingDTO.setJiraDodKPI155(new ArrayList<>());
		fieldMappingDTO.setJiraDodKPI156(new ArrayList<>());
		fieldMappingDTO.setJiraDodKPI163(new ArrayList<>());
		fieldMappingDTO.setJiraDodKPI166(new ArrayList<>());
		fieldMappingDTO.setJiraDodKPI171(new ArrayList<>());
		fieldMappingDTO.setJiraDodKPI37(new ArrayList<>());
		fieldMappingDTO.setJiraDodQAKPI111(new ArrayList<>());
		fieldMappingDTO.setJiraDor("Jira Dor");
		fieldMappingDTO.setJiraDorKPI171(new ArrayList<>());
		fieldMappingDTO.setJiraDorToLiveIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraDueDateCustomField("2020-03-01");
		fieldMappingDTO.setJiraDueDateField("2020-03-01");
		fieldMappingDTO.setJiraFTPRStoryIdentification(new ArrayList<>());
		fieldMappingDTO.setJiraFtprRejectStatus(new ArrayList<>());
		fieldMappingDTO.setJiraFtprRejectStatusKPI135(new ArrayList<>());
		fieldMappingDTO.setJiraFtprRejectStatusKPI82(new ArrayList<>());
		fieldMappingDTO.setJiraIncludeBlockedStatus("Jira Include Blocked Status");
		fieldMappingDTO.setJiraIncludeBlockedStatusKPI131("Jira Include Blocked Status KPI131");
		fieldMappingDTO.setJiraIntakeToDorIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraIssueClosedStateKPI170(new ArrayList<>());
		fieldMappingDTO.setJiraIssueDeliverdStatus(new ArrayList<>());
		fieldMappingDTO.setJiraIssueDeliverdStatusAVR(new ArrayList<>());
		fieldMappingDTO.setJiraIssueDeliverdStatusKPI126(new ArrayList<>());
		fieldMappingDTO.setJiraIssueDeliverdStatusKPI138(new ArrayList<>());
		fieldMappingDTO.setJiraIssueDeliverdStatusKPI82(new ArrayList<>());
		fieldMappingDTO.setJiraIssueEpicType(new ArrayList<>());
		fieldMappingDTO.setJiraIssueEpicTypeKPI153(new ArrayList<>());
		fieldMappingDTO.setJiraIssueTypeKPI156(new ArrayList<>());
		fieldMappingDTO.setJiraIssueTypeKPI171(new ArrayList<>());
		fieldMappingDTO.setJiraIssueTypeKPI3(new ArrayList<>());
		fieldMappingDTO.setJiraIssueTypeKPI35(new ArrayList<>());
		fieldMappingDTO.setJiraIssueTypeNames(new String[]{"Jira Issue Type Names"});
		fieldMappingDTO.setJiraIssueTypeNamesAVR(new String[]{"Jira Issue Type Names AVR"});
		fieldMappingDTO.setJiraIssueTypeNamesKPI146(new ArrayList<>());
		fieldMappingDTO.setJiraIssueTypeNamesKPI148(new ArrayList<>());
		fieldMappingDTO.setJiraIssueTypeNamesKPI151(new ArrayList<>());
		fieldMappingDTO.setJiraIssueTypeNamesKPI152(new ArrayList<>());
		fieldMappingDTO.setJiraIssueTypeNamesKPI161(new ArrayList<>());
		fieldMappingDTO.setJiraIssueWaitStateKPI170(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusCustomField(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI119(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI120(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI122(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI123(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI124(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI125(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI128(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI131(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI132(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI133(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI134(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI135(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI136(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI138(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI140(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI145(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI154(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKPI75(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKpi39(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKpi5(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionStatusKpi72(new ArrayList<>());
		fieldMappingDTO.setJiraIterationCompletionTypeCustomField(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI119(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI120(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI122(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI123(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI124(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI125(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI128(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI131(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI134(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI138(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI145(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI39(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKPI75(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKpi5(new ArrayList<>());
		fieldMappingDTO.setJiraIterationIssuetypeKpi72(new ArrayList<>());
		fieldMappingDTO.setJiraItrQSIssueTypeKPI133(new ArrayList<>());
		fieldMappingDTO.setJiraKPI135StoryIdentification(new ArrayList<>());
		fieldMappingDTO.setJiraKPI82StoryIdentification(new ArrayList<>());
		fieldMappingDTO.setJiraLabelsKPI135(new ArrayList<>());
		fieldMappingDTO.setJiraLabelsKPI14(new ArrayList<>());
		fieldMappingDTO.setJiraLabelsKPI82(new ArrayList<>());
		fieldMappingDTO.setJiraLiveStatus("Jira Live Status");
		fieldMappingDTO.setJiraLiveStatusKPI127("Jira Live Status KPI127");
		fieldMappingDTO.setJiraLiveStatusKPI151("Jira Live Status KPI151");
		fieldMappingDTO.setJiraLiveStatusKPI152("Jira Live Status KPI152");
		fieldMappingDTO.setJiraLiveStatusKPI155("Jira Live Status KPI155");
		fieldMappingDTO.setJiraLiveStatusKPI171(new ArrayList<>());
		fieldMappingDTO.setJiraLiveStatusKPI3(new ArrayList<>());
		fieldMappingDTO.setJiraLiveStatusLTK("Jira Live Status LTK");
		fieldMappingDTO.setJiraLiveStatusNOPK("Jira Live Status NOPK");
		fieldMappingDTO.setJiraLiveStatusNORK("Jira Live Status NORK");
		fieldMappingDTO.setJiraLiveStatusNOSK("Jira Live Status NOSK");
		fieldMappingDTO.setJiraLiveStatusOTA("Jira Live Status OTA");
		fieldMappingDTO.setJiraOnHoldStatus(new ArrayList<>());
		fieldMappingDTO.setJiraOnHoldStatusKPI154(new ArrayList<>());
		fieldMappingDTO.setJiraProdIncidentRaisedByCustomField("Jira Prod Incident Raised By Custom Field");
		fieldMappingDTO.setJiraProdIncidentRaisedByValue(new ArrayList<>());
		fieldMappingDTO.setJiraProductionIncidentIdentification("Jira Production Incident Identification");
		fieldMappingDTO.setJiraProductiveStatus(new ArrayList<>());
		fieldMappingDTO.setJiraQADefectDensityIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraQADoneStatusKPI154(new ArrayList<>());
		fieldMappingDTO.setJiraQAKPI111IssueType(new ArrayList<>());
		fieldMappingDTO.setJiraReadyForRefinement(new ArrayList<>());
		fieldMappingDTO.setJiraReadyForRefinementKPI139(new ArrayList<>());
		fieldMappingDTO.setJiraRejectedInRefinement(new ArrayList<>());
		fieldMappingDTO.setJiraRejectedInRefinementKPI139(new ArrayList<>());
		fieldMappingDTO.setJiraSprintCapacityIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraSprintCapacityIssueTypeKpi46(new ArrayList<>());
		fieldMappingDTO.setJiraSprintVelocityIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraSprintVelocityIssueTypeKPI138(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForDevelopment(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForDevelopmentAVR(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForDevelopmentKPI135(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForDevelopmentKPI82(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForInProgress(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForInProgressKPI119(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForInProgressKPI122(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForInProgressKPI123(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForInProgressKPI125(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForInProgressKPI128(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForInProgressKPI145(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForInProgressKPI148(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForInProgressKPI154(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForInProgressKPI161(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForNotRefinedKPI161(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForQa(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForQaKPI135(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForQaKPI148(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForQaKPI82(new ArrayList<>());
		fieldMappingDTO.setJiraStatusForRefinedKPI161(new ArrayList<>());
		fieldMappingDTO.setJiraStatusMappingCustomField("Jira Status Mapping Custom Field");
		fieldMappingDTO.setJiraStatusStartDevelopmentKPI154(new ArrayList<>());
		fieldMappingDTO.setJiraStoryIdentification(new ArrayList<>());
		fieldMappingDTO.setJiraStoryIdentificationKPI129(new ArrayList<>());
		fieldMappingDTO.setJiraStoryIdentificationKPI164(new ArrayList<>());
		fieldMappingDTO.setJiraStoryIdentificationKPI166(new ArrayList<>());
		fieldMappingDTO.setJiraStoryIdentificationKpi40(new ArrayList<>());
		fieldMappingDTO.setJiraStoryPointsCustomField("Jira Story Points Custom Field");
		fieldMappingDTO.setJiraSubTaskDefectType(new ArrayList<>());
		fieldMappingDTO.setJiraSubTaskIdentification(new ArrayList<>());
		fieldMappingDTO.setJiraTechDebtCustomField("Jira Tech Debt Custom Field");
		fieldMappingDTO.setJiraTechDebtIdentification("Jira Tech Debt Identification");
		fieldMappingDTO.setJiraTechDebtIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraTechDebtValue(new ArrayList<>());
		fieldMappingDTO.setJiraTestAutomationIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraTicketClosedStatus(new ArrayList<>());
		fieldMappingDTO.setJiraTicketRejectedStatus(new ArrayList<>());
		fieldMappingDTO.setJiraTicketResolvedStatus(new ArrayList<>());
		fieldMappingDTO.setJiraTicketTriagedStatus(new ArrayList<>());
		fieldMappingDTO.setJiraTicketVelocityIssueType(new ArrayList<>());
		fieldMappingDTO.setJiraTicketWipStatus(new ArrayList<>());
		fieldMappingDTO.setJiraWaitStatus(new ArrayList<>());
		fieldMappingDTO.setJiraWaitStatusKPI131(new ArrayList<>());
		fieldMappingDTO.setJiradefecttype(new ArrayList<>());
		fieldMappingDTO.setKanbanCycleTimeIssueType(new ArrayList<>());
		fieldMappingDTO.setKanbanJiraTechDebtIssueType(new ArrayList<>());
		fieldMappingDTO.setKanbanRCACountIssueType(new ArrayList<>());
		fieldMappingDTO.setLeadTimeConfigRepoTool("Lead Time Config Repo Tool");
		fieldMappingDTO.setLinkDefectToStoryField(new String[]{"Link Defect To Story Field"});
		fieldMappingDTO.setNotificationEnabler(true);
		fieldMappingDTO.setPickNewATMJIRADetails(true);
		fieldMappingDTO.setPopulateByDevDoneKPI150(true);
		fieldMappingDTO.setProductionDefectComponentValue("42");
		fieldMappingDTO.setProductionDefectCustomField("Production Defect Custom Field");
		fieldMappingDTO.setProductionDefectIdentifier("42");
		fieldMappingDTO.setProductionDefectValue(new ArrayList<>());
		fieldMappingDTO.setProjectId("myproject");
		fieldMappingDTO.setProjectToolConfigId(ObjectId.get());
		fieldMappingDTO.setReadyForDevelopmentStatus("Ready For Development Status");
		fieldMappingDTO.setReadyForDevelopmentStatusKPI138(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejection(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejectionAVR(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejectionKPI133(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejectionKPI135(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejectionKPI14(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejectionKPI28(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejectionKPI35(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejectionKPI37(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejectionKPI82(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejectionQAKPI111(new ArrayList<>());
		fieldMappingDTO.setResolutionTypeForRejectionRCAKPI36(new ArrayList<>());
		fieldMappingDTO.setRootCause("Root Cause");
		fieldMappingDTO.setRootCauseValue(new ArrayList<>());
		fieldMappingDTO.setSprintName("Sprint Name");
		fieldMappingDTO.setSquadIdentMultiValue(new ArrayList<>());
		fieldMappingDTO.setSquadIdentSingleValue("42");
		fieldMappingDTO.setSquadIdentifier("42");
		fieldMappingDTO.setStartDateCountKPI150(3);
		fieldMappingDTO.setStoryFirstStatus("Story First Status");
		fieldMappingDTO.setStoryFirstStatusKPI148("Story First Status KPI148");
		fieldMappingDTO.setStoryFirstStatusKPI154(new ArrayList<>());
		fieldMappingDTO.setStoryFirstStatusKPI171("Story First Status KPI171");
		fieldMappingDTO.setStoryPointToHourMapping(10.0d);
		fieldMappingDTO.setTestingPhaseDefectComponentValue("42");
		fieldMappingDTO.setTestingPhaseDefectCustomField("Testing Phase Defect Custom Field");
		fieldMappingDTO.setTestingPhaseDefectValue(new ArrayList<>());
		fieldMappingDTO.setTestingPhaseDefectsIdentifier("42");
		fieldMappingDTO.setThresholdValueKPI11("42");
		fieldMappingDTO.setThresholdValueKPI111("42");
		fieldMappingDTO.setThresholdValueKPI113("42");
		fieldMappingDTO.setThresholdValueKPI116("42");
		fieldMappingDTO.setThresholdValueKPI118("42");
		fieldMappingDTO.setThresholdValueKPI126("42");
		fieldMappingDTO.setThresholdValueKPI127("42");
		fieldMappingDTO.setThresholdValueKPI139("42");
		fieldMappingDTO.setThresholdValueKPI14("42");
		fieldMappingDTO.setThresholdValueKPI149("42");
		fieldMappingDTO.setThresholdValueKPI153("42");
		fieldMappingDTO.setThresholdValueKPI156("42");
		fieldMappingDTO.setThresholdValueKPI157("42");
		fieldMappingDTO.setThresholdValueKPI158("42");
		fieldMappingDTO.setThresholdValueKPI159("42");
		fieldMappingDTO.setThresholdValueKPI16("42");
		fieldMappingDTO.setThresholdValueKPI160("42");
		fieldMappingDTO.setThresholdValueKPI162("42");
		fieldMappingDTO.setThresholdValueKPI164("42");
		fieldMappingDTO.setThresholdValueKPI166("42");
		fieldMappingDTO.setThresholdValueKPI168("42");
		fieldMappingDTO.setThresholdValueKPI17("42");
		fieldMappingDTO.setThresholdValueKPI170("42");
		fieldMappingDTO.setThresholdValueKPI27("42");
		fieldMappingDTO.setThresholdValueKPI28("42");
		fieldMappingDTO.setThresholdValueKPI3("42");
		fieldMappingDTO.setThresholdValueKPI34("42");
		fieldMappingDTO.setThresholdValueKPI35("42");
		fieldMappingDTO.setThresholdValueKPI36("42");
		fieldMappingDTO.setThresholdValueKPI37("42");
		fieldMappingDTO.setThresholdValueKPI38("42");
		fieldMappingDTO.setThresholdValueKPI39("42");
		fieldMappingDTO.setThresholdValueKPI40("42");
		fieldMappingDTO.setThresholdValueKPI42("42");
		fieldMappingDTO.setThresholdValueKPI46("42");
		fieldMappingDTO.setThresholdValueKPI5("42");
		fieldMappingDTO.setThresholdValueKPI62("42");
		fieldMappingDTO.setThresholdValueKPI64("42");
		fieldMappingDTO.setThresholdValueKPI65("42");
		fieldMappingDTO.setThresholdValueKPI67("42");
		fieldMappingDTO.setThresholdValueKPI70("42");
		fieldMappingDTO.setThresholdValueKPI72("42");
		fieldMappingDTO.setThresholdValueKPI73("42");
		fieldMappingDTO.setThresholdValueKPI8("42");
		fieldMappingDTO.setThresholdValueKPI82("42");
		fieldMappingDTO.setThresholdValueKPI84("42");
		fieldMappingDTO.setTicketCountIssueType(new ArrayList<>());
		fieldMappingDTO.setTicketDeliverdStatus(new ArrayList<>());
		fieldMappingDTO.setTicketReopenStatus(new ArrayList<>());
		fieldMappingDTO.setToBranchForMRKPI156("janedoe/featurebranch");
		fieldMappingDTO.setUploadData(true);
		fieldMappingDTO.setUploadDataKPI16(true);
		fieldMappingDTO.setUploadDataKPI42(true);
		fieldMappingDTO.setWorkingHoursDayCPT(10.0d);
		fieldMappingDTO.setLinkDefectToStoryField(null);
	}

	/**
	 * Method under test: default or parameterless constructor of
	 * {@link FieldMappingDTO}
	 */
	@Test
	public void testConstructor() {
		FieldMappingDTO actualFieldMappingDTO = new FieldMappingDTO();
		assertNull(actualFieldMappingDTO.getAdditionalFilterConfig());
		assertNull(actualFieldMappingDTO.getJiraDefectCountlIssueTypeKPI28());
		assertNull(actualFieldMappingDTO.getJiraDefectCountlIssueType());
		assertNull(actualFieldMappingDTO.getJiraDefectClosedStatusKPI137());
		assertNull(actualFieldMappingDTO.getJiraDefectClosedStatus());
		assertNull(actualFieldMappingDTO.getJiraCommitmentReliabilityIssueType());
		assertNull(actualFieldMappingDTO.getJiraBugRaisedByValue());
		assertNull(actualFieldMappingDTO.getJiraBugRaisedByQAValue());
		assertNull(actualFieldMappingDTO.getJiraBugRaisedByQAIdentification());
		assertNull(actualFieldMappingDTO.getJiraBugRaisedByQACustomField());
		assertNull(actualFieldMappingDTO.getJiraBugRaisedByIdentification());
		assertNull(actualFieldMappingDTO.getJiraBugRaisedByCustomField());
		assertNull(actualFieldMappingDTO.getJiraBlockedStatusKPI131());
		assertNull(actualFieldMappingDTO.getJiraBlockedStatus());
		assertNull(actualFieldMappingDTO.getJiraAtmProjectKey());
		assertNull(actualFieldMappingDTO.getJiraAtmProjectId());
		assertNull(actualFieldMappingDTO.getJiraAcceptedInRefinementKPI139());
		assertNull(actualFieldMappingDTO.getJiraAcceptedInRefinement());
		assertNull(actualFieldMappingDTO.getIssueStatusToBeExcludedFromMissingWorklogs());
		assertNull(actualFieldMappingDTO.getIssueStatusExcluMissingWorkKPI124());
		assertNull(actualFieldMappingDTO.getIssueStatusExcluMissingWork());
		assertNull(actualFieldMappingDTO.getIncludeRCAForQAKPI111());
		assertNull(actualFieldMappingDTO.getIncludeRCAForKPI82());
		assertNull(actualFieldMappingDTO.getIncludeRCAForKPI14());
		assertNull(actualFieldMappingDTO.getIncludeRCAForKPI135());
		assertNull(actualFieldMappingDTO.getIncludeRCAForKPI133());
		assertNull(actualFieldMappingDTO.getId());
		assertNull(actualFieldMappingDTO.getExcludeStatusKpi129());
		assertNull(actualFieldMappingDTO.getExcludeRCAFromFTPR());
		assertEquals("Story Point", actualFieldMappingDTO.getEstimationCriteria());
		assertNull(actualFieldMappingDTO.getEpicWsjf());
		assertNull(actualFieldMappingDTO.getEpicUserBusinessValue());
		assertNull(actualFieldMappingDTO.getEpicTimeCriticality());
		assertNull(actualFieldMappingDTO.getEpicRiskReduction());
		assertNull(actualFieldMappingDTO.getEpicPlannedValue());
		assertNull(actualFieldMappingDTO.getEpicName());
		assertNull(actualFieldMappingDTO.getEpicLink());
		assertNull(actualFieldMappingDTO.getEpicJobSize());
		assertNull(actualFieldMappingDTO.getEpicCostOfDelay());
		assertNull(actualFieldMappingDTO.getEpicAchievedValue());
		assertNull(actualFieldMappingDTO.getDefectPriorityQAKPI111());
		assertNull(actualFieldMappingDTO.getDefectPriorityKPI82());
		assertNull(actualFieldMappingDTO.getDefectPriorityKPI14());
		assertNull(actualFieldMappingDTO.getDefectPriorityKPI135());
		assertNull(actualFieldMappingDTO.getDefectPriorityKPI133());
		assertNull(actualFieldMappingDTO.getDefectPriority());
		assertNull(actualFieldMappingDTO.getBasicProjectConfigId());
		assertNull(actualFieldMappingDTO.getAtmSubprojectField());
		assertNull(actualFieldMappingDTO.getAtmQueryEndpoint());
	}
}
