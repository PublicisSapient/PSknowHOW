package com.publicissapient.kpidashboard.apis.pushdata.service;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.pushdata.model.BuildDeployErrorData;
import com.publicissapient.kpidashboard.apis.pushdata.model.PushBuildDeploy;
import com.publicissapient.kpidashboard.apis.pushdata.model.PushBuildDeployResponse;
import com.publicissapient.kpidashboard.apis.pushdata.util.PushDataException;
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
	CustomApiConfig customApiConfig;

	public PushBuildDeployResponse processPushDataInput(PushBuildDeploy buildDeploy, String projectConfigId) {
		PushBuildDeployResponse pushBuildDeployResponse = new PushBuildDeployResponse();
		pushBuildDeployResponse.setTotalRecords(getTotalRecords(buildDeploy));
		log.info("Total Records are " +pushBuildDeployResponse.getTotalRecords());
		List<Build> buildList = new ArrayList<>();
		List<Deployment> deploymentList = new ArrayList<>();
		List<BuildDeployErrorData> buildErrorList = new ArrayList<>();
		List<BuildDeployErrorData> deployErrorList = new ArrayList<>();
		int buildFailedRecords = buildService.checkandCreateBuilds(projectConfigId, buildDeploy.getBuilds(), buildList,
				buildErrorList);
		int deployFailedRecords = deployService.checkandCreateDeployment(projectConfigId, buildDeploy.getDeployments(),
				deploymentList, deployErrorList);
		pushBuildDeployResponse.setBuilds(buildErrorList);
		pushBuildDeployResponse.setDeploy(deployErrorList);
		pushBuildDeployResponse.setTotalFailedRecords(buildFailedRecords + deployFailedRecords);
		pushBuildDeployResponse.setTotalSavedRecords(buildList.size() + deploymentList.size());
		log.info("Total Records to be Saved are " +pushBuildDeployResponse.getTotalSavedRecords());
		totalSaveRecords(pushBuildDeployResponse, buildList, deploymentList);
		return pushBuildDeployResponse;

	}

	/**
	 * partial correct data will not be saved to respective dba
	 * @param pushBuildDeployResponse
	 * @param buildList
	 * @param deploymentList
	 */
	private void totalSaveRecords(PushBuildDeployResponse pushBuildDeployResponse, List<Build> buildList,
			List<Deployment> deploymentList) {
		if (pushBuildDeployResponse.getTotalRecords() != pushBuildDeployResponse.getTotalSavedRecords()) {
			pushBuildDeployResponse.setTotalSavedRecords(0);
			throw new PushDataException("Errors in particular below ids", pushBuildDeployResponse);
		}
		buildService.saveBuilds(buildList);
		deployService.saveDeployments(deploymentList);
	}

	/**
	 * if input record is more than set value, then throw exception
	 * @param buildDeploy
	 * @return
	 */
	@Override
	public int getTotalRecords(PushBuildDeploy buildDeploy) {
		if ((CollectionUtils.isNotEmpty(buildDeploy.getDeployments())
				&& buildDeploy.getDeployments().size() >= customApiConfig.getPushDataLimit())
				|| (CollectionUtils.isNotEmpty(buildDeploy.getBuilds())
						&& buildDeploy.getBuilds().size() >= customApiConfig.getPushDataLimit())) {
			throw new PushDataException("Maximum Limit of build/deployment is " + customApiConfig.getPushDataLimit());
		}
		return (CollectionUtils.isNotEmpty(buildDeploy.getDeployments()) ? buildDeploy.getDeployments().size() : 0)
				+ (CollectionUtils.isNotEmpty(buildDeploy.getBuilds()) ? buildDeploy.getBuilds().size() : 0);
	}

}
