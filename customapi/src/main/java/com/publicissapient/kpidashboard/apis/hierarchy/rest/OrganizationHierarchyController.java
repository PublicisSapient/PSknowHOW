package com.publicissapient.kpidashboard.apis.hierarchy.rest;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.application.OrganizationHierarchy;
import com.publicissapient.kpidashboard.apis.hierarchy.service.OrganizationHierarchyService;

@RestController
@RequestMapping("/organizationHierarchy")
public class OrganizationHierarchyController {

	@Autowired
	private OrganizationHierarchyService organizationHierarchyService;

	@GetMapping
	public ResponseEntity<ServiceResponse> getHierarchyLevel() {

		List<OrganizationHierarchy> organizationHierarchies = organizationHierarchyService.findAll();

		if (CollectionUtils.isNotEmpty(organizationHierarchies)) {
			return ResponseEntity.status(HttpStatus.OK).body(new ServiceResponse(true,
					"Fetched organization Hierarchies Successfully.", organizationHierarchies));
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ServiceResponse(false, "Not Found Any Organization Hierarchies.", null));
		}
	}

}
