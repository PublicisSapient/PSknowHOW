package com.publicissapient.kpidashboard.apis.pushdata.service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.publicissapient.kpidashboard.apis.common.service.BuildValidation;
import com.publicissapient.kpidashboard.apis.common.service.impl.BuildValidationServiceImpl;
import com.publicissapient.kpidashboard.apis.enums.PushValidationType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;

import com.publicissapient.kpidashboard.apis.pushdata.model.BuildDeployErrorData;
import com.publicissapient.kpidashboard.apis.pushdata.model.dto.PushDeploy;
import com.publicissapient.kpidashboard.common.constant.DeploymentStatus;
import com.publicissapient.kpidashboard.common.model.application.Deployment;
import com.publicissapient.kpidashboard.common.repository.application.DeploymentRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DeployServiceImpl{

	@Autowired
	DeploymentRepository deploymentRepository;

	@Autowired
	BuildValidationServiceImpl buildValidationService;

	public int checkandCreateDeployment(String basicProjectConfigId, List<PushDeploy> deployList,
			List<Deployment> deploymentList, List<BuildDeployErrorData> deployErrorList) {
		AtomicInteger failedRecords = new AtomicInteger();
		if (CollectionUtils.isNotEmpty(deployList)) {
			deployList.forEach(pushDeploy -> {
				BuildDeployErrorData buildDeployErrorData = new BuildDeployErrorData();
				buildDeployErrorData.setJobName(pushDeploy.getJobName());
				buildDeployErrorData.setNumber(pushDeploy.getNumber());
				Map<String, String> errorMap = createErrorMap(pushDeploy);
				if (MapUtils.isNotEmpty(errorMap)) {
					failedRecords.getAndIncrement();
					log.error("Errors in deploy for jobNumber "+pushDeploy.getNumber()+ " jobName "+pushDeploy.getJobName() +" are ",errorMap);
					buildDeployErrorData.setErrors(errorMap);
				} else {
					//if no errors are present in the input job then it will create Deployment List
					ObjectId basicProjectObjectConfigId = new ObjectId(basicProjectConfigId);
					deploymentList.add(createDeployment(basicProjectObjectConfigId, pushDeploy,
							checkExisitingJob(pushDeploy, basicProjectObjectConfigId)));
				}
				deployErrorList.add(buildDeployErrorData);
			});
		}
		return failedRecords.get();

	}

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
	 *  validation data and creating error map for each validation
	 * @param pushDeploy
	 * @return
	 */
	private Map<String, String> createErrorMap(PushDeploy pushDeploy) {
		Map<String, String> errors = new HashMap<>();
		Map<Pair<String,String>, List<PushValidationType>> validations=new HashMap<>();
		validations.put(Pair.of("jobName",pushDeploy.getJobName()), Arrays.asList(PushValidationType.BLANK));
		validations.put(Pair.of("number",pushDeploy.getNumber()), Arrays.asList(PushValidationType.BLANK));
		validations.put(Pair.of("deploymentStatus",pushDeploy.getDeploymentStatus()), Arrays.asList(PushValidationType.BLANK,PushValidationType.DEPLOYMENT_STATUS));
		validations.put(Pair.of("envName",pushDeploy.getEnvName()), Arrays.asList(PushValidationType.BLANK));
		validations.put(Pair.of("startTime",pushDeploy.getStartTime().toString()), Arrays.asList(PushValidationType.BLANK));
		validations.put(Pair.of("endTime",pushDeploy.getEndTime().toString()), Arrays.asList(PushValidationType.BLANK));
		validations.put(Pair.of("duration",pushDeploy.getDuration().toString()), Arrays.asList(PushValidationType.BLANK));
		buildValidationService.createErrorMap(validations,errors);
		return errors;
	}


}
