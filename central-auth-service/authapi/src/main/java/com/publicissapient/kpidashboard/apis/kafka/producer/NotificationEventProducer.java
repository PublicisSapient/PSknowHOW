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
import java.util.concurrent.CompletableFuture;

import org.apache.commons.collections4.MapUtils;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.service.dto.EmailEventDTO;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * This class is responsible to send message event to kafka topic
 *
 * @author Hiren Babariya
 */
@Slf4j
@Component
@AllArgsConstructor
public class NotificationEventProducer {

	private static final Logger LOGGER = LoggerFactory.getLogger(NotificationEventProducer.class);
	private static final String SUCCESS_MESSAGE = "Mail message to topic sent successfully";
	private static final String FAILURE_MESSAGE = "Error Sending the mail message to topic and the exception is: ";

	public void sendNotificationEvent(String key, EmailEventDTO email, Map<String, String> headerDetails, String topic,
			KafkaTemplate<String, Object> kafkaTemplate) {
		try {
			LOGGER.info("Notification Switch is on. Sending message now.....");
			ProducerRecord<String, Object> producerRecord = buildProducerRecord(key, email, headerDetails, topic);
			LOGGER.info("created producer record.....");
			CompletableFuture<SendResult<String, Object>> completableFuture = kafkaTemplate.send(producerRecord);
			LOGGER.info("sent msg.....");
			completableFuture.whenComplete((result, ex) -> {
				if (ex == null) {
					handleSuccess(key, email, result);
				} else {
					handleFailure(ex);
				}
			});
		} catch (Exception ex) {
			LOGGER.info(String.format("Notification Event %s", ex.getMessage()));
		}
	}

	private ProducerRecord<String, Object> buildProducerRecord(String key, EmailEventDTO email,
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
		LOGGER.error(FAILURE_MESSAGE + ex.getMessage(), ex);
	}

	private void handleSuccess(String key, EmailEventDTO email, SendResult<String, Object> result) {
		LOGGER.info(SUCCESS_MESSAGE + " key : {}, value : {}, Partition : {}", key, email.getSubject(),
				result.getRecordMetadata().partition());
	}

}
