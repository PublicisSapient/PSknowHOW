package com.publicissapient.kpidashboard.apis.controller;

import com.publicissapient.kpidashboard.apis.config.AuthConfig;
import com.publicissapient.kpidashboard.apis.service.TokenAuthenticationService;
import com.publicissapient.kpidashboard.apis.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Objects;

@Slf4j
@RestController
@AllArgsConstructor
public class SSOAuthenticationController {

	private final AuthConfig authProperties;

	private final TokenAuthenticationService tokenAuthenticationService;

	@GetMapping("/sso-login")
	public RedirectView login(
			@AuthenticationPrincipal
			Saml2AuthenticatedPrincipal principal,
			@RequestParam(required = false)
			String redirectUri,
			HttpServletResponse response
	) {
		tokenAuthenticationService.saveSamlData(principal, response);

		RedirectView redirectView = new RedirectView();
		if (Objects.nonNull(redirectUri)) {
			redirectView.setUrl(redirectUri);
		} else {
			redirectView.setUrl(authProperties.getBaseUiUrl());
		}

		return redirectView;
	}

	@GetMapping("/sso-logout")
	public ResponseEntity<Void> logout(
			HttpServletRequest request,
			HttpServletResponse response
	) {
		CookieUtil.deleteCookie(request, response, CookieUtil.COOKIE_NAME);
		CookieUtil.deleteCookie(request, response, CookieUtil.EXPIRY_COOKIE_NAME);

		return ResponseEntity.ok().build();
	}

	@GetMapping("/sso-logout-view")
	public ModelAndView logoutView() {
		return new ModelAndView(authProperties.getLogoutView());
	}
}
