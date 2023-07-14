/**
 *
 */
package com.publicissapient.kpidashboard.apis.jira.kanban.service;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyKanbanFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.HierachyLevelFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.data.ProjectReleaseDataFactory;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyDataKanban;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.util.KPIHelperUtil;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.ProjectRelease;
import com.publicissapient.kpidashboard.common.repository.application.ProjectReleaseRepo;

/**
 * @author swalamba
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ProjectVersionKanbanServiceImplTest {

	@InjectMocks
	private ProjectVersionKanbanServiceImpl projectVersionKanbanService;

	@Mock
	private ProjectReleaseRepo projectReleaseRepo;

	@Mock
	private CustomApiConfig customApiConfig;

	@Mock
	private ConfigHelperService configHelperService;

	private KpiRequest kpiRequest;
	private List<ProjectRelease> releaseList;
	private List<AccountHierarchyDataKanban> accountHierarchyKanbanDataList;
	@Mock
	private CacheService cacheService;

	@Before
	public void setUp() throws Exception {
		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance("/json/default/kanban_kpi_request.json");
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi74");
		kpiRequest.setLabel("PROJECT");
		kpiRequest.setDuration("MONTHS");

		AccountHierarchyKanbanFilterDataFactory accountHierarchyKanbanFilterDataFactory = AccountHierarchyKanbanFilterDataFactory
				.newInstance();
		accountHierarchyKanbanDataList = accountHierarchyKanbanFilterDataFactory.getAccountHierarchyKanbanDataList();

		ProjectReleaseDataFactory projectReleaseDataFactory = ProjectReleaseDataFactory.newInstance();
		releaseList = projectReleaseDataFactory.findByBasicProjectConfigId("6335368249794a18e8a4479f");
		Mockito.when(cacheService.getFromApplicationCache(Mockito.anyString())).thenReturn("Excel-trackerid");
		when(customApiConfig.getJiraXaxisMonthCount()).thenReturn(5);
	}

	@Test
	public void testGetQualifierType() {
		assertTrue(projectVersionKanbanService.getQualifierType().equals(KPICode.PROJECT_RELEASES_KANBAN.name()));
	}

	@Test
	public void testGetKpiData() throws ApplicationException {
		Mockito.when(projectReleaseRepo.findByConfigIdIn(any())).thenReturn(releaseList);
		HierachyLevelFactory hierachyLevelFactory = HierachyLevelFactory.newInstance();
		when(cacheService.getFullKanbanHierarchyLevel()).thenReturn(hierachyLevelFactory.getHierarchyLevels());
		TreeAggregatorDetail treeAggregatorDetail = KPIHelperUtil.getTreeLeafNodesGroupedByFilter(kpiRequest,
				new ArrayList<>(), accountHierarchyKanbanDataList, "hierarchyLevelOne", 4);

		KpiElement ele = projectVersionKanbanService.getKpiData(kpiRequest, kpiRequest.getKpiList().get(0),
				treeAggregatorDetail);
		assertTrue(((List<DataCount>) ele.getTrendValueList()).size() == 1);
	}

	@Test
	public void testFetchKPIDataFromDbNoFilterData() {
		List<Node> leafNodeList = new ArrayList<>();
		Mockito.when(projectReleaseRepo.findByConfigIdIn(any())).thenReturn(null);
		assertNull(projectVersionKanbanService.fetchKPIDataFromDb(leafNodeList, null, null, null)
				.get("projectReleaseDetail"));
	}

}
