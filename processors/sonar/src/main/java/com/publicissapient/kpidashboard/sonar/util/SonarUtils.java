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

package com.publicissapient.kpidashboard.sonar.util;

import com.publicissapient.kpidashboard.common.model.ToolCredential;
import com.publicissapient.kpidashboard.common.model.processortool.ProcessorToolConnection;
import com.publicissapient.kpidashboard.common.service.ToolCredentialProvider;

/**
 * Utility class for common methods.
 *
 */
public final class SonarUtils {

	private static final char BLANK_SPACE = ' ';
	private static final String MINUTES_FORMAT = "%smin";
	private static final String HOURS_FORMAT = "%sh";
	private static final String DAYS_FORMAT = "%sd";

	private SonarUtils() {

	}

	/**
	 * Adds space to message if needed.
	 *
	 * @param message
	 *            the message
	 */
	public static void addSpaceIfNeeded(StringBuilder message) {
		if (message.length() > 0) {
			message.append(BLANK_SPACE);
		}
	}

	/**
	 * Format time duration.
	 * 
	 * @param days
	 *            the number of days
	 * @param hours
	 *            the numbers of hours
	 * @param minutes
	 *            the number of minutes
	 * @param isNegative
	 *            the negative value
	 * @return the formatted duration
	 */
	public static String formatDuration(int days, int hours, int minutes, boolean isNegative) {
		StringBuilder message = new StringBuilder();
		if (days > 0) {
			message.append(String.format(DAYS_FORMAT, isNegative ? (-1 * days) : days));
		}
		if (displayHours(days, hours)) {
			addSpaceIfNeeded(message);
			message.append(String.format(HOURS_FORMAT, isNegative && message.length() == 0 ? (-1 * hours) : hours));
		}
		if (displayMinutes(days, hours, minutes)) {
			addSpaceIfNeeded(message);
			message.append(
					String.format(MINUTES_FORMAT, isNegative && message.length() == 0 ? (-1 * minutes) : minutes));
		}
		return message.toString();
	}

	/**
	 * Checks if hour should be displayed.
	 * 
	 * @param days
	 *            the number of days
	 * @param hours
	 *            the number of hours
	 * @return true if hour should be displayed
	 */
	private static boolean displayHours(int days, int hours) {
		return hours > 0 && days < 10;
	}

	/**
	 * Checks if minutes should be displayed.
	 * 
	 * @param days
	 *            the number of days
	 * @param hours
	 *            the number of hours
	 * @param minutes
	 *            the number of minutes
	 * @return true if minutes should be displayed
	 */
	private static boolean displayMinutes(int days, int hours, int minutes) {
		return minutes > 0 && hours < 10 && days == 0;
	}

	public static ToolCredential getToolCredentials(ToolCredentialProvider toolCredentialProvider,
			ProcessorToolConnection sonarServer) {
		ToolCredential toolCredential = new ToolCredential();
		if (sonarServer.isVault()) {
			ToolCredential toolCredentialFromProvider = toolCredentialProvider
					.findCredential(sonarServer.getUsername());
			if (toolCredentialFromProvider != null) {
				toolCredential.setUsername(toolCredentialFromProvider.getUsername());
				toolCredential.setPassword(toolCredentialFromProvider.getPassword());
			}

		} else {
			toolCredential.setUsername(sonarServer.getUsername() == null ? null : sonarServer.getUsername().trim());
			if (sonarServer.isCloudEnv()) {
				toolCredential
						.setPassword(sonarServer.getAccessToken() == null ? null : sonarServer.getAccessToken().trim());
			} else if (sonarServer.isAccessTokenEnabled()) {
				toolCredential
						.setPassword(sonarServer.getAccessToken() == null ? null : sonarServer.getAccessToken().trim());
			} else {
				toolCredential.setPassword(sonarServer.getPassword() == null ? null : sonarServer.getPassword().trim());
			}
		}

		return toolCredential;
	}

}
