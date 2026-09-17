package com.goforer.phogal.presentation.stateholder.uistate.home.gallery

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.goforer.phogal.presentation.stateholder.uistate.EditableInputUiState
import com.goforer.phogal.presentation.stateholder.uistate.rememberEditableInputState

@Stable
class SearchSectionUiState(
    val editableInputState: EditableInputUiState,
    val interactionSource: MutableInteractionSource,
    initialWordChanged: Boolean,
    initialEnabled: Boolean
) {
    var wordChanged: Boolean by mutableStateOf(initialWordChanged)
    var enabled: Boolean by mutableStateOf(initialEnabled)

    companion object {
        fun Saver(
            editableInputState: EditableInputUiState,
            interactionSource: MutableInteractionSource
        ): Saver<SearchSectionUiState, *> = listSaver(
            save = { listOf(it.wordChanged, it.enabled) },
            restore = {
                SearchSectionUiState(
                    editableInputState = editableInputState,
                    interactionSource = interactionSource,
                    initialWordChanged = it[0] ,
                    initialEnabled = it[1]
                )
            }
        )
    }
}

@Composable
fun rememberSearchSectionUiState(
    editableInputState: EditableInputUiState = rememberEditableInputState(hint = "Search"),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    initialWordChanged: Boolean = false,
    initialEnabled: Boolean = false
): SearchSectionUiState = rememberSaveable(
    editableInputState, interactionSource,
    saver = SearchSectionUiState.Saver(editableInputState, interactionSource)
) {
    SearchSectionUiState(
        editableInputState = editableInputState,
        interactionSource = interactionSource,
        initialWordChanged = initialWordChanged,
        initialEnabled = initialEnabled
    )
}
