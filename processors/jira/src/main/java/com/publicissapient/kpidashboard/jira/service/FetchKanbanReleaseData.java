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
package com.publicissapient.kpidashboard.jira.service;

import java.io.IOException;

import org.json.simple.parser.ParseException;

import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.model.application.ProjectRelease;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

public interface FetchKanbanReleaseData {
	/**
	 *
	 * @param projectConfig
	 *            projectConfig
	 * @param krb5Client
	 *            krb5Client
	 * @return ProjectRelease
	 */
	ProjectRelease processReleaseInfo(ProjectConfFieldMapping projectConfig, KerberosClient krb5Client)
			throws IOException, ParseException;
}
