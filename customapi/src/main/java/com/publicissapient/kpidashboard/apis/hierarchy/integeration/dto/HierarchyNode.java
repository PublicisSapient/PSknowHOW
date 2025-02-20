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
package com.publicissapient.kpidashboard.apis.hierarchy.integeration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class HierarchyNode {

	@JsonProperty("Opportunity")
	private String opportunity;

	@JsonProperty("Opportunity_unique_id")
	private String opportunityUniqueId;

	@JsonProperty("Opportunity_id")
	private String opportunityId;

	@JsonProperty("poc")
	private String poc;

	@JsonProperty("alternate_poc")
	private String alternatePoc;

	@JsonProperty("pid")
	private String pid;

	@JsonProperty("archetype")
	private String archetype;

	@JsonProperty("probability")
	private String probability;

	@JsonProperty("clientPartnerLeadName")
	private String clientPartnerLeadName;

	@JsonProperty("clientPartnerLeadEmail")
	private String clientPartnerLeadEmail;

	@JsonProperty("deliveryLeadName")
	private String deliveryLeadName;

	@JsonProperty("deliveryLeadEmail")
	private String deliveryLeadEmail;

	@JsonProperty("engineeringLeadName")
	private String engineeringLeadName;

	@JsonProperty("engineeringLeadEmail")
	private String engineeringLeadEmail;

	@JsonProperty("buGroup")
	private String buGroup;

	@JsonProperty("team")
	private String team;

	@JsonProperty("projectType")
	private String projectType;

	@JsonProperty("capabilityGroup")
	private String capabilityGroup;

	@JsonProperty("Portfolio")
	private String portfolio;

	@JsonProperty("Portfolio_unique_id")
	private String portfolioUniqueId;

	@JsonProperty("Portfolio_id")
	private String portfolioId;

	@JsonProperty("Account")
	private String account;

	@JsonProperty("Account_unique_id")
	private String accountUniqueId;

	@JsonProperty("Account_id")
	private String accountId;

	@JsonProperty("Vertical")
	private String vertical;

	@JsonProperty("Vertical_unique_id")
	private String verticalUniqueId;

	@JsonProperty("Vertical_id")
	private String verticalId;

	@JsonProperty("BU")
	private String bu;

	@JsonProperty("BU_unique_id")
	private String buUniqueId;

	@JsonProperty("BU_id")
	private String buId;

	@JsonProperty("Root")
	private String root;

	@JsonProperty("Root_unique_id")
	private String rootUniqueId;

	@JsonProperty("Root_id")
	private String rootId;

	@JsonProperty("Id")
	private String id;

}
