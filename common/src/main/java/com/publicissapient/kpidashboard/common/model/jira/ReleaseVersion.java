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

package com.publicissapient.kpidashboard.common.model.jira;

import org.joda.time.DateTime;
import org.springframework.data.mongodb.core.index.Indexed;

/**
 * @author vijkumar18
 *
 */
public class ReleaseVersion {
	/** The release name. */
	@Indexed
	private String releaseName;

	/** The release date. */
	private DateTime releaseDate;

	public ReleaseVersion(String releaseName, DateTime releaseDate) {
		super();
		this.releaseName = releaseName;
		this.releaseDate = releaseDate;
	}

	public ReleaseVersion() {
		super();
	}

	/**
	 * Gets the release name.
	 *
	 * @return the release name
	 */
	public String getReleaseName() {
		return releaseName;
	}

	/**
	 * Sets the release name.
	 *
	 * @param releaseName
	 *            the new release name
	 */
	public void setReleaseName(String releaseName) {
		this.releaseName = releaseName;
	}

	/**
	 * Gets the release date.
	 *
	 * @return the release date
	 */
	public DateTime getReleaseDate() {
		return releaseDate;
	}

	/**
	 * Sets the release date.
	 *
	 * @param releaseDate
	 *            the new release date
	 */
	public void setReleaseDate(DateTime releaseDate) {
		this.releaseDate = releaseDate;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}
		ReleaseVersion version = (ReleaseVersion) obj;
		return (releaseName == version.releaseName
				|| (releaseName != null && releaseName.equals(version.getReleaseName())))
				&& (releaseDate == version.getReleaseDate()
						|| (releaseDate != null && releaseDate.equals(version.getReleaseDate())));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((releaseName == null) ? 0 : releaseName.hashCode());
		result = prime * result + ((releaseDate == null) ? 0 : releaseDate.hashCode());
		return result;
	}

}
