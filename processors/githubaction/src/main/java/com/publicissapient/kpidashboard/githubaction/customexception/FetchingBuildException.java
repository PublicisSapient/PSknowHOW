package com.publicissapient.kpidashboard.githubaction.customexception;

public class FetchingBuildException extends Exception {

	private static final long serialVersionUID = -7003770711199668845L;

	/**
	 *
	 * The constructor.
	 * 
	 * @param message
	 *            message
	 */
	public FetchingBuildException(String message) {
		super(message);
	}

	/**
	 *
	 * The constructor.
	 * 
	 * @param message
	 *            message
	 * @param cause
	 *            cause
	 */
	public FetchingBuildException(String message, Throwable cause) {
		super(message, cause);
	}
}
