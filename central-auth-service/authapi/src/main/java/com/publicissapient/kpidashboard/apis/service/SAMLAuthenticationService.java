package com.publicissapient.kpidashboard.apis.service;

import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;

import jakarta.servlet.http.HttpServletResponse;

public interface SAMLAuthenticationService {

	void saveSamlData(Saml2AuthenticatedPrincipal principal, HttpServletResponse response);
}
