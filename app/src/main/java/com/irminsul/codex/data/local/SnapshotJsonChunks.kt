package com.irminsul.codex.data.local

/**
 * Keeps each SQLite text value small enough to fit safely in Android CursorWindow.
 * Chunk boundaries preserve UTF-16 surrogate pairs and the original JSON exactly.
 */
internal object SnapshotJsonChunks {
    const val CHUNK_SIZE = 32 * 1024

    fun split(value: String): List<String> {
        if (value.isEmpty()) return emptyList()
        val chunks = mutableListOf<String>()
        var start = 0
        while (start < value.length) {
            var end = minOf(start + CHUNK_SIZE, value.length)
            if (
                end < value.length &&
                value[end - 1].isHighSurrogate() &&
                value[end].isLowSurrogate()
            ) {
                end--
            }
            chunks += value.substring(start, end)
            start = end
        }
        return chunks
    }

    fun join(chunks: List<String>): String = chunks.joinToString(separator = "")
}
