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

package com.publicissapient.kpidashboard.sonar.processor.service;

import static org.mockito.ArgumentMatchers.any;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.publicissapient.kpidashboard.common.util.PropertyUtils;
import com.publicissapient.kpidashboard.sonar.config.SonarConfig;

/**
 * @author shi6
 */
@ExtendWith(SpringExtension.class)
public class SonarConfigServiceTest {

	@InjectMocks
	private SonarConfigService service;

	@Mock
	private SonarConfig sonarConfig;
	@Mock
	private PropertyUtils propertyUtils;

	@Test
	public void updateSettingsObject() {
		Mockito.doNothing().when(propertyUtils).trimProps(any(), any());
		service.updateSettingsObject();
	}
}
