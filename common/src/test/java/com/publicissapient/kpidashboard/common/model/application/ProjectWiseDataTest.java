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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.mockito.Mockito.*;

public class ProjectWiseDataTest {
	@Mock
	List<DataCount> value;
	@InjectMocks
	ProjectWiseData projectWiseData;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testEquals() throws Exception {
		boolean result = projectWiseData.equals("o");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testCanEqual() throws Exception {
		boolean result = projectWiseData.canEqual("other");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testSetValue() throws Exception {
		projectWiseData.setValue(Arrays.<DataCount>asList(
				new DataCount("data", Integer.valueOf(0), "priority", "sProjectName", "sSprintID", null, "sStatus",
						"sSprintName", "sRootCause", "value", "drillDown", "kanbanDate", new HashMap<String, Object>() {
							{
								put("String", "hoverValue");
							}
						}, new HashMap<String, ArrayList<Double>>() {
							{
								put("String", new ArrayList<Double>(Arrays.asList(Double.valueOf(0))));
							}
						}, new HashMap<String, Integer>() {
							{
								put("String", Integer.valueOf(0));
							}
						}, "executed", "passed", "subFilter", "lineCategory", "date", Integer.valueOf(0), "startDate",
						"endDate", "kpiGroup", "url", Arrays.<String>asList("String"), Arrays.<String>asList("String"),
						Arrays.<String>asList("String"), "groupBy", "maturity", "maturityValue", "aggregationValue",
						"lineValue", new HashMap<String, Object>() {
							{
								put("String", "subfilterValues");
							}
						}, "graphType", Arrays.<DataValue>asList(
								new DataValue("name", "lineType", "data", "value", new HashMap<String, Object>() {
									{
										put("String", "hoverValue");
									}
								})),
						"size")));
	}

	@Test
	public void testSetData() throws Exception {
		projectWiseData.setData("data");
	}

	@Test
	public void testToString() throws Exception {
		String result = projectWiseData.toString();
		Assert.assertNotNull(result);
	}
}

// Generated with love by TestMe :) Please report issues and submit feature
// requests at: http://weirddev.com/forum#!/testme