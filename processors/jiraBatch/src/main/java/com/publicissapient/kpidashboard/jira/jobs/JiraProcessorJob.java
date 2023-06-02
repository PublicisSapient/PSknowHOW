package com.publicissapient.kpidashboard.jira.jobs;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.jira.listener.FirstJobListener;
import com.publicissapient.kpidashboard.jira.processor.IssueScrumProcessor;
import com.publicissapient.kpidashboard.jira.reader.IssueScrumReader;
import com.publicissapient.kpidashboard.jira.tasklet.MetaDataScrumTasklet;
import com.publicissapient.kpidashboard.jira.writer.IssueScrumWriter;

@Configuration
public class JiraProcessorJob {

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Autowired
	private IssueScrumReader issueScrumReader;

	@Autowired
	private IssueScrumProcessor issueScrumProcessor;

	@Autowired
	private IssueScrumWriter issueScrumWriter;

	@Autowired
	private FirstJobListener firstJobListener;

	@Autowired
	private MetaDataScrumTasklet metaDataTasklet;

	@Bean
	public Job fetchIssueScrumJob() {
		return jobBuilderFactory.get("FetchIssueScrum Job").incrementer(new RunIdIncrementer()).start(metaDataStep())
				.next(fetchIssueScrumChunkStep()).build();
	}

	private Step metaDataStep() {
		return stepBuilderFactory.get("Fetch metadata Step").tasklet(metaDataTasklet).build();
	}
	private Step fetchIssueScrumChunkStep() {
		return stepBuilderFactory.get("Fetch Issue for Scrum-Chunk Step").<JiraIssue, JiraIssue>chunk(3)
				.reader(issueScrumReader).processor(issueScrumProcessor).writer(issueScrumWriter).build();
	}
}
