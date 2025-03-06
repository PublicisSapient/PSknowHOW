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

package com.publicissapient.kpidashboard.apis.hierarchy.integration.adapter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.hierarchy.integration.dto.HierarchyDetails;
import com.publicissapient.kpidashboard.apis.hierarchy.integration.dto.HierarchyLevel;
import com.publicissapient.kpidashboard.apis.hierarchy.integration.dto.HierarchyNode;
import com.publicissapient.kpidashboard.common.model.application.OrganizationHierarchy;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrganizationHierarchyAdapterImpl implements OrganizationHierarchyAdapter {
	private final Map<String, OrganizationHierarchy> hierarchyMap = new HashMap<>();
	@Autowired
	private HierarchyLevelService hierarchyLevelService;

	/*
	 * add logic of converting input datalist to Organization Hierarchy
	 */
	@Override
	public Set<OrganizationHierarchy> convertToOrganizationHierarchy(HierarchyDetails hierarchyDetails) {
		Set<OrganizationHierarchy> transformedList = new HashSet<>();
		List<HierarchyNode> hierarchyNodes = hierarchyDetails.getHierarchyNode();
		List<String> centralHieracyLevels = hierarchyDetails.getHierarchyLevels().parallelStream()
				.filter(a -> a.getLevel() > 0).sorted(Comparator.comparing(HierarchyLevel::getLevel))
				.map(k -> k.getDisplayName().toUpperCase()).toList();
		Map<String, String> localLevels = getHierachyLevelTillProject().stream().limit(centralHieracyLevels.size())
				.collect(Collectors.toMap(
						(com.publicissapient.kpidashboard.common.model.application.HierarchyLevel h) -> h
								.getHierarchyLevelName().toUpperCase(),
						com.publicissapient.kpidashboard.common.model.application.HierarchyLevel::getHierarchyLevelId));
		List<String> levels = new ArrayList<>();
		for (String hierarchyNode : centralHieracyLevels) {
			levels.add(getMatchingValue(localLevels, hierarchyNode));
		}

		ensureHierarchyExists(hierarchyNodes, transformedList, levels);
		return transformedList;

	}

	private List<com.publicissapient.kpidashboard.common.model.application.HierarchyLevel> getHierachyLevelTillProject() {
		List<com.publicissapient.kpidashboard.common.model.application.HierarchyLevel> topHierarchyLevels = hierarchyLevelService
				.getTopHierarchyLevels();
		topHierarchyLevels.add(hierarchyLevelService.getProjectHierarchyLevel());
		return topHierarchyLevels;
	}

	public void ensureHierarchyExists(List<HierarchyNode> nodes, Set<OrganizationHierarchy> transformedList,
			List<String> centralHierarchyLevels) {
		for (HierarchyNode node : nodes) {
			try {
				List<OrganizationHierarchy> fullNode = processHierarchyNode(node, centralHierarchyLevels);
				if (!fullNode.isEmpty()) {
					transformedList.addAll(fullNode);
				}
			} catch (Exception e) {
				log.error("Error processing node: " + node.getOpportunityUniqueId() + " - " + e.getMessage(), e);
			}
		}
	}

	private List<OrganizationHierarchy> processHierarchyNode(HierarchyNode node, List<String> centralHierarchyLevels) {
		List<OrganizationHierarchy> fullNode = new ArrayList<>();

		Map<String, OrganizationHierarchy> createdNodes = new HashMap<>();
		Map<String, String> idMappings = getNodeIdMappings(node); // Extracts all IDs

		for (String chsLevel : centralHierarchyLevels) {
			String parentLevel = getParentLevel(chsLevel);
			String parentId = parentLevel == null ? null
					: createdNodes.getOrDefault(parentLevel, null) != null ? createdNodes.get(parentLevel).getNodeId()
							: null;

			// If it's not "bu" and parent is null, skip hierarchy
			if (!"bu".equals(chsLevel) && parentId == null) {
				log.warn("Skipping " + chsLevel + " as parent is missing for node: " + node);
				return Collections.emptyList();
			}

			OrganizationHierarchy newNode = createOrUpdateNode(idMappings.get(chsLevel), getNodeName(node, chsLevel),
					chsLevel, parentId);

			createdNodes.put(chsLevel, newNode);
			fullNode.add(newNode);
		}

		return fullNode;
	}

	private Map<String, String> getNodeIdMappings(HierarchyNode node) {
		Map<String, String> idMappings = new HashMap<>();
		idMappings.put("bu", node.getBuUniqueId());
		idMappings.put("ver", node.getVerticalUniqueId());
		idMappings.put("acc", node.getAccountUniqueId());
		idMappings.put("port", node.getPortfolioUniqueId());
		idMappings.put("project", node.getOpportunityUniqueId());

		idMappings.replaceAll((k, v) -> StringUtils.isEmpty(v) ? k + "_unique_" + UUID.randomUUID() : v);
		return idMappings;
	}

	private String getNodeName(HierarchyNode node, String chsLevel) {
		switch (chsLevel) {
		case "bu":
			return node.getBu();
		case "ver":
			return node.getVertical();
		case "acc":
			return node.getAccount();
		case "port":
			return node.getPortfolio();
		case "project":
			return node.getOpportunity();
		default:
			throw new IllegalArgumentException("Invalid hierarchy level: " + chsLevel);
		}
	}

	private String getParentLevel(String chsLevel) {
		switch (chsLevel) {
		case "bu":
			return null;
		case "ver":
			return "bu";
		case "acc":
			return "ver";
		case "port":
			return "acc";
		case "project":
			return "port";
		default:
			throw new IllegalArgumentException("Invalid hierarchy level: " + chsLevel);
		}
	}

	public OrganizationHierarchy createOrUpdateNode(String nodeId, String nodeName, String hierarchyLevelId,
			String parentId) {

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
		newNode.setNodeDisplayName(nodeName);
		newNode.setHierarchyLevelId(hierarchyLevelId);
		newNode.setParentId(parentId);
		newNode.setCreatedDate(LocalDateTime.now());
		newNode.setModifiedDate(LocalDateTime.now());

		hierarchyMap.put(nodeId, newNode);
		return newNode;
	}

	public static String getMatchingValue(Map<String, String> dataMap, String inputKey) {
		String[] possibleKeys = inputKey.split("[/\\s]+");
		for (String key : possibleKeys) {
			if (dataMap.containsKey(key)) {
				return dataMap.get(key); // Return value of "Project"
			}
		}
		throw new RuntimeException("Hierarchy Missing");
	}

}
