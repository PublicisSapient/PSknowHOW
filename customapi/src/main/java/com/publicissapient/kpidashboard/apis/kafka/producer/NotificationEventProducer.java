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

import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.common.model.notification.EmailEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * This class is responsible to send message event to kafka topic
 *
 * @author pkum34
 */
@Slf4j
@Component
public class NotificationEventProducer {

	private static final String SUCCESS_MESSAGE = "Mail message to topic sent successfully";
	private static final String FAILURE_MESSAGE = "Error Sending the mail message to topic and the exception is: ";
	@Autowired
	private KafkaTemplate<String, Object> kafkaTemplate;
	@Autowired
	private CustomApiConfig customApiConfig;

	public void sendNotificationEvent(String key, EmailEvent email, Map<String, String> headerDetails, String topic) {
		if (customApiConfig.isNotificationSwitch()) {
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
		} else {
			log.info(
					"Notification Switch is Off. If want to send notification set true for notification.switch in property");
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
		log.error(FAILURE_MESSAGE + ex.getMessage(), ex);
	}

	private void handleSuccess(String key, EmailEvent email, SendResult<String, Object> result) {
		log.info(SUCCESS_MESSAGE + " key : {}, value : {}, Partition : {}", key, email.getSubject(),
				result.getRecordMetadata().partition());
	}

}
