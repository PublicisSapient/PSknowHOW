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
package com.publicissapient.kpidashboard.apis.pushdata.model.dto;

import java.util.Objects;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BuildDeployFields {
	@NotNull(message = "The jobName must not be null.")
	@JsonProperty("jobName")
	private String jobName;
	@NotNull(message = "The number must not be null.")
	@JsonProperty("number")
	private String number;

	@NotNull(message = "The startTime must not be null.")
	@Min(value = 0, message = "The startTime must be positive.")
	@JsonProperty("startTime")
	private Long startTime;

	@NotNull(message = "The endTime must not be null.")
	@Min(value = 0, message = "The endTime must be positive.")
	@JsonProperty("endTime")
	private Long endTime;

	@NotNull(message = "The duration must not be null.")
	@Min(value = 0, message = "The duration must be positive.")
	@JsonProperty("duration")
	private Long duration;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		BuildDeployFields that = (BuildDeployFields) o;
		return jobName.equals(that.jobName) && number.equals(that.number);
	}

	@Override
	public int hashCode() {
		return Objects.hash(jobName, number);
	}

}
