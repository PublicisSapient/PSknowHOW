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

package com.publicissapient.kpidashboard.jira.cache;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;

@RunWith(MockitoJUnitRunner.class)
public class CacheClearingMechanismTest {

	@InjectMocks
	private CacheClearingMechanism cacheClearingMechanism;

	@Mock
	private JiraProcessorCacheEvictor jiraProcessorCacheEvictor;

	@Test
	public void testSignalJobCompletion_ClearCacheWhenJobCountIsZero() {
		int jobCount = 0;
		cacheClearingMechanism.setJobCount(jobCount);
		cacheClearingMechanism.signalJobCompletion();

		verify(jiraProcessorCacheEvictor, times(1)).evictCache(CommonConstant.CACHE_CLEAR_ENDPOINT,
				CommonConstant.CACHE_ACCOUNT_HIERARCHY);
		verify(jiraProcessorCacheEvictor, times(1)).evictCache(CommonConstant.CACHE_CLEAR_ENDPOINT,
				CommonConstant.JIRA_KPI_CACHE);
	}

	@Test
	public void testSignalJobCompletion_DoesNotClearCacheWhenJobCountIsNotZero() {
		int jobCount = 2;
		cacheClearingMechanism.setJobCount(jobCount);
		cacheClearingMechanism.signalJobCompletion();

		verify(jiraProcessorCacheEvictor, times(0)).evictCache(CommonConstant.CACHE_CLEAR_ENDPOINT,
				CommonConstant.CACHE_ACCOUNT_HIERARCHY);
		verify(jiraProcessorCacheEvictor, times(0)).evictCache(CommonConstant.CACHE_CLEAR_ENDPOINT,
				CommonConstant.JIRA_KPI_CACHE);
	}

	@Test
	public void testSignalJobCompletion_ClearCacheWhenJobCountBecomesZero() {
		int jobCount = 3;
		cacheClearingMechanism.setJobCount(jobCount);
		cacheClearingMechanism.signalJobCompletion(); // Job 1 completed
		cacheClearingMechanism.signalJobCompletion(); // Job 2 completed
		cacheClearingMechanism.signalJobCompletion(); // Job 3 completed, now cache should be cleared

		verify(jiraProcessorCacheEvictor, times(1)).evictCache(CommonConstant.CACHE_CLEAR_ENDPOINT,
				CommonConstant.CACHE_ACCOUNT_HIERARCHY);
		verify(jiraProcessorCacheEvictor, times(1)).evictCache(CommonConstant.CACHE_CLEAR_ENDPOINT,
				CommonConstant.JIRA_KPI_CACHE);
	}
}
