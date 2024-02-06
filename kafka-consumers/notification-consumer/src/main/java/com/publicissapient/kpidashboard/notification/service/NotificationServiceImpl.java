package com.publicissapient.kpidashboard.notification.service;

import com.publicissapient.kpidashboard.notification.config.NotificationConsumerConfig;
import com.publicissapient.kpidashboard.notification.model.EmailEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateInputException;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

	@Autowired
	private SpringTemplateEngine templateEngine;

	@Autowired
	private NotificationConsumerConfig notificationConsumerConfig;

	public void sendMail(String key, EmailEvent emailEvent) {

		JavaMailSenderImpl mailSender = getJavaMailSender(emailEvent);
		MimeMessage message = mailSender.createMimeMessage();
		try {
			MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
					StandardCharsets.UTF_8.name());
			Context context = new Context();
			Map<String,String> customData=emailEvent.getCustomData();
			if(MapUtils.isNotEmpty(customData)) {
				customData.forEach((k,value) ->context.setVariable(k, value));
			}	
			String template = getTemplate(key);
			if (StringUtils.isNotBlank(template)) {
				String html = templateEngine.process(template, context);
				helper.setTo(emailEvent.getTo().stream().toArray(String[]::new));
				helper.setText(html, true);
				helper.setSubject(emailEvent.getSubject());
				helper.setFrom(emailEvent.getFrom());
				mailSender.send(message);
				log.info("Email successfully sent for the key : {}", key);
			}else {
				log.error("Email not sent. Template not found for the key : {}", key);
			}

		} catch (MessagingException me) {
			log.error("Email not sent for the key : {}", key);
		}catch (TemplateInputException tie) {
			log.error("Template not found for the key : {}", key);
			throw new RecoverableDataAccessException("Template not found for the key :"+ key);
		} catch(TemplateProcessingException tpe) {
			throw new RecoverableDataAccessException("Template not parsed for the key :"+ key);
		}
			
	}

	private String getTemplate(String key) {
		String template="";
		Map<String,String> keyTemplateMapping=notificationConsumerConfig.getMailTemplate();
		if(MapUtils.isNotEmpty(keyTemplateMapping)) {
			template=keyTemplateMapping.get(key);
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
		return mailSender;
	}
}