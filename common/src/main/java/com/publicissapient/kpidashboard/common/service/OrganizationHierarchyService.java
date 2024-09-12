package com.publicissapient.kpidashboard.common.service;

import com.publicissapient.kpidashboard.common.model.application.OrganizationHierarchy;

import java.util.List;

public interface OrganizationHierarchyService {

	List<OrganizationHierarchy> findAll();

	OrganizationHierarchy findByNodeId(String nodeId);

	OrganizationHierarchy save(OrganizationHierarchy organizationHierarchy);

	void clearCache();
}
