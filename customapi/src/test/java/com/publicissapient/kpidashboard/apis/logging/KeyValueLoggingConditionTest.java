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

package com.publicissapient.kpidashboard.apis.logging;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

@RunWith(MockitoJUnitRunner.class)
public class KeyValueLoggingConditionTest {

	@Mock
	private ConditionContext context;

	@Mock
	private Environment environment;

	@Mock
	private AnnotatedTypeMetadata metadata;

	@InjectMocks
	private KeyValueLoggingCondition condition;

	@Test
	public void shouldBeTrueWhenPropertyTrue() {
		KeyValueLoggingCondition condition = new KeyValueLoggingCondition();

		when(context.getEnvironment()).thenReturn(environment);
		when(environment.getProperty(KeyValueLoggingCondition.LOG_REQUEST_KEY_VALUE)).thenReturn("true");

		assertTrue(condition.matches(context, metadata));
	}

	@Test
	public void shouldBeFalseWhenPropertyFalse() {
		KeyValueLoggingCondition condition = new KeyValueLoggingCondition();

		when(context.getEnvironment()).thenReturn(environment);
		when(environment.getProperty(KeyValueLoggingCondition.LOG_REQUEST_KEY_VALUE)).thenReturn("false");

		assertFalse(condition.matches(context, metadata));
	}

	@Test
	public void shouldBeFalseWhenPropertyNull() {
		KeyValueLoggingCondition condition = new KeyValueLoggingCondition();

		when(context.getEnvironment()).thenReturn(environment);
		when(environment.getProperty(KeyValueLoggingCondition.LOG_REQUEST_KEY_VALUE)).thenReturn(null);

		assertFalse(condition.matches(context, metadata));
	}

}
