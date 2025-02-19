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

package com.publicissapient.kpidashboard.apis.appsetting.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.BoardMetadata;
import com.publicissapient.kpidashboard.common.model.jira.Metadata;
import com.publicissapient.kpidashboard.common.model.jira.MetadataValue;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.util.DateUtil;

/**
 * This class provides various methods related to operations on Edit KPI
 * Configurations Data
 *
 * @author jagmongr
 */
@Service
public class EditKpiConfigServiceImpl implements EditKpiConfigService {

	public static final String RELEASE_KEY = "releases";
	public static final String LABEL_NAME = "release";
	public static final String STATE = "Released";
	private final ConfigHelperService configHelperService;
	private final AccountHierarchyRepository accountHierarchyRepository;
	DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

	/**
	 * @param configHelperService
	 *          for board meta data
	 * @param accountHierarchyRepository
	 *          account hierarchy
	 */
	@Autowired
	public EditKpiConfigServiceImpl(ConfigHelperService configHelperService,
			AccountHierarchyRepository accountHierarchyRepository) {
		this.configHelperService = configHelperService;
		this.accountHierarchyRepository = accountHierarchyRepository;
	}

	/**
	 * Gets data by type for the environment.
	 *
	 * @param projectBasicConfigid
	 *          - used for project config id
	 * @param kpiCode
	 * @return ServiceResponse with data object,message and status flag true if data
	 *         is found,false if not data found
	 */
	@Override
	public Map<String, List<MetadataValue>> getDataForType(String projectBasicConfigid, String kpiCode) {

		Map<String, List<MetadataValue>> data = new HashMap<>();
		BoardMetadata boardmetadata = configHelperService.getBoardMetaData(new ObjectId(projectBasicConfigid));
		if (boardmetadata != null && CollectionUtils.isNotEmpty(boardmetadata.getMetadata())) {
			data = boardmetadata.getMetadata().stream().collect(
					Collectors.toMap(Metadata::getType, metadata -> new ArrayList<>(new HashSet<>(metadata.getValue()))));
		}

		getClosedReleaseName(projectBasicConfigid, kpiCode, data);

		return data;
	}

	/**
	 * get list of closed releases
	 *
	 * @param projectBasicConfigid
	 *          projectBasicConfigid
	 * @param kpiCode
	 *          kpiCode
	 * @param data
	 *          data
	 */
	private void getClosedReleaseName(String projectBasicConfigid, String kpiCode,
			Map<String, List<MetadataValue>> data) {
		if (kpiCode.equalsIgnoreCase(KPICode.RELEASE_BURNUP.getKpiId())) {
			List<MetadataValue> metadataValueList = accountHierarchyRepository
					.findByLabelNameAndBasicProjectConfigIdAndReleaseStateOrderByEndDateDesc(LABEL_NAME,
							new ObjectId(projectBasicConfigid), STATE)
					.stream().map(accountHierarchy -> {
						String releaseName;
						double duration = 0;
						duration = getDurationInDays(accountHierarchy, duration);
						releaseName = getReleaseName(accountHierarchy, duration);

						MetadataValue metadataValue = new MetadataValue();
						metadataValue.setKey(releaseName);
						metadataValue.setData(releaseName);
						return metadataValue;
					}).collect(Collectors.toList());

			data.put(RELEASE_KEY, metadataValueList);
		}
	}

	/**
	 * @param accountHierarchy
	 *          accountHierarchy
	 * @param duration
	 *          duration
	 * @return return closed release name
	 */
	private static String getReleaseName(AccountHierarchy accountHierarchy, double duration) {
		String releaseName;
		if (duration == 0) {
			releaseName = splitNodeName(accountHierarchy.getNodeName()) + " (duration - days)";
		} else {
			releaseName = splitNodeName(accountHierarchy.getNodeName()) + " (duration " + duration + " days)";
		}
		return releaseName;
	}

	/**
	 * @param releaseName
	 *          releaseName
	 * @return releaseName
	 */
	private static String splitNodeName(String releaseName) {
		int lastUnderscoreIndex = releaseName.lastIndexOf('_');
		if (lastUnderscoreIndex != -1) {
			return releaseName.substring(0, lastUnderscoreIndex);
		} else {
			return releaseName;
		}
	}

	/**
	 * This method calculate no. of working days between start and end date by
	 * excluding saturday and sunday
	 *
	 * @param accountHierarchy
	 *          accountHierarchy
	 * @param duration
	 *          duration
	 * @return duration between start and end date in days
	 */
	private static double getDurationInDays(AccountHierarchy accountHierarchy, double duration) {
		if (StringUtils.isNotEmpty(accountHierarchy.getBeginDate()) &&
				StringUtils.isNotEmpty(accountHierarchy.getEndDate())) {
			LocalDateTime startDate = DateUtil.convertingStringToLocalDateTime(accountHierarchy.getBeginDate(),
					DateUtil.TIME_FORMAT);
			LocalDateTime releaseDate = DateUtil.convertingStringToLocalDateTime(accountHierarchy.getEndDate(),
					DateUtil.TIME_FORMAT);
			duration = DateUtil.calculateWorkingDays(startDate, releaseDate);
		}
		return duration;
	}
}
