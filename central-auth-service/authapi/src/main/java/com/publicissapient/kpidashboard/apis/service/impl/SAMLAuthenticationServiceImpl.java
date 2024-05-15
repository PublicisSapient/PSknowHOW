package com.publicissapient.kpidashboard.apis.service.impl;

import com.publicissapient.kpidashboard.apis.entity.User;
import com.publicissapient.kpidashboard.apis.enums.AuthType;
import com.publicissapient.kpidashboard.apis.service.SAMLAuthenticationService;
import com.publicissapient.kpidashboard.apis.service.TokenAuthenticationService;
import com.publicissapient.kpidashboard.apis.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
public class SAMLAuthenticationServiceImpl implements SAMLAuthenticationService {

	private final UserService userService;

	private final TokenAuthenticationService tokenAuthenticationService;

	@Override
	public void saveSamlData(
			Saml2AuthenticatedPrincipal principal,
			HttpServletResponse response
	) {
		String userEmail = principal.getName();

		String username = extractUsernameFromEmail(userEmail);

		String jwt = this.tokenAuthenticationService.createJWT(username, AuthType.SAML, null);

		this.tokenAuthenticationService.addSamlCookies(
				username,
				jwt,
				response
		);

		saveSamlUserData(principal);
	}

	private String extractUsernameFromEmail(String email) {
		if (Objects.nonNull(email) && email.contains("@")) {
			return email.substring(
					0,
					email.indexOf("@")
			);
		}

		return email;
	}

	@Override
	public User saveSamlUserData(Saml2AuthenticatedPrincipal principal) {
		User userData = createUserFromSamlPrincipal(principal);

		Optional<User> user = this.userService.findByUserName(userData.getUsername());

		return user.orElseGet(() -> this.userService.save(userData));
	}

	private User createUserFromSamlPrincipal(Saml2AuthenticatedPrincipal principal) {
		try {
			User user = new User();

			// TODO: clean up the hardcoded strings
			user.setFirstName(
					principal.getAttribute("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname").get(0)
							 .toString());
			user.setLastName(
					principal.getAttribute("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname").get(0)
							 .toString());
			user.setDisplayName(
					principal.getAttribute("http://schemas.microsoft.com/identity/claims/displayname").get(0)
							 .toString());
			user.setEmail(
					principal.getAttribute("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress").get(0)
							 .toString());
			user.setSamlEmail(
					principal.getAttribute("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name").get(0)
							 .toString());

			if (user.getSamlEmail() != null && user.getSamlEmail().contains("@")) {
				// Extract the substring before '@' as the username
				String userName = user.getSamlEmail().substring(0, user.getSamlEmail().indexOf("@"));
				user.setUsername(userName);
			}

			user.setApproved(true);
			user.setUserVerified(true);
			user.setAuthType(AuthType.SAML.name());
			user.setCreatedDate(LocalDateTime.now());

			return user;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}
}
