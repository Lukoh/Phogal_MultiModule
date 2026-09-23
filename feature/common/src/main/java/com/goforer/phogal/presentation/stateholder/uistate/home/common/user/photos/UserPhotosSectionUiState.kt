package com.goforer.phogal.presentation.stateholder.uistate.home.common.user.photos

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.paging.compose.LazyPagingItems
import com.goforer.designsystem.component.paging.rememberLazyListState
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import com.goforer.phogal.presentation.stateholder.uistate.home.common.base.BaseSectionUiState
import kotlinx.coroutines.CoroutineScope

@Stable
class UserPhotosSectionUiState internal constructor(
    photos: LazyPagingItems<Photo>,
    override val scope: CoroutineScope,
    override val lazyListState: LazyListState,
) : BaseSectionUiState(
    scope = scope,
    lazyListState = lazyListState,
) {
    var photos: LazyPagingItems<Photo> by mutableStateOf(photos)
}

@Composable
fun rememberUserPhotosSectionUiState(
    photos: LazyPagingItems<Photo>,
    scope: CoroutineScope = rememberCoroutineScope(),
    lazyListState: LazyListState = photos.rememberLazyListState(),
): UserPhotosSectionUiState {
    val uiState = remember {
        UserPhotosSectionUiState(
            photos = photos,
            scope = scope,
            lazyListState = lazyListState,
        )
    }
    uiState.photos = photos
    return uiState
}
