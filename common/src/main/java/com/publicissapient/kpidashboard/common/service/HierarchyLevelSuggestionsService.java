package com.publicissapient.kpidashboard.common.service;

import com.publicissapient.kpidashboard.common.model.application.HierarchyLevelSuggestion;

import java.util.List;

public interface HierarchyLevelSuggestionsService {

    List<HierarchyLevelSuggestion> getSuggestions();

    HierarchyLevelSuggestion addIfNotPresent(String hierarchyLevelId, String value);
}
