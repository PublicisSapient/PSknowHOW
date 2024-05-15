package com.publicissapient.kpidashboard.apis.controller;

import lombok.AllArgsConstructor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.publicissapient.kpidashboard.apis.config.AuthConfig;
import com.publicissapient.kpidashboard.apis.util.CookieUtil;

@RestController
@AllArgsConstructor
public class LogoutController {

	private final AuthConfig authConfig;

	// logs out the user from the central auth application
	@GetMapping("/sso-logout")
	public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
		CookieUtil.deleteCookie(request, response, CookieUtil.COOKIE_NAME);
		CookieUtil.deleteCookie(request, response, CookieUtil.EXPIRY_COOKIE_NAME);

		return ResponseEntity.ok().build();
	}


	// logs out the user from the Microsoft AD
	@GetMapping("/saml-logout")
	public ModelAndView logoutView() {
		return new ModelAndView(authConfig.getLogoutView());
	}
}
