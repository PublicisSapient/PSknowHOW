package com.publicissapient.kpidashboard.common.model.application.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import com.publicissapient.kpidashboard.common.model.application.AssigneeDetailsDTO;

class AssigneeResponseDTOTest {
	/** Method under test: {@link AssigneeResponseDTO#canEqual(Object)} */
	@Test
	void testCanEqual() {
		assertFalse((new AssigneeResponseDTO()).canEqual("Other"));
	}

	/** Method under test: {@link AssigneeResponseDTO#canEqual(Object)} */
	@Test
	void testCanEqual2() {
		AssigneeResponseDTO assigneeResponseDTO = new AssigneeResponseDTO();

		AssigneeResponseDTO assigneeResponseDTO1 = new AssigneeResponseDTO();
		assigneeResponseDTO1.setAssigneeDetailsList(new ArrayList<>());
		assigneeResponseDTO1.setBasicProjectConfigId(ObjectId.get());
		assigneeResponseDTO1.setId(ObjectId.get());
		assertTrue(assigneeResponseDTO.canEqual(assigneeResponseDTO1));
	}

	/**
	 * Methods under test:
	 *
	 * <ul>
	 * <li>default or parameterless constructor of {@link AssigneeResponseDTO}
	 * <li>{@link AssigneeResponseDTO#setAssigneeDetailsList(List)}
	 * <li>{@link AssigneeResponseDTO#setBasicProjectConfigId(ObjectId)}
	 * <li>{@link AssigneeResponseDTO#toString()}
	 * <li>{@link AssigneeResponseDTO#getAssigneeDetailsList()}
	 * <li>{@link AssigneeResponseDTO#getBasicProjectConfigId()}
	 * </ul>
	 */
	@Test
	void testConstructor() {
		AssigneeResponseDTO actualAssigneeResponseDTO = new AssigneeResponseDTO();
		ArrayList<AssigneeDetailsDTO> assigneeDetailsDTOList = new ArrayList<>();
		actualAssigneeResponseDTO.setAssigneeDetailsList(assigneeDetailsDTOList);
		ObjectId getResult = ObjectId.get();
		actualAssigneeResponseDTO.setBasicProjectConfigId(getResult);
		actualAssigneeResponseDTO.toString();
		assertSame(assigneeDetailsDTOList, actualAssigneeResponseDTO.getAssigneeDetailsList());
		assertSame(getResult, actualAssigneeResponseDTO.getBasicProjectConfigId());
	}

	/** Method under test: {@link AssigneeResponseDTO#equals(Object)} */
	@Test
	void testEquals() {
		AssigneeResponseDTO assigneeResponseDTO = new AssigneeResponseDTO();
		assigneeResponseDTO.setAssigneeDetailsList(new ArrayList<>());
		assigneeResponseDTO.setBasicProjectConfigId(ObjectId.get());
		assigneeResponseDTO.setId(ObjectId.get());
		assertNotEquals(assigneeResponseDTO, null);
	}

	/** Method under test: {@link AssigneeResponseDTO#equals(Object)} */
	@Test
	void testEquals2() {
		AssigneeResponseDTO assigneeResponseDTO = new AssigneeResponseDTO();
		assigneeResponseDTO.setAssigneeDetailsList(new ArrayList<>());
		assigneeResponseDTO.setBasicProjectConfigId(ObjectId.get());
		assigneeResponseDTO.setId(ObjectId.get());
		assertNotEquals(assigneeResponseDTO, "Different type to AssigneeResponseDTO");
	}

	/**
	 * Methods under test:
	 *
	 * <ul>
	 * <li>{@link AssigneeResponseDTO#equals(Object)}
	 * <li>{@link AssigneeResponseDTO#hashCode()}
	 * </ul>
	 */
	@Test
	void testEquals3() {
		AssigneeResponseDTO assigneeResponseDTO = new AssigneeResponseDTO();
		assigneeResponseDTO.setAssigneeDetailsList(new ArrayList<>());
		assigneeResponseDTO.setBasicProjectConfigId(ObjectId.get());
		assigneeResponseDTO.setId(ObjectId.get());
		assertEquals(assigneeResponseDTO, assigneeResponseDTO);
		int expectedHashCodeResult = assigneeResponseDTO.hashCode();
		assertEquals(expectedHashCodeResult, assigneeResponseDTO.hashCode());
	}

	/** Method under test: {@link AssigneeResponseDTO#equals(Object)} */
	@Test
	void testEquals4() {
		AssigneeResponseDTO assigneeResponseDTO = new AssigneeResponseDTO();
		assigneeResponseDTO.setAssigneeDetailsList(new ArrayList<>());
		assigneeResponseDTO.setBasicProjectConfigId(ObjectId.get());
		assigneeResponseDTO.setId(ObjectId.get());

		AssigneeResponseDTO assigneeResponseDTO1 = new AssigneeResponseDTO();
		assigneeResponseDTO1.setAssigneeDetailsList(new ArrayList<>());
		assigneeResponseDTO1.setBasicProjectConfigId(ObjectId.get());
		assigneeResponseDTO1.setId(ObjectId.get());
		assertNotEquals(assigneeResponseDTO, assigneeResponseDTO1);
	}

	/** Method under test: {@link AssigneeResponseDTO#equals(Object)} */
	@Test
	void testEquals5() {
		AssigneeResponseDTO assigneeResponseDTO = new AssigneeResponseDTO();
		assigneeResponseDTO.setAssigneeDetailsList(new ArrayList<>());
		assigneeResponseDTO.setBasicProjectConfigId(null);
		assigneeResponseDTO.setId(ObjectId.get());

		AssigneeResponseDTO assigneeResponseDTO1 = new AssigneeResponseDTO();
		assigneeResponseDTO1.setAssigneeDetailsList(new ArrayList<>());
		assigneeResponseDTO1.setBasicProjectConfigId(ObjectId.get());
		assigneeResponseDTO1.setId(ObjectId.get());
		assertNotEquals(assigneeResponseDTO, assigneeResponseDTO1);
	}

	/**
	 * Methods under test:
	 *
	 * <ul>
	 * <li>{@link AssigneeResponseDTO#equals(Object)}
	 * <li>{@link AssigneeResponseDTO#hashCode()}
	 * </ul>
	 */
	@Test
	void testEquals6() {
		AssigneeResponseDTO assigneeResponseDTO = new AssigneeResponseDTO();
		assigneeResponseDTO.setAssigneeDetailsList(new ArrayList<>());
		assigneeResponseDTO.setBasicProjectConfigId(null);
		assigneeResponseDTO.setId(ObjectId.get());

		AssigneeResponseDTO assigneeResponseDTO1 = new AssigneeResponseDTO();
		assigneeResponseDTO1.setAssigneeDetailsList(new ArrayList<>());
		assigneeResponseDTO1.setBasicProjectConfigId(null);
		assigneeResponseDTO1.setId(ObjectId.get());
		assertEquals(assigneeResponseDTO, assigneeResponseDTO1);
		int expectedHashCodeResult = assigneeResponseDTO.hashCode();
		assertEquals(expectedHashCodeResult, assigneeResponseDTO1.hashCode());
	}

	/** Method under test: {@link AssigneeResponseDTO#equals(Object)} */
	@Test
	void testEquals7() {
		ArrayList<AssigneeDetailsDTO> assigneeDetailsDTOList = new ArrayList<>();
		assigneeDetailsDTOList.add(new AssigneeDetailsDTO("Name", "Display Name"));

		AssigneeResponseDTO assigneeResponseDTO = new AssigneeResponseDTO();
		assigneeResponseDTO.setAssigneeDetailsList(assigneeDetailsDTOList);
		assigneeResponseDTO.setBasicProjectConfigId(null);
		assigneeResponseDTO.setId(ObjectId.get());

		AssigneeResponseDTO assigneeResponseDTO1 = new AssigneeResponseDTO();
		assigneeResponseDTO1.setAssigneeDetailsList(new ArrayList<>());
		assigneeResponseDTO1.setBasicProjectConfigId(null);
		assigneeResponseDTO1.setId(ObjectId.get());
		assertNotEquals(assigneeResponseDTO, assigneeResponseDTO1);
	}
}
