package com.publicissapient.kpidashboard.apis.common.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.common.service.PushDataValidation;
import com.publicissapient.kpidashboard.apis.enums.PushValidationType;

@Component
public class PushDataValidationServiceImpl extends PushDataValidation {

	private static final String START_TIME = "startTime";
	private static final String END_TIME = "endTime";
	private static final String DURATION = "duration";

	public Map<String, String> createBuildDeployErrorMap(Map<Pair<String, String>, List<PushValidationType>> validations) {
		Map<String, String> errors = new HashMap<>();
		if (MapUtils.isNotEmpty(validations)) {
			AtomicBoolean timeCheck = new AtomicBoolean(false);
			validations.forEach((key, value) -> {
				String parameter = key.getLeft();
				String parameterValue = key.getRight();
				value.forEach(validate -> {
					boolean lasttimeCheck = parameterWiseError(errors, parameter, parameterValue, validate);
					if (!timeCheck.get()) {
						timeCheck.set(lasttimeCheck);
					}
				});
			});
			checkRequiredTimeDetails(errors, validations, timeCheck.get());
		}
		return errors;
	}

	private void checkRequiredTimeDetails(Map<String, String> errors,
			Map<Pair<String, String>, List<PushValidationType>> validations, boolean timeCheck) {
		Set<Pair<String, String>> pairs = validations.keySet();
		List<String> leftKey = pairs.stream().map(Pair::getLeft).collect(Collectors.toList());
		if (timeCheck
				&& !(errors.containsKey(START_TIME) && errors.containsKey(END_TIME) && errors.containsKey(DURATION))
				&& (leftKey.contains(START_TIME) && leftKey.contains(END_TIME) && leftKey.contains(DURATION))) {
			AtomicReference<String> startTime = new AtomicReference<>();
			AtomicReference<String> endTime = new AtomicReference<>();
			AtomicReference<String> duration = new AtomicReference<>();
			pairs.forEach(pair -> {
				if (pair.getLeft().equals(START_TIME)) {
					startTime.set(pair.getValue());
				}
				if (pair.getLeft().equals(END_TIME)) {
					endTime.set(pair.getValue());
				}
				if (pair.getLeft().equals(DURATION)) {
					duration.set(pair.getValue());
				}
			});
			checkTimeDetails(startTime.get(), endTime.get(), duration.get(), errors);
		}
	}

	private boolean parameterWiseError(Map<String, String> errors, String parameter, String parameterValue,
			PushValidationType validate) {
		boolean timeCheck = false;
		switch (validate) {
		case BLANK:
			checkBlank(parameter, parameterValue, errors);
			break;
		case NUMERIC:
			checkNumeric(parameter, parameterValue, errors);
			break;
		case BUILD_STATUS:
			checkBuildStatus(parameter, parameterValue, errors);
			break;
		case DEPLOYMENT_STATUS:
			checkDeploymentStatus(parameter, parameterValue, errors);
			break;
		case TIME_DETAILS:
			timeCheck = true;
			break;
		default:
		}
		return timeCheck;
	}
}
