package com.publicissapient.kpidashboard.common.model.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

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
	 * @utbot.returnsFrom {@code return Objects.hash(this.nodeId, this.path, this.beginDate, this.endDate, this.releaseState);}
	 */
	@Test
	public void testHashCode_ObjectsHash() {
		AccountHierarchy accountHierarchy = new AccountHierarchy(null, null, null, null, null, null, null, null, null,
				null, null, null, null);

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
