package com.publicissapient.kpidashboard.apis.converter;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Date;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class DateToJodaDateTimeConverterTest {

    @InjectMocks
    DateToJodaDateTimeConverter dateToJodaDateTimeConverter;
    @Test
    public void testConvert() {
        DateTime expected = new DateTime(2022, 2, 2, 0, 0);
        DateTime actual = dateToJodaDateTimeConverter.convert(new Date(1643769600000L));
        assertEquals(expected, actual);
    }
}
