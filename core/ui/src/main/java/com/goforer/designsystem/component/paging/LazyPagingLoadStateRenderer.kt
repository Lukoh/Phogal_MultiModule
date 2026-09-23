package com.goforer.designsystem.component.paging

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
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

            refresh is LoadState.Loading -> {
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

data class PagingRenderState<T : Any>(
    val items: LazyPagingItems<T>,
    val loadingDone: Boolean,
    val isInitialRefresh: Boolean = false
)

data class PagingRenderCallbacks(
    val loadingPlaceholder: LazyListScope.() -> Unit,
    val appendLoading: LazyListScope.() -> Unit,
    val emptyState: LazyListScope.() -> Unit = { item { EmptyStatePlaceholder() } },
    val errorState: (LazyListScope.(Throwable) -> Unit)? = null
)

/**
 * A unified renderer for [LazyPagingItems] load states.
 * Dispatches the current [LoadState] into the appropriate sub-renderer.
 *
 * @param state The [PagingRenderState] containing paging items and state flags.
 * @param callbacks The [PagingRenderCallbacks] containing slots for various load states.
 * @param modifier Modifier for styling rendering layout.
 * @param content The primary content to render when items are available.
 */
@OptIn(ExperimentalFoundationApi::class)
fun <T : Any> LazyListScope.renderPagingLoadState(
    state: PagingRenderState<T>,
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit,
    callbacks: PagingRenderCallbacks,
) {
    val items = state.items
    val refresh = items.loadState.refresh
    val append = items.loadState.append
    val isRefreshing = refresh is LoadState.Loading || items.loadState.mediator?.refresh is LoadState.Loading
    val isEmpty = items.itemCount == 0

    val actualErrorState = callbacks.errorState ?: { error ->
        item {
            ErrorStateHost(
                modifier = if (isEmpty) modifier.fillParentMaxSize() else modifier.fillMaxWidth(),
                isFullMaxSize = isEmpty,
                throwable = error,
                onRetry = items::retry
            )
        }
    }

    if (isEmpty || (state.isInitialRefresh && isRefreshing)) {
        when {
            refresh is LoadState.Error -> actualErrorState(refresh.error)
            state.loadingDone && append.endOfPaginationReached -> callbacks.emptyState(this)
            else -> callbacks.loadingPlaceholder(this)
        }
    } else {
        content()

        if (refresh is LoadState.Error) {
            actualErrorState(refresh.error)
        }

        when (append) {
            is LoadState.Loading -> callbacks.appendLoading(this)
            is LoadState.Error -> actualErrorState(append.error)
            else -> Unit
        }
    }
}
