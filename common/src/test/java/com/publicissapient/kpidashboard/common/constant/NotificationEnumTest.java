package com.publicissapient.kpidashboard.common.constant;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NotificationEnumTest {

	@Test
	public void testEnumValues() {
		assertEquals("Project Access Request", NotificationEnum.PROJECT_ACCESS.getValue());
		assertEquals("User Access Request", NotificationEnum.USER_APPROVAL.getValue());
	}

	@Test
	public void testEnumEquality() {
		assertEquals(NotificationEnum.PROJECT_ACCESS, NotificationEnum.valueOf("PROJECT_ACCESS"));
		assertEquals(NotificationEnum.USER_APPROVAL, NotificationEnum.valueOf("USER_APPROVAL"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidEnumValue() {
		NotificationEnum.valueOf("INVALID_NOTIFICATION");
	}
}
