package com.goforer.designsystem.component.dialog

import android.R
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.goforer.designsystem.theme.PhogalTheme

@Composable
fun AlertDialogContent(
    title: String,
    message: String,
    confirmText: String,
    dismissText: String?,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            if (dismissText == null) {
                TextButton(
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Text(
                        text = confirmText,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp
                        )
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    TextButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text(
                            text = dismissText,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp
                            )
                        )
                    }

                    VerticalDivider(
                        modifier = Modifier.fillMaxHeight(),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    TextButton(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text(
                            text = confirmText,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 16.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AlertDialog(
    title: String,
    message: String,
    confirmText: String = stringResource(id = R.string.ok),
    dismissText: String? = null,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit = onDismissRequest
) {
    Dialog(onDismissRequest = onDismissRequest) {
        AlertDialogContent(
            title = title,
            message = message,
            confirmText = confirmText,
            dismissText = dismissText,
            onDismissRequest = onDismissRequest,
            onConfirm = onConfirm
        )
    }
}

@Preview(showSystemUi = true, name = "BaseAlertDialog")
@Composable
fun AlertDialogDefaultPreview() {
    PhogalTheme {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            AlertDialogContent(
                title = "Download complete",
                message = "The photo has been saved to your gallery successfully.",
                confirmText = "OK",
                dismissText = null,
                onDismissRequest = { },
                onConfirm = { }
            )
        }
    }
}

@Preview(showSystemUi = true, name = "TwoButtonsAlertDialog")
@Composable
fun AlertDialogTwoButtonsPreview() {
    PhogalTheme {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            AlertDialogContent(
                title = "Delete Photo",
                message = "Are you sure you want to delete this photo from your gallery? This action cannot be undone.",
                confirmText = "Delete",
                dismissText = "Cancel",
                onDismissRequest = { },
                onConfirm = { }
            )
        }
    }
}
