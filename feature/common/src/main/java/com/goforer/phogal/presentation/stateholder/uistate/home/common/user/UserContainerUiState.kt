package com.goforer.phogal.presentation.stateholder.uistate.home.common.user

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import com.goforer.phogal.presentation.stateholder.uistate.BaseUiState
import com.goforer.phogal.presentation.stateholder.uistate.rememberBaseUiState
import kotlin.collections.emptyList

@Stable
class UserContainerUiState internal constructor(
    val baseUiState: BaseUiState,

    private val _user: MutableState<String>,
    private val _profileSize: MutableState<Double>,
    private val _colors: MutableState<List<Color>>,
    private val _visibleViewButton: MutableState<Boolean>,
    private val _fromItem: MutableState<Boolean>,
) {
    val user: String get() = _user.value
    val profileSize: Double get() = _profileSize.value
    val colors: List<Color> get() = _colors.value
    val visibleViewButton get() = _visibleViewButton.value
    val fromItem get() = _fromItem.value

    fun setUser(user: String) {
        _user.value = user
    }

    fun setProfileSize(profileSize: Double) {
        _profileSize.value = profileSize
    }

    fun setColors(colors: List<Color>) {
        _colors.value = colors
    }

    fun setVisibleViewButton(visibleViewButton: Boolean) {
        _visibleViewButton.value = visibleViewButton
    }

    fun setFromItem(fromItem: Boolean) {
        _fromItem.value = fromItem
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun rememberUserContainerUiState(
    baseUiState: BaseUiState = rememberBaseUiState(),
    user: MutableState<String> = rememberSaveable { mutableStateOf("") },
    profileSize: MutableState<Double> = rememberSaveable { mutableStateOf(0.toDouble()) },
    colors: MutableState<List<Color>> = rememberSaveable { mutableStateOf(emptyList()) },
    visibleViewButton: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
    fromItem: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) }
): UserContainerUiState = remember(user, profileSize, colors, visibleViewButton, fromItem) {
        UserContainerUiState(
            baseUiState = baseUiState,
            _user = user,
            _profileSize = profileSize,
            _colors = colors,
            _visibleViewButton = visibleViewButton,
            _fromItem = fromItem
        )
    }