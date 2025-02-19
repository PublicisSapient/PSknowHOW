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

package com.publicissapient.kpidashboard.common.feature;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.togglz.core.manager.FeatureManager;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestTogglzConfig.class)
public class FeatureEnumTest {

	@Autowired
	private FeatureManager featureManager;

	@Test
	public void testDailyStandupFeatureIsActive() {
		assertTrue(featureManager.isActive(FeatureEnum.DAILY_STANDUP));
	}

	@Test
	public void testRecommendationFeatureIsActive() {
		assertFalse(featureManager.isActive(FeatureEnum.RECOMMENDATIONS));
	}

	@Test
	public void testnewUIFeatureIsActive() {
		assertTrue(featureManager.isActive(FeatureEnum.NEW_UI_SWITCH));
	}
}
