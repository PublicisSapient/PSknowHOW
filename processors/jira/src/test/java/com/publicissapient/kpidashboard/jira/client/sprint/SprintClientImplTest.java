package com.publicissapient.kpidashboard.jira.client.sprint;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.jira.BoardDetails;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import com.publicissapient.kpidashboard.jira.adapter.JiraAdapter;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;
import com.publicissapient.kpidashboard.jira.model.JiraProcessor;
import com.publicissapient.kpidashboard.jira.model.JiraToolConfig;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.repository.JiraProcessorRepository;

@ExtendWith(SpringExtension.class)
class SprintClientImplTest {

	@Mock
	private SprintRepository sprintRepository;
	
	@Mock
	private JiraProcessorRepository jiraProcessorRepository;
	
	@Mock
	private JiraProcessorConfig jiraProcessorConfig;
		
	@InjectMocks
	private SprintClientImpl sprintClientImpl;
	
	@Mock
	private JiraAdapter jiraAdapter;
	
	@BeforeEach
	public void setUp() throws Exception {
	}
	
	@Test
	void validateAndCollectIssuesScrum() throws Exception {
		JiraToolConfig projectToolConfig = new JiraToolConfig();
		projectToolConfig.setBasicProjectConfigId("5ba8e182d3735010e7f1fa45");
		Optional<Connection> conn = Optional.of(new Connection());
		conn.get().setOffline(Boolean.FALSE);
		conn.get().setBaseUrl("https://abcd.com/jira");
		conn.get().setUsername("test");
		conn.get().setPassword("testPassword");
		
		BoardDetails jiraBoard = new BoardDetails();
		jiraBoard.setBoardId("11856");
		jiraBoard.setBoardName("TEST");
		List<BoardDetails> jiraBoardList = new ArrayList<>();
		jiraBoardList.add(jiraBoard);
		projectToolConfig.setBoards(jiraBoardList);
		projectToolConfig.setConnection(conn);
		
		JiraProcessor processor = new JiraProcessor();
		processor.setId(new ObjectId("5ba8e182d3735010e7f1fa45"));
		
		
		ProjectConfFieldMapping projectConfig = ProjectConfFieldMapping.builder().build();
		projectConfig.setBasicProjectConfigId(new ObjectId("5ba8e182d3735010e7f1fa45"));
		projectConfig.setJira(projectToolConfig);
		when(jiraProcessorConfig.getAesEncryptionKey()).thenReturn("abxg");
		when(sprintRepository.findTopByBasicProjectConfigIdAndState(any(), anyString())).thenReturn(null);
		when(jiraProcessorRepository.findByProcessorName(anyString())).thenReturn(processor);
		sprintClientImpl.processSprints(projectConfig, getSprintDetails(),jiraAdapter);
	}

	@Test
	void validateAndCollectIssuesScrumWithExistingData() throws Exception {
		JiraToolConfig projectToolConfig = new JiraToolConfig();
		projectToolConfig.setBasicProjectConfigId("5ba8e182d3735010e7f1fa45");
		Optional<Connection> conn = Optional.of(new Connection());
		conn.get().setOffline(Boolean.FALSE);
		conn.get().setBaseUrl("https://abcd.com/jira");
		conn.get().setUsername("jira");
		conn.get().setPassword("hRjE0RY0GkbiZirguoqtcO/niMjBTcdvwOji0ZEpL6yl6e5L7/hBs0dsBM43mGiF");

		BoardDetails jiraBoard = new BoardDetails();
		jiraBoard.setBoardId("11856");
		jiraBoard.setBoardName("DTS");
		List<BoardDetails> jiraBoardList = new ArrayList<>();
		jiraBoardList.add(jiraBoard);
		projectToolConfig.setBoards(jiraBoardList);
		projectToolConfig.setConnection(conn);

		JiraProcessor processor = new JiraProcessor();
		processor.setId(new ObjectId("5ba8e182d3735010e7f1fa45"));


		ProjectConfFieldMapping projectConfig = ProjectConfFieldMapping.builder().build();
		projectConfig.setBasicProjectConfigId(new ObjectId("5ba8e182d3735010e7f1fa45"));
		projectConfig.setJira(projectToolConfig);

		SprintDetails sprintDetails = new SprintDetails();
		sprintDetails.setSprintID("asprintid");
		sprintDetails.setState("CLOSED");
		List<String> list = new ArrayList<>();
		list.add("1111");
		sprintDetails.setOriginBoardId(list);

		when(jiraProcessorConfig.getAesEncryptionKey()).thenReturn("abxg");
		when(sprintRepository.findBySprintIDIn(any())).thenReturn(Arrays.asList(sprintDetails));
		when(jiraProcessorRepository.findByProcessorName(anyString())).thenReturn(processor);
		sprintClientImpl.processSprints(projectConfig, getSprintDetails(),jiraAdapter);
	}

	@Test
	void validateAndCollectIssuesScrumWithNoChange() throws Exception {
		JiraToolConfig projectToolConfig = new JiraToolConfig();
		projectToolConfig.setBasicProjectConfigId("5ba8e182d3735010e7f1fa45");
		Optional<Connection> conn = Optional.of(new Connection());
		conn.get().setOffline(Boolean.FALSE);
		conn.get().setBaseUrl("https://abcd.com/jira");
		conn.get().setUsername("jira");
		conn.get().setPassword("hRjE0RY0GkbiZirguoqtcO/niMjBTcdvwOji0ZEpL6yl6e5L7/hBs0dsBM43mGiF");

		BoardDetails jiraBoard = new BoardDetails();
		jiraBoard.setBoardId("11856");
		jiraBoard.setBoardName("DTS");
		List<BoardDetails> jiraBoardList = new ArrayList<>();
		jiraBoardList.add(jiraBoard);
		projectToolConfig.setBoards(jiraBoardList);
		projectToolConfig.setConnection(conn);

		JiraProcessor processor = new JiraProcessor();
		processor.setId(new ObjectId("5ba8e182d3735010e7f1fa45"));


		ProjectConfFieldMapping projectConfig = ProjectConfFieldMapping.builder().build();
		projectConfig.setBasicProjectConfigId(new ObjectId("5ba8e182d3735010e7f1fa45"));
		projectConfig.setJira(projectToolConfig);

		SprintDetails sprintDetails = new SprintDetails();
		sprintDetails.setSprintID("asprintid");
		sprintDetails.setState("ACTIVE");
		List<String> list = new ArrayList<>();
		list.add("1111");
		sprintDetails.setOriginBoardId(list);

		when(jiraProcessorConfig.getAesEncryptionKey()).thenReturn("abxg");
		when(sprintRepository.findBySprintIDIn(any())).thenReturn(Arrays.asList(sprintDetails));
		when(jiraProcessorRepository.findByProcessorName(anyString())).thenReturn(processor);
		sprintClientImpl.processSprints(projectConfig, getSprintDetails(),jiraAdapter);
	}
	
	private Set<SprintDetails> getSprintDetails(){
		Set<SprintDetails> set = new HashSet<>();
		SprintDetails sprintDetails = new SprintDetails();
		sprintDetails.setSprintID("asprintid");
		sprintDetails.setState("ACTIVE");
		List<String> list = new ArrayList<>();
		list.add("1111");
		sprintDetails.setOriginBoardId(list);
		set.add(sprintDetails);
		return set;
	}
	
}
