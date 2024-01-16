package com.publicissapient.kpidashboard.apis.azure.rest;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.azure.model.AzurePipelinesResponseDTO;
import com.publicissapient.kpidashboard.apis.azure.service.AzureToolConfigServiceImpl;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class AzureController {

	@Autowired
	private AzureToolConfigServiceImpl azureToolConfigService;

	/**
	 *
	 * @param connectionId
	 *            the Azure server connection details
	 * @return @{@code ServiceResponse}
	 */
	@GetMapping(value = "/azure/pipeline/{connectionId}/{version}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> getAzurePipelineNameAndDefinitionIdList(@PathVariable String connectionId,
			@PathVariable String version) {
		ServiceResponse response;
		List<AzurePipelinesResponseDTO> pipelinesResponseList = azureToolConfigService
				.getAzurePipelineNameAndDefinitionIdList(connectionId, version);
		if (CollectionUtils.isEmpty(pipelinesResponseList)) {
			response = new ServiceResponse(false, "No Pipelines details found", null);
		} else {
			response = new ServiceResponse(true, "Fetched Pipelines Successfully", pipelinesResponseList);
		}
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	/**
	 *
	 * @param connectionId
	 *            the Azure connection details
	 *
	 * @return @{@code ServiceResponse}
	 */

	@GetMapping(value = "/azure/release/{connectionId}/6.0", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> getAzureReleaseNameAndDefinitionIdList(@PathVariable String connectionId) {
		ServiceResponse response;
		List<AzurePipelinesResponseDTO> releasesResponseList = azureToolConfigService
				.getAzureReleaseNameAndDefinitionIdList(connectionId);
		if (CollectionUtils.isEmpty(releasesResponseList)) {
			response = new ServiceResponse(false, "No Release Pipeline details found", null);
		} else {
			response = new ServiceResponse(true, "Fetched Release Pipeline details Successfully", releasesResponseList);
		}
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
}
