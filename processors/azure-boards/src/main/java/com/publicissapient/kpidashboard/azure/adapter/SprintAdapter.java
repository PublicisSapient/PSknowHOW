package com.publicissapient.kpidashboard.azure.adapter;

import java.util.List;

import com.publicissapient.kpidashboard.azure.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;

/**
 * @author Hiren Babariya
 *
 */
public interface SprintAdapter {

	/**
	 * this method fetch sprints from azure server
	 *
	 * @param projectConfFieldMapping
	 *            projectConfFieldMapping
	 * @param teamName
	 *            teamName
	 * @return List of SprintDetails
	 */
	List<SprintDetails> getSprints(ProjectConfFieldMapping projectConfFieldMapping, String teamName);

}
