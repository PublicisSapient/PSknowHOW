package com.publicissapient.kpidashboard.common.kafka.producer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.publicissapient.kpidashboard.common.model.notification.EmailEvent;

@RunWith(MockitoJUnitRunner.class)
public class NotificationEventProducerTest {
	@Mock
	private KafkaTemplate<String, Object> kafkaTemplate;

	@InjectMocks
	private NotificationEventProducer notificationEventProducer;

	String key = "testKey";
	String topic = "testTopic";
	EmailEvent email = new EmailEvent(/* provide necessary data */);
	Map<String, String> headerDetails = new HashMap<>();
	ListenableFuture<SendResult<String, Object>> listenableFuture = mock(ListenableFuture.class);
	boolean notificationSwitch;

	@Before
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
		doThrow(new RuntimeException("Test exception")).when(kafkaTemplate)
				.send((ProducerRecord<String, Object>) any());

		notificationEventProducer.sendNotificationEvent(key, email, headerDetails, topic, notificationSwitch,
				kafkaTemplate);

	}

	@Test
	public void testSendNotificationEvent_OnListner() {
		notificationSwitch = true;

		ListenableFuture<SendResult<String, Object>> listenableFuture = mock(ListenableFuture.class);
		doReturn(listenableFuture).when(kafkaTemplate).send((ProducerRecord<String, Object>) any());

		// Test
		notificationEventProducer.sendNotificationEvent(key, email, headerDetails, topic, notificationSwitch,
				kafkaTemplate);

		// Assertions or verifications as needed
		// For example, verify that kafkaTemplate.send is called once
		verify(kafkaTemplate, times(1)).send((ProducerRecord<String, Object>) any());
		ArgumentCaptor<ListenableFutureCallback> callbackCaptor = ArgumentCaptor
				.forClass(ListenableFutureCallback.class);
		verify(listenableFuture).addCallback(callbackCaptor.capture());

		// Simulate onSuccess
		SendResult<String, Object> sendResult = mock(SendResult.class);
		when(sendResult.getRecordMetadata()).thenReturn(mock(org.apache.kafka.clients.producer.RecordMetadata.class));
		callbackCaptor.getValue().onSuccess(sendResult);

		// Assertions or verifications for onSuccess as needed
		// Simulate onFailure
		callbackCaptor.getValue().onFailure(new RuntimeException("Test failure"));
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