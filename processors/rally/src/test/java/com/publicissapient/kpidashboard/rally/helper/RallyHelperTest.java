package com.publicissapient.kpidashboard.rally.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.json.simple.JSONArray;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.atlassian.jira.rest.client.api.domain.ChangelogGroup;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.rally.constant.RallyConstants;
import com.publicissapient.kpidashboard.rally.model.HierarchicalRequirement;
import com.publicissapient.kpidashboard.rally.model.QueryResult;
import com.publicissapient.kpidashboard.rally.model.RallyResponse;

@ExtendWith(MockitoExtension.class)
public class RallyHelperTest {

    @Test
    public void testBuildFieldMap() {
        IssueField field1 = mock(IssueField.class);
        IssueField field2 = mock(IssueField.class);
        when(field1.getId()).thenReturn("field1");
        when(field2.getId()).thenReturn("field2");

        List<IssueField> fields = Arrays.asList(field1, field2);
        Map<String, IssueField> fieldMap = RallyHelper.buildFieldMap(fields);

        assertEquals(2, fieldMap.size());
        assertEquals(field1, fieldMap.get("field1"));
        assertEquals(field2, fieldMap.get("field2"));
    }

    @Test
    public void testGetLabelsList() {
        Issue issue = mock(Issue.class);
        when(issue.getLabels()).thenReturn((Set<String>) Arrays.asList("label1", "label2"));

        List<String> labels = RallyHelper.getLabelsList(issue);

        assertEquals(2, labels.size());
        assertTrue(labels.contains("label1"));
        assertTrue(labels.contains("label2"));
    }

    @Test
    public void testGetAffectedVersions() {
        Issue issue = mock(Issue.class);
        Version version1 = mock(Version.class);
        Version version2 = mock(Version.class);
        when(version1.getName()).thenReturn("v1.0");
        when(version2.getName()).thenReturn("v2.0");
        when(issue.getAffectedVersions()).thenReturn(Arrays.asList(version1, version2));

        List<String> versions = RallyHelper.getAffectedVersions(issue);

        assertEquals(2, versions.size());
        assertTrue(versions.contains("v1.0"));
        assertTrue(versions.contains("v2.0"));
    }

    @Test
    public void testGetFieldValueForDouble() {
        IssueField field = mock(IssueField.class);
        when(field.getValue()).thenReturn(10.5);

        Map<String, IssueField> fields = Map.of("customField", field);
        String value = RallyHelper.getFieldValue("customField", fields);

        assertEquals("10.5", value);
    }

    @Test
    public void testGetFieldValueForJSONObject() throws JSONException {
        IssueField field = mock(IssueField.class);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(RallyConstants.VALUE, "jsonValue");
        when(field.getValue()).thenReturn(jsonObject);

        Map<String, IssueField> fields = Map.of("customField", field);
        String value = RallyHelper.getFieldValue("customField", fields);

        assertEquals("jsonValue", value);
    }

    @Test
    public void testSortChangeLogGroup() {
        Issue issue = mock(Issue.class);
        ChangelogGroup group1 = mock(ChangelogGroup.class);
        ChangelogGroup group2 = mock(ChangelogGroup.class);
        when(group1.getCreated()).thenReturn(new DateTime(2023, 1, 1, 0, 0));
        when(group2.getCreated()).thenReturn(new DateTime(2023, 1, 2, 0, 0));
        when(issue.getChangelog()).thenReturn(Arrays.asList(group2, group1));

        List<ChangelogGroup> sortedGroups = RallyHelper.sortChangeLogGroup(issue);

        assertEquals(2, sortedGroups.size());
        assertEquals(group1, sortedGroups.get(0));
        assertEquals(group2, sortedGroups.get(1));
    }

    @Test
    public void testGetIssuesFromResult() {
        RallyResponse response = new RallyResponse();
        QueryResult queryResult = new QueryResult();
        List<HierarchicalRequirement> requirements = Arrays.asList(
            new HierarchicalRequirement(),
            new HierarchicalRequirement()
        );
        queryResult.setResults(requirements);
        response.setQueryResult(queryResult);

        List<HierarchicalRequirement> issues = RallyHelper.getIssuesFromResult(response);

        assertEquals(2, issues.size());
    }

    @Test
    public void testGetAssignee() {
        User user = mock(User.class);
        URI uri = URI.create("https://rally.com/rest/api/2/user?accountId=123456");
        when(user.getSelf()).thenReturn(uri);

        String assigneeId = RallyHelper.getAssignee(user);

        assertEquals("123456", assigneeId);
    }

    @Test
    public void testGetListFromJson() throws JSONException {
        IssueField field = mock(IssueField.class);
        JSONArray jsonArray = new JSONArray();
        JSONObject obj1 = new JSONObject();
        JSONObject obj2 = new JSONObject();
        obj1.put(RallyConstants.VALUE, "value1");
        obj2.put(RallyConstants.VALUE, "value2");
        jsonArray.add(obj1);
        jsonArray.add(obj2);
        when(field.getValue()).thenReturn(jsonArray);

        Collection result = RallyHelper.getListFromJson(field);

        assertEquals(2, result.size());
        assertTrue(result.contains("value1"));
        assertTrue(result.contains("value2"));
    }

    @Test
    public void testConvertDateToCustomFormat() {
        long timestamp = 1677667200000L; // March 1, 2023 12:00:00 AM UTC
        String formattedDate = RallyHelper.convertDateToCustomFormat(timestamp);
        assertNotNull(formattedDate);
        assertTrue(formattedDate.contains("March"));
        assertTrue(formattedDate.contains("2023"));
    }

    @Test
    public void testSprintComparator() {
        SprintDetails sprint1 = new SprintDetails();
        SprintDetails sprint2 = new SprintDetails();
        sprint1.setStartDate("2023-01-01");
        sprint1.setEndDate("2023-01-15");
        sprint2.setStartDate("2023-02-01");
        sprint2.setEndDate("2023-02-15");

        int result = RallyHelper.SPRINT_COMPARATOR.compare(sprint1, sprint2);
        assertTrue(result < 0);
    }
}
