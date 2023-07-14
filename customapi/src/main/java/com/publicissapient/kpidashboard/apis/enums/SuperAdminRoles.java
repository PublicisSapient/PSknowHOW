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

package com.publicissapient.kpidashboard.apis.enums;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public enum SuperAdminRoles {
	ROLE_SUPERADMIN;

	public static List<String> getAdminRoles() {

		return EnumSet.allOf(SuperAdminRoles.class).stream().map(SuperAdminRoles::name).collect(Collectors.toList());
	}
}
