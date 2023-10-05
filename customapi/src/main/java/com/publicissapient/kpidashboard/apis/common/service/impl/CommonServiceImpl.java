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

package com.publicissapient.kpidashboard.apis.common.service.impl;

import java.io.File;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateInputException;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.spring5.SpringTemplateEngine;

import com.publicissapient.kpidashboard.apis.auth.model.Authentication;
import com.publicissapient.kpidashboard.apis.auth.repository.AuthenticationRepository;
import com.publicissapient.kpidashboard.apis.common.service.CommonService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.kafka.producer.NotificationEventProducer;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.EmailServerDetail;
import com.publicissapient.kpidashboard.common.model.application.GlobalConfig;
import com.publicissapient.kpidashboard.common.model.application.HierarchyValue;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.notification.EmailEvent;
import com.publicissapient.kpidashboard.common.model.rbac.ProjectsAccess;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;
import com.publicissapient.kpidashboard.common.repository.application.GlobalConfigRepository;
import com.publicissapient.kpidashboard.common.repository.application.ProjectBasicConfigRepository;
import com.publicissapient.kpidashboard.common.repository.rbac.UserInfoRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of {@link CommonService} to get maturity level
 * 
 * @author anisingh4
 *
 */

@Service
@Slf4j
public class CommonServiceImpl implements CommonService {

	public static final String AND_VALUE_IS = " & Value is: ";
	public static final String SPRINT_NAME_IS = "Sprint Name is: ";

	@Autowired
	private UserInfoRepository userInfoRepository;

	@Autowired
	private AuthenticationRepository authenticationRepository;

	@Autowired
	private NotificationEventProducer notificationEventProducer;

	@Autowired
	private CustomApiConfig customApiConfig;

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private ProjectBasicConfigRepository projectBasicConfigRepository;

	@Autowired
	private GlobalConfigRepository globalConfigRepository;

	@Autowired
	private SpringTemplateEngine templateEngine;

	@SuppressWarnings("PMD.AvoidCatchingGenericException")
	@Override
	public String getMaturityLevel(List<String> maturityRangeList, String kpiId, String actualMaturityVal) {

		String maturityRange = Arrays.toString(maturityRangeList.toArray());

		log.info("Fetching maturity level for {} with value: {} and range: {}", kpiId, actualMaturityVal,
				maturityRange);
		try {
			if (actualMaturityVal == null || Constant.NOT_AVAILABLE.equalsIgnoreCase(actualMaturityVal)) {
				return Constant.ZERO;
			}

			String[] array = maturityRangeList.get(3).split("-");
			boolean upFlag = isBasedOnHighVal(kpiId, array);

			for (int i = maturityRangeList.size() - 1; i >= 0; i--) {
				boolean isValueMatched;
				if (hasSingleValueList(kpiId)) {
					isValueMatched = isValueMatchedForSingleVal(maturityRangeList, actualMaturityVal, i);
				} else {
					Double actualVal = Double.valueOf(actualMaturityVal);
					String[] boundaries = maturityRangeList.get(i).split("-");
					if (upFlag) {
						isValueMatched = isValueMatchedForUpRange(actualVal, boundaries);
					} else {
						isValueMatched = isValueMatchedForDownRange(actualVal, boundaries);
					}
				}
				if (isValueMatched) {
					return String.valueOf(i + 1);
				}
			}
		} catch (Exception e) {
			log.error("Exception occurred for {}  with value as {}, Exception: {}", kpiId, actualMaturityVal,
					ExceptionUtils.getStackTrace(e));
			return Constant.ZERO;
		}
		return Constant.ZERO;
	}

	/**
	 * Returns true if maturity level matched with actual value
	 * 
	 * @param maturityRangeList
	 * @param actualMaturityVal
	 * @param index
	 * @return
	 */
	private boolean isValueMatchedForSingleVal(List<String> maturityRangeList, String actualMaturityVal, int index) {
		String maturityLevel = maturityRangeList.get(index);

		return maturityLevel.equalsIgnoreCase(actualMaturityVal);
	}

	/**
	 * Returns true actual value is matched with down range
	 * 
	 * @param actualVal
	 * @param boundaries
	 * @return
	 */
	private boolean isValueMatchedForDownRange(Double actualVal, String[] boundaries) {
		if (boundaries.length == 2) {
			if ((boundaries[0].equalsIgnoreCase(Constant.EMPTY_STRING) && actualVal > Double.valueOf(boundaries[1]))
					|| (!boundaries[0].equalsIgnoreCase(Constant.EMPTY_STRING)
							&& actualVal <= Double.valueOf(boundaries[0])
							&& actualVal > Double.valueOf(boundaries[1]))) {
				return true;
			}
		} else {
			if (actualVal <= Double.valueOf(boundaries[0])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true actual value is matched with Up range
	 * 
	 * @param actualVal
	 * @param boundaries
	 * @return
	 */
	private boolean isValueMatchedForUpRange(Double actualVal, String[] boundaries) {

		if (boundaries.length == 2) {
			if ((boundaries[0].equalsIgnoreCase(Constant.EMPTY_STRING) && actualVal < Double.valueOf(boundaries[1]))
					|| (!boundaries[0].equalsIgnoreCase(Constant.EMPTY_STRING)
							&& actualVal >= Double.valueOf(boundaries[0])
							&& actualVal < Double.valueOf(boundaries[1]))) {
				return true;
			}
		} else {
			if (actualVal >= Double.valueOf(boundaries[0])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if type of kpi has single valued list of range
	 *
	 * @param type
	 * @return
	 */
	private boolean hasSingleValueList(String type) {
		return KPICode.SONAR_CODE_QUALITY.getKpiId().equalsIgnoreCase(type)
				|| KPICode.CODE_QUALITY_KANBAN.getKpiId().equalsIgnoreCase(type);
	}

	/**
	 * Returns true if high value means high maturity
	 *
	 * @param type
	 * @param array
	 * @return
	 */
	private boolean isBasedOnHighVal(String type, String[] array) {
		return !hasSingleValueList(type) && Double.valueOf(array[0]) <= Double.valueOf(array[1]);
	}

	/**
	 * This method is to search the email addresses based on roles
	 * 
	 * @param roles
	 * @return list of email addresses
	 */
	public List<String> getEmailAddressBasedOnRoles(List<String> roles) {
		Set<String> emailAddresses = new HashSet<>();
		List<UserInfo> superAdminUsersList = userInfoRepository.findByAuthoritiesIn(roles);
		if (CollectionUtils.isNotEmpty(superAdminUsersList)) {
			List<String> userNames = superAdminUsersList.stream().map(UserInfo::getUsername)
					.collect(Collectors.toList());
			emailAddresses
					.addAll(superAdminUsersList.stream().filter(user -> StringUtils.isNotEmpty(user.getEmailAddress()))
							.map(UserInfo::getEmailAddress).collect(Collectors.toSet()));
			List<Authentication> authentications = authenticationRepository.findByUsernameIn(userNames);
			if (CollectionUtils.isNotEmpty(authentications)) {
				emailAddresses
						.addAll(authentications.stream().map(Authentication::getEmail).collect(Collectors.toSet()));

			}
		}
		return emailAddresses.stream().filter(StringUtils::isNotEmpty).collect(Collectors.toList());
	}

	/**
	 * This method get list of project admin email address
	 * 
	 * @param projectConfigId
	 *            projectConfigId
	 * @return list of email address based on projectconfigid
	 */
	public List<String> getProjectAdminEmailAddressBasedProjectId(String projectConfigId) {
		Set<String> emailAddresses = new HashSet<>();
		List<String> usernameList = new ArrayList<>();
		List<UserInfo> usersList = userInfoRepository.findByAuthoritiesIn(Arrays.asList(Constant.ROLE_PROJECT_ADMIN));
		Map<String, String> projectMap = getHierarchyMap(projectConfigId);
		if (CollectionUtils.isNotEmpty(usersList)) {
			usersList.forEach(action -> {
				Optional<ProjectsAccess> projectAccess = action.getProjectsAccess().stream()
						.filter(access -> access.getRole().equalsIgnoreCase(Constant.ROLE_PROJECT_ADMIN)).findAny();
				if (projectAccess.isPresent()) {
					projectAccess.get().getAccessNodes().stream().forEach(accessNode -> {
						if (accessNode.getAccessItems().stream().anyMatch(item -> item.getItemId()
								.equalsIgnoreCase(projectMap.get(accessNode.getAccessLevel())))) {
							usernameList.add(action.getUsername());
							emailAddresses.add(action.getEmailAddress());
						}
					});
				}
			});
		}

		if (CollectionUtils.isNotEmpty(usernameList)) {
			List<Authentication> authentications = authenticationRepository.findByUsernameIn(usernameList);
			if (CollectionUtils.isNotEmpty(authentications)) {
				emailAddresses
						.addAll(authentications.stream().map(Authentication::getEmail).collect(Collectors.toSet()));
			}
		}
		return emailAddresses.stream().filter(StringUtils::isNotEmpty).collect(Collectors.toList());
	}

	/**
	 * This method createaproject map
	 * 
	 * @param projectConfigId
	 *            projectConfigId
	 * @return map
	 */
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

	/**
	 * This method create EmailEvent object and send async message to kafka broker
	 */
	public void sendNotificationEvent(List<String> emailAddresses, Map<String, String> customData, String notSubject,
			String notKey, String topic) {
		if (!customApiConfig.isMailWithoutKafka()) {
			if (StringUtils.isNotBlank(notSubject)) {
				EmailServerDetail emailServerDetail = getEmailServerDetail();
				if (emailServerDetail != null) {
					EmailEvent emailEvent = new EmailEvent(emailServerDetail.getFromEmail(), emailAddresses, null, null,
							notSubject, null, customData, emailServerDetail.getEmailHost(),
							emailServerDetail.getEmailPort());
					notificationEventProducer.sendNotificationEvent(notKey, emailEvent, null, topic);
				} else {
					log.error("Notification Event not sent : notification emailServer Details not found in db");
				}
			} else {
				log.error("Notification Event not sent : notification subject for {} not found in properties file",
						notSubject);
			}
		} else {
			String templateKey = customApiConfig.getMailTemplate().getOrDefault(notKey, "");
			sendEmailWithoutKafka(emailAddresses, customData, notSubject, notKey, topic, templateKey);
		}

	}

	/**
	 * 
	 * Gets api host
	 **/
	public String getApiHost() throws UnknownHostException {

		StringBuilder urlPath = new StringBuilder();
		if (StringUtils.isNotEmpty(customApiConfig.getUiHost())) {
			urlPath.append(request.getScheme()).append(':').append(File.separator + File.separator)
					.append(customApiConfig.getUiHost().trim());
			// append port if local setup
			if (StringUtils.isNotEmpty(customApiConfig.getUiPort())) {
				urlPath.append(':').append(customApiConfig.getUiPort());
			}
		} else {
			throw new UnknownHostException("Api host not found in properties.");
		}

		return urlPath.toString();
	}

	/**
	 * Sort trend value map.
	 */
	public Map<String, List<DataCount>> sortTrendValueMap(Map<String, List<DataCount>> trendMap) {
		Map<String, List<DataCount>> sortedMap = new LinkedHashMap<>();
		if (null != trendMap.get(CommonConstant.OVERALL)) {
			sortedMap.put(CommonConstant.OVERALL, trendMap.get(CommonConstant.OVERALL));
			trendMap.remove(CommonConstant.OVERALL);
		}
		Map<String, List<DataCount>> temp = trendMap.entrySet().stream()
				.sorted((i1, i2) -> i1.getKey().compareTo(i2.getKey()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

		sortedMap.putAll(temp);
		return sortedMap;
	}

	@Override
	public void sendEmailWithoutKafka(List<String> emailAddresses, Map<String, String> additionalData,
			String notSubject, String notKey, String topic, String templateKey) {
		EmailServerDetail emailServerDetail = getEmailServerDetail();
		if (StringUtils.isNotBlank(notSubject) && emailServerDetail != null) {
			EmailEvent emailEvent = new EmailEvent(emailServerDetail.getFromEmail(), emailAddresses, null, null,
					notSubject, null, additionalData, emailServerDetail.getEmailHost(),
					emailServerDetail.getEmailPort());
			JavaMailSenderImpl javaMailSender = getJavaMailSender(emailEvent);
			MimeMessage message = javaMailSender.createMimeMessage();
			try {
				MimeMessageHelper helper = new MimeMessageHelper(message,
						MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
				Context context = new Context();
				Map<String, String> customData = emailEvent.getCustomData();
				if (MapUtils.isNotEmpty(customData)) {
					customData.forEach((k, value) -> {
						BiConsumer<String, Object> setVariable = context::setVariable;
						setVariable.accept(k, value);
					});
				}
				String html = templateEngine.process(templateKey, context);
				if (StringUtils.isNotEmpty(html)) {
					helper.setTo(emailEvent.getTo().stream().toArray(String[]::new));
					helper.setText(html, true);
					helper.setSubject(emailEvent.getSubject());
					helper.setFrom(emailEvent.getFrom());
					javaMailSender.send(message);
					log.info("Email successfully sent for the key : {}", templateKey);
				}
			} catch (MessagingException me) {
				log.error("Email not sent for the key : {}", templateKey);
			} catch (TemplateInputException tie) {
				log.error("Template not found for the key : {}", templateKey);
				throw new RecoverableDataAccessException("Template not found for the key :" + templateKey);
			} catch (TemplateProcessingException tpe) {
				throw new RecoverableDataAccessException("Template not parsed for the key :" + templateKey);
			}
		}
	}

	private EmailServerDetail getEmailServerDetail() {
		List<GlobalConfig> globalConfigs = globalConfigRepository.findAll();
		GlobalConfig globalConfig = CollectionUtils.isEmpty(globalConfigs) ? null : globalConfigs.get(0);
		return globalConfig == null ? null : globalConfig.getEmailServerDetail();
	}

	private JavaMailSenderImpl getJavaMailSender(EmailEvent emailEvent) {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost(emailEvent.getEmailHost());
		mailSender.setPort(emailEvent.getEmailPort());
		Properties props = mailSender.getJavaMailProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.auth", "false");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.ssl.trust", "*");
		props.put("mail.debug", "true");
		return mailSender;
	}

}
