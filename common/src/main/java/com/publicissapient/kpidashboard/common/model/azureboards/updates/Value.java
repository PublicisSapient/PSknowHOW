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

package com.publicissapient.kpidashboard.common.model.azureboards.updates;

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
@JsonPropertyOrder({ "id", "workItemId", "rev", "fields" })
public class Value {

	@JsonProperty("id")
	private Integer id;
	@JsonProperty("workItemId")
	private Integer workItemId;
	@JsonProperty("rev")
	private Integer rev;
	@JsonProperty("fields")
	private Fields fields;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	@JsonProperty("id")
	public Integer getId() {
		return id;
	}

	@JsonProperty("id")
	public void setId(Integer id) {
		this.id = id;
	}

	@JsonProperty("workItemId")
	public Integer getWorkItemId() {
		return workItemId;
	}

	@JsonProperty("workItemId")
	public void setWorkItemId(Integer workItemId) {
		this.workItemId = workItemId;
	}

	@JsonProperty("rev")
	public Integer getRev() {
		return rev;
	}

	@JsonProperty("rev")
	public void setRev(Integer rev) {
		this.rev = rev;
	}

	@JsonProperty("fields")
	public Fields getFields() {
		return fields;
	}

	@JsonProperty("fields")
	public void setFields(Fields fields) {
		this.fields = fields;
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
		return new ToStringBuilder(this).append("id", id).append("workItemId", workItemId).append("rev", rev)
				.append("fields", fields).append("additionalProperties", additionalProperties).toString();
	}

}
