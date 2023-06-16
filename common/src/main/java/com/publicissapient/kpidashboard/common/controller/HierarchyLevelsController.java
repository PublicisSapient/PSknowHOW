package com.publicissapient.kpidashboard.common.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevelSuggestion;
import com.publicissapient.kpidashboard.common.model.application.dto.HierarchyLevelDTO;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelService;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelSuggestionsService;

@RestController
@RequestMapping("/hierarchylevels")
public class HierarchyLevelsController {

	@Autowired
	private HierarchyLevelService hierarchyLevelService;

	@Autowired
	private HierarchyLevelSuggestionsService hierarchyLevelSuggestionsService;

	@GetMapping
	private ResponseEntity<List<HierarchyLevelDTO>> getHierarchyLevel() {

		List<HierarchyLevel> hierarchyLevels = hierarchyLevelService.getTopHierarchyLevels();
		List<HierarchyLevelSuggestion> suggestions = hierarchyLevelSuggestionsService.getSuggestions();

		return new ResponseEntity<>(createHierarchyLevelsResult(hierarchyLevels, suggestions), HttpStatus.OK);
	}

	private List<HierarchyLevelDTO> createHierarchyLevelsResult(List<HierarchyLevel> hierarchyLevels,
			List<HierarchyLevelSuggestion> suggestions) {

		List<HierarchyLevelDTO> result = new ArrayList<>();
		for (HierarchyLevel hierarchyLevel : hierarchyLevels) {
			HierarchyLevelDTO hierarchyLevelDTO = new HierarchyLevelDTO();
			hierarchyLevelDTO.setLevel(hierarchyLevel.getLevel());
			hierarchyLevelDTO.setHierarchyLevelId(hierarchyLevel.getHierarchyLevelId());
			hierarchyLevelDTO.setHierarchyLevelName(hierarchyLevel.getHierarchyLevelName());
			HierarchyLevelSuggestion hierarchyLevelSuggestion = suggestions.stream()
					.filter(value -> value.getHierarchyLevelId().equals(hierarchyLevel.getHierarchyLevelId()))
					.findFirst().orElse(null);
			if (hierarchyLevelSuggestion != null) {

				hierarchyLevelDTO.setSuggestions(hierarchyLevelSuggestion.getValues());
			}
			result.add(hierarchyLevelDTO);
		}
		return result;
	}
}
