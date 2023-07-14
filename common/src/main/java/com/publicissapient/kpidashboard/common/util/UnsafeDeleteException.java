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
 * Class provides method for unsafe delete exception. Created by jkc on 1/20/16.
 */
public class UnsafeDeleteException extends RuntimeException {
	private static final long serialVersionUID = -664077740219817001L;

	/**
	 * Instantiates a new unsafe delete exception.
	 */
	public UnsafeDeleteException() {
		super();
	}

	/**
	 * Instantiates a new unsafe delete exception.
	 *
	 * @param str
	 *            the str
	 */
	public UnsafeDeleteException(String str) {
		super(str);
	}

	/**
	 * Instantiates a new unsafe delete exception.
	 *
	 * @param str
	 *            the str
	 * @param throwable
	 *            the throwable
	 */
	public UnsafeDeleteException(String str, Throwable throwable) {
		super(str, throwable);
	}

	/**
	 * Instantiates a new unsafe delete exception.
	 *
	 * @param throwable
	 *            the throwable
	 */
	public UnsafeDeleteException(Throwable throwable) {
		super(throwable);
	}

}
