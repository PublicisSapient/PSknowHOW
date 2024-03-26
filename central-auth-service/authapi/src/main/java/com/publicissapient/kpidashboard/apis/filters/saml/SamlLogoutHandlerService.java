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
package com.publicissapient.kpidashboard.apis.filters.saml;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.saml.provider.provisioning.SamlProviderProvisioning;
import org.springframework.security.saml.provider.service.ServiceProviderService;
import org.springframework.security.saml.provider.service.authentication.ServiceProviderLogoutHandler;
import org.springframework.security.saml.saml2.authentication.Assertion;
import org.springframework.security.saml.saml2.authentication.NameIdPrincipal;
import org.springframework.security.saml.saml2.authentication.Subject;
import org.springframework.security.saml.saml2.metadata.NameId;
import org.springframework.security.saml.spi.DefaultSamlAuthentication;

import com.publicissapient.kpidashboard.apis.config.AuthProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SamlLogoutHandlerService extends ServiceProviderLogoutHandler {

	private final AuthProperties authConfigurationProperties;

	public SamlLogoutHandlerService(SamlProviderProvisioning<ServiceProviderService> provisioning,
			AuthProperties authConfigurationProperties) {
		super(provisioning);
		this.authConfigurationProperties = authConfigurationProperties;
	}

	@Override
	protected void spInitiatedLogout(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException {
		try {
			DefaultSamlAuthentication samlAuthentication = new DefaultSamlAuthentication(Boolean.TRUE,
					new Assertion().setSubject(new Subject().setPrincipal(new NameIdPrincipal()
							.setValue(request.getParameter(authConfigurationProperties.getLogoutEmailQueryParam()))
							.setFormat(new NameId(URI.create(authConfigurationProperties.getNameId()))))),
					authConfigurationProperties.getAssertingEntityId(),
					authConfigurationProperties.getHoldingEntityId(),
					URLEncoder.encode(authConfigurationProperties.getBaseUrl() + "/api/logoutsuccess",
							StandardCharsets.UTF_8.displayName()));
			super.spInitiatedLogout(request, response, samlAuthentication);
		} catch (Exception exception) {
			log.error("failed", exception);
			response.sendRedirect(authConfigurationProperties.getDefaultRedirectToAfterLogout());
		}
	}
}
