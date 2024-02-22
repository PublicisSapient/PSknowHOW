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

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.publicissapient.kpidashboard.apis.auth.service.AuthTypesConfigService;
import com.publicissapient.kpidashboard.apis.auth.token.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

/**
 * Extension of {WebSecurityConfigurerAdapter} to provide configuration for web
 * security.
 *
 * @author anisingh4
 *
 * @author pawkandp Removed the depricate WebSecurityConfigurerAdapter with new
 *         spring version 6+
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	private final AuthenticationProvider authenticationProvider;

	private final AuthenticationConfiguration authenticationConfiguration;
	private CustomApiConfig customApiConfig;

	private final AuthTypesConfigService authTypesConfigService;

	public static Properties getProps() throws IOException {
		Properties prop = new Properties();
		try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("crowd.properties")) {
			prop.load(in);
		}
		return prop;
	}

	@Autowired
	public void setCustomApiConfig(CustomApiConfig customApiConfig) {
		this.customApiConfig = customApiConfig;
	}

	/**
	 * Added below fixes for security scan: - commented the headers in the response
	 * - added CorsFilter in filter chain for endpoints mentioned in the method
	 *
	 * @param http
	 *            - reference to HttpSecurity
	 */
	@Bean
	protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		// Configure AuthenticationManagerBuilder
		AuthenticationManagerBuilder authenticationManagerBuilder = http
				.getSharedObject(AuthenticationManagerBuilder.class);
		setAuthenticationProvider(authenticationManagerBuilder);
		http.headers(headers -> headers.cacheControl(HeadersConfigurer.CacheControlConfig::disable));
		http.csrf(AbstractHttpConfigurer::disable);
		http.logout(AbstractHttpConfigurer::disable);
		http.formLogin(AbstractHttpConfigurer::disable);
		http.authorizeHttpRequests(authz -> authz.requestMatchers("/appinfo").permitAll()
				.requestMatchers("/registerUser").permitAll().requestMatchers("/changePassword").permitAll()
				.requestMatchers("/login/captcha").permitAll().requestMatchers("/login/captchavalidate").permitAll()
				.requestMatchers("/login**").permitAll().requestMatchers("/error").permitAll()
				.requestMatchers("/authenticationProviders").permitAll().requestMatchers("/auth-types-status")
				.permitAll().requestMatchers("/pushData/*").permitAll().requestMatchers("/getversionmetadata")
				.permitAll().requestMatchers("/signIn").permitAll()
				// management metrics
				.requestMatchers("/info").permitAll().requestMatchers("/health").permitAll().requestMatchers("/env")
				.permitAll().requestMatchers("/metrics").permitAll().requestMatchers("/actuator/togglz**").permitAll()
				.requestMatchers("/togglz-console**").permitAll().requestMatchers("/actuator**").permitAll()
				.requestMatchers("/forgotPassword").permitAll().requestMatchers("/forgotPassword").permitAll()
				.requestMatchers("/validateToken**").permitAll().requestMatchers("/resetPassword").permitAll()
				.requestMatchers("/cache/clearAllCache").permitAll()
				.requestMatchers(HttpMethod.GET, "/cache/clearCache/**").permitAll()
				.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/analytics/switch").permitAll().anyRequest().authenticated())
				.sessionManagement(manager -> manager.sessionCreationPolicy(STATELESS))
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.addFilterAfter(corsFilter(), ChannelProcessingFilter.class)
				.exceptionHandling(Customizer.withDefaults());
		return http.build();
	}

	@Bean
	protected CorsFilter corsFilter() {
		return new CorsFilter();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
			throws Exception {
		return authenticationConfiguration.getAuthenticationManager();

	}

	protected void setAuthenticationProvider(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(authenticationProvider);
	}

	@Bean
	CorsConfigurationSource apiConfigurationSource() {
		final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		final CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		config.setAllowedOrigins(customApiConfig.getCorsFilterValidOrigin());
		config.addAllowedHeader("*");
		config.addAllowedMethod("OPTIONS");
		config.addAllowedMethod("HEAD");
		config.addAllowedMethod("GET");
		config.addAllowedMethod("PUT");
		config.addAllowedMethod("POST");
		config.addAllowedMethod("DELETE");
		config.addAllowedMethod("PATCH");
		source.registerCorsConfiguration("/**", config);
		return source;
	}

	@Bean
	public WebSecurityCustomizer a() {
		return web -> web.ignoring().requestMatchers("/v2/api-docs", "/configuration/ui", "/swagger-resources/**",
				"/configuration/security", "/swagger-ui/**", "/webjars/**");
	}

}
