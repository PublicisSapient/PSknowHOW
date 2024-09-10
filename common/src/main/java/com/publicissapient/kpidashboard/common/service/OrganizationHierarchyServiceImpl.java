package com.publicissapient.kpidashboard.common.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.model.application.OrganizationHierarchy;
import com.publicissapient.kpidashboard.common.repository.application.OrganizationHierarchyRepository;

@Service
public class OrganizationHierarchyServiceImpl implements OrganizationHierarchyService {

	@Autowired
	private OrganizationHierarchyRepository organizationHierarchyRepository;

	@Override
	public List<OrganizationHierarchy> findAllOrganizationHierarchiesFromDB() {
		return organizationHierarchyRepository.findAll();
	}
}
