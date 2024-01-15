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

import com.publicissapient.kpidashboard.common.activedirectory.modal.ADServerDetail;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

import static org.mockito.Mockito.*;

public class GlobalConfigTest {
	@Mock
	AuthTypeStatus authTypeStatus;
	@Mock
	ADServerDetail adServerDetail;
	@Mock
	EmailServerDetail emailServerDetail;
	// Field id of type ObjectId - was not mocked since Mockito doesn't mock a Final
	// class when 'mock-maker-inline' option is not set
	@InjectMocks
	GlobalConfig globalConfig;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testSetEnv() throws Exception {
		globalConfig.setEnv("env");
	}

	@Test
	public void testSetAuthTypeStatus() throws Exception {
		globalConfig.setAuthTypeStatus(new AuthTypeStatus());
	}

	@Test
	public void testSetAdServerDetail() throws Exception {
		globalConfig
				.setAdServerDetail(new ADServerDetail("username", "password", "host", 0, "userDn", "rootDn", "domain"));
	}

	@Test
	public void testSetEmailServerDetail() throws Exception {
		globalConfig.setEmailServerDetail(
				new EmailServerDetail("emailHost", 0, "fromEmail", Arrays.<String>asList("String")));
	}

	@Test
	public void testSetZephyrCloudBaseUrl() throws Exception {
		globalConfig.setZephyrCloudBaseUrl("zephyrCloudBaseUrl");
	}

	@Test
	public void testEquals() throws Exception {
		boolean result = globalConfig.equals("o");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testCanEqual() throws Exception {
		boolean result = globalConfig.canEqual("other");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testToString() throws Exception {
		String result = globalConfig.toString();
		Assert.assertNotNull(result);
	}

	@Test
	public void testBuilder() throws Exception {
		GlobalConfig.GlobalConfigBuilder result = GlobalConfig.builder();
		Assert.assertNotNull( result);
	}

	@Test
	public void testSetId() throws Exception {
		globalConfig.setId(null);
	}
}

// Generated with love by TestMe :) Please report issues and submit feature
// requests at: http://weirddev.com/forum#!/testme