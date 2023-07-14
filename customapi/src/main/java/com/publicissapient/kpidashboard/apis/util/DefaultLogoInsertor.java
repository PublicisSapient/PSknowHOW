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

package com.publicissapient.kpidashboard.apis.util;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;

import lombok.extern.slf4j.Slf4j;

/**
 * Provides method to insert default logo.
 * 
 * @author prijain3
 *
 */
@Service
@Slf4j
public class DefaultLogoInsertor {

	@Autowired
	private GridFsOperations gridOperations;

	@Autowired
	private CustomApiConfig customApiConfig;

	/**
	 * Inserts default image.
	 */
	public void insertDefaultImage() {
		log.info("Inside insertDefaultImage Default Logo uploaded successfully");

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		DBObject metaData = new BasicDBObject();
		String fileName = Constant.LOGO_FIL_NAME;
		metaData.put("type", "image");

		GridFSFile availableFile = gridOperations
				.findOne(new Query().addCriteria(Criteria.where("filename").is(fileName)));

		if (null == availableFile) {

			log.info("DefaultLogoInsertor: There is no image available in database");
			try (InputStream imageInputStream = classLoader
					.getResourceAsStream(customApiConfig.getApplicationDefaultLogo())) {
				if (null == imageInputStream) {
					log.info("DefaultLogoInsertor: Input stream for default logo is null");
				} else {

					log.info("DefaultLogoInsertor: Inserted default logo");

					gridOperations.store(imageInputStream, fileName, "image/png", metaData);

				}
			} catch (IOException ex) {
				log.error("Error fetching default logo");
			}
		} else {
			log.info("DefaultLogoInsertor: There is already an image available in database");
		}

		log.info("Exit: insertDefaultImage exit");
	}

}
