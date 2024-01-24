package com.publicissapient.kpidashboard.common.model.application.dto;

import org.junit.jupiter.api.Test;

import java.util.TreeSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HierarchyLevelDTOTest {
    /**
     * Method under test: {@link HierarchyLevelDTO#canEqual(Object)}
     */
    @Test
    public void testCanEqual() {
        assertFalse((new HierarchyLevelDTO()).canEqual("Other"));
    }

    /**
     * Method under test: {@link HierarchyLevelDTO#canEqual(Object)}
     */
    @Test
    public void testCanEqual2() {
        HierarchyLevelDTO hierarchyLevelDTO = new HierarchyLevelDTO();

        HierarchyLevelDTO hierarchyLevelDTO1 = new HierarchyLevelDTO();
        hierarchyLevelDTO1.setHierarchyLevelId("42");
        hierarchyLevelDTO1.setHierarchyLevelName("Hierarchy Level Name");
        hierarchyLevelDTO1.setLevel(3);
        hierarchyLevelDTO1.setSuggestions(new TreeSet<>());
        assertTrue(hierarchyLevelDTO.canEqual(hierarchyLevelDTO1));
    }

    /**
     * Methods under test:
     *
     * <ul>
     * <li>default or parameterless constructor of {@link HierarchyLevelDTO}
     * <li>{@link HierarchyLevelDTO#setHierarchyLevelId(String)}
     * <li>{@link HierarchyLevelDTO#setHierarchyLevelName(String)}
     * <li>{@link HierarchyLevelDTO#setLevel(int)}
     * <li>{@link HierarchyLevelDTO#setSuggestions(TreeSet)}
     * <li>{@link HierarchyLevelDTO#toString()}
     * <li>{@link HierarchyLevelDTO#getHierarchyLevelId()}
     * <li>{@link HierarchyLevelDTO#getHierarchyLevelName()}
     * <li>{@link HierarchyLevelDTO#getLevel()}
     * <li>{@link HierarchyLevelDTO#getSuggestions()}
     * </ul>
     */
    @Test
    public void testConstructor() {
        HierarchyLevelDTO actualHierarchyLevelDTO = new HierarchyLevelDTO();
        actualHierarchyLevelDTO.setHierarchyLevelId("42");
        actualHierarchyLevelDTO.setHierarchyLevelName("Hierarchy Level Name");
        actualHierarchyLevelDTO.setLevel(1);
        TreeSet<String> stringSet = new TreeSet<>();
        actualHierarchyLevelDTO.setSuggestions(stringSet);
        String actualToStringResult = actualHierarchyLevelDTO.toString();
        assertEquals("42", actualHierarchyLevelDTO.getHierarchyLevelId());
        assertEquals("Hierarchy Level Name", actualHierarchyLevelDTO.getHierarchyLevelName());
        assertEquals(1, actualHierarchyLevelDTO.getLevel());
        assertSame(stringSet, actualHierarchyLevelDTO.getSuggestions());
        assertEquals("HierarchyLevelDTO(level=1, hierarchyLevelId=42, hierarchyLevelName=Hierarchy Level Name,"
                + " suggestions=[])", actualToStringResult);
    }

    /**
     * Method under test: {@link HierarchyLevelDTO#equals(Object)}
     */
    @Test
    public void testEquals() {
        HierarchyLevelDTO hierarchyLevelDTO = new HierarchyLevelDTO();
        hierarchyLevelDTO.setHierarchyLevelId("42");
        hierarchyLevelDTO.setHierarchyLevelName("Hierarchy Level Name");
        hierarchyLevelDTO.setLevel(1);
        hierarchyLevelDTO.setSuggestions(new TreeSet<>());
        assertNotEquals(hierarchyLevelDTO, null);
    }

    /**
     * Method under test: {@link HierarchyLevelDTO#equals(Object)}
     */
    @Test
    public void testEquals2() {
        HierarchyLevelDTO hierarchyLevelDTO = new HierarchyLevelDTO();
        hierarchyLevelDTO.setHierarchyLevelId("42");
        hierarchyLevelDTO.setHierarchyLevelName("Hierarchy Level Name");
        hierarchyLevelDTO.setLevel(1);
        hierarchyLevelDTO.setSuggestions(new TreeSet<>());
        assertNotEquals(hierarchyLevelDTO, "Different type to HierarchyLevelDTO");
    }

    /**
     * Methods under test:
     *
     * <ul>
     * <li>{@link HierarchyLevelDTO#equals(Object)}
     * <li>{@link HierarchyLevelDTO#hashCode()}
     * </ul>
     */
    @Test
    public void testEquals3() {
        HierarchyLevelDTO hierarchyLevelDTO = new HierarchyLevelDTO();
        hierarchyLevelDTO.setHierarchyLevelId("42");
        hierarchyLevelDTO.setHierarchyLevelName("Hierarchy Level Name");
        hierarchyLevelDTO.setLevel(1);
        hierarchyLevelDTO.setSuggestions(new TreeSet<>());
        assertEquals(hierarchyLevelDTO, hierarchyLevelDTO);
        int expectedHashCodeResult = hierarchyLevelDTO.hashCode();
        assertEquals(expectedHashCodeResult, hierarchyLevelDTO.hashCode());
    }

    /**
     * Methods under test:
     *
     * <ul>
     * <li>{@link HierarchyLevelDTO#equals(Object)}
     * <li>{@link HierarchyLevelDTO#hashCode()}
     * </ul>
     */
    @Test
    public void testEquals4() {
        HierarchyLevelDTO hierarchyLevelDTO = new HierarchyLevelDTO();
        hierarchyLevelDTO.setHierarchyLevelId("42");
        hierarchyLevelDTO.setHierarchyLevelName("Hierarchy Level Name");
        hierarchyLevelDTO.setLevel(1);
        hierarchyLevelDTO.setSuggestions(new TreeSet<>());

        HierarchyLevelDTO hierarchyLevelDTO1 = new HierarchyLevelDTO();
        hierarchyLevelDTO1.setHierarchyLevelId("42");
        hierarchyLevelDTO1.setHierarchyLevelName("Hierarchy Level Name");
        hierarchyLevelDTO1.setLevel(1);
        hierarchyLevelDTO1.setSuggestions(new TreeSet<>());
        assertEquals(hierarchyLevelDTO, hierarchyLevelDTO1);
        int expectedHashCodeResult = hierarchyLevelDTO.hashCode();
        assertEquals(expectedHashCodeResult, hierarchyLevelDTO1.hashCode());
    }

    /**
     * Method under test: {@link HierarchyLevelDTO#equals(Object)}
     */
    @Test
    public void testEquals5() {
        HierarchyLevelDTO hierarchyLevelDTO = new HierarchyLevelDTO();
        hierarchyLevelDTO.setHierarchyLevelId("Hierarchy Level Name");
        hierarchyLevelDTO.setHierarchyLevelName("Hierarchy Level Name");
        hierarchyLevelDTO.setLevel(1);
        hierarchyLevelDTO.setSuggestions(new TreeSet<>());

        HierarchyLevelDTO hierarchyLevelDTO1 = new HierarchyLevelDTO();
        hierarchyLevelDTO1.setHierarchyLevelId("42");
        hierarchyLevelDTO1.setHierarchyLevelName("Hierarchy Level Name");
        hierarchyLevelDTO1.setLevel(1);
        hierarchyLevelDTO1.setSuggestions(new TreeSet<>());
        assertNotEquals(hierarchyLevelDTO, hierarchyLevelDTO1);
    }

    /**
     * Method under test: {@link HierarchyLevelDTO#equals(Object)}
     */
    @Test
    public void testEquals6() {
        HierarchyLevelDTO hierarchyLevelDTO = new HierarchyLevelDTO();
        hierarchyLevelDTO.setHierarchyLevelId(null);
        hierarchyLevelDTO.setHierarchyLevelName("Hierarchy Level Name");
        hierarchyLevelDTO.setLevel(1);
        hierarchyLevelDTO.setSuggestions(new TreeSet<>());

        HierarchyLevelDTO hierarchyLevelDTO1 = new HierarchyLevelDTO();
        hierarchyLevelDTO1.setHierarchyLevelId("42");
        hierarchyLevelDTO1.setHierarchyLevelName("Hierarchy Level Name");
        hierarchyLevelDTO1.setLevel(1);
        hierarchyLevelDTO1.setSuggestions(new TreeSet<>());
        assertNotEquals(hierarchyLevelDTO, hierarchyLevelDTO1);
    }

    /**
     * Method under test: {@link HierarchyLevelDTO#equals(Object)}
     */
    @Test
    public void testEquals7() {
        HierarchyLevelDTO hierarchyLevelDTO = new HierarchyLevelDTO();
        hierarchyLevelDTO.setHierarchyLevelId("42");
        hierarchyLevelDTO.setHierarchyLevelName("42");
        hierarchyLevelDTO.setLevel(1);
        hierarchyLevelDTO.setSuggestions(new TreeSet<>());

        HierarchyLevelDTO hierarchyLevelDTO1 = new HierarchyLevelDTO();
        hierarchyLevelDTO1.setHierarchyLevelId("42");
        hierarchyLevelDTO1.setHierarchyLevelName("Hierarchy Level Name");
        hierarchyLevelDTO1.setLevel(1);
        hierarchyLevelDTO1.setSuggestions(new TreeSet<>());
        assertNotEquals(hierarchyLevelDTO, hierarchyLevelDTO1);
    }

    /**
     * Method under test: {@link HierarchyLevelDTO#equals(Object)}
     */
    @Test
    public void testEquals8() {
        HierarchyLevelDTO hierarchyLevelDTO = new HierarchyLevelDTO();
        hierarchyLevelDTO.setHierarchyLevelId("42");
        hierarchyLevelDTO.setHierarchyLevelName(null);
        hierarchyLevelDTO.setLevel(1);
        hierarchyLevelDTO.setSuggestions(new TreeSet<>());

        HierarchyLevelDTO hierarchyLevelDTO1 = new HierarchyLevelDTO();
        hierarchyLevelDTO1.setHierarchyLevelId("42");
        hierarchyLevelDTO1.setHierarchyLevelName("Hierarchy Level Name");
        hierarchyLevelDTO1.setLevel(1);
        hierarchyLevelDTO1.setSuggestions(new TreeSet<>());
        assertNotEquals(hierarchyLevelDTO, hierarchyLevelDTO1);
    }

    /**
     * Method under test: {@link HierarchyLevelDTO#equals(Object)}
     */
    @Test
    public void testEquals9() {
        HierarchyLevelDTO hierarchyLevelDTO = new HierarchyLevelDTO();
        hierarchyLevelDTO.setHierarchyLevelId("42");
        hierarchyLevelDTO.setHierarchyLevelName("Hierarchy Level Name");
        hierarchyLevelDTO.setLevel(3);
        hierarchyLevelDTO.setSuggestions(new TreeSet<>());

        HierarchyLevelDTO hierarchyLevelDTO1 = new HierarchyLevelDTO();
        hierarchyLevelDTO1.setHierarchyLevelId("42");
        hierarchyLevelDTO1.setHierarchyLevelName("Hierarchy Level Name");
        hierarchyLevelDTO1.setLevel(1);
        hierarchyLevelDTO1.setSuggestions(new TreeSet<>());
        assertNotEquals(hierarchyLevelDTO, hierarchyLevelDTO1);
    }

    /**
     * Method under test: {@link HierarchyLevelDTO#equals(Object)}
     */
    @Test
    public void testEquals10() {
        TreeSet<String> stringSet = new TreeSet<>();
        stringSet.add("42");

        HierarchyLevelDTO hierarchyLevelDTO = new HierarchyLevelDTO();
        hierarchyLevelDTO.setHierarchyLevelId("42");
        hierarchyLevelDTO.setHierarchyLevelName("Hierarchy Level Name");
        hierarchyLevelDTO.setLevel(1);
        hierarchyLevelDTO.setSuggestions(stringSet);

        HierarchyLevelDTO hierarchyLevelDTO1 = new HierarchyLevelDTO();
        hierarchyLevelDTO1.setHierarchyLevelId("42");
        hierarchyLevelDTO1.setHierarchyLevelName("Hierarchy Level Name");
        hierarchyLevelDTO1.setLevel(1);
        hierarchyLevelDTO1.setSuggestions(new TreeSet<>());
        assertNotEquals(hierarchyLevelDTO, hierarchyLevelDTO1);
    }

    /**
     * Methods under test:
     *
     * <ul>
     * <li>{@link HierarchyLevelDTO#equals(Object)}
     * <li>{@link HierarchyLevelDTO#hashCode()}
     * </ul>
     */
    @Test
    public void testEquals11() {
        HierarchyLevelDTO hierarchyLevelDTO = new HierarchyLevelDTO();
        hierarchyLevelDTO.setHierarchyLevelId(null);
        hierarchyLevelDTO.setHierarchyLevelName("Hierarchy Level Name");
        hierarchyLevelDTO.setLevel(1);
        hierarchyLevelDTO.setSuggestions(new TreeSet<>());

        HierarchyLevelDTO hierarchyLevelDTO1 = new HierarchyLevelDTO();
        hierarchyLevelDTO1.setHierarchyLevelId(null);
        hierarchyLevelDTO1.setHierarchyLevelName("Hierarchy Level Name");
        hierarchyLevelDTO1.setLevel(1);
        hierarchyLevelDTO1.setSuggestions(new TreeSet<>());
        assertEquals(hierarchyLevelDTO, hierarchyLevelDTO1);
        int expectedHashCodeResult = hierarchyLevelDTO.hashCode();
        assertEquals(expectedHashCodeResult, hierarchyLevelDTO1.hashCode());
    }

    /**
     * Methods under test:
     *
     * <ul>
     * <li>{@link HierarchyLevelDTO#equals(Object)}
     * <li>{@link HierarchyLevelDTO#hashCode()}
     * </ul>
     */
    @Test
    public void testEquals12() {
        HierarchyLevelDTO hierarchyLevelDTO = new HierarchyLevelDTO();
        hierarchyLevelDTO.setHierarchyLevelId("42");
        hierarchyLevelDTO.setHierarchyLevelName(null);
        hierarchyLevelDTO.setLevel(1);
        hierarchyLevelDTO.setSuggestions(new TreeSet<>());

        HierarchyLevelDTO hierarchyLevelDTO1 = new HierarchyLevelDTO();
        hierarchyLevelDTO1.setHierarchyLevelId("42");
        hierarchyLevelDTO1.setHierarchyLevelName(null);
        hierarchyLevelDTO1.setLevel(1);
        hierarchyLevelDTO1.setSuggestions(new TreeSet<>());
        assertEquals(hierarchyLevelDTO, hierarchyLevelDTO1);
        int expectedHashCodeResult = hierarchyLevelDTO.hashCode();
        assertEquals(expectedHashCodeResult, hierarchyLevelDTO1.hashCode());
    }
}
