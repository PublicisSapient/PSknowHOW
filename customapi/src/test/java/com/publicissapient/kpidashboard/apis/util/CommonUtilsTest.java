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

package com.publicissapient.kpidashboard.apis.util;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.model.SymbolValueUnit;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;

public class CommonUtilsTest {

	@Test
	public void getListFromStringTest() {

		List<String> convertedList = CommonUtils.getListFromString("AB~CD~EF~GH");
		assertTrue("List is of size 4", convertedList.size() == 4);
	}

	@Test
	public void getListFromStringTestNegative() {

		List<String> convertedList = CommonUtils.getListFromString("AB-CD-EF-GH");
		assertTrue("List is of size 1", convertedList.size() == 1);
	}

	@Test
	public void getSymbolValueUnitTest() {
		SymbolValueUnit symbolValueUnit = new SymbolValueUnit();
		symbolValueUnit.setSymbol("A");
		symbolValueUnit.setUnit("U");
		symbolValueUnit.setValue("V");
		symbolValueUnit.setTrend("T");
		SymbolValueUnit symbolValueUnitReturn = CommonUtils.getSymbolValueUnit("A", "V", "U");
		assertTrue(symbolValueUnit.getSymbol().equals(symbolValueUnitReturn.getSymbol()));
	}

	@Test
	public void getDaysBetweenDateTest() {
		DateTime endDate = DateTime.now();
		String separator = "-";
		DateTime beginDate = endDate.minusDays(3);
		Map<String, Integer> returnMap = CommonUtils.getDaysBetweenDate(beginDate, endDate);
		String key = beginDate.getYear() + separator + beginDate.getMonthOfYear() + separator
				+ beginDate.getDayOfMonth();

	}

	@Test
	public void getDaysBetwDateTest() {
		DateTime endDate = DateTime.now();
		String separator = "-";
		DateTime beginDate = endDate.minusDays(3);
		Integer returnMap = CommonUtils.closedStoryAndPotentialDelays(beginDate, endDate);
		String key = beginDate.getYear() + separator + beginDate.getMonthOfYear() + separator
				+ beginDate.getDayOfMonth();

	}

	@Test
	public void getDaysBetwDate2() {
		DateTime endDate = DateTime.now();
		String separator = "-";
		DateTime beginDate = endDate.minusDays(3);
		Integer returnMap = CommonUtils.openStoryDelay(beginDate, endDate, true);
		String key = beginDate.getYear() + separator + beginDate.getMonthOfYear() + separator
				+ beginDate.getDayOfMonth();

	}

	@Test
	public void getDaysBetweenDateTestFalse() {
		DateTime endDate = DateTime.now();
		String separator = "-";
		DateTime beginDate = endDate.minusDays(5);
		Map<String, Integer> returnMap = CommonUtils.getDaysBetweenDate(beginDate, endDate);
		String key = beginDate.getYear() + separator + beginDate.getMonthOfYear() + separator
				+ beginDate.getDayOfMonth();

	}

	@Test
	public void getMedian() {
		List<Long> numbers = new ArrayList<>();
		numbers.add(123456L);
		Collections.sort(numbers);
		CommonUtils.median(numbers);

	}

	@Test
	public void getMedianEmpty() {
		List<Long> numbers = new ArrayList<>();
		Collections.sort(numbers);
		CommonUtils.median(numbers);

	}

	@Test
	public void buildDateCountMapForXdays() {
		Integer lastXdays = 10;
		DateTime untilDate = new DateTime(DateTimeZone.UTC).plusDays(Constant.INCREMENTER_DAY);

		List<DateTime> allFixDates = new ArrayList<DateTime>();
		allFixDates.add(untilDate);
		CommonUtils.buildDateCountMapForXdays(lastXdays, allFixDates);

	}

	@Test
	public void convertFromISOFormat() {
		String source = "2021-08-05";
		String renderFormat = "2021-08-01";
		CommonUtils.convertFromISOFormat(source, renderFormat);
	}

	@Test
	public void epochToStrDate() {
		Long longdate = 120045L;
		CommonUtils.epochToStrDate(longdate);
	}

	@Test
	public void convertToPatternList() {
		List<String> stringList = new ArrayList<>();
		stringList.add("abc");
		CommonUtils.convertToPatternList(stringList);
	}

	@Test
	public void convertToPatternListFalse() {
		List<String> stringList = new ArrayList<>();
		CommonUtils.convertToPatternList(stringList);
	}

	@Test
	public void convertTestFolderToPatternList() {
		List<String> stringList = new ArrayList<>();
		stringList.add("abc");
		CommonUtils.convertTestFolderToPatternList(stringList);
	}

	@Test
	public void getCacheName() {
		String key = "jiraKpiCache";
		Map<String, String> cacheManagerMap = new HashMap<>();
		cacheManagerMap.put(KPISource.JIRA.name(), CommonConstant.JIRA_KPI_CACHE);
		CommonUtils.getCacheName(key);
	}

	@Test
	public void handleCrossScriptingTaintedValue() {
		String key = "jira";
		CommonUtils.handleCrossScriptingTaintedValue(key);
	}

	@Test
	public void handleCrossScriptingTaintedValueNull() {
		String key = null;
		CommonUtils.handleCrossScriptingTaintedValue(key);
	}

	@Test
	public void convertToPatternListForCapacity() {
		List<String> stringList = new ArrayList<>();
		stringList.add("abc");
		CommonUtils.convertToPatternListForCapacity(stringList);
	}

	@Test
	public void convertToPatternListForCapacityEmpty() {
		List<String> stringList = new ArrayList<>();
		CommonUtils.convertToPatternListForCapacity(stringList);
	}

	@Test
	public void appendbackSlash() {
		String key = "jira";
		CommonUtils.appendbackSlash(key);
	}

	@Test
	public void truncateToTest() {
		Double result = CommonUtils.truncateTo(12.234f, 2);
		assertTrue(result == 12.23);
	}

	@Test
	public void isNumberTestNegative() {
		boolean result = CommonUtils.isNumber("123");
		assertTrue(result);
	}

	@Test
	public void isNumberTestPositive() {
		boolean result = CommonUtils.isNumber("2");
		assertTrue(result);
	}
}
