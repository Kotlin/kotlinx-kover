/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.offline.runtime.api;

/**
 * Coverage of the code branch, if branching occurred on one line.
 */
public class BranchCoverage {
	/**
	 * The unique branch number for the line. For the same method bytecode, the branch numbers are stable.
	 * <p>
	 * The branch number is not directly related to the source code, and it is impossible to reliably understand which expression this branch refers to.
	 * </p>
	 */
	public int branchNumber;

	/**
	 * The number of code executions of this branch.
	 * <p>
	 * The accuracy of the value is not guaranteed, it is recommended to use this field in this form <code>(hit != 0)</code>.
	 * </p>
	 */
	public int hit;
}