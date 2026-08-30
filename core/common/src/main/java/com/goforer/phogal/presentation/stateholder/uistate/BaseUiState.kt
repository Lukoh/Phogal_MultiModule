package com.goforer.phogal.presentation.stateholder.uistate

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalComposeUiApi::class)
@Stable
class BaseUiState(
    val context: Context,
    val scope: CoroutineScope,
    val lifecycle: Lifecycle,
    val keyboardController: SoftwareKeyboardController?,

    private val isOnline: MutableState<Boolean>
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun rememberBaseUiState(
    context: Context = LocalContext.current,
    scope: CoroutineScope = rememberCoroutineScope(),
    lifecycle: Lifecycle = LocalLifecycleOwner.current.lifecycle,
    keyboardController: SoftwareKeyboardController? = LocalSoftwareKeyboardController.current,
    isOnline: MutableState<Boolean> = remember { mutableStateOf(true) }
): BaseUiState = remember(context, scope, lifecycle, keyboardController) {
    BaseUiState(
        context = context,
        scope = scope,
        lifecycle = lifecycle,
        keyboardController = keyboardController,
        isOnline = isOnline
    )
}