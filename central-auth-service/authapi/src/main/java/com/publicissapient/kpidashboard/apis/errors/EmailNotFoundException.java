package com.publicissapient.kpidashboard.apis.errors;

import com.publicissapient.kpidashboard.apis.enums.AuthType;

public class EmailNotFoundException extends RuntimeException {

	private static final long serialVersionUID = -8596676033217258687L;

	private static final String MESSAGE = "No user found with email: %1$2s, and authorization type %2$2s.";

	public EmailNotFoundException(String email, AuthType authType) {
		super(String.format(MESSAGE, email, authType.name()));
	}
}
