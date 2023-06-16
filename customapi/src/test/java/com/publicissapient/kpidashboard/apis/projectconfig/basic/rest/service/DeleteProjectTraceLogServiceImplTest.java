package com.publicissapient.kpidashboard.apis.projectconfig.basic.rest.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.publicissapient.kpidashboard.apis.auth.service.AuthenticationService;
import com.publicissapient.kpidashboard.apis.projectconfig.basic.service.DeleteProjectTraceLogServiceImpl;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.tracelog.DeleteProjectTraceLog;
import com.publicissapient.kpidashboard.common.repository.tracelog.DeleteProjectTraceLogRepository;

@RunWith(MockitoJUnitRunner.class)
public class DeleteProjectTraceLogServiceImplTest {

	@InjectMocks
	DeleteProjectTraceLogServiceImpl service;

	@Mock
	DeleteProjectTraceLogRepository deleteProjectTraceLogRepository;

	@Mock
	Authentication authentication;

	@Mock
	SecurityContext securityContext;

	@Mock
	private AuthenticationService authenticationService;

	@Captor
	private ArgumentCaptor<DeleteProjectTraceLog> captor;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		SecurityContextHolder.setContext(securityContext);
	}

	@Test
	public void validateSave() {
		when(authenticationService.getLoggedInUser()).thenReturn("SUPERADMIN");
		service.save(new ProjectBasicConfig());
		verify(deleteProjectTraceLogRepository, times(1)).save(captor.capture());
	}
}
