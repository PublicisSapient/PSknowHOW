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
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.atlassian.jira.rest.client.api.domain.Subtask;

import lombok.Getter;

@Getter
public class CustomSubtask extends Subtask {
	private final BasicPriority priority;

	public CustomSubtask(String issueKey, URI issueUri, String summary, IssueType issueType, Status status,
			BasicPriority priority) {
		super(issueKey, issueUri, summary, issueType, status);
		this.priority = priority;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof CustomSubtask that))
			return false;
		if (!super.equals(obj))
			return false;
		return Objects.equals(priority, that.priority);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (priority != null ? priority.hashCode() : 0);
		return result;
	}
}
