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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.pushdata.model.PushBuildDeploy;
import com.publicissapient.kpidashboard.apis.pushdata.model.PushDataDetail;
import com.publicissapient.kpidashboard.apis.pushdata.model.PushDataResponse;
import com.publicissapient.kpidashboard.apis.pushdata.model.PushDataTraceLog;
import com.publicissapient.kpidashboard.apis.pushdata.model.PushErrorData;
import com.publicissapient.kpidashboard.apis.pushdata.model.dto.PushBuild;
import com.publicissapient.kpidashboard.apis.pushdata.model.dto.PushDeploy;
import com.publicissapient.kpidashboard.apis.pushdata.service.PushBaseService;
import com.publicissapient.kpidashboard.apis.pushdata.service.PushDataTraceLogService;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.Deployment;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PushBuildServiceImpl implements PushBaseService {

	@Autowired
	CustomApiConfig customApiConfig;
	@Autowired
	private BuildServiceImpl buildService;
	@Autowired
	private DeployServiceImpl deployService;
	@Autowired
	private PushDataTraceLogService pushDataTraceLogService;
	@Autowired
	private CacheService cacheService;

	/**
	 * validate pushed buildDeploy data and if all requested data is valid then only
	 * saved in db , otherwise rejected all data and show errors msg of particular
	 * failed data
	 * 
	 * @param buildDeploy
	 * @param projectConfigId
	 * @return
	 */

	@Override
	public PushDataResponse processPushDataInput(PushBuildDeploy buildDeploy, ObjectId projectConfigId) {
		PushDataResponse pushDataResponse = new PushDataResponse();
		pushDataResponse.setTotalRecords(getTotalRecords(buildDeploy));
		log.info("Total Records input for " + projectConfigId.toHexString() + " are "
				+ pushDataResponse.getTotalRecords());
		List<Build> buildList = new ArrayList<>();
		List<Deployment> deploymentList = new ArrayList<>();
		List<PushErrorData> buildErrorList = new ArrayList<>();
		List<PushErrorData> deployErrorList = new ArrayList<>();
		List<PushDataDetail> pushDataDetails = new ArrayList<>();
		int buildFailedRecords = buildService.checkandCreateBuilds(projectConfigId, buildDeploy.getBuilds(), buildList,
				buildErrorList, pushDataDetails);
		int deployFailedRecords = deployService.checkandCreateDeployment(projectConfigId, buildDeploy.getDeployments(),
				deploymentList, deployErrorList, pushDataDetails);
		pushDataResponse.setBuilds(buildErrorList);
		pushDataResponse.setDeploy(deployErrorList);
		pushDataResponse.setTotalFailedRecords(buildFailedRecords + deployFailedRecords);
		pushDataResponse.setTotalSavedRecords(buildList.size() + deploymentList.size());
		log.info(
				"Total Records for " + projectConfigId + " to be Saved are " + pushDataResponse.getTotalSavedRecords());
		totalSaveRecords(pushDataResponse, buildList, deploymentList, pushDataDetails);
		return pushDataResponse;

	}

	/**
	 * partial correct data will not be saved to respective dba
	 * 
	 * @param pushDataResponse
	 * @param buildList
	 * @param deploymentList
	 * @param pushDataDetails
	 */
	private void totalSaveRecords(PushDataResponse pushDataResponse, List<Build> buildList,
			List<Deployment> deploymentList, List<PushDataDetail> pushDataDetails) {
		PushDataTraceLog instance = PushDataTraceLog.getInstance();
		instance.setTotalRecord(pushDataResponse.getTotalRecords());
		instance.setTotalFailedRecord(pushDataResponse.getTotalFailedRecords());
		if (pushDataResponse.getTotalFailedRecords() > 0) {
			pushDataResponse.setTotalSavedRecords(0);
			instance.setTotalSavedRecord(0);
			instance.setPushDataDetails(pushDataDetails);
			pushDataTraceLogService.setExceptionTraceLog("Errors in particular below ids", pushDataResponse);
		}
		instance.setTotalSavedRecord(pushDataResponse.getTotalSavedRecords());
		pushDataTraceLogService.save(instance);
		buildService.saveBuilds(buildList);
		deployService.saveDeployments(deploymentList);
		cacheService.clearCache(CommonConstant.JENKINS_KPI_CACHE);
	}

	/**
	 * if input record is more than set value, then throw exception
	 * 
	 * @param buildDeploy
	 * @return
	 */
	public int getTotalRecords(PushBuildDeploy buildDeploy) {
		if ((CollectionUtils.isNotEmpty(buildDeploy.getDeployments())
				&& buildDeploy.getDeployments().size() > customApiConfig.getPushDataLimit())
				|| (CollectionUtils.isNotEmpty(buildDeploy.getBuilds())
						&& buildDeploy.getBuilds().size() > customApiConfig.getPushDataLimit())) {
			Set<PushDeploy> pushDeploys = Optional.ofNullable(buildDeploy.getDeployments()).orElse(new HashSet<>());
			Set<PushBuild> pushBuilds = Optional.ofNullable(buildDeploy.getBuilds()).orElse(new HashSet<>());
			pushDataTraceLogService.setExceptionTraceLog(
					"Maximum Limit of build/deployment is " + customApiConfig.getPushDataLimit() + ", input-builds are "
							+ pushBuilds.size() + " and input-deployments are " + pushDeploys.size(),
					null);
		}
		return (CollectionUtils.isNotEmpty(buildDeploy.getDeployments()) ? buildDeploy.getDeployments().size() : 0)
				+ (CollectionUtils.isNotEmpty(buildDeploy.getBuilds()) ? buildDeploy.getBuilds().size() : 0);
	}

}
