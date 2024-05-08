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

import com.publicissapient.kpidashboard.apis.config.AuthProperties;
import com.publicissapient.kpidashboard.apis.entity.GlobalConfig;
import com.publicissapient.kpidashboard.apis.entity.User;
import com.publicissapient.kpidashboard.apis.entity.UserRole;
import com.publicissapient.kpidashboard.apis.kafka.producer.NotificationEventProducer;
import com.publicissapient.kpidashboard.apis.repository.GlobalConfigRepository;
import com.publicissapient.kpidashboard.apis.repository.UserRepository;
import com.publicissapient.kpidashboard.apis.repository.UserRoleRepository;
import com.publicissapient.kpidashboard.apis.service.CommonService;
import com.publicissapient.kpidashboard.common.model.notification.EmailEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Implementation of {@link CommonService} to get maturity level
 *
 * @author Hiren Babariya
 */

@Service
@AllArgsConstructor
@Slf4j
public class CommonServiceImpl implements CommonService {
    public static final String NOTIFICATION_MESSAGE_SENT_TO_KAFKA_WITH_KEY = "Notification message sent to kafka with key : {}";

    private final UserRoleRepository userRoleRepository;

    private final UserRepository userRepository;

    private final NotificationEventProducer notificationEventProducer;

    private final AuthProperties customApiConfig;

    private final GlobalConfigRepository globalConfigRepository;

    private final SpringTemplateEngine templateEngine;

    private final KafkaTemplate<String, Object> kafkaTemplate;

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
        boolean notificationSwitch = customApiConfig.isNotificationSwitch();
        if (notificationSwitch) {
            Map<String, String> notificationSubjects = customApiConfig.getNotificationSubject();
            if (CollectionUtils.isNotEmpty(emailAddresses) && MapUtils.isNotEmpty(notificationSubjects)) {
                String subject = notificationSubjects.get(subjectKey);
                log.info(NOTIFICATION_MESSAGE_SENT_TO_KAFKA_WITH_KEY, notKey);
                String templateKey = customApiConfig.getMailTemplate().getOrDefault(notKey, "");
                sendNotificationEvent(emailAddresses, customData, subject, notKey, customApiConfig.getKafkaMailTopic(),
                        kafkaTemplate, templateKey, customApiConfig.isMailWithoutKafka());
            } else {
                log.error("Notification Event not sent : No email address found "
                        + "or Property - notificationSubject.accessRequest not set in property file ");
            }
        } else {
            log.info(
                    "Notification Switch is Off. If want to send notification set true for notification.switch in property");
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
                GlobalConfig globalConfigs = globalConfigRepository.findByEnv(customApiConfig.getNotificationEnv());
                if (globalConfigs != null) {
                    EmailEvent emailEvent = new EmailEvent(globalConfigs.getFromEmail(), emailAddresses, null, null,
                            notSubject, null, customData, globalConfigs.getEmailHost(), globalConfigs.getEmailPort());
                    notificationEventProducer.sendNotificationEvent(notKey, emailEvent, null, topic, kafkaTemplate);
                } else {
                    log.error("Notification Event not sent : notification emailServer Details not found in db");
                }
            } else {
                log.error("Notification Event not sent : notification subject for {} not found in properties file",
                        notSubject);
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

    private void sendEmailWithoutKafka(List<String> emailAddresses, Map<String, String> additionalData,
                                       String notSubject, String templateKey) {
        GlobalConfig globalConfigs = globalConfigRepository.findByEnv(customApiConfig.getNotificationEnv());
        if (StringUtils.isNotBlank(notSubject) && globalConfigs != null) {
            EmailEvent emailEvent = new EmailEvent(globalConfigs.getFromEmail(), emailAddresses, null, null, notSubject,
                    null, additionalData, globalConfigs.getEmailHost(), globalConfigs.getEmailPort());
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

    /**
     * Gets api host
     **/
    public String getApiHost() throws UnknownHostException {

        StringBuilder urlPath = new StringBuilder();
        if (StringUtils.isNotEmpty(customApiConfig.getUiHost())) {
            urlPath.append(customApiConfig.getUiHost().trim());
            // append port if local setup
            if (StringUtils.isNotEmpty(customApiConfig.getServerPort())) {
                urlPath.append(':').append(customApiConfig.getServerPort());
            }
        } else {
            throw new UnknownHostException("Api host not found in properties.");
        }

        return urlPath.toString();
    }

    public String getUIHost() throws UnknownHostException {
        StringBuilder urlPath = new StringBuilder();
        urlPath.append(':').append("//");

        if (StringUtils.isNotEmpty(customApiConfig.getUiHost())) {

            if (StringUtils.isNotEmpty(customApiConfig.getUiPort())) {
                urlPath.append(customApiConfig.getUiHost());
                urlPath.append(':').append(customApiConfig.getUiPort());
            } else {
                urlPath.append(customApiConfig.getUiHost());
            }

        } else {
            throw new UnknownHostException("Ui host not found in properties.");
        }
        return urlPath.toString();
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
