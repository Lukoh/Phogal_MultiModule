package com.goforer.phogal.presentation.stateholder.uistate.home.common.user

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
import androidx.compose.ui.ExperimentalComposeUiApi
import com.goforer.phogal.presentation.stateholder.uistate.BaseUiState
import com.goforer.phogal.presentation.stateholder.uistate.rememberBaseUiState
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Stable
class UserInfoUiState internal constructor(
    val baseUiState: BaseUiState,
    val scope: CoroutineScope,
    val bottomSheetState: SheetState,

    private val _openBottomSheet: MutableState<Boolean>
) {
    val openBottomSheet: Boolean get() = _openBottomSheet.value
    fun setOpenBottomSheet(openBottomSheet: Boolean) {
        _openBottomSheet.value = openBottomSheet
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun rememberUserInfoUiState(
    baseUiState: BaseUiState = rememberBaseUiState(),
    scope: CoroutineScope = rememberCoroutineScope(),
    bottomSheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
    openBottomSheet: MutableState<Boolean> = remember { mutableStateOf(false) }
): UserInfoUiState = remember(baseUiState, bottomSheetState, scope, openBottomSheet) {
        UserInfoUiState(
            baseUiState = baseUiState,
            scope = scope,
            bottomSheetState = bottomSheetState,
            _openBottomSheet = openBottomSheet
        )
    }