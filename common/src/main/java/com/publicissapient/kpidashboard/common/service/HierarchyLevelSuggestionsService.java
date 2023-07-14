package com.publicissapient.kpidashboard.common.service;

import java.util.List;

import com.publicissapient.kpidashboard.common.model.application.HierarchyLevelSuggestion;

public interface HierarchyLevelSuggestionsService {

	List<HierarchyLevelSuggestion> getSuggestions();

	HierarchyLevelSuggestion addIfNotPresent(String hierarchyLevelId, String value);
}
