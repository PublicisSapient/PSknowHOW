package com.publicissapient.kpidashboard.apis.controller;

import com.publicissapient.kpidashboard.apis.config.AuthProperties;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@RestController
@AllArgsConstructor
public class AuthenticationController {
	private final AuthProperties authProperties;
	private final TokenAuthenticationService tokenAuthenticationService;

	@GetMapping("/sso-login")
	public ModelAndView login(
			@AuthenticationPrincipal
			Saml2AuthenticatedPrincipal principal,
			HttpServletResponse response
	) {
		String email = tokenAuthenticationService.saveSamlData(principal, response);

		ModelAndView modelAndView = new ModelAndView(authProperties.getLoginView());
		modelAndView.addObject("email", email);
		modelAndView.addObject("redirectUrl", authProperties.getBaseUiUrl());

		return modelAndView;
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
