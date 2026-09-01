package com.goforer.designsystem.component.paging

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyListScope
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.goforer.designsystem.component.EmptyContent
import com.goforer.designsystem.component.ErrorRow
import timber.log.Timber

/**
 * A unified renderer for [LazyPagingItems] load states.
 * Dispatches the current [LoadState] into the appropriate sub-renderer.
 *
 * @param items The [LazyPagingItems] to render.
 * @param loadingDone Flag indicating if initial loading is considered complete by the UI state holder.
 * @param onLoading Callback triggered when any load state is [LoadState.Loading].
 * @param onSuccess Callback triggered when the primary load state is [LoadState.NotLoading].
 * @param onError Callback triggered when any load state is [LoadState.Error].
 * @param content The primary content to render when items are available.
 * @param loadingPlaceholder UI shown when the list is empty and loading.
 * @param emptyState UI shown when the list is empty and not loading.
 * @param errorRow UI shown for error states, either as a full-screen state or a list item.
 * @param appendLoading UI shown at the end of the list when loading more items.
 */
@OptIn(ExperimentalFoundationApi::class)
fun <T : Any> LazyListScope.renderPagingLoadState(
    items: LazyPagingItems<T>,
    loadingDone: Boolean,
    onLoading: () -> Unit,
    onSuccess: () -> Unit,
    onError: (Throwable) -> Unit,
    content: LazyListScope.() -> Unit,
    loadingPlaceholder: LazyListScope.() -> Unit,
    emptyState: LazyListScope.() -> Unit = { item { EmptyContent() } },
    errorRow: LazyListScope.(Throwable) -> Unit = { error ->
        item { ErrorRow(throwable = error, onRetry = { items.retry() }) }
    },
    appendLoading: LazyListScope.() -> Unit
) {
    val refresh = items.loadState.refresh
    val append = items.loadState.append

    when {
        refresh is LoadState.Error -> onError(refresh.error)
        append is LoadState.Error -> onError(append.error)
        refresh is LoadState.Loading || append is LoadState.Loading -> onLoading()
        refresh is LoadState.NotLoading -> onSuccess()
    }

    if (items.itemCount == 0) {
        when (refresh) {
            is LoadState.Loading -> loadingPlaceholder()
            is LoadState.Error -> errorRow(refresh.error)
            is LoadState.NotLoading if loadingDone -> emptyState()
            is LoadState.NotLoading if true -> loadingPlaceholder()
            is LoadState.NotLoading -> TODO()
        }
    } else {
        content()

        if (refresh is LoadState.Error) {
            errorRow(refresh.error)
        }

        when (append) {
            is LoadState.Loading -> appendLoading()
            is LoadState.Error -> errorRow(append.error)
            is LoadState.NotLoading -> {
                if (append.endOfPaginationReached) {
                    Timber.d("Pagination reached to the end of page")
                }
            }
        }
    }
}
