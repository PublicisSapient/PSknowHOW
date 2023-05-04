/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.jira.processor.mode.impl.offline;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.publicissapient.kpidashboard.jira.client.release.ReleaseDataClientFactory;
import com.publicissapient.kpidashboard.jira.client.release.ScrumReleaseDataClientImpl;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.application.SubProjectConfig;
import com.publicissapient.kpidashboard.common.model.application.Subproject;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueOfflineFileTraceLogs;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.KanbanAccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectReleaseRepo;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.SubProjectRepository;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.common.repository.jira.IssueOfflineTraceLogsRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueRepository;
import com.publicissapient.kpidashboard.common.service.ProcessorExecutionTraceLogService;
import com.publicissapient.kpidashboard.jira.adapter.JiraAdapter;
import com.publicissapient.kpidashboard.jira.client.jiraissue.JiraIssueClientFactory;
import com.publicissapient.kpidashboard.jira.client.jiraissue.KanbanJiraIssueClientImpl;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.JiraProcessor;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.repository.JiraProcessorRepository;
import com.publicissapient.kpidashboard.jira.util.AlphanumComparator;

@ExtendWith(SpringExtension.class)
@PrepareForTest(OfflineDataProcessorImpl.class)
public class OfflineDataProcessorImplTest {

    @Mock
    private JiraProcessorConfig jiraProcessorConfig;

    @Mock
    private IssueOfflineTraceLogsRepository issueOfflineTraceLogsRepository;

    @Mock
    private FieldMappingRepository fieldMappingRepository;

    @Mock
    private AlphanumComparator alphanumComparator;

    @Mock
    private JiraIssueClientFactory jiraIssueClientFactory;

    @Mock
    private KanbanJiraIssueRepository kanbanJiraRepo;
    @Mock
    JiraProcessor jiraProcessor;
    @Mock
    private KanbanJiraIssueHistoryRepository kanbanIssueHistoryRepo;

    @Mock
    private JiraProcessorRepository jiraProcessorRepository;
    
    @Mock
	private ProjectToolConfigRepository toolRepository;
    
    @Mock
	private ConnectionRepository connectionRepository;
    
    @Mock
	private SubProjectRepository subProjectRepository;
    @Mock
    private ProcessorExecutionTraceLogService processorExecutionTraceLogService;


    private AccountHierarchyRepository accountHierarchyRepository =Mockito.mock(AccountHierarchyRepository.class);
    private KanbanAccountHierarchyRepository kanbanAccountHierarchyRepo =Mockito.mock(KanbanAccountHierarchyRepository.class);
    private JiraAdapter jiraAdapter = Mockito.mock(JiraAdapter.class);
    private ProjectReleaseRepo projectReleaseRepo =Mockito.mock(ProjectReleaseRepo.class);
    @InjectMocks
    private ReleaseDataClientFactory releaseDataClientFactory;
    @InjectMocks
    private ScrumReleaseDataClientImpl releaseData;

    List<FieldMapping> fieldMappingList = new ArrayList<>();
    List<JiraIssueOfflineFileTraceLogs> traceLogsList = new ArrayList<>();


    @InjectMocks
    KanbanJiraIssueClientImpl kanbanJiraIssueClient = new KanbanJiraIssueClientImpl();
    @InjectMocks
    OfflineDataProcessorImpl offlineDataProcessor = new OfflineDataProcessorImpl();
    List<ProjectBasicConfig> scrumProjectList = new ArrayList<>();
    
    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        prepareFiledMapping();
        prepareProjectData();
        createOfflineTraceLog();
    }

    @Test
    public void validateAndCollectIssues() throws Exception {
    	List<ProjectToolConfig> projectToolConfigList=new ArrayList<>();
    	ProjectToolConfig projectToolConfig=new ProjectToolConfig();
    	projectToolConfig.setBasicProjectConfigId(new ObjectId("5ba8e182d3735010e7f1fa45"));
    	projectToolConfig.setConnectionId(new ObjectId("5b719d06a500d00814bfb2b9"));
    	projectToolConfig.setToolName(ProcessorConstants.JIRA);
    	projectToolConfig.setProjectKey("TestProject");
    	projectToolConfigList.add(projectToolConfig);
    	Optional<Connection> conn=Optional.ofNullable(new Connection());
    	conn.get().setOffline(Boolean.TRUE);
    	
    	List<Subproject> subProjectsList=new ArrayList<>();
    	Subproject subproject=new Subproject();
    	subproject.setBasicProjectConfigId(new ObjectId("5ba8e182d3735010e7f1fa45"));
    	subproject.setToolConfigId(new ObjectId("5b674d58f47cae8935b1b26f"));
    	subProjectsList.add(subproject);
    	

    	doNothing().when(processorExecutionTraceLogService).save(any(ProcessorExecutionTraceLog.class));
        when(fieldMappingRepository.findAll()).thenReturn(fieldMappingList);
        String currentDirectory = System.getProperty("user.dir");
        String localDir = currentDirectory + "/src/test/resources/offlineData";
        when(jiraProcessorConfig.getJsonFileName()).thenReturn(localDir);
        when(issueOfflineTraceLogsRepository.findAll()).thenReturn(traceLogsList);
        PowerMockito.whenNew(ScrumReleaseDataClientImpl.class).withAnyArguments().thenReturn(releaseData);
        when(jiraIssueClientFactory.getJiraIssueDataClient(any(ProjectConfFieldMapping.class))).thenReturn(kanbanJiraIssueClient);
        when(jiraProcessorConfig.getStartDate()).thenReturn("2019-01-07 00:00");
        when(jiraProcessorRepository.findByProcessorName(ProcessorConstants.JIRA)).thenReturn(jiraProcessor);
        when(jiraProcessor.getId()).thenReturn(new ObjectId("5e16c126e4b098db673cc372"));
        when(toolRepository.findByToolNameAndBasicProjectConfigId(any(), any())).thenReturn(projectToolConfigList);
        when(connectionRepository.findById(any())).thenReturn(conn);
        when(subProjectRepository.findBybasicProjectConfigIdIn(any())).thenReturn(subProjectsList);
        offlineDataProcessor.validateAndCollectIssues(scrumProjectList);
    }



	private void prepareProjectData(){
        ProjectBasicConfig projectConfig = new ProjectBasicConfig();
        //Online Project Config data
        projectConfig.setId(new ObjectId("5e15d8b195fe1300014538ce"));
        projectConfig.setProjectName("TestProject");
        SubProjectConfig subProjectConfig = new SubProjectConfig();
        subProjectConfig.setSubProjectIdentification("CustomField");
        subProjectConfig.setSubProjectIdentSingleValue("customfield_37903");
        List<SubProjectConfig> subProjectList = new ArrayList<>();
        subProjectList.add(subProjectConfig);
        ProjectToolConfig jiraConfig = new ProjectToolConfig();
        jiraConfig.setProjectKey("TestProject");
        projectConfig.setIsKanban(false);
        scrumProjectList.add(projectConfig);
    }


    private void prepareFiledMapping() {
        FieldMapping fieldMapping = new FieldMapping();
        fieldMapping.setBasicProjectConfigId(new ObjectId("5e15d8b195fe1300014538ce"));
        fieldMapping.setSprintName("customfield_12700");
        List<String> jiraType = new ArrayList<>();
        jiraType.add("Defect");
        fieldMapping.setJiradefecttype(jiraType);
        jiraType = new ArrayList<>(Arrays.asList(new String[]{"Story", "Defect", "Pre Story", "Feature", "Enabler Story"}));
        String[] jiraIssueType = new String[]{"Story", "Defect", "Pre Story", "Feature", "Enabler Story"};
        fieldMapping.setJiraIssueTypeNames(jiraIssueType);
        fieldMapping.setRootCause("customfield_19121");

        jiraType = new ArrayList<>();
        jiraType.add("Story");
        fieldMapping.setJiraDefectInjectionIssueType(jiraType);
        fieldMapping.setJiraTechDebtIssueType(jiraType);
        fieldMapping.setJiraDefectSeepageIssueType(jiraType);
        fieldMapping.setJiraDefectRemovalStatus(jiraType);
        fieldMapping.setJiraDefectRejectionlIssueType(jiraType);
        fieldMapping.setJiraTestAutomationIssueType(jiraType);
        fieldMapping.setJiraDefectRejectionlIssueType(jiraType);
        fieldMapping.setJiraDefectCountlIssueType(jiraType);
        fieldMapping.setJiraIntakeToDorIssueType(jiraType);
        fieldMapping.setJiraBugRaisedByCustomField("customfield_12121");

        fieldMapping.setJiraTechDebtIdentification("Labels");

        jiraType = new ArrayList<>();
        jiraType.add("TECH_DEBT");
        fieldMapping.setJiraTechDebtValue(jiraType);
        fieldMapping.setJiraDefectRejectionStatus("Dropped");
        fieldMapping.setJiraBugRaisedByIdentification("CustomField");

        jiraType = new ArrayList<>();
        jiraType.add("Ready for Sign-off");
        fieldMapping.setJiraDod(jiraType);

        jiraType = new ArrayList<>();
        jiraType.add("Closed");
        fieldMapping.setJiraDefectRemovalStatus(jiraType);

        fieldMapping.setJiraStoryPointsCustomField("customfield_20803");

        jiraType = new ArrayList<>();
        jiraType.add("Automation");


        jiraType = new ArrayList<>();
        jiraType.add("Client Testing (UAT)");
        fieldMapping.setJiraBugRaisedByValue(jiraType);

        jiraType = new ArrayList<>();
        jiraType.add("Story");
        jiraType.add("Feature");
        fieldMapping.setJiraSprintVelocityIssueType(jiraType);

        jiraType = new ArrayList<>(Arrays.asList(new String[]{"Story", "Defect", "Pre Story", "Feature"}));
        fieldMapping.setJiraSprintCapacityIssueType(jiraType);

        jiraType = new ArrayList<>();
        jiraType.add("Closed");
        fieldMapping.setJiraIssueDeliverdStatus(jiraType);

        fieldMapping.setJiraDor("In Progress");
        fieldMapping.setJiraLiveStatus("Closed");
        fieldMapping.setRootCauseValue(Arrays.asList("Coding","None"));

        jiraType = new ArrayList<>(Arrays.asList(new String[]{"Story", "Pre Story"}));
        fieldMapping.setJiraStoryIdentification(jiraType);

        fieldMapping.setJiraDefectCreatedStatus("Open");

        jiraType = new ArrayList<>();
        jiraType.add("Ready for Sign-off");
        fieldMapping.setJiraDod(jiraType);
        jiraType = new ArrayList<>();
        jiraType.add("2.1.0");


        List<String> jiraSegData = new ArrayList<>();
        jiraSegData.add("Tech Story");
        jiraSegData.add("Task");
    
        jiraSegData = new ArrayList<>();
        jiraSegData.add("Component");

        fieldMappingList.add(fieldMapping);

        //FieldMapping on 2nd project

        fieldMapping = new FieldMapping();
        fieldMapping.setBasicProjectConfigId(new ObjectId("5e1811cc0d248f0001ba6271"));
        jiraType = new ArrayList<>();
        jiraType.add("Defect");
        fieldMapping.setJiradefecttype(jiraType);

        jiraIssueType = new String[]{"Support Request", "Incident", "Project Request", "Member Account Request", "Test Consulting Request","Test Case"};
        fieldMapping.setJiraIssueTypeNames(jiraIssueType);
        fieldMapping.setStoryFirstStatus("Open");

        fieldMapping.setRootCause("customfield_19121");


        fieldMapping.setJiraDefectRejectionStatus("Dropped");
        fieldMapping.setJiraBugRaisedByIdentification("CustomField");

        jiraType = new ArrayList<>();
        jiraType.add("Ready for Sign-off");
        fieldMapping.setJiraDod(jiraType);

        jiraType = new ArrayList<>();
        jiraType.add("Closed");
        fieldMapping.setJiraDefectRemovalStatus(jiraType);

        fieldMapping.setJiraStoryPointsCustomField("customfield_20803");
        fieldMapping.setJiraTechDebtIdentification("CustomField");

        jiraType = new ArrayList<>(Arrays.asList(new String[]{"Support Request", "Incident", "Project Request", "Member Account Request", "Test Consulting Request","Test Case"}));
        fieldMapping.setTicketCountIssueType(jiraType);
        fieldMapping.setJiraTicketVelocityIssueType(jiraType);
        fieldMapping.setKanbanJiraTechDebtIssueType(jiraType);
        fieldMapping.setKanbanCycleTimeIssueType(jiraType);

        jiraType = new ArrayList<>();
        jiraType.add("Resolved");
        fieldMapping.setTicketDeliverdStatus(jiraType);
        fieldMapping.setJiraTicketResolvedStatus(jiraType);

        jiraType = new ArrayList<>();
        jiraType.add("Reopen");
        fieldMapping.setTicketReopenStatus(jiraType);

        jiraType = new ArrayList<>();
        jiraType.add("Closed");
        fieldMapping.setJiraTicketClosedStatus(jiraType);

        jiraType = new ArrayList<>();
        jiraType.add("Assigned");
        fieldMapping.setJiraTicketTriagedStatus(jiraType);


        fieldMapping.setJiraLiveStatus("Closed");
        fieldMapping.setRootCauseValue(Arrays.asList("Coding","None"));

        fieldMapping.setEpicName("customfield_14502");
        jiraType = new ArrayList<>();
        jiraType.add("Ready for Sign-off");
        fieldMapping.setJiraDod(jiraType);

        

        jiraSegData = new ArrayList<>();
        jiraSegData.add("Tech Story");
        jiraSegData.add("Task");
      
        jiraSegData = new ArrayList<>();
        jiraSegData.add("In Analysis");
        jiraSegData.add("In Development");
        fieldMapping.setJiraStatusForDevelopment(jiraSegData);

        jiraType = new ArrayList<>();
        jiraType.add("AutomationAutomation");

        jiraSegData = new ArrayList<>();
        jiraSegData.add("2.1.0");


        jiraSegData = new ArrayList<>();
        jiraSegData.add("customfield_20810");
        fieldMappingList.add(fieldMapping);

    }

    public void createOfflineTraceLog(){
        JiraIssueOfflineFileTraceLogs traceLogs = new JiraIssueOfflineFileTraceLogs();

        traceLogs.setFileName("Test.txt");
        traceLogs.setDate(DateTime.now());
        traceLogs.setId("5ce50d75a1de1800111a995c");
        traceLogs.setStatus("Uploaded");
        traceLogsList.add(traceLogs);
    }
}
