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

package com.publicissapient.kpidashboard.common.constant;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum Role {

	BACKEND_DEVELOPER("Backend Developer"), FRONTEND_DEVELOPER("Frontend Developer"), TESTER("Tester");

	private String roleValue;

	Role(String roleValue) {
		this.setRoleValue(roleValue);
	}

	public static Role getRoleByValue(String value) {
		return Arrays.stream(Role.values()).filter(t -> t.getRoleValue().equalsIgnoreCase(value)).findAny().get();
	}

	public static Map<String, String> getAllRoles() {
		return Arrays.stream(Role.values()).collect(Collectors.toMap(Role::name, Role::getRoleValue));
	}

	public String getRoleValue() {
		return roleValue;
	}

	private void setRoleValue(String roleValue) {
		this.roleValue = roleValue;
	}
}
