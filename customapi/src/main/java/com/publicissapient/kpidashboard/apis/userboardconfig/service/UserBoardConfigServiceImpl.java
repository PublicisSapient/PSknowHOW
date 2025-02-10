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

package com.publicissapient.kpidashboard.apis.userboardconfig.service;

import static com.publicissapient.kpidashboard.apis.userboardconfig.service.UserBoardConfigHelper.applyProjectConfigToUserBoard;
import static com.publicissapient.kpidashboard.apis.userboardconfig.service.UserBoardConfigHelper.checkCategories;
import static com.publicissapient.kpidashboard.apis.userboardconfig.service.UserBoardConfigHelper.checkKPIAddOrRemoveForExistingUser;
import static com.publicissapient.kpidashboard.apis.userboardconfig.service.UserBoardConfigHelper.checkKPISubCategory;
import static com.publicissapient.kpidashboard.apis.userboardconfig.service.UserBoardConfigHelper.sanitizeProjectIds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.ObjIntConsumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.auth.service.AuthenticationService;
import com.publicissapient.kpidashboard.apis.common.service.CacheService;
import com.publicissapient.kpidashboard.apis.enums.UserBoardConfigEnum;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterCategory;
import com.publicissapient.kpidashboard.common.model.application.Filters;
import com.publicissapient.kpidashboard.common.model.application.KpiCategory;
import com.publicissapient.kpidashboard.common.model.application.KpiCategoryMapping;
import com.publicissapient.kpidashboard.common.model.application.KpiMaster;
import com.publicissapient.kpidashboard.common.model.userboardconfig.BoardDTO;
import com.publicissapient.kpidashboard.common.model.userboardconfig.BoardKpisDTO;
import com.publicissapient.kpidashboard.common.model.userboardconfig.ConfigLevel;
import com.publicissapient.kpidashboard.common.model.userboardconfig.ProjectListRequested;
import com.publicissapient.kpidashboard.common.model.userboardconfig.UserBoardConfig;
import com.publicissapient.kpidashboard.common.model.userboardconfig.UserBoardConfigDTO;
import com.publicissapient.kpidashboard.common.repository.application.KpiCategoryMappingRepository;
import com.publicissapient.kpidashboard.common.repository.application.KpiCategoryRepository;
import com.publicissapient.kpidashboard.common.repository.application.KpiMasterRepository;
import com.publicissapient.kpidashboard.common.repository.userboardconfig.UserBoardConfigRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementation class for UserBoardConfigService
 *
 * @author narsingh9
 */
@Service
@Slf4j
public class UserBoardConfigServiceImpl implements UserBoardConfigService {

	private static final String ITERATION = "Iteration";
	private static final String DEFAULT_BOARD_NAME = "My KnowHOW";
	private boolean handleDeveloperKpi = false;
	@Autowired
	private UserBoardConfigRepository userBoardConfigRepository;
	@Autowired
	private AuthenticationService authenticationService;
	@Autowired
	private KpiMasterRepository kpiMasterRepository;
	@Autowired
	private KpiCategoryRepository kpiCategoryRepository;
	@Autowired
	private KpiCategoryMappingRepository kpiCategoryMappingRepository;
	@Autowired
	private ConfigHelperService configHelperService;
	@Autowired
	private CacheService cacheService;
	@Autowired
	private UserBoardConfigMapper userBoardConfigMapper;

	/**
	 * Retrieves or prepares the user board configuration based on
	 * {@link ConfigLevel} and basicConfigId. If no existing configuration is found,
	 * it prepares a default configuration.
	 *
	 * @param configLevel
	 *            the configuration level (see {@link ConfigLevel})
	 * @param basicProjectConfigId
	 *            basicProjectConfigId
	 * @return the user board configuration DTO
	 */
	public UserBoardConfigDTO getOrPrepareBoardConfig(ConfigLevel configLevel, String basicProjectConfigId) {
		final String loggedInUser = authenticationService.getLoggedInUser();

		String configId = configLevel == ConfigLevel.PROJECT ? basicProjectConfigId : null;
		String username = configLevel == ConfigLevel.USER ? loggedInUser : null;
		UserBoardConfig existingUserBoardConfig = fetchUserBoardConfig(configId, username);

		UserBoardConfigDTO existingUserBoardConfigDTO = userBoardConfigMapper.toDto(existingUserBoardConfig);
		Iterable<KpiMaster> allKPIs = configHelperService.loadKpiMaster();
		Map<String, KpiMaster> kpiMasterMap = StreamSupport.stream(allKPIs.spliterator(), false)
				.collect(Collectors.toMap(KpiMaster::getKpiId, Function.identity()));
		List<KpiCategory> kpiCategoryList = kpiCategoryRepository.findAll();
		UserBoardConfigDTO defaultUserBoardConfigDTO = new UserBoardConfigDTO();
		defaultUserBoardConfigDTO
				.setBasicProjectConfigId(configLevel == ConfigLevel.PROJECT ? basicProjectConfigId : null);
		defaultUserBoardConfigDTO.setUsername(configLevel == ConfigLevel.USER ? loggedInUser : null);

		handleDeveloperKpi = configHelperService.getProjectConfig(basicProjectConfigId) != null
				&& configHelperService.getProjectConfig(basicProjectConfigId).isDeveloperKpiEnabled();

		if (null == existingUserBoardConfigDTO) {
			setUserBoardConfigBasedOnCategoryForFreshUser(defaultUserBoardConfigDTO, kpiCategoryList, kpiMasterMap);
			return defaultUserBoardConfigDTO;
		} else {
			String boardName = existingUserBoardConfigDTO.getScrum().stream()
					.filter(boardDTO -> boardDTO.getBoardId() == 1).findFirst().map(BoardDTO::getBoardName)
					.orElse(null);
			if ((checkKPIAddOrRemoveForExistingUser(existingUserBoardConfigDTO, kpiMasterMap)
					&& checkCategories(existingUserBoardConfigDTO, kpiCategoryList))
					|| checkKPISubCategory(existingUserBoardConfigDTO, kpiMasterMap)) {
				setUserBoardConfigBasedOnCategory(boardName, defaultUserBoardConfigDTO, kpiCategoryList, kpiMasterMap);
				filtersBoardsAndSetKpisForExistingUser(existingUserBoardConfigDTO.getScrum(),
						defaultUserBoardConfigDTO.getScrum());
				filtersBoardsAndSetKpisForExistingUser(existingUserBoardConfigDTO.getKanban(),
						defaultUserBoardConfigDTO.getKanban());
				filtersBoardsAndSetKpisForExistingUser(existingUserBoardConfigDTO.getOthers(),
						defaultUserBoardConfigDTO.getOthers());
				return defaultUserBoardConfigDTO;
			}
			filterKpis(existingUserBoardConfigDTO, kpiMasterMap);
			return existingUserBoardConfigDTO;
		}
	}

	@Override
	public UserBoardConfigDTO getBoardConfig(ConfigLevel configLevel, ProjectListRequested listOfRequestedProj) {
		String basicProjectConfigId = listOfRequestedProj.getBasicProjectConfigIds().stream().findFirst().orElse(null);
		UserBoardConfigDTO boardConfigDTO;

		switch (configLevel) {
		case PROJECT:
			boardConfigDTO = getOrPrepareBoardConfig(ConfigLevel.PROJECT, basicProjectConfigId);
			break;
		case USER:
			boardConfigDTO = getOrPrepareBoardConfig(ConfigLevel.USER, basicProjectConfigId);
			List<UserBoardConfig> projectBoardConfigs = listOfRequestedProj.getBasicProjectConfigIds().stream()
					.map(projectId -> fetchUserBoardConfig(projectId, null)).filter(Objects::nonNull)
					.collect(Collectors.toList());
			applyProjectConfigToUserBoard(boardConfigDTO, listOfRequestedProj, projectBoardConfigs);
			break;
		default:
			throw new IllegalArgumentException("Invalid board config level: " + configLevel);
		}

		return boardConfigDTO;
	}

	private void setUserBoardConfigBasedOnCategoryForFreshUser(UserBoardConfigDTO defaultUserBoardConfigDTO,
			List<KpiCategory> kpiCategoryList, Map<String, KpiMaster> kpiMasterMap) {
		setUserBoardConfigBasedOnCategory(DEFAULT_BOARD_NAME, defaultUserBoardConfigDTO, kpiCategoryList, kpiMasterMap);
	}

	/**
	 * compare existing user board and default user board (KpiMaster). checked any
	 * new added or remove kpi
	 *
	 * @param existingBoardListDTO
	 *            existingBoardListDTO
	 * @param defaultBoardListDTO
	 *            existingBoardListDTO
	 */
	private void filtersBoardsAndSetKpisForExistingUser(List<BoardDTO> existingBoardListDTO,
			List<BoardDTO> defaultBoardListDTO) {
		defaultBoardListDTO.forEach(defaultBoardDTO -> existingBoardListDTO.forEach(existingBoardDTO -> {
			if (defaultBoardDTO.getBoardId() == existingBoardDTO.getBoardId()
					&& !CollectionUtils.containsAll(defaultBoardDTO.getKpis(), existingBoardDTO.getKpis())) {
				filtersKPIAndSetKPIsForExistingUser(defaultBoardDTO, existingBoardDTO);
			}
			if (defaultBoardDTO.getBoardId() == existingBoardDTO.getBoardId()
					&& !CollectionUtils.containsAll(existingBoardDTO.getKpis(), defaultBoardDTO.getKpis())) {
				filtersKPIAndSetKPIsForExistingUser(defaultBoardDTO, existingBoardDTO);
			}
		}));
	}

	/**
	 * any kpiId added or removed found then we are re place user board config with
	 * default BoardDTO. order maintain only used of user board config when board is
	 * iteration. otherwise work on defaultBoardDTO (kpi_master and
	 * kpi_category_mapping collections)
	 *
	 * @param defaultBoardDTO
	 *            defaultBoardDTO
	 * @param existingBoardDTO
	 *            existingBoardDTO
	 */
	private void filtersKPIAndSetKPIsForExistingUser(BoardDTO defaultBoardDTO, BoardDTO existingBoardDTO) {

		List<BoardKpisDTO> boardKpisList = new ArrayList<>();

		Map<String, BoardKpisDTO> kpiWiseUserBoardConfig = new HashMap<>();
		existingBoardDTO.getKpis()
				.forEach(existingKPI -> kpiWiseUserBoardConfig.put(existingKPI.getKpiId(), existingKPI));
		AtomicInteger iterationOrderSize = new AtomicInteger(2);
		defaultBoardDTO.getKpis().forEach(defaultKPIList -> {
			BoardKpisDTO boardKpis = new BoardKpisDTO();
			boardKpis.setKpiId(defaultKPIList.getKpiId());
			boardKpis.setKpiName(defaultKPIList.getKpiName());
			if (kpiWiseUserBoardConfig.get(defaultKPIList.getKpiId()) != null) {
				BoardKpisDTO existingKPI = kpiWiseUserBoardConfig.get(defaultKPIList.getKpiId());
				boardKpis.setShown(existingKPI.isShown());
				boardKpis.setIsEnabled(existingKPI.getIsEnabled());
				boardKpis.setSubCategoryBoard(existingKPI.getSubCategoryBoard());
				if (defaultBoardDTO.getBoardName().equals(ITERATION)) {
					iterationOrderSize.getAndIncrement();
					boardKpis.setOrder(existingKPI.getOrder());
				} else {
					boardKpis.setOrder(defaultKPIList.getOrder());
				}
			} else {
				boardKpis.setShown(defaultKPIList.isShown());
				boardKpis.setIsEnabled(defaultKPIList.getIsEnabled());
				boardKpis.setSubCategoryBoard(defaultKPIList.getSubCategoryBoard());
				if (!defaultBoardDTO.getBoardName().equalsIgnoreCase(ITERATION)) {
					boardKpis.setOrder(defaultKPIList.getOrder());
				}
			}
			boardKpis.setKpiDetail(defaultKPIList.getKpiDetail());
			boardKpisList.add(boardKpis);
		});
		boardKpisList.stream().filter(boardKpisDTO -> boardKpisDTO.getOrder() == 0)
				.forEach(boardKpisDTO -> boardKpisDTO.setOrder(iterationOrderSize.getAndIncrement()));
		defaultBoardDTO.setKpis(boardKpisList.stream().sorted(Comparator.comparing(BoardKpisDTO::getOrder))
				.collect(Collectors.toList()));
	}

	/**
	 * set user board config for new or default user from kpi master and kpi
	 * category , category mapping.
	 *
	 * @param boardName
	 *            boardName
	 * @param newUserBoardConfig
	 *            newUserBoardConfig
	 * @param kpiCategoryList
	 *            kpiCategoryList
	 * @param kpiMasterMap
	 *            kpiMasterMap
	 */
	private void setUserBoardConfigBasedOnCategory(String boardName, UserBoardConfigDTO newUserBoardConfig,
			List<KpiCategory> kpiCategoryList, Map<String, KpiMaster> kpiMasterMap) {
		AtomicReference<Integer> kpiCategoryBoardId = new AtomicReference<>(1);
		List<BoardDTO> scrumBoards = new ArrayList<>();
		List<BoardDTO> kanbanBoards = new ArrayList<>();
		List<BoardDTO> otherBoards = new ArrayList<>();
		List<String> scrumKanbanBoardNameList = UserBoardConfigEnum.SCRUM_KANBAN_BOARD.getBoardName();
		List<String> otherBoardNameList = UserBoardConfigEnum.OTHER_BOARD.getBoardName();
		List<String> defaultKpiCategory = new ArrayList<>();
		defaultKpiCategory.addAll(scrumKanbanBoardNameList);
		defaultKpiCategory.addAll(otherBoardNameList);

		setDefaultBoardInfoFromKpiMaster(kpiCategoryBoardId.getAndSet(kpiCategoryBoardId.get() + 1), false,
				defaultKpiCategory, scrumBoards, boardName);
		if (CollectionUtils.isNotEmpty(kpiCategoryList)) {
			setAsPerCategoryMappingBoardInfo(kpiCategoryBoardId, kpiCategoryList, kpiMasterMap, scrumBoards, false);
		}
		setUserBoardInfo(kpiCategoryBoardId, scrumKanbanBoardNameList, scrumBoards, false);

		setDefaultBoardInfoFromKpiMaster(kpiCategoryBoardId.getAndSet(kpiCategoryBoardId.get() + 1), true,
				defaultKpiCategory, kanbanBoards, boardName);
		if (CollectionUtils.isNotEmpty(kpiCategoryList)) {
			setAsPerCategoryMappingBoardInfo(kpiCategoryBoardId, kpiCategoryList, kpiMasterMap, kanbanBoards, true);
		}
		setUserBoardInfo(kpiCategoryBoardId, scrumKanbanBoardNameList, kanbanBoards, true);

		setUserBoardInfo(kpiCategoryBoardId, otherBoardNameList, otherBoards, false);

		setFiltersInfoInBoard(scrumBoards, kanbanBoards, otherBoards);

		newUserBoardConfig.setScrum(scrumBoards);
		newUserBoardConfig.setKanban(kanbanBoards);
		newUserBoardConfig.setOthers(otherBoards);
	}

	/**
	 * Sets the filters for the provided scrum, kanban, and other boards. Filters
	 * are fetched from the cache, modified to update specific label names, and then
	 * assigned to each board based on its boardId.
	 *
	 * @param scrumBoards
	 *            the list of scrum boards to set filters for
	 * @param kanbanBoards
	 *            the list of kanban boards to set filters for
	 * @param otherBoards
	 *            the list of other boards to set filters for
	 */
	private void setFiltersInfoInBoard(List<BoardDTO> scrumBoards, List<BoardDTO> kanbanBoards,
			List<BoardDTO> otherBoards) {
		// Fetch all filters and filter categories once
		List<Filters> filtersList = configHelperService.loadAllFilters();
		List<AdditionalFilterCategory> filterCategory = cacheService.getAdditionalFilterHierarchyLevel().values()
				.stream().toList();
		String sqdFilterCategoryId;
		if (!filterCategory.isEmpty()) {
			sqdFilterCategoryId = filterCategory.get(0).getFilterCategoryId();
		} else {
			sqdFilterCategoryId = "sqd";
		}

		// Update the labelName for additional filters
		filtersList.forEach(filter -> {
			if (filter.getAdditionalFilters() != null) {
				filter.getAdditionalFilters().stream()
						.filter(f -> f.getDefaultLevel().getLabelName().equalsIgnoreCase("sqd"))
						.forEach(f -> f.getDefaultLevel().setLabelName(sqdFilterCategoryId));
			}
		});

		// Create a map for fast lookup by boardId
		Map<Integer, Filters> filtersMap = filtersList.stream()
				.collect(Collectors.toMap(Filters::getBoardId, Function.identity()));

		// Helper method to set filters for a list of boards
		ObjIntConsumer<List<BoardDTO>> setFiltersForBoards = (boards, offset) -> boards
				.forEach(boardDTO -> boardDTO.setFilters(copyFiltersWithoutId(
						filtersMap.getOrDefault(boardDTO.getBoardId() - offset, filtersMap.get(1)))));

		// Set filters for each type of board
		setFiltersForBoards.accept(scrumBoards, 0);
		setFiltersForBoards.accept(kanbanBoards, scrumBoards.size());
		setFiltersForBoards.accept(otherBoards, 0);
	}

	/**
	 * Creates a copy of the provided Filters object without including the id and
	 * boardId fields.
	 *
	 * @param original
	 *            the original Filters object to copy
	 * @return a new Filters object with the same values as the original, except for
	 *         the id and boardId fields
	 */
	private Filters copyFiltersWithoutId(Filters original) {
		Filters copy = new Filters();
		copy.setProjectTypeSwitch(original.getProjectTypeSwitch());
		copy.setPrimaryFilter(original.getPrimaryFilter());
		copy.setParentFilter(original.getParentFilter());
		copy.setAdditionalFilters(original.getAdditionalFilters());
		return copy;
	}

	/**
	 * This method is used to set user board information
	 *
	 * @param kpiCategoryBoardId
	 *            kpiCategoryBoardId is used to set the board order
	 * @param otherBoardNameList
	 *            this contains board name list
	 * @param otherBoards
	 *            otherBoards
	 * @param value
	 *            value
	 */
	private void setUserBoardInfo(AtomicReference<Integer> kpiCategoryBoardId, List<String> otherBoardNameList,
			List<BoardDTO> otherBoards, boolean value) {

		otherBoardNameList.forEach(board -> setBoardInfoAsPerDefaultKpiCategory(
				kpiCategoryBoardId.getAndSet(kpiCategoryBoardId.get() + 1), board, otherBoards, value));

	}

	/**
	 * prepare boards for as per category and kpi category mappings
	 *
	 * @param kpiCategoryBoardId
	 *            kpiCategoryBoardId
	 * @param kpiCategoryList
	 *            kpiCategoryList
	 * @param kpiMasterMap
	 *            kpiMasterMap
	 * @param boardDTOList
	 *            boardDTOList
	 * @param kanban
	 *            kanban
	 */
	private void setAsPerCategoryMappingBoardInfo(AtomicReference<Integer> kpiCategoryBoardId,
			List<KpiCategory> kpiCategoryList, Map<String, KpiMaster> kpiMasterMap, List<BoardDTO> boardDTOList,
			boolean kanban) {
		List<KpiCategoryMapping> kpiCategoryMappingList = kpiCategoryMappingRepository.findAll();
		if (CollectionUtils.isNotEmpty(kpiCategoryMappingList)) {
			Map<String, List<KpiCategoryMapping>> kpiIdWiseCategory = kpiCategoryMappingList.stream()
					.collect(Collectors.groupingBy(KpiCategoryMapping::getCategoryId, Collectors.toList()));
			kpiCategoryList.forEach(kpiCategory -> setBoardInfoAsPerKpiCategory(
					kpiCategoryBoardId.getAndSet(kpiCategoryBoardId.get() + 1), kpiCategory,
					kpiIdWiseCategory.get(kpiCategory.getCategoryId()), kpiMasterMap, boardDTOList, kanban));
		}
	}

	/**
	 * set board details and kpi list as per KPI category.
	 *
	 * @param kpiCategoryBoardId
	 *            kpiCategoryBoardId
	 * @param kpiCategory
	 *            kpiCategory
	 * @param kpiCategoryMappingList
	 *            kpiCategoryMappingList
	 * @param kpiMasterMap
	 *            kpiMasterMap
	 * @param asPerCategoryBoardList
	 *            asPerCategoryBoardList
	 * @param kanban
	 *            kanban
	 */
	private void setBoardInfoAsPerKpiCategory(Integer kpiCategoryBoardId, KpiCategory kpiCategory,
			List<KpiCategoryMapping> kpiCategoryMappingList, Map<String, KpiMaster> kpiMasterMap,
			List<BoardDTO> asPerCategoryBoardList, boolean kanban) {
		BoardDTO asPerCategoryBoard = new BoardDTO();
		asPerCategoryBoard.setBoardId(kpiCategoryBoardId);
		asPerCategoryBoard.setBoardName(kpiCategory.getCategoryName());
		asPerCategoryBoard.setBoardSlug(kpiCategory.getCategoryId().toLowerCase());
		List<BoardKpisDTO> boardKpisList = new ArrayList<>();
		kpiCategoryMappingList.stream().filter(kpiCategoryMapping -> kpiCategoryMapping.isKanban() == kanban)
				.sorted(Comparator.comparing(KpiCategoryMapping::getKpiOrder))
				.forEach(kpiCategoryMapping -> setKpiUserBoardCategoryWise(boardKpisList, kpiCategoryMapping,
						kpiMasterMap.get(kpiCategoryMapping.getKpiId())));
		asPerCategoryBoard.setKpis(boardKpisList);
		asPerCategoryBoardList.add(asPerCategoryBoard);
	}

	/**
	 * set board details and kpi list as per Default category like ITERATION ,
	 * BACKLOG
	 *
	 * @param boardId
	 *            boardId
	 * @param boardName
	 *            boardName
	 * @param asPerCategoryBoardList
	 *            asPerCategoryBoardList
	 * @param kanban
	 *            kanban
	 */
	private void setBoardInfoAsPerDefaultKpiCategory(int boardId, String boardName,
			List<BoardDTO> asPerCategoryBoardList, boolean kanban) {
		BoardDTO asPerCategoryBoard = new BoardDTO();
		asPerCategoryBoard.setBoardId(boardId);
		asPerCategoryBoard.setBoardName(boardName);
		if (boardName.equalsIgnoreCase("Kpi Maturity"))
			asPerCategoryBoard.setBoardSlug("kpi-maturity");
		else
			asPerCategoryBoard.setBoardSlug(boardName.toLowerCase());
		List<BoardKpisDTO> boardKpisList = new ArrayList<>();
		kpiMasterRepository.findByKpiCategoryAndKanban(boardName, kanban).stream()
				.sorted(Comparator.comparing(KpiMaster::getDefaultOrder))
				.forEach(kpiMaster -> setKpiUserBoardDefaultFromKpiMaster(boardKpisList, kpiMaster));
		asPerCategoryBoard.setKpis(boardKpisList);
		asPerCategoryBoardList.add(asPerCategoryBoard);
	}

	/**
	 * set board details and kpi list as per Default category like
	 * DEFAULT_BOARD_NAME.
	 *
	 * @param boardId
	 *            boardId
	 * @param kanban
	 *            kanban
	 * @param kpiCategory
	 *            kpiCategory
	 * @param defaultBoardList
	 *            defaultBoardList
	 * @param boardName
	 *            boardName
	 */
	private void setDefaultBoardInfoFromKpiMaster(int boardId, boolean kanban, List<String> kpiCategory,
			List<BoardDTO> defaultBoardList, String boardName) {
		BoardDTO defaultBoard = new BoardDTO();
		defaultBoard.setBoardId(boardId);
		defaultBoard.setBoardName(boardName);
		defaultBoard.setBoardSlug("my-knowhow");
		List<BoardKpisDTO> boardKpisList = new ArrayList<>();
		kpiMasterRepository.findByKanbanAndKpiCategoryNotIn(kanban, kpiCategory).stream()
				.sorted(Comparator.comparing(KpiMaster::getDefaultOrder))
				.forEach(kpiMaster -> setKpiUserBoardDefaultFromKpiMaster(boardKpisList, kpiMaster));
		defaultBoard.setKpis(boardKpisList);
		defaultBoardList.add(defaultBoard);
	}

	/**
	 * set Kpi details in board for user board config from kpi master.
	 *
	 * @param boardKpisList
	 *            boardKpisList
	 * @param kpiMaster
	 *            kpiMaster
	 */
	private void setKpiUserBoardDefaultFromKpiMaster(List<BoardKpisDTO> boardKpisList, KpiMaster kpiMaster) {
		Boolean isRepoToolFlag = handleDeveloperKpi;
		if ((kpiMaster.getIsRepoToolKpi() == null) || (kpiMaster.getIsRepoToolKpi().equals(isRepoToolFlag))) {
			BoardKpisDTO boardKpis = new BoardKpisDTO();
			boardKpis.setKpiId(kpiMaster.getKpiId());
			boardKpis.setKpiName(kpiMaster.getKpiName());
			boardKpis.setShown(true);
			boardKpis.setIsEnabled(true);
			boardKpis.setOrder(kpiMaster.getDefaultOrder());
			boardKpis.setSubCategoryBoard(kpiMaster.getKpiSubCategory());
			boardKpis.setKpiDetail(kpiMaster);
			boardKpisList.add(boardKpis);
		}
	}

	/**
	 * set Kpi details in board for user board config from kpi category mapping
	 *
	 * @param boardKpisList
	 *            boardKpisList
	 * @param kpiCategoryMapping
	 *            kpiCategoryMapping
	 * @param kpiMaster
	 *            kpiMaster
	 */
	private void setKpiUserBoardCategoryWise(List<BoardKpisDTO> boardKpisList, KpiCategoryMapping kpiCategoryMapping,
			KpiMaster kpiMaster) {
		if (Objects.nonNull(kpiMaster)) {
			BoardKpisDTO boardKpis = new BoardKpisDTO();
			boardKpis.setKpiId(kpiCategoryMapping.getKpiId());
			boardKpis.setKpiName(kpiMaster.getKpiName());
			boardKpis.setShown(true);
			boardKpis.setIsEnabled(true);
			boardKpis.setOrder(kpiCategoryMapping.getKpiOrder());
			boardKpis.setKpiDetail(kpiMaster);
			boardKpisList.add(boardKpis);
		} else {
			log.error("[UserBoardConfig]. No kpi Data found for {}", kpiCategoryMapping.getKpiId());

		}
	}

	/**
	 * added kpi master details in user board config
	 *
	 * @param userBoardConfigDTO
	 *            userBoardConfigDTO
	 * @param kpiDetailMap
	 *            kpiDetailMap
	 */
	private void filterKpis(UserBoardConfigDTO userBoardConfigDTO, Map<String, KpiMaster> kpiDetailMap) {
		if (userBoardConfigDTO != null) {
			addKpiDetails(userBoardConfigDTO.getScrum(), kpiDetailMap);
			addKpiDetails(userBoardConfigDTO.getKanban(), kpiDetailMap);
			addKpiDetails(userBoardConfigDTO.getOthers(), kpiDetailMap);
		}
	}

	private void addKpiDetails(List<BoardDTO> boardList, Map<String, KpiMaster> kpiDetailMap) {
		CollectionUtils.emptyIfNull(boardList).forEach(board -> {
			List<BoardKpisDTO> boardKpiDtoList = new ArrayList<>();
			board.getKpis().forEach(boardKpisDTO -> {
				KpiMaster kpiMaster = kpiDetailMap.get(boardKpisDTO.getKpiId());
				if (null != kpiMaster) {
					boardKpisDTO.setKpiName(kpiMaster.getKpiName());
					boardKpisDTO.setKpiDetail(kpiMaster);
					boardKpiDtoList.add(boardKpisDTO);
				}
			});
			board.setKpis(boardKpiDtoList);
		});
	}

	@Override
	public ServiceResponse saveBoardConfig(UserBoardConfigDTO userBoardConfigDTO, ConfigLevel configLevel,
			String basicProjectConfigId) {
		UserBoardConfig userBoardConfig = userBoardConfigMapper.toEntity(userBoardConfigDTO);

		if (userBoardConfig == null) {
			return new ServiceResponse(false, "User Board Configuration is empty", null);
		}

		final String loggedInUser = authenticationService.getLoggedInUser();
		if (!loggedInUser.equals(userBoardConfig.getUsername())) {
			return new ServiceResponse(false, "Logged In user is not authorized to change the board", null);
		}

		String configId = configLevel == ConfigLevel.PROJECT ? basicProjectConfigId : null;
		String username = configLevel == ConfigLevel.USER ? loggedInUser : null;
		UserBoardConfig boardConfig = fetchUserBoardConfig(configId, username);

		if (boardConfig != null) {
			boardConfig.setScrum(userBoardConfig.getScrum());
			boardConfig.setKanban(userBoardConfig.getKanban());
			boardConfig.setOthers(userBoardConfig.getOthers());
		} else {
			boardConfig = userBoardConfig;
		}

		boardConfig.setBasicProjectConfigId(configLevel == ConfigLevel.PROJECT ? basicProjectConfigId : null);
		boardConfig.setUsername(configLevel == ConfigLevel.USER ? loggedInUser : null);

		boardConfig = userBoardConfigRepository.save(boardConfig);
		cacheService.clearCache(CommonConstant.CACHE_USER_BOARD_CONFIG);
		log.info("Successfully saved {} BoardConfig: {}", configLevel,
				configLevel == ConfigLevel.PROJECT ? sanitizeProjectIds(Collections.singletonList(basicProjectConfigId))
						: loggedInUser);
		return new ServiceResponse(true, "Successfully Saved board Configuration",
				userBoardConfigMapper.toDto(boardConfig));
	}

	/**
	 * delete user from user_board_config
	 *
	 * @param userName
	 *            userName
	 */
	@Override
	public void deleteUser(String userName) {
		log.info("UserBoardConfigServiceImpl::deleteUser start");
		userBoardConfigRepository.deleteByUsername(userName);
		cacheService.clearCache(CommonConstant.CACHE_USER_BOARD_CONFIG);
		log.info("{} deleted Successfully from user_board_config", userName.replaceAll("[^a-zA-Z0-9_-]", ""));
	}

	/**
	 * Deletes the project board config.
	 *
	 * @param basicProjectConfigId
	 *            basicProjectConfigId
	 */
	@Override
	public void deleteProjectBoardConfig(String basicProjectConfigId) {
		userBoardConfigRepository.deleteByBasicProjectConfigId(basicProjectConfigId);
		cacheService.clearCache(CommonConstant.CACHE_USER_BOARD_CONFIG);
		log.info("Successfully deleted project board config: {}", basicProjectConfigId);
	}

	/**
	 * Fetches the board configuration form cache. If `username` is null and
	 * basicProjectConfigId` is not, it represents project level config. If
	 * username` is not null and `basicProjectConfigId` is null, it represents user
	 * level config.
	 * 
	 * @param basicProjectConfigId
	 *            basicProjectConfigId
	 * @param username
	 *            username
	 * @return the board config
	 */
	private UserBoardConfig fetchUserBoardConfig(String basicProjectConfigId, String username) {
		Map<Pair<String, String>, UserBoardConfig> userBoardConfigMap = configHelperService.loadUserBoardConfig();
		return userBoardConfigMap.get(Pair.of(username, basicProjectConfigId));
	}

}
