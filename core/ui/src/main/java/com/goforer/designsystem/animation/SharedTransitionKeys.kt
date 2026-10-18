package com.goforer.designsystem.animation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.staticCompositionLocalOf

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }

val LocalNavAnimatedVisibilityScope = staticCompositionLocalOf<AnimatedVisibilityScope?> { null }

/**
 * Builds a stable, collision-free shared element key for a photo thumbnail ↔
 * detail pair. The photo `id` uniquely identifies the element within the app.
 */
fun photoSharedElementKey(photoId: String): String = "photo-hero-$photoId"
