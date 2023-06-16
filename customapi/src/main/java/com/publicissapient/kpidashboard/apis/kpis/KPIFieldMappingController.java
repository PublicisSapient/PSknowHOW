package com.publicissapient.kpidashboard.apis.kpis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.common.service.impl.KpiHelperService;
import com.publicissapient.kpidashboard.apis.model.KPIFieldMappingResponse;

/**
 * Rest Controller for all kpi field mapping requests.
 *
 * @author dayshank2
 */
@RestController
@RequestMapping("/kpiFieldMapping")
public class KPIFieldMappingController {
	private final KpiHelperService kPIHelperService;

	/**
	 * Instantiates a new Kpi fieldmapping controller.
	 *
	 * @param kPIHelperService
	 *            the k pi helper service
	 */
	@Autowired
	public KPIFieldMappingController(KpiHelperService kPIHelperService) {
		this.kPIHelperService = kPIHelperService;
	}

	/**
	 * Fetch kip fieldmapping data KpiFieldMapping response.
	 *
	 * @return the KpiFieldMapping response
	 */
	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public KPIFieldMappingResponse fetchKpiFieldMappingData() {
		return kPIHelperService.fetchKpiFieldMappingList();
	}
}
