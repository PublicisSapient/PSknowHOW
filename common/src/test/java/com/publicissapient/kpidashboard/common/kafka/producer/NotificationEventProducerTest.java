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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.publicissapient.kpidashboard.common.model.notification.EmailEvent;

@ExtendWith(SpringExtension.class)
public class NotificationEventProducerTest {
	@Mock
	private KafkaTemplate<String, Object> kafkaTemplate;

	@InjectMocks
	private NotificationEventProducer notificationEventProducer;

	String key = "testKey";
	String topic = "testTopic";
	EmailEvent email = new EmailEvent(/* provide necessary data */ );
	Map<String, String> headerDetails = new HashMap<>();
	CompletableFuture<SendResult<String, Object>> listenableFuture = mock(CompletableFuture.class);
	boolean notificationSwitch;

	@BeforeEach
	public void setup() {
		headerDetails.put("key", "value");
	}

	@Test
	public void testSendNotificationEvent_Success() {
		notificationSwitch = true;

		doReturn(listenableFuture).when(kafkaTemplate).send((ProducerRecord<String, Object>) any());
		// Test
		notificationEventProducer.sendNotificationEvent(key, email, headerDetails, topic, notificationSwitch,
				kafkaTemplate);
		// Assertions or verifications as needed
		// For example, verify that kafkaTemplate.send is called once
		verify(kafkaTemplate, times(1)).send((ProducerRecord<String, Object>) any());
	}

	@Test
	public void testSendNotificationEvent_Throw() {
		notificationSwitch = true;
		// Test
		// Throw an exception during the test
		doThrow(new RuntimeException("Test exception")).when(kafkaTemplate).send((ProducerRecord<String, Object>) any());

		notificationEventProducer.sendNotificationEvent(key, email, headerDetails, topic, notificationSwitch,
				kafkaTemplate);
	}

	@Test
	public void testSendNotificationEvent_OnListnerSuccess() {
		ArgumentCaptor<BiConsumer> callbackCaptor = initilizeDataForxSendingNotificationEvent();
		TopicPartition topicPartition = new TopicPartition(topic, 2);
		SendResult<String, Object> sendResult = new SendResult<>(null, new RecordMetadata(topicPartition, 0, 0, 0, 0, 0));
		callbackCaptor.getValue().accept(sendResult, null);
	}

	@Test
	public void testSendNotificationEvent_OnListnerFailure() {
		ArgumentCaptor<BiConsumer> callbackCaptor = initilizeDataForxSendingNotificationEvent();
		callbackCaptor.getValue().accept(null, new Throwable("Test failure"));
	}

	private ArgumentCaptor<BiConsumer> initilizeDataForxSendingNotificationEvent() {
		notificationSwitch = true;

		CompletableFuture<SendResult<String, Object>> completableFuture = mock(CompletableFuture.class);
		doReturn(completableFuture).when(kafkaTemplate).send((ProducerRecord<String, Object>) any());

		// Test
		notificationEventProducer.sendNotificationEvent(key, email, headerDetails, topic, notificationSwitch,
				kafkaTemplate);

		// Assertions or verifications as needed
		// For example, verify that kafkaTemplate.send is called once
		verify(kafkaTemplate, times(1)).send((ProducerRecord<String, Object>) any());
		ArgumentCaptor<BiConsumer> callbackCaptor = ArgumentCaptor.forClass(BiConsumer.class);

		verify(completableFuture).whenComplete(callbackCaptor.capture());
		// Simulate onSuccess
		return callbackCaptor;
	}

	@Test
	public void testSendNotificationEvent_SwitchOff() {
		notificationSwitch = false;
		// Test
		notificationEventProducer.sendNotificationEvent(key, email, headerDetails, topic, notificationSwitch,
				kafkaTemplate);
		// Assertions or verifications as needed
		// For example, verify that kafkaTemplate.send is never called
		verify(kafkaTemplate, never()).send((ProducerRecord<String, Object>) any());
	}
}
