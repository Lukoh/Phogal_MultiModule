package com.goforer.phogal.presentation.stateholder.uistate.home.gallery

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Stable
class PermissionState internal constructor(
    val scope: CoroutineScope,
    val bottomSheetState: SheetState,

    private val _openBottomSheet: MutableState<Boolean>,
    private val _rationaleText: MutableState<String>
) {
    val openBottomSheet: Boolean get() = _openBottomSheet.value
    val rationaleText: String get() = _rationaleText.value

    fun setOpenBottomSheet(openBottomSheet: Boolean) {
        _openBottomSheet.value = openBottomSheet
    }

    fun setRationaleText(rationaleText: String) {
        _rationaleText.value = rationaleText
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberPermissionState(
    openBottomSheet: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
    skipPartiallyExpanded: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
    scope: CoroutineScope = rememberCoroutineScope(),
    bottomSheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
    rationaleText: MutableState<String> = rememberSaveable { mutableStateOf("") }
): PermissionState = remember(openBottomSheet, skipPartiallyExpanded, scope, bottomSheetState, rationaleText) {
    PermissionState(
        _openBottomSheet = openBottomSheet,
        scope = scope,
        bottomSheetState = bottomSheetState,
        _rationaleText = rationaleText
    )
}