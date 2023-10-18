package com.publicissapient.kpidashboard.jira.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.connection.Connection;

@RunWith(MockitoJUnitRunner.class)
public class JiraToolConfigTest {

	@Mock
	private Connection connectionMock;

	@Test
	public void testConstructorAndGetters() {
		FieldMapping fieldMapping = new FieldMapping();
		String createdAt = "2023-09-20";
		String updatedAt = "2023-09-21";
		String boardQuery = "boardQuery";

		JiraToolConfig config = new JiraToolConfig("basicProjectConfigId", Optional.of(connectionMock), "projectId",
				"projectKey", fieldMapping, createdAt, updatedAt, true, boardQuery, null // Assuming boards is null
		);

		assertEquals("basicProjectConfigId", config.getBasicProjectConfigId());
		assertEquals("projectId", config.getProjectId());
		assertEquals("projectKey", config.getProjectKey());
		assertEquals(fieldMapping, config.getFieldMapping());
		assertEquals(createdAt, config.getCreatedAt());
		assertEquals(updatedAt, config.getUpdatedAt());
		assertTrue(config.isQueryEnabled());
		assertEquals(boardQuery, config.getBoardQuery());
		assertNull(config.getBoards()); // Assuming boards is null
	}

	@Test
	public void testSetters() {
		JiraToolConfig config = new JiraToolConfig();

		config.setBasicProjectConfigId("newBasicProjectConfigId");
		config.setConnection(Optional.of(connectionMock));
		config.setProjectId("newProjectId");
		config.setProjectKey("newProjectKey");
		FieldMapping newFieldMapping = new FieldMapping();
		config.setFieldMapping(newFieldMapping);
		config.setCreatedAt("newCreatedAt");
		config.setUpdatedAt("newUpdatedAt");
		config.setQueryEnabled(false);
		config.setBoardQuery("newBoardQuery");
		config.setBoards(null);

		assertEquals("newBasicProjectConfigId", config.getBasicProjectConfigId());
		assertEquals(Optional.of(connectionMock), config.getConnection());
		assertEquals("newProjectId", config.getProjectId());
		assertEquals("newProjectKey", config.getProjectKey());
		assertEquals(newFieldMapping, config.getFieldMapping());
		assertEquals("newCreatedAt", config.getCreatedAt());
		assertEquals("newUpdatedAt", config.getUpdatedAt());
		assertFalse(config.isQueryEnabled());
		assertEquals("newBoardQuery", config.getBoardQuery());
		assertNull(config.getBoards());
	}
}
