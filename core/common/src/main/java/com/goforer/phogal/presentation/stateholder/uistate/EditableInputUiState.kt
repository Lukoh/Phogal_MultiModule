package com.goforer.phogal.presentation.stateholder.uistate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@Stable
class EditableInputUiState(private val hint: String, initialText: String) {
    var textState by mutableStateOf(initialText)

    val isHint: Boolean
        get() = textState == hint

    companion object {
        val Saver: Saver<EditableInputUiState, *> = listSaver(
            save = { listOf(it.hint, it.textState) },
            restore = {
                EditableInputUiState(
                    hint = it[0],
                    initialText = it[1],
                )
            }
        )
    }
}

@Composable
fun rememberEditableInputState(hint: String): EditableInputUiState =
    rememberSaveable(hint, saver = EditableInputUiState.Saver) {
        EditableInputUiState(hint, hint)
    }