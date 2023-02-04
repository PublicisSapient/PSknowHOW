package com.publicissapient.kpidashboard.apis.pushdata.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;

import com.publicissapient.kpidashboard.apis.pushdata.model.BuildDeployErrorData;
import com.publicissapient.kpidashboard.apis.pushdata.model.dto.PushBuild;
import com.publicissapient.kpidashboard.common.constant.BuildStatus;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.repository.application.BuildRepository;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BuildServiceImpl extends BuildValidation {

	@Autowired
	BuildRepository buildRepository;

	public int checkandCreateBuilds(String basicProjectConfigId, List<PushBuild> buildsList, List<Build> buildList,
			List<BuildDeployErrorData> buildErrorList) {
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
					ObjectId basicProjectObjectConfigId = new ObjectId(basicProjectConfigId);
					buildList.add(createBuild(basicProjectObjectConfigId, pushBuild,
							checkExisitingJob(pushBuild, basicProjectObjectConfigId)));
				}
				buildErrorList.add(buildDeployErrorData);
			});
		}
		return failedRecords.get();

	}

	private Map<String, String> createErrorMap(PushBuild pushBuild) {
		Map<String, String> errors = new HashMap<>();
		checkJobName(pushBuild.getJobName(), errors);
		checkNumber(pushBuild.getNumber(), errors);
		checkTimeDetails(pushBuild.getStartTime(), pushBuild.getEndTime(), pushBuild.getDuration(), errors);
		checkStatus(pushBuild.getBuildStatus(), errors);
		return errors;
	}

	private Build checkExisitingJob(PushBuild pushBuild, ObjectId basicProjectConfigId) {
		return buildRepository.findByNumberAndBuildJobAndBasicProjectConfigId(pushBuild.getNumber(),
				pushBuild.getJobName(), basicProjectConfigId);
	}

	protected void saveBuilds(List<Build> buildList) {
		buildRepository.saveAll(buildList);
	}

	private Build createBuild(ObjectId basicProjectConfigId, PushBuild pushBuild, Build existingBuild) {
		Build build = (existingBuild != null) ? existingBuild : new Build();
		build.setBasicProjectConfigId(basicProjectConfigId);
		build.setBuildJob(pushBuild.getJobName());
		build.setNumber(pushBuild.getNumber());
		build.setBuildUrl(pushBuild.getBuildUrl());
		build.setStartTime(pushBuild.getStartTime());
		build.setEndTime(pushBuild.getEndTime());
		build.setDuration(pushBuild.getDuration());
		build.setBuildStatus(BuildStatus.fromString(pushBuild.getBuildStatus()));
		build.setTimestamp(build.getTimestamp() == 0 ? System.currentTimeMillis() : build.getTimestamp());
		build.setUpdateTimestamp(System.currentTimeMillis());
		return build;
	}

	@Override
	public void checkStatus(String buildStatus, Map<String, String> errors) {
		if (!BuildStatus.contains(buildStatus)) {
			errors.put("buildStatus", "buildStatus should be among "+BuildStatus.getAllValues());
		}
	}


}
