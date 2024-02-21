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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.model.jira.BoardMetadata;
import com.publicissapient.kpidashboard.common.model.jira.Metadata;
import com.publicissapient.kpidashboard.common.model.jira.MetadataValue;
import com.publicissapient.kpidashboard.common.repository.jira.BoardMetadataRepository;

/**
 * This class provides various methods related to operations on Edit KPI
 * Configurations Data
 *
 * @author jagmongr
 */
@Service
public class EditKpiConfigServiceImpl implements EditKpiConfigService {

	private final BoardMetadataRepository boardMetadataRepository;

	/**
	 * @param boardMetadataRepository
	 *            -for fetch
	 */
	@Autowired
	public EditKpiConfigServiceImpl(BoardMetadataRepository boardMetadataRepository) {
		this.boardMetadataRepository = boardMetadataRepository;
	}

	/**
	 * Gets data by type for the environment.
	 *
	 * @param projectToolConfigid
	 *            - used for project config id
	 * @return ServiceResponse with data object,message and status flag true if data
	 *         is found,false if not data found
	 */

	@Override
	public Map<String, List<MetadataValue>> getDataForType(String projectToolConfigid) {

		Map<String, List<MetadataValue>> data = new HashMap<>();
		BoardMetadata boardmetadata = boardMetadataRepository
				.findByProjectToolConfigId(new ObjectId(projectToolConfigid));
		if (boardmetadata != null && CollectionUtils.isNotEmpty(boardmetadata.getMetadata())) {
			data = boardmetadata.getMetadata().stream()
					.collect(Collectors.toMap(Metadata::getType, Metadata::getValue));
		}

		return data;
	}

}
