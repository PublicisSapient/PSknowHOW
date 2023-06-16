package com.publicissapient.kpidashboard.bamboo.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.bamboo.client.BambooClient;
import com.publicissapient.kpidashboard.bamboo.client.impl.BambooClientBuildImpl;
import com.publicissapient.kpidashboard.bamboo.client.impl.BambooClientDeployImpl;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BambooClientFactory {

	private static final String BUILD = "build";
	private static final String DEPLOY = "deploy";

	private final BambooClientBuildImpl bambooClientBuild;
	private final BambooClientDeployImpl bambooClientDeploy;

	@Autowired
	public BambooClientFactory(BambooClientBuildImpl bambooClientBuild, BambooClientDeployImpl bambooClientDeploy) {
		this.bambooClientBuild = bambooClientBuild;
		this.bambooClientDeploy = bambooClientDeploy;
	}

	/**
	 * getBambooClient on the basis of jobType
	 * 
	 * @param jobType
	 * @return
	 */
	public BambooClient getBambooClient(String jobType) {
		BambooClient bambooClient = null;
		if (jobType.equalsIgnoreCase(BUILD)) {
			bambooClient = bambooClientBuild;
		} else if (jobType.equalsIgnoreCase(DEPLOY)) {
			bambooClient = bambooClientDeploy;
		}
		return bambooClient;
	}
}
