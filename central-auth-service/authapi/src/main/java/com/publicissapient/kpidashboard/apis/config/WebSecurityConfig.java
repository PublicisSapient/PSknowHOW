/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.apis.config;

import java.io.ByteArrayInputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.saml2.Saml2RelyingPartyProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrations;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.publicissapient.kpidashboard.apis.filters.CorsFilter;
import com.publicissapient.kpidashboard.apis.filters.JwtAuthenticationFilter;
import com.publicissapient.kpidashboard.apis.filters.standard.StandardLoginRequestFilter;
import com.publicissapient.kpidashboard.apis.filters.standard.handlers.CustomAuthenticationFailureHandler;
import com.publicissapient.kpidashboard.apis.filters.standard.handlers.CustomAuthenticationSuccessHandler;

import lombok.AllArgsConstructor;

@Configuration
@AllArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {
	private final AuthConfig authConfig;

	private final AuthenticationConfiguration authenticationConfiguration;

	private final AuthEndpointsProperties authEndpointsProperties;

	private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

	private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

	@Bean
	protected SecurityFilterChain filterChain(HttpSecurity http, HttpSecurity httpSecurity) throws Exception {
		http.csrf(csrf -> csrf.disable());
		http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
		httpSecurity.headers(httpSecurityHeadersConfigurer -> httpSecurityHeadersConfigurer
				.httpStrictTransportSecurity(hstsCustomizer -> hstsCustomizer.maxAgeInSeconds(authConfig.getMaxAgeSeconds())
						.includeSubDomains(authConfig.getIncludeSubdomains())));
		http.headers(httpSecurityHeadersConfigurer -> {
			httpSecurityHeadersConfigurer.contentSecurityPolicy(contentSecurityPolicyConfig -> contentSecurityPolicyConfig
					.policyDirectives(authConfig.getContentSecurityPolicy()));
		});
		http.authorizeHttpRequests(authz -> authz.requestMatchers(HttpMethod.OPTIONS).permitAll().requestMatchers("/login")
				.permitAll().requestMatchers("/register-user").permitAll().requestMatchers("/forgot-password").permitAll()
				.requestMatchers("/reset-password").permitAll().requestMatchers("/change-password").permitAll()
				.requestMatchers("/validateEmailToken").permitAll().requestMatchers("/verifyUser").permitAll()
				.requestMatchers("/user-info").permitAll().requestMatchers("/users/**").permitAll()
				.requestMatchers("/sso-logout").permitAll().requestMatchers("/user-approvals/pending").permitAll()
				.requestMatchers("/approve").permitAll().requestMatchers("/reject").permitAll().anyRequest().authenticated())
				.saml2Login((saml2) -> saml2.loginProcessingUrl("/saml/SSO"))
				.saml2Logout((saml2) -> saml2.logoutRequest((request) -> request.logoutUrl("/saml/logout")))
				.saml2Logout((saml2) -> saml2.logoutResponse((response) -> response.logoutUrl("/saml/SingleLogout")))
				.saml2Metadata((saml2) -> saml2.metadataUrl("/saml/metadata"))
				.addFilterBefore(
						new StandardLoginRequestFilter("/login", authenticationManager(authenticationConfiguration),
								customAuthenticationSuccessHandler, customAuthenticationFailureHandler),
						UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(new JwtAuthenticationFilter(authEndpointsProperties, authConfig),
						UsernamePasswordAuthenticationFilter.class)
				.addFilterAfter(new CorsFilter(authConfig), ChannelProcessingFilter.class);
		return http.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
			throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	@Bean
	InMemoryRelyingPartyRegistrationRepository repository(Saml2RelyingPartyProperties properties,
			@Value("${auth.rpPrivateKey}") String privateKeyStr, @Value("${auth.rpCertificate}") String certStr) {
		Saml2X509Credential signing = Saml2X509Credential.signing(rsaPrivateKey(privateKeyStr), x509Certificate(certStr));

		Saml2RelyingPartyProperties.Registration registration = properties.getRegistration().values().iterator().next();

		return new InMemoryRelyingPartyRegistrationRepository(RelyingPartyRegistrations
				.collectionFromMetadataLocation(registration.getAssertingparty().getMetadataUri()).stream()
				.map((builder) -> builder.registrationId(UUID.randomUUID().toString()).entityId(registration.getEntityId())
						.assertionConsumerServiceLocation(registration.getAcs().getLocation())
						.singleLogoutServiceLocation(registration.getSinglelogout().getUrl())
						.singleLogoutServiceResponseLocation(registration.getSinglelogout().getResponseUrl())
						.signingX509Credentials((credentials) -> credentials.add(signing)).build())
				.collect(Collectors.toList()));
	}

	RSAPrivateKey rsaPrivateKey(String privateKeyStr) {
		byte[] pkcs8EncodedBytes = Base64.getDecoder().decode(privateKeyStr);
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8EncodedBytes);

		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new IllegalArgumentException(e);
		}
	}

	X509Certificate x509Certificate(String certSrt) {
		byte[] certificateBytes = Base64.getDecoder().decode(certSrt);
		try {
			return (X509Certificate) CertificateFactory.getInstance("X.509")
					.generateCertificate(new ByteArrayInputStream(certificateBytes));
		} catch (CertificateException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

		configuration.setAllowedOriginPatterns(Arrays.asList("*"));
		configuration.setAllowedMethods(Arrays.asList("*"));
		configuration.setAllowCredentials(true);
		configuration.setAllowedHeaders(Arrays.asList("*"));
		configuration.setMaxAge(1800L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);

		return source;
	}
}
