package com.publicissapient.kpidashboard.apis.hierarchy.service;

import java.util.List;

import com.publicissapient.kpidashboard.common.model.application.OrganizationHierarchy;

public interface OrganizationHierarchyService {

	List<OrganizationHierarchy> findAll();

	OrganizationHierarchy findByNodeId(String nodeId);

	OrganizationHierarchy save(OrganizationHierarchy organizationHierarchy);

	void deleteByNodeId(String nodeId);

	void clearCache();
}
