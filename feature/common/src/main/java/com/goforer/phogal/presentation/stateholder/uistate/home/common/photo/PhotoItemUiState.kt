package com.goforer.phogal.presentation.stateholder.uistate.home.common.photo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User

@Stable
data class PhotoItemCallbacks(
    val onFollowClick: (User) -> Unit,
    val onShowUserInfo: (User) -> Unit,
    val onItemClicked: (item: Photo, index: Int) -> Unit,
    val onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit
)

@Stable
class PhotoItemUiState(
    val index: Int = 0,
    val photo: Photo = Photo.empty(),
    initialVisibleViewButton: Boolean = false,
    initialClicked: Boolean = false,
    initialBookmarked: Boolean = false
) {
    var visibleViewButton: Boolean by mutableStateOf(initialVisibleViewButton)
    var clicked: Boolean by mutableStateOf(initialClicked)
    var bookmarked: Boolean by mutableStateOf(initialBookmarked)
    companion object {
        fun Saver(photo: Photo): Saver<PhotoItemUiState, *> = listSaver(
            save = {
                listOf(it.index, it.visibleViewButton, it.clicked, it.bookmarked)
            },
            restore = {
                PhotoItemUiState(
                    index = it[0] as Int,
                    photo = photo,
                    initialVisibleViewButton = it[1] as Boolean,
                    initialClicked = it[2] as Boolean,
                    initialBookmarked = it[3] as Boolean
                )
            }
        )
    }
}

@Composable
fun rememberPhotoItemUiState(
    index: Int = 0,
    photo: Photo = Photo.empty(),
    initialVisibleViewButton: Boolean = false,
    initialClicked: Boolean = false,
    initialBookmarked: Boolean = false
): PhotoItemUiState {
    return rememberSaveable(
        photo,
        saver = PhotoItemUiState.Saver(photo)
    ) {
        PhotoItemUiState(
            index = index,
            photo = photo,
            initialVisibleViewButton = initialVisibleViewButton,
            initialClicked = initialClicked,
            initialBookmarked = initialBookmarked
        )
    }
}