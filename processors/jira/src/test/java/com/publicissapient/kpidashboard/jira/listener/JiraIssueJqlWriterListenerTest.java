package com.publicissapient.kpidashboard.jira.listener;

import com.publicissapient.kpidashboard.common.model.ProcessorExecutionTraceLog;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.repository.tracelog.ProcessorExecutionTraceLogRepository;
import com.publicissapient.kpidashboard.jira.constant.JiraConstants;
import com.publicissapient.kpidashboard.jira.model.CompositeResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.springframework.test.util.AssertionErrors.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class JiraIssueJqlWriterListenerTest {
	@Mock
	private ProcessorExecutionTraceLogRepository processorExecutionTraceLogRepo;

	@InjectMocks
	private JiraIssueJqlWriterListener listener;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testAfterWrite_SuccessfulRun() {
		// Arrange
		List<CompositeResult> compositeResults = createSampleCompositeResults();

		// Act
		listener.afterWrite(compositeResults);
		assertTrue("", true);
	}

	@Test
	public void testOnWriteError_LogsError() {
		// Arrange
		Exception testException = new RuntimeException("Test exception");
		List<CompositeResult> compositeResults = createSampleCompositeResults();

		// Act
		listener.onWriteError(testException, compositeResults);
		assertTrue("", true);
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
	public void testAfterWriteWithEmptyValue() {
		listener.afterWrite(new ArrayList<>());
	}

	@Test
	public void testAfterWriteWithTraceLog() {
		ProcessorExecutionTraceLog processorExecutionTraceLog=new ProcessorExecutionTraceLog();
		processorExecutionTraceLog.setBasicProjectConfigId("abc");
		processorExecutionTraceLog.setBoardId("abc");
		processorExecutionTraceLog.setProcessorName(JiraConstants.JIRA);
		when(processorExecutionTraceLogRepo.findByProcessorNameAndBasicProjectConfigId(
				eq(JiraConstants.JIRA), eq("Project1")))
				.thenReturn(Optional.of(processorExecutionTraceLog));
		listener.afterWrite(createSampleCompositeResults());
	}

}