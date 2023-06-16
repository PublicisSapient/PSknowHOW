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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * The type Access requests data dto.
 */
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
public class AccessRequestsDataDTO {

	private ObjectId id;

	private String username;

	private String status;

	private String reviewComments;

	private List<ProjectsForAccessRequest> projects;

	private List<RoleData> roles;

	private String isDeleted;

	private Date createdDate;

	private Date lastModifiedDate;

	/**
	 * Instantiates a new Access requests data dto.
	 */
	public AccessRequestsDataDTO() {
		this.setId(new ObjectId());
		this.setCreatedDate(new Date());
	}

	/**
	 * Instantiates a new Access requests data dto.
	 *
	 * @param id
	 *            the id
	 * @param username
	 *            the username
	 * @param status
	 *            the status
	 * @param reviewComments
	 *            the review comments
	 * @param projects
	 *            the projects
	 * @param roles
	 *            the roles
	 */
	public AccessRequestsDataDTO(ObjectId id, String username, String status, String reviewComments,
			List<ProjectsForAccessRequest> projects, List<RoleData> roles) {
		super();
		this.id = id;
		this.username = username;
		this.status = status;
		this.reviewComments = reviewComments;
		this.projects = projects;
		this.roles = roles;
	}

	private void resetLastModifiedDate() {
		this.setLastModifiedDate(new Date());
	}

	/**
	 * Sets id.
	 *
	 * @param id
	 *            the id
	 */
	public void setId(ObjectId id) {
		this.id = id;
		resetLastModifiedDate();
	}

	/**
	 * Sets username.
	 *
	 * @param username
	 *            the username
	 */
	public void setUsername(String username) {
		this.username = username;
		resetLastModifiedDate();
	}

	/**
	 * Sets status.
	 *
	 * @param status
	 *            the status
	 */
	public void setStatus(String status) {
		this.status = status;
		resetLastModifiedDate();
	}

	/**
	 * Sets review comments.
	 *
	 * @param reviewComments
	 *            the review comments
	 */
	public void setReviewComments(String reviewComments) {
		this.reviewComments = reviewComments;
		resetLastModifiedDate();
	}

	/**
	 * Sets projects.
	 *
	 * @param projects
	 *            the projects
	 */
	public void setProjects(List<ProjectsForAccessRequest> projects) {
		this.projects = projects;
		resetLastModifiedDate();
	}

	/**
	 * Sets roles.
	 *
	 * @param roles
	 *            the roles
	 */
	public void setRoles(List<RoleData> roles) {
		this.roles = roles;
		resetLastModifiedDate();
	}

	/**
	 * Sets is deleted.
	 *
	 * @param isDeleted
	 *            the is deleted
	 */
	public void setIsDeleted(String isDeleted) {
		this.isDeleted = isDeleted;
		resetLastModifiedDate();
	}

	/**
	 * Sets created date.
	 *
	 * @param createdDate
	 *            the created date
	 */
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
		resetLastModifiedDate();
	}

}
