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

package com.publicissapient.kpidashboard.apis.pushdata.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.enums.PushValidationType;
import com.publicissapient.kpidashboard.apis.pushdata.service.PushDataValidation;

@Component
public class PushDataValidationServiceImpl {

	private static final String START_TIME = "startTime";
	private static final String END_TIME = "endTime";
	private static final String DURATION = "duration";

	/**
	 * create Build and Deploy Error Map based on the required types
	 * 
	 * @param validations
	 * @return
	 */
	public Map<String, String> createBuildDeployErrorMap(
			Map<Pair<String, String>, List<PushValidationType>> validations) {
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
			PushDataValidation.checkTimeDetails(startTime.get(), endTime.get(), duration.get(), errors);
		}
	}

	private boolean parameterWiseError(Map<String, String> errors, String parameter, String parameterValue,
			PushValidationType validate) {
		boolean timeCheck = false;
		switch (validate) {
		case BLANK:
			PushDataValidation.checkBlank(parameter, parameterValue, errors);
			break;
		case NUMERIC:
			PushDataValidation.checkNumeric(parameter, parameterValue, errors);
			break;
		case BUILD_STATUS:
			PushDataValidation.checkBuildStatus(parameter, parameterValue, errors);
			break;
		case DEPLOYMENT_STATUS:
			PushDataValidation.checkDeploymentStatus(parameter, parameterValue, errors);
			break;
		case TIME_DETAILS:
			timeCheck = true;
			break;
		default:
		}
		return timeCheck;
	}
}
