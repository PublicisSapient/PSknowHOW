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

package com.publicissapient.kpidashboard.apis.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * The Tree aggregator detail.
 * 
 * @author anisingh4
 */
public class TreeAggregatorDetail implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Node root;
	private Map<String, List<Node>> mapOfListOfLeafNodes;
	// This map holds temporary value of nodes
	private Map<String, Node> mapTmp;

	private Map<String, List<Node>> mapOfListOfProjectNodes;

	/**
	 * Instantiates a new Tree aggregator detail.
	 */
	public TreeAggregatorDetail() {
	}

	/**
	 * Instantiates a new Tree aggregator detail.
	 *
	 * @param node
	 *            the node
	 * @param mapOfListOfLeafNodes
	 *            the map of list of leaf nodes
	 * @param mapTmp
	 *            the map tmp
	 * @param mapOfListOfProjectNodes
	 *            the map of list of project nodes
	 */
	public TreeAggregatorDetail(Node node, Map<String, List<Node>> mapOfListOfLeafNodes, Map<String, Node> mapTmp,
			Map<String, List<Node>> mapOfListOfProjectNodes) {
		this.root = node;
		this.mapOfListOfLeafNodes = mapOfListOfLeafNodes;
		this.mapTmp = mapTmp;
		this.mapOfListOfProjectNodes = mapOfListOfProjectNodes;
	}

	/**
	 * Gets root.
	 *
	 * @return the root
	 */
	public Node getRoot() {
		return root;
	}

	/**
	 * Sets root.
	 *
	 * @param root
	 *            the root
	 */
	public void setRoot(Node root) {
		this.root = root;
	}

	/**
	 * Gets map of list of leaf nodes.
	 *
	 * @return the map of list of leaf nodes
	 */
	public Map<String, List<Node>> getMapOfListOfLeafNodes() {
		return mapOfListOfLeafNodes;
	}

	/**
	 * Sets map of list of leaf nodes.
	 *
	 * @param mapOfListOfLeafNodes
	 *            the map of list of leaf nodes
	 */
	public void setMapOfListOfLeafNodes(Map<String, List<Node>> mapOfListOfLeafNodes) {
		this.mapOfListOfLeafNodes = mapOfListOfLeafNodes;
	}

	/**
	 * Gets map tmp.
	 *
	 * @return the map tmp
	 */
	public Map<String, Node> getMapTmp() {
		return mapTmp;
	}

	/**
	 * Sets map tmp.
	 *
	 * @param mapTmp
	 *            the map tmp
	 */
	public void setMapTmp(Map<String, Node> mapTmp) {
		this.mapTmp = mapTmp;
	}

	/**
	 * Gets map of list of project nodes.
	 *
	 * @return the map of list of project nodes
	 */
	public Map<String, List<Node>> getMapOfListOfProjectNodes() {
		return mapOfListOfProjectNodes;
	}

	/**
	 * Sets map of list of tower nodes.
	 *
	 * @param mapOfListOfProjectNodes
	 *            the map of list of tower nodes
	 */
	public void setMapOfListOfProjectNodes(Map<String, List<Node>> mapOfListOfProjectNodes) {
		this.mapOfListOfProjectNodes = mapOfListOfProjectNodes;
	}
}
