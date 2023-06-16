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

package com.publicissapient.kpidashboard.teamcity.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.teamcity.processor.adapter.TeamcityClient;
import com.publicissapient.kpidashboard.teamcity.processor.adapter.impl.DefaultTeamcityClient;
import com.publicissapient.kpidashboard.teamcity.processor.adapter.impl.Teamcity2Client;

/**
 * Provides factory to create Teamcity Clients.
 *
 */
@Component
public class TeamcityClientFactory {

	private static final String TEAMCITY_CLIENT = "teamcityClient";
	private static final String TEAMCITY2_CLIENT = "teamcity2Client";
	private final DefaultTeamcityClient teamcityClient;
	private final Teamcity2Client teamcity2Client;

	@Autowired
	public TeamcityClientFactory(DefaultTeamcityClient teamcityClient, Teamcity2Client teamcity2Client) {
		this.teamcityClient = teamcityClient;
		this.teamcity2Client = teamcity2Client;
	}

	/**
	 * Provides instance of Teamcity client.
	 * 
	 * @param client
	 *            the required Teamcity client
	 * @return returns the instance of Teamcity Client
	 */
	public TeamcityClient getTeamcityClient(String client) {
		TeamcityClient temp = null;
		switch (client == null ? "" : client) {
		case TEAMCITY_CLIENT:
			temp = teamcityClient;
			break;
		case TEAMCITY2_CLIENT:
			temp = teamcity2Client;
			break;
		default:
			temp = teamcityClient;
			break;
		}
		return temp;
	}

}
