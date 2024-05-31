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
import java.util.Comparator;
import java.util.HashMap;
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
import com.publicissapient.kpidashboard.common.repository.jira.BoardMetadataRepository;
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
	private final BoardMetadataRepository boardMetadataRepository;
	private final AccountHierarchyRepository accountHierarchyRepository;
	DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

	/**
	 * @param boardMetadataRepository
	 *            -for fetch
	 */
	@Autowired
	public EditKpiConfigServiceImpl(BoardMetadataRepository boardMetadataRepository,
			AccountHierarchyRepository accountHierarchyRepository) {
		this.boardMetadataRepository = boardMetadataRepository;
		this.accountHierarchyRepository = accountHierarchyRepository;
	}

	/**
	 * Gets data by type for the environment.
	 *
	 * @param projectBasicConfigid
	 *            - used for project config id
	 * @param kpiCode
	 * @return ServiceResponse with data object,message and status flag true if data
	 *         is found,false if not data found
	 */

	@Override
	public Map<String, List<MetadataValue>> getDataForType(String projectBasicConfigid, String kpiCode) {

		Map<String, List<MetadataValue>> data = new HashMap<>();
		BoardMetadata boardmetadata = boardMetadataRepository
				.findByProjectBasicConfigId(new ObjectId(projectBasicConfigid));
		if (boardmetadata != null && CollectionUtils.isNotEmpty(boardmetadata.getMetadata())) {
			data = boardmetadata.getMetadata().stream()
					.collect(Collectors.toMap(Metadata::getType, Metadata::getValue));
		}

		if (kpiCode.equalsIgnoreCase(KPICode.RELEASE_BURNUP.getKpiId())) {
			List<MetadataValue> metadataValueList = accountHierarchyRepository
					.findByLabelNameAndBasicProjectConfigIdAndReleaseState(LABEL_NAME,
							new ObjectId(projectBasicConfigid), STATE)
					.stream().sorted(Comparator.comparing((AccountHierarchy ah) -> {
						if (ah.getEndDate() != null && !ah.getEndDate().isEmpty()) {
							return LocalDateTime.parse(ah.getEndDate(), formatter);
						} else {
							return LocalDateTime.MIN;
						}
					}).reversed()).limit(10).map(accountHierarchy -> {
						MetadataValue metadataValue = new MetadataValue();
						double duration = 0;
						if (StringUtils.isNotEmpty(accountHierarchy.getBeginDate())
								&& StringUtils.isNotEmpty(accountHierarchy.getEndDate())) {
							LocalDateTime startDate = DateUtil.convertingStringToLocalDateTime(
									accountHierarchy.getBeginDate(), DateUtil.TIME_FORMAT);
							LocalDateTime releaseDate = DateUtil.convertingStringToLocalDateTime(
									accountHierarchy.getEndDate(), DateUtil.TIME_FORMAT);
							duration = DateUtil.calculateWorkingDays(startDate, releaseDate);
						}
						String releaseName = accountHierarchy.getNodeName().split("_")[0] + " (duration " + duration
								+ ")";
						metadataValue.setKey(releaseName);
						metadataValue.setData(releaseName);
						return metadataValue;
					}).collect(Collectors.toList());

			data.put(RELEASE_KEY, metadataValueList);
		}

		return data;
	}

}
