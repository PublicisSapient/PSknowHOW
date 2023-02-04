package com.publicissapient.kpidashboard.apis.pushdata.service;

import java.time.LocalDateTime;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.publicissapient.kpidashboard.common.util.DateUtil;

public abstract class BuildValidation {

	public abstract void checkStatus(String buildStatus, Map<String, String> errors);
	public void checkJobName(String jobName, Map<String, String> errors) {
		if (StringUtils.isBlank(jobName)) {
			errors.put("jobName", "jobName is Blank");
		}
	}

	public void checkNumber(String number, Map<String, String> errors) {
		if (StringUtils.isBlank(number)) {
			errors.put("number", "number is Blank");
		} else if (!StringUtils.isNumeric(number)) {
			errors.put("number", "number should be in digits");
		}
	}

	public void checkTimeDetails(Long startTime, Long endTime, Long duration, Map<String, String> errors) {
		LocalDateTime startLocalTime = DateUtil.convertMillisToLocalDateTime(startTime);
		LocalDateTime endLocalDateTime = DateUtil.convertMillisToLocalDateTime(endTime);
		if (!startLocalTime.isEqual(endLocalDateTime)) {
			if (startLocalTime.isAfter(endLocalDateTime)) {
				errors.put("startTime", "startTime is after endTime");
			}
			if (duration == 0) {
				errors.put("duration", "duration should be > 0");
			}
		}
	}

}
