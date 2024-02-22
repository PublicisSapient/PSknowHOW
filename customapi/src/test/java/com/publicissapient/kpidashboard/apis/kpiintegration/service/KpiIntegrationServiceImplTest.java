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

package com.publicissapient.kpidashboard.apis.kpiintegration.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.data.HierachyLevelFactory;
import com.publicissapient.kpidashboard.apis.jenkins.service.JenkinsServiceR;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.data.KpiMasterDataFactory;
import com.publicissapient.kpidashboard.apis.errors.EntityNotFoundException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraServiceR;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.sonar.service.SonarServiceR;
import com.publicissapient.kpidashboard.apis.zephyr.service.ZephyrService;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataCountGroup;
import com.publicissapient.kpidashboard.common.repository.application.KpiMasterRepository;

/**
 * @author kunkambl
 */
@RunWith(MockitoJUnitRunner.class)
public class KpiIntegrationServiceImplTest {

	@InjectMocks
	KpiIntegrationServiceImpl maturityService;

	@Mock
	KpiMasterRepository kpiMasterRepository;

	@Mock
	private JiraServiceR jiraService;

	@Mock
	private SonarServiceR sonarService;

	@Mock
	private ZephyrService zephyrService;

	@Mock
	private JenkinsServiceR jenkinsService;

	@Mock
	private HierarchyLevelService hierarchyLevelService;

	@Mock
	private CacheService cacheService;

	private KpiRequest kpiRequest;
	private KpiElement kpiElement1;
	private KpiElement kpiElement2;
	private KpiMasterDataFactory kpiMasterDataFactory = KpiMasterDataFactory.newInstance();
	private List<String> kpiIdList = Arrays.asList("kpi14", "kpi70", "kpi27", "kpi8");
	private List<HierarchyLevel> hierarchyLevels = null;

	@Before
	public void setup() {
		kpiRequest = new KpiRequest();
		kpiRequest.setKpiIdList(kpiIdList);
		kpiRequest.setHierarchyName("DTS");
		kpiRequest.setHierarchyId("535");
		kpiRequest.setLevel(4);
		DataCount dataCount = new DataCount();
		dataCount.setMaturity("1");
		dataCount.setMaturityValue("35");
		kpiElement1 = new KpiElement();
		kpiElement1.setTrendValueList(Arrays.asList(dataCount));
		DataCountGroup dataCountGroup = new DataCountGroup();
		dataCountGroup.setFilter("Overall");
		dataCountGroup.setValue(Arrays.asList(dataCount));
		kpiElement2 = new KpiElement();
		kpiElement2.setTrendValueList(Arrays.asList(dataCountGroup));

		HierachyLevelFactory hierachyLevelFactory = HierachyLevelFactory.newInstance();
		hierarchyLevels = hierachyLevelFactory.getHierarchyLevels();
		when(hierarchyLevelService.getFullHierarchyLevels(true)).thenReturn(hierarchyLevels);
	}

	@Test
    public void getMaturityValuesTestSuccess() throws EntityNotFoundException {
        when(kpiMasterRepository.findByKpiIdIn(kpiIdList)).thenReturn(kpiMasterDataFactory.getSpecificKpis(kpiIdList));
        when(jiraService.processWithExposedApiToken(kpiRequest)).thenReturn(Arrays.asList(kpiElement1));
        when(sonarService.processWithExposedApiToken(kpiRequest)).thenReturn(Arrays.asList(kpiElement2));
        when(zephyrService.processWithExposedApiToken(kpiRequest)).thenReturn(Arrays.asList(kpiElement2));
		when(jenkinsService.processWithExposedApiToken(kpiRequest)).thenReturn(Arrays.asList(kpiElement2));
        List<KpiElement> kpiElementList = maturityService.getKpiResponses(kpiRequest);
        assertEquals(4, kpiElementList.size());
    }

	@Test
	public void getMaturityValuesTestEmpty() {
		kpiIdList = Arrays.asList("kpi84");
		kpiRequest.setKpiIdList(kpiIdList);
		when(kpiMasterRepository.findByKpiIdIn(kpiIdList)).thenReturn(kpiMasterDataFactory.getSpecificKpis(kpiIdList));
		List<KpiElement> kpiElementList = maturityService.getKpiResponses(kpiRequest);
		assertEquals(0, kpiElementList.size());
	}
}