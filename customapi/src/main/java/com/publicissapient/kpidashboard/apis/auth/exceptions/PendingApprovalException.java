package com.publicissapient.kpidashboard.apis.auth.exceptions;

import org.springframework.security.core.AuthenticationException;

public class PendingApprovalException extends AuthenticationException {

	private static final long serialVersionUID = -8596676033265458687L;

	public PendingApprovalException(String msg) {
		super(msg);
	}
}
