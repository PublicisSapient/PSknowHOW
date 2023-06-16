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

package com.publicissapient.kpidashboard.common.model.application;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * The Class KeyValueLog.
 */
public class KeyValueLog {

	private static final char SEPERATOR = ' ';

	private static final char EQUALS = '=';

	private static final char QUOTE = '"';

	private final Map<String, Object> attributes = new LinkedHashMap<>();

	/**
	 * With.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return the key value log
	 */
	public KeyValueLog with(String key, Object value) {
		attributes.put(key, value);
		return this;
	}

	@Override
	public String toString() {

		Set<String> keySet = attributes.keySet();
		StringBuilder builder = new StringBuilder();
		for (String key : keySet) {
			builder.append(key).append(EQUALS).append(QUOTE).append(attributes.get(key)).append(QUOTE)
					.append(SEPERATOR);
		}

		return builder.toString().trim();
	}

}
