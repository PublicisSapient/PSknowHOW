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

package com.publicissapient.kpidashboard.github.customexception;

/**
 * @author anisingh4
 */
public class FetchingCommitException extends Exception {

	private static final long serialVersionUID = -7003770711199668845L;

	/**
	 * The constructor.
	 *
	 * @param message
	 *          message
	 */
	public FetchingCommitException(String message) {
		super(message);
	}

	/**
	 * The constructor.
	 *
	 * @param message
	 *          message
	 * @param cause
	 *          cause
	 */
	public FetchingCommitException(String message, Throwable cause) {
		super(message, cause);
	}
}
