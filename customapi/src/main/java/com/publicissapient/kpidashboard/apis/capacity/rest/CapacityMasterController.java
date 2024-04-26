package com.publicissapient.kpidashboard.apis.capacity.rest;

import java.util.List;

import javax.validation.Valid;

import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.abac.ContextAwarePolicyEnforcement;
import com.publicissapient.kpidashboard.apis.capacity.service.CapacityMasterService;
import com.publicissapient.kpidashboard.apis.capacity.service.HappinessKpiCapacityImpl;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.constant.Role;
import com.publicissapient.kpidashboard.common.model.application.CapacityMaster;
import com.publicissapient.kpidashboard.common.model.jira.HappinessKpiDTO;

/**
 * @author narsingh9
 *
 */
@RestController
@RequestMapping("/capacity")
public class CapacityMasterController {

	@Autowired
	CapacityMasterService capacityMasterService;

	@Autowired
	private ContextAwarePolicyEnforcement policy;

	@Autowired
	private HappinessKpiCapacityImpl happinessKpiService;

	/**
	 * This api saves capacity data.
	 * 
	 * @param capacityMaster
	 *            data to be saved
	 * @return service response entity
	 */
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> addCapacity(@RequestBody CapacityMaster capacityMaster) {
		ServiceResponse response = new ServiceResponse(false, "Failed to add Capacity Data", null);
		try {
			String projectNodeId = capacityMaster.getProjectNodeId();
			capacityMaster.setBasicProjectConfigId(new ObjectId(projectNodeId
					.substring(projectNodeId.lastIndexOf(CommonConstant.UNDERSCORE) + 1, projectNodeId.length())));
			policy.checkPermission(capacityMaster, "SAVE_UPDATE_CAPACITY");
			capacityMaster = capacityMasterService.processCapacityData(capacityMaster);

			if (null != capacityMaster) {
				response = new ServiceResponse(true, "Successfully added Capacity Data", capacityMaster);
			}
		} catch (AccessDeniedException ade) {
			response = new ServiceResponse(false, "Unauthorized", null);
		}

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@GetMapping("/{basicProjectConfigId}")
	public ResponseEntity<ServiceResponse> getCapacities(@PathVariable String basicProjectConfigId) {
		ServiceResponse response = null;

		List<CapacityMaster> capacities = capacityMasterService.getCapacities(basicProjectConfigId);
		if (CollectionUtils.isNotEmpty(capacities)) {
			response = new ServiceResponse(true, "Capacity Data", capacities);
		} else {
			response = new ServiceResponse(false, "No data", null);
		}
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@RequestMapping(value = "/assignee", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE) // NOSONAR
	public ResponseEntity<ServiceResponse> saveOrUpdateAssignee(@Valid @RequestBody CapacityMaster capacityMaster) {
		policy.checkPermission(capacityMaster, "SAVE_UPDATE_CAPACITY");
		ServiceResponse response = new ServiceResponse(false, "Failed to add Capacity Data", null);
		try {
			capacityMaster = capacityMasterService.processCapacityData(capacityMaster);
			if (null != capacityMaster) {
				response = new ServiceResponse(true, "Successfully added Capacity Data", capacityMaster);
			}
		} catch (AccessDeniedException ade) {
			response = new ServiceResponse(false, "Unauthorized", null);
		}
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@GetMapping("/assignee/roles")
	public ResponseEntity<ServiceResponse> assigneeRolesSuggestion() {
		return ResponseEntity.status(HttpStatus.OK).body(new ServiceResponse(true, "All Roles", Role.getAllRoles()));
	}

	@PostMapping(value = "/jira/happiness", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ServiceResponse> saveHappinessKPIData(@Valid @RequestBody HappinessKpiDTO happinessKpiDTO) {
		return ResponseEntity.status(HttpStatus.OK).body(happinessKpiService.saveHappinessKpiData(happinessKpiDTO));
	}

}
