package com.goforer.phogal.presentation.ui.compose.screen.home.gallery

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.goforer.phogal.core.ui.R
import com.goforer.phogal.presentation.stateholder.uistate.home.gallery.SearchSectionUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.gallery.rememberSearchSectionUiState
import com.goforer.phogal.presentation.stateholder.uistate.rememberEditableInputState
import com.goforer.designsystem.theme.PhogalTheme

@Composable
fun SearchSection(
    modifier: Modifier = Modifier,
    sectionUiState: SearchSectionUiState = rememberSearchSectionUiState(),
    onSearched: (word: String) -> Unit,
) {
    val isFocused by sectionUiState.interactionSource.collectIsFocusedAsState()
    val borderColor = if (isFocused) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    val borderWidth = if (isFocused) 1.5.dp else 1.dp

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .zIndex(1f)
    ) {
        TextField(
            value = if (sectionUiState.editableInputState.isHint) "" else sectionUiState.editableInputState.textState,
            onValueChange = {
                if (!it.contains("\n")) {
                    sectionUiState.setWordChanged(true)
                    sectionUiState.editableInputState.textState = it
                }
            },
            enabled = sectionUiState.enabled,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            colors = TextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            singleLine = true,
            placeholder = {
                Text(
                    text = stringResource(R.string.placeholder_search),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            },
            textStyle = TextStyle(
                fontSize = 16.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    if (sectionUiState.wordChanged) {
                        onSearched(sectionUiState.editableInputState.textState)
                    }
                }
            ),
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )

        val onClick = remember(sectionUiState) {
            {
                if (sectionUiState.wordChanged) {
                    onSearched(sectionUiState.editableInputState.textState)
                }
            }
        }

        TextButton(
            onClick = onClick,
            enabled = sectionUiState.wordChanged,
            modifier = Modifier
                .padding(end = 8.dp)
                .fillMaxHeight(),
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = stringResource(id = R.string.text_search),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Preview(
    name = "1. Light Mode",
    showBackground = true,
    device = "spec:width=360dp,height=120dp,dpi=420"
)
@Preview(
    name = "2. Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    device = "spec:width=360dp,height=120dp,dpi=420"
)
@Composable
fun SearchSectionPreview() {
    PhogalTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            val mockInteractionSource = remember { MutableInteractionSource() }
            val mockEditableInputState = rememberEditableInputState("")
            val mockSectionUiState = rememberSearchSectionUiState(
                interactionSource = mockInteractionSource,
                editableInputState = mockEditableInputState
            ).apply {
                setWordChanged(true)
                editableInputState.textState = "Android Compose"
            }

            Box(modifier = Modifier.padding(16.dp)) {
                SearchSection(
                    sectionUiState = mockSectionUiState,
                    onSearched = {}
                )
            }
        }
    }
}