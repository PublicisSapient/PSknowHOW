package com.publicissapient.kpidashboard.notification.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.notification.config.NotificationConsumerConfig;
import com.publicissapient.kpidashboard.notification.model.EmailEvent;
import com.publicissapient.kpidashboard.notification.service.NotificationService;

@Slf4j
@Component
public class NotificationEventConsumer {
	
	@Autowired
	NotificationConsumerConfig notificationConsumerConfig;
	
	@Autowired
	NotificationService notificationService;
	
	@KafkaListener(topics= "#{'${kafka.mailtopic}'}")
	public void onMessage(ConsumerRecord<String,EmailEvent> consumerRecord) {
		
		String key=consumerRecord.key();
		EmailEvent emailEvent=consumerRecord.value();
		log.info("Message Received key :{} Subject :{}",key,emailEvent.getSubject());
		notificationService.sendMail(key,emailEvent);
		
	}

}
