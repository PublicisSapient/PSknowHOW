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
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

/**
 * Represents boards config - user/ project admin can customize KPIs config on each board
 * if basicProjectConfigId is there then it is proj level config set by admin/superAdmin
 * else if basicProjectConfigId is null then it is user level config details
 * @author yasbano
 *
 */

@Data
@Document(collection = "user_board_config")
public class UserBoardConfig {
	@Id
	private ObjectId id;
	private String username;
	private String basicProjectConfigId;// will be used to save proj level configs
	private List<Board> scrum;
	private List<Board> kanban;
	private List<Board> others;

}
