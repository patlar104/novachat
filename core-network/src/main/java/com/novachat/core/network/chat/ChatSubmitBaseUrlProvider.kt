package com.novachat.core.network.chat

/**
 * Provides base URLs for Path B chat submit (primary/secondary).
 * App module provides implementation that returns emulator URLs when
 * FUNCTIONS_EMULATOR_HOST is set, else production URLs.
 */
interface ChatSubmitBaseUrlProvider {
    fun getBaseUrls(): List<String>
}
