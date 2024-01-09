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

package com.publicissapient.kpidashboard.apis.filter.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.errors.EntityNotFoundException;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyDataKanban;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterCategory;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.KanbanAccountHierarchy;
import com.publicissapient.kpidashboard.common.model.application.dto.HierarchyValueDTO;
import com.publicissapient.kpidashboard.common.model.application.dto.ProjectBasicConfigDTO;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.KanbanAccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author tauakram
 *
 */
@Service
@Slf4j
public class FilterHelperService {

	private static final int SINGLECHILD = 1;

	@Autowired
	private CacheService cacheService;

	@Autowired
	private AccountHierarchyRepository accountHierarchyRepository;

	@Autowired
	private KanbanAccountHierarchyRepository kanbanAccountHierarchyRepo;

	@Autowired
	private HierarchyLevelService hierarchyLevelService;

	public List<AccountHierarchyData> getFilteredBuilds(KpiRequest kpiRequest, String groupName) {

		List<AccountHierarchyData> accountDataListAll = (List<AccountHierarchyData>) cacheService
				.cacheAccountHierarchyData();

		List<AccountHierarchyData> dataList = getAccountHierarchyDataForRequest(
				new HashSet<>(kpiRequest.getSprintIncluded()), accountDataListAll);
		List<AccountHierarchyData> filteredDataSetNew = null;
		filteredDataSetNew = filter(dataList, groupName, kpiRequest);
		if (CollectionUtils.isNotEmpty(kpiRequest.getSelectedMap()
				.getOrDefault(CommonConstant.HIERARCHY_LEVEL_ID_SPRINT, new ArrayList<>()))) {
			filteredDataSetNew = filter(filteredDataSetNew, CommonConstant.HIERARCHY_LEVEL_ID_SPRINT, kpiRequest);
		}

		if (CollectionUtils.isNotEmpty(kpiRequest.getSelectedMap()
				.getOrDefault(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT, new ArrayList<>()))) {
			filteredDataSetNew = filter(filteredDataSetNew, CommonConstant.HIERARCHY_LEVEL_ID_PROJECT, kpiRequest);
		}
		return filteredDataSetNew;
	}

	private List<AccountHierarchyData> filter(List<AccountHierarchyData> dataList, String groupName,
			KpiRequest kpiRequest) {
		Set<String> str = new HashSet<>(kpiRequest.getSelectedMap().getOrDefault(groupName, new ArrayList<>()));
		List<AccountHierarchyData> filteredDataSetNew = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(str)) {
			dataList.forEach(data -> {
				if (data.getNode().stream()
						.anyMatch(d -> d.getGroupName().equalsIgnoreCase(groupName) && str.contains(d.getId()))) {
					filteredDataSetNew.add(data);
				}
			});
		}
		return filteredDataSetNew;
	}

	/**
	 * filter data based on sprint state
	 * 
	 * @param sprintStateRequestList
	 *            sprintStateList
	 * @param hierarchyDataAll
	 *            hierarchyDataAll
	 * @return List<AccountHierarchyData>
	 */
	public List<AccountHierarchyData> getAccountHierarchyDataForRequest(Set<String> sprintStateList,
			List<AccountHierarchyData> hierarchyDataAll) {
		Set<String> nsprintStateList = sprintStateList.stream().map(String::toLowerCase).collect(Collectors.toSet());

		List<AccountHierarchyData> hierarchyData = new ArrayList<>();

		hierarchyDataAll.forEach(data -> {
			// add all which donot have sprint level
			if (data.getLabelName().equalsIgnoreCase(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT)
					|| data.getNode().stream()
							.anyMatch(node -> node.getGroupName().equals(CommonConstant.HIERARCHY_LEVEL_ID_SPRINT)
									&& node.getAccountHierarchy().getSprintState() != null
									&& nsprintStateList
											.contains(node.getAccountHierarchy().getSprintState().toLowerCase()))
					|| data.getNode().stream()
							.anyMatch(node -> node.getGroupName().equals(CommonConstant.HIERARCHY_LEVEL_ID_RELEASE))) {
				hierarchyData.add(data);
			}
		});
		return hierarchyData;
	}

	@SuppressWarnings("unchecked")
	public List<AccountHierarchyDataKanban> getFilteredBuildsKanban(KpiRequest kpiRequest, String groupName)// NOPMD
			throws EntityNotFoundException {// NOPMD
		// Do not remove NOPMD comment. This is required to ignore nthcomplexity
		// and
		// cyclomatic Complexity.

		List<AccountHierarchyDataKanban> accountDataList = (List<AccountHierarchyDataKanban>) cacheService
				.cacheAccountHierarchyKanbanData();
		List<AccountHierarchyDataKanban> filteredDataSetNew = null;

		filteredDataSetNew = filterKanban(accountDataList, groupName, kpiRequest);

		if (CollectionUtils.isNotEmpty(kpiRequest.getSelectedMap()
				.getOrDefault(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT, new ArrayList<>()))) {
			filteredDataSetNew = filterKanban(filteredDataSetNew, CommonConstant.HIERARCHY_LEVEL_ID_PROJECT,
					kpiRequest);
		}

		if (filteredDataSetNew.isEmpty()) {
			log.error("[FILTERED-DATA][{}]. No filtered data found  in the cache for the filter level{} and id{}",
					kpiRequest.getRequestTrackerId(), kpiRequest.getLevel(), kpiRequest.getIds());
			throw new EntityNotFoundException(KpiRequest.class, "kpiId", Arrays.deepToString(kpiRequest.getIds()));
		}

		return filteredDataSetNew;
	}

	private List<AccountHierarchyDataKanban> filterKanban(List<AccountHierarchyDataKanban> dataList, String groupName,
			KpiRequest kpiRequest) {
		Set<String> str = new HashSet<>(kpiRequest.getSelectedMap().getOrDefault(groupName, new ArrayList<>()));
		List<AccountHierarchyDataKanban> filteredDataSetNew = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(str)) {
			dataList.forEach(data -> {
				if (data.getNode().stream()
						.anyMatch(d -> d.getGroupName().equalsIgnoreCase(groupName) && str.contains(d.getId()))) {
					filteredDataSetNew.add(data);
				}
			});
		}
		return filteredDataSetNew;
	}

	/**
	 * @param projectConfig
	 *            for filter creation
	 */
	public void filterCreation(final ProjectBasicConfigDTO projectConfig) {
		try {
			/*
			 * created a set in correct hierarchical order for easy computation
			 */
			List<HierarchyLevel> filterCategoryLevels = hierarchyLevelService
					.getFullHierarchyLevels(projectConfig.getIsKanban());
			int projectLevel = filterCategoryLevels.stream()
					.filter(x -> x.getHierarchyLevelId().equalsIgnoreCase(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT))
					.collect(Collectors.toList()).get(0).getLevel();
			List<AccountHierarchy> accountHierarchyList = Lists.newArrayList();
			List<String> pathList = Lists.newLinkedList();

			Map<Pair<String, String>, AccountHierarchy> existingHierarchyMap = getExistingAccountHierarchy(
					projectConfig.getIsKanban());
			for (HierarchyLevel filter : filterCategoryLevels) {
				if (filter.getLevel() <= projectLevel) {
					AccountHierarchy accountHierarchy = createFilterObject(projectConfig, filter);

					accountHierarchy.setPath(String.join(CommonConstant.ACC_HIERARCHY_PATH_SPLITTER, pathList));
					// filter parent_id
					if (StringUtils.isNotBlank(accountHierarchy.getPath())) {
						accountHierarchy.setParentId(
								accountHierarchy.getPath().split(CommonConstant.ACC_HIERARCHY_PATH_SPLITTER)[0]);
					}

					pathList.add(0, accountHierarchy.getNodeId());
					AccountHierarchy exHiery = existingHierarchyMap
							.get(Pair.of(accountHierarchy.getNodeId(), accountHierarchy.getPath()));

					if (null == exHiery) {
						accountHierarchy.setCreatedDate(LocalDateTime.now());
						accountHierarchyList.add(accountHierarchy);
					}

				}
			}
			if (projectConfig.getIsKanban()) {
				final ModelMapper modelMapper = new ModelMapper();
				kanbanAccountHierarchyRepo.saveAll(accountHierarchyList.stream()
						.map(h1 -> modelMapper.map(h1, KanbanAccountHierarchy.class)).collect(Collectors.toList()));
			} else {
				accountHierarchyRepository.saveAll(accountHierarchyList);
			}
		} catch (Exception e) {
			log.error("FilterHelperService: error while creating filter.", e);
		}

	}

	private AccountHierarchy createFilterObject(ProjectBasicConfigDTO projectConfig, HierarchyLevel filter) {
		AccountHierarchy accountHierarchy = new AccountHierarchy();
		accountHierarchy.setBasicProjectConfigId(projectConfig.getId());
		accountHierarchy.setIsDeleted(Constant.FALSE);

		// filter based on hierarchy and prepare account hierarchy
		List<HierarchyValueDTO> hierarchy = projectConfig.getHierarchy();
		if (CollectionUtils.isNotEmpty(hierarchy)) {
			hierarchy.stream().forEach(hierarchyValueDTO -> {
				if (hierarchyValueDTO.getHierarchyLevel().getLevel() == filter.getLevel()) {
					String nodeId = new StringBuffer(hierarchyValueDTO.getValue()).append(Constant.UNDERSCORE)
							.append(filter.getHierarchyLevelId()).toString();
					accountHierarchy.setNodeName(hierarchyValueDTO.getValue());
					accountHierarchy.setNodeId(nodeId);
					accountHierarchy.setFilterCategoryId(filter.getId());
					accountHierarchy.setLabelName(filter.getHierarchyLevelId());
				}
			});
		}
		if (filter.getHierarchyLevelId().equalsIgnoreCase(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT)) {
			String nodeId = new StringBuffer(projectConfig.getProjectName()).append(Constant.UNDERSCORE)
					.append(projectConfig.getId().toString()).toString();
			accountHierarchy.setNodeName(projectConfig.getProjectName());
			accountHierarchy.setFilterCategoryId(filter.getId());
			accountHierarchy.setLabelName(filter.getHierarchyLevelId());
			accountHierarchy.setNodeName(projectConfig.getProjectName());
			accountHierarchy.setNodeId(nodeId);
		}
		return accountHierarchy;
	}

	private Map<Pair<String, String>, AccountHierarchy> getExistingAccountHierarchy(boolean isKanban) {
		Map<Pair<String, String>, AccountHierarchy> existingHierarchy = null;
		if (isKanban) {
			List<KanbanAccountHierarchy> accountHierarchyList = kanbanAccountHierarchyRepo.findAll();
			ModelMapper modelMapper = new ModelMapper();
			existingHierarchy = accountHierarchyList.stream().map(h1 -> modelMapper.map(h1, AccountHierarchy.class))
					.collect(Collectors.toMap(p -> Pair.of(p.getNodeId(), p.getPath()), p -> p));
		} else {
			List<AccountHierarchy> accountHierarchyList = accountHierarchyRepository.findAll();
			existingHierarchy = accountHierarchyList.stream()
					.collect(Collectors.toMap(p -> Pair.of(p.getNodeId(), p.getPath()), p -> p));
		}
		return existingHierarchy;
	}

	/**
	 * clean filter data
	 * 
	 * @param basicProjectConfigId
	 *            id
	 * @param isKanban
	 *            kanban or scrum
	 */
	public void cleanFilterData(ProjectBasicConfigDTO basicConfig) {
		ObjectId basicProjectConfigId = basicConfig.getId();
		log.info("cleaning filter data for {}", basicProjectConfigId.toHexString());
		List<HierarchyValueDTO> hierarchyLevelValues = basicConfig.getHierarchy();
		List<String> reverseOrderHierarchy = CollectionUtils.emptyIfNull(hierarchyLevelValues).stream()
				.sorted(Comparator
						.comparing((HierarchyValueDTO hierarchyValue) -> hierarchyValue.getHierarchyLevel().getLevel())
						.reversed())
				.map(hierarchyValue -> hierarchyValue.getHierarchyLevel().getHierarchyLevelId())
				.collect(Collectors.toList());

		if (basicConfig.getIsKanban()) {
			cleanKanbanFilterData(reverseOrderHierarchy, basicProjectConfigId);
		} else {
			cleanScrumFilterData(reverseOrderHierarchy, basicProjectConfigId);
		}
	}

	/**
	 * @param reversehierarchy
	 * @param projId
	 */
	private void cleanScrumFilterData(List<String> reversehierarchy, ObjectId projId) {
		List<AccountHierarchy> projectDataList = accountHierarchyRepository
				.findByLabelNameAndBasicProjectConfigId(Constant.PROJECT, projId);
		if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(projectDataList)) {
			String childNodesPath = StringUtils.EMPTY;
			AccountHierarchy projectData = projectDataList.get(0);
			String path = projectData.getPath();
			for (String hier : reversehierarchy) {
				List<AccountHierarchy> list = accountHierarchyRepository.findByLabelNameAndPath(hier, path);
				if (list.size() > SINGLECHILD) {
					break;
				}
				childNodesPath = path;
				path = path.substring(path.indexOf(CommonConstant.ACC_HIERARCHY_PATH_SPLITTER) + 1, path.length())
						.trim();
			}

			if (childNodesPath.isEmpty()) {
				childNodesPath = projectData.getNodeId() + CommonConstant.ACC_HIERARCHY_PATH_SPLITTER + path;
				accountHierarchyRepository.deleteByNodeIdAndPath(projectData.getNodeId(), path);
			} else if (childNodesPath.indexOf(CommonConstant.ACC_HIERARCHY_PATH_SPLITTER) > -1) {
				accountHierarchyRepository.deleteByNodeIdAndPath(
						childNodesPath.substring(0, childNodesPath.indexOf(CommonConstant.ACC_HIERARCHY_PATH_SPLITTER)),
						childNodesPath.substring(childNodesPath.indexOf(CommonConstant.ACC_HIERARCHY_PATH_SPLITTER) + 1,
								childNodesPath.length()));
			}
			accountHierarchyRepository.deleteByPathEndsWith(childNodesPath);
		}

	}

	/**
	 * @param reversehierarchy
	 * @param projId
	 */
	private void cleanKanbanFilterData(List<String> reversehierarchy, ObjectId projId) {

		List<KanbanAccountHierarchy> projectDataList = kanbanAccountHierarchyRepo
				.findByLabelNameAndBasicProjectConfigId(Constant.PROJECT, projId);

		if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(projectDataList)) {
			KanbanAccountHierarchy projectData = projectDataList.get(0);
			String childNodesPath = StringUtils.EMPTY;
			String path = projectData.getPath();
			for (String hier : reversehierarchy) {
				List<KanbanAccountHierarchy> list = kanbanAccountHierarchyRepo.findByLabelNameAndPath(hier, path);
				if (list.size() > SINGLECHILD) {
					break;
				}
				childNodesPath = path;
				path = path.substring(path.indexOf(CommonConstant.ACC_HIERARCHY_PATH_SPLITTER) + 1, path.length())
						.trim();
			}

			if (childNodesPath.isEmpty()) {
				childNodesPath = projectData.getNodeId() + CommonConstant.ACC_HIERARCHY_PATH_SPLITTER + path;
				kanbanAccountHierarchyRepo.deleteByNodeIdAndPath(projectData.getNodeId(), path);
			} else if (childNodesPath.indexOf(CommonConstant.ACC_HIERARCHY_PATH_SPLITTER) > -1) {
				kanbanAccountHierarchyRepo.deleteByNodeIdAndPath(
						childNodesPath.substring(0, childNodesPath.indexOf(CommonConstant.ACC_HIERARCHY_PATH_SPLITTER)),
						childNodesPath.substring(childNodesPath.indexOf(CommonConstant.ACC_HIERARCHY_PATH_SPLITTER) + 1,
								childNodesPath.length()));
			}
			kanbanAccountHierarchyRepo.deleteByPathEndsWith(childNodesPath);
		}

	}

	public void deleteAccountHierarchiesOfProject(ObjectId projectBasicConfigId, boolean isKanban) {
		if (isKanban) {
			deleteAccountHierarchiesOfProjectKanban(projectBasicConfigId);
		} else {
			deleteAccountHierarchiesOfProjectScrum(projectBasicConfigId);
		}
	}

	private void deleteAccountHierarchiesOfProjectScrum(ObjectId projectBasicConfigId) {
		AccountHierarchy ahProjetLabel = getAccountHierarchyProjectLevel(projectBasicConfigId);
		final List<ObjectId> idsForDeletion = new ArrayList<>();

		if (ahProjetLabel != null) {
			// find all the items below project including project
			idsForDeletion.add(ahProjetLabel.getId());
			List<AccountHierarchy> ahBellowProjectLabel = findAccountHierarchiesBellowProjectLevelForDeletion(
					ahProjetLabel);
			ahBellowProjectLabel.forEach(ah -> idsForDeletion.add(ah.getId()));

			// find all the items above project label
			List<AccountHierarchy> ahAboveProjectLevel = findAccountHierarchiesAboveProvidedForDeletion(ahProjetLabel,
					null);
			ahAboveProjectLevel.forEach(ah -> idsForDeletion.add(ah.getId()));

		}

		if (CollectionUtils.isNotEmpty(idsForDeletion)) {

			accountHierarchyRepository.deleteByIdIn(idsForDeletion);
		}

	}

	private List<AccountHierarchy> findAccountHierarchiesBellowProjectLevelForDeletion(
			AccountHierarchy ahProjectLevel) {

		List<AccountHierarchy> resultAccountHierarchies = new ArrayList<>();
		List<HierarchyLevel> hierarchyList = hierarchyLevelService.getFullHierarchyLevels(false);
		HierarchyLevel projectHierarchyLevel = getHierarchyLevelMap(false)
				.getOrDefault(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT, null);
		if (CollectionUtils.isNotEmpty(hierarchyList) && null != projectHierarchyLevel) {
			List<HierarchyLevel> filterCategoryBellowProjectLevel = hierarchyList.stream()
					.filter(filterCategory -> filterCategory.getLevel() > projectHierarchyLevel.getLevel())
					.collect(Collectors.toList());

			filterCategoryBellowProjectLevel.forEach(filterCategory -> {
				List<AccountHierarchy> ahBellowProjectLevel = accountHierarchyRepository
						.findByLabelNameAndBasicProjectConfigId(filterCategory.getHierarchyLevelId(),
								ahProjectLevel.getBasicProjectConfigId());
				resultAccountHierarchies.addAll(ahBellowProjectLevel);
			});

		}

		return resultAccountHierarchies;
	}

	private List<AccountHierarchy> findAccountHierarchiesAboveProvidedForDeletion(AccountHierarchy ah,
			List<AccountHierarchy> toBeDeleted) {

		if (toBeDeleted == null) {
			toBeDeleted = new ArrayList<>();
		}
		// splits the path to find out top hierarchy
		String path = ah.getPath();
		String parentNodeId = ah.getParentId();
		String parentNodePath = getParentNodePath(path);
		List<AccountHierarchy> parentNodes = accountHierarchyRepository.findByNodeIdAndPath(parentNodeId,
				parentNodePath);
		AccountHierarchy parentNode = CollectionUtils.isNotEmpty(parentNodes) ? parentNodes.get(0) : null;

		if (parentNode != null && isOnlyNode(ah)) {
			toBeDeleted.add(parentNode);
			findAccountHierarchiesAboveProvidedForDeletion(parentNode, toBeDeleted);
		}
		return toBeDeleted;

	}

	private String getParentNodePath(String nodePath) {
		if (nodePath == null) {
			return null;
		}
		int splitterIndex = nodePath.indexOf(CommonConstant.ACC_HIERARCHY_PATH_SPLITTER);

		return splitterIndex == -1 ? ""
				: nodePath.substring(splitterIndex + CommonConstant.ACC_HIERARCHY_PATH_SPLITTER.length()).trim();

	}

	private boolean isOnlyNode(AccountHierarchy node) {
		List<AccountHierarchy> accountHierarchies = accountHierarchyRepository
				.findByLabelNameAndPath(node.getLabelName(), node.getPath());
		return CollectionUtils.isNotEmpty(accountHierarchies) && accountHierarchies.size() == 1;
	}

	private AccountHierarchy getAccountHierarchyProjectLevel(ObjectId projectBasicConfigId) {
		List<AccountHierarchy> accountHierarchiesProjectLabel = accountHierarchyRepository
				.findByLabelNameAndBasicProjectConfigId(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT,
						projectBasicConfigId);

		return CollectionUtils.isNotEmpty(accountHierarchiesProjectLabel) ? accountHierarchiesProjectLabel.get(0)
				: null;
	}

	private void deleteAccountHierarchiesOfProjectKanban(ObjectId projectBasicConfigId) {
		KanbanAccountHierarchy ahProjetLabel = getAccountHierarchyProjectLevelKanban(projectBasicConfigId);
		final List<ObjectId> idsForDeletion = new ArrayList<>();

		if (ahProjetLabel != null) {
			// find all the items below project including project
			idsForDeletion.add(ahProjetLabel.getId());
			List<KanbanAccountHierarchy> ahBellowProjectLabel = findAccountHierarchiesBellowProjectLevelForDeletionKanban(
					ahProjetLabel);
			ahBellowProjectLabel.forEach(ah -> idsForDeletion.add(ah.getId()));

			// find all the items above project label
			List<KanbanAccountHierarchy> ahAboveProjectLevel = findAccountHierarchiesAboveProvidedForDeletionKanban(
					ahProjetLabel, null);
			ahAboveProjectLevel.forEach(ah -> idsForDeletion.add(ah.getId()));

		}

		if (CollectionUtils.isNotEmpty(idsForDeletion)) {

			kanbanAccountHierarchyRepo.deleteByIdIn(idsForDeletion);
		}
	}

	private boolean isOnlyNodeKanban(KanbanAccountHierarchy parentNode) {
		List<KanbanAccountHierarchy> accountHierarchies = kanbanAccountHierarchyRepo
				.findByLabelNameAndPath(parentNode.getLabelName(), parentNode.getPath());
		return CollectionUtils.isNotEmpty(accountHierarchies) && accountHierarchies.size() == 1;
	}

	private KanbanAccountHierarchy getAccountHierarchyProjectLevelKanban(ObjectId projectBasicConfigId) {
		List<KanbanAccountHierarchy> accountHierarchiesProjectLabel = kanbanAccountHierarchyRepo
				.findByLabelNameAndBasicProjectConfigId(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT,
						projectBasicConfigId);

		return CollectionUtils.isNotEmpty(accountHierarchiesProjectLabel) ? accountHierarchiesProjectLabel.get(0)
				: null;
	}

	private List<KanbanAccountHierarchy> findAccountHierarchiesBellowProjectLevelForDeletionKanban(
			KanbanAccountHierarchy ahProjectLevel) {

		List<KanbanAccountHierarchy> resultAccountHierarchies = new ArrayList<>();

		List<HierarchyLevel> hierarchyList = hierarchyLevelService.getFullHierarchyLevels(true);
		HierarchyLevel projectHierarchyLevel = getHierarchyLevelMap(true)
				.getOrDefault(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT, null);
		if (CollectionUtils.isNotEmpty(hierarchyList) && null != projectHierarchyLevel) {
			List<HierarchyLevel> filterCategoryBellowProjectLevel = hierarchyList.stream()
					.filter(filterCategory -> filterCategory.getLevel() > projectHierarchyLevel.getLevel())
					.collect(Collectors.toList());

			filterCategoryBellowProjectLevel.forEach(filterCategory -> {
				List<KanbanAccountHierarchy> ahBellowProjectLevel = kanbanAccountHierarchyRepo
						.findByLabelNameAndBasicProjectConfigId(filterCategory.getHierarchyLevelId(),
								ahProjectLevel.getBasicProjectConfigId());
				resultAccountHierarchies.addAll(ahBellowProjectLevel);
			});

		}

		return resultAccountHierarchies;
	}

	private List<KanbanAccountHierarchy> findAccountHierarchiesAboveProvidedForDeletionKanban(KanbanAccountHierarchy ah,
			List<KanbanAccountHierarchy> toBeDeleted) {

		if (toBeDeleted == null) {
			toBeDeleted = new ArrayList<>();
		}
		// splits the path to find out top hierarchy
		String path = ah.getPath();
		String parentNodeId = ah.getParentId();
		String parentNodePath = getParentNodePath(path);
		List<KanbanAccountHierarchy> parentNodes = kanbanAccountHierarchyRepo.findByNodeIdAndPath(parentNodeId,
				parentNodePath);
		KanbanAccountHierarchy parentNode = CollectionUtils.isNotEmpty(parentNodes) ? parentNodes.get(0) : null;

		if (parentNode != null && isOnlyNodeKanban(ah)) {
			toBeDeleted.add(parentNode);
			findAccountHierarchiesAboveProvidedForDeletionKanban(parentNode, toBeDeleted);
		}
		return toBeDeleted;

	}

	public Map<String, HierarchyLevel> getHierarchyLevelMap(boolean isKanban) {
		if (isKanban) {
			return cacheService.getFullKanbanHierarchyLevelMap();
		} else {
			return cacheService.getFullHierarchyLevelMap();
		}
	}

	public String getHierarachyLevelId(int level, String label, boolean isKanban) {
		String hierarchyId = CommonConstant.HIERARCHY_LEVEL_ID_PROJECT;
		Map<String, HierarchyLevel> map = getHierarchyLevelMap(isKanban);
		if (MapUtils.isNotEmpty(map)) {
			if (StringUtils.isNotEmpty(label)) {
				hierarchyId = map.values().stream().filter(hlevel -> (hlevel.getLevel() == level)
						&& (StringUtils.isNotEmpty(label) && hlevel.getHierarchyLevelId().equalsIgnoreCase(label)))
						.map(HierarchyLevel::getHierarchyLevelId).findFirst()
						.orElse(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT);
			} else {
				hierarchyId = map.values().stream().filter(hlevel -> (hlevel.getLevel() == level))
						.map(HierarchyLevel::getHierarchyLevelId).findFirst()
						.orElse(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT);
			}
		}
		return hierarchyId;
	}

	public String getFirstHierarachyLevel() {
		return getHierarachyLevelId(1, "", true);
	}

	public Map<String, Integer> getHierarchyIdLevelMap(boolean isKanban) {
		Map<String, Integer> hierarchyLevelMap = new HashMap<>();
		getHierarchyLevelMap(isKanban).forEach((key, value) -> hierarchyLevelMap.put(key, value.getLevel()));
		return hierarchyLevelMap;
	}

	public Map<String, AdditionalFilterCategory> getAdditionalFilterHierarchyLevel() {
		return cacheService.getAdditionalFilterHierarchyLevel();
	}

}