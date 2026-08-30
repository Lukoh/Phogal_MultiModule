package com.goforer.phogal.presentation.ui.compose.screen.home.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.goforer.phogal.core.ui.R
import com.goforer.designsystem.theme.ColorSystemGray7

@Composable
fun EmptyState(
    modifier: Modifier = Modifier,
    text: String = stringResource(id = R.string.no_picture)
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(320.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(color = ColorSystemGray7),
            modifier = Modifier.align(Alignment.Center),
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium
        )
    }
}