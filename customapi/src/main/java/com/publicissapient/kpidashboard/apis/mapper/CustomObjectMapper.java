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

package com.publicissapient.kpidashboard.apis.mapper;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Custom object mapper that knows how to serialize Mongo ObjectIds.
 */
public class CustomObjectMapper extends ObjectMapper {
	private static final long serialVersionUID = 2035695746790240402L;

	/**
	 * Default Constructor
	 */
	public CustomObjectMapper() {
		super();
		SimpleModule module = new SimpleModule("ObjectIdModule");
		module.addSerializer(ObjectId.class, new ObjectIdSerializer());
		this.registerModule(module);
	}
}
