package com.goforer.phogal.presentation.stateholder.uistate.home.common.user.photos

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.paging.compose.LazyPagingItems
import com.goforer.designsystem.component.paging.rememberLazyListState
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import com.goforer.phogal.presentation.stateholder.uistate.home.common.base.BaseSectionUiState
import kotlinx.coroutines.CoroutineScope

@Stable
class UserPhotosSectionUiState internal constructor(
    val photos: LazyPagingItems<Photo>,
    override val scope: CoroutineScope,
    override val lazyListState: LazyListState,
) : BaseSectionUiState(
    scope = scope,
    lazyListState = lazyListState,
)

@Composable
fun rememberUserPhotosSectionUiState(
    photos: LazyPagingItems<Photo>,
    scope: CoroutineScope = rememberCoroutineScope(),
    lazyListState: LazyListState = photos.rememberLazyListState(),
): UserPhotosSectionUiState = remember(photos, scope, lazyListState) {
    UserPhotosSectionUiState(
        photos = photos,
        scope = scope,
        lazyListState = lazyListState,
    )
}
