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
package com.publicissapient.kpidashboard.apis.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoCollection;

public final class MongockUtil {

	private MongockUtil() {
	}

	public static MongoCollection<Document> getOrCreateCollection(MongoTemplate mongoTemplate, String collectionName) {
		if (!mongoTemplate.collectionExists(collectionName))
			return mongoTemplate.createCollection(collectionName);
		return mongoTemplate.getCollection(collectionName);
	}

	public static void saveListToDB(List<?> dataList, String collectionName, MongoTemplate mongoTemplate) {
		MongoCollection<Document> collection = getOrCreateCollection(mongoTemplate, collectionName);
		if (collection.countDocuments() == 0) {
			List<Document> documentList = new ArrayList<>();
			dataList.forEach(data -> {
				Document document = new Document();
				for (Field field : data.getClass().getDeclaredFields()) {
					field.setAccessible(true);
					try {
						Object value = field.get(data);
						document.append(field.getName(), value);
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
				documentList.add(document);
			});
			mongoTemplate.insert(documentList, collectionName);
		}
	}

	/**
	 * Method to create a fieldMappingStructure
	 * 
	 * @param fieldName
	 *            fieldName
	 * @param fieldLabel
	 *            fieldLabel
	 * @param section
	 *            section
	 * @param fieldCategory
	 *            fieldCategory
	 * @param fieldType
	 *            fieldType
	 * @param tooltipDefinition
	 *            tooltipDefinition
	 * @return Document
	 */
	public static Document createFieldMapping(String fieldName, String fieldLabel, String section, String fieldCategory,
			String fieldType, String tooltipDefinition) {
		return new Document().append("fieldName", fieldName).append("fieldLabel", fieldLabel).append("section", section)
				.append("fieldType", fieldType).append("fieldCategory", fieldCategory)
				.append("tooltip", new Document("definition", tooltipDefinition));
	}

}
