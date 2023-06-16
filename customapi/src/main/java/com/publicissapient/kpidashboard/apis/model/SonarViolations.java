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

package com.publicissapient.kpidashboard.apis.model;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The type Sonar violations.
 */
@Getter
@Setter
@Data
@NoArgsConstructor
@SuppressWarnings({ "squid:S00100", "squid:S00117", "squid:S00116" }) // to avoid sonar for _class
public class SonarViolations {
	private Long info;
	private Long minor;
	private Long major;
	private Long blocker;
	private Long critical;
	private String sprintBeginDate;
	private String kanbanDate;
	// This field is used when object is inserting in mongo from json data if the
	// field is Object type.
	private String _class; // NOPMD

	/**
	 * Instantiates a new Sonar violations.
	 *
	 * @param value
	 *            the value
	 */
	public SonarViolations(Long value) {
		this.info = value;
		this.minor = value;
		this.major = value;
		this.blocker = value;
		this.critical = value;
	}

	/**
	 * Overridden method of String's toString()
	 *
	 * @return SonarViloations object with
	 *         <ul>
	 *         <li>info</li>
	 *         <li>minor</li>
	 *         <li>major</li>
	 *         <li>blocker</li>
	 *         <li>critical</li>
	 *         </ul>
	 *         whenever toString() method is invoked
	 */
	@Override
	public String toString() {
		return "info=" + info + "\nminor=" + minor + "\nmajor=" + major + "\nblocker=" + blocker + "\ncritical="
				+ critical;
	}

}
