package com.irminsul.codex.data.local

/**
 * Keeps each SQLite text value small enough to fit safely in Android CursorWindow.
 * Chunk boundaries are character-based and preserve the original JSON exactly.
 */
internal object SnapshotJsonChunks {
    const val CHUNK_SIZE = 32 * 1024

    fun split(value: String): List<String> =
        if (value.isEmpty()) emptyList() else value.chunked(CHUNK_SIZE)

    fun join(chunks: List<String>): String = chunks.joinToString(separator = "")
}
