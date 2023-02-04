package com.publicissapient.kpidashboard.apis.pushdata.service;

import org.apache.commons.collections.CollectionUtils;

import com.publicissapient.kpidashboard.apis.pushdata.model.PushBuildDeploy;
import com.publicissapient.kpidashboard.apis.pushdata.model.PushBuildDeployResponse;
import com.publicissapient.kpidashboard.apis.pushdata.util.PushDataException;

public interface PushBaseService {

	 int getTotalRecords(PushBuildDeploy buildDeploy);

}
