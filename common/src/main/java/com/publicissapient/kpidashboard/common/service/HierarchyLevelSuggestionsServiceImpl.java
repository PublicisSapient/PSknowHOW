package com.publicissapient.kpidashboard.common.service;

import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.model.application.HierarchyLevelSuggestion;
import com.publicissapient.kpidashboard.common.repository.application.HierarchyLevelSuggestionRepository;

@Service
public class HierarchyLevelSuggestionsServiceImpl implements HierarchyLevelSuggestionsService {

	@Autowired
	private HierarchyLevelSuggestionRepository hierarchyLevelSuggestionRepository;

	@Override
	public List<HierarchyLevelSuggestion> getSuggestions() {
		return hierarchyLevelSuggestionRepository.findAll();
	}

	/*
	 * @Autowired private ConfigHelperService configHelperService;
	 * 
	 * @Autowired private CacheService cacheService;
	 * 
	 * @Override public List<HierarchyLevelSuggestion> getSuggestions() { return
	 * configHelperService.loadHierarchyLevelSuggestion(); }
	 */

	@Override
	public HierarchyLevelSuggestion addIfNotPresent(String hierarchyLevelId, String hierarchyValue) {

		HierarchyLevelSuggestion addedHierarchyLevel = null;
		TreeSet<String> suggestions = new TreeSet<>();

		HierarchyLevelSuggestion existingHierarchyLevel = hierarchyLevelSuggestionRepository
				.findByHierarchyLevelId(hierarchyLevelId);
		String normalizeHierarchyValue = StringUtils.normalizeSpace(hierarchyValue);
		if (existingHierarchyLevel == null) {
			HierarchyLevelSuggestion hierarchyLevelSuggestion = new HierarchyLevelSuggestion();
			hierarchyLevelSuggestion.setHierarchyLevelId(hierarchyLevelId);
			suggestions.add(normalizeHierarchyValue);
			hierarchyLevelSuggestion.setValues(suggestions);
			addedHierarchyLevel = hierarchyLevelSuggestionRepository.save(hierarchyLevelSuggestion);
			// cacheService.clearCache(CommonConstant.CACHE_HIERARCHY_LEVEL_VALUE);
		}

		if (Objects.nonNull(existingHierarchyLevel) && CollectionUtils.isNotEmpty(existingHierarchyLevel.getValues())) {
			TreeSet<String> existingSuggestions = existingHierarchyLevel.getValues();
			if (existingSuggestions.stream().noneMatch(normalizeHierarchyValue::equalsIgnoreCase)) {
				suggestions.add(normalizeHierarchyValue);
			}
			suggestions.addAll(existingSuggestions);
			existingHierarchyLevel.setValues(suggestions);
			addedHierarchyLevel = hierarchyLevelSuggestionRepository.save(existingHierarchyLevel);
			// cacheService.clearCache(CommonConstant.CACHE_HIERARCHY_LEVEL_VALUE);
		}

		return addedHierarchyLevel;
	}
}
