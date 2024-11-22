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

package com.publicissapient.kpidashboard.apis.service.impl;

import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateInputException;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.publicissapient.kpidashboard.apis.config.AuthConfig;
import com.publicissapient.kpidashboard.apis.config.ForgotPasswordConfig;
import com.publicissapient.kpidashboard.apis.config.UserInterfacePathsConfig;
import com.publicissapient.kpidashboard.apis.constant.CommonConstant;
import com.publicissapient.kpidashboard.apis.entity.GlobalConfig;
import com.publicissapient.kpidashboard.apis.entity.User;
import com.publicissapient.kpidashboard.apis.entity.UserRole;
import com.publicissapient.kpidashboard.apis.enums.NotificationCustomDataEnum;
import com.publicissapient.kpidashboard.apis.kafka.producer.NotificationEventProducer;
import com.publicissapient.kpidashboard.apis.repository.GlobalConfigRepository;
import com.publicissapient.kpidashboard.apis.repository.UserRepository;
import com.publicissapient.kpidashboard.apis.repository.UserRoleRepository;
import com.publicissapient.kpidashboard.apis.service.NotificationService;
import com.publicissapient.kpidashboard.apis.service.dto.EmailEventDTO;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {
	public static final String NOTIFICATION_MESSAGE_SENT_TO_KAFKA_WITH_KEY = "Notification message sent to kafka with key : {}";
	private static final String FORGOT_PASSWORD_TEMPLATE_KEY = "Forgot_Password";
	private static final String FORGOT_PASSWORD_NOTIFICATION_KEY = "forgotPassword";
	private static final String VALIDATE_PATH = "/api/validateEmailToken?token="; // NOSONAR

	private final UserRoleRepository userRoleRepository;

	private final UserRepository userRepository;

	private final NotificationEventProducer notificationEventProducer;

	private final AuthConfig authConfig;

	private final ForgotPasswordConfig forgotPasswordConfig;

	private final GlobalConfigRepository globalConfigRepository;

	private final SpringTemplateEngine templateEngine;

	private final KafkaTemplate<String, Object> kafkaTemplate;

	private final UserInterfacePathsConfig userInterfacePathsConfig;

	@SuppressWarnings("PMD.AvoidCatchingGenericException")
	/**
	 * This method is to search the email addresses based on roles
	 *
	 * @param roles
	 * @return list of email addresses
	 */
	public List<String> getEmailAddressBasedOnRoles(List<String> roles) {
		Set<String> emailAddresses = new HashSet<>();
		List<UserRole> superAdminUserRoleList = userRoleRepository.findByRoles(roles);

		if (CollectionUtils.isNotEmpty(superAdminUserRoleList)) {
			List<String> superAdminUserNameList = superAdminUserRoleList.stream().map(UserRole::getUsername)
					.collect(Collectors.toList());
			List<User> superAdminUsersList = userRepository.findByUsernameIn(superAdminUserNameList);
			emailAddresses.addAll(superAdminUsersList.stream().filter(user -> StringUtils.isNotEmpty(user.getEmail()))
					.map(User::getEmail).collect(Collectors.toSet()));
			if (CollectionUtils.isNotEmpty(superAdminUsersList)) {
				emailAddresses.addAll(superAdminUsersList.stream().map(User::getEmail).collect(Collectors.toSet()));
			}
		}
		return emailAddresses.stream().filter(StringUtils::isNotEmpty).collect(Collectors.toList());
	}

	/**
	 * send notification to super admin for approval and notification to user for
	 * the status of the request used this function for mail with kafka or without
	 * kafka.
	 *
	 * @param emailAddresses
	 * @param customData
	 * @param subjectKey
	 * @param notKey
	 */
	@Override
	public void sendEmailNotification(List<String> emailAddresses, Map<String, String> customData, String subjectKey,
			String notKey) {
		if (authConfig.isNotificationSwitch()) {
			Map<String, String> notificationSubjects = authConfig.getNotificationSubject();
			if (CollectionUtils.isNotEmpty(emailAddresses) && MapUtils.isNotEmpty(notificationSubjects)) {
				String subject = notificationSubjects.get(subjectKey);
				log.info(NOTIFICATION_MESSAGE_SENT_TO_KAFKA_WITH_KEY, notKey);
				String templateKey = authConfig.getMailTemplate().getOrDefault(notKey, "");
				sendNotificationEvent(emailAddresses, customData, subject, notKey, authConfig.getKafkaMailTopic(),
						kafkaTemplate, templateKey, authConfig.isMailWithoutKafka());
			} else {
				log.error("Notification Event not sent : No email address found " +
						"or Property - notificationSubject.accessRequest not set in property file ");
			}
		} else {
			log.info("Notification Switch is Off. If want to send notification set true for notification.switch in property");
		}
	}

	/**
	 * This method create EmailEvent object and send async message to kafka broker
	 *
	 * @param emailAddresses
	 * @param customData
	 * @param notSubject
	 * @param notKey
	 * @param topic
	 * @param kafkaTemplate
	 * @param templateKey
	 * @param isMailWithoutKafka
	 */
	private void sendNotificationEvent(List<String> emailAddresses, Map<String, String> customData, String notSubject,
			String notKey, String topic, KafkaTemplate<String, Object> kafkaTemplate, String templateKey,
			boolean isMailWithoutKafka) {

		if (!isMailWithoutKafka) {
			if (StringUtils.isNotBlank(notSubject)) {
				GlobalConfig globalConfigs = globalConfigRepository.findByEnv(authConfig.getNotificationEnv());
				if (globalConfigs != null) {
					EmailEventDTO emailEventDTO = new EmailEventDTO(globalConfigs.getFromEmail(), emailAddresses, null, null,
							notSubject, null, customData, globalConfigs.getEmailHost(), globalConfigs.getEmailPort());
					notificationEventProducer.sendNotificationEvent(notKey, emailEventDTO, null, topic, kafkaTemplate);
				} else {
					log.error("Notification Event not sent : notification emailServer Details not found in db");
				}
			} else {
				log.error("Notification Event not sent : notification subject for {} not found in properties file", notSubject);
			}
		} else {
			sendEmailWithoutKafka(emailAddresses, customData, notSubject, templateKey);
		}
	}

	/**
	 * this function only use for smtp mail service
	 *
	 * @param emailAddresses
	 * @param additionalData
	 * @param notSubject
	 * @param templateKey
	 */
	private void sendEmailWithoutKafka(List<String> emailAddresses, Map<String, String> additionalData, String notSubject,
			String templateKey) {
		GlobalConfig globalConfigs = globalConfigRepository.findByEnv(authConfig.getNotificationEnv());
		if (StringUtils.isNotBlank(notSubject) && globalConfigs != null) {
			EmailEventDTO emailEventDTO = new EmailEventDTO(globalConfigs.getFromEmail(), emailAddresses, null, null,
					notSubject, null, additionalData, globalConfigs.getEmailHost(), globalConfigs.getEmailPort());
			JavaMailSenderImpl javaMailSender = getJavaMailSender(emailEventDTO);
			MimeMessage message = javaMailSender.createMimeMessage();
			try {
				MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
						StandardCharsets.UTF_8.name());
				Context context = new Context();
				Map<String, String> customData = emailEventDTO.getCustomData();
				if (MapUtils.isNotEmpty(customData)) {
					customData.forEach((k, value) -> {
						BiConsumer<String, Object> setVariable = context::setVariable;
						setVariable.accept(k, value);
					});
				}
				String html = templateEngine.process(templateKey, context);
				if (StringUtils.isNotEmpty(html)) {
					helper.setTo(emailEventDTO.getTo().stream().toArray(String[]::new));
					helper.setText(html, true);
					helper.setSubject(emailEventDTO.getSubject());
					helper.setFrom(emailEventDTO.getFrom());
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

	/** Gets api host */
	public String getApiHost() throws UnknownHostException {

		StringBuilder urlPath = new StringBuilder();
		if (StringUtils.isNotEmpty(forgotPasswordConfig.getUiHost())) {
			urlPath.append(forgotPasswordConfig.getUiHost().trim());
			// append port if local setup
			if (StringUtils.isNotEmpty(forgotPasswordConfig.getServerPort())) {
				urlPath.append(':').append(forgotPasswordConfig.getServerPort());
			}
		} else {
			throw new UnknownHostException("Api host not found in properties.");
		}

		return urlPath.toString();
	}

	String getUIHost() throws UnknownHostException {
		StringBuilder urlPath = new StringBuilder();
		urlPath.append(':').append("//");

		if (StringUtils.isNotEmpty(forgotPasswordConfig.getUiHost())) {

			if (StringUtils.isNotEmpty(forgotPasswordConfig.getUiPort())) {
				urlPath.append(forgotPasswordConfig.getUiHost());
				urlPath.append(':').append(forgotPasswordConfig.getUiPort());
			} else {
				urlPath.append(forgotPasswordConfig.getUiHost());
			}

		} else {
			throw new UnknownHostException("Ui host not found in properties.");
		}
		return urlPath.toString();
	}

	private JavaMailSenderImpl getJavaMailSender(EmailEventDTO emailEventDTO) {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost(emailEventDTO.getEmailHost());
		mailSender.setPort(emailEventDTO.getEmailPort());
		Properties props = mailSender.getJavaMailProperties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.auth", "false");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.ssl.trust", "*");
		props.put("mail.debug", "true");
		props.put("mail.smtp.ssl.checkserveridentity", "false");
		return mailSender;
	}

	@Override
	public void sendUserApprovalEmail(String username, String email) {
		String serverPath = getServerPath();
		List<String> superAdminEmailList = getEmailAddressBasedOnRoles(Arrays.asList(CommonConstant.ROLE_SUPERADMIN));

		List<String> emailAddresses = new ArrayList<>();
		emailAddresses.add(email);

		Map<String, String> customData = createCustomData(username, email, serverPath, superAdminEmailList.get(0));
		sendEmailNotification(emailAddresses, customData, CommonConstant.APPROVAL_NOTIFICATION_KEY,
				CommonConstant.APPROVAL_SUCCESS_TEMPLATE_KEY);
	}

	@Override
	public void sendRecoverPasswordEmail(String email, String username, String forgotPasswordToken) {
		try {
			Map<String, String> customData = createCustomDataForgotPassword(username, forgotPasswordToken, getApiHost(),
					forgotPasswordConfig.getExpiryInterval());

			sendEmailNotification(Arrays.asList(email), customData, FORGOT_PASSWORD_NOTIFICATION_KEY,
					FORGOT_PASSWORD_TEMPLATE_KEY);
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void sendVerificationMailToRegisterUser(String username, String email, String token) {
		String serverPath = getServerPath();

		Map<String, String> customData = createCustomDataVerificationUser(username, email, serverPath,
				this.authConfig.getVerifyUserTokenExpiryDays(), token);
		sendEmailNotification(Arrays.asList(email), customData, CommonConstant.USER_VERIFICATION_NOTIFICATION_KEY,
				CommonConstant.USER_VERIFICATION_TEMPLATE_KEY);
	}

	/**
	 * Verification Failed Mail
	 *
	 * @param username
	 * @param email
	 */
	@Override
	public void sendVerificationFailedMailUser(String username, String email) {
		String serverPath = getServerPath();
		log.info("UserServiceImpl: registered mail {}", email);
		Optional<User> userOptional = userRepository.findByEmail(email);
		if (userOptional.isPresent()) {
			Map<String, String> customData = createCustomDataVerificationUser(username, email, serverPath, "", " ");
			sendEmailNotification(Arrays.asList(email), customData, CommonConstant.USER_VERIFICATION_FAILED_NOTIFICATION_KEY,
					CommonConstant.USER_VERIFICATION_FAILED_TEMPLATE_KEY);
			userRepository.deleteByUsername(userOptional.get().getUsername());
		}
	}

	/**
	 * @param username
	 * @param email
	 */
	@Override
	public void sendUserPreApprovalRequestEmailToAdmin(String username, String email) {
		List<String> emailAddresses = getEmailAddressBasedOnRoles(Arrays.asList(CommonConstant.ROLE_SUPERADMIN));
		String serverPath = getServerPath();
		Map<String, String> customData = createCustomDataVerificationUser(username, email, serverPath, "", "");
		sendEmailNotification(emailAddresses, customData, CommonConstant.PRE_APPROVAL_NOTIFICATION_SUBJECT_KEY,
				CommonConstant.PRE_APPROVAL_NOTIFICATION_KEY);
	}

	/**
	 * * create custom data for email
	 *
	 * @param username
	 * @param email
	 * @param serverPath
	 * @param adminEmail
	 * @return
	 */
	private Map<String, String> createCustomData(String username, String email, String serverPath, String adminEmail) {
		Map<String, String> customData = new HashMap<>();
		customData.put(NotificationCustomDataEnum.USER_NAME.getValue(), username);
		customData.put(NotificationCustomDataEnum.USER_EMAIL.getValue(), email);
		customData.put(NotificationCustomDataEnum.SERVER_HOST.getValue(), serverPath);
		customData.put(NotificationCustomDataEnum.ADMIN_EMAIL.getValue(), adminEmail);
		return customData;
	}

	/**
	 * * create custom data for email
	 *
	 * @param username
	 *          emailId
	 * @param token
	 *          token
	 * @param url
	 *          url
	 * @param expiryTime
	 *          expiryTime in Min
	 * @return Map<String, String>
	 */
	private Map<String, String> createCustomDataForgotPassword(String username, String token, String url,
			Integer expiryTime) {
		Map<String, String> customData = new HashMap<>();
		customData.put("token", token);
		customData.put("user", username);
		String resetUrl = url + VALIDATE_PATH + token;
		customData.put("resetUrl", resetUrl);
		customData.put("expiryTime", expiryTime.toString());
		return customData;
	}

	/**
	 * * create custom data for email
	 *
	 * @param username
	 * @param email
	 * @param url
	 * @param expiryTime
	 * @param token
	 * @return
	 */
	private Map<String, String> createCustomDataVerificationUser(String username, String email, String url,
			String expiryTime, String token) {
		Map<String, String> customData = new HashMap<>();

		customData.put(NotificationCustomDataEnum.USER_NAME.getValue(), username);
		customData.put(NotificationCustomDataEnum.USER_EMAIL.getValue(), email);
		customData.put(NotificationCustomDataEnum.SERVER_HOST.getValue(), url);

		if (StringUtils.isNotEmpty(token) && StringUtils.isNotEmpty(expiryTime)) {
			customData.put(NotificationCustomDataEnum.USER_TOKEN.getValue(), token);
			customData.put(NotificationCustomDataEnum.USER_TOKEN_EXPIRY.getValue(), expiryTime);

			String resetUrl = url + this.userInterfacePathsConfig.getValidateUser() + token;
			customData.put("resetUrl", resetUrl);
		}

		return customData;
	}

	private String getServerPath() {
		String serverPath = "";
		try {
			serverPath = getApiHost();
		} catch (UnknownHostException e) {
			log.error("ApproveRequestController: Server Host name is not bind with Approval Request mail ");
		}
		return serverPath;
	}
}
