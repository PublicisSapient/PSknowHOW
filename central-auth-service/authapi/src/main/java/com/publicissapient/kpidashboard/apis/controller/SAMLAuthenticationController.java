package com.publicissapient.kpidashboard.apis.controller;

import java.util.Objects;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.publicissapient.kpidashboard.apis.config.AuthConfig;
import com.publicissapient.kpidashboard.apis.service.SAMLAuthenticationService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
public class SAMLAuthenticationController {

	private final AuthConfig authProperties;

	private final SAMLAuthenticationService samlAuthenticationService;

	@GetMapping("/saml-login")
	public RedirectView login(@AuthenticationPrincipal Saml2AuthenticatedPrincipal principal,
			@RequestParam(required = false) String redirectUri, HttpServletResponse response) {
		samlAuthenticationService.saveSamlData(principal, response);

		RedirectView redirectView = new RedirectView();

		if (Objects.nonNull(redirectUri)) {
			redirectView.setUrl(redirectUri);
		} else {
			redirectView.setUrl(authProperties.getBaseUiUrl());
		}

		return redirectView;
	}
}
