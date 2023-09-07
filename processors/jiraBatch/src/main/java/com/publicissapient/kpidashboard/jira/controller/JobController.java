package com.publicissapient.kpidashboard.jira.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.jira.config.FetchProjectConfiguration;
import com.publicissapient.kpidashboard.jira.constant.JiraConstants;

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

	@Qualifier("fetchIssueScrumJqlJob")
	@Autowired
	Job fetchIssueScrumJqlJob;

	@Qualifier("fetchIssueKanbanBoardJob")
	@Autowired
	Job fetchIssueKanbanBoardJob;

	@Qualifier("fetchIssueKanbanJqlJob")
	@Autowired
	Job fetchIssueKanbanJqlJob;

	@Qualifier("fetchIssueSprintJob")
	@Autowired
	Job fetchIssueSprintJob;

	@Autowired
	private FetchProjectConfiguration fetchProjectConfiguration;

	private static String PROJECT_ID = "projectId";
	private static String SPRINT_ID = "sprintId";
	private static String CURRENTTIME = "currentTime";

	@GetMapping("/startscrumboardjob")
	public ResponseEntity<Map> startScrumBoardJob() throws Exception {
		log.info("Request coming for job for Scrum project configured with board");

		List<String> scrumBoardbasicProjConfIds = fetchProjectConfiguration.fetchBasicProjConfId(JiraConstants.JIRA,
				false, false);

		List<JobParameters> parameterSets = getDynamicParameterSets(scrumBoardbasicProjConfIds);

		ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		for (JobParameters params : parameterSets) {
			executorService.submit(() -> {
				try {
					jobLauncher.run(fetchIssueScrumBoardJob, params);
				} catch (Exception e) {
					log.info("Jira Scrum data for board fetch failed for BasicProjectConfigId : {}",
							params.getString(PROJECT_ID));
					e.printStackTrace();
				}
			});
		}
		executorService.shutdown();

		Map response = new HashMap();
		response.put("status", "processing");
		return ResponseEntity.ok().body(response);
	}

	@GetMapping("/startscrumjqljob")
	public ResponseEntity<Map> startScrumJqlJob() throws Exception {
		log.info("Request coming for job for Scrum project configured with JQL");

		List<String> scrumBoardbasicProjConfIds = fetchProjectConfiguration.fetchBasicProjConfId(JiraConstants.JIRA,
				true, false);

		List<JobParameters> parameterSets = getDynamicParameterSets(scrumBoardbasicProjConfIds);

		ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		for (JobParameters params : parameterSets) {
			executorService.submit(() -> {
				try {
					jobLauncher.run(fetchIssueScrumJqlJob, params);
				} catch (Exception e) {
					log.info("Jira Scrum data for JQL fetch failed for BasicProjectConfigId : {}",
							params.getString(PROJECT_ID));
					e.printStackTrace();
				}
			});
		}
		executorService.shutdown();
		Map response = new HashMap();
		response.put("status", "processing");
		return ResponseEntity.ok().body(response);
	}

	private List<JobParameters> getDynamicParameterSets(List<String> scrumBoardbasicProjConfIds) {
		List<JobParameters> parameterSets = new ArrayList<>();

		scrumBoardbasicProjConfIds.forEach(configId -> {
			JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
			// Add dynamic parameters as needed
			jobParametersBuilder.addString(PROJECT_ID, configId);
			jobParametersBuilder.addLong(CURRENTTIME, System.currentTimeMillis());

			JobParameters params = jobParametersBuilder.toJobParameters();
			parameterSets.add(params);
		});

		return parameterSets;
	}

	@GetMapping("/startkanbanboardjob")
	public ResponseEntity<Map> startKanbanJob() throws Exception {
		log.info("Request coming for job");
		List<String> kanbanBoardbasicProjConfIds = fetchProjectConfiguration.fetchBasicProjConfId(JiraConstants.JIRA,
				false, true);
		List<JobParameters> parameterSets = getDynamicParameterSets(kanbanBoardbasicProjConfIds);

		ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		for (JobParameters params : parameterSets) {
			executorService.submit(() -> {
				try {
					jobLauncher.run(fetchIssueKanbanBoardJob, params);
				} catch (Exception e) {
					log.info("Jira Kanban data for board fetch failed for BasicProjectConfigId : {}",
							params.getString(PROJECT_ID));
					e.printStackTrace();
				}
			});
		}
		executorService.shutdown();
		Map response = new HashMap();
		response.put("status", "processing");
		return ResponseEntity.ok().body(response);
	}

	@GetMapping("/startkanbanjqljob")
	public ResponseEntity<Map> startKanbanJqlJob() throws Exception {
		log.info("Request coming for job for Kanban project configured with JQL");

		List<String> scrumBoardbasicProjConfIds = fetchProjectConfiguration.fetchBasicProjConfId(JiraConstants.JIRA,
				true, true);

		List<JobParameters> parameterSets = getDynamicParameterSets(scrumBoardbasicProjConfIds);

		ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		for (JobParameters params : parameterSets) {
			executorService.submit(() -> {
				try {
					jobLauncher.run(fetchIssueKanbanJqlJob, params);
				} catch (Exception e) {
					log.info("Jira Kanban data for JQL fetch failed for BasicProjectConfigId : {}",
							params.getString(PROJECT_ID));
					e.printStackTrace();
				}
			});
		}
		executorService.shutdown();
		Map response = new HashMap();
		response.put("status", "processing");
		return ResponseEntity.ok().body(response);
	}

	@GetMapping("/startfetchsprintjob")
	public ResponseEntity<Map> startfetchsprintjob(@RequestBody String sprintId) throws Exception {
		log.info("Request coming for fetching sprint job");
		JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();

		jobParametersBuilder.addString(SPRINT_ID, sprintId);
		jobParametersBuilder.addLong(CURRENTTIME, System.currentTimeMillis());
		JobParameters params = jobParametersBuilder.toJobParameters();
		try {
			jobLauncher.run(fetchIssueSprintJob, params);
		} catch (Exception e) {
			log.info("Jira Sprint data fetch failed for SprintId : {}", params.getString(SPRINT_ID));
			e.printStackTrace();
		}

		Map response = new HashMap();
		response.put("status", "processing");
		return ResponseEntity.ok().body(response);
	}

}
