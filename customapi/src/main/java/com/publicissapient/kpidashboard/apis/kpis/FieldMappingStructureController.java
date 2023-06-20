package com.publicissapient.kpidashboard.apis.kpis;

import com.publicissapient.kpidashboard.apis.model.FieldMappingStructureResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
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


	@RequestMapping(value = "/{kpiId} ", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public FieldMappingStructureResponse fetchFieldMappingStructureByKpiFieldMappingData(@PathVariable String kpiId) {
		return kPIHelperService.fetchFieldMappingStructureByKpiFieldMappingData(kpiId);
	}
}
