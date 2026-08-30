package com.goforer.phogal.presentation.stateholder.uistate.home.setting.bookmark

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable

@Stable
class BookmarkSectionUiState internal constructor(
    private val _clicked: MutableState<Boolean>,
    private val _visibleUpButton: MutableState<Boolean>,
    private val _loadingDone: MutableState<Boolean>
) {
    val clicked: Boolean get() = _clicked.value
    val visibleUpButton: Boolean get() = _visibleUpButton.value
    val loadingDone: Boolean get() = _loadingDone.value

    fun setUpButtonClicked() { _clicked.value = true }
    fun setScrollConsumed() { _clicked.value = false }
    fun setUpButtonVisibilityChanged(visible: Boolean) { _visibleUpButton.value = visible }
    fun setLoadingDone() { _loadingDone.value = true }
    fun setLoadingStarted() { _loadingDone.value = false }
}

@Composable
fun rememberBookmarkSectionUiState(
    clicked: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
    visibleUpButton: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
    loadingDone: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) }
): BookmarkSectionUiState =remember(clicked, visibleUpButton, loadingDone) {
        BookmarkSectionUiState(
            _clicked = clicked, _visibleUpButton = visibleUpButton, _loadingDone = loadingDone
        )
    }
