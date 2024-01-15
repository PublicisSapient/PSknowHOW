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

import java.time.LocalDateTime;
import java.time.Month;

public class KanbanAccountHierarchyTest {
	// Field filterCategoryId of type ObjectId - was not mocked since Mockito
	// doesn't mock a Final class when 'mock-maker-inline' option is not set
	// Field basicProjectConfigId of type ObjectId - was not mocked since Mockito
	// doesn't mock a Final class when 'mock-maker-inline' option is not set
	// Field createdDate of type LocalDateTime - was not mocked since Mockito
	// doesn't mock a Final class when 'mock-maker-inline' option is not set
	// Field id of type ObjectId - was not mocked since Mockito doesn't mock a Final
	// class when 'mock-maker-inline' option is not set
	KanbanAccountHierarchy kanbanAccountHierarchy = new KanbanAccountHierarchy("nodeId", "nodeName", "labelName", null,
			"beginDate", "endDate", "parentId", null, "isDeleted", "path", "releaseState",
			LocalDateTime.of(2024, Month.JANUARY, 12, 0, 23, 2));

	@Test
	public void testEquals() throws Exception {
		boolean result = kanbanAccountHierarchy.equals("obj");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testToString() throws Exception {
		String result = kanbanAccountHierarchy.toString();
		Assert.assertNotNull(result);
	}

	@Test
	public void testSetNodeId() throws Exception {
		kanbanAccountHierarchy.setNodeId("nodeId");
	}

	@Test
	public void testSetNodeName() throws Exception {
		kanbanAccountHierarchy.setNodeName("nodeName");
	}

	@Test
	public void testSetLabelName() throws Exception {
		kanbanAccountHierarchy.setLabelName("labelName");
	}

	@Test
	public void testSetFilterCategoryId() throws Exception {
		kanbanAccountHierarchy.setFilterCategoryId(null);
	}

	@Test
	public void testSetBeginDate() throws Exception {
		kanbanAccountHierarchy.setBeginDate("beginDate");
	}

	@Test
	public void testSetEndDate() throws Exception {
		kanbanAccountHierarchy.setEndDate("endDate");
	}

	@Test
	public void testSetParentId() throws Exception {
		kanbanAccountHierarchy.setParentId("parentId");
	}

	@Test
	public void testSetBasicProjectConfigId() throws Exception {
		kanbanAccountHierarchy.setBasicProjectConfigId(null);
	}

	@Test
	public void testSetIsDeleted() throws Exception {
		kanbanAccountHierarchy.setIsDeleted("isDeleted");
	}

	@Test
	public void testSetPath() throws Exception {
		kanbanAccountHierarchy.setPath("path");
	}

	@Test
	public void testSetReleaseState() throws Exception {
		kanbanAccountHierarchy.setReleaseState("releaseState");
	}

	@Test
	public void testSetCreatedDate() throws Exception {
		kanbanAccountHierarchy.setCreatedDate(LocalDateTime.of(2024, Month.JANUARY, 12, 0, 23, 2));
	}

	@Test
	public void testBuilder() throws Exception {
		KanbanAccountHierarchy.KanbanAccountHierarchyBuilder result = KanbanAccountHierarchy.builder();
		Assert.assertNotNull(result);
	}

	@Test
	public void testSetId() throws Exception {
		kanbanAccountHierarchy.setId(null);
	}
}

// Generated with love by TestMe :) Please report issues and submit feature
// requests at: http://weirddev.com/forum#!/testme