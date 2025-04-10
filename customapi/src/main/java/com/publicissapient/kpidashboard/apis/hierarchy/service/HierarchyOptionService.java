package com.publicissapient.kpidashboard.apis.hierarchy.service;

import com.publicissapient.kpidashboard.apis.hierarchy.dto.CreateHierarchyRequest;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;

import jakarta.validation.Valid;

public interface HierarchyOptionService {

	ServiceResponse addHierarchyOption(@Valid CreateHierarchyRequest hierarchyOption, String parentId);
}
