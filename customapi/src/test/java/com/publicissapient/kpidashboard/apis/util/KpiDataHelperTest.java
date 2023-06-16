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

package com.publicissapient.kpidashboard.apis.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.data.AdditionalFilterCategoryFactory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterCategory;

/**
 * @author anisingh4
 */
@RunWith(MockitoJUnitRunner.class)
public class KpiDataHelperTest {

	@Mock
	FilterHelperService flterHelperService;
	@Mock
	private CacheService cacheService;
	private Map<String, AdditionalFilterCategory> additonalFilterMap;

	@Before
	public void setUp() {
		AdditionalFilterCategoryFactory additionalFilterCategoryFactory = AdditionalFilterCategoryFactory.newInstance();
		List<AdditionalFilterCategory> additionalFilterCategoryList = additionalFilterCategoryFactory
				.getAdditionalFilterCategoryList();
		additonalFilterMap = additionalFilterCategoryList.stream()
				.collect(Collectors.toMap(AdditionalFilterCategory::getFilterCategoryId, x -> x));
		when(flterHelperService.getAdditionalFilterHierarchyLevel()).thenReturn(additonalFilterMap);

	}

	@Test
	public void createAdditionalFilterMap_SPRINT() {

		KpiRequest kpiRequest = createKpiRequest();
		Map<String, List<String>> selectedMap = new HashMap<>();
		selectedMap.put(Constant.SPRINT, Arrays.asList("Test"));

		kpiRequest.setSelectedMap(selectedMap);

		Map<String, List<String>> mapOfFilters = new HashMap<>();

		String actual = KpiDataHelper.createAdditionalFilterMap(kpiRequest, mapOfFilters, Constant.SCRUM,
				Constant.SPRINT, flterHelperService);
		assertEquals(Constant.SPRINT, actual);
	}

	private KpiRequest createKpiRequest() {
		KpiRequest kpiRequest = new KpiRequest();
		List<KpiElement> kpiList = new ArrayList<>();
		KpiElement kpiElement = new KpiElement();
		kpiElement.setKpiId(KPICode.DEFECT_COUNT_BY_PRIORITY.getKpiId());
		kpiElement.setKpiName("Defect Count");
		kpiElement.setKpiCategory("Quality");
		kpiElement.setKpiUnit("%");
		kpiElement.setKpiSource("Jira");

		kpiElement.setMaxValue("500");
		kpiElement.setChartType("gaugeChart");
		kpiList.add(kpiElement);
		kpiRequest.setLevel(2);
		kpiRequest.setIds(new String[] { "Alpha_Tower_Id" }); // This is
		// immaterial as
		// all the sprint
		// in Account
		// Hierarchy List created above is sent for processing.
		kpiRequest.setKpiList(kpiList);
		kpiRequest.setRequestTrackerId();
		Map<String, List<String>> selectedMap = new HashMap<>();
		selectedMap.put("ACCOUNT", Arrays.asList("Speedy"));
		kpiRequest.setSelectedMap(selectedMap);
		return kpiRequest;
	}

}