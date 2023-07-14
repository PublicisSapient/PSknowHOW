package com.publicissapient.kpidashboard.zephyr.processor.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

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
import com.publicissapient.kpidashboard.zephyr.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.zephyr.model.ZephyrProcessor;
import com.publicissapient.kpidashboard.zephyr.repository.ZephyrProcessorRepository;

@ExtendWith(SpringExtension.class)
public class ZephyrDBServiceImplTest {

	@Mock
	private ZephyrProcessorRepository zephyrProcessorRepository;

	@Mock
	private AccountHierarchyRepository accountHierarchyRepository;

	@Mock
	private KanbanAccountHierarchyRepository kanbanAccountHierarchyRepo;

	@Mock
	private TestCaseDetailsRepository testCaseDetailsRepository;

	@InjectMocks
	private ZephyrDBServiceImpl zephyrDBService;

	@Test
	public void testProcessTestCaseInfoToDBForKanban() {
		List<ZephyrTestCaseDTO> testCases = new ArrayList<>();
		ZephyrTestCaseDTO zephyrDto = new ZephyrTestCaseDTO();
		zephyrDto.setComponent("component");
		zephyrDto.setKey("key");
		zephyrDto.setCreatedOn("2020-07-10T12:02:31.000Z");
		zephyrDto.setUpdatedOn("2021-08-11T11:08:32.000Z");
		List<String> labels = new ArrayList<>();
		labels.add("label1");
		zephyrDto.setLabels(labels);
		zephyrDto.setFolder("folder");
		Set<String> issues = new HashSet<>();
		issues.add("issue1");
		issues.add("issue2");
		zephyrDto.setIssueLinks(issues);
		Map<String, String> customFields = new HashMap<>();
		customFields.put("testAutomated", "value");
		customFields.put("AutomationStatus", NormalizedJira.YES_VALUE.getValue());
		customFields.put("testRegressionLabel", "testRegressionLabel");
		zephyrDto.setCustomFields(customFields);
		testCases.add(zephyrDto);
		ProjectConfFieldMapping projectConfigMapping = new ProjectConfFieldMapping();
		projectConfigMapping.setKanban(true);
		projectConfigMapping.setBasicProjectConfigId(new ObjectId("62668297d604e57b01709a28"));
		ProcessorToolConnection toolConnection = new ProcessorToolConnection();
		List<String> jiraAutomatedTestValue = new ArrayList<>();
		jiraAutomatedTestValue.add(NormalizedJira.YES_VALUE.getValue());
		toolConnection.setBasicProjectConfigId(new ObjectId("62668297d604e57b01709a28"));
		toolConnection.setAutomatedTestValue(jiraAutomatedTestValue);
		toolConnection.setTestAutomationStatusLabel("AutomationStatus");
		toolConnection.setTestRegressionLabel("testRegressionLabel");
		List<String> testRegressionValue = new ArrayList<>();
		testRegressionValue.add("testRegressionLabel");
		toolConnection.setTestRegressionValue(testRegressionValue);
		List<String> canNotAutomatedTestValue = new ArrayList<>();
		canNotAutomatedTestValue.add("value");
		toolConnection.setCanNotAutomatedTestValue(canNotAutomatedTestValue);
		projectConfigMapping.setProcessorToolConnection(toolConnection);
		ZephyrProcessor processor = ZephyrProcessor.prototype();
		ObjectId id = new ObjectId();
		processor.setId(id);
		when(zephyrProcessorRepository.findByProcessorName(ProcessorConstants.ZEPHYR)).thenReturn(processor);
		List<KanbanAccountHierarchy> projectAccHierList = new ArrayList<>();
		KanbanAccountHierarchy projectHierarchy = new KanbanAccountHierarchy();
		projectHierarchy.setParentId("parentId");
		projectAccHierList.add(projectHierarchy);
		when(kanbanAccountHierarchyRepo.findByLabelNameAndBasicProjectConfigId(eq("project"), any(ObjectId.class)))
				.thenReturn(projectAccHierList);
		List<TestCaseDetails> testCaseDetailsList = new ArrayList<>();
		TestCaseDetails testCaseDetails = new TestCaseDetails();
		testCaseDetailsList.add(testCaseDetails);
		when(testCaseDetailsRepository.findByNumberAndBasicProjectConfigId(anyString(), anyString()))
				.thenReturn(testCaseDetailsList);
		when(testCaseDetailsRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
		zephyrDBService.processTestCaseInfoToDB(testCases, projectConfigMapping.getProcessorToolConnection(), true,
				false);
	}

	@Test
	public void testProcessTestCaseInfoToDBForScrum() {
		List<ZephyrTestCaseDTO> testCases = new ArrayList<>();
		ZephyrTestCaseDTO zephyrDto = new ZephyrTestCaseDTO();
		zephyrDto.setComponent("component");
		zephyrDto.setKey("key");
		zephyrDto.setCreatedOn("2020-02-05T21:45:22Z");
		zephyrDto.setUpdatedOn(zephyrDto.getCreatedOn());
		List<String> labels = new ArrayList<>();
		labels.add("label1");
		zephyrDto.setLabels(labels);
		zephyrDto.setFolder("folder");
		Set<String> issues = new HashSet<>();
		issues.add("issue1");
		issues.add("issue2");
		zephyrDto.setIssueLinks(issues);
		Map<String, String> customFields = new HashMap<>();
		customFields.put("testAutomated", "value");
		customFields.put("AutomationStatus", NormalizedJira.YES_VALUE.getValue());
		zephyrDto.setCustomFields(customFields);
		testCases.add(zephyrDto);

		ZephyrTestCaseDTO zephyrDto2 = new ZephyrTestCaseDTO();
		zephyrDto2.setCreatedOn("2020-02-05T21:45:22Z");
		zephyrDto2.setUpdatedOn(zephyrDto2.getCreatedOn());
		zephyrDto2.setComponent("component");
		zephyrDto2.setKey("key2");
		zephyrDto2.setLabels(labels);
		zephyrDto2.setFolder("folder");
		Set<String> issues2 = new HashSet<>();
		issues2.add("issue1");
		issues2.add("issue2");
		zephyrDto2.setIssueLinks(issues2);
		testCases.add(zephyrDto2);

		ZephyrTestCaseDTO zephyrDto3 = new ZephyrTestCaseDTO();
		zephyrDto3.setCreatedOn("2020-02-05T21:45:22Z");
		zephyrDto3.setUpdatedOn(zephyrDto3.getCreatedOn());
		zephyrDto3.setComponent("component");
		zephyrDto3.setKey("key3");
		zephyrDto.setLabels(labels);
		zephyrDto3.setFolder("folder");
		Set<String> issues3 = new HashSet<>();
		issues3.add("issue1");
		issues3.add("issue2");
		zephyrDto3.setIssueLinks(issues3);
		Map<String, String> customFields1 = new HashMap<>();
		customFields1.put("testAutomated", "value");
		customFields1.put("AutomationStatus", NormalizedJira.YES_VALUE.getValue());
		customFields1.put("Sub Project", "SP");
		zephyrDto3.setCustomFields(customFields1);
		testCases.add(zephyrDto3);

		ProjectConfFieldMapping projectConfigMapping = new ProjectConfFieldMapping();
		projectConfigMapping.setKanban(false);
		projectConfigMapping.setBasicProjectConfigId(new ObjectId("62668297d604e57b01709a28"));
		ProcessorToolConnection toolConnection = new ProcessorToolConnection();
		toolConnection.setBasicProjectConfigId(new ObjectId("62668297d604e57b01709a28"));
		List<String> jiraAutomatedTestValue = new ArrayList<>();
		jiraAutomatedTestValue.add(NormalizedJira.YES_VALUE.getValue());
		toolConnection.setAutomatedTestValue(jiraAutomatedTestValue);
		toolConnection.setTestAutomationStatusLabel("AutomationStatus");
		toolConnection.setTestRegressionLabel("testRegressionLabel");
		List<String> testRegressionValue = new ArrayList<>();
		testRegressionValue.add("testRegressionLabel");
		toolConnection.setTestRegressionValue(testRegressionValue);
		List<String> canNotAutomatedTestValue = new ArrayList<>();
		canNotAutomatedTestValue.add("value");
		toolConnection.setCanNotAutomatedTestValue(canNotAutomatedTestValue);
		projectConfigMapping.setProcessorToolConnection(toolConnection);
		ZephyrProcessor processor = ZephyrProcessor.prototype();
		ObjectId id = new ObjectId();
		processor.setId(id);
		when(zephyrProcessorRepository.findByProcessorName(ProcessorConstants.ZEPHYR)).thenReturn(processor);
		AccountHierarchy projectAccHier = new AccountHierarchy();
		projectAccHier.setParentId("parentId");
		projectAccHier.setPath("a,b,c");
		projectAccHier.setNodeId("componentId");
		projectAccHier.setNodeName("nodeName");
		when(accountHierarchyRepository.findByLabelNameAndBasicProjectConfigId(eq("project"), any(ObjectId.class)))
				.thenReturn(Arrays.asList(projectAccHier));

		List<TestCaseDetails> testCaseDetailsList = new ArrayList<>();
		TestCaseDetails testCaseDetails = new TestCaseDetails();
		testCaseDetailsList.add(testCaseDetails);
		when(testCaseDetailsRepository.findByNumberAndBasicProjectConfigId(anyString(), anyString()))
				.thenReturn(testCaseDetailsList);
		when(testCaseDetailsRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
		zephyrDBService.processTestCaseInfoToDB(testCases, projectConfigMapping.getProcessorToolConnection(), false,
				true);
	}
}
