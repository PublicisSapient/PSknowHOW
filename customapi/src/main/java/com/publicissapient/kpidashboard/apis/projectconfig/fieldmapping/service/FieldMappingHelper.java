/*
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.publicissapient.kpidashboard.apis.projectconfig.fieldmapping.service;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Update;

import com.publicissapient.kpidashboard.common.model.application.BaseFieldMappingStructure;
import com.publicissapient.kpidashboard.common.model.application.ConfigurationHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.FieldMappingMeta;
import com.publicissapient.kpidashboard.common.model.application.FieldMappingResponse;
import com.publicissapient.kpidashboard.common.model.application.FieldMappingStructure;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class FieldMappingHelper {
	public static final String HISTORY = "history";
	public static final String OBJECT_ID = "org.bson.types.ObjectId";
	public static final String DOUBLE = "java.lang.Double";

	private FieldMappingHelper() {
	}

	public static Object getFieldMappingField(FieldMapping fieldMapping, Class<?> fieldMapping1, String field)
			throws NoSuchFieldException, IllegalAccessException {
		Field declaredField = fieldMapping1.getDeclaredField(field);
		setAccessible(declaredField);
		return declaredField.get(fieldMapping);
	}

	public static void setAccessible(Field field) {
		field.setAccessible(true); // NOSONAR
	}

	/**
	 *
	 * @param fieldMapping
	 *            fieldMapping
	 * @param fieldName
	 *            fieldName
	 * @return list of history logs
	 * @throws NoSuchFieldException
	 *             no field exception
	 * @throws IllegalAccessException
	 *             accessibility
	 */
	public static List<ConfigurationHistoryChangeLog> getAccessibleFieldHistory(FieldMapping fieldMapping,
			String fieldName) throws NoSuchFieldException, IllegalAccessException {
		return (List<ConfigurationHistoryChangeLog>) getFieldMappingField(fieldMapping,
				FieldMapping.class.getSuperclass(), HISTORY + fieldName);
	}

	/*
	 * while getting fields get fieldmappinghistory
	 */
	public static List<ConfigurationHistoryChangeLog> getFieldMappingHistory(FieldMapping fieldMapping, String field,
			String nodeId, boolean nodeSpecificField) throws NoSuchFieldException, IllegalAccessException {
		List<ConfigurationHistoryChangeLog> accessibleFieldHistory = getAccessibleFieldHistory(fieldMapping, field);
		if (nodeSpecificField && StringUtils.isNotEmpty(nodeId) && CollectionUtils.isNotEmpty(accessibleFieldHistory)) {
			return accessibleFieldHistory.stream()
					.filter(configurationHistoryChangeLog -> StringUtils
							.isNotEmpty(configurationHistoryChangeLog.getReleaseNodeId())
							&& configurationHistoryChangeLog.getReleaseNodeId().equalsIgnoreCase(nodeId))
					.toList();
		}
		return accessibleFieldHistory;
	}

	/*
	 * to get the field from fieldMapping
	 */
	public static Object getFieldMappingData(FieldMapping fieldMapping, Class<FieldMapping> fieldMappingClass,
			String field, String nodeId, boolean nodeSpecificField)
			throws NoSuchFieldException, IllegalAccessException {
		Object fieldMappingField = getFieldMappingField(fieldMapping, fieldMappingClass, field);
		if (nodeSpecificField && StringUtils.isNotEmpty(nodeId)) {
			if (ObjectUtils.isNotEmpty(fieldMappingField)) {
				Map<String, Integer> mappingField = (Map<String, Integer>) fieldMappingField;
				return mappingField.getOrDefault(nodeId, 0);
			} else {
				return 0;
			}
		} else {
			return fieldMappingField;
		}
	}

	/**
	 * compares field values for saved and unsaved data.
	 *
	 * @param value
	 *            unsaved value
	 * @param value1
	 *            existing value
	 * @return is value updated
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static boolean isValueUpdated(Object value, Object value1) {
		if (ObjectUtils.isEmpty(value) && ObjectUtils.isEmpty(value1)) {
			return false;

		} else {
			if (value instanceof List) {
				return !(value1 instanceof List && (((List) value).size() == ((List) value1).size())
						&& ((List) value).containsAll((List) value1));

			} else if (value instanceof String[]) {
				return !(value1 instanceof String[] && Arrays.equals((String[]) value, (String[]) value1));

			} else {
				if (value1 != null) {
					return !value1.equals(value);
				} else {
					return true;
				}

			}
		}
	}

	public static Object getNestedField(FieldMapping newMapping, Class<FieldMapping> fieldMappingClass, Object newValue,
			FieldMappingStructure mappingStructure) throws NoSuchFieldException, IllegalAccessException {
		if (null != mappingStructure && CollectionUtils.isNotEmpty(mappingStructure.getNestedFields())) {
			StringBuilder originalValue = new StringBuilder(newValue + "-");
			// for nested fields
			for (BaseFieldMappingStructure nestedField : mappingStructure.getNestedFields()) {
				if (nestedField.getFilterGroup().contains(newValue)) {
					Object fieldMappingField = FieldMappingHelper.getFieldMappingField(newMapping, fieldMappingClass,
							nestedField.getFieldName());
					if (fieldMappingField != null) {
						originalValue.append(fieldMappingField).append(":");
					}
				}
			}
			return originalValue.deleteCharAt(originalValue.length() - 1).toString();
		}
		return newValue;
	}

	public static Object generateAdditionalFilters(Object newValue, String fieldName) {
		if (fieldName.equalsIgnoreCase("additionalFilterConfig")) {
			List<LinkedHashMap<String, Object>> additonalValue = (List<LinkedHashMap<String, Object>>) newValue;
			StringBuilder originalValue = new StringBuilder();
			for (Map<String, Object> value : additonalValue) {

				String identificationButton = (String) value.get("identifyFrom");
				String identificationValue;
				if (identificationButton.equalsIgnoreCase("customfield")) {
					identificationValue = (String) value.get("identificationField");
				} else {
					identificationValue = value.get("values").toString();
				}
				originalValue.append(value.get("filterId")).append("-").append(identificationButton).append(":")
						.append(identificationValue).append(" ,");
			}
			return originalValue.toString();
		}
		return null;
	}

	public static void setFieldValue(FieldMapping object, String fieldName, Object value)
			throws IllegalAccessException {
		try {
			Field field = FieldMapping.class.getDeclaredField(fieldName);
			setAccessible(field);
			Object v = convertToSameType(field, value);
			field.set(object, v);
		} catch (NoSuchFieldException e) {
			log.warn("Field not found");
		}
	}

	private static Object convertToSameType(Field field, Object value1) {
		Class<?> fieldType = field.getType();
		if (fieldType.isArray() && fieldType.getComponentType() == String.class) {
			List<?> list = (List<?>) value1;
			return list.toArray(new String[0]);
		} else if (fieldType.getName().equalsIgnoreCase(OBJECT_ID)) {
			return new ObjectId((String) value1);
		} else if (fieldType.getName().equalsIgnoreCase(DOUBLE)) {
			return ((Integer) value1).doubleValue();
		} else {
			// Unsupported type, return null
			return value1;
		}
	}

	public static boolean isFieldPresent(Class<?> clazz, String fieldName) {
		try {
			clazz.getDeclaredField(fieldName);
			return true;
		} catch (NoSuchFieldException e) {
			return false;
		}
	}

	/**
	 * the history of all the nested fields should appear on the first identifier.
	 *
	 * @param fieldMappingResponseList
	 *            fieldMappingResponseList
	 * @param fieldMappingResponse
	 *            fieldMappingResponse
	 * @param mappingStructure
	 *            mappingStructure
	 * @param fieldMapping
	 *            fieldMapping
	 * @throws NoSuchFieldException
	 *             NoSuchFieldException
	 * @throws IllegalAccessException
	 *             IllegalAccessException
	 */
	public static void generateHistoryForNestedFields(List<FieldMappingResponse> fieldMappingResponseList,
			FieldMappingResponse fieldMappingResponse, FieldMappingStructure mappingStructure,
			FieldMapping fieldMapping) throws NoSuchFieldException, IllegalAccessException {
		if (CollectionUtils.isNotEmpty(mappingStructure.getNestedFields())) {

			StringBuilder originalValue = new StringBuilder(fieldMappingResponse.getOriginalValue() + "-");
			// check the fields in nestedfield section of fieldmapping structure and find if
			// those fields are present in the fieldmapping response
			for (BaseFieldMappingStructure nestedField : mappingStructure.getNestedFields()) {
				Optional<FieldMappingResponse> mappingResponse = fieldMappingResponseList.stream()
						.filter(response -> response.getFieldName().equalsIgnoreCase(nestedField.getFieldName())
								&& nestedField.getFilterGroup()
										.contains(fieldMappingResponse.getOriginalValue().toString()))
						.findFirst();
				mappingResponse.ifPresent(response -> originalValue.append(response.getOriginalValue()).append(":"));
			}
			setFieldMappingResponse(fieldMappingResponse, fieldMapping, originalValue);
		}

	}

	public static void setMappingResponseWithGeneratedField(FieldMappingResponse fieldMappingResponse,
			FieldMapping fieldMapping) throws NoSuchFieldException, IllegalAccessException {
		Object additonalFilter = generateAdditionalFilters(fieldMappingResponse.getOriginalValue(),
				fieldMappingResponse.getFieldName());
		if (additonalFilter != null) {
			setFieldMappingResponse(fieldMappingResponse, fieldMapping, new StringBuilder((String) additonalFilter));
		}
	}

	public static void setFieldMappingResponse(FieldMappingResponse fieldMappingResponse, FieldMapping fieldMapping,
			StringBuilder originalValue) throws NoSuchFieldException, IllegalAccessException {
		List<ConfigurationHistoryChangeLog> changeLogs = FieldMappingHelper.getAccessibleFieldHistory(fieldMapping,
				fieldMappingResponse.getFieldName());
		String previousValue = "";
		if (CollectionUtils.isNotEmpty(changeLogs)) {
			ConfigurationHistoryChangeLog configurationHistoryChangeLog = changeLogs.get(changeLogs.size() - 1);
			previousValue = String.valueOf(configurationHistoryChangeLog.getChangedTo());

		}
		if (ObjectUtils.isNotEmpty(originalValue)) {
			fieldMappingResponse.setOriginalValue(originalValue.deleteCharAt(originalValue.length() - 1).toString());
		}
		fieldMappingResponse.setPreviousValue(previousValue);
	}

	/*
	 * create Node Specific FieldData
	 */
	public static void setNodeSpecificFields(FieldMappingStructure mappingStructure,
			FieldMappingResponse fieldMappingResponse, FieldMapping fieldMapping, String nodeId, Update update)
			throws NoSuchFieldException, IllegalAccessException {
		if (mappingStructure.isNodeSpecific() && StringUtils.isNotEmpty(nodeId)) {
			Object fieldMappingField = getFieldMappingField(fieldMapping, FieldMapping.class,
					fieldMappingResponse.getFieldName());
			Object originalValue = fieldMappingResponse.getOriginalValue();
			if (ObjectUtils.isNotEmpty(originalValue)) {
				Map<String, Integer> map = new HashMap<>();
				if (ObjectUtils.isNotEmpty(fieldMappingField)) {
					map = (Map<String, Integer>) fieldMappingField;
				}
				map.put(nodeId, (Integer) originalValue);
				update.set(fieldMappingResponse.getFieldName(), map);
			}

			List<ConfigurationHistoryChangeLog> getNodeSpecificFieldHistory = FieldMappingHelper
					.getAccessibleFieldHistory(fieldMapping, fieldMappingResponse.getFieldName());
			String previousValue = "";
			if (CollectionUtils.isNotEmpty(getNodeSpecificFieldHistory)) {
				List<ConfigurationHistoryChangeLog> changeLogs = getNodeSpecificFieldHistory.stream()
						.filter(configurationHistoryChangeLog -> configurationHistoryChangeLog.getReleaseNodeId()
								.equalsIgnoreCase(nodeId))
						.toList();
				if (CollectionUtils.isNotEmpty(changeLogs)) {
					ConfigurationHistoryChangeLog configurationHistoryChangeLog = changeLogs.get(changeLogs.size() - 1);
					previousValue = String.valueOf(configurationHistoryChangeLog.getChangedTo());
				}
			}
			if (ObjectUtils.isNotEmpty(originalValue)) {
				fieldMappingResponse.setOriginalValue(originalValue);
			}
			fieldMappingResponse.setPreviousValue(previousValue);
		}
	}

	public static void removeDuplicateFieldsFromResponse(List<FieldMappingResponse> originalFieldMappingResponseList,
			Map<String, FieldMappingResponse> responseHashMap) {
		for (FieldMappingResponse response : originalFieldMappingResponseList) {
			responseHashMap.computeIfPresent(response.getFieldName(),
					(k, existingResponse) -> (existingResponse.getPreviousValue() == null
							&& response.getPreviousValue() != null) ? response : existingResponse);
			responseHashMap.putIfAbsent(response.getFieldName(), response);
		}
	}

	public static ConfigurationHistoryChangeLog createHistoryChangeLog(FieldMappingMeta fieldMappingMeta,
			FieldMappingResponse fieldMappingResponse, FieldMappingStructure mappingStructure, String loggedInUser) {
		ConfigurationHistoryChangeLog configurationHistoryChangeLog = new ConfigurationHistoryChangeLog();
		configurationHistoryChangeLog.setChangedTo(fieldMappingResponse.getOriginalValue());
		configurationHistoryChangeLog.setChangedFrom(fieldMappingResponse.getPreviousValue());
		configurationHistoryChangeLog.setChangedBy(loggedInUser);
		configurationHistoryChangeLog.setUpdatedOn(LocalDateTime.now().toString());
		if (mappingStructure.isNodeSpecific()) {
			configurationHistoryChangeLog.setReleaseNodeId(fieldMappingMeta.getReleaseNodeId());
		}
		return configurationHistoryChangeLog;
	}

}
