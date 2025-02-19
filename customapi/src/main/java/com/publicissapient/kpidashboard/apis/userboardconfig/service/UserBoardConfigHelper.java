/*
 *   Copyright 2014 CapitalOne, LLC.
 *   Further development Copyright 2022 Sapient Corporation.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.publicissapient.kpidashboard.apis.userboardconfig.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.owasp.encoder.Encode;

import com.publicissapient.kpidashboard.apis.enums.UserBoardConfigEnum;
import com.publicissapient.kpidashboard.common.model.application.KpiCategory;
import com.publicissapient.kpidashboard.common.model.application.KpiMaster;
import com.publicissapient.kpidashboard.common.model.userboardconfig.BoardDTO;
import com.publicissapient.kpidashboard.common.model.userboardconfig.BoardKpis;
import com.publicissapient.kpidashboard.common.model.userboardconfig.BoardKpisDTO;
import com.publicissapient.kpidashboard.common.model.userboardconfig.ProjectListRequested;
import com.publicissapient.kpidashboard.common.model.userboardconfig.UserBoardConfig;
import com.publicissapient.kpidashboard.common.model.userboardconfig.UserBoardConfigDTO;

import lombok.extern.slf4j.Slf4j;

/**
 * The Class UserBoardConfigHelper.
 *
 * @author shunaray
 */
@Slf4j
public final class UserBoardConfigHelper {

	private UserBoardConfigHelper() {
	}

	/**
	 * Checks if any KPI is added or removed for an existing user by comparing the
	 * user's KPI list with the KPI master list.
	 *
	 * @param existingUserBoardConfig
	 *          the existing user board configuration
	 * @param kpiMasterMap
	 *          a map of KPI master data
	 * @return true if there is a difference in the number of KPIs between the
	 *         user's configuration and the master list, false otherwise
	 */
	public static boolean checkKPIAddOrRemoveForExistingUser(UserBoardConfigDTO existingUserBoardConfig,
			Map<String, KpiMaster> kpiMasterMap) {
		Set<String> userKpiIdList = new HashSet<>();
		getKpiIdListFromExistingUser(existingUserBoardConfig.getScrum(), userKpiIdList);
		getKpiIdListFromExistingUser(existingUserBoardConfig.getKanban(), userKpiIdList);
		getKpiIdListFromExistingUser(existingUserBoardConfig.getOthers(), userKpiIdList);

		Set<String> kpiMasterKpiIdList = kpiMasterMap.keySet();
		if (kpiMasterKpiIdList.size() > userKpiIdList.size()) {
			return !CollectionUtils.containsAll(userKpiIdList, kpiMasterKpiIdList);
		} else if (kpiMasterKpiIdList.size() < userKpiIdList.size()) {
			return !CollectionUtils.containsAll(kpiMasterKpiIdList, userKpiIdList);
		} else {
			return false;
		}
	}

	/**
	 * Extracts KPI IDs from the existing user board configuration and adds them to
	 * the provided set.
	 *
	 * @param existingUserBoardConfig
	 *          the list of board configurations for the existing user
	 * @param userKpiIdList
	 *          the set to which the extracted KPI IDs will be added
	 */
	public static void getKpiIdListFromExistingUser(List<BoardDTO> existingUserBoardConfig, Set<String> userKpiIdList) {
		existingUserBoardConfig.forEach(kpiBoard -> {
			kpiBoard.getKpis().removeIf(Objects::isNull);
			Optional.ofNullable(kpiBoard.getKpis()).get().stream().filter(Objects::nonNull)
					.forEach(boardKpisDTO -> userKpiIdList.add(Optional.ofNullable(boardKpisDTO.getKpiId()).get()));
		});
	}

	/**
	 * Checks if any default KPI categories are absent in the existing user board
	 * configuration.
	 *
	 * @param existingUserBoardConfigDTO
	 *          the existing user board configuration
	 * @param kpiCategoryList
	 *          a list of KPI categories
	 * @return true if any default KPI categories are absent in the existing user
	 *         board configuration, false otherwise
	 */
	public static boolean checkCategories(UserBoardConfigDTO existingUserBoardConfigDTO,
			List<KpiCategory> kpiCategoryList) {

		Stream<List<BoardDTO>> existingUserBoardStreamList = Stream.of(existingUserBoardConfigDTO.getScrum(),
				existingUserBoardConfigDTO.getKanban(), existingUserBoardConfigDTO.getOthers());

		Set<String> existingCategories = existingUserBoardStreamList
				.flatMap(boardList -> boardList.stream().map(BoardDTO::getBoardName)).collect(Collectors.toSet());

		Set<String> defaultKpiCategories = kpiCategoryList.stream().map(KpiCategory::getCategoryName)
				.collect(Collectors.toSet());
		defaultKpiCategories.addAll(UserBoardConfigEnum.SCRUM_KANBAN_BOARD.getBoardName());
		defaultKpiCategories.addAll(UserBoardConfigEnum.OTHER_BOARD.getBoardName());

		return !defaultKpiCategories.containsAll(existingCategories);
	}

	/**
	 * Checks if any board subcategory is added or removed for an existing user.
	 *
	 * @param existingUserBoardConfigDTO
	 *          the existing user board configuration
	 * @param kpiMasterMap
	 *          a map of KPI master data
	 * @return true if the number of subcategories in the KPI master data does not
	 *         match the number of subcategories in the existing user board
	 *         configuration, false otherwise
	 */
	public static boolean checkKPISubCategory(UserBoardConfigDTO existingUserBoardConfigDTO,
			Map<String, KpiMaster> kpiMasterMap) {

		Set<String> existingUserSubCategories = Stream
				.of(existingUserBoardConfigDTO.getOthers(), existingUserBoardConfigDTO.getScrum(),
						existingUserBoardConfigDTO.getKanban())
				.flatMap(boardList -> boardList.stream()
						.flatMap(boardDTO -> boardDTO.getKpis().stream().map(BoardKpisDTO::getSubCategoryBoard)))
				.filter(Objects::nonNull).collect(Collectors.toSet());

		Set<String> kpiMasterSubCategories = kpiMasterMap.values().stream().map(KpiMaster::getKpiSubCategory)
				.filter(Objects::nonNull).collect(Collectors.toSet());

		return (kpiMasterSubCategories.size() != existingUserSubCategories.size());
	}

	/**
	 * Updates the visibility of KPIs in the user board config based on selected
	 * projects board config
	 *
	 * @param userBoardConfig
	 *          user board config
	 * @param projectBoardConfigs
	 *          selected project board configs
	 */
	public static void applyProjectConfigToUserBoard(UserBoardConfigDTO userBoardConfig,
			ProjectListRequested listOfRequestedProj, List<UserBoardConfig> projectBoardConfigs) {
		if (CollectionUtils.isEmpty(projectBoardConfigs)) {
			return;
		}

		Map<String, Boolean> kpiWiseIsShownFlag = projectBoardConfigs.stream()
				.flatMap(config -> Stream.of(config.getScrum(), config.getKanban(), config.getOthers())
						.flatMap(Collection::stream).flatMap(board -> board.getKpis().stream()))
				.filter(kpi -> !kpi.isShown()).collect(Collectors.toMap(BoardKpis::getKpiId, kpi -> false, (a, b) -> a && b));

		log.debug("Applying project configuration: Disabled KPIs {} for user {} with selected project IDs {}",
				kpiWiseIsShownFlag, userBoardConfig.getUsername(),
				listOfRequestedProj.getBasicProjectConfigIds().stream().map(Encode::forJava).toList());

		Stream.of(userBoardConfig.getScrum(), userBoardConfig.getKanban(), userBoardConfig.getOthers())
				.flatMap(Collection::stream).forEach(boardDTO -> boardDTO.getKpis().forEach(boardKpis -> {
					boolean isShown = kpiWiseIsShownFlag.getOrDefault(boardKpis.getKpiId(), true);
					boardKpis.setShown(isShown);
				}));
	}
}
