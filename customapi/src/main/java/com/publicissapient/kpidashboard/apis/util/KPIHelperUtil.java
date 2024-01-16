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

package com.publicissapient.kpidashboard.apis.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang3.StringUtils;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyDataKanban;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.ProjectFilter;
import com.publicissapient.kpidashboard.apis.model.ReleaseFilter;
import com.publicissapient.kpidashboard.apis.model.SprintFilter;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.application.KanbanAccountHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.KanbanIssueCustomHistory;
import com.publicissapient.kpidashboard.common.model.jira.KanbanJiraIssue;

import lombok.extern.slf4j.Slf4j;

/**
 * This class provides basic utility to create n-ary tree from list of filtered
 * AccountHierarchyData and find leaves to calculate KPI value further.
 *
 * @author tauakram
 */
@SuppressWarnings("PMD.GodClass")
@Slf4j
public final class KPIHelperUtil {

	private KPIHelperUtil() {
	}

	/**
	 * Sets root filter in aggregationTreeNodeList based upon account hierarchy .
	 *
	 * @param aggregationTreeNodeList
	 * @param isKanban
	 */
	private static List<Node> setRootFilterAndLimitHierarchy(List<Node> aggregationTreeNodeList, boolean isKanban,
			String firstLevel, int level) {
		List<Node> filteredNode = new ArrayList<>();
		String rootId = Constant.SPEEDY_ROOT;
		if (isKanban) {
			aggregationTreeNodeList
					.add(new Node(0, rootId, rootId, null, Filters.ROOT.name(), new KanbanAccountHierarchy()));
		} else {
			aggregationTreeNodeList.add(new Node(0, rootId, rootId, null, Filters.ROOT.name(), new AccountHierarchy()));
		}

		aggregationTreeNodeList.stream().filter(node -> node.getGroupName().equalsIgnoreCase(firstLevel))
				.forEach(node -> node.setParentId(rootId));
		if (level > 0) {
			filteredNode = aggregationTreeNodeList.stream().filter(node -> node.getLevel() <= level)
					.collect(Collectors.toList());
		}
		return filteredNode;
	}

	/**
	 * Create a n-ary tree from the list of filtered nodes.
	 *
	 * @param nodes
	 *            of filtered nodes as per UI filter
	 * @param mapTmp
	 *            is a temporary map which holds value (Node) of child nodes.
	 * @return root of the tree
	 */

	public static Node createTree(List<Node> nodes, Map<String, Node> mapTmp) {
		// Save all nodes to a map
		Node root = nodes.parallelStream().filter(node -> node.getId().equalsIgnoreCase(Constant.SPEEDY_ROOT)).findAny()
				.orElse(null);

		if (root != null) {
			Queue<Node> nodeQueue = new LinkedList<>();
			nodeQueue.add(root);

			while (!nodeQueue.isEmpty()) {
				Node currentNode = nodeQueue.poll();
				Set<Node> nodesList = nodes.parallelStream().filter(
						node -> node.getParentId() != null && node.getParentId().equalsIgnoreCase(currentNode.getId()))
						.collect(Collectors.toSet());
				List<Node> list = new ArrayList<>(nodesList);
				currentNode.setChildren(list);
				mapTmp.put(currentNode.getId(), currentNode);
				addProjectFilter(currentNode);
				list.forEach(node -> {
					node.setParent(currentNode);
					nodeQueue.add(node);
				});
			}
		}
		return root;

	}

	private static void addProjectFilter(Node node) {
		if (node.getGroupName().equalsIgnoreCase(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT)) {
			if (isAccountHierarchyData(node)) {
				node.setProjectFilter(new ProjectFilter(node.getId(), node.getName(),
						node.getAccountHierarchy().getBasicProjectConfigId()));
			} else {
				node.setProjectFilter(new ProjectFilter(node.getId(), node.getName(),
						node.getAccountHierarchyKanban().getBasicProjectConfigId()));
			}
		} else if (node.getGroupName().equalsIgnoreCase(CommonConstant.HIERARCHY_LEVEL_ID_SPRINT)) {
			node.setProjectFilter(new ProjectFilter(node.getParent().getId(), node.getParent().getName(),
					node.getAccountHierarchy().getBasicProjectConfigId()));
			node.setSprintFilter(new SprintFilter(node.getId(), node.getName(),
					node.getAccountHierarchy().getBeginDate(), node.getAccountHierarchy().getEndDate()));
		} else if (node.getGroupName().equalsIgnoreCase(CommonConstant.HIERARCHY_LEVEL_ID_RELEASE)) {
			node.setProjectFilter(new ProjectFilter(node.getParent().getId(), node.getParent().getName(),
					node.getAccountHierarchy().getBasicProjectConfigId()));
			node.setReleaseFilter(new ReleaseFilter(node.getId(), node.getName(),
					node.getAccountHierarchy().getBeginDate(), node.getAccountHierarchy().getEndDate()));
		}
	}

	/**
	 * Returns the list of leaf nodes for which KPI value has to be calculated.
	 *
	 * @param node
	 *            the node
	 * @param leafNodeList
	 *            list of leaf nodes in the created tree
	 * @return leaf nodes
	 */
	public static List<Node> getLeafNodes(Node node, List<Node> leafNodeList) {

		if (null == node) {
			return Collections.emptyList();
		}

		if (node.getChildren().isEmpty()) {

			if (isAccountHierarchyData(node)) {
				Node newNode = new Node(node.getValue(), node.getId(), node.getName(), node.getParentId(),
						node.getGroupName(), node.getAccountHierarchy(), node.getProjectFilter(),
						node.getSprintFilter(), node.getReleaseFilter());
				leafNodeList.add(newNode);
			} else if (isAccountHierarchyDataKanban(node)) {
				Node newNode = new Node(node.getValue(), node.getId(), node.getName(), node.getParentId(),
						node.getGroupName(), node.getAccountHierarchyKanban(), node.getProjectFilter());
				leafNodeList.add(newNode);
			}
		}

		List<Node> children = node.getChildren();
		for (Node child : children) {
			if (child.getChildren() != null) {
				if (child.getGroupName().equalsIgnoreCase(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT)) {
					List<Node> sortedChildNodes = new ArrayList<>();
					Map<String, List<Node>> allChildrenMap = child.getChildren().stream()
							.collect(Collectors.groupingBy(Node::getGroupName));
					allChildrenMap.computeIfPresent(CommonConstant.HIERARCHY_LEVEL_ID_SPRINT, (k, v) -> {
						v.sort((node1, node2) -> node2.getSprintFilter().getStartDate()
								.compareTo(node1.getSprintFilter().getStartDate()));
						sortedChildNodes.addAll(v);
						return v;
					});
					allChildrenMap.computeIfPresent(CommonConstant.HIERARCHY_LEVEL_ID_RELEASE, (k, v) -> {
						sortedChildNodes.addAll(v);
						return v;
					});
					child.setChildren(sortedChildNodes);
				}
				getLeafNodes(child, leafNodeList);
			}
		}

		return leafNodeList;

	}

	/**
	 * Checks for account hierarchy data
	 *
	 * @param node
	 * @return
	 */
	private static boolean isAccountHierarchyData(Node node) {
		return null != node.getAccountHierarchy();
	}

	/**
	 * Returns the leaf node grouped by filters and the root of created tree.
	 *
	 * @param kpiRequest
	 *            the kpi request
	 * @param filteredAccountDataList
	 *            the filtered account data list
	 * @param filteredAccountDataKanban
	 *            the filtered account data kanban
	 * @return tree leaf nodes grouped by filter
	 * @throws ApplicationException
	 *             the application exception
	 */
	public static TreeAggregatorDetail getTreeLeafNodesGroupedByFilter(KpiRequest kpiRequest,
			List<AccountHierarchyData> filteredAccountDataList,
			List<AccountHierarchyDataKanban> filteredAccountDataKanban, String firstLevel, int leafNodeLevel)
			throws ApplicationException {

		Map<String, Node> mapTmp = new HashMap<>();
		List<Node> aggregatedTreeNodeList = new ArrayList<>();
		List<Node> filteredNode = null;
		if (CollectionUtils.isNotEmpty(filteredAccountDataList)) {
			filteredAccountDataList.forEach(data -> cloneNode(data.getNode(), aggregatedTreeNodeList));
			filteredNode = setRootFilterAndLimitHierarchy(aggregatedTreeNodeList, false, firstLevel, leafNodeLevel);
		} else if (CollectionUtils.isNotEmpty(filteredAccountDataKanban)) {
			filteredAccountDataKanban.forEach(data -> cloneNode(data.getNode(), aggregatedTreeNodeList));
			filteredNode = setRootFilterAndLimitHierarchy(aggregatedTreeNodeList, true, firstLevel, leafNodeLevel);
		}

		Node root = createTree(filteredNode, mapTmp);

		if (null == root) {
			throw new ApplicationException(KpiRequest.class, "kpiRequestTrackerId", kpiRequest.getRequestTrackerId());
		}

		log.debug("[CREATED-TREE][{}]. Tree created from nodes {}", kpiRequest.getRequestTrackerId(), root);

		List<Node> leafNodeList = new ArrayList<>();
		List<Node> projectNodeList = new ArrayList<>();
		getLeafNodes(root, leafNodeList);
		getProjectNodes(root, projectNodeList);

		log.debug("[LEAF_NODES][{}]. Leaf nodes of the tree {}", kpiRequest.getRequestTrackerId(), leafNodeList);

		Map<String, List<Node>> result = leafNodeList.stream().distinct()
				.collect(Collectors.groupingBy(Node::getGroupName, Collectors.toList()));
		Map<String, List<Node>> projectMap = projectNodeList.stream().distinct()
				.collect(Collectors.groupingBy(Node::getGroupName, Collectors.toList()));
		return new TreeAggregatorDetail(root, result, mapTmp, projectMap);
	}

	private static void cloneNode(List<Node> nodeList, List<Node> aggregatedTreeNodeList) {
		nodeList.forEach(node -> aggregatedTreeNodeList.add((Node) SerializationUtils.clone(node)));
	}

	/**
	 * Distinct by key predicate.
	 *
	 * @param <T>
	 *            the type parameter
	 * @param keyExtractor
	 *            the key extractor
	 * @return predicate
	 */
	public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
		Map<Object, Boolean> seen = new ConcurrentHashMap<>();
		return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}

	/**
	 * Returns the list of project nodes for which KPI value has to be calculated.
	 *
	 * @param node
	 *            the node
	 * @param projectNodeList
	 *            list of leaf nodes in the created tree
	 * @return project nodes
	 */
	public static List<Node> getProjectNodes(Node node, List<Node> projectNodeList) {

		if (null == node) {
			return Collections.emptyList();
		}

		if (Constant.PROJECT.equalsIgnoreCase(node.getGroupName())) {

			if (isAccountHierarchyData(node)) {
				Node newNode = new Node(node.getValue(), node.getId(), node.getName(), node.getParentId(),
						node.getGroupName(), node.getAccountHierarchy(), node.getProjectFilter(),
						node.getSprintFilter());
				projectNodeList.add(newNode);
			} else if (isAccountHierarchyDataKanban(node)) {
				Node newNode = new Node(node.getValue(), node.getId(), node.getName(), node.getParentId(),
						node.getGroupName(), node.getAccountHierarchyKanban(), node.getProjectFilter());
				projectNodeList.add(newNode);
			}
		}

		List<Node> children = node.getChildren();
		for (Node child : children) {
			if (child.getChildren() != null) {
				getProjectNodes(child, projectNodeList);
			}
		}

		return projectNodeList;

	}

	/**
	 * Checks for account hierarchy data kanban
	 *
	 * @param node
	 * @return
	 */
	private static boolean isAccountHierarchyDataKanban(Node node) {
		return null != node.getAccountHierarchyKanban();
	}

	public static Map<String, Long> setpriorityScrum(List<JiraIssue> sprintWiseDefectDataList,
			CustomApiConfig customApiConfig) {
		Map<String, Long> priorityCountMap = new HashMap<>();
		Long p1Count = 0L;
		Long p2Count = 0L;
		Long p3Count = 0L;
		Long p4Count = 0L;
		Long p5Count = 0L;

		for (JiraIssue issue : sprintWiseDefectDataList) {

			if (StringUtils.isBlank(issue.getPriority())) {
				p5Count++;
				priorityCountMap.put(Constant.MISC, p5Count);
			} else {
				if (StringUtils.containsIgnoreCase(
						customApiConfig.getpriorityP1().replaceAll(Constant.WHITESPACE, "").trim(),
						issue.getPriority().replaceAll(Constant.WHITESPACE, "").toLowerCase().trim())) {
					p1Count++;
					priorityCountMap.put(Constant.P1, p1Count);
				} else if (StringUtils.containsIgnoreCase(
						customApiConfig.getpriorityP2().replaceAll(Constant.WHITESPACE, "").trim(),
						issue.getPriority().replaceAll(Constant.WHITESPACE, "").toLowerCase().trim())) {
					p2Count++;
					priorityCountMap.put(Constant.P2, p2Count);
				} else if (StringUtils.containsIgnoreCase(
						customApiConfig.getpriorityP3().replaceAll(Constant.WHITESPACE, "").trim(),
						issue.getPriority().replaceAll(Constant.WHITESPACE, "").toLowerCase().trim())) {
					p3Count++;
					priorityCountMap.put(Constant.P3, p3Count);
				} else if (StringUtils.containsIgnoreCase(
						customApiConfig.getpriorityP4().replaceAll(Constant.WHITESPACE, "").trim(),
						issue.getPriority().replaceAll(Constant.WHITESPACE, "").toLowerCase().trim())) {
					p4Count++;
					priorityCountMap.put(Constant.P4, p4Count);
				} else {
					p5Count++;
					priorityCountMap.put(Constant.MISC, p5Count);
				}
			}
		}

		return priorityCountMap;
	}

	public static Map<String, Long> setpriorityScrumForBacklog(List<JiraIssue> sprintWiseDefectDataList,
			CustomApiConfig customApiConfig) {
		Map<String, Long> priorityCountMap = new HashMap<>();
		Long p1Count = 0L;
		Long p2Count = 0L;
		Long p3Count = 0L;
		Long p4Count = 0L;
		Long p5Count = 0L;

		for (JiraIssue issue : sprintWiseDefectDataList) {

			if (StringUtils.isBlank(issue.getPriority())) {
				p5Count++;
				priorityCountMap.put(Constant.MISC, p5Count);
			} else {
				if (StringUtils.containsIgnoreCase(
						customApiConfig.getpriorityP1().replaceAll(Constant.WHITESPACE, "").trim(),
						issue.getPriority().replaceAll(Constant.WHITESPACE, "").toLowerCase().trim())) {
					p1Count++;
					priorityCountMap.put(Constant.P1, p1Count);
				} else if (StringUtils.containsIgnoreCase(
						customApiConfig.getpriorityP2().replaceAll(Constant.WHITESPACE, "").trim(),
						issue.getPriority().replaceAll(Constant.WHITESPACE, "").toLowerCase().trim())) {
					p2Count++;
					priorityCountMap.put(Constant.P2, p2Count);
				} else if (StringUtils.containsIgnoreCase(
						customApiConfig.getpriorityP3().replaceAll(Constant.WHITESPACE, "").trim(),
						issue.getPriority().replaceAll(Constant.WHITESPACE, "").toLowerCase().trim())) {
					p3Count++;
					priorityCountMap.put(Constant.P3, p3Count);
				} else if (StringUtils.containsIgnoreCase(
						customApiConfig.getpriorityP4().replaceAll(Constant.WHITESPACE, "").trim(),
						issue.getPriority().replaceAll(Constant.WHITESPACE, "").toLowerCase().trim())) {
					p4Count++;
					priorityCountMap.put(Constant.P4, p4Count);
				} else {
					p5Count++;
					priorityCountMap.put(Constant.MISC, p5Count);
				}
			}
		}

		return priorityCountMap;
	}

	public static Map<String, Long> setpriorityKanban(List<KanbanJiraIssue> sprintWiseDefectDataList,
			CustomApiConfig customApiConfig) {
		Map<String, Long> priorityCountMap = new HashMap<>();
		Long p1Count = 0L;
		Long p2Count = 0L;
		Long p3Count = 0L;
		Long p4Count = 0L;
		Long p5Count = 0L;
		for (KanbanJiraIssue issue : sprintWiseDefectDataList) {
			if (StringUtils.isBlank(issue.getPriority())) {
				p5Count++;
				priorityCountMap.put(Constant.MISC, p5Count);
			} else {
				if (customApiConfig.getpriorityP1().replaceAll(Constant.WHITESPACE, "").toLowerCase().trim()
						.contains(issue.getPriority().replaceAll(Constant.WHITESPACE, "").toLowerCase().trim())) {
					p1Count++;
					priorityCountMap.put(Constant.P1, p1Count);
				} else if (customApiConfig.getpriorityP2().replaceAll(Constant.WHITESPACE, "").toLowerCase().trim()
						.contains(issue.getPriority().replaceAll(Constant.WHITESPACE, "").toLowerCase().trim())) {
					p2Count++;
					priorityCountMap.put(Constant.P2, p2Count);
				} else if (customApiConfig.getpriorityP3().replaceAll(Constant.WHITESPACE, "").toLowerCase().trim()
						.contains(issue.getPriority().replaceAll(Constant.WHITESPACE, "").toLowerCase().trim())) {
					p3Count++;
					priorityCountMap.put(Constant.P3, p3Count);
				} else if (customApiConfig.getpriorityP4().replaceAll(Constant.WHITESPACE, "").toLowerCase().trim()
						.contains(issue.getPriority().replaceAll(Constant.WHITESPACE, "").toLowerCase().trim())) {
					p4Count++;
					priorityCountMap.put(Constant.P4, p4Count);
				} else {
					p5Count++;
					priorityCountMap.put(Constant.MISC, p5Count);
				}
			}
		}

		return priorityCountMap;
	}

	public static Map<String, Long> setpriorityKanbanHistory(List<KanbanIssueCustomHistory> sprintWiseDefectDataList,
			CustomApiConfig customApiConfig) {
		Map<String, Long> priorityCountMap = new HashMap<>();
		Long p1Count = 0L;
		Long p2Count = 0L;
		Long p3Count = 0L;
		Long p4Count = 0L;
		Long p5Count = 0L;
		for (KanbanIssueCustomHistory issue : sprintWiseDefectDataList) {
			if (StringUtils.isBlank(issue.getPriority())) {
				p5Count++;
				priorityCountMap.put(Constant.MISC, p5Count);
			} else {
				if (customApiConfig.getpriorityP1().replaceAll(Constant.WHITESPACE, "").toLowerCase().trim()
						.contains(issue.getPriority().replaceAll(Constant.WHITESPACE, "").toLowerCase().trim())) {
					p1Count++;
					priorityCountMap.put(Constant.P1, p1Count);
				} else if (customApiConfig.getpriorityP2().replaceAll(Constant.WHITESPACE, "").toLowerCase().trim()
						.contains(issue.getPriority().replaceAll(Constant.WHITESPACE, "").toLowerCase().trim())) {
					p2Count++;
					priorityCountMap.put(Constant.P2, p2Count);
				} else if (customApiConfig.getpriorityP3().replaceAll(Constant.WHITESPACE, "").toLowerCase().trim()
						.contains(issue.getPriority().replaceAll(Constant.WHITESPACE, "").toLowerCase().trim())) {
					p3Count++;
					priorityCountMap.put(Constant.P3, p3Count);
				} else if (customApiConfig.getpriorityP4().replaceAll(Constant.WHITESPACE, "").toLowerCase().trim()
						.contains(issue.getPriority().replaceAll(Constant.WHITESPACE, "").toLowerCase().trim())) {
					p4Count++;
					priorityCountMap.put(Constant.P4, p4Count);
				} else {
					p5Count++;
					priorityCountMap.put(Constant.MISC, p5Count);
				}
			}
		}

		return priorityCountMap;
	}

	public static String mappingPriority(String priorityFromIssues, CustomApiConfig customApiConfig) {

		if (StringUtils.isBlank(priorityFromIssues)) {
			return Constant.MISC;
		} else if (customApiConfig.getpriorityP1().replaceAll(Constant.WHITESPACE, "").toLowerCase().trim()
				.contains(priorityFromIssues.replaceAll(Constant.WHITESPACE, "").toLowerCase().trim())) {
			return Constant.P1;
		} else if (customApiConfig.getpriorityP2().replaceAll(Constant.WHITESPACE, "").toLowerCase().trim()
				.contains(priorityFromIssues.replaceAll(Constant.WHITESPACE, "").toLowerCase().trim())) {
			return Constant.P2;
		} else if (customApiConfig.getpriorityP3().replaceAll(Constant.WHITESPACE, "").toLowerCase().trim()
				.contains(priorityFromIssues.replaceAll(Constant.WHITESPACE, "").toLowerCase().trim())) {
			return Constant.P3;
		} else if (customApiConfig.getpriorityP4().replaceAll(Constant.WHITESPACE, "").toLowerCase().trim()
				.contains(priorityFromIssues.replaceAll(Constant.WHITESPACE, "").toLowerCase().trim())) {
			return Constant.P4;
		} else {
			return Constant.MISC;
		}
	}

}
