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

package com.publicissapient.kpidashboard.common.model.scm;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents Commit count.
 */
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommitCount {
	private String date;
	private Long count;
	private Long mergeCount;
	private Map<String, Long> hoverValue;
	private Map<String, Long> mergeHoverValue;

	/**
	 * Instantiates a new Commit count.
	 *
	 * @param date
	 *            the date
	 * @param count
	 *            the count
	 */
	public CommitCount(String date, Long count) {
		this.date = date;
		this.count = count;
	}

}
