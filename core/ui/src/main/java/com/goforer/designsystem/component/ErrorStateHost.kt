package com.goforer.designsystem.component

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.goforer.designsystem.theme.PhogalTheme
import com.goforer.phogal.core.ui.R

@Composable
fun ErrorStateHost(
    throwable: Throwable,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = true,
        modifier = modifier,
        enter = scaleIn(transformOrigin = TransformOrigin(0f, 0f)) + fadeIn() +
                expandIn(expandFrom = Alignment.TopStart),
        exit = scaleOut(transformOrigin = TransformOrigin(0f, 0f)) + fadeOut() +
                shrinkOut(shrinkTowards = Alignment.TopStart)
    ) {
        ErrorStatePlaceholder(
            title = stringResource(id = R.string.error_dialog_title),
            message = throwable.message ?: stringResource(id = R.string.error_dialog_content),
            onRetry = onRetry
        )
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
@Composable
fun ErrorStateHostPreview() {
    PhogalTheme {
        ErrorStateHost(
            throwable = RuntimeException("OAuth Token Error"),
            onRetry = {}
        )
    }
}
