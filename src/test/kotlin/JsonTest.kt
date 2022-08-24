/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.json

import kotlinx.kover.util.json.*
import java.io.File
import java.math.BigDecimal
import kotlin.test.*

class JsonTest {
    private val encodingText = """{
  "field": "text",
  "numberField": 42,
  "array": [
    "array \" value",
    true,
    "/\\test"
  ],
  "BD": "230"
}"""

    private val encodingObject = mapOf(
        "field" to "text",
        "numberField" to 42,
        "array" to listOf("array \" value", true, File("/\\test")),
        "BD" to 230.toBigDecimal()
    )

    private val decodingText = """[{
  "very long field name to check array expansion": {"0": {"min": {"all": 3.0000007}}},
  "id": 0
}]"""

    private val decodedObject = listOf(
        mapOf(
            "very long field name to check array expansion" to mapOf("0" to mapOf("min" to mapOf("all" to "3.0000007".toBigDecimal()))),
            "id" to BigDecimal.ZERO
        )
    )

    @Test
    fun testEncoding() {
        val file = File.createTempFile("encoding", "json")
        file.writeJsonObject(encodingObject)
        assertEquals(encodingText, file.readText())
    }

    @Test
    fun testDecoding() {
        val file = File.createTempFile("decoding", "json")
        file.writeText(decodingText)
        val decoded = file.readJsonArray()
        assertEquals(decodedObject, decoded)
    }
}
