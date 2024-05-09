package com.publicissapient.kpidashboard.common.exceptions;

public enum ClientErrorMessageEnum {


	UNAUTHORIZED(401, "Sorry, you are not authorized to access the requested resource. Please check your credentials/token and try again."),
	FORBIDDEN(403, "Oops! You don't have permission to access the requested resource/tool."),
	NOT_FOUND(404, "We couldn't find the requested resource. Please check the base URL, resource version and try again."),
	METHOD_NOT_ALLOWED(405, "Oops! The action you're trying to perform is not allowed for this resource. Please check your request and try again."),
	REQUEST_TIMEOUT(408, "Oops! The server timed out while waiting for the request. Please try again later."),
	TOO_MANY_REQUESTS (429, "Oops! Too many requests have been sent in a short amount of time. Please try again later."),
	OTHER_CLIENT_ERRORS(-1,"Please check logs and contact the KnowHow support team for assistance or clarification");


	private final int value;
	private final String reasonPhrase;

	private ClientErrorMessageEnum(int value, String reasonPhrase) {
		this.value = value;
		this.reasonPhrase = reasonPhrase;
	}

	public int value() {
		return this.value;
	}

	public String getReasonPhrase() {
		return this.reasonPhrase;
	}

	public static ClientErrorMessageEnum fromValue(int value) {
		for (ClientErrorMessageEnum error : ClientErrorMessageEnum.values()) {
			if (error.value == value) {
				return error;
			}
		}
		return OTHER_CLIENT_ERRORS;
	}
}
