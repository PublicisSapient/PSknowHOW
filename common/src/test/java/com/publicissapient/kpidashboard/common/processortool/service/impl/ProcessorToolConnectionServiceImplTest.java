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

package com.publicissapient.kpidashboard.common.processortool.service.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;

/**
 * @author narsingh9
 *
 */
@ExtendWith(SpringExtension.class)
public class ProcessorToolConnectionServiceImplTest {

	public MockMvc mockMvc;
	@InjectMocks
	ProcessorToolConnectionServiceImpl processorToolConnectionServiceImpl;
	@Mock
	private ProjectToolConfigRepository projectToolConfigRepository;
	@Mock
	private ConnectionRepository connectionRepository;
	private List<Connection> connectionList = Lists.newArrayList();

	private List<ProjectToolConfig> projectToolList = Lists.newArrayList();

	/**
	 * method includes pre processes for test cases
	 */
	@BeforeEach
	public void before() {
		mockMvc = MockMvcBuilders.standaloneSetup(processorToolConnectionServiceImpl).build();

		Connection c1 = new Connection();
		c1.setId(new ObjectId("5f9014743cb73ce896167658"));
		c1.setConnectionName("dummy");
		c1.setType("jsa");
		c1.setBaseUrl("");
		c1.setUsername("does");
		c1.setPassword("dummyPassword");
		Connection c2 = new Connection();
		c2.setId(new ObjectId("5f9014743cb73ce896167659"));
		c2.setConnectionName("dummy2");
		c2.setType("aj");
		c2.setBaseUrl("");
		c2.setUsername("does");
		c2.setPassword("dummyPassword1");
		connectionList.add(c1);
		connectionList.add(c2);

		ProjectToolConfig t1 = new ProjectToolConfig();
		t1.setId(new ObjectId());
		t1.setToolName("Jira");
		t1.setBasicProjectConfigId(new ObjectId("5f9014743cb73ce896167659"));
		t1.setConnectionId(new ObjectId("5f9014743cb73ce896167658"));
		t1.setJobName("dsa");
		ProjectToolConfig t2 = new ProjectToolConfig();
		t2.setToolName("Jira");
		t2.setBasicProjectConfigId(new ObjectId("5f9014743cb73ce896167658"));
		t2.setConnectionId(new ObjectId("5f9014743cb73ce896167659"));
		t2.setJobName("dsab");
		projectToolList.add(t1);
		projectToolList.add(t2);
	}

	/**
	 * method includes post processes for test cases
	 */
	@AfterEach
	public void after() {
		mockMvc = null;
	}

	/**
	 * method test successful return of processorToolConnection list
	 */
	@Test
	public void findByToolTest_success() {
		when(projectToolConfigRepository.findByToolName(any())).thenReturn(projectToolList);
		when(connectionRepository.findByIdIn(connectionIdSet())).thenReturn(connectionList);
		List<ProcessorToolConnection> projectToolConnectionList = processorToolConnectionServiceImpl.findByTool(any());
		assertThat(projectToolConnectionList.size(), equalTo(2));
	}

	/**
	 * method test for null project tool case
	 */
	@Test
	public void findByToolTest_nullProjectTool_success() {
		when(projectToolConfigRepository.findByToolName(any())).thenReturn(null);
		List<ProcessorToolConnection> projectToolConnectionList = processorToolConnectionServiceImpl.findByTool("Jira");
		assertThat(projectToolConnectionList.size(), equalTo(0));
	}

	/**
	 * method test for null connections
	 */
	@Test
	public void findByToolTest_nullConnection_success() {
		when(projectToolConfigRepository.findByToolName(any())).thenReturn(projectToolList);
		when(connectionRepository.findByIdIn(connectionIdSet())).thenReturn(null);
		List<ProcessorToolConnection> projectToolConnectionList = processorToolConnectionServiceImpl.findByTool(any());
		assertThat(projectToolConnectionList.size(), equalTo(0));
	}

	private Set<ObjectId> connectionIdSet() {
		return Sets.newHashSet(new ObjectId("5f9014743cb73ce896167658"), new ObjectId("5f9014743cb73ce896167659"));
	}

}
