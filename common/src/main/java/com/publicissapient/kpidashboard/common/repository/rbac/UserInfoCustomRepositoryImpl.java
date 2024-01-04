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

package com.publicissapient.kpidashboard.common.repository.rbac;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.rbac.UserInfo;

/**
 * @author anisingh4
 */
@Repository
public class UserInfoCustomRepositoryImpl implements UserInfoCustomRepository {

	@Autowired
	private MongoOperations mongoOperations;

	@Override
	public List<UserInfo> findByProjectAccess(String basicProjectConfigId) {
		Query query = new Query();
		query.addCriteria(
				Criteria.where("projectsAccess.accessNodes.accessLevel").is(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT));
		query.addCriteria(Criteria.where("projectsAccess.accessNodes.accessItems.itemId").is(basicProjectConfigId));
		return mongoOperations.find(query, UserInfo.class);
	}

	/**
	 * Fetch all the admin user of given projID
	 * 
	 * @param basicProjectConfigId
	 *            basicProjectConfigId
	 * @return List<UserInfo>
	 */
	@Override
	public List<UserInfo> findAdminUserOfProject(String basicProjectConfigId) {

		Query query = new Query();

		Criteria accessCriteria = Criteria.where("projectsAccess").elemMatch(
				Criteria.where("role").in("ROLE_PROJECT_ADMIN", "ROLE_SUPERADMIN").and("accessNodes").elemMatch(Criteria
						.where("accessLevel").is("project").and("accessItems.itemId").is(basicProjectConfigId)));

		query.addCriteria(accessCriteria);

		return mongoOperations.find(query, UserInfo.class);
	}

}
