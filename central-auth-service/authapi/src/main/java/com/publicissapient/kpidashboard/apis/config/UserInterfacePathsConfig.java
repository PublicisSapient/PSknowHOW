package com.publicissapient.kpidashboard.apis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "auth.ui")
public class UserInterfacePathsConfig {

	private String registerPath;

	private String validateUser;

	private String resetPath;
}
