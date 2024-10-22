package com.publicissapient.kpidashboard.common.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterCategory;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.repository.application.HierarchyLevelRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HierarchyLevelServiceImpl implements HierarchyLevelService {

	@Autowired
	private HierarchyLevelRepository hierarchyLevelRepository;

	@Autowired
	private AdditionalFilterCategoryService filterCategoryLevelService;

	@Override
	public List<HierarchyLevel> getTopHierarchyLevels() {
		return hierarchyLevelRepository.findAllByOrderByLevel();
	}

	@Override
	public List<HierarchyLevel> getFullHierarchyLevels(boolean isKanban) {
		List<HierarchyLevel> hierarchyLevels = new ArrayList<>();
		List<HierarchyLevel> topHierarchyLevels = getTopHierarchyLevels();
		log.info("GS List of hierarchy {}", topHierarchyLevels);
		HierarchyLevel projectHierarchyLevel = getProjectHierarchyLevel();
		log.info("GS List of projectHierarchyLevel {}", projectHierarchyLevel);
		HierarchyLevel sprintHierarchyLevel = getSprintHierarchyLevel();
		log.info("GS List of sprintHierarchyLevel {}", sprintHierarchyLevel);
		HierarchyLevel releaseHierarchyLevel = getReleaseHierarchyLevel();
		log.info("GS List of releaseHierarchyLevel {}", releaseHierarchyLevel);
		hierarchyLevels.addAll(topHierarchyLevels);
		hierarchyLevels.add(projectHierarchyLevel);
		if (!isKanban) {
			hierarchyLevels.add(sprintHierarchyLevel);
		}
		hierarchyLevels.add(releaseHierarchyLevel);
		List<AdditionalFilterCategory> additionalFilterCategories = filterCategoryLevelService
				.getAdditionalFilterCategories();
		log.info("GS additionalFilterCategories {}", additionalFilterCategories);
		if (CollectionUtils.isNotEmpty(additionalFilterCategories)) {

			for (AdditionalFilterCategory additionalFilterCategory : additionalFilterCategories) {
				HierarchyLevel bottomHierarchyLevel = new HierarchyLevel();
				bottomHierarchyLevel.setHierarchyLevelId(additionalFilterCategory.getFilterCategoryId());
				bottomHierarchyLevel.setHierarchyLevelName(additionalFilterCategory.getFilterCategoryName());
				if (isKanban) {
					bottomHierarchyLevel.setLevel(releaseHierarchyLevel.getLevel() + 1);
				} else {
					bottomHierarchyLevel.setLevel(sprintHierarchyLevel.getLevel() + 1);
				}
				hierarchyLevels.add(bottomHierarchyLevel);
			}
		}
		log.info("GS additionalFilterCategories {}", hierarchyLevels);
		return hierarchyLevels;
	}

	@Override
	public HierarchyLevel getProjectHierarchyLevel() {
		return createProjectHierarchyLevel(getTopHierarchyLevels());
	}

	@Override
	public HierarchyLevel getSprintHierarchyLevel() {
		return createSprintHierarchyLevel(getTopHierarchyLevels());
	}

	@Override
	public HierarchyLevel getReleaseHierarchyLevel() {
		return createReleaseHierarchyLevel(getTopHierarchyLevels());
	}

	private HierarchyLevel createProjectHierarchyLevel(List<HierarchyLevel> topHierarchies) {

		HierarchyLevel hierarchyLevel = new HierarchyLevel();
		hierarchyLevel.setHierarchyLevelId(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT);
		hierarchyLevel.setHierarchyLevelName(CommonConstant.HIERARCHY_LEVEL_NAME_PROJECT);
		if (CollectionUtils.isNotEmpty(topHierarchies)) {
			log.info("GS topHierarchies", topHierarchies);
			HierarchyLevel parent = topHierarchies.get(topHierarchies.size() - 1);
			hierarchyLevel.setLevel(parent.getLevel() + 1);
		} else {
			log.info("GS topHierarchies", topHierarchies);
			hierarchyLevel.setLevel(1);
		}
		log.info("GS hierarchyLevel", hierarchyLevel);
		return hierarchyLevel;

	}

	private HierarchyLevel createSprintHierarchyLevel(List<HierarchyLevel> topHierarchies) {

		HierarchyLevel hierarchyLevel = new HierarchyLevel();
		hierarchyLevel.setHierarchyLevelId(CommonConstant.HIERARCHY_LEVEL_ID_SPRINT);
		hierarchyLevel.setHierarchyLevelName(CommonConstant.HIERARCHY_LEVEL_NAME_SPRINT);
		if (CollectionUtils.isNotEmpty(topHierarchies)) {
			log.info("GS topHierarchies {}", topHierarchies);
			HierarchyLevel parent = getProjectHierarchyLevel();
			hierarchyLevel.setLevel(parent.getLevel() + 1);
		} else {
			log.info("GS topHierarchies {}", topHierarchies);
			hierarchyLevel.setLevel(2);
		}
		log.info("GS hierarchyLevel", hierarchyLevel);
		return hierarchyLevel;

	}

	private HierarchyLevel createReleaseHierarchyLevel(List<HierarchyLevel> topHierarchies) {

		HierarchyLevel hierarchyLevel = new HierarchyLevel();
		hierarchyLevel.setHierarchyLevelId(CommonConstant.HIERARCHY_LEVEL_ID_RELEASE);
		hierarchyLevel.setHierarchyLevelName(CommonConstant.HIERARCHY_LEVEL_NAME_RELEASE);
		if (CollectionUtils.isNotEmpty(topHierarchies)) {
			HierarchyLevel parent = getProjectHierarchyLevel();
			hierarchyLevel.setLevel(parent.getLevel() + 1);
		} else {
			hierarchyLevel.setLevel(2);
		}
		return hierarchyLevel;

	}
}
