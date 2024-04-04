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

package com.publicissapient.kpidashboard.argocd.utils;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static com.publicissapient.kpidashboard.argocd.constants.ArgoCDConstants.DATETIME_FORMAT;

/**
 * Utilities
 */
public class ArgoCDUtils {

	private ArgoCDUtils() {
	}

	/**
	 * @param startTime
	 * 					start time
	 * @param endTime
	 * 					end time
	 * @return long - time difference in milliseconds
	 */
	public static long calculateDuration(String startTime, String endTime) {
		Instant startInstant = Instant.parse(startTime);
		Instant endInstant = Instant.parse(endTime);
		Duration duration = Duration.between(startInstant, endInstant);
		return duration.toMillis();
	}

	/**
	 * @param date
	 * 				String format of date
	 * @return String - formatted Date
	 */
	public static String formatDate(String date) {
		Instant dateTime = Instant.parse(date);
		return dateTime.atZone(ZoneId.systemDefault()).toLocalDateTime()
				.format(DateTimeFormatter.ofPattern(DATETIME_FORMAT));
	}

}
