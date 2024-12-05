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

package com.publicissapient.kpidashboard.apis.datamigration.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.publicissapient.kpidashboard.apis.datamigration.model.HierarchyValueDup;
import com.publicissapient.kpidashboard.apis.datamigration.model.MigrateData;
import com.publicissapient.kpidashboard.apis.datamigration.model.ProjectBasicDup;
import com.publicissapient.kpidashboard.apis.datamigration.util.InconsistentDataException;
import com.publicissapient.kpidashboard.common.model.application.OrganizationHierarchy;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.AdditionalFilterCategoryRepository;
import com.publicissapient.kpidashboard.common.repository.application.KanbanAccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.OrganizationHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DataMigrationService {

	@Autowired
	private ProjectBasicConfigRepository basicConfigRepository;
	@Autowired
	private OrganizationHierarchyRepository organizationHierarchyRepository;
	@Autowired
	public SaveService saveService;

	@Autowired
	private AccountHierarchyRepository accountHierarchyRepository;
	@Autowired
	private KanbanAccountHierarchyRepository kanbanAccountHierarchyRepository;
	@Autowired
	private SprintRepository sprintRepository;

	@Autowired
	private AdditionalFilterCategoryRepository additionalFilterCategoryRepository;

	protected Map<String, OrganizationHierarchy> nodeWiseOrganizationHierarchy;
	protected List<ProjectBasicConfig> projectBasicConfigList;

	public List<MigrateData> dataMigration() {
		List<MigrateData> failureData = new ArrayList<>();
		log.info("Fetching basic Config");
		projectBasicConfigList = basicConfigRepository.findAll();
		List<ProjectBasicDup> projectBasicDupList = duplicateProject(projectBasicConfigList);
		updateCustomizedName(projectBasicDupList);

		nodeWiseOrganizationHierarchy = new HashMap<>();

		for (ProjectBasicDup project : projectBasicDupList) {
			List<HierarchyValueDup> hierarchyList = project.getHierarchy();
			if (hierarchyList == null || hierarchyList.isEmpty())
				continue;
			// copy mainOrganzation Hierarchy
			Map<String, OrganizationHierarchy> projectHierarchyMap = new HashMap<>(nodeWiseOrganizationHierarchy);

			hierarchyList.sort(
					(h1, h2) -> Integer.compare(h2.getHierarchyLevel().getLevel(), h1.getHierarchyLevel().getLevel()));

			try {
				// Creating project node
				int projectAboveLevel = hierarchyList.get(0).getHierarchyLevel().getLevel();
				String projectParentId = checkParent(projectAboveLevel, hierarchyList, projectHierarchyMap);
				OrganizationHierarchy projectHierarchy = new OrganizationHierarchy();
				projectHierarchy.setNodeName(project.getProjectName());
				projectHierarchy.setNodeDisplayName(project.getProjectName());
				projectHierarchy.setHierarchyLevelId("project");
				projectHierarchy.setCreatedDate(LocalDateTime.now());
				projectHierarchy.setParentId(projectParentId);
				projectHierarchy.setNodeId(UUID.randomUUID().toString());
				projectHierarchyMap.put(projectAboveLevel - 1 + ":" + project.getProjectName(), projectHierarchy);
				nodeWiseOrganizationHierarchy.putAll(projectHierarchyMap);

			} catch (InconsistentDataException e) {
				log.error("Error in project: " + project.getProjectName() + " -> " + e.getMessage());
				String[] message = e.getMessage().split(":");
				failureData.add(new MigrateData(project.getProjectName(), message[0], message[1]));
			}
		}

		return failureData;
	}

	private void updateCustomizedName(List<ProjectBasicDup> projectBasicDupList) {
		log.info("Start of Coping Name of Parent to Child");
		for (int i = 0; i < projectBasicDupList.size(); i++) {
			ProjectBasicDup projectBasicDup = projectBasicDupList.get(i);
			projectBasicDup.getHierarchy().sort(
					(h1, h2) -> Integer.compare(h2.getHierarchyLevel().getLevel(), h1.getHierarchyLevel().getLevel()));

			// recuursion
			updateNameWithParent(projectBasicDup.getHierarchy().get(0).getHierarchyLevel().getLevel(),
					projectBasicDup.getHierarchy());

		}
		log.info("End of Coping Name of Parent to Child");

	}

	private String updateNameWithParent(int level, List<HierarchyValueDup> hierarchyList) {
		// Find current level hierarchy value
		HierarchyValueDup currentHierarchy = hierarchyList.stream()
				.filter(hv -> hv.getHierarchyLevel().getLevel() == level).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("No hierarchy found for level: " + level));
		if (level == 1) {
			return currentHierarchy.getValue();
		} else {
			currentHierarchy.setCustomizedValue(
					currentHierarchy.getValue() + "-" + updateNameWithParent(level - 1, hierarchyList));
		}

		return currentHierarchy.getCustomizedValue();
	}

	private List<ProjectBasicDup> duplicateProject(List<ProjectBasicConfig> projectBasicConfigList) {
		ModelMapper mapper = new ModelMapper();
		List<ProjectBasicDup> projectBasicDupList = new ArrayList<>();
		for (ProjectBasicConfig projectBasicConfig : projectBasicConfigList) {
			ProjectBasicDup projectBasicDup = mapper.map(projectBasicConfig, ProjectBasicDup.class);
			List<HierarchyValueDup> hierarchyValueDupList = projectBasicDup.getHierarchy();
			if (CollectionUtils.isNotEmpty(hierarchyValueDupList)) {
				for (HierarchyValueDup values : hierarchyValueDupList) {
					values.setCustomizedValue(values.getCustomizedValue());
				}
				projectBasicDupList.add(projectBasicDup);
			}
		}

		return projectBasicDupList;

	}

	private static String checkParent(int level, List<HierarchyValueDup> hierarchyList,
			Map<String, OrganizationHierarchy> nodeWiseOrganizationHierachy) throws InconsistentDataException {

		if (level < 1) {
			throw new IllegalArgumentException("Level cannot be less than 1");
		}

		// Find current level hierarchy value
		HierarchyValueDup currentHierarchy = hierarchyList.stream()
				.filter(hv -> hv.getHierarchyLevel().getLevel() == level).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("No hierarchy found for level: " + level));

		String key = level + ":"
				+ (StringUtils.isEmpty(currentHierarchy.getCustomizedValue()) ? currentHierarchy.getValue()
						: currentHierarchy.getCustomizedValue());
		OrganizationHierarchy organizationHierarchy = nodeWiseOrganizationHierachy.get(key);

		if (organizationHierarchy == null) {
			// Create a new node if not found
			organizationHierarchy = new OrganizationHierarchy();
			organizationHierarchy.setNodeName(currentHierarchy.getValue());
			organizationHierarchy.setNodeDisplayName(currentHierarchy.getValue());
			organizationHierarchy.setHierarchyLevelId(currentHierarchy.getHierarchyLevel().getHierarchyLevelId());
			organizationHierarchy.setCreatedDate(LocalDateTime.now());

			// Recursively set parent
			if (level > 1) {
				organizationHierarchy.setParentId(checkParent(level - 1, hierarchyList, nodeWiseOrganizationHierachy));
			}

			organizationHierarchy.setNodeId(UUID.randomUUID().toString());
			nodeWiseOrganizationHierachy.put(key, organizationHierarchy);
		} else {
			// Validate parent ID consistency
			String expectedParentId = level > 1 ? checkParent(level - 1, hierarchyList, nodeWiseOrganizationHierachy)
					: null;
			if (expectedParentId != null && !expectedParentId.equals(organizationHierarchy.getParentId())) {
				throw new InconsistentDataException(level + ":" + currentHierarchy.getValue());
			}
		}

		return organizationHierarchy.getNodeId();
	}

	/*
	 * create organization hierarchy and update project basic hierarchy
	 */
	public void populateOrganizationHierarchy() {
		if (MapUtils.isEmpty(nodeWiseOrganizationHierarchy)) {
			log.info("Calling the validation process");
			List<MigrateData> failureData = dataMigration();
			// Proceed only if there are no failures and the map is populated
			if (CollectionUtils.isEmpty(failureData) && MapUtils.isNotEmpty(nodeWiseOrganizationHierarchy)) {
				saveOrganizationHierarchyAndUpdateProjectBasic();
			}

		} else {
			saveOrganizationHierarchyAndUpdateProjectBasic();
		}
	}

	@Transactional
	public void saveOrganizationHierarchyAndUpdateProjectBasic() {
		log.info("Start - Updating Node Names to Original Values");

		try {
			// Update node names to match their display names
			nodeWiseOrganizationHierarchy.values()
					.forEach(orgHierarchy -> orgHierarchy.setNodeName(orgHierarchy.getNodeDisplayName()));

			// Map project names to their unique node IDs
			Map<String, String> projectNameWiseUniqueId = nodeWiseOrganizationHierarchy.values().stream()
					.filter(orgHierarchy -> "project".equalsIgnoreCase(orgHierarchy.getHierarchyLevelId()))
					.collect(Collectors.toMap(OrganizationHierarchy::getNodeDisplayName,
							OrganizationHierarchy::getNodeId));

			// Update project basic config list with unique projectNodeId
			projectBasicConfigList.forEach(projectBasicConfig -> {
				projectBasicConfig.setProjectNodeId(projectNameWiseUniqueId.get(projectBasicConfig.getProjectName()));
				projectBasicConfig.setProjectDisplayName(projectBasicConfig.getProjectName());

			});

			// Save all data to the repository
			saveService.saveToOrganizationHierarchy(new ArrayList<>(nodeWiseOrganizationHierarchy.values()));
			saveService.saveToBasicConfig(projectBasicConfigList);
			log.info("Data successfully saved to the database.");

		} catch (DuplicateKeyException ex) {
			log.error("Duplicate project name found in organization hierarchy: {}", ex.getMessage());
			throw new DuplicateKeyException(
					"Duplicate project name found in organization hierarchy: " + ex.getMessage(), ex);

		} catch (DataIntegrityViolationException ex) {
			log.error("Data integrity violation occurred: {}", ex.getMessage());
			throw new DataIntegrityViolationException(
					"Data integrity violation while saving organization hierarchy or project basic config: "
							+ ex.getMessage(),
					ex);

		} catch (Exception ex) {
			log.error("An unexpected error occurred: {}", ex.getMessage());
			throw new IllegalStateException("An unexpected error occurred while saving data: " + ex.getMessage(), ex);
		}
	}

}
