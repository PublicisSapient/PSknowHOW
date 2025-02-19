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

package com.publicissapient.kpidashboard.common.context;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ExecutionLogContextTest {

	private ExecutionLogContext executionLogContext;

	@BeforeEach
	public void setUp() {
		executionLogContext = new ExecutionLogContext();
	}

	@AfterEach
	public void tearDown() {
		executionLogContext.destroy();
	}

	@Test
	public void testContextInitialization() {
		assertNull(ExecutionLogContext.getContext().getRequestId());
		assertNull(ExecutionLogContext.getContext().getEnvironment());
		assertNull(ExecutionLogContext.getContext().getProjectName());
		assertNull(ExecutionLogContext.getContext().getProjectBasicConfgId());
		assertNull(ExecutionLogContext.getContext().getIsCron());
	}

	@Test
	public void testUpdateContextValues() {
		executionLogContext.setRequestId("initialRequestId");
		executionLogContext.setEnvironment("initialEnvironment");
		executionLogContext.setProjectName("initialProjectName");
		executionLogContext.setProjectBasicConfgId("initialProjectConfigId");
		executionLogContext.setIsCron("initialIsCron");

		ExecutionLogContext updateContext = new ExecutionLogContext();
		updateContext.setRequestId("updatedRequestId");
		updateContext.setEnvironment("updatedEnvironment");
		updateContext.setProjectName("updatedProjectName");
		updateContext.setProjectBasicConfgId("updatedProjectConfigId");
		updateContext.setIsCron("updatedIsCron");

		ExecutionLogContext updatedContext = ExecutionLogContext.updateContext(updateContext);

		assertEquals("updatedRequestId", updatedContext.getRequestId());
		assertEquals("updatedEnvironment", updatedContext.getEnvironment());
		assertEquals("updatedProjectName", updatedContext.getProjectName());
		assertEquals("updatedProjectConfigId", updatedContext.getProjectBasicConfgId());
		assertEquals("updatedIsCron", updatedContext.getIsCron());
	}

	@Test
	public void testThreadIdAssignment() {
		assertEquals(1, ExecutionLogContext.getContext().getThreadId());
	}
}
