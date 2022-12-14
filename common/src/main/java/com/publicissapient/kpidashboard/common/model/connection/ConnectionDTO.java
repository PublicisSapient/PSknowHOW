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

package com.publicissapient.kpidashboard.common.model.connection;

import java.util.List;

import org.bson.types.ObjectId;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Represents the ConnectionDTO data.
 */
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConnectionDTO {

	private ObjectId id;
	private String type;
	private String connectionName;
	private boolean cloudEnv;
	private boolean accessTokenEnabled;
	private String baseUrl;
	private String username;
	private String password;
	private String apiEndPoint;
	private String consumerKey;
	private String privateKey;
	private String apiKey;
	private String clientSecretKey;
	private boolean isOAuth;
	private String clientId;
	private String tenantId;
	private String pat;
	private String apiKeyFieldName;
	private String accessToken;
	private boolean offline;
	private String offlineFilePath;
	private String createdBy;
	private boolean connPrivate;
	private String updatedBy;
	private List<String> connectionUser;
	private boolean vault;//GS requirement

	public void setIsOAuth(boolean isOAuth) {
		this.isOAuth = isOAuth;
	}

	public boolean getIsOAuth() {
		return this.isOAuth;
	}

	/**
	 * Checks if the parameter object is equal to the class object
	 *
	 * @param obj object
	 * @return boolean true or false
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (null == obj) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		ConnectionDTO other = (ConnectionDTO) obj;
		if (null == connectionName) {
			if (null != other.connectionName) {
				return false;
			}
		} else if (!connectionName.equals(other.connectionName)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((connectionName == null) ? 0 : connectionName.hashCode());
		return result;
	}

}
