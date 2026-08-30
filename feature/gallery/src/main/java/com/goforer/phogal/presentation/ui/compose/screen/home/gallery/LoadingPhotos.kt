package com.goforer.phogal.presentation.ui.compose.screen.home.gallery

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.goforer.designsystem.component.LoadingIndicator
import com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.LoadingPicture
import com.goforer.designsystem.theme.DarkGreen10
import com.goforer.designsystem.theme.PhogalTheme

@Composable
fun LoadingPhotos(
    modifier: Modifier = Modifier,
    count: Int,
    enableLoadIndicator: Boolean = false
) {
    Box(modifier = modifier.clip(RoundedCornerShape(4.dp))) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            repeat(count) {
                LoadingPicture(
                    modifier = Modifier.fillMaxWidth(),
                    enableLoadIndicator = false
                )
            }
        }

        if (enableLoadIndicator) {
            LoadingIndicator(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(vertical = 22.dp),
                color = DarkGreen10
            )
        }
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
fun LoadingPhotosPreview(modifier: Modifier = Modifier) {
    PhogalTheme {
        Box(
            modifier = modifier.clip(RoundedCornerShape(4.dp))
        ) {
            Column {
                repeat(3) {
                    LoadingPicture(
                        modifier = Modifier.fillMaxWidth(),
                        enableLoadIndicator = false
                    )
                }
            }

            LoadingIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = DarkGreen10
            )
        }
    }
}
