/*
 *   Copyright 2014 CapitalOne, LLC.
 *   Further development Copyright 2022 Sapient Corporation.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.publicissapient.kpidashboard.jira.model;

import java.net.URI;
import java.util.Objects;

import com.atlassian.jira.rest.client.api.domain.BasicPriority;
import com.atlassian.jira.rest.client.api.domain.IssueLink;
import com.atlassian.jira.rest.client.api.domain.IssueLinkType;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Status;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomIssueLink extends IssueLink {
	private String summary;
	private Status status;
	private BasicPriority priority;
	private IssueType issueType;

	public CustomIssueLink(String targetIssueKey, URI targetIssueUri, IssueLinkType issueLinkType, String summary,
			Status status, BasicPriority priority, IssueType issueType) {
		super(targetIssueKey, targetIssueUri, issueLinkType);
		this.summary = summary;
		this.status = status;
		this.priority = priority;
		this.issueType = issueType;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof CustomIssueLink that))
			return false;
		if (!super.equals(obj))
			return false;
		return Objects.equals(summary, that.summary) && Objects.equals(status, that.status)
				&& Objects.equals(priority, that.priority) && Objects.equals(issueType, that.issueType);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (summary != null ? summary.hashCode() : 0);
		result = 31 * result + (status != null ? status.hashCode() : 0);
		result = 31 * result + (priority != null ? priority.hashCode() : 0);
		result = 31 * result + (issueType != null ? issueType.hashCode() : 0);
		return result;
	}
}