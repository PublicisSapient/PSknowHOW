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

package com.publicissapient.kpidashboard.common.repository.application;

import com.mongodb.BasicDBObject;
import com.publicissapient.kpidashboard.common.model.application.Build;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The interface Build repository custom.
 *
 * @author anisingh4
 */
public interface BuildRepositoryCustom {

    /**
     * Find build list account to sprint list.
     *
     * @param dbObjectList the db object list
     * @return the list of Build
     */
    List<Build> findBuildListAccToSprint(List<BasicDBObject> dbObjectList);

    /**
     * Find build list using date wise
     *
     * @param mapOfFilters
     * @param processorItemIdList
     * @param startDate
     * @param endDate
     * @return the list of Build
     */
    List<Build> findBuildList(Map<String, List<String>> mapOfFilters , Set<ObjectId> processorItemIdList , String startDate , String endDate);
}
