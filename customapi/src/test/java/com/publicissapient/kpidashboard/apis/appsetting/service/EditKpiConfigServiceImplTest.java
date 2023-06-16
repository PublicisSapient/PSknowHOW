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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.common.model.jira.BoardMetadata;
import com.publicissapient.kpidashboard.common.model.jira.Metadata;
import com.publicissapient.kpidashboard.common.model.jira.MetadataValue;
import com.publicissapient.kpidashboard.common.repository.jira.BoardMetadataRepository;

/**
 * This class provides various methods to TEST operations on EditKPIConfig
 *
 * @author jagmongr
 */
@RunWith(MockitoJUnitRunner.class)
public class EditKpiConfigServiceImplTest {

	BoardMetadata testBoardMetadata = new BoardMetadata();
	/*
	 * Creating a new string object to store test projectconfigid
	 */
	String testProjectconfigid;

	/*
	 * Creating a new test BoardMetadata object
	 */
	/*
	 * Creating a new string object to store test type
	 */
	String testType;
	@InjectMocks
	private EditKpiConfigServiceImpl editKpiConfigServiceImpl;
	@Mock
	private BoardMetadataRepository boardMetadataRepository;

	/**
	 * method includes preprocesses for test cases
	 */
	@Before
	public void setUp() {
		List<String> typevalueList = new ArrayList<>();
		typevalueList.add("Test");
		testBoardMetadata.setProjectBasicConfigId(new ObjectId("5f7ee917485b2c09bc8bac7a"));
		testBoardMetadata.setProjectToolConfigId(new ObjectId("5f7ee917485b2c09bc8bac7a"));
		List<Metadata> metaList = new ArrayList<>();
		Metadata metadata = new Metadata();
		metadata.setType("Issue_Type");
		List<MetadataValue> metaValueList = new ArrayList<>();
		MetadataValue metaDataValue = new MetadataValue();
		metaDataValue.setKey("Story");
		metaDataValue.setData("Story");
		metaValueList.add(metaDataValue);
		metadata.setValue(metaValueList);
		metaList.add(metadata);
		testBoardMetadata.setMetadata(metaList);

	}

	@After
	public void cleanup() {
		testBoardMetadata = new BoardMetadata();
	}

	/**
	 * 1. Input String projectconfigid is valid and data at this id doesnot exists
	 * in the database.
	 *
	 */
	@Test
	public void testgetDataForType1() {
		testType = "Test";
		testProjectconfigid = "5f7ee917485b2c09bc8bac7a";
		List<BoardMetadata> testListBoardMetadata = new ArrayList<>();
		testListBoardMetadata.add(testBoardMetadata);
		when(boardMetadataRepository.findByProjectToolConfigId(new ObjectId(testProjectconfigid)))
				.thenReturn(testBoardMetadata);
		Map<String, List<MetadataValue>> data = editKpiConfigServiceImpl.getDataForType(testProjectconfigid);
		assertThat("Count : ", data.size(), equalTo(1));

	}
}
