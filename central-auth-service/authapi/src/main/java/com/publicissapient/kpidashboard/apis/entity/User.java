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

package com.publicissapient.kpidashboard.apis.entity;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;

import com.google.common.hash.Hashing;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This class serves as the model for storing credential used for login and
 * Signup.
 *
 * @author Hiren Babariya
 */
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "users")
public class User {
	private static final String HASH_PREFIX = "sha512:";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String username;

	private String samlEmail;

	private String password;

	private String firstName;

	private String lastName;

	private String displayName;

	private long failedLoginAttemptCount;

	private LocalDateTime lastUnsuccessfulLoginTime;

	private String email;

	private boolean approved = false;

	private boolean userVerified = false;

	private String authType;

	private LocalDateTime createdDate;

	private LocalDateTime modifiedDate;

	@SuppressWarnings("java:S107")
	public User(String username, String password, String firstName, String lastName, String displayName, String email,
			LocalDateTime createdDate, String authType, LocalDateTime modifiedDate, boolean userVerified, boolean approved) {
		this.username = username;
		this.password = hash(password);
		this.firstName = firstName;
		this.lastName = lastName;
		this.displayName = displayName;
		this.email = email;
		this.createdDate = createdDate;
		this.authType = authType;
		this.modifiedDate = modifiedDate;
		this.userVerified = userVerified;
		this.approved = approved;
	}

	/**
	 * Hash string.
	 *
	 * @param password
	 *          the password
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
	 *          the password
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
	 *          the password
	 * @return the boolean
	 */
	public boolean checkPassword(String password) {
		return hash(this.password).equals(hash(password));
	}

	@Override
	public String toString() {
		return "User{" + "id=" + id + ", username='" + username + '\'' + ", password='" + password + '\'' +
				", firstName='" + firstName + '\'' + ", lastName='" + lastName + '\'' + ", displayName='" + displayName + '\'' +
				", failedLoginAttemptCount=" + failedLoginAttemptCount + ", lastUnsuccessfulLoginTime=" +
				lastUnsuccessfulLoginTime + ", email='" + email + '\'' + ", approved=" + approved + ", userVerified=" +
				userVerified + ", authType='" + authType + '\'' + ", createdDate=" + createdDate + ", modifiedDate=" +
				modifiedDate + '}';
	}
}
