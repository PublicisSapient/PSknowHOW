package com.publicissapient.kpidashboard.jira.fetchData;

import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.model.application.*;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectReleaseRepo;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelService;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class FetchScrumReleaseDataImplTest {


    @Mock
    AccountHierarchyRepository accountHierarchyRepository;
    @Mock
    private HierarchyLevelService hierarchyLevelService;

    @Mock
    private JiraCommonService jiraCommonService;

    @InjectMocks
    private FetchScrumReleaseDataImpl fetchScrumReleaseData;

    @Mock
    private JiraProcessorConfig jiraProcessorConfig;

    @Mock
    KerberosClient krb5Client;

    ProjectConfFieldMapping scrumProjectMapping = ProjectConfFieldMapping.builder().build();
    List<AccountHierarchy> accountHierarchylist = new ArrayList<>();

    List<HierarchyLevel> hierarchyLevels= new ArrayList<>();

    @BeforeEach
    public void setUp() throws Exception {
        prepareAccountHierarchy();
        prepareProjectConfig();
        prepareHierarchyLevel();
    }

    private void prepareHierarchyLevel() {
        hierarchyLevels.add(new HierarchyLevel(5,"sprint","Sprint"));
        hierarchyLevels.add(new HierarchyLevel(5,"release","Release"));
        hierarchyLevels.add(new HierarchyLevel(4,"project","Project"));
        hierarchyLevels.add(new HierarchyLevel(3,"hierarchyLevelThree","Level Three"));
        hierarchyLevels.add(new HierarchyLevel(2,"hierarchyLevelTwo","Level Two"));
        hierarchyLevels.add(new HierarchyLevel(1,"hierarchyLevelOne","Level One"));
    }

    @Test
    void processReleaseInfo() {
        when(accountHierarchyRepository.findByLabelNameAndBasicProjectConfigId(Mockito.anyString(),
                any())).thenReturn(accountHierarchylist);
        when(accountHierarchyRepository.findAll()).thenReturn(accountHierarchylist);
        when(hierarchyLevelService.getFullHierarchyLevels(anyBoolean())).thenReturn(hierarchyLevels);
        ProjectVersion version = new ProjectVersion();
        List<ProjectVersion> versionList = new ArrayList<>();
        version.setId(Long.valueOf("123"));
        version.setName("V1.0.2");
        version.setArchived(false);
        version.setReleased(true);
        version.setReleaseDate(DateTime.now());
        versionList.add(version);
        when(jiraCommonService.getVersion(any(),any())).thenReturn(versionList);
        when(jiraProcessorConfig.getCustomApiBaseUrl()).thenReturn("http://customapi:8080/");
        Assert.assertNull(fetchScrumReleaseData.processReleaseInfo(scrumProjectMapping,krb5Client));
    }

    @Test
    void processReleaseInfoNull() {
        when(accountHierarchyRepository.findByLabelNameAndBasicProjectConfigId("Project",
                scrumProjectMapping.getBasicProjectConfigId())).thenReturn(null);
        Assert.assertNull(fetchScrumReleaseData.processReleaseInfo(scrumProjectMapping,krb5Client));
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
        ProjectBasicConfig projectBasicConfig = new ProjectBasicConfig();
        projectBasicConfig.setProjectName("TEST Project Internal");
        projectBasicConfig.setIsKanban(false);
        scrumProjectMapping.setProjectBasicConfig(projectBasicConfig);
        scrumProjectMapping.setJira(jiraToolConfig);
    }

    void prepareAccountHierarchy(){
        AccountHierarchy accountHierarchy =new AccountHierarchy();
        accountHierarchy.setId(new ObjectId("5e15d9d5e4b098db674614b8"));
        accountHierarchy.setNodeId("TEST_1234_TEST");
        accountHierarchy.setNodeName("TEST");
        accountHierarchy.setLabelName("Project");
        accountHierarchy.setFilterCategoryId(new ObjectId("5e15d7262b6a0532e258ce9c"));
        accountHierarchy.setParentId("25071_TestHow_61160fa56c1b4842c1741fe1");
        accountHierarchy.setBasicProjectConfigId(new  ObjectId("5e15d8b195fe1300014538ce"));
        accountHierarchy.setIsDeleted("False");
        accountHierarchy.setPath(("25071_TestHow_61160fa56c1b4842c1741fe1###TestHow_61160fa56c1b4842c1741fe1"));
        accountHierarchylist.add(accountHierarchy);
    }


}
