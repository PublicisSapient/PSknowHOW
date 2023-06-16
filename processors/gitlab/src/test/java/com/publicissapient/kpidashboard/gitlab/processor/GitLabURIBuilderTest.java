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

package com.publicissapient.kpidashboard.gitlab.processor;

import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.gitlab.config.GitLabConfig;
import com.publicissapient.kpidashboard.gitlab.model.GitLabRepo;
import com.publicissapient.kpidashboard.gitlab.processor.service.impl.GitLabURIBuilder;

@ExtendWith(SpringExtension.class)
public class GitLabURIBuilderTest {

	ProcessorToolConnection gitLabInfo = new ProcessorToolConnection();
	@Mock
	private GitLabRepo repo;
	@Mock
	private GitLabConfig config;
	private GitLabURIBuilder uriBuilder;

	@BeforeEach
	public void init() {
		gitLabInfo.setBranch("release/core-r4.4");
		gitLabInfo.setPassword("testPassword");
		gitLabInfo.setUrl("http://localhost:9999/scm/testproject/testProject.git");
		gitLabInfo.setApiEndPoint("/rest/api/1.0/");
		gitLabInfo.setUsername("User");

		Map<String, Object> options = new HashMap<>();
		options.put("gitLabApi", "/rest/api/1.0/");
		when(repo.getGitLabProjectId()).thenReturn("577");
		uriBuilder = new GitLabURIBuilder(repo, config, gitLabInfo);
	}

	@Test
	public void testBuild() throws Exception {
		String url = uriBuilder.build();
		String expected = "http://localhost:9999/api/v4/projects/577/repository/commits?ref_name=release%2Fcore-r4.4&per_page=100";
		Assert.assertEquals(expected, url);
	}

}
