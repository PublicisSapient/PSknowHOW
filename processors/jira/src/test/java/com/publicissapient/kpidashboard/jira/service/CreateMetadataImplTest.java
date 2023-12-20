package com.publicissapient.kpidashboard.jira.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.atlassian.jira.rest.client.api.MetadataRestClient;
import com.atlassian.jira.rest.client.api.StatusCategory;
import com.atlassian.jira.rest.client.api.domain.Field;
import com.atlassian.jira.rest.client.api.domain.FieldType;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.Identifier;
import com.publicissapient.kpidashboard.common.model.jira.MetadataIdentifier;
import com.publicissapient.kpidashboard.common.repository.application.FieldMappingRepository;
import com.publicissapient.kpidashboard.common.repository.jira.BoardMetadataRepository;
import com.publicissapient.kpidashboard.common.repository.jira.MetadataIdentifierRepository;
import com.publicissapient.kpidashboard.jira.client.ProcessorJiraRestClient;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.dataFactories.ConnectionsDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.ProjectBasicConfigDataFactory;
import com.publicissapient.kpidashboard.jira.dataFactories.ToolConfigDataFactory;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

import io.atlassian.util.concurrent.Promise;

@RunWith(MockitoJUnitRunner.class)
public class CreateMetadataImplTest {

	@Mock
	Promise<Iterable<Field>> metaDataFieldPromise;
	@Mock
	Promise<Iterable<IssueType>> metaDataIssueTypePromise;
	@Mock
	Promise<Iterable<Status>> metaDataStatusPromise;
	@Mock
	RestTemplate restTemplate;
	List<ProjectToolConfig> projectToolConfigs;
	List<FieldMapping> fieldMappingList;
	Optional<Connection> connection;
	List<ProjectBasicConfig> projectConfigsList;
	Iterable<Field> fieldItr;
	Iterable<IssueType> issueTypeItr;
	Iterable<Status> statusItr;
	@Mock
	private BoardMetadataRepository boardMetadataRepository;
	@Mock
	private MetadataIdentifierRepository metadataIdentifierRepository;
	@Mock
	private ProcessorJiraRestClient client;
	@Mock
	private JiraProcessorConfig jiraProcessorConfig;
	@Mock
	private FieldMappingRepository fieldMappingRepository;
	@InjectMocks
	private CreateMetadataImpl createMetadata;

	@Before
	public void setup() throws URISyntaxException {
		projectToolConfigs = getMockProjectToolConfig();
		fieldMappingList = getMockFieldMapping();
		connection = getMockConnection();
		projectConfigsList = getMockProjectConfig();

		Field field1 = new Field("Story Points", "customfield_20803", FieldType.JIRA, true, true, true, null);
		Field field2 = new Field("Sprint", "customfield_12700", FieldType.JIRA, true, true, true, null);
		Field field3 = new Field("Root Cause", "customfield_19121", FieldType.JIRA, true, true, true, null);
		Field field4 = new Field("Tech Debt", "customfield_59601", FieldType.JIRA, true, true, true, null);
		Field field5 = new Field("UAT", "UAT", FieldType.JIRA, true, true, true, null);
		List<Field> fields = Arrays.asList(field1, field2, field3, field4, field5);

		fieldItr = fields;

		IssueType issueType1 = new IssueType(new URI("self"), 1l, "Story", false, "desc", new URI("iconURI"));
		IssueType issueType2 = new IssueType(new URI("self"), 1l, "Enabler Story", false, "desc", new URI("iconURI"));
		IssueType issueType3 = new IssueType(new URI("self"), 1l, "Tech Story", false, "desc", new URI("iconURI"));
		IssueType issueType4 = new IssueType(new URI("self"), 1l, "Change request", false, "desc", new URI("iconURI"));
		IssueType issueType5 = new IssueType(new URI("self"), 1l, "Defect", false, "desc", new URI("iconURI"));
		IssueType issueType6 = new IssueType(new URI("self"), 1l, "Epic", false, "desc", new URI("iconURI"));
		IssueType issueType7 = new IssueType(new URI("self"), 1l, "UAT Defect", false, "desc", new URI("iconURI"));
		List<IssueType> issueTypes = Arrays.asList(issueType1, issueType2, issueType3, issueType4, issueType5,
				issueType6, issueType7);

		issueTypeItr = issueTypes;

		Status status1 = new Status(new URI("self"), 1l, "Ready for Sprint Planning", "desc", new URI("iconURI"),
				new StatusCategory(new URI("self"), "name", 1l, "key", "colorname"));
		Status status2 = new Status(new URI("self"), 1l, "Closed", "desc", new URI("iconURI"),
				new StatusCategory(new URI("self"), "name", 1l, "key", "colorname"));
		Status status3 = new Status(new URI("self"), 1l, "Implementing", "desc", new URI("iconURI"),
				new StatusCategory(new URI("self"), "name", 1l, "key", "colorname"));
		Status status4 = new Status(new URI("self"), 1l, "In Testing", "desc", new URI("iconURI"),
				new StatusCategory(new URI("self"), "name", 1l, "key", "colorname"));
		List<Status> statuses = Arrays.asList(status1, status2, status3, status4);

		statusItr = statuses;
	}

	@Test
	public void collectMetadata() throws Exception {

		when(fieldMappingRepository.save(any())).thenReturn(null);
		when(boardMetadataRepository.save(any())).thenReturn(null);
		when(boardMetadataRepository.findByProjectBasicConfigId(any())).thenReturn(null);

		MetadataRestClient metadataRestClient = mock(MetadataRestClient.class);
		when(client.getMetadataClient()).thenReturn(metadataRestClient);

		when(metadataRestClient.getFields()).thenReturn(metaDataFieldPromise);
		when(metaDataFieldPromise.claim()).thenReturn(fieldItr);

		when(metadataRestClient.getIssueTypes()).thenReturn(metaDataIssueTypePromise);
		when(metaDataIssueTypePromise.claim()).thenReturn(issueTypeItr);

		when(metadataRestClient.getStatuses()).thenReturn(metaDataStatusPromise);
		when(metaDataStatusPromise.claim()).thenReturn(statusItr);

		MetadataIdentifier metadataIdentifier = createMetaDataIdentifier();
		when(metadataIdentifierRepository.findByTemplateCodeAndToolAndIsKanban(any(), any(), any()))
				.thenReturn(metadataIdentifier);

		Mockito.when(jiraProcessorConfig.getCustomApiBaseUrl()).thenReturn("http://10.123.45.678:9090/");
		PowerMockito.whenNew(RestTemplate.class).withNoArguments().thenReturn(restTemplate);
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		HttpEntity<?> entity = new HttpEntity<>(headers);
		ResponseEntity<String> response = new ResponseEntity<>("Success", HttpStatus.OK);
		Mockito.when(restTemplate.exchange(new URI("http://10.123.45.678:9090/api/cache/clearCache/GenericCache"),
				HttpMethod.GET, entity, String.class)).thenReturn(response);

		Assert.assertThrows(Exception.class, () -> createMetadata.collectMetadata(createProjectConfig(), client));
	}

	private MetadataIdentifier createMetaDataIdentifier() {
		String tool = "Jira";
		Boolean isKanban = Boolean.FALSE;

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
		return new MetadataIdentifier(tool, "Standard Template", "7", isKanban, false, issuesIdentifier,
				customfieldIdentifer, workflowIdentifer, issuelinkIdentifer, valuestoidentifyIdentifer);
		// return new MetadataIdentifier(tool,"Dojo template", isKanban,
		// issuesIdentifier, customfieldIdentifer, workflowIdentifer,
		// issuelinkIdentifer, valuestoidentifyIdentifer);

	}

	private Identifier createIdentifier(String type, List<String> value) {
		Identifier identifier = new Identifier();
		identifier.setType(type);
		identifier.setValue(value);
		return identifier;
	}

	private ProjectConfFieldMapping createProjectConfig() {
		ProjectConfFieldMapping projectConfFieldMapping = ProjectConfFieldMapping.builder().build();
		ProjectBasicConfig projectConfig = projectConfigsList.get(2);
		try {
			BeanUtils.copyProperties(projectConfFieldMapping, projectConfig);
		} catch (IllegalAccessException | InvocationTargetException e) {

		}
		projectConfFieldMapping.setProjectBasicConfig(projectConfig);
		projectConfFieldMapping.setKanban(projectConfig.getIsKanban());
		projectConfFieldMapping.setBasicProjectConfigId(projectConfig.getId());
		projectConfFieldMapping.setJira(getJiraToolConfig());
		projectConfFieldMapping.setJiraToolConfigId(projectToolConfigs.get(0).getId());
		projectConfFieldMapping.setProjectToolConfig(projectToolConfigs.get(0));

		return projectConfFieldMapping;
	}

	private List<ProjectBasicConfig> getMockProjectConfig() {
		ProjectBasicConfigDataFactory projectConfigDataFactory = ProjectBasicConfigDataFactory
				.newInstance("/json/default/project_basic_configs.json");
		return projectConfigDataFactory.getProjectBasicConfigs();
	}

	private JiraToolConfig getJiraToolConfig() {
		JiraToolConfig toolObj = new JiraToolConfig();
		try {
			BeanUtils.copyProperties(toolObj, projectToolConfigs.get(0));
		} catch (IllegalAccessException | InvocationTargetException e) {

		}
		toolObj.setConnection(connection);
		return toolObj;
	}

	private List<ProjectToolConfig> getMockProjectToolConfig() {
		ToolConfigDataFactory projectToolConfigDataFactory = ToolConfigDataFactory
				.newInstance("/json/default/project_tool_configs.json");
		return projectToolConfigDataFactory.findByToolNameAndBasicProjectConfigId(ProcessorConstants.JIRA,
				"63c04dc7b7617e260763ca4e");
	}

	private Optional<Connection> getMockConnection() {
		ConnectionsDataFactory connectionDataFactory = ConnectionsDataFactory
				.newInstance("/json/default/connections.json");
		return connectionDataFactory.findConnectionById("5fd99f7bc8b51a7b55aec836");
	}

	private List<FieldMapping> getMockFieldMapping() {
		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/field_mapping.json");
		return fieldMappingDataFactory.getFieldMappings();
	}

}
