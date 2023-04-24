package com.publicissapient.kpidashboard.azure.client.azureissue;


import com.publicissapient.kpidashboard.azure.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.azureboards.updates.*;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;


@RunWith(MockitoJUnitRunner.class)
class ScrumHandleAzureIssueHistoryTest {
    @InjectMocks
    private ScrumHandleAzureIssueHistory handleJiraHistory;

    @Mock
    private JiraIssueCustomHistory jiraIssueCustomHistory;

    @Mock
    private FieldMapping fieldMapping;

    private List<Value> changeLogList = new ArrayList<>();

    @Before
    public void setUp() throws URISyntaxException {

        jiraIssueCustomHistory = new JiraIssueCustomHistory();
        FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
                .newInstance("/json/default/field_mapping.json");
        fieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
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
        systemState.setOldValue("To Do");
        systemState.setNewValue("In Progress");
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
        subMap3.put("oldValue", subMap3);
        fields1.setAdditionalProperty("System.AssignedTo", subMap3);
        fields1.setSystemState(systemState);
        fields1.setSystemChangedDate(systemChangedDate);
        Value changelogGroup2 = new Value();
        changelogGroup2.setId(1);
        changelogGroup2.setFields(fields1);
        changeLogList.add(changelogGroup);
    }

    @Test
    public void testSetJiraFieldChangeLog1() {

        handleJiraHistory.setJiraIssueCustomHistoryUpdationLog(jiraIssueCustomHistory, changeLogList, fieldMapping);
        Assert.assertEquals(jiraIssueCustomHistory.getStatusUpdationLog().size(), 2);
        Assert.assertEquals(jiraIssueCustomHistory.getAssigneeUpdationLog().size(), 2);
        Assert.assertEquals(jiraIssueCustomHistory.getLabelUpdationLog().size(), 2);
        Assert.assertEquals(jiraIssueCustomHistory.getFixVersionUpdationLog().size(), 3);
        Assert.assertEquals(jiraIssueCustomHistory.getPriorityUpdationLog().size(), 2);
        Assert.assertEquals(jiraIssueCustomHistory.getSprintUpdationLog().size(), 1);
        Assert.assertEquals(jiraIssueCustomHistory.getDueDateUpdationLog().size(), 1);

    }

    @Test
    public void testSetJiraFieldChangeLog2() {
        if (ObjectUtils.isNotEmpty(changeLogList))
            changeLogList.clear();
        handleJiraHistory.setJiraIssueCustomHistoryUpdationLog(jiraIssueCustomHistory, changeLogList, fieldMapping,
                fields);
        Assert.assertEquals(jiraIssueCustomHistory.getStatusUpdationLog().size(), 1);
        Assert.assertEquals(jiraIssueCustomHistory.getAssigneeUpdationLog().size(), 1);
        Assert.assertEquals(jiraIssueCustomHistory.getLabelUpdationLog().size(), 0);
        Assert.assertEquals(jiraIssueCustomHistory.getFixVersionUpdationLog().size(), 0);
        Assert.assertEquals(jiraIssueCustomHistory.getPriorityUpdationLog().size(), 1);
        Assert.assertEquals(jiraIssueCustomHistory.getDueDateUpdationLog().size(), 0);
        Assert.assertEquals(jiraIssueCustomHistory.getSprintUpdationLog().size(), 0);
    }

    @Test
    public void testJiraDueDateChangeLog() throws URISyntaxException {
        fieldMapping.setJiraDueDateCustomField("");
        fieldMapping.setJiraDueDateField("Due Date");
        ChangelogGroup changelogGroup = new ChangelogGroup(new BasicUser(new URI(""), "", "", ""),
                new DateTime("2023-02-28T03:57:59.000+0000"), Arrays.asList(new ChangelogItem(FieldType.JIRA, "dueDate",
                "2023-02-21", "2023-02-21 00:00:00.0", "2023-02-24", "2023-02-24 00:00:00.0")));
        changeLogList.add(changelogGroup);
        handleJiraHistory.setJiraIssueCustomHistoryUpdationLog(jiraIssueCustomHistory, changeLogList, fieldMapping,
                fields, issue);
        Assert.assertEquals(jiraIssueCustomHistory.getStatusUpdationLog().size(), 2);
        Assert.assertEquals(jiraIssueCustomHistory.getAssigneeUpdationLog().size(), 2);
        Assert.assertEquals(jiraIssueCustomHistory.getLabelUpdationLog().size(), 2);
        Assert.assertEquals(jiraIssueCustomHistory.getFixVersionUpdationLog().size(), 3);
        Assert.assertEquals(jiraIssueCustomHistory.getPriorityUpdationLog().size(), 2);
        Assert.assertEquals(jiraIssueCustomHistory.getSprintUpdationLog().size(), 1);
        Assert.assertEquals(jiraIssueCustomHistory.getDueDateUpdationLog().size(), 2);
    }

}