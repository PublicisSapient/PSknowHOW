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

import lombok.extern.slf4j.Slf4j;

/**
 * Provides Common utilities.
 *
 */
@Slf4j
public final class CommonUtils {

	/**
	 * handle taint value propagation vulnerability
	 *
	 * @param value
	 *            taintedValue
	 * @return string response
	 */
	public static final String AUTH_DETAILS_UPDATED_FLAG = "auth-details-updated";
	public static String handleCrossScriptingTaintedValue(String value) {
		return null == value ? null : value.replaceAll("[\\n|\\r\\t]", "");
	}
}