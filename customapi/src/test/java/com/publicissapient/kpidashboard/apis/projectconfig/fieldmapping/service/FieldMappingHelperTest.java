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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.data.mongodb.core.query.Update;

import com.publicissapient.kpidashboard.apis.data.FieldMappingDataFactory;
import com.publicissapient.kpidashboard.apis.data.FieldMappingStructureDataFactory;
import com.publicissapient.kpidashboard.common.model.application.ConfigurationHistoryChangeLog;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.FieldMappingMeta;
import com.publicissapient.kpidashboard.common.model.application.FieldMappingResponse;
import com.publicissapient.kpidashboard.common.model.application.FieldMappingStructure;

class FieldMappingHelperTest {
	@InjectMocks
	FieldMappingHelper fieldMappingHelper;

	FieldMapping scrumFieldMapping;

	List<FieldMappingStructure> fieldMappingStructureList;

	@BeforeEach
	public void setUp() {
		FieldMappingDataFactory fieldMappingDataFactory = FieldMappingDataFactory
				.newInstance("/json/default/scrum_project_field_mappings.json");
		scrumFieldMapping = fieldMappingDataFactory.getFieldMappings().get(0);
		List<ConfigurationHistoryChangeLog> configurationHistoryChangeLogList = new ArrayList<>();
		configurationHistoryChangeLogList.add(
				new ConfigurationHistoryChangeLog("", "customField", "currentUser", LocalDateTime.now().toString()));
		scrumFieldMapping.setHistorysprintName(configurationHistoryChangeLogList);
		ConfigurationHistoryChangeLog configurationHistoryChangeLog = new ConfigurationHistoryChangeLog();
		configurationHistoryChangeLog.setChangedTo("Customfield");
		configurationHistoryChangeLog.setChangedFrom("");
		configurationHistoryChangeLog.setChangedBy("currentUser");
		configurationHistoryChangeLog.setUpdatedOn(java.time.LocalDateTime.now().toString());
		scrumFieldMapping.setHistoryrootCauseIdentifier(Arrays.asList(configurationHistoryChangeLog));
		FieldMappingStructureDataFactory fieldMappingStructureDataFactory = FieldMappingStructureDataFactory
				.newInstance();
		fieldMappingStructureList = fieldMappingStructureDataFactory.getFieldMappingStructureList();

	}

	@Test
	void testGetFieldMappingField() throws NoSuchFieldException, IllegalAccessException {
		Object result = FieldMappingHelper.getFieldMappingField(scrumFieldMapping, FieldMapping.class, "sprintName");
		Assertions.assertEquals("customfield_12700", result);
	}

	@Test
	void testSetAccessible() throws NoSuchFieldException {
		FieldMappingHelper.setAccessible(FieldMapping.class.getDeclaredField("sprintName"));
	}

	@Test
	void testGetAccessibleFieldHistory() throws NoSuchFieldException, IllegalAccessException {
		List<ConfigurationHistoryChangeLog> result = FieldMappingHelper.getAccessibleFieldHistory(scrumFieldMapping,
				"sprintName");
		ConfigurationHistoryChangeLog configurationHistoryChangeLog = new ConfigurationHistoryChangeLog("",
				"customField", "currentUser", LocalDateTime.now().toString());
		List<ConfigurationHistoryChangeLog> configurationHistoryChangeLogList = new ArrayList<>();
		configurationHistoryChangeLogList.add(configurationHistoryChangeLog);
		Assertions.assertNull(result.get(0).getReleaseNodeId());
	}

	@Test
	void testGetFieldMappingHistory_NodeSpecific() throws NoSuchFieldException, IllegalAccessException {
		ConfigurationHistoryChangeLog configurationHistoryChangeLog = new ConfigurationHistoryChangeLog();
		configurationHistoryChangeLog.setChangedTo(78);
		configurationHistoryChangeLog.setChangedFrom("");
		configurationHistoryChangeLog.setChangedBy("changedBy");
		configurationHistoryChangeLog.setUpdatedOn(java.time.LocalDateTime.now().toString());
		configurationHistoryChangeLog.setReleaseNodeId("nodeId");
		scrumFieldMapping.setHistorystartDateCountKPI150(List.of(configurationHistoryChangeLog));
		List<ConfigurationHistoryChangeLog> result = FieldMappingHelper.getFieldMappingHistory(scrumFieldMapping,
				"startDateCountKPI150", "nodeId", true);
		Assertions.assertEquals(78, result.get(0).getChangedTo());
	}

	@Test
	void testGetFieldMappingData() throws NoSuchFieldException, IllegalAccessException {
		Object result = FieldMappingHelper.getFieldMappingData(scrumFieldMapping, FieldMapping.class, "sprintName",
				null, false);
		Assertions.assertEquals("customfield_12700", result);
	}

	@Test
	void testGetFieldMappingData_NodeSpecific_WithoutHistory() throws NoSuchFieldException, IllegalAccessException {
		Object result = FieldMappingHelper.getFieldMappingData(scrumFieldMapping, FieldMapping.class,
				"startDateCountKPI150", "nodeId", true);
		Assertions.assertEquals(0, result);
	}

	@Test
	void testGetFieldMappingData_NodeSpecific() throws NoSuchFieldException, IllegalAccessException {
		ConfigurationHistoryChangeLog configurationHistoryChangeLog = new ConfigurationHistoryChangeLog();
		configurationHistoryChangeLog.setChangedTo(78);
		configurationHistoryChangeLog.setChangedFrom("");
		configurationHistoryChangeLog.setChangedBy("changedBy");
		configurationHistoryChangeLog.setUpdatedOn(java.time.LocalDateTime.now().toString());
		configurationHistoryChangeLog.setReleaseNodeId("nodeId");
		scrumFieldMapping.setHistorystartDateCountKPI150(List.of(configurationHistoryChangeLog));
		Object result = FieldMappingHelper.getFieldMappingData(scrumFieldMapping, FieldMapping.class,
				"startDateCountKPI150", "node1", true);
		Assertions.assertEquals(10, result);
	}

	@Test
	void testIsValueUpdated() {
		boolean result = FieldMappingHelper.isValueUpdated("value", "value1");
		Assertions.assertEquals(true, result);
	}

	@Test
	void testGetNestedField() throws NoSuchFieldException, IllegalAccessException {
		Object result = FieldMappingHelper.getNestedField(scrumFieldMapping, FieldMapping.class, "CustomField",
				fieldMappingStructureList.stream().filter(fieldMappingStructure -> fieldMappingStructure.getFieldName()
						.equalsIgnoreCase("rootCauseIdentifier")).toList().get(0));
		Assertions.assertEquals("CustomField-customfield_19121", result);
	}

	@Test
	void testGenerateAdditionalFilters() {
		List<LinkedHashMap<String, Object>> additonalValue = new ArrayList<>();
		LinkedHashMap<String, Object> map = new LinkedHashMap<>();
		map.put("identifyFrom", "customfield");
		map.put("identificationField", "abc");
		map.put("filterId", "sqd");

		LinkedHashMap<String, Object> map1 = new LinkedHashMap<>();
		map1.put("identifyFrom", "Componnend");
		map1.put("values", "abc");
		map1.put("filterId", "sqd");
		additonalValue.add(map);
		additonalValue.add(map1);
		Object result = FieldMappingHelper.generateAdditionalFilters(additonalValue, "additionalFilterConfig");
		Assertions.assertEquals("sqd-customfield:abc ,sqd-Componnend:abc ,", result);
	}

	@Test
	void testNotToGenerateAdditionalFilters() {
		Object result = FieldMappingHelper.generateAdditionalFilters(new ArrayList<>(), "additionalFilterConfg");
		Assertions.assertNull(result);
	}

	@Test
	void testSetFieldValue() throws IllegalAccessException {
		FieldMappingHelper.setFieldValue(scrumFieldMapping, "fieldName", "value");
	}

	@Test
	void testIsFieldAbsent() {
		boolean result = FieldMappingHelper.isFieldPresent(FieldMapping.class, "fieldName");
		Assertions.assertEquals(false, result);
	}

	@Test
	void testIsFieldPresent() {
		boolean result = FieldMappingHelper.isFieldPresent(FieldMapping.class, "sprintName");
		Assertions.assertEquals(true, result);
	}

	@Test
	void testGenerateHistoryForNestedFields() throws NoSuchFieldException, IllegalAccessException {
		FieldMappingResponse response = new FieldMappingResponse();
		response.setFieldName("rootCauseIdentifier");
		response.setOriginalValue("CustomField");
		response.setPreviousValue("");

		FieldMappingResponse response2 = new FieldMappingResponse();
		response2.setFieldName("rootCause");
		response2.setOriginalValue("CustomField_123");
		response2.setPreviousValue("");
		FieldMappingMeta fieldMappingMeta = new FieldMappingMeta();
		fieldMappingMeta.setFieldMappingRequests(Arrays.asList(response, response2));

		FieldMappingHelper.generateHistoryForNestedFields(List.of(response), response, fieldMappingStructureList.get(0),
				scrumFieldMapping);
	}

	@Test
	void testSetMappingResponseWithGeneratedField() throws NoSuchFieldException, IllegalAccessException {
		FieldMappingHelper.setMappingResponseWithGeneratedField(new FieldMappingResponse("fieldName", "originalValue",
				"previousValue", List.of(new ConfigurationHistoryChangeLog("changedFrom", "changedTo", "changedBy",
						"updatedOn", "releaseNodeId"))),
				scrumFieldMapping);
	}

	@Test
	void testSetFieldMappingResponse() throws NoSuchFieldException, IllegalAccessException {
		FieldMappingHelper.setFieldMappingResponse(new FieldMappingResponse("sprintName", "originalValue",
				"previousValue", List.of(new ConfigurationHistoryChangeLog("changedFrom", "changedTo", "changedBy",
						"updatedOn", "releaseNodeId"))),
				scrumFieldMapping, null);
	}

	@Test
	void testSetNodeSpecificFields() throws NoSuchFieldException, IllegalAccessException {
		FieldMappingResponse response = new FieldMappingResponse();
		response.setFieldName("rootCauseIdentifier");
		response.setOriginalValue("CustomField");
		response.setPreviousValue("");
		FieldMappingHelper.setNodeSpecificFields(fieldMappingStructureList.get(0), response, scrumFieldMapping,
				"nodeId", null);
	}

	@Test
	void testRemoveDuplicateFieldsFromResponse() {
		FieldMappingResponse response = new FieldMappingResponse();
		response.setFieldName("rootCauseIdentifier");
		response.setOriginalValue("CustomField");
		response.setPreviousValue("");

		FieldMappingResponse response3 = new FieldMappingResponse();
		response3.setFieldName("rootCauseIdentifier");
		response3.setOriginalValue("CustomField");
		response3.setPreviousValue("Labels");

		FieldMappingResponse response2 = new FieldMappingResponse();
		response2.setFieldName("rootCause");
		response2.setOriginalValue("CustomField_123");
		response2.setPreviousValue("");
		FieldMappingMeta fieldMappingMeta = new FieldMappingMeta();
		fieldMappingMeta.setFieldMappingRequests(Arrays.asList(response, response2, response3));

		Map<String, FieldMappingResponse> responseHashMap = new HashMap<>();
		FieldMappingHelper.removeDuplicateFieldsFromResponse(Arrays.asList(response, response2, response3),
				responseHashMap);
		Assertions.assertEquals(2, responseHashMap.size());
	}

	@Test
	void testRemoveDuplicateFieldsFromResponse_PreviousValuePresent() {
		FieldMappingResponse response = new FieldMappingResponse();
		response.setFieldName("rootCauseIdentifier");
		response.setOriginalValue("CustomField");
		response.setPreviousValue("Labels");

		FieldMappingResponse response3 = new FieldMappingResponse();
		response3.setFieldName("rootCauseIdentifier");
		response3.setOriginalValue("CustomField");
		response3.setPreviousValue("");

		FieldMappingResponse response2 = new FieldMappingResponse();
		response2.setFieldName("rootCause");
		response2.setOriginalValue("CustomField_123");
		response2.setPreviousValue("");
		FieldMappingMeta fieldMappingMeta = new FieldMappingMeta();
		fieldMappingMeta.setFieldMappingRequests(Arrays.asList(response, response2, response3));

		Map<String, FieldMappingResponse> responseHashMap = new HashMap<>();
		FieldMappingHelper.removeDuplicateFieldsFromResponse(Arrays.asList(response, response2, response3),
				responseHashMap);
		Assertions.assertEquals(2, responseHashMap.size());
	}

	@Test
	void testCreateHistoryChangeLog() {
		ConfigurationHistoryChangeLog result = FieldMappingHelper
				.createHistoryChangeLog(
						new FieldMappingMeta(
								List.of(new FieldMappingResponse("fieldName", "originalValue", "previousValue",
										List.of(new ConfigurationHistoryChangeLog("changedFrom", "changedTo",
												"changedBy", "updatedOn", "releaseNodeId")))),
								List.of(new FieldMappingResponse("fieldName", "originalValue", "previousValue",
										List.of(new ConfigurationHistoryChangeLog("changedFrom", "changedTo",
												"changedBy", "updatedOn", "releaseNodeId")))),
								"metaTemplateCode", "releaseNodeId"),
						new FieldMappingResponse("fieldName", "originalValue", "previousValue",
								List.of(new ConfigurationHistoryChangeLog("changedFrom", "changedTo", "changedBy",
										"updatedOn", "releaseNodeId"))),
						fieldMappingStructureList.get(0), "loggedInUser");
		Assertions.assertNull(result.getReleaseNodeId());
	}

	@Test
	void testCreateHistoryChangeLog_NodeSpecific() {
		FieldMappingStructure fieldMappingStructure = fieldMappingStructureList.get(0);
		fieldMappingStructure.setNodeSpecific(true);
		ConfigurationHistoryChangeLog result = FieldMappingHelper.createHistoryChangeLog(
				new FieldMappingMeta(
						List.of(new FieldMappingResponse("fieldName", "originalValue", "previousValue",
								List.of(new ConfigurationHistoryChangeLog("changedFrom", "changedTo", "changedBy",
										"updatedOn", "releaseNodeId")))),
						List.of(new FieldMappingResponse("fieldName", "originalValue", "previousValue",
								List.of(new ConfigurationHistoryChangeLog("changedFrom", "changedTo", "changedBy",
										"updatedOn", "releaseNodeId")))),
						"metaTemplateCode", "releaseNodeId"),
				new FieldMappingResponse("fieldName", "originalValue", "previousValue",
						List.of(new ConfigurationHistoryChangeLog("changedFrom", "changedTo", "changedBy", "updatedOn",
								"releaseNodeId"))),
				fieldMappingStructure, "loggedInUser");
		Assertions.assertEquals("releaseNodeId", result.getReleaseNodeId());
	}

	@Test
	void testSetNodeSpecificField() throws NoSuchFieldException, IllegalAccessException {
		FieldMappingResponse mappingResponse = new FieldMappingResponse();
		mappingResponse.setFieldName("startDateCountKPI150");
		mappingResponse.setOriginalValue(17);
		mappingResponse.setPreviousValue(0);
		FieldMappingStructure startDateCountKPI150 = fieldMappingStructureList.stream().filter(
				fieldMappingStructure -> fieldMappingStructure.getFieldName().equalsIgnoreCase("startDateCountKPI150"))
				.toList().get(0);
		Update update = new Update();
		FieldMappingHelper.setNodeSpecificFields(startDateCountKPI150, mappingResponse, scrumFieldMapping, "node1",
				update);
		Assertions.assertNotNull(update);
	}

	@Test
	void testSetNodeSpecificField_WithHistory() throws NoSuchFieldException, IllegalAccessException {
		FieldMappingResponse mappingResponse = new FieldMappingResponse();
		mappingResponse.setFieldName("startDateCountKPI150");
		mappingResponse.setOriginalValue(17);
		mappingResponse.setPreviousValue(0);
		FieldMappingStructure startDateCountKPI150 = fieldMappingStructureList.stream().filter(
				fieldMappingStructure -> fieldMappingStructure.getFieldName().equalsIgnoreCase("startDateCountKPI150"))
				.toList().get(0);
		ConfigurationHistoryChangeLog configurationHistoryChangeLog = new ConfigurationHistoryChangeLog();
		configurationHistoryChangeLog.setChangedTo(78);
		configurationHistoryChangeLog.setChangedFrom("");
		configurationHistoryChangeLog.setChangedBy("changedBy");
		configurationHistoryChangeLog.setUpdatedOn(java.time.LocalDateTime.now().toString());
		configurationHistoryChangeLog.setReleaseNodeId("node1");
		scrumFieldMapping.setHistorystartDateCountKPI150(List.of(configurationHistoryChangeLog));
		Update update = new Update();
		FieldMappingHelper.setNodeSpecificFields(startDateCountKPI150, mappingResponse, scrumFieldMapping, "node1",
				update);
		Assertions.assertNotNull(update);
	}
}
