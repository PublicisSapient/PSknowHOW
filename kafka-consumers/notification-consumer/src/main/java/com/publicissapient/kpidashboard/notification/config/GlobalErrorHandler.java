package com.publicissapient.kpidashboard.notification.config;

import com.publicissapient.kpidashboard.notification.model.EmailEvent;
import com.publicissapient.kpidashboard.notification.service.NotificationFailedMsgHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;

import java.util.List;

@Slf4j
public class GlobalErrorHandler implements CommonErrorHandler {
    @Autowired
    NotificationFailedMsgHandlerService notificationFailedMsgHandlerService;

    @Override
    public boolean handleOne(Exception thrownException, ConsumerRecord<?, ?> record, Consumer<?, ?> consumer, MessageListenerContainer container) {
        log.info("Exception in consumerConfig is {} and the record is {}", thrownException.getMessage(), record);
        ConsumerRecord<String, EmailEvent> failedMessage = (ConsumerRecord<String, EmailEvent>) record;
        notificationFailedMsgHandlerService.handleFailedMessage(failedMessage);
        return true;
    }

}
