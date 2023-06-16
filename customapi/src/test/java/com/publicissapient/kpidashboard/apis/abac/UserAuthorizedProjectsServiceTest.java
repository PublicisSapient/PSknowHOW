package com.publicissapient.kpidashboard.apis.abac;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.publicissapient.kpidashboard.apis.auth.service.AuthenticationService;
import com.publicissapient.kpidashboard.apis.auth.token.TokenAuthenticationService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.common.service.impl.UserInfoServiceImpl;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchiesDataFactory;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchiesKanbanDataFactory;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyKanbanFilterDataFactory;
import com.publicissapient.kpidashboard.apis.data.HierachyLevelFactory;
import com.publicissapient.kpidashboard.apis.data.KpiRequestFactory;
import com.publicissapient.kpidashboard.apis.data.UserInfoDataFactory;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.application.KanbanAccountHierarchyRepository;

@RunWith(MockitoJUnitRunner.class)
public class UserAuthorizedProjectsServiceTest {

	@Mock
	TokenAuthenticationService tokenAuthenticationService;
	@Mock
	Authentication authentication;
	@InjectMocks
	UserAuthorizedProjectsService userAuthorizedProjectsService;
	KpiRequest kpiRequest;
	KpiElement kpiElement;
	@Mock
	private UserInfoServiceImpl userInfoService;
	@Mock
	private AccountHierarchyRepository accountHierarchyRepo;
	@Mock
	private KanbanAccountHierarchyRepository kanbanAccountHierarchyRepository;
	@Mock
	private AuthenticationService authenticationService;
	@Mock
	private CacheService cacheService;
	private UserInfoDataFactory userInfoDataFactory = null;
	private AccountHierarchyFilterDataFactory filterDataFactory = null;
	private AccountHierarchiesDataFactory hierarchyFactory = null;
	private AccountHierarchyKanbanFilterDataFactory kanbanFilterDataFactory = null;
	private AccountHierarchiesKanbanDataFactory kanbanDataFactory = null;
	private List<HierarchyLevel> hierarchyLevels = null;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		SecurityContext securityContext = mock(SecurityContext.class);

		KpiRequestFactory kpiRequestFactory = KpiRequestFactory.newInstance("/json/default/kanban_kpi_request.json");
		kpiRequest = kpiRequestFactory.findKpiRequest("kpi51");
		kpiRequest.setLabel("PROJECT");
		kpiElement = kpiRequest.getKpiList().get(0);

		SecurityContextHolder.setContext(securityContext);
		userInfoDataFactory = UserInfoDataFactory.newInstance();
		filterDataFactory = AccountHierarchyFilterDataFactory.newInstance();
		hierarchyFactory = AccountHierarchiesDataFactory.newInstance();
		kanbanFilterDataFactory = AccountHierarchyKanbanFilterDataFactory.newInstance();
		kanbanDataFactory = AccountHierarchiesKanbanDataFactory.newInstance();

		HierachyLevelFactory hierachyLevelFactory = HierachyLevelFactory.newInstance();
		hierarchyLevels = hierachyLevelFactory.getHierarchyLevels();

	}

	@Test
	public void ifSuperAdminUser() {
		UserInfo userInfo = userInfoDataFactory.getUserInfoByRole("ROLE_SUPERADMIN");
		when(authenticationService.getLoggedInUser()).thenReturn("SUPERADMIN");
		when(userInfoService.getUserInfo(anyString())).thenReturn(userInfo);
		Assertions.assertTrue(userAuthorizedProjectsService.ifSuperAdminUser());
	}

	@Test
	public void ifNotSuperAdminUser() {
		UserInfo userInfo = userInfoDataFactory.getUserInfoByRole("ROLE_VIEWER");
		when(authenticationService.getLoggedInUser()).thenReturn("ROLE_VIEWER");
		when(userInfoService.getUserInfo(anyString())).thenReturn(userInfo);
		Assertions.assertFalse(userAuthorizedProjectsService.ifSuperAdminUser());
	}

	@Test
	public void checkUserAuthForProjects() {
		List<AccountHierarchy> accountHierarchyList = hierarchyFactory.getAccountHierarchies();
		Set<String> projectList = new HashSet<>();
		projectList.add("5fd9ab0995fe13000165d0ba");
		when(tokenAuthenticationService.getUserProjects()).thenReturn(projectList);
		when(accountHierarchyRepo.findAll()).thenReturn(accountHierarchyList);
		Assertions.assertTrue(!userAuthorizedProjectsService
				.checkUserAuthForProjects(filterDataFactory.getAccountHierarchyDataList()));
	}

	@Test
	public void filterProjects() {
		Set<String> projectList = new HashSet<>();
		projectList.add("5fd9ab0995fe13000165d0ba");
		when(tokenAuthenticationService.getUserProjects()).thenReturn(projectList);
		userAuthorizedProjectsService.filterProjects(filterDataFactory.getAccountHierarchyDataList());
	}

	@Test
	public void checkKanbanUserAuthForProjects() {
		Set<String> projectList = new HashSet<>();
		projectList.add("60dabc03e17b2269cc76d13c");
		when(tokenAuthenticationService.getUserProjects()).thenReturn(projectList);
		userAuthorizedProjectsService
				.checkKanbanUserAuthForProjects(kanbanFilterDataFactory.getAccountHierarchyKanbanDataList());
	}

	@Test
	public void filterKanbanProjects() {
		Set<String> projectList = new HashSet<>();
		projectList.add("63330b7068b5d05cf59c4386");
		when(tokenAuthenticationService.getUserProjects()).thenReturn(projectList);
		userAuthorizedProjectsService.filterKanbanProjects(kanbanFilterDataFactory.getAccountHierarchyKanbanDataList());

	}

	@Test
	public void getKanbanProjectKey() {
		Set<String> projectList = new HashSet<>();
		projectList.add("63330b7068b5d05cf59c4386");
		userAuthorizedProjectsService.getKanbanProjectKey(kanbanFilterDataFactory.getAccountHierarchyKanbanDataList(),
				kpiRequest);

	}

	@Test
	public void getProjectKey() {
		Set<String> projectList = new HashSet<>();
		projectList.add("63330b7068b5d05cf59c4386");
		userAuthorizedProjectsService.getProjectKey(filterDataFactory.getAccountHierarchyDataList(), kpiRequest);

	}

}