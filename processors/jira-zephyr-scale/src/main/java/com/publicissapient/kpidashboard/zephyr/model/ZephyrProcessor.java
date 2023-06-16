
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

package com.publicissapient.kpidashboard.zephyr.model;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import com.publicissapient.kpidashboard.common.constant.ProcessorType;
import com.publicissapient.kpidashboard.common.model.generic.Processor;

/**
 * The type Zephyr processor.
 */
public class ZephyrProcessor extends Processor {

	/**
	 * Prototype zephyr processor.
	 *
	 * @return the zephyr processor
	 */
	public static ZephyrProcessor prototype() {
		ZephyrProcessor protoType = new ZephyrProcessor();
		protoType.setProcessorName(ProcessorConstants.ZEPHYR);
		protoType.setOnline(true);
		protoType.setActive(true);
		protoType.setLastSuccess(false);
		protoType.setProcessorType(ProcessorType.TESTING_TOOLS);
		protoType.setUpdatedTime(System.currentTimeMillis());

		return protoType;
	}
}
