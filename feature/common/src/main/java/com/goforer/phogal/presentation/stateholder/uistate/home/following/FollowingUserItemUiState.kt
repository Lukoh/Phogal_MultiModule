package com.goforer.phogal.presentation.stateholder.uistate.home.following

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User

@Stable
data class FollowingUserItemActions(
    val onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    val onOpenWebView: (firstName: String, url: String?) -> Unit,
    val onFollow: (User) -> Unit
)

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