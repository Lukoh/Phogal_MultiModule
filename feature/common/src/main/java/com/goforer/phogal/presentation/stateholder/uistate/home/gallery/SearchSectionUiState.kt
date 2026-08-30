package com.goforer.phogal.presentation.stateholder.uistate.home.gallery

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import com.goforer.phogal.presentation.stateholder.uistate.EditableInputUiState
import com.goforer.phogal.presentation.stateholder.uistate.rememberEditableInputState

@Stable
class SearchSectionUiState internal constructor(
    val editableInputState: EditableInputUiState,
    val interactionSource: MutableInteractionSource,

    private val _wordChanged: MutableState<Boolean>,
    private val _enabled: MutableState<Boolean>
) {
    val wordChanged: Boolean get() = _wordChanged.value
    val enabled: Boolean get() = _enabled.value

    fun setWordChanged(wordChanged: Boolean) {
        _wordChanged.value = wordChanged
    }

    fun setEnabled(enabled: Boolean) {
        _enabled.value = enabled
    }
}

@Composable
fun rememberSearchSectionUiState(
    editableInputState: EditableInputUiState = rememberEditableInputState(hint = "Search"),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    wordChanged: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
    enabled: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) }
): SearchSectionUiState = remember(editableInputState, interactionSource, wordChanged, enabled) {
        SearchSectionUiState(
            editableInputState = editableInputState,
            interactionSource = interactionSource,
            _wordChanged = wordChanged,
            _enabled = enabled
        )
    }