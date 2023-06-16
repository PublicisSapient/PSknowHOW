package com.publicissapient.kpidashboard.apis.jira.service;

import java.util.List;

import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;

public interface SprintDetailsService {

	public List<SprintDetails> getSprintDetails(String basicProjectConfigId);
}
