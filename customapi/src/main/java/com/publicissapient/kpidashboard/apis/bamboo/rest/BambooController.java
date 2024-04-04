package com.publicissapient.kpidashboard.apis.bamboo.rest;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.bamboo.model.BambooBranchesResponseDTO;
import com.publicissapient.kpidashboard.apis.bamboo.model.BambooDeploymentProjectsResponseDTO;
import com.publicissapient.kpidashboard.apis.bamboo.model.BambooPlansResponseDTO;
import com.publicissapient.kpidashboard.apis.bamboo.service.BambooToolConfigServiceImpl;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class BambooController {

	@Autowired
	private BambooToolConfigServiceImpl bambooToolConfigService;

	/**
	 *
	 * @param connectionId
	 *            the bamboo server connection details
	 * @return @{@code ServiceResponse}
	 */
	@GetMapping(value = "/bamboo/plans/{connectionId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> getBambooProjectsAndPlanKeys(@PathVariable String connectionId) {
		ServiceResponse response;
		List<BambooPlansResponseDTO> projectKeyList = bambooToolConfigService.getProjectsAndPlanKeyList(connectionId);
		if (CollectionUtils.isEmpty(projectKeyList)) {
			response = new ServiceResponse(false, "No plans details found", null);
		} else {
			response = new ServiceResponse(true, "FETCHED_SUCCESSFULLY", projectKeyList);
		}
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	/**
	 *
	 * @param connectionId
	 *            the bamboo server connection details
	 * @param jobNameKey
	 *            the bamboo server jobNameKey
	 * @return @{@code ServiceResponse}
	 */
	@GetMapping(value = "/bamboo/branches/{connectionId}/{jobNameKey}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> getBambooBranchesNameAndKeys(@PathVariable String connectionId,
			@PathVariable String jobNameKey) {
		ServiceResponse response;
		List<BambooBranchesResponseDTO> projectKeyList = bambooToolConfigService
				.getBambooBranchesNameAndKeys(connectionId, jobNameKey);
		if (CollectionUtils.isEmpty(projectKeyList)) {
			response = new ServiceResponse(false, "No branches details found", null);
		} else {
			response = new ServiceResponse(true, "Fetched successfully", projectKeyList);
		}
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	/**
	 *
	 * @param connectionId
	 *            the bamboo server connection details
	 * @return @{@code ServiceResponse}
	 */
	@GetMapping(value = "/bamboo/deploy/{connectionId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> getBambooDeploymentProject(@PathVariable String connectionId) {
		ServiceResponse response;
		List<BambooDeploymentProjectsResponseDTO> projectKeyList = bambooToolConfigService
				.getDeploymentProjectList(connectionId);
		if (CollectionUtils.isEmpty(projectKeyList)) {
			response = new ServiceResponse(false, "No deployment project found", null);
		} else {
			response = new ServiceResponse(true, "FETCHED_SUCCESSFULLY", projectKeyList);
		}
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
}
