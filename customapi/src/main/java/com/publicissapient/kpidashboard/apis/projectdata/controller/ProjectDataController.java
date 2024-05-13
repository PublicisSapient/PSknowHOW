package com.publicissapient.kpidashboard.apis.projectdata.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.Collections;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.projectdata.service.impl.ProjectDataServiceImpl;
import com.publicissapient.kpidashboard.common.model.application.MasterProjectRelease;
import com.publicissapient.kpidashboard.common.model.jira.DataRequest;
import com.publicissapient.kpidashboard.common.model.jira.MasterJiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.MasterSprintDetails;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/data")
@Slf4j
public class ProjectDataController {

	@Autowired
	private ProjectDataServiceImpl projectDataService;

	@Autowired
	private CustomApiConfig customApiConfig;

	@PostMapping(value = "/issues", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<List<MasterJiraIssue>> getProjectData(HttpServletRequest request,
			@NotNull @RequestBody DataRequest dataRequest) {
		log.info("Received {} request for /issues for request {}", request.getMethod(), dataRequest.toString());
		Boolean isApiAuth = customApiConfig.getxApiKey().equalsIgnoreCase(request.getHeader(Constant.TOKEN_KEY));
		if (Boolean.FALSE.equals(isApiAuth)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());
		}

		List<MasterJiraIssue> responseList = projectDataService.getProjectJiraIssues(dataRequest);
		if (responseList.isEmpty()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseList);
		} else {
			return ResponseEntity.ok().body(responseList);
		}
	}

	@PostMapping(value = "/issueTypes", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<List<String>> getIssueTypes(HttpServletRequest request,
			@NotNull @RequestBody DataRequest dataRequest) {
		log.info("Received {} request for /issueType for request {}", request.getMethod(), dataRequest.toString());
		Boolean isApiAuth = customApiConfig.getxApiKey().equalsIgnoreCase(request.getHeader(Constant.TOKEN_KEY));
		if (Boolean.FALSE.equals(isApiAuth)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());
		}

		List<String> responseList = projectDataService.getIssueTypes(dataRequest);
		if (responseList.isEmpty()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseList);
		} else {
			return ResponseEntity.ok().body(responseList);
		}
	}

	@PostMapping(value = "/sprints", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<List<MasterSprintDetails>> getProjectSprints(HttpServletRequest request,
			@NotNull @RequestBody DataRequest dataRequest) {
		log.info("Received {} request for /sprints for request {}", request.getMethod(), dataRequest.toString());
		Boolean isApiAuth = customApiConfig.getxApiKey().equalsIgnoreCase(request.getHeader(Constant.TOKEN_KEY));
		if (Boolean.FALSE.equals(isApiAuth)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());
		}

		List<MasterSprintDetails> responseList = projectDataService.getProjectSprints(dataRequest);
		if (responseList.isEmpty()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseList);
		} else {
			return ResponseEntity.ok().body(responseList);
		}
	}

	@PostMapping(value = "/releases", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<MasterProjectRelease> getProjectReleases(HttpServletRequest request,
			@NotNull @RequestBody DataRequest dataRequest) {
		log.info("Received {} request for /releases for request {}", request.getMethod(), dataRequest.toString());
		Boolean isApiAuth = customApiConfig.getxApiKey().equalsIgnoreCase(request.getHeader(Constant.TOKEN_KEY));
		if (Boolean.FALSE.equals(isApiAuth)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}

		MasterProjectRelease response = projectDataService.getProjectReleases(dataRequest);
		if (response == null) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		} else {
			return ResponseEntity.ok().body(response);
		}
	}

}
