package com.publicissapient.kpidashboard.jira.jobs;

import com.publicissapient.kpidashboard.jira.processor.IssueKanbanProcessor;
import com.publicissapient.kpidashboard.jira.reader.IssueKanbanBoardReader;
import com.publicissapient.kpidashboard.jira.tasklet.MetaDataKanbanBoardTasklet;
import com.publicissapient.kpidashboard.jira.writer.IssueKanbanWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.publicissapient.kpidashboard.jira.listener.JiraIssueStepListener;
import com.publicissapient.kpidashboard.jira.listener.JiraIssueWriterListener;
import com.publicissapient.kpidashboard.jira.model.CompositeResult;
import com.publicissapient.kpidashboard.jira.model.ReadData;
import com.publicissapient.kpidashboard.jira.processor.IssueScrumProcessor;
import com.publicissapient.kpidashboard.jira.reader.IssueScrumBoardReader;
import com.publicissapient.kpidashboard.jira.tasklet.MetaDataScrumBoardTasklet;
import com.publicissapient.kpidashboard.jira.tasklet.SprintScrumBoardTasklet;
import com.publicissapient.kpidashboard.jira.writer.IssueScrumWriter;

@Configuration
public class JiraProcessorJob {

	@Autowired
	JobBuilderFactory jobBuilderFactory;

	@Autowired
	StepBuilderFactory stepBuilderFactory;

	@Autowired
	IssueScrumBoardReader issueScrumBoardReader;

	@Autowired
	IssueScrumProcessor issueScrumProcessor;

	@Autowired
	IssueScrumWriter issueScrumWriter;
    @Autowired
	IssueKanbanWriter issueKanbanWriter;

	@Autowired
	MetaDataScrumBoardTasklet metaDataScrumBoardTasklet;

	@Autowired
	MetaDataKanbanBoardTasklet metaDataKanbanBoardTasklet;

	@Autowired
	SprintScrumBoardTasklet sprintScrumBoardTasklet;

	@Autowired
	JiraIssueStepListener jiraIssueStepListener;

	@Autowired
	JiraIssueWriterListener jiraIssueWriterListener;

	@Autowired
	IssueKanbanProcessor issueKanbanProcessor;

	@Autowired
	IssueKanbanBoardReader issueKanbanBoardReader;

	@Bean
	public Job fetchIssueScrumBoardJob() {
		return jobBuilderFactory.get("FetchIssueScrum Job").incrementer(new RunIdIncrementer()).start(metaDataStep())
				.next(sprintReportStep()).next(fetchIssueScrumBoardChunkStep()).build();
//		return jobBuilderFactory.get("FetchIssueScrum Job").incrementer(new RunIdIncrementer()).start(fetchIssueScrumBoardChunkStep()).build();
	}

	private Step metaDataStep() {
		return stepBuilderFactory.get("Fetch metadata-Scrum-board").tasklet(metaDataScrumBoardTasklet).build();
	}

	private Step sprintReportStep() {
		return stepBuilderFactory.get("Fetch Sprint Report-Scrum-board").tasklet(sprintScrumBoardTasklet).build();
	}

	private Step fetchIssueScrumBoardChunkStep() {
		return stepBuilderFactory.get("Fetch Issue-Scrum-board").<ReadData, CompositeResult>chunk(50)
				.reader(issueScrumBoardReader).processor(issueScrumProcessor).writer(issueScrumWriter)
				.listener(jiraIssueWriterListener).listener(jiraIssueStepListener).build();
	}

//	@Bean
//	public Job fetchIssueKanbanBoardJob() {
//		return jobBuilderFactory.get("FetchIssueKanban Job").incrementer(new RunIdIncrementer()).start(metaDataStepForKanban())
//				.next(fetchIssueKanbanBoardChunkStep()).build();
//	}

	private Step metaDataStepForKanban() {
		return stepBuilderFactory.get("Fetch metadata-Kanban-board").tasklet(metaDataKanbanBoardTasklet).build();
	}

	private Step fetchIssueKanbanBoardChunkStep() {
		return stepBuilderFactory.get("Fetch Issue-Kanban-board").<ReadData, CompositeResult>chunk(10)
				.reader(issueKanbanBoardReader).processor(issueKanbanProcessor).writer(issueKanbanWriter)
				.listener(jiraIssueStepListener).build();
	}
}
