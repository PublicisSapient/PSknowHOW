/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.common.exceptions;

/**
 * The enum Client error message code.
 *
 * @author aksshriv1
 */
public enum ClientErrorMessageEnum {
	UNAUTHORIZED(401, "Sorry, you are not authorized to access the requested resource." +
			" Please check your credentials/token and try again. \n\n" + noteMessage()),

	FORBIDDEN(403, "Oops! You don't have permission to access the requested resource/tool. \n\n" + noteMessage()),

	NOT_FOUND(404,
			"We couldn't find the requested resource. Please check the base URL, resource version and try again. \n\n" +
					noteMessage()), METHOD_NOT_ALLOWED(
							405,
							"Oops! The action you're trying to perform is not allowed for this resource." +
									" Please check your request and try again. \n\n" + noteMessage()), REQUEST_TIMEOUT(
											408,
											"Oops! The server timed out while waiting for the request. Please try again later. \n\n" +
													noteMessage()), TOO_MANY_REQUESTS(
															429,
															"Oops! Too many requests have been sent in a short amount of time. Please try again later. \n\n" +
																	noteMessage()), OTHER_CLIENT_ERRORS(-1,
																			"Please check logs and contact the KnowHow support team for assistance or clarification \n\n" +
																					noteMessage());

	private final int value;
	private final String reasonPhrase;

	/** Common note message for all client errors. */
	private static String noteMessage() {
		return "Note:- One or more projects using this connection are impacted. " +
				"Please ignore this message if the concerned processors are running successfully " +
				"and your KPI dashboards remain unaffected for your project.";
	}

	/**
	 * @param value
	 *          value
	 * @param reasonPhrase
	 *          reasonPhrase
	 */
	private ClientErrorMessageEnum(int value, String reasonPhrase) {
		this.value = value;
		this.reasonPhrase = reasonPhrase;
	}

	/**
	 * @return it returns error code
	 */
	public int value() {
		return this.value;
	}

	/**
	 * @return it returns error msg
	 */
	public String getReasonPhrase() {
		return this.reasonPhrase;
	}

	/**
	 * @param value
	 *          value
	 * @return return error message
	 */
	public static ClientErrorMessageEnum fromValue(int value) {
		for (ClientErrorMessageEnum error : ClientErrorMessageEnum.values()) {
			if (error.value == value) {
				return error;
			}
		}
		return OTHER_CLIENT_ERRORS;
	}
}
