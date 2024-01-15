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

package com.publicissapient.kpidashboard.common.model.comments;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

public class CommentViewRequestDTOTest {
	@Mock
	List<String> nodes;
	@Mock
	List<String> kpiIds;
	@InjectMocks
	CommentViewRequestDTO commentViewRequestDTO;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testEquals() throws Exception {
		boolean result = commentViewRequestDTO.equals("o");
		Assert.assertEquals(true, result);
	}

	@Test
	public void testCanEqual() throws Exception {
		boolean result = commentViewRequestDTO.canEqual("other");
		Assert.assertEquals(true, result);
	}

	@Test
	public void testHashCode() throws Exception {
		int result = commentViewRequestDTO.hashCode();
		Assert.assertEquals(0, result);
	}

	@Test
	public void testToString() throws Exception {
		String result = commentViewRequestDTO.toString();
		Assert.assertEquals("replaceMeWithExpectedResult", result);
	}

	@Test
	public void testSetNodes() throws Exception {
		commentViewRequestDTO.setNodes(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetLevel() throws Exception {
		commentViewRequestDTO.setLevel("level");
	}

	@Test
	public void testSetNodeChildId() throws Exception {
		commentViewRequestDTO.setNodeChildId("nodeChildId");
	}

	@Test
	public void testSetKpiIds() throws Exception {
		commentViewRequestDTO.setKpiIds(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetCommentId() throws Exception {
		commentViewRequestDTO.setCommentId("commentId");
	}

	@Test
	public void testBuilder() throws Exception {
		CommentViewRequestDTO.CommentViewRequestDTOBuilder result = CommentViewRequestDTO.builder();
		Assert.assertEquals(null, result);
	}
}

// Generated with love by TestMe :) Please report issues and submit feature
// requests at: http://weirddev.com/forum#!/testme