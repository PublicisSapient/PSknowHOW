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

package com.publicissapient.kpidashboard.jira.client;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import com.atlassian.httpclient.api.HttpClient;

public class ProcessorAsynchSearchRestClientTest {

	static URI baseUri;
	static ProcessorAsynchSearchRestClient processorAsynchSearchRestClient;

	@BeforeClass
	public static void processorAsynchJiraRestClientSetup() throws URISyntaxException {
		baseUri = new URI("https://www.baseuri.com/");
		HttpClient mockHttpClient = mock(HttpClient.class);
		processorAsynchSearchRestClient = new ProcessorAsynchSearchRestClient(baseUri, mockHttpClient);
		assertNotNull(processorAsynchSearchRestClient);
	}

	@Test(expected = NullPointerException.class)
	public void searchJqlTest() {
		processorAsynchSearchRestClient.searchJql("dummyStr");
	}

	@Test(expected = NullPointerException.class)
	public void searchJqlGetTest() {
		Set<String> set = new HashSet<>();
		set.add("field1");
		set.add("field2");
		processorAsynchSearchRestClient.searchJql("dummyStr", 12, 1, set);
	}

	@Test(expected = NullPointerException.class)
	public void searchJqlPostTest() {
		Set<String> set = new HashSet<>();
		set.add("field1");
		set.add("field2");
		StringBuilder sb = new StringBuilder("dummyString");
		for (int i = 0; i < 300; i++) {
			sb.append("dummyString");
		}
		processorAsynchSearchRestClient.searchJql(sb.toString(), 12, 1, set);
	}

	@Test(expected = NullPointerException.class)
	public void getFavouriteFiltersTest() {
		processorAsynchSearchRestClient.getFavouriteFilters();
	}

	@Test(expected = NullPointerException.class)
	public void getFilterUriTest() {
		processorAsynchSearchRestClient.getFilter(baseUri);
	}

	@Test(expected = NullPointerException.class)
	public void getFilterLongTest() {
		processorAsynchSearchRestClient.getFilter(1234567);
	}
}
