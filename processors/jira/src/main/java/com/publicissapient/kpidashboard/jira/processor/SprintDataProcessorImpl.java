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
package com.publicissapient.kpidashboard.jira.processor;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.jira.constant.JiraConstants;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.service.FetchSprintReport;
import com.publicissapient.kpidashboard.jira.util.JiraIssueClientUtil;
import com.publicissapient.kpidashboard.jira.util.JiraProcessorUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author pankumar8
 *
 */
@Slf4j
@Service
public class SprintDataProcessorImpl implements SprintDataProcessor {

	@Autowired
	private FetchSprintReport fetchSprintReport;

	@Override
	public Set<SprintDetails> processSprintData(Issue issue, ProjectConfFieldMapping projectConfig, String boardId)
			throws IOException {
		log.info("creating sprint report for the project : {}", projectConfig.getProjectName());
		Set<SprintDetails> sprintDetailsSet = new HashSet<>();
		FieldMapping fieldMapping = projectConfig.getFieldMapping();
		Map<String, IssueField> fields = JiraIssueClientUtil.buildFieldMap(issue.getFields());
		IssueField sprintField = fields.get(fieldMapping.getSprintName());
		if (null != sprintField && null != sprintField.getValue()
				&& !JiraConstants.EMPTY_STR.equals(sprintField.getValue())) {
			Object sValue = sprintField.getValue();
			try {
				List<SprintDetails> sprints = JiraProcessorUtil.processSprintDetail(sValue);
				if (CollectionUtils.isNotEmpty(sprints)) {
					for (SprintDetails sprint : sprints) {
						sprint.setSprintID(sprint.getOriginalSprintId() + JiraConstants.COMBINE_IDS_SYMBOL
								+ projectConfig.getProjectName() + JiraConstants.COMBINE_IDS_SYMBOL
								+ projectConfig.getBasicProjectConfigId());
						sprint.setBasicProjectConfigId(projectConfig.getBasicProjectConfigId());
					}
					sprintDetailsSet.addAll(sprints);
				}
			} catch (ParseException | JSONException e) {
				log.error("JIRA Processor | Failed to obtain sprint data from {} {}", sValue, e);
			}
		}
		KerberosClient krb5Client = null;
		if (StringUtils.isEmpty(boardId)) {
			return fetchSprintReport.fetchSprints(projectConfig, sprintDetailsSet, krb5Client, false);
		}

		return sprintDetailsSet;
	}
}
