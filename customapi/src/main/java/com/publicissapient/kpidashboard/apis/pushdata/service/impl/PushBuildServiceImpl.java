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
import java.util.List;

import com.publicissapient.kpidashboard.apis.pushdata.service.PushBaseService;
import com.publicissapient.kpidashboard.apis.pushdata.service.impl.BuildServiceImpl;
import com.publicissapient.kpidashboard.apis.pushdata.service.impl.DeployServiceImpl;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.pushdata.model.PushBuildDeploy;
import com.publicissapient.kpidashboard.apis.pushdata.model.PushDataResponse;
import com.publicissapient.kpidashboard.apis.pushdata.model.PushErrorData;
import com.publicissapient.kpidashboard.apis.pushdata.util.PushDataException;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.Deployment;

@Service
@Slf4j
public class PushBuildServiceImpl implements PushBaseService {

	@Autowired
	private BuildServiceImpl buildService;

	@Autowired
	private DeployServiceImpl deployService;

	@Autowired
	private CacheService cacheService;

	@Autowired
	CustomApiConfig customApiConfig;

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
		int buildFailedRecords = buildService.checkandCreateBuilds(projectConfigId, buildDeploy.getBuilds(), buildList,
				buildErrorList);
		int deployFailedRecords = deployService.checkandCreateDeployment(projectConfigId, buildDeploy.getDeployments(),
				deploymentList, deployErrorList);
		pushDataResponse.setBuilds(buildErrorList);
		pushDataResponse.setDeploy(deployErrorList);
		pushDataResponse.setTotalFailedRecords(buildFailedRecords + deployFailedRecords);
		pushDataResponse.setTotalSavedRecords(buildList.size() + deploymentList.size());
		log.info(
				"Total Records for " + projectConfigId + " to be Saved are " + pushDataResponse.getTotalSavedRecords());
		totalSaveRecords(pushDataResponse, buildList, deploymentList);
		return pushDataResponse;

	}

	/**
	 * partial correct data will not be saved to respective dba
	 * 
	 * @param pushDataResponse
	 * @param buildList
	 * @param deploymentList
	 */
	private void totalSaveRecords(PushDataResponse pushDataResponse, List<Build> buildList,
			List<Deployment> deploymentList) {
		if (pushDataResponse.getTotalRecords() != pushDataResponse.getTotalSavedRecords()) {
			pushDataResponse.setTotalSavedRecords(0);
			throw new PushDataException("Errors in particular below ids", pushDataResponse);
		}
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
			throw new PushDataException("Maximum Limit of build/deployment is " + customApiConfig.getPushDataLimit());
		}
		return (CollectionUtils.isNotEmpty(buildDeploy.getDeployments()) ? buildDeploy.getDeployments().size() : 0)
				+ (CollectionUtils.isNotEmpty(buildDeploy.getBuilds()) ? buildDeploy.getBuilds().size() : 0);
	}

}
