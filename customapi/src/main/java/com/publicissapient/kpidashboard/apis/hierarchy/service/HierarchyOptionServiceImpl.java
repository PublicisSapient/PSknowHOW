/*
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.publicissapient.kpidashboard.apis.hierarchy.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.validation.constraints.NotBlank;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.hierarchy.dto.CreateHierarchyRequest;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.OrganizationHierarchy;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HierarchyOptionServiceImpl implements HierarchyOptionService {

	@Autowired
	private CacheService cacheService;

	@Autowired
	private OrganizationHierarchyService organizationHierarchyService;

	@Override
	public ServiceResponse addHierarchyOption(CreateHierarchyRequest hierarchyRequest, String parentId) {
		List<HierarchyLevel> fullHierarchyLevel = cacheService.getFullHierarchyLevel();

		// Validate hierarchy levels
		if (CollectionUtils.isEmpty(fullHierarchyLevel)) {
			return new ServiceResponse(false, "No Hierarchy Level Found", null);
		}

		// If parentId is empty, create a root node
		if (StringUtils.isEmpty(parentId)) {
			return createRootNode(hierarchyRequest, fullHierarchyLevel);
		}

		// Find parent organization
		OrganizationHierarchy parentOrganization = organizationHierarchyService.findByNodeId(parentId);
		if (parentOrganization == null) {
			return new ServiceResponse(false, "Invalid parentId: No such parent node exists", null);
		}

		// Find hierarchy level of parent
		Optional<HierarchyLevel> parentHierarchyOpt = fullHierarchyLevel.stream()
				.filter(a -> a.getHierarchyLevelId().equalsIgnoreCase(parentOrganization.getHierarchyLevelId()))
				.findFirst();

		if (parentHierarchyOpt.isEmpty()) {
			return new ServiceResponse(false, "Hierarchy level for parent node not found", null);
		}

		int childLevel = parentHierarchyOpt.get().getLevel() + 1;

		// Find the hierarchy level for the child
		Optional<HierarchyLevel> currentLevelOpt = fullHierarchyLevel.stream().filter(a -> a.getLevel() == childLevel)
				.findFirst();

		if (currentLevelOpt.isEmpty()) {
			return new ServiceResponse(false, "Hierarchy level for child node not found", null);
		}

		// Create child node
		OrganizationHierarchy organizationHierarchy = createOrganizationHierarchy(parentId, hierarchyRequest.getName(), currentLevelOpt.get().getHierarchyLevelId());
		return new ServiceResponse(true, "Node created successfully under parentId: " + parentId, organizationHierarchy);
	}

	/**
	 * Creates a root-level node when no parentId is provided.
	 */
	private ServiceResponse createRootNode(CreateHierarchyRequest hierarchyRequest,
			List<HierarchyLevel> fullHierarchyLevel) {
		Optional<HierarchyLevel> topMostHierarchyOpt = fullHierarchyLevel.stream().filter(a -> a.getLevel() == 1)
				.findFirst();

		if (topMostHierarchyOpt.isEmpty()) {
			return new ServiceResponse(false, "No top-level hierarchy found", null);
		}

		OrganizationHierarchy organizationHierarchy = createOrganizationHierarchy(null, hierarchyRequest.getName(), topMostHierarchyOpt.get().getHierarchyLevelId());
		return new ServiceResponse(true, "Node is created at root level.", organizationHierarchy);
	}

	public OrganizationHierarchy createOrganizationHierarchy(String parentId, @NotBlank @NotNull @NotEmpty String hierarchyName, String hierarchyLevelId) {
		OrganizationHierarchy save = new OrganizationHierarchy();
		save.setNodeId(UUID.randomUUID().toString());
		save.setHierarchyLevelId(hierarchyLevelId);
		save.setNodeName(hierarchyName);
		save.setNodeDisplayName(hierarchyName);
		save.setParentId(parentId);
		organizationHierarchyService.save(save);
		organizationHierarchyService.clearCache();
		log.debug("Hierarchy Node create successfully: {}", CommonUtils.sanitize(save.getNodeId()));
		return save;
	}

}
