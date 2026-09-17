package com.goforer.designsystem.component.paging

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.goforer.designsystem.component.EmptyStatePlaceholder
import com.goforer.designsystem.component.ErrorStateHost
import com.goforer.phogal.presentation.stateholder.uistate.PagingResult
import com.goforer.phogal.presentation.stateholder.uistate.toErrorEntity
import timber.log.Timber

data class PagingLoadStateCallbacks(
    val onLoadingStarted: () -> Unit,
    val onLoadingDone: () -> Unit,
    val onLoadResult: (PagingResult) -> Unit,
    val onRefreshTransition: (isRefreshing: Boolean) -> Unit = {},
    val onPaginationReached: () -> Unit = {}
)

/**
 * A side effect handler for [LazyPagingItems] load states.
 * Detects state changes and handles side effects such as success/error callbacks,
 * loading state updates, and logging.
 *
 * @param pagingItems The [LazyPagingItems] to monitor.
 * @param callbacks The [PagingLoadStateCallbacks] defining behavior for lifecycle hooks.
 * @param logTag Tag for logging pagination events.
 */
@Composable
fun <T : Any> PagingLoadStateEffect(
    pagingItems: LazyPagingItems<T>,
    callbacks: PagingLoadStateCallbacks,
    logTag: String = "PagingLoadStateEffect"
) {
    var hasStartedLoading by remember(pagingItems) { mutableStateOf(false) }

    LaunchedEffect(pagingItems.loadState) {
        val refresh = pagingItems.loadState.refresh
        val append = pagingItems.loadState.append

        when {
            refresh is LoadState.Error -> {
                callbacks.onLoadResult(PagingResult.Error(refresh.error.toErrorEntity()))
            }

            append is LoadState.Error -> {
                callbacks.onLoadResult(PagingResult.Error(append.error.toErrorEntity()))
            }

            refresh is LoadState.Loading || append is LoadState.Loading -> {
                hasStartedLoading = true
                callbacks.onLoadingStarted()
                callbacks.onLoadResult(PagingResult.Loading)
            }

            refresh is LoadState.NotLoading -> {
                callbacks.onLoadResult(PagingResult.Success(""))
            }
        }

        if (append is LoadState.NotLoading && append.endOfPaginationReached) {
            Timber.tag(logTag).d("Pagination reached to the end of page")
            callbacks.onPaginationReached()
        }
    }

    LaunchedEffect(pagingItems.loadState.refresh, pagingItems.itemCount) {
        val refresh = pagingItems.loadState.refresh
        val append = pagingItems.loadState.append

        if (refresh is LoadState.NotLoading) {
            if (pagingItems.itemCount > 0 || (hasStartedLoading && append.endOfPaginationReached)) {
                callbacks.onLoadingDone()
            }
        }

        if (refresh !is LoadState.Loading) {
            callbacks.onRefreshTransition(false)
        }
    }
}

/**
 * A unified renderer for [LazyPagingItems] load states.
 * Dispatches the current [LoadState] into the appropriate sub-renderer.
 *
 * @param items The [LazyPagingItems] to render.
 * @param loadingDone Flag indicating if initial loading is considered complete by the UI state holder.
 * @param content The primary content to render when items are available.
 * @param loadingPlaceholder UI shown when the list is empty and loading.
 * @param emptyState UI shown when the list is empty and not loading.
 * @param errorState UI shown for error states, either as a full-screen state or a list item.
 * @param appendLoading UI shown at the end of the list when loading more items.
 */
@OptIn(ExperimentalFoundationApi::class)
fun <T : Any> LazyListScope.renderPagingLoadState(
    items: LazyPagingItems<T>,
    loadingDone: Boolean,
    content: LazyListScope.() -> Unit,
    loadingPlaceholder: LazyListScope.() -> Unit,
    emptyState: LazyListScope.() -> Unit = { item { EmptyStatePlaceholder() } },
    errorState: LazyListScope.(Throwable) -> Unit = { error ->
        item { ErrorStateHost(throwable = error, onRetry = { items.retry() }) }
    },
    appendLoading: LazyListScope.() -> Unit
) {
    val refresh = items.loadState.refresh
    val append = items.loadState.append
    val isRefreshing = refresh is LoadState.Loading ||
            items.loadState.mediator?.refresh is LoadState.Loading

    if (items.itemCount == 0) {
        when {
            isRefreshing -> loadingPlaceholder()
            refresh is LoadState.Error -> errorState(refresh.error)
            else -> {
                // When a new stream arrives, the loadState could be briefly NotLoading
                // before it flips to Loading or right before itemCount is dispatched.
                // We should only show empty state if we've actually completed loading and confirmed there is zero items.
                if (loadingDone && append.endOfPaginationReached) {
                    emptyState()
                } else {
                    loadingPlaceholder()
                }
            }
        }
    } else {
        content()

        if (refresh is LoadState.Error) {
            errorState(refresh.error)
        }

        when (append) {
            is LoadState.Loading -> appendLoading()
            is LoadState.Error -> errorState(append.error)
            is LoadState.NotLoading -> Unit
        }
    }
}
