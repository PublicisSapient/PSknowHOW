package com.publicissapient.kpidashboard.apis.pushdata.model;

import java.util.List;

import lombok.Data;

@Data
public class PushBuildDeployResponse {
	private int totalRecords;
	private int totalSavedRecords;
	private int totalFailedRecords;
	List<BuildDeployErrorData> builds;
	List<BuildDeployErrorData> deploy;

}
