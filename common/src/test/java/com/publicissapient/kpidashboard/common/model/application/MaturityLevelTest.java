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

public class MaturityLevelTest {
	@Mock
	List<String> range;
	@InjectMocks
	MaturityLevel maturityLevel;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testSetLevel() throws Exception {
		maturityLevel.setLevel("level");
	}

	@Test
	public void testSetRange() throws Exception {
		maturityLevel.setRange(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetBgColor() throws Exception {
		maturityLevel.setBgColor("bgColor");
	}

	@Test
	public void testSetLabel() throws Exception {
		maturityLevel.setLabel("label");
	}

	@Test
	public void testSetDisplayRange() throws Exception {
		maturityLevel.setDisplayRange("displayRange");
	}

	@Test
	public void testEquals() throws Exception {
		boolean result = maturityLevel.equals("o");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testCanEqual() throws Exception {
		boolean result = maturityLevel.canEqual("other");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testToString() throws Exception {
		String result = maturityLevel.toString();
		Assert.assertNotNull(result);
	}
}

// Generated with love by TestMe :) Please report issues and submit feature
// requests at: http://weirddev.com/forum#!/testme