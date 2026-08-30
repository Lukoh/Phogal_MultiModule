package com.goforer.designsystem.component.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.goforer.designsystem.theme.Blue20
import com.goforer.designsystem.theme.Blue75
import com.goforer.designsystem.theme.DarkGreen20

@Composable
fun AlertDialog(
    title: String,
    message: String,
    confirmText: String = stringResource(id = android.R.string.ok),
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit = onDismissRequest
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Blue20
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = DarkGreen20
                )
                Spacer(modifier = Modifier.height(20.dp))
                TextButton(
                    onClick = onConfirm,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = confirmText,
                        color = Blue75,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Preview(showSystemUi = true, name = "BaseAlertDialog")
@Composable
fun AlertDialogDefaultPreview() {
    MaterialTheme {
        AlertDialog(
            title = "Download complete",
            message = "The photo has been saved to your gallery successfully.",
            confirmText = "OK",
            onDismissRequest = { },
            onConfirm = { }
        )
    }
}

@Preview(showSystemUi = true, name = "NetworkAlertDialog")
@Composable
fun AlertDialogLongMessagePreview() {
    MaterialTheme {
        AlertDialog(
            title = "Network error",
            message = "Network is unstable and we couldn't load the data. Please try again in a moment. If the problem persists, please contact customer support.",
            confirmText = "Retry",
            onDismissRequest = {},
            onConfirm = {}
        )
    }
}