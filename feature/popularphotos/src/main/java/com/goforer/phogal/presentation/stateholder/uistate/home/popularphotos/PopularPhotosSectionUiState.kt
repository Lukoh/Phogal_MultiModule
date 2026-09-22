package com.goforer.phogal.presentation.stateholder.uistate.home.popularphotos

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
class PopularPhotosSectionUiState internal constructor(
    val photos: LazyPagingItems<Photo>,
    val sessionId: Int,
    override val scope: CoroutineScope,
    override val lazyListState: LazyListState,
) : BaseSectionUiState(
    scope = scope,
    lazyListState = lazyListState
)

@Composable
fun rememberPopularPhotosSectionUiState(
    photos: LazyPagingItems<Photo>,
    sessionId: Int,
    scope: CoroutineScope = rememberCoroutineScope(),
    lazyListState: LazyListState = photos.rememberLazyListState(),
): PopularPhotosSectionUiState {
    return remember(photos, sessionId, scope, lazyListState) {
        PopularPhotosSectionUiState(
            photos = photos,
            sessionId = sessionId,
            scope = scope,
            lazyListState = lazyListState,
        )
    }
}
