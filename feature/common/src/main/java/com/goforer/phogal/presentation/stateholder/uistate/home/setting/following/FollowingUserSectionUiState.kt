package com.goforer.phogal.presentation.stateholder.uistate.home.setting.following

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable

class FollowingUserSectionUiState internal constructor(
    private val _clicked: MutableState<Boolean>,
    private val _visibleUpButton: MutableState<Boolean>,
    private val _loadingDone: MutableState<Boolean>
) {
    val clicked: Boolean get() = _clicked.value
    val visibleUpButton: Boolean get() = _visibleUpButton.value
    val loadingDone: Boolean get() = _loadingDone.value

    fun setClicked(clicked: Boolean) {
        _clicked.value = clicked
    }

    fun setVisibleUpButton(visibleUpButton: Boolean) {
        _visibleUpButton.value = visibleUpButton
    }

    fun setLoadingDone() { _loadingDone.value = true }
    fun setLoadingStarted() { _loadingDone.value = false }
}

@Composable
fun rememberFollowingUserSectionUiState(
    clicked: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
    visibleUpButton: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
    loadingDone: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) }
): FollowingUserSectionUiState = remember(clicked, visibleUpButton, loadingDone) {
    FollowingUserSectionUiState(
        _clicked = clicked,
        _visibleUpButton = visibleUpButton,
        _loadingDone = loadingDone
    )
}