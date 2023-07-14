package com.publicissapient.kpidashboard.common.model.application.dto;

import java.util.TreeSet;

import lombok.Data;

@Data
public class HierarchyLevelDTO {
	private int level;
	private String hierarchyLevelId;
	private String hierarchyLevelName;
	private TreeSet<String> suggestions;
}
