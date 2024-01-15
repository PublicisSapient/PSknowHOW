/*
 *
 *   Copyright 2014 CapitalOne, LLC.
 *   Further development Copyright 2022 Sapient Corporation.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.publicissapient.kpidashboard.common.model.application;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

public class ProjectBasicConfigTest {
	@Mock
	List<HierarchyValue> hierarchy;
	// Field id of type ObjectId - was not mocked since Mockito doesn't mock a Final
	// class when 'mock-maker-inline' option is not set
	@InjectMocks
	ProjectBasicConfig projectBasicConfig;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testEquals() throws Exception {
		boolean result = projectBasicConfig.equals("o");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testToString() throws Exception {
		String result = projectBasicConfig.toString();
		Assert.assertNotNull(result);
	}

	@Test
	public void testSetEmmUpdatedOn() throws Exception {
		projectBasicConfig.setEmmUpdatedOn("emmUpdatedOn");
	}

	@Test
	public void testSetConsumerCreatedOn() throws Exception {
		projectBasicConfig.setConsumerCreatedOn("consumerCreatedOn");
	}

	@Test
	public void testSetProjectName() throws Exception {
		projectBasicConfig.setProjectName("projectName");
	}

	@Test
	public void testSetCreatedAt() throws Exception {
		projectBasicConfig.setCreatedAt("createdAt");
	}

	@Test
	public void testSetUpdatedAt() throws Exception {
		projectBasicConfig.setUpdatedAt("updatedAt");
	}

	@Test
	public void testSetKanban() throws Exception {
		projectBasicConfig.setKanban(true);
	}

	@Test
	public void testSetHierarchy() throws Exception {
		projectBasicConfig.setHierarchy(Arrays.<HierarchyValue>asList(new HierarchyValue()));
	}

	@Test
	public void testSetSaveAssigneeDetails() throws Exception {
		projectBasicConfig.setSaveAssigneeDetails(true);
	}

	@Test
	public void testBuilder() throws Exception {
		ProjectBasicConfig.ProjectBasicConfigBuilder result = ProjectBasicConfig.builder();
		Assert.assertNotNull(result);
	}

	@Test
	public void testSetId() throws Exception {
		projectBasicConfig.setId(null);
	}
}

// Generated with love by TestMe :) Please report issues and submit feature
// requests at: http://weirddev.com/forum#!/testme