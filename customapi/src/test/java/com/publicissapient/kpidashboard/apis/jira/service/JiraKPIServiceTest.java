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

package com.publicissapient.kpidashboard.apis.jira.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;

/**
 * @author anisingh4
 */
@ExtendWith(SpringExtension.class)
public class JiraKPIServiceTest {

	@InjectMocks
	JiraKpiServiceTestImpl jiraKPIService;

	@Mock
	private CustomApiConfig customApiConfig;

	private Map<String, String> aggregationCriteriaMap;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);

		aggregationCriteriaMap = new HashMap<>();
		aggregationCriteriaMap.put("kpi1", Constant.PERCENTILE);
		aggregationCriteriaMap.put("kpi2", Constant.MEDIAN);
		aggregationCriteriaMap.put("kpi3", Constant.AVERAGE);
		aggregationCriteriaMap.put("kpi4", Constant.SUM);
	}

	private List<Map<String, Long>> createAggregationInputData1() {
		List<Map<String, Long>> aggregatedValueList = new ArrayList<>();
		Map<String, Long> aggregatedValuesMap1 = new HashMap<>();
		aggregatedValuesMap1.put("Bug", 1L);
		Map<String, Long> aggregatedValuesMap2 = new HashMap<>();
		aggregatedValuesMap2.put("Bug", 4L);
		Map<String, Long> aggregatedValuesMap3 = new HashMap<>();
		aggregatedValuesMap3.put("Bug", 3L);
		Map<String, Long> aggregatedValuesMap4 = new HashMap<>();
		aggregatedValuesMap4.put("Bug", 0L);
		Map<String, Long> aggregatedValuesMap5 = new HashMap<>();
		aggregatedValuesMap5.put("Bug", 2L);
		Map<String, Long> aggregatedValuesMap6 = new HashMap<>();
		aggregatedValuesMap5.put("Bug", 6L);

		aggregatedValueList.add(aggregatedValuesMap1);
		aggregatedValueList.add(aggregatedValuesMap2);
		aggregatedValueList.add(aggregatedValuesMap3);
		aggregatedValueList.add(aggregatedValuesMap4);
		aggregatedValueList.add(aggregatedValuesMap5);
		aggregatedValueList.add(aggregatedValuesMap6);
		return aggregatedValueList;
	}

	public static class JiraKpiServiceTestImpl extends JiraKPIService {

		@Override
		public String getQualifierType() {
			return null;
		}

		@Override
		public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
				TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
			return null;
		}

		@Override
		public Object calculateKPIMetrics(Object o) {
			return null;
		}

		@Override
		public Object fetchKPIDataFromDb(List leafNodeList, String startDate, String endDate, KpiRequest kpiRequest) {
			return null;
		}

	}
}