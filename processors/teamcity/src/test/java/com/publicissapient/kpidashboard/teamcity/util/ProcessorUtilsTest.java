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

package com.publicissapient.kpidashboard.teamcity.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.publicissapient.kpidashboard.teamcity.config.TeamcityConfig;

/**
 * @author anisingh4
 */
@ExtendWith(SpringExtension.class)
public class ProcessorUtilsTest {

	@Test
	public void isSameServerInfo_Positive() {
		String url1 = "https://123456:234567@teamcity.com/job/job1";
		String url2 = "https://123456:234567@teamcity.com/job/job1";
		assertTrue(ProcessorUtils.isSameServerInfo(url1, url2));
	}

	@Test
	public void isSameServerInfo_Negative() {
		String url1 = "";
		String url2 = "https://123456:234567@teamcity.com/job/job1";
		assertFalse(ProcessorUtils.isSameServerInfo(url1, url2));
	}

	@Test
	public void isSameServerInfo_MalformedUrl() {
		String invalidUrlWithSpace = "https:// 234567@teamcity.com/job/job1";
		String url2 = "https://234567@teamcity.com/job/job1";
		assertFalse(ProcessorUtils.isSameServerInfo(invalidUrlWithSpace, url2));
	}

	@Test
	public void getCommitTimestamp_WithTimestamp() {

		String jsonObject = new String();
		jsonObject = "20200402T142557";
		long result = 1585817757000L;

		long timestamp = ProcessorUtils.getCommitTimestamp(jsonObject);
		// assertEquals(result, timestamp);

	}

	@Test
	public void getCommitTimestamp_UnknownFormat() {

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("date", "2020/01/27");
		long result = 0L;
		long timestamp = ProcessorUtils.getCommitTimestamp(jsonObject.toString());
		assertEquals(result, timestamp);

	}

	@Test
	public void buildJobQueryString() {

		TeamcityConfig config = new TeamcityConfig();
		config.setFolderDepth(5);
		String jobQuery = "abc";

		ProcessorUtils.buildJobQueryString(config, jobQuery);

	}

	@Test
	public void createHeaders() {

		final String userInfo = "abc";

		ProcessorUtils.createHeaders(userInfo);

	}

	@Test
	public void joinURL() {

		String base = "abc";
		String paths = "C:/local";

		ProcessorUtils.joinURL(base, paths);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void getGitRepoBranch() {
		List<String> list = new ArrayList<String>();
		list.add("test1");
		list.add("test2");
		JSONObject d = new JSONObject();
		for (int i = 0; i < list.size(); i++) {

			d.put(i, list.get(i));

		}

		ProcessorUtils.getGitRepoBranch(d);

	}

	@Test
	public void removeGitExtensionFromUrl() {

		String paths = "C:/local";

		ProcessorUtils.removeGitExtensionFromUrl(paths);

	}

	@Test
	public void removeGitExtensionFromUrlGit() {

		String paths = "C:/local.git";

		ProcessorUtils.removeGitExtensionFromUrl(paths);

	}

	@Test
	public void getUnqualifiedBranch() {

		String branch = "master";

		ProcessorUtils.getUnqualifiedBranch(branch);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void startDate() {
		JSONObject d = new JSONObject();

		d.put("triggered", "test1");
		d.put("user", "testuser");
		d.put("lastChanges", "test2");

		ProcessorUtils.startDate(d);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void getCommitAuthor() {
		JSONObject d = new JSONObject();
		ProcessorUtils.getCommitAuthor(d);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void firstCulprit() {
		JSONObject d = new JSONObject();
		ProcessorUtils.firstCulprit(d);
	}
}