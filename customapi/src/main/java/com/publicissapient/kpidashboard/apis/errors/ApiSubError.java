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

package com.publicissapient.kpidashboard.apis.errors;

/**
 * This class is used for representing multiple errors in a single call.
 *
 * @author tauakram
 */
public class ApiSubError { // NOSONAR
	// This is required for the existing structure
}

/**
 * Represents api validation error
 *
 * @author tauakram
 */
/* package */ class ApiValidationError extends ApiSubError {
	private String object;
	private String field;
	private Object rejectedValue;
	private String message;

	/**
	 * Instantiates a new Api validation error.
	 *
	 * @param object
	 *            objectName the name of the affected object
	 * @param message
	 *            the message
	 */
	public ApiValidationError(String object, String message) {
		super();
		this.object = object;
		this.message = message;
	}

	/**
	 * Instantiates a new Api validation error.
	 *
	 * @param object
	 *            the name of the affected object
	 * @param field
	 *            the field
	 * @param rejectedValue
	 *            the rejected value
	 * @param message
	 *            the message
	 */
	public ApiValidationError(String object, String field, Object rejectedValue, String message) {
		super();
		this.object = object;
		this.field = field;
		this.rejectedValue = rejectedValue;
		this.message = message;
	}

	/**
	 * Gets object.
	 *
	 * @return the name of the affected object
	 */
	public String getObject() {
		return object;
	}

	/**
	 * Sets object.
	 *
	 * @param object
	 *            the name of the affected object
	 */
	public void setObject(String object) {
		this.object = object;
	}

	/**
	 * Gets field.
	 *
	 * @return the field
	 */
	public String getField() {
		return field;
	}

	/**
	 * Sets field.
	 *
	 * @param field
	 *            the field
	 */
	public void setField(String field) {
		this.field = field;
	}

	/**
	 * Gets rejected value.
	 *
	 * @return the rejected value
	 */
	public Object getRejectedValue() {
		return rejectedValue;
	}

	/**
	 * Sets rejected value.
	 *
	 * @param rejectedValue
	 *            the rejected value
	 */
	public void setRejectedValue(Object rejectedValue) {
		this.rejectedValue = rejectedValue;
	}

	/**
	 * Gets message.
	 *
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Sets message.
	 *
	 * @param message
	 *            the message
	 */
	public void setMessage(String message) {
		this.message = message;
	}
}
