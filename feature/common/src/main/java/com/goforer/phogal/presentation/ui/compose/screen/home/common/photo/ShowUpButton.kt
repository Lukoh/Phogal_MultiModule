package com.goforer.phogal.presentation.ui.compose.screen.home.common.photo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.goforer.designsystem.theme.GreenGray60

@Composable
fun ShowUpButton(modifier: Modifier, visible: Boolean, onClick: () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier
    ) {
        FloatingActionButton(
            modifier = modifier,
            containerColor = GreenGray60,
            onClick = onClick
        ) {
            Text("Up!")
        }
    }
}