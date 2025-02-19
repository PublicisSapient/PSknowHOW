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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.Request.Builder;
import com.atlassian.httpclient.api.ResponsePromise;
import com.atlassian.jira.rest.client.api.IssueRestClient.Expandos;
import com.atlassian.jira.rest.client.api.MetadataRestClient;
import com.atlassian.jira.rest.client.api.SessionRestClient;
import com.atlassian.jira.rest.client.internal.async.AbstractAsynchronousRestClient;

@RunWith(MockitoJUnitRunner.class)
public class CustomAsynchronousIssueRestClientTest {

	private static String baseUrlValid = "https://example.com/";

	private static String baseUrlNotValid = "https://www.mockdummyurl.com/";

	static CustomAsynchronousIssueRestClient customAsynchronousIssueRestClient;

	static URI baseUri;

	@Mock
	AbstractAsynchronousRestClient abstractAsynchronousRestClient;

	@Mock
	HttpClient mockClient;

	@Mock
	private Builder builder;

	@BeforeClass
	public static void customAsynchronousIssueRestClientSetup() throws URISyntaxException {

		HttpClient mockClient = mock(HttpClient.class);
		SessionRestClient mockSessionRestClient = mock(SessionRestClient.class);
		MetadataRestClient mockMetadataRestClient = mock(MetadataRestClient.class);
		baseUri = new URI(baseUrlValid);
		customAsynchronousIssueRestClient = new CustomAsynchronousIssueRestClient(baseUri, mockClient,
				mockSessionRestClient, mockMetadataRestClient);
		assertNotNull(customAsynchronousIssueRestClient);
	}

	@Test(expected = NullPointerException.class)
	public void getIssueTest() {
		String issueKey = "issuekey";
		Expandos expandos1 = Expandos.CHANGELOG;
		Expandos expandos2 = Expandos.TRANSITIONS;
		Expandos expandos3 = Expandos.OPERATIONS;
		ResponsePromise mockGetMethod = mock(ResponsePromise.class);
		// when(mockClient.newRequest(baseUri)).thenReturn(builder);
		// when(builder.setAccept("application/json")).thenReturn(builder);
		// when(builder.get()).thenReturn(mockGetMethod);
		customAsynchronousIssueRestClient.getIssue(issueKey,
				new ArrayList<>(Arrays.asList(expandos1, expandos2, expandos3)));
	}

	@Test(expected = NullPointerException.class)
	public void searchBoardIssueTest() {
		customAsynchronousIssueRestClient.searchBoardIssue("123", null, 7, 1, new HashSet<>());
	}

	@Test(expected = NullPointerException.class)
	public void searchBoardIssueGetTest() {
		Set<String> set = new HashSet<>();
		set.add("field1");
		set.add("field2");
		customAsynchronousIssueRestClient.searchBoardIssue("BoardId", "dummyStr", 12, 1, set);
	}

	@Test(expected = NullPointerException.class)
	public void searchBoardIssuePostTest() {
		Set<String> set = new HashSet<>();
		set.add("field1");
		set.add("field2");
		StringBuilder sb = new StringBuilder("dummyString");
		for (int i = 0; i < 300; i++) {
			sb.append("dummyString");
		}
		customAsynchronousIssueRestClient.searchBoardIssue("BoardId", sb.toString(), 12, 1, set);
	}
}
