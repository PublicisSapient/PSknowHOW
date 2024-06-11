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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.publicissapient.kpidashboard.apis.enums.AuthType;
import com.publicissapient.kpidashboard.apis.errors.CustomAuthenticationEntryPoint;
import com.publicissapient.kpidashboard.apis.filters.CustomAuthenticationFailureHandler;
import com.publicissapient.kpidashboard.apis.filters.JwtAuthenticationFilter;
import com.publicissapient.kpidashboard.apis.filters.saml.SuccessHandlerService;
import com.publicissapient.kpidashboard.apis.filters.standard.AuthenticationResultHandler;
import com.publicissapient.kpidashboard.apis.filters.standard.StandardLoginRequestFilter;

/**
 * Extension of {@link WebSecurityConfigurerAdapter} to provide configuration
 * for web security.
 * 
 * @author Hiren Babariya
 */
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Order(2)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	@Autowired
	private JwtAuthenticationFilter jwtAuthenticationFilter;

	@Autowired
	private SuccessHandlerService authenticationResultHandler;

	@Autowired
	private AuthenticationResultHandler standardAuthenticationResultHandler;

	@Autowired
	private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

	@Autowired
	private AuthenticationProvider standardAuthenticationProvider;

	@Autowired
	private AuthProperties authProperties;

	public static Properties getProps() throws IOException {
		Properties prop = new Properties();
		try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("crowd.properties")) {
			prop.load(in);
		}
		return prop;
	}

	/**
	 * Added below fixes for security scan: - commented the headers in the response
	 * - added CorsFilter in filter chain for endpoints mentioned in the method
	 *
	 * @param http
	 *            - reference to HttpSecurity
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.headers().cacheControl();
		http.headers().httpStrictTransportSecurity().maxAgeInSeconds(authProperties.getMaxAgeInSeconds()).includeSubDomains(authProperties.isIncludeSubDomains());
		http.csrf().disable().authorizeRequests()
				// authentication API calls - Public
				.antMatchers("/registerUser").permitAll().antMatchers("/login**").permitAll().antMatchers("/error")
				.permitAll().antMatchers("/authenticationProviders").permitAll().antMatchers("/auth-types-status")
				.permitAll().antMatchers("/saml").permitAll().antMatchers("/saml/login/**").permitAll()
				.antMatchers("/login/**").permitAll().antMatchers("/auth/success").permitAll()
				.antMatchers("/login-success").permitAll()
				// management metrics - Public
				.antMatchers("/appinfo").permitAll().antMatchers("/info").permitAll().antMatchers("/health").permitAll()
				.antMatchers("/env").permitAll().antMatchers("/metrics").permitAll().antMatchers("/actuator/togglz")
				.permitAll().antMatchers("/actuator**").permitAll().antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				// forgot Password API calls - Public
				.antMatchers("/forgotPassword").permitAll().antMatchers("/resetPassword").permitAll()
				.antMatchers("/validateEmailToken/**").permitAll()
				// verify user - ResetPasswordToken - Public
				.antMatchers("/verifyUser").permitAll().anyRequest().authenticated().and().httpBasic().and().csrf()
				.disable().headers().and()
				.addFilterBefore(standardLoginRequestFilter(), UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.addFilterAfter(corsFilterKnowHOW(), ChannelProcessingFilter.class).exceptionHandling()
				.authenticationEntryPoint(customAuthenticationEntryPoint()).and().logout().disable().formLogin()
				.disable();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		List<AuthType> authenticationProviders = authProperties.getAuthenticationProviders();

		if (authenticationProviders.contains(AuthType.STANDARD)) {
			auth.authenticationProvider(standardAuthenticationProvider);
		}
	}

	@Bean
	protected StandardLoginRequestFilter standardLoginRequestFilter() throws Exception {
		return new StandardLoginRequestFilter("/login", authenticationManager(), standardAuthenticationResultHandler,
				customAuthenticationFailureHandler, authProperties);
	}

	@Bean
	protected CorsFilter corsFilterKnowHOW() throws Exception {
		return new CorsFilter();
	}

	@Bean
	public CustomAuthenticationEntryPoint customAuthenticationEntryPoint() {
		return new CustomAuthenticationEntryPoint();
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/v2/api-docs", "/configuration/ui", "/swagger-resources/**",
				"/configuration/security", "/swagger-ui/**", "/webjars/**");
	}

	@Bean(name = "validationMessageSource")
	@Primary
	public ReloadableResourceBundleMessageSource validationMessageSource() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasename("classpath:locale/messages");
		return messageSource;
	}
}
