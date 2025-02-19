/*
 *   Copyright 2014 CapitalOne, LLC.
 *   Further development Copyright 2022 Sapient Corporation.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.publicissapient.kpidashboard.common.model.application.dto;

import lombok.Data;

/**
 * Dto for azure sprint refresh log.
 *
 * @author shunaray
 */
@Data
public class SprintRefreshLogDTO {
	private String sprintName;
	private String sprintStartDate;
	private String sprintEndDate;
	private long refreshedOn;
	private String refreshBy;
}
