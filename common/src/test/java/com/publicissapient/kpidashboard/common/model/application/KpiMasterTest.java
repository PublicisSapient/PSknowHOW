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

import com.publicissapient.kpidashboard.common.model.kpivideolink.KPIVideoLink;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

public class KpiMasterTest {
	@Mock
	List<String> kpiOnDashboard;
	@Mock
	KPIVideoLink videoLink;
	@Mock
	Object maxValue;
	@Mock
	KpiInfo kpiInfo;
	@Mock
	List<KpiFormula> trendCalculation;
	@Mock
	List<String> maturityRange;
	@Mock
	List<MaturityLevel> maturityLevel;
	@Mock
	Map<Integer, String> yaxisOrder;
	// Field id of type ObjectId - was not mocked since Mockito doesn't mock a Final
	// class when 'mock-maker-inline' option is not set
	@InjectMocks
	KpiMaster kpiMaster;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testEquals() throws Exception {
		boolean result = kpiMaster.equals("o");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testCanEqual() throws Exception {
		boolean result = kpiMaster.canEqual("other");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testToString() throws Exception {
		String result = kpiMaster.toString();
		Assert.assertNotNull(result);
	}

	@Test
	public void testSetKpiId() throws Exception {
		kpiMaster.setKpiId("kpiId");
	}

	@Test
	public void testSetKpiName() throws Exception {
		kpiMaster.setKpiName("kpiName");
	}

	@Test
	public void testSetIsDeleted() throws Exception {
		kpiMaster.setIsDeleted("isDeleted");
	}

	@Test
	public void testSetDefaultOrder() throws Exception {
		kpiMaster.setDefaultOrder(Integer.valueOf(0));
	}

	@Test
	public void testSetKpiCategory() throws Exception {
		kpiMaster.setKpiCategory("kpiCategory");
	}

	@Test
	public void testSetKpiSubCategory() throws Exception {
		kpiMaster.setKpiSubCategory("kpiSubCategory");
	}

	@Test
	public void testSetKpiInAggregatedFeed() throws Exception {
		kpiMaster.setKpiInAggregatedFeed("kpiInAggregatedFeed");
	}

	@Test
	public void testSetKpiOnDashboard() throws Exception {
		kpiMaster.setKpiOnDashboard(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetKpiBaseLine() throws Exception {
		kpiMaster.setKpiBaseLine("kpiBaseLine");
	}

	@Test
	public void testSetKpiUnit() throws Exception {
		kpiMaster.setKpiUnit("kpiUnit");
	}

	@Test
	public void testSetChartType() throws Exception {
		kpiMaster.setChartType("chartType");
	}

	@Test
	public void testSetUpperThresholdBG() throws Exception {
		kpiMaster.setUpperThresholdBG("upperThresholdBG");
	}

	@Test
	public void testSetLowerThresholdBG() throws Exception {
		kpiMaster.setLowerThresholdBG("lowerThresholdBG");
	}

	@Test
	public void testSetXAxisLabel() throws Exception {
		kpiMaster.setXAxisLabel("xAxisLabel");
	}

	@Test
	public void testSetYAxisLabel() throws Exception {
		kpiMaster.setYAxisLabel("yAxisLabel");
	}

	@Test
	public void testSetShowTrend() throws Exception {
		kpiMaster.setShowTrend(true);
	}

	@Test
	public void testSetIsPositiveTrend() throws Exception {
		kpiMaster.setIsPositiveTrend(Boolean.TRUE);
	}

	@Test
	public void testSetLineLegend() throws Exception {
		kpiMaster.setLineLegend("lineLegend");
	}

	@Test
	public void testSetBarLegend() throws Exception {
		kpiMaster.setBarLegend("barLegend");
	}

	@Test
	public void testSetBoxType() throws Exception {
		kpiMaster.setBoxType("boxType");
	}

	@Test
	public void testSetCalculateMaturity() throws Exception {
		kpiMaster.setCalculateMaturity(true);
	}

	@Test
	public void testSetHideOverallFilter() throws Exception {
		kpiMaster.setHideOverallFilter(true);
	}

	@Test
	public void testSetVideoLink() throws Exception {
		kpiMaster.setVideoLink(new KPIVideoLink("kpiId", "videoUrl", true, "source"));
	}

	@Test
	public void testSetIsTrendUpOnValIncrease() throws Exception {
		kpiMaster.setIsTrendUpOnValIncrease(Boolean.TRUE);
	}

	@Test
	public void testSetKpiSource() throws Exception {
		kpiMaster.setKpiSource("kpiSource");
	}

	@Test
	public void testSetMaxValue() throws Exception {
		kpiMaster.setMaxValue("maxValue");
	}

	@Test
	public void testSetThresholdValue() throws Exception {
		kpiMaster.setThresholdValue(Double.valueOf(0));
	}

	@Test
	public void testSetKanban() throws Exception {
		kpiMaster.setKanban(Boolean.TRUE);
	}

	@Test
	public void testSetGroupId() throws Exception {
		kpiMaster.setGroupId(Integer.valueOf(0));
	}

	@Test
	public void testSetKpiInfo() throws Exception {
		kpiMaster.setKpiInfo(new KpiInfo());
	}

	@Test
	public void testSetKpiFilter() throws Exception {
		kpiMaster.setKpiFilter("kpiFilter");
	}

	@Test
	public void testSetAggregationCriteria() throws Exception {
		kpiMaster.setAggregationCriteria("aggregationCriteria");
	}

	@Test
	public void testSetAggregationCircleCriteria() throws Exception {
		kpiMaster.setAggregationCircleCriteria("aggregationCircleCriteria");
	}

	@Test
	public void testSetTrendCalculative() throws Exception {
		kpiMaster.setTrendCalculative(true);
	}

	@Test
	public void testSetTrendCalculation() throws Exception {
		kpiMaster.setTrendCalculation(Arrays.<KpiFormula>asList(new KpiFormula()));
	}

	@Test
	public void testSetAdditionalFilterSupport() throws Exception {
		kpiMaster.setAdditionalFilterSupport(true);
	}

	@Test
	public void testSetMaturityRange() throws Exception {
		kpiMaster.setMaturityRange(Arrays.<String>asList("String"));
	}

	@Test
	public void testSetKpiWidth() throws Exception {
		kpiMaster.setKpiWidth(Integer.valueOf(0));
	}

	@Test
	public void testSetMaturityLevel() throws Exception {
		kpiMaster.setMaturityLevel(Arrays.<MaturityLevel>asList(new MaturityLevel()));
	}

	@Test
	public void testSetIsRepoToolKpi() throws Exception {
		kpiMaster.setIsRepoToolKpi(Boolean.TRUE);
	}

	@Test
	public void testSetYaxisOrder() throws Exception {
		kpiMaster.setYaxisOrder(new HashMap<Integer, String>() {
			{
				put(Integer.valueOf(0), "String");
			}
		});
	}

	@Test
	public void testSetIsAggregationStacks() throws Exception {
		kpiMaster.setIsAggregationStacks(Boolean.TRUE);
	}

	@Test
	public void testBuilder() throws Exception {
		KpiMaster.KpiMasterBuilder result = KpiMaster.builder();
		Assert.assertNotNull(result);
	}

	@Test
	public void testSetId() throws Exception {
		kpiMaster.setId(null);
	}
}

// Generated with love by TestMe :) Please report issues and submit feature
// requests at: http://weirddev.com/forum#!/testme