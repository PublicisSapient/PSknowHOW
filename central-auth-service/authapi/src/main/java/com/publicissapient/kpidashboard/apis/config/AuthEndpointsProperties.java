package com.publicissapient.kpidashboard.apis.config;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "auth.auth-endpoints")
public class AuthEndpointsProperties {
	private static final String[] NO_ENDPOINTS = {};
	private static final String[] NO_EXTERNAL_ENDPOINTS = {};
	private static final String[] DEFAULT_PUBLIC_ENDPOINTS = {"/api-docs", "/img/**", "/css/**", "/js/**", "/**.js**",
			"/**.css**", "/**.png**", "/**.jpeg**", "/**.jpg**", "/**.ico**"};

	private String[] publicEndpoints;

	private String[] authenticatedEndpoints;

	private String[] externalEndpoints;

	public String[] getPublicEndpoints() {
		return Stream.concat(Arrays.stream(Optional.ofNullable(publicEndpoints).orElse(NO_ENDPOINTS)),
				Arrays.stream(DEFAULT_PUBLIC_ENDPOINTS)).toArray(String[]::new);
	}

	public String[] getAuthenticatedEndpoints() {
		return Optional.ofNullable(authenticatedEndpoints).orElse(NO_ENDPOINTS).clone();
	}

	public String[] getExternalAuthentication() {
		return Optional.ofNullable(externalEndpoints).orElse(NO_EXTERNAL_ENDPOINTS).clone();
	}
}
