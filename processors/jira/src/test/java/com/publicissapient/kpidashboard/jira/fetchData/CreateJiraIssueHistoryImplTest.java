package com.publicissapient.kpidashboard.jira.fetchData;


import com.atlassian.jira.rest.client.api.StatusCategory;
import com.atlassian.jira.rest.client.api.domain.*;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.jira.data.ConnectionsDataFactory;
import com.publicissapient.kpidashboard.jira.data.FieldMappingDataFactory;

import com.publicissapient.kpidashboard.jira.data.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.jira.data.ToolConfigDataFactory;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import org.apache.commons.beanutils.BeanUtils;
import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CreateJiraIssueHistoryImplTest {

    @InjectMocks
    private CreateJiraIssueHistoryImpl createJiraIssueHistory;

    @Mock
    private JiraIssueCustomHistory jiraIssueCustomHistory;

    @Mock
    private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

    @Mock
    private FieldMapping fieldMapping;

    private List<ChangelogGroup> changeLogList = new ArrayList<>();

    Map<String, IssueField> fields = new HashMap<>();

    private Issue issue ;

    JiraIssue jiraIssue;

    @Before
    public void setUp() throws URISyntaxException {

        jiraIssueCustomHistory = new JiraIssueCustomHistory();
        FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory.newInstance("/json/default/field_mapping.json");
        fieldMapping = fieldMappingDataFactory.findById("63bfa0f80b28191677615735");

        createFieldsMap(true);
        createIssue();
        jiraIssue=getMockJiraIssue();

    }

    @Test
    public void createIssueCustomHistory(){
        when(jiraIssueCustomHistoryRepository.findByStoryIDAndBasicProjectConfigId(any(),any())).thenReturn(Collections.EMPTY_LIST);
        Assert.assertEquals("63c04dc7b7617e260763ca4e",createJiraIssueHistory.createIssueCustomHistory(createProjectConfig(),"12345",jiraIssue,issue,fieldMapping,fields).getBasicProjectConfigId());
    }

    private ProjectConfFieldMapping createProjectConfig(){
        ProjectConfFieldMapping projectConfFieldMapping=ProjectConfFieldMapping.builder().build();
        projectConfFieldMapping.setBasicProjectConfigId(new ObjectId("63c04dc7b7617e260763ca4e"));

        return projectConfFieldMapping;
    }

    private JiraIssue getMockJiraIssue() {
        JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory
                .newInstance("/json/default/jira_issues.json");
        return jiraIssueDataFactory.findTopByBasicProjectConfigId("63c04dc7b7617e260763ca4e");
    }

    private void createIssue() throws URISyntaxException {
        BasicProject basicProj = new BasicProject(new URI("self"), "proj1", 1l, "project1");
        IssueType issueType1 = new IssueType(new URI("self"), 1l, "Story", false, "desc", new URI("iconURI"));
        Status status1 = new Status(new URI("self"), 1l, "Ready for Sprint Planning", "desc", new URI("iconURI"),
                new StatusCategory(new URI("self"), "name", 1l, "key", "colorname"));
        BasicPriority basicPriority = new BasicPriority(new URI("self"), 1l, "priority1");
        Resolution resolution = new Resolution(new URI("self"), 1l, "resolution", "resolution");
        Map<String, URI> avatarMap = new HashMap<>();
        avatarMap.put("48x48", new URI("value"));
        User user1 = new User(new URI("self"), "user1", "user1", "userAccount", "user1@xyz.com", true, null, avatarMap,
                null);
        Map<String, String> map = new HashMap<>();
        map.put("customfield_12121", "Client Testing (UAT)");
        map.put("self", "https://jiradomain.com/jira/rest/api/2/customFieldOption/20810");
        map.put("value", "Component");
        map.put("id", "20810");
        JSONObject value = new JSONObject(map);
        IssueField issueField = new IssueField("20810", "Component", null, value);
        List<IssueField> issueFields = Arrays.asList(issueField);
        Comment comment = new Comment(new URI("self"), "body", null, null, DateTime.now(), DateTime.now(),
                new Visibility(Visibility.Type.ROLE, "abc"), 1l);
        List<Comment> comments = Arrays.asList(comment);
        BasicVotes basicVotes = new BasicVotes(new URI("self"), 1, true);
        BasicUser basicUser = new BasicUser(new URI("self"), "basicuser", "basicuser", "accountId");
        Worklog worklog = new Worklog(new URI("self"), new URI("self"), basicUser, basicUser, null, DateTime.now(),
                DateTime.now(), DateTime.now(), 60, null);
        List<Worklog> workLogs = Arrays.asList(worklog);
//        ChangelogItem changelogItem = new ChangelogItem(FieldType.JIRA, "field1", "from", "fromString", "to",
//                "toString");
//        ChangelogGroup changelogGroup = new ChangelogGroup(basicUser, DateTime.now(), Arrays.asList(changelogItem));

        ChangelogGroup changelogGroup;
        changelogGroup = new ChangelogGroup(new BasicUser(new URI(""), "", "", ""), new DateTime("2023-02-28T03:57:59.000+0000"), Arrays.asList(new ChangelogItem(FieldType.JIRA, "status", "10003", "In Development", "15752", "Code Review")));
        changeLogList.add(changelogGroup);
        changelogGroup = new ChangelogGroup(new BasicUser(new URI(""), "", "", ""), new DateTime("2023-02-28T03:57:59.000+0000"), Arrays.asList(new ChangelogItem(FieldType.JIRA, "priority", "10003", "P1", "15752", "P2")));
        changeLogList.add(changelogGroup);
        changelogGroup = new ChangelogGroup(new BasicUser(new URI(""), "", "", ""), new DateTime("2023-02-28T03:57:59.000+0000"), Arrays.asList(new ChangelogItem(FieldType.JIRA, "assignee", "10003", "Harsh", "15752", "Akshat")));
        changeLogList.add(changelogGroup);
        changelogGroup = new ChangelogGroup(new BasicUser(new URI(""), "", "", ""), new DateTime("2023-02-28T03:57:59.000+0000"), Arrays.asList(new ChangelogItem(FieldType.JIRA, "fix version", "10003", "KnowHOW v6.7.0", "15752", "KnowHOW v6.8.0")));
        changeLogList.add(changelogGroup);
        changelogGroup = new ChangelogGroup(new BasicUser(new URI(""), "", "", ""), new DateTime("2023-02-28T03:57:59.000+0000"), Arrays.asList(new ChangelogItem(FieldType.JIRA, "fix version", "10003", "KnowHOW v6.8.0", "15752", "KnowHOW v6.9.0")));
        changeLogList.add(changelogGroup);
        changelogGroup = new ChangelogGroup(new BasicUser(new URI(""), "", "", ""), new DateTime("2023-02-28T03:59:59.000+0000"), Arrays.asList(new ChangelogItem(FieldType.JIRA, "fix version", "10003", "KnowHOW v6.7.0", "15752", "KnowHOW v6.8.0")));
        changeLogList.add(changelogGroup);
        changelogGroup = new ChangelogGroup(new BasicUser(new URI(""), "", "", ""), new DateTime("2023-02-28T03:57:59.000+0000"), Arrays.asList(new ChangelogItem(FieldType.JIRA, "Labels", "10003", "L1", "15752", "L2")));
        changeLogList.add(changelogGroup);
        changelogGroup = new ChangelogGroup(new BasicUser(new URI(""), "", "", ""), new DateTime("2023-02-28T03:57:59.000+0000"), Arrays.asList(new ChangelogItem(FieldType.CUSTOM, "Due Date", "2023-02-21", "2023-02-21 00:00:00.0", "2023-02-24", "2023-02-24 00:00:00.0")));
        changeLogList.add(changelogGroup);
        changelogGroup = new ChangelogGroup(new BasicUser(new URI(""), "", "", ""), new DateTime("2023-02-28T03:57:59.000+0000"), Arrays.asList(new ChangelogItem(FieldType.CUSTOM, "Sprint", "10003", "KnowHOW | PI_12| ITR_4, KnowHOW | PI_12| ITR_5", "15752", "KnowHOW | PI_12| ITR_5")));
        changeLogList.add(changelogGroup);

        issue = new Issue("summary1", new URI("self"), "key1", 1l, basicProj, issueType1, status1, "story",
                basicPriority, resolution, new ArrayList<>(), user1, user1, DateTime.now(), DateTime.now(),
                DateTime.now(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), null, issueFields, comments,
                null, createIssueLinkData(), basicVotes, workLogs, null, Arrays.asList("expandos"), null,
                changeLogList, null, new HashSet<>(Arrays.asList("label1")));
    }

    private List<IssueLink> createIssueLinkData() throws URISyntaxException {
        List<IssueLink> issueLinkList = new ArrayList<>();
        URI uri = new URI("https://testDomain.com/jira/rest/api/2/issue/12344");
        IssueLinkType linkType = new IssueLinkType("Blocks", "blocks", IssueLinkType.Direction.OUTBOUND);
        IssueLink issueLink = new IssueLink("IssueKey", uri, linkType);
        issueLinkList.add(issueLink);

        return issueLinkList;
    }

    private void createFieldsMap(boolean sprintStatus) {
        List<IssueField> issueFieldList = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("self", "https://jiradomain.com/jira/rest/api/2/customFieldOption/20810");
        map.put("value", "Client Testing (UAT)");
        map.put("id", "12121");
        IssueField issueField = new IssueField("customfield_12121", "UAT", null, new JSONObject(map));
        issueFieldList.add(issueField);
        fields.put("customfield_12121",issueField);

        JSONArray array = null;
        if (sprintStatus) {
            List<Object> sprintList = new ArrayList<>();
            String sprint = "com.atlassian.greenhopper.service.sprint.Sprint@6fc7072e[id=23356,rapidViewId=11649,state=CLOSED,name=TEST | 06 Jan - 19 Jan,startDate=2020-01-06T11:38:31.937Z,endDate=2020-01-19T11:38:00.000Z,completeDate=2020-01-20T11:15:21.528Z,sequence=22778,goal=]";
            sprintList.add(sprint);
            array = new JSONArray(sprintList);
        }
        issueField = new IssueField("customfield_12700", "Sprint", null, array);
        issueFieldList.add(issueField);
        fields.put("customfield_12700",issueField);

        List<String> list = new ArrayList<>();
        list.add("BrandName-12");
        issueField = new IssueField("customfield_48531", "Bran", null, new JSONArray(list));
        issueFieldList.add(issueField);
        fields.put("customfield_48531",issueField);

        issueField = new IssueField("customfield_56789", "StoryPoints", null, Integer.parseInt("5"));
        issueFieldList.add(issueField);
        fields.put("customfield_56789",issueField);

        map = new HashMap<>();
        map.put("self", "https://jiradomain.com/jira/rest/api/2/customFieldOption/20810");
        map.put("value", "TECH_DEBT");
        map.put("id", "14141");
        issueField = new IssueField("customfield_14141", "StoryPoints", null, new JSONObject(map));
        issueFieldList.add(issueField);
        fields.put("customfield_14141",issueField);

        map = new HashMap<>();
        map.put("self", "https://jiradomain.com/jira/rest/api/2/customFieldOption/20810");
        map.put("value", "Mobile");
        map.put("id", "18181");
        issueField = new IssueField("customfield_18181", "Device Platform", null, new JSONObject(map));
        issueFieldList.add(issueField);
        fields.put("customfield_18181",issueField);

        map = new HashMap<>();
        map.put("self", "https://jiradomain.com/jira/rest/api/2/customFieldOption/20810");
        map.put("value", "code");
        map.put("id", "19121");
        issueField = new IssueField("customfield_19121", "code_issue", null, new JSONObject(map));
        issueFieldList.add(issueField);
        fields.put("customfield_19121",issueField);

        map = new HashMap<>();
        map.put("self", "https://jiradomain.com/jira/rest/api/2/customFieldOption/20810");
        map.put("value", "stage");
        map.put("id", "13131");
        issueField = new IssueField("customfield_13131", "stage", null, new JSONObject(map));
        issueFieldList.add(issueField);
        fields.put("customfield_13131",issueField);

        List<JSONObject> jsonArrayList = new ArrayList<>();
        map = new HashMap<>();
        map.put("self", "https://jiradomain.com/jira/rest/api/2/customFieldOption/20810");
        map.put("value", "40");
        map.put("id", "Test_Automation");
        JSONObject jsonObject = new JSONObject(map);
        jsonArrayList.add(jsonObject);
        issueField = new IssueField("40", "Test_Automation", null, new JSONArray(jsonArrayList));
        issueFieldList.add(issueField);
        fields.put("40",issueField);

        fields.put("custom_007",new IssueField("","Fix Version",null,"KnowHowv6.7"));

//        Iterable<IssueField> issueFieldIterable = new Iterable<IssueField>() {
//            @Override
//            public Iterator<IssueField> iterator() {
//                return issueFieldList.iterator();
//            }
//        };
    }

}

//    @Test
//    public void testSetJiraFieldChangeLog1() {
//
//        handleJiraHistory.setJiraIssueCustomHistoryUpdationLog(jiraIssueCustomHistory, changeLogList, fieldMapping, fields, issue);
//        Assert.assertEquals(jiraIssueCustomHistory.getStatusUpdationLog().size(), 2);
//        Assert.assertEquals(jiraIssueCustomHistory.getAssigneeUpdationLog().size(), 2);
//        Assert.assertEquals(jiraIssueCustomHistory.getLabelUpdationLog().size(), 2);
//        Assert.assertEquals(jiraIssueCustomHistory.getFixVersionUpdationLog().size(), 3);
//        Assert.assertEquals(jiraIssueCustomHistory.getPriorityUpdationLog().size(), 2);
//        Assert.assertEquals(jiraIssueCustomHistory.getSprintUpdationLog().size(), 1);
//        Assert.assertEquals(jiraIssueCustomHistory.getDueDateUpdationLog().size(), 1);
//
//    }
//
//    @Test
//    public void testSetJiraFieldChangeLog2() {
//        if (ObjectUtils.isNotEmpty(changeLogList)) changeLogList.clear();
//        handleJiraHistory.setJiraIssueCustomHistoryUpdationLog(jiraIssueCustomHistory, changeLogList, fieldMapping, fields, issue);
//        Assert.assertEquals(jiraIssueCustomHistory.getStatusUpdationLog().size(), 1);
//        Assert.assertEquals(jiraIssueCustomHistory.getAssigneeUpdationLog().size(), 1);
//        Assert.assertEquals(jiraIssueCustomHistory.getLabelUpdationLog().size(), 0);
//        Assert.assertEquals(jiraIssueCustomHistory.getFixVersionUpdationLog().size(), 0);
//        Assert.assertEquals(jiraIssueCustomHistory.getPriorityUpdationLog().size(), 1);
//        Assert.assertEquals(jiraIssueCustomHistory.getDueDateUpdationLog().size(), 0);
//        Assert.assertEquals(jiraIssueCustomHistory.getSprintUpdationLog().size(), 0);
//    }
//
//    @Test
//    public void testJiraDueDateChangeLog() throws URISyntaxException {
//        fieldMapping.setJiraDueDateCustomField("");
//        fieldMapping.setJiraDueDateField("Due Date");
//        ChangelogGroup changelogGroup = new ChangelogGroup(new BasicUser(new URI(""), "", "", ""), new DateTime("2023-02-28T03:57:59.000+0000"), Arrays.asList(new ChangelogItem(FieldType.JIRA, "dueDate", "2023-02-21", "2023-02-21 00:00:00.0", "2023-02-24", "2023-02-24 00:00:00.0")));
//        changeLogList.add(changelogGroup);
//        handleJiraHistory.setJiraIssueCustomHistoryUpdationLog(jiraIssueCustomHistory, changeLogList, fieldMapping, fields, issue);
//        Assert.assertEquals(jiraIssueCustomHistory.getStatusUpdationLog().size(), 2);
//        Assert.assertEquals(jiraIssueCustomHistory.getAssigneeUpdationLog().size(), 2);
//        Assert.assertEquals(jiraIssueCustomHistory.getLabelUpdationLog().size(), 2);
//        Assert.assertEquals(jiraIssueCustomHistory.getFixVersionUpdationLog().size(), 3);
//        Assert.assertEquals(jiraIssueCustomHistory.getPriorityUpdationLog().size(), 2);
//        Assert.assertEquals(jiraIssueCustomHistory.getSprintUpdationLog().size(), 1);
//        Assert.assertEquals(jiraIssueCustomHistory.getDueDateUpdationLog().size(), 2);
//    }
//
//}
