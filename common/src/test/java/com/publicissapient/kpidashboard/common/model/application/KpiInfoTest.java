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

public class KpiInfoTest {
	@Mock
	List<KpiFormula> formula;
	@Mock
	List<KpiDetail> details;
	@Mock
	List<KpiMaturityLevel> maturityLevels;
	@InjectMocks
	KpiInfo kpiInfo;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testSetDefinition() throws Exception {
		kpiInfo.setDefinition("definition");
	}

	@Test
	public void testSetFormula() throws Exception {
		kpiInfo.setFormula(Arrays.<KpiFormula>asList(new KpiFormula()));
	}

	@Test
	public void testSetDetails() throws Exception {
		kpiInfo.setDetails(Arrays.<KpiDetail>asList(new KpiDetail()));
	}

	@Test
	public void testSetMaturityLevels() throws Exception {
		kpiInfo.setMaturityLevels(Arrays.<KpiMaturityLevel>asList(new KpiMaturityLevel()));
	}

	@Test
	public void testEquals() throws Exception {
		boolean result = kpiInfo.equals("o");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testCanEqual() throws Exception {
		boolean result = kpiInfo.canEqual("other");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testToString() throws Exception {
		String result = kpiInfo.toString();
		Assert.assertNotNull(result);
	}
}

// Generated with love by TestMe :) Please report issues and submit feature
// requests at: http://weirddev.com/forum#!/testme