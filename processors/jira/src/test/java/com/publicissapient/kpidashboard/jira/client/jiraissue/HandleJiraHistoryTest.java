package com.publicissapient.kpidashboard.jira.client.jiraissue;

import com.atlassian.jira.rest.client.api.domain.*;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import com.publicissapient.kpidashboard.jira.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.jira.data.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.jira.data.JiraIssueHistoryDataFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class HandleJiraHistoryTest {

    @InjectMocks
    private HandleJiraHistory handleJiraHistory;

    @Mock
    private JiraIssueCustomHistory jiraIssueCustomHistory;

    @Mock
    private JiraIssue jiraIssue;

    @Mock
    private FieldMapping fieldMapping;

    private List<ChangelogGroup> changeLogList=new ArrayList<>();

    Map<String, IssueField> fields = new HashMap<>();

    @Before
    public void setUp() throws URISyntaxException {
        JiraIssueHistoryDataFactory jiraIssueHistoryDataFactory = JiraIssueHistoryDataFactory.newInstance("/json/default/jira_issue_custom_history.json");
        JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance("/json/default/jira_issue.json");
        FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory.newInstance("/json/default/field_mapping.json");


         jiraIssueCustomHistory = jiraIssueHistoryDataFactory.getJiraIssueCustomHistory().get(0);
         jiraIssue = jiraIssueDataFactory.getJiraIssues().get(0);
         fieldMapping=fieldMappingDataFactory.getFieldMappings().get(0);

        ChangelogGroup changelogGroup;
        changelogGroup = new ChangelogGroup(new BasicUser(new URI(""), "", "", ""), new DateTime("2023-02-28T03:57:59.000+0000"), Arrays.asList(new ChangelogItem( FieldType.JIRA, "status", "10003", "In Development", "15752", "Code Review")));
        changeLogList.add(changelogGroup);
        changelogGroup = new ChangelogGroup(new BasicUser(new URI(""), "", "", ""), new DateTime("2023-02-28T03:57:59.000+0000"), Arrays.asList(new ChangelogItem(FieldType.JIRA, "priority", "10003", "P1", "15752", "P2")));
        changeLogList.add(changelogGroup);
        changelogGroup = new ChangelogGroup(new BasicUser(new URI(""), "", "", ""), new DateTime("2023-02-28T03:57:59.000+0000"), Arrays.asList( new ChangelogItem(FieldType.JIRA, "assignee", "10003", "Harsh", "15752", "Akshat")));
        changeLogList.add(changelogGroup);
        changelogGroup = new ChangelogGroup(new BasicUser(new URI(""), "", "", ""), new DateTime("2023-02-28T03:57:59.000+0000"), Arrays.asList( new ChangelogItem(FieldType.JIRA, "fix version", "10003", "KnowHOW v6.7.0", "15752", "KnowHOW v6.8.0")));
        changeLogList.add(changelogGroup);
        changelogGroup = new ChangelogGroup(new BasicUser(new URI(""), "", "", ""), new DateTime("2023-02-28T03:57:59.000+0000"), Arrays.asList( new ChangelogItem(FieldType.JIRA, "fix version", "10003", "KnowHOW v6.8.0", "15752", "KnowHOW v6.9.0")));
        changeLogList.add(changelogGroup);
        changelogGroup = new ChangelogGroup(new BasicUser(new URI(""), "", "", ""), new DateTime("2023-02-28T03:59:59.000+0000"), Arrays.asList( new ChangelogItem(FieldType.JIRA, "fix version", "10003", "KnowHOW v6.7.0", "15752", "KnowHOW v6.8.0")));
        changeLogList.add(changelogGroup);
        changelogGroup = new ChangelogGroup(new BasicUser(new URI(""), "", "", ""), new DateTime("2023-02-28T03:57:59.000+0000"), Arrays.asList( new ChangelogItem(FieldType.JIRA, "Labels", "10003", "L1", "15752", "L2")));
        changeLogList.add(changelogGroup);
        changelogGroup = new ChangelogGroup(new BasicUser(new URI(""), "", "", ""), new DateTime("2023-02-28T03:57:59.000+0000"), Arrays.asList( new ChangelogItem(FieldType.CUSTOM, "Due Date", "10003", "2023-02-21 00:00:00.0", "15752", "2023-02-24 00:00:00.0")));
        changeLogList.add(changelogGroup);
        changelogGroup = new ChangelogGroup(new BasicUser(new URI(""), "", "", ""), new DateTime("2023-02-28T03:57:59.000+0000"), Arrays.asList( new ChangelogItem(FieldType.CUSTOM, "Sprint", "10003", "KnowHOW | PI_12| ITR_4", "15752", "KnowHOW | PI_12| ITR_5")));
        changeLogList.add(changelogGroup);


        fields.put("customfield_11528",new IssueField("","Due Date","",null));
        fields.put("customfield_12700",new IssueField("","Sprint","",null));
    }

    @Test
    public void testSetJiraFieldChangeLog1()  {
        handleJiraHistory.setJiraIssueCustomHistoryUpdationLog(jiraIssueCustomHistory,changeLogList,fieldMapping,jiraIssue,fields);
        Assert.assertEquals(jiraIssueCustomHistory.getStatusUpdationLog().size(), 2);
        Assert.assertEquals(jiraIssueCustomHistory.getAssigneeUpdationLog().size(), 1);
        Assert.assertEquals(jiraIssueCustomHistory.getLabelUpdationLog().size(), 1);
        Assert.assertEquals(jiraIssueCustomHistory.getFixVersionUpdationLog().size(), 2);
        Assert.assertEquals(jiraIssueCustomHistory.getPriorityUpdationLog().size(), 1);
        Assert.assertEquals(jiraIssueCustomHistory.getSprintUpdationLog().size(), 1);
        Assert.assertEquals(jiraIssueCustomHistory.getDueDateUpdationLog().size(), 1);

    }

    @Test
    public void testSetJiraFieldChangeLog2()  {
        if(ObjectUtils.isNotEmpty(changeLogList))changeLogList.clear();
        handleJiraHistory.setJiraIssueCustomHistoryUpdationLog(jiraIssueCustomHistory,changeLogList,fieldMapping,jiraIssue,fields);
        Assert.assertEquals(jiraIssueCustomHistory.getStatusUpdationLog().size(), 1);
        Assert.assertEquals(jiraIssueCustomHistory.getAssigneeUpdationLog().size(), 0);
        Assert.assertEquals(jiraIssueCustomHistory.getLabelUpdationLog().size(), 0);
        Assert.assertEquals(jiraIssueCustomHistory.getFixVersionUpdationLog().size(), 0);
        Assert.assertEquals(jiraIssueCustomHistory.getPriorityUpdationLog().size(), 0);
        Assert.assertEquals(jiraIssueCustomHistory.getDueDateUpdationLog().size(), 0);
        Assert.assertEquals(jiraIssueCustomHistory.getSprintUpdationLog().size(), 0);
    }

    @Test
    public void testJiraDueDateChangeLog() throws URISyntaxException {
        fieldMapping.setJiraDueDateCustomField(null);
        fieldMapping.setJiraDueDateField(null);
        ChangelogGroup changelogGroup = new ChangelogGroup(new BasicUser(new URI(""), "", "", ""), new DateTime("2023-02-28T03:57:59.000+0000"), Arrays.asList( new ChangelogItem(FieldType.JIRA, "dueDate", "10003", "2023-02-21 00:00:00.0", "15752", "2023-02-24 00:00:00.0")));
        changeLogList.add(changelogGroup);
        handleJiraHistory.setJiraIssueCustomHistoryUpdationLog(jiraIssueCustomHistory,changeLogList,fieldMapping,jiraIssue,fields);
        Assert.assertEquals(jiraIssueCustomHistory.getStatusUpdationLog().size(), 2);
        Assert.assertEquals(jiraIssueCustomHistory.getAssigneeUpdationLog().size(), 1);
        Assert.assertEquals(jiraIssueCustomHistory.getLabelUpdationLog().size(), 1);
        Assert.assertEquals(jiraIssueCustomHistory.getFixVersionUpdationLog().size(), 2);
        Assert.assertEquals(jiraIssueCustomHistory.getPriorityUpdationLog().size(), 1);
        Assert.assertEquals(jiraIssueCustomHistory.getSprintUpdationLog().size(), 1);
        Assert.assertEquals(jiraIssueCustomHistory.getDueDateUpdationLog().size(), 1);
    }

}

