package com.publicissapient.kpidashboard.apis.kpis;

import com.publicissapient.kpidashboard.apis.model.FieldMappingStructureResponse;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest Controller for all kpi field mapping requests.
 *
 * @author dayshank2
 */
@RestController
@RequestMapping("/kpiFieldMapping")
public class FieldMappingStructureController {
	private final KpiHelperService kPIHelperService;

	/**
	 * Instantiates a new Kpi fieldmapping controller.
	 *
	 * @param kPIHelperService
	 *            the k pi helper service
	 */

	@Autowired
	public FieldMappingStructureController(KpiHelperService kPIHelperService) {
		this.kPIHelperService = kPIHelperService;
	}

	@GetMapping(value = "{projectBasicConfigId}/{kpiId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> fetchFieldMappingStructureByKpiFieldMappingData(
			@PathVariable String projectBasicConfigId, @PathVariable String kpiId) {
		projectBasicConfigId = CommonUtils.handleCrossScriptingTaintedValue(projectBasicConfigId);
		FieldMappingStructureResponse result = kPIHelperService.fetchFieldMappingStructureByKpiId(projectBasicConfigId, kpiId);
		ServiceResponse response = null;
		if (result == null) {
			response = new ServiceResponse(false, "no field mapping stucture found", null);
		} else {
			response = new ServiceResponse(true, "field mapping stucture", result);
		}

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
}
