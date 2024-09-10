package com.publicissapient.kpidashboard.common.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
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
	private ResponseEntity<List<HierarchyLevel>> getHierarchyLevel() {

		List<HierarchyLevel> hierarchyLevels = hierarchyLevelService.getTopHierarchyLevels();

		return new ResponseEntity<>(hierarchyLevels, HttpStatus.OK);
	}
}
