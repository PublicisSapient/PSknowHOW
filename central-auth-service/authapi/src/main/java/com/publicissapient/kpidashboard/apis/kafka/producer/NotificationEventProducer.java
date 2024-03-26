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
package com.publicissapient.kpidashboard.apis.kafka.producer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.publicissapient.kpidashboard.apis.service.MessageService;
import com.publicissapient.kpidashboard.common.model.notification.EmailEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * This class is responsible to send message event to kafka topic
 *
 * @author Hiren Babariya
 */
@Slf4j
@Component
public class NotificationEventProducer {
	@Autowired
	private KafkaTemplate<String, Object> kafkaTemplate;

	@Autowired
	private MessageService messageService;

	public void sendNotificationEvent(String key, EmailEvent email, Map<String, String> headerDetails, String topic,
			KafkaTemplate<String, Object> kafkaTemplate) {
		try {
			log.info("Notification Switch is on. Sending message now.....");
			ProducerRecord<String, Object> producerRecord = buildProducerRecord(key, email, headerDetails, topic);
			log.info("created producer record.....");
			ListenableFuture<SendResult<String, Object>> listenableFuture = kafkaTemplate.send(producerRecord);
			log.info("sent msg.....");
			listenableFuture.addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {

				@Override
				public void onFailure(Throwable ex) {
					handleFailure(ex);
				}

				@Override
				public void onSuccess(SendResult<String, Object> result) {
					handleSuccess(key, email, result);
				}

			});
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private ProducerRecord<String, Object> buildProducerRecord(String key, EmailEvent email,
			Map<String, String> headerDetails, String topic) {
		List<Header> recordHeaders = new ArrayList<>();
		if (MapUtils.isNotEmpty(headerDetails)) {
			headerDetails.forEach((k, v) -> {
				RecordHeader recordHeader = new RecordHeader(k, v.getBytes(StandardCharsets.UTF_8));
				recordHeaders.add(recordHeader);
			});
		}
		return new ProducerRecord<>(topic, null, key, email, recordHeaders);
	}

	private void handleFailure(Throwable ex) {
		log.error(messageService.getMessage("error_mail_sent") + ": " + ex.getMessage(), ex);
	}

	private void handleSuccess(String key, EmailEvent email, SendResult<String, Object> result) {
		log.info(messageService.getMessage("success_mail_sent") + " key : {}, value : {}, Partition : {}", key,
				email.getSubject(), result.getRecordMetadata().partition());
	}

}
