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

package com.publicissapient.kpidashboard.apis.common.rest;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.apis.common.service.CacheService;

/**
 * @author anisingh4
 */
@RunWith(MockitoJUnitRunner.class)
public class CacheControllerTest {

	@InjectMocks
	CacheController cacheController;

	@Mock
	CacheService cacheService;

	@Test
	public void clearCache() {
		doNothing().when(cacheService).clearCache(Mockito.anyString());
		cacheController.clearCache("abc");
		verify(cacheService, times(1)).clearCache(Mockito.anyString());

	}

	@Test
	public void clearAllCache() {
		doNothing().when(cacheService).clearAllCache();
		cacheController.clearAllCache();
		verify(cacheService, times(1)).clearAllCache();
	}
}