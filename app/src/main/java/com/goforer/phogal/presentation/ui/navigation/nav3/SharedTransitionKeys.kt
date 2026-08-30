package com.goforer.phogal.presentation.ui.navigation.nav3

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.staticCompositionLocalOf

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }

/**
 * Builds a stable, collision-free shared element key for a photo thumbnail ↔
 * detail pair. The photo `id` uniquely identifies the element within the app.
 *
 * Using a typed helper (instead of string concatenation scattered across
 * files) keeps the contract between the list screen and the detail screen
 * in one place — if the key scheme ever changes, there is only one file
 * to update.
 */
fun photoSharedElementKey(photoId: String): String = "photo-hero-$photoId"
