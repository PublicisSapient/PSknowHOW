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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/data")
@Slf4j
@Tag(name = "PSKnowHow data", description = "APIs for consuming PSKnowHOW project data")
public class ProjectDataController {

	@Autowired
	private ProjectDataServiceImpl projectDataService;

	@Autowired
	private CustomApiConfig customApiConfig;

	@Operation(summary = "Get project issues", description = "Retrieve project issues based on the provided data request")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful retrieval of project issues"),
			@ApiResponse(responseCode = "401", description = "Unauthorized access"),
			@ApiResponse(responseCode = "403", description = "Forbidden access")
	})
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

	@Operation(summary = "Get distinct issue types", description = "Retrieve issue types based on the provided data request")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful retrieval of issue types"),
			@ApiResponse(responseCode = "401", description = "Unauthorized access"),
			@ApiResponse(responseCode = "403", description = "Forbidden access")
	})
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

	@Operation(summary = "Get project sprints", description = "Retrieve project sprints based on the provided data request")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful retrieval of project sprints"),
			@ApiResponse(responseCode = "401", description = "Unauthorized access"),
			@ApiResponse(responseCode = "403", description = "Forbidden access")
	})
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

	@Operation(summary = "Get project releases", description = "Retrieve project releases based on the provided data request")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful retrieval of project releases"),
			@ApiResponse(responseCode = "401", description = "Unauthorized access"),
			@ApiResponse(responseCode = "403", description = "Forbidden access")
	})
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

	@Operation(summary = "Get scrum project metadata", description = "Retrieve metadata for scrum projects")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful retrieval of project metadata"),
			@ApiResponse(responseCode = "401", description = "Unauthorized access"),
			@ApiResponse(responseCode = "403", description = "Forbidden access")
	})
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
