package com.publicissapient.kpidashboard.jira.processor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.publicissapient.kpidashboard.jira.dataFactories.JiraIssueDataFactory;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.atlassian.jira.rest.client.api.domain.ChangelogGroup;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.jira.Assignee;
import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.repository.jira.AssigneeDetailsRepository;
import com.publicissapient.kpidashboard.jira.dataFactories.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

@RunWith(MockitoJUnitRunner.class)
public class JiraIssueAssigneeProcessorImplTest {

	JiraIssue jiraIssue;
	Set<Assignee> assigneeSetToSave = new HashSet<>();
	List<Issue> issues = new ArrayList<>();
	@Mock
	private AssigneeDetailsRepository assigneeDetailsRepository;
	@InjectMocks
	private JiraIssueAssigneeProcessorImpl createAssigneeDetails;
	@Mock
	private FieldMapping fieldMapping;
	private List<ChangelogGroup> changeLogList = new ArrayList<>();
	private AssigneeDetails assigneeDetails;

	@Before
	public void setUp() throws URISyntaxException {

		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/field_mapping.json");
		fieldMapping = fieldMappingDataFactory.findById("63bfa0f80b28191677615735");

		Assignee assignee = Assignee.builder().assigneeId("123").assigneeName("puru").build();
		assigneeSetToSave.add(assignee);

		assigneeDetails = AssigneeDetails.builder().assignee(assigneeSetToSave).basicProjectConfigId("123")
				.source("willNotReveal").build();

		jiraIssue = getMockJiraIssue();
	}

	@Test
	public void setAssigneeDetails() {

		when(assigneeDetailsRepository.findByBasicProjectConfigIdAndSource(any(), any())).thenReturn(assigneeDetails);
		createAssigneeDetails.createAssigneeDetails(createProjectConfig(), jiraIssue);
	}

	//@Test
	public void setAssigneeDetails2() {

		when(assigneeDetailsRepository.findByBasicProjectConfigIdAndSource(any(), any())).thenReturn(null);
		createAssigneeDetails.createAssigneeDetails(createProjectConfig(), jiraIssue);
	}

	private ProjectConfFieldMapping createProjectConfig() {
		ProjectConfFieldMapping projectConfFieldMapping = ProjectConfFieldMapping.builder().build();
		projectConfFieldMapping.setBasicProjectConfigId(new ObjectId("63c04dc7b7617e260763ca4e"));
		projectConfFieldMapping.setFieldMapping(fieldMapping);
		ProjectBasicConfig projectBasicConfig = ProjectBasicConfig.builder().build();
		projectBasicConfig.setSaveAssigneeDetails(false);
		projectConfFieldMapping.setProjectBasicConfig(projectBasicConfig);

		return projectConfFieldMapping;
	}

    private JiraIssue getMockJiraIssue() {
        JiraIssueDataFactory jiraIssueDataFactory = JiraIssueDataFactory.newInstance("/json/default/jira_issues.json");
        return jiraIssueDataFactory.findTopByBasicProjectConfigId("63c04dc7b7617e260763ca4e");
    }

}
