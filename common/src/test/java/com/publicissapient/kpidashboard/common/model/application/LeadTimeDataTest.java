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

public class LeadTimeDataTest {
	@Mock
	List<String> issueNumber;
	@Mock
	List<String> urlList;
	@Mock
	List<String> issueDiscList;
	@Mock
	List<String> intakeToDor;
	@Mock
	List<String> dorToDOD;
	@Mock
	List<String> dodToLive;
	@Mock
	List<String> intakeToLive;
	@Mock
	List<String> openToTriage;
	@Mock
	List<String> triageToComplete;
	@Mock
	List<String> completeToLive;
	@Mock
	List<String> leadTime;
	@InjectMocks
	LeadTimeData leadTimeData;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testEquals() throws Exception {
		boolean result = leadTimeData.equals("o");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testCanEqual() throws Exception {
		boolean result = leadTimeData.canEqual("other");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testSetIssueNumber() throws Exception {
		leadTimeData.setIssueNumber(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetUrlList() throws Exception {
		leadTimeData.setUrlList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetIssueDiscList() throws Exception {
		leadTimeData.setIssueDiscList(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetIntakeToDor() throws Exception {
		leadTimeData.setIntakeToDor(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetDorToDOD() throws Exception {
		leadTimeData.setDorToDOD(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetDodToLive() throws Exception {
		leadTimeData.setDodToLive(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetIntakeToLive() throws Exception {
		leadTimeData.setIntakeToLive(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetOpenToTriage() throws Exception {
		leadTimeData.setOpenToTriage(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetTriageToComplete() throws Exception {
		leadTimeData.setTriageToComplete(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetCompleteToLive() throws Exception {
		leadTimeData.setCompleteToLive(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetLeadTime() throws Exception {
		leadTimeData.setLeadTime(Arrays.<String>asList("String"));
	}

	@Test
	public void testToString() throws Exception {
		String result = leadTimeData.toString();
		Assert.assertNotNull(result);
	}

	@Test
	public void testBuilder() throws Exception {
		LeadTimeData.LeadTimeDataBuilder result = LeadTimeData.builder();
		Assert.assertNotNull(result);
	}
}

// Generated with love by TestMe :) Please report issues and submit feature
// requests at: http://weirddev.com/forum#!/testme