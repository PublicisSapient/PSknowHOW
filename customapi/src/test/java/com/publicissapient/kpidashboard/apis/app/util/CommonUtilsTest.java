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

package com.publicissapient.kpidashboard.apis.app.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import com.publicissapient.kpidashboard.apis.util.CommonUtils;

public class CommonUtilsTest {

	@Test
	public void getListFromString() {
		Assert.assertEquals("List from string", 2, CommonUtils.getListFromString("Test~String").size());
	}

	@Test
	public void getSymbolValueUnit() {
		Map<String, List<String>> map = new HashMap<>();
		map.put("key", Arrays.asList("value1", "value2"));
		Assert.assertNotNull(CommonUtils.getSymbolValueUnit("test", "test", "value1"));
	}

	@Test
	public void convertToPatternText() {
		Assert.assertNotNull(CommonUtils.convertToPatternText("test"));
	}

	@Test
	public void convertToPatternListStartsWith() {
		Assert.assertNotNull(CommonUtils.convertTestFolderToPatternList(Arrays.asList("test", "test/2", "test3")));
		Assert.assertNotNull(CommonUtils.convertToPatternList(Arrays.asList("test", "test2", "test3")));
	}

	@Test
	public void epochToStrDate() {

		Assert.assertNotNull(CommonUtils.epochToStrDate(12345543l));
	}

	@Test
	public void median() {

		List<Long> longList = new ArrayList<>();
		longList.add(123l);
		longList.add(12473l);
		longList.add(14523l);
		Assert.assertNotNull(CommonUtils.median(longList));
	}

	@Test
	public void isNumber() {

		Assert.assertNotNull(CommonUtils.isNumber("4"));
	}

	@Test
	public void truncateTo() {

		Assert.assertNotNull(CommonUtils.truncateTo(2.22, 2));
	}

	@Test
	public void getDaysBetweenDate() {

		Assert.assertNotNull(CommonUtils.getDaysBetweenDate(DateTime.now().minusDays(4), new DateTime()));
	}

	@Test
	public void getStringDate() {

		Assert.assertNotNull(CommonUtils.getStringDate(new DateTime(), "///"));
	}

	@Test
	public void buildDateCountMapForXdays() {
		List<DateTime> dateTimeList = new ArrayList<>();
		dateTimeList.add(new DateTime());
		dateTimeList.add(new DateTime());
		dateTimeList.add(new DateTime());
		Assert.assertNotNull(CommonUtils.buildDateCountMapForXdays(3, dateTimeList));
	}
}