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

import com.publicissapient.kpidashboard.common.model.generic.Processor;
import com.publicissapient.kpidashboard.common.model.generic.ProcessorError;
import com.publicissapient.kpidashboard.common.model.generic.ProcessorItem;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.mockito.Mockito.*;

public class ToolTest {
	// Field projectIds of type ObjectId - was not mocked since Mockito doesn't mock
	// a Final class when 'mock-maker-inline' option is not set
	@Mock
	List<ProcessorItem> processorItemList;
	@InjectMocks
	Tool tool;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testEquals() throws Exception {
		boolean result = tool.equals("o");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testCanEqual() throws Exception {
		boolean result = tool.canEqual("other");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testToString() throws Exception {
		String result = tool.toString();
		Assert.assertNotNull(result);
	}

	@Test
	public void testSetProjectIds() throws Exception {
		tool.setProjectIds(null);
	}

	@Test
	public void testSetTool() throws Exception {
		tool.setTool("tool");
	}

	@Test
	public void testSetUrl() throws Exception {
		tool.setUrl("url");
	}

	@Test
	public void testSetBranch() throws Exception {
		tool.setBranch("branch");
	}

	@Test
	public void testSetRepoSlug() throws Exception {
		tool.setRepoSlug("repoSlug");
	}

	@Test
	public void testSetRepositoryName() throws Exception {
		tool.setRepositoryName("repositoryName");
	}

	@Test
	public void testSetProcessorItemList() throws Exception {
		tool.setProcessorItemList(Arrays.<ProcessorItem>asList(new ProcessorItem("desc", true,
				Arrays.<ProcessorError>asList(new ProcessorError("errorCode", "errorMessage", 0L)), null, 0L,
				new HashMap<String, Object>() {
					{
						put("String", "toolDetailsMap");
					}
				}, Short.valueOf((short) 0), null, new Processor(null, null, true, true, null, 0L, null, true))));
	}

	@Test
	public void testBuilder() throws Exception {
		Tool.ToolBuilder result = Tool.builder();
		Assert.assertNotNull(result);
	}
}

// Generated with love by TestMe :) Please report issues and submit feature
// requests at: http://weirddev.com/forum#!/testme