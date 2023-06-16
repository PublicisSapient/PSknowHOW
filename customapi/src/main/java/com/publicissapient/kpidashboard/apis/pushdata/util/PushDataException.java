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

package com.publicissapient.kpidashboard.apis.pushdata.util;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.pushdata.model.PushDataResponse;

@Component
public class PushDataException extends RuntimeException {
	private static final long serialVersionUID = -664077740219817001L;

	private PushDataResponse pushDataResponse;
	private HttpStatus code;

	public PushDataException() {
		super();
	}

	public PushDataException(String unauthorizedAccessException, HttpStatus code) {
		super(unauthorizedAccessException);
		this.code = code;
	}

	public PushDataException(String str) {
		super(str);
	}

	public PushDataException(String str, PushDataResponse pushDataResponse) {
		super(str);
		setPushBuildDeployResponse(pushDataResponse);
	}

	/**
	 * Instantiates a new unsafe delete exception.
	 *
	 * @param str
	 *            the str
	 * @param throwable
	 *            the throwable
	 */
	public PushDataException(String str, Throwable throwable) {
		super(str, throwable);
	}

	/**
	 * Instantiates a new unsafe delete exception.
	 *
	 * @param throwable
	 *            the throwable
	 */
	public PushDataException(Throwable throwable) {
		super(throwable);
	}

	public HttpStatus getCode() {
		return code;
	}

	public PushDataResponse getPushBuildDeployResponse() {
		return pushDataResponse;
	}

	public void setPushBuildDeployResponse(PushDataResponse pushDataResponse) {
		this.pushDataResponse = pushDataResponse;
	}

}
