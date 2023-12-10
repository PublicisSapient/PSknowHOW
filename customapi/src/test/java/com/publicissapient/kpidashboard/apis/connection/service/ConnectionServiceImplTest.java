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

package com.publicissapient.kpidashboard.apis.connection.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.publicissapient.kpidashboard.apis.abac.UserAuthorizedProjectsService;
import com.publicissapient.kpidashboard.apis.auth.service.AuthenticationService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.data.ConnectionsDataFactory;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfig;
import com.publicissapient.kpidashboard.common.model.connection.Connection;
import com.publicissapient.kpidashboard.common.model.connection.ConnectionDTO;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectToolConfigRepository;
import com.publicissapient.kpidashboard.common.repository.connection.ConnectionRepository;
import com.publicissapient.kpidashboard.common.service.AesEncryptionService;

/**
 * @author dilipKr
 * 
 * @author jagmongr
 *
 */

@RunWith(MockitoJUnitRunner.class)
public class ConnectionServiceImplTest {
	Connection listDataConnection = new Connection();
	Connection listDataConnection1 = new Connection();
	ConnectionsDataFactory connectionsDataFactory = null;
	Connection testConnection = new Connection();
	Optional<Connection> testConnectionOpt = Optional.empty();
	ConnectionDTO testConnectiondto = new ConnectionDTO();
	List<String> connectionUser = new ArrayList<>();
	List<ProjectToolConfig> projectToolList = new ArrayList<>();
	List<ProjectBasicConfig> projectBasicConfigList = new ArrayList<>();
	String testId;
	String testConnectionName;
	@InjectMocks
	private ConnectionServiceImpl connectionServiceImpl;
	@Mock
	private ConnectionRepository connectionRepository;
	@Mock
	private ProjectToolConfigRepository toolRepositroy;
	@Mock
	private UserAuthorizedProjectsService authorizedProjectsService;
	@Mock
	private Authentication authentication;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private CustomApiConfig customApiConfig;
	@Mock
	private AesEncryptionService aesEncryptionService;
	@Mock
	private ProjectBasicConfigRepository projectBasicConfigRepository;
	@Mock
	private AuthenticationService authenticationService;

	/**
	 * method includes preprocesses for test cases
	 */
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		SecurityContext securityContext = mock(SecurityContext.class);

		SecurityContextHolder.setContext(securityContext);
		when(customApiConfig.getAesEncryptionKey()).thenReturn("aeskey");
		when(aesEncryptionService.encrypt(anyString(), anyString())).thenReturn("encryptedPassword");

		connectionsDataFactory = ConnectionsDataFactory.newInstance();

		listDataConnection.setType("GitLab");
		listDataConnection.setAccessToken("accesstoken");
		listDataConnection.setIsOAuth(true);

		listDataConnection1.setType("GitLab");
		listDataConnection1.setAccessToken("accesstoken");
		listDataConnection1.setIsOAuth(true);
		connectionUser.add("user91");
		listDataConnection1.setConnectionUsers(connectionUser);

		testConnectionName = "UnitTest";
		testConnection.setId(new ObjectId("5f993135485b2c5028a5d33b"));
		testConnection.setConnectionName(testConnectionName);
		testConnection.setCreatedBy("projectadmin");
		testConnectiondto.setId(new ObjectId("5f993135485b2c5028a5d33b"));
		testConnectiondto.setConnectionName(testConnectionName);
		listDataConnection.setConnPrivate(true);
		connectionUser.add("user91");
		listDataConnection.setConnectionUsers(connectionUser);

		ProjectToolConfig projectToolConfig = new ProjectToolConfig();
		projectToolConfig.setToolName("Jira");
		projectToolConfig.setBasicProjectConfigId(new ObjectId("5fc0853c410df80001701321"));
		projectToolConfig.setProjectKey("TEST");
		projectToolConfig.setCreatedAt("2020-11-27T04:50:56");
		projectToolConfig.setUpdatedAt("2020-11-30T04:54:59");
		projectToolConfig.setQueryEnabled(true);
		projectToolList.add(projectToolConfig);

		ProjectBasicConfig projectBasicConfig = new ProjectBasicConfig();

		projectBasicConfig.setProjectName("knowHow");
		projectBasicConfigList.add(projectBasicConfig);
		testConnectionOpt = Optional.of(testConnection);
		testConnectionOpt.get().setCreatedBy("superadmin");

	}

	/**
	 * method includes post processes for test cases
	 */
	@After
	public void cleanUp() {
		testConnection = new Connection();
		testConnectiondto = new ConnectionDTO();
		testConnection.setId(new ObjectId("5f993135485b2c5028a5d33b"));
		testConnection.setConnectionName("UnitTest");
		testConnection.setType("jira");
		testConnectiondto.setId(new ObjectId("5f993135485b2c5028a5d33b"));
		testConnectiondto.setConnectionName("UnitTest");

	}

	/**
	 * 1. database call has records and returns them as an array
	 *
	 */

	@Test
	public void testgetAllConnection() {
		List<Connection> dataConnection = new ArrayList<>();
		dataConnection.add(listDataConnection);
		when(connectionRepository.findAllWithoutSecret()).thenReturn(connectionsDataFactory.getConnections());
		when(authorizedProjectsService.ifSuperAdminUser()).thenReturn(true);
		ServiceResponse response = connectionServiceImpl.getAllConnection();
		Assertions.assertEquals(Boolean.TRUE, response.getSuccess());
		Assertions.assertTrue(((List<Connection>) response.getData()).size() > 0);
	}

	/**
	 * 2. database call has an error and returns null
	 *
	 */
	@Test
	public void testgetAllConnectionNoData() {

		when(connectionRepository.findAllWithoutSecret()).thenReturn(null);
		ServiceResponse response = connectionServiceImpl.getAllConnection();
		assertThat("status: ", response.getSuccess(), equalTo(false));
		assertThat("Data should not exist:  ", response.getData(), equalTo(null));
	}

	/**
	 * 3. database call has an error and returns null
	 *
	 */
	@Test
	public void testgetConnectionByTypeNoData() {
		String type = "jira";
		when(connectionRepository.findByType(type)).thenReturn(null);
		ServiceResponse response = connectionServiceImpl.getConnectionByType(type);
		assertThat("status", response.getSuccess(), equalTo(false));

		assertThat("Data should exist but empty:  ", response.getData(), equalTo(null));
	}

	/**
	 * 4. database call has records return true
	 *
	 */
	@Test
	public void testgetConnectionByType() {
		List<Connection> dataConnection1 = new ArrayList<>();
		dataConnection1.add(listDataConnection);
		dataConnection1.add(listDataConnection1);
		String type = "GitLab";
		when(authorizedProjectsService.ifSuperAdminUser()).thenReturn(true);
		when(connectionRepository.findAllWithoutSecret()).thenReturn(dataConnection1);
		ServiceResponse response = connectionServiceImpl.getConnectionByType(type);
		assertThat("status", response.getSuccess(), equalTo(true));

	}

	/**
	 * 1. Creating a connection
	 *
	 */
	@Test
	public void testSaveConnectionDetails1() {
		when(authenticationService.getLoggedInUser()).thenReturn("superadmin");
		ServiceResponse response = connectionServiceImpl
				.saveConnectionDetails(connectionsDataFactory.findConnectionsByType("Sonar").get(0));
		assertThat("status: ", response.getSuccess(), equalTo(true));

	}

	/**
	 * 2. Input connection has no name.
	 *
	 */
	@Test
	public void testSaveConnectionDetails2() {
		testConnection.setConnectionName(null);
		List<Connection> a = new ArrayList<>();
		a.add(testConnection);
		ServiceResponse response = connectionServiceImpl.saveConnectionDetails(testConnection);
		assertThat("status: ", response.getSuccess(), equalTo(false));
		assertEquals(null, response.getData());
	}

	@Test
	public void testSaveConnectionDetailsAzure() {
		Connection connection = connectionsDataFactory.findConnectionsByType("Azure").get(0);
		connection.setId(null);
		ServiceResponse response = connectionServiceImpl.saveConnectionDetails(connection);
		assertThat("status: ", response.getSuccess(), equalTo(true));
		assertNotNull(response.getData());
	}

	@Test
	public void testSaveConnectionDetailsJenkins() {
		Connection connection = connectionsDataFactory.findConnectionsByType("Jenkins").get(0);
		connection.setId(null);
		ServiceResponse response = connectionServiceImpl.saveConnectionDetails(connection);
		assertThat("status: ", response.getSuccess(), equalTo(true));
		assertNotNull(response.getData());
	}

	@Test
	public void testUpdateConnection() {
		Connection connection = connectionsDataFactory.findConnectionById("5fdc809fb55d53cc1692543c");
		when(connectionRepository.findById(new ObjectId("5fdc809fb55d53cc1692543c")))
				.thenReturn(Optional.of(connection));
		when(authenticationService.getLoggedInUser()).thenReturn("SUPERADMIN");
		ServiceResponse response = connectionServiceImpl.updateConnection("5fdc809fb55d53cc1692543c", connection);
		assertThat("status: ", response.getSuccess(), equalTo(true));
		assertEquals(((ConnectionDTO) response.getData()).getConnectionName(), connection.getConnectionName());
	}

	@Test
	public void testSaveConnectionDetailsZephyrCloud() {
		ConnectionsDataFactory connectionsDataFactory = ConnectionsDataFactory
				.newInstance("/json/connections/zephyr_conn_input.json");

		List<Connection> connectionsByType = connectionsDataFactory.findConnectionsByType(ProcessorConstants.ZEPHYR);
		List<Connection> connList = new ArrayList<>();
		List<String> connUsers = new ArrayList<>();
		connUsers.add("test");
		Connection c1 = new Connection();
		c1.setConnPrivate(true);
		c1.setType("Zephyr");
		c1.setConnectionName("Zephyr Test");
		c1.setBaseUrl("https://test.abc.com/");
		c1.setConnectionUsers(connUsers);
		c1.setConnPrivate(true);
		connList.add(c1);
		Connection connectionInput = connectionsByType.get(0);
		connectionInput.setBaseUrl("https://test.abc.com");
		connectionInput.setAccessToken("testAccessToken");
		when(authenticationService.getLoggedInUser()).thenReturn("test");
		when(connectionRepository.save(any(Connection.class))).thenReturn(connectionInput);
		when(connectionRepository.findByTypeAndConnPrivate("Zephyr", true)).thenReturn(connList);
		ServiceResponse serviceResponse = connectionServiceImpl.saveConnectionDetails(connectionInput);
		assertTrue(serviceResponse.getSuccess());
	}

	@Test
	public void testSaveConnectionDetailsZephyrServer() {
		ConnectionsDataFactory connectionsDataFactory = ConnectionsDataFactory
				.newInstance("/json/connections/zephyr_server_conn_input.json");

		List<Connection> connectionsByType = connectionsDataFactory.findConnectionsByType(ProcessorConstants.ZEPHYR);
		Connection connectionInput = connectionsByType.get(0);
		connectionInput.setBaseUrl("https://test.abc.com/jira");
		connectionInput.setUsername("test");
		connectionInput.setAccessToken("testAccessToken");
		List<Connection> connList = new ArrayList<>();
		List<String> connUsers = new ArrayList<>();
		connUsers.add("test");
		Connection c1 = new Connection();
		c1.setConnPrivate(false);
		c1.setType("Zephyr");
		c1.setConnectionName("Zephyr Test Connection");
		c1.setBaseUrl("https://test.abc.com/jira");
		c1.setUsername("test");
		c1.setConnectionUsers(connUsers);
		c1.setApiEndPoint("/rest/atm/1.0");
		c1.setCloudEnv(false);
		connList.add(c1);
		when(authenticationService.getLoggedInUser()).thenReturn("test");
		when(connectionRepository.findByTypeAndConnPrivate("Zephyr", false)).thenReturn(connList);
		ServiceResponse serviceResponse = connectionServiceImpl.saveConnectionDetails(connectionInput);
		assertFalse(serviceResponse.getSuccess());
	}

	/**
	 * 3. Input String id is null
	 *
	 */
	@Test
	public void testmodifyConnectionById1() {
		testId = null;
		List<ConnectionDTO> a = new ArrayList<>();
		a.add(testConnectiondto);
		ServiceResponse response = connectionServiceImpl.updateConnection("test", testConnection);
		assertThat("status: ", response.getSuccess(), equalTo(false));
		assertEquals(null, response.getData());
	}

	/**
	 * 4. Input String id creates invalid ObjectId
	 *
	 */
	@Test
	public void testmodifyConnectionById2() {
		testId = "5f993135485b2c5028a5d";
		List<ConnectionDTO> a = new ArrayList<>();
		a.add(testConnectiondto);
		ServiceResponse response = connectionServiceImpl.updateConnection("test", testConnection);
		assertThat("status: ", response.getSuccess(), equalTo(false));
		assertEquals(null, response.getData());
	}

	/**
	 * 5. Input String id is valid but input Connection has no name.
	 *
	 */
	@Test
	public void testModifyConnectionById3() {
		testId = "5f993135485b2c5028a5d33b";
		testConnection.setConnectionName(null);
		ServiceResponse response = connectionServiceImpl.updateConnection(testId, testConnection);
		assertThat("status: ", response.getSuccess(), equalTo(false));
		assertEquals(null, response.getData());
	}

	@Test
	public void testModifyConnectionNull() {
		testId = "5f993135485b2c5028a5d33b";
		testConnection = null;
		ServiceResponse response = connectionServiceImpl.updateConnection(testId, testConnection);
		assertThat("status: ", response.getSuccess(), equalTo(false));
		assertEquals(null, response.getData());
	}

	@Test
	public void deleteConnection_Success() {
		String id = "5fc4d61f80b6350f048a93e5";
		when(authenticationService.getLoggedInUser()).thenReturn("superadmin");
		when(connectionRepository.findById(new ObjectId(id))).thenReturn(testConnectionOpt);
		when(authorizedProjectsService.ifSuperAdminUser()).thenReturn(false);
		when(toolRepositroy.findByConnectionId(new ObjectId(id))).thenReturn(projectToolList);
		ServiceResponse response = connectionServiceImpl.deleteConnection(id);
		assertThat("deleted connection ", response.getSuccess(), equalTo(false));
	}

	@Test
	public void deleteConnection_connectionAlreadyInUse() {
		String id = "5fc4d61f80b6350f048a93e3";
		ServiceResponse response = connectionServiceImpl.deleteConnection(id);
		assertThat("Connection already in use: ", response.getSuccess(), equalTo(false));
	}

	@Test
	public void deleteConnection_invalidConnection() {
		testId = "5f993135485b2c502";
		ServiceResponse response = connectionServiceImpl.deleteConnection(testId);
		assertThat("invalid connection id: ", response.getSuccess(), equalTo(false));
		assertEquals(null, response.getData());
	}

	@Test
	public void deleteConnection_Exception() {
		String id = "5fc4d61f80b6350f048a93e5";
		Optional<Connection> testConnectionOpt = Optional.empty();
		when(connectionRepository.findById(new ObjectId(id))).thenReturn(testConnectionOpt);
		ServiceResponse response = connectionServiceImpl.deleteConnection(id);
		assertThat("no connection found to delete ", response.getSuccess(), equalTo(false));
	}

	private List<ProjectToolConfig> findByConnectionId(ObjectId id) {
		List<ProjectToolConfig> first = createMockTools().stream().filter(conn -> conn.getConnectionId().equals(id))
				.collect(Collectors.toList());
		return first;
	}

	private List<ProjectToolConfig> createMockTools() {
		ProjectToolConfig sonar1 = new ProjectToolConfig();
		sonar1.setId(new ObjectId("5fc4d61f80b6350f048a93e3"));
		sonar1.setToolName(ProcessorConstants.SONAR);
		sonar1.setBasicProjectConfigId(new ObjectId("5fc4d61e80b6350f048a9381"));
		sonar1.setConnectionId(new ObjectId("5fc4d61f80b6350f048a93da"));
		sonar1.setProjectKey("test-project-one");

		ProjectToolConfig sonar2 = new ProjectToolConfig();
		sonar2.setId(new ObjectId("5fc4d61f80b6350f048a93e2"));
		sonar2.setToolName(ProcessorConstants.SONAR);
		sonar2.setBasicProjectConfigId(new ObjectId("5fc4d61e80b6350f048a9381"));
		sonar2.setConnectionId(new ObjectId("5fc4d61f80b6350f048a93da"));
		sonar2.setProjectKey("test-project-two");

		ProjectToolConfig jira = new ProjectToolConfig();
		jira.setId(new ObjectId("5fc4d61f80b6350f048a93d9"));
		jira.setToolName(ProcessorConstants.JIRA);
		jira.setBasicProjectConfigId(new ObjectId("5fc4d61e80b6350f048a9381"));
		jira.setConnectionId(new ObjectId("5fc4d61e80b6350f048a93ad"));
		jira.setProjectId("1234");
		jira.setProjectKey("JIRA_PROJECT");

		ProjectToolConfig azure = new ProjectToolConfig();
		azure.setId(new ObjectId("5fc4d61f80b6350f048a93d7"));
		azure.setToolName(ProcessorConstants.AZURE);
		azure.setBasicProjectConfigId(new ObjectId("5fc4d61e80b6350f048a9381"));
		azure.setConnectionId(new ObjectId("5fc4d61e80b6350f048a93ae"));
		azure.setProjectId("5678");
		azure.setProjectKey("AZURE_PROJECT");

		return Arrays.asList(jira, azure, sonar1, sonar2);
	}

	@Test
	public void testgetAllConnectionForUser() {
		List<Connection> dataConnection = new ArrayList<>();
		dataConnection.add(listDataConnection);
		when(connectionRepository.findAllWithoutSecret()).thenReturn(dataConnection);
		when(authorizedProjectsService.ifSuperAdminUser()).thenReturn(false);
		when(authenticationService.getLoggedInUser()).thenReturn("user91");
		ServiceResponse response = connectionServiceImpl.getAllConnection();
		assertThat("status: ", response.getSuccess(), equalTo(true));
		dataConnection.get(0).getConnectionUsers().get(0).equals("user91");
		assertThat("Data should exist but empty: ", response.getData(), equalTo(new ArrayList<>()));
	}

	@Test
	public void testSaveConnectionDetailsSonarCloud() {
		ConnectionsDataFactory connectionsDataFactory = ConnectionsDataFactory
				.newInstance("/json/connections/sonar_conn_input.json");

		List<Connection> connectionsByType = connectionsDataFactory.findConnectionsByType(ProcessorConstants.SONAR);
		List<Connection> connList = new ArrayList<>();
		List<String> connUsers = new ArrayList<>();
		connUsers.add("test User");
		Connection conn = new Connection();
		conn.setConnPrivate(true);
		conn.setType("Sonar");
		conn.setConnectionName("Sonar Test Connection");
		conn.setBaseUrl("https://abc.com");
		conn.setAccessToken("testAccessToken");
		conn.setConnectionUsers(connUsers);
		conn.setConnPrivate(false);
		connList.add(conn);
		Connection connectionInput = connectionsByType.get(0);
		connectionInput.setBaseUrl("https://abc.com");
		connectionInput.setAccessToken("testAccessToken");
		when(authenticationService.getLoggedInUser()).thenReturn("test User");
		when(connectionRepository.findByTypeAndConnPrivate("Sonar", false)).thenReturn(connList);

		ServiceResponse serviceResponse = connectionServiceImpl.saveConnectionDetails(connectionInput);
		assertTrue(serviceResponse.getSuccess());
	}

	@Test
	public void testgetConnectionByTypeForUser() {
		List<Connection> dataConnection1 = new ArrayList<>();
		dataConnection1.add(listDataConnection);
		dataConnection1.add(listDataConnection1);
		String type = "GitLab";
		when(connectionRepository.findAllWithoutSecret()).thenReturn(dataConnection1);
		when(authorizedProjectsService.ifSuperAdminUser()).thenReturn(false);
		when(authenticationService.getLoggedInUser()).thenReturn("user91");
		ServiceResponse response = connectionServiceImpl.getConnectionByType(type);
		dataConnection1.get(0).getConnectionUsers().get(0).equals("user91");
		assertThat("status", response.getSuccess(), equalTo(true));
	}

	@Test
	public void deleteConnectionInUse() {
		String id = "5fc4d61f80b6350f048a93e5";
		when(connectionRepository.findById(new ObjectId(id))).thenReturn(testConnectionOpt);
		when(toolRepositroy.findByConnectionId(new ObjectId(id))).thenReturn(projectToolList);
		when(authorizedProjectsService.ifSuperAdminUser()).thenReturn(true);
		Set<ObjectId> basicProjectConfigIds = new HashSet<>();
		basicProjectConfigIds.add(new ObjectId("5fc4d61f80b6350f048a93da"));
		ServiceResponse response = connectionServiceImpl.deleteConnection(id);
	}

	@Test
	public void saveGitHubConnection() {
		ConnectionsDataFactory connectionsDataFactory = ConnectionsDataFactory
				.newInstance("/json/connections/github_connections_input.json");
		List<Connection> connectionsByType = connectionsDataFactory.findConnectionsByType(ProcessorConstants.GITHUB);
		List<Connection> connList = new ArrayList<>();
		List<String> connUsers = new ArrayList<>();
		connUsers.add("test");
		Connection c1 = new Connection();
		c1.setConnPrivate(true);
		c1.setType("GitHub");
		c1.setConnectionName("Test GitHub");
		c1.setBaseUrl("https://test.server.com//gitlab");
		c1.setUsername("testUser");
		c1.setConnectionUsers(connUsers);
		connList.add(c1);
		Connection connectionInput = connectionsByType.get(0);
		connectionInput.setBaseUrl("https://test.server.com//gitlab");
		connectionInput.setUsername("test");
		connectionInput.setAccessToken("testAccessToken");
		when(authenticationService.getLoggedInUser()).thenReturn("test");
		when(connectionRepository.save(any(Connection.class))).thenReturn(connectionInput);
		when(connectionRepository.findByTypeAndConnPrivate("GitHub", true)).thenReturn(connList);
		ServiceResponse serviceResponse = connectionServiceImpl.saveConnectionDetails(connectionInput);
		assertTrue(serviceResponse.getSuccess());

	}

	@Test
	public void updateGitHubConnection_ConnectionNotPresent() {

		Connection connectionInput = createGitHubConnectionInput("/json/connections/github_input_for_update.json");

		when(connectionRepository.findById(any(ObjectId.class))).thenReturn(Optional.empty());

		ServiceResponse serviceResponse = connectionServiceImpl.updateConnection(connectionInput.getId().toHexString(),
				connectionInput);
		assertFalse(serviceResponse.getSuccess());

	}

	@Test
	public void updateGitHubConnection_ConnectionWithSameName() {

		Connection connectionInput = createGitHubConnectionInput("/json/connections/github_input_for_update.json");
		Connection existingConnection = getExistingGitHubConnection();
		Connection otherConnection = getExistingGitHubConnection();
		otherConnection.setId(new ObjectId("61d6ec31caa15d6d9cdc0727"));

		when(connectionRepository.findById(any(ObjectId.class))).thenReturn(Optional.of(existingConnection));
		when(connectionRepository.findByConnectionName(anyString())).thenReturn(otherConnection);

		ServiceResponse serviceResponse = connectionServiceImpl.updateConnection(connectionInput.getId().toHexString(),
				connectionInput);
		assertFalse(serviceResponse.getSuccess());

	}

	@Test
	public void updateGitHubConnection_ConnectionByUnAuthorizedUser() {

		Connection connectionInput = createGitHubConnectionInput("/json/connections/github_input_for_update.json");
		Connection existingConnection = getExistingGitHubConnection();

		when(connectionRepository.findById(any(ObjectId.class))).thenReturn(Optional.of(existingConnection));
		when(connectionRepository.findByConnectionName(anyString())).thenReturn(existingConnection);
		when(authorizedProjectsService.ifSuperAdminUser()).thenReturn(false);
		when(authenticationService.getLoggedInUser()).thenReturn("anotherUser");

		ServiceResponse serviceResponse = connectionServiceImpl.updateConnection(connectionInput.getId().toHexString(),
				connectionInput);
		assertFalse(serviceResponse.getSuccess());

	}

	private Connection getExistingGitHubConnection() {
		return createAndGetOneGitHubConnection("/json/connections/saved_github_connection.json");
	}

	private Connection createGitHubConnectionInput(String jsonFilePath) {
		return createAndGetOneGitHubConnection(jsonFilePath);
	}

	private Connection createAndGetOneGitHubConnection(String jsonFilePath) {
		ConnectionsDataFactory connectionsDataFactory = ConnectionsDataFactory.newInstance(jsonFilePath);
		List<Connection> connectionsByType = connectionsDataFactory.findConnectionsByType(ProcessorConstants.GITHUB);
		return connectionsByType.get(0);
	}

}
