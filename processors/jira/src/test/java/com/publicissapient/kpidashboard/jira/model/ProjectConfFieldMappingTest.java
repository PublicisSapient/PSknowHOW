package com.publicissapient.kpidashboard.jira.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.jira.client.jiraprojectmetadata.JiraIssueMetadata;

@RunWith(MockitoJUnitRunner.class)
public class ProjectConfFieldMappingTest {

	@Mock
	private JiraToolConfig jiraToolConfigMock;
	@Mock
	private FieldMapping fieldMappingMock;
	@Mock
	private ProjectToolConfig projectToolConfigMock;
	@Mock
	private ProjectBasicConfig projectBasicConfigMock;
	@Mock
	private JiraIssueMetadata jiraIssueMetadataMock;

	@Test
	public void testConstructorAndGetters() {
		ProjectConfFieldMapping mapping = new ProjectConfFieldMapping(jiraToolConfigMock, fieldMappingMock, null, true,
				42, 10, "ProjectName", projectToolConfigMock, null, projectBasicConfigMock, jiraIssueMetadataMock);

		assertEquals(jiraToolConfigMock, mapping.getJira());
		assertEquals(fieldMappingMock, mapping.getFieldMapping());
		assertTrue(mapping.isKanban());
		assertEquals(42, mapping.getIssueCount());
		assertEquals(10, mapping.getSprintCount());
		assertEquals("ProjectName", mapping.getProjectName());
		assertEquals(projectToolConfigMock, mapping.getProjectToolConfig());
		assertEquals(projectBasicConfigMock, mapping.getProjectBasicConfig());
		assertEquals(jiraIssueMetadataMock, mapping.getJiraIssueMetadata());
	}

	@Test
	public void testSetters() {
		ProjectConfFieldMapping mapping = new ProjectConfFieldMapping(jiraToolConfigMock, fieldMappingMock, null, true,
				42, 10, "ProjectName", projectToolConfigMock, null, projectBasicConfigMock, jiraIssueMetadataMock);

		assertEquals(jiraToolConfigMock, mapping.getJira());
		assertEquals(fieldMappingMock, mapping.getFieldMapping());
		assertTrue(mapping.isKanban());
		assertEquals("ProjectName", mapping.getProjectName());
		assertEquals(projectToolConfigMock, mapping.getProjectToolConfig());
		assertNotNull(mapping.getProjectBasicConfig());
		assertEquals(jiraIssueMetadataMock, mapping.getJiraIssueMetadata());
	}
}
