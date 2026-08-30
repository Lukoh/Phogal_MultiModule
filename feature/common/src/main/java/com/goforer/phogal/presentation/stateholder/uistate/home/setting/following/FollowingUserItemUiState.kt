package com.goforer.phogal.presentation.stateholder.uistate.home.setting.following

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable

@Stable
class FollowingUserItemUiState internal constructor(
    private val _index: MutableState<Int>,
    private val _user: MutableState<String>,
    private val _visibleViewButton: MutableState<Boolean>,
    private val _clicked: MutableState<Boolean>,
    private val _followed: MutableState<Boolean>
) {
    val index: Int get() = _index.value
    val user: String get() = _user.value
    val visibleViewButton: Boolean get() = _visibleViewButton.value
    val clicked: Boolean get() = _clicked.value
    val followed: Boolean get() = _followed.value
}

@Composable
fun rememberFollowingUserItemUiState(
    index: MutableState<Int> = rememberSaveable { mutableIntStateOf(0) },
    user: MutableState<String> = remember { mutableStateOf("") },
    visibleViewButton: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
    clicked: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
    followed: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) }
): FollowingUserItemUiState = remember(index, user, visibleViewButton, clicked) {
    FollowingUserItemUiState(
        _index = index,
        _user = user,
        _visibleViewButton = visibleViewButton,
        _clicked = clicked,
        _followed = followed
    )
}