package com.goforer.phogal.presentation.ui.compose.screen.home.setting.following

import android.content.res.Configuration
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
import com.goforer.designsystem.component.snsShimmer
import com.goforer.designsystem.theme.PhogalTheme

@Composable
fun LoadingUser(
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Avatar Circle
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .snsShimmer()
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Name Line
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .snsShimmer()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .snsShimmer()
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Info Lines
            repeat(3) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .snsShimmer()
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .width(200.dp)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .snsShimmer()
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))
            // Portfolio Button Placeholder
            Box(
                modifier = Modifier
                    .align(Alignment.End)
                    .width(100.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .snsShimmer()
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
fun LoadingUserPreview() {
    PhogalTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            LoadingUser(modifier = Modifier.fillMaxWidth())
        }
    }
}
