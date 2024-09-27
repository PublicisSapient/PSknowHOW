package com.publicissapient.kpidashboard.apis.config;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "auth.ui")
public class UserInterfacePathsConfig {

	private String registerPath;

	private String validateUser;

	private String uiResetPath;
}
