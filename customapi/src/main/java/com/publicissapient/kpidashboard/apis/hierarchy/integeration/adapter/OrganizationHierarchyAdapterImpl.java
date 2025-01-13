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

package com.publicissapient.kpidashboard.apis.hierarchy.integeration.adapter;

import java.util.List;

import com.publicissapient.kpidashboard.common.model.application.OrganizationHierarchy;

public class OrganizationHierarchyAdapterImpl implements OrganizationHierarchyAdapter {

	/*
	 * add logic of converting input datalist to Organization Hierarchy
	 */
	@Override
	public List<OrganizationHierarchy> convertToOrganizationHierarchy() {

		/*
		 * List<OrganizationHierarchy> hierarchyList = new ArrayList<>();
		 * 
		 * List<HierarchyNode> hierarchyNodes = hierarchyDetails.getHierarchyNode();
		 * List<HierarchyLevel> hierarchyLevels = hierarchyDetails.getHierarchyLevels();
		 * 
		 * for (HierarchyNode node : hierarchyNodes) { // Map the hierarchy node fields
		 * to OrganizationHierarchy OrganizationHierarchy hierarchy = new
		 * OrganizationHierarchy(); hierarchy.setNodeId(node.getVertical_unique_id());
		 * hierarchy.setNodeName(node.getVertical());
		 * hierarchy.setNodeDisplayName(node.getVertical());
		 * hierarchy.setHierarchyLevelId(getLevelId(node.getVertical_unique_id(),
		 * hierarchyLevels)); hierarchy.setParentId(node.getBU_unique_id());
		 * hierarchy.setCreatedDate(LocalDateTime.now());
		 * hierarchy.setModifiedDate(LocalDateTime.now());
		 * hierarchy.setExternalId(node.getVertical_id());
		 * 
		 * hierarchyList.add(hierarchy); }
		 * 
		 * return hierarchyList;
		 */
		return List.of();
	}
}
