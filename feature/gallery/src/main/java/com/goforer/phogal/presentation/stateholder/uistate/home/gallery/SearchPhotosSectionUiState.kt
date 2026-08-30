package com.goforer.phogal.presentation.stateholder.uistate.home.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.CoroutineScope

@Stable
class SearchPhotosSectionUiState internal constructor(
    val scope: CoroutineScope,

    private val _loadingDone: MutableState<Boolean>
) {
    val loadingDone: Boolean get() = _loadingDone.value
    fun setLoadingDone() { _loadingDone.value = true }
    fun setLoadingStarted() { _loadingDone.value = false }
}

@Composable
fun rememberSearchPhotosSectionUiState(
    scope: CoroutineScope = rememberCoroutineScope(),
    loadingDone: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) }
): SearchPhotosSectionUiState = remember(scope, loadingDone) {
        SearchPhotosSectionUiState(
            scope = scope, _loadingDone = loadingDone
        )
    }
