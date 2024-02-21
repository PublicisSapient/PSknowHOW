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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.enums.PushValidationType;
import com.publicissapient.kpidashboard.apis.pushdata.model.PushDataDetail;
import com.publicissapient.kpidashboard.apis.pushdata.model.PushErrorData;
import com.publicissapient.kpidashboard.apis.pushdata.model.dto.PushDeploy;
import com.publicissapient.kpidashboard.common.constant.DeploymentStatus;
import com.publicissapient.kpidashboard.common.model.application.Deployment;
import com.publicissapient.kpidashboard.common.repository.application.DeploymentRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DeployServiceImpl {

	@Autowired
	DeploymentRepository deploymentRepository;

	@Autowired
	PushDataValidationServiceImpl buildValidationService;

	/**
	 *
	 * @param basicProjectConfigId
	 * @param deployList
	 * @param deploymentList
	 * @param deployErrorList
	 * @param pushDataDetails
	 * @return
	 */
	public int checkandCreateDeployment(ObjectId basicProjectConfigId, Set<PushDeploy> deployList,
			List<Deployment> deploymentList, List<PushErrorData> deployErrorList,
			List<PushDataDetail> pushDataDetails) {
		AtomicInteger failedRecords = new AtomicInteger();
		if (CollectionUtils.isNotEmpty(deployList)) {
			deployList.forEach(pushDeploy -> {
				PushErrorData pushErrorData = new PushErrorData();
				pushErrorData.setJobName(pushDeploy.getJobName());
				pushErrorData.setNumber(pushDeploy.getNumber());
				Map<String, String> errorMap = createErrorMap(pushDeploy);
				if (MapUtils.isNotEmpty(errorMap)) {
					failedRecords.getAndIncrement();
					log.error("Errors in deploy for jobNumber " + pushDeploy.getNumber() + " jobName "
							+ pushDeploy.getJobName() + " are ", errorMap);
					pushErrorData.setErrors(errorMap);
				} else {
					// if no errors are present in the input job then it will create Deployment List
					deploymentList.add(createDeployment(basicProjectConfigId, pushDeploy,
							checkExisitingJob(pushDeploy, basicProjectConfigId)));
				}
				pushDataDetails.add(createTraceLog(pushErrorData));
				deployErrorList.add(pushErrorData);
			});
		}
		return failedRecords.get();

	}

	private PushDataDetail createTraceLog(PushErrorData pushErrorData) {
		PushDataDetail pushDataDetail = new PushDataDetail();
		pushDataDetail.setTool("deploy");
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
	 * create Deployment Object based on the existence in collection
	 * 
	 * @param basicProjectConfigId
	 * @param pushDeploy
	 * @param checkExisitingDeployment
	 * @return
	 */
	private Deployment createDeployment(ObjectId basicProjectConfigId, PushDeploy pushDeploy,
			Deployment checkExisitingDeployment) {
		Deployment deployment = (checkExisitingDeployment != null) ? checkExisitingDeployment : new Deployment();
		deployment.setBasicProjectConfigId(basicProjectConfigId);
		deployment.setJobName(pushDeploy.getJobName());
		deployment.setNumber(pushDeploy.getNumber());
		deployment.setEnvName(pushDeploy.getEnvName());
		deployment.setStartTime(DateUtil.dateTimeFormatter(new Date(pushDeploy.getStartTime()), DateUtil.TIME_FORMAT));
		deployment.setEndTime(DateUtil.dateTimeFormatter(new Date(pushDeploy.getEndTime()), DateUtil.TIME_FORMAT));
		deployment.setDuration(pushDeploy.getDuration());
		deployment.setDeploymentStatus(DeploymentStatus.fromString(pushDeploy.getDeploymentStatus()));
		deployment.setCreatedAt(StringUtils.isEmpty(deployment.getCreatedAt())
				? DateUtil.dateTimeFormatter(Instant.ofEpochMilli(System.currentTimeMillis())
						.atZone(ZoneId.systemDefault()).toLocalDateTime(), DateUtil.TIME_FORMAT)
				: deployment.getCreatedAt());
		deployment.setUpdatedTime(DateUtil.dateTimeFormatter(
				Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.systemDefault()).toLocalDateTime(),
				DateUtil.TIME_FORMAT));
		return deployment;
	}

	/**
	 * check existing job on the basis of jobName/jobNumber/basicprojectConfigId
	 * 
	 * @param pushDeploy
	 * @param basicProjectObjectConfigId
	 * @return
	 */
	private Deployment checkExisitingJob(PushDeploy pushDeploy, ObjectId basicProjectObjectConfigId) {
		return deploymentRepository.findByNumberAndJobNameAndBasicProjectConfigId(pushDeploy.getNumber(),
				pushDeploy.getJobName(), basicProjectObjectConfigId);
	}

	protected void saveDeployments(List<Deployment> deploymentList) {
		deploymentRepository.saveAll(deploymentList);
	}

	/**
	 * validation data and creating error map for each validation
	 * 
	 * @param pushDeploy
	 * @return
	 */
	private Map<String, String> createErrorMap(PushDeploy pushDeploy) {
		Map<Pair<String, String>, List<PushValidationType>> validations = new HashMap<>();
		validations.put(Pair.of("jobName", pushDeploy.getJobName()), Arrays.asList(PushValidationType.BLANK));
		validations.put(Pair.of("number", pushDeploy.getNumber()), Arrays.asList(PushValidationType.BLANK));
		validations.put(Pair.of("deploymentStatus", pushDeploy.getDeploymentStatus()),
				Arrays.asList(PushValidationType.BLANK, PushValidationType.DEPLOYMENT_STATUS));
		validations.put(Pair.of("envName", pushDeploy.getEnvName()), Arrays.asList(PushValidationType.BLANK));
		validations.put(Pair.of("startTime", pushDeploy.getStartTime().toString()),
				Arrays.asList(PushValidationType.BLANK));
		validations.put(Pair.of("endTime", pushDeploy.getEndTime().toString()),
				Arrays.asList(PushValidationType.BLANK));
		validations.put(Pair.of("duration", pushDeploy.getDuration().toString()),
				Arrays.asList(PushValidationType.BLANK));
		return buildValidationService.createBuildDeployErrorMap(validations);
	}

}
