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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.application.KanbanAccountHierarchy;

public class Node implements Serializable {

	private static final long serialVersionUID = 1L;
	private String id;
	private String name;
	private String parentId;

	private Object value; // NOSONAR
	private Node parent;

	private List<Node> children;
	private int level;
	private String groupName;

	private AccountHierarchy accountHierarchy; // NOSONAR
	private KanbanAccountHierarchy accountHierarchyKanban;

	private ProjectFilter projectFilter;
	private SprintFilter sprintFilter;
	private ReleaseFilter releaseFilter;

	public Node() {
		super();
		this.children = new ArrayList<>();
	}

	/**
	 * 
	 * @param object
	 * @param childId
	 * @param parentId
	 * @param groupName
	 * @param accountHierarchy
	 */
	public Node(Object object, String childId, String name, String parentId, String groupName,
			AccountHierarchy accountHierarchy) {
		this.value = object;
		this.id = childId;
		this.name = name;
		this.parentId = parentId;
		this.groupName = groupName;
		this.accountHierarchy = accountHierarchy;
		this.children = new ArrayList<>();
	}

	public Node(Object object, String childId, String name, String parentId, String groupName,
			AccountHierarchy accountHierarchy, ProjectFilter projectFilter, SprintFilter sprintFilter) {
		this.value = object;
		this.id = childId;
		this.name = name;
		this.parentId = parentId;
		this.groupName = groupName;
		this.accountHierarchy = accountHierarchy;
		this.children = new ArrayList<>();
		this.projectFilter = projectFilter;
		this.sprintFilter = sprintFilter;
	}

	public Node(Object object, String childId, String name, String parentId, String groupName,
			AccountHierarchy accountHierarchy, ProjectFilter projectFilter, SprintFilter sprintFilter,
			ReleaseFilter releaseFilter) {
		this.value = object;
		this.id = childId;
		this.name = name;
		this.parentId = parentId;
		this.groupName = groupName;
		this.accountHierarchy = accountHierarchy;
		this.children = new ArrayList<>();
		this.projectFilter = projectFilter;
		this.sprintFilter = sprintFilter;
		this.releaseFilter = releaseFilter;
	}

	/**
	 * 
	 * @param object
	 * @param childId
	 * @param parentId
	 * @param groupName
	 * @param accountHierarchyKanban
	 */
	public Node(Object object, String childId, String name, String parentId, String groupName,
			KanbanAccountHierarchy accountHierarchyKanban) {
		this.value = object;
		this.id = childId;
		this.name = name;
		this.parentId = parentId;
		this.groupName = groupName;
		this.accountHierarchyKanban = accountHierarchyKanban;
		this.children = new ArrayList<>();
	}

	public Node(Object object, String childId, String name, String parentId, String groupName,
			KanbanAccountHierarchy accountHierarchyKanban, ProjectFilter projectFilter) {
		this.value = object;
		this.id = childId;
		this.name = name;
		this.parentId = parentId;
		this.groupName = groupName;
		this.accountHierarchyKanban = accountHierarchyKanban;
		this.children = new ArrayList<>();
		this.projectFilter = projectFilter;
	}

	/**
	 * 
	 * @return value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Sets value
	 * 
	 * @param value
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * 
	 * @return id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets id
	 * 
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * 
	 * @return parentId
	 */
	public String getParentId() {
		return parentId;
	}

	/**
	 * Sets parentId
	 * 
	 * @param parentId
	 */
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	/**
	 * 
	 * @return parent of type Node
	 */
	public Node getParent() {
		return parent;
	}

	/**
	 * Sets parent
	 * 
	 * @param parent
	 */
	public void setParent(Node parent) {
		this.parent = parent;
	}

	/**
	 * 
	 * @return List of children of type Node
	 */
	public List<Node> getChildren() {
		return children;
	}

	/**
	 * Sets children
	 * 
	 * @param children
	 */
	public void setChildren(List<Node> children) {
		this.children = children;
	}

	/**
	 * Adds an entry to the Children Nodes list
	 * 
	 * @param child
	 */
	public void addChild(Node child) {
		if (null != child && !this.children.contains(child)) {
			this.children.add(child);
		}
	}

	/**
	 * toString() method of String by adding Parent and Children Nodes
	 */
	@Override
	public String toString() {
		return "Node [id=" + id + ", parentId=" + parentId + ", value=" + value + ", children=" + children + ", level="
				+ level + "]";
	}

	/**
	 * 
	 * @return groupName
	 */
	public String getGroupName() {
		return groupName;
	}

	/**
	 * Sets groupName
	 * 
	 * @param groupName
	 */
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	/**
	 * 
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public AccountHierarchy getAccountHierarchy() {
		return accountHierarchy;
	}

	public void setAccountHierarchy(AccountHierarchy accountHierarchy) {
		this.accountHierarchy = accountHierarchy;
	}

	public KanbanAccountHierarchy getAccountHierarchyKanban() {
		return accountHierarchyKanban;
	}

	public void setAccountHierarchyKanban(KanbanAccountHierarchy accountHierarchyKanban) {
		this.accountHierarchyKanban = accountHierarchyKanban;
	}

	public ProjectFilter getProjectFilter() {
		return projectFilter;
	}

	public void setProjectFilter(ProjectFilter projectFilter) {
		this.projectFilter = projectFilter;
	}

	public SprintFilter getSprintFilter() {
		return sprintFilter;
	}

	public void setSprintFilter(SprintFilter sprintFilter) {
		this.sprintFilter = sprintFilter;
	}

	public ReleaseFilter getReleaseFilter() {
		return releaseFilter;
	}

	public void setReleaseFilter(ReleaseFilter releaseFilter) {
		this.releaseFilter = releaseFilter;
	}

	@Override
	public boolean equals(Object obj) {
		boolean isEqual = false;
		if ((null != obj && this.getClass() != obj.getClass()) || null == obj) {
			return false;
		}
		Node other = (Node) obj;
		if (obj instanceof Node && this.id.equals(other.id)
				&& (null == this.parentId || this.parentId.equals(other.parentId))) {
			isEqual = true;

		}
		return isEqual;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id, this.parentId);
	}

}
