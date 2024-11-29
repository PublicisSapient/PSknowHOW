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

/** */
package com.publicissapient.kpidashboard.apis.errors;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import org.springframework.util.StringUtils;

/**
 * A Generic CustomApiApplication Exception
 *
 * @author Hiren Babariya
 */
public class ApplicationException extends Exception {

	public static final int NOTHING_TO_UPDATE = 0;
	public static final int JSON_FORMAT_ERROR = -1;
	public static final int COLLECTOR_CREATE_ERROR = -10;
	public static final int COLLECTOR_ITEM_CREATE_ERROR = -11;
	public static final int ERROR_INSERTING_DATA = -12;
	public static final int DUPLICATE_DATA = -13;
	public static final int BAD_DATA = -14;

	private static final long serialVersionUID = 1L;
	private static final int ONE = 1;

	private int errorCode;

	/**
	 * Instantiates a new application exception.
	 *
	 * @param message
	 *          the message
	 * @param errorCode
	 *          the error code
	 */
	public ApplicationException(String message, int errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

	/**
	 * Instantiates a new CustomApiApplication exception.
	 *
	 * @param clazz
	 *          the clazz
	 * @param searchParamsMap
	 *          the search params map
	 */
	public ApplicationException(Class clazz, String... searchParamsMap) {
		super(ApplicationException.generateMessage(clazz.getSimpleName(),
				toMap(String.class, String.class, searchParamsMap)));
	}

	/**
	 * @param entity
	 * @param searchParams
	 * @return
	 */
	private static String generateMessage(String entity, Map<String, String> searchParams) {
		return StringUtils.capitalize(entity) + " error while processing " + searchParams;
	}

	/**
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
