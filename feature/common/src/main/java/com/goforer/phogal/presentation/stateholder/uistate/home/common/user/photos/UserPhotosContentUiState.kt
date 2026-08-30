package com.goforer.phogal.presentation.stateholder.uistate.home.common.user.photos

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture
import com.goforer.phogal.presentation.stateholder.business.home.common.user.UserPhotosViewModel
import com.goforer.phogal.presentation.stateholder.uistate.BaseUiState
import com.goforer.phogal.presentation.stateholder.uistate.rememberBaseUiState

@Stable
class UserPhotosContentUiState internal constructor(
    val baseUiState: BaseUiState,
    val userPhotosViewModel: UserPhotosViewModel,
    val photos: LazyPagingItems<Photo>,

    private val _name: MutableState<String>,
    private val _firstName: MutableState<String>,
    private val _visibleActions: MutableState<Boolean>
) {
    val name: String get() = _name.value
    val firstName: String get() = _firstName.value
    val visibleActions: Boolean get() = _visibleActions.value

    fun setName(name: String) {
        _name.value = name
    }

    fun setFirstName(firstName: String) {
        _firstName.value = firstName
    }

    fun setVisibleAction(visibleActions: Boolean) {
        _visibleActions.value = visibleActions
    }
}

@Composable
fun rememberUserPhotosContentUiState(
    userPhotosViewModel: UserPhotosViewModel,
    baseUiState: BaseUiState = rememberBaseUiState(),
    name: MutableState<String> = rememberSaveable { mutableStateOf("") },
    firstName: MutableState<String> = rememberSaveable { mutableStateOf("") },
    visibleActions: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) }
): UserPhotosContentUiState  {
    val photos = userPhotosViewModel.photos.collectAsLazyPagingItems()

    return remember(
        baseUiState, userPhotosViewModel, name, firstName, visibleActions
    ) {
        UserPhotosContentUiState(
            baseUiState = baseUiState,
            userPhotosViewModel = userPhotosViewModel,
            photos = photos,
            _name = name,
            _firstName = firstName,
            _visibleActions = visibleActions
        )
    }
}
