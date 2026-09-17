package com.goforer.phogal.presentation.stateholder.uistate.home.common.photo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture

@Stable
data class PictureItemCallbacks(
    val onFollowClick: (User) -> Unit,
    val onShowUserInfo: (User) -> Unit,
    val onItemClicked: (item: Picture, index: Int) -> Unit,
    val onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit
)

@Stable
class PictureItemUiState(
    val index: Int = 0,
    val picture: Picture = Picture.empty(),
    val visibleViewButton: Boolean = false,
    initialClicked: Boolean = false,
) {
    var clicked: Boolean by mutableStateOf(initialClicked)
    companion object {
        fun Saver(picture: Picture): Saver<PictureItemUiState, *> = listSaver(
            save = {
                listOf(it.index, it.visibleViewButton, it.clicked)
            },
            restore = {
                PictureItemUiState(
                    index = it[0] as Int,
                    picture = picture,
                    visibleViewButton = it[1] as Boolean,
                    initialClicked = it[2] as Boolean
                )
            }
        )
    }
}

@Composable
fun rememberPictureItemUiState(
    index: Int = 0,
    picture: Picture = Picture.empty(),
    initialVisibleViewButton: Boolean = false,
    initialClicked: Boolean = false
): PictureItemUiState {
    return rememberSaveable(
        picture,
        saver = PictureItemUiState.Saver(picture)
    ) {
        PictureItemUiState(
            index = index,
            picture = picture,
            visibleViewButton = initialVisibleViewButton,
            initialClicked = initialClicked
        )
    }
}