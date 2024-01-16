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

package com.publicissapient.kpidashboard.apis.common.rest;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.publicissapient.kpidashboard.apis.appsetting.service.FileStorageService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.model.BaseResponse;
import com.publicissapient.kpidashboard.apis.model.Logo;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;

@RunWith(MockitoJUnitRunner.class)
public class FileStorageControllerTest {

	private MockMvc mockMvc;

	@Mock
	private FileStorageService fileStorageService;

	@InjectMocks
	private FileStorageController fileStorageController;

	@Mock
	private CustomApiConfig customApiConfig;

	@Before
	public void before() {
		MockitoAnnotations.openMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(fileStorageController).build();
	}

	@After
	public void after() {
		mockMvc = null;
	}

	@Test
	public void testFileStorageService() throws Exception {
		Logo logo = new Logo();

		when(fileStorageService.getLogo()).thenReturn(logo);
		mockMvc.perform(get("/file/logo")).andExpect(status().isOk());
	}

	@Test
	public void testDeleteLogo() throws Exception {
		when(fileStorageService.deleteLogo()).thenReturn(true);
		mockMvc.perform(get("/file/delete")).andExpect(status().isOk());
	}

	@Test
	public void testUploadFile() throws Exception {
		MockMultipartFile file = new MockMultipartFile("file", "PsKnowHowLogo.png", MediaType.TEXT_PLAIN_VALUE,
				"Hello, World!".getBytes());

		when(fileStorageService.upload(Mockito.any())).thenReturn(new BaseResponse());
		mockMvc.perform(multipart("/file/upload").file(file)).andExpect(status().isOk());
	}

	@Test
	public void testUploadCertificateSuccess() throws Exception {
		MockMultipartFile file = new MockMultipartFile("file", "certFile.cer", "application/x-x509-ca-cert",
				"LDAP certificate file success scenario to be mocked".getBytes());
		when(customApiConfig.getHostPath()).thenReturn("/app/certs/");
		ResponseEntity<ServiceResponse> response = fileStorageController.uploadCertificate(file);
		Assert.assertNotNull(response.getStatusCode());
	}

	@Test
	public void testUploadCertificateFailure() throws Exception {
		MockMultipartFile file = new MockMultipartFile("file", "certFile.cer", "application/x-x509-ca-cert",
				"LDAP certificate file failure scenario to be mocked".getBytes());
		when(customApiConfig.getHostPath()).thenReturn("/nonexistent/");
		ResponseEntity<ServiceResponse> response = fileStorageController.uploadCertificate(file);
		Assert.assertNotNull(response.getStatusCode());
		ServiceResponse serviceResponse = response.getBody();
		Assert.assertNotNull(serviceResponse.getSuccess());
		Assert.assertNotNull(serviceResponse.getMessage());
	}

	@Test
	public void testUploadCertificateTypeFailure() throws Exception {
		MockMultipartFile file = new MockMultipartFile("file", "cerFile.txt", "application/x-x509-ca-cert",
				"LDAP certificate file type scenario to be mocked".getBytes());
		when(customApiConfig.getHostPath()).thenReturn("/app/certs/");
		ResponseEntity<ServiceResponse> response = fileStorageController.uploadCertificate(file);
		Assert.assertNotNull(response.getStatusCode());
	}

}