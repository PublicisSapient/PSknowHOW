/*
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.publicissapient.kpidashboard.common.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.publicissapient.kpidashboard.common.model.jira.BoardMetadata;
import com.publicissapient.kpidashboard.common.repository.jira.BoardMetadataRepository;

@ExtendWith(SpringExtension.class)
public class BoardMetadataServiceImplTest {

	@Mock
	private BoardMetadataRepository boardMetadataRepository;

	@InjectMocks
	private BoardMetadataServiceImpl boardMetadataService;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testFindAll() {
		List<BoardMetadata> expectedBoardMetadata = Arrays.asList(new BoardMetadata(), new BoardMetadata());
		when(boardMetadataRepository.findAll()).thenReturn(expectedBoardMetadata);

		List<BoardMetadata> actualBoardMetadata = boardMetadataService.findAll();

		Assertions.assertEquals(expectedBoardMetadata, actualBoardMetadata);
		verify(boardMetadataRepository, times(1)).findAll();
	}
}
