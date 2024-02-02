package com.publicissapient.kpidashboard.notification.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.publicissapient.kpidashboard.notification.model.EmailEvent;

public interface NotificationFailedMsgHandlerService {

	public void handleFailedMessage(ConsumerRecord<String, EmailEvent> consumerRecord);

	public void handleRecoverableMessage(ConsumerRecord<String, EmailEvent> consumerRecord);

}
