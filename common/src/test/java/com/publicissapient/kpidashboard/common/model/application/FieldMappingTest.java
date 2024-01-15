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

package com.publicissapient.kpidashboard.common.model.application;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

public class FieldMappingTest {
	// Field projectToolConfigId of type ObjectId - was not mocked since Mockito
	// doesn't mock a Final class when 'mock-maker-inline' option is not set
	// Field basicProjectConfigId of type ObjectId - was not mocked since Mockito
	// doesn't mock a Final class when 'mock-maker-inline' option is not set
	@Mock
	List<String> jiradefecttype;
	@Mock
	List<String> jiraSubTaskDefectType;
	@Mock
	List<String> defectPriority;
	@Mock
	List<String> defectPriorityKPI135;
	@Mock
	List<String> defectPriorityKPI14;
	@Mock
	List<String> defectPriorityQAKPI111;
	@Mock
	List<String> defectPriorityKPI82;
	@Mock
	List<String> defectPriorityKPI133;
	@Mock
	List<String> jiraIssueEpicType;
	@Mock
	List<String> jiraStatusForDevelopment;
	@Mock
	List<String> jiraStatusForDevelopmentAVR;
	@Mock
	List<String> jiraStatusForDevelopmentKPI82;
	@Mock
	List<String> jiraStatusForDevelopmentKPI135;
	@Mock
	List<String> jiraStatusForQa;
	@Mock
	List<String> jiraStatusForQaKPI148;
	@Mock
	List<String> jiraStatusForQaKPI135;
	@Mock
	List<String> jiraStatusForQaKPI82;
	@Mock
	List<String> jiraDefectInjectionIssueType;
	@Mock
	List<String> jiraDefectInjectionIssueTypeKPI14;
	@Mock
	List<String> jiraDod;
	@Mock
	List<String> jiraDodKPI152;
	@Mock
	List<String> jiraDodKPI151;
	@Mock
	List<String> jiraDodKPI14;
	@Mock
	List<String> jiraDodQAKPI111;
	@Mock
	List<String> jiraDodKPI127;
	@Mock
	List<String> jiraDodKPI37;
	@Mock
	List<String> jiraTechDebtIssueType;
	@Mock
	List<String> jiraTechDebtValue;
	@Mock
	List<String> jiraBugRaisedByValue;
	@Mock
	List<String> jiraDefectSeepageIssueType;
	@Mock
	List<String> jiraIssueTypeKPI35;
	@Mock
	List<String> jiraDefectRemovalStatus;
	@Mock
	List<String> jiraDefectRemovalStatusKPI34;
	@Mock
	List<String> jiraDefectRemovalIssueType;
	@Mock
	List<String> jiraDefectClosedStatus;
	@Mock
	List<String> jiraDefectClosedStatusKPI137;
	@Mock
	List<String> jiraTestAutomationIssueType;
	@Mock
	List<String> jiraSprintVelocityIssueType;
	@Mock
	List<String> jiraSprintVelocityIssueTypeKPI138;
	@Mock
	List<String> jiraSprintCapacityIssueType;
	@Mock
	List<String> jiraSprintCapacityIssueTypeKpi46;
	@Mock
	List<String> jiraDefectRejectionlIssueType;
	@Mock
	List<String> jiraDefectCountlIssueType;
	@Mock
	List<String> jiraDefectCountlIssueTypeKPI28;
	@Mock
	List<String> jiraDefectCountlIssueTypeKPI36;
	@Mock
	List<String> jiraIssueDeliverdStatus;
	@Mock
	List<String> jiraIssueDeliverdStatusKPI138;
	@Mock
	List<String> jiraIssueDeliverdStatusAVR;
	@Mock
	List<String> jiraIssueDeliverdStatusKPI126;
	@Mock
	List<String> jiraIssueDeliverdStatusKPI82;
	@Mock
	List<String> readyForDevelopmentStatusKPI138;
	@Mock
	List<String> jiraIntakeToDorIssueType;
	@Mock
	List<String> jiraIssueTypeKPI3;
	@Mock
	List<String> jiraStoryIdentification;
	@Mock
	List<String> jiraStoryIdentificationKPI129;
	@Mock
	List<String> jiraStoryIdentificationKpi40;
	@Mock
	List<String> jiraStoryIdentificationKPI164;
	@Mock
	List<String> jiraLiveStatusKPI3;
	@Mock
	List<String> ticketCountIssueType;
	@Mock
	List<String> kanbanRCACountIssueType;
	@Mock
	List<String> jiraTicketVelocityIssueType;
	@Mock
	List<String> ticketDeliverdStatus;
	@Mock
	List<String> ticketReopenStatus;
	@Mock
	List<String> kanbanJiraTechDebtIssueType;
	@Mock
	List<String> jiraTicketResolvedStatus;
	@Mock
	List<String> jiraTicketClosedStatus;
	@Mock
	List<String> kanbanCycleTimeIssueType;
	@Mock
	List<String> jiraTicketTriagedStatus;
	@Mock
	List<String> jiraTicketWipStatus;
	@Mock
	List<String> jiraTicketRejectedStatus;
	@Mock
	List<String> excludeStatusKpi129;
	@Mock
	List<String> rootCauseValue;
	@Mock
	List<String> excludeRCAFromFTPR;
	@Mock
	List<String> includeRCAForKPI82;
	@Mock
	List<String> includeRCAForKPI135;
	@Mock
	List<String> includeRCAForKPI14;
	@Mock
	List<String> includeRCAForQAKPI111;
	@Mock
	List<String> includeRCAForKPI133;
	@Mock
	List<String> jiraDorToLiveIssueType;
	@Mock
	List<String> jiraProductiveStatus;
	@Mock
	List<String> jiraCommitmentReliabilityIssueType;
	@Mock
	List<String> resolutionTypeForRejection;
	@Mock
	List<String> resolutionTypeForRejectionAVR;
	@Mock
	List<String> resolutionTypeForRejectionKPI28;
	@Mock
	List<String> resolutionTypeForRejectionKPI37;
	@Mock
	List<String> resolutionTypeForRejectionKPI35;
	@Mock
	List<String> resolutionTypeForRejectionKPI82;
	@Mock
	List<String> resolutionTypeForRejectionKPI135;
	@Mock
	List<String> resolutionTypeForRejectionKPI133;
	@Mock
	List<String> resolutionTypeForRejectionRCAKPI36;
	@Mock
	List<String> resolutionTypeForRejectionKPI14;
	@Mock
	List<String> resolutionTypeForRejectionQAKPI111;
	@Mock
	List<String> jiraQADefectDensityIssueType;
	@Mock
	List<String> jiraQAKPI111IssueType;
	@Mock
	List<String> jiraItrQSIssueTypeKPI133;
	@Mock
	List<String> jiraBugRaisedByQAValue;
	@Mock
	List<String> jiraDefectDroppedStatus;
	@Mock
	List<String> jiraDefectDroppedStatusKPI127;
	@Mock
	List<String> squadIdentMultiValue;
	@Mock
	List<String> productionDefectValue;
	@Mock
	List<String> jiraStatusForInProgress;
	@Mock
	List<String> jiraStatusForInProgressKPI148;
	@Mock
	List<String> jiraStatusForInProgressKPI122;
	@Mock
	List<String> jiraStatusForInProgressKPI145;
	@Mock
	List<String> jiraStatusForInProgressKPI125;
	@Mock
	List<String> jiraStatusForInProgressKPI128;
	@Mock
	List<String> jiraStatusForInProgressKPI123;
	@Mock
	List<String> jiraStatusForInProgressKPI119;
	@Mock
	List<AdditionalFilterConfig> additionalFilterConfig;
	@Mock
	List<String> issueStatusExcluMissingWork;
	@Mock
	List<String> issueStatusExcluMissingWorkKPI124;
	@Mock
	List<String> jiraOnHoldStatus;
	@Mock
	List<String> jiraFTPRStoryIdentification;
	@Mock
	List<String> jiraKPI82StoryIdentification;
	@Mock
	List<String> jiraKPI135StoryIdentification;
	@Mock
	List<String> jiraWaitStatus;
	@Mock
	List<String> jiraWaitStatusKPI131;
	@Mock
	List<String> jiraBlockedStatus;
	@Mock
	List<String> jiraBlockedStatusKPI131;
	@Mock
	List<String> jiraDevDoneStatus;
	@Mock
	List<String> jiraDevDoneStatusKPI119;
	@Mock
	List<String> jiraDevDoneStatusKPI145;
	@Mock
	List<String> jiraDevDoneStatusKPI128;
	@Mock
	List<String> jiraRejectedInRefinement;
	@Mock
	List<String> jiraRejectedInRefinementKPI139;
	@Mock
	List<String> jiraAcceptedInRefinement;
	@Mock
	List<String> jiraAcceptedInRefinementKPI139;
	@Mock
	List<String> jiraReadyForRefinement;
	@Mock
	List<String> jiraReadyForRefinementKPI139;
	@Mock
	List<String> jiraFtprRejectStatus;
	@Mock
	List<String> jiraFtprRejectStatusKPI135;
	@Mock
	List<String> jiraFtprRejectStatusKPI82;
	@Mock
	List<String> jiraIterationCompletionStatusCustomField;
	@Mock
	List<String> jiraIterationCompletionStatusKPI135;
	@Mock
	List<String> jiraIterationCompletionStatusKPI122;
	@Mock
	List<String> jiraIterationCompletionStatusKPI75;
	@Mock
	List<String> jiraIterationCompletionStatusKPI145;
	@Mock
	List<String> jiraIterationCompletionStatusKPI140;
	@Mock
	List<String> jiraIterationCompletionStatusKPI132;
	@Mock
	List<String> jiraIterationCompletionStatusKPI136;
	@Mock
	List<String> jiraIterationCompletionStatusKpi72;
	@Mock
	List<String> jiraIterationCompletionStatusKpi39;
	@Mock
	List<String> jiraIterationCompletionStatusKpi5;
	@Mock
	List<String> jiraIterationCompletionStatusKPI124;
	@Mock
	List<String> jiraIterationCompletionStatusKPI123;
	@Mock
	List<String> jiraIterationCompletionStatusKPI125;
	@Mock
	List<String> jiraIterationCompletionStatusKPI120;
	@Mock
	List<String> jiraIterationCompletionStatusKPI128;
	@Mock
	List<String> jiraIterationCompletionStatusKPI134;
	@Mock
	List<String> jiraIterationCompletionStatusKPI133;
	@Mock
	List<String> jiraIterationCompletionStatusKPI119;
	@Mock
	List<String> jiraIterationCompletionStatusKPI131;
	@Mock
	List<String> jiraIterationCompletionStatusKPI138;
	@Mock
	List<String> jiraIterationCompletionTypeCustomField;
	@Mock
	List<String> jiraIterationIssuetypeKPI122;
	@Mock
	List<String> jiraIterationIssuetypeKPI138;
	@Mock
	List<String> jiraIterationIssuetypeKPI131;
	@Mock
	List<String> jiraIterationIssuetypeKPI128;
	@Mock
	List<String> jiraIterationIssuetypeKPI134;
	@Mock
	List<String> jiraIterationIssuetypeKPI145;
	@Mock
	List<String> jiraIterationIssuetypeKpi72;
	@Mock
	List<String> jiraIterationIssuetypeKPI119;
	@Mock
	List<String> jiraIterationIssuetypeKpi5;
	@Mock
	List<String> jiraIterationIssuetypeKPI75;
	@Mock
	List<String> jiraIterationIssuetypeKPI123;
	@Mock
	List<String> jiraIterationIssuetypeKPI125;
	@Mock
	List<String> jiraIterationIssuetypeKPI120;
	@Mock
	List<String> jiraIterationIssuetypeKPI124;
	@Mock
	List<String> jiraIterationIssuetypeKPI39;
	// Field createdDate of type LocalDateTime - was not mocked since Mockito
	// doesn't mock a Final class when 'mock-maker-inline' option is not set
	@Mock
	List<String> jiraDodKPI155;
	@Mock
	List<String> jiraIssueEpicTypeKPI153;
	@Mock
	List<String> testingPhaseDefectValue;
	@Mock
	List<String> jiraDodKPI163;
	@Mock
	List<String> jiraIssueTypeNamesKPI161;
	@Mock
	List<String> jiraIssueTypeNamesKPI146;
	@Mock
	List<String> jiraIssueTypeNamesKPI148;
	@Mock
	List<String> jiraIssueTypeNamesKPI151;
	@Mock
	List<String> jiraIssueTypeNamesKPI152;
	@Mock
	List<String> jiraDodKPI156;
	@Mock
	List<String> jiraIssueTypeKPI156;
	@Mock
	List<String> jiraLabelsKPI14;
	@Mock
	List<String> jiraLabelsKPI82;
	@Mock
	List<String> jiraIssueWaitStateKPI170;
	@Mock
	List<String> jiraIssueClosedStateKPI170;
	@Mock
	List<String> jiraDevDoneStatusKPI150;
	@Mock
	List<String> jiraProdIncidentRaisedByValue;
	@Mock
	List<String> jiraStoryIdentificationKPI166;
	@Mock
	List<String> jiraDodKPI166;
	@Mock
	List<String> storyFirstStatusKPI154;
	@Mock
	List<String> jiraStatusForInProgressKPI154;
	@Mock
	List<String> jiraDevDoneStatusKPI154;
	@Mock
	List<String> jiraQADoneStatusKPI154;
	@Mock
	List<String> jiraOnHoldStatusKPI154;
	@Mock
	List<String> jiraIterationCompletionStatusKPI154;
	@Mock
	List<String> jiraSubTaskIdentification;
	@Mock
	List<String> jiraStatusStartDevelopmentKPI154;
	@Mock
	List<String> jiraLabelsKPI135;
	@Mock
	List<String> jiraStatusForInProgressKPI161;
	@Mock
	List<String> jiraStatusForRefinedKPI161;
	@Mock
	List<String> jiraStatusForNotRefinedKPI161;
	@Mock
	List<String> jiraIssueTypeKPI171;
	@Mock
	List<String> jiraDodKPI171;
	@Mock
	List<String> jiraDorKPI171;
	@Mock
	List<String> jiraLiveStatusKPI171;
	// Field id of type ObjectId - was not mocked since Mockito doesn't mock a Final
	// class when 'mock-maker-inline' option is not set
	@InjectMocks
	FieldMapping fieldMapping;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testGetJiraIssueTypeNames() throws Exception {
		String[] result = fieldMapping.getJiraIssueTypeNames();
		Assert.assertNull(result);
	}

	@Test
	public void testSetJiraIssueTypeNames() throws Exception {
		fieldMapping.setJiraIssueTypeNames(new String[] { "jiraIssueTypeNames" });
	}

	@Test
	public void testSetJiraStatusForInProgressKPI119() throws Exception {
		fieldMapping.setJiraStatusForInProgressKPI119(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetProjectToolConfigId() throws Exception {
		fieldMapping.setProjectToolConfigId(null);
	}

	@Test
	public void testSetBasicProjectConfigId() throws Exception {
		fieldMapping.setBasicProjectConfigId(null);
	}

	@Test
	public void testSetProjectId() throws Exception {
		fieldMapping.setProjectId("projectId");
	}

	@Test
	public void testSetSprintName() throws Exception {
		fieldMapping.setSprintName("sprintName");
	}

	@Test
	public void testSetEpicName() throws Exception {
		fieldMapping.setEpicName("epicName");
	}

	@Test
	public void testSetJiradefecttype() throws Exception {
		fieldMapping.setJiradefecttype(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetEpicLink() throws Exception {
		fieldMapping.setEpicLink("epicLink");
	}

	@Test
	public void testSetJiraSubTaskDefectType() throws Exception {
		fieldMapping.setJiraSubTaskDefectType(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetDefectPriority() throws Exception {
		fieldMapping.setDefectPriority(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetDefectPriorityKPI135() throws Exception {
		fieldMapping.setDefectPriorityKPI135(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetDefectPriorityKPI14() throws Exception {
		fieldMapping.setDefectPriorityKPI14(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetDefectPriorityQAKPI111() throws Exception {
		fieldMapping.setDefectPriorityQAKPI111(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetDefectPriorityKPI82() throws Exception {
		fieldMapping.setDefectPriorityKPI82(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetDefectPriorityKPI133() throws Exception {
		fieldMapping.setDefectPriorityKPI133(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIssueTypeNamesAVR() throws Exception {
		fieldMapping.setJiraIssueTypeNamesAVR(new String[] { "jiraIssueTypeNamesAVR" });
	}

	@Test
	public void testSetJiraIssueEpicType() throws Exception {
		fieldMapping.setJiraIssueEpicType(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetStoryFirstStatus() throws Exception {
		fieldMapping.setStoryFirstStatus("storyFirstStatus");
	}

	@Test
	public void testSetStoryFirstStatusKPI148() throws Exception {
		fieldMapping.setStoryFirstStatusKPI148("storyFirstStatusKPI148");
	}

	@Test
	public void testSetRootCause() throws Exception {
		fieldMapping.setRootCause("rootCause");
	}

	@Test
	public void testSetJiraStatusForDevelopment() throws Exception {
		fieldMapping.setJiraStatusForDevelopment(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraStatusForDevelopmentAVR() throws Exception {
		fieldMapping.setJiraStatusForDevelopmentAVR(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraStatusForDevelopmentKPI82() throws Exception {
		fieldMapping.setJiraStatusForDevelopmentKPI82(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraStatusForDevelopmentKPI135() throws Exception {
		fieldMapping.setJiraStatusForDevelopmentKPI135(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraStatusForQa() throws Exception {
		fieldMapping.setJiraStatusForQa(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraStatusForQaKPI148() throws Exception {
		fieldMapping.setJiraStatusForQaKPI148(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraStatusForQaKPI135() throws Exception {
		fieldMapping.setJiraStatusForQaKPI135(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraStatusForQaKPI82() throws Exception {
		fieldMapping.setJiraStatusForQaKPI82(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraDefectInjectionIssueType() throws Exception {
		fieldMapping.setJiraDefectInjectionIssueType(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraDefectInjectionIssueTypeKPI14() throws Exception {
		fieldMapping.setJiraDefectInjectionIssueTypeKPI14(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraDod() throws Exception {
		fieldMapping.setJiraDod(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraDodKPI152() throws Exception {
		fieldMapping.setJiraDodKPI152(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraDodKPI151() throws Exception {
		fieldMapping.setJiraDodKPI151(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraDodKPI14() throws Exception {
		fieldMapping.setJiraDodKPI14(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraDodQAKPI111() throws Exception {
		fieldMapping.setJiraDodQAKPI111(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraDodKPI127() throws Exception {
		fieldMapping.setJiraDodKPI127(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraDodKPI37() throws Exception {
		fieldMapping.setJiraDodKPI37(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraDefectCreatedStatus() throws Exception {
		fieldMapping.setJiraDefectCreatedStatus("jiraDefectCreatedStatus");
	}

	@Test
	public void testSetJiraDefectCreatedStatusKPI14() throws Exception {
		fieldMapping.setJiraDefectCreatedStatusKPI14("jiraDefectCreatedStatusKPI14");
	}

	@Test
	public void testSetJiraTechDebtIssueType() throws Exception {
		fieldMapping.setJiraTechDebtIssueType(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraTechDebtIdentification() throws Exception {
		fieldMapping.setJiraTechDebtIdentification("jiraTechDebtIdentification");
	}

	@Test
	public void testSetJiraTechDebtCustomField() throws Exception {
		fieldMapping.setJiraTechDebtCustomField("jiraTechDebtCustomField");
	}

	@Test
	public void testSetJiraTechDebtValue() throws Exception {
		fieldMapping.setJiraTechDebtValue(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraDefectRejectionStatus() throws Exception {
		fieldMapping.setJiraDefectRejectionStatus("jiraDefectRejectionStatus");
	}

	@Test
	public void testSetJiraDefectRejectionStatusKPI152() throws Exception {
		fieldMapping.setJiraDefectRejectionStatusKPI152("jiraDefectRejectionStatusKPI152");
	}

	@Test
	public void testSetJiraDefectRejectionStatusKPI151() throws Exception {
		fieldMapping.setJiraDefectRejectionStatusKPI151("jiraDefectRejectionStatusKPI151");
	}

	@Test
	public void testSetJiraDefectRejectionStatusAVR() throws Exception {
		fieldMapping.setJiraDefectRejectionStatusAVR("jiraDefectRejectionStatusAVR");
	}

	@Test
	public void testSetJiraDefectRejectionStatusKPI28() throws Exception {
		fieldMapping.setJiraDefectRejectionStatusKPI28("jiraDefectRejectionStatusKPI28");
	}

	@Test
	public void testSetJiraDefectRejectionStatusKPI37() throws Exception {
		fieldMapping.setJiraDefectRejectionStatusKPI37("jiraDefectRejectionStatusKPI37");
	}

	@Test
	public void testSetJiraDefectRejectionStatusKPI35() throws Exception {
		fieldMapping.setJiraDefectRejectionStatusKPI35("jiraDefectRejectionStatusKPI35");
	}

	@Test
	public void testSetJiraDefectRejectionStatusKPI82() throws Exception {
		fieldMapping.setJiraDefectRejectionStatusKPI82("jiraDefectRejectionStatusKPI82");
	}

	@Test
	public void testSetJiraDefectRejectionStatusKPI135() throws Exception {
		fieldMapping.setJiraDefectRejectionStatusKPI135("jiraDefectRejectionStatusKPI135");
	}

	@Test
	public void testSetJiraDefectRejectionStatusKPI133() throws Exception {
		fieldMapping.setJiraDefectRejectionStatusKPI133("jiraDefectRejectionStatusKPI133");
	}

	@Test
	public void testSetJiraDefectRejectionStatusRCAKPI36() throws Exception {
		fieldMapping.setJiraDefectRejectionStatusRCAKPI36("jiraDefectRejectionStatusRCAKPI36");
	}

	@Test
	public void testSetJiraDefectRejectionStatusKPI14() throws Exception {
		fieldMapping.setJiraDefectRejectionStatusKPI14("jiraDefectRejectionStatusKPI14");
	}

	@Test
	public void testSetJiraDefectRejectionStatusQAKPI111() throws Exception {
		fieldMapping.setJiraDefectRejectionStatusQAKPI111("jiraDefectRejectionStatusQAKPI111");
	}

	@Test
	public void testSetJiraBugRaisedByIdentification() throws Exception {
		fieldMapping.setJiraBugRaisedByIdentification("jiraBugRaisedByIdentification");
	}

	@Test
	public void testSetJiraBugRaisedByValue() throws Exception {
		fieldMapping.setJiraBugRaisedByValue(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraBugRaisedByCustomField() throws Exception {
		fieldMapping.setJiraBugRaisedByCustomField("jiraBugRaisedByCustomField");
	}

	@Test
	public void testSetJiraDefectSeepageIssueType() throws Exception {
		fieldMapping.setJiraDefectSeepageIssueType(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIssueTypeKPI35() throws Exception {
		fieldMapping.setJiraIssueTypeKPI35(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraDefectRemovalStatus() throws Exception {
		fieldMapping.setJiraDefectRemovalStatus(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraDefectRemovalStatusKPI34() throws Exception {
		fieldMapping.setJiraDefectRemovalStatusKPI34(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraDefectRemovalIssueType() throws Exception {
		fieldMapping.setJiraDefectRemovalIssueType(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraDefectClosedStatus() throws Exception {
		fieldMapping.setJiraDefectClosedStatus(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraDefectClosedStatusKPI137() throws Exception {
		fieldMapping.setJiraDefectClosedStatusKPI137(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraStoryPointsCustomField() throws Exception {
		fieldMapping.setJiraStoryPointsCustomField("jiraStoryPointsCustomField");
	}

	@Test
	public void testSetJiraTestAutomationIssueType() throws Exception {
		fieldMapping.setJiraTestAutomationIssueType(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraSprintVelocityIssueType() throws Exception {
		fieldMapping.setJiraSprintVelocityIssueType(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraSprintVelocityIssueTypeKPI138() throws Exception {
		fieldMapping.setJiraSprintVelocityIssueTypeKPI138(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraSprintCapacityIssueType() throws Exception {
		fieldMapping.setJiraSprintCapacityIssueType(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraSprintCapacityIssueTypeKpi46() throws Exception {
		fieldMapping.setJiraSprintCapacityIssueTypeKpi46(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraDefectRejectionlIssueType() throws Exception {
		fieldMapping.setJiraDefectRejectionlIssueType(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraDefectCountlIssueType() throws Exception {
		fieldMapping.setJiraDefectCountlIssueType(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraDefectCountlIssueTypeKPI28() throws Exception {
		fieldMapping.setJiraDefectCountlIssueTypeKPI28(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraDefectCountlIssueTypeKPI36() throws Exception {
		fieldMapping.setJiraDefectCountlIssueTypeKPI36(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIssueDeliverdStatus() throws Exception {
		fieldMapping.setJiraIssueDeliverdStatus(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIssueDeliverdStatusKPI138() throws Exception {
		fieldMapping.setJiraIssueDeliverdStatusKPI138(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIssueDeliverdStatusAVR() throws Exception {
		fieldMapping.setJiraIssueDeliverdStatusAVR(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIssueDeliverdStatusKPI126() throws Exception {
		fieldMapping.setJiraIssueDeliverdStatusKPI126(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIssueDeliverdStatusKPI82() throws Exception {
		fieldMapping.setJiraIssueDeliverdStatusKPI82(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetReadyForDevelopmentStatus() throws Exception {
		fieldMapping.setReadyForDevelopmentStatus("readyForDevelopmentStatus");
	}

	@Test
	public void testSetReadyForDevelopmentStatusKPI138() throws Exception {
		fieldMapping.setReadyForDevelopmentStatusKPI138(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraDor() throws Exception {
		fieldMapping.setJiraDor("jiraDor");
	}

	@Test
	public void testSetJiraIntakeToDorIssueType() throws Exception {
		fieldMapping.setJiraIntakeToDorIssueType(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIssueTypeKPI3() throws Exception {
		fieldMapping.setJiraIssueTypeKPI3(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraStoryIdentification() throws Exception {
		fieldMapping.setJiraStoryIdentification(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraStoryIdentificationKPI129() throws Exception {
		fieldMapping.setJiraStoryIdentificationKPI129(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraStoryIdentificationKpi40() throws Exception {
		fieldMapping.setJiraStoryIdentificationKpi40(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraStoryIdentificationKPI164() throws Exception {
		fieldMapping.setJiraStoryIdentificationKPI164(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraLiveStatus() throws Exception {
		fieldMapping.setJiraLiveStatus("jiraLiveStatus");
	}

	@Test
	public void testSetJiraLiveStatusKPI152() throws Exception {
		fieldMapping.setJiraLiveStatusKPI152("jiraLiveStatusKPI152");
	}

	@Test
	public void testSetJiraLiveStatusKPI151() throws Exception {
		fieldMapping.setJiraLiveStatusKPI151("jiraLiveStatusKPI151");
	}

	@Test
	public void testSetJiraLiveStatusKPI3() throws Exception {
		fieldMapping.setJiraLiveStatusKPI3(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraLiveStatusLTK() throws Exception {
		fieldMapping.setJiraLiveStatusLTK("jiraLiveStatusLTK");
	}

	@Test
	public void testSetJiraLiveStatusNOPK() throws Exception {
		fieldMapping.setJiraLiveStatusNOPK("jiraLiveStatusNOPK");
	}

	@Test
	public void testSetJiraLiveStatusNOSK() throws Exception {
		fieldMapping.setJiraLiveStatusNOSK("jiraLiveStatusNOSK");
	}

	@Test
	public void testSetJiraLiveStatusNORK() throws Exception {
		fieldMapping.setJiraLiveStatusNORK("jiraLiveStatusNORK");
	}

	@Test
	public void testSetJiraLiveStatusOTA() throws Exception {
		fieldMapping.setJiraLiveStatusOTA("jiraLiveStatusOTA");
	}

	@Test
	public void testSetJiraLiveStatusKPI127() throws Exception {
		fieldMapping.setJiraLiveStatusKPI127("jiraLiveStatusKPI127");
	}

	@Test
	public void testSetTicketCountIssueType() throws Exception {
		fieldMapping.setTicketCountIssueType(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetKanbanRCACountIssueType() throws Exception {
		fieldMapping.setKanbanRCACountIssueType(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraTicketVelocityIssueType() throws Exception {
		fieldMapping.setJiraTicketVelocityIssueType(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetTicketDeliverdStatus() throws Exception {
		fieldMapping.setTicketDeliverdStatus(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetTicketReopenStatus() throws Exception {
		fieldMapping.setTicketReopenStatus(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetKanbanJiraTechDebtIssueType() throws Exception {
		fieldMapping.setKanbanJiraTechDebtIssueType(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraTicketResolvedStatus() throws Exception {
		fieldMapping.setJiraTicketResolvedStatus(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraTicketClosedStatus() throws Exception {
		fieldMapping.setJiraTicketClosedStatus(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetKanbanCycleTimeIssueType() throws Exception {
		fieldMapping.setKanbanCycleTimeIssueType(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraTicketTriagedStatus() throws Exception {
		fieldMapping.setJiraTicketTriagedStatus(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraTicketWipStatus() throws Exception {
		fieldMapping.setJiraTicketWipStatus(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraTicketRejectedStatus() throws Exception {
		fieldMapping.setJiraTicketRejectedStatus(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetExcludeStatusKpi129() throws Exception {
		fieldMapping.setExcludeStatusKpi129(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraStatusMappingCustomField() throws Exception {
		fieldMapping.setJiraStatusMappingCustomField("jiraStatusMappingCustomField");
	}

	@Test
	public void testSetRootCauseValue() throws Exception {
		fieldMapping.setRootCauseValue(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetExcludeRCAFromFTPR() throws Exception {
		fieldMapping.setExcludeRCAFromFTPR(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetIncludeRCAForKPI82() throws Exception {
		fieldMapping.setIncludeRCAForKPI82(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetIncludeRCAForKPI135() throws Exception {
		fieldMapping.setIncludeRCAForKPI135(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetIncludeRCAForKPI14() throws Exception {
		fieldMapping.setIncludeRCAForKPI14(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetIncludeRCAForQAKPI111() throws Exception {
		fieldMapping.setIncludeRCAForQAKPI111(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetIncludeRCAForKPI133() throws Exception {
		fieldMapping.setIncludeRCAForKPI133(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraDorToLiveIssueType() throws Exception {
		fieldMapping.setJiraDorToLiveIssueType(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraProductiveStatus() throws Exception {
		fieldMapping.setJiraProductiveStatus(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraCommitmentReliabilityIssueType() throws Exception {
		fieldMapping.setJiraCommitmentReliabilityIssueType(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetResolutionTypeForRejection() throws Exception {
		fieldMapping.setResolutionTypeForRejection(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetResolutionTypeForRejectionAVR() throws Exception {
		fieldMapping.setResolutionTypeForRejectionAVR(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetResolutionTypeForRejectionKPI28() throws Exception {
		fieldMapping.setResolutionTypeForRejectionKPI28(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetResolutionTypeForRejectionKPI37() throws Exception {
		fieldMapping.setResolutionTypeForRejectionKPI37(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetResolutionTypeForRejectionKPI35() throws Exception {
		fieldMapping.setResolutionTypeForRejectionKPI35(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetResolutionTypeForRejectionKPI82() throws Exception {
		fieldMapping.setResolutionTypeForRejectionKPI82(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetResolutionTypeForRejectionKPI135() throws Exception {
		fieldMapping.setResolutionTypeForRejectionKPI135(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetResolutionTypeForRejectionKPI133() throws Exception {
		fieldMapping.setResolutionTypeForRejectionKPI133(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetResolutionTypeForRejectionRCAKPI36() throws Exception {
		fieldMapping.setResolutionTypeForRejectionRCAKPI36(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetResolutionTypeForRejectionKPI14() throws Exception {
		fieldMapping.setResolutionTypeForRejectionKPI14(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetResolutionTypeForRejectionQAKPI111() throws Exception {
		fieldMapping.setResolutionTypeForRejectionQAKPI111(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraQADefectDensityIssueType() throws Exception {
		fieldMapping.setJiraQADefectDensityIssueType(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraQAKPI111IssueType() throws Exception {
		fieldMapping.setJiraQAKPI111IssueType(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraItrQSIssueTypeKPI133() throws Exception {
		fieldMapping.setJiraItrQSIssueTypeKPI133(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraBugRaisedByQACustomField() throws Exception {
		fieldMapping.setJiraBugRaisedByQACustomField("jiraBugRaisedByQACustomField");
	}

	@Test
	public void testSetJiraBugRaisedByQAIdentification() throws Exception {
		fieldMapping.setJiraBugRaisedByQAIdentification("jiraBugRaisedByQAIdentification");
	}

	@Test
	public void testSetJiraBugRaisedByQAValue() throws Exception {
		fieldMapping.setJiraBugRaisedByQAValue(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraDefectDroppedStatus() throws Exception {
		fieldMapping.setJiraDefectDroppedStatus(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraDefectDroppedStatusKPI127() throws Exception {
		fieldMapping.setJiraDefectDroppedStatusKPI127(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetEpicCostOfDelay() throws Exception {
		fieldMapping.setEpicCostOfDelay("epicCostOfDelay");
	}

	@Test
	public void testSetEpicRiskReduction() throws Exception {
		fieldMapping.setEpicRiskReduction("epicRiskReduction");
	}

	@Test
	public void testSetEpicUserBusinessValue() throws Exception {
		fieldMapping.setEpicUserBusinessValue("epicUserBusinessValue");
	}

	@Test
	public void testSetEpicWsjf() throws Exception {
		fieldMapping.setEpicWsjf("epicWsjf");
	}

	@Test
	public void testSetEpicTimeCriticality() throws Exception {
		fieldMapping.setEpicTimeCriticality("epicTimeCriticality");
	}

	@Test
	public void testSetEpicJobSize() throws Exception {
		fieldMapping.setEpicJobSize("epicJobSize");
	}

	@Test
	public void testSetEpicPlannedValue() throws Exception {
		fieldMapping.setEpicPlannedValue("epicPlannedValue");
	}

	@Test
	public void testSetEpicAchievedValue() throws Exception {
		fieldMapping.setEpicAchievedValue("epicAchievedValue");
	}

	@Test
	public void testSetSquadIdentifier() throws Exception {
		fieldMapping.setSquadIdentifier("squadIdentifier");
	}

	@Test
	public void testSetSquadIdentMultiValue() throws Exception {
		fieldMapping.setSquadIdentMultiValue(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetSquadIdentSingleValue() throws Exception {
		fieldMapping.setSquadIdentSingleValue("squadIdentSingleValue");
	}

	@Test
	public void testSetProductionDefectCustomField() throws Exception {
		fieldMapping.setProductionDefectCustomField("productionDefectCustomField");
	}

	@Test
	public void testSetProductionDefectIdentifier() throws Exception {
		fieldMapping.setProductionDefectIdentifier("productionDefectIdentifier");
	}

	@Test
	public void testSetProductionDefectValue() throws Exception {
		fieldMapping.setProductionDefectValue(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetProductionDefectComponentValue() throws Exception {
		fieldMapping.setProductionDefectComponentValue("productionDefectComponentValue");
	}

	@Test
	public void testSetJiraStatusForInProgress() throws Exception {
		fieldMapping.setJiraStatusForInProgress(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraStatusForInProgressKPI148() throws Exception {
		fieldMapping.setJiraStatusForInProgressKPI148(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraStatusForInProgressKPI122() throws Exception {
		fieldMapping.setJiraStatusForInProgressKPI122(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraStatusForInProgressKPI145() throws Exception {
		fieldMapping.setJiraStatusForInProgressKPI145(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraStatusForInProgressKPI125() throws Exception {
		fieldMapping.setJiraStatusForInProgressKPI125(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraStatusForInProgressKPI128() throws Exception {
		fieldMapping.setJiraStatusForInProgressKPI128(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraStatusForInProgressKPI123() throws Exception {
		fieldMapping.setJiraStatusForInProgressKPI123(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetEstimationCriteria() throws Exception {
		fieldMapping.setEstimationCriteria("estimationCriteria");
	}

	@Test
	public void testSetStoryPointToHourMapping() throws Exception {
		fieldMapping.setStoryPointToHourMapping(Double.valueOf(0));
	}

	@Test
	public void testSetWorkingHoursDayCPT() throws Exception {
		fieldMapping.setWorkingHoursDayCPT(Double.valueOf(0));
	}

	@Test
	public void testSetAdditionalFilterConfig() throws Exception {
		fieldMapping.setAdditionalFilterConfig(Arrays.<AdditionalFilterConfig>asList(new AdditionalFilterConfig()));
	}

	@Test
	public void testSetIssueStatusExcluMissingWork() throws Exception {
		fieldMapping.setIssueStatusExcluMissingWork(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetIssueStatusExcluMissingWorkKPI124() throws Exception {
		fieldMapping.setIssueStatusExcluMissingWorkKPI124(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraOnHoldStatus() throws Exception {
		fieldMapping.setJiraOnHoldStatus(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraFTPRStoryIdentification() throws Exception {
		fieldMapping.setJiraFTPRStoryIdentification(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraKPI82StoryIdentification() throws Exception {
		fieldMapping.setJiraKPI82StoryIdentification(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraKPI135StoryIdentification() throws Exception {
		fieldMapping.setJiraKPI135StoryIdentification(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraWaitStatus() throws Exception {
		fieldMapping.setJiraWaitStatus(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraWaitStatusKPI131() throws Exception {
		fieldMapping.setJiraWaitStatusKPI131(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraBlockedStatus() throws Exception {
		fieldMapping.setJiraBlockedStatus(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraBlockedStatusKPI131() throws Exception {
		fieldMapping.setJiraBlockedStatusKPI131(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIncludeBlockedStatus() throws Exception {
		fieldMapping.setJiraIncludeBlockedStatus("jiraIncludeBlockedStatus");
	}

	@Test
	public void testSetJiraIncludeBlockedStatusKPI131() throws Exception {
		fieldMapping.setJiraIncludeBlockedStatusKPI131("jiraIncludeBlockedStatusKPI131");
	}

	@Test
	public void testSetJiraDueDateField() throws Exception {
		fieldMapping.setJiraDueDateField("jiraDueDateField");
	}

	@Test
	public void testSetJiraDueDateCustomField() throws Exception {
		fieldMapping.setJiraDueDateCustomField("jiraDueDateCustomField");
	}

	@Test
	public void testSetJiraDevDueDateField() throws Exception {
		fieldMapping.setJiraDevDueDateField("jiraDevDueDateField");
	}

	@Test
	public void testSetJiraDevDueDateCustomField() throws Exception {
		fieldMapping.setJiraDevDueDateCustomField("jiraDevDueDateCustomField");
	}

	@Test
	public void testSetJiraDevDoneStatus() throws Exception {
		fieldMapping.setJiraDevDoneStatus(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraDevDoneStatusKPI119() throws Exception {
		fieldMapping.setJiraDevDoneStatusKPI119(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraDevDoneStatusKPI145() throws Exception {
		fieldMapping.setJiraDevDoneStatusKPI145(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraDevDoneStatusKPI128() throws Exception {
		fieldMapping.setJiraDevDoneStatusKPI128(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraRejectedInRefinement() throws Exception {
		fieldMapping.setJiraRejectedInRefinement(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraRejectedInRefinementKPI139() throws Exception {
		fieldMapping.setJiraRejectedInRefinementKPI139(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraAcceptedInRefinement() throws Exception {
		fieldMapping.setJiraAcceptedInRefinement(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraAcceptedInRefinementKPI139() throws Exception {
		fieldMapping.setJiraAcceptedInRefinementKPI139(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraReadyForRefinement() throws Exception {
		fieldMapping.setJiraReadyForRefinement(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraReadyForRefinementKPI139() throws Exception {
		fieldMapping.setJiraReadyForRefinementKPI139(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraFtprRejectStatus() throws Exception {
		fieldMapping.setJiraFtprRejectStatus(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraFtprRejectStatusKPI135() throws Exception {
		fieldMapping.setJiraFtprRejectStatusKPI135(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraFtprRejectStatusKPI82() throws Exception {
		fieldMapping.setJiraFtprRejectStatusKPI82(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIterationCompletionStatusCustomField() throws Exception {
		fieldMapping.setJiraIterationCompletionStatusCustomField(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIterationCompletionStatusKPI135() throws Exception {
		fieldMapping.setJiraIterationCompletionStatusKPI135(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIterationCompletionStatusKPI122() throws Exception {
		fieldMapping.setJiraIterationCompletionStatusKPI122(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIterationCompletionStatusKPI75() throws Exception {
		fieldMapping.setJiraIterationCompletionStatusKPI75(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIterationCompletionStatusKPI145() throws Exception {
		fieldMapping.setJiraIterationCompletionStatusKPI145(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIterationCompletionStatusKPI140() throws Exception {
		fieldMapping.setJiraIterationCompletionStatusKPI140(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIterationCompletionStatusKPI132() throws Exception {
		fieldMapping.setJiraIterationCompletionStatusKPI132(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIterationCompletionStatusKPI136() throws Exception {
		fieldMapping.setJiraIterationCompletionStatusKPI136(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIterationCompletionStatusKpi72() throws Exception {
		fieldMapping.setJiraIterationCompletionStatusKpi72(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIterationCompletionStatusKpi39() throws Exception {
		fieldMapping.setJiraIterationCompletionStatusKpi39(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIterationCompletionStatusKpi5() throws Exception {
		fieldMapping.setJiraIterationCompletionStatusKpi5(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIterationCompletionStatusKPI124() throws Exception {
		fieldMapping.setJiraIterationCompletionStatusKPI124(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIterationCompletionStatusKPI123() throws Exception {
		fieldMapping.setJiraIterationCompletionStatusKPI123(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIterationCompletionStatusKPI125() throws Exception {
		fieldMapping.setJiraIterationCompletionStatusKPI125(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIterationCompletionStatusKPI120() throws Exception {
		fieldMapping.setJiraIterationCompletionStatusKPI120(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIterationCompletionStatusKPI128() throws Exception {
		fieldMapping.setJiraIterationCompletionStatusKPI128(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIterationCompletionStatusKPI134() throws Exception {
		fieldMapping.setJiraIterationCompletionStatusKPI134(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIterationCompletionStatusKPI133() throws Exception {
		fieldMapping.setJiraIterationCompletionStatusKPI133(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIterationCompletionStatusKPI119() throws Exception {
		fieldMapping.setJiraIterationCompletionStatusKPI119(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIterationCompletionStatusKPI131() throws Exception {
		fieldMapping.setJiraIterationCompletionStatusKPI131(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIterationCompletionStatusKPI138() throws Exception {
		fieldMapping.setJiraIterationCompletionStatusKPI138(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIterationCompletionTypeCustomField() throws Exception {
		fieldMapping.setJiraIterationCompletionTypeCustomField(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIterationIssuetypeKPI122() throws Exception {
		fieldMapping.setJiraIterationIssuetypeKPI122(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIterationIssuetypeKPI138() throws Exception {
		fieldMapping.setJiraIterationIssuetypeKPI138(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIterationIssuetypeKPI131() throws Exception {
		fieldMapping.setJiraIterationIssuetypeKPI131(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIterationIssuetypeKPI128() throws Exception {
		fieldMapping.setJiraIterationIssuetypeKPI128(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIterationIssuetypeKPI134() throws Exception {
		fieldMapping.setJiraIterationIssuetypeKPI134(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIterationIssuetypeKPI145() throws Exception {
		fieldMapping.setJiraIterationIssuetypeKPI145(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIterationIssuetypeKpi72() throws Exception {
		fieldMapping.setJiraIterationIssuetypeKpi72(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIterationIssuetypeKPI119() throws Exception {
		fieldMapping.setJiraIterationIssuetypeKPI119(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIterationIssuetypeKpi5() throws Exception {
		fieldMapping.setJiraIterationIssuetypeKpi5(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIterationIssuetypeKPI75() throws Exception {
		fieldMapping.setJiraIterationIssuetypeKPI75(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIterationIssuetypeKPI123() throws Exception {
		fieldMapping.setJiraIterationIssuetypeKPI123(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIterationIssuetypeKPI125() throws Exception {
		fieldMapping.setJiraIterationIssuetypeKPI125(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIterationIssuetypeKPI120() throws Exception {
		fieldMapping.setJiraIterationIssuetypeKPI120(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIterationIssuetypeKPI124() throws Exception {
		fieldMapping.setJiraIterationIssuetypeKPI124(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIterationIssuetypeKPI39() throws Exception {
		fieldMapping.setJiraIterationIssuetypeKPI39(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetUploadData() throws Exception {
		fieldMapping.setUploadData(true);
	}

	@Test
	public void testSetUploadDataKPI42() throws Exception {
		fieldMapping.setUploadDataKPI42(true);
	}

	@Test
	public void testSetUploadDataKPI16() throws Exception {
		fieldMapping.setUploadDataKPI16(true);
	}

	@Test
	public void testSetCreatedDate() throws Exception {
		fieldMapping.setCreatedDate(LocalDateTime.of(2024, Month.JANUARY, 12, 0, 0, 36));
	}

	@Test
	public void testSetJiraDefectRejectionStatusKPI155() throws Exception {
		fieldMapping.setJiraDefectRejectionStatusKPI155("jiraDefectRejectionStatusKPI155");
	}

	@Test
	public void testSetJiraDodKPI155() throws Exception {
		fieldMapping.setJiraDodKPI155(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraLiveStatusKPI155() throws Exception {
		fieldMapping.setJiraLiveStatusKPI155("jiraLiveStatusKPI155");
	}

	@Test
	public void testSetNotificationEnabler() throws Exception {
		fieldMapping.setNotificationEnabler(true);
	}

	@Test
	public void testSetJiraIssueEpicTypeKPI153() throws Exception {
		fieldMapping.setJiraIssueEpicTypeKPI153(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetTestingPhaseDefectCustomField() throws Exception {
		fieldMapping.setTestingPhaseDefectCustomField("testingPhaseDefectCustomField");
	}

	@Test
	public void testSetTestingPhaseDefectsIdentifier() throws Exception {
		fieldMapping.setTestingPhaseDefectsIdentifier("testingPhaseDefectsIdentifier");
	}

	@Test
	public void testSetTestingPhaseDefectValue() throws Exception {
		fieldMapping.setTestingPhaseDefectValue(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetTestingPhaseDefectComponentValue() throws Exception {
		fieldMapping.setTestingPhaseDefectComponentValue("testingPhaseDefectComponentValue");
	}

	@Test
	public void testSetJiraDodKPI163() throws Exception {
		fieldMapping.setJiraDodKPI163(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIssueTypeNamesKPI161() throws Exception {
		fieldMapping.setJiraIssueTypeNamesKPI161(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIssueTypeNamesKPI146() throws Exception {
		fieldMapping.setJiraIssueTypeNamesKPI146(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIssueTypeNamesKPI148() throws Exception {
		fieldMapping.setJiraIssueTypeNamesKPI148(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIssueTypeNamesKPI151() throws Exception {
		fieldMapping.setJiraIssueTypeNamesKPI151(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIssueTypeNamesKPI152() throws Exception {
		fieldMapping.setJiraIssueTypeNamesKPI152(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraDodKPI156() throws Exception {
		fieldMapping.setJiraDodKPI156(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIssueTypeKPI156() throws Exception {
		fieldMapping.setJiraIssueTypeKPI156(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraLabelsKPI14() throws Exception {
		fieldMapping.setJiraLabelsKPI14(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraLabelsKPI82() throws Exception {
		fieldMapping.setJiraLabelsKPI82(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIssueWaitStateKPI170() throws Exception {
		fieldMapping.setJiraIssueWaitStateKPI170(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIssueClosedStateKPI170() throws Exception {
		fieldMapping.setJiraIssueClosedStateKPI170(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetLeadTimeConfigRepoTool() throws Exception {
		fieldMapping.setLeadTimeConfigRepoTool("leadTimeConfigRepoTool");
	}

	@Test
	public void testSetToBranchForMRKPI156() throws Exception {
		fieldMapping.setToBranchForMRKPI156("toBranchForMRKPI156");
	}

	@Test
	public void testSetStartDateCountKPI150() throws Exception {
		fieldMapping.setStartDateCountKPI150(Integer.valueOf(0));
	}

	@Test
	public void testSetJiraDevDoneStatusKPI150() throws Exception {
		fieldMapping.setJiraDevDoneStatusKPI150(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetPopulateByDevDoneKPI150() throws Exception {
		fieldMapping.setPopulateByDevDoneKPI150(true);
	}

	@Test
	public void testSetThresholdValueKPI14() throws Exception {
		fieldMapping.setThresholdValueKPI14("thresholdValueKPI14");
	}

	@Test
	public void testSetThresholdValueKPI82() throws Exception {
		fieldMapping.setThresholdValueKPI82("thresholdValueKPI82");
	}

	@Test
	public void testSetThresholdValueKPI111() throws Exception {
		fieldMapping.setThresholdValueKPI111("thresholdValueKPI111");
	}

	@Test
	public void testSetThresholdValueKPI35() throws Exception {
		fieldMapping.setThresholdValueKPI35("thresholdValueKPI35");
	}

	@Test
	public void testSetThresholdValueKPI34() throws Exception {
		fieldMapping.setThresholdValueKPI34("thresholdValueKPI34");
	}

	@Test
	public void testSetThresholdValueKPI37() throws Exception {
		fieldMapping.setThresholdValueKPI37("thresholdValueKPI37");
	}

	@Test
	public void testSetThresholdValueKPI28() throws Exception {
		fieldMapping.setThresholdValueKPI28("thresholdValueKPI28");
	}

	@Test
	public void testSetThresholdValueKPI36() throws Exception {
		fieldMapping.setThresholdValueKPI36("thresholdValueKPI36");
	}

	@Test
	public void testSetThresholdValueKPI16() throws Exception {
		fieldMapping.setThresholdValueKPI16("thresholdValueKPI16");
	}

	@Test
	public void testSetThresholdValueKPI17() throws Exception {
		fieldMapping.setThresholdValueKPI17("thresholdValueKPI17");
	}

	@Test
	public void testSetThresholdValueKPI38() throws Exception {
		fieldMapping.setThresholdValueKPI38("thresholdValueKPI38");
	}

	@Test
	public void testSetThresholdValueKPI27() throws Exception {
		fieldMapping.setThresholdValueKPI27("thresholdValueKPI27");
	}

	@Test
	public void testSetThresholdValueKPI72() throws Exception {
		fieldMapping.setThresholdValueKPI72("thresholdValueKPI72");
	}

	@Test
	public void testSetThresholdValueKPI84() throws Exception {
		fieldMapping.setThresholdValueKPI84("thresholdValueKPI84");
	}

	@Test
	public void testSetThresholdValueKPI11() throws Exception {
		fieldMapping.setThresholdValueKPI11("thresholdValueKPI11");
	}

	@Test
	public void testSetThresholdValueKPI62() throws Exception {
		fieldMapping.setThresholdValueKPI62("thresholdValueKPI62");
	}

	@Test
	public void testSetThresholdValueKPI64() throws Exception {
		fieldMapping.setThresholdValueKPI64("thresholdValueKPI64");
	}

	@Test
	public void testSetThresholdValueKPI67() throws Exception {
		fieldMapping.setThresholdValueKPI67("thresholdValueKPI67");
	}

	@Test
	public void testSetThresholdValueKPI65() throws Exception {
		fieldMapping.setThresholdValueKPI65("thresholdValueKPI65");
	}

	@Test
	public void testSetThresholdValueKPI157() throws Exception {
		fieldMapping.setThresholdValueKPI157("thresholdValueKPI157");
	}

	@Test
	public void testSetThresholdValueKPI158() throws Exception {
		fieldMapping.setThresholdValueKPI158("thresholdValueKPI158");
	}

	@Test
	public void testSetThresholdValueKPI159() throws Exception {
		fieldMapping.setThresholdValueKPI159("thresholdValueKPI159");
	}

	@Test
	public void testSetThresholdValueKPI160() throws Exception {
		fieldMapping.setThresholdValueKPI160("thresholdValueKPI160");
	}

	@Test
	public void testSetThresholdValueKPI164() throws Exception {
		fieldMapping.setThresholdValueKPI164("thresholdValueKPI164");
	}

	@Test
	public void testSetThresholdValueKPI3() throws Exception {
		fieldMapping.setThresholdValueKPI3("thresholdValueKPI3");
	}

	@Test
	public void testSetThresholdValueKPI126() throws Exception {
		fieldMapping.setThresholdValueKPI126("thresholdValueKPI126");
	}

	@Test
	public void testSetThresholdValueKPI42() throws Exception {
		fieldMapping.setThresholdValueKPI42("thresholdValueKPI42");
	}

	@Test
	public void testSetThresholdValueKPI168() throws Exception {
		fieldMapping.setThresholdValueKPI168("thresholdValueKPI168");
	}

	@Test
	public void testSetThresholdValueKPI70() throws Exception {
		fieldMapping.setThresholdValueKPI70("thresholdValueKPI70");
	}

	@Test
	public void testSetThresholdValueKPI40() throws Exception {
		fieldMapping.setThresholdValueKPI40("thresholdValueKPI40");
	}

	@Test
	public void testSetThresholdValueKPI5() throws Exception {
		fieldMapping.setThresholdValueKPI5("thresholdValueKPI5");
	}

	@Test
	public void testSetThresholdValueKPI39() throws Exception {
		fieldMapping.setThresholdValueKPI39("thresholdValueKPI39");
	}

	@Test
	public void testSetThresholdValueKPI46() throws Exception {
		fieldMapping.setThresholdValueKPI46("thresholdValueKPI46");
	}

	@Test
	public void testSetThresholdValueKPI8() throws Exception {
		fieldMapping.setThresholdValueKPI8("thresholdValueKPI8");
	}

	@Test
	public void testSetThresholdValueKPI73() throws Exception {
		fieldMapping.setThresholdValueKPI73("thresholdValueKPI73");
	}

	@Test
	public void testSetThresholdValueKPI113() throws Exception {
		fieldMapping.setThresholdValueKPI113("thresholdValueKPI113");
	}

	@Test
	public void testSetThresholdValueKPI149() throws Exception {
		fieldMapping.setThresholdValueKPI149("thresholdValueKPI149");
	}

	@Test
	public void testSetThresholdValueKPI153() throws Exception {
		fieldMapping.setThresholdValueKPI153("thresholdValueKPI153");
	}

	@Test
	public void testSetThresholdValueKPI162() throws Exception {
		fieldMapping.setThresholdValueKPI162("thresholdValueKPI162");
	}

	@Test
	public void testSetThresholdValueKPI116() throws Exception {
		fieldMapping.setThresholdValueKPI116("thresholdValueKPI116");
	}

	@Test
	public void testSetThresholdValueKPI156() throws Exception {
		fieldMapping.setThresholdValueKPI156("thresholdValueKPI156");
	}

	@Test
	public void testSetThresholdValueKPI118() throws Exception {
		fieldMapping.setThresholdValueKPI118("thresholdValueKPI118");
	}

	@Test
	public void testSetThresholdValueKPI127() throws Exception {
		fieldMapping.setThresholdValueKPI127("thresholdValueKPI127");
	}

	@Test
	public void testSetThresholdValueKPI170() throws Exception {
		fieldMapping.setThresholdValueKPI170("thresholdValueKPI170");
	}

	@Test
	public void testSetThresholdValueKPI139() throws Exception {
		fieldMapping.setThresholdValueKPI139("thresholdValueKPI139");
	}

	@Test
	public void testSetThresholdValueKPI166() throws Exception {
		fieldMapping.setThresholdValueKPI166("thresholdValueKPI166");
	}

	@Test
	public void testSetJiraProductionIncidentIdentification() throws Exception {
		fieldMapping.setJiraProductionIncidentIdentification("jiraProductionIncidentIdentification");
	}

	@Test
	public void testSetJiraProdIncidentRaisedByCustomField() throws Exception {
		fieldMapping.setJiraProdIncidentRaisedByCustomField("jiraProdIncidentRaisedByCustomField");
	}

	@Test
	public void testSetJiraProdIncidentRaisedByValue() throws Exception {
		fieldMapping.setJiraProdIncidentRaisedByValue(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraStoryIdentificationKPI166() throws Exception {
		fieldMapping.setJiraStoryIdentificationKPI166(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraDodKPI166() throws Exception {
		fieldMapping.setJiraDodKPI166(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetStoryFirstStatusKPI154() throws Exception {
		fieldMapping.setStoryFirstStatusKPI154(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraStatusForInProgressKPI154() throws Exception {
		fieldMapping.setJiraStatusForInProgressKPI154(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraDevDoneStatusKPI154() throws Exception {
		fieldMapping.setJiraDevDoneStatusKPI154(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraQADoneStatusKPI154() throws Exception {
		fieldMapping.setJiraQADoneStatusKPI154(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraOnHoldStatusKPI154() throws Exception {
		fieldMapping.setJiraOnHoldStatusKPI154(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIterationCompletionStatusKPI154() throws Exception {
		fieldMapping.setJiraIterationCompletionStatusKPI154(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraSubTaskIdentification() throws Exception {
		fieldMapping.setJiraSubTaskIdentification(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraStatusStartDevelopmentKPI154() throws Exception {
		fieldMapping.setJiraStatusStartDevelopmentKPI154(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraLabelsKPI135() throws Exception {
		fieldMapping.setJiraLabelsKPI135(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraStatusForInProgressKPI161() throws Exception {
		fieldMapping.setJiraStatusForInProgressKPI161(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraStatusForRefinedKPI161() throws Exception {
		fieldMapping.setJiraStatusForRefinedKPI161(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraStatusForNotRefinedKPI161() throws Exception {
		fieldMapping.setJiraStatusForNotRefinedKPI161(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraIssueTypeKPI171() throws Exception {
		fieldMapping.setJiraIssueTypeKPI171(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraDodKPI171() throws Exception {
		fieldMapping.setJiraDodKPI171(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraDorKPI171() throws Exception {
		fieldMapping.setJiraDorKPI171(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetJiraLiveStatusKPI171() throws Exception {
		fieldMapping.setJiraLiveStatusKPI171(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetStoryFirstStatusKPI171() throws Exception {
		fieldMapping.setStoryFirstStatusKPI171("storyFirstStatusKPI171");
	}

	@Test
	public void testSetId() throws Exception {
		fieldMapping.setId(null);
	}

	@Test
	public void testToString() throws Exception {
		String result = fieldMapping.toString();
		Assert.assertNotNull(result);
	}
}

// Generated with love by TestMe :) Please report issues and submit feature
// requests at: http://weirddev.com/forum#!/testme