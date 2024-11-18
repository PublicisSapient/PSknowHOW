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
package com.publicissapient.kpidashboard.common.model.userboardconfig;

import java.util.List;

import org.bson.types.ObjectId;

import lombok.Data;

/**
 * DTO of userboardconfig
 * 
 * @author yasbano
 *
 */
@Data
public class UserBoardConfigDTO {
	private ObjectId id;
	private String username;
	private String basicProjectConfigId;
	private List<BoardDTO> scrum;
	private List<BoardDTO> kanban;
	private List<BoardDTO> others;
}
