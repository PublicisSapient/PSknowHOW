package com.publicissapient.kpidashboard.common.converter;

import java.util.Date;

import org.joda.time.DateTime;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class ZonedDateTimeWriteConverter implements Converter<DateTime, Date> {

	@Override
	public Date convert(DateTime source) {
		return source.toDate();
	}
}
