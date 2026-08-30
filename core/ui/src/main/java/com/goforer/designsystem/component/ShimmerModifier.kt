package com.goforer.designsystem.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize

/**
 * SNS-style (Instagram-like) premium Shimmer effect.
 *
 * Features:
 * - Smooth diagonal sweep (45 degrees).
 * - Linear constant motion.
 * - Soft neutral colors that work in both Light and Dark themes.
 *
 * @param baseColor The background color of the skeleton element.
 * @param highlightColor The moving "sheen" color.
 * @param durationMillis Speed of the sweep animation.
 */
@Composable
fun Modifier.shimmer(
    baseColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
    highlightColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
    durationMillis: Int = 1300,
): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "shimmer_premium")

    val translateAnim by transition.animateFloat(
        initialValue = -2f * (size.width.toFloat() + size.height.toFloat()),
        targetValue = 2f * (size.width.toFloat() + size.height.toFloat()),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val brush = if (size.width > 0 && size.height > 0) {
        Brush.linearGradient(
            colors = listOf(
                baseColor,
                highlightColor,
                baseColor,
            ),
            start = Offset(translateAnim, translateAnim),
            end = Offset(
                translateAnim + size.width.toFloat(),
                translateAnim + size.height.toFloat()
            )
        )
    } else {
        // Fallback for initial state
        Brush.linearGradient(listOf(baseColor, baseColor))
    }

    this
        .onGloballyPositioned { size = it.size }
        .background(brush)
}

/**
 * Standard shimmer with predefined premium SNS colors.
 */
@Composable
fun Modifier.snsShimmer(): Modifier {
    return if (isSystemInDarkTheme()) {
        shimmer(
            baseColor = Color(0xFF242424),
            highlightColor = Color(0xFF323232),
            durationMillis = 1200
        )
    } else {
        shimmer(
            baseColor = Color(0xFFEBEBEB),
            highlightColor = Color(0xFFF5F5F5),
            durationMillis = 1200
        )
    }

}