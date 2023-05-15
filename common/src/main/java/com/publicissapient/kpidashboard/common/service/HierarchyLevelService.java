package com.publicissapient.kpidashboard.common.service;

import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;

import java.util.List;

public interface HierarchyLevelService {

    List<HierarchyLevel> getTopHierarchyLevels();

    List<HierarchyLevel> getFullHierarchyLevels(boolean isKanban);

    HierarchyLevel getProjectHierarchyLevel();

    HierarchyLevel getSprintHierarchyLevel();

    HierarchyLevel getReleaseHierarchyLevel();
}
