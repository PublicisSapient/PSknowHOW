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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.apis.filters.CustomAuthenticationFailureHandler;
import com.publicissapient.kpidashboard.apis.filters.standard.AuthenticationResultHandler;
import com.publicissapient.kpidashboard.apis.filters.standard.StandardLoginRequestFilter;
import com.publicissapient.kpidashboard.apis.service.TokenAuthenticationService;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.saml2.Saml2RelyingPartyProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrations;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.publicissapient.kpidashboard.apis.filters.JwtAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@AllArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {
	private AuthenticationConfiguration authConfig;

	private final AuthEndpointsProperties authEndpointsProperties;

	private final AuthProperties authProperties;

	private final TokenAuthenticationService tokenAuthenticationService;

	private AuthenticationResultHandler standardAuthenticationResultHandler;

	private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

	@Bean
	protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable());
		http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

		http.authorizeHttpRequests(authz -> authz.requestMatchers(HttpMethod.OPTIONS)
												 .permitAll()
												 .anyRequest()
												 .authenticated())
			.saml2Login((saml2) -> saml2.loginProcessingUrl("/saml/SSO"))
			.saml2Logout((saml2) -> saml2.logoutRequest((request) -> request.logoutUrl("/saml/logout")))
			.saml2Logout((saml2) -> saml2.logoutResponse((response) -> response.logoutUrl("/saml/SingleLogout")))
			.saml2Metadata((saml2) -> saml2.metadataUrl("/saml/metadata"))
			.addFilterBefore(
					standardLoginRequestFilter(),
					UsernamePasswordAuthenticationFilter.class
			)
			.addFilterBefore(
					new JwtAuthenticationFilter(
							tokenAuthenticationService,
							authEndpointsProperties
					),
					UsernamePasswordAuthenticationFilter.class
			);
		return http.build();
	}

	@Bean
	protected StandardLoginRequestFilter standardLoginRequestFilter() throws Exception {
		return new StandardLoginRequestFilter("/login",
											  authenticationManager(authConfig),
											  standardAuthenticationResultHandler,
											  customAuthenticationFailureHandler,
											  authProperties
		);
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}

	@Bean
	InMemoryRelyingPartyRegistrationRepository repository(
			Saml2RelyingPartyProperties properties,
			@Value("classpath:credentials/rp-private.key")
			RSAPrivateKey key,
			@Value("classpath:credentials/rp-certificate.crt")
			File cert
	) {
		Saml2X509Credential signing = Saml2X509Credential.signing(
				key,
				x509Certificate(cert)
		);
		Saml2RelyingPartyProperties.Registration registration = properties.getRegistration()
																		  .values()
																		  .iterator()
																		  .next();
		return new InMemoryRelyingPartyRegistrationRepository(
				RelyingPartyRegistrations.collectionFromMetadataLocation(
						registration.getAssertingparty()
									.getMetadataUri())
                                   .stream()
                                   .map((builder) -> builder.registrationId(UUID.randomUUID()
                                                                                .toString())
                                                            .entityId(registration.getEntityId())
                                                            .assertionConsumerServiceLocation(registration.getAcs()
                                                                                                          .getLocation())
                                                            .singleLogoutServiceLocation(registration.getSinglelogout()
                                                                                                     .getUrl())
                                                            .singleLogoutServiceResponseLocation(registration.getSinglelogout()
                                                                                                             .getResponseUrl())
                                                            .signingX509Credentials((credentials) -> credentials.add(signing))
                                                            .build())
                                   .collect(Collectors.toList()));
	}

	X509Certificate x509Certificate(File location) {
		try (InputStream source = new FileInputStream(location)) {
			return (X509Certificate) CertificateFactory.getInstance("X.509")
													   .generateCertificate(source);
		} catch (CertificateException | IOException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		// TODO: get the allowed origins from the .yml file
		configuration.setAllowedOriginPatterns(Arrays.asList("*"));
		configuration.setAllowedMethods(Arrays.asList("*"));
		configuration.setAllowCredentials(true);
		configuration.setAllowedHeaders(Arrays.asList("*"));
		configuration.setMaxAge(1800L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration(
				"/**",
				configuration
		);

		return source;
	}
}
