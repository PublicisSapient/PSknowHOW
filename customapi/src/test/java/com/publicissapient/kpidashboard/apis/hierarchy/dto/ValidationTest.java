package com.publicissapient.kpidashboard.apis.hierarchy.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

@RunWith(MockitoJUnitRunner.class)
public class ValidationTest {

	@Test
	public void testValidCreateHierarchyRequest() {
		Validator validator = getValidator();
		CreateHierarchyRequest request = new CreateHierarchyRequest("Valid Name");
		UpdateHierarchyRequest updateHierarchyRequest = new UpdateHierarchyRequest();
		updateHierarchyRequest.setDisplayName("Valid New Name");
		Set<ConstraintViolation<CreateHierarchyRequest>> hierarchyRequest = validator.validate(request);
		Set<ConstraintViolation<UpdateHierarchyRequest>> updateRequest = validator.validate(updateHierarchyRequest);

		assertTrue("Expected no validation errors", hierarchyRequest.isEmpty());
		assertTrue("Expected no validation errors", updateRequest.isEmpty());
		assertEquals("Valid Name", request.getName());
		assertEquals("Valid New Name", updateHierarchyRequest.getDisplayName());
	}

	@Test
	public void testCreateHierarchyRequest_NameIsNull() {
		Validator validator = getValidator();
		CreateHierarchyRequest request = new CreateHierarchyRequest(null);
		Set<ConstraintViolation<CreateHierarchyRequest>> hierarchyRequest = validator.validate(request);
		UpdateHierarchyRequest updateHierarchyRequest = new UpdateHierarchyRequest();
		updateHierarchyRequest.setDisplayName(null);
		Set<ConstraintViolation<UpdateHierarchyRequest>> updateRequest = validator.validate(updateHierarchyRequest);

		assertFalse("Expected validation errors", hierarchyRequest.isEmpty());
		assertEquals(2, hierarchyRequest.size());
		assertEquals("name cannot be empty", hierarchyRequest.iterator().next().getMessage());

		assertFalse("Expected validation errors", updateRequest.isEmpty());
		assertEquals(2, updateRequest.size());
		assertEquals("displayName cannot be null", updateRequest.iterator().next().getMessage());
	}

	@Test
	public void testCreateHierarchyRequest_NameIsEmpty() {
		// Arrange
		Validator validator = getValidator();
		CreateHierarchyRequest request = new CreateHierarchyRequest();
		request.setName("");
		Set<ConstraintViolation<CreateHierarchyRequest>> violations = validator.validate(request);
		UpdateHierarchyRequest updateHierarchyRequest = new UpdateHierarchyRequest();
		updateHierarchyRequest.setDisplayName("");
		Set<ConstraintViolation<UpdateHierarchyRequest>> updateRequest = validator.validate(updateHierarchyRequest);


		assertFalse("Expected validation errors", violations.isEmpty());
		assertEquals(1, violations.size());
		assertEquals("name cannot be empty", violations.iterator().next().getMessage());

		assertFalse("Expected validation errors", updateRequest.isEmpty());
		assertEquals(1, updateRequest.size());
		assertEquals("displayName cannot be empty", updateRequest.iterator().next().getMessage());
	}

	private static Validator getValidator() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		return factory.getValidator();
	}

}