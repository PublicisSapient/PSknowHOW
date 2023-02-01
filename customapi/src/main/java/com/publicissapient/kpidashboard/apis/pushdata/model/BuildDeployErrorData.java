package com.publicissapient.kpidashboard.apis.pushdata.model;

import java.util.Map;

import lombok.Data;

@Data
public class BuildDeployErrorData {
	private String jobName;
	private String number;
	Map<String, String> errors;
}
