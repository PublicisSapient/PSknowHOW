package com.publicissapient.kpidashboard.common.service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiConsumer;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateInputException;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.publicissapient.kpidashboard.common.kafka.producer.NotificationEventProducer;
import com.publicissapient.kpidashboard.common.model.application.EmailServerDetail;
import com.publicissapient.kpidashboard.common.model.application.GlobalConfig;
import com.publicissapient.kpidashboard.common.model.notification.EmailEvent;
import com.publicissapient.kpidashboard.common.repository.application.GlobalConfigRepository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class NotificationServiceImpl implements NotificationService  {

    @Autowired
    private NotificationEventProducer notificationEventProducer;

    @Autowired
    private GlobalConfigRepository globalConfigRepository;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Override
    public void sendNotificationEvent(List<String> emailAddresses, Map<String, String> customData, String notSubject,
                                      String notKey, String topic, boolean notificationSwitch, KafkaTemplate<String, Object> kafkaTemplate, String templateKey, boolean isMailWithoutKafka) {

        if (!isMailWithoutKafka) {
            if (StringUtils.isNotBlank(notSubject)) {
                EmailServerDetail emailServerDetail = getEmailServerDetail();
                if (emailServerDetail != null) {
                    EmailEvent emailEvent = new EmailEvent(emailServerDetail.getFromEmail(), emailAddresses, null, null,
                            notSubject, null, customData, emailServerDetail.getEmailHost(),
                            emailServerDetail.getEmailPort());
                    notificationEventProducer.sendNotificationEvent(notKey, emailEvent, null, topic, notificationSwitch, kafkaTemplate);
                } else {
                    log.error("Notification Event not sent : notification emailServer Details not found in db");
                }
            } else {
                log.error("Notification Event not sent : notification subject for {} not found in properties file",
                        notSubject);
            }
        }else {
            sendEmailWithoutKafka(emailAddresses, customData, notSubject, notKey, topic, templateKey);
        }

    }

    @Override
    public void sendEmailWithoutKafka(List<String> emailAddresses, Map<String, String> additionalData, String notSubject, String notKey, String topic, String templateKey) {
        EmailServerDetail emailServerDetail = getEmailServerDetail();
        if (StringUtils.isNotBlank(notSubject) && emailServerDetail!=null) {
            EmailEvent emailEvent = new EmailEvent(emailServerDetail.getFromEmail(), emailAddresses, null, null, notSubject, null, additionalData, emailServerDetail.getEmailHost(), emailServerDetail.getEmailPort());
            JavaMailSenderImpl javaMailSender = getJavaMailSender(emailEvent);
            MimeMessage message = javaMailSender.createMimeMessage();
            try {
                MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
                Context context = new Context();
                Map<String, String> customData = emailEvent.getCustomData();
                if (MapUtils.isNotEmpty(customData)) {
                    customData.forEach((k, value) -> {
                        BiConsumer<String, Object> setVariable = context::setVariable;
                        setVariable.accept(k,value);
                    });
                }
                String html = templateEngine.process(templateKey, context);
                if(StringUtils.isNotEmpty(html)) {
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
