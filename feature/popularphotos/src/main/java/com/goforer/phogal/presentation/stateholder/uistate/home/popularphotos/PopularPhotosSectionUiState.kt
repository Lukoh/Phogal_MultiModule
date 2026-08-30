package com.goforer.phogal.presentation.stateholder.uistate.home.popularphotos

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.CoroutineScope

/**
 * UI-local state holder for the popular photos screen's scrollable section.
 *
 * ### Hoisting refactor (April 2026)
 *
 * Previously this holder exposed both flags as public `MutableState<Boolean>`,
 * which let any consumer write `state.clickedState.value = true` from anywhere
 * in the composable tree. The new shape exposes **read-only `Boolean`** values
 * and **typed callbacks** for the two transitions that actually happen:
 *
 *  - `onUpButtonClicked()`             — user tapped the "scroll-to-top" FAB
 *  - `onUpButtonVisibilityChanged(visible)` — visibility derived from scroll state
 *  - `onScrollConsumed()`              — clear the "click consumed" flag after scrollToTop animation
 *
 * Read-only properties (`clicked`, `visibleUpButton`) are stable from
 * Compose's perspective: the value flips, the holder identity does not, and
 * children that consume only one of them recompose only when that one
 * actually changes.
 */
@Stable
class PopularPhotosSectionUiState internal constructor(
    val scope: CoroutineScope,

    private val _loadingDone: MutableState<Boolean>
) {
    val loadingDone: Boolean get() = _loadingDone.value
    fun setLoadingDone() { _loadingDone.value = true }
    fun setLoadingStarted() { _loadingDone.value = false }
}

@Composable
fun rememberPopularPhotosSectionUiState(
    scope: CoroutineScope = rememberCoroutineScope(),
    loadingDone: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) }
): PopularPhotosSectionUiState = remember(scope, loadingDone) {
    PopularPhotosSectionUiState(
        scope = scope, _loadingDone = loadingDone
    )
}
