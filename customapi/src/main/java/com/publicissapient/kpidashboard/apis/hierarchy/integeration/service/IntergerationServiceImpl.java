/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.apis.hierarchy.integeration.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.hierarchy.integeration.adapter.OrganizationHierarchyAdapter;
import com.publicissapient.kpidashboard.apis.hierarchy.service.OrganizationHierarchyService;
import com.publicissapient.kpidashboard.common.model.application.OrganizationHierarchy;
import com.publicissapient.kpidashboard.common.repository.application.OrganizationHierarchyRepository;

@Service
public class IntergerationServiceImpl implements IntegerationService {

	@Autowired
	OrganizationHierarchyRepository organizationHierarchyRepository;
	@Autowired
	OrganizationHierarchyAdapter organizationHierarchyAdapter;
	@Autowired
	OrganizationHierarchyService organizationHierarchyService;

	@Override
	public void syncOrganizationHierarchy(List<OrganizationHierarchy> externalList) {
		// Step 1: Extract externalIds from the external list
		List<String> externalIds = externalList.stream().map(OrganizationHierarchy::getExternalId)
				.collect(Collectors.toList());

		// Step 2: Fetch existing records from the database by externalId
		List<OrganizationHierarchy> databaseList = organizationHierarchyRepository.findByExternalIds(externalIds);

		// Step 3: Map database records by externalId for quick lookup
		Map<String, OrganizationHierarchy> databaseMap = databaseList.stream()
				.collect(Collectors.toMap(OrganizationHierarchy::getExternalId, node -> node));

		// Step 4: Prepare lists for inserts and updates
		List<OrganizationHierarchy> nodesToInsert = new ArrayList<>();

		for (OrganizationHierarchy externalNode : externalList) {
			String externalId = externalNode.getExternalId();
			OrganizationHierarchy dbNode = databaseMap.get(externalId);

			if (dbNode == null) {
				// New node: set createdDate and modifiedDate, and add to insert list
				externalNode.setCreatedDate(LocalDateTime.now());
				externalNode.setModifiedDate(LocalDateTime.now());
				nodesToInsert.add(externalNode);
			} else {
				// Existing node: update fields and add to update list
				dbNode.setNodeName(externalNode.getNodeName());
				dbNode.setNodeDisplayName(externalNode.getNodeDisplayName());
				dbNode.setHierarchyLevelId(externalNode.getHierarchyLevelId());
				dbNode.setParentId(externalNode.getParentId());
				dbNode.setModifiedDate(LocalDateTime.now());
				nodesToInsert.add(dbNode);
			}
		}

		// Step 5: Perform batch database operations
		if (CollectionUtils.isNotEmpty(nodesToInsert)) {
			organizationHierarchyService.saveAll(nodesToInsert);
			organizationHierarchyService.clearCache();

		}
	}

	@Override
	public List<OrganizationHierarchy> convertHieracyResponseToOrganizationHierachy() {
		return organizationHierarchyAdapter.convertToOrganizationHierarchy();

	}

}
