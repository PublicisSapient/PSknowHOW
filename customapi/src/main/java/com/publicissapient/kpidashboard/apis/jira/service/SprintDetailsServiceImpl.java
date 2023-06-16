package com.publicissapient.kpidashboard.apis.jira.service;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

@Service
public class SprintDetailsServiceImpl implements SprintDetailsService {

	@Autowired
	private SprintRepository sprintRepository;

	@Override
	public List<SprintDetails> getSprintDetails(String basicProjectConfigId) {
		return sprintRepository.findByBasicProjectConfigId(new ObjectId(basicProjectConfigId));
	}

}
