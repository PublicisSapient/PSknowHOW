package com.publicissapient.kpidashboard.apis.common.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;

import com.publicissapient.kpidashboard.common.constant.BuildStatus;
import com.publicissapient.kpidashboard.common.constant.DeploymentStatus;
import com.publicissapient.kpidashboard.common.util.DateUtil;

public abstract class BuildValidation {

	protected void checkBlank(String parameter, String literal, Map<String, String> errors) {
		if (StringUtils.isBlank(literal)) {
			errors.computeIfPresent(parameter,(param,error)->error.concat(" ,"+parameter + " is Blank"));
			errors.putIfAbsent(parameter, parameter + " is Blank");
		}
	}

	protected void checkNumeric(String parameter, String number, Map<String, String> errors) {
		if (!StringUtils.isNumeric(number)) {
			errors.computeIfPresent(parameter,(param,error)->error.concat(" ,"+parameter + " should be in digits"));
			errors.putIfAbsent(parameter, parameter + " should be in digits");
		}
	}

	protected void checkBuildStatus(String parameter, String status, Map<String, String> errors) {
		Optional<BuildStatus> buildStatusOptional = Arrays.stream(BuildStatus.values())
				.filter(buildStatus -> buildStatus.toString().equalsIgnoreCase(status)).findFirst();
		if (!buildStatusOptional.isPresent()) {
			errors.computeIfPresent(parameter,(param,error)->error.concat(" ,"+parameter + " should be among " + getAllBuildValues()));
			errors.putIfAbsent(parameter, parameter +" should be among " + getAllBuildValues());
		}

	}

	protected void checkDeploymentStatus(String parameter, String status, Map<String, String> errors) {
		Optional<DeploymentStatus> deploymentStatusOptional = Arrays.stream(DeploymentStatus.values())
				.filter(deploymentStatus -> deploymentStatus.toString().equalsIgnoreCase(status)).findFirst();
		if (!deploymentStatusOptional.isPresent()) {
			errors.computeIfPresent(parameter,(param,error)->error.concat(" ,"+parameter + " should be among " + getAllDeploymentValues()));
			errors.putIfAbsent(parameter, parameter +" should be among " + getAllDeploymentValues());
		}
	}

	protected String getAllBuildValues() {
		StringBuilder allStatus = new StringBuilder();
		for (BuildStatus buildStatus : BuildStatus.values()) {
			allStatus.append(buildStatus + "/");
		}
		return allStatus.substring(0, allStatus.length() - 1);
	}

	protected String getAllDeploymentValues() {
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
	protected void checkTimeDetails(String startTime, String endTime, String duration, Map<String, String> errors) {
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
