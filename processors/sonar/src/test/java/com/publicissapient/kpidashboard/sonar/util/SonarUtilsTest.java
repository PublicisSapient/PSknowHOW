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

package com.publicissapient.kpidashboard.sonar.util;

import org.junit.Assert;
import org.junit.Test;

public class SonarUtilsTest {

	@Test
	public void testAddSpaceIfNeeded() {
		StringBuilder stringBuilder = new StringBuilder("Test message");
		SonarUtils.addSpaceIfNeeded(stringBuilder);
		Assert.assertEquals(' ', stringBuilder.toString().charAt(stringBuilder.length() - 1));

	}

	@Test
	public void testFormatDuration() {
		int days = 1;
		int hours = 0;
		int minutes = 0;
		boolean isNegative = false;
		String actual = SonarUtils.formatDuration(days, hours, minutes, isNegative);

		Assert.assertEquals("1d", actual);

	}

	@Test
	public void testFormatDurationDisplayHour() {
		int days = 2;
		int hours = 3;
		int minutes = 0;
		boolean isNegative = false;
		String actual = SonarUtils.formatDuration(days, hours, minutes, isNegative);
		Assert.assertEquals("2d 3h", actual);

	}

	@Test
	public void testFormatDurationDisplyMinute() {
		int days = 0;
		int hours = 3;
		int minutes = 5;
		boolean isNegative = false;
		SonarUtils.formatDuration(days, hours, minutes, isNegative);

		String actual = SonarUtils.formatDuration(days, hours, minutes, isNegative);
		Assert.assertEquals("3h 5min", actual);

	}

	@Test
	public void testFormatDurationDisplyMinuteNegative() {
		int days = 0;
		int hours = 3;
		int minutes = 5;
		boolean isNegative = true;
		SonarUtils.formatDuration(days, hours, minutes, isNegative);

		String actual = SonarUtils.formatDuration(days, hours, minutes, isNegative);
		Assert.assertEquals("-3h 5min", actual);

	}
}