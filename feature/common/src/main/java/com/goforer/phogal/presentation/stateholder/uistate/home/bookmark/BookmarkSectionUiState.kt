package com.goforer.phogal.presentation.stateholder.uistate.home.bookmark

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.paging.compose.LazyPagingItems
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture
import com.goforer.phogal.presentation.stateholder.uistate.home.common.base.BaseSectionUiState
import kotlinx.coroutines.CoroutineScope

@Stable
class BookmarkSectionUiState internal constructor(
    val photos: LazyPagingItems<Picture>,
    override val scope: CoroutineScope,
    override val lazyListState: LazyListState,
) : BaseSectionUiState(
    scope = scope,
    lazyListState = lazyListState,
)

@Composable
fun rememberBookmarkSectionUiState(
    photos: LazyPagingItems<Picture>,
    scope: CoroutineScope = rememberCoroutineScope(),
    lazyListState: LazyListState = LazyListState(),
): BookmarkSectionUiState = remember(photos, scope, lazyListState) {
    BookmarkSectionUiState(
        photos = photos,
        scope = scope,
        lazyListState = lazyListState,
    )
}
