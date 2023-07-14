package com.publicissapient.kpidashboard.apis.filter.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelService;

@RestController
public class FilterController {

	@Autowired
	private HierarchyLevelService hierarchyLevelService;

	@GetMapping("/filters")
	public ResponseEntity<ServiceResponse> getFilters() {

		List<HierarchyLevel> scrumHierarchyLevels = hierarchyLevelService.getFullHierarchyLevels(false);
		List<HierarchyLevel> kanbanHierarchyLevels = hierarchyLevelService.getFullHierarchyLevels(true);
		Map<String, List<HierarchyLevel>> filtersMap = new HashMap<>();
		filtersMap.put("scrum", scrumHierarchyLevels);
		filtersMap.put("kanban", kanbanHierarchyLevels);

		ServiceResponse response = new ServiceResponse(true, "filters", filtersMap);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
}
