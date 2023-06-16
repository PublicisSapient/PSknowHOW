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

package com.publicissapient.kpidashboard.azure.client.azureissue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.publicissapient.kpidashboard.azure.model.Sprint;
import com.publicissapient.kpidashboard.azure.util.AzureProcessorUtil;
import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.azureboards.Fields;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class AzureIssueClientUtil {

	public static final Comparator<Sprint> SPRINT_COMPARATOR = (Sprint o1, Sprint o2) -> {
		int cmp1 = ObjectUtils.compare(o1.getStartDateStr(), o2.getStartDateStr());
		if (cmp1 != 0) {
			return cmp1;
		}
		return ObjectUtils.compare(o1.getEndDateStr(), o2.getEndDateStr());
	};

	private AzureIssueClientUtil() {
		super();
	}

	/**
	 * Builds Filed Map
	 *
	 * @param fields
	 *            IssueField Iterable
	 * @return Map of FieldIssue ID and FieldIssue Object
	 */
	public static Map<String, Object> buildFieldMap(Fields fields) {
		Map<String, Object> rt = new HashMap<>();

		if (fields != null) {
			ObjectMapper oMapper = new ObjectMapper();
			rt = oMapper.convertValue(fields, Map.class);
		}
		return rt;
	}

	/**
	 * Gets Account Hierarchy
	 * 
	 * @param accountHierarchyRepository
	 *            accountHierarchyRepository
	 * @return Pair of NodeId and path and Account Hierarchy Map
	 */
	public static Map<Pair<String, String>, AccountHierarchy> getAccountHierarchy(
			AccountHierarchyRepository accountHierarchyRepository) {
		List<AccountHierarchy> accountHierarchyList = accountHierarchyRepository.findAll();
		return accountHierarchyList.stream()
				.collect(Collectors.toMap(p -> Pair.of(p.getNodeId(), p.getPath()), p -> p));

	}

	public static List<String> getLabelsList(Fields fields) {
		List<String> labels = new ArrayList<>();
		if (fields.getSystemTags() != null) {
			String[] tags = fields.getSystemTags().split(";");
			for (String tag : tags) {
				labels.add(AzureProcessorUtil.deodeUTF8String(tag.trim()));
			}
		}
		return labels;
	}
}