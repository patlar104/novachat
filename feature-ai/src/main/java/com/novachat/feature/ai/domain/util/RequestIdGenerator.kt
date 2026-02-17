package com.novachat.feature.ai.domain.util

import java.security.SecureRandom
import java.util.UUID

/**
 * Generates time-ordered request IDs (UUIDv7-style) for SPEC-1 chat.
 * Format: 48-bit Unix ms | 4-bit version (7) | 12-bit rand | 2-bit variant | 62-bit random.
 */
object RequestIdGenerator {

    private val rng = SecureRandom()

    fun next(): String {
        val nowMs = System.currentTimeMillis()
        val msb = (nowMs and 0xFFFFFFFFFFFFL) shl 16
        val version = 0x7000L
        val randA = rng.nextInt(0x1000).toLong()
        val msbFull = msb or version or randA
        val lsb = (0x8000L shl 48) or (rng.nextLong() and 0x3FFFFFFFFFFFFFFFL)
        return UUID(msbFull, lsb).toString()
    }
}
