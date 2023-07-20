/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.offline.runtime.api;

import java.util.List;

/**
 * Coverage for a line of source code.
 */
public class LineCoverage {
	/**
	 * Line number in the source code file.
	 */
	public int lineNumber;

	/**
	 * The number of code executions at least one byte-code instruction of this line.
	 * <p/>
	 * The accuracy of the value is not guaranteed, it is recommended to use this field in this form <code>(hit != 0)<code/>.
	 */
	public int hit;

	/**
	 * If there is a conditional expression and several code branches on one line, the coverage is measured for each of them individually.
	 * <p/>
	 * If there is no branching in the line, then empty list
	 * <p/>
	 * Ordered by branch number in ascending order.
	 */
	//
	public List<BranchCoverage> branches;
}