package com.publicissapient.kpidashboard.jira.jobs;

import com.publicissapient.kpidashboard.jira.tasklet.JiraIssueReleaseStatusTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.publicissapient.kpidashboard.jira.listener.JiraIssueBoardWriterListener;
import com.publicissapient.kpidashboard.jira.listener.JiraIssueJqlWriterListener;
import com.publicissapient.kpidashboard.jira.listener.JiraIssueStepListener;
import com.publicissapient.kpidashboard.jira.listener.KanbanJiraIssueStepListener;
import com.publicissapient.kpidashboard.jira.listener.KanbanJiraIssueWriterListener;
import com.publicissapient.kpidashboard.jira.listener.NotificationJobListener;
import com.publicissapient.kpidashboard.jira.model.CompositeResult;
import com.publicissapient.kpidashboard.jira.model.ReadData;
import com.publicissapient.kpidashboard.jira.processor.IssueKanbanProcessor;
import com.publicissapient.kpidashboard.jira.processor.IssueScrumProcessor;
import com.publicissapient.kpidashboard.jira.reader.IssueBoardReader;
import com.publicissapient.kpidashboard.jira.reader.IssueJqlReader;
import com.publicissapient.kpidashboard.jira.tasklet.MetaDataTasklet;
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
	IssueJqlReader issueJqlReader;

	@Autowired
	IssueScrumProcessor issueScrumProcessor;

	@Autowired
	IssueScrumWriter issueScrumWriter;
	@Autowired
	IssueKanbanWriter issueKanbanWriter;

	@Autowired
	MetaDataTasklet metaDataTasklet;

	@Autowired
	SprintScrumBoardTasklet sprintScrumBoardTasklet;

	@Autowired
	JiraIssueReleaseStatusTasklet jiraIssueReleaseStatusTasklet;

	@Autowired
	JiraIssueStepListener jiraIssueStepListener;

	@Autowired
	JiraIssueBoardWriterListener jiraIssueBoardWriterListener;
	
	@Autowired
	JiraIssueJqlWriterListener jiraIssueJqlWriterListener;

	@Autowired
	NotificationJobListener notificationJobListener;

	@Autowired
	IssueKanbanProcessor issueKanbanProcessor;

	@Autowired
	KanbanJiraIssueStepListener kanbanJiraIssueStepListener;

	@Autowired
	KanbanJiraIssueWriterListener kanbanJiraIssueWriterListener;

	/** Scrum projects for board job : Start **/
	@Bean
	public Job fetchIssueScrumBoardJob() {
		return jobBuilderFactory.get("FetchIssueScrum Board Job").incrementer(new RunIdIncrementer())
				.start(metaDataStep()).next(sprintReportStep()).next(processProjectStatusStep()).next(fetchIssueScrumBoardChunkStep()).build();
	}

	private Step metaDataStep() {
		return stepBuilderFactory.get("Fetch metadata").tasklet(metaDataTasklet).build();
	}

	private Step sprintReportStep() {
		return stepBuilderFactory.get("Fetch Sprint Report-Scrum-board").tasklet(sprintScrumBoardTasklet).build();
	}

	private Step processProjectStatusStep() {
		return stepBuilderFactory.get("processProjectStatus-Scrum-board").tasklet(jiraIssueReleaseStatusTasklet).build();
	}

	private Step fetchIssueScrumBoardChunkStep() {
		return stepBuilderFactory.get("Fetch Issue-Scrum-board").<ReadData, CompositeResult>chunk(50)
				.reader(issueBoardReader).processor(issueScrumProcessor).writer(issueScrumWriter)
				.listener(jiraIssueBoardWriterListener).listener(jiraIssueStepListener)
				.listener(notificationJobListener).build();
	}

	/** Scrum projects for board job : End **/

	/** Scrum projects for Jql job : Start **/
	@Bean
	public Job fetchIssueScrumJqlJob() {
		return jobBuilderFactory.get("FetchIssueScrum JQL Job").incrementer(new RunIdIncrementer())
				.start(metaDataStep()).next(processProjectStatusStep()).next(fetchIssueScrumJqlChunkStep()).build();
	}

	private Step fetchIssueScrumJqlChunkStep() {
		return stepBuilderFactory.get("Fetch Issue-Scrum-Jql").<ReadData, CompositeResult>chunk(50)
				.reader(issueJqlReader).processor(issueScrumProcessor).writer(issueScrumWriter)
				.listener(jiraIssueJqlWriterListener).listener(jiraIssueStepListener)
				.listener(notificationJobListener).build();
	}

	/** Scrum projects for Jql job : End **/

	/** Kanban projects for board job : Start **/
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
	/** Kanban projects for board job : End **/
}
