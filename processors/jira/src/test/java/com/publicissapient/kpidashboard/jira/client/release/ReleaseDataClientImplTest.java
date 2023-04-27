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

package com.publicissapient.kpidashboard.jira.client.release;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.publicissapient.kpidashboard.common.model.application.ProjectVersion;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.atlassian.jira.rest.client.api.domain.Version;
import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.application.KanbanAccountHierarchy;
import com.publicissapient.kpidashboard.common.model.application.SubProjectConfig;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.KanbanAccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectReleaseRepo;
import com.publicissapient.kpidashboard.jira.adapter.JiraAdapter;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ReleaseDataClientImplTest {

    @Mock
    private JiraAdapter jiraAdapter;
    @Mock
    private AccountHierarchyRepository accountHierarchyRepository;
    @Mock
    private KanbanAccountHierarchyRepository kanbanAccountHierarchyRepo;
    @Mock
    ProjectReleaseRepo projectReleaseRepo;
    @InjectMocks
    private ReleaseDataClientImpl releaseDataClient;


    ProjectConfFieldMapping scrumProjectMapping = ProjectConfFieldMapping.builder().build();
    ProjectConfFieldMapping kanbanProjectMapping = ProjectConfFieldMapping.builder().build();
    AccountHierarchy accountHierarchy;
    KanbanAccountHierarchy kanbanAccountHierarchy;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        prepareAccountHierarchy();
        prepareKanbanAccountHierarchy();
        prepareProjectConfig();
    }

   @Test
    public void processReleaseInfo() {
        when(accountHierarchyRepository.findByLabelNameAndBasicProjectConfigId(Mockito.anyString(),
                any(ObjectId.class))).thenReturn(Arrays.asList(accountHierarchy));
        when(accountHierarchyRepository.findAll()).thenReturn(Arrays.asList(accountHierarchy));
        ProjectVersion version = new ProjectVersion();
        List<ProjectVersion> versionList = new ArrayList<>();
        version.setId(Long.valueOf("123"));
        version.setName("V1.0.2");
        version.setArchived(false);
        version.setReleased(true);
        version.setReleaseDate(DateTime.now());
       versionList.add(version);
        when(jiraAdapter.getVersion(any())).thenReturn(versionList);
        releaseDataClient.processReleaseInfo(scrumProjectMapping);
    }

    @Test
    public void processReleaseInfoNull() {
        when(accountHierarchyRepository.findByLabelNameAndBasicProjectConfigId("Project",
                scrumProjectMapping.getBasicProjectConfigId())).thenReturn(null);
        releaseDataClient.processReleaseInfo(scrumProjectMapping);
    }

    @Test
    public void processReleaseInfoForKanban() {
        when(kanbanAccountHierarchyRepo.findByLabelNameAndBasicProjectConfigId("Project",
                kanbanProjectMapping.getBasicProjectConfigId())).thenReturn(Arrays.asList(kanbanAccountHierarchy));
        ProjectVersion version = new ProjectVersion();
        List<ProjectVersion> versionList = new ArrayList<>();
        version.setId(Long.valueOf("123"));
        version.setName("V1.0.2");
        version.setArchived(false);
        version.setReleased(true);
        version.setReleaseDate(DateTime.now());
        versionList.add(version);
        when(jiraAdapter.getVersion(kanbanProjectMapping)).thenReturn(versionList);
        releaseDataClient.processReleaseInfo(kanbanProjectMapping);
    }

    private void prepareProjectConfig() {
        //Online Project Config data
        scrumProjectMapping.setBasicProjectConfigId(new ObjectId("5e15d8b195fe1300014538ce"));
        scrumProjectMapping.setProjectName("TEST Project Internal");
        SubProjectConfig subProjectConfig = new SubProjectConfig();
        subProjectConfig.setSubProjectIdentification("CustomField");
        subProjectConfig.setSubProjectIdentSingleValue("customfield_37903");
        scrumProjectMapping.setKanban(false);

        JiraToolConfig jiraToolConfig = new JiraToolConfig();
        jiraToolConfig.setBoardQuery("");
        jiraToolConfig.setQueryEnabled(false);
        jiraToolConfig.setProjectKey("TEST");

		scrumProjectMapping.setJira(jiraToolConfig);


        //Online Project Config data Kanban
        kanbanProjectMapping = ProjectConfFieldMapping.builder().build();
        kanbanProjectMapping.setBasicProjectConfigId(new ObjectId("5e1811cc0d248f0001ba6271"));
        kanbanProjectMapping.setProjectName("Tools-Atlassian Tools Support");
        subProjectConfig = new SubProjectConfig();
        subProjectConfig.setSubProjectIdentification("CustomField");
        subProjectConfig.setSubProjectIdentSingleValue("customfield_20810");
        kanbanProjectMapping.setKanban(true);
        kanbanProjectMapping.setJira(jiraToolConfig);

    }

    void prepareAccountHierarchy(){

        accountHierarchy =new AccountHierarchy();
        accountHierarchy.setId(new ObjectId("5e15d9d5e4b098db674614b8"));
        accountHierarchy.setNodeId("TEST_1234_TEST");
        accountHierarchy.setNodeName("TEST");
        accountHierarchy.setLabelName("Project");
        accountHierarchy.setFilterCategoryId(new ObjectId("5e15d7262b6a0532e258ce9c"));
        accountHierarchy.setParentId("25071_TestHow_61160fa56c1b4842c1741fe1");
        accountHierarchy.setBasicProjectConfigId(new  ObjectId("5e15d8b195fe1300014538ce"));
        accountHierarchy.setIsDeleted("False");
        accountHierarchy.setPath(("25071_TestHow_61160fa56c1b4842c1741fe1###TestHow_61160fa56c1b4842c1741fe1"));
    }

    void prepareKanbanAccountHierarchy(){

        kanbanAccountHierarchy =new KanbanAccountHierarchy();
        kanbanAccountHierarchy.setId(new ObjectId("5e15d9d5e4b098db674614b8"));
        kanbanAccountHierarchy.setNodeId("TEST_1234_TEST");
        kanbanAccountHierarchy.setNodeName("TEST");
        kanbanAccountHierarchy.setLabelName("Project");
        kanbanAccountHierarchy.setFilterCategoryId(new ObjectId("5e15d7262b6a0532e258ce9c"));
        kanbanAccountHierarchy.setParentId("25071_TestHow_61160fa56c1b4842c1741fe1");
        kanbanAccountHierarchy.setBasicProjectConfigId(new  ObjectId("5e15d8b195fe1300014538ce"));
        kanbanAccountHierarchy.setIsDeleted("False");
        kanbanAccountHierarchy.setPath(("25071_TestHow_61160fa56c1b4842c1741fe1###TestHow_61160fa56c1b4842c1741fe1"));


    }

}