package com.publicissapient.kpidashboard.common.service;

import com.publicissapient.kpidashboard.common.model.application.OrganizationHierarchy;

import java.util.List;

public interface OrganizationHierarchyService {

	List<OrganizationHierarchy> findAllOrganizationHierarchiesFromDB();
}
