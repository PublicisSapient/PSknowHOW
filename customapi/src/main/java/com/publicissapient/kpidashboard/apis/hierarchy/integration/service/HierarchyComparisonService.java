package com.publicissapient.kpidashboard.apis.hierarchy.integration.service;

import java.util.Set;
import com.publicissapient.kpidashboard.common.model.application.OrganizationHierarchy;

public interface HierarchyComparisonService {
    /**
     * Compare API hierarchy with database hierarchy and update externalIds
     * @param apiHierarchy Set of hierarchy nodes from API
     * @return Set of updated hierarchy nodes
     */
    Set<OrganizationHierarchy> compareAndUpdateHierarchy(Set<OrganizationHierarchy> apiHierarchy);
}
