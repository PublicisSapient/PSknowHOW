package com.publicissapient.kpidashboard.apis.config;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "auth.forgot-password")
public class ForgotPasswordConfig {

	private String expiryInterval;

	// forgot password server host only for server where nginex is not
	// setup
	private String serverPort;

	// forgot password server host only for server where nginex is not
	// setup
	private String uiHost;

	// forgot password UI port only for server where nginex is not setup
	private String uiPort;
}
