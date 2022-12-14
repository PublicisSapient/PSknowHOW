package com.publicissapient.kpidashboard.common.service;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterCategory;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.repository.application.HierarchyLevelRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
		HierarchyLevel projectHierarchyLevel = getProjectHierarchyLevel();
		HierarchyLevel sprintHierarchyLevel = getSprintHierarchyLevel();
		hierarchyLevels.addAll(topHierarchyLevels);
		hierarchyLevels.add(projectHierarchyLevel);
		if (!isKanban) {
			hierarchyLevels.add(sprintHierarchyLevel);
		}
		List<AdditionalFilterCategory> additionalFilterCategories = filterCategoryLevelService.getAdditionalFilterCategories();
		if (CollectionUtils.isNotEmpty(additionalFilterCategories)) {

			for (AdditionalFilterCategory additionalFilterCategory : additionalFilterCategories) {
				HierarchyLevel bottomHierarchyLevel = new HierarchyLevel();
				bottomHierarchyLevel.setHierarchyLevelId(additionalFilterCategory.getFilterCategoryId());
				bottomHierarchyLevel.setHierarchyLevelName(additionalFilterCategory.getFilterCategoryName());
				if (isKanban){
					bottomHierarchyLevel.setLevel(projectHierarchyLevel.getLevel() + 1);
				} else {
					bottomHierarchyLevel.setLevel(sprintHierarchyLevel.getLevel()  + 1);
				}
				hierarchyLevels.add(bottomHierarchyLevel);
			}
		}

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

	private HierarchyLevel createProjectHierarchyLevel(List<HierarchyLevel> topHierarchies) {

		HierarchyLevel hierarchyLevel = new HierarchyLevel();
		hierarchyLevel.setHierarchyLevelId(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT);
		hierarchyLevel.setHierarchyLevelName(CommonConstant.HIERARCHY_LEVEL_NAME_PROJECT);
		if (CollectionUtils.isNotEmpty(topHierarchies)) {
			HierarchyLevel parent = topHierarchies.get(topHierarchies.size() - 1);
			hierarchyLevel.setLevel(parent.getLevel() + 1);
		} else {
			hierarchyLevel.setLevel(1);
		}
		return hierarchyLevel;

	}

	private HierarchyLevel createSprintHierarchyLevel(List<HierarchyLevel> topHierarchies) {

		HierarchyLevel hierarchyLevel = new HierarchyLevel();
		hierarchyLevel.setHierarchyLevelId(CommonConstant.HIERARCHY_LEVEL_ID_SPRINT);
		hierarchyLevel.setHierarchyLevelName(CommonConstant.HIERARCHY_LEVEL_NAME_SPRINT);
		if (CollectionUtils.isNotEmpty(topHierarchies)) {
			HierarchyLevel parent = getProjectHierarchyLevel();
			hierarchyLevel.setLevel(parent.getLevel() + 1);
		} else {
			hierarchyLevel.setLevel(2);
		}
		return hierarchyLevel;

	}
}
