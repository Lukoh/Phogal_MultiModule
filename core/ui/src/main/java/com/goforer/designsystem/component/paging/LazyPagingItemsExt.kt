package com.goforer.designsystem.component.paging

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.paging.compose.LazyPagingItems
import androidx.compose.foundation.lazy.rememberLazyListState as rememberStandardLazyListState

/**
 * Remembers a [LazyListState] that survives process recreation and screen unmounting / tab switching
 * when using [LazyPagingItems].
 *
 * After recreation or tab switching, [LazyPagingItems] initially returns a smaller batch of items (e.g. 20)
 * before subsequent pages are appended. Standard `scrollToItem` fails if the saved index exceeds initial [itemCount].
 *
 * This workaround saves the target scroll index/offset separately in [rememberSaveable],
 * and progressively scrolls to the target index as [itemCount] grows until the exact target item is reached.
 *
 * More info: https://issuetracker.google.com/issues/177245496.
 */
@Composable
fun <T : Any> LazyPagingItems<T>.rememberLazyListState(): LazyListState {
    var savedIndex by rememberSaveable { mutableIntStateOf(0) }
    var savedOffset by rememberSaveable { mutableIntStateOf(0) }

    val listState = rememberStandardLazyListState(savedIndex, savedOffset)

    // Save scroll position ONLY when the user is actively scrolling by touch.
    // This prevents system-triggered `scrollToItem` transitions from corrupting `savedIndex` with intermediate frames (e.g. index 13 instead of 15).
    LaunchedEffect(listState) {
        snapshotFlow {
            Triple(
                listState.firstVisibleItemIndex,
                listState.firstVisibleItemScrollOffset,
                listState.isScrollInProgress
            )
        }.collect { (index, offset, isScrolling) ->
            if (isScrolling && (index > 0 || offset > 0)) {
                savedIndex = index
                savedOffset = offset
            }
        }
    }

    // Progressively scroll towards savedIndex as Paging3 appends items upon tab re-entry
    LaunchedEffect(itemCount, savedIndex) {
        if (itemCount > 0 && (savedIndex > 0 || savedOffset > 0)) {
            val targetIndex = savedIndex.coerceAtMost(itemCount - 1)
            if (listState.firstVisibleItemIndex < savedIndex) {
                listState.scrollToItem(targetIndex, savedOffset)
            }
        }
    }

    return listState
}