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

package com.publicissapient.kpidashboard.apis.pushdata.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;

import com.publicissapient.kpidashboard.common.constant.BuildStatus;
import com.publicissapient.kpidashboard.common.constant.DeploymentStatus;
import com.publicissapient.kpidashboard.common.util.DateUtil;

public class PushDataValidation {

	private static final String COMMA= " ,";
	private static final String SHOULD_BE_AMONG= " should be among ";

	private PushDataValidation(){}

	/**
	 * check the Blank
	 * 
	 * @param parameter
	 * @param literal
	 * @param errors
	 */
	public static void checkBlank(String parameter, String literal, Map<String, String> errors) {
		if (StringUtils.isBlank(literal)) {
			errors.computeIfPresent(parameter, (param, error) -> error.concat(COMMA + parameter + " is Blank"));
			errors.putIfAbsent(parameter, parameter + " is Blank");
		}
	}

	/**
	 * check if input is numeric
	 * 
	 * @param parameter
	 * @param number
	 * @param errors
	 */
	public static void checkNumeric(String parameter, String number, Map<String, String> errors) {
		if (!StringUtils.isNumeric(number)) {
			errors.computeIfPresent(parameter,
					(param, error) -> error.concat(COMMA + parameter + " should be in digits"));
			errors.putIfAbsent(parameter, parameter + " should be in digits");
		}
	}

	/**
	 * check if status is among Build Statuses
	 * 
	 * @param parameter
	 * @param status
	 * @param errors
	 */
	public static void checkBuildStatus(String parameter, String status, Map<String, String> errors) {
		Optional<BuildStatus> buildStatusOptional = Arrays.stream(BuildStatus.values())
				.filter(buildStatus -> buildStatus.toString().equalsIgnoreCase(status)).findFirst();
		if (!buildStatusOptional.isPresent()) {
			errors.computeIfPresent(parameter,
					(param, error) -> error.concat(COMMA + parameter + SHOULD_BE_AMONG + getAllBuildValues()));
			errors.putIfAbsent(parameter, parameter + SHOULD_BE_AMONG + getAllBuildValues());
		}

	}

	/**
	 * check if status among Deployments Statuses
	 * 
	 * @param parameter
	 * @param status
	 * @param errors
	 */
	public static void checkDeploymentStatus(String parameter, String status, Map<String, String> errors) {
		Optional<DeploymentStatus> deploymentStatusOptional = Arrays.stream(DeploymentStatus.values())
				.filter(deploymentStatus -> deploymentStatus.toString().equalsIgnoreCase(status)).findFirst();
		if (!deploymentStatusOptional.isPresent()) {
			errors.computeIfPresent(parameter,
					(param, error) -> error.concat(COMMA + parameter + SHOULD_BE_AMONG + getAllDeploymentValues()));
			errors.putIfAbsent(parameter, parameter + SHOULD_BE_AMONG + getAllDeploymentValues());
		}
	}

	private static String getAllBuildValues() {
		StringBuilder allStatus = new StringBuilder();
		for (BuildStatus buildStatus : BuildStatus.values()) {
			allStatus.append(buildStatus + "/");
		}
		return allStatus.substring(0, allStatus.length() - 1);
	}

	private static String getAllDeploymentValues() {
		StringBuilder allStatus = new StringBuilder();
		for (DeploymentStatus deploymentStatus : DeploymentStatus.values()) {
			allStatus.append(deploymentStatus + "/");
		}
		return allStatus.substring(0, allStatus.length() - 1);
	}

	/**
	 * check for time details and duration
	 * 
	 * @param startTime
	 * @param endTime
	 * @param duration
	 * @param errors
	 */
	public static void checkTimeDetails(String startTime, String endTime, String duration, Map<String, String> errors) {
		LocalDateTime startLocalTime = DateUtil.convertMillisToLocalDateTime(Long.parseLong(startTime));
		LocalDateTime endLocalDateTime = DateUtil.convertMillisToLocalDateTime(Long.parseLong(endTime));
		if (!startLocalTime.isEqual(endLocalDateTime)) {
			if (startLocalTime.isAfter(endLocalDateTime)) {
				errors.put("startTime", "startTime is after endTime");
			}
			if (Long.parseLong(duration) == 0) {
				errors.put("duration", "duration should be > 0");
			}
		}
	}

}
