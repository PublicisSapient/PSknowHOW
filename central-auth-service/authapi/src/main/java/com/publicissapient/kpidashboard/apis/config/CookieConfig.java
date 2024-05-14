package com.publicissapient.kpidashboard.apis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "auth.cookie")
public class CookieConfig {
	private Integer duration;

	private Boolean isSameSite;

	private Boolean isSecure;

	private String domain;
}
