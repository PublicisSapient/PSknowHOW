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
package com.publicissapient.kpidashboard.common.kafka.producer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.publicissapient.kpidashboard.common.model.notification.EmailEvent;

/**
 * This class is responsible to send message event to kafka topic
 *
 * @author pkum34
 */
@Component
public class NotificationEventProducer {

	private static final Logger LOGGER = LoggerFactory.getLogger(NotificationEventProducer.class);
	private static final String SUCCESS_MESSAGE = "Mail message to topic sent successfully";
	private static final String FAILURE_MESSAGE = "Error Sending the mail message to topic and the exception is: ";

	public void sendNotificationEvent(String key, EmailEvent email, Map<String, String> headerDetails, String topic, boolean notificationSwitch, KafkaTemplate<String, Object> kafkaTemplate) {
		if (notificationSwitch) {
			try {
				LOGGER.info(
						"Notification Switch is on. Sending message now.....");
				ProducerRecord<String, Object> producerRecord = buildProducerRecord(key, email, headerDetails, topic);
				LOGGER.info(
						"created producer record.....");
				//TODO:Check the functionality
				ListenableFuture<SendResult<String, Object>> listenableFuture = (ListenableFuture<SendResult<String, Object>>) kafkaTemplate.send(producerRecord);
				LOGGER.info(
						"sent msg.....");
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
			}catch(Exception ex) {
				ex.printStackTrace();
			}
		} else {
			LOGGER.info(
					"Notification Switch is Off. If want to send notification set true for notification.switch in property");
		}

	}

	private ProducerRecord<String, Object> buildProducerRecord(String key, EmailEvent email,
															   Map<String, String> headerDetails,String topic) {
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

	private void handleSuccess(String key, EmailEvent email, SendResult<String, Object> result) {
		LOGGER.info(SUCCESS_MESSAGE + " key : {}, value : {}, Partition : {}", key, email.getSubject(),
				result.getRecordMetadata().partition());
	}

}
