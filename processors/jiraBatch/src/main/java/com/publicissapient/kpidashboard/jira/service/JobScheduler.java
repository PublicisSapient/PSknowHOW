package com.publicissapient.kpidashboard.jira.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class JobScheduler {
	
	@Autowired
	JobLauncher jobLauncher;
	
	@Qualifier("fetchIssueScrumJob")
	@Autowired
	Job fetchIssueScrumJob;

	@Async
	@Scheduled(cron="0 0/1 * 1/1 * ?")
	public void fetchIssueScrumStarter() throws Exception {
		Map<String, JobParameter> params = new HashMap<>();
		params.put("currentTime", new JobParameter(System.currentTimeMillis()));
		JobParameters jobParameters = new JobParameters(params);
		jobLauncher.run(fetchIssueScrumJob, jobParameters);
		System.out.println("Fetch Issue for Scrum Job Started .....");
		
	}
}
