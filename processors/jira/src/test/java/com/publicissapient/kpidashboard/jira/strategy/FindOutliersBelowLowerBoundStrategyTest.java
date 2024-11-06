/*
 *   Copyright 2014 CapitalOne, LLC.
 *   Further development Copyright 2022 Sapient Corporation.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.publicissapient.kpidashboard.jira.strategy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
class FindOutliersBelowLowerBoundStrategyTest {

    @Test
    void findOutliers() {
        FindOutliersBelowLowerBoundStrategy findOutliersBelowLowerBoundStrategy = new FindOutliersBelowLowerBoundStrategy();
        assertNotNull(findOutliersBelowLowerBoundStrategy);
    }

    @Test
    void findOutliersWithValidData() {
        FindOutliersBelowLowerBoundStrategy strategy = new FindOutliersBelowLowerBoundStrategy();
        ObjectId projectId = new ObjectId();
        Map<ObjectId, Map<String, List<String>>> projectSprintIssuesMap = new HashMap<>();
        Map<String, List<String>> sprintIssues = new HashMap<>();
        sprintIssues.put("Sprint1", Arrays.asList("Issue1", "Issue2"));
        sprintIssues.put("Sprint2", List.of("Issue1"));
        sprintIssues.put("Sprint3", Arrays.asList("Issue1", "Issue2", "Issue3"));
        projectSprintIssuesMap.put(projectId, sprintIssues);

        Map<String, List<String>> outliers = strategy.findOutliers(projectId, projectSprintIssuesMap);

        assertEquals(0, outliers.size());
    }

    @Test
    void findOutliersWithNoData() {
        FindOutliersBelowLowerBoundStrategy strategy = new FindOutliersBelowLowerBoundStrategy();
        ObjectId projectId = new ObjectId();
        Map<ObjectId, Map<String, List<String>>> projectSprintIssuesMap = new HashMap<>();

        Map<String, List<String>> outliers = strategy.findOutliers(projectId, projectSprintIssuesMap);

        assertTrue(outliers.isEmpty());
    }

    @Test
    void findOutliersWithEmptySprintIssues() {
        FindOutliersBelowLowerBoundStrategy strategy = new FindOutliersBelowLowerBoundStrategy();
        ObjectId projectId = new ObjectId();
        Map<ObjectId, Map<String, List<String>>> projectSprintIssuesMap = new HashMap<>();
        projectSprintIssuesMap.put(projectId, Collections.emptyMap());

        Map<String, List<String>> outliers = strategy.findOutliers(projectId, projectSprintIssuesMap);

        assertTrue(outliers.isEmpty());
    }

    @Test
    void findOutliersWithSingleSprint() {
        FindOutliersBelowLowerBoundStrategy strategy = new FindOutliersBelowLowerBoundStrategy();
        ObjectId projectId = new ObjectId();
        Map<ObjectId, Map<String, List<String>>> projectSprintIssuesMap = new HashMap<>();
        Map<String, List<String>> sprintIssues = new HashMap<>();
        sprintIssues.put("Sprint1", Arrays.asList("Issue1", "Issue2"));
        projectSprintIssuesMap.put(projectId, sprintIssues);

        Map<String, List<String>> outliers = strategy.findOutliers(projectId, projectSprintIssuesMap);

        assertTrue(outliers.isEmpty());
    }
}