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

package com.publicissapient.kpidashboard.apis.common.service.impl;


import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.data.AccountHierarchyFilterDataFactory;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.filter.service.AccountHierarchyServiceImpl;
import com.publicissapient.kpidashboard.apis.filter.service.AccountHierarchyServiceKanbanImpl;
import com.publicissapient.kpidashboard.apis.model.AccountHierarchyData;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.apis.util.CommonUtils;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterCategory;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectHierarchy;
import com.publicissapient.kpidashboard.common.repository.application.AdditionalFilterCategoryRepository;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelService;
import com.publicissapient.kpidashboard.common.service.ProjectHierarchyService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.SimpleValueWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CacheServiceImplTest {

    @Mock
    private HierarchyLevelService hierarchyLevelService;

    @Mock
    private AccountHierarchyServiceImpl accountHierarchyService;

    @Mock
    private AccountHierarchyServiceKanbanImpl accountHierarchyServiceKanban;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private ConfigHelperService configHelperService;

    @Mock
    private AdditionalFilterCategoryRepository additionalFilterCategoryRepository;

    @InjectMocks
    private CacheServiceImpl cacheService;

    @Mock
    private ProjectHierarchyService projectHierarchyService;

    @Mock
    private Cache cache;

    @Test
    public void testClearCache_ValidCacheName_CacheCleared() {
        String cacheName = "exampleCache";
        Cache cache = mock(Cache.class);
        when(cacheManager.getCache(cacheName)).thenReturn(cache);

        cacheService.clearCache(cacheName);

        verify(cache, times(1)).clear();
        verify(cache, times(1)).evict(cacheName);
    }

    @Test
    public void testCacheAccountHierarchyData_ValidInput_ReturnsData() {
        when(accountHierarchyService.createHierarchyData()).thenReturn(new ArrayList<>());
        Object result = cacheService.cacheAccountHierarchyData();
        assertNotNull(result);
    }
    @Test
    public void testCacheSprintHierarchyData_ValidInput_ReturnsData() {
        AccountHierarchyFilterDataFactory accountHierarchyFilterDataFactory = AccountHierarchyFilterDataFactory
                .newInstance("/json/default/project_hierarchy_filter_data.json");
        List<AccountHierarchyData> accountHierarchyDataList;
        cacheService.accountHierarchyDataList = accountHierarchyFilterDataFactory.getAccountHierarchyDataList();
        Object result = cacheService.cacheSprintLevelData();
        assertNotNull(result);
    }

    @Test
    public void testCacheAccountHierarchyKanbanData_ValidInput_ReturnsData() {
        when(accountHierarchyServiceKanban.createHierarchyData()).thenReturn(new ArrayList<>());
        Object result = cacheService.cacheAccountHierarchyKanbanData();
        assertNotNull(result);
    }

    @Test
    public void testSetIntoApplicationCache_ValidInput_DataSetIntoCache() {
        String key = "exampleKey";
        String value = "exampleValue";
        Cache cache = mock(Cache.class);
        when(cacheManager.getCache("requestTrackerCache")).thenReturn(cache);
        cacheService.setIntoApplicationCache(key, value);
        verify(cache, times(1)).put(key, value);
    }

    @Test
    public void testGetFromApplicationCache_ValidInput_ReturnsData() {
        String[] keyList = {"key1", "key2"};
        String kpiSource = "JIRA";
        Integer groupId = 123;
        List<String> sprintIncluded = Arrays.asList("sprint1", "sprint2");

        Cache cache = mock(Cache.class);
        when(cacheManager.getCache("jiraKpiCache")).thenReturn(cache);
        when(cache.get(any(String.class))).thenReturn(new SimpleValueWrapper("jira"));

        Object result = cacheService.getFromApplicationCache(keyList, kpiSource, groupId, sprintIncluded);

        assertNotNull(result);
    }

    @Test
    public void testGetFullHierarchyLevel_ValidInput_ReturnsData() {
        when(hierarchyLevelService.getFullHierarchyLevels(false)).thenReturn(new ArrayList<>());
        List<HierarchyLevel> result = cacheService.getFullHierarchyLevel();
        assertNotNull(result);
    }

    @Test
    public void testGetFullKanbanHierarchyLevel_ValidInput_ReturnsData() {
        when(hierarchyLevelService.getFullHierarchyLevels(true)).thenReturn(new ArrayList<>());
        List<HierarchyLevel> result = cacheService.getFullKanbanHierarchyLevel();
        assertNotNull(result);
    }

    @Test
    public void testGetFullHierarchyLevelMap_ValidInput_ReturnsData() {
        // Arrange
        List<HierarchyLevel> hierarchyLevels = new ArrayList<>();
        when(hierarchyLevelService.getFullHierarchyLevels(false)).thenReturn(hierarchyLevels);
        Map<String, HierarchyLevel> result = cacheService.getFullHierarchyLevelMap();
        assertNotNull(result);
        assertEquals(hierarchyLevels.size(), result.size());
    }

    @Test
    public void testGetFullKanbanHierarchyLevelMap_ValidInput_ReturnsData() {
        List<HierarchyLevel> kanbanHierarchyLevels = new ArrayList<>();
        when(hierarchyLevelService.getFullHierarchyLevels(true)).thenReturn(kanbanHierarchyLevels);
        Map<String, HierarchyLevel> result = cacheService.getFullKanbanHierarchyLevelMap();
        assertNotNull(result);
        assertEquals(kanbanHierarchyLevels.size(), result.size());
    }

    @Test
    public void testGetAdditionalFilterHierarchyLevel_ValidInput_ReturnsData() {
        List<AdditionalFilterCategory> additionalFilterCategories = new ArrayList<>();
        when(additionalFilterCategoryRepository.findAll()).thenReturn(additionalFilterCategories);
        Map<String, AdditionalFilterCategory> result = cacheService.getAdditionalFilterHierarchyLevel();

        assertNotNull(result);
        assertEquals(additionalFilterCategories.size(), result.size());
    }

    @Test
    public void testCacheProjectConfigMapData_ValidInput_ReturnsData() {
        doNothing().when(configHelperService).loadConfigData();
        cacheService.cacheProjectConfigMapData();
        verify(configHelperService).loadConfigData();
    }
    @Test
    public void testUpdateCacheProjectConfigMapData_ValidInput_ReturnsData() {
        doNothing().when(configHelperService).loadConfigData();
        cacheService.cacheProjectConfigMapData();
        verify(configHelperService).loadConfigData();
    }

    @Test
    public void testCacheFieldMappingMapData_ValidInput_ReturnsData() {
        doNothing().when(configHelperService).loadConfigData();
        cacheService.cacheFieldMappingMapData();
        verify(configHelperService).loadConfigData();
    }

    @Test
    public void testCacheToolConfigMapData_ValidInput_ReturnsData() {
        doNothing().when(configHelperService).loadToolConfig();
        cacheService.cacheToolConfigMapData();
        verify(configHelperService).loadToolConfig();
    }

    @Test
    public void testCacheProjectToolConfigMapData_ValidInput_ReturnsData() {
        doNothing().when(configHelperService).loadProjectToolConfig();
        cacheService.cacheProjectToolConfigMapData();
        verify(configHelperService).loadProjectToolConfig();
    }

    // Add more test cases for the remaining methods...

    @Test
    public void testClearAllCache_CacheManagerHasCaches_CachesCleared() {
        // Arrange
        List<String> cacheNames = Arrays.asList("cache1", "cache2");
        when(cacheManager.getCacheNames()).thenReturn(cacheNames);

        // Mock cache and its behavior
        Cache cache1 = mock(Cache.class);
        Cache cache2 = mock(Cache.class);
        when(cacheManager.getCache("cache1")).thenReturn(cache1);
        when(cacheManager.getCache("cache2")).thenReturn(cache2);

        // Act
        cacheService.clearAllCache();

        // Assert
        verify(cache1, times(1)).clear();
        verify(cache2, times(1)).clear();
    }

    @Test
    public void testSetIntoApplicationCache_WithArrayInput_DataSetIntoCache() {
        // Arrange
        String[] keyList = {"key1", "key2"};
        Object value = "exampleValue";
        String kpiSource = "JIRA";
        Integer groupId = 123;
        List<String> sprintIncluded = Arrays.asList("sprint1", "sprint2");
        Cache cache = mock(Cache.class);
        when(cacheManager.getCache("jiraKpiCache")).thenReturn(cache);

        cacheService.setIntoApplicationCache(keyList, value, kpiSource, groupId, sprintIncluded);

        verify(cache, times(1)).put("key1key2JIRA123sprint1sprint2", value);
    }


    @Test
    public void testGetAllProjectHierarchy() {
        List<ProjectHierarchy> expectedHierarchies = Arrays.asList(new ProjectHierarchy());
        // Mock the service call
        when(projectHierarchyService.findAll()).thenReturn(expectedHierarchies);
        // Verify interactions and assertions
        assertEquals(expectedHierarchies, cacheService.getAllProjectHierarchy());
    }

    @Test
    public void testGetFromApplicationCache_CacheHit() {
        // Mock data
        String key = "testKey";
        String expectedValue = "testValue";
        String cacheName = "testCache";

        // Mock the cache and value wrapper
        SimpleValueWrapper valueWrapper = new SimpleValueWrapper(expectedValue);
        when(cacheManager.getCache(cacheName)).thenReturn(cache);
        when(cache.get(key)).thenReturn(valueWrapper);

        // Mock CommonUtils.getCacheName
        try (var mockedStatic = mockStatic(CommonUtils.class)) {
            mockedStatic.when(() -> CommonUtils.getCacheName(Constant.KPI_REQUEST_TRACKER_ID_KEY))
                    .thenReturn(cacheName);

            // Call the method
            String actualValue = cacheService.getFromApplicationCache(key);

            // Verify interactions and assertions
            assertEquals(expectedValue, actualValue);
            verify(cacheManager, times(1)).getCache(cacheName);
            verify(cache, times(1)).get(key);
        }
    }

    @Test
    public void testGetFromApplicationCache_CacheMiss() {
        // Mock data
        String key = "testKey";
        String cacheName = "testCache";

        // Mock the cache returning null
        when(cacheManager.getCache(cacheName)).thenReturn(cache);
        when(cache.get(key)).thenReturn(null);

        // Mock CommonUtils.getCacheName
        try (var mockedStatic = mockStatic(CommonUtils.class)) {
            mockedStatic.when(() -> CommonUtils.getCacheName(Constant.KPI_REQUEST_TRACKER_ID_KEY))
                    .thenReturn(cacheName);

            // Call the method
            String actualValue = cacheService.getFromApplicationCache(key);

            // Verify interactions and assertions
            assertEquals("", actualValue); // Cache miss returns an empty string
            verify(cacheManager, times(1)).getCache(cacheName);
            verify(cache, times(1)).get(key);
        }
    }

    @Test
    public void testGetFromApplicationCache_NullCache() {
        // Mock data
        String key = "testKey";
        String cacheName = "testCache";

        // Mock the cacheManager returning null
        when(cacheManager.getCache(cacheName)).thenReturn(null);

        // Mock CommonUtils.getCacheName
        try (var mockedStatic = mockStatic(CommonUtils.class)) {
            mockedStatic.when(() -> CommonUtils.getCacheName(Constant.KPI_REQUEST_TRACKER_ID_KEY))
                    .thenReturn(cacheName);

            // Call the method
            String actualValue = cacheService.getFromApplicationCache(key);

            // Verify interactions and assertions
            assertEquals("", actualValue); // Null cache returns an empty string
            verify(cacheManager, times(1)).getCache(cacheName);
            verifyNoInteractions(cache); // Ensure the cache itself is not interacted with
        }
    }


    @Test
    public void testCacheBoardMetaDataMapData() {
        // Mocked data
        Object expectedConfigMap = new Object(); // Replace with the expected return type
        String cacheKey = CommonConstant.CACHE_BOARD_META_DATA_MAP;

        // Mock the service methods
        doNothing().when(configHelperService).loadBoardMetaData();
        when(configHelperService.getConfigMapData(cacheKey)).thenReturn(expectedConfigMap);

        // Call the method
        Object actualConfigMap = cacheService.cacheBoardMetaDataMapData();

        // Verify interactions and assertions
        assertEquals(expectedConfigMap, actualConfigMap);
        verify(configHelperService, times(1)).loadBoardMetaData();
        verify(configHelperService, times(1)).getConfigMapData(cacheKey);
    }

}