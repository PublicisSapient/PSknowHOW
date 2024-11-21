package com.publicissapient.kpidashboard.azure.client.sprint;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import org.bson.types.ObjectId;

import com.publicissapient.kpidashboard.azure.adapter.AzureAdapter;
import com.publicissapient.kpidashboard.azure.model.AzureServer;
import com.publicissapient.kpidashboard.azure.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;

/**
 * @author Hiren Babariya
 *
 */
public interface SprintClient {

	/**
	 *
	 * @param projectConfig
	 * @param sprintDetailsSet
	 * @param azureAdapter
	 * @param azureServer
	 */
	void prepareSprintReport(ProjectConfFieldMapping projectConfig, Set<SprintDetails> sprintDetailsSet,
			AzureAdapter azureAdapter, AzureServer azureServer,
			Map<ObjectId, Map<String, LocalDateTime>> projectWiseReportToggle) throws Exception;

}
