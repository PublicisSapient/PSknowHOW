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
		MockitoAnnotations.openMocks(this);
		SecurityContextHolder.setContext(securityContext);
	}

	@Test
	public void validateSave() {
		when(authenticationService.getLoggedInUser()).thenReturn("SUPERADMIN");
		service.save(new ProjectBasicConfig());
		verify(deleteProjectTraceLogRepository, times(1)).save(captor.capture());
	}
}
