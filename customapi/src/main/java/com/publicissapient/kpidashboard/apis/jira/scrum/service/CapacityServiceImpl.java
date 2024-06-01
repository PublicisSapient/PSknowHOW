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

	private CapacityKpiData getCapacityDataForAdditionalFilter(KpiRequest kpiRequest, CapacityKpiData capacityKpiData) {
		Map<String, AdditionalFilterCategory> addFilterCategory = filterHelperService
				.getAdditionalFilterHierarchyLevel().entrySet().stream()
				.collect(Collectors.toMap(entry -> entry.getKey().toUpperCase(), Map.Entry::getValue));
		boolean additionalCapacity = false;
		Double capacity = 0.0D;
		for (Map.Entry<String, List<String>> entry : kpiRequest.getSelectedMap().entrySet()) {
			String key = entry.getKey();
			List<String> value = entry.getValue();
			if (CollectionUtils.isNotEmpty(value) && addFilterCategory.containsKey(key.toUpperCase())
					&& ObjectUtils.isNotEmpty(capacityKpiData)) {
				List<AdditionalFilterCapacity> additionalFilterCapacityList = capacityKpiData
						.getAdditionalFilterCapacityList();
				if (CollectionUtils.isNotEmpty(additionalFilterCapacityList)) {
					additionalCapacity = true;
					String upperCaseKey = key.toUpperCase();
					List<String> additionalFilter = new ArrayList<>(value);
					capacity += additionalFilterCapacityList.stream()
							.filter(additionalFilterCapacity -> upperCaseKey
									.equals(additionalFilterCapacity.getFilterId().toUpperCase()))
							.flatMap(
									additionalFilterCapacity -> additionalFilterCapacity.getNodeCapacityList().stream())
							.filter(leaf -> additionalFilter.contains(leaf.getAdditionalFilterId()) && leaf.getAdditionalFilterCapacity()!=null)
							.mapToDouble(LeafNodeCapacity::getAdditionalFilterCapacity).sum();
				}
			}

		}
		if (additionalCapacity) {
			CapacityKpiData newCapacity = new CapacityKpiData();
			newCapacity.setBasicProjectConfigId(capacityKpiData.getBasicProjectConfigId());
			newCapacity.setSprintID(capacityKpiData.getSprintID());
			newCapacity.setProjectName(capacityKpiData.getProjectName());
			newCapacity.setProjectId(capacityKpiData.getProjectId());
			newCapacity.setCapacityPerSprint(capacity);
			return newCapacity;
		}

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
