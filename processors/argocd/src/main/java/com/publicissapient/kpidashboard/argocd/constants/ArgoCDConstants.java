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

public class ArgoCDConstants {
	private ArgoCDConstants() {
	}
	
	public static final String APPLICATIONS_ENDPOINT = "/api/v1/applications";
	
	public static final String APPLICATIONS_PARAM = "fields=items.metadata.name";
	
	public static final String AUTHTOKEN_ENDPOINT = "/api/v1/session";
	
	public static final String USER_INFO_ENDPOINT = "/api/v1/session/userinfo";
	
	public static final String AUTHORIZATION_HEADER = "Authorization";
	
	public static final String BEARER = "Bearer ";

	public static final String PROCESSOR_EXECUTION_UID = "processorExecutionUid";
	
	public static final String PROCESSOR_START_TIME = "processorStartTime";
	
	public static final String INSTANCE_URL = "instanceUrl";
	
	public static final String PROCESSOR_END_TIME = "processorEndTime";
	
	public static final String EXECUTION_TIME = "executionTime";
	
	public static final String EXECUTION_STATUS = "executionStatus";
	
	public static final String TOTAL_UPDATED_COUNT = "totalUpdatedCount";
	
	public static final String TOTAL_SELECTED_PROJECTS_FOR_PROCESSING = "TotalSelectedProjectsForProcessing";
	
	public static final String TOTAL_CONFIGURED_PROJECTS = "TotalConfiguredProject";
	
	public static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
}
