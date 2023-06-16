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

package com.publicissapient.kpidashboard.common.util;

/**
 * Encryption exception too be used for encryption methods.
 */
public class EncryptionException extends Exception {
	private static final long serialVersionUID = -4472911532254883259L;

	/**
	 * Constructs a {@code EncrytionException} with no detail message.
	 */
	public EncryptionException() {
		super();
	}

	/**
	 * Constructs a {@code EncrytionException} with the specified detail message.
	 *
	 * @param str
	 *            the detail message.
	 */
	public EncryptionException(String str) {
		super(str);
	}

	/**
	 * Constructs a {@code EncrytionException} with the specified detail message and
	 * exception.
	 *
	 * @param str
	 *            the str
	 * @param throwable
	 *            the throwable
	 */
	public EncryptionException(String str, Throwable throwable) {
		super(str, throwable);
	}
}
