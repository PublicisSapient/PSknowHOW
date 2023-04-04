package com.publicissapient.kpidashboard.azure.client.sprint;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.publicissapient.kpidashboard.azure.model.AzureProcessor;
import com.publicissapient.kpidashboard.azure.model.AzureToolConfig;
import com.publicissapient.kpidashboard.azure.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.azure.repository.AzureProcessorRepository;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.BoardDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

@ExtendWith(SpringExtension.class)
public class SprintClientImplTest {

	@Mock
	private SprintRepository sprintRepository;

	@InjectMocks
	private SprintClientImpl sprintClientImpl;
	
	@Mock
	private AzureProcessorRepository azureProcessorRepository;

	@BeforeEach
	public void setUp() throws Exception {
	}

	@Test
	void validateAndCollectSprintsTest() throws Exception {
		AzureToolConfig azureToolConfig = new AzureToolConfig();
		azureToolConfig.setBasicProjectConfigId("5ba8e182d3735010e7f1fa45");
		Connection conn = new Connection();
		conn.setId(new ObjectId("6225b5c93f59bf41fe9f1b9d"));
		conn.setOffline(Boolean.FALSE);
		conn.setBaseUrl("https://test.com/testUser/Test%20ProjectOne");
		conn.setUsername("testUser");
		conn.setPat("testPat");

		BoardDetails boardDetails = new BoardDetails();
		boardDetails.setBoardId("b573d13f-a0c5-43a3-b30a-1893c7593402");
		boardDetails.setBoardName("Test ProjectOne Team");
		List<BoardDetails> azureBoardList = new ArrayList<>();
		azureBoardList.add(boardDetails);
		azureToolConfig.setBoards(azureBoardList);
		azureToolConfig.setConnection(conn);

		AzureProcessor processor = new AzureProcessor();
		processor.setId(new ObjectId("61d6b74df0d2833694dcceb7"));

		ProjectConfFieldMapping projectConfig = ProjectConfFieldMapping.builder().build();
		projectConfig.setBasicProjectConfigId(new ObjectId("621dc33afa2fae5a9cf47219"));
		projectConfig.setAzure(azureToolConfig);
		ProjectToolConfig projectToolConfig = new ProjectToolConfig();
		projectToolConfig.setApiVersion("5.1");
		projectConfig.setProjectToolConfig(projectToolConfig);
		when(sprintRepository.findTopByBasicProjectConfigIdAndState(any(), anyString())).thenReturn(null);
		when(azureProcessorRepository.findByProcessorName(anyString())).thenReturn(processor);
		sprintClientImpl.processSprints(projectConfig, getSprintDetails());
	}
	
	private Set<SprintDetails> getSprintDetails(){
		Set<SprintDetails> set = new HashSet<>();
		SprintDetails sprintDetails = new SprintDetails();
		sprintDetails.setSprintID("asprintid");
		set.add(sprintDetails);
		return set;
	}
}
