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

import java.util.Arrays;

/**
 * @author pkum34 Mapping of Platform id with Platform Name
 */
public enum PlatformCode {

	ANDROID("1", "Android"), IOS("2", "iOS"), INVALID("3", "Invalid");

	private String platformdId;

	private String platformName;

	PlatformCode(String platformdId, String platformName) {
		this.platformdId = platformdId;
		this.platformName = platformName;
	}

	public static PlatformCode getDevicePlatform(String platformdId) {

		return Arrays.asList(PlatformCode.values()).stream()
				.filter(platId -> platId.getPlatformdId().equalsIgnoreCase(platformdId)).findAny().orElse(INVALID);
	}

	public String getPlatformdId() {
		return platformdId;
	}

	public String getPlatformName() {
		return platformName;
	}

}
