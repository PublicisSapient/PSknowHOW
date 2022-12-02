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

package com.publicissapient.kpidashboard.common.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

import com.publicissapient.kpidashboard.common.model.application.Week;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

/**
 * @author narsingh9
 *
 */

/**
 * Date util for common date operations
 */
@Slf4j
public class DateUtil {

	public static final String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

	public static final String TIME_FORMAT_WITH_SEC = "yyyy-MM-dd'T'HH:mm:ss.SSSX";

	public static final String TIME_FORMAT_WITH_SEC_DATE = "yyyy-MM-dd'T'HH:mm:ssX";

	private DateUtil() {
		// to prevent creation on object
	}

	/**
	 * returns the formatted date
	 * 
	 * @param dateTime
	 *            LocalDateTime object
	 * @param format
	 *            response format
	 * @return formatted date
	 */

	public static String dateTimeFormatter(LocalDateTime dateTime, final String format) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
		return dateTime.format(formatter);
	}

	/**
	 * 
	 * @param dateTime
	 *            string date
	 * @param format
	 *            response format
	 * @return parsed date
	 */

	public static Date dateTimeParser(String dateTime, final String format) {
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		Date date = null;
		try {
			date = formatter.parse(dateTime);
		} catch (ParseException e) {
			log.error("Exception while parse date..." + e.getMessage());
		}
		return date;
	}

	/**
	 * 
	 * @param dateTime
	 *            Date object
	 * @param format
	 *            response format
	 * @return formatted date
	 */
	public static String dateTimeFormatter(Date dateTime, final String format) {
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		return formatter.format(dateTime);
	}
	
	
	/**
	 * 
	 * @param dateTime
	 *            dateTime
	 * @param fromFormat
	 *            fromFormat
	 * @param toFormat
	 *            toFormat
	 * @return converted date
	 */
	public static String dateTimeConverter(String dateTime, final String fromFormat, final String toFormat) {
		String strDate = null;
		Date date = dateTimeParser(dateTime, fromFormat);
		if (date != null) {
			strDate = dateTimeFormatter(date, toFormat);
		}
		return strDate;
	}


	public static LocalDateTime stringToLocalDateTime(String time, String format){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
		return LocalDateTime.parse(time, formatter);
	}

	public static Week getWeek(LocalDate date) {
		Week week = new Week();
		LocalDate monday = date;
		while (monday.getDayOfWeek() != DayOfWeek.MONDAY) {
			monday = monday.minusDays(1);
		}
		week.setStartDate(monday);
		LocalDate sunday = date;
		while (sunday.getDayOfWeek() != DayOfWeek.SUNDAY) {
			sunday = sunday.plusDays(1);
		}
		week.setEndDate(sunday);
		return week;
	}

	public static boolean isWithinDateRange(LocalDate targetDate, LocalDate startDate, LocalDate endDate){
		return !targetDate.isBefore(startDate) && !targetDate.isAfter(endDate);
	}
}
