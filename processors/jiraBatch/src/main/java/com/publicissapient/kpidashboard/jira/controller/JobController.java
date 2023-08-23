package com.publicissapient.kpidashboard.jira.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
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

	@Qualifier("fetchIssueKanbanBoardJob")
	@Autowired
	Job fetchIssueKanbanBoardJob;

	@Autowired
	private ProjectBasicConfigRepository projectConfigRepository;

	@GetMapping("/startscrumboardjob")
	public String startScrumBoardJob() throws Exception {
		log.info("Request coming for job");
		List<ProjectBasicConfig> allProjects = projectConfigRepository.findByKanban(false);
		List<JobParameters> parameterSets = getDynamicParameterSets(allProjects); // Implement this method

		ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		for (JobParameters params : parameterSets) {
			executorService.submit(() -> {
			try {
				jobLauncher.run(fetchIssueScrumBoardJob, params);
			} catch (Exception e) {
				e.printStackTrace();
			}
			});
		}
		executorService.shutdown();
		return "job started ....";
	}

	private List<JobParameters> getDynamicParameterSets(List<ProjectBasicConfig> allProjects) {
		List<JobParameters> parameterSets = new ArrayList<>();

		for (ProjectBasicConfig project : allProjects) {
			JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();

			// Add dynamic parameters as needed
			jobParametersBuilder.addString("projectId", String.valueOf(project.getId()));
			jobParametersBuilder.addLong("currentTime",System.currentTimeMillis());

			JobParameters params = jobParametersBuilder.toJobParameters();
			parameterSets.add(params);
		}

		return parameterSets;
	}

	@GetMapping("/startkanbanboardjob")
	public String startKanbanJob() throws Exception {
		log.info("Request coming for job");
		List<ProjectBasicConfig> allProjects = projectConfigRepository.findByKanban(true);
		List<JobParameters> parameterSets = getDynamicParameterSets(allProjects); // Implement this method

		ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		for (JobParameters params : parameterSets) {
			executorService.submit(() -> {
				try {
					jobLauncher.run(fetchIssueKanbanBoardJob, params);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
		executorService.shutdown();
		return "Kanban job started ....";
	}


}
