package com.irminsul.codex.data.export

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class JsonDocumentWriterTest {
    @Test
    fun writesExactUtf8BytesAndReturnsOnlyAfterClose() {
        val destination = ByteArrayOutputStream()
        val json = """{"name":"Aloy","note":"旅人 😀"}"""

        val byteCount = JsonDocumentWriter.write(json) { destination }

        assertArrayEquals(json.toByteArray(Charsets.UTF_8), destination.toByteArray())
        assertEquals(json.toByteArray(Charsets.UTF_8).size, byteCount)
    }

    @Test
    fun nullOutputStreamIsReportedAsFailure() {
        try {
            JsonDocumentWriter.write("{}") { null }
            fail("A null output stream must not be treated as a successful save.")
        } catch (expected: IOException) {
            assertEquals("The selected destination could not be opened.", expected.message)
        }
    }

    @Test
    fun propagatesOutputStreamOpenFailure() {
        val expected = IOException("provider unavailable")

        try {
            JsonDocumentWriter.write("{}") { throw expected }
            fail("An output-stream open failure must be propagated.")
        } catch (actual: IOException) {
            assertEquals(expected, actual)
        }
    }

    @Test
    fun propagatesWriteFailure() {
        val expected = IOException("disk full")
        val destination = object : OutputStream() {
            override fun write(value: Int) {
                throw expected
            }
        }

        try {
            JsonDocumentWriter.write("{}") { destination }
            fail("A write failure must be propagated.")
        } catch (actual: IOException) {
            assertEquals(expected, actual)
        }
    }

    @Test
    fun propagatesCloseFailureInsteadOfReportingCompletedWrite() {
        val expected = IOException("close failed")
        val destination = object : OutputStream() {
            override fun write(value: Int) = Unit
            override fun close() {
                throw expected
            }
        }

        try {
            JsonDocumentWriter.write("{}") { destination }
            fail("A close failure must be propagated.")
        } catch (actual: IOException) {
            assertEquals(expected, actual)
        }
    }
}
