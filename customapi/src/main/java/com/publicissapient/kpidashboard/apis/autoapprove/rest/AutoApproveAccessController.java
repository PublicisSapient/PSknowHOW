package com.publicissapient.kpidashboard.apis.autoapprove.rest;

import java.util.Arrays;

import javax.validation.Valid;

import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.autoapprove.service.AutoApproveAccessService;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.rbac.AutoApproveAccessConfig;
import com.publicissapient.kpidashboard.common.model.rbac.AutoApproveAccessConfigDTO;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/autoapprove")
@Slf4j
public class AutoApproveAccessController {

	@Autowired
	AutoApproveAccessService autoApproveService;

	@PostMapping
	@PreAuthorize("hasPermission(null, 'ENABLE_AUTO_APPROVE')")
	public ResponseEntity<ServiceResponse> saveAutoApproveRoles(
			@Valid @RequestBody AutoApproveAccessConfigDTO autoAcessDTO) {
		final ModelMapper modelMapper = new ModelMapper();
		final AutoApproveAccessConfig autoApproveRole = modelMapper.map(autoAcessDTO, AutoApproveAccessConfig.class);
		AutoApproveAccessConfig approvalConfig = autoApproveService.saveAutoApproveConfig(autoApproveRole);
		log.info("saved Auto Approve Roles");
		return ResponseEntity.status(HttpStatus.OK)
				.body(new ServiceResponse(true, "Added new  auto approve role", Arrays.asList(approvalConfig)));
	}

	@GetMapping
	@PreAuthorize("hasPermission(null, 'ENABLE_AUTO_APPROVE')")
	public ResponseEntity<ServiceResponse> getAutoApproveConfig() {
		log.info("Getting all requests");
		AutoApproveAccessConfig autoAccessApprovalData = autoApproveService.getAutoApproveConfig();
		if (autoAccessApprovalData == null) {
			log.info("No roles found for auto approval in db");
			return ResponseEntity.status(HttpStatus.OK).body(
					new ServiceResponse(false, "auto approval not configured", Arrays.asList(autoAccessApprovalData)));
		}
		log.info("Fetched roles for auto access successfully");
		return ResponseEntity.status(HttpStatus.OK).body(
				new ServiceResponse(true, "Found all roles for auto approval", Arrays.asList(autoAccessApprovalData)));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasPermission(null, 'ENABLE_AUTO_APPROVE')")
	public ResponseEntity<ServiceResponse> modifyAutoApprovConfigById(@PathVariable("id") String id,
			@Valid @RequestBody AutoApproveAccessConfigDTO autoAcessDTO) {
		ModelMapper modelMapper = new ModelMapper();
		AutoApproveAccessConfig autoApproveRole = modelMapper.map(autoAcessDTO, AutoApproveAccessConfig.class);

		if (!ObjectId.isValid(id)) {
			log.info("Id not valid");
			return ResponseEntity.status(HttpStatus.OK).body(new ServiceResponse(false,
					"access_request@" + id + " does not exist", Arrays.asList(autoAcessDTO)));
		}

		AutoApproveAccessConfig autoApproveData = autoApproveService.modifyAutoApprovConfigById(id, autoApproveRole);
		log.info("Modifying request@{}", id);
		return ResponseEntity.status(HttpStatus.OK)
				.body(new ServiceResponse(true, "modified access_request@" + id, Arrays.asList(autoApproveData)));
	}
}
