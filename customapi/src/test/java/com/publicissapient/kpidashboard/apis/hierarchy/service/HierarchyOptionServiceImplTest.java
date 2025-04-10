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
package com.publicissapient.kpidashboard.apis.hierarchy.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.hierarchy.dto.CreateHierarchyRequest;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.OrganizationHierarchy;

@RunWith(MockitoJUnitRunner.class)
public class HierarchyOptionServiceImplTest {

	@InjectMocks
	private HierarchyOptionServiceImpl hierarchyOptionService;

	@Mock
	private CacheService cacheService;

	@Mock
	private OrganizationHierarchyService organizationHierarchyService;

	@BeforeEach
	public void setUp() {

	}

	@Test
	public void testAddHierarchyOption_NoHierarchyLevelFound() {
		// Arrange
		CreateHierarchyRequest request = new CreateHierarchyRequest("Test Node");
		when(cacheService.getFullHierarchyLevel()).thenReturn(Collections.emptyList());

		// Act
		ServiceResponse response = hierarchyOptionService.addHierarchyOption(request, null);

		// Assert
		assertFalse(response.getSuccess());
		assertEquals("No Hierarchy Level Found", response.getMessage());
	}

	@Test
	public void testAddHierarchyOption_CreateRootNode() {
		// Arrange
		CreateHierarchyRequest request = new CreateHierarchyRequest("Root Node");
		HierarchyLevel hierarchyLevel = new HierarchyLevel(1, "bu", "bu", "bu");
		when(cacheService.getFullHierarchyLevel()).thenReturn(List.of(hierarchyLevel));

		// Act
		ServiceResponse response = hierarchyOptionService.addHierarchyOption(request, null);

		// Assert
		assertTrue(response.getSuccess());
		assertEquals("Node is created at root level.", response.getMessage());
		verify(organizationHierarchyService).save(any(OrganizationHierarchy.class));
	}

	@Test
	public void testAddHierarchyOption_InvalidParentId() {
		// Arrange
		CreateHierarchyRequest request = new CreateHierarchyRequest("Child Node");
		HierarchyLevel hierarchyLevel = new HierarchyLevel(1, "bu", "bu", "bu");
		when(cacheService.getFullHierarchyLevel()).thenReturn(List.of(hierarchyLevel));
		when(organizationHierarchyService.findByNodeId("invalidParentId")).thenReturn(null);
		// Act
		ServiceResponse response = hierarchyOptionService.addHierarchyOption(request, "invalidParentId");

		// Assert
		assertFalse(response.getSuccess());
		assertEquals("Invalid parentId: No such parent node exists", response.getMessage());
	}

	@Test
	public void testAddHierarchyOption_ParentHierarchyLevelNotFound() {
		// Arrange
		CreateHierarchyRequest request = new CreateHierarchyRequest("Child Node");
		HierarchyLevel parentLevel = new HierarchyLevel(1, "bu", "bu", "bu");
		when(cacheService.getFullHierarchyLevel()).thenReturn(List.of(parentLevel));
		OrganizationHierarchy parentOrganization = new OrganizationHierarchy();
		parentOrganization.setHierarchyLevelId("bu");
		when(organizationHierarchyService.findByNodeId("parentId")).thenReturn(parentOrganization);
		when(cacheService.getFullHierarchyLevel()).thenReturn(List.of(parentLevel));

		// Act
		ServiceResponse response = hierarchyOptionService.addHierarchyOption(request, "parentId");

		// Assert
		assertFalse(response.getSuccess());
		assertEquals("Hierarchy level for child node not found", response.getMessage());
	}

	@Test
	public void testAddHierarchyOption_SuccessfulChildNodeCreation() {
		// Arrange
		CreateHierarchyRequest request = new CreateHierarchyRequest("Child Node");
		HierarchyLevel parentLevel = new HierarchyLevel(1, "bu", "bu", "bu");
		HierarchyLevel childLevel = new HierarchyLevel(2, "ver", "ver", "vertical");
		when(cacheService.getFullHierarchyLevel()).thenReturn(List.of(parentLevel, childLevel));
		OrganizationHierarchy parentOrganization = new OrganizationHierarchy();
		parentOrganization.setHierarchyLevelId("bu");
		when(organizationHierarchyService.findByNodeId("parentId")).thenReturn(parentOrganization);

		// Act
		ServiceResponse response = hierarchyOptionService.addHierarchyOption(request, "parentId");

		// Assert
		assertTrue(response.getSuccess());
		assertEquals("Node created successfully under parentId: parentId", response.getMessage());
		verify(organizationHierarchyService).save(any(OrganizationHierarchy.class));
	}

}