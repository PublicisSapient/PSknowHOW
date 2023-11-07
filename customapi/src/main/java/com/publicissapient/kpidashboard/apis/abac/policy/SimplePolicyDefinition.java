/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.apis.abac.policy;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.auth.model.ActionPoliciesDTO;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;

@Component("simplePolicyDefinition")
public class SimplePolicyDefinition implements PolicyDefinition {
	@Autowired
	CacheService cacheService;
	private List<ActionPoliciesDTO> rules;

	@PostConstruct
	private void init() {
		rules = cacheService.getActionPoliciesFromCache();
	}

	public List<ActionPoliciesDTO> getAllPolicyRules() {
		return rules;
	}

}
