/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.common.model.application;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public final class AccountHierarchyTest {
	/// region Test suites for executable
	/// com.publicissapient.kpidashboard.common.model.application.AccountHierarchy.equals

	/// region SYMBOLIC EXECUTION: SUCCESSFUL EXECUTIONS for method
	/// equals(java.lang.Object)

	/**
	 * @utbot.classUnderTest {@link AccountHierarchy}
	 * @utbot.methodUnderTest {@link AccountHierarchy#equals(Object)}
	 * @utbot.executesCondition {@code (null != obj): False}
	 * @utbot.executesCondition {@code (null == obj): True}
	 * @utbot.returnsFrom {@code return false;}
	 */
	@Test
	public void testEquals_NullEqualsObj() {
		AccountHierarchy accountHierarchy = new AccountHierarchy();

		boolean actual = accountHierarchy.equals(null);

		assertFalse(actual);
	}

	/**
	 * @utbot.classUnderTest {@link AccountHierarchy}
	 * @utbot.methodUnderTest {@link AccountHierarchy#equals(Object)}
	 * @utbot.executesCondition {@code (null != obj): True}
	 * @utbot.executesCondition {@code (this.getClass() != obj.getClass()): True}
	 * @utbot.returnsFrom {@code return false;}
	 */
	@Test
	public void testEquals_ThisGetClassNotEqualsObjGetClass() {
		AccountHierarchy accountHierarchy = new AccountHierarchy();
		short[] obj = {};

		boolean actual = accountHierarchy.equals(obj);

		assertFalse(actual);
	}

	/// endregion

	/// region Errors report for equals

	public void testEquals_errors() {
		// Couldn't generate some tests. List of errors:
		//
		// 1 occurrences of:
		// Default concrete execution failed

	}

	/// endregion

	/// endregion

	/// region Test suites for executable
	/// com.publicissapient.kpidashboard.common.model.application.AccountHierarchy.hashCode

	/// region SYMBOLIC EXECUTION: SUCCESSFUL EXECUTIONS for method hashCode()

	/**
	 * @utbot.classUnderTest {@link AccountHierarchy}
	 * @utbot.methodUnderTest {@link AccountHierarchy#hashCode()}
	 * @utbot.invokes {@link java.util.Objects#hash(Object[])}
	 * @utbot.returnsFrom {@code return Objects.hash(this.nodeId, this.path, this.beginDate,
	 *     this.endDate, this.releaseState);}
	 */
	@Test
	public void testHashCode_ObjectsHash() {
		AccountHierarchy accountHierarchy = new AccountHierarchy(null, null, null, null, null, null, null, null, null, null,
				null, null, null);

		int actual = accountHierarchy.hashCode();

		assertEquals(28629151, actual);
	}

	/// endregion

	/// region Errors report for hashCode

	public void testHashCode_errors() {
		// Couldn't generate some tests. List of errors:
		//
		// 1 occurrences of:
		// Default concrete execution failed

	}
	/// endregion

	/// endregion
}
