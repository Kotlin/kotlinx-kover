/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.offline.runtime.api;

import java.util.List;

/**
 * Coverage of method in the JVM class.
 */
public class MethodCoverage {
	/**
	 * JVM signature of the method.
	 * <p/>
	 * example: <code>convert(I)Ljava.lang.String</code>
	 */
	public String signature;

	/**
	 * The number of code executions of the first instruction of this method.
	 * <p/>
	 * The accuracy of the value is not guaranteed, it is recommended to use this field in this form <code>(hit != 0)<code/>.
	 */
	public int hit;

	/**
	 * Coverage for each line of source code for this method.
	 * <p/>
	 * Ordered by line number in ascending order.
	 */
	public List<LineCoverage> lines;
}