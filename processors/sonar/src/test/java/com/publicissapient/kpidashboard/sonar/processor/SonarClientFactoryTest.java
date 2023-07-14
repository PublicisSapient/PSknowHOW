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

package com.publicissapient.kpidashboard.sonar.processor;

import static org.hamcrest.Matchers.instanceOf;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.sonar.factory.SonarClientFactory;
import com.publicissapient.kpidashboard.sonar.processor.adapter.SonarClient;
import com.publicissapient.kpidashboard.sonar.processor.adapter.impl.Sonar6And7Client;
import com.publicissapient.kpidashboard.sonar.processor.adapter.impl.Sonar8Client;

@RunWith(MockitoJUnitRunner.class)
public class SonarClientFactoryTest {

	@InjectMocks
	private SonarClientFactory factory;
	@Mock
	private Sonar6And7Client sonar6And7Client;
	@Mock
	private Sonar8Client sonar8Client;

	@Test
	public void testGetSonarClient() throws Exception {
		SonarClient sonarClient = factory.getSonarClient("9.0");
		Assert.assertThat(sonarClient, instanceOf(Sonar8Client.class));
		sonarClient = factory.getSonarClient("7.9");
		Assert.assertThat(sonarClient, instanceOf(Sonar6And7Client.class));
		sonarClient = factory.getSonarClient("8.0");
		Assert.assertThat(sonarClient, instanceOf(Sonar8Client.class));
		sonarClient = factory.getSonarClient("8.3");
		Assert.assertThat(sonarClient, instanceOf(Sonar8Client.class));
		sonarClient = factory.getSonarClient("5.3");
		Assert.assertThat(sonarClient, instanceOf(Sonar8Client.class));
	}

	@Test
	public void testGetSonarClientNull() {
		try {
			factory.getSonarClient(null);
		} catch (NullPointerException exception) {
			Assert.assertEquals("API Version should be Empty For Sonar", exception.getMessage());
		}
	}

	@Test
	public void getSonarClieent_versionInInt() throws Exception {
		SonarClient sonarClient = factory.getSonarClient("8");
		Assert.assertThat(sonarClient, instanceOf(Sonar8Client.class));
	}

}