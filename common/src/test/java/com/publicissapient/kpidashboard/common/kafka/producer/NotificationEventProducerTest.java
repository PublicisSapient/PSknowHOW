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

import com.publicissapient.kpidashboard.common.model.notification.EmailEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
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