package com.publicissapient.kpidashboard.apis.filters;

import java.io.IOException;

import lombok.AllArgsConstructor;

import jakarta.servlet.ServletException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

import com.publicissapient.kpidashboard.apis.config.AuthConfig;

@AllArgsConstructor
public class CorsFilter extends OncePerRequestFilter {

	private static final String STRICT_TRANSPORT_SECURITY = "Strict-Transport-Security";

	private final AuthConfig authConfig;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		response.setHeader(STRICT_TRANSPORT_SECURITY,
						   "max-age=" + authConfig.getMaxAgeSeconds() + "; includeSubDomains");

		filterChain.doFilter(request,response);
	}
}
