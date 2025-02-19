package com.publicissapient.kpidashboard.apis.kpis;

import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.model.FieldMappingStructureResponse;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.apis.util.ProjectAccessUtil;

/**
 * Rest Controller for all kpi field mapping requests.
 *
 * @author dayshank2
 */
@RestController
@RequestMapping("/kpiFieldMapping")
public class FieldMappingStructureController {
	private final KpiHelperService kPIHelperService;

	private ProjectAccessUtil projectAccessUtil;

	/**
	 * Instantiates a new Kpi fieldmapping controller.
	 *
	 * @param kPIHelperService
	 *          the k pi helper service
	 */
	@Autowired
	public FieldMappingStructureController(KpiHelperService kPIHelperService, ProjectAccessUtil projectAccessUtil) {
		this.kPIHelperService = kPIHelperService;
		this.projectAccessUtil = projectAccessUtil;
	}

	@GetMapping(value = "{projectBasicConfigId}/{kpiId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> fetchFieldMappingStructureByKpiFieldMappingData(
			@PathVariable String projectBasicConfigId, @PathVariable String kpiId) {
		projectBasicConfigId = CommonUtils.handleCrossScriptingTaintedValue(projectBasicConfigId);
		ServiceResponse response = null;
		boolean hasProjectAccess = projectAccessUtil.configIdHasUserAccess(projectBasicConfigId);
		if (!hasProjectAccess) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(new ServiceResponse(false, "Unauthorized to get the kpi field mapping", "Unauthorized"));
		}
		FieldMappingStructureResponse result = kPIHelperService.fetchFieldMappingStructureByKpiId(projectBasicConfigId,
				kpiId);

		if (result == null) {
			response = new ServiceResponse(false, "no field mapping stucture found", null);
		} else {
			if (StringUtils.isNotEmpty(result.getProjectToolConfigId())) {
				result.setKpiSource(kPIHelperService.updateKPISource(new ObjectId(projectBasicConfigId),
						new ObjectId(result.getProjectToolConfigId())));
				response = new ServiceResponse(true, "field mapping stucture", result);
			} else {
				response = new ServiceResponse(true, "Tool Source Absent", result);
			}
		}

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
}
