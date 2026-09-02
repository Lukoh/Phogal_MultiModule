package com.goforer.phogal.presentation.ui.compose.screen.home.setting

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.size.Size
import com.goforer.designsystem.component.IconContainer
import com.goforer.designsystem.component.ImageCrossFade
import com.goforer.designsystem.component.loadImagePainter
import com.goforer.phogal.core.ui.R
import com.goforer.phogal.data.model.remote.response.setting.Profile
import com.goforer.designsystem.theme.ColorBgSecondary

@Composable
fun SetProfileItem(
    modifier: Modifier = Modifier,
    profileUiState: Profile,
    onItemClicked: (profileUiState: Profile) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ColorBgSecondary)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconContainer(72.dp) {
                Box {
                    val painter = loadImagePainter(
                        data = profileUiState.profileImage,
                        size = Size.ORIGINAL
                    )

                    ImageCrossFade(painter = painter, durationMillis = null)
                    Image(
                        painter = painter,
                        contentDescription = "Profile",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant,
                                CircleShape
                            ),
                        Alignment.Center,
                        contentScale = ContentScale.Crop
                    )

                    if (painter.state is AsyncImagePainter.State.Loading) {
                        val preloadPainter = loadImagePainter(
                            data = R.drawable.ic_profile_logo,
                            size = Size.ORIGINAL
                        )

                        Image(
                            painter = preloadPainter,
                            contentDescription = null,
                            modifier = Modifier
                                .size(72.dp)
                                .align(Alignment.Center),
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profileUiState.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = profileUiState.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Button(
            onClick = { onItemClicked(profileUiState) },
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = stringResource(id = R.string.setting_profile_edit),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
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
fun SetProfileItemPreview() {
    val profileUiState = Profile(
        id = 0,
        name = "Lukoh",
        sex = "남성",
        favor = true,
        followed = true,
        email = "lukoh.nam@gmail.com",
        profileImage = "https://avatars.githubusercontent.com/u/18302717?v=4",
        personality = "sociable & gregarious",
        cellphone = "+820101111-1111",
        address = "",
        birthday = "Mar, 04, 1999",
        reputation = "Professional mobile SW engineer...",
        deleted = false
    )

    SetProfileItem(
        profileUiState = profileUiState,
        onItemClicked = {}
    )
}
