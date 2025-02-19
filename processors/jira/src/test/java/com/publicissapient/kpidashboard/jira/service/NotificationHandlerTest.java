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

package com.publicissapient.kpidashboard.jira.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.kafka.core.KafkaTemplate;

import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.HierarchyValue;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.rbac.AccessItem;
import com.publicissapient.kpidashboard.common.model.rbac.AccessNode;
import com.publicissapient.kpidashboard.common.model.rbac.ProjectsAccess;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.rbac.UserInfoRepository;
import com.publicissapient.kpidashboard.common.service.NotificationService;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;

@RunWith(MockitoJUnitRunner.class)
public class NotificationHandlerTest {

	@Mock
	private JiraProcessorConfig jiraProcessorConfig;

	@Mock
	private KafkaTemplate<String, Object> kafkaTemplate;

	@Mock
	private ProjectBasicConfigRepository projectBasicConfigRepository;

	@Mock
	private UserInfoRepository userInfoRepository;

	@Mock
	private NotificationService notificationService;

	@InjectMocks
	private NotificationHandler notificationHandler;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testSendEmailToProjectAdmin() {
		// Mock data
		String value = "Test message";
		String allFailureExceptions = "Test exceptions";
		String projectBasicConfigId = "5fd99f7bc8b51a7b55aec836";
		List<String> emailAddresses = Arrays.asList("admin1@example.com");

		// Mock behavior
		when(jiraProcessorConfig.getDomainNames()).thenReturn(Collections.singletonList("example.com"));
		when(jiraProcessorConfig.getNotificationSubject())
				.thenReturn(Collections.singletonMap("errorInJiraProcessor", "TestSubject"));
		when(userInfoRepository.findByAuthoritiesIn(
				Arrays.asList(NotificationHandler.ROLE_PROJECT_ADMIN, NotificationHandler.ROLE_SUPERADMIN)))
				.thenReturn(Collections.singletonList(createMockUserInfo()));
		HierarchyLevel hierarchyLevel = new HierarchyLevel();
		hierarchyLevel.setLevel(1);
		hierarchyLevel.setHierarchyLevelId("level1");
		hierarchyLevel.setHierarchyLevelName("Level One");

		// Mock HierarchyValue
		HierarchyValue hierarchyValue = new HierarchyValue();
		hierarchyValue.setHierarchyLevel(hierarchyLevel);
		hierarchyValue.setValue("SomeValue");

		// Mock ProjectBasicConfig
		ProjectBasicConfig projectBasicConfig = new ProjectBasicConfig();
		projectBasicConfig.setId(new ObjectId(projectBasicConfigId));
		projectBasicConfig.setProjectName("TestProject");
		projectBasicConfig.setHierarchy(Collections.singletonList(hierarchyValue));

		// Mock the findById method
		when(projectBasicConfigRepository.findById(any())).thenReturn(Optional.of(projectBasicConfig));

		// Call the method under test
		notificationHandler.sendEmailToProjectAdminAndSuperAdmin(value, allFailureExceptions, projectBasicConfigId,
				"errorInJiraProcessor", "Error_In_Jira_Processor");
		Map<String, String> customData = new HashMap<>();
		customData.put("Notification_Error", allFailureExceptions);
		customData.put("Notification_Msg", value);
		// Verify the interactions
		verify(notificationService).sendNotificationEvent(emailAddresses, customData, "TestSubject",
				"Error_In_Jira_Processor", null, false, kafkaTemplate, "", false);
	}

	private UserInfo createMockUserInfo() {
		Map<String, Boolean> notificationEmail = new HashMap<>();
		notificationEmail.put("accessAlertNotification", false);
		notificationEmail.put("errorAlertNotification", true);
		UserInfo userInfo = new UserInfo();
		userInfo.setEmailAddress("admin1@example.com");
		ProjectsAccess projectsAccess = new ProjectsAccess();
		projectsAccess.setRole(NotificationHandler.ROLE_PROJECT_ADMIN);
		AccessNode accessNode = new AccessNode();
		accessNode.setAccessLevel("project");
		AccessItem accessItem = new AccessItem();
		accessItem.setItemId("5fd99f7bc8b51a7b55aec836");
		accessNode.setAccessItems(Collections.singletonList(accessItem));
		projectsAccess.setAccessNodes(Collections.singletonList(accessNode));
		userInfo.setProjectsAccess(Collections.singletonList(projectsAccess));
		userInfo.setNotificationEmail(notificationEmail);
		return userInfo;
	}
}
