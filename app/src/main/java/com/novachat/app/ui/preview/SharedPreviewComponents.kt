package com.novachat.app.ui.preview

import androidx.compose.ui.tooling.preview.Devices

/**
 * Shared preview components and constants for all @Preview annotations.
 *
 * This file centralizes device specifications, colors, and utilities
 * used across all preview Composables to maintain consistency and
 * enable easy updates to preview configurations.
 */
object PreviewDevices {
    // Standard phone devices
    const val DEVICE_PHONE_SMALL = "spec:width=360dp,height=740dp,dpi=420"
    const val DEVICE_PHONE = "spec:width=411dp,height=891dp,dpi=420"
    const val DEVICE_PHONE_LARGE = "spec:width=480dp,height=854dp,dpi=420"

    // Tablet devices
    const val DEVICE_TABLET_PORTRAIT = "spec:width=600dp,height=800dp,dpi=160"
    const val DEVICE_TABLET_LANDSCAPE = "spec:width=1000dp,height=600dp,dpi=160"

    // Foldable device
    const val DEVICE_FOLDABLE = "spec:width=412dp,height=915dp,dpi=420"

    // Landscape orientation
    const val DEVICE_PHONE_LANDSCAPE = "spec:width=854dp,height=480dp,dpi=420"
    const val DEVICE_PHONE_LANDSCAPE_SMALL = "spec:width=640dp,height=360dp,dpi=420"

    // High-density devices
    const val DEVICE_COMPACT = "spec:width=320dp,height=640dp,dpi=420"

    // Common preset devices from Compose
    const val DEVICE_PIXEL_6 = Devices.PIXEL_6
    const val DEVICE_PIXEL_6_PRO = Devices.PIXEL_6_PRO
    const val DEVICE_PIXEL_TABLET = Devices.PIXEL_TABLET
    const val DEVICE_FOLDABLE_PRESET = Devices.FOLDABLE
}

/**
 * Preview color scheme configuration.
 *
 * Used to verify theme colors across light and dark modes.
 */
object PreviewColors {
    const val LIGHT_BACKGROUND = 0xFFFFFBFE
    const val DARK_BACKGROUND = 0xFF1C1B1F
}
