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
package com.publicissapient.kpidashboard.apis.hierarchy.integration.service;

import com.publicissapient.kpidashboard.common.model.application.OrganizationHierarchy;
import com.publicissapient.kpidashboard.common.repository.application.OrganizationHierarchyRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class HierarchyComparisonServiceImpl implements HierarchyComparisonService {

    private static final int BATCH_SIZE = 100;

    @Autowired
    private OrganizationHierarchyRepository organizationHierarchyRepository;

    /**
     * Creates a set of node names for case-insensitive matching Includes both
     * nodeName and nodeDisplayName for accurate matching
     */
    private Set<String> createNodeNameSet(Set<OrganizationHierarchy> nodes) {
        Set<String> nodeNames = new HashSet<>();

        nodes.forEach(node -> {
            if (StringUtils.isNotEmpty(node.getNodeName())) {
                nodeNames.add(node.getNodeName());
            }
            if (StringUtils.isNotEmpty(node.getNodeDisplayName())) {
                nodeNames.add(node.getNodeDisplayName());
            }
        });

        return nodeNames;
    }

    @Override
    public Set<OrganizationHierarchy> compareAndUpdateHierarchy(Set<OrganizationHierarchy> apiHierarchy) {
        // Group API nodes by level for processing
        Map<String, Set<OrganizationHierarchy>> apiHierarchyByLevel = groupByLevel(apiHierarchy);

        // Process each level separately for better memory efficiency
        Map<String, Map<NodeKey, Set<OrganizationHierarchy>>> dbNodesByLevel = new HashMap<>();

        // Get ordered hierarchy levels
        List<String> orderedLevels = getOrderedHierarchyLevels();

        // Process each level
        for (String level : orderedLevels) {
            Set<OrganizationHierarchy> levelNodes = apiHierarchyByLevel.get(level);
            if (levelNodes == null || levelNodes.isEmpty()) {
                continue;
            }

            // Create name set for batch query
            Set<String> nodeNames = createNodeNameSet(levelNodes);

            // Query DB for matching nodes at this level using case-insensitive collation
            List<OrganizationHierarchy> matchingNodes = organizationHierarchyRepository
                    .findByNodeNamesAndLevel(nodeNames, level);

            // Create lookup map for this level
            Map<NodeKey, Set<OrganizationHierarchy>> collect = matchingNodes.stream()
                    .collect(Collectors.groupingBy(a -> new NodeKey(a.getNodeName()), Collectors.toSet()));
            Map<NodeKey, Set<OrganizationHierarchy>> collect1 = matchingNodes.stream()
                    .collect(Collectors.groupingBy(a -> new NodeKey(a.getNodeDisplayName()), Collectors.toSet()));
            collect.forEach((key, value) -> collect1.merge(key, value, (v1, v2) -> {
                v1.addAll(v2);
                return v1;
            }));

            dbNodesByLevel.put(level, collect1);
        }

        // Process top level (BU) nodes first
        Map<Pair<String, String>, String> processedNodesExternalIds = new HashMap<>();
        String buLevel = "bu";
        Set<OrganizationHierarchy> processedBuNodes = processTopLevelNodes(
                apiHierarchyByLevel.getOrDefault(buLevel, new HashSet<>()),
                dbNodesByLevel.getOrDefault(buLevel, new HashMap<>()), processedNodesExternalIds);

        // Process child levels
        Set<OrganizationHierarchy> result = new HashSet<>(processedBuNodes);

        for (String level : orderedLevels) {
            if (!level.equals(buLevel)) {
                Set<OrganizationHierarchy> processedLevel = processChildNodes(
                        apiHierarchyByLevel.getOrDefault(level, new HashSet<>()),
                        dbNodesByLevel.getOrDefault(level, new HashMap<>()), processedNodesExternalIds);
                result.addAll(processedLevel);
            }
        }

        // if chs data completely okay, first we need to change internal data, otherwise, we will have 2 node ids where both have them are correct
		/*Set<String> strings = new HashSet<>(processedNodesExternalIds.values());
		result.addAll(apiHierarchy.stream().filter(api->!strings.contains(api.getExternalId())).toList());

		 */
        // Batch save all updates with optimal batch size
        if (CollectionUtils.isNotEmpty(result)) {
            int optimalBatchSize = Math.min(BATCH_SIZE, result.size());
            List<List<OrganizationHierarchy>> batches = new ArrayList<>(result).stream()
                    .collect(Collectors.groupingBy(node -> result.size() / optimalBatchSize)).values().stream()
                    .collect(Collectors.toList());

            for (List<OrganizationHierarchy> batch : batches) {
                //		organizationHierarchyRepository.saveAll(batch);
            }
        }

        return result;
    }

    private List<String> getOrderedHierarchyLevels() {
        return List.of("bu", "ver", "acc", "port");
    }

    private Map<String, Set<OrganizationHierarchy>> groupByLevel(Set<OrganizationHierarchy> hierarchy) {
        return hierarchy.stream()
                .collect(Collectors.groupingBy(OrganizationHierarchy::getHierarchyLevelId, Collectors.toSet()));
    }

    private Set<OrganizationHierarchy> processTopLevelNodes(Set<OrganizationHierarchy> apiNodes,
                                                            Map<NodeKey, Set<OrganizationHierarchy>> dbNodeMap,
                                                            Map<Pair<String, String>, String> processedNodesExternalIds) {
        Set<OrganizationHierarchy> result = new HashSet<>();

        for (OrganizationHierarchy apiNode : apiNodes) {
            NodeKey nodeKey = new NodeKey(apiNode.getNodeName());
            Set<OrganizationHierarchy> matchingNodes = dbNodeMap.get(nodeKey);

            if (CollectionUtils.isNotEmpty(matchingNodes)) {
                matchingNodes.forEach(matchingNode -> {
                    matchingNode.setExternalId(apiNode.getExternalId());
                    processedNodesExternalIds.put(Pair.of(apiNode.getNodeId(), matchingNode.getNodeId()),
                            apiNode.getExternalId());
                    result.add(matchingNode);
                    log.info("Updated top-level node: {} with externalId: {}", matchingNode.getNodeName(),
                            matchingNode.getExternalId());

                });

            }
        }

        return result;
    }

    /**
     * Process child level nodes, ensuring parent hierarchy is maintained
     */
    private Set<OrganizationHierarchy> processChildNodes(Set<OrganizationHierarchy> apiNodes,
                                                         Map<NodeKey, Set<OrganizationHierarchy>> dbNodesMap,
                                                         Map<Pair<String, String>, String> processedNodesExternalIds) {

        Set<OrganizationHierarchy> result = new HashSet<>();

        for (OrganizationHierarchy apiNode : apiNodes) {
            NodeKey nodeKey = new NodeKey(apiNode.getNodeName());
            Set<OrganizationHierarchy> matchingNodes = dbNodesMap.get(nodeKey);

            if (CollectionUtils.isNotEmpty(matchingNodes)) {
                // Check if parent has externalId in previously processed nodes
                for (OrganizationHierarchy matchingNode : matchingNodes) {
                    String parentExternalId = processedNodesExternalIds
                            .get(Pair.of(apiNode.getParentId(), matchingNode.getParentId()));
                    if (StringUtils.isEmpty(parentExternalId)) {
                        log.info("Skipping node at level: {} with name: {} as parent has no externalId",
                                apiNode.getHierarchyLevelId(), matchingNode.getNodeName());
                        continue;
                    }

                    matchingNode.setExternalId(apiNode.getExternalId());
                    result.add(matchingNode);
                    processedNodesExternalIds.put(Pair.of(apiNode.getNodeId(), matchingNode.getNodeId()),
                            apiNode.getExternalId());
                    log.info("Updated child node: {} with externalId: {}", matchingNode.getNodeName(),
                            matchingNode.getExternalId());

                }

            }
        }

        return result;
    }

    /**
     * Wrapper class for node names to provide case-insensitive key functionality
     */
    private static class NodeKey {
        private final String name;

        public NodeKey(String name) {
            this.name = name != null ? name.toLowerCase() : null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            NodeKey nodeKey = (NodeKey) o;
            return name != null && name.equals(nodeKey.name);
        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }
    }
}
