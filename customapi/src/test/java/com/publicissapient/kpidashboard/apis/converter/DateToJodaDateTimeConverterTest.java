package com.publicissapient.kpidashboard.apis.converter;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Date;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class DateToJodaDateTimeConverterTest {
    @Test
    public void testConvert() {
        DateToJodaDateTimeConverter converter = new DateToJodaDateTimeConverter();
        DateTime expected = new DateTime(2022, 2, 2, 8, 10);
        DateTime actual = converter.convert(new Date(1643769600000L));
        assertEquals(expected, actual);
    }
}
