package com.goforer.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
fun ScaffoldContent(
    topInterval: Dp,
    content: @Composable (PaddingValues) -> Unit
) {
    Column {
        Spacer(modifier = Modifier.height(topInterval))
        content(PaddingValues())
    }
}

