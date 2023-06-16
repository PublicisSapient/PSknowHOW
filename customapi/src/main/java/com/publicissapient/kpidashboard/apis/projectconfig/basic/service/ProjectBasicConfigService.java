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

package com.publicissapient.kpidashboard.apis.projectconfig.basic.service;

import java.util.List;
import java.util.Map;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.dto.ProjectBasicConfigDTO;
import com.publicissapient.kpidashboard.common.model.rbac.ProjectBasicConfigNode;

/**
 * @author narsingh9
 *
 */
public interface ProjectBasicConfigService {

	/**
	 * Service to save a new project's basic configuration in the
	 * project_basic_config collection
	 * 
	 * @param projectBasicConfigDTO
	 *            detail to be saved.
	 * @return {@link ServiceResponse} object.
	 */
	ServiceResponse addBasicConfig(ProjectBasicConfigDTO projectBasicConfigDTO);

	/**
	 * 
	 * Service to update an existing project's basic configuration in the
	 * project_basic_config collection
	 * 
	 * @param basicConfigId
	 * @param projectBasicConfigDTO
	 *            detail to be updated.
	 * @return {@link ServiceResponse} object.
	 */
	ServiceResponse updateBasicConfig(String basicConfigId, ProjectBasicConfigDTO projectBasicConfigDTO);

	/**
	 * Service to fetch the list of project basic configuration in the
	 * project_basic_config collection
	 * 
	 * @param basicProjectConfigIds
	 *            : if null or empty, return null
	 * @return {@link ProjectBasicConfig }
	 */
	ProjectBasicConfig getProjectBasicConfigs(String basicProjectConfigIds);

	/**
	 * Service to fetch the list of all project basic configuration in the
	 * project_basic_config collection
	 * 
	 * @return {@code List<ProjectBasicConfig>} : empty list incase no data found
	 */
	List<ProjectBasicConfig> getAllProjectsBasicConfigs();

	/**
	 * Service to fetch the list of all project basic configuration in the
	 * project_basic_config collection
	 * 
	 * @return {@code List<ProjectBasicConfig>} : empty list in case no data found
	 */
	List<ProjectBasicConfig> getAllProjectsBasicConfigsWithoutPermission();

	/**
	 * Delete basic project congig
	 * 
	 * @param basicProjectConfigId
	 * @return deleted ProjectBasicConfig
	 */
	ProjectBasicConfig deleteProject(String basicProjectConfigId);

	/**
	 * Service to fetch the list of all project basic configuration including all
	 * hierarchy levels property
	 * 
	 * @return {@code List<ProjectBasicConfigDTO>} : empty list incase no data found
	 */
	List<ProjectBasicConfigDTO> getAllProjectsBasicConfigsDTOWithoutPermission();

	/**
	 * Service to fetch the map of all project basic configuration including all
	 * hierarchy levels
	 *
	 * @return {@code Map<String, ProjectBasicConfigDTO> }: empty map incase no data
	 *         found
	 */
	Map<String, ProjectBasicConfigDTO> getBasicConfigsDTOMapWithoutPermission();

	/**
	 * 
	 * @return {@code ProjectBasicConfigNode }: empty object incase no data found
	 */
	ProjectBasicConfigNode getBasicConfigTree();

	/**
	 * Method to find out the node from a project basic config tree
	 * 
	 * @param node
	 * @param searchValue
	 * @param groupName
	 * @return
	 */
	ProjectBasicConfigNode findNode(ProjectBasicConfigNode node, String searchValue, String groupName);

	/**
	 * Method to find out all the child nodes of a tree
	 * 
	 * @param node
	 */
	void findChildren(ProjectBasicConfigNode node, List<ProjectBasicConfigNode> children);

	/**
	 * Method to find out all the parent nodes of a tree
	 * 
	 * @param nodes
	 * @param parents
	 */
	void findParents(List<ProjectBasicConfigNode> nodes, List<ProjectBasicConfigNode> parents);

	/**
	 * Method to find out all the projects nodes of a tree
	 * 
	 * @param node
	 * @param leafNodes
	 */
	void findLeaf(ProjectBasicConfigNode node, List<ProjectBasicConfigNode> leafNodes);

	/**
	 * sort based on Hierarchy Level
	 * 
	 * @param projectBasicConfig
	 * @return
	 */
	void projectBasicConfigSortedBasedOnHierarchyLevel(ProjectBasicConfig projectBasicConfig);
}
