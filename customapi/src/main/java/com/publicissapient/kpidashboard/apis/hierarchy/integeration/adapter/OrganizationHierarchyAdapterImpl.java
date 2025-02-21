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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.hierarchy.integeration.dto.HierarchyDetails;
import com.publicissapient.kpidashboard.apis.hierarchy.integeration.dto.HierarchyNode;
import com.publicissapient.kpidashboard.common.model.application.OrganizationHierarchy;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrganizationHierarchyAdapterImpl implements OrganizationHierarchyAdapter {
	private final Map<String, OrganizationHierarchy> hierarchyMap = new HashMap<>();

	/*
	 * add logic of converting input datalist to Organization Hierarchy
	 */
	@Override
	public Set<OrganizationHierarchy> convertToOrganizationHierarchy(HierarchyDetails hierarchyDetails) {

		Set<OrganizationHierarchy> transformedList = new HashSet<>();
		List<HierarchyNode> hierarchyNodes = hierarchyDetails.getHierarchyNode();
		ensureHierarchyExists(hierarchyNodes, transformedList);
		// List<HierarchyLevel> hierarchyLevels = hierarchyDetails.getHierarchyLevels();
		return transformedList;

	}

	public void ensureHierarchyExists(List<HierarchyNode> nodes, Set<OrganizationHierarchy> transformedList) {
		for (HierarchyNode node : nodes) {
			List<OrganizationHierarchy> fullNode = new ArrayList<>();
			try {
				String buId = node.getBuUniqueId();
				String verticalId = node.getVerticalUniqueId();
				String accountId = node.getAccountUniqueId();
				String portfolioId = node.getPortfolioUniqueId();
				String projectId = node.getOpportunityUniqueId();

				// Ensure BU exists
				OrganizationHierarchy buNode = createOrUpdateNode(buId, node.getBu(), node.getBu(), "bu", null);
				fullNode.add(buNode);

				// Ensure Vertical exists, create if missing
				if (verticalId == null) {
					verticalId = "vertical_unique_" + buId;
				}
				OrganizationHierarchy verticalNode = createOrUpdateNode(verticalId, node.getVertical(),
						node.getVertical(), "vertical", buNode.getNodeId());
				fullNode.add(verticalNode);

				// Ensure Account exists, create if missing
				if (accountId == null) {
					accountId = "account_unique_" + verticalId;
				}
				OrganizationHierarchy accountNode = createOrUpdateNode(accountId, node.getAccount(), node.getAccount(),
						"account", verticalNode.getNodeId());
				fullNode.add(accountNode);

				// Ensure Portfolio exists, create if missing
				if (portfolioId == null) {
					portfolioId = "portfolio_unique_" + accountId;
				}
				OrganizationHierarchy portfolioNode = createOrUpdateNode(portfolioId, node.getPortfolio(),
						node.getPortfolio(), "portfolio", accountNode.getNodeId());
				fullNode.add(portfolioNode);

				// Ensure Project exists
				OrganizationHierarchy projectNode = createOrUpdateNode(projectId, node.getOpportunity(),
						node.getOpportunity(), "project", portfolioNode.getNodeId());
				fullNode.add(projectNode);

				// If all transformations were successful, add the full node list
				transformedList.addAll(fullNode);
			} catch (Exception e) {
				// Log the error and continue processing the next node
				log.error("Error processing node: " + node.getOpportunityUniqueId() + " - " + e.getMessage());
			}
		}
	}

	public OrganizationHierarchy createOrUpdateNode(String nodeId, String nodeName, String nodeDisplayName,
			String hierarchyLevelId, String parentId) {

		if (hierarchyMap.containsKey(nodeId)) {
			OrganizationHierarchy existingNode = hierarchyMap.get(nodeId);

			// Ensure child has only one parent
			if (!Objects.equals(existingNode.getParentId(), parentId)) {
				throw new IllegalStateException("Node " + nodeId + " cannot have multiple parents!");
			}
			return existingNode;
		}

		// Create a new node if not exists
		OrganizationHierarchy newNode = new OrganizationHierarchy();
		newNode.setNodeId(UUID.randomUUID().toString());
		newNode.setExternalId(nodeId);
		newNode.setNodeName(nodeName);
		newNode.setNodeDisplayName(nodeDisplayName);
		newNode.setHierarchyLevelId(hierarchyLevelId);
		newNode.setParentId(parentId);
		newNode.setCreatedDate(LocalDateTime.now());
		newNode.setModifiedDate(LocalDateTime.now());

		hierarchyMap.put(nodeId, newNode);
		return newNode;
	}

}
