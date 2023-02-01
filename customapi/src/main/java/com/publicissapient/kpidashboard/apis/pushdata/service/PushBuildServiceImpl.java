package com.publicissapient.kpidashboard.apis.pushdata.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.pushdata.model.BuildDeployErrorData;
import com.publicissapient.kpidashboard.apis.pushdata.model.PushBuildDeploy;
import com.publicissapient.kpidashboard.apis.pushdata.model.PushBuildDeployResponse;
import com.publicissapient.kpidashboard.apis.pushdata.model.dto.PushBuild;
import com.publicissapient.kpidashboard.apis.pushdata.util.PushDataException;
import com.publicissapient.kpidashboard.common.constant.BuildStatus;
import com.publicissapient.kpidashboard.common.model.application.Build;

@Service
@Slf4j
public class PushBuildServiceImpl {

	public void processPushDataInput(PushBuildDeploy buildDeploy) {
		PushBuildDeployResponse pushBuildDeployResponse = checkSize(buildDeploy);
		List<Build> buildList = new ArrayList<>();
		List<BuildDeployErrorData> buildErrorList= new ArrayList<>();
		int buildFailedRecords = checkandCreateBuilds("",buildDeploy.getBuilds(), buildList,buildErrorList);
		pushBuildDeployResponse.setBuilds(buildErrorList);
		pushBuildDeployResponse.setTotalFailedRecords(buildFailedRecords);
		//pushBuildDeployResponse.setTotalSavedRecords();
		createResponse(pushBuildDeployResponse);

	}

	private void createResponse(PushBuildDeployResponse pushBuildDeployResponse) {
		if(pushBuildDeployResponse.getTotalRecords()!= pushBuildDeployResponse.getTotalSavedRecords()){

			throw new PushDataException("",pushBuildDeployResponse);
		}
	}

	private int checkandCreateBuilds(String basicProjectConfigId, List<PushBuild> buildsList, List<Build> buildList, List<BuildDeployErrorData> buildErrorList) {
		AtomicInteger failedRecords = new AtomicInteger();
		if (CollectionUtils.isNotEmpty(buildsList)) {
			buildsList.forEach(pushBuild -> {
				BuildDeployErrorData buildDeployErrorData = new BuildDeployErrorData();
				buildDeployErrorData.setJobName(pushBuild.getJobName());
				buildDeployErrorData.setNumber(pushBuild.getNumber());
				Map<String, String> errorMap = createErrorMap(pushBuild);
				if (MapUtils.isNotEmpty(errorMap)) {
					failedRecords.getAndIncrement();
					buildDeployErrorData.setErrors(errorMap);
				} else {
					buildList.add(createBuild(basicProjectConfigId,pushBuild));
				}
				buildErrorList.add(buildDeployErrorData);
			});
		}
		return failedRecords.get();

	}

	private Build createBuild(String basicProjectConfigId,PushBuild pushBuild) {
		Build build = new Build();
		build.setBasicProjectConfigId(new ObjectId(basicProjectConfigId));
		build.setBuildJob(pushBuild.getJobName());
		build.setNumber(pushBuild.getNumber());
		build.setBuildUrl(pushBuild.getBuildUrl());
		build.setStartTime(pushBuild.getStartTime());
		build.setEndTime(pushBuild.getEndTime());
		build.setDuration(pushBuild.getDuration());
		build.setBuildStatus(BuildStatus.fromString(pushBuild.getBuildStatus()));
		build.setTimestamp(System.currentTimeMillis());
		return build;
	}

	private Map<String, String> createErrorMap(PushBuild pushBuild) {
		Map<String, String> errors = new HashMap<>();
		if (StringUtils.isBlank(pushBuild.getJobName())) {
			errors.put("jobName", "jobName is Blank");
		}
		checkNumber(pushBuild.getNumber(), errors);
		checkBuildStatus(pushBuild.getBuildStatus(), errors);
		return errors;
	}

	private void checkBuildStatus(String buildStatus, Map<String, String> errors) {
		if (!BuildStatus.contains(buildStatus)) {
			errors.put("buildStatus", "value should be given format of request");
		}
	}

	private void checkNumber(String number, Map<String, String> errors) {
		if (StringUtils.isBlank(number)) {
			errors.put("number", "number is Blank");
		} else if (number.matches("[0-9]+")) {
			errors.put("number", "number should be in digits");
		}
	}

	private PushBuildDeployResponse checkSize(PushBuildDeploy buildDeploy) {
		if ((CollectionUtils.isNotEmpty(buildDeploy.getDeployments()) && buildDeploy.getDeployments().size() >= 50)
				|| (CollectionUtils.isNotEmpty(buildDeploy.getBuilds()) && buildDeploy.getBuilds().size() >= 50)) {
			throw new PushDataException("Maximum Limit of build/deployment is 50");
		}
		PushBuildDeployResponse pushBuildDeployResponse = new PushBuildDeployResponse();
		pushBuildDeployResponse.setTotalRecords(
				(CollectionUtils.isNotEmpty(buildDeploy.getDeployments()) ? buildDeploy.getDeployments().size() : 0)
						+ (CollectionUtils.isNotEmpty(buildDeploy.getBuilds()) ? buildDeploy.getBuilds().size() : 0));

		return pushBuildDeployResponse;
	}

}
