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

package com.publicissapient.kpidashboard.apis.appsetting.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.web.multipart.MultipartFile;

import com.mongodb.client.gridfs.GridFSFindIterable;
import com.publicissapient.kpidashboard.apis.model.BaseResponse;
import com.publicissapient.kpidashboard.apis.model.Logo;

@RunWith(MockitoJUnitRunner.class)
public class FileStorageServiceImplTest {

	@Mock
	private GridFsOperations gridOperations;

	@InjectMocks
	private FileStorageServiceImpl fileStorageServiceImpl;

	@Test
	public void testUpload() {

		MultipartFile multipartFile = new MultipartFile() {

			@Override
			public void transferTo(File dest) throws IOException, IllegalStateException {

			}

			@Override
			public boolean isEmpty() {
				return false;
			}

			@Override
			public long getSize() {
				return 0;
			}

			@Override
			public String getOriginalFilename() {
				return "test_file_for_upload";
			}

			@Override
			public String getName() {
				return null;
			}

			@Override
			public InputStream getInputStream() throws IOException {
				return null;
			}

			@Override
			public String getContentType() {
				return null;
			}

			@Override
			public byte[] getBytes() throws IOException {
				return new byte[30];
			}
		};

		BaseResponse response = fileStorageServiceImpl.upload(multipartFile);
		Assert.assertTrue(null != response);

	}

	@Test
	public void testGetLogo() {
		GridFSFindIterable gridFSFindIterable = Mockito.mock(GridFSFindIterable.class);
		Mockito.when(gridOperations.find(Mockito.any())).thenReturn(gridFSFindIterable);
		Logo logo = fileStorageServiceImpl.getLogo();
		Assert.assertTrue(null != logo);
	}

}