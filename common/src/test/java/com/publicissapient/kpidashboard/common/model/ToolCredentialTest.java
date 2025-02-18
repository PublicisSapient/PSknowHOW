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

package com.publicissapient.kpidashboard.common.model;

import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ToolCredentialTest {
	/**
	 * Methods under test:
	 *
	 * <ul>
	 * <li>{@link ToolCredential#ToolCredential()}
	 * <li>{@link ToolCredential#setEmail(String)}
	 * <li>{@link ToolCredential#setPassword(String)}
	 * <li>{@link ToolCredential#setUsername(String)}
	 * <li>{@link ToolCredential#toString()}
	 * <li>{@link ToolCredential#getEmail()}
	 * <li>{@link ToolCredential#getPassword()}
	 * <li>{@link ToolCredential#getUsername()}
	 * </ul>
	 */
	@Test
	public void testConstructor() {
		ToolCredential actualToolCredential = new ToolCredential();
		actualToolCredential.setEmail("jane.doe@example.org");
		actualToolCredential.setPassword("iloveyou");
		actualToolCredential.setUsername("janedoe");
		String actualToStringResult = actualToolCredential.toString();
		assertEquals("jane.doe@example.org", actualToolCredential.getEmail());
		assertEquals("iloveyou", actualToolCredential.getPassword());
		assertEquals("janedoe", actualToolCredential.getUsername());
		assertEquals("ToolCredential(username=janedoe, password=iloveyou, email=jane.doe@example.org)",
				actualToStringResult);
	}

	/**
	 * Methods under test:
	 *
	 * <ul>
	 * <li>{@link ToolCredential#ToolCredential(String, String, String)}
	 * <li>{@link ToolCredential#setEmail(String)}
	 * <li>{@link ToolCredential#setPassword(String)}
	 * <li>{@link ToolCredential#setUsername(String)}
	 * <li>{@link ToolCredential#toString()}
	 * <li>{@link ToolCredential#getEmail()}
	 * <li>{@link ToolCredential#getPassword()}
	 * <li>{@link ToolCredential#getUsername()}
	 * </ul>
	 */
	@Test
	public void testConstructor2() {
		ToolCredential actualToolCredential = new ToolCredential("janedoe", "iloveyou", "jane.doe@example.org");
		actualToolCredential.setEmail("jane.doe@example.org");
		actualToolCredential.setPassword("iloveyou");
		actualToolCredential.setUsername("janedoe");
		String actualToStringResult = actualToolCredential.toString();
		assertEquals("jane.doe@example.org", actualToolCredential.getEmail());
		assertEquals("iloveyou", actualToolCredential.getPassword());
		assertEquals("janedoe", actualToolCredential.getUsername());
		assertEquals("ToolCredential(username=janedoe, password=iloveyou, email=jane.doe@example.org)",
				actualToStringResult);
	}

	/** Method under test: {@link ToolCredential#equals(Object)} */
	@Test
	public void testEquals() {
		assertNotEquals(new ToolCredential("janedoe", "iloveyou", "jane.doe@example.org"), null);
		assertNotEquals(new ToolCredential("janedoe", "iloveyou", "jane.doe@example.org"),
				"Different type to ToolCredential");
	}

	/**
	 * Methods under test:
	 *
	 * <ul>
	 * <li>{@link ToolCredential#equals(Object)}
	 * <li>{@link ToolCredential#hashCode()}
	 * </ul>
	 */
	@Test
	public void testEquals2() {
		ToolCredential toolCredential = new ToolCredential("janedoe", "iloveyou", "jane.doe@example.org");
		assertEquals(toolCredential, toolCredential);
		int expectedHashCodeResult = toolCredential.hashCode();
		assertEquals(expectedHashCodeResult, toolCredential.hashCode());
	}

	/**
	 * Methods under test:
	 *
	 * <ul>
	 * <li>{@link ToolCredential#equals(Object)}
	 * <li>{@link ToolCredential#hashCode()}
	 * </ul>
	 */
	@Test
	public void testEquals3() {
		ToolCredential toolCredential = new ToolCredential("janedoe", "iloveyou", "jane.doe@example.org");
		ToolCredential toolCredential1 = new ToolCredential("janedoe", "iloveyou", "jane.doe@example.org");

		assertEquals(toolCredential, toolCredential1);
		int expectedHashCodeResult = toolCredential.hashCode();
		assertEquals(expectedHashCodeResult, toolCredential1.hashCode());
	}

	/** Method under test: {@link ToolCredential#equals(Object)} */
	@Test
	public void testEquals4() {
		ToolCredential toolCredential = new ToolCredential("iloveyou", "iloveyou", "jane.doe@example.org");
		assertNotEquals(toolCredential, new ToolCredential("janedoe", "iloveyou", "jane.doe@example.org"));
	}

	/** Method under test: {@link ToolCredential#equals(Object)} */
	@Test
	public void testEquals5() {
		ToolCredential toolCredential = new ToolCredential(null, "iloveyou", "jane.doe@example.org");
		assertNotEquals(toolCredential, new ToolCredential("janedoe", "iloveyou", "jane.doe@example.org"));
	}

	/** Method under test: {@link ToolCredential#equals(Object)} */
	@Test
	public void testEquals6() {
		ToolCredential toolCredential = new ToolCredential("janedoe", "janedoe", "jane.doe@example.org");
		assertNotEquals(toolCredential, new ToolCredential("janedoe", "iloveyou", "jane.doe@example.org"));
	}

	/** Method under test: {@link ToolCredential#equals(Object)} */
	@Test
	public void testEquals7() {
		ToolCredential toolCredential = new ToolCredential("janedoe", null, "jane.doe@example.org");
		assertNotEquals(toolCredential, new ToolCredential("janedoe", "iloveyou", "jane.doe@example.org"));
	}

	/** Method under test: {@link ToolCredential#equals(Object)} */
	@Test
	public void testEquals8() {
		ToolCredential toolCredential = new ToolCredential("janedoe", "iloveyou", "janedoe");
		assertNotEquals(toolCredential, new ToolCredential("janedoe", "iloveyou", "jane.doe@example.org"));
	}

	/** Method under test: {@link ToolCredential#equals(Object)} */
	@Test
	public void testEquals9() {
		ToolCredential toolCredential = new ToolCredential("janedoe", "iloveyou", null);
		assertNotEquals(toolCredential, new ToolCredential("janedoe", "iloveyou", "jane.doe@example.org"));
	}

	/**
	 * Methods under test:
	 *
	 * <ul>
	 * <li>{@link ToolCredential#equals(Object)}
	 * <li>{@link ToolCredential#hashCode()}
	 * </ul>
	 */
	@Test
	public void testEquals10() {
		ToolCredential toolCredential = new ToolCredential(null, "iloveyou", "jane.doe@example.org");
		ToolCredential toolCredential1 = new ToolCredential(null, "iloveyou", "jane.doe@example.org");

		assertEquals(toolCredential, toolCredential1);
		int expectedHashCodeResult = toolCredential.hashCode();
		assertEquals(expectedHashCodeResult, toolCredential1.hashCode());
	}

	/**
	 * Methods under test:
	 *
	 * <ul>
	 * <li>{@link ToolCredential#equals(Object)}
	 * <li>{@link ToolCredential#hashCode()}
	 * </ul>
	 */
	@Test
	public void testEquals11() {
		ToolCredential toolCredential = new ToolCredential("janedoe", null, "jane.doe@example.org");
		ToolCredential toolCredential1 = new ToolCredential("janedoe", null, "jane.doe@example.org");

		assertEquals(toolCredential, toolCredential1);
		int expectedHashCodeResult = toolCredential.hashCode();
		assertEquals(expectedHashCodeResult, toolCredential1.hashCode());
	}
}
