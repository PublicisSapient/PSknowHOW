package com.publicissapient.kpidashboard.jira.fetchData;

import com.atlassian.jira.rest.client.api.StatusCategory;
import com.atlassian.jira.rest.client.api.domain.*;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.jira.Assignee;
import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.repository.jira.AssigneeDetailsRepository;
import com.publicissapient.kpidashboard.jira.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.jira.data.JiraIssueDataFactory;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import org.bson.types.ObjectId;
import org.checkerframework.checker.units.qual.A;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class CreateAssigneeDetailsImplTest {

    @Mock
    private AssigneeDetailsRepository assigneeDetailsRepository;

    @InjectMocks
    private CreateAssigneeDetailsImpl createAssigneeDetails;

    @Mock
    private FieldMapping fieldMapping;

    private List<ChangelogGroup> changeLogList = new ArrayList<>();

    Set<Assignee> assigneeSetToSave = new HashSet<>();

    private AssigneeDetails assigneeDetails;

    @Before
    public void setUp() throws URISyntaxException {

        FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory.newInstance("/json/default/field_mapping.json");
        fieldMapping = fieldMappingDataFactory.findById("63bfa0f80b28191677615735");

        Assignee assignee=Assignee.builder()
                .assigneeId("123")
                .assigneeName("puru").build();
        assigneeSetToSave.add(assignee);

        assigneeDetails= AssigneeDetails.builder()
                .assignee(assigneeSetToSave)
                .basicProjectConfigId("123")
                .source("willNotReveal").build();

    }

    @Test
    public void setAssigneeDetails(){

        when(assigneeDetailsRepository.findByBasicProjectConfigIdAndSource(any(),any())).thenReturn(assigneeDetails);
        createAssigneeDetails.createAssigneeDetails(createProjectConfig(),assigneeSetToSave);
    }

    @Test
    public void setAssigneeDetails2(){

        when(assigneeDetailsRepository.findByBasicProjectConfigIdAndSource(any(),any())).thenReturn(null);
        createAssigneeDetails.createAssigneeDetails(createProjectConfig(),assigneeSetToSave);
    }

    private ProjectConfFieldMapping createProjectConfig(){
        ProjectConfFieldMapping projectConfFieldMapping=ProjectConfFieldMapping.builder().build();
        projectConfFieldMapping.setBasicProjectConfigId(new ObjectId("63c04dc7b7617e260763ca4e"));
        projectConfFieldMapping.setFieldMapping(fieldMapping);
        ProjectBasicConfig projectBasicConfig= ProjectBasicConfig.builder().build();
        projectBasicConfig.setSaveAssigneeDetails(true);
        projectConfFieldMapping.setProjectBasicConfig(projectBasicConfig);

        return projectConfFieldMapping;
    }
}
