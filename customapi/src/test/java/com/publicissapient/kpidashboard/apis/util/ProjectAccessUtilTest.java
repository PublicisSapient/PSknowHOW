package com.publicissapient.kpidashboard.apis.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.auth.service.AuthenticationService;
import com.publicissapient.kpidashboard.apis.auth.token.TokenAuthenticationService;
import com.publicissapient.kpidashboard.apis.common.service.impl.UserInfoServiceImpl;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProjectAccessUtilTest {
	@InjectMocks
	ProjectAccessUtil projectAccessUtil;

	@Mock
	private TokenAuthenticationService tokenAuthenticationService;
	@Mock
	private UserAuthorizedProjectsService userAuthorizedProjectsService;
	@Mock
	private UserInfoServiceImpl userInfoService;
	@Mock
	private AuthenticationService authenticationService;

	@Before
	public void setUp() {
	}

	@Test
	public void testConfigIdHasUserAccess_WhenUserIsSuperAdmin() {
		when(userAuthorizedProjectsService.ifSuperAdminUser()).thenReturn(true);
		boolean result = projectAccessUtil.configIdHasUserAccess("configId123");
		assertTrue(result);
		verify(userAuthorizedProjectsService).ifSuperAdminUser();
	}

	@Test
	public void testConfigIdHasUserAccess_WhenUserHasAccessToConfigId() {
		Set<String> userProjects = Set.of("configId123", "configId456");
		when(userAuthorizedProjectsService.ifSuperAdminUser()).thenReturn(false);
		when(tokenAuthenticationService.getUserProjects()).thenReturn(userProjects);

		boolean result = projectAccessUtil.configIdHasUserAccess("configId123");

		assertTrue(result);
		verify(userAuthorizedProjectsService).ifSuperAdminUser();
		verify(tokenAuthenticationService).getUserProjects();
	}

	@Test
	public void testConfigIdHasUserAccess_WhenUserDoesNotHaveAccessToConfigId() {
		Set<String> userProjects = Set.of("configId456", "configId789");
		when(userAuthorizedProjectsService.ifSuperAdminUser()).thenReturn(false);
		when(tokenAuthenticationService.getUserProjects()).thenReturn(userProjects);

		boolean result = projectAccessUtil.configIdHasUserAccess("configId123");

		assertFalse(result);
		verify(userAuthorizedProjectsService).ifSuperAdminUser();
		verify(tokenAuthenticationService).getUserProjects();
	}

	@Test
	public void testConfigIdHasUserAccess_WhenUserProjectsAreNull() {
		when(userAuthorizedProjectsService.ifSuperAdminUser()).thenReturn(false);
		when(tokenAuthenticationService.getUserProjects()).thenReturn(null);
		boolean result = projectAccessUtil.configIdHasUserAccess("configId123");
		assertFalse(result);
		verify(userAuthorizedProjectsService).ifSuperAdminUser();
		verify(tokenAuthenticationService).getUserProjects();
	}

	@Test
	public void testIfConnectionNotAccessible_WhenConnectionIsShared() {
		Connection connection = mock(Connection.class);
		when(connection.isSharedConnection()).thenReturn(true);

		boolean result = projectAccessUtil.ifConnectionNotAccessible(connection);

		assertFalse(result);
		verify(connection).isSharedConnection();
		verifyNoInteractions(authenticationService, userAuthorizedProjectsService);
	}

	@Test
	public void testIfConnectionNotAccessible_WhenConnectionCreatedByLoggedInUser() {
		Connection connection = mock(Connection.class);
		when(connection.isSharedConnection()).thenReturn(false);
		when(connection.getCreatedBy()).thenReturn("user123");
		when(authenticationService.getLoggedInUser()).thenReturn("user123");

		boolean result = projectAccessUtil.ifConnectionNotAccessible(connection);

		assertFalse(result);
		verify(connection).isSharedConnection();
		verify(connection).getCreatedBy();
		verify(authenticationService).getLoggedInUser();
		verifyNoInteractions(userAuthorizedProjectsService);
	}

	@Test
	public void testIfConnectionNotAccessible_WhenUserIsSuperAdmin() {
		Connection connection = mock(Connection.class);
		when(connection.isSharedConnection()).thenReturn(false);
		when(connection.getCreatedBy()).thenReturn("user123");
		when(authenticationService.getLoggedInUser()).thenReturn("user456");
		when(userAuthorizedProjectsService.ifSuperAdminUser()).thenReturn(true);

		boolean result = projectAccessUtil.ifConnectionNotAccessible(connection);

		assertFalse(result);
		verify(connection).isSharedConnection();
		verify(connection).getCreatedBy();
		verify(authenticationService).getLoggedInUser();
		verify(userAuthorizedProjectsService).ifSuperAdminUser();
	}

	@Test
	public void testIfConnectionNotAccessible_WhenConnectionNotAccessible() {
		Connection connection = mock(Connection.class);
		when(connection.isSharedConnection()).thenReturn(false);
		when(connection.getCreatedBy()).thenReturn("user123");
		when(authenticationService.getLoggedInUser()).thenReturn("user456");
		when(userAuthorizedProjectsService.ifSuperAdminUser()).thenReturn(false);

		boolean result = projectAccessUtil.ifConnectionNotAccessible(connection);

		assertTrue(result);
		verify(connection).isSharedConnection();
		verify(connection).getCreatedBy();
		verify(authenticationService).getLoggedInUser();
		verify(userAuthorizedProjectsService).ifSuperAdminUser();
	}
}