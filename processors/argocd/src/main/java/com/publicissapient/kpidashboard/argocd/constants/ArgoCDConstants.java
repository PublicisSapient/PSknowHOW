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

package com.publicissapient.kpidashboard.argocd.constants;

/**
 * ArgoCDConstants represents a class which holds ArgoCDConfiuration
 * related fields
 */
public class ArgoCDConstants {
	private ArgoCDConstants() {
	}
	
	/** The Constant Application endpoint. */
	public static final String APPLICATIONS_ENDPOINT = "/api/v1/applications";
	
	/** The Constant Application parameters. */
	public static final String APPLICATIONS_PARAM = "fields=items.metadata.name";
	
	/** The Constant session token endpoint. */
	public static final String AUTHTOKEN_ENDPOINT = "/api/v1/session";
	
	/** The Constant user information endpoint. */
	public static final String USER_INFO_ENDPOINT = "/api/v1/session/userinfo";
	
	/** The Constant Authorization header. */
	public static final String AUTHORIZATION_HEADER = "Authorization";
	
	/** The Constant Bearer. */
	public static final String BEARER = "Bearer ";

	/** The Constant Processor Execution Uid. */
	public static final String PROCESSOR_EXECUTION_UID = "processorExecutionUid";
	
	/** The Constant Processor Start time. */
	public static final String PROCESSOR_START_TIME = "processorStartTime";
	
	/** The Constant Instance Url. */
	public static final String INSTANCE_URL = "instanceUrl";
	
	/** The Constant Processor End time. */
	public static final String PROCESSOR_END_TIME = "processorEndTime";
	
	/** The Constant Processor Execution time. */
	public static final String EXECUTION_TIME = "executionTime";
	
	/** The Constant Processor Execution status. */
	public static final String EXECUTION_STATUS = "executionStatus";
	
	/** The Constant Processor total updated entry count. */
	public static final String TOTAL_UPDATED_COUNT = "totalUpdatedCount";
	
	/** The Constant total selected projects for processing. */
	public static final String TOTAL_SELECTED_PROJECTS_FOR_PROCESSING = "TotalSelectedProjectsForProcessing";
	
	/** The Constant total configured projects. */
	public static final String TOTAL_CONFIGURED_PROJECTS = "TotalConfiguredProject";
	
	/** The Constant Date time format. */
	public static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

	public static final String ARGOCD_CLUSTER_ENDPOINT = "/api/v1/clusters";
}
