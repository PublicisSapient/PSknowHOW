package com.publicissapient.kpidashboard.apis.datamigration.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.publicissapient.kpidashboard.common.model.application.OrganizationHierarchy;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.repository.application.OrganizationHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SaveService {
	@Autowired
	private ProjectBasicConfigRepository basicConfigRepository;
	@Autowired
	private OrganizationHierarchyRepository organizationHierarchyRepository;

	@Transactional
	public void saveToOrganizationHierarchy(List<OrganizationHierarchy> nodeWiseOrganizationHierarchyList) {
		// Save all data to the repository
		if (organizationHierarchyRepository.count() > 0) {
			organizationHierarchyRepository.deleteAll(); // Delete existing records
		}

		// Save new data
		organizationHierarchyRepository.saveAll(nodeWiseOrganizationHierarchyList);

		log.info("Data successfully saved to the database.");
	}

	@Transactional
	public void saveToBasicConfig(List<ProjectBasicConfig> projectBasicConfigList) {
		if (basicConfigRepository.count() > 0) {
			basicConfigRepository.deleteAll(); // Delete existing records
		}

		// Save new data
		basicConfigRepository.saveAll(projectBasicConfigList);

		log.info("Data successfully saved to the database.");
	}
}
