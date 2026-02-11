package com.novachat.feature.ai.presentation.model

data class MessageStats(
    val userCount: Int,
    val aiCount: Int
) {
    val total: Int get() = userCount + aiCount

    fun isEmpty(): Boolean = total == 0
}
