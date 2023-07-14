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

package com.publicissapient.kpidashboard.common.repository.connection;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import com.publicissapient.kpidashboard.common.model.connection.Connection;

/**
 * @author dilip Repository for {@link Connection}.
 */

public interface ConnectionRepository
		extends MongoRepository<Connection, ObjectId>, QuerydslPredicateExecutor<Connection> {

	/**
	 * Find by type Connection.
	 *
	 * @param type
	 *            the Connection
	 * @return the Connection
	 */

	List<Connection> findByType(String type);

	/**
	 * s Returns connection from persistence store by id
	 *
	 * @param objectId
	 *            id
	 * @return Connection
	 */
	Optional<Connection> findById(ObjectId objectId);

	Connection findByConnectionName(String connectionName);

	List<Connection> findByTypeAndConnPrivate(String type, Boolean connPrivate);

	/**
	 * s Returns List Of connection based on connection id's
	 *
	 * @param connectionId
	 *            id
	 * @return Connection
	 */
	List<Connection> findByIdIn(Set<ObjectId> connectionId);

	/**
	 *
	 * @return
	 */
	@Query(value = "{}", fields = "{ 'password' : 0,'apiKey':0,'accessToken':0,'privateKey':0,'pat':0,'consumerKey':0,'patOAuthToken':0}")
	List<Connection> findAllWithoutSecret();

}
