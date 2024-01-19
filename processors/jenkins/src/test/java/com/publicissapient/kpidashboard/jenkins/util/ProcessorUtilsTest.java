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

package com.publicissapient.kpidashboard.jenkins.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import com.publicissapient.kpidashboard.common.model.application.RepoBranch;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.publicissapient.kpidashboard.jenkins.config.JenkinsConfig;

/**
 * @author anisingh4
 */
@ExtendWith(SpringExtension.class)
public class ProcessorUtilsTest {

	@Test
	public void isSameServerInfo_Positive() {
		String url1 = "https://123456:234567@jenkins.com/job/job1";
		String url2 = "https://123456:234567@jenkins.com/job/job1";
		assertTrue(ProcessorUtils.isSameServerInfo(url1, url2));
	}

	@Test
	public void isSameServerInfo_Negative() {
		String url1 = "";
		String url2 = "https://123456:234567@jenkins.com/job/job1";
		assertFalse(ProcessorUtils.isSameServerInfo(url1, url2));
	}

	@Test
	public void isSameServerInfo_MalformedUrl() {
		String invalidUrlWithSpace = "https:// 234567@jenkins.com/job/job1";
		String url2 = "https://234567@jenkins.com/job/job1";
		assertFalse(ProcessorUtils.isSameServerInfo(invalidUrlWithSpace, url2));
	}

	@Test
	public void getCommitTimestamp_WithTimestamp() {

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("timestamp", 1580098463L);
		long result = 1580098463L;

		long timestamp = ProcessorUtils.getCommitTimestamp(jsonObject);
		assertEquals(result, timestamp);

	}

	@Test
	public void getCommitTimestamp_WithDate() {

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("date", "2020-01-27 04:14:23 +0530");
		long result = 1580078663000L;

		long timestamp = ProcessorUtils.getCommitTimestamp(jsonObject);
		assertEquals(result, timestamp);

	}

	@Test
	public void getCommitTimestamp_UnknownFormat() {

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("date", "2020/01/27");
		long result = 0L;
		long timestamp = ProcessorUtils.getCommitTimestamp(jsonObject);
		assertEquals(result, timestamp);

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

	@Test
	public void getUnqualifiedBranch() {

		String branch = "master";

		ProcessorUtils.getUnqualifiedBranch(branch);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void getGitRepoBranch() {
		List<String> list = new ArrayList<String>();
		list.add("Raja");
		list.add("Jai");
		JSONObject d = new JSONObject();
		for (int i = 0; i < list.size(); i++) {

			d.put(i, list.get(i));

		}

		ProcessorUtils.getGitRepoBranch(d);

	}

	@Test
	public void getGitRepoBranchTest() {
		List<String> list = new ArrayList<String>();
		list.add("actions");
		list.add("remoteUrls");
		list.add("lastBuiltRevision");
		list.add("branch");

		JSONArray array = new JSONArray();
		for (String str : list) {
			array.add(str);
		}

		JSONObject build1 = new JSONObject();
		build1.put("name", "remote/branch");

		JSONArray array2 = new JSONArray();
		array2.add(build1);

		JSONObject build = new JSONObject();
		build.put("lastBuiltRevision", array2);
		build.put("branch", array2);

		JSONObject d = new JSONObject();
		d.put("actions", array);
		d.put("remoteUrls", array);
		d.put("lastBuiltRevision", build);
		d.put("branch", array);

		JSONArray array1 = new JSONArray();
		array1.add(d);

		JSONObject objNew = new JSONObject();
		objNew.put("actions", array1);
		objNew.put("actions1", array1);

		List<RepoBranch> branchList = ProcessorUtils.getGitRepoBranch(objNew);
		assertEquals(4, branchList.size());
		assertEquals("actions", branchList.get(0).getUrl());
		assertEquals("remote/branch", branchList.get(0).getBranch());

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
	public void buildJobQueryString() {

		JenkinsConfig config = new JenkinsConfig();
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
}