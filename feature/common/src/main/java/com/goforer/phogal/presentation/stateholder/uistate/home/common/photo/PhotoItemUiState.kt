package com.goforer.phogal.presentation.stateholder.uistate.home.common.photo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo

@Stable
class PhotoItemUiState internal constructor(
    private val _index: MutableState<Int>,
    private val _photo: MutableState<Photo>,
    private val _visibleViewButton: MutableState<Boolean>,
    private val _clicked: MutableState<Boolean>,
    private val _bookmarked: MutableState<Boolean>
) {
    val index: Int get() = _index.value
    val photo: Photo get() = _photo.value
    val visibleViewButton: Boolean get() = _visibleViewButton.value
    val clicked: Boolean get() = _clicked.value
    val bookmarked: Boolean get() = _bookmarked.value

    fun setIndex(index: Int) {
        _index.value = index
    }

    fun setPhoto(photo: Photo) {
        _photo.value = photo
    }

    fun setClicked(clicked: Boolean) {
        _clicked.value = clicked
    }

    fun setVisibleViewButton(visibleViewButton: Boolean) {
        _visibleViewButton.value = visibleViewButton
    }

    fun setBookmark(bookmarked: Boolean) {
        _bookmarked.value = bookmarked
    }
}

@Composable
fun rememberPhotoItemUiState(
    index: MutableState<Int> = rememberSaveable { mutableIntStateOf(0) },
    photo: MutableState<Photo> = remember { mutableStateOf(Photo.empty()) },
    visibleViewButton: MutableState<Boolean> = rememberSaveable() { mutableStateOf(false) },
    clicked: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
    bookmarked: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) }
): PhotoItemUiState = remember(index, photo, visibleViewButton, clicked, bookmarked) {
        PhotoItemUiState(
            _index = index,
            _photo = photo,
            _visibleViewButton = visibleViewButton,
            _clicked = clicked,
            _bookmarked = bookmarked
        )
    }