package com.publicissapient.kpidashboard.jira.helper;

import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.stereotype.Component;

@Component
public class BuilderFactory {

	public JobBuilder getJobBuilder(String name, JobRepository jobRepository) {
		return new JobBuilder(name, jobRepository);
	}

	public StepBuilder getStepBuilder(String name, JobRepository jobRepository) {
		return new StepBuilder(name, jobRepository);
	}
}
