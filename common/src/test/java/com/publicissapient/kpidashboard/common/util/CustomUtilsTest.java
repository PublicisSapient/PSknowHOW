package com.publicissapient.kpidashboard.common.util;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class CustomUtilsTest {

    @Test
    public void testPrimaryKeyGenerator() {
        String key1 = CustomUtils.primaryKeygenerator();
        String key2 = CustomUtils.primaryKeygenerator();

        assertNotNull(key1);
        assertNotNull(key2);
        assertNotEquals(key1, key2);
    }

    @Test
    public void testPrimaryKeyOnName() {
        String name = "Test Name";
        String key = CustomUtils.primaryKeyOnName(name);

        assertEquals("test name", key);
    }

    @Test
    public void testPrimaryKeyOnNameWithMap() {
        Map<String, String> nodeNameMap = new HashMap<>();
        nodeNameMap.put("node1", "Node One");
        nodeNameMap.put("node2", "Node Two");

        String key = CustomUtils.primaryKeyOnName(nodeNameMap);

        assertEquals("node two_node one", key);
    }

    @Test
    public void testPrimaryKeyOnNameWithEmptyMap() {
        Map<String, String> emptyMap = new HashMap<>();

        String key = CustomUtils.primaryKeyOnName(emptyMap);

        assertEquals("", key);
    }
}
