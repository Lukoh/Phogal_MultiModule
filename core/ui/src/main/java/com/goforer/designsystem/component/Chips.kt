package com.goforer.designsystem.component

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.goforer.designsystem.theme.PhogalTheme

@Composable
fun Chips(
    modifier: Modifier = Modifier,
    items: List<String>,
    onClicked: (String) -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(items.size) {
        if (items.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    val isDark = isSystemInDarkTheme()
    val skyBlueContainer = if (isDark) {
        Color(0xFF2C3E50).copy(alpha = 0.25f)
    } else {
        Color(0xFFE0E8F5).copy(alpha = 0.4f)
    }

    val skyBlueBorder = if (isDark) {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    } else {
        Color(0xFFBACBE3).copy(alpha = 0.6f)
    }

    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        items(
            items = items,
            key = { item -> item }
        ) { item ->
            AssistChip(
                onClick = { onClicked(item) },
                label = {
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = skyBlueContainer,
                    labelColor = MaterialTheme.colorScheme.onSurface,
                    leadingIconContentColor = MaterialTheme.colorScheme.primary
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = skyBlueBorder
                )
            )
        }
    }
}

@Preview(
    name = "1. Light Mode - Chips",
    showBackground = true,
    device = "spec:width=360dp,height=80dp,dpi=420"
)
@Preview(
    name = "2. Dark Mode - Chips",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    device = "spec:width=360dp,height=80dp,dpi=420"
)
@Composable
fun ChipsPreview(modifier: Modifier = Modifier) {
    val mockItems = listOf("Mountain", "Train", "Seoul", "San Diego", "Sea", "Cook")

    PhogalTheme {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Chips(
                    items = mockItems,
                    onClicked = { }
                )
            }
        }
    }
}