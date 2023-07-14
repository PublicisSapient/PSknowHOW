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

import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

/**
 * Custom utils for common operations
 */
public final class CustomUtils {

	private CustomUtils() {
		// to prevent creation on object
	}

	/**
	 * Primary keygenerator string.
	 *
	 * @return the string
	 */
	public static String primaryKeygenerator() {

		return UUID.randomUUID().toString().replace("-", "");
	}

	/**
	 * Primary key on name string.
	 *
	 * @param name
	 *            the name
	 * @return the string
	 */
	public static String primaryKeyOnName(String name) {

		String returnString = null;
		if (StringUtils.isNotEmpty(name) && StringUtils.isNotBlank(name)) {
			returnString = name.toLowerCase().trim();

		}

		return returnString;
	}

	/**
	 * Generates a primary key for last level of input node names with level in the
	 * order top to bottom
	 *
	 * @param nodeNameMap
	 *            the node name map
	 * @return string
	 */
	public static String primaryKeyOnName(Map<String, String> nodeNameMap) {

		StringBuilder returnString = new StringBuilder(100);

		nodeNameMap.forEach(
				(key, value) -> returnString.append(null == value ? "" : value.trim().toLowerCase()).append("_"));

		return returnString.toString().endsWith("_")
				? returnString.toString().substring(0, returnString.toString().length() - 1)
				: returnString.toString();

	}

}
