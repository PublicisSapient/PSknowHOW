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

public class ProjectReleaseTest {
	// Field configId of type ObjectId - was not mocked since Mockito doesn't mock a
	// Final class when 'mock-maker-inline' option is not set
	@Mock
	List<ProjectVersion> listProjectVersion;
	// Field id of type ObjectId - was not mocked since Mockito doesn't mock a Final
	// class when 'mock-maker-inline' option is not set
	@InjectMocks
	ProjectRelease projectRelease;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testEquals() throws Exception {
		boolean result = projectRelease.equals("o");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testCanEqual() throws Exception {
		boolean result = projectRelease.canEqual("other");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testToString() throws Exception {
		String result = projectRelease.toString();
		Assert.assertNotNull(result);
	}

	@Test
	public void testSetProjectName() throws Exception {
		projectRelease.setProjectName("projectName");
	}

	@Test
	public void testSetProjectId() throws Exception {
		projectRelease.setProjectId("projectId");
	}

	@Test
	public void testSetConfigId() throws Exception {
		projectRelease.setConfigId(null);
	}

	@Test
	public void testSetListProjectVersion() throws Exception {
		projectRelease.setListProjectVersion(Arrays.<ProjectVersion>asList(
				new ProjectVersion(Long.valueOf(1), "description", "name", true, true, null, null)));
	}

	@Test
	public void testBuilder() throws Exception {
		ProjectRelease.ProjectReleaseBuilder result = ProjectRelease.builder();
		Assert.assertNotNull(result);
	}

	@Test
	public void testSetId() throws Exception {
		projectRelease.setId(null);
	}
}

// Generated with love by TestMe :) Please report issues and submit feature
// requests at: http://weirddev.com/forum#!/testme