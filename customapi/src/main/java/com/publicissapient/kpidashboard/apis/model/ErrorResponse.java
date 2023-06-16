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

package com.publicissapient.kpidashboard.apis.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

public class ErrorResponse {

	private final Map<String, List<String>> globalErrors = new HashMap<>();
	private final Map<String, List<String>> fieldErrors = new HashMap<>();
	private long timestamp;
	private int code;
	private String error;
	private String message;

	public ErrorResponse() {
		timestamp = new Date().getTime();
	}

	/**
	 * Iterates over list of global errors and maps errors to ObjectName
	 *
	 * @param bindException
	 * @return errorResponse
	 */
	public static ErrorResponse fromBindException(BindException bindException) {
		ErrorResponse errorResponse = new ErrorResponse();

		for (ObjectError objectError : bindException.getGlobalErrors()) {
			List<String> errors = errorResponse.getGlobalErrors().get(objectError.getObjectName());
			if (errors == null) {
				errors = new ArrayList<>();
				errorResponse.getGlobalErrors().put(objectError.getObjectName(), errors);
			}
			errors.add(objectError.getDefaultMessage());
		}

		for (FieldError fieldError : bindException.getFieldErrors()) {
			List<String> errors = errorResponse.getFieldErrors().get(fieldError.getField());
			if (errors == null) {
				errors = new ArrayList<>();
				errorResponse.getFieldErrors().put(fieldError.getField(), errors);
			}
			errors.add(fieldError.getDefaultMessage());
		}

		return errorResponse;
	}

	/**
	 *
	 * @return error.
	 */
	public String getError() {
		return error;
	}

	/**
	 * Sets error
	 *
	 * @param error
	 */
	public void setError(String error) {
		this.error = error;
	}

	/**
	 *
	 * @return code
	 */
	public int getCode() {
		return code;
	}

	/**
	 * Sets code
	 *
	 * @param code
	 */
	public void setCode(int code) {
		this.code = code;
	}

	/**
	 *
	 * @return globalErrors
	 */
	public Map<String, List<String>> getGlobalErrors() {
		return globalErrors;
	}

	/**
	 *
	 * @return fieldErrors
	 */
	public Map<String, List<String>> getFieldErrors() {
		return fieldErrors;
	}

	/**
	 * Adds fieldError entries to List
	 *
	 * @param field
	 * @param error
	 */
	public void addFieldError(String field, String error) {
		List<String> errors = getFieldErrors().get(field);
		if (errors == null) {
			errors = new ArrayList<>();
			getFieldErrors().put(field, errors);
		}
		errors.add(error);
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
