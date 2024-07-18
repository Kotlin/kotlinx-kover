/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.maven.plugin.mojo.abstracts

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject

/**
 * Common Mojo logic for all Kover goals.
 */
abstract class AbstractKoverMojo : AbstractMojo() {
    @Parameter(property = "project", required = true, readonly = true)
    protected lateinit var project: MavenProject

    /**
     * Disable Kover goal
     */
    @Parameter(property = "kover.skip", defaultValue = "false")
    private var skip: Boolean = false

    protected abstract fun doExecute()

    final override fun execute() {
        if (checkKoverIsEnabled()) {
            doExecute()
        }
    }

    protected fun checkKoverIsEnabled(): Boolean {
        if (skip) {
            log.info("Kover is disabled by property 'kover.skip'")
        }
        return !skip
    }
}