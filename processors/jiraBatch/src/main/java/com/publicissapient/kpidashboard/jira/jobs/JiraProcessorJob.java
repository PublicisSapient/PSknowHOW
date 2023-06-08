package com.publicissapient.kpidashboard.jira.jobs;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.jira.processor.IssueScrumProcessor;
import com.publicissapient.kpidashboard.jira.reader.IssueScrumBoardReader;
import com.publicissapient.kpidashboard.jira.tasklet.MetaDataScrumBoardTasklet;
import com.publicissapient.kpidashboard.jira.tasklet.SprintScrumBoardTasklet;
import com.publicissapient.kpidashboard.jira.writer.IssueScrumWriter;

@Configuration
public class JiraProcessorJob {

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Autowired
	private IssueScrumBoardReader issueScrumBoardReader;

	@Autowired
	private IssueScrumProcessor issueScrumProcessor;

	@Autowired
	private IssueScrumWriter issueScrumWriter;

	@Autowired
	private MetaDataScrumBoardTasklet metaDataScrumBoardTasklet;
	
	@Autowired
	private SprintScrumBoardTasklet sprintScrumBoardTasklet;

	@Bean
	public Job fetchIssueScrumBoardJob() {
		return jobBuilderFactory.get("FetchIssueScrum Job").incrementer(new RunIdIncrementer()).start(metaDataStep())
				.next(sprintReportStep())
				.next(fetchIssueScrumBoardChunkStep()).build();
	}

	private Step metaDataStep() {
		return stepBuilderFactory.get("Fetch metadata-Scrum-board").tasklet(metaDataScrumBoardTasklet).build();
	}
	
	private Step sprintReportStep() {
		return stepBuilderFactory.get("Fetch Sprint Report-Scrum-board").tasklet(sprintScrumBoardTasklet).build();
	}
	private Step fetchIssueScrumBoardChunkStep() {
		return stepBuilderFactory.get("Fetch Issue-Scrum-board").<Issue, JiraIssue>chunk(5)
				.reader(issueScrumBoardReader).processor(issueScrumProcessor).writer(issueScrumWriter).build();
	}
}
