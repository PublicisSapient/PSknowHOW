package com.publicissapient.kpidashboard.apis.service;

import com.publicissapient.kpidashboard.apis.entity.User;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;

public interface SAMLAuthenticationService {
	void saveSamlData(Saml2AuthenticatedPrincipal principal, HttpServletResponse response);

	User saveSamlUserData(Saml2AuthenticatedPrincipal principal);
}
