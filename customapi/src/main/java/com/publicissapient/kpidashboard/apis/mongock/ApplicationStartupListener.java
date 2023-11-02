/*
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.publicissapient.kpidashboard.apis.mongock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.abac.policy.SimplePolicyDefinition;
import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.common.repository.application.GlobalConfigRepository;

/**
 * @author bogolesw
 */
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
