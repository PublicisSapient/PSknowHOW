/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.apis.appsetting.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.common.service.impl.ConfigDetailsServiceImpl;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.model.ConfigDetails;
import com.publicissapient.kpidashboard.apis.model.DateRangeFilter;

@RunWith(MockitoJUnitRunner.class)
public class ConfigDetailsServiceImplTest {

	private static final String PERCENTILE = "percentile";
	private static final String SUM = "sum";
	@Mock
	ConfigHelperService configHelperService;
	@InjectMocks
	private ConfigDetailsServiceImpl configDetailsServiceImpl;
	@Mock
	private CustomApiConfig customApiConfig;
	private Map<String, String> kpiWiseAggregation;
	private DateRangeFilter dateRangeFilter;

	@Before
	public void setUp() {
		// set aggregation criteria kpi wise
		kpiWiseAggregation = new HashMap<>();
		kpiWiseAggregation.put("defectRemovalEfficiency", PERCENTILE);
		kpiWiseAggregation.put("cycleTime", "median");
		kpiWiseAggregation.put("codeCommit", PERCENTILE);
		kpiWiseAggregation.put("defectSeepageRate", PERCENTILE);
		kpiWiseAggregation.put("regressionAutomation", PERCENTILE);
		kpiWiseAggregation.put("automationPercentage", PERCENTILE);
		kpiWiseAggregation.put("sprintVelocity", SUM);
		kpiWiseAggregation.put("sonar", PERCENTILE);
		kpiWiseAggregation.put("defectRCA", SUM);
		kpiWiseAggregation.put("defectRejectionRate", PERCENTILE);
		kpiWiseAggregation.put("defectCountByPriority", SUM);
		kpiWiseAggregation.put("sprintPredictability", PERCENTILE);
		kpiWiseAggregation.put("jiraTechDebt", PERCENTILE);
		kpiWiseAggregation.put("codeBuildTime", PERCENTILE);
		kpiWiseAggregation.put("defectInjectionRate", PERCENTILE);
		kpiWiseAggregation.put("storyCount", SUM);
		dateRangeFilter = new DateRangeFilter(Arrays.asList("Weeks", "Days", "Months"), Arrays.asList(5, 10, 15));
	}

	@After
	public void after() {
	}

	@Test
	public void testConfigDetails() throws Exception {
		Mockito.when(configHelperService.calculateCriteria()).thenReturn(kpiWiseAggregation);
		Mockito.when(customApiConfig.getPercentileValue()).thenReturn(90d);
		Mockito.when(customApiConfig.getHierarchySelectionCount()).thenReturn(3);
		Mockito.when(customApiConfig.getDateRangeFilterCounts()).thenReturn(Arrays.asList(5, 10, 15));
		Mockito.when(customApiConfig.getDateRangeFilterTypes()).thenReturn(Arrays.asList("Days", "Weeks", "Months"));
		Mockito.when(customApiConfig.getIsRepoToolEnable()).thenReturn(false);

		ConfigDetails configDetail = getConfigDetailsObject();
		ConfigDetails configDetails = configDetailsServiceImpl.getConfigDetails();
		Assert.assertEquals(configDetail.getKpiWiseAggregationType(), configDetails.getKpiWiseAggregationType());
	}

	private ConfigDetails getConfigDetailsObject() {
		ConfigDetails configDetails = new ConfigDetails();
		configDetails.setHierarchySelectionCount(3);
		configDetails.setKpiWiseAggregationType(kpiWiseAggregation);
		configDetails.setPercentile(90d);
		configDetails.setDateRangeFilter(dateRangeFilter);
		configDetails.setRepoToolFlag(false);
		return configDetails;
	}

}
