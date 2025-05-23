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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.ISODateTimeFormat;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.Week;

import lombok.extern.slf4j.Slf4j;

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

	public static final String TIME_FORMAT_WITH_SEC_ZONE = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

	public static final String TIME_FORMAT_WITH_SEC_DATE = "yyyy-MM-dd'T'HH:mm:ssX";

	public static final String ZERO_TIME_ZONE_FORMAT = "T00:00:00.000Z";

	public static final String DISPLAY_DATE_FORMAT = "dd-MMM-yyyy";

	public static final String DISPLAY_DATE_TIME_FORMAT = "dd-MMM-yyyy HH:mm:ss";

	public static final String DATE_FORMAT = "yyyy-MM-dd";

	public static final String BASIC_DATE_FORMAT = "dd-MM-yyyy";

	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
	public static final String NOT_APPLICABLE = "NA";
	public static final String DD_MM = "dd/MM";

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
	public static String dateTimeConverterUsingFromAndTo(DateTime dateTime, final String fromFormat,
			final String toFormat) {
		if (dateTime != null) {
			try {
				org.joda.time.format.DateTimeFormatter sourceFormatter = DateTimeFormat.forPattern(fromFormat);
				DateTime parsedDateTime = sourceFormatter.parseDateTime(dateTime.toString());
				org.joda.time.format.DateTimeFormatter targetFormatter = DateTimeFormat.forPattern(toFormat);
				return parsedDateTime.toString(targetFormatter);
			} catch (IllegalArgumentException e) {
				log.error("error while parse date", e);
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * already a UTC Time "2023-10-02T03:51:00.000Z", in format DateUtil.TIME_FORMAT_WITH_SEC
	 * @param time
	 * @param format
	 * @return
	 */
	public static LocalDateTime stringToLocalDateTime(String time, String format) {
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

	public static boolean isWithinDateRange(LocalDate targetDate, LocalDate startDate, LocalDate endDate) {
		return !targetDate.isBefore(startDate) && !targetDate.isAfter(endDate);
	}

	public static boolean isWithinDateTimeRange(LocalDateTime targetDate, LocalDateTime startDate,
			LocalDateTime endDate) {
		return !targetDate.isBefore(startDate) && !targetDate.isAfter(endDate);
	}

	public static boolean equalAndAfterTime(LocalDateTime targetDate, LocalDateTime startDate) {
		return targetDate.isAfter(startDate) || targetDate.isEqual(startDate);
	}

	public static boolean equalAndBeforTime(LocalDateTime targetDate, LocalDateTime startDate) {
		return targetDate.isBefore(startDate) || targetDate.isEqual(startDate);
	}

	/**
	 * Checks if the target date is equal to or before the end date.
	 *
	 * @param targetDate
	 *            the date to check, not null
	 * @param endDate
	 *            the date to compare against, not null
	 * @return {@code true} if the target date is equal to or before the end date;
	 *         {@code false} otherwise
	 * @throws NullPointerException
	 *             if either targetDate or endDate is null
	 */
	public static boolean equalAndBeforeTime(LocalDate targetDate, LocalDate endDate) {
		return targetDate.isEqual(endDate) || targetDate.isBefore(endDate) ;
	}

	public static String convertMillisToDateTime(long milliSeconds) {
		return convertMillisToLocalDateTime(milliSeconds).toString();
	}

	public static LocalDateTime convertMillisToLocalDateTime(long milliSeconds) {
		return Instant.ofEpochMilli(milliSeconds).atZone(ZoneId.systemDefault()).toLocalDateTime();
	}

	public static DateTime stringToDateTime(String date, String formater) {
		return DateTimeFormat.forPattern(formater).parseDateTime(date);
	}

	public static LocalDate stringToLocalDate(String time, String format) {
		LocalDate formattedDate;
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
		try {
			formattedDate = LocalDate.parse(time, formatter);
		} catch (DateTimeParseException dateTimeParseException) {
			formattedDate = OffsetDateTime.parse(time).toLocalDate();
		}
		return formattedDate;
	}

	public static long convertStringToLong(String date) {
		return ZonedDateTime.of(stringToLocalDateTime(date, TIME_FORMAT), ZoneId.systemDefault()).toInstant()
				.toEpochMilli();
	}

	public static LocalDateTime convertingStringToLocalDateTime(String time, String format) {
		Instant timestamp = Instant.parse(time);
		return timestamp.atZone(ZoneId.systemDefault()).toLocalDateTime();
	}

	public static String getFormattedDate(DateTime dateTime1) {
		String date = "";
		if (dateTime1 != null)
			date = dateTime1.toString();
		if (date != null && !date.isEmpty()) {
			try {
				DateTime dateTime = ISODateTimeFormat.dateOptionalTimeParser().parseDateTime(date);
				return ISODateTimeFormat.dateHourMinuteSecondMillis().print(dateTime) + "0000";
			} catch (IllegalArgumentException e) {
				log.error("error while parsing date: {} {}", date, e);
			}
		}

		return "";
	}

	public static String dateTimeConverter(DateTime dateTime, final String fromFormat) {
		String strDate = null;
		Date dateTimeData = dateTimeParser(dateTime.toString(fromFormat), fromFormat);
		if (dateTime != null) {
			strDate = dateTimeFormatter(dateTimeData, DISPLAY_DATE_FORMAT);
		}
		return strDate;
	}

	public static String localDateTimeConverter(LocalDate dateTime) {
		String strDate = null;
		Date dateTimeData = Date.from(dateTime.atStartOfDay(ZoneId.systemDefault()).toInstant());
		if (dateTime != null) {
			strDate = dateTimeFormatter(dateTimeData, DISPLAY_DATE_FORMAT);
		}
		return strDate;
	}

	public static String dateConverter(Date dateTime) {
		String strDate = null;
		if (dateTime != null) {
			strDate = dateTimeFormatter(dateTime, DISPLAY_DATE_FORMAT);
		}
		return strDate;
	}

	public static DateTime convertLocalDateTimeToDateTime(LocalDateTime dateTime) {
		Instant instant = dateTime.atZone(ZoneId.systemDefault()).toInstant();
		return new DateTime(instant.toEpochMilli());
	}

	public static LocalDateTime convertDateTimeToLocalDateTime(DateTime dateTime) {
		return (ObjectUtils.isNotEmpty(dateTime)) ? LocalDateTime.ofInstant(
				java.time.Instant.ofEpochMilli(dateTime.getMillis()), ZoneId.of(dateTime.getZone().getID())) : null;
	}

	public static String getWeekRange(LocalDate currentDate) {
		LocalDate monday = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		LocalDate sunday = currentDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

		String formattedMonday = monday.format(DateTimeFormatter.ofPattern(DD_MM));
		String formattedSunday = sunday.format(DateTimeFormatter.ofPattern(DD_MM));

		return formattedMonday + " - " + formattedSunday;
	}

	public static String getWeekRangeUsingDateTime(DateTime currentDate) {
		DateTime monday = currentDate.withDayOfWeek(DateTimeConstants.MONDAY);
		DateTime sunday = currentDate.withDayOfWeek(DateTimeConstants.SUNDAY);

		String formattedMondayDate = DateTimeFormat.forPattern(DD_MM).print(monday);
		String formattedSundayDate = DateTimeFormat.forPattern(DD_MM).print(sunday);

		return formattedMondayDate + " - " + formattedSundayDate;
	}

	/**
	 *
	 * @param valueInDays
	 * @return
	 */
	public static String convertDoubleToDaysAndHoursString(double valueInDays) {
		// Extract the integer part as days
		String result = "";
		if (valueInDays > 0) {
			int daysPart = (int) valueInDays;

			// Calculate the remaining fractional part as hours and minutes
			double fractionalPart = (valueInDays - daysPart) * 8;
			int hoursPart = (int) fractionalPart;
			int minutesPart = (int) ((fractionalPart - hoursPart) * 60);

			if (daysPart > 0) {
				result = daysPart + " Days ";
			}

			if (hoursPart > 0) {
				result += hoursPart + " Hours ";
			}

			if (minutesPart > 0 && ObjectUtils.defaultIfNull(daysPart, 0) == 0) {
				result += minutesPart + " Min";
			}
		} else {
			result = "NA";
		}
		return result;
	}

	/**
	 * Calculating total no. of working days between two dates
	 * 
	 * @param startDateTime
	 *            startDateTime
	 * @param endDateTime
	 *            endDateTime
	 * @return no. of days
	 */
	public static double calculateWorkingDays(LocalDateTime startDateTime, LocalDateTime endDateTime) {
		LocalDate startDate = startDateTime.toLocalDate();
		LocalDate endDate = endDateTime.toLocalDate();

		if (startDate.isAfter(endDate)) {
			throw new IllegalArgumentException("Release Start date must be before release end date");
		}

		return Stream.iterate(startDate, date -> date.plusDays(1))
				.limit(ChronoUnit.DAYS.between(startDate, endDate) + 1) // +1 to include endDate
				.filter(date -> !(date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY))
				.count();
	}


    public static LocalDateTime localDateTimeToUTC(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    public static String localDateTimeToUTC(String time) {
        LocalDateTime localDateTime = LocalDateTime.parse(time);
        return localDateTimeToUTC(localDateTime).toString();
    }

    public static String tranformUTCLocalTimeToZFormat(LocalDateTime ldt) {
        ldt = ldt.truncatedTo(ChronoUnit.SECONDS);
        Instant instant = ldt.toInstant(ZoneOffset.UTC);
        return DateTimeFormatter.ISO_INSTANT.format(instant);
    }

    public static String tranformUTCLocalDateTimeStringToZFormat(String utcTime) {
        if(StringUtils.isEmpty(utcTime)){
            return CommonConstant.BLANK;
        }
		List<String> nullValue = List.of("-", "null");
		if(nullValue.contains(utcTime)){
            return utcTime;
        }
        if (StringUtils.isNotEmpty(utcTime)) {
            LocalDateTime ldt = LocalDateTime.parse(utcTime);
            return tranformUTCLocalTimeToZFormat(ldt);
        }
        return utcTime;
    }

    public static LocalDateTime getTodayTime(){
        return DateUtil.localDateTimeToUTC(LocalDateTime.now());
    }

    public static LocalDate getTodayDate(){
        return DateUtil.localDateTimeToUTC(LocalDateTime.now()).toLocalDate();
    }

	public static LocalDateTime convertJodaDateTimeToLocalDateTime(DateTime dateTime) {
		Instant instant = Instant.ofEpochMilli(dateTime.getMillis());
		return LocalDateTime.ofInstant(instant,ZoneOffset.UTC);
	}
	/**
	 * Convert a string time to UTC LocalDateTime
	 *
	 * @param time
	 *            2024-10-17T23:08:15.6740000
	 * @return LocalDateTime
	 */
	public static LocalDateTime convertToUTCLocalDateTime(String time) {
		String truncatedTime = time.split("\\.")[0];
		return localDateTimeToUTC(LocalDateTime.parse(truncatedTime, DateTimeFormatter.ofPattern(TIME_FORMAT)));
	}
}
