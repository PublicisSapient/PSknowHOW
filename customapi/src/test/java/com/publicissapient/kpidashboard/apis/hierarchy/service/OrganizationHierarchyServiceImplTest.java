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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.data.OrganizationHierarchyDataFactory;
import com.publicissapient.kpidashboard.common.model.application.OrganizationHierarchy;
import com.publicissapient.kpidashboard.common.repository.application.OrganizationHierarchyRepository;

@RunWith(MockitoJUnitRunner.class)
public class OrganizationHierarchyServiceImplTest {

	@Mock
	ConfigHelperService configHelperService;

	@Mock
	OrganizationHierarchyRepository organizationHierarchyRepository;

	@InjectMocks
	OrganizationHierarchyServiceImpl organizationHierarchyService;

	List<OrganizationHierarchy> organizationHierarchyList = null;

	@Before
	public void setUp() {
		organizationHierarchyList = OrganizationHierarchyDataFactory.newInstance().getOrganizationHierarchies();
	}

	@Test
	public void testFindAll() {

		when(configHelperService.loadAllOrganizationHierarchy()).thenReturn(organizationHierarchyList);

		List<OrganizationHierarchy> result = organizationHierarchyService.findAll();

		assertEquals(organizationHierarchyList, result);
		verify(configHelperService, times(1)).loadAllOrganizationHierarchy();
	}

	@Test
	public void testFindByNodeId() {
		OrganizationHierarchy orgHierarchy = new OrganizationHierarchy();

		orgHierarchy.setNodeId("project_unique_004");
		orgHierarchy.setNodeName("Test Project");
		orgHierarchy.setNodeDisplayName("Test Project");
		orgHierarchy.setHierarchyLevelId("project");
		orgHierarchy.setParentId("hierarchyLevelThree_unique_001");

		when(configHelperService.loadAllOrganizationHierarchy()).thenReturn(organizationHierarchyList);

		OrganizationHierarchy result = organizationHierarchyService.findByNodeId("project_unique_004");

		assertEquals(orgHierarchy, result);
	}

	@Test
	public void testProjectSaveNode() {
		OrganizationHierarchy orgHierarchy = new OrganizationHierarchy();
		orgHierarchy.setNodeId("project_unique_002");
		orgHierarchy.setNodeName("Test Project 2");
		orgHierarchy.setNodeDisplayName("Test Project 2");
		orgHierarchy.setHierarchyLevelId("project");
		orgHierarchy.setParentId("hierarchyLevelThree_unique_001");

		when(organizationHierarchyRepository.save(orgHierarchy)).thenReturn(orgHierarchy);

		OrganizationHierarchy result = organizationHierarchyService.save(orgHierarchy);

		assertEquals(orgHierarchy, result);
		verify(organizationHierarchyRepository, times(1)).save(orgHierarchy);
	}
}
