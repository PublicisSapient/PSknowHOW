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

package com.publicissapient.kpidashboard.common.model.application;

import org.joda.time.DateTime;

import com.google.common.base.Objects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Project version.
 */
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProjectVersion {
	private Long id;
	private String description;
	private String name;
	private boolean isArchived;
	private boolean isReleased;
	private DateTime releaseDate;
	private DateTime startDate;

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ProjectVersion) {
			ProjectVersion that = (ProjectVersion) obj;
			return Objects.equal(this.id, that.id) && Objects.equal(this.name, that.name)
					&& Objects.equal(this.description, that.description)
					&& Objects.equal(this.isArchived, that.isArchived)
					&& Objects.equal(this.isReleased, that.isReleased)
					&& Objects.equal(this.releaseDate, that.releaseDate)
					&& Objects.equal(this.startDate, that.startDate);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id, name, description, isArchived, isReleased, releaseDate, startDate);
	}

}
