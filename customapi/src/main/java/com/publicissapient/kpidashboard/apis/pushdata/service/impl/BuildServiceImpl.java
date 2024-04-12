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

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.enums.PushValidationType;
import com.publicissapient.kpidashboard.apis.pushdata.model.PushDataDetail;
import com.publicissapient.kpidashboard.apis.pushdata.model.PushErrorData;
import com.publicissapient.kpidashboard.apis.pushdata.model.dto.PushBuild;
import com.publicissapient.kpidashboard.common.constant.BuildStatus;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.repository.application.BuildRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BuildServiceImpl {

	@Autowired
	BuildRepository buildRepository;

	@Autowired
	PushDataValidationServiceImpl pushDataValidationService;

	/**
	 *
	 * @param basicProjectConfigId
	 * @param buildsList
	 * @param buildList
	 * @param buildErrorList
	 * @param pushDataDetails
	 * @return
	 */
	public int checkandCreateBuilds(ObjectId basicProjectConfigId, Set<PushBuild> buildsList, List<Build> buildList,
			List<PushErrorData> buildErrorList, List<PushDataDetail> pushDataDetails) {
		AtomicInteger failedRecords = new AtomicInteger();
		if (CollectionUtils.isNotEmpty(buildsList)) {
			buildsList.forEach(pushBuild -> {
				PushErrorData pushErrorData = new PushErrorData();
				pushErrorData.setJobName(pushBuild.getJobName());
				pushErrorData.setNumber(pushBuild.getNumber());
				Map<String, String> errorMap = createErrorMap(pushBuild);
				if (MapUtils.isNotEmpty(errorMap)) {
					failedRecords.getAndIncrement();
					log.error("Errors in build for jobNumber " + pushBuild.getNumber() + " jobName "
							+ pushBuild.getJobName() + " are ", errorMap);
					pushErrorData.setErrors(errorMap);
				} else {
					// if no errors are present in the input job then it will create Build List
					buildList.add(createBuild(basicProjectConfigId, pushBuild,
							checkExisitingJob(pushBuild, basicProjectConfigId)));
				}
				pushDataDetails.add(createTraceLog(pushErrorData));
				buildErrorList.add(pushErrorData);
			});
		}
		return failedRecords.get();

	}

	private PushDataDetail createTraceLog(PushErrorData pushErrorData) {
		PushDataDetail pushDataDetail = new PushDataDetail();
		pushDataDetail.setTool("build");
		pushDataDetail.setJobName(pushErrorData.getJobName());
		pushDataDetail.setJobNumber(pushErrorData.getNumber());
		List<String> errors = new ArrayList<>();
		if (MapUtils.isNotEmpty(pushErrorData.getErrors())) {
			pushErrorData.getErrors().forEach((k, v) -> errors.add(k + ":" + v));
		}
		pushDataDetail.setErrors(errors);
		return pushDataDetail;
	}

	/**
	 * validation data and creating error map for each validation
	 * 
	 * @param pushBuild
	 * @return
	 */
	private Map<String, String> createErrorMap(PushBuild pushBuild) {
		Map<Pair<String, String>, List<PushValidationType>> validations = new HashMap<>();
		validations.put(Pair.of("jobName", pushBuild.getJobName()), Arrays.asList(PushValidationType.BLANK));
		validations.put(Pair.of("number", pushBuild.getNumber()),
				Arrays.asList(PushValidationType.BLANK, PushValidationType.NUMERIC));
		validations.put(Pair.of("buildStatus", pushBuild.getBuildStatus()),
				Arrays.asList(PushValidationType.BLANK, PushValidationType.BUILD_STATUS));
		validations.put(Pair.of("startTime", pushBuild.getStartTime().toString()),
				Arrays.asList(PushValidationType.BLANK, PushValidationType.TIME_DETAILS));
		validations.put(Pair.of("endTime", pushBuild.getEndTime().toString()),
				Arrays.asList(PushValidationType.BLANK, PushValidationType.TIME_DETAILS));
		validations.put(Pair.of("duration", pushBuild.getDuration().toString()),
				Arrays.asList(PushValidationType.BLANK, PushValidationType.TIME_DETAILS));
		return pushDataValidationService.createBuildDeployErrorMap(validations);
	}

	/**
	 * check existing job on the basis of jobName/jobNumber/basicprojectConfigId
	 * 
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
		build.setUpdatedTime(DateUtil.dateTimeFormatter(
				Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.systemDefault()).toLocalDateTime(),
				DateUtil.TIME_FORMAT));
		return build;
	}

}
