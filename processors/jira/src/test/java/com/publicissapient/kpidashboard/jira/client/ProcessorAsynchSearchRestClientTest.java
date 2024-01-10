package com.publicissapient.kpidashboard.jira.client;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.jira.rest.client.internal.async.DisposableHttpClient;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

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
		for (int i = 0; i < 277; i++) {
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
