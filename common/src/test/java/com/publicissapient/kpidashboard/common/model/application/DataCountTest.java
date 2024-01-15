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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DataCountTest {
	// Field deploymentDate of type DateTime - was not mocked since Mockito doesn't
	// mock a Final class when 'mock-maker-inline' option is not set
	@Mock
	Object value;
	@Mock
	Object drillDown;
	@Mock
	Map<String, Object> hoverValue;
	@Mock
	Map<String, ArrayList<Double>> hoverMap;
	@Mock
	Map<String, Integer> lineHoverValue;
	@Mock
	List<String> sprintIds;
	@Mock
	List<String> sprintNames;
	@Mock
	List<String> projectNames;
	@Mock
	Object maturityValue;
	@Mock
	Object aggregationValue;
	@Mock
	Object lineValue;
	@Mock
	Map<String, Object> subfilterValues;
	@Mock
	List<DataValue> dataValue;
	@Mock
	Object size;
	@InjectMocks
	DataCount dataCount;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testEquals() throws Exception {
		boolean result = dataCount.equals(new DataCount());
		Assert.assertEquals(false, result);
	}

	@Test
	public void testCanEqual() throws Exception {
		boolean result = dataCount.canEqual(new DataCount());
		Assert.assertEquals(true, result);
	}

	@Test
	public void testSetData() throws Exception {
		dataCount.setData("data");
	}

	@Test
	public void testSetCount() throws Exception {
		dataCount.setCount(Integer.valueOf(0));
	}

	@Test
	public void testSetPriority() throws Exception {
		dataCount.setPriority("priority");
	}

	@Test
	public void testSetSProjectName() throws Exception {
		dataCount.setSProjectName("sProjectName");
	}

	@Test
	public void testSetSSprintID() throws Exception {
		dataCount.setSSprintID("sSprintID");
	}

	@Test
	public void testSetDeploymentDate() throws Exception {
		dataCount.setDeploymentDate(null);
	}

	@Test
	public void testSetSStatus() throws Exception {
		dataCount.setSStatus("sStatus");
	}

	@Test
	public void testSetSSprintName() throws Exception {
		dataCount.setSSprintName("sSprintName");
	}

	@Test
	public void testSetSRootCause() throws Exception {
		dataCount.setSRootCause("sRootCause");
	}

	@Test
	public void testSetValue() throws Exception {
		dataCount.setValue("value");
	}

	@Test
	public void testSetDrillDown() throws Exception {
		dataCount.setDrillDown("drillDown");
	}

	@Test
	public void testSetKanbanDate() throws Exception {
		dataCount.setKanbanDate("kanbanDate");
	}

	@Test
	public void testSetHoverValue() throws Exception {
		dataCount.setHoverValue(new HashMap<String, Object>() {
			{
				put("String", "hoverValue");
			}
		});
	}

	@Test
	public void testSetHoverMap() throws Exception {
		dataCount.setHoverMap(new HashMap<String, ArrayList<Double>>() {
			{
				put("String", new ArrayList<Double>(Arrays.asList(Double.valueOf(0))));
			}
		});
	}

	@Test
	public void testSetLineHoverValue() throws Exception {
		dataCount.setLineHoverValue(new HashMap<String, Integer>() {
			{
				put("String", Integer.valueOf(0));
			}
		});
	}

	@Test
	public void testSetExecuted() throws Exception {
		dataCount.setExecuted("executed");
	}

	@Test
	public void testSetPassed() throws Exception {
		dataCount.setPassed("passed");
	}

	@Test
	public void testSetSubFilter() throws Exception {
		dataCount.setSubFilter("subFilter");
	}

	@Test
	public void testSetLineCategory() throws Exception {
		dataCount.setLineCategory("lineCategory");
	}

	@Test
	public void testSetDate() throws Exception {
		dataCount.setDate("date");
	}

	@Test
	public void testSetNoOfRelease() throws Exception {
		dataCount.setNoOfRelease(Integer.valueOf(0));
	}

	@Test
	public void testSetStartDate() throws Exception {
		dataCount.setStartDate("startDate");
	}

	@Test
	public void testSetEndDate() throws Exception {
		dataCount.setEndDate("endDate");
	}

	@Test
	public void testSetKpiGroup() throws Exception {
		dataCount.setKpiGroup("kpiGroup");
	}

	@Test
	public void testSetUrl() throws Exception {
		dataCount.setUrl("url");
	}

	@Test
	public void testSetSprintIds() throws Exception {
		dataCount.setSprintIds(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetSprintNames() throws Exception {
		dataCount.setSprintNames(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetProjectNames() throws Exception {
		dataCount.setProjectNames(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetGroupBy() throws Exception {
		dataCount.setGroupBy("groupBy");
	}

	@Test
	public void testSetMaturity() throws Exception {
		dataCount.setMaturity("maturity");
	}

	@Test
	public void testSetMaturityValue() throws Exception {
		dataCount.setMaturityValue("maturityValue");
	}

	@Test
	public void testSetAggregationValue() throws Exception {
		dataCount.setAggregationValue("aggregationValue");
	}

	@Test
	public void testSetLineValue() throws Exception {
		dataCount.setLineValue("lineValue");
	}

	@Test
	public void testSetSubfilterValues() throws Exception {
		dataCount.setSubfilterValues(new HashMap<String, Object>() {
			{
				put("String", "subfilterValues");
			}
		});
	}

	@Test
	public void testSetGraphType() throws Exception {
		dataCount.setGraphType("graphType");
	}

	@Test
	public void testSetDataValue() throws Exception {
		dataCount.setDataValue(Arrays
				.<DataValue>asList(new DataValue("name", "lineType", "data", "value", new HashMap<String, Object>() {
					{
						put("String", "hoverValue");
					}
				})));
	}

	@Test
	public void testSetSize() throws Exception {
		dataCount.setSize("size");
	}

	@Test
	public void testToString() throws Exception {
		String result = dataCount.toString();
		Assert.assertNotNull(result);
	}

	@Test
	public void testBuilder() throws Exception {
		DataCount.DataCountBuilder result = DataCount.builder();
		Assert.assertNotNull(result);
	}
}