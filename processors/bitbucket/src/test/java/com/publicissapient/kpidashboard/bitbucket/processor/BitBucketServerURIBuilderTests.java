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

package com.publicissapient.kpidashboard.bitbucket.processor;

import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.publicissapient.kpidashboard.bitbucket.config.BitBucketConfig;
import com.publicissapient.kpidashboard.bitbucket.model.BitbucketRepo;
import com.publicissapient.kpidashboard.bitbucket.processor.service.impl.BitBucketServerURIBuilder;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;

@ExtendWith(SpringExtension.class)
public class BitBucketServerURIBuilderTests {

	@Mock
	private BitbucketRepo bitBucketRepo;

	@Mock
	private BitBucketConfig config;

	private BitBucketServerURIBuilder uriBuilder;

	@BeforeEach
	public void init() {
		Map<String, Object> options = new HashMap<>();
		options.put("bitbucketApi", "/rest/api/1.0/");
		when(bitBucketRepo.getToolDetailsMap()).thenReturn(options);
		when(config.getPageSize()).thenReturn(25);
		when(config.getApi()).thenReturn("/rest/api/1.0");
		ProcessorToolConnection connectionDetail = new ProcessorToolConnection();
		connectionDetail.setBranch("release/core-r4.4");
		connectionDetail.setPassword("testPassword");
		connectionDetail.setUrl("http://localhost:9999/bitbucket/");
		connectionDetail.setApiEndPoint("/rest/api/1.0/");
		connectionDetail.setUsername("User");
		connectionDetail.setBitbucketProjKey("testproject");
		connectionDetail.setRepoSlug("testRepoSlug");

		uriBuilder = new BitBucketServerURIBuilder(bitBucketRepo, config, connectionDetail);
	}

	@Test
	public void testBuild() throws Exception {
		String url = uriBuilder.build();
		String expected = "http://localhost:9999/rest/api/1.0/projects/testproject/repos/testRepoSlug/commits?limit=25&until=release%2Fcore-r4.4";
		Assert.assertEquals(expected, url);
	}

}
