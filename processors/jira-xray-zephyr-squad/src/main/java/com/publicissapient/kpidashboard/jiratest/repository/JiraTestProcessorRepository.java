package com.publicissapient.kpidashboard.jiratest.repository;

import org.springframework.stereotype.Repository;

import com.publicissapient.kpidashboard.common.repository.generic.ProcessorRepository;
import com.publicissapient.kpidashboard.jiratest.model.JiraTestProcessor;

@Repository
public interface JiraTestProcessorRepository extends ProcessorRepository<JiraTestProcessor> {
}
