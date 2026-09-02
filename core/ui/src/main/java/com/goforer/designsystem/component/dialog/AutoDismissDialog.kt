package com.goforer.designsystem.component.dialog

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.goforer.designsystem.theme.ColorBlackLight
import com.goforer.designsystem.theme.PhogalTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AutoDismissDialog(
    message: String,
    visible: Boolean,
    durationMillis: Long = 1000L,
    onDismiss: () -> Unit
) {
    if (visible) {
        val alpha = remember { Animatable(0f) }
        val scale = remember { Animatable(0.8f) }

        LaunchedEffect(Unit) {
            launch {
                alpha.animateTo(1f, animationSpec = tween(durationMillis = 300))
            }
            launch {
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
            delay(durationMillis)
            alpha.animateTo(0f, animationSpec = tween(durationMillis = 300))
            onDismiss()
        }

        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(
                        alpha = alpha.value,
                        scaleX = scale.value,
                        scaleY = scale.value
                    ),
                contentAlignment = Alignment.Center
            ) {
                AutoDismissDialogContent(message = message)
            }
        }
    }
}

@Composable
fun AutoDismissDialogContent(
    message: String
) {
    Surface(
        modifier = Modifier.padding(horizontal = 32.dp),
        color = ColorBlackLight.copy(alpha = 0.85f),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 8.dp
    ) {
        Text(
            text = message,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        )
    }
}

@Preview(name = "Light Mode")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode",
    showSystemUi = true
)
@Composable
fun AutoDismissDialogPreview() {
    PhogalTheme {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
            AutoDismissDialogContent(message = "Changes saved successfully")
        }
    }
}
