package com.publicissapient.kpidashboard.apis.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class DeploymentFrequencyInfo {

	private List<String> jobNameList;
	private List<String> environmentList;
	private List<String> deploymentDateList;
	private List<String> monthList;

	public DeploymentFrequencyInfo() {
		this.jobNameList = new ArrayList<>();
		this.environmentList = new ArrayList<>();
		this.deploymentDateList = new ArrayList<>();
		this.monthList = new ArrayList<>();
	}

	public void addJobNameList(String jobName) {
		jobNameList.add(jobName);
	}

	public void addEnvironmentList(String environment) {
		environmentList.add(environment);
	}

	public void addDeploymentDateList(String date) {
		deploymentDateList.add(date);
	}

	public void addMonthList(String month) {
		monthList.add(month);
	}
}
