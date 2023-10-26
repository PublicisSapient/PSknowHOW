package com.publicissapient.kpidashboard.apis.mongock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.abac.policy.SimplePolicyDefinition;
import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.common.repository.application.GlobalConfigRepository;

@Component
public class ApplicationStartupListener implements ApplicationListener<ApplicationReadyEvent> {

	@Autowired
	ConfigHelperService configHelperService;
	@Autowired
	SimplePolicyDefinition simplePolicyDefinition;
	@Autowired
	GlobalConfigRepository globalConfigRepository;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		configHelperService.loadKpiMaster();
		configHelperService.calculateMaturity();
		configHelperService.calculateCriteria();
		configHelperService.calculateCriteriaForCircleKPI();
		configHelperService.loadProjectBasicTree();
		configHelperService.loadHierarchyLevelSuggestion();
		configHelperService.loadFieldMappingStructure();
		configHelperService.loadUserBoardConfig();
		configHelperService.loadAllProjectToolConfig();
		configHelperService.loadConfigData();
		configHelperService.loadToolConfig();
		simplePolicyDefinition.init();
		globalConfigRepository.findAll();

	}
}
