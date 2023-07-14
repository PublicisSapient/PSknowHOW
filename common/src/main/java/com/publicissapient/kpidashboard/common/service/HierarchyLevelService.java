package com.publicissapient.kpidashboard.common.service;

import java.util.List;

import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;

public interface HierarchyLevelService {

	List<HierarchyLevel> getTopHierarchyLevels();

	List<HierarchyLevel> getFullHierarchyLevels(boolean isKanban);

	HierarchyLevel getProjectHierarchyLevel();

	HierarchyLevel getSprintHierarchyLevel();

	HierarchyLevel getReleaseHierarchyLevel();
}
