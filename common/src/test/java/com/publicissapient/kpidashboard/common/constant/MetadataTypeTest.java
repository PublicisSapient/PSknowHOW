package com.publicissapient.kpidashboard.common.constant;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MetadataTypeTest {

	@Test
	public void testEnumValues() {
		assertEquals("Issue_Type", MetadataType.ISSUETYPE.type());
		assertEquals("workflow", MetadataType.WORKFLOW.type());
		assertEquals("fields", MetadataType.FIELDS.type());
	}

}
