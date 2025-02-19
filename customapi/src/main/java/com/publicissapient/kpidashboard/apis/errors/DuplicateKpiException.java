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
 * Exception thrown when a duplicate KPI is detected.
 *
 * <p>
 * This exception is used to indicate that an attempt to create or update a
 * report has failed due to the presence of duplicate KPI IDs.
 */
public class DuplicateKpiException extends RuntimeException {
	/**
	 * Constructs a new DuplicateKpiException with the specified detail message.
	 *
	 * @param message
	 *          the detail message
	 */
	public DuplicateKpiException(String message) {
		super(message);
	}
}
