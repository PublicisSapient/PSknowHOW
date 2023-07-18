package com.publicissapient.kpidashboard.apis.kpis;

import com.publicissapient.kpidashboard.apis.model.FieldMappingStructureResponse;
import com.publicissapient.kpidashboard.apis.model.KPIFieldMappingResponse;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.common.model.application.FieldMappingStructure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

	@RequestMapping(value = "{projectBasicConfigId}/{kpiId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public FieldMappingStructureResponse fetchFieldMappingStructureByKpiFieldMappingData(@PathVariable String projectBasicConfigId,@PathVariable String kpiId) {
		projectBasicConfigId = CommonUtils.handleCrossScriptingTaintedValue(projectBasicConfigId);
		return kPIHelperService.fetchFieldMappingStructureByKpiFieldMappingData(projectBasicConfigId,kpiId);
	}
}
