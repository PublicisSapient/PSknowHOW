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

package com.publicissapient.kpidashboard.apis.enums;

import java.util.Arrays;

/**
 * @author tauakram
 *
 */
public enum Filters {

	PROJECT, SPRINT, RELEASE, INVALID, ROOT;

	/**
	 * Returns list view of Filter enums
	 * 
	 * @param filter
	 * @return Filters
	 */
	public static Filters getFilter(String filter) {

		return Arrays.asList(Filters.values()).stream().filter(f -> f.name().equalsIgnoreCase(filter)).findAny()
				.orElse(INVALID);
	}

}
