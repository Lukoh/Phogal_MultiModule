package com.goforer.designsystem.component.paging

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.paging.compose.LazyPagingItems
import androidx.compose.foundation.lazy.rememberLazyListState as rememberStandardLazyListState

@Composable
fun <T : Any> LazyPagingItems<T>.rememberLazyListState(): LazyListState {
    // After recreation, LazyPagingItems first return 0 items, then the cached items.
    // This behavior/issue is resetting the LazyListState scroll position.
    // Below is a workaround. More info: https://issuetracker.google.com/issues/177245496.
    return rememberStandardLazyListState()
}