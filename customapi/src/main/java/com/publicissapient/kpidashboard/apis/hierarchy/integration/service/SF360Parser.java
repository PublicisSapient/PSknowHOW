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

package com.publicissapient.kpidashboard.apis.hierarchy.integration.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.publicissapient.kpidashboard.apis.hierarchy.integration.dto.HierarchyDetails;

public class SF360Parser implements HierarchyDetailParser {

    @Override
    public HierarchyDetails convertToHierachyDetail(String jsonResponse) {
        ObjectMapper objectMapper = new ObjectMapper().enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        try (JsonParser parser = objectMapper.createParser(jsonResponse)) {
            JsonNode rootNode = objectMapper.readTree(parser);
            JsonNode hierarchyDetailsNode = rootNode.path("data").get(0).path("hierarchyDetails");
            return objectMapper.treeToValue(hierarchyDetailsNode, HierarchyDetails.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse hierarchy details due to " + e.getMessage(), e);
        }
    }

}
