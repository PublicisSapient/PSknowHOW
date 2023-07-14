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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents the user role data.
 */
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@Document(collection = "user_roles")
public class UserRoleData {

	@Id
	private ObjectId id;

	@Field("projectId")
	private String projectId;

	@Field("projectName")
	private String projectName;

	@Field("accountId")
	private String accountId;

	@Field("accountName")
	private String accountName;

	@Field("createdDate")
	private Date createdDate;

	@Field("lastModifiedDate")
	private Date lastModifiedDate;

	@Field("isDeleted")
	private String isDeleted;

	@Field("roles")
	private List<RoleData> roles;

	public UserRoleData() {
		this.id = new ObjectId();
		this.createdDate = new Date();
		this.setLastModifiedDate(new Date());
		roles = new ArrayList<>();
	}

}
