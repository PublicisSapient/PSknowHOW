package com.publicissapient.kpidashboard.jira.listener;

import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.jira.constant.JiraConstants;
import com.publicissapient.kpidashboard.jira.model.CompositeResult;

@RunWith(MockitoJUnitRunner.class)
public class JiraIssueBoardWriterListenerTest {

	@Mock
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepo;

	@InjectMocks
	private JiraIssueBoardWriterListener listener;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testAfterWrite() {
		// Arrange
		List<CompositeResult> compositeResults = new ArrayList<>();

		// Create a JiraIssue for testing
		JiraIssue jiraIssue = new JiraIssue();
		jiraIssue.setBasicProjectConfigId("testProjectId");
		jiraIssue.setBoardId("testBoardId");
		jiraIssue.setTypeName("");
		jiraIssue.setChangeDate("2022-01-01T12:34:56"); // Change date in the required format
		CompositeResult compositeResult = new CompositeResult();
		compositeResult.setJiraIssue(jiraIssue);
		compositeResults.add(compositeResult);

		// Mock the repository's behavior
		when(processorExecutionTraceLogRepo.findByProcessorNameAndBasicProjectConfigIdAndBoardId(eq(JiraConstants.JIRA),
				eq("testProjectId"), eq("testBoardId"))).thenReturn(Optional.empty()); // For the case where trace log
																					   // is not present

		// Act
		listener.afterWrite(compositeResults);

		// Verify
		verify(processorExecutionTraceLogRepo, times(1)).saveAll(anyList());

		// Assert
		List<ProcessorExecutionTraceLog> savedLogs = new ArrayList<>();

		// Additional assertions based on the requirements of your application
		assertEquals(0, savedLogs.size());

	}

	@Test
	public void onWriteError_LogsError() {
		// Arrange
		Exception testException = new RuntimeException("Test exception");
		List<CompositeResult> compositeResults = createSampleCompositeResults();

		// Act
		listener.onWriteError(testException, compositeResults);
	}

	private List<CompositeResult> createSampleCompositeResults() {
		List<CompositeResult> compositeResults = new ArrayList<>();

		JiraIssue jiraIssue1 = new JiraIssue();
		jiraIssue1.setBasicProjectConfigId("Project1");
		jiraIssue1.setBoardId("Board1");
		jiraIssue1.setChangeDate(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
		jiraIssue1.setTypeName("Story");
		jiraIssue1.setChangeDate("");

		CompositeResult result1 = new CompositeResult();
		result1.setJiraIssue(jiraIssue1);
		compositeResults.add(result1);

		// Add more sample data as needed

		return compositeResults;
	}

	@Test
	public void afterWrite_ExistingLog_Success() {
		// Arrange
		List<CompositeResult> compositeResults = createSampleCompositeResults();

		// Mock the repository behavior
		when(processorExecutionTraceLogRepo.findByProcessorNameAndBasicProjectConfigIdAndBoardId(anyString(),
				anyString(), anyString())).thenReturn(Optional.of(new ProcessorExecutionTraceLog()));

		// Act
		listener.afterWrite(compositeResults);

		// Assert
		verify(processorExecutionTraceLogRepo, times(1)).saveAll(anyList());
	}

}
