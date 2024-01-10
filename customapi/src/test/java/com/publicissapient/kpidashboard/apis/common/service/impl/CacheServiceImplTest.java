package com.publicissapient.kpidashboard.apis.common.service.impl;


import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.filter.service.AccountHierarchyServiceImpl;
import com.publicissapient.kpidashboard.apis.filter.service.AccountHierarchyServiceKanbanImpl;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterCategory;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.repository.application.AdditionalFilterCategoryRepository;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.SimpleValueWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

}