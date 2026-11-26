package com.irminsul.codex.data.export

import java.io.IOException
import java.io.OutputStream

internal object JsonDocumentWriter {
    /**
     * Writes the complete UTF-8 document and closes the destination before returning.
     * A null destination is a failure, never a successful no-op.
     */
    @Throws(Exception::class)
    fun write(
        content: String,
        openOutputStream: () -> OutputStream?
    ): Int {
        val bytes = content.toByteArray(Charsets.UTF_8)
        val output = openOutputStream()
            ?: throw IOException("The selected destination could not be opened.")

        output.use { stream ->
            stream.write(bytes)
        }
        return bytes.size
    }
}
