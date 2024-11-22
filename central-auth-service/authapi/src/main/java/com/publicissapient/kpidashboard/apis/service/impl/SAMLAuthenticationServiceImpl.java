package com.publicissapient.kpidashboard.apis.service.impl;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.entity.User;
import com.publicissapient.kpidashboard.apis.enums.AuthType;
import com.publicissapient.kpidashboard.apis.service.SAMLAuthenticationService;
import com.publicissapient.kpidashboard.apis.service.TokenAuthenticationService;
import com.publicissapient.kpidashboard.apis.service.UserRoleService;
import com.publicissapient.kpidashboard.apis.service.UserService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SAMLAuthenticationServiceImpl implements SAMLAuthenticationService {
	private final String FIRST_NAME_ATTRIBUTE_KEY = "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname";
	private final String LAST_NAME_ATTRIBUTE_KEY = "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname";
	private final String DISPLAY_NAME_ATTRIBUTE_KEY = "http://schemas.microsoft.com/identity/claims/displayname";
	private final String EMAIL_ATTRIBUTE_KEY = "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress";
	private final String SAML_EMAIL_ATTRIBUTE_KEY = "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name";

	private final UserService userService;

	private final UserRoleService userRoleService;

	private final TokenAuthenticationService tokenAuthenticationService;

	@Override
	public void saveSamlData(Saml2AuthenticatedPrincipal principal, HttpServletResponse response) {
		String userEmail = principal.getName();

		String username = extractUsernameFromEmail(userEmail);

		if (Objects.nonNull(username)) {
			String jwt = this.tokenAuthenticationService.createJWT(username, AuthType.SAML,
					this.tokenAuthenticationService.createAuthorities(this.userRoleService.getRolesNamesByUsername(username)));

			this.tokenAuthenticationService.addSamlCookies(username, jwt, response);

			saveSamlUserData(principal);
		}
	}

	private String extractUsernameFromEmail(String email) {
		if (Objects.nonNull(email) && email.contains("@")) {
			return email.substring(0, email.indexOf("@"));
		}

		return email;
	}

	private void saveSamlUserData(Saml2AuthenticatedPrincipal principal) {
		User userData = createUserFromSamlPrincipal(principal);

		Optional<User> existingUserOptional = this.userService.findByUsername(userData.getUsername());

		if (existingUserOptional.isEmpty()) {
			this.userService.save(userData);
		}
	}

	private User createUserFromSamlPrincipal(Saml2AuthenticatedPrincipal principal) {
		try {
			User user = new User();

			user.setFirstName(extractAttribute(principal, FIRST_NAME_ATTRIBUTE_KEY));
			user.setLastName(extractAttribute(principal, LAST_NAME_ATTRIBUTE_KEY));
			user.setDisplayName(extractAttribute(principal, DISPLAY_NAME_ATTRIBUTE_KEY));
			user.setEmail(extractAttribute(principal, EMAIL_ATTRIBUTE_KEY));
			user.setSamlEmail(extractAttribute(principal, SAML_EMAIL_ATTRIBUTE_KEY));

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
			// TODO: Create special exception for SAML user creation failure
			throw new RuntimeException(e.getMessage());
		}
	}

	private String extractAttribute(Saml2AuthenticatedPrincipal principal, String attributeKey) {
		var attributeList = principal.getAttribute(attributeKey);

		if (Objects.nonNull(attributeList) && !attributeList.isEmpty()) {
			return attributeList.get(0).toString();
		}

		return null;
	}
}
