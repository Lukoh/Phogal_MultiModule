package com.goforer.phogal.presentation.ui.compose.screen.home.common.user.userphotos

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.goforer.phogal.core.ui.R
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.presentation.stateholder.uistate.home.common.user.photos.UserPhotosContentUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.common.user.photos.rememberUserPhotosSectionUiState
import com.goforer.phogal.presentation.ui.compose.screen.home.common.InitScreen
import com.goforer.phogal.presentation.ui.compose.screen.home.gallery.SearchSection
import com.goforer.designsystem.theme.ColorSystemGray7
import com.goforer.designsystem.theme.PhogalTheme

@Composable
fun UserPhotosContent(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(4.dp),
    contentUiState: UserPhotosContentUiState,
    photos: LazyPagingItems<Photo>,
    onShowUserInfo: (User) -> Unit,
    onItemClicked: (String) -> Unit,
    onLoadResult: (isSuccessful: Boolean, message: String) -> Unit
) {
    if (contentUiState.name.isNotBlank()) {
        UserPhotosSection(
            modifier = modifier,
            paddingValues = paddingValues,
            photos = photos,
            sectionUiState = rememberUserPhotosSectionUiState(),
            onShowUserInfo = onShowUserInfo,
            onItemClicked = { photo, _ -> onItemClicked(photo.id) },
            onViewPhotos = { _, _, _, _ -> },
            onLoadResult = onLoadResult
        )
    } else {
        InitScreen(
            modifier = modifier,
            text = stringResource(id = R.string.search_photos)
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
fun UserPhotosContentPreview(modifier: Modifier = Modifier) {
    PhogalTheme {
        BoxWithConstraints(modifier = modifier) {
            val isWideScreen = maxWidth > 600.dp
            val dynamicPadding = if (isWideScreen) 32.dp else 2.dp
            val dynamicFontSize = if (isWideScreen) {
                MaterialTheme.typography.headlineMedium
            } else {
                MaterialTheme.typography.titleMedium
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SearchSection(
                    modifier = Modifier.padding(horizontal = dynamicPadding),
                    onSearched = { }
                )

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.search_photos),
                        style = dynamicFontSize.copy(
                            color = ColorSystemGray7,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
