package com.publicissapient.kpidashboard.apis.projectdata.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.projectdata.service.impl.ProjectDataServiceImpl;
import com.publicissapient.kpidashboard.common.model.jira.DataRequest;

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
	public ResponseEntity<ServiceResponse> getProjectData(HttpServletRequest request,
			@NotNull @RequestBody DataRequest dataRequest, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		log.info("Received {} request for /issues for request {}", request.getMethod(), dataRequest.toString());
		Boolean isApiAuth = customApiConfig.getxApiKey().equalsIgnoreCase(request.getHeader(Constant.TOKEN_KEY));
		if (Boolean.FALSE.equals(isApiAuth)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}

		ServiceResponse response = projectDataService.getProjectJiraIssues(dataRequest, page, size);
		if (response == null) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		} else {
			return ResponseEntity.ok().body(response);
		}
	}

	@PostMapping(value = "/issueTypes", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> getIssueTypes(HttpServletRequest request,
			@NotNull @RequestBody DataRequest dataRequest) {
		log.info("Received {} request for /issueType for request {}", request.getMethod(), dataRequest.toString());
		Boolean isApiAuth = customApiConfig.getxApiKey().equalsIgnoreCase(request.getHeader(Constant.TOKEN_KEY));
		if (Boolean.FALSE.equals(isApiAuth)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}

		ServiceResponse response = projectDataService.getIssueTypes(dataRequest);
		if (response == null) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		} else {
			return ResponseEntity.ok().body(response);
		}
	}

	@PostMapping(value = "/sprints", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> getProjectSprints(HttpServletRequest request,
			@NotNull @RequestBody DataRequest dataRequest, @RequestParam(defaultValue = "false") boolean onlyActive) {
		log.info("Received {} request for /sprints for request {}", request.getMethod(), dataRequest.toString());
		Boolean isApiAuth = customApiConfig.getxApiKey().equalsIgnoreCase(request.getHeader(Constant.TOKEN_KEY));
		if (Boolean.FALSE.equals(isApiAuth)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}

		ServiceResponse response = projectDataService.getProjectSprints(dataRequest, onlyActive);
		if (response == null) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		} else {
			return ResponseEntity.ok().body(response);
		}
	}

	@PostMapping(value = "/releases", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> getProjectReleases(HttpServletRequest request,
			@NotNull @RequestBody DataRequest dataRequest) {
		log.info("Received {} request for /releases for request {}", request.getMethod(), dataRequest.toString());
		Boolean isApiAuth = customApiConfig.getxApiKey().equalsIgnoreCase(request.getHeader(Constant.TOKEN_KEY));
		if (Boolean.FALSE.equals(isApiAuth)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}

		ServiceResponse response = projectDataService.getProjectReleases(dataRequest);
		if (response == null) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		} else {
			return ResponseEntity.ok().body(response);
		}
	}

	@GetMapping(value = "/project/metadata", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> getProjectMetaData(HttpServletRequest request) {
		log.info("Received {} request for scrum project meta data", request.getMethod());
		Boolean isApiAuth = customApiConfig.getxApiKey().equalsIgnoreCase(request.getHeader(Constant.TOKEN_KEY));
		if (Boolean.FALSE.equals(isApiAuth)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}

		ServiceResponse response = projectDataService.getScrumProjects();
		if (response == null) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		} else {
			return ResponseEntity.ok().body(response);
		}
	}

}
