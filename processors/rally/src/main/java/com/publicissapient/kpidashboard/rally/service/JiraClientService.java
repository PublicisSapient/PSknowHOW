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
package com.publicissapient.kpidashboard.rally.service;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.client.KerberosClient;

/**
 * @author purgupta2
 */
@Service
public class JiraClientService {

//	private final ConcurrentHashMap<String, ProcessorJiraRestClient> restClientMap = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, KerberosClient> kerberosClientMap = new ConcurrentHashMap<>();

//	public boolean isContainRestClient(String basicProjectConfigId) {
//		return restClientMap.containsKey(basicProjectConfigId);
//	}
////
//	public void setRestClientMap(String basicProjectConfigId, ProcessorJiraRestClient client) {
//		restClientMap.put(basicProjectConfigId, client);
//	}
//
//	public ProcessorJiraRestClient getRestClientMap(String basicProjectConfigId) {
//		return restClientMap.get(basicProjectConfigId);
//	}

//	public void removeRestClientMapClientForKey(String basicProjectConfigId) {
//		restClientMap.remove(basicProjectConfigId);
//	}

	public boolean isContainKerberosClient(String basicProjectConfigId) {
		return kerberosClientMap.containsKey(basicProjectConfigId);
	}

	public void setKerberosClientMap(String basicProjectConfigId, KerberosClient client) {
		kerberosClientMap.put(basicProjectConfigId, client);
	}

	public KerberosClient getKerberosClientMap(String basicProjectConfigId) {
		return kerberosClientMap.get(basicProjectConfigId);
	}

	public void removeKerberosClientMapClientForKey(String basicProjectConfigId) {
		kerberosClientMap.remove(basicProjectConfigId);
	}
}
