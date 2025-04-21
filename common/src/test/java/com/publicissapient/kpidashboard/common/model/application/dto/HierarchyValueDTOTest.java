package com.publicissapient.kpidashboard.common.model.application.dto;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import org.junit.jupiter.api.Test;

public class HierarchyValueDTOTest {
	/** Method under test: {@link HierarchyValueDTO#equals(Object)} */
	@Test
	public void testCanEqual() {
		assertFalse((new HierarchyValueDTO()).equals("Other"));
		assertFalse((new HierarchyValueDTO()).equals("Other"));
	}

	/** Method under test: {@link HierarchyValueDTO#equals(Object)} */
	@Test
	public void testCanEqual2() {
		HierarchyValueDTO hierarchyValueDTO = new HierarchyValueDTO();

		HierarchyLevel hierarchyLevelDTO = new HierarchyLevel();
		hierarchyLevelDTO.setHierarchyLevelId("42");
		hierarchyLevelDTO.setHierarchyLevelName("Hierarchy Level Name");
		hierarchyLevelDTO.setLevel(3);

		HierarchyValueDTO hierarchyValueDTO1 = new HierarchyValueDTO();
		hierarchyValueDTO1.setHierarchyLevel(hierarchyLevelDTO);
		hierarchyValueDTO1.setValue("42");
		assertFalse(hierarchyValueDTO.equals(hierarchyValueDTO1));
	}

	/** Method under test: {@link HierarchyValueDTO#equals(Object)} */
	@Test
	public void testCanEqual3() {
		HierarchyValueDTO hierarchyValueDTO = new HierarchyValueDTO();

		HierarchyLevel hierarchyLevelDTO = new HierarchyLevel();
		hierarchyLevelDTO.setHierarchyLevelId("42");
		hierarchyLevelDTO.setHierarchyLevelName("Hierarchy Level Name");
		hierarchyLevelDTO.setLevel(3);

		HierarchyValueDTO hierarchyValueDTO1 = new HierarchyValueDTO();
		hierarchyValueDTO1.setHierarchyLevel(hierarchyLevelDTO);
		hierarchyValueDTO1.setValue("42");
		assertFalse(hierarchyValueDTO.equals(hierarchyValueDTO1));
	}

	/**
	 * Methods under test:
	 *
	 * <ul>
	 * <li>default or parameterless constructor of {@link HierarchyValueDTO}
	 * <li>{@link HierarchyValueDTO#setHierarchyLevel(HierarchyLevel)}
	 * <li>{@link HierarchyValueDTO#setValue(String)}
	 * <li>{@link HierarchyValueDTO#toString()}
	 * <li>{@link HierarchyValueDTO#getHierarchyLevel()}
	 * <li>{@link HierarchyValueDTO#getValue()}
	 * </ul>
	 */
	@Test
	public void testConstructor() {
		HierarchyValueDTO actualHierarchyValueDTO = new HierarchyValueDTO();
		HierarchyLevel hierarchyLevelDTO = new HierarchyLevel();
		hierarchyLevelDTO.setHierarchyLevelId("42");
		hierarchyLevelDTO.setHierarchyLevelName("Hierarchy Level Name");
		hierarchyLevelDTO.setLevel(1);
		actualHierarchyValueDTO.setHierarchyLevel(hierarchyLevelDTO);
		actualHierarchyValueDTO.setOrgHierarchyNodeId("orgNodeUniqueId");
		actualHierarchyValueDTO.setValue("42");
		String actualToStringResult = actualHierarchyValueDTO.toString();
		assertSame(hierarchyLevelDTO, actualHierarchyValueDTO.getHierarchyLevel());
		assertEquals("42", actualHierarchyValueDTO.getValue());
		assertEquals(
				"HierarchyValueDTO(hierarchyLevel=HierarchyLevel(level=1, hierarchyLevelId=42, hierarchyLevelName=Hierarchy Level Name, hierarchyLevelInfo=null), orgHierarchyNodeId=orgNodeUniqueId, value=42)",
				actualToStringResult);
	}

	/**
	 * Methods under test:
	 *
	 * <ul>
	 * <li>default or parameterless constructor of {@link HierarchyValueDTO}
	 * <li>{@link HierarchyValueDTO#setHierarchyLevel(HierarchyLevel)}
	 * <li>{@link HierarchyValueDTO#setValue(String)}
	 * <li>{@link HierarchyValueDTO#toString()}
	 * <li>{@link HierarchyValueDTO#getHierarchyLevel()}
	 * <li>{@link HierarchyValueDTO#getValue()}
	 * </ul>
	 */
	@Test
	public void testConstructor2() {
		HierarchyValueDTO actualHierarchyValueDTO = new HierarchyValueDTO();
		HierarchyLevel hierarchyLevelDTO = new HierarchyLevel();
		hierarchyLevelDTO.setHierarchyLevelId("42");
		hierarchyLevelDTO.setHierarchyLevelName("Hierarchy Level Name");
		hierarchyLevelDTO.setLevel(1);
		actualHierarchyValueDTO.setHierarchyLevel(hierarchyLevelDTO);
		actualHierarchyValueDTO.setValue("42");
		actualHierarchyValueDTO.setOrgHierarchyNodeId("orgNodeUniqueId");
		String actualToStringResult = actualHierarchyValueDTO.toString();
		assertSame(hierarchyLevelDTO, actualHierarchyValueDTO.getHierarchyLevel());
		assertEquals("42", actualHierarchyValueDTO.getValue());
		assertEquals(
				"HierarchyValueDTO(hierarchyLevel=HierarchyLevel(level=1, hierarchyLevelId=42, hierarchyLevelName=Hierarchy Level Name, hierarchyLevelInfo=null), orgHierarchyNodeId=orgNodeUniqueId, value=42)",
				actualToStringResult);
	}

	/** Method under test: {@link HierarchyValueDTO#equals(Object)} */
	@Test
	public void testEquals() {
		HierarchyLevel hierarchyLevelDTO = new HierarchyLevel();
		hierarchyLevelDTO.setHierarchyLevelId("42");
		hierarchyLevelDTO.setHierarchyLevelName("Hierarchy Level Name");
		hierarchyLevelDTO.setLevel(1);

		HierarchyValueDTO hierarchyValueDTO = new HierarchyValueDTO();
		hierarchyValueDTO.setHierarchyLevel(hierarchyLevelDTO);
		hierarchyValueDTO.setValue("42");
		assertNotEquals(hierarchyValueDTO, null);
	}

	/** Method under test: {@link HierarchyValueDTO#equals(Object)} */
	@Test
	public void testEquals2() {
		HierarchyLevel hierarchyLevelDTO = new HierarchyLevel();
		hierarchyLevelDTO.setHierarchyLevelId("42");
		hierarchyLevelDTO.setHierarchyLevelName("Hierarchy Level Name");
		hierarchyLevelDTO.setLevel(1);

		HierarchyValueDTO hierarchyValueDTO = new HierarchyValueDTO();
		hierarchyValueDTO.setHierarchyLevel(hierarchyLevelDTO);
		hierarchyValueDTO.setValue("42");
		assertNotEquals(hierarchyValueDTO, "Different type to HierarchyValueDTO");
	}

	/**
	 * Methods under test:
	 *
	 * <ul>
	 * <li>{@link HierarchyValueDTO#equals(Object)}
	 * <li>{@link HierarchyValueDTO#hashCode()}
	 * </ul>
	 */
	@Test
	public void testEquals3() {
		HierarchyLevel hierarchyLevelDTO = new HierarchyLevel();
		hierarchyLevelDTO.setHierarchyLevelId("42");
		hierarchyLevelDTO.setHierarchyLevelName("Hierarchy Level Name");
		hierarchyLevelDTO.setLevel(1);

		HierarchyValueDTO hierarchyValueDTO = new HierarchyValueDTO();
		hierarchyValueDTO.setHierarchyLevel(hierarchyLevelDTO);
		hierarchyValueDTO.setValue("42");
		assertEquals(hierarchyValueDTO, hierarchyValueDTO);
		int expectedHashCodeResult = hierarchyValueDTO.hashCode();
		assertEquals(expectedHashCodeResult, hierarchyValueDTO.hashCode());
	}

	/**
	 * Methods under test:
	 *
	 * <ul>
	 * <li>{@link HierarchyValueDTO#equals(Object)}
	 * <li>{@link HierarchyValueDTO#hashCode()}
	 * </ul>
	 */
	@Test
	public void testEquals4() {
		HierarchyLevel hierarchyLevelDTO = new HierarchyLevel();
		hierarchyLevelDTO.setHierarchyLevelId("42");
		hierarchyLevelDTO.setHierarchyLevelName("Hierarchy Level Name");
		hierarchyLevelDTO.setLevel(1);

		HierarchyValueDTO hierarchyValueDTO = new HierarchyValueDTO();
		hierarchyValueDTO.setHierarchyLevel(hierarchyLevelDTO);
		hierarchyValueDTO.setValue("42");

		HierarchyLevel hierarchyLevelDTO1 = new HierarchyLevel();
		hierarchyLevelDTO1.setHierarchyLevelId("42");
		hierarchyLevelDTO1.setHierarchyLevelName("Hierarchy Level Name");
		hierarchyLevelDTO1.setLevel(1);

		HierarchyValueDTO hierarchyValueDTO1 = new HierarchyValueDTO();
		hierarchyValueDTO1.setHierarchyLevel(hierarchyLevelDTO1);
		hierarchyValueDTO1.setValue("42");
		assertEquals(hierarchyValueDTO, hierarchyValueDTO1);
		int expectedHashCodeResult = hierarchyValueDTO.hashCode();
		assertEquals(expectedHashCodeResult, hierarchyValueDTO1.hashCode());
	}

	/** Method under test: {@link HierarchyValueDTO#equals(Object)} */
	@Test
	public void testEquals5() {
		HierarchyLevel hierarchyLevelDTO = mock(HierarchyLevel.class);
		doNothing().when(hierarchyLevelDTO).setHierarchyLevelId((String) any());
		doNothing().when(hierarchyLevelDTO).setHierarchyLevelName((String) any());
		doNothing().when(hierarchyLevelDTO).setLevel(anyInt());
		hierarchyLevelDTO.setHierarchyLevelId("42");
		hierarchyLevelDTO.setHierarchyLevelName("Hierarchy Level Name");
		hierarchyLevelDTO.setLevel(1);

		HierarchyValueDTO hierarchyValueDTO = new HierarchyValueDTO();
		hierarchyValueDTO.setHierarchyLevel(hierarchyLevelDTO);
		hierarchyValueDTO.setValue("42");

		HierarchyLevel hierarchyLevelDTO1 = new HierarchyLevel();
		hierarchyLevelDTO1.setHierarchyLevelId("42");
		hierarchyLevelDTO1.setHierarchyLevelName("Hierarchy Level Name");
		hierarchyLevelDTO1.setLevel(1);

		HierarchyValueDTO hierarchyValueDTO1 = new HierarchyValueDTO();
		hierarchyValueDTO1.setHierarchyLevel(hierarchyLevelDTO1);
		hierarchyValueDTO1.setValue("42");
		assertNotEquals(hierarchyValueDTO, hierarchyValueDTO1);
	}

	/** Method under test: {@link HierarchyValueDTO#equals(Object)} */
	@Test
	public void testEquals6() {
		HierarchyLevel hierarchyLevelDTO = new HierarchyLevel();
		hierarchyLevelDTO.setHierarchyLevelId("42");
		hierarchyLevelDTO.setHierarchyLevelName("Hierarchy Level Name");
		hierarchyLevelDTO.setLevel(1);

		HierarchyValueDTO hierarchyValueDTO = new HierarchyValueDTO();
		hierarchyValueDTO.setHierarchyLevel(hierarchyLevelDTO);
		hierarchyValueDTO.setValue("42");
		assertNotEquals(hierarchyValueDTO, null);
	}

	/** Method under test: {@link HierarchyValueDTO#equals(Object)} */
	@Test
	public void testEquals7() {
		HierarchyLevel hierarchyLevelDTO = new HierarchyLevel();
		hierarchyLevelDTO.setHierarchyLevelId("42");
		hierarchyLevelDTO.setHierarchyLevelName("Hierarchy Level Name");
		hierarchyLevelDTO.setLevel(1);

		HierarchyValueDTO hierarchyValueDTO = new HierarchyValueDTO();
		hierarchyValueDTO.setHierarchyLevel(hierarchyLevelDTO);
		hierarchyValueDTO.setValue("42");
		assertNotEquals(hierarchyValueDTO, "Different type to HierarchyValueDTO");
	}

	/**
	 * Methods under test:
	 *
	 * <ul>
	 * <li>{@link HierarchyValueDTO#equals(Object)}
	 * <li>{@link HierarchyValueDTO#hashCode()}
	 * </ul>
	 */
	@Test
	public void testEquals8() {
		HierarchyLevel hierarchyLevelDTO = new HierarchyLevel();
		hierarchyLevelDTO.setHierarchyLevelId("42");
		hierarchyLevelDTO.setHierarchyLevelName("Hierarchy Level Name");
		hierarchyLevelDTO.setLevel(1);

		HierarchyValueDTO hierarchyValueDTO = new HierarchyValueDTO();
		hierarchyValueDTO.setHierarchyLevel(hierarchyLevelDTO);
		hierarchyValueDTO.setValue("42");
		assertEquals(hierarchyValueDTO, hierarchyValueDTO);
		int expectedHashCodeResult = hierarchyValueDTO.hashCode();
		assertEquals(expectedHashCodeResult, hierarchyValueDTO.hashCode());
	}

	/**
	 * Methods under test:
	 *
	 * <ul>
	 * <li>{@link HierarchyValueDTO#equals(Object)}
	 * <li>{@link HierarchyValueDTO#hashCode()}
	 * </ul>
	 */
	@Test
	public void testEquals9() {
		HierarchyLevel hierarchyLevelDTO = new HierarchyLevel();
		hierarchyLevelDTO.setHierarchyLevelId("42");
		hierarchyLevelDTO.setHierarchyLevelName("Hierarchy Level Name");
		hierarchyLevelDTO.setLevel(1);

		HierarchyValueDTO hierarchyValueDTO = new HierarchyValueDTO();
		hierarchyValueDTO.setHierarchyLevel(hierarchyLevelDTO);
		hierarchyValueDTO.setValue("42");

		HierarchyLevel hierarchyLevelDTO1 = new HierarchyLevel();
		hierarchyLevelDTO1.setHierarchyLevelId("42");
		hierarchyLevelDTO1.setHierarchyLevelName("Hierarchy Level Name");
		hierarchyLevelDTO1.setLevel(1);

		HierarchyValueDTO hierarchyValueDTO1 = new HierarchyValueDTO();
		hierarchyValueDTO1.setHierarchyLevel(hierarchyLevelDTO1);
		hierarchyValueDTO1.setValue("42");
		assertEquals(hierarchyValueDTO, hierarchyValueDTO1);
		int expectedHashCodeResult = hierarchyValueDTO.hashCode();
		assertEquals(expectedHashCodeResult, hierarchyValueDTO1.hashCode());
	}

	/** Method under test: {@link HierarchyValueDTO#equals(Object)} */
	@Test
	public void testEquals10() {
		HierarchyLevel hierarchyLevelDTO = mock(HierarchyLevel.class);
		doNothing().when(hierarchyLevelDTO).setHierarchyLevelId((String) any());
		doNothing().when(hierarchyLevelDTO).setHierarchyLevelName((String) any());
		doNothing().when(hierarchyLevelDTO).setLevel(anyInt());
		hierarchyLevelDTO.setHierarchyLevelId("42");
		hierarchyLevelDTO.setHierarchyLevelName("Hierarchy Level Name");
		hierarchyLevelDTO.setLevel(1);

		HierarchyValueDTO hierarchyValueDTO = new HierarchyValueDTO();
		hierarchyValueDTO.setHierarchyLevel(hierarchyLevelDTO);
		hierarchyValueDTO.setValue("42");

		HierarchyLevel hierarchyLevelDTO1 = new HierarchyLevel();
		hierarchyLevelDTO1.setHierarchyLevelId("42");
		hierarchyLevelDTO1.setHierarchyLevelName("Hierarchy Level Name");
		hierarchyLevelDTO1.setLevel(1);

		HierarchyValueDTO hierarchyValueDTO1 = new HierarchyValueDTO();
		hierarchyValueDTO1.setHierarchyLevel(hierarchyLevelDTO1);
		hierarchyValueDTO1.setValue("42");
		assertNotEquals(hierarchyValueDTO, hierarchyValueDTO1);
	}
}
