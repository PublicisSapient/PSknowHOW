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

import static org.springframework.security.saml.provider.service.config.SamlServiceProviderSecurityDsl.serviceProvider;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.saml.provider.service.config.SamlServiceProviderSecurityConfiguration;
import org.springframework.security.saml.provider.service.config.SamlServiceProviderServerBeanConfiguration;

@Configuration
@EnableWebSecurity
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SamlWebSecurityConfiguration extends SamlServiceProviderSecurityConfiguration {

	private final SamlSecurityConfiguration.SamlConfiguration samlConfiguration;

	public SamlWebSecurityConfiguration(SamlServiceProviderServerBeanConfiguration configuration,
			SamlSecurityConfiguration.SamlConfiguration samlConfiguration) {
		super(configuration);
		this.samlConfiguration = samlConfiguration;
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		super.configure(http);
		http.apply(serviceProvider()).configure(samlConfiguration).useStandardFilters(false);
	}

}
