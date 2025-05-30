/*
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.publicissapient.kpidashboard.common.model.rbac;

import org.springframework.data.mongodb.core.index.Indexed;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccessItem {

	@Indexed(unique = true)
	private String itemId;

	private String itemName;

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		AccessItem that = (AccessItem) o;

		return itemId != null ? itemId.equals(that.itemId) : that.itemId == null;
	}

	@Override
	public int hashCode() {
		return itemId != null ? itemId.hashCode() : 0;
	}
}
