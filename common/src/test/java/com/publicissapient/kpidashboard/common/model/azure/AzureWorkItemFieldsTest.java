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

package com.publicissapient.kpidashboard.common.model.azure;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AzureWorkItemFieldsTest {
	@Mock
	JSONObject assignedTo;
	@Mock
	JSONObject createdBy;
	@Mock
	Date createdDate;
	@Mock
	JSONObject changedBy;
	@Mock
	Date changedDate;
	@Mock
	Date StateChangeDate;
	@InjectMocks
	AzureWorkItemFields azureWorkItemFields;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testSetAreaPath() throws Exception {
		azureWorkItemFields.setAreaPath("areaPath");
	}

	@Test
	public void testSetTeamProject() throws Exception {
		azureWorkItemFields.setTeamProject("teamProject");
	}

	@Test
	public void testSetIterationPath() throws Exception {
		azureWorkItemFields.setIterationPath("iterationPath");
	}

	@Test
	public void testSetWorkItemType() throws Exception {
		azureWorkItemFields.setWorkItemType("workItemType");
	}

	@Test
	public void testSetWorkItemState() throws Exception {
		azureWorkItemFields.setWorkItemState("workItemState");
	}

	@Test
	public void testSetAssignedTo() throws Exception {
		azureWorkItemFields.setAssignedTo(null);
	}

	@Test
	public void testSetCreatedBy() throws Exception {
		azureWorkItemFields.setCreatedBy(null);
	}

	@Test
	public void testSetCreatedDate() throws Exception {
		azureWorkItemFields.setCreatedDate(new GregorianCalendar(2024, Calendar.JANUARY, 11, 22, 55).getTime());
	}

	@Test
	public void testSetChangedBy() throws Exception {
		azureWorkItemFields.setChangedBy(null);
	}

	@Test
	public void testSetChangedDate() throws Exception {
		azureWorkItemFields.setChangedDate(new GregorianCalendar(2024, Calendar.JANUARY, 11, 22, 55).getTime());
	}

	@Test
	public void testSetTitle() throws Exception {
		azureWorkItemFields.setTitle("title");
	}

	@Test
	public void testSetDescription() throws Exception {
		azureWorkItemFields.setDescription("description");
	}

	@Test
	public void testSetReason() throws Exception {
		azureWorkItemFields.setReason("reason");
	}

	@Test
	public void testSetPriority() throws Exception {
		azureWorkItemFields.setPriority(Long.valueOf(1));
	}

	@Test
	public void testSetCommentCount() throws Exception {
		azureWorkItemFields.setCommentCount(Long.valueOf(1));
	}

	@Test
	public void testSetStateChangeDate() throws Exception {
		azureWorkItemFields.setStateChangeDate(new GregorianCalendar(2024, Calendar.JANUARY, 11, 22, 55).getTime());
	}

}