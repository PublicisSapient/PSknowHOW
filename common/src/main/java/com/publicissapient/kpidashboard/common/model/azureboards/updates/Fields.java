
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
@JsonPropertyOrder({ "System.IterationId", "System.State", "System.Reason", "System.CreatedDate", "System.ChangedDate",
		"System.IterationPath" })
public class Fields {

	@JsonProperty("System.IterationId")
	private SystemIterationId systemIterationId;
	@JsonProperty("System.State")
	private SystemState systemState;
	@JsonProperty("System.Reason")
	private SystemReason systemReason;
	@JsonProperty("System.CreatedDate")
	private SystemCreatedDate systemCreatedDate;
	@JsonProperty("System.ChangedDate")
	private SystemChangedDate systemChangedDate;
	@JsonProperty("System.IterationPath")
	private SystemIterationPath systemIterationPath;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	@JsonProperty("System.IterationId")
	public SystemIterationId getSystemIterationId() {
		return systemIterationId;
	}

	@JsonProperty("System.IterationId")
	public void setSystemIterationId(SystemIterationId systemIterationId) {
		this.systemIterationId = systemIterationId;
	}

	@JsonProperty("System.State")
	public SystemState getSystemState() {
		return systemState;
	}

	@JsonProperty("System.State")
	public void setSystemState(SystemState systemState) {
		this.systemState = systemState;
	}

	@JsonProperty("System.Reason")
	public SystemReason getSystemReason() {
		return systemReason;
	}

	@JsonProperty("System.Reason")
	public void setSystemReason(SystemReason systemReason) {
		this.systemReason = systemReason;
	}

	@JsonProperty("System.CreatedDate")
	public SystemCreatedDate getSystemCreatedDate() {
		return systemCreatedDate;
	}

	@JsonProperty("System.CreatedDate")
	public void setSystemCreatedDate(SystemCreatedDate systemCreatedDate) {
		this.systemCreatedDate = systemCreatedDate;
	}

	@JsonProperty("System.ChangedDate")
	public SystemChangedDate getSystemChangedDate() {
		return systemChangedDate;
	}

	@JsonProperty("System.ChangedDate")
	public void setSystemChangedDate(SystemChangedDate systemChangedDate) {
		this.systemChangedDate = systemChangedDate;
	}

	@JsonProperty("System.IterationPath")
	public SystemIterationPath getSystemIterationPath() {
		return systemIterationPath;
	}

	@JsonProperty("System.IterationPath")
	public void setSystemIterationPath(SystemIterationPath systemIterationPath) {
		this.systemIterationPath = systemIterationPath;
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
		return new ToStringBuilder(this).append("systemIterationId", systemIterationId)
				.append("systemState", systemState).append("systemReason", systemReason)
				.append("systemCreatedDate", systemCreatedDate).append("systemChangedDate", systemChangedDate)
				.append("systemIterationPath", systemIterationPath).append("additionalProperties", additionalProperties)
				.toString();
	}

}
