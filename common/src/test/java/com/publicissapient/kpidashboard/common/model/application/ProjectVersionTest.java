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
import org.junit.Test;

public class ProjectVersionTest {
	// Field releaseDate of type DateTime - was not mocked since Mockito doesn't
	// mock a Final class when 'mock-maker-inline' option is not set
	// Field startDate of type DateTime - was not mocked since Mockito doesn't mock
	// a Final class when 'mock-maker-inline' option is not set
	ProjectVersion projectVersion = new ProjectVersion(Long.valueOf(1), "description", "name", true, true, null, null);

	@Test
	public void testEquals() throws Exception {
		boolean result = projectVersion.equals("obj");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testSetId() throws Exception {
		projectVersion.setId(Long.valueOf(1));
	}

	@Test
	public void testSetDescription() throws Exception {
		projectVersion.setDescription("description");
	}

	@Test
	public void testSetName() throws Exception {
		projectVersion.setName("name");
	}

	@Test
	public void testSetArchived() throws Exception {
		projectVersion.setArchived(true);
	}

	@Test
	public void testSetReleased() throws Exception {
		projectVersion.setReleased(true);
	}

	@Test
	public void testSetReleaseDate() throws Exception {
		projectVersion.setReleaseDate(null);
	}

	@Test
	public void testSetStartDate() throws Exception {
		projectVersion.setStartDate(null);
	}

	@Test
	public void testToString() throws Exception {
		String result = projectVersion.toString();
		Assert.assertNotNull(result);
	}

	@Test
	public void testBuilder() throws Exception {
		ProjectVersion.ProjectVersionBuilder result = ProjectVersion.builder();
		Assert.assertNotNull(result);
	}
}

// Generated with love by TestMe :) Please report issues and submit feature
// requests at: http://weirddev.com/forum#!/testme