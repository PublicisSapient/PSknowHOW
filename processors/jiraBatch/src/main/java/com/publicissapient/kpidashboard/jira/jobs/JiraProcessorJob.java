package com.publicissapient.kpidashboard.jira.jobs;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.publicissapient.kpidashboard.jira.listener.JiraIssueStepListener;
import com.publicissapient.kpidashboard.jira.listener.JiraIssueWriterListener;
import com.publicissapient.kpidashboard.jira.listener.KanbanJiraIssueStepListener;
import com.publicissapient.kpidashboard.jira.listener.KanbanJiraIssueWriterListener;
import com.publicissapient.kpidashboard.jira.listener.NotificationJobListener;
import com.publicissapient.kpidashboard.jira.model.CompositeResult;
import com.publicissapient.kpidashboard.jira.model.ReadData;
import com.publicissapient.kpidashboard.jira.processor.IssueKanbanProcessor;
import com.publicissapient.kpidashboard.jira.processor.IssueScrumProcessor;
import com.publicissapient.kpidashboard.jira.reader.IssueBoardReader;
import com.publicissapient.kpidashboard.jira.tasklet.MetaDataBoardTasklet;
import com.publicissapient.kpidashboard.jira.tasklet.SprintScrumBoardTasklet;
import com.publicissapient.kpidashboard.jira.writer.IssueKanbanWriter;
import com.publicissapient.kpidashboard.jira.writer.IssueScrumWriter;

@Configuration
public class JiraProcessorJob {

	@Autowired
	JobBuilderFactory jobBuilderFactory;

	@Autowired
	StepBuilderFactory stepBuilderFactory;

	@Autowired
	IssueBoardReader issueBoardReader;

	@Autowired
	IssueScrumProcessor issueScrumProcessor;

	@Autowired
	IssueScrumWriter issueScrumWriter;
	@Autowired
	IssueKanbanWriter issueKanbanWriter;

	@Autowired
	MetaDataBoardTasklet metaDataBoardTasklet;

	@Autowired
	SprintScrumBoardTasklet sprintScrumBoardTasklet;

	@Autowired
	JiraIssueStepListener jiraIssueStepListener;

	@Autowired
	JiraIssueWriterListener jiraIssueWriterListener;

	@Autowired
	NotificationJobListener notificationJobListener;

	@Autowired
	IssueKanbanProcessor issueKanbanProcessor;

	@Autowired
	KanbanJiraIssueStepListener kanbanJiraIssueStepListener;

	@Autowired
	KanbanJiraIssueWriterListener kanbanJiraIssueWriterListener;

	@Bean
	public Job fetchIssueScrumBoardJob() {
		return jobBuilderFactory.get("FetchIssueScrum Job").incrementer(new RunIdIncrementer()).start(metaDataStep())
				.next(sprintReportStep()).next(fetchIssueScrumBoardChunkStep()).build();
	}

	private Step metaDataStep() {
		return stepBuilderFactory.get("Fetch metadata-Scrum-board").tasklet(metaDataBoardTasklet).build();
	}

	private Step sprintReportStep() {
		return stepBuilderFactory.get("Fetch Sprint Report-Scrum-board").tasklet(sprintScrumBoardTasklet).build();
	}

	private Step fetchIssueScrumBoardChunkStep() {
		return stepBuilderFactory.get("Fetch Issue-Scrum-board").<ReadData, CompositeResult>chunk(50)
				.reader(issueBoardReader).processor(issueScrumProcessor).writer(issueScrumWriter)
				.listener(jiraIssueWriterListener).listener(jiraIssueStepListener).listener(notificationJobListener)
				.build();
	}

	@Bean
	public Job fetchIssueKanbanBoardJob() {
		return jobBuilderFactory.get("FetchIssueKanban Job").incrementer(new RunIdIncrementer()).start(metaDataStep())
				.next(fetchIssueKanbanBoardChunkStep()).build();
	}

	private Step fetchIssueKanbanBoardChunkStep() {
		return stepBuilderFactory.get("Fetch Issue-Kanban-board").<ReadData, CompositeResult>chunk(50)
				.reader(issueBoardReader).processor(issueKanbanProcessor).writer(issueKanbanWriter)
				.listener(kanbanJiraIssueWriterListener).listener(kanbanJiraIssueStepListener).build();
	}
}
