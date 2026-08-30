package com.goforer.phogal.presentation.stateholder.uistate.home.common.photo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture

@Stable
class PictureItemUiState(
    private val _index: MutableState<Int>,
    private val _picture: MutableState<Picture>,
    private val _visibleViewButton: MutableState<Boolean>,
    private val _clicked: MutableState<Boolean>,
) {
    val index: Int get() = _index.value
    val picture: Picture get() = _picture.value
    val visibleViewButton: Boolean get() = _visibleViewButton.value
    val clicked: Boolean get() = _clicked.value

    fun setIndex(index: Int) {
        _index.value = index
    }

    fun setPicture(picture: Picture) {
        _picture?.value = picture
    }

    fun setClicked(clicked: Boolean) {
        _clicked.value = clicked
    }

    fun setVisibleViewButton(visibleViewButton: Boolean) {
        _visibleViewButton.value = visibleViewButton
    }
}

@Composable
fun rememberPictureItemUiState(
    index: MutableState<Int> = rememberSaveable { mutableIntStateOf(0) },
    picture: MutableState<Picture> = remember { mutableStateOf(Picture.empty()) },
    visibleViewButton: MutableState<Boolean> = rememberSaveable() { mutableStateOf(false) },
    clicked: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) }
): PictureItemUiState = remember(index, picture, visibleViewButton, clicked) {
    PictureItemUiState(
        _index = index,
        _picture = picture,
        _visibleViewButton = visibleViewButton,
        _clicked = clicked
    )
}