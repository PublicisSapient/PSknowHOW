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
package com.publicissapient.kpidashboard.apis.pushdata.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.modelmapper.ModelMapper;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.data.PushDataFactory;
import com.publicissapient.kpidashboard.apis.pushdata.model.PushBuildDeploy;
import com.publicissapient.kpidashboard.apis.pushdata.model.dto.PushBuildDeployDTO;
import com.publicissapient.kpidashboard.apis.pushdata.service.PushDataTraceLogService;
import com.publicissapient.kpidashboard.apis.pushdata.util.PushDataException;

@RunWith(MockitoJUnitRunner.class)
public class PushBuildServiceImplTest {

	@InjectMocks
	private PushBuildServiceImpl pushBuildService;

	@Mock
	private BuildServiceImpl buildService;

	@Mock
	private DeployServiceImpl deployService;

	@Mock
	private CustomApiConfig customApiConfig;

	@Mock
	private PushDataTraceLogService pushDataTraceLogService;

	private PushBuildDeploy pushBuildDeploy;
	private ObjectId projectBasicConfigId;

	private Validator validator;

	@Before
	public void setUp() {
		projectBasicConfigId = new ObjectId("632824e949794a18e8a44787");
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
		Set<ConstraintViolation<PushBuildDeployDTO>> validate = validator
				.validate(PushDataFactory.newInstance().getPushBuildDeploy().get(0));
		if (validate.isEmpty()) {
			pushBuildDeploy = new ModelMapper().map(PushDataFactory.newInstance().getPushBuildDeploy().get(0),
					PushBuildDeploy.class);
		}
	}

	@Test
	public void unsucessfullInsert() {
		when(customApiConfig.getPushDataLimit()).thenReturn(51);
		when(buildService.checkandCreateBuilds(any(), anySet(), anyList(), anyList(), anyList())).thenReturn(2);
		when(deployService.checkandCreateDeployment(any(), anySet(), anyList(), anyList(), anyList())).thenReturn(1);
		doThrow(new PushDataException()).when(pushDataTraceLogService).setExceptionTraceLog(anyString(),
				any(Object.class));
		Assert.assertThrows(PushDataException.class, () -> {
			pushBuildService.processPushDataInput(pushBuildDeploy, projectBasicConfigId);
		});
	}

	@Test
	public void checkSizeFalse() {
		when(customApiConfig.getPushDataLimit()).thenReturn(1);
		doThrow(new PushDataException()).when(pushDataTraceLogService).setExceptionTraceLog(anyString(), isNull());
		Assert.assertThrows(PushDataException.class, () -> {
			pushBuildService.getTotalRecords(pushBuildDeploy);
		});
	}

	@Test
	public void sucessfullInsert() {
		when(customApiConfig.getPushDataLimit()).thenReturn(51);
		when(buildService.checkandCreateBuilds(any(), anySet(), anyList(), anyList(), anyList())).thenReturn(2);
		when(deployService.checkandCreateDeployment(any(), anySet(), anyList(), anyList(), anyList())).thenReturn(1);
		doThrow(new PushDataException()).when(pushDataTraceLogService).setExceptionTraceLog(anyString(),
				any(Object.class));
		Assert.assertThrows(PushDataException.class, () -> {
			pushBuildService.processPushDataInput(pushBuildDeploy, projectBasicConfigId);
		});
	}

}
