package com.goforer.phogal.presentation.ui.compose.screen.home.popularphotos

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.designsystem.theme.PhogalTheme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PopularPhotosContent(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    photos: LazyPagingItems<Photo>,
    onShowUserInfo: (User) -> Unit,
    onItemClicked: (id: String, index: Int) -> Unit,
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    onLoadResult: (isSuccessful: Boolean, message: String) -> Unit,
    onLoadedPhotos: (isLoadedPhotos: Boolean) -> Unit
) {
    PopularPhotosSection(
        modifier = modifier,
        paddingValues = paddingValues,
        photos = photos,
        onShowUserInfo = onShowUserInfo,
        onItemClicked = { photo: Photo, index: Int -> onItemClicked(photo.id, index) },
        onViewPhotos = onViewPhotos,
        onLoadResult = onLoadResult,
        onLoadedPhotos = onLoadedPhotos
    )
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Suppress("unused")
@Preview(name = "Light Mode")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode",
    showSystemUi = true
)
@Composable
fun PopularPhotosContentPreview(modifier: Modifier = Modifier) {
    PhogalTheme {
        BoxWithConstraints(modifier = modifier) {
            Column(
                modifier = modifier.padding(0.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BoxWithConstraints(modifier = Modifier.weight(1f)) { }
            }
        }
    }
}
