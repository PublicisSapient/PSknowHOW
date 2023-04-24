package com.publicissapient.kpidashboard.azure.client.azureissue;

import com.publicissapient.kpidashboard.azure.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.azureboards.updates.*;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class KanbanHandleAzureIssueHistoryTest {
    @InjectMocks
    private KanbanHandleAzureIssueHistory handleJiraHistory;

    @Mock
    private KanbanIssueCustomHistory jiraIssueCustomHistory;

    @Mock
    private FieldMapping fieldMapping;

    private List<Value> changeLogList = new ArrayList<>();

    @Before
    public void setUp() throws URISyntaxException {

        jiraIssueCustomHistory = new KanbanIssueCustomHistory();
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


    }

    @Test
    public void testSetJiraFieldChangeLog1() {
        HashMap fieldsMap = new HashMap<>();
        handleJiraHistory.setJiraIssueCustomHistoryUpdationLog(jiraIssueCustomHistory, changeLogList, fieldMapping, fieldsMap);
        Assert.assertEquals(jiraIssueCustomHistory.getStatusUpdationLog().size(), 1);
        Assert.assertEquals(jiraIssueCustomHistory.getAssigneeUpdationLog().size(), 1);
        Assert.assertEquals(jiraIssueCustomHistory.getLabelUpdationLog().size(), 1);
        Assert.assertEquals(jiraIssueCustomHistory.getPriorityUpdationLog().size(), 1);
        Assert.assertEquals(jiraIssueCustomHistory.getSprintUpdationLog().size(), 1);
        Assert.assertEquals(jiraIssueCustomHistory.getDueDateUpdationLog().size(), 1);

    }

    @Test
    public void testSetJiraFieldChangeLog2() {
        if (ObjectUtils.isNotEmpty(changeLogList))
            changeLogList.clear();
        HashMap fieldsMap = new HashMap<>();
        handleJiraHistory.setJiraIssueCustomHistoryUpdationLog(jiraIssueCustomHistory, changeLogList, fieldMapping,
                fieldsMap);
        Assert.assertEquals(jiraIssueCustomHistory.getStatusUpdationLog().size(), 0);
        Assert.assertEquals(jiraIssueCustomHistory.getAssigneeUpdationLog().size(), 0);
        Assert.assertEquals(jiraIssueCustomHistory.getLabelUpdationLog().size(), 0);
        Assert.assertEquals(jiraIssueCustomHistory.getPriorityUpdationLog().size(), 0);
        Assert.assertEquals(jiraIssueCustomHistory.getDueDateUpdationLog().size(), 0);
        Assert.assertEquals(jiraIssueCustomHistory.getSprintUpdationLog().size(), 0);
    }

}