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

package com.publicissapient.kpidashboard.common.util;

import java.util.Comparator;

import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;

/**
 * Comparator utility class for handling comparisons of Super Features (epics)
 * to other Super Features. This is used when manipulating results of MongoDB
 * repository responses and sorting them in an efficient manner within memory.
 * 
 * @author kfk884
 * 
 */
public class JiraIssueComparator implements Comparator<JiraIssue> {

	/**
	 * Compares two feature object models and sorts based on their subsequent epic
	 * IDs, as a string comparator
	 * 
	 * @return A list of Features sorted by EpicID, descending
	 */
	@Override
	public int compare(JiraIssue jiraIssue1, JiraIssue jiraIssue2) {
		if (jiraIssue1.getEpicID().compareToIgnoreCase(jiraIssue2.getEpicID()) <= -1) {
			return -1;
		} else if (jiraIssue1.getEpicID().compareToIgnoreCase(jiraIssue2.getEpicID()) >= 1) {
			return 1;
		} else {
			return 0;
		}
	}

}
