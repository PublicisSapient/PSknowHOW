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

package com.publicissapient.kpidashboard.azure.processor.mode.impl.online;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.BeanUtils;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.atlassian.jira.rest.client.api.MetadataRestClient;
import com.atlassian.jira.rest.client.api.domain.Field;
import com.atlassian.jira.rest.client.api.domain.FieldType;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.atlassian.util.concurrent.Promise;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.publicissapient.kpidashboard.azure.adapter.AzureAdapter;
import com.publicissapient.kpidashboard.azure.adapter.helper.AzureRestClientFactory;
import com.publicissapient.kpidashboard.azure.adapter.impl.OnlineAdapter;
import com.publicissapient.kpidashboard.azure.adapter.impl.async.ProcessorAzureRestClient;
import com.publicissapient.kpidashboard.azure.client.azureissue.AzureIssueClient;
import com.publicissapient.kpidashboard.azure.client.azureissue.AzureIssueClientFactory;
import com.publicissapient.kpidashboard.azure.client.azureissue.KanbanAzureIssueClientImpl;
import com.publicissapient.kpidashboard.azure.client.azureissue.ScrumAzureIssueClientImpl;
import com.publicissapient.kpidashboard.azure.client.metadata.MetaDataClientImpl;
import com.publicissapient.kpidashboard.azure.config.AzureProcessorConfig;
import com.publicissapient.kpidashboard.azure.model.AzureProcessor;
import com.publicissapient.kpidashboard.azure.model.AzureServer;
import com.publicissapient.kpidashboard.azure.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.azure.repository.AzureProcessorRepository;
import com.publicissapient.kpidashboard.azure.util.AzureConstants;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.azureboards.AzureBoardsWIModel;
import com.publicissapient.kpidashboard.common.model.azureboards.iterations.AzureIterationsModel;
import com.publicissapient.kpidashboard.common.model.azureboards.updates.AzureUpdatesModel;
import com.publicissapient.kpidashboard.common.model.azureboards.wiql.AzureWiqlModel;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.Identifier;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.MetadataIdentifier;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.KanbanAccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectReleaseRepo;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.KanbanJiraIssueRepository;
import com.publicissapient.kpidashboard.common.repository.jira.MetadataIdentifierRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;

@ExtendWith(SpringExtension.class)
public class OnlineDataProcessorImplTest {

	private static final String PLAIN_TEXT_PASSWORD = "testPat";
	List<ProjectBasicConfig> scrumProjectList = new ArrayList<>();
	List<ProjectBasicConfig> kanbanProjectlist = new ArrayList<>();
	List<FieldMapping> fieldMappingList = new ArrayList<>();
	@Mock
	AzureProcessor azureProcessor;
	@Mock
	ProcessorAzureRestClient client;
	@Mock
	AzureIssueClient kanbanJiraIssueClient = new KanbanAzureIssueClientImpl();
	@Mock
	AzureIssueClient scrumJiraIssueClient = new ScrumAzureIssueClientImpl();
	@InjectMocks
	OnlineDataProcessorImpl onlineDataProcessor;
	@InjectMocks
	OnlineAdapter adapter;
	@InjectMocks
	MetaDataClientImpl metadataImpl;
	@Mock
	AzureAdapter azureAdapter;
	@Mock
	Promise<Iterable<Field>> metaDataFieldPromise;
	@Mock
	Promise<Iterable<Status>> metaDataStatusPromise;
	List<ProjectToolConfig> projectToolConfigList = new ArrayList<>();
	AccountHierarchy accountHierarchy;
	List<ProjectConfFieldMapping> projectConfFieldMappingList = new ArrayList<>();
	ProjectConfFieldMapping projectConfFieldMapping = ProjectConfFieldMapping.builder().build();
	ProjectConfFieldMapping projectConfFieldMapping2 = ProjectConfFieldMapping.builder().build();
	@Mock
	Promise<Iterable<IssueType>> metaDataIssueTypePromise;
	@Mock
	private AzureProcessorConfig azureProcessorConfig;
	@Mock
	private JiraIssueCustomHistoryRepository azureIssueCustomHistoryRepository;
	@Mock
	private FieldMappingRepository fieldMappingRepository;
	@Mock
	private KanbanAccountHierarchyRepository kanbanAccountHierarchyRepo;
	@Mock
	private KanbanJiraIssueHistoryRepository kanbanIssueHistoryRepo;
	@Mock
	private KanbanJiraIssueRepository kanbanJiraRepo;
	@Mock
	private ProjectReleaseRepo projectReleaseRepo;
	@Mock
	private AccountHierarchyRepository accountHierarchyRepository;
	@Mock
	private AzureProcessorRepository azureProcessorRepository;
	@Mock
	private AzureRestClientFactory azureRestClientFactory;
	@Mock
	private JiraIssueRepository azureIssueRepository;
	@Mock
	private AzureIssueClientFactory azureIssueClientFactory;
	@Mock
	private ProjectToolConfigRepository toolRepository;
	@Mock
	private MetadataIdentifierRepository metadataIdentifierRepository;
	@Mock
	private ConnectionRepository connectionRepository;

	@Mock
	private AesEncryptionService aesEncryptionService;

	@BeforeEach
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		prepareProjectConfig();
		prepareFieldMapping();
		setProjectConfigFieldMap();
		when(aesEncryptionService.decrypt(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
				.thenReturn(PLAIN_TEXT_PASSWORD);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void validateAndCollectIssuesScrum()
			throws URISyntaxException, JsonParseException, JsonMappingException, IOException {
		prepareAccountHierarchy();

		when(fieldMappingRepository.findAll()).thenReturn(fieldMappingList);
		when(azureProcessorConfig.getThreadPoolSize()).thenReturn(3);
		when(azureProcessorConfig.getStartDate()).thenReturn("2019-01-07T00:00:00.000000");
		when(azureIssueClientFactory.getAzureIssueDataClient(any())).thenReturn(scrumJiraIssueClient);
		when(azureProcessorConfig.getMinsToReduce()).thenReturn(30);
		when(azureProcessorConfig.getPageSize()).thenReturn(4);
		when(azureProcessorConfig.getEstimationCriteria()).thenReturn("StoryPoints");
		when(toolRepository.findByToolNameAndBasicProjectConfigId(any(), any())).thenReturn(prepareProjectToolConfig());
		when(connectionRepository.findById(any())).thenReturn(returnConnectionObject());

		when(azureProcessorRepository.findByProcessorName(ProcessorConstants.AZURE)).thenReturn(azureProcessor);
		when(azureProcessor.getId()).thenReturn(new ObjectId("5f0c1e1c204347d129590ef8"));
		when(azureProcessorConfig.getEstimationCriteria()).thenReturn("StoryPoints");
		when(azureIssueRepository.findByIssueIdAndBasicProjectConfigId(any(), any()))
				.thenReturn(new JiraIssue());
		when(azureIssueCustomHistoryRepository.findByStoryIDAndBasicProjectConfigId(any(), any()))
				.thenReturn(new JiraIssueCustomHistory());

		when(accountHierarchyRepository.findByLabelNameAndBasicProjectConfigId("Project",
				scrumProjectList.get(0).getId())).thenReturn(Arrays.asList(accountHierarchy));
		MetadataRestClient metadataRestClient = mock(MetadataRestClient.class);

		Field field1 = new Field("Story Points", "customfield_20803", FieldType.JIRA, true, true, true, null);
		Field field2 = new Field("Sprint", "customfield_12700", FieldType.JIRA, true, true, true, null);
		Field field3 = new Field("Root Cause", "customfield_19121", FieldType.JIRA, true, true, true, null);
		Field field4 = new Field("Tech Debt", "customfield_59601", FieldType.JIRA, true, true, true, null);
		Field field5 = new Field("UAT", "UAT", FieldType.JIRA, true, true, true, null);
		List<Field> fields = Arrays.asList(field1, field2, field3, field4, field5);

		Iterable<Field> fieldItr = fields;
		when(metadataRestClient.getFields()).thenReturn(metaDataFieldPromise);
		when(metaDataFieldPromise.claim()).thenReturn(fieldItr);

		IssueType issueType1 = new IssueType(new URI("self"), 1l, "Story", false, "desc", new URI("iconURI"));
		IssueType issueType2 = new IssueType(new URI("self"), 1l, "Enabler Story", false, "desc", new URI("iconURI"));
		IssueType issueType3 = new IssueType(new URI("self"), 1l, "Tech Story", false, "desc", new URI("iconURI"));
		IssueType issueType4 = new IssueType(new URI("self"), 1l, "Change request", false, "desc", new URI("iconURI"));
		IssueType issueType5 = new IssueType(new URI("self"), 1l, "Defect", false, "desc", new URI("iconURI"));
		IssueType issueType6 = new IssueType(new URI("self"), 1l, "Epic", false, "desc", new URI("iconURI"));
		IssueType issueType7 = new IssueType(new URI("self"), 1l, "UAT Defect", false, "desc", new URI("iconURI"));
		List<IssueType> issueTypes = Arrays.asList(issueType1, issueType2, issueType3, issueType4, issueType5,
				issueType6, issueType7);

		Iterable<IssueType> issueTypeItr = issueTypes;
		when(metadataRestClient.getIssueTypes()).thenReturn(metaDataIssueTypePromise);
		when(metaDataIssueTypePromise.claim()).thenReturn(issueTypeItr);
		Status status1 = new Status(new URI("self"), 1l, "Ready for Sprint Planning", "desc", new URI("iconURI"));
		Status status2 = new Status(new URI("self"), 1l, "Closed", "desc", new URI("iconURI"));
		Status status3 = new Status(new URI("self"), 1l, "Implementing", "desc", new URI("iconURI"));
		Status status4 = new Status(new URI("self"), 1l, "In Testing", "desc", new URI("iconURI"));
		List<Status> statuses = Arrays.asList(status1, status2, status3, status4);
		Iterable<Status> statusItr = statuses;
		when(metadataRestClient.getStatuses()).thenReturn(metaDataStatusPromise);
		when(metaDataStatusPromise.claim()).thenReturn(statusItr);

		MetadataIdentifier metadataIdentifier = createMetaDataIdentifier();
		when(metadataIdentifierRepository.findByToolAndIsKanban(any(), any())).thenReturn(metadataIdentifier);

		when(client.getWiqlResponse(any(AzureServer.class), any(Map.class), any(ProjectConfFieldMapping.class),
				any(Boolean.class))).thenReturn(createWiqlResponse());
		when(client.getIterationsResponse(Mockito.any())).thenReturn(createIterationsResponse());
		when(client.getWorkItemInfo(Mockito.any(), Mockito.any())).thenReturn(createWorkItemInfoResponse());
		when(client.getUpdatesResponse(Mockito.any(), Mockito.anyString())).thenReturn(createUpdatesResponse());
		onlineDataProcessor.validateAndCollectIssues(scrumProjectList);

	}

	private AzureWiqlModel createWiqlResponse() throws JsonParseException, JsonMappingException, IOException {
		String filePath = "src/test/resources/onlinedata/azure/azurewiqlmodel.json";
		File file = new File(filePath);
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		AzureWiqlModel azureWiqlModel = objectMapper.readValue(file, AzureWiqlModel.class);
		return azureWiqlModel;
	}

	private AzureIterationsModel createIterationsResponse()
			throws JsonParseException, JsonMappingException, IOException {
		String filePath = "src/test/resources/onlinedata/azure/azureiterationsmodel.json";
		File file = new File(filePath);
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		AzureIterationsModel azureIterationsModel = objectMapper.readValue(file, AzureIterationsModel.class);
		return azureIterationsModel;

	}

	private AzureBoardsWIModel createWorkItemInfoResponse()
			throws JsonParseException, JsonMappingException, IOException {
		String filePath = "src/test/resources/onlinedata/azure/azureworkitemmodel.json";
		File file = new File(filePath);
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		AzureBoardsWIModel azureWIModel = objectMapper.readValue(file, AzureBoardsWIModel.class);
		return azureWIModel;
	}

	private AzureUpdatesModel createUpdatesResponse() throws JsonParseException, JsonMappingException, IOException {
		String filePath = "src/test/resources/onlinedata/azure/azureupdatesmodel.json";
		File file = new File(filePath);
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		AzureUpdatesModel azureUpdateModel = objectMapper.readValue(file, AzureUpdatesModel.class);
		return azureUpdateModel;
	}

	private void prepareProjectConfig() throws JsonParseException, JsonMappingException, IOException {

		String filePath = "src/test/resources/onlinedata/azure/scrumprojectconfig.json";
		File file = new File(filePath);
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		ProjectBasicConfig projectConfig = objectMapper.readValue(file, ProjectBasicConfig.class);
		scrumProjectList.add(projectConfig);

		String kanbanFilePath = "src/test/resources/onlinedata/azure/kanbanprojectconfig.json";
		File kanbanFile = new File(kanbanFilePath);
		ObjectMapper kanbanObjectMapper = new ObjectMapper();
		kanbanObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		ProjectBasicConfig kanbanProjectConfig = kanbanObjectMapper.readValue(kanbanFile, ProjectBasicConfig.class);
		kanbanProjectlist.add(kanbanProjectConfig);

	}

	private void setProjectConfigFieldMap() throws IllegalAccessException, InvocationTargetException {

		BeanUtils.copyProperties(projectConfFieldMapping, scrumProjectList.get(0));
		projectConfFieldMapping.setBasicProjectConfigId(scrumProjectList.get(0).getId());
		projectConfFieldMapping.setFieldMapping(fieldMappingList.get(0));

		BeanUtils.copyProperties(projectConfFieldMapping2, kanbanProjectlist.get(0));
		projectConfFieldMapping2.setKanban(true);
		projectConfFieldMapping2.setBasicProjectConfigId(kanbanProjectlist.get(0).getId());
		projectConfFieldMapping2.setFieldMapping(fieldMappingList.get(1));

		projectConfFieldMappingList.add(projectConfFieldMapping);
		projectConfFieldMappingList.add(projectConfFieldMapping2);
	}

	private void prepareFieldMapping() throws JsonParseException, JsonMappingException, IOException {

		String filePath = "src/test/resources/onlinedata/azure/scrumfieldmapping.json";
		File file = new File(filePath);
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		FieldMapping fieldMapping = objectMapper.readValue(file, FieldMapping.class);

		fieldMappingList.add(fieldMapping);

		// FieldMapping on 2nd project

		String kanbanFilePath = "src/test/resources/onlinedata/azure/kanbanfieldmapping.json";
		File kanbanFile = new File(kanbanFilePath);
		ObjectMapper kanbanObjectMapper = new ObjectMapper();
		kanbanObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		FieldMapping kanbanFieldMapping = kanbanObjectMapper.readValue(kanbanFile, FieldMapping.class);
		fieldMappingList.add(kanbanFieldMapping);
	}

	void prepareAccountHierarchy() throws JsonParseException, JsonMappingException, IOException {

		String filePath = "src/test/resources/onlinedata/azure/accounthierarchy.json";
		File file = new File(filePath);
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		accountHierarchy = objectMapper.readValue(file, AccountHierarchy.class);

	}

	@Test
	public void validateAndCollectIssuesKanban() throws JsonParseException, JsonMappingException,
			IOException, IllegalAccessException, InvocationTargetException, ParseException {
		when(azureProcessorConfig.getStartDate()).thenReturn("2019-01-07T00:00:00.0000000");
		LocalDateTime configuredStartDate = LocalDateTime.parse(azureProcessorConfig.getStartDate(),
				DateTimeFormatter.ofPattern(AzureConstants.JIRA_ISSUE_CHANGE_DATE_FORMAT));
		Map<String, LocalDateTime> time = new HashMap();
		time.put("User Story", configuredStartDate);
		time.put("Issue", configuredStartDate);
		when(fieldMappingRepository.findAll()).thenReturn(fieldMappingList);
		when(azureProcessorConfig.getThreadPoolSize()).thenReturn(3);
		when(azureIssueClientFactory.getAzureIssueDataClient(any())).thenReturn(kanbanJiraIssueClient);

		when(azureProcessorConfig.getMinsToReduce()).thenReturn(30);
		when(azureProcessorConfig.getPageSize()).thenReturn(4);
		when(azureProcessorConfig.getEstimationCriteria()).thenReturn("StoryPoints");
		when(toolRepository.findByToolNameAndBasicProjectConfigId(any(), any())).thenReturn(prepareProjectToolConfig());
		when(connectionRepository.findById(any())).thenReturn(returnConnectionObject());
		when(azureProcessorRepository.findByProcessorName(ProcessorConstants.AZURE)).thenReturn(azureProcessor);
		when(kanbanJiraRepo.findByIssueId(any())).thenReturn(new ArrayList<KanbanJiraIssue>());
		when(kanbanIssueHistoryRepo.findByStoryIDAndBasicProjectConfigId(any(), any()))
				.thenReturn(new KanbanIssueCustomHistory());

		when(client.getWiqlResponse(prepareAzureServer(), time, projectConfFieldMapping, false))
				.thenReturn(createWiqlResponse());
		when(client.getIterationsResponse(Mockito.any())).thenReturn(createIterationsResponse());
		when(client.getWorkItemInfo(Mockito.any(), Mockito.any())).thenReturn(createWorkItemInfoResponse());
		when(client.getUpdatesResponse(Mockito.any(), Mockito.anyString())).thenReturn(createUpdatesResponse());

		onlineDataProcessor.validateAndCollectIssues(kanbanProjectlist);
	}

	private List<ProjectToolConfig> prepareProjectToolConfig() {
		ProjectToolConfig tool = new ProjectToolConfig();
		tool.setToolName(ProcessorConstants.AZURE);
		tool.setConnectionId(new ObjectId());
		tool.setQueryEnabled(false);
		tool.setBoardQuery("");
		tool.setProjectKey("");
		projectToolConfigList.add(tool);
		return projectToolConfigList;
	}

	private Optional<Connection> returnConnectionObject() {
		Connection connection = new Connection();
		connection.setOffline(false);
		connection.setBaseUrl("https://test.com/sun/azure");
		connection.setPassword(PLAIN_TEXT_PASSWORD);
		connection.setPat(PLAIN_TEXT_PASSWORD);
		return Optional.of(connection);
	}

	private AzureServer prepareAzureServer() {
		AzureServer azureServer = new AzureServer();
		azureServer.setPat("testPat");
		azureServer.setUrl("https://test.com/testUser/testProject");
		azureServer.setApiVersion("5.1");
		azureServer.setUsername("");
		return azureServer;

	}

	private MetadataIdentifier createMetaDataIdentifier() {
		String tool = "Azure";
		Boolean isKanban = Boolean.FALSE;
		String templateName = "DOJO Safe Template";
		String templateCode = "6";

		Identifier issue1 = createIdentifier("story",
				Arrays.asList("Story", "Enabler Story", "Tech Story", "Change request"));
		Identifier issue2 = createIdentifier("bug", Arrays.asList("Defect", "Bug"));
		Identifier issue3 = createIdentifier("epic", Arrays.asList("Epic"));
		Identifier issue4 = createIdentifier("issuetype",
				Arrays.asList("Story", "Enabler Story", "Tech Story", "Change request", "Defect", "Bug", "Epic"));
		Identifier issue5 = createIdentifier("uatdefect", Arrays.asList("UAT Defect"));
		List<Identifier> issuesIdentifier = Arrays.asList(issue1, issue2, issue3, issue4, issue5);

		Identifier customField1 = createIdentifier("storypoint", Arrays.asList("storypoint"));
		Identifier customField2 = createIdentifier("sprint", Arrays.asList("Sprint"));
		Identifier customField3 = createIdentifier("rootcause", Arrays.asList("Root Cause"));
		Identifier customField4 = createIdentifier("techdebt", Arrays.asList("Tech Debt"));
		Identifier customField5 = createIdentifier("uat", Arrays.asList("UAT"));
		Identifier customField6 = createIdentifier("timeCriticality", Arrays.asList("Time Criticality"));
		Identifier customField7 = createIdentifier("wsjf", Arrays.asList("WSJF"));
		Identifier customField8 = createIdentifier("costOfDelay", Arrays.asList("Cost of Delay"));
		Identifier customField9 = createIdentifier("businessValue", Arrays.asList("User-Business Value"));
		Identifier customField10 = createIdentifier("riskReduction",
				Arrays.asList("Risk Reduction-Opportunity Enablement Value"));
		Identifier customField11 = createIdentifier("jobSize", Arrays.asList("Job Size"));
		List<Identifier> customfieldIdentifer = Arrays.asList(customField1, customField2, customField3, customField4,
				customField5, customField6, customField7, customField8, customField9, customField10, customField11);

		Identifier workflow1 = createIdentifier("dor", Arrays.asList("Ready for Sprint Planning", "In Progress"));
		Identifier workflow2 = createIdentifier("dod", Arrays.asList("Closed", "Resolved", "Ready for Delivery"));
		Identifier workflow3 = createIdentifier("qa", Arrays.asList("In Testing"));
		Identifier workflow4 = createIdentifier("firststatus", Arrays.asList("Open"));
		Identifier workflow5 = createIdentifier("rejection", Arrays.asList("Closed", "Rejected"));
		Identifier workflow6 = createIdentifier("delivered",
				Arrays.asList("Closed", "Resolved", "Ready for Delivery", "Ready for Release"));
		Identifier workflow7 = createIdentifier("firststatus", Arrays.asList("Open"));
		List<Identifier> workflowIdentifer = Arrays.asList(workflow1, workflow2, workflow3, workflow4, workflow5,
				workflow6, workflow7);

		Identifier valuestoidentify1 = createIdentifier("rootCauseValue", Arrays.asList("Coding"));
		Identifier valuestoidentify2 = createIdentifier("rejectionResolution",
				Arrays.asList("Invalid", "Duplicate", "Unrequired"));
		Identifier valuestoidentify3 = createIdentifier("qaRootCause",
				Arrays.asList("Coding", "Configuration", "Regression", "Data"));
		List<Identifier> valuestoidentifyIdentifer = Arrays.asList(valuestoidentify1, valuestoidentify2,
				valuestoidentify3);

		List<Identifier> issuelinkIdentifer = new ArrayList<>();
		return new MetadataIdentifier(tool, templateName, templateCode, isKanban, false, issuesIdentifier,
				customfieldIdentifer, workflowIdentifer, issuelinkIdentifer, valuestoidentifyIdentifer);

	}

	private Identifier createIdentifier(String type, List<String> value) {
		Identifier identifier = new Identifier();
		identifier.setType(type);
		identifier.setValue(value);
		return identifier;
	}
}