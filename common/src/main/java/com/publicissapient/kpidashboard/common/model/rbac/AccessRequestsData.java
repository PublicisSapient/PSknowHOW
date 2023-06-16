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

package com.publicissapient.kpidashboard.common.model.rbac;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents the access requests data.
 */
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@Document(collection = "access_requests")
public class AccessRequestsData {

	@Id
	private ObjectId id;

	@Field("username")
	private String username;

	@Field("status")
	private String status;

	@Field("reviewComments")
	private String reviewComments;

	@Field("projects")
	private List<ProjectsForAccessRequest> projects;

	@Field("roles")
	private List<RoleData> roles;

	@Field("isDeleted")
	private String isDeleted;

	@CreatedDate
	private Date createdDate;

	@LastModifiedDate
	private Date lastModifiedDate;

	public AccessRequestsData() {
		this.setId(new ObjectId());
		this.setCreatedDate(new Date());
		this.setLastModifiedDate(new Date());
	}

	public AccessRequestsData(ObjectId id, String username, String status, String reviewComments,
			List<ProjectsForAccessRequest> projects, List<RoleData> roles) {
		super();
		this.id = id;
		this.username = username;
		this.status = status;
		this.reviewComments = reviewComments;
		this.projects = projects;
		this.roles = roles;
	}

}
