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

package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.filter.service.FilterHelperService;
import com.publicissapient.kpidashboard.apis.jira.service.iterationdashboard.JiraIterationKPIService;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterCapacity;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterCategory;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.LeafNodeCapacity;
import com.publicissapient.kpidashboard.common.model.excel.CapacityKpiData;
import com.publicissapient.kpidashboard.common.repository.excel.CapacityKpiDataRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CapacityServiceImpl extends JiraIterationKPIService {

	private static final String CAPACITY_DATA = "Capacity";

	@Autowired
	private CapacityKpiDataRepository capacityKpiDataRepository;

	@Autowired
	private FilterHelperService filterHelperService;

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement, Node filteredNode)
			throws ApplicationException {
		DataCount trendValue = new DataCount();
		projectWiseLeafNodeValue(filteredNode, trendValue, kpiElement, kpiRequest);
		return kpiElement;
	}

	@Override
	public String getQualifierType() {
		return KPICode.CAPACITY.name();
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(Node leafNode, String startDate, String endDate,
			KpiRequest kpiRequest) {
		Map<String, Object> resultListMap = new HashMap<>();
		if (null != leafNode) {
			log.info("Capacity -> Requested sprint : {}", leafNode.getName());
			ObjectId basicProjectConfigId = leafNode.getProjectFilter().getBasicProjectConfigId();

			String sprintId = leafNode.getSprintFilter().getId();

			CapacityKpiData capacityKpiData = capacityKpiDataRepository.findBySprintIDAndBasicProjectConfigId(sprintId,
					basicProjectConfigId);

			resultListMap.put(CAPACITY_DATA, getCapacityDataForAdditionalFilter(kpiRequest, capacityKpiData));
		}
		return resultListMap;
	}

	/**
	 * Retrieves the capacity data for an additional filter based on the given KPI
	 * request.
	 *
	 * @param kpiRequest
	 *          the KPI request containing selected filters
	 * @param capacityKpiData
	 *          the current capacity KPI data
	 * @return a new CapacityKpiData object with updated capacity per sprint if
	 *         additional capacity was found, otherwise returns the original
	 *         capacityKpiData
	 */
	private CapacityKpiData getCapacityDataForAdditionalFilter(KpiRequest kpiRequest, CapacityKpiData capacityKpiData) {
		// Create a map of additional filter categories with keys in uppercase for
		// case-insensitive matching
		Map<String, AdditionalFilterCategory> addFilterCategory = filterHelperService.getAdditionalFilterHierarchyLevel()
				.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().toUpperCase(), Map.Entry::getValue));

		boolean additionalCapacity = false;
		Double capacity = 0.0D;

		// Iterate through the selected filters in the KPI request
		for (Map.Entry<String, List<String>> entry : kpiRequest.getSelectedMap().entrySet()) {
			String key = entry.getKey();
			List<String> value = entry.getValue();

			// Check if the filter has a non-empty value list, exists in the additional
			// filter categories, and capacity data is not empty
			if (CollectionUtils.isNotEmpty(value) && addFilterCategory.containsKey(key.toUpperCase()) &&
					ObjectUtils.isNotEmpty(capacityKpiData)) {

				List<AdditionalFilterCapacity> additionalFilterCapacityList = capacityKpiData.getAdditionalFilterCapacityList();

				// If there are additional filter capacities, calculate the total capacity
				if (CollectionUtils.isNotEmpty(additionalFilterCapacityList)) {
					additionalCapacity = true;
					String upperCaseKey = key.toUpperCase();
					List<String> additionalFilter = new ArrayList<>(value);

					// Sum the capacities matching the filter criteria
					capacity += additionalFilterCapacityList.stream()
							.filter(
									additionalFilterCapacity -> upperCaseKey.equals(additionalFilterCapacity.getFilterId().toUpperCase()))
							.flatMap(additionalFilterCapacity -> additionalFilterCapacity.getNodeCapacityList().stream())
							.filter(leaf -> additionalFilter.contains(leaf.getAdditionalFilterId()) &&
									leaf.getAdditionalFilterCapacity() != null)
							.mapToDouble(LeafNodeCapacity::getAdditionalFilterCapacity).sum();
				}
			}
		}

		// If additional capacity was found, create a new CapacityKpiData object with
		// the updated capacity to not hamper the further calculations
		if (additionalCapacity) {
			CapacityKpiData newCapacity = new CapacityKpiData();
			newCapacity.setBasicProjectConfigId(capacityKpiData.getBasicProjectConfigId());
			newCapacity.setSprintID(capacityKpiData.getSprintID());
			newCapacity.setProjectName(capacityKpiData.getProjectName());
			newCapacity.setProjectId(capacityKpiData.getProjectId());
			newCapacity.setCapacityPerSprint(capacity);
			return newCapacity;
		}

		// If no additional capacity was found, return the original capacity data
		return capacityKpiData;
	}

	/**
	 * Populates KPI value to sprint leaf nodes and gives the trend analysis at
	 * sprint level.
	 *
	 * @param sprintLeafNode
	 * @param trendValue
	 * @param kpiElement
	 * @param kpiRequest
	 */
	private void projectWiseLeafNodeValue(Node sprintLeafNode, DataCount trendValue, KpiElement kpiElement,
			KpiRequest kpiRequest) {
		String requestTrackerId = getRequestTrackerId();

		Map<String, Object> resultMap = fetchKPIDataFromDb(sprintLeafNode, null, null, kpiRequest);
		CapacityKpiData capacityKpiData = (CapacityKpiData) resultMap.get(CAPACITY_DATA);
		if (null != capacityKpiData) {
			log.info("Capacity -> request id : {} Project Name : {}  Sprint Id : {}", requestTrackerId,
					capacityKpiData.getProjectName(), capacityKpiData.getSprintID());
			kpiElement.setSprint(Objects.requireNonNull(sprintLeafNode).getName());
			trendValue.setValue(capacityKpiData.getCapacityPerSprint());
			kpiElement.setTrendValueList(trendValue);
		}
	}
}
