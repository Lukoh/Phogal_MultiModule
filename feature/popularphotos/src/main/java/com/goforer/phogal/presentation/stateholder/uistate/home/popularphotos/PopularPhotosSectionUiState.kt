package com.goforer.phogal.presentation.stateholder.uistate.home.popularphotos

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
class PopularPhotosSectionUiState internal constructor(
    photos: LazyPagingItems<Photo>,
    val sessionId: Int,
    override val scope: CoroutineScope,
    override val lazyListState: LazyListState,
) : BaseSectionUiState(
    scope = scope,
    lazyListState = lazyListState
) {
    var photos: LazyPagingItems<Photo> by mutableStateOf(photos)
}

@Composable
fun rememberPopularPhotosSectionUiState(
    photos: LazyPagingItems<Photo>,
    sessionId: Int,
    scope: CoroutineScope = rememberCoroutineScope(),
    lazyListState: LazyListState = photos.rememberLazyListState(),
): PopularPhotosSectionUiState {
    val uiState = remember(sessionId) {
        PopularPhotosSectionUiState(
            photos = photos,
            sessionId = sessionId,
            scope = scope,
            lazyListState = lazyListState,
        )
    }
    uiState.photos = photos
    return uiState
}
