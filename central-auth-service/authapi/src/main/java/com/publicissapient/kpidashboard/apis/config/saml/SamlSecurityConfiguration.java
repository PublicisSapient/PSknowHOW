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
package com.publicissapient.kpidashboard.apis.config.saml;

import javax.servlet.Filter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.saml.SamlValidator;
import org.springframework.security.saml.provider.SamlProviderLogoutFilter;
import org.springframework.security.saml.provider.SamlServerConfiguration;
import org.springframework.security.saml.provider.service.authentication.SamlAuthenticationResponseFilter;
import org.springframework.security.saml.provider.service.config.SamlServiceProviderServerBeanConfiguration;
import org.springframework.security.saml.spi.DefaultValidator;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

import com.publicissapient.kpidashboard.apis.config.AuthProperties;
import com.publicissapient.kpidashboard.apis.filters.saml.FailureHandlerService;
import com.publicissapient.kpidashboard.apis.filters.saml.SamlLogoutHandlerService;
import com.publicissapient.kpidashboard.apis.filters.saml.SamlUserService;
import com.publicissapient.kpidashboard.apis.filters.saml.SuccessHandlerService;
import com.publicissapient.kpidashboard.apis.service.UserService;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Configuration
@AllArgsConstructor
public class SamlSecurityConfiguration extends SamlServiceProviderServerBeanConfiguration {

	private final SamlConfiguration samlConfiguration;
	private final SuccessHandlerService successHandlerService;
	private final FailureHandlerService failureHandlerService;
	private final UserService userTransformerService;
	private final AuthProperties authConfigurationProperties;

	@Override
	protected SamlServerConfiguration getDefaultHostSamlServerConfiguration() {
		return samlConfiguration;
	}

	@Override
	public Filter spAuthenticationResponseFilter() {
		SamlAuthenticationResponseFilter authenticationFilter = new SamlAuthenticationResponseFilter(
				getSamlProvisioning());
		authenticationFilter.setAuthenticationManager(new SamlUserService(userTransformerService));
		authenticationFilter.setAuthenticationSuccessHandler(successHandlerService);
		authenticationFilter.setAuthenticationFailureHandler(failureHandlerService);
		return authenticationFilter;
	}

	@Override
	public Filter spSamlLogoutFilter() {
		return new SamlProviderLogoutFilter<>(getSamlProvisioning(),
				new SamlLogoutHandlerService(getSamlProvisioning(), authConfigurationProperties),
				new SimpleUrlLogoutSuccessHandler(), new SecurityContextLogoutHandler());
	}

	@Override
	public SamlValidator samlValidator() {
		DefaultValidator validator = new DefaultValidator(samlImplementation());
		validator.setMaxAuthenticationAgeMillis(authConfigurationProperties.getSamlMaxAuthenticationAgeMillis());
		return validator;
	}

	@Data
	@Configuration
	@EqualsAndHashCode(callSuper = true)
	@ConfigurationProperties(prefix = "spring.security.saml2")
	public static class SamlConfiguration extends SamlServerConfiguration {
	}
}
