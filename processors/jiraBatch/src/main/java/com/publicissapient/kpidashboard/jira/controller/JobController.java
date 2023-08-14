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

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/job")
@Slf4j
public class JobController {

	@Autowired
	JobLauncher jobLauncher;

	@Qualifier("fetchIssueScrumBoardJob")
	@Autowired
	Job fetchIssueScrumBoardJob;

	@GetMapping("/start")
	public String startJob() throws Exception {
		log.info("Request coming for job");
		Map<String, JobParameter> params = new HashMap<>();
		params.put("currentTime", new JobParameter(System.currentTimeMillis()));
		JobParameters jobParameters = new JobParameters(params);
		jobLauncher.run(fetchIssueScrumBoardJob, jobParameters);
		return "job started ....";
	}
}
