package com.publicissapient.kpidashboard.apis.auth.service;

import com.publicissapient.kpidashboard.apis.model.ServiceResponse;

/**
 * aksshriv1
 */
public interface AuthNAuthService {

	/**
	 * Check policies by resources in central auth
	 * 
	 * @param apiKey
	 * @return
	 */
	ServiceResponse checkPoliciesByResource(String apiKey);

	/**
	 * fetch policies by resources from central auth
	 * 
	 * @param apiKey
	 * @return
	 */
	ServiceResponse fetchActionPolicyByResource(String apiKey);
}
