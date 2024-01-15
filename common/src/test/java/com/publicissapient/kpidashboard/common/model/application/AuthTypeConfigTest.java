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

import static org.mockito.Mockito.*;

public class AuthTypeConfigTest {
	@Mock
	AuthTypeStatus authTypeStatus;
	@Mock
	ADServerDetail adServerDetail;
	@InjectMocks
	AuthTypeConfig authTypeConfig;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testSetAuthTypeStatus() throws Exception {
		authTypeConfig.setAuthTypeStatus(new AuthTypeStatus());
	}

	@Test
	public void testSetAdServerDetail() throws Exception {
		authTypeConfig
				.setAdServerDetail(new ADServerDetail("username", "password", "host", 0, "userDn", "rootDn", "domain"));
	}

	@Test
	public void testEquals() throws Exception {
		boolean result = authTypeConfig.equals(new AuthTypeConfig());
		Assert.assertEquals(false, result);
	}

	@Test
	public void testCanEqual() throws Exception {
		boolean result = authTypeConfig.canEqual(new AuthTypeConfigTest());
		Assert.assertEquals(false, result);
	}


	@Test
	public void testToString() throws Exception {
		String result = authTypeConfig.toString();
		Assert.assertNotNull(result);
	}
}

