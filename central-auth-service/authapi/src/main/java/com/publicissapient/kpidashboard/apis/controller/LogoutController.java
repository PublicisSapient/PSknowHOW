package com.publicissapient.kpidashboard.apis.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.publicissapient.kpidashboard.apis.config.AuthConfig;
import com.publicissapient.kpidashboard.apis.config.CookieConfig;
import com.publicissapient.kpidashboard.apis.util.CookieUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
public class LogoutController {

	private final AuthConfig authConfig;

	private final CookieConfig cookieConfig;

	// logs out the user from the central auth application
	@GetMapping("/sso-logout")
	public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
		CookieUtil.deleteCookie(request, response, cookieConfig.getDomain(), CookieUtil.COOKIE_NAME,
				CookieUtil.API_COOKIE_PATH);
		CookieUtil.deleteCookie(request, response, cookieConfig.getDomain(), CookieUtil.EXPIRY_COOKIE_NAME,
				CookieUtil.DEFAULT_COOKIE_PATH);

		return ResponseEntity.ok().build();
	}

	// logs out the user from the Microsoft AD
	@GetMapping("/saml-logout")
	public ModelAndView logoutView() {
		return new ModelAndView(authConfig.getLogoutView());
	}
}
