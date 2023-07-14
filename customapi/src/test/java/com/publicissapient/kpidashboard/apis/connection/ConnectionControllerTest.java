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

package com.publicissapient.kpidashboard.apis.connection;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.publicissapient.kpidashboard.apis.connection.rest.ConnectionController;
import com.publicissapient.kpidashboard.apis.connection.service.ConnectionService;
import com.publicissapient.kpidashboard.apis.util.TestUtil;
import com.publicissapient.kpidashboard.common.model.connection.Connection;

/**
 * @author dilip
 * 
 * @author jagmongr
 *
 */

@RunWith(MockitoJUnitRunner.class)
public class ConnectionControllerTest {
	private MockMvc mockMvc;
	private Connection testconnection;

	@InjectMocks
	private ConnectionController connectionController;

	@Mock
	private ConnectionService connectionService;

	/**
	 * method includes preprocesses for test cases
	 */
	@Before
	public void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(connectionController).build();

		testconnection = new Connection();
		testconnection.setId(new ObjectId("5da46000e645ca33dc927b4a"));

	}

	/**
	 * method includes post processes for test cases
	 */
	@After
	public void cleanUp() {
		mockMvc = null;
	}

	/**
	 * method to testgetAllConnection()/connection restPoint ;
	 *
	 * Get all connection
	 *
	 * @throws Exception
	 *             exception
	 */

	@Test
	public void testgetAllConnection() throws Exception {
		this.mockMvc.perform(
				MockMvcRequestBuilders.get("/connections?type=GitLab").contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk());
	}

	/**
	 * method to Modify single connection
	 *
	 * @throws Exception
	 *             exception
	 */
	@Test
	public void testModifyConnectionById() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.put("/connections/5da46000e645ca33dc927b4a")
				.content(TestUtil.convertObjectToJsonBytes(testconnection))
				.contentType(MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isOk());
	}

	/**
	 * method to test save restPoint ; Create new connection
	 *
	 * @throws Exception
	 *             exception
	 */
	@Test
	public void testSaveConnectionDetails() throws Exception {
		mockMvc.perform(
				MockMvcRequestBuilders.post("/connections").content(TestUtil.convertObjectToJsonBytes(testconnection))
						.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk());
	}

	/**
	 * method to delete single connection
	 *
	 * @throws Exception
	 *             exception
	 */
	@Test
	public void testDeleteConnectionById() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.delete("/connections/5da46000e645ca33dc927b4a")
				.content(TestUtil.convertObjectToJsonBytes(testconnection))
				.contentType(MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isOk());
	}

}
