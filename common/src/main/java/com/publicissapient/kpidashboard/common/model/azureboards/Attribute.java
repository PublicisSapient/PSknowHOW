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

package com.publicissapient.kpidashboard.common.model.azureboards;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "isLocked", "name", "authorizedDate", "id", "resourceCreatedDate", "resourceModifiedDate",
		"revisedDate", "resourceSize" })
public class Attribute {

	@JsonProperty("isLocked")
	private Boolean isLocked;
	@JsonProperty("name")
	private String name;
	@JsonProperty("authorizedDate")
	private String authorizedDate;
	@JsonProperty("id")
	private Integer id;
	@JsonProperty("resourceCreatedDate")
	private String resourceCreatedDate;
	@JsonProperty("resourceModifiedDate")
	private String resourceModifiedDate;
	@JsonProperty("revisedDate")
	private String revisedDate;
	@JsonProperty("resourceSize")
	private Integer resourceSize;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	@JsonProperty("isLocked")
	public Boolean getIsLocked() {
		return isLocked;
	}

	@JsonProperty("isLocked")
	public void setIsLocked(Boolean isLocked) {
		this.isLocked = isLocked;
	}

	@JsonProperty("name")
	public String getName() {
		return name;
	}

	@JsonProperty("name")
	public void setName(String name) {
		this.name = name;
	}

	@JsonProperty("authorizedDate")
	public String getAuthorizedDate() {
		return authorizedDate;
	}

	@JsonProperty("authorizedDate")
	public void setAuthorizedDate(String authorizedDate) {
		this.authorizedDate = authorizedDate;
	}

	@JsonProperty("id")
	public Integer getId() {
		return id;
	}

	@JsonProperty("id")
	public void setId(Integer id) {
		this.id = id;
	}

	@JsonProperty("resourceCreatedDate")
	public String getResourceCreatedDate() {
		return resourceCreatedDate;
	}

	@JsonProperty("resourceCreatedDate")
	public void setResourceCreatedDate(String resourceCreatedDate) {
		this.resourceCreatedDate = resourceCreatedDate;
	}

	@JsonProperty("resourceModifiedDate")
	public String getResourceModifiedDate() {
		return resourceModifiedDate;
	}

	@JsonProperty("resourceModifiedDate")
	public void setResourceModifiedDate(String resourceModifiedDate) {
		this.resourceModifiedDate = resourceModifiedDate;
	}

	@JsonProperty("revisedDate")
	public String getRevisedDate() {
		return revisedDate;
	}

	@JsonProperty("revisedDate")
	public void setRevisedDate(String revisedDate) {
		this.revisedDate = revisedDate;
	}

	@JsonProperty("resourceSize")
	public Integer getResourceSize() {
		return resourceSize;
	}

	@JsonProperty("resourceSize")
	public void setResourceSize(Integer resourceSize) {
		this.resourceSize = resourceSize;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("isLocked", isLocked).append("name", name)
				.append("authorizedDate", authorizedDate).append("id", id)
				.append("resourceCreatedDate", resourceCreatedDate).append("resourceModifiedDate", resourceModifiedDate)
				.append("revisedDate", revisedDate).append("resourceSize", resourceSize)
				.append("additionalProperties", additionalProperties).toString();
	}

}