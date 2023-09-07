package com.publicissapient.kpidashboard.jira.service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.HierarchyValue;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.rbac.ProjectsAccess;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.rbac.UserInfoRepository;
import com.publicissapient.kpidashboard.common.service.NotificationService;
import com.publicissapient.kpidashboard.jira.config.JiraProcessorConfig;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class NotificationHandler {

	@Autowired
	private JiraProcessorConfig jiraProcessorConfig;

	@Autowired
	private KafkaTemplate<String, Object> kafkaTemplate;

	@Autowired
	private ProjectBasicConfigRepository projectBasicConfigRepository;

	@Autowired
	private UserInfoRepository userInfoRepository;

	@Autowired
	private NotificationService notificationService;

	private static final Logger LOGGER = LoggerFactory.getLogger(NotificationHandler.class);

	private static final String NOTIFICATION_SUBJECT_KEY = "errorInJiraProcessor";

	private static final String NOTIFICATION_KEY = "Error_In_Jira_Processor";
	private static final String Error_In_Jira_Processor_Template_Key = "Error_In_Jira_Processor_Template";

	public static final String ROLE_PROJECT_ADMIN = "ROLE_PROJECT_ADMIN";
	public static final String FAILED = "Failed";
	public static final String ROLE_SUPERADMIN = "ROLE_SUPERADMIN";

	public void sendEmailToProjectAdmin(String key, String value, String projectBasicConfigId) {
		List<String> emailAddresses = getProjectAdminEmailAddressBasedProjectId(projectBasicConfigId);
		if (!value.equalsIgnoreCase(FAILED)) {
			emailAddresses.addAll(getSuperAdminEmailAddress());
		}
		Map<String, String> notificationSubjects = jiraProcessorConfig.getNotificationSubject();
		if (CollectionUtils.isNotEmpty(emailAddresses) && MapUtils.isNotEmpty(notificationSubjects)) {

			Map<String, String> customData = new HashMap<>();
			customData.put(key, value);
			String subject = notificationSubjects.get(NOTIFICATION_SUBJECT_KEY);
			log.info("Notification message sent to kafka with key : {}", NOTIFICATION_KEY);
			String templateKey = jiraProcessorConfig.getMailTemplate()
					.getOrDefault(Error_In_Jira_Processor_Template_Key, "");
			notificationService.sendNotificationEvent(emailAddresses, customData, subject, NOTIFICATION_KEY,
					jiraProcessorConfig.getKafkaMailTopic(), jiraProcessorConfig.isNotificationSwitch(), kafkaTemplate,
					templateKey, jiraProcessorConfig.isMailWithoutKafka());
		} else {
			log.error("Notification Event not sent : No email address found associated with Project-Admin role");
		}
	}

	private List<String> getSuperAdminEmailAddress() {
		Set<String> emailAddresses = new HashSet<>();
		List<UserInfo> superAdminUsersList = userInfoRepository.findByAuthoritiesIn(Arrays.asList(ROLE_SUPERADMIN));
		if (CollectionUtils.isNotEmpty(superAdminUsersList)) {
			emailAddresses
					.addAll(superAdminUsersList.stream().filter(user -> StringUtils.isNotEmpty(user.getEmailAddress()))
							.map(UserInfo::getEmailAddress).collect(Collectors.toSet()));
		}
		return emailAddresses.stream().filter(StringUtils::isNotEmpty).collect(Collectors.toList());
	}

	private List<String> getProjectAdminEmailAddressBasedProjectId(String projectConfigId) {
		Set<String> emailAddresses = new HashSet<>();
		List<UserInfo> usersList = userInfoRepository.findByAuthoritiesIn(Arrays.asList(ROLE_PROJECT_ADMIN));
		Map<String, String> projectMap = getHierarchyMap(projectConfigId);
		if (CollectionUtils.isNotEmpty(usersList)) {
			usersList.forEach(action -> {
				Optional<ProjectsAccess> projectAccess = action.getProjectsAccess().stream()
						.filter(access -> access.getRole().equalsIgnoreCase(ROLE_PROJECT_ADMIN)).findAny();
				if (projectAccess.isPresent()) {
					projectAccess.get().getAccessNodes().stream().forEach(accessNode -> {
						if (accessNode.getAccessItems().stream().anyMatch(item -> item.getItemId()
								.equalsIgnoreCase(projectMap.get(accessNode.getAccessLevel())))) {
							emailAddresses.add(action.getEmailAddress());
						}
					});
				}
			});
		}

		return emailAddresses.stream().filter(StringUtils::isNotEmpty).collect(Collectors.toList());
	}

	private Map<String, String> getHierarchyMap(String projectConfigId) {
		Map<String, String> map = new HashMap<>();
		Optional<ProjectBasicConfig> basicConfig = projectBasicConfigRepository.findById(new ObjectId(projectConfigId));
		if (basicConfig.isPresent()) {
			ProjectBasicConfig projectBasicConfig = basicConfig.get();
			CollectionUtils.emptyIfNull(projectBasicConfig.getHierarchy()).stream()
					.sorted(Comparator.comparing(
							(HierarchyValue hierarchyValue) -> hierarchyValue.getHierarchyLevel().getLevel()))
					.forEach(hierarchyValue -> map.put(hierarchyValue.getHierarchyLevel().getHierarchyLevelId(),
							hierarchyValue.getValue()));
			map.put(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT, projectBasicConfig.getId().toHexString());
		}

		return map;
	}

	// public void sendEmailWithoutKafka(String key, String value, String
	// projectBasicConfigId) {
	//
	// List<String> emailAddresses =
	// getProjectAdminEmailAddressBasedProjectId(projectBasicConfigId);
	// Map<String, String> additionalData=new HashMap<>();
	// additionalData.put(key,value);
	// Map<String, String> notificationSubjects =
	// jiraProcessorConfig.getNotificationSubject();
	// String subject = notificationSubjects.get(NOTIFICATION_SUBJECT_KEY);
	// notificationService.sendEmailWithoutKafka(emailAddresses,additionalData,subject,NOTIFICATION_KEY,
	// jiraProcessorConfig.getKafkaMailTopic(),
	// Error_In_Jira_Processor_Template_Key);
	// }

}
