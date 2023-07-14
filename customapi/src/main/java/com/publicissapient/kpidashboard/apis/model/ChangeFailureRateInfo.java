package com.publicissapient.kpidashboard.apis.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ChangeFailureRateInfo {

	private List<String> buildJobNameList;
	private List<Integer> totalBuildCountList;
	private List<Integer> totalBuildFailureCountList;
	private List<Double> buildFailurePercentageList;
	private List<String> dateList;

	public ChangeFailureRateInfo() {
		this.buildJobNameList = new ArrayList<>();
		this.totalBuildCountList = new ArrayList<>();
		this.totalBuildFailureCountList = new ArrayList<>();
		this.buildFailurePercentageList = new ArrayList<>();
		this.dateList = new ArrayList<>();
	}

	public void addBuildJobNameList(String jobName) {
		buildJobNameList.add(jobName);
	}

	public void addTotalBuildCountList(Integer totalBuildCount) {
		totalBuildCountList.add(totalBuildCount);
	}

	public void addTotalBuildFailureCountList(Integer totalBuildFailureCount) {
		totalBuildFailureCountList.add(totalBuildFailureCount);
	}

	public void addBuildFailurePercentageList(Double buildFailurePercentage) {
		buildFailurePercentageList.add(buildFailurePercentage);
	}

	public void addDateList(String date) {
		dateList.add(date);
	}
}
