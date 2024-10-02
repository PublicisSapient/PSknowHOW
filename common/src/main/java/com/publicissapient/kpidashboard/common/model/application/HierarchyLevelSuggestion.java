package com.publicissapient.kpidashboard.common.model.application;

import java.io.Serializable;
import java.util.TreeSet;

import org.springframework.data.mongodb.core.mapping.Document;

import com.publicissapient.kpidashboard.common.model.generic.BasicModel;

import lombok.Data;

@Data
@Document(collection = "hierarchy_level_suggestions")
public class HierarchyLevelSuggestion extends BasicModel implements Serializable {
	private String hierarchyLevelId;
	private TreeSet<String> values;
}
