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

package com.publicissapient.kpidashboard.apis.hierarchy.integeration.service;

import java.util.List;
import java.util.Map;

import org.modelmapper.ModelMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.publicissapient.kpidashboard.apis.hierarchy.integeration.dto.HierarchyDetails;

public class SF360Parser implements HierarchyDetailParser {

	@Override
	public void convertToHierachyDetail(String jsonResponse) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			Map<String, Object> jsonMap = objectMapper.readValue(jsonResponse, new TypeReference<>() {
			});
			ModelMapper modelMapper = new ModelMapper();
			List<Map<String, Object>> list = (List<Map<String, Object>>) jsonMap.get("data");
			HierarchyDetails hierarchyDetails = modelMapper.map(list.get(0).get("hierarchyDetails"),
					HierarchyDetails.class);

		} catch (JsonMappingException e) {
			throw new RuntimeException(e);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
