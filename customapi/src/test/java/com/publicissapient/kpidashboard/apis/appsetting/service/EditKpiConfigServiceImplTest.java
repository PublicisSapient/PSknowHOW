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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
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

import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.BoardMetadata;
import com.publicissapient.kpidashboard.common.model.jira.Metadata;
import com.publicissapient.kpidashboard.common.model.jira.MetadataValue;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;

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
	private ConfigHelperService configHelperService;
	@Mock
	private AccountHierarchyRepository accountHierarchyRepository;

	/** method includes preprocesses for test cases */
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
	 */
	@Test
	public void testgetDataForType1() {
		testProjectconfigid = "5f7ee917485b2c09bc8bac7a";
		AccountHierarchy ac = new AccountHierarchy();
		ac.setNodeName("KnowHOW v8.0.0_PSknowHOW");
		ac.setBasicProjectConfigId(new ObjectId(testProjectconfigid));
		ac.setBeginDate("2023-08-31T00:00:00.000Z");
		ac.setEndDate("2023-10-31T00:00:00.000Z");
		ac.setLabelName("release");
		ac.setReleaseState("Released");
		AccountHierarchy ac1 = new AccountHierarchy();
		ac1.setNodeName("KnowHOW v8.0.0_PSknowHOW");
		ac1.setBasicProjectConfigId(new ObjectId(testProjectconfigid));
		ac1.setBeginDate("");
		ac1.setEndDate("2023-10-31T00:00:00.000Z");
		ac1.setLabelName("release");
		ac1.setReleaseState("Released");

		List<AccountHierarchy> ahlist = new ArrayList<>();
		ahlist.add(ac);
		ahlist.add(ac1);
		testType = "Test";
		List<BoardMetadata> testListBoardMetadata = new ArrayList<>();
		testListBoardMetadata.add(testBoardMetadata);
		when(configHelperService.getBoardMetaData(new ObjectId(testProjectconfigid))).thenReturn(testBoardMetadata);
		when(accountHierarchyRepository.findByLabelNameAndBasicProjectConfigIdAndReleaseStateOrderByEndDateDesc("release",
				new ObjectId(testProjectconfigid), "Released")).thenReturn(ahlist);
		Map<String, List<MetadataValue>> data = editKpiConfigServiceImpl.getDataForType(testProjectconfigid, "kpi150");
		assertThat("Count : ", data.size(), equalTo(2));
	}
}
