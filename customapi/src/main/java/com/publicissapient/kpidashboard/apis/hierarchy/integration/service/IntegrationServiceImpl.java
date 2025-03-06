/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.apis.hierarchy.integration.service;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.hierarchy.integration.adapter.OrganizationHierarchyAdapter;
import com.publicissapient.kpidashboard.apis.hierarchy.integration.dto.HierarchyDetails;
import com.publicissapient.kpidashboard.apis.hierarchy.integration.service.HierarchyComparisonService;
import com.publicissapient.kpidashboard.common.model.application.OrganizationHierarchy;

@Service
public class IntegrationServiceImpl implements IntegrationService {

	@Autowired
	private OrganizationHierarchyAdapter organizationHierarchyAdapter;
	
	@Autowired
	private HierarchyComparisonService hierarchyComparisonService;

	@Override
	public Set<OrganizationHierarchy> convertHieracyResponseToOrganizationHierachy(HierarchyDetails hierarchyDetails) {
		// First convert API response to comparable objects
		Set<OrganizationHierarchy> apiHierarchy = organizationHierarchyAdapter.convertToOrganizationHierarchy(hierarchyDetails);
		
		// Then compare with database and update externalIds
		return hierarchyComparisonService.compareAndUpdateHierarchy(apiHierarchy);
	}
}
