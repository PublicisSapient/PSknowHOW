package com.publicissapient.kpidashboard.notification.config;

import lombok.Data;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Setter
@Data
@ConfigurationProperties
public class SendGridEmailConsumerConfig {

    @Value("${spring.sendgrid.api-key}")
    private String sendGridApiKey;

    @Value("${spring.sendgrid.api-end-point}")
    private String sendGridApiEndPoint;

}
