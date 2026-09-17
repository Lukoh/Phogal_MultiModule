package com.goforer.phogal.presentation.stateholder.uistate.home.common.base

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Common UI state for home sections.
 *
 * Shared state and behavior are centralized here so concrete SectionUiState
 * classes only keep section-specific dependencies.
 */
@Stable
abstract class BaseSectionUiState(
    open val scope: CoroutineScope,
    open val lazyListState: LazyListState,
    initialClicked: Boolean = false,
    initialVisibleUpButton: Boolean = false,
    initialLoadingDone: Boolean = false,
    initialManualRefreshing: Boolean = false,
    initialIsResettingScroll: Boolean = false,
) {
    var clicked: Boolean by mutableStateOf(initialClicked)
    var visibleUpButton: Boolean by mutableStateOf(initialVisibleUpButton)
    var loadingDone: Boolean by mutableStateOf(initialLoadingDone)
    var manualRefreshing: Boolean by mutableStateOf(initialManualRefreshing)
    var isResettingScroll: Boolean by mutableStateOf(initialIsResettingScroll)

    fun scrollToTop() {
        scope.launch {
            lazyListState.animateScrollToTop()
        }
    }

    private suspend fun LazyListState.animateScrollToTop() {
        animateScrollToItem(0)
    }

    /**
     * Resets the scroll position to the top if a reset is pending and there are items.
     *
     * @param itemCount The current number of items in the list.
     * @param force If true, resets the scroll regardless of the current state.
     */
    suspend fun resetScrollIfNeeded(itemCount: Int, force: Boolean = false) {
        if ((!isResettingScroll && !force) || itemCount == 0) {
            return
        }

        lazyListState.scrollToItem(0)
        isResettingScroll = false
    }
}
