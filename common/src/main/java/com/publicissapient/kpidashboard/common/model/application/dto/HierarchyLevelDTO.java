package com.publicissapient.kpidashboard.common.model.application.dto;

import lombok.Data;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Data
public class HierarchyLevelDTO {
    private int level;
    private String hierarchyLevelId;
    private String hierarchyLevelName;
    private TreeSet<String> suggestions;
}
