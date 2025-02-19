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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Date;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import com.publicissapient.kpidashboard.common.model.application.Week;

public class DateUtilTest {

	@Test
	public void testDateTimeFormatter() {
		LocalDateTime dateTime = LocalDateTime.of(2023, 1, 1, 12, 0);
		String formattedDate = DateUtil.dateTimeFormatter(dateTime, DateUtil.DISPLAY_DATE_FORMAT);
		assertEquals("01-Jan-2023", formattedDate);
	}

	@Test
	public void testDateTimeParser() {
		String dateString = "01-Jan-2023";
		Date date = DateUtil.dateTimeParser(dateString, DateUtil.DISPLAY_DATE_FORMAT);
		assertNotNull(date);
	}

	@Test
	public void testDateTimeFormatterWithDate() {
		Date date = new Date(1641088800000L); // 2023-01-02
		String formattedDate = DateUtil.dateTimeFormatter(date, DateUtil.DISPLAY_DATE_FORMAT);
		assertEquals("02-Jan-2022", formattedDate);
	}

	@Test
	public void testDateTimeConverter() {
		String dateTimeString = "2023-01-01T12:00:00";
		String convertedDate = DateUtil.dateTimeConverter(dateTimeString, DateUtil.TIME_FORMAT,
				DateUtil.DISPLAY_DATE_FORMAT);
		assertEquals("01-Jan-2023", convertedDate);
	}

	@Test
	public void testGetWeek() {
		LocalDate date = LocalDate.of(2023, 1, 15);
		Week week = DateUtil.getWeek(date);
		assertEquals(LocalDate.of(2023, 1, 9), week.getStartDate());
		assertEquals(LocalDate.of(2023, 1, 15), week.getEndDate());
	}

	@Test
	public void testIsWithinDateRange() {
		LocalDate targetDate = LocalDate.of(2023, 1, 10);
		LocalDate startDate = LocalDate.of(2023, 1, 1);
		LocalDate endDate = LocalDate.of(2023, 1, 15);
		assertEquals(true, DateUtil.isWithinDateRange(targetDate, startDate, endDate));
	}

	@Test
	public void testIsWithinDateTimeRange() {
		LocalDateTime targetDateTime = LocalDateTime.of(2023, 1, 10, 12, 0);
		LocalDateTime startDateTime = LocalDateTime.of(2023, 1, 1, 12, 0);
		LocalDateTime endDateTime = LocalDateTime.of(2023, 1, 15, 12, 0);
		assertEquals(true, DateUtil.isWithinDateTimeRange(targetDateTime, startDateTime, endDateTime));
	}

	@Test
	public void calculateWorkingDaysTest() {
		LocalDateTime startDateTime = LocalDateTime.of(2024, 1, 1, 12, 0);
		LocalDateTime endDateTime = LocalDateTime.of(2024, 1, 15, 12, 0);
		assertEquals(11, DateUtil.calculateWorkingDays(startDateTime, endDateTime));
	}

	@Test
	public void testEqualAndAfterTime() {
		LocalDateTime targetDateTime = LocalDateTime.of(2023, 1, 10, 12, 0);
		LocalDateTime startDateTime = LocalDateTime.of(2023, 1, 1, 12, 0);
		assertEquals(true, DateUtil.equalAndAfterTime(targetDateTime, startDateTime));
	}

	@Test
	public void testEqualAndBeforeTime() {
		LocalDateTime targetDateTime = LocalDateTime.of(2023, 1, 1, 12, 0);
		LocalDateTime startDateTime = LocalDateTime.of(2023, 1, 10, 12, 0);
		assertEquals(true, DateUtil.equalAndBeforTime(targetDateTime, startDateTime));
	}

	@Test
	public void testConvertMillisToDateTime() {
		long milliSeconds = 1641088800000L; // 2023-01-02
		assertNotNull(DateUtil.convertMillisToDateTime(milliSeconds));
	}

	@Test
	public void testDateTimeConverterUsingFromAndTo_NullInput() {
		DateTime dateTime = null;
		String fromFormat = "yyyy-MM-dd'T'HH:mm:ss";
		String toFormat = "dd-MMM-yyyy";
		String convertedDate = DateUtil.dateTimeConverterUsingFromAndTo(dateTime, fromFormat, toFormat);
		assertNull(convertedDate);
	}

	@Test
	public void testDateTimeConverterUsingFromAndTo_InvalidFormat() {
		DateTime dateTime = new DateTime(2023, 1, 1, 12, 0);
		String fromFormat = "yyyy-MM-dd";
		String toFormat = "dd-MMM-yyyy";
		String convertedDate = DateUtil.dateTimeConverterUsingFromAndTo(dateTime, fromFormat, toFormat);
		assertNull(convertedDate);
	}

	@Test
	public void testStringToLocalDateTime() {
		String time = "2023-01-01T12:00:00";
		String format = "yyyy-MM-dd'T'HH:mm:ss";
		LocalDateTime expectedDateTime = LocalDateTime.of(2023, 1, 1, 12, 0);
		LocalDateTime actualDateTime = DateUtil.stringToLocalDateTime(time, format);
		assertEquals(expectedDateTime, actualDateTime);
	}

	@Test
	public void testStringToLocalDateTime_2() {
		String time = "2023-01-01T12:00:00";
		String format = "yyyy-MM-dd'T'HH:mm:ss";
		LocalDateTime expectedDateTime = LocalDateTime.of(2023, 1, 1, 12, 0);
		LocalDateTime actualDateTime = DateUtil.stringToLocalDateTime(time, format);
		assertEquals(expectedDateTime, actualDateTime);
	}

	@Test
	public void testStringToLocalDate_ValidFormat() {
		String time = "2023-01-01";
		String format = "yyyy-MM-dd";
		LocalDate expectedDate = LocalDate.of(2023, 1, 1);
		LocalDate actualDate = DateUtil.stringToLocalDate(time, format);
		assertEquals(expectedDate, actualDate);
	}

	@Test
	public void testStringToLocalDate_InvalidDate() {
		String time = "2023-13-01";
		String format = "yyyy-MM-dd";
		assertThrows(DateTimeParseException.class, () -> {
			DateUtil.stringToLocalDate(time, format);
		});
	}

	@Test
	public void testConvertDoubleToDaysAndHoursString() {
		double valueInDays = 2.5;
		String expected = "2 Days 4 Hours ";
		String actual = DateUtil.convertDoubleToDaysAndHoursString(valueInDays);
		assertEquals(expected, actual);
	}

	@Test
	public void testGetWeekRangeUsingDateTime_Positive() {
		DateTime currentDate = new DateTime(2023, 1, 10, 12, 0); // Tuesday
		String expectedRange = "09/01 - 15/01";
		String actualRange = DateUtil.getWeekRangeUsingDateTime(currentDate);
		assertEquals(expectedRange, actualRange);
	}

	@Test
	public void testGetWeekRangeUsingDateTime_Negative() {
		DateTime currentDate = null;
		assertThrows(NullPointerException.class, () -> {
			DateUtil.getWeekRangeUsingDateTime(currentDate);
		});
	}

	@Test
	public void testGetWeekRange() {
		LocalDate currentDate = LocalDate.of(2023, 1, 10); // Tuesday
		String expectedRange = "09/01 - 15/01";
		String actualRange = DateUtil.getWeekRange(currentDate);
		assertEquals(expectedRange, actualRange);
	}

	@Test
	public void testConvertDateTimeToLocalDateTime_NotEmptyDateTime() {
		DateTime dateTime = new DateTime(2023, 1, 1, 12, 0);
		LocalDateTime expectedLocalDateTime = LocalDateTime.of(2023, 1, 1, 12, 0);
		LocalDateTime actualLocalDateTime = DateUtil.convertDateTimeToLocalDateTime(dateTime);
		assertEquals(expectedLocalDateTime, actualLocalDateTime);
	}

	@Test
	public void testConvertDateTimeToLocalDateTime_NullDateTime() {
		DateTime dateTime = null;
		LocalDateTime actualLocalDateTime = DateUtil.convertDateTimeToLocalDateTime(dateTime);
		assertNull(actualLocalDateTime);
	}

	@Test
	public void testConvertLocalDateTimeToDateTime() {
		LocalDateTime localDateTime = LocalDateTime.of(2023, 1, 1, 12, 0);
		DateTime expectedDateTime = new DateTime(2023, 1, 1, 12, 0);
		DateTime actualDateTime = DateUtil.convertLocalDateTimeToDateTime(localDateTime);
		assertEquals(expectedDateTime, actualDateTime);
	}

	@Test
	public void testConvertLocalDateTimeToDateTime_NullInput() {
		LocalDateTime localDateTime = null;
		assertThrows(NullPointerException.class, () -> {
			DateUtil.convertLocalDateTimeToDateTime(localDateTime);
		});
	}

	@Test
	public void testDateConverter() {
		Date dateTime = new Date(1641088800000L); // 2023-01-02
		String expectedDate = "02-Jan-2022";
		String actualDate = DateUtil.dateConverter(dateTime);
		assertEquals(expectedDate, actualDate);
	}

	@Test
	public void testLocalDateTimeConverter() {
		LocalDate date = LocalDate.of(2023, 1, 1);
		String expectedDate = "01-Jan-2023";
		String actualDate = DateUtil.localDateTimeConverter(date);
		assertEquals(expectedDate, actualDate);
	}

	@Test
	public void testDateTimeConverter_2() {
		DateTime dateTime = new DateTime(2023, 1, 1, 12, 0);
		String fromFormat = "yyyy-MM-dd'T'HH:mm:ss";
		String expectedDate = "01-Jan-2023";
		String actualDate = DateUtil.dateTimeConverter(dateTime, fromFormat);
		assertEquals(expectedDate, actualDate);
	}

	@Test
	public void testGetFormattedDate() {
		DateTime dateTime = new DateTime(2023, 1, 1, 12, 0);
		String expectedFormattedDate = "2023-01-01T12:00:00.0000000";
		String actualFormattedDate = DateUtil.getFormattedDate(dateTime);
		assertEquals(expectedFormattedDate, actualFormattedDate);
	}
}
