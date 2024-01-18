package com.publicissapient.kpidashboard.common.util;

import com.publicissapient.kpidashboard.common.model.application.Week;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
        assertNotNull( DateUtil.convertMillisToDateTime(milliSeconds));
    }


}
