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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Utils class.
 */
@Component
@Slf4j
public class PropertyUtils {

	/**
	 * Trim props.
	 *
	 * @param fields
	 *            the fields
	 * @param propertyInstance
	 *            the property instance
	 */
	public void trimProps(Field[] fields, Object propertyInstance) {
		log.info("trimProps started for : {}", propertyInstance.getClass());

		for (Field field : fields) {
			field.setAccessible(true);
			Object oldValue;
			try {
				if (isStringType(field)) {
					oldValue = field.get(propertyInstance);
					field.set(propertyInstance, trimString(oldValue));

				} else if (isList(field)) {
					trimListProps(propertyInstance, field);
				} else if (isMap(field)) {
					trimMapProps(propertyInstance, field);
				}

			} catch (IllegalArgumentException | IllegalAccessException e) {
				log.error("Error while trimming field", e);
			}
		}
		log.info("trimProps ended for : {}", propertyInstance.getClass());
	}

	/**
	 * Trim map props.
	 *
	 * @param propertyInstance
	 *            the property instance
	 * @param field
	 *            the field
	 * @throws IllegalAccessException
	 *             the illegal access exception
	 */
	private void trimMapProps(Object propertyInstance, Field field) throws IllegalAccessException {
		Map<Object, Object> orgMap = (Map<Object, Object>) field.get(propertyInstance);
		Map<Object, Object> tmpMap = new HashMap<>();
		for (Map.Entry<Object, Object> entry : orgMap.entrySet()) {
			Object objValue = entry.getValue();
			if (objValue instanceof String) {
				tmpMap.put(entry.getKey(), trimString(objValue));
				field.set(propertyInstance, tmpMap);
			} else if (objValue instanceof Collection) {
				tmpMap.put(entry.getKey(), trimCollection(objValue));
				field.set(propertyInstance, tmpMap);
			}
		}
	}

	/**
	 * Trim list props.
	 *
	 * @param propertyInstance
	 *            the property instance
	 * @param field
	 *            the field
	 * @throws IllegalAccessException
	 *             the illegal access exception
	 */
	private void trimListProps(Object propertyInstance, Field field) throws IllegalAccessException {
		List<Object> orgList = (List<Object>) field.get(propertyInstance);
		List<Object> tmpList = new ArrayList<>();
		if (!CollectionUtils.isEmpty(orgList)) {
			for (Object obj : orgList) {
				if (obj instanceof String) {
					tmpList.add(trimString(obj));
					field.set(propertyInstance, tmpList);
				} else if (obj instanceof Collection) {
					tmpList.add(trimCollection(obj));
					field.set(propertyInstance, tmpList);
				}
			}
		}
	}

	/**
	 * Checks if is map.
	 *
	 * @param field
	 *            the field
	 * @return true, if is map
	 */
	private boolean isMap(Field field) {
		return field.getType().isAssignableFrom(Map.class);
	}

	/**
	 * Checks if is list.
	 *
	 * @param field
	 *            the field
	 * @return true, if is list
	 */
	private boolean isList(Field field) {
		return field.getType().isAssignableFrom(List.class);
	}

	/**
	 * Checks if is string type.
	 *
	 * @param field
	 *            the field
	 * @return true, if is string type
	 */
	private boolean isStringType(Field field) {

		return field.getType().isAssignableFrom(String.class);
	}

	/**
	 * Trim collection.
	 *
	 * @param obj
	 *            the obj
	 * @return the collection
	 */
	private Collection<String> trimCollection(Object obj) {
		if (null != obj) {
			Collection<String> list = (Collection<String>) obj;
			return list.stream().map(String::trim).collect(Collectors.toList());
		}
		return null; // NOSONAR
	}

	/**
	 * Trim string.
	 *
	 * @param obj
	 *            the obj
	 * @return the string
	 */
	private String trimString(Object obj) {
		if (null != obj) {
			return obj.toString().trim();
		}
		return null;
	}
}
