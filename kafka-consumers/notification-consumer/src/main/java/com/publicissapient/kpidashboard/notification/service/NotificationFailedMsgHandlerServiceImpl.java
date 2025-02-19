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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.collections4.MapUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.notification.config.NotificationConsumerConfig;
import com.publicissapient.kpidashboard.notification.model.EmailEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NotificationFailedMsgHandlerServiceImpl implements NotificationFailedMsgHandlerService {

	@Autowired
	KafkaTemplate<String, Object> kafkaTemplate;

	@Autowired
	private NotificationConsumerConfig notificationConsumerConfig;

	private static final String SUCCESS_MESSAGE = "Mail message to topic sent successfully";
	private static final String FAILURE_MESSAGE = "Error Sending the mail message to topic and the exception is: {}";

	public void handleFailedMessage(ConsumerRecord<String, EmailEvent> consumerRecord) {
		log.info("Persisting failed messages");
	}

	public void handleRecoverableMessage(ConsumerRecord<String, EmailEvent> consumerRecord) {
		String key = consumerRecord.key();
		EmailEvent email = consumerRecord.value();

		ProducerRecord<String, Object> producerRecord = buildProducerRecord(key, email, new HashMap<>());
		CompletableFuture<SendResult<String, Object>> completableFuture = kafkaTemplate.send(producerRecord);
		completableFuture.whenComplete((result, ex) -> {
			if (ex == null) {
				handleSuccess(key, email, result);
			} else {
				handleFailure(key, email, ex);
			}
		});
	}

	private ProducerRecord<String, Object> buildProducerRecord(String key, Object email,
			Map<String, String> headerDetails) {
		List<Header> recordHeaders = new ArrayList<>();
		if (MapUtils.isNotEmpty(headerDetails)) {
			headerDetails.forEach((k, v) -> {
				RecordHeader recordHeader = new RecordHeader(k, v.getBytes(StandardCharsets.UTF_8));
				recordHeaders.add(recordHeader);
			});
		}
		return new ProducerRecord<>(notificationConsumerConfig.getKafkaMailTopic(), null, key, email, recordHeaders);
	}

	private void handleFailure(String key, EmailEvent email, Throwable ex) {
		log.error(FAILURE_MESSAGE, ex.getMessage());
		try {
			throw ex;
		} catch (Throwable th) {
			log.error("Error in onFailure :{}", th.getMessage());
		}
	}

	private void handleSuccess(String key, EmailEvent email, SendResult<String, Object> result) {
		log.info(SUCCESS_MESSAGE + " key : {}, value : {}, Partition : {}", key, email.getSubject(),
				result.getRecordMetadata().partition());
	}
}
