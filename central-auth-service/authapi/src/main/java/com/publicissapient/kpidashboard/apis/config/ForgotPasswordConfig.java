package com.publicissapient.kpidashboard.apis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@EnableConfigurationProperties
public class ForgotPasswordConfig {

	@Value("${auth.forgotPassword.expiryInterval}")
	private Integer expiryInterval;

	// forgot password server host only for server where nginex is not
	// setup
	@Value("${auth.forgotPassword.serverPort}")
	private String serverPort;

	// forgot password server host only for server where nginex is not
	// setup
	@Value("${auth.forgotPassword.uiHost}")
	private String uiHost;

	// forgot password UI port only for server where nginex is not setup
	@Value("${auth.forgotPassword.uiPort}")
	private String uiPort;
}
