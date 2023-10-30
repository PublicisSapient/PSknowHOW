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

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public final class MongockUtil {

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
}
