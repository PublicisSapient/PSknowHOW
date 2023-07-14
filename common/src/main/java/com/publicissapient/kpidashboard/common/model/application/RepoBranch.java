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

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The Class RepoBranch.
 */
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RepoBranch {
	private String url = StringUtils.EMPTY;
	private String branch = StringUtils.EMPTY;
	private RepoType type = RepoType.UNKNOWN;

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		RepoBranch that = (RepoBranch) obj;

		return getRepoName().equals(that.getRepoName()) && branch.equals(that.branch);
	}

	@Override
	public int hashCode() {
		int result = url.hashCode();
		result = 31 * result + branch.hashCode();
		return result;
	}

	/**
	 * Gets the repo name.
	 *
	 * @return the repo name
	 */
	protected String getRepoName() {
		try {
			URL temp = new URL(url);
			return temp.getHost() + temp.getPath();
		} catch (MalformedURLException e) {
			return url;
		}

	}

	/**
	 * The Enum RepoType.
	 */
	public enum RepoType {
		SVN, GIT, UNKNOWN;

		/**
		 * From string.
		 *
		 * @param value
		 *            the value
		 * @return the com.publicissapient.kpidashboard.model. repo branch. repo type
		 */
		public static RepoBranch.RepoType fromString(String value) {
			if (value == null) {
				return RepoType.UNKNOWN;
			}
			for (RepoBranch.RepoType repoType : values()) {
				if (repoType.toString().equalsIgnoreCase(value)) {
					return repoType;
				}
			}
			throw new IllegalArgumentException(value + " is not a valid RepoType.");
		}
	}
}
