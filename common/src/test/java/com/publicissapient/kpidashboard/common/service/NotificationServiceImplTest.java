package com.publicissapient.kpidashboard.common.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.MailSendException;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.publicissapient.kpidashboard.common.kafka.producer.NotificationEventProducer;
import com.publicissapient.kpidashboard.common.model.application.EmailServerDetail;
import com.publicissapient.kpidashboard.common.model.application.GlobalConfig;
import com.publicissapient.kpidashboard.common.model.notification.EmailEvent;
import com.publicissapient.kpidashboard.common.repository.application.GlobalConfigRepository;

@RunWith(MockitoJUnitRunner.class)
public class NotificationServiceImplTest {

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Mock
    private NotificationEventProducer notificationEventProducer;

    @Mock
    private GlobalConfigRepository globalConfigRepository;
    @Mock
    private SpringTemplateEngine templateEngine;
    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private GlobalConfig globalConfig;

    private List<GlobalConfig> globalConfigs = new ArrayList<>();

	@Before
	public void setUp() throws Exception {
		globalConfig = new GlobalConfig();
		globalConfig.setEnv("email");
		EmailServerDetail emailServerDetail = new EmailServerDetail();
		emailServerDetail.setEmailPort(25);
		emailServerDetail.setEmailHost("xyz.smtp.com");
		emailServerDetail.setFromEmail("xyz@abc.com");
		globalConfig.setEmailServerDetail(emailServerDetail);
		globalConfigs.add(globalConfig);
	}

    @Test
	public void testSendNotificationEventNull() {
		List<String> emailList = new ArrayList<>();
		emailList.add("abc@xyz.com");
		Map<String, String> customData=new HashMap<>();
		customData.put("abc", "xyz");
		String notSubject="";
		String notKey="key";
		String topic="topic";
		EmailEvent emailEvent = new EmailEvent(globalConfig.getEmailServerDetail().getFromEmail(), emailList, null,
				null, notSubject, null, customData, globalConfig.getEmailServerDetail().getEmailHost(),
				globalConfig.getEmailServerDetail().getEmailPort());
		notificationEventProducer.sendNotificationEvent(notKey, emailEvent, null,topic, true, kafkaTemplate);
        notificationService.sendNotificationEvent(emailList, customData, notSubject, notKey, topic, true, kafkaTemplate, "abc",false);

	}

    @Test
	public void testSendEmailWithoutKafka() {
		List<String> emailList = new ArrayList<>();
		emailList.add("abc@xyz.com");
		Map<String, String> customData = new HashMap<>();
		customData.put("abc", "xyz");
		String notSubject = "subject";
		String notKey = "key";
		String topic = "topic";
		when(globalConfigRepository.findAll()).thenReturn(globalConfigs);
		when(templateEngine.process(anyString(),any())).thenReturn("abc");
		EmailEvent emailEvent = new EmailEvent(globalConfig.getEmailServerDetail().getFromEmail(), emailList, null,
				null, notSubject, null, customData, globalConfig.getEmailServerDetail().getEmailHost(),
				globalConfig.getEmailServerDetail().getEmailPort());
		notificationEventProducer.sendNotificationEvent(notKey, emailEvent, null, topic, false, kafkaTemplate);
		Assert.assertThrows(MailSendException.class,()->
				notificationService.sendEmailWithoutKafka(emailList, customData, notSubject, notKey, topic,"Forgot_Password_Template")
		);

	}

	@Test
	public void testSendEmailWithoutKafkaKeyNotFound() {
		List<String> emailList = new ArrayList<>();
		emailList.add("abc@xyz.com");
		Map<String, String> customData = new HashMap<>();
		customData.put("abc", "xyz");
		String notSubject = "subject";
		String notKey = "key";
		String topic = "topic";
		when(globalConfigRepository.findAll()).thenReturn(globalConfigs);
		when(templateEngine.process(anyString(), any())).thenReturn(null);
		EmailEvent emailEvent = new EmailEvent(globalConfig.getEmailServerDetail().getFromEmail(), emailList, null,
				null, notSubject, null, customData, globalConfig.getEmailServerDetail().getEmailHost(),
				globalConfig.getEmailServerDetail().getEmailPort());
		notificationEventProducer.sendNotificationEvent(notKey, emailEvent, null, topic, false, kafkaTemplate);
		notificationService.sendEmailWithoutKafka(emailList, customData, notSubject, notKey, topic,"Forgot_Password_Template");
	}

	@Test
	public void testSendNotificationEvent() {
		List<String> emailList = new ArrayList<>();
		emailList.add("abc@xyz.com");
		Map<String, String> customData = new HashMap<>();
		customData.put("abc", "xyz");
		String notSubject = "subject";
		String notKey = "key";
		String topic = "topic";
		when(globalConfigRepository.findAll()).thenReturn(globalConfigs);
		EmailEvent emailEvent = new EmailEvent(globalConfig.getEmailServerDetail().getFromEmail(), emailList, null,
				null, notSubject, null, customData, globalConfig.getEmailServerDetail().getEmailHost(),
				globalConfig.getEmailServerDetail().getEmailPort());
		notificationEventProducer.sendNotificationEvent(notKey, emailEvent, null, topic, true, kafkaTemplate);
		notificationService.sendNotificationEvent(emailList, customData, notSubject, notKey, topic, true, kafkaTemplate, "abc",false);

	}
}
