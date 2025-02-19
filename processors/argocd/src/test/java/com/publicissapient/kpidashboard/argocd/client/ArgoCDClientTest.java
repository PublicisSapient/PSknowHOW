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

package com.publicissapient.kpidashboard.argocd.client;

import static com.publicissapient.kpidashboard.argocd.constants.ArgoCDConstants.APPLICATIONS_ENDPOINT;
import static com.publicissapient.kpidashboard.argocd.constants.ArgoCDConstants.APPLICATIONS_PARAM;
import static com.publicissapient.kpidashboard.argocd.constants.ArgoCDConstants.ARGOCD_CLUSTER_ENDPOINT;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.publicissapient.kpidashboard.argocd.dto.Application;
import com.publicissapient.kpidashboard.argocd.dto.ApplicationsList;

@ExtendWith(SpringExtension.class)
class ArgoCDClientTest {

	public static final String ARGOCD_URL = "url";

	public static final String ACCESS_TOKEN = "accessToken";

	@InjectMocks
	private ArgoCDClient argoCDClient;

	@Mock
	private RestTemplate restTemplate;

	@Test
	void testGetApplications() throws JsonMappingException, JsonProcessingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		ApplicationsList applications = mapper.readValue(getStringFromJson("applications.json"), ApplicationsList.class);
		when(restTemplate.exchange(Mockito.eq(URI.create(ARGOCD_URL + APPLICATIONS_ENDPOINT + "?" + APPLICATIONS_PARAM)),
				Mockito.eq(HttpMethod.GET), Mockito.any(HttpEntity.class), Mockito.<Class<ApplicationsList>>any()))
				.thenReturn(new ResponseEntity<ApplicationsList>(applications, HttpStatus.OK));
		ApplicationsList response = argoCDClient.getApplications(ARGOCD_URL, ACCESS_TOKEN);
		assertNotNull(response);
	}

	@Test
	void testGetApplicationsWithException() {
		when(restTemplate.exchange(
						Mockito.eq(URI.create(ARGOCD_URL + APPLICATIONS_ENDPOINT + "?" + APPLICATIONS_PARAM)),
						Mockito.eq(HttpMethod.GET),
						Mockito.any(HttpEntity.class),
						Mockito.<Class<ApplicationsList>>any()))
				.thenThrow(RestClientException.class);
		Assertions.assertThrows(
				RestClientException.class, () -> argoCDClient.getApplications(ARGOCD_URL, ACCESS_TOKEN));
	}

	@Test
	void testGestApplicationByName() {
		when(restTemplate.exchange(
						Mockito.eq(URI.create(ARGOCD_URL + APPLICATIONS_ENDPOINT + "/application2")),
						Mockito.eq(HttpMethod.GET),
						Mockito.any(HttpEntity.class),
						Mockito.<Class<Application>>any()))
				.thenReturn(new ResponseEntity<Application>(new Application(), HttpStatus.OK));
		Application response =
				argoCDClient.getApplicationByName(ARGOCD_URL, "application2", ACCESS_TOKEN);
		assertNotNull(response);
	}

	@Test
	void testGetApplicationByNameWithException() {
		when(restTemplate.exchange(
						Mockito.eq(URI.create(ARGOCD_URL + APPLICATIONS_ENDPOINT + "/application2")),
						Mockito.eq(HttpMethod.GET),
						Mockito.any(HttpEntity.class),
						Mockito.<Class<Application>>any()))
				.thenThrow(RestClientException.class);
		Assertions.assertThrows(
				RestClientException.class,
				() -> argoCDClient.getApplicationByName(ARGOCD_URL, "application2", ACCESS_TOKEN));
	}

	private String getStringFromJson(String fileName) throws IOException {
		String filePath = "src/test/resources/" + fileName;
		return new String(Files.readAllBytes(Paths.get(filePath)));
	}

	@Test
	void testGetClusterName() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		String jsonResponse = "{\"items\":[{\"server\":\"mdgsseunspdaks03\",\"name\":\"dev-auth\"}]}";
		when(restTemplate.exchange(Mockito.eq(URI.create(ARGOCD_URL + ARGOCD_CLUSTER_ENDPOINT)), Mockito.eq(HttpMethod.GET),
				Mockito.any(HttpEntity.class), Mockito.eq(String.class)))
				.thenReturn(new ResponseEntity<>(jsonResponse, HttpStatus.OK));

		Map<String, String> response = argoCDClient.getClusterName(ARGOCD_URL, ACCESS_TOKEN);
		assertNotNull(response);
		assertEquals(1, response.size());
		assertEquals("dev-auth", response.get("mdgsseunspdaks03"));
	}

	@Test
	void testGetClusterNameWithException() {
		when(restTemplate.exchange(
						Mockito.eq(URI.create(ARGOCD_URL + ARGOCD_CLUSTER_ENDPOINT)),
						Mockito.eq(HttpMethod.GET),
						Mockito.any(HttpEntity.class),
						Mockito.eq(String.class)))
				.thenThrow(RestClientException.class);

		Assertions.assertThrows(
				RestClientException.class, () -> argoCDClient.getClusterName(ARGOCD_URL, ACCESS_TOKEN));
	}
}
