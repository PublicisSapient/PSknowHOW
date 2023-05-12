package com.publicissapient.kpidashboard.common.service;

import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.Map;

public interface NotificationService {

    void sendNotificationEvent(List<String> emailAddresses, Map<String, String> customData, String notSubject,
                               String notKey, String topic, boolean notificationSwitch, KafkaTemplate<String, Object> kafkaTemplate, String templateKey, boolean isMailWithoutKafka);

    void sendEmailWithoutKafka(List<String> emailAddresses, Map<String, String> additionalData, String notSubject, String notKey, String topic, String templateKey);
}
