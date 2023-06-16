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

package com.publicissapient.kpidashboard.bamboo.client;

import java.net.MalformedURLException;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.json.simple.parser.ParseException;

import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.model.application.Deployment;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;

/**
 * Client for fetching jobs and build information from Bamboo server
 */
public interface BambooClient {

	/**
	 * Joins a base url to another path or paths - this will handle trailing or //
	 * non-trailing /'s
	 *
	 * @param baseUrl
	 *            baseUrl
	 * @param params
	 *            string list to append in the url
	 * @return full url
	 */
	static String appendToURL(String baseUrl, String... params) {
		StringBuilder url = new StringBuilder(baseUrl);
		for (String param : params) {
			String finalP = param.replaceFirst("^(\\/)+", "");
			if (url.lastIndexOf("/") != url.length() - 1) {
				url.append('/');
			}
			url.append(finalP);
		}
		return url.toString();
	}

	/**
	 * Finds the configured jobs for a given instance and returns the set of builds
	 * for each job populating atleast, the build number and url of each build.
	 *
	 * @param bambooServer
	 *            {@link ProcessorToolConnection}
	 * @param proBasicConfig
	 * @return a summary of every build for each job on the instance
	 * @throws ParseException
	 *             if the response from the bamboo service is not interpretable
	 * @throws MalformedURLException
	 *             if the bamboo service url is not formed correctly
	 */
	Map<ObjectId, Set<Build>> getJobsFromServer(ProcessorToolConnection bambooServer, ProjectBasicConfig proBasicConfig)
			throws ParseException, MalformedURLException;

	/**
	 * Gets the complete information of a build from Bamboo server.
	 *
	 * @param buildUrl
	 *            the url of the build
	 * @param instanceUrl
	 *            the URL for the Bamboo instance
	 * @param bambooServer
	 *            {@link ProcessorToolConnection}
	 * @return a Build instance or null
	 */
	Build getBuildDetailsFromServer(String buildUrl, String instanceUrl, ProcessorToolConnection bambooServer);

	Map<Pair<ObjectId, String>, Set<Deployment>> getDeployJobsFromServer(ProcessorToolConnection bambooServer,
			ProjectBasicConfig proBasicConfig) throws ParseException, MalformedURLException;

}
