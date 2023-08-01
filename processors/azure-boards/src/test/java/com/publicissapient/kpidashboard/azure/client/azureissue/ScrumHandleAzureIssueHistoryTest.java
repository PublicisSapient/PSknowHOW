package com.publicissapient.kpidashboard.azure.client.azureissue;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.publicissapient.kpidashboard.azure.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.azureboards.updates.Fields;
import com.publicissapient.kpidashboard.common.model.azureboards.updates.SystemChangedDate;
import com.publicissapient.kpidashboard.common.model.azureboards.updates.SystemIterationPath;
import com.publicissapient.kpidashboard.common.model.azureboards.updates.SystemState;
import com.publicissapient.kpidashboard.common.model.azureboards.updates.Value;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;

@ExtendWith(SpringExtension.class)
public class ScrumHandleAzureIssueHistoryTest {
	JiraIssue jiraIssue = new JiraIssue();
	@InjectMocks
	private ScrumHandleAzureIssueHistory handleJiraHistory;
	@Mock
	private JiraIssueCustomHistory jiraIssueCustomHistory;
	@Mock
	private FieldMapping fieldMapping;
	private List<Value> changeLogList = new ArrayList<>();

	@BeforeEach
	public void setUp() throws URISyntaxException {

		jiraIssueCustomHistory = new JiraIssueCustomHistory();
		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/onlinedata/azure/scrumfieldmapping.json");
		fieldMapping = fieldMappingDataFactory.getFieldMappings();
		Fields fields = new Fields();
		SystemIterationPath systemIterationId = new SystemIterationPath();
		systemIterationId.setOldValue("2");
		systemIterationId.setNewValue("1");
		fields.setSystemIterationPath(systemIterationId);
		SystemChangedDate systemChangedDate = new SystemChangedDate();
		systemChangedDate.setNewValue("2021-07-06T09:20:00.28Z");
		fields.setSystemChangedDate(systemChangedDate);
		Value changelogGroup = new Value();
		changelogGroup.setId(1);
		changelogGroup.setFields(fields);
		changeLogList.add(changelogGroup);

		Fields fields1 = new Fields();
		SystemState systemState = new SystemState();

		systemState.setNewValue("In Progress");
		fields1.setSystemState(systemState);
		Map<String, Object> subMap = new HashMap<>();
		subMap.put("newValue", "2021-07-06T09:20:00.28Z");
		subMap.put("oldValue", "2021-07-05T09:20:00.28Z");
		fields1.setAdditionalProperty("Microsoft.VSTS.Common.Priority", subMap);
		fields1.setAdditionalProperty("System.Tags", subMap);
		fields1.setAdditionalProperty("Microsoft.VSTS.Scheduling.DueDate", subMap);
		Map<String, String> subMap2 = new HashMap<>();
		subMap2.put("displayName", "test");
		Map<String, Object> subMap3 = new HashMap<>();
		subMap3.put("newValue", subMap3);
		fields1.setAdditionalProperty("System.AssignedTo", subMap3);
		fields1.setSystemState(systemState);
		fields1.setSystemChangedDate(systemChangedDate);
		Value changelogGroup2 = new Value();
		changelogGroup2.setId(1);
		changelogGroup2.setFields(fields1);
		changeLogList.add(changelogGroup2);

		jiraIssue.setAssigneeId("1");
		jiraIssue.setAssigneeName("test");

	}

	@Test
	public void testSetJiraFieldChangeLog1() {
		HashMap fieldsMap = new HashMap<>();
		handleJiraHistory.setJiraIssueCustomHistoryUpdationLog(jiraIssueCustomHistory, changeLogList, fieldMapping,
				fieldsMap);
		Assert.assertEquals(1, jiraIssueCustomHistory.getStatusUpdationLog().size());
		Assert.assertEquals(1, jiraIssueCustomHistory.getAssigneeUpdationLog().size());
		Assert.assertEquals(1, jiraIssueCustomHistory.getLabelUpdationLog().size());
		Assert.assertEquals(1, jiraIssueCustomHistory.getPriorityUpdationLog().size());
		Assert.assertEquals(1, jiraIssueCustomHistory.getSprintUpdationLog().size());
		Assert.assertEquals(1, jiraIssueCustomHistory.getDueDateUpdationLog().size());

	}

	@Test
	public void testSetJiraFieldChangeLog2() {
		if (ObjectUtils.isNotEmpty(changeLogList))
			changeLogList.clear();
		HashMap fieldsMap = new HashMap<>();
		handleJiraHistory.setJiraIssueCustomHistoryUpdationLog(jiraIssueCustomHistory, changeLogList, fieldMapping,
				fieldsMap);
		Assert.assertEquals(0, jiraIssueCustomHistory.getStatusUpdationLog().size());
		Assert.assertEquals(0, jiraIssueCustomHistory.getAssigneeUpdationLog().size());
		Assert.assertEquals(0, jiraIssueCustomHistory.getLabelUpdationLog().size());
		Assert.assertEquals(0, jiraIssueCustomHistory.getFixVersionUpdationLog().size());
		Assert.assertEquals(0, jiraIssueCustomHistory.getPriorityUpdationLog().size());
		Assert.assertEquals(0, jiraIssueCustomHistory.getDueDateUpdationLog().size());
		Assert.assertEquals(0, jiraIssueCustomHistory.getSprintUpdationLog().size());
	}

}