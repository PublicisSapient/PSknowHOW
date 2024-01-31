package com.publicissapient.kpidashboard.notification.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties
@AllArgsConstructor
@NoArgsConstructor
@Data
public class NotificationConsumerConfig {

    @Value("${kafka.mailtopic}")
    private String kafkaMailTopic;

    private String fromEmail;

    private Map<String, String> mailTemplate;

    private boolean sendGridEnabled;
}
