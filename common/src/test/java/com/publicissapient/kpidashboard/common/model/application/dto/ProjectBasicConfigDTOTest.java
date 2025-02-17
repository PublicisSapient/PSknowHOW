/*
 *
 *   Copyright 2014 CapitalOne, LLC.
 *   Further development Copyright 2022 Sapient Corporation.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.publicissapient.kpidashboard.common.model.application.dto;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProjectBasicConfigDTOTest {

    /**
     * Method under test: {@link ProjectBasicConfigDTO#canEqual(Object)}
     */
    @Test
    public void testCanEqual() {
        assertFalse((new ProjectBasicConfigDTO()).canEqual("Other"));
    }

    /**
     * Method under test: {@link ProjectBasicConfigDTO#canEqual(Object)}
     */
    @Test
    public void testCanEqual2() {
        ProjectBasicConfigDTO projectBasicConfigDTO = new ProjectBasicConfigDTO();
        assertTrue(projectBasicConfigDTO.canEqual(new ProjectBasicConfigDTO()));
    }

    /**
     * Methods under test:
     *
     * <ul>
     * <li>{@link ProjectBasicConfigDTO#ProjectBasicConfigDTO()}
     * <li>{@link ProjectBasicConfigDTO#setConsumerCreatedOn(String)}
     * <li>{@link ProjectBasicConfigDTO#setCreatedAt(String)}
     * <li>{@link ProjectBasicConfigDTO#setHierarchy(List)}
     * <li>{@link ProjectBasicConfigDTO#setId(ObjectId)}
     * <li>{@link ProjectBasicConfigDTO#setKanban(boolean)}
     * <li>{@link ProjectBasicConfigDTO#setProjectName(String)}
     * <li>{@link ProjectBasicConfigDTO#setSaveAssigneeDetails(boolean)}
     * <li>{@link ProjectBasicConfigDTO#setUpdatedAt(String)}
     * <li>{@link ProjectBasicConfigDTO#toString()}
     * <li>{@link ProjectBasicConfigDTO#getConsumerCreatedOn()}
     * <li>{@link ProjectBasicConfigDTO#getCreatedAt()}
     * <li>{@link ProjectBasicConfigDTO#getHierarchy()}
     * <li>{@link ProjectBasicConfigDTO#getId()}
     * <li>{@link ProjectBasicConfigDTO#getProjectName()}
     * <li>{@link ProjectBasicConfigDTO#getUpdatedAt()}
     * <li>{@link ProjectBasicConfigDTO#isKanban()}
     * <li>{@link ProjectBasicConfigDTO#isSaveAssigneeDetails()}
     * </ul>
     */
    @Test
    public void testConstructor() {
        ProjectBasicConfigDTO actualProjectBasicConfigDTO = new ProjectBasicConfigDTO();
        actualProjectBasicConfigDTO.setConsumerCreatedOn("Jan 1, 2020 8:00am GMT+0100");
        actualProjectBasicConfigDTO.setCreatedAt("Jan 1, 2020 8:00am GMT+0100");
        ArrayList<HierarchyValueDTO> hierarchyValueDTOList = new ArrayList<>();
        actualProjectBasicConfigDTO.setHierarchy(hierarchyValueDTOList);
        ObjectId getResult = ObjectId.get();
        actualProjectBasicConfigDTO.setId(getResult);
        actualProjectBasicConfigDTO.setKanban(true);
        actualProjectBasicConfigDTO.setProjectName("Project Name");
        actualProjectBasicConfigDTO.setSaveAssigneeDetails(true);
        actualProjectBasicConfigDTO.setUpdatedAt("2020-03-01");
        actualProjectBasicConfigDTO.toString();
        assertEquals("Jan 1, 2020 8:00am GMT+0100", actualProjectBasicConfigDTO.getConsumerCreatedOn());
        assertEquals("Jan 1, 2020 8:00am GMT+0100", actualProjectBasicConfigDTO.getCreatedAt());
        assertSame(hierarchyValueDTOList, actualProjectBasicConfigDTO.getHierarchy());
        assertSame(getResult, actualProjectBasicConfigDTO.getId());
        assertEquals("Project Name", actualProjectBasicConfigDTO.getProjectName());
        assertEquals("2020-03-01", actualProjectBasicConfigDTO.getUpdatedAt());
        assertTrue(actualProjectBasicConfigDTO.isKanban());
        assertTrue(actualProjectBasicConfigDTO.isSaveAssigneeDetails());
    }

    /**
     * Methods under test:
     *
     * <ul>
     * <li>{@link ProjectBasicConfigDTO#setConsumerCreatedOn(String)}
     * <li>{@link ProjectBasicConfigDTO#setCreatedAt(String)}
     * <li>{@link ProjectBasicConfigDTO#setHierarchy(List)}
     * <li>{@link ProjectBasicConfigDTO#setId(ObjectId)}
     * <li>{@link ProjectBasicConfigDTO#setKanban(boolean)}
     * <li>{@link ProjectBasicConfigDTO#setProjectName(String)}
     * <li>{@link ProjectBasicConfigDTO#setSaveAssigneeDetails(boolean)}
     * <li>{@link ProjectBasicConfigDTO#setUpdatedAt(String)}
     * <li>{@link ProjectBasicConfigDTO#toString()}
     * <li>{@link ProjectBasicConfigDTO#getConsumerCreatedOn()}
     * <li>{@link ProjectBasicConfigDTO#getCreatedAt()}
     * <li>{@link ProjectBasicConfigDTO#getHierarchy()}
     * <li>{@link ProjectBasicConfigDTO#getId()}
     * <li>{@link ProjectBasicConfigDTO#getProjectName()}
     * <li>{@link ProjectBasicConfigDTO#getUpdatedAt()}
     * <li>{@link ProjectBasicConfigDTO#isKanban()}
     * <li>{@link ProjectBasicConfigDTO#isSaveAssigneeDetails()}
     * </ul>
     */
    @Test
    public void testConstructor2() {
        ObjectId id = ObjectId.get();
		ProjectBasicConfigDTO actualProjectBasicConfigDTO = new ProjectBasicConfigDTO(id, "projectUniqueId", "Project Name" , "Project Display Name" ,
				"Jan 1, 2020 8:00am GMT+0100", "", "2020-03-01", "", "Jan 1, 2020 8:00am GMT+0100", true,
				new ArrayList<>(), true, true, false,new ObjectId());
        actualProjectBasicConfigDTO.setConsumerCreatedOn("Jan 1, 2020 8:00am GMT+0100");
        actualProjectBasicConfigDTO.setCreatedAt("Jan 1, 2020 8:00am GMT+0100");
        ArrayList<HierarchyValueDTO> hierarchyValueDTOList = new ArrayList<>();
        actualProjectBasicConfigDTO.setHierarchy(hierarchyValueDTOList);
        ObjectId getResult = ObjectId.get();
        actualProjectBasicConfigDTO.setId(getResult);
        actualProjectBasicConfigDTO.setKanban(true);
        actualProjectBasicConfigDTO.setProjectName("Project Name");
        actualProjectBasicConfigDTO.setSaveAssigneeDetails(true);
        actualProjectBasicConfigDTO.setUpdatedAt("2020-03-01");
        actualProjectBasicConfigDTO.toString();
        assertEquals("Jan 1, 2020 8:00am GMT+0100", actualProjectBasicConfigDTO.getConsumerCreatedOn());
        assertEquals("Jan 1, 2020 8:00am GMT+0100", actualProjectBasicConfigDTO.getCreatedAt());
        assertSame(hierarchyValueDTOList, actualProjectBasicConfigDTO.getHierarchy());
        assertSame(getResult, actualProjectBasicConfigDTO.getId());
        assertEquals("Project Name", actualProjectBasicConfigDTO.getProjectName());
        assertEquals("2020-03-01", actualProjectBasicConfigDTO.getUpdatedAt());
        assertTrue(actualProjectBasicConfigDTO.isKanban());
        assertTrue(actualProjectBasicConfigDTO.isSaveAssigneeDetails());
    }

    /**
     * Methods under test:
     *
     * <ul>
     * <li>{@link ProjectBasicConfigDTO#ProjectBasicConfigDTO()}
     * <li>{@link ProjectBasicConfigDTO#setIsKanban(boolean)}
     * <li>{@link ProjectBasicConfigDTO#getIsKanban()}
     * </ul>
     */
    @Test
    public void testConstructor3() {
        ProjectBasicConfigDTO actualProjectBasicConfigDTO = new ProjectBasicConfigDTO();
        actualProjectBasicConfigDTO.setIsKanban(true);
        assertTrue(actualProjectBasicConfigDTO.getIsKanban());
    }

    /**
     * Method under test: {@link ProjectBasicConfigDTO#equals(Object)}
     */
    @Test
    public void testEquals() {
        assertNotEquals(new ProjectBasicConfigDTO(), null);
        assertNotEquals(new ProjectBasicConfigDTO(), "Different type to ProjectBasicConfigDTO");
    }

    /**
     * Methods under test:
     *
     * <ul>
     * <li>{@link ProjectBasicConfigDTO#equals(Object)}
     * <li>{@link ProjectBasicConfigDTO#hashCode()}
     * </ul>
     */
    @Test
    public void testEquals2() {
        ProjectBasicConfigDTO projectBasicConfigDTO = new ProjectBasicConfigDTO();
        assertEquals(projectBasicConfigDTO, projectBasicConfigDTO);
        int expectedHashCodeResult = projectBasicConfigDTO.hashCode();
        assertEquals(expectedHashCodeResult, projectBasicConfigDTO.hashCode());
    }

    /**
     * Methods under test:
     *
     * <ul>
     * <li>{@link ProjectBasicConfigDTO#equals(Object)}
     * <li>{@link ProjectBasicConfigDTO#hashCode()}
     * </ul>
     */
    @Test
    public void testEquals3() {
        ProjectBasicConfigDTO projectBasicConfigDTO = new ProjectBasicConfigDTO();
        ProjectBasicConfigDTO projectBasicConfigDTO1 = new ProjectBasicConfigDTO();
        assertEquals(projectBasicConfigDTO, projectBasicConfigDTO1);
        int expectedHashCodeResult = projectBasicConfigDTO.hashCode();
        assertEquals(expectedHashCodeResult, projectBasicConfigDTO1.hashCode());
    }

    /**
     * Method under test: {@link ProjectBasicConfigDTO#equals(Object)}
     */
    @Test
    public void testEquals4() {
        ObjectId id = ObjectId.get();
        ProjectBasicConfigDTO projectBasicConfigDTO = new ProjectBasicConfigDTO(id, "projectUniqueId", "Project Name" , "Project Display Name" ,
                "Jan 1, 2020 8:00am GMT+0100", "user1", "2020-03-01", "user1", "Jan 1, 2020 8:00am GMT+0100", true, new ArrayList<>(),
                true, true, true,new ObjectId());
        assertNotEquals(projectBasicConfigDTO, new ProjectBasicConfigDTO());
    }

    /**
     * Method under test: {@link ProjectBasicConfigDTO#equals(Object)}
     */
    @Test
    public void testEquals5() {
        ProjectBasicConfigDTO projectBasicConfigDTO = new ProjectBasicConfigDTO();
        projectBasicConfigDTO.setId(ObjectId.get());
        assertNotEquals(projectBasicConfigDTO, new ProjectBasicConfigDTO());
    }

    /**
     * Method under test: {@link ProjectBasicConfigDTO#equals(Object)}
     */
    @Test
    public void testEquals6() {
        ProjectBasicConfigDTO projectBasicConfigDTO = new ProjectBasicConfigDTO();
        projectBasicConfigDTO.setProjectName("Project Name");
        assertNotEquals(projectBasicConfigDTO, new ProjectBasicConfigDTO());
    }

    /**
     * Method under test: {@link ProjectBasicConfigDTO#equals(Object)}
     */
    @Test
    public void testEquals7() {
        ProjectBasicConfigDTO projectBasicConfigDTO = new ProjectBasicConfigDTO();
        projectBasicConfigDTO.setCreatedAt("Jan 1, 2020 8:00am GMT+0100");
        assertNotEquals(projectBasicConfigDTO, new ProjectBasicConfigDTO());
    }

    /**
     * Method under test: {@link ProjectBasicConfigDTO#equals(Object)}
     */
    @Test
    public void testEquals8() {
        ProjectBasicConfigDTO projectBasicConfigDTO = new ProjectBasicConfigDTO();
        projectBasicConfigDTO.setUpdatedAt("2020-03-01");
        assertNotEquals(projectBasicConfigDTO, new ProjectBasicConfigDTO());
    }

    /**
     * Method under test: {@link ProjectBasicConfigDTO#equals(Object)}
     */
    @Test
    public void testEquals9() {
        ProjectBasicConfigDTO projectBasicConfigDTO = new ProjectBasicConfigDTO();
        projectBasicConfigDTO.setConsumerCreatedOn("Jan 1, 2020 8:00am GMT+0100");
        assertNotEquals(projectBasicConfigDTO, new ProjectBasicConfigDTO());
    }

    /**
     * Method under test: {@link ProjectBasicConfigDTO#equals(Object)}
     */
    @Test
    public void testEquals10() {
        ProjectBasicConfigDTO projectBasicConfigDTO = new ProjectBasicConfigDTO();
        projectBasicConfigDTO.setHierarchy(new ArrayList<>());
        assertNotEquals(projectBasicConfigDTO, new ProjectBasicConfigDTO());
    }

    /**
     * Method under test: {@link ProjectBasicConfigDTO#equals(Object)}
     */
    @Test
    public void testEquals11() {
        ProjectBasicConfigDTO projectBasicConfigDTO = new ProjectBasicConfigDTO();
        projectBasicConfigDTO.setSaveAssigneeDetails(true);
        assertNotEquals(projectBasicConfigDTO, new ProjectBasicConfigDTO());
    }

    /**
     * Method under test: {@link ProjectBasicConfigDTO#equals(Object)}
     */
    @Test
    public void testEquals12() {
        ProjectBasicConfigDTO projectBasicConfigDTO = new ProjectBasicConfigDTO();

        ProjectBasicConfigDTO projectBasicConfigDTO1 = new ProjectBasicConfigDTO();
        projectBasicConfigDTO1.setId(ObjectId.get());
        assertNotEquals(projectBasicConfigDTO, projectBasicConfigDTO1);
    }

    /**
     * Method under test: {@link ProjectBasicConfigDTO#equals(Object)}
     */
    @Test
    public void testEquals13() {
        ProjectBasicConfigDTO projectBasicConfigDTO = new ProjectBasicConfigDTO();

        ProjectBasicConfigDTO projectBasicConfigDTO1 = new ProjectBasicConfigDTO();
        projectBasicConfigDTO1.setProjectName("Project Name");
        assertNotEquals(projectBasicConfigDTO, projectBasicConfigDTO1);
    }

    /**
     * Method under test: {@link ProjectBasicConfigDTO#equals(Object)}
     */
    @Test
    public void testEquals14() {
        ProjectBasicConfigDTO projectBasicConfigDTO = new ProjectBasicConfigDTO();

        ProjectBasicConfigDTO projectBasicConfigDTO1 = new ProjectBasicConfigDTO();
        projectBasicConfigDTO1.setCreatedAt("Jan 1, 2020 8:00am GMT+0100");
        assertNotEquals(projectBasicConfigDTO, projectBasicConfigDTO1);
    }

    /**
     * Method under test: {@link ProjectBasicConfigDTO#equals(Object)}
     */
    @Test
    public void testEquals15() {
        ProjectBasicConfigDTO projectBasicConfigDTO = new ProjectBasicConfigDTO();

        ProjectBasicConfigDTO projectBasicConfigDTO1 = new ProjectBasicConfigDTO();
        projectBasicConfigDTO1.setUpdatedAt("2020-03-01");
        assertNotEquals(projectBasicConfigDTO, projectBasicConfigDTO1);
    }

    /**
     * Method under test: {@link ProjectBasicConfigDTO#equals(Object)}
     */
    @Test
    public void testEquals16() {
        ProjectBasicConfigDTO projectBasicConfigDTO = new ProjectBasicConfigDTO();

        ProjectBasicConfigDTO projectBasicConfigDTO1 = new ProjectBasicConfigDTO();
        projectBasicConfigDTO1.setConsumerCreatedOn("Jan 1, 2020 8:00am GMT+0100");
        assertNotEquals(projectBasicConfigDTO, projectBasicConfigDTO1);
    }

    /**
     * Method under test: {@link ProjectBasicConfigDTO#equals(Object)}
     */
    @Test
    public void testEquals17() {
        ProjectBasicConfigDTO projectBasicConfigDTO = new ProjectBasicConfigDTO();

        ProjectBasicConfigDTO projectBasicConfigDTO1 = new ProjectBasicConfigDTO();
        projectBasicConfigDTO1.setHierarchy(new ArrayList<>());
        assertNotEquals(projectBasicConfigDTO, projectBasicConfigDTO1);
    }

    /**
     * Methods under test:
     *
     * <ul>
     * <li>{@link ProjectBasicConfigDTO#equals(Object)}
     * <li>{@link ProjectBasicConfigDTO#hashCode()}
     * </ul>
     */
    @Test
    public void testEquals18() {
        ProjectBasicConfigDTO projectBasicConfigDTO = new ProjectBasicConfigDTO();
        projectBasicConfigDTO.setProjectName("Project Name");

        ProjectBasicConfigDTO projectBasicConfigDTO1 = new ProjectBasicConfigDTO();
        projectBasicConfigDTO1.setProjectName("Project Name");
        assertEquals(projectBasicConfigDTO, projectBasicConfigDTO1);
        int expectedHashCodeResult = projectBasicConfigDTO.hashCode();
        assertEquals(expectedHashCodeResult, projectBasicConfigDTO1.hashCode());
    }

    /**
     * Methods under test:
     *
     * <ul>
     * <li>{@link ProjectBasicConfigDTO#equals(Object)}
     * <li>{@link ProjectBasicConfigDTO#hashCode()}
     * </ul>
     */
    @Test
    public void testEquals19() {
        ProjectBasicConfigDTO projectBasicConfigDTO = new ProjectBasicConfigDTO();
        projectBasicConfigDTO.setCreatedAt("Jan 1, 2020 8:00am GMT+0100");

        ProjectBasicConfigDTO projectBasicConfigDTO1 = new ProjectBasicConfigDTO();
        projectBasicConfigDTO1.setCreatedAt("Jan 1, 2020 8:00am GMT+0100");
        assertEquals(projectBasicConfigDTO, projectBasicConfigDTO1);
        int expectedHashCodeResult = projectBasicConfigDTO.hashCode();
        assertEquals(expectedHashCodeResult, projectBasicConfigDTO1.hashCode());
    }

    /**
     * Methods under test:
     *
     * <ul>
     * <li>{@link ProjectBasicConfigDTO#equals(Object)}
     * <li>{@link ProjectBasicConfigDTO#hashCode()}
     * </ul>
     */
    @Test
    public void testEquals20() {
        ProjectBasicConfigDTO projectBasicConfigDTO = new ProjectBasicConfigDTO();
        projectBasicConfigDTO.setUpdatedAt("2020-03-01");

        ProjectBasicConfigDTO projectBasicConfigDTO1 = new ProjectBasicConfigDTO();
        projectBasicConfigDTO1.setUpdatedAt("2020-03-01");
        assertEquals(projectBasicConfigDTO, projectBasicConfigDTO1);
        int expectedHashCodeResult = projectBasicConfigDTO.hashCode();
        assertEquals(expectedHashCodeResult, projectBasicConfigDTO1.hashCode());
    }

    /**
     * Methods under test:
     *
     * <ul>
     * <li>{@link ProjectBasicConfigDTO#equals(Object)}
     * <li>{@link ProjectBasicConfigDTO#hashCode()}
     * </ul>
     */
    @Test
    public void testEquals21() {
        ProjectBasicConfigDTO projectBasicConfigDTO = new ProjectBasicConfigDTO();
        projectBasicConfigDTO.setConsumerCreatedOn("Jan 1, 2020 8:00am GMT+0100");

        ProjectBasicConfigDTO projectBasicConfigDTO1 = new ProjectBasicConfigDTO();
        projectBasicConfigDTO1.setConsumerCreatedOn("Jan 1, 2020 8:00am GMT+0100");
        assertEquals(projectBasicConfigDTO, projectBasicConfigDTO1);
        int expectedHashCodeResult = projectBasicConfigDTO.hashCode();
        assertEquals(expectedHashCodeResult, projectBasicConfigDTO1.hashCode());
    }

    /**
     * Methods under test:
     *
     * <ul>
     * <li>{@link ProjectBasicConfigDTO#equals(Object)}
     * <li>{@link ProjectBasicConfigDTO#hashCode()}
     * </ul>
     */
    @Test
    public void testEquals22() {
        ProjectBasicConfigDTO projectBasicConfigDTO = new ProjectBasicConfigDTO();
        projectBasicConfigDTO.setHierarchy(new ArrayList<>());

        ProjectBasicConfigDTO projectBasicConfigDTO1 = new ProjectBasicConfigDTO();
        projectBasicConfigDTO1.setHierarchy(new ArrayList<>());
        assertEquals(projectBasicConfigDTO, projectBasicConfigDTO1);
        int expectedHashCodeResult = projectBasicConfigDTO.hashCode();
        assertEquals(expectedHashCodeResult, projectBasicConfigDTO1.hashCode());
    }

}
