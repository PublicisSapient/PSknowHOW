package com.publicissapient.kpidashboard.common.model.application.dto;

import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import lombok.Data;

@Data
public class HierarchyValueDTO {

    private HierarchyLevel hierarchyLevel;
    private String orgHierarchyNodeId;
    private String value;
}
