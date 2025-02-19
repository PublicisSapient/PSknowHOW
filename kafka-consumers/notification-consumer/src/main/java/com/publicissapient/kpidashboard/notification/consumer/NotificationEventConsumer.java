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
package com.publicissapient.kpidashboard.notification.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.notification.config.NotificationConsumerConfig;
import com.publicissapient.kpidashboard.notification.model.EmailEvent;
import com.publicissapient.kpidashboard.notification.service.NotificationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class NotificationEventConsumer {

	@Autowired
	NotificationConsumerConfig notificationConsumerConfig;

	@Autowired
	NotificationService notificationService;

	@KafkaListener(topics = "#{'${kafka.mailtopic}'}")
	public void onMessage(ConsumerRecord<String, EmailEvent> consumerRecord) {

		String key = consumerRecord.key();
		EmailEvent emailEvent = consumerRecord.value();
		log.info("Message Received key :{} Subject :{}", key, emailEvent.getSubject());
		if (!notificationConsumerConfig.isSendGridEnabled()) {
			notificationService.sendMail(key, emailEvent);
		} else {
			notificationService.sendMailUsingSendGrid(key, emailEvent);
		}
	}
}
