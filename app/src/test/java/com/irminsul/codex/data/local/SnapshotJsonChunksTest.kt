package com.irminsul.codex.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SnapshotJsonChunksTest {
    @Test
    fun splitsLargeJsonIntoCursorWindowSafeChunksAndRestoresExactContent() {
        val original = buildString {
            repeat(SnapshotJsonChunks.CHUNK_SIZE * 3 + 137) { index ->
                append(('a'.code + (index % 26)).toChar())
            }
        }

        val chunks = SnapshotJsonChunks.split(original)

        assertEquals(4, chunks.size)
        assertTrue(chunks.all { it.length <= SnapshotJsonChunks.CHUNK_SIZE })
        assertEquals(original, SnapshotJsonChunks.join(chunks))
    }

    @Test
    fun doesNotSplitUtf16SurrogatePairAtChunkBoundary() {
        val original = "a".repeat(SnapshotJsonChunks.CHUNK_SIZE - 1) + "😀" + "z"

        val chunks = SnapshotJsonChunks.split(original)

        assertEquals(2, chunks.size)
        assertTrue(chunks.first().last().isHighSurrogate().not())
        assertTrue(chunks.last().first().isHighSurrogate())
        assertEquals(original, SnapshotJsonChunks.join(chunks))
    }

    @Test
    fun emptyJsonProducesNoChunks() {
        assertTrue(SnapshotJsonChunks.split("").isEmpty())
        assertEquals("", SnapshotJsonChunks.join(emptyList()))
    }
}
