package com.publicissapient.kpidashboard.jira.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JobScheduler {

	@Autowired
	JobLauncher jobLauncher;

	@Autowired
	ProjectBasicConfigRepository projectConfigRepository;

	@Qualifier("fetchIssueScrumBoardJob")
	@Autowired
	Job fetchIssueScrumBoardJob;

	@Qualifier("fetchIssueKanbanBoardJob")
	@Autowired
	Job fetchIssueKanbanBoardJob;
	
	/**
	@Qualifier("myJob")
	@Autowired
	Job myJob;
	**/

	@Async
	//@Scheduled(cron = "0 0/1 * 1/1 * ?")
	public String fetchIssueScrumBoardStarter() throws Exception {
		log.info("Request coming for job");
		List<ProjectBasicConfig> allProjects = projectConfigRepository.findByKanban(false);
		List<JobParameters> parameterSets = getDynamicParameterSets(allProjects); // Implement this method

		ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		for (JobParameters params : parameterSets) {
			executorService.submit(() -> {
				try {
//					jobLauncher.run(fetchIssueScrumBoardJob, params);
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

    @Async
	//@Scheduled(cron = "0 0/1 * 1/1 * ?")
	public String fetchIssueKanbanBoardStarter() throws Exception {
		log.info("Request coming for job");
		List<ProjectBasicConfig> allProjects = projectConfigRepository.findByKanban(true);
		List<JobParameters> parameterSets = getDynamicParameterSets(allProjects); // Implement this method

		ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		for (JobParameters params : parameterSets) {
			executorService.submit(() -> {
				try {
//					jobLauncher.run(fetchIssueKanbanBoardJob, params);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
		executorService.shutdown();
		return "Kanban job started ....";
	}

}
