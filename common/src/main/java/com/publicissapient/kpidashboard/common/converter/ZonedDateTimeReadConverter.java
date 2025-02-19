package com.publicissapient.kpidashboard.common.converter;

import java.util.Date;

import org.joda.time.DateTime;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class ZonedDateTimeReadConverter implements Converter<Date, DateTime> {

	@Override
	public DateTime convert(Date source) {
		return new DateTime(source);
	}
}
