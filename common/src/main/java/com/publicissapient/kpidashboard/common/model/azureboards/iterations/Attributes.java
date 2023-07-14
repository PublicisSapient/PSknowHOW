
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

package com.publicissapient.kpidashboard.common.model.azureboards.iterations;

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
@JsonPropertyOrder({ "startDate", "finishDate", "timeFrame" })
public class Attributes {

	@JsonProperty("startDate")
	private String startDate;
	@JsonProperty("finishDate")
	private String finishDate;
	@JsonProperty("timeFrame")
	private String timeFrame;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	@JsonProperty("startDate")
	public String getStartDate() {
		return startDate;
	}

	@JsonProperty("startDate")
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	@JsonProperty("finishDate")
	public String getFinishDate() {
		return finishDate;
	}

	@JsonProperty("finishDate")
	public void setFinishDate(String finishDate) {
		this.finishDate = finishDate;
	}

	@JsonProperty("timeFrame")
	public String getTimeFrame() {
		return timeFrame;
	}

	@JsonProperty("timeFrame")
	public void setTimeFrame(String timeFrame) {
		this.timeFrame = timeFrame;
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
		return new ToStringBuilder(this).append("startDate", startDate).append("finishDate", finishDate)
				.append("timeFrame", timeFrame).append("additionalProperties", additionalProperties).toString();
	}

}
