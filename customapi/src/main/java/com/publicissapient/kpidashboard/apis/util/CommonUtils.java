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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;

import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.KPISource;
import com.publicissapient.kpidashboard.apis.model.SymbolValueUnit;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;

import lombok.extern.slf4j.Slf4j;

/**
 * Provides Common utilities.
 * 
 * @author anisingh4
 */
@Slf4j
public final class CommonUtils {

	public static final int FIFTH_DAY_OF_WEEK = 5;

	private CommonUtils() {
	}

	/**
	 * Gets the list from string.
	 *
	 * @param input
	 *            the input
	 * @return the list from string
	 */
	public static List<String> getListFromString(String input) {
		String[] arr = input.split(Constant.SPLITTER);
		return Arrays.asList(arr);
	}


	/**
	 * Gets the symbol value unit.
	 *
	 * @param symbol
	 *            the symbol
	 * @param value
	 *            the value
	 * @param unit
	 *            the unit
	 * @return the symbol value unit
	 */
	public static SymbolValueUnit getSymbolValueUnit(String symbol, String value, String unit) {
		SymbolValueUnit symbolValueUnit = new SymbolValueUnit();
		symbolValueUnit.setSymbol(symbol);
		symbolValueUnit.setValue(value);
		symbolValueUnit.setUnit(unit);
		return symbolValueUnit;
	}


	/**
	 * Gets the days between date.
	 *
	 * @param beginDate
	 *            the begin date
	 * @param endDate
	 *            the end date
	 * @return the days between date
	 */
	public static Map<String, Integer> getDaysBetweenDate(DateTime beginDate, DateTime endDate) {
		Map<String, Integer> mapDays = new HashMap<>();
		DateTime theBeginDate = beginDate;
		DateTime theEndDate = endDate;
		String separator = "-";
		while (!theBeginDate.isAfter(theEndDate)) {
			if (theBeginDate.getDayOfWeek() <= FIFTH_DAY_OF_WEEK) {
				String strBDate = getStringDate(theBeginDate, separator);
				mapDays.put(strBDate, 0);
			}
			theBeginDate = theBeginDate.plusDays(1);
		}
		return mapDays;
	}

	/**
	 * Gets the string date.
	 *
	 * @param date
	 *            the date
	 * @param separator
	 *            the separator
	 * @return the string date
	 */
	public static String getStringDate(DateTime date, String separator) {
		return date.getYear() + separator + date.getMonthOfYear() + separator + date.getDayOfMonth();
	}

	/**
	 * Round to certain number of decimals.
	 *
	 * @param unroundedNumber
	 *            the unrounded number
	 * @param decimalPlaces
	 *            the decimal places
	 * @return the double
	 */
	public static double truncateTo(double unroundedNumber, int decimalPlaces) {
		int truncatedNumberInt = (int) (unroundedNumber * Math.pow(10, decimalPlaces));
		return truncatedNumberInt / Math.pow(10, decimalPlaces);
	}

	public static boolean isNumber(String value) {
		return NumberUtils.isNumber(value);
	}

	public static long median(List<Long> numbers) {
		if (CollectionUtils.isNotEmpty(numbers)) {
			Collections.sort(numbers);
			int middle = numbers.size() / 2;
			if (numbers.size() % 2 == 1) {
				return numbers.get(middle);
			} else {
				return (numbers.get(middle - 1) + numbers.get(middle)) / 2;
			}
		} else {
			return 0;
		}

	}

	/**
	 * Builds the date count map for xdays.
	 *
	 * @param lastXdays
	 *            the last xdays
	 * @param allFixDates
	 *            the all fix dates
	 * @return the map
	 */
	public static Map<String, Integer> buildDateCountMapForXdays(Integer lastXdays, List<DateTime> allFixDates) {
		int lastXdaysIntVal = Constant.LAST_X_DAYS_INTERVAL;
		if (null != lastXdays) {
			lastXdaysIntVal = lastXdays.intValue();
		}
		// Include today as well
		DateTime untilDate = new DateTime(DateTimeZone.UTC).plusDays(Constant.INCREMENTER_DAY);
		DateTime sinceDate = untilDate.minusDays(lastXdaysIntVal);
		// Initialize map with all possible dates
		Map<String, Integer> resultMap = new TreeMap<>();
		while (sinceDate.isBefore(untilDate)) {
			String key = sinceDate.toString(Constant.ISO_DATE_FORMAT);
			resultMap.put(key, Constant.DEFAULT_DEFECT_COUNT);
			sinceDate = sinceDate.plusDays(Constant.INCREMENTER_DAY);
		}

		for (DateTime fixDate : allFixDates) {
			String placeholderKey = fixDate.toString(Constant.ISO_DATE_FORMAT);

			// current count is null means the fixed date is not in range.
			resultMap.computeIfPresent(placeholderKey, (key, value) -> ++value);

		}
		return resultMap;
	}

	/**
	 * Convert from ISO format.
	 *
	 * @param source
	 *            the source
	 * @param renderFormat
	 *            the render format
	 * @return the string
	 */
	public static String convertFromISOFormat(String source, String renderFormat) {// dd-MMM
		return DateTimeFormat.forPattern(Constant.ISO_DATE_FORMAT).parseDateTime(source).toString(renderFormat);
	}

	/**
	 * This method is use to convert epoch(unix) time to String date
	 *
	 * @param longdate
	 * 
	 * @return String
	 */
	public static String epochToStrDate(Long longdate) {
		Date date = new Date(longdate);
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
		return dateFormat.format(date);
	}

	/**
	 * This method used to convert string list to pattern list to support ignore
	 * case
	 * 
	 * @param stringList
	 * @return return list of patttern
	 */
	public static List<Pattern> convertToPatternList(List<String> stringList) {
		List<Pattern> regexList = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(stringList)) {
			for (String value : stringList) {
				regexList.add(Pattern.compile(Constant.TILDA_SYMBOL+Pattern.quote(value)
								+Constant.DOLLAR_SYMBOL,
						Pattern.CASE_INSENSITIVE));

			}
		}
		return regexList;
	}

	/**
	 * This method used to convert string list to pattern list to support ignore
	 * case
	 * 
	 * @param stringList
	 * @return return list of pattern
	 */
	public static List<Pattern> convertTestFolderToPatternList(List<String> stringList) {
		List<Pattern> regexList = new ArrayList<>();
		for (String value : stringList) {
			String pattern = new StringBuilder(value).toString();
			if (pattern.contains(Constant.FORWARD_SLASH)) {
				pattern = pattern.replace(Constant.FORWARD_SLASH, Constant.BACKWARD_FORWARD_SLASH);
			}
			regexList.add(Pattern.compile(pattern, Pattern.CASE_INSENSITIVE));
			if(!value.startsWith(Constant.FORWARD_SLASH)){
				value = Constant.FORWARD_SLASH.concat(value);
			}
			regexList.add(CommonUtils.convertToPatternText(value));
		}
		return regexList;
	}

	/**
	 * This method convert string to pattern to support ignore case
	 * 
	 * @param text
	 * @return patern string with regex
	 */
	public static Pattern convertToPatternText(String text) {
		return Pattern.compile(text, Pattern.CASE_INSENSITIVE);
	}

	/**
	 * Gets the cache name based on key.
	 * 
	 * @param key
	 *            key
	 * @return cache manager name
	 */
	public static String getCacheName(String key) {
		Map<String, String> cacheManagerMap = new HashMap<>();
		cacheManagerMap.put(KPISource.JIRA.name(), CommonConstant.JIRA_KPI_CACHE);
		cacheManagerMap.put(KPISource.JIRAKANBAN.name(), CommonConstant.JIRAKANBAN_KPI_CACHE);

		cacheManagerMap.put(KPISource.SONAR.name(), CommonConstant.SONAR_KPI_CACHE);
		cacheManagerMap.put(KPISource.SONARKANBAN.name(), CommonConstant.SONAR_KPI_CACHE);

		cacheManagerMap.put(KPISource.BITBUCKET.name(), CommonConstant.BITBUCKET_KPI_CACHE);
		cacheManagerMap.put(KPISource.BITBUCKETKANBAN.name(), CommonConstant.BITBUCKET_KPI_CACHE);

		cacheManagerMap.put(KPISource.JENKINS.name(), CommonConstant.JENKINS_KPI_CACHE);
		cacheManagerMap.put(KPISource.JENKINSKANBAN.name(), CommonConstant.JENKINS_KPI_CACHE);

		cacheManagerMap.put(KPISource.ZEPHYR.name(), CommonConstant.TESTING_KPI_CACHE);
		cacheManagerMap.put(KPISource.ZEPHYRKANBAN.name(), CommonConstant.TESTING_KPI_CACHE);

		cacheManagerMap.put(Constant.KPI_REQUEST_TRACKER_ID_KEY, "requestTrackerCache");

		cacheManagerMap.put(CommonConstant.CACHE_FIELD_MAPPING_MAP, CommonConstant.CACHE_FIELD_MAPPING_MAP);
		cacheManagerMap.put(CommonConstant.CACHE_TOOL_CONFIG_MAP, CommonConstant.CACHE_TOOL_CONFIG_MAP);
		cacheManagerMap.put(CommonConstant.CACHE_PROJECT_CONFIG_MAP, CommonConstant.CACHE_PROJECT_CONFIG_MAP);

		if (cacheManagerMap.containsKey(key)) {
			return cacheManagerMap.get(key);
		}

		return null;
	}

	/**
	 * handle taint value propagation vulnerability
	 * 
	 * @param value
	 *            taintedValue
	 * @return string response
	 */
	public static String handleCrossScriptingTaintedValue(String value) {
		return null == value ? null : value.replaceAll("[\\n|\\r\\t]", "");
	}
	
	/**
	 * This method used to convert string list to pattern list to support ignore
	 * case
	 * 
	 * @param stringList
	 * @return return list of patttern
	 */
	public static List<Pattern> convertToPatternListForCapacity(List<String> stringList) {
		List<Pattern> regexList = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(stringList)) {
			for (String value : stringList) {
				value=appendbackSlash(value);
				regexList.add(Pattern.compile(Constant.TILDA_SYMBOL + value + Constant.DOLLAR_SYMBOL,
						Pattern.CASE_INSENSITIVE));
			}
		}
		return regexList;
	}

	/**
	 * This method used to convert string list to pattern list to support ignore
	 * case
	 *
	 * @param value
	 * @return return String
	 */
	public static String appendbackSlash(String value) {
		value = value.replaceAll(Constant.ROUND_CLOSE_BRACKET,Constant.BACKWARD_SLASH_CLOSE);
		return value.replaceAll(Constant.ROUND_OPEN_BRACKET,Constant.BACKWARD_SLASH_OPEN);
	}
	
}