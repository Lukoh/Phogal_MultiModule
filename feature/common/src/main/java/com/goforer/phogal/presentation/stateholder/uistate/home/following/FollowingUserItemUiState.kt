package com.goforer.phogal.presentation.stateholder.uistate.home.following

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User

@Stable
data class FollowingUserItemCallbacks(
    val onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    val onOpenWebView: (firstName: String, url: String?) -> Unit,
    val onFollow: (User) -> Unit
)

@Stable
class FollowingUserItemUiState(
    val index: Int,
    initialUser: String,
    initialVisibleViewButton: Boolean,
    initialClicked: Boolean,
    initialFollowed: Boolean
) {
    var user: String by mutableStateOf(initialUser)
    var visibleViewButton: Boolean by mutableStateOf(initialVisibleViewButton)
    var clicked: Boolean by mutableStateOf(initialClicked)
    var followed: Boolean by mutableStateOf(initialFollowed)

    companion object {
        fun Saver(userData: String): Saver<FollowingUserItemUiState, *> = listSaver(
            save = {
                listOf(it.index, it.visibleViewButton, it.clicked, it.followed)
            },
            restore = {
                FollowingUserItemUiState(
                    index = it[0] as Int,
                    initialUser = userData,
                    initialVisibleViewButton = it[1] as Boolean,
                    initialClicked = it[2] as Boolean,
                    initialFollowed = it[3] as Boolean
                )
            }
        )
    }
}

@Composable
fun rememberFollowingUserItemUiState(
    userData: String = "",
    index: Int = 0,
    initialVisibleViewButton: Boolean = false,
    initialClicked: Boolean = false,
    initialFollowed: Boolean = false
): FollowingUserItemUiState {
    return rememberSaveable(
        userData,
        saver = FollowingUserItemUiState.Saver(userData)
    ) {
        FollowingUserItemUiState(
            index = index,
            initialUser = userData,
            initialVisibleViewButton = initialVisibleViewButton,
            initialClicked = initialClicked,
            initialFollowed = initialFollowed
        )
    }
}
