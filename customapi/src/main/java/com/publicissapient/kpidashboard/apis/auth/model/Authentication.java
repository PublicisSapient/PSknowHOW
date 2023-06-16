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

package com.publicissapient.kpidashboard.apis.auth.model;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.hash.Hashing;
import com.publicissapient.kpidashboard.common.model.generic.BasicModel;
import com.publicissapient.kpidashboard.common.model.rbac.UserRoleData;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This class serves as the model for storing credential used for login and
 * Signup.
 */
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "authentication")
public class Authentication extends BasicModel {
	/**
	 * The Hash prefix.
	 */
	private static final String HASH_PREFIX = "sha512:";

	@Indexed(unique = true)
	private String username;

	private String password;

	private Integer loginAttemptCount;

	@JsonSerialize(using = CustomDateSerializer.class)
	private DateTime lastUnsuccessfulLoginTime;

	private String email;

	private List<UserRoleData> userRoleAssigned;

	private String userRole;

	private boolean approved;

	/**
	 * Instantiates a new Authentication.
	 *
	 * @param username
	 *            the username
	 * @param password
	 *            the password
	 * @param email
	 *            the email
	 */
	public Authentication(String username, String password, String email) {
		super();
		this.username = username;
		this.password = hash(password);
		this.email = email;

	}

	/**
	 * Hash string.
	 *
	 * @param password
	 *            the password
	 * @return the string
	 */
	public static String hash(String password) {
		if (StringUtils.isNotBlank(password) && !password.startsWith(HASH_PREFIX)) {
			return HASH_PREFIX + Hashing.sha512().hashString(password, StandardCharsets.UTF_8).toString();
		}
		return password;
	}

	/**
	 * Sets password.
	 *
	 * @param password
	 *            the password
	 */
	public void setPassword(String password) {
		this.password = hash(password);
	}

	/**
	 * Is hashed boolean.
	 *
	 * @return the boolean
	 */
	public boolean isHashed() {
		boolean isHashed = false;
		if (StringUtils.isNotBlank(password)) {
			isHashed = password.startsWith(HASH_PREFIX);
		}
		return isHashed;
	}

	/**
	 * Check password boolean.
	 *
	 * @param password
	 *            the password
	 * @return the boolean
	 */
	public boolean checkPassword(String password) {
		return hash(this.password).equals(hash(password));
	}

	@Override
	public String toString() {
		return "Authentication [username=" + username + ", password=" + password + ", approved=" + approved + " ]";
	}

}
