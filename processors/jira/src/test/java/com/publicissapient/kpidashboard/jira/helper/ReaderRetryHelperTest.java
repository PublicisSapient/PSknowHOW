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

package com.publicissapient.kpidashboard.jira.helper;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReaderRetryHelperTest {

	ReaderRetryHelper.RetryableOperation<Double> operation = () -> Math.sqrt(49);

	@InjectMocks
	ReaderRetryHelper readerRetryHelper;

	@Test
	public void executeWithRetryTest() throws Exception {
		assertEquals(Double.valueOf(7.0), readerRetryHelper.executeWithRetry(operation));
	}

	@Test(expected = NullPointerException.class)
	public void executeWithRetryNullTest() throws Exception {
		readerRetryHelper.executeWithRetry(null);
	}
}
