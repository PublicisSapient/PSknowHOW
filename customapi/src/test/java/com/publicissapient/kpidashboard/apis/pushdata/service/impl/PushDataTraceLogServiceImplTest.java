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
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import com.publicissapient.kpidashboard.apis.data.PushDataTraceLogFactory;
import com.publicissapient.kpidashboard.apis.pushdata.model.PushDataResponse;
import com.publicissapient.kpidashboard.apis.pushdata.model.PushDataTraceLog;
import com.publicissapient.kpidashboard.apis.pushdata.repository.PushDataTraceLogRepository;
import com.publicissapient.kpidashboard.apis.pushdata.util.PushDataException;

@RunWith(MockitoJUnitRunner.class)
public class PushDataTraceLogServiceImplTest {

	List<PushDataTraceLog> pushBuildDeploy;
	@InjectMocks
	private PushDataTraceLogServiceImpl pushDataTraceLogService;
	@Mock
	private PushDataTraceLogRepository pushDataTraceLogRepository;

	@Before
	public void setup() {
		PushDataTraceLogFactory pushDataTraceLogFactory = PushDataTraceLogFactory.newInstance();
		pushBuildDeploy = pushDataTraceLogFactory.getPushDataTraceLog();
	}

	@Test
	public void save() {
		PushDataTraceLog pushDataTraceLog = new PushDataTraceLog();
		pushDataTraceLogService.save(pushDataTraceLog);
	}

	@Test
	public void getByProjectConfigId() {
		when(pushDataTraceLogRepository.findByBasicProjectConfigId(any())).thenReturn(pushBuildDeploy);
		Assert.assertNotNull(pushDataTraceLogService.getByProjectConfigId(new ObjectId("6360fefc3fa9e175755f0728")));
	}

	@Test
	public void getByProjectConfigIdNull() {
		when(pushDataTraceLogRepository.findByBasicProjectConfigId(any())).thenReturn(new ArrayList<>());
		Assert.assertEquals(pushDataTraceLogService.getByProjectConfigId(new ObjectId("6360fefc3fa9e175755f0728")),new ArrayList<>());
	}

	@Test
	public void setTraceLog() {
		Assert.assertThrows(PushDataException.class, () -> {
			pushDataTraceLogService.setExceptionTraceLog("", HttpStatus.BAD_REQUEST);
			pushDataTraceLogService.setExceptionTraceLog("", new PushDataResponse());
		});
	}

	@Test
	public void setTraceLogPushData() {
		Assert.assertThrows(PushDataException.class, () -> {
			pushDataTraceLogService.setExceptionTraceLog("", new PushDataResponse());
		});
	}

}
