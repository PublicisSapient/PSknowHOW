/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.apis.jira.scrum.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.enums.Filters;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.jira.service.JiraKPIService;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.common.model.ProjectSprintDetails;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.dto.HierarchyValueDTO;
import com.publicissapient.kpidashboard.common.model.application.dto.ProjectBasicConfigDTO;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SprintGoalServiceImpl extends JiraKPIService<Double, List<Object>, Map<String, Object>> {

	private static final String SPRINT_DETAILS = "sprintDetails";

	private final SprintRepository sprintRepository;
	private final ConfigHelperService configHelperService;

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		List<DataCount> trendValueList = new ArrayList<>();
		Node root = treeAggregatorDetail.getRoot();
		Map<String, Node> mapTmp = treeAggregatorDetail.getMapTmp();

		treeAggregatorDetail.getMapOfListOfLeafNodes().forEach((k, v) -> {
			Filters filters = Filters.getFilter(k);
			if (Filters.SPRINT == filters) {
				sprintWiseLeafNodeValue(mapTmp, v, trendValueList, kpiElement, kpiRequest);
			}
		});

		log.debug("[SPRINT-GOAL-LEAF-NODE-VALUE][{}]. Values of leaf node after KPI calculation {}",
				kpiRequest.getRequestTrackerId(), root);

		return kpiElement;
	}

	@SuppressWarnings("unchecked")
	private void sprintWiseLeafNodeValue(Map<String, Node> mapTmp, List<Node> sprintLeafNodeList,
			List<DataCount> trendValueList, KpiElement kpiElement, KpiRequest kpiRequest) {

		sprintLeafNodeList.sort(Comparator.comparing(node -> node.getSprintFilter().getStartDate()));

		Map<String, Object> resultMap = fetchKPIDataFromDb(sprintLeafNodeList, null, null, kpiRequest);

		Map<Pair<String, String>, String> sprintWiseGoals = new HashMap<>();

		List<SprintDetails> sprintDetails = (List<SprintDetails>) resultMap.get(SPRINT_DETAILS);

		if (CollectionUtils.isNotEmpty(sprintDetails)) {
			sprintDetails.forEach(sd -> sprintWiseGoals
					.put(Pair.of(sd.getBasicProjectConfigId().toString(), sd.getSprintID()), sd.getGoal()));
		}

		final Map<ObjectId, List<SprintDetails>> projectWiseSprints = sprintDetails.stream()
				.collect(Collectors.groupingBy(SprintDetails::getBasicProjectConfigId));

		Map<String, ProjectSprintDetails> projectSprintDetailsMap = new HashMap<>();

		for (Node node : sprintLeafNodeList) {

			// Leaf node wise data
			String projectName = node.getProjectFilter().getName();
			String projectId = node.getProjectFilter().getBasicProjectConfigId().toString();

			ProjectSprintDetails projectSprintDetails = projectSprintDetailsMap.get(projectId);
			if (projectSprintDetails == null) {
				projectSprintDetails = new ProjectSprintDetails();
				projectSprintDetails.setName(projectName);
				projectSprintDetails.setProjectId(projectId);

				ProjectBasicConfig projectConfig = configHelperService.getProjectConfig(projectId);
				ProjectBasicConfigDTO projectBasicDTO = null;
				if (projectConfig != null) {
					ModelMapper mapper = new ModelMapper();
					projectBasicDTO = mapper.map(projectConfig, ProjectBasicConfigDTO.class);
				}
				assert projectBasicDTO != null;
				List<HierarchyValueDTO> projectHierarchy = projectBasicDTO.getHierarchy();
				projectSprintDetails.setHierarchy(projectHierarchy);

				projectSprintDetailsMap.put(projectId, projectSprintDetails);
			}

			// Populate sprint details
			Set<ProjectSprintDetails.SprintDTO> sprintDetailSet = projectSprintDetails.getSprintGoals();
			if (sprintDetailSet == null) {
				sprintDetailSet = new HashSet<>();
				projectSprintDetails.setSprintGoals(sprintDetailSet);
			}

			List<SprintDetails> sprints = projectWiseSprints.get(new ObjectId(projectId));
			if (sprints != null) {
				for (SprintDetails sprint : sprints) {
					ProjectSprintDetails.SprintDTO sprintDetail = new ProjectSprintDetails.SprintDTO();
					sprintDetail.setName(sprint.getSprintName());
					sprintDetail.setSprintId(sprint.getSprintID());
					sprintDetail.setGoal(sprint.getGoal());
					sprintDetailSet.add(sprintDetail);
				}
			}
		}
		kpiElement.setTrendValueList(new ArrayList<>(projectSprintDetailsMap.values()));
		log.debug(trendValueList.toString());
		log.debug(mapTmp.toString());
	}

	@Override
	public Map<String, Object> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {

		List<String> sprintList = new ArrayList<>();
		Map<String, Object> resultListMap = new HashMap<>();
		leafNodeList.forEach(leaf -> sprintList.add(leaf.getSprintFilter().getId()));
		List<SprintDetails> sprintDetails = sprintRepository.findBySprintIDInWithFieldsSorted(sprintList);
		resultListMap.put(SPRINT_DETAILS, sprintDetails);
		return resultListMap;
	}

	@Override
	public Double calculateKPIMetrics(Map<String, Object> stringObjectMap) {
		return null;
	}

	@Override
	public String getQualifierType() {
		return KPICode.SPRINT_GOALS.name();
	}

}
