package com.publicissapient.kpidashboard.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.publicissapient.kpidashboard.notification.config.NotificationConsumerConfig;
import com.publicissapient.kpidashboard.notification.config.SendGridEmailConsumerConfig;
import com.publicissapient.kpidashboard.notification.model.EmailEvent;
import com.publicissapient.kpidashboard.notification.model.EmailTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.internet.MimeMessage;
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

    @Autowired
    private SendGridEmailConsumerConfig sendGridEmailConsumerConfig;

    @Autowired
    private RestTemplate restTemplate;

    private static HttpHeaders buildHttpHeader(String apiKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    public void sendMail(String key, EmailEvent emailEvent) {
        try {
            Context context = createContext(emailEvent.getCustomData());
            String html = processTemplate(key, context);
            sendEmailViaJMS(emailEvent, html);
        } catch (Exception e) {
            handleEmailException(key, e);
        }
    }

    public void sendMailUsingSendGrid(String key, EmailEvent emailEvent) {
        try {
            Context context = createContext(emailEvent.getCustomData());
            String html = processTemplate(key, context);
            EmailTemplate emailTemplate = EmailTemplate.fromEmailEvent(emailEvent, html);
            sendEmailViaSendGrid(emailTemplate);
        } catch (Exception e) {
            handleEmailException(emailEvent, e);
        }
    }

    private void handleEmailException(Object source, Exception e) {
        log.error("Error while sending email: {}", e.getMessage(), e);
        throw new RecoverableDataAccessException("Error while sending email from source: " + source, e);
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

    private void sendEmailViaJMS(EmailEvent emailEvent, String htmlContent) {
        JavaMailSenderImpl mailSender = getJavaMailSender(emailEvent);
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());
            helper.setTo(emailEvent.getTo().toArray(new String[0]));
            helper.setText(htmlContent, true);
            helper.setSubject(emailEvent.getSubject());
            helper.setFrom(emailEvent.getFrom());
            mailSender.send(message);
            log.info("Email successfully sent for the key: {}", emailEvent);
        } catch (Exception e) {
            handleEmailException(emailEvent, e);
        }
    }

    private void sendEmailViaSendGrid(EmailTemplate emailTemplate) {
        try {
            String jsonPayload = new ObjectMapper().writeValueAsString(emailTemplate);
            HttpEntity<?> httpEntity = new HttpEntity<>(jsonPayload,
                    buildHttpHeader(sendGridEmailConsumerConfig.getSendGridApiKey()));
            restTemplate.exchange(sendGridEmailConsumerConfig.getSendGridApiEndPoint(), HttpMethod.POST,
                    httpEntity, String.class);
            log.info("Email successfully sent via SendGrid");
        } catch (Exception e) {
            handleEmailException(emailTemplate, e);
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
        return mailSender;
    }
}