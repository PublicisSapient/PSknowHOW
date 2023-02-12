package com.publicissapient.kpidashboard.apis.pushdata.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.publicissapient.kpidashboard.apis.common.service.impl.BuildValidationServiceImpl;
import com.publicissapient.kpidashboard.apis.enums.PushValidationType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
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
public class BuildServiceImpl{

	@Autowired
	BuildRepository buildRepository;

	@Autowired
	BuildValidationServiceImpl buildValidationService;

	public int checkandCreateBuilds(ObjectId basicProjectConfigId, List<PushBuild> buildsList, List<Build> buildList,
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
					log.error("Errors in build for jobNumber "+pushBuild.getNumber()+ " jobName "+pushBuild.getJobName() +" are ",errorMap);
					buildDeployErrorData.setErrors(errorMap);
				} else {
					//if no errors are present in the input job then it will create Build List
					buildList.add(createBuild(basicProjectConfigId, pushBuild,
							checkExisitingJob(pushBuild, basicProjectConfigId)));
				}
				buildErrorList.add(buildDeployErrorData);
			});
		}
		return failedRecords.get();

	}

	/**
	 * validation data and creating error map for each validation
	 * @param pushBuild
	 * @return
	 */
	private Map<String, String> createErrorMap(PushBuild pushBuild) {
		Map<String, String> errors = new HashMap<>();
		Map<Pair<String,String>, List<PushValidationType>> validations=new HashMap<>();
		validations.put(Pair.of("jobName",pushBuild.getJobName()), Arrays.asList(PushValidationType.BLANK));
		validations.put(Pair.of("number",pushBuild.getNumber()), Arrays.asList(PushValidationType.BLANK,PushValidationType.NUMERIC));
		validations.put(Pair.of("buildStatus",pushBuild.getBuildStatus()), Arrays.asList(PushValidationType.BLANK,PushValidationType.BUILD_STATUS));
		validations.put(Pair.of("startTime",pushBuild.getStartTime().toString()), Arrays.asList(PushValidationType.BLANK,PushValidationType.TIME_DETAILS));
		validations.put(Pair.of("endTime",pushBuild.getEndTime().toString()), Arrays.asList(PushValidationType.BLANK,PushValidationType.TIME_DETAILS));
		validations.put(Pair.of("duration",pushBuild.getDuration().toString()), Arrays.asList(PushValidationType.BLANK,PushValidationType.TIME_DETAILS));
		buildValidationService.createErrorMap(validations,errors);
		return errors;
	}

	/**
	 * check existing job on the basis of jobName/jobNumber/basicprojectConfigId
	 * @param pushBuild
	 * @param basicProjectConfigId
	 * @return
	 */
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
		build.setJobFolder(pushBuild.getJobName());
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

}
