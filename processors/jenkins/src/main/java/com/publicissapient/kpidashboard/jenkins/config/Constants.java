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

package com.publicissapient.kpidashboard.jenkins.config;

import org.apache.commons.lang3.StringUtils;

/**
 * Module constants
 * 
 * @author anisingh4
 */
public final class Constants {

	public static final String JOBS = "jobs";
	public static final String NAME = "name";
	public static final String BUILDS = "builds";
	public static final String NUMBER = "number";
	public static final String URL = "url";
	public static final String TIMESTAMP = "timestamp";
	public static final String DURATION = "duration";
	public static final String BUILDING = "building";
	public static final String RESULT = "result";
	public static final String REVISION = "revision";
	public static final String JOB_URL_END_POINT = "/job/BUILD_NAME/api/json?tree=";
	private static final String[] ITEMS_TREE = { "user", "author[fullName]", REVISION, "id", "msg", TIMESTAMP, "date",
			"paths[file]" };
	private static final String[] BUILD_TREE = { NUMBER, URL, TIMESTAMP, DURATION, BUILDING, RESULT,
			"culprits[fullName]", "changeSets[items[" + StringUtils.join(ITEMS_TREE, ",") + "],kind]",
			"changeSet[items[" + StringUtils.join(ITEMS_TREE, ",") + "]", "kind", "revisions[module,revision]]",
			"actions[lastBuiltRevision[SHA1,branch[SHA1,name]],remoteUrls]" };
	public static final String BUILD_URL_END_POINT = "/api/json?tree=" + StringUtils.join(BUILD_TREE, ",");
	private static final String[] JOB_BUILD_TREE = { NUMBER, URL, TIMESTAMP, DURATION, BUILDING, RESULT,
			"culprits[fullName]" };
	public static final String JOB_FIELDS = "name,url,builds[" + StringUtils.join(JOB_BUILD_TREE, ",")
			+ "],lastSuccessfulBuild[timestamp,builtOn],lastBuild[timestamp,builtOn]";
	public static final String CHILD_JOBS_TREE = "jobs[" + JOB_FIELDS + "]";

	private Constants() {
		// to prevent object creation
	}

}
