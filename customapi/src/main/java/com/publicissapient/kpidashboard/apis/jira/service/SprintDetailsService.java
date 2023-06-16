package com.publicissapient.kpidashboard.apis.jira.service;

import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;

import java.util.List;

public interface SprintDetailsService {

    public List<SprintDetails> getSprintDetails(String basicProjectConfigId);
}
