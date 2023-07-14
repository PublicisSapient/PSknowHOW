/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.common.exceptions;

/**
 * Class providing methods for exception.
 */
public class ApplicationException extends Exception {
	public static final int NOTHING_TO_UPDATE = 0;
	public static final int JSON_FORMAT_ERROR = -1;
	public static final int COLLECTOR_CREATE_ERROR = -10;
	public static final int COLLECTOR_ITEM_CREATE_ERROR = -11;
	public static final int ERROR_INSERTING_DATA = -12;
	public static final int DUPLICATE_DATA = -13;
	public static final int BAD_DATA = -14;
	public static final int INVALID_CONFIGURATION = -999;
	private static final long serialVersionUID = 4596406816345733781L;
	// sonar wants it final but compiler said not initialized
	private int errorCode; // NOSONAR

	/**
	 * Instantiates a new application exception.
	 *
	 * @param message
	 *            the message
	 * @param errorCode
	 *            the error code
	 */
	public ApplicationException(String message, int errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

	/**
	 * Instantiates a new application exception.
	 *
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 * @param errorCode
	 *            the error code
	 */
	public ApplicationException(String message, Throwable cause, int errorCode) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	/**
	 * Instantiates a new application exception.
	 *
	 * @param cause
	 *            the cause
	 */
	public ApplicationException(Throwable cause) {
		super(cause);
	}

	/**
	 * Instantiates a new application exception.
	 *
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 * @param enableSuppression
	 *            the enable suppression
	 * @param writableStackTrace
	 *            the writable stack trace
	 */
	public ApplicationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * Gets the error code.
	 *
	 * @return the error code
	 */
	public int getErrorCode() {
		return errorCode;
	}
}
