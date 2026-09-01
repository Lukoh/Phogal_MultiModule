package com.goforer.phogal.presentation.ui.compose.screen.home.common.photo

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.goforer.designsystem.component.LoadingIndicator
import com.goforer.designsystem.component.snsShimmer
import com.goforer.designsystem.theme.DarkGreen10
import com.goforer.designsystem.theme.PhogalTheme

@Composable
fun LoadingPicture(
    modifier: Modifier = Modifier,
    enableLoadIndicator: Boolean = false
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
        ) {
            // Image Shimmer Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .snsShimmer()
            ) {
                if (enableLoadIndicator) {
                    LoadingIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = DarkGreen10
                    )
                }
            }

            // User Info Shimmer Placeholder
            Column {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .padding(start = 4.dp, end = 4.dp)
                        .heightIn(min = 76.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar Circle
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .snsShimmer()
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Name Line
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .height(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .snsShimmer()
                        )
                        // Stats Line
                        Box(
                            modifier = Modifier
                                .width(150.dp)
                                .height(10.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .snsShimmer()
                        )
                        // Updated At Line
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .height(10.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .snsShimmer()
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Follow Button Shimmer
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .snsShimmer()
                    )
                }

                // View Photos Text Shimmer
                Box(
                    modifier = Modifier
                        .padding(start = 56.dp, bottom = 12.dp)
                        .width(140.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .snsShimmer()
                )
            }
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
fun LoadingPicturePreview() {
    PhogalTheme {
        Box(modifier = Modifier.padding(8.dp)) {
            LoadingPicture(
                modifier = Modifier.fillMaxWidth(),
                enableLoadIndicator = true
            )
        }
    }
}
