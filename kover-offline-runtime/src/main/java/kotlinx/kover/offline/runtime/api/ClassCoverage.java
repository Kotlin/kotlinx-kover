/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.offline.runtime.api;

import java.util.List;

/**
 * Coverage for JVM class.
 */
public class ClassCoverage {
    /**
     * Fully-qualified JVM class name from byte code
     */
    public String className;

    /**
     * File name from byte code (without directories).
     * <p/>
     * Full file name is not stored in bytecode, only the file name without a path
     * which later makes it possible to "guess" in which source file the class was declared.
     */
    public String fileName;

    /**
     * Coverage for methods.
     */
    public List<MethodCoverage> methods;
}