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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationException;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.common.model.rbac.ActionPolicyRule;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BasicPolicyEnforcement implements PolicyEnforcement {

	@Autowired
	private PolicyDefinition policyDefinition;

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * edu.mostafa.abac.security.policy.PolicyEnforcement#check(java.lang.Object,
	 * java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean check(Object projectAccessManager, Object subject, Object resource, Object action,
			Object environment) {
		// Get all policy rules
		List<ActionPolicyRule> allRules = policyDefinition.getAllPolicyRules();
		// Wrap the context
		SecurityAccessContext cxt = new SecurityAccessContext(projectAccessManager, subject, resource, action,
				environment);
		// Filter the rules according to context.
		List<ActionPolicyRule> matchedRules = filterRules(allRules, cxt);
		// finally, check if any of the rules are satisfied, otherwise return false.
		return checkRules(matchedRules, cxt);
	}

	private List<ActionPolicyRule> filterRules(List<ActionPolicyRule> allRules, SecurityAccessContext cxt) {
		List<ActionPolicyRule> matchedRules = new ArrayList<>();
		for (ActionPolicyRule rule : allRules) {
			try {
				if (Boolean.TRUE.equals(rule.getRoleActionCheck().getValue(cxt, Boolean.class))) {
					matchedRules.add(rule);
				}
			} catch (EvaluationException ex) {
				log.info("An error occurred while evaluating PolicyRule.", ex);
			}
		}
		return matchedRules;
	}

	private boolean checkRules(List<ActionPolicyRule> matchedRules, SecurityAccessContext cxt) {
		for (ActionPolicyRule rule : matchedRules) {
			try {
				if (Boolean.TRUE.equals(rule.getCondition().getValue(cxt, Boolean.class))) {
					return true;
				}
			} catch (EvaluationException ex) {
				log.info("An error occurred while evaluating PolicyRule.", ex);
			}
		}
		return false;
	}
}
