package com.goforer.phogal.presentation.ui.compose.screen.home.setting.bookmark

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.goforer.phogal.core.ui.R
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture
import com.goforer.phogal.presentation.stateholder.uistate.PagingResult
import com.goforer.phogal.presentation.ui.compose.screen.home.common.InitScreen

@Composable
fun BookmarkedPhotosContent(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    bookmarkedPictures: LazyPagingItems<Picture>,
    enabledLoadPhotos: Boolean,
    isUserFollowed: (User) -> Boolean,
    onToggleFollow: (User) -> Unit,
    onShowUserInfo: (User) -> Unit,
    onItemClicked: (item: Picture, index: Int) -> Unit,
    onLoadResult: (PagingResult) -> Unit,
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit
) {
    if (bookmarkedPictures.itemCount > 0) {
        BookmarkedPhotosSection(
            modifier = modifier,
            paddingValues = paddingValues,
            photos = bookmarkedPictures,
            isUserFollowed = isUserFollowed,
            onToggleFollow = onToggleFollow,
            onShowUserInfo = onShowUserInfo,
            onItemClicked = onItemClicked,
            onLoadResult = onLoadResult,
            onViewPhotos = onViewPhotos
        )
    } else {
        if (enabledLoadPhotos) {
            BoxWithConstraints(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val isTablet = maxWidth > 600.dp

                InitScreen(
                    modifier = Modifier.padding(horizontal = if (isTablet) 40.dp else 16.dp),
                    text = stringResource(id = R.string.setting_no_bookmarked_photos)
                )
            }
        }
    }
}