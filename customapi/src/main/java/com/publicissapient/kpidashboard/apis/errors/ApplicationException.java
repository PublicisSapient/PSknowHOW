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

/**
 * 
 */
package com.publicissapient.kpidashboard.apis.errors;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import org.springframework.util.StringUtils;

/**
 * A Generic CustomApiApplication Exception
 *
 * @author tauakram
 */
public class ApplicationException extends Exception {

	private static final long serialVersionUID = 1L;
	private static final int ONE = 1;

	/**
	 * Instantiates a new CustomApiApplication exception.
	 *
	 * @param clazz
	 *            the clazz
	 * @param searchParamsMap
	 *            the search params map
	 */
	public ApplicationException(Class clazz, String... searchParamsMap) {
		super(ApplicationException.generateMessage(clazz.getSimpleName(),
				toMap(String.class, String.class, searchParamsMap)));
	}

	/**
	 *
	 * @param entity
	 * @param searchParams
	 * @return
	 */
	private static String generateMessage(String entity, Map<String, String> searchParams) {
		return StringUtils.capitalize(entity) + " error while processing " + searchParams;
	}

	/**
	 *
	 * @param keyType
	 * @param valueType
	 * @param entries
	 * @param <K>
	 * @param <V>
	 * @return
	 */
	private static <K, V> Map<K, V> toMap(Class<K> keyType, Class<V> valueType, Object... entries) {
		if (entries.length % 2 == ONE) {
			throw new IllegalArgumentException("Invalid entries");
		}
		return IntStream.range(0, entries.length / 2).map(i -> i * 2).collect(HashMap::new,
				(m, i) -> m.put(keyType.cast(entries[i]), valueType.cast(entries[i + 1])), Map::putAll);
	}

}
