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

package com.publicissapient.kpidashboard.apis.util;

import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.mongodb.gridfs.GridFsOperations;

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;

@RunWith(MockitoJUnitRunner.class)
public class DefaultLogoInsertorTest {

	@Mock
	private GridFsOperations gridOperations;

	@Mock
	private CustomApiConfig customApiConfig;

	@InjectMocks
	private DefaultLogoInsertor defaultLogoInsertor;

	@Test
	public void testInsertDefaultImage() {

		when(customApiConfig.getApplicationDefaultLogo()).thenReturn("PsKnowHowLogo.png");
		defaultLogoInsertor.insertDefaultImage();
	}

	@Test
	public void testInsertDefaultImageGridFileNotNull() {

		when(customApiConfig.getApplicationDefaultLogo()).thenReturn("PsKnowHowLogo.png");
		defaultLogoInsertor.insertDefaultImage();

	}

	@Test
	public void testInsertDefaultImageGridFileNull() {

		when(customApiConfig.getApplicationDefaultLogo()).thenReturn("PsKnowHowLogo.png");

		defaultLogoInsertor.insertDefaultImage();

	}

}
