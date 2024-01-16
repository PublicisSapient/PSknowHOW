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

package com.publicissapient.kpidashboard.common.repository.application.impl;

import java.util.ArrayList;
import java.util.List;

import com.publicissapient.kpidashboard.common.constant.ProcessorConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCursor;
import com.publicissapient.kpidashboard.common.model.application.ProjectToolConfigProcessorItem;
import com.publicissapient.kpidashboard.common.model.application.Tool;

/**
 * An implementation of {@link ProjectToolConfigRepositoryCustom}
 */
public class ProjectToolConfigRepositoryImpl implements ProjectToolConfigRepositoryCustom {

	@Autowired
	private MongoOperations operations;

	@Override
	public List<Tool> getToolList() {
		BasicDBObject connectionObj = new BasicDBObject("$lookup", new BasicDBObject("from", "connections")
				.append("localField", "connectionId").append("foreignField", "_id").append("as", "connection"));

		BasicDBObject processorItemObj = new BasicDBObject("$lookup", new BasicDBObject("from", "processor_items")
				.append("localField", "_id").append("foreignField", "toolConfigId").append("as", "processorItemList"));
		List<BasicDBObject> pipeline = Lists.newArrayList(connectionObj, processorItemObj);
		AggregateIterable<Document> cursor = operations.getCollection("project_tool_configs").aggregate(pipeline);
		MongoCursor<Document> itr = cursor.iterator();
		List<ProjectToolConfigProcessorItem> returnList = new ArrayList<>();
		while (itr.hasNext()) {
			Document obj = itr.next();
			ProjectToolConfigProcessorItem item = operations.getConverter().read(ProjectToolConfigProcessorItem.class,
					obj);
			returnList.add(item);
		}
		return transform(returnList);
	}

	private List<Tool> transform(List<ProjectToolConfigProcessorItem> list) {
		List<Tool> tools = new ArrayList<>();
		for (ProjectToolConfigProcessorItem item : list) {
			Tool toolObj = new Tool();
			toolObj.setProjectIds(item.getBasicProjectConfigId());
			toolObj.setTool(item.getToolName());
			toolObj.setBranch(item.getBranch());
			toolObj.setRepoSlug(item.getRepoSlug());
			toolObj.setRepositoryName(item.getRepositoryName());
			toolObj.setProcessorItemList(item.getProcessorItemList());
			if (CollectionUtils.isNotEmpty(item.getConnection())) {
				String url = item.getToolName().equals(ProcessorConstants.REPO_TOOLS)
						? item.getConnection().get(0).getHttpUrl()
						: item.getConnection().get(0).getBaseUrl();
				toolObj.setUrl(url);
			}
			tools.add(toolObj);
		}
		return tools;
	}

}
