package com.goforer.phogal.presentation.ui.compose.screen.home.gallery

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.goforer.phogal.core.ui.R
import com.goforer.designsystem.theme.Blue20
import com.goforer.designsystem.theme.ColorSystemGray8
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionBottomSheet(
    rationaleText: String,
    onDismissedRequest: () -> Unit,
    onClicked: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
) {
    val scope = rememberCoroutineScope()

    // Splits the rationale text the same way the original did. Defensive —
    // if "Setting" isn't present, fall back to showing the whole text in the
    // "muted" style rather than crashing.
    val splitIndex = rationaleText.indexOf("Setting").coerceAtLeast(0)
    val highlightPart = rationaleText.substring(0, splitIndex)
    val mutedPart = rationaleText.substring(splitIndex)

    ModalBottomSheet(
        onDismissRequest = {
            scope.launch {
                sheetState.hide()
            }.invokeOnCompletion {
                if (!sheetState.isVisible) onDismissedRequest()
            }
        },
        sheetState = sheetState,
    ) {
        // Note: SearchSection text input is now hoisted into rememberSearchSectionUiState
        // alongside the screen, so the chip-tap path goes through the same channel as
        // typed input. This collapses two state mutation paths into one.
        val onClick: () -> Unit = remember(sheetState, scope, onClicked) {
            {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) onClicked()
                }
            }
        }

        Column(
            modifier = Modifier.wrapContentHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = ParagraphStyle(
                            lineHeight = 30.sp,
                            textAlign = TextAlign.Center
                        )
                    ) {
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = Blue20,
                                baselineShift = BaselineShift.Superscript
                            )
                        ) { append(highlightPart) }

                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                color = ColorSystemGray8
                            )
                        ) { append(mutedPart) }
                    }
                },
                modifier = Modifier.padding(16.dp),
                fontFamily = FontFamily.SansSerif
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onClick
            ) {
                Text(text = stringResource(id = R.string.permission_request))
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
