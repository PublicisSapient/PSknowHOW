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

package com.publicissapient.kpidashboard.common.repository.comments;//NOPMD

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.model.comments.KpiCommentsHistory;

@Service
public class KpiCommentHistoryRepositoryImpl implements KpiCommentHistoryRepositoryCustom {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public void markCommentDelete(String commentId) {
		Query query = Query.query(Criteria.where("commentsInfo.commentId").is(commentId));
		Update update = new Update().set("commentsInfo.$.isDeleted", true);
		mongoTemplate.updateMulti(query, update, KpiCommentsHistory.class);
	}

}
