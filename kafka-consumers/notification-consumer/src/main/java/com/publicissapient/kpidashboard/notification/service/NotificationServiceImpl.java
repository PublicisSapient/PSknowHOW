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
package com.publicissapient.kpidashboard.notification.service;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import javax.validation.constraints.NotNull;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateInputException;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.publicissapient.kpidashboard.notification.config.NotificationConsumerConfig;
import com.publicissapient.kpidashboard.notification.model.EmailEvent;
import com.publicissapient.kpidashboard.notification.model.EmailTemplate;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

	@Autowired
	private SpringTemplateEngine templateEngine;

	@Autowired
	private NotificationConsumerConfig notificationConsumerConfig;

	@Autowired
	private RestTemplate restTemplate;

	private static HttpHeaders buildHttpHeader(String apiKey) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + apiKey);
		headers.setContentType(MediaType.APPLICATION_JSON);
		return headers;
	}

	/**
	 * @param key
	 *          key
	 * @param emailEvent
	 *          emailEvent
	 */
	public void sendMail(String key, EmailEvent emailEvent) {
		try {
			log.info("Send mail using SMTP");
			Context context = createContext(emailEvent.getCustomData());
			String html = processTemplate(key, context);
			sendEmailViaJMS(emailEvent, html);
		} catch (MessagingException me) {
			log.error("Email not sent for the key : {}", key);
		} catch (TemplateInputException tie) {
			log.error("Template not found for the key : {}", key);
			throw new RecoverableDataAccessException("Template not found for the key :" + key);
		} catch (TemplateProcessingException tpe) {
			throw new RecoverableDataAccessException("Template not parsed for the key :" + key);
		}
	}

	/**
	 * @param key
	 *          key
	 * @param emailEvent
	 *          emailEvent
	 */
	public void sendMailUsingSendGrid(String key, EmailEvent emailEvent) {
		try {
			log.info("Send mail using SendGrid");
			Context context = createContext(emailEvent.getCustomData());
			String html = processTemplate(key, context);
			EmailTemplate emailTemplate = EmailTemplate.fromEmailEvent(emailEvent, html);
			sendEmailViaSendGrid(emailTemplate);
			log.info("Email successfully sent via SendGrid to user {}", emailEvent.getTo().toString());
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		} catch (TemplateInputException tie) {
			log.error("Template not found for the key : {}", key);
			throw new RecoverableDataAccessException("Template not found for the key :" + key);
		} catch (TemplateProcessingException tpe) {
			throw new RecoverableDataAccessException("Template not parsed for the key :" + key);
		}
	}

	private Context createContext(Map<String, String> customData) {
		Context context = new Context();
		customData.forEach(context::setVariable);
		return context;
	}

	private String processTemplate(String key, Context context) {
		String template = getTemplate(key);
		if (StringUtils.isNotBlank(template)) {
			return templateEngine.process(template, context);
		} else {
			throw new RecoverableDataAccessException("Template not found for the key: " + key);
		}
	}

	private void sendEmailViaJMS(EmailEvent emailEvent, String htmlContent) throws MessagingException {
		JavaMailSenderImpl mailSender = getJavaMailSender(emailEvent);
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
				StandardCharsets.UTF_8.name());
		helper.setTo(emailEvent.getTo().toArray(new String[0]));
		helper.setText(htmlContent, true);
		helper.setSubject(emailEvent.getSubject());
		helper.setFrom(emailEvent.getFrom());
		mailSender.send(message);
		log.info("Email successfully sent for the key: {}", emailEvent);
	}

	private void sendEmailViaSendGrid(EmailTemplate emailTemplate) throws JsonProcessingException {
		String jsonPayload = new ObjectMapper().writeValueAsString(emailTemplate);
		HttpEntity<?> httpEntity = new HttpEntity<>(jsonPayload,
				buildHttpHeader(notificationConsumerConfig.getSendGridApiKey()));
		ResponseEntity<String> resp = restTemplate.exchange(notificationConsumerConfig.getSendGridApiEndPoint(),
				HttpMethod.POST, httpEntity, String.class);
		if (resp.getStatusCode() == HttpStatus.ACCEPTED) {
			log.info("Email successfully sent via SendGrid to user");
		} else {
			log.info("failed");
		}
	}

	private String getTemplate(String key) {
		String template = "";
		Map<String, String> keyTemplateMapping = notificationConsumerConfig.getMailTemplate();
		if (MapUtils.isNotEmpty(keyTemplateMapping)) {
			template = keyTemplateMapping.get(key);
		}
		return template;
	}

	@NotNull
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
		props.put("mail.smtp.ssl.checkserveridentity", "false");
		return mailSender;
	}
}
