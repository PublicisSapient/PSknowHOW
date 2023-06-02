package com.publicissapient.kpidashboard.jira.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/job")
public class JobController {
	
	@Autowired
	JobLauncher jobLauncher;
	
	@Qualifier("fetchIssueScrumJob")
	@Autowired
	Job fetchIssueScrumJob;

	@GetMapping("/start/{jobName}")
	public String startJob(@PathVariable String jobName) throws Exception {
		Map<String, JobParameter> params = new HashMap<>();
		params.put("currentTime", new JobParameter(System.currentTimeMillis()));
		JobParameters jobParameters = new JobParameters(params);
		jobLauncher.run(fetchIssueScrumJob, jobParameters);
		return "job started ....";
	}
}
