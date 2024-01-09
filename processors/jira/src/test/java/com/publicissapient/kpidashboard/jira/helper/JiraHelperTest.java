package com.publicissapient.kpidashboard.jira.helper;

import com.atlassian.jira.rest.client.api.domain.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JiraHelperTest {

    @Mock
    Issue issue;

    @Mock
    ChangelogGroup changelogGroup;

    @Test
    public void testBuildFieldMap() {
        List<IssueField> fields = Arrays.asList(
                new IssueField("1", "Field1","h",new Object()),
                new IssueField("2", "Field2","h",new Object())
        );

        Map<String, IssueField> fieldMap = JiraHelper.buildFieldMap(fields);

        assertEquals(fields.size(), fieldMap.size());
        for (IssueField field : fields) {
            assertTrue(fieldMap.containsKey(field.getId()));
            assertEquals(field, fieldMap.get(field.getId()));
        }
    }

    @Test
    public void testBuildFieldMapWithNull() {
        Map<String, IssueField> fieldMap = JiraHelper.buildFieldMap(null);

        assertNotNull(fieldMap);
        assertTrue(fieldMap.isEmpty());
    }

    @Test
    public void getAffectedVersionsTest(){
        Version v =new Version(null, 1234567L,"v1","desc",true,false,null);
        Iterable<Version> iterable=new Iterable() {
            @Override
            public Iterator<Version> iterator() {
                return Collections.singletonList(v).iterator();
            }
        };
        when(issue.getAffectedVersions()).thenReturn(iterable );

        assertEquals(1,JiraHelper.getAffectedVersions(issue).size());
    }

//    @Test
//    public void testGetLabelsList() {
//
//        issue.setLabels(Arrays.asList("Label1", "Label2"));
//
//        List<String> labels = JiraHelper.getLabelsList(issue);
//
//        assertEquals(issue.getLabels().size(), labels.size());
//        assertTrue(labels.containsAll(issue.getLabels()));
//    }

    @Test
    public void testGetLabelsListWithNullLabels() {


        List<String> labels = JiraHelper.getLabelsList(issue);

        assertNotNull(labels);
        assertTrue(labels.isEmpty());
    }

    // Add more test methods for other methods in JiraHelper...

    @Test
    public void testSortChangeLogGroup() {

        when(issue.getChangelog()).thenReturn(Arrays.asList(changelogGroup));

        List<ChangelogGroup> sortedChangeLogList = JiraHelper.sortChangeLogGroup(issue);

        assertEquals(Arrays.asList(changelogGroup), sortedChangeLogList);
    }

    @Test
    public void testGetIssuesFromResult() {
        SearchResult searchResult = mock(SearchResult.class);

        when(searchResult.getIssues()).thenReturn(Arrays.asList(issue));

        List<Issue> issues = JiraHelper.getIssuesFromResult(searchResult);

        assertEquals(Arrays.asList(issue), issues);
    }

    @Test
    public void testHash() {
        String input = "TestInput";
        String hashedValue = JiraHelper.hash(input);

        assertNotNull(hashedValue);
        assertEquals(String.valueOf(Objects.hash(input)), hashedValue);
    }

    @Test
    public void testConvertDateToCustomFormat() {
        long currentTimeMillis = System.currentTimeMillis();
        String formattedDate = JiraHelper.convertDateToCustomFormat(currentTimeMillis);

        assertNotNull(formattedDate);
        // Add assertions based on your expected output format
    }
}
