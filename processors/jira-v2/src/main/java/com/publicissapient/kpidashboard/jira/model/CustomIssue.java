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
import java.util.Collection;
import java.util.Set;

import org.joda.time.DateTime;

import com.atlassian.jira.rest.client.api.domain.Attachment;
import com.atlassian.jira.rest.client.api.domain.BasicComponent;
import com.atlassian.jira.rest.client.api.domain.BasicPriority;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.BasicVotes;
import com.atlassian.jira.rest.client.api.domain.BasicWatchers;
import com.atlassian.jira.rest.client.api.domain.ChangelogGroup;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.IssueLink;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Operations;
import com.atlassian.jira.rest.client.api.domain.Resolution;
import com.atlassian.jira.rest.client.api.domain.Status;
import com.atlassian.jira.rest.client.api.domain.Subtask;
import com.atlassian.jira.rest.client.api.domain.TimeTracking;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.atlassian.jira.rest.client.api.domain.Worklog;

public class CustomIssue extends Issue {

	private final Collection<CustomIssueLink> customIssueLinks;
	private final Collection<CustomSubtask> customSubtasks;

	public CustomIssue(String summary, URI self, String key, Long id, BasicProject project, IssueType issueType,
			Status status, String description, BasicPriority priority, Resolution resolution,
			Collection<Attachment> attachments, User reporter, User assignee, DateTime creationDate,
			DateTime updateDate, DateTime dueDate, Collection<Version> affectedVersions,
			Collection<Version> fixVersions, Collection<BasicComponent> components, TimeTracking timeTracking,
			Collection<IssueField> issueFields, Collection<Comment> comments, URI transitionsUri,
			Collection<IssueLink> issueLinks, BasicVotes votes, Collection<Worklog> worklogs, BasicWatchers watchers,
			Iterable<String> expandos, Collection<Subtask> subtasks, Collection<ChangelogGroup> changelog,
			Operations operations, Set<String> labels, Collection<CustomIssueLink> customIssueLinks,
			Collection<CustomSubtask> customSubtasks) {
		super(summary, self, key, id, project, issueType, status, description, priority, resolution, attachments,
				reporter, assignee, creationDate, updateDate, dueDate, affectedVersions, fixVersions, components,
				timeTracking, issueFields, comments, transitionsUri, issueLinks, votes, worklogs, watchers, expandos,
				subtasks, changelog, operations, labels);
		this.customIssueLinks = customIssueLinks;
		this.customSubtasks = customSubtasks;
	}

	public Iterable<CustomIssueLink> getCustomIssueLinks() {
		return customIssueLinks;
	}

	public Iterable<CustomSubtask> getCustomSubtasks() {
		return customSubtasks;
	}
}